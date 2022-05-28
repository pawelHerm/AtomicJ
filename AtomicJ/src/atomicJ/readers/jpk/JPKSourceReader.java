
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

package atomicJ.readers.jpk;

import java.io.*;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;



public class JPKSourceReader extends AbstractSourceReader<ChannelSource>
{	
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"force", "jpk", "jpk-force","jpk-force-map", "jpk-qi-image", "out"};
    private static final String DESCRIPTION = "JPK files (.force, .jpk, .jpk-force, .jpk-force-map, .jpk-qi-image, .out)";

    private static final String JPK_QI_IMAGE = "jpk-qi-image";
    private static final String JPK = "jpk";
    private static final String FORCE = "force";

    private final JPKQIImageReader qiImageReader = new JPKQIImageReader();
    private final JPKImageReader reguarImageReader = new JPKImageReader();
    private final JPKSpectroscopyReader spectroscopyReader = new JPKSpectroscopyReader();

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
    public List<ChannelSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException
    {    		
        String extension = IOUtilities.getExtension(f);


        List<ChannelSource> sources = new ArrayList<>();

        if(JPK.equals(extension) || FORCE.equals(extension))
        {
            List<ImageSource> imageSources = reguarImageReader.readSources(f, readingDirective);
            sources.addAll(imageSources);
        }
        else if(JPK_QI_IMAGE.equals(extension))
        {
            List<ImageSource> imageSources = qiImageReader.readSources(f, readingDirective);
            sources.addAll(imageSources);
        }
        else
        {
            List<SimpleSpectroscopySource> spectroscopySources = spectroscopyReader.readSources(f, readingDirective);
            sources.addAll(spectroscopySources);
        }

        return sources; 
    }
}

