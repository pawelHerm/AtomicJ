
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
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;

/**
 * A reader to open the nanosurf (.nid) spectroscopy files. All Sourcefiles are read by a subclass
 * of SourceReader, where SimpleSpectroscopySource is used for single force curves.  
 * 
 * When reading in spectroscopy files, the readSourceFile() Methode is called. 
 * 
 * @author Pawel Hermanowicz, Marcel Neidinger
 *
 */
public class NIDSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String HEADER_END = "#!";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"nid"};
    private static final String DESCRIPTION = "Nanosurf force curve file (.nid)";

    /**
     * Return accepted extensions stored in ACCEPTED_EXTENSIONS constants
     * @return An array of accepted extensions
     */
    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    /**
     * Return a description
     * @return A description string 
     */
    public static String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    /**
     * Check if a file is readable by this reader, using the file extension
     * 
     */
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    @Override
    /**
     * Read source file. First, the header is parsed to get all relevant information. 
     */
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException 
    {      
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel) Files.newByteChannel(f.toPath());Scanner fileScanner = new Scanner(channel,"ISO-8859-1"))
        {            
            fileScanner.useLocale(Locale.US);    
            fileScanner.useDelimiter(HEADER_END);

            String header = fileScanner.next();
            int headerLength = header.length();

            // Read in header, creating a list of NIDSections using {@link atomicJ.readers.nanosurf.NIDSection#INIT_SECTION_PATTERN} as a delimeter
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

            // Create a dataset to build the spectroscopy source on and parse in the actual data. See {@link atomicJ.readers.nanosurf.NIDDataset#build()} next. 
            NIDDataset dataset = NIDDataset.build(sectionMap);

            int elementCount = dataset.getSpectroscopyElementCount();
            SourceReadingState state  = elementCount > 10 ? new SourceReadingStateMonitored(elementCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) : new SourceReadingStateMute(elementCount);   

            try
            {
                List<SimpleSpectroscopySource> readInSources = dataset.readInSpectroscopyDataAndSkipOthers(channel, f, readingDirectives, state);
                sources.addAll(readInSources);
            } 
            catch (Exception e) 
            {
                state.setOutOfJob();
                throw new UserCommunicableException("Error has occured while reading a NID recording", e);
            }             

        } catch (Exception e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }

        return sources; 
    }

    //        int n = 0;
    //        MapReadingState state  = n > 10  ? new MapReadingStateMonitored(n) : new MapReadingStateMute(n);   

}

