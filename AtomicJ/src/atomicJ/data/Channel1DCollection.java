
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

package atomicJ.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.Range;

import atomicJ.curveProcessing.Channel1DDataTransformation;

public class Channel1DCollection implements TransformableData1D<Channel1DCollection>
{	
    private final Map<String, Channel1D> channels = new LinkedHashMap<>();
    private final String name;	
    private final String key;	

    public Channel1DCollection(String name)
    {
        this(name, name);
    }

    public Channel1DCollection(String name, String key)
    {
        this.name = name;
        this.key = key;
    }

    private Channel1DCollection(Map<String, Channel1D> channels, String name, String key)
    {
        this.key = key;
        this.name = name;

        this.channels.putAll(channels);
    }

    @Override
    public boolean transform(Channel1DDataTransformation tr)
    {       
        boolean transformed = false;

        for(Entry<String, Channel1D> entry : channels.entrySet())
        {
            Channel1D channel = entry.getValue();
            transformed = transformed || channel.transform(tr);
        }

        return transformed;
    }

    public boolean transform(String channelIdentifier, Channel1DDataTransformation tr)
    {       
        boolean transformed = false;

        if(channels.containsKey(channelIdentifier))
        {
            Channel1D channel = channels.get(channelIdentifier);
            transformed = channel.transform(tr);
        }

        return transformed;
    }

    public void addChannel(Channel1D channel)
    {
        String identifier = channel.getIdentifier();
        this.channels.put(identifier, channel);         
    }

    public void addChannel(Channel1DData channelData, String identifier)
    {
        this.channels.put(identifier, new Channel1DStandard(channelData, identifier));         
    }

    public boolean removeChannel(String channelIndentifier)
    {
        Channel1D removedChannel = this.channels.remove(channelIndentifier);
        boolean removalSuccessful = (removedChannel != null);

        return removalSuccessful;
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> leaves = new ArrayList<>(channels.values());
        return leaves;	
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = this.channels.isEmpty();
        return empty;
    }

    @Override
    public String getIdentifier() 
    {
        return name;
    }

    @Override
    public String getName() 
    {
        return key;
    }

    @Override
    public Channel1DCollection getCopy(double s)
    {
        Map<String, Channel1D> channelsNew = new LinkedHashMap<>();

        for(Entry<String, Channel1D> entry : this.channels.entrySet())
        {
            channelsNew.put(entry.getKey(), entry.getValue().getCopy(s));
        }

        return new Channel1DCollection(channelsNew, this.name, this.key);
    }

    public Range getCombinedXRange()
    {
        if(channels.isEmpty())
        {
            return new Range(Double.NaN, Double.NaN);
        }

        Collection<Channel1D> channelsSet = channels.values();

        Iterator<Channel1D> it = channelsSet.iterator();
        Range combinedRange = it.next().getXRange();

        while(it.hasNext())
        {
            combinedRange = Range.combine(combinedRange, it.next().getXRange());
        }

        return combinedRange;
    }

    public Range getCombinedYRange()
    {
        if(channels.isEmpty())
        {
            return new Range(Double.NaN, Double.NaN);
        }

        Collection<Channel1D> channelsSet = channels.values();

        Iterator<Channel1D> it = channelsSet.iterator();
        Range combinedRange = it.next().getYRange();

        while(it.hasNext())
        {
            combinedRange = Range.combine(combinedRange, it.next().getYRange());
        }

        return combinedRange;
    }
}
