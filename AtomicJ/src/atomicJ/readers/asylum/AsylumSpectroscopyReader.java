
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Paweł Hermanowicz
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

package atomicJ.readers.asylum;

import java.awt.geom.Point2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Quantities;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.MapDelayedCreator;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.MathUtilities;


public class AsylumSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"ibw", "bwav", "ARDF"};
    private static final String DESCRIPTION = "Asylum force curve file (.ibw, .bwavm .ARDF)";

    //maps
    private static final String X_MAP_POSITION = "XLVDT";
    private static final String Y_MAP_POSITION = "YLVDT";
    private static final String MAP_COLUMN_COUNT = "FMapScanPoints";
    private static final String MAP_ROW_COUNT = "FMapScanLines";

    private static final String INV_OLS = "InvOLS";
    private static final String SPRING_CONSTANT = "SpringConstant";
    private static final String POINTS_PER_SECOND = "NumPtsPerSec";

    ////////// DWELL - IMPORTANT PROPERTIES
    private static final String TRIGGERED_WITH_DWELL = "IsTriggeredWithDwell";
    private static final String DWELL_SETTING = "DwellSetting";
    private static final String DWELL_TIME = "DwellTime";
    private static final String DWELL_TIME_1 = "DwellTime1";
    private static final String DWELL_RATE = "DwellRate";
    /////

    private static final String Z_SENSOR_POSITION_CHANNEL = "ZSnsr";
    private static final String RAW_POSITION_CHANNEL = "Raw";
    private static final String LVDT_POSITION_CHANNEL = "LVDT";

    private static final String DEFLECTION_CHANNEL = "Defl";

    private static final String CURVE_INDICES_KEY = "Indexes";
    private static final String CURVE_SEGMENT_DIRECTIONS = "Direction";

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {           
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {
            ByteBuffer versionBuffer = ByteBuffer.allocate(2);

            FileInputUtilities.readBytes(channel, versionBuffer);        
            versionBuffer.flip();

            IgorFileType fileType = new IgorFileType(versionBuffer);

            // reading the bin header
            ByteBuffer binHeaderBuffer = ByteBuffer.allocate(fileType.getBinHeaderSize());            
            binHeaderBuffer.put(versionBuffer.rewind());

            FileInputUtilities.readBytes(channel, binHeaderBuffer);
            binHeaderBuffer.flip();

            IgorBinaryHeader binHeader = fileType.readInBinHeader(binHeaderBuffer);

            WaveHeader waveHeader = fileType.readInWaveHeader(FileInputUtilities.readBytesToBuffer(channel, fileType.getWaveHeaderSize(), fileType.getByteOrder()));


            double[][][] channelData = waveHeader.readIn(FileInputUtilities.readBytesToBuffer(channel, waveHeader.getWaveDataByteCount(), fileType.getByteOrder()));

            //DEPENDENCY FORMULA

            FileInputUtilities.readBytesToBuffer(channel, binHeader.getDependencyFormulaSize(), fileType.getByteOrder());

            //WAVE NOTE

            IgorWaveNote waveNote = binHeader.readInWaveNote(FileInputUtilities.readBytesToBuffer(channel, binHeader.getWaveNoteSize(), fileType.getByteOrder()));

            //OPTIONAL EXTENDED DATA UNITS

            String extendedDataUnit = binHeader.readInExtendedDataUnit(FileInputUtilities.readBytesToBuffer(channel, binHeader.getExtendedDataUnitSize(), fileType.getByteOrder()));

            //OPTIONAL EXTENDED DIMENSION UNITS

            List<String> extendedDimUnits = binHeader.readInExtendedDimensionUnits(FileInputUtilities.readBytesToBuffer(channel, binHeader.getTotalExtendedDimensionUnitSize(), fileType.getByteOrder()));

            //OPTIONAL DIMENSION LABELS

            List<String> dimensionLabels = binHeader.readInDimensionLabels(FileInputUtilities.readBytesToBuffer(channel, binHeader.getTotalDimensionLabelSize(), fileType.getByteOrder()));

            List<String> channelLabels = IgorUtilities.extractChannelNames(dimensionLabels.get(1), waveHeader.getDimensionItemCount(1));

            int zIndex = getZPositionIndex(channelLabels);
            int deflectionIndex = channelLabels.lastIndexOf(DEFLECTION_CHANNEL);

            String sensitivityString = waveNote.getValue(INV_OLS);
            String springConstantString = waveNote.getValue(SPRING_CONSTANT);
            String[] curveIndices = waveNote.getValue(CURVE_INDICES_KEY).split(",");
            String[] directionIdentifiers = waveNote.getValue(CURVE_SEGMENT_DIRECTIONS).split(",");
            String triggeredWithDwell = waveNote.getValue(TRIGGERED_WITH_DWELL);

            String xPositionString = waveNote.getValue(X_MAP_POSITION);
            String yPositionString = waveNote.getValue(Y_MAP_POSITION);         

            Point2D recordingPosition = (xPositionString != null && xPositionString != null) ? new Point2D.Double(1e6*Double.parseDouble(xPositionString), 1e6*Double.parseDouble(yPositionString)) : null;

            String rowCountString = waveNote.getValue(MAP_ROW_COUNT);
            int rowCount = rowCountString != null ? Integer.parseInt(rowCountString) : -1;


            String columnCountString = waveNote.getValue(MAP_COLUMN_COUNT);
            int columnCount = columnCountString != null ? Integer.parseInt(columnCountString) : -1;

            int approachSegmentLocation = AsylumCurveSegmentType.APPROACH.getIndex(directionIdentifiers);
            int withdrawSegmentLocation = AsylumCurveSegmentType.WITHDRAW.getIndex(directionIdentifiers);

            boolean approachPresent = approachSegmentLocation > -1;
            boolean withdrawPresent = withdrawSegmentLocation > -1;

            int approachFirstIndex = approachPresent ? Integer.parseInt(curveIndices[approachSegmentLocation - 1]) : -1;
            approachFirstIndex = approachFirstIndex  + + MathUtilities.boole(approachFirstIndex > 0);
            int approachLastIndex = approachPresent ? Integer.parseInt(curveIndices[approachSegmentLocation]) : -2;

            int withdrawFirstIndex = withdrawPresent ? Integer.parseInt(curveIndices[withdrawSegmentLocation - 1]) : -1;
            withdrawFirstIndex = withdrawFirstIndex + MathUtilities.boole(withdrawFirstIndex > 0);           
            int withdrawLastIndex = withdrawPresent  ? Integer.parseInt(curveIndices[withdrawSegmentLocation]) : -2;

            //SimpleSpectroscopyFile.setSensitivity() expects the value of spring constant to be
            //in microns per V, i.e. 10^6 times greater than the SI value
            double sensitivity = sensitivityString != null ? 1e6*Double.parseDouble(sensitivityString) : Double.NaN;        
            double springConstant = springConstantString != null ? Double.parseDouble(springConstantString) : Double.NaN;

            int approachLength = approachPresent ? approachLastIndex - approachFirstIndex + 1 : 0;
            int withdrawLength = withdrawPresent ? withdrawLastIndex - withdrawFirstIndex + 1 : 0;

            int curveCount = Math.max(1,waveHeader.getLayerCount());
            boolean multipleCurves = curveCount > 1;

            for(int i = 0; i<curveCount; i++)
            {
                double[][] curveData = channelData[i];

                double[] approachDataXs = new double[approachLength];
                double[] approachDataYs = new double[approachLength];

                double[] withdrawDataXs = new double[withdrawLength];
                double[] withdrawDataYs = new double[withdrawLength];

                for(int m = approachFirstIndex; m < approachLastIndex + 1; m++)
                {
                    approachDataXs[m] = -1e6*curveData[m][zIndex];
                    approachDataYs[m] = 1e6*curveData[m][deflectionIndex];
                }

                for(int m = withdrawFirstIndex; m < withdrawLastIndex + 1; m++)
                {
                    withdrawDataXs[m - withdrawFirstIndex] = -1e6*curveData[m][zIndex];
                    withdrawDataYs[m - withdrawFirstIndex] = 1e6*curveData[m][deflectionIndex];
                }

                String suffix = multipleCurves ? " (" + Integer.toString(i) + ")": "";

                String longName = f.getAbsolutePath() + suffix;
                String shortName = IOUtilities.getBareName(f) + suffix;

                Channel1DData approachChannelData = new FlexibleFlatChannel1DData(approachDataXs, approachDataYs, Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_MICRONS, SortedArrayOrder.DESCENDING);
                Channel1DData withdrawChannelData = new FlexibleFlatChannel1DData(withdrawDataXs, withdrawDataYs, Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_MICRONS, SortedArrayOrder.ASCENDING);

                StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachChannelData, withdrawChannelData);
                source.setSensitivity(sensitivity);
                source.setSpringConstant(springConstant);
                source.setRecordingPoint(recordingPosition);

                MapDelayedCreator mapDelayedCreator = (recordingPosition != null) ? new AsylumMapDelayedCreater(f.toPath().getParent().getParent(), rowCount, columnCount) : null;
                source.setMapDelayedCreator(mapDelayedCreator);

                sources.add(source);
            }
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;        
    }

    private static int getZPositionIndex(List<String> channelLabels)
    {
        if(channelLabels.contains(Z_SENSOR_POSITION_CHANNEL))
        {
            return channelLabels.lastIndexOf(Z_SENSOR_POSITION_CHANNEL);
        }
        if(channelLabels.contains(LVDT_POSITION_CHANNEL))
        {
            return channelLabels.lastIndexOf(LVDT_POSITION_CHANNEL);
        }
        if(channelLabels.contains(RAW_POSITION_CHANNEL))
        {
            return channelLabels.lastIndexOf(RAW_POSITION_CHANNEL);
        }
        return -1;
    }

    public static enum AsylumCurveSegmentType
    {
        APPROACH(Pattern.compile("\\s*1\\s*")), WITHDRAW(Pattern.compile("\\s*-1\\s*")), DWELL(Pattern.compile("\\s*0\\s*")), UNKNOWN(Pattern.compile(".*"));

        private final Pattern idPattern;

        AsylumCurveSegmentType(Pattern idPattern)
        {
            this.idPattern = idPattern;
        }

        public int getIndex(String[] directions)
        {
            for(int i = 0;i<directions.length;i++)
            {
                Matcher matcher = idPattern.matcher(directions[i]);

                if(matcher.matches())
                {
                    return i;
                }
            }

            return -1;
        }

        public static AsylumCurveSegmentType getInstance(String id)
        {
            for(AsylumCurveSegmentType type : AsylumCurveSegmentType.values())
            {
                Matcher matcher = type.idPattern.matcher(id);

                if(matcher.matches())
                {
                    return type;
                }
            }

            throw new IllegalArgumentException("No AsylumCurveSegmentType enum known for the identifier " + id);
        }
    }
}

