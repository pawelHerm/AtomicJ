
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

package atomicJ.readers.gwyddionSimple;

import java.io.*;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class GwyddionGsfSourceReader extends AbstractSourceReader<ChannelSource>
{	
    public static final String GSF_MAGIC_LINE_STRING = "Gwyddion Simple Field 1.0\n";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"gsf"};
    private static final String DESCRIPTION = "Gwyddion simple format files (.gsf)";

    private final GwyddionGsfImageReader imageReader = new GwyddionGsfImageReader();

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
    public List<ChannelSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException
    {    	
        if(!imageReader.accept(f) )
        {
            throw new UserCommunicableException("File format is not supported");
        }

        List<ChannelSource> sources = new ArrayList<>();

        if(imageReader.accept(f))
        {
            List<ImageSource> imageSources = imageReader.readSources(f, readingDirective);
            sources.addAll(imageSources);
        }


        return sources; 
    }
}

