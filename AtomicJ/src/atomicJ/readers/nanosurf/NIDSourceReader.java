
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

package atomicJ.readers.nanosurf;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class NIDSourceReader extends AbstractSourceReader<ChannelSource>
{   
    private static final String HEADER_END = "#!";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"nid"};
    private static final String DESCRIPTION = "Nanosurf files (.nid) (v2)";

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
    public List<ChannelSource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalSpectroscopySourceException, IllegalImageException
    {       
        List<ChannelSource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel) Files.newByteChannel(f.toPath());Scanner fileScanner = new Scanner(channel,"ISO-8859-1") )
        {                   
            fileScanner.useLocale(Locale.US);    
            fileScanner.useDelimiter(HEADER_END);

            String header = fileScanner.next();
            int headerLength = header.length();

            Map<String, INISection> sectionMap = new LinkedHashMap<>();

            try(Scanner headerScanner = new Scanner(header))
            {
                while(true)
                {
                    String sectionString = headerScanner.findWithinHorizon(INISection.INIT_SECTION_PATTERN, 0);

                    if(sectionString == null)
                    {
                        break;
                    }

                    INISection section = INISection.build(sectionString);               
                    sectionMap.put(section.getName(), section);
                }
            }


            channel.position(headerLength + 2); //2 bytes for the delimiter "#!"

            NIDDataset dataset = NIDDataset.build(sectionMap);      

            int elementCount = dataset.getReadableElementCount();
            SourceReadingState state  = elementCount > 10  ? new SourceReadingStateMonitored(elementCount, SourceReadingStateMonitored.SOURCE_PROBLEM) : new SourceReadingStateMute(elementCount);   

            try{

                List<ChannelSource> readInSources = dataset.readInData(channel, f, readingDirectives, state);
                sources.addAll(readInSources);
            } 
            catch (Exception e) 
            {
                state.setOutOfJob();
                throw new UserCommunicableException("Error has occured while reading a NID file", e);
            }          
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;
    }
}

