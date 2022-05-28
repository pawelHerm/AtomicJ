
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

package atomicJ.readers.regularImage;


import java.io.*;
import java.util.*;

import loci.formats.FormatException;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.BioRadReader;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class BioRadImageReader extends RegularImageReader
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"pic"};
    private static final String DESCRIPTION = "BioRad image (.pic)";

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
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalSpectroscopySourceException
    {       
        BufferedImageReader imageReader = new BufferedImageReader(new BioRadReader());
        try
        {
            imageReader.setId(f.getPath());
            int num = imageReader.getImageCount();

            ChannelProvider[] img = new ChannelProvider[num];
            for (int i=0; i<num; i++) 
            {
                img[i] = new BufferedImageChannelProvider( imageReader.openImage(i));
            }

            imageReader.close(true);

            return readImages(f, readingDirectives, img);

        } catch (FormatException | IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }
    }
}

