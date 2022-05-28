
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

package atomicJ.readers.mdt;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

import atomicJ.data.ChannelFilter;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class MDTImageReader extends AbstractSourceReader<ImageSource>
{
    public static final int IMAGE_FRAME_CODE = 0;

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"mdt"};
    private static final String DESCRIPTION = "NT-MDT image file (.mdt)";

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    public boolean acceptInquisitive(File f) 
    {
        boolean canBeImage = false;

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {

            MDTFileHeader fileHeader = MDTFileHeader.readIn(channel);

            long nextFramePosition = channel.position();

            for(int frameIndex = 0; frameIndex <= fileHeader.getLastFrameIndex(); frameIndex++)
            {

                channel.position(nextFramePosition);

                MDTFrameHeader frameHeader = MDTFrameHeader.readIn(channel);
                nextFramePosition = nextFramePosition + frameHeader.getTotalFrameSize();
                canBeImage = canBeImage || frameHeader.getFrameType().canContainImageSources();

                if(canBeImage)
                {
                    break;
                }
            } 

        } catch (IOException | UserCommunicableException e) {
            e.printStackTrace();
        }

        return canBeImage;
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {           
        List<ImageSource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {
            MDTFileHeader fileHeader = MDTFileHeader.readIn(channel);

            long nextFramePosition = channel.position();

            for(int frameIndex = 0; frameIndex <= fileHeader.getLastFrameIndex(); frameIndex++)
            {
                if(readingDirective.isCanceled())
                {
                    break;
                }

                channel.position(nextFramePosition);

                MDTFrameHeader frameHeader = MDTFrameHeader.readIn(channel);
                nextFramePosition = nextFramePosition + frameHeader.getTotalFrameSize();               

                MDTFrameType frameType = frameHeader.getFrameType();

                try
                {
                    ChannelFilter filter = readingDirective.getDataFilter();
                    sources.addAll(frameType.getFrameReader(filter).readInImages(f, channel, frameIndex, frameHeader));
                }
                catch(UserCommunicableException e)
                {
                    e.printStackTrace();
                }
            }
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;        
    }
}

