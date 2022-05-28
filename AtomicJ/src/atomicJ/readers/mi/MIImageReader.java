
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.readers.mi;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.ImageChannel.ImageChannelBuilder;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;


public class MIImageReader extends AbstractSourceReader<ImageSource>
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"mi"};
    private static final String DESCRIPTION = "Agilent image (.mi)";

    private static final String BUFFER_LABEL = "bufferLabel";
    private static final String SCAN_UP = "scanUp";
    private static final String Y_LENGTH = "yLength";
    private static final String X_LENGTH = "xLength";
    private static final String Y_PIXELS = "yPixels";
    private static final String X_PIXELS = "xPixels";
    private static final String FILE_TYPE = "fileType";
    private static final String DIRECTION = "direction";
    private static final String FILTER = "filter";
    private static final String BUFFER_RANGE = "bufferRange";
    private static final String BUFFER_UNIT = "bufferUnit";
    private static final String DATA = "data";
    private static final String BINARY_32 = "BINARY_32";
    private static final String BINARY = "BINARY";

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
        return filter.accept(f);       
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalSpectroscopySourceException
    {    
        boolean isBinary = false;
        boolean isBinary32Bit = false;
        boolean isScanUp = false;

        int textLength = 0;

        try(BufferedReader bsr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) 
        {		
            double factorXY = 1e6;

            int rowCount = 1;
            int columnCount = 1;

            double xLength = 1;
            double yLength = 1;

            String line;

            List<String> labelBareIds = new ArrayList<>();
            List<ImageChannelBuilder> builders = new ArrayList<>();

            String firstLine = bsr.readLine();
            textLength = textLength + firstLine.length() + 1;

            if(firstLine.startsWith(FILE_TYPE))
            {			
                String[] splitted = firstLine.split("\\s+");				
                boolean isImage = "Image".equals(splitted[1]);

                if(!isImage)
                {
                    throw new IllegalSpectroscopySourceException();
                }
            }			

            while((line = bsr.readLine()) != null)
            {	
                textLength = textLength + line.length() + 1;

                if(line.startsWith(X_PIXELS))
                {				
                    String[] splitted = line.split("\\s+");				
                    rowCount = Integer.parseInt(splitted[1]);
                }
                else if(line.startsWith(Y_PIXELS))
                {							
                    String[] splitted = line.split("\\s+");			
                    columnCount = Integer.parseInt(splitted[1]);
                }
                else if(line.startsWith(X_LENGTH))
                {				
                    String[] splitted = line.split("\\s+");			
                    xLength = factorXY*Double.parseDouble(splitted[1]);
                }
                else if(line.startsWith(Y_LENGTH))
                {							
                    String[] splitted = line.split("\\s+");			
                    yLength = factorXY*Double.parseDouble(splitted[1]);
                }
                else if(line.startsWith(SCAN_UP))
                {
                    String[] splitted = line.split("\\s+");			
                    isScanUp = "TRUE".equals(splitted[1]);
                }
                else if(line.startsWith(BUFFER_LABEL))
                {
                    String[] splittedFirst = line.split("\\s+");		
                    String label = splittedFirst[1];

                    Map<String, String> entries = new Hashtable<>();
                    String insideLine;
                    while((insideLine = bsr.readLine()) != null)
                    {
                        textLength = textLength + insideLine.length() + 1;

                        String[] splitted = insideLine.split("\\s+");
                        String key = splitted[0];
                        String value = splitted[1];
                        entries.put(key, value);
                        if(key.equals("trace"))
                        {
                            break;
                        }
                    }

                    String filter = entries.get(FILTER);
                    String direction = entries.get(DIRECTION);
                    boolean isTrace = "Trace".equals(direction);

                    String bareIdentifier = label + (isTrace ? " trace" : " retrace");
                    int nrOfIdenticalBareIds = Collections.frequency(labelBareIds, bareIdentifier);

                    String identifier = nrOfIdenticalBareIds == 0 ? bareIdentifier : bareIdentifier + " (" + Integer.toString(nrOfIdenticalBareIds + 1) + ")"; 
                    labelBareIds.add(bareIdentifier);

                    String unitString = entries.get(BUFFER_UNIT);
                    PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);

                    double range = Double.parseDouble(entries.get(BUFFER_RANGE));

                    ImageChannelBuilder channelBuilder = new ImageChannelBuilder();

                    Quantity quantity = new UnitQuantity(identifier, unit);
                    channelBuilder.setZQuantity(quantity).setIdentifier(identifier).setFilter(filter).setRange(range).setTrace(isTrace);
                    builders.add(channelBuilder);
                }
                else if(line.startsWith(DATA))
                {
                    isBinary = line.contains(BINARY);
                    isBinary32Bit = line.contains(BINARY_32);
                    break;
                }
            }	

            double incrementX = xLength/(columnCount - 1);
            double incrementY = yLength/(rowCount - 1);

            Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

            ChannelFilter filter = readingDirective.getDataFilter();
            if(isBinary)
            {
                try (FileChannel fileChannel = (FileChannel)Files.newByteChannel(f.toPath()))
                {
                    fileChannel.position(textLength);
                    if(isBinary32Bit)
                    {                        
                        int bufferSize = 4*rowCount*columnCount*builders.size();					

                        ByteBuffer byteBuffer = FileInputUtilities.readBytesToBuffer(fileChannel, bufferSize, ByteOrder.LITTLE_ENDIAN);

                        for(ImageChannelBuilder imgBuilder: builders)
                        {       
                            if(filter.accepts(imgBuilder.getIdentifier(), imgBuilder.getQuantity()))
                            {
                                double[][] data = new double[rowCount][columnCount];
                                double range = imgBuilder.getRange();

                                for(int i = 0;i<rowCount; i++)
                                {   
                                    for(int j = 0; j<columnCount;j++)
                                    {
                                        double x = range*byteBuffer.getChar()/32768.;
                                        double y = (range*byteBuffer.getShort())/32768. + x;
                                        data[i][j] = y;
                                    }                       
                                }   

                                imgBuilder.setData(data).setGrid(grid);
                            }

                            //if a channel is filtered out, we have to skip the corresponding bytes
                            else
                            {
                                int position = byteBuffer.position();
                                int newPosition = position + 2*rowCount*columnCount;
                                byteBuffer.position(newPosition);
                            }
                        }	
                    }
                    else
                    {
                        int bufferSize = 2*rowCount*columnCount*builders.size();					

                        ByteBuffer byteBuffer = FileInputUtilities.readBytesToBuffer(fileChannel, bufferSize, ByteOrder.LITTLE_ENDIAN);

                        for(ImageChannelBuilder imgBuilder: builders)
                        {  
                            if(filter.accepts(imgBuilder.getIdentifier(), imgBuilder.getQuantity()))
                            {
                                double[][] data = new double[rowCount][columnCount];
                                double range = imgBuilder.getRange();

                                for(int i = 0;i<rowCount; i++)
                                {
                                    for(int j = 0; j<columnCount; j++)
                                    {
                                        byte fractionalPart = byteBuffer.get();
                                        int unisgnedFractionalPart = (0x000000FF & fractionalPart);
                                        byte integerPart = byteBuffer.get();

                                        double x = range*(integerPart + unisgnedFractionalPart/256.)/128.;

                                        data[i][j] = x;
                                    }

                                }   
                                imgBuilder.setData(data).setGrid(grid);
                            }

                            //if a channel is filtered out, we have to skip the corresponding bytes
                            else
                            {
                                int position = byteBuffer.position();
                                int newPosition = position + 2*rowCount*columnCount;
                                byteBuffer.position(newPosition);
                            }
                        }		
                    }						
                }
            }
            else
            {
                try(Scanner scanner = new Scanner(bsr);) 
                {
                    scanner.useLocale(Locale.US);		            
                    for(ImageChannelBuilder imgBuilder: builders)
                    {  
                        if(filter.accepts(imgBuilder.getIdentifier(), imgBuilder.getQuantity()))
                        {
                            double[][] data = new double[rowCount][columnCount];

                            for(int i = 0;i<rowCount; i++)
                            {
                                for(int j = 0; j<columnCount; j++)
                                {
                                    data[i][j]  = scanner.nextDouble();

                                }                           
                            }   

                            imgBuilder.setData(data).setGrid(grid);
                        }
                        //if a channel is filtered out, we have to skip the corresponding doubles
                        else
                        {
                            for(int i = 0;i<rowCount; i++)
                            {
                                for(int j = 0; j<columnCount; j++)
                                {
                                    scanner.nextDouble();

                                }                           
                            }   
                        }
                    }           	
                } 			
            }

            List<ImageChannel> imageChannels = new ArrayList<>();
            for(ImageChannelBuilder builder: builders)
            {
                if(filter.accepts(builder.getIdentifier(), builder.getQuantity()))
                {
                    imageChannels.add(builder.build());
                }
            }

            ImageSource sourceFile = new StandardImageSource(f);
            sourceFile.setChannels(imageChannels);

            List<ImageSource> sourceFiles = Collections.singletonList(sourceFile);

            return sourceFiles;	
        } 
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);		
        } 
    }
}

