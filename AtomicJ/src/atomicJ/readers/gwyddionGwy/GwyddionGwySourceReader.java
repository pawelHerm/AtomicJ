
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

package atomicJ.readers.gwyddionGwy;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;


public class GwyddionGwySourceReader extends AbstractSourceReader<ChannelSource>
{	
    public static final String TITILE_COMPONENT_NAME_SUFFIX = "title";
    public static final String COMPONENT_NAME_SEPARATOR = "/";
    public static final String GWYP_MAGIC_NUMBER_STRING = "GWYP";
    public static final String GWY_CONTAINER_NAME = "GwyContainer";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"gwy"};
    private static final String DESCRIPTION = "Gwyddion native format files (.gwy)";

    private final GwyddionGwyImageReader imageReader = new GwyddionGwyImageReader();
    private final GwyddionGwySpectroscopyReader spectroscopyReader = new GwyddionGwySpectroscopyReader();

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

    public static ByteBuffer readGwyContainerData(FileChannel channel) throws UserCommunicableException
    {
        ByteBuffer magicBuffer = FileInputUtilities.readBytesToBuffer(channel, GWYP_MAGIC_NUMBER_STRING.length(), ByteOrder.LITTLE_ENDIAN); 
        String magic = StandardCharsets.UTF_8.decode(magicBuffer).toString();

        if(!GwyddionGwySourceReader.GWYP_MAGIC_NUMBER_STRING.equals(magic))
        {
            throw new UserCommunicableException("The magic number is not " + GwyddionGwySourceReader.GWYP_MAGIC_NUMBER_STRING);
        }          

        ByteBuffer topLevelContainerBuffer = FileInputUtilities.readBytesToBuffer(channel, 17, ByteOrder.LITTLE_ENDIAN); 

        String topLevelContainerName = FileInputUtilities.readInNullTerminatedString(topLevelContainerBuffer);

        if(!GwyddionGwySourceReader.GWY_CONTAINER_NAME.equals(topLevelContainerName))
        {
            throw new UserCommunicableException("The top level container is not a GwyContainer");
        }

        long gwyContainerDataSize = FileInputUtilities.getUnsigned(topLevelContainerBuffer.getInt());

        int gwyContainerDataSizeInt = (int)gwyContainerDataSize;

        ByteBuffer gwyContainerData = FileInputUtilities.readBytesToBuffer(channel, gwyContainerDataSizeInt, ByteOrder.LITTLE_ENDIAN);

        return gwyContainerData;
    }

    @Override
    public List<ChannelSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException
    {    	

        if(!imageReader.accept(f) && !spectroscopyReader.accept(f))
        {
            throw new UserCommunicableException("File format is not supported");
        }

        List<ChannelSource> sources = new ArrayList<>();

        if(imageReader.accept(f))
        {
            List<ImageSource> imageSources = imageReader.readSources(f, readingDirective);
            sources.addAll(imageSources);
        }
        if(spectroscopyReader.accept(f))
        {
            List<SimpleSpectroscopySource> imageSources = spectroscopyReader.readSources(f, readingDirective);
            sources.addAll(imageSources);
        }

        return sources; 
    }
}

