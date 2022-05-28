
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
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

package atomicJ.sources;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import atomicJ.data.ImageChannel;
import atomicJ.readers.regularImage.Channel2DSourceMetadata;

public class StandardImageSource extends AbstractChannel2DSource<ImageChannel> implements ImageSource
{    
    public StandardImageSource(File f)
    {
        super(f);	
    }

    public StandardImageSource(String pathname)
    {
        super(pathname);
    }

    public StandardImageSource(Channel2DSourceMetadata metadata, File f, String shortName, String longName)
    {
        super(metadata, f, shortName, longName);    
    }

    public StandardImageSource(Channel2DSourceMetadata metadata, String pathname, String shortName, String longName)
    {
        super(metadata, pathname, shortName, longName);        
    }

    public StandardImageSource(StandardImageSource sourceFile)
    {
        super(sourceFile);

    }

    public StandardImageSource(StandardImageSource sourceFile, Collection<String> identifiers)
    {
        super(sourceFile);
    }


    @Override
    public List<ImageChannel> getChannelCopies()
    {
        List<ImageChannel> channels = getChannels();
        List<ImageChannel> channelsCopied = new ArrayList<>();

        for(ImageChannel channel : channels)
        {
            channelsCopied.add(channel.getCopy());
        }

        return channelsCopied;
    }

    @Override
    public List<ImageChannel> getChannelCopies(Collection<String> identifiers)
    {
        List<ImageChannel> channelsForIdentifiers = new ArrayList<>();

        for(String identifier : identifiers)
        {
            ImageChannel channel = getChannel(identifier);
            if(channel != null)
            {
                channelsForIdentifiers.add(channel);
            }
        }

        return channelsForIdentifiers;
    }

    @Override
    public ImageChannel duplicateChannel(String identifier)
    {
        ImageChannel channel = getChannel(identifier);
        ImageChannel channelCopy = channel.duplicate();

        int indexNew = getChannelPosition(identifier) + 1;

        insertChannel(channelCopy, indexNew);

        return channelCopy;
    }

    @Override
    public ImageChannel duplicateChannel(String identifier, String identifierNew)
    {
        ImageChannel channel = getChannel(identifier);
        ImageChannel channelCopy = channel.duplicate(identifierNew);

        int indexNew = getChannelPosition(identifier) + 1;
        insertChannel(channelCopy, indexNew);

        return channelCopy;
    }

    @Override
    public StandardImageSource copy() 
    {
        return new StandardImageSource(this);
    }

    @Override
    public StandardImageSource copy(Collection<String> identifiers) 
    {
        return new StandardImageSource(this, identifiers);
    }
}
