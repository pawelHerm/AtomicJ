/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2018 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.readers.csinstruments;

import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardUnitType;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.CalibrationState;
import atomicJ.utilities.FileInputUtilities;

public class CSISpectroscopyData
{
    private static final String TIP_LEVER_SENSITIVITY = "TipLeverSensitivity";
    private static final String TIP_LEVER_SPRING_CONSTANT = "TipLeverSpringConstant";
    private static final String SWEEP_FROM_VALUE = "SweepFromValue";
    private static final String SWEEP_TO_VALUE = "SweepToValue";
    private static final String SWEEP_SIGNAL_UNIT_NAME = "SweepSignalUnitName";
    private static final String SWEEP_SIGNAL_UNIT_SYMBOL = "SweepSignalUnitSymbol";
    private static final String SWEEP_MODE = "SweepMode";
    private static final String RESOLUTION = "Resolution";
    private static final String RESOLUTION_BACK = "ResolBack";
    private static final String IMAGING_POSITION = "ImagingPos";

    private static final String SPECTROSCOPY_PARAMETERS = "//Spectroscopy/SpectroParameters";
    private static final String SPECTROSCOPY_DATA = "//Spectroscopy/SpectroData";
    private static final String NAN = "NaN";

    private static final String CHANNEL_DATA_TAG = "ChannelData";
    private static final String PASS_DATA_TAG = "PassData";    

    private static final String SIZE_USED_ATTRIBUTE = "SizeUsed";
    private static final String CAPACITY_ATTRIBUTE = "Capacity";

    private static final String UNIT_ATTRIBUTE = "Unit";
    private static final String NAME_ATTRIBUTE = "Name";

    private static final String GO_BRANCH = "Go";
    private static final String BACK_BRANCH = "Back";

    private final double sensitivity;//in SI units, i.e. m/V
    private final double springConstant; //in SI units, i.e. N/m

    private final Channel1DData approachChannelData;
    private final Channel1DData withdrawChannelData;

    private final CSISpectroscopyCurvePosition position;
    private PhotodiodeSignalType photodiodeSignalType;

    private CSISpectroscopyData(Channel1DData approachChannelData, Channel1DData withdrawChannelData, double sensitivity, double springConstant,  CSISpectroscopyCurvePosition position)
    {
        this.approachChannelData = approachChannelData;
        this.withdrawChannelData = withdrawChannelData;

        this.sensitivity = sensitivity;
        this.springConstant = springConstant;

        this.position = position;
    }

    public CSISpectroscopyCurvePosition getCurvePosition()
    {
        return position;
    }

    //in SI units, i.e. m/V
    public double getSensitivity()
    {
        return sensitivity;
    }

    //in SI units, i.e. N/m
    public double getSpringConstant()
    {
        return springConstant;
    }

    public Channel1DData getApproachChannelData()
    {
        return approachChannelData;
    }

    public Channel1DData getWithdrawChannelData()
    {
        return withdrawChannelData;
    }

    public PhotodiodeSignalType getPhotodiodeSignalType()
    {
        return photodiodeSignalType;
    }

    private void setPhotodiodeSignalType(PhotodiodeSignalType photodiodeSignalType)
    {
        this.photodiodeSignalType = photodiodeSignalType;
    }

