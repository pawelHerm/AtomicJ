
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

package atomicJ.readers.nanoscope;

import java.io.*;
import java.util.*;
import atomicJ.data.ImageChannel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;

public class NanoscopeImageReader extends AbstractSourceReader<ImageSource>
{
    //The file header usually ends in "\*File list end". This is true for force curves and images
    //However, in .spm files this may not be the case. For example, in the file Bloodcell.spm
    //distributed together with the Nanoscope software there is not "\*File list end" line at the end of the header
    //It seems that the line  carriage return (u000D) - line feed (u000A) - substitution character (u001A)
    //always flanks the header

    //as we read header line by line, using BufferedReader's method readLine(), we can detect end of 
    //text header by testing each line whether it starts with \u001A character (carriage return followed
    //by line feed is regarded as line delimiter and eaten up by the readLine() method)

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"([-0-9]++[0-9.Ee-]*+)", "pfc", "spm"};
    private static final String DESCRIPTION = "Nanoscope image file (.001, 0.002, ..., .pfc, spm)";

    private static final String FILE_LIST_END = "\\*File list end";

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
        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(ACCEPTED_EXTENSIONS);
        return filter.accept(f);       
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {	                
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1"));) 
        {
            List<NanoscopeImageData> allImageData = new ArrayList<>();

            NanoscopeFileList fileList = new NanoscopeFileList();

            //we have to read the reference fields in the headers Scan List and Scanner List
            // in the Scanner List, there is the field "Zsens", necessary ex for height images
            //while in the Scan List there are soft scales for QNM images
            NanoscopeScanList scanList = new NanoscopeScanList();
            NanoscopeScannerList scannerList = new NanoscopeScannerList();

            String line;
            while((line = reader.readLine()) != null)
            {
                if(fileList.isSectionBeginning(line))
                {   
                    line = fileList.readInFieldsToHeaderSection(reader);
                } 
                if(scannerList.isSectionBeginning(line))
                {                    
                    line = scannerList.readInFieldsToHeaderSection(reader);
                } 
                if(scanList.isSectionBeginning(line))
                {                    
                    line = scanList.readInFieldsToHeaderSection(reader);
                }      
                if(NanoscopeImageData.isSectionBeginningStatic(line))
                {
                    while(line != null && (NanoscopeImageData.isSectionBeginningStatic(line)))
                    {
                        NanoscopeImageData data = new NanoscopeImageData();
                        allImageData.add(data);
                        line = data.readInFieldsToHeaderSection(reader);
                    }

                }

                if(FILE_LIST_END.equals(line))
                {                    
                    break;
                }
            }

            List<ImageChannel> imageChannels = new ArrayList<>();

            List<String> referenceFields = new ArrayList<>();

            referenceFields.addAll(scannerList.getReferenceFields());
            referenceFields.addAll(scanList.getReferenceFields());

            for(NanoscopeImageData d : allImageData)
            {
                d.readInReferenceFields(referenceFields);
                d.setScanSize(scanList.getScanSize());
                d.setNanoscopeVersion(fileList.getVersion());

                boolean specified = d.isFullySpecified();

                if(specified)
                {
                    ImageChannel channel = d.buildImageChannel(f);
                    imageChannels.add(channel);
                }             

                if(readingDirective.isCanceled())
                {
                    return Collections.emptyList();
                }              
            }

            List<ImageSource> sourceFiles = new ArrayList<>();


            if(!imageChannels.isEmpty())
            {
                ImageSource sourceFile = new StandardImageSource(f);
                sourceFile.setChannels(imageChannels);   

                sourceFiles.add(sourceFile);
            }      

            return sourceFiles;	
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);		
        } 
    }
}

