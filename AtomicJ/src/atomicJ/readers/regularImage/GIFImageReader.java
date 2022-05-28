
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

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class GIFImageReader extends RegularImageReader
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"GIF","gif"};
    private static final String DESCRIPTION = "GIF image (.gif)";

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
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalSpectroscopySourceException
    {   
        try( ImageInputStream iis = ImageIO.createImageInputStream(f);)
        {
            Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("gif");
            ImageReader reader = iterator.next();
            reader.setInput(iis, true);

            ImageReadParam param = reader.getDefaultReadParam();

            // reader.getNumImages(true);

            ChannelProvider image = new BufferedImageChannelProvider(reader.read(0, param));

            return readImages(f, readingDirectives, image);

        } catch (IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }
    }
}

