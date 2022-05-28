
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
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class InnovaSourceReader extends AbstractSourceReader<ChannelSource>
{   
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"FLT","dat"};
    private static final String DESCRIPTION = "Innova files (.FLT,.dat)";

    private final InnovaSpectroscopyReader spectroscopyReader = new InnovaSpectroscopyReader();
    private final InnovaImageReader imageReader = new InnovaImageReader();

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
    public List<ChannelSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalSpectroscopySourceException, IllegalImageException
    {       
        List<ChannelSource> sources = new ArrayList<>();

        if(imageReader.accept(f))
        {
            sources.addAll(imageReader.readSources(f, readingDirective));
        }
        if(spectroscopyReader.accept(f))
        {
            sources.addAll(spectroscopyReader.readSources(f, readingDirective));
        }

        return sources;
    }
}

