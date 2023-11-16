
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
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

package atomicJ.readers.park;


import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.analysis.ForceCurveSimpleStorage;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;



public class ParkSpectroscopyTextReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"txt"};
    private static final String DESCRIPTION = "Park force curve text file (.txt)";

    private static final String INDEX = "Index";
    private static final String Z_SCAN = "Z Scan";
    private static final String Z_DECTOR = "Z Detector";
    private static final String FORCE = "Force";
    private static final String CURRENT = "Current";

    private static final String POINT = "point";

    private static final String DELIMITER = "((?:\\p{javaWhitespace}*)[\\n\\t]+(?:\\p{javaWhitespace}*))";

    private final ChannelFilter filter = PermissiveChannelFilter.getInstance();

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        boolean accept = filter.accept(f) && ParkTextHeaderNew.isFileBeginnigConsistentWithParkTextHeaderNew(f); 

        return accept;
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirectives) throws UserCommunicableException 
    {        
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {                   
            try(Scanner scanner = new Scanner(channel);) 
            {
                scanner.useLocale(Locale.US);

                ParkTextHeader textHeader = ParkTextHeaderNew.readIn(scanner);

                int xColumnIndex = textHeader.getXColumnIndex();
                int yColumnIndex = textHeader.getYColumnIndex();

                double factorX = textHeader.getXFactor();
                double factorY = textHeader.getYFactor();

                Quantity xQuantity = Quantities.DISTANCE_MICRONS;
                Quantity yQuantity = textHeader.getYQuantity();

                List<double[]> readInPoints = new ArrayList<>();
                while(scanner.hasNextLine())
                {
                    String nextLine = scanner.nextLine().trim();
                    if(nextLine.isEmpty())
                    {
                        continue;
                    }

                    String[] data = nextLine.split(DELIMITER);

                    double x = factorX*format.parse(data[xColumnIndex]).doubleValue();
                    double y = factorY*format.parse(data[yColumnIndex]).doubleValue();

                    double[] p = new double[] {x,y};

                    readInPoints.add(p);                   
                }  

                double[][] points = readInPoints.toArray(new double[][] {});

                ForceCurveSimpleStorage fc = ForceCurveOrientation.partition(points);

                double[][] approach = fc.getApproach();
                double[][] withdraw = fc.getWithdraw();

                Channel1DData approachChannelData = new FlexibleChannel1DData(approach, xQuantity, yQuantity, SortedArrayOrder.DESCENDING);
                Channel1DData withdrawChannelData = new FlexibleChannel1DData(withdraw, xQuantity, yQuantity, SortedArrayOrder.ASCENDING);

                StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, IOUtilities.getBareName(f), f.getAbsolutePath(), approachChannelData, withdrawChannelData);
                sources.add(source);
            } 
            catch (Exception e)     
            {
                e.printStackTrace();
                throw new UserCommunicableException("Error occured while reading the file", e);
            } 
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }

        return sources;		
    }	

    private static interface ParkTextHeader
    {
        public int getXColumnIndex();       
        public int getYColumnIndex();
        public double getXFactor();       
        public double getYFactor();
        public boolean isSensitivityCalibrated();
        public boolean isForceCalibrated();
        public Quantity getXQuantity();
        public Quantity getYQuantity();
    }


    private static class ParkTextHeaderNew implements ParkTextHeader
    {
        private static final String X_UNIT = "X Unit";
        private static final String Y_UNIT = "Y Unit";

        private static final String X_LABEL = "X";
        private static final String Y_LABEL = "Y";

        private int xColumnIndex;
        private int yColumnIndex;

        private Quantity xQuantity;
        private Quantity yQuantity;
        private double factorX;
        private double factorY;

        private boolean forceCalibrated;
        private boolean sensitivityCalibrated;

        private static boolean isFileBeginnigConsistentWithParkTextHeaderNew(File f)
        {
            try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
            {                   
                try(Scanner scanner = new Scanner(channel);) 
                {
                    scanner.useLocale(Locale.US);
                    while(scanner.hasNext())
                    {
                        String line = scanner.nextLine().trim();

                        if(line.startsWith(X_UNIT))
                        {
                            return true;
                        }
                        else if(line.startsWith(Y_UNIT))
                        {
                            return true;
                        }                    
                        else if(!line.isEmpty())
                        {
                            return false;
                        }
                    }
                }      
            }
            catch (Exception e) {return false;}

            return false;
        }

        private static ParkTextHeaderNew readIn(Scanner scanner) throws UserCommunicableException
        {
            ParkTextHeaderNew header = new ParkTextHeaderNew();

            String xUnitLine = null;
            String yUnitLine = null;

            while((xUnitLine == null || yUnitLine == null) && scanner.hasNext())
            {
                String line = scanner.nextLine().trim();

                if(line.startsWith(X_UNIT))
                {
                    xUnitLine = line;
                }
                else if(line.startsWith(Y_UNIT))
                {
                    yUnitLine = line;
                }             
            }
    
            String xUnitString = xUnitLine.split(":")[1];
            String yUnitString = yUnitLine.split(":")[1];

            PrefixedUnit xUnit = UnitUtilities.getSIUnit(xUnitString);
            PrefixedUnit yUnit = UnitUtilities.getSIUnit(yUnitString);

            String columnLabelsLine = null;

            while((columnLabelsLine == null) && scanner.hasNext())
            {
                String line = scanner.nextLine().trim();
                if(!line.isEmpty())
                {
                    columnLabelsLine = line;
                }
            }

            List<String> columnLabels = Arrays.asList(columnLabelsLine.split(DELIMITER));

            int xColumnIndex = columnLabels.indexOf(X_LABEL);
            int yColumnIndex = columnLabels.indexOf(Y_LABEL);

            if(xColumnIndex == -1 || yColumnIndex == -1)
            {
                throw new UserCommunicableException("Error occured while reading the file");
            }

            header.xColumnIndex = xColumnIndex;
            header.yColumnIndex = yColumnIndex;

            header.xQuantity = Quantities.DISTANCE_MICRONS;

            Quantity yQuantity = CalibrationState.getDefaultYQuantity(yUnit);
            header.yQuantity = yQuantity;

            header.factorX = xUnit.getConversionFactorTo(header.xQuantity.getUnit());
            header.factorY = yUnit.getConversionFactorTo(yQuantity.getUnit());

            header.forceCalibrated =  StandardQuantityTypes.FORCE.isCompatible(yQuantity.getUnit());
            header.sensitivityCalibrated = StandardQuantityTypes.LENGTH.isCompatible(yQuantity.getUnit());

            return header;
        }


        @Override
        public int getXColumnIndex()
        {
            return xColumnIndex;
        }

        @Override
        public int getYColumnIndex()
        {
            return yColumnIndex;
        }

        @Override
        public double getXFactor()
        {
            return factorX;
        }

        @Override
        public double getYFactor()
        {
            return factorY;
        }

        @Override
        public boolean isSensitivityCalibrated()
        {
            return sensitivityCalibrated;
        }

        @Override
        public boolean isForceCalibrated()
        {
            return forceCalibrated;
        }

        @Override
        public Quantity getXQuantity()
        {
            return xQuantity;
        }

        @Override
        public Quantity getYQuantity()
        {
            return yQuantity;
        }
    }

    private static class ParkTextHeaderOld implements ParkTextHeader
    {
        private int xColumnIndex;
        private int yColumnIndex;

        private Quantity xQuantity;
        private Quantity yQuantity;
        private double factorX;
        private double factorY;

        private boolean forceCalibrated;
        private boolean sensitivityCalibrated;

        private ParkTextHeaderOld()
        {}

        private static ParkTextHeaderOld readIn(Scanner scanner) throws UserCommunicableException
        {
            ParkTextHeaderOld header = new ParkTextHeaderOld();

            String firstLine = scanner.nextLine().trim();

            String quantitiesLine = firstLine.startsWith(POINT) ? scanner.nextLine().trim() : firstLine;
            List<String> quantities = Arrays.asList(quantitiesLine.split(DELIMITER));

            String xChannelName = quantities.contains(Z_DECTOR) ? Z_DECTOR : Z_SCAN;

            int xColumnIndex = quantities.indexOf(xChannelName);
            int yColumnIndex = quantities.indexOf(FORCE);

            if(xColumnIndex == -1 || yColumnIndex == -1)
            {
                throw new UserCommunicableException("Error occured while reading the file");
            }

            header.xColumnIndex = xColumnIndex;
            header.yColumnIndex = yColumnIndex;

            //there are no true unit for Indexes, but this field is composed of non tab
            //white characters, so we do not trim the unit line, but split it using tab as
            //the delimiter
            String unitsLine = scanner.nextLine();
            String[] units = unitsLine.split(DELIMITER);

            //instead of micron sign, the files uses ?
            String xUnitString = units[xColumnIndex].replaceFirst("\\?", "u");

            PrefixedUnit xUnit = UnitUtilities.getSIUnit(xUnitString);

            String yUnitString = units[yColumnIndex];
            PrefixedUnit yUnit = UnitUtilities.getSIUnit(yUnitString);

            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = CalibrationState.getDefaultYQuantity(yUnit);

            header.xQuantity = xQuantity;
            header.yQuantity = yQuantity;

            header.factorY = yUnit.getConversionFactorTo(yQuantity.getUnit());
            header.factorX = xUnit.getConversionFactorTo(xQuantity.getUnit());

            header.forceCalibrated =  StandardQuantityTypes.FORCE.isCompatible(yQuantity.getUnit());
            header.sensitivityCalibrated = StandardQuantityTypes.LENGTH.isCompatible(yQuantity.getUnit());


            return header;
        }

        @Override
        public int getXColumnIndex()
        {
            return xColumnIndex;
        }

        @Override
        public int getYColumnIndex()
        {
            return yColumnIndex;
        }

        @Override
        public double getXFactor()
        {
            return factorX;
        }

        @Override
        public double getYFactor()
        {
            return factorY;
        }

        @Override
        public boolean isSensitivityCalibrated()
        {
            return sensitivityCalibrated;
        }

        @Override
        public boolean isForceCalibrated()
        {
            return forceCalibrated;
        }

        @Override
        public Quantity getXQuantity()
        {
            return xQuantity;
        }

        @Override
        public Quantity getYQuantity()
        {
            return yQuantity;
        }
    }
}