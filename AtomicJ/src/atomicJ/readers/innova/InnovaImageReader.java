
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Paweł Hermanowicz
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

package atomicJ.readers.innova;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;


public class InnovaImageReader extends AbstractSourceReader<ImageSource>
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"FLT","flt"};
    private static final String DESCRIPTION = "Innova image (.FLT)";

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
        List<ImageSource> imageSources = new ArrayList<>();

        try(FileChannel channel = (FileChannel) Files.newByteChannel(f.toPath());Scanner fileScanner = new Scanner(channel,"ISO-8859-1"))
        {            
            fileScanner.useDelimiter("\\Z");
            fileScanner.useLocale(Locale.US);    

            String header = fileScanner.next();

            // Read in header, creating a list of NIDSections using {@link atomicJ.readers.nanosurf.NIDSection#INIT_SECTION_PATTERN} as a delimeter
            Map<String, InnovaINISection> sectionMap = new LinkedHashMap<>();

            try(Scanner headerScanner = new Scanner(header))
            {
                while(true)
                {
                    String sectionString = headerScanner.findWithinHorizon(InnovaINISection.INIT_SECTION_PATTERN, 0);

                    if(sectionString == null)
                    {
                        break;
                    }

                    InnovaINISection section = InnovaINISection.build(sectionString);               
                    sectionMap.put(section.getName(), section);
                }

                InnovaINISection dataSection = sectionMap.get(InnovaImageDataParameters.NAME);
                InnovaImageDataParameters dataParameters = new InnovaImageDataParameters(dataSection);               

                String identifier = dataParameters.getDataName();
                Quantity zQuantity = dataParameters.getZQuantity();

                ChannelFilter channelFilter = readingDirective.getDataFilter();

                if(channelFilter.accepts(identifier, zQuantity))
                {
                    int dataOffset = dataParameters.getDataOffset();

                    int rowCount = dataParameters.getResolutionY();
                    int columnCount = dataParameters.getResolutionX();

                    int byteCount = 4*rowCount*columnCount;

                    channel.position(dataOffset);

                    ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, byteCount, ByteOrder.LITTLE_ENDIAN);

                    DoubleArrayReaderType reader = DoubleArrayReaderType.FLOAT32;

                    double zScale = dataParameters.getZScale();
                    double[][] data = reader.readIn2DArrayRowByRow(rowCount, columnCount, zScale, buffer);
                    Grid2D grid = dataParameters.getGrid();

                    ImageChannel ch = new ImageChannel(data, grid, zQuantity, identifier,  true);

                    ImageSource sourceFile = new StandardImageSource(f);                  
                    sourceFile.setChannels(Collections.singletonList(ch));

                    imageSources.add(sourceFile);
                }             
            }

        } catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }

        return imageSources;
    }
}

