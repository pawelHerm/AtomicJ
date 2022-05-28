/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanosurf;

import java.util.Collections;
import java.util.List;
import atomicJ.data.Channel1DData;
import atomicJ.data.ImageChannel;

public class NIDGroupData
{
    private final List<ImageChannel> readInImageChannels;
    private final List<Channel1DData> readInSpectroscopyData;

    private static final NIDGroupData NULL_INSTANCE = new NIDGroupData(Collections.<ImageChannel>emptyList(), Collections.<Channel1DData>emptyList());

    public NIDGroupData(List<ImageChannel> readInImageChannels, List<Channel1DData> readInSpectroscopyData)
    {
        this.readInImageChannels = readInImageChannels;
        this.readInSpectroscopyData = readInSpectroscopyData;        
    }

    public static NIDGroupData getEmptyInstance()
    {
        return NULL_INSTANCE;
    }

    public static NIDGroupData getSpectroscopyOnlyInstance(List<Channel1DData> readInSpectroscopyData)
    {
        return new NIDGroupData(Collections.<ImageChannel>emptyList(), readInSpectroscopyData);
    }

    public static NIDGroupData getScanOnlyInstance(List<ImageChannel> readInImageChannels)
    {
        return new NIDGroupData(readInImageChannels, Collections.<Channel1DData>emptyList());
    }

    public List<ImageChannel> getImageChannels()
    {
        return readInImageChannels;
    }

    public List<Channel1DData> getSpectroscopyData()
    {
        return readInSpectroscopyData;
    }
}