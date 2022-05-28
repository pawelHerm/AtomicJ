
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

package atomicJ.readers.nanopuller;

import java.io.*;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReader;
import atomicJ.sources.ChannelSource;

public class NanopullerSourceReader implements SourceReader<ChannelSource>
{	
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"txt"};
    private static final String DESCRIPTION = "Nanopuller file (.txt)";

    private final NanopullerSpectroscopyReader spectroscopyReader = new NanopullerSpectroscopyReader();

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
        return spectroscopyReader.accept(f);       
    }

    @Override
    public List<ChannelSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingState) throws UserCommunicableException, IllegalImageException
    {          
        List<ChannelSource> sources = new ArrayList<>();

        if(spectroscopyReader.accept(f))
        {
            sources.addAll(spectroscopyReader.readSources(f, readingState));   
        }

        return sources;
    }

    @Override
    public boolean prepareSourceReader(List<File> files) throws UserCommunicableException 
    {
        return spectroscopyReader.prepareSourceReader(files);
    }
}

