
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

import java.io.*;
import java.util.*;

import atomicJ.readers.AbstractSourceReader;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class AsylumSourceReader extends AbstractSourceReader<ChannelSource>
{	
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"ibw", "bwav"};
    private static final String DESCRIPTION = "Asylum files (.ibw, .bwav)";

    private final AsylumImageReader imageReader = new AsylumImageReader();
    private final AsylumSpectroscopyReader spectroscopyReader = new AsylumSpectroscopyReader();

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
    public List<ChannelSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective)
    {    	
        List<ChannelSource> sources = new ArrayList<>();

        try
        {
            if(imageReader.canBeImage(f))
            {
                List<ImageSource> imageSources = imageReader.readSources(f, readingDirective);
                sources.addAll(imageSources);
            }
            else
            {
                List<SimpleSpectroscopySource> spectroscopySources = spectroscopyReader.readSources(f, readingDirective);
                sources.addAll(spectroscopySources);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return sources; 
    }
}

