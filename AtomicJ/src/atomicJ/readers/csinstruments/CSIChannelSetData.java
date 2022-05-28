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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import atomicJ.data.Grid2D;
import atomicJ.data.Quantities;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardUnitType;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;

public class CSIChannelSetData
{
    private static final String IMAGING_PARAMETERS_PATH = "//Imaging/ImagingParameters";
    private static final String CHANNEL_LIST_PATH = "//Imaging/ChannelList";
    private static final String IMAGING_VIEW_PATH = "//Imaging/ImagingView";    
    private static final String CHANNEL_DATA_TAG = "ChannelData";
    private static final String CHANNEL_VIEW_TAG = "ChannelView";

    private static final String NAME_ATTRIBUTE = "Name";
    private static final String UNIT_ATTRIBUTE = "Unit";
    private static final String NAO_SUB_FILE_ATTRIBUTE = "NaoSubFile";

    private static final String NAN = "NaN";

    private final Grid2D grid;
    private final List<CSIImageChannelData> individualChannels;

    private CSIChannelSetData(Grid2D grid, List<CSIImageChannelData> individualChannels)
    {
        this.grid = grid;
        this.individualChannels = Collections.unmodifiableList(individualChannels);
    }

    public List<CSIImageChannelData> getIndividualChannelData()
    {
        return individualChannels;
    }

    public Grid2D getGrid()
    {
        return grid;
    }

    public boolean isGridSpecified()
    {
        boolean gridSpecified = (grid != null);
        return gridSpecified;
    }

