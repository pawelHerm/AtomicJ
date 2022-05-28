
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

package atomicJ.readers.afmworkshop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.MetaMap;


public class AFMWorkshopSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final Pattern BRACKETS_CONTENT_PATTERN = Pattern.compile("(.+?)\\((.*)\\)");

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"csv"};
    private static final String DESCRIPTION = "AFMWorkshop force curve (.csv)";

    private static final String DELIMITER = "((?:\\p{javaWhitespace}*)[,\\n]+(?:\\p{javaWhitespace}*))";

    private static final String FORCE_CURVE_FIELD = "Force-Distance Curve";
    private static final String DATE_AND_TIME_FIELD_KEY = "Date and Time";

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try (Scanner scanner = new Scanner(Files.newByteChannel(f.toPath())))
        {                   
            scanner.useLocale(Locale.US);

            String firstNonEmptyLine = getNextNonEmptyLine(scanner);         

            boolean isSpectroscopy = FORCE_CURVE_FIELD.equals(firstNonEmptyLine);
            String dateAndTimeFiled = scanner.nextLine();

            scanner.useDelimiter(DELIMITER);     

            String columnHeadersLine = getNextNonEmptyLine(scanner);
            String[] headers = columnHeadersLine.split(DELIMITER);

            MetaMap<AFMWorkshopCurveBranch, AFMWorkshopSignalType, DataColumn> dataColumns = new MetaMap<>();

            int columnCount = headers.length;

            for(int i = 0; i<headers.length;i++)
            {
                DataColumn dataColumn = buildDataColumn(headers[i], i);
                dataColumns.put(dataColumn.getCurveBranch(),dataColumn.getSignalType(), dataColumn);
            }

            List<double[]> data = new ArrayList<>();

            while(scanner.hasNextDouble())
            {
                double[] row = new double[columnCount];
                for(int i = 0; i < columnCount; i++)
                {
                    row[i] = scanner.nextDouble();
                }      

                data.add(row);
            }  

            AFMWorkshopSignalType approachXSignal = dataColumns.containsKeyPair(AFMWorkshopCurveBranch.EXTEND, AFMWorkshopSignalType.Z_SENSOR) ? AFMWorkshopSignalType.Z_SENSOR : AFMWorkshopSignalType.Z_DRIVE;
            DataColumn approachXColumn = dataColumns.get(AFMWorkshopCurveBranch.EXTEND, approachXSignal);
            DataColumn approachYColumn = dataColumns.get(AFMWorkshopCurveBranch.EXTEND, AFMWorkshopSignalType.TOP_MINUS_BOTTOM);

            AFMWorkshopSignalType withdrawXSignal = dataColumns.containsKeyPair(AFMWorkshopCurveBranch.RETRACT, AFMWorkshopSignalType.Z_SENSOR) ? AFMWorkshopSignalType.Z_SENSOR : AFMWorkshopSignalType.Z_DRIVE;
            DataColumn withdrawXColumn = dataColumns.get(AFMWorkshopCurveBranch.RETRACT, withdrawXSignal);
            DataColumn withdrawYColumn = dataColumns.get(AFMWorkshopCurveBranch.RETRACT, AFMWorkshopSignalType.TOP_MINUS_BOTTOM);

            boolean approachSpecified = approachXColumn != null && approachYColumn != null;
            boolean withdrawSpecified = withdrawXColumn != null && withdrawYColumn != null;

            if(!approachSpecified && !withdrawSpecified)
            {
                return sources;
            }

            DataColumn nonNullColumn = approachSpecified ? approachYColumn : withdrawYColumn;

            Quantity defaultXQuantity = Quantities.DISTANCE_MICRONS;
            Quantity defaultYQuantity = CalibrationState.getDefaultYQuantity(nonNullColumn.getUnit());

            Channel1DData approachChannelData = approachSpecified ? buildChannelData(approachXColumn, approachYColumn, data, SortedArrayOrder.DESCENDING) : FlexibleChannel1DData.getEmptyInstance(defaultXQuantity, defaultYQuantity);
            Channel1DData withdrawChannelData = withdrawSpecified ? buildChannelData(withdrawXColumn, withdrawYColumn, data, SortedArrayOrder.ASCENDING) : FlexibleChannel1DData.getEmptyInstance(defaultXQuantity, defaultYQuantity);

            StandardSimpleSpectroscopySource source = buildSource(f, "", approachChannelData, withdrawChannelData);

            PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.getSignalType(nonNullColumn.getUnit(), PhotodiodeSignalType.VOLTAGE);
            source.setPhotodiodeSignalType(photodiodeSignalType);

            sources.add(source);

            return sources;
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        }  
    }   

    private static Channel1DData buildChannelData(DataColumn xColumn, DataColumn yColumn, List<double[]> values, SortedArrayOrder sortedArrayOrder)
    {
        Quantity xQuantity = Quantities.DISTANCE_MICRONS;           
        Quantity yQuantity = CalibrationState.getDefaultYQuantity(yColumn.getUnit());

        double factorX = xColumn.getUnit().getConversionFactorTo(xQuantity.getUnit());
        double factorY = yColumn.getUnit().getConversionFactorTo(yQuantity.getUnit());

        double[][] data = ArrayUtilities.trimIsolatedEndPoints(ArrayUtilities.trimXRepetitionsIfNecessary(combineValueLists(values, xColumn.getIndex(), yColumn.getIndex(), factorX, factorY, sortedArrayOrder),0), 3, 10);

        Channel1DData channelData = new FlexibleChannel1DData(data, xQuantity, yQuantity, sortedArrayOrder);

        return channelData;
    }

    private static double[][] combineValueLists(List<double[]> values, int xIndex, int yIndex, double xFactor, double yFactor, SortedArrayOrder requestedXOrder)
    {
        int n = values.size();

        double[][] points = new double[n][]; 

        SortedArrayOrder currentOrder = SortedArrayOrder.getOverallXOrder(values);

        if(Objects.equals(currentOrder, requestedXOrder))
        {
            for(int i = 0; i<n; i++)
            {
                double[] valuesRow = values.get(i);
                points[i] = new double[] {xFactor*valuesRow[xIndex], yFactor*valuesRow[yIndex]};
            }
        }
        else
        {
            for(int i = 0; i<n; i++)
            {
                double[] valuesRow = values.get(n - i - 1);
                points[i] = new double[] {xFactor*valuesRow[xIndex], yFactor*valuesRow[yIndex]};
            }
        }

        return points;
    }

    private String getNextNonEmptyLine(Scanner scanner)
    {                
        String line = scanner.nextLine();

        while(line.isEmpty()) //we don't check if it hasNext(). If it does not, then end of file exception will be thrown, which is the expected behaviour for us
        {
            line = scanner.nextLine().trim();
        }

        return line;
    }

    public static boolean isRightRecordingType(File f)
    {        
        try (Scanner scanner =  new Scanner(Files.newByteChannel(f.toPath())))
        {                   
            scanner.useLocale(Locale.US);

            String firstNonEmptyLine = scanner.nextLine();

            while(firstNonEmptyLine.isEmpty() && scanner.hasNext())
            {
                firstNonEmptyLine = scanner.nextLine().trim();
            }

            boolean isSpectroscopy = firstNonEmptyLine.startsWith(FORCE_CURVE_FIELD);
            return isSpectroscopy;
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
        }  

        return false;
    }  

    private static StandardSimpleSpectroscopySource buildSource(File f, String suffix, Channel1DData approach, Channel1DData withdraw)
    {
        String longName = f.getAbsolutePath() + suffix;
        String shortName = IOUtilities.getBareName(f) + suffix;

        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approach, withdraw);

        return source;
    }

    private static DataColumn buildDataColumn(String label, int index)
    {
        Matcher matcher = BRACKETS_CONTENT_PATTERN.matcher(label);
        matcher.matches();

        String channelName = matcher.group(1);
        String unitString = matcher.group(2);

        PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);

        String[] channelNameParts = channelName.split("\\s++");

        AFMWorkshopCurveBranch curveBranch = AFMWorkshopCurveBranch.getCurveBranch(channelNameParts[0]);
        AFMWorkshopSignalType signalType = AFMWorkshopSignalType.getAFMWorkshopChannelType(channelNameParts[1]);

        DataColumn dataColumn = new DataColumn(index, curveBranch, signalType, unit);

        return dataColumn;
    }

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
        boolean accept =  filter.accept(f) && isRightRecordingType(f); 
        return accept;
    }

    private static class DataColumn
    {
        private final AFMWorkshopCurveBranch curveBranch;
        private final AFMWorkshopSignalType signalType;
        private final PrefixedUnit unit;
        private final int index;

        public DataColumn(int index, AFMWorkshopCurveBranch curveBranch, AFMWorkshopSignalType signalType, PrefixedUnit unit)
        {
            this.unit = unit;
            this.curveBranch = curveBranch;
            this.signalType = signalType;
            this.index = index;
        }

        public int getIndex()
        {
            return index;
        }

        public PrefixedUnit getUnit()
        {
            return unit;
        }

        public AFMWorkshopCurveBranch getCurveBranch()
        {
            return curveBranch;
        }

        public AFMWorkshopSignalType getSignalType()
        {
            return signalType;
        }
    }

    public static enum AFMWorkshopCurveBranch
    {
        EXTEND("Extend"), RETRACT("Retract"), UNRECOGNIZED(".*");

        private final String name;

        AFMWorkshopCurveBranch(String name)
        {
            this.name = name;
        }

        public static AFMWorkshopCurveBranch getCurveBranch(String name)
        {
            for(AFMWorkshopCurveBranch channelType : AFMWorkshopCurveBranch.values())
            {
                if(channelType.name.matches(name.trim()))
                {
                    return channelType;
                }
            }

            //cannot happen
            throw new IllegalArgumentException("No AFMWorkshopChannelType is named " + name);
        }     
    }
}