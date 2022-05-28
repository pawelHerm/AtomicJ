package atomicJ.readers.csinstruments;


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


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.csinstruments.CSIChannelSetData.CSIImageChannelData;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;

public class CSIImageReader extends AbstractSourceReader<ImageSource>
{    
    public static final String IMAGING_DATA_PATH = "Data/Imaging.xml";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"nao"};
    private static final String DESCRIPTION = "CS Instruments image file (.nao)";

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
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException 
    {      
        List<ImageSource> sources = new ArrayList<>();

        try(ZipFile zipFile = new ZipFile(f))
        {                  
            ZipEntry imagingDataEntry = zipFile.getEntry(IMAGING_DATA_PATH); 

            if(imagingDataEntry == null)
            {
                return sources;
            }

            InputStream dataStream = zipFile.getInputStream(imagingDataEntry);
            Document dataDocument = FileInputUtilities.readInXMLDocument(dataStream);
            CSIChannelSetData channelDataSet = CSIChannelSetData.readInChannelSetData(dataDocument);

            int wordSize = 4;

            if(channelDataSet.isGridSpecified())
            {
                List<ImageChannel> imageChannels = new ArrayList<>();

                Grid2D grid = channelDataSet.getGrid();
                int rowCount = grid.getRowCount();
                int columnCount = grid.getColumnCount();

                List<CSIImageChannelData> individualChannelData = channelDataSet.getIndividualChannelData();

                List<String> channelIdentifiers = new ArrayList<>();


                for(CSIImageChannelData chD : individualChannelData)
                {
                    String dataPath = chD.getDataPathRecognizableByZipFile();
                    boolean isTrace = chD.isTrace();

                    Quantity readInDataQuantity = chD.getZQuantity();
                    String bareIdentifier = chD.getIdentifier();
                    int nrOfIdenticalBareIds = Collections.frequency(channelIdentifiers, bareIdentifier);
                    channelIdentifiers.add(bareIdentifier);

                    String identifier = (nrOfIdenticalBareIds == 0) ? bareIdentifier : bareIdentifier + " (" + Integer.toString(nrOfIdenticalBareIds + 1) + ")"; 

                    ChannelFilter filter = readingDirective.getDataFilter();
                    if(filter.accepts(identifier, readInDataQuantity))
                    {
                        ZipEntry dataEntry = zipFile.getEntry(dataPath);

                        ByteBuffer initialDataBuffer = readInToByteBuffer(zipFile.getInputStream(dataEntry), dataEntry, 3*4);           

                        int width = initialDataBuffer.getInt();
                        int height = initialDataBuffer.getInt();
                        int numberOfLines = initialDataBuffer.getInt();                     

                        int bufferSize = wordSize*numberOfLines*(columnCount + 1) + 3*4;//3*4 is for three first ints
                        ByteBuffer dataBuffer = readInToByteBuffer(zipFile.getInputStream(dataEntry), dataEntry, bufferSize);           

                        dataBuffer.position(dataBuffer.position() + 3*4);

                        double[][] readInData = new double[rowCount][width];//we intentionally create all rows right at initiallization, i.e. new double[rowCount][width] instead of new double[rowCount][], because part of the data may be missing, so those rows have to be filled with zeroes 

                        for(int i = 0; i<numberOfLines; i++)
                        {
                            int lineno = dataBuffer.getInt();                                                               
                            readInData[lineno] = DoubleArrayReaderType.FLOAT32.readIn1DArray(width, 1, dataBuffer);
                        }

                        ImageChannel imageChannel = new ImageChannel(readInData, grid, readInDataQuantity, identifier, isTrace);

                        imageChannels.add(imageChannel);
                    }                    
                }

                ImageSource sourceFile = new StandardImageSource(f);
                sourceFile.setChannels(imageChannels);

                List<ImageSource> sourceFiles = Collections.singletonList(sourceFile);
                return sourceFiles; 
            }

            zipFile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
        return sources;
    }

    private static ByteBuffer readInToByteBuffer(InputStream inputStream, ZipEntry dataEntry, int bufferSize) throws UserCommunicableException
    {            
        try(ReadableByteChannel channel = Channels.newChannel(inputStream))
        {
            return FileInputUtilities.readBytesToBuffer(channel, bufferSize, ByteOrder.LITTLE_ENDIAN);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }
}

