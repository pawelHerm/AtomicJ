
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

package atomicJ.readers.afmworkshop;

import java.io.*;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class AFMWorkshopSourceReader extends AbstractSourceReader<ChannelSource>
{	
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"wsf", "csv"};
    private static final String DESCRIPTION = "AFMWorkshop file (.wsf, .csv)";

    private final AFMWorkshopImageReader imageReader = new AFMWorkshopImageReader();
    private final AFMWorkshopSpectroscopyReader spectroscopyReader = new AFMWorkshopSpectroscopyReader();


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
    public List<ChannelSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException
    {          
        List<ChannelSource> sources = new ArrayList<>();

        if(spectroscopyReader.accept(f))
        {
            try 
            {
                sources.addAll(spectroscopyReader.readSources(f, readingDirective));
            } catch (IllegalImageException e)
            {
                e.printStackTrace();
            }
        }

        if(imageReader.accept(f))
        {
            try 
            {
                sources.addAll(imageReader.readSources(f, readingDirective));
            } catch (IllegalSpectroscopySourceException e) {
                e.printStackTrace();
            }
        }

        return sources;
    }
}

