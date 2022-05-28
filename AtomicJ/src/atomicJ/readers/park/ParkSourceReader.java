
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

package atomicJ.readers.park;

import java.io.*;
import java.util.*;

import loci.common.RandomAccessInputStream;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffParser;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;



public class ParkSourceReader extends AbstractSourceReader<ChannelSource>
{	
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"tif","tiff"};
    private static final String DESCRIPTION = "Park files (.tif, .tiff)";

    private final ParkImageReader imageReader = new ParkImageReader();
    private final ParkSpectroscopyReader spectroscopyReader = new ParkSpectroscopyReader();


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
        boolean isParkTiff = filter.accept(f) && ParkSourceReader.checkIfParkTiff(f);      

        return isParkTiff;     
    }

    @Override
    public List<ChannelSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException
    {    		        
        List<ChannelSource> sources = new ArrayList<>();
        if(imageReader.accept(f))
        {
            List<ImageSource> imageSources = imageReader.readSources(f, readingDirectives);
            sources.addAll(imageSources);
        }
        if(spectroscopyReader.accept(f) && spectroscopyReader.isSpectroscopy(f))
        {
            List<SimpleSpectroscopySource> readIn = spectroscopyReader.readSources(f, readingDirectives);
            sources.addAll(readIn);
        }

        return sources;
    }

    public static boolean checkIfParkTiff(File f)
    {
        boolean isParkTiff = false;

        try(RandomAccessInputStream in = new RandomAccessInputStream(f.getAbsolutePath()))
        {
            TiffParser parser = new TiffParser(new RandomAccessInputStream(f.getAbsolutePath()));

            IFD firstIFD = parser.getFirstIFD();     

            Object value = firstIFD.getIFDValue(ParkImageReader.TAG_PARK_MAGIC_NUMBER);
            isParkTiff = value != null && (ParkImageReader.PARK_MAGIC_NUMBER == ((Number)value).longValue());

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return isParkTiff;
    }
}