    public static CSIChannelSetData readInChannelSetData(Document xmlData) throws UserCommunicableException
    {        
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            XPathExpression xPathImParameters = xpath.compile(IMAGING_PARAMETERS_PATH);
            String imagingParametersString = (String)xPathImParameters.evaluate(xmlData, XPathConstants.STRING);

            CSIImagingParameters imagingParameters = CSIImagingParameters.readIn(imagingParametersString);
            Grid2D grid = imagingParameters.getGrid();

            XPathExpression xPathImagingView = xpath.compile(IMAGING_VIEW_PATH);
            Node imagingViewNode = (Node)xPathImagingView.evaluate(xmlData,XPathConstants.NODE);
            List<Element> channelViewElements = FileInputUtilities.getChildElementsWithTag(imagingViewNode, CHANNEL_VIEW_TAG);

            Map<String,Quantity> channelNameQuantityMap = new HashMap<>();
            for(Element channelViewElement : channelViewElements)
            {
                String channelName = channelViewElement.getAttribute(NAME_ATTRIBUTE);
                String unitName = channelViewElement.getAttribute(UNIT_ATTRIBUTE);

                PrefixedUnit unit = (StandardUnitType.isUnitTypeRecognizable(unitName)) ?  StandardUnitType.getUnitType(unitName).getUnit() : new SimplePrefixedUnit(unitName);
                Quantity quantity = new UnitQuantity(channelName, unit);

                channelNameQuantityMap.put(channelName, quantity);
            }

            XPathExpression xPathChannelList = xpath.compile(CHANNEL_LIST_PATH);
            Node channelListNode = (Node)xPathChannelList.evaluate(xmlData,XPathConstants.NODE);

            List<Element> channelDataElements = FileInputUtilities.getChildElementsWithTag(channelListNode, CHANNEL_DATA_TAG);
            List<CSIImageChannelData> channelData = new ArrayList<>();

            for(Element channelDataElement : channelDataElements)
            {
                String name = channelDataElement.getAttribute(NAME_ATTRIBUTE);
                Quantity quantity = channelNameQuantityMap.containsKey(name) ? channelNameQuantityMap.get(name) : new DimensionlessQuantity(name);

                Element leftElement = FileInputUtilities.getFirstChildElementWithTag(channelDataElement, ChannelDirection.LEFT.getTag());
                Element rightElement = FileInputUtilities.getFirstChildElementWithTag(channelDataElement, ChannelDirection.RIGHT.getTag());
                if(leftElement != null)
                {
                    String dataPath = leftElement.getAttribute(NAO_SUB_FILE_ATTRIBUTE);
                    channelData.add(new CSIImageChannelData(name, quantity, ChannelDirection.LEFT, dataPath));
                }

                if(rightElement != null)
                {
                    String dataPath = rightElement.getAttribute(NAO_SUB_FILE_ATTRIBUTE);
                    channelData.add(new CSIImageChannelData(name, quantity, ChannelDirection.RIGHT, dataPath));
                }
            }     

            CSIChannelSetData channelSetData = new CSIChannelSetData(grid, channelData);

            return channelSetData;
        } catch (XPathExpressionException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    public static Grid2D readInGrid(Document xmlData) throws UserCommunicableException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            XPathExpression xPathImParameters = xpath.compile(IMAGING_PARAMETERS_PATH);
            String imagingParametersString = (String)xPathImParameters.evaluate(xmlData, XPathConstants.STRING);

            CSIImagingParameters imagingParameters = CSIImagingParameters.readIn(imagingParametersString);
            Grid2D grid = imagingParameters.getGrid();
            return grid;

        } catch (XPathExpressionException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    public static class CSIImagingParameters
    {
        private static final String RESOLUTION_PARAMETER = "Resolution";
        private static final String SIZE_PARAMETER = "Size";

        private final int columnCount;
        private final int rowCount;

        private final double sizeX;
        private final double sizeY;

        private CSIImagingParameters(int columnCount, int rowCount, double sizeX, double sizeY)
        {
            this.columnCount = columnCount;
            this.rowCount = rowCount;

            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        public static CSIImagingParameters readIn(String imagingParametersString)
        {
            String[] imagingParametersStringSplit = imagingParametersString.split("[\r\n]+");
            Map<String, String> parameterValueStrings = new HashMap<>();
            for(String s : imagingParametersStringSplit)
            {
                String[] keyValue = s.split("=");
                if(keyValue.length >= 2)
                {
                    parameterValueStrings.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            List<Number> resolution = extractMultipleNumbers(parameterValueStrings, ",", RESOLUTION_PARAMETER);
            Number resolutionX = resolution.size() > 0 ? resolution.get(0) : Double.NaN;
            Number resolutionY = resolution.size() > 1 ? resolution.get(1) : Double.NaN;

            List<Number> size = extractMultipleNumbers(parameterValueStrings, ",", SIZE_PARAMETER);
            double sizeX = size.size() > 0 ? size.get(0).doubleValue() : Double.NaN;
            double sizeY = size.size() > 1 ? size.get(1).doubleValue() : Double.NaN;

            int rowCount = Double.isNaN(resolutionY.doubleValue()) ? -1 : resolutionY.intValue();
            int columnCount = Double.isNaN(resolutionX.doubleValue()) ? -1 : resolutionX.intValue();

            CSIImagingParameters imagingParameters = new CSIImagingParameters(columnCount, rowCount, sizeX, sizeY);

            return imagingParameters;
        }

        public int getColumnCount()
        {
            return columnCount;
        }

        public int getRowCount()
        {
            return rowCount;
        }

        public double getSizeX()
        {
            return sizeX;
        }

        public double getSizeY()
        {
            return sizeY;
        }

        public boolean isGridSpecified()
        {
            boolean isGridSpecified = !Double.isNaN(sizeX) && !Double.isNaN(sizeY) 
                    && columnCount > -1 && rowCount > -1;

                    return isGridSpecified;
        }

        public Grid2D getGrid()
        {
            boolean isGridSpecified = !Double.isNaN(sizeX) && !Double.isNaN(sizeY) 
                    && columnCount > -1 && rowCount > -1;

                    double incrementX = 1e6*sizeX/(columnCount - 1);//we multiply by 1e6 to get the increment in microns
                    double incrementY = 1e6*sizeY/(rowCount - 1);

                    Grid2D grid = isGridSpecified ? new Grid2D(incrementX, incrementY, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS) : null;

                    return grid;
        }
    }

    public static enum ChannelDirection
    {
        LEFT("Left", true), RIGHT("Right", false);

        private final String tag;
        private final boolean trace;

        private ChannelDirection(String tag, boolean trace)
        {
            this.tag = tag;
            this.trace = trace;
        }

        public boolean isTrace()
        {
            return trace;
        }

        public String getTag()
        {
            return tag;
        }
    }

    public static class CSIImageChannelData
    {
        private final String channelName;
        private final String dataPath;
        private final Quantity zQuantity;
        private final ChannelDirection direction;

        public CSIImageChannelData(String channelName, Quantity zQuantity, ChannelDirection direction, String dataPath)
        {
            this.channelName = channelName;
            this.zQuantity = zQuantity;
            this.direction = direction;
            this.dataPath = dataPath;
        }

        public String getChannelName()
        {
            return channelName;
        }

        public Quantity getZQuantity()
        {
            return zQuantity;
        }

        public String getIdentifier()
        {
            String id = channelName + " " + direction.getTag();
            return id;
        }

        public boolean isTrace()
        {
            return direction.isTrace();
        }

        public String getDataPath()
        {
            return dataPath;
        }

        public String getDataPathRecognizableByZipFile()
        {
            String pathCorrected = dataPath.replace("\\", "/");

            return pathCorrected;
        }
    }

    private static List<Number> extractMultipleNumbers(Map<String, String> parameterValueStrings, String separator, String key)
    {
        List<Number> values = new ArrayList<>();

        if(parameterValueStrings.containsKey(key))
        {
            NumberFormat format = NumberFormat.getInstance(Locale.US);

            String valueString = parameterValueStrings.get(key);
            String[] valueStringSplit = valueString.split(separator);

            for(String vs : valueStringSplit)
            {
                Number val = Double.NaN;
                String vsTrimmed = vs.trim();
                try {

                    val = NAN.equals(vsTrimmed) ? Double.NaN: format.parse(vsTrimmed);
                } catch (ParseException e) 
                {
                    e.printStackTrace();
                }
                values.add(val);
            }

        }    

        return values;
    }
}