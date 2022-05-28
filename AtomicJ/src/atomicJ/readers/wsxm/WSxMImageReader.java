package atomicJ.readers.wsxm;


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


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

import atomicJ.data.Datasets;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.ImageChannel.ImageChannelBuilder;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;


public class WSxMImageReader extends AbstractSourceReader<ImageSource>
{
    private static final String HEADER_END = "Header end";
    private static final String IMAGE_SIGNATURE = "SxM Image file";
    private static final String CURVE_SIGNATURE = "FZ curve file";
    private static final String GENERAL_CURVE_SIGNATURE = "Generic curve file";

    //units and labels of axes are under this title
    private static final String GENERAL_INFO_TITLE = "General Info";

    private static final String ACQUISITION_CHANNEL = "Acquisition channel";
    private static final String X_SCANNING_DIRECTION = "X scanning direction";
    private static final String IMAGE_DATA_TYPE = "Image Data Type";
    private static final String IMAGE_PROCESS = "Image processes";
    private static final String NUMBER_OF_COLUMNS = "Number of columns";
    private static final String NUMBER_OF_ROWS = "Number of rows";
    private static final String Z_AMPLITUDE = "Z Amplitude";

    private static final String BACKWARD_X_DIRECTION = "Backward";

    private static final String CONTROL_TITLE = "Control";

    private static final String Z_GAIN = "Z Gain";
    private static final String X_AMPLITUDE = "X Amplitude";
    private static final String X_OFFSET = "X Offset";
    private static final String Y_AMPLITUDE = "Y Amplitude";
    private static final String Y_OFFSET = "Y Offset";

    private static final String IMAGE_HEADER_SIZE = "Image header size";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"stp"};
    private static final String DESCRIPTION = "WSxM image file (.stp)";

    final static Pattern squareBracketsPattern = Pattern.compile("\\[(.*?)\\]");

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
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingState) throws UserCommunicableException, IllegalSpectroscopySourceException 
    {           
        UnitExpression xAmplitude = new UnitExpression(1, SimplePrefixedUnit.getNullInstance());
        UnitExpression yAmplitude = new UnitExpression(1, SimplePrefixedUnit.getNullInstance());
        UnitExpression zAmplitude = new UnitExpression(1, SimplePrefixedUnit.getNullInstance());

        UnitExpression xOffset = new UnitExpression(0, SimplePrefixedUnit.getNullInstance());
        UnitExpression yOffset = new UnitExpression(0, SimplePrefixedUnit.getNullInstance());

        int rowCount = 0;
        int columnCount = 0;

        int imageHeaderSize = 0;

        boolean xDirectionBackward = true;
        String identifier = "Image";
        WsXMImageType imageDataType = null;

        //"ISO8859-1"
        try(BufferedReader bsr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) 
        {
            bsr.readLine(); //we skip first line, it always should be "WSxM file copyright Nanotec Electronica"
            String signature = bsr.readLine();

            boolean isSpectroscopy = CURVE_SIGNATURE.equals(signature) || GENERAL_CURVE_SIGNATURE.equals(signature);
            if(isSpectroscopy)
            {
                throw new IllegalSpectroscopySourceException();
            }

            String line; 
            while((line = bsr.readLine().trim()) != null)
            {
                Matcher titleMatcher = squareBracketsPattern.matcher(line);
                boolean titleMatches = titleMatcher.matches();
                if(titleMatches)
                {
                    String title = titleMatcher.group(1);
                    if(HEADER_END.equals(title))
                    {
                        break;
                    }
                } 
                if(line.startsWith(IMAGE_HEADER_SIZE))
                {
                    imageHeaderSize = Integer.parseInt(line.split(":")[1].trim());
                }
                if(line.startsWith(X_AMPLITUDE))
                {      
                    String[] words = line.split(":");
                    xAmplitude = UnitExpression.parse(words[1]).derive(Units.MICRO_METER_UNIT);
                }
                else if(line.startsWith(Y_AMPLITUDE))
                {
                    String[] words = line.split(":");
                    yAmplitude = UnitExpression.parse(words[1]).derive(Units.MICRO_METER_UNIT);
                }
                else if(line.startsWith(Z_AMPLITUDE))
                {
                    String[] words = line.split(":");
                    zAmplitude = UnitExpression.parse(words[1]).deriveSimpleForm();
                }
                else if(line.startsWith(X_OFFSET))
                {
                    String[] words = line.split(":");
                    xOffset = UnitExpression.parse(words[1]);
                }
                else if(line.startsWith(Y_OFFSET))
                {                   
                    String[] words = line.split(":");
                    yOffset = UnitExpression.parse(words[1]);
                }
                else if(line.startsWith(NUMBER_OF_COLUMNS))
                {                    
                    columnCount = Integer.parseInt(line.split(":")[1].trim());
                }
                else if(line.startsWith(NUMBER_OF_ROWS))
                {                    
                    rowCount = Integer.parseInt(line.split(":")[1].trim());
                }
                else if(line.startsWith(X_SCANNING_DIRECTION))
                {
                    String[] words = line.split(":");
                    xDirectionBackward = BACKWARD_X_DIRECTION.equals(words[1].trim());
                }
                else if(line.startsWith(ACQUISITION_CHANNEL))
                {
                    String[] words = line.split(":");
                    identifier = words[1].trim();
                }
                else if(line.startsWith(IMAGE_DATA_TYPE))
                {
                    String[] words = line.split(":");
                    imageDataType = WsXMImageType.getImageType(words[1].trim());
                }
            }

            double incrementX = xAmplitude.getValue()/(columnCount - 1);
            double incrementY = yAmplitude.getValue()/(rowCount - 1);
            double xOffsetMicron = xOffset.derive(xAmplitude.getUnit()).getValue();
            double yOffsetMicron = yOffset.derive(yAmplitude.getUnit()).getValue();

            Grid2D grid = new Grid2D(incrementX, incrementY, xOffsetMicron, yOffsetMicron, rowCount, columnCount,
                    new UnitQuantity(Datasets.DISTANCE , xAmplitude.getUnit()), new UnitQuantity(Datasets.DISTANCE, yAmplitude.getUnit()));

            ImageChannelBuilder builder = new ImageChannelBuilder();
            builder.setRange(zAmplitude.getValue());

            Quantity quantity = new UnitQuantity(identifier, zAmplitude.getUnit());
            builder.setZQuantity(quantity);

            builder.setIdentifier(identifier);

            try (FileChannel fileChannel = (FileChannel)Files.newByteChannel(f.toPath()))
            {
                fileChannel.position(imageHeaderSize);

                double[][] data = imageDataType.readIn(fileChannel, xDirectionBackward, rowCount, columnCount);
                builder.setData(data).setGrid(grid);     
            }


            List<ImageChannel> imageChannels = new ArrayList<>();
            imageChannels.add(builder.build());

            ImageSource source = new StandardImageSource(f);
            source.setChannels(imageChannels);

            List<ImageSource> sourceFiles = Collections.singletonList(source);

            return sourceFiles; 
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 
    }


    private static enum WsXMImageType
    {                
        DOUBLE("double", DoubleArrayReaderType.FLOAT64), FLOAT("float", DoubleArrayReaderType.FLOAT32), SHORT("short", DoubleArrayReaderType.INT16);

        private final String name;
        private final DoubleArrayReaderType readerType;

        WsXMImageType(String name, DoubleArrayReaderType readerType)
        {
            this.name = name;
            this.readerType = readerType;
        }

        public double[][] readIn(FileChannel fileChannel, boolean xDirectionBackward, int rowCount, int columnCount) throws UserCommunicableException
        {
            int bufferSize = readerType.getByteSize()*rowCount*columnCount;                    

            ByteBuffer dataBuffer = FileInputUtilities.readBytesToBuffer(fileChannel, bufferSize, ByteOrder.LITTLE_ENDIAN);

            double[][] data = new double[rowCount][];
            for(int i = 0;i<rowCount; i++)
            {   
                data[i] = xDirectionBackward ? readerType.readIn1DArrayReversed(columnCount, 1, 0, dataBuffer) : readerType.readIn1DArray(columnCount, 1, dataBuffer); 
            }   

            return data;
        }

        public static WsXMImageType getImageType(String type)
        {
            for(WsXMImageType imageType : WsXMImageType.values())
            {
                if(imageType.name.equals(type))
                {
                    return imageType;
                }
            }

            throw new IllegalArgumentException("No image type corresponds to " + type);
        }
    }
}