    public static List<CSISpectroscopyData> readInSpectroscopySources(Document xmlData) throws UserCommunicableException
    {        
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            XPathExpression spectroscopyParametersPath = xpath.compile(SPECTROSCOPY_PARAMETERS);
            String spectroscopyParametersString = (String)spectroscopyParametersPath.evaluate(xmlData, XPathConstants.STRING);

            String[] spectroscopyParametersStringSplit = spectroscopyParametersString.split("[\r\n]+");
            Map<String, String> parameterValueStrings = new HashMap<>();
            for(String s : spectroscopyParametersStringSplit)
            {
                String[] keyValue = s.split("=");
                if(keyValue.length >= 2)
                {
                    parameterValueStrings.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            double springConstant = extractNumericValue(parameterValueStrings, TIP_LEVER_SPRING_CONSTANT).doubleValue();//in SI units, i.e. N/m
            double opticalLeverSensitivity = extractNumericValue(parameterValueStrings, TIP_LEVER_SENSITIVITY).doubleValue();//in SI units, i.e. m/V

            double sweepStart = extractNumericValue(parameterValueStrings, SWEEP_FROM_VALUE).doubleValue();
            double sweepEnd = extractNumericValue(parameterValueStrings, SWEEP_TO_VALUE).doubleValue();

            PrefixedUnit sweepUnit = parameterValueStrings.containsKey(SWEEP_SIGNAL_UNIT_SYMBOL) ? UnitUtilities.getSIUnit(parameterValueStrings.get(SWEEP_SIGNAL_UNIT_SYMBOL)) : null;
            CSISpectroscopySweepMode sweepMode = CSISpectroscopySweepMode.getMode(parameterValueStrings.get(SWEEP_MODE));

            Number resolutionApproach = extractNumericValue(parameterValueStrings, RESOLUTION);//this number is usually an integer, but it can be also NaN, so we have to use Number as the type
            Number resolutionWithdraw = extractNumericValue(parameterValueStrings, RESOLUTION_BACK);//this number is usually an integer, but it can be also NaN, so we have to use Number as the type

            String imagingPositionString = parameterValueStrings.containsKey(IMAGING_POSITION) ? parameterValueStrings.get(IMAGING_POSITION) : "";

            CSISpectroscopyCurvePosition curvePosition = CSISpectroscopyCurvePosition.parsePosition(imagingPositionString);

            boolean isDistanceSweeped = StandardUnitType.METER.getUnit().isCompatible(sweepUnit);

            if(!isDistanceSweeped)
            {
                return Collections.emptyList();
            }

            Quantity heighQuantity = Quantities.DISTANCE_MICRONS;
            double heightFactor = sweepUnit.getConversionFactorTo(heighQuantity.getUnit());

            XPathExpression spectroscopyDataPath = xpath.compile(SPECTROSCOPY_DATA);
            NodeList spectroscopyDataNodes = (NodeList)spectroscopyDataPath.evaluate(xmlData,XPathConstants.NODESET);//there can be multiple spectroscopy data in a single file

            List<CSISpectroscopyData> spDataAll = new ArrayList<>();
            for(int spDataIndex = 0; spDataIndex < spectroscopyDataNodes.getLength();spDataIndex++)
            {
                Node spectroscopyNode = spectroscopyDataNodes.item(spDataIndex);
                List<Element> channelDatasets = FileInputUtilities.getChildElementsWithTag(spectroscopyNode, CHANNEL_DATA_TAG);
                for(Element channelData : channelDatasets)
                {
                    String channelName = channelData.getAttribute(NAME_ATTRIBUTE);

                    if(!CSISpectroscopyChannelType.DEFLECTION.isCompatible(channelName))
                    {
                        continue;
                    }

                    String unitName = channelData.getAttribute(UNIT_ATTRIBUTE);
                    StandardUnitType unitType = StandardUnitType.getUnitType(unitName);
                    PrefixedUnit deflUnit = unitType.getUnit();

                    PhotodiodeSignalType photodiodeSignalType = StandardUnitType.AMPERE.equals(unitType)? PhotodiodeSignalType.ELECTRIC_CURRENT : PhotodiodeSignalType.VOLTAGE; //we assume that by default the photodiode signal is volatge

                    List<Element> passDatasets = FileInputUtilities.getChildElementsWithTag(channelData, PASS_DATA_TAG);

                    Channel1DData approach = FlexibleChannel1DData.getEmptyInstance(heighQuantity, Quantities.DEFLECTION_VOLTS);
                    Channel1DData withdraw = FlexibleChannel1DData.getEmptyInstance(heighQuantity, Quantities.DEFLECTION_VOLTS);

                    Quantity deflQuantity = CalibrationState.getDefaultYQuantity(deflUnit);
                    double defFactor = deflUnit.getConversionFactorTo(deflQuantity.getUnit());

                    for(Element passData : passDatasets)
                    {
                        int sizeUsed = Integer.valueOf(passData.getAttribute(SIZE_USED_ATTRIBUTE));
                        int capacity = Integer.valueOf(passData.getAttribute(CAPACITY_ATTRIBUTE));
                        String branchType = passData.getAttribute(NAME_ATTRIBUTE);
                        boolean isWithdraw = BACK_BRANCH.equals(branchType);

                        double[] passValues = new double[sizeUsed];

                        String passDataText = xpath.evaluate("./text()", passData);

                        Scanner passDataScanner = new Scanner(passDataText);
                        passDataScanner.useLocale(Locale.US);
                        passDataScanner.useDelimiter("[\r\n]+");

                        if(isWithdraw)
                        {
                            for(int i = sizeUsed - 1;i>-1;i--)
                            {                           
                                passValues[i] = defFactor*passDataScanner.nextDouble();
                            }
                        }
                        else
                        {
                            for(int i = 0;i<sizeUsed;i++)
                            {                           
                                passValues[i] = defFactor*passDataScanner.nextDouble();
                            }
                        }

                        passDataScanner.close();

                        double increment = (isWithdraw ? -1:1)*heightFactor*(sweepEnd - sweepStart)/(capacity - 1.);
                        double origin = isWithdraw ? heightFactor*sweepEnd + (capacity - sizeUsed)*increment  : heightFactor*sweepStart;

                        Grid1D grid = new Grid1D(increment, origin, sizeUsed, heighQuantity);
                        Channel1DData gridChannel = new GridChannel1DData(passValues, grid, deflQuantity);

                        approach = isWithdraw? approach : gridChannel;
                        withdraw = isWithdraw? gridChannel : withdraw;
                    }

                    CSISpectroscopyData spData = new CSISpectroscopyData(approach, withdraw, opticalLeverSensitivity, springConstant, curvePosition);
                    spData.setPhotodiodeSignalType(photodiodeSignalType);

                    spDataAll.add(spData);
                }               
            }
            return spDataAll;

        } catch (XPathExpressionException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    private static Number extractNumericValue(Map<String, String> parameterValueStrings, String key)
    {
        Number val = Double.NaN;
        if(parameterValueStrings.containsKey(key))
        {
            NumberFormat format = NumberFormat.getInstance(Locale.US);

            String valueString = parameterValueStrings.get(key).trim();
            try {
                val = NAN.equals(valueString) ? Double.NaN: format.parse(valueString);
            } catch (ParseException e) 
            {
                e.printStackTrace();
                val = Double.NaN;
            }
        }      

        return val;
    }

    private static enum CSISpectroscopyChannelType
    {
        DEFLECTION("Deflection"), FRICTION("Friction"), TOPOGRAPHY("Topography"), TIP_BIAS("Tip Bias"), AUX_PIN_5("AUX pin 5"), AUX_PIN_6("AUX pin 6"), CONDUCTIVE("Conductive"), AMPLITUDE("Amplitude"), 
        PHASE("Phase"), AMPLITUDE_LOCK_IN_2("Amplitude Lock-in 2"), PHASE_LOCK_IN_2("Phase Lock-in 2"), CURRENT("Current"), RESISTANCE("Resistance");

        private final String name;

        CSISpectroscopyChannelType(String name)
        {
            this.name = name;
        }

        public boolean isCompatible(String channelName)
        {        
            boolean compatible = channelName != null ? this.name.equals(channelName.trim()):false;
            return compatible;
        }

        public String getName()
        {
            return name;
        }
    }

    public static class CSISpectroscopyCurvePosition
    {
        private static final String X = "X";
        private static final String Y = "Y";

        private final Number x;
        private final Number y;

        private CSISpectroscopyCurvePosition(Number x, Number y)
        {
            this.x = x;
            this.y = y;
        }

        public boolean arePositionCoordinatesKnown()
        {
            boolean known = !Double.isNaN(x.doubleValue()) && !Double.isNaN(y.doubleValue());

            return known;
        }

        public Point2D getPoint(Grid2D grid)
        {
            if(grid == null || Double.isNaN(x.doubleValue()) || Double.isNaN(y.doubleValue()))
            {
                return null;
            }

            Point2D p = grid.getPoint(x.intValue(), y.intValue());

            return p;
        }

        public static CSISpectroscopyCurvePosition parsePosition(String positionString)
        {
            String[] imagingPositionStringSplit = positionString.split("\\s+");
            Map<String, String> imagingPositionMap = new HashMap<>();
            for(String s : imagingPositionStringSplit)
            {
                String[] keyValue = s.split(":");
                imagingPositionMap.put(keyValue[0].trim(), keyValue[1].trim());
            }

            Number x = imagingPositionMap.containsKey(X) ? Integer.valueOf(imagingPositionMap.get(X)): Double.NaN;
            Number y = imagingPositionMap.containsKey(Y) ? Integer.valueOf(imagingPositionMap.get(Y)): Double.NaN;

            CSISpectroscopyCurvePosition position = new CSISpectroscopyCurvePosition(x, y);
            return position;
        }
    }

    private static enum CSISpectroscopySweepMode
    {
        GO_ONLY("1",1), GO_PLUS_BACK("2",2);

        private final String name;
        private final int passCount;

        CSISpectroscopySweepMode(String name, int passCount)
        {
            this.name = name;
            this.passCount = passCount;
        }

        public int getPassCount()
        {
            return passCount;
        }

        public static CSISpectroscopySweepMode getMode(String name)
        {
            for(CSISpectroscopySweepMode mode : CSISpectroscopySweepMode.values())
            {
                if(mode.name.equals(name.trim()))
                {
                    return mode;
                }
            }

            throw new IllegalArgumentException("No CSISpectroscopySweepMode corresponds to " + name);
        }
    }
}