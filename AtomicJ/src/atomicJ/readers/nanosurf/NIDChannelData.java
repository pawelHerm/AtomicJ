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
import java.util.Map;

import atomicJ.data.ImageChannel;
import atomicJ.data.units.QuantityArray2DExpression;

public class NIDChannelData
{
    private final Map<String, ImageChannel> readInImageChannels;
    private final Map<String, QuantityArray2DExpression> readInSpectroscopyData;

    private static final NIDChannelData NULL_INSTANCE = new NIDChannelData(Collections.<String, ImageChannel>emptyMap(), Collections.<String, QuantityArray2DExpression>emptyMap());

    public NIDChannelData(Map<String, ImageChannel> readInImageChannels, Map<String, QuantityArray2DExpression> readInSpectroscopyData)
    {
        this.readInImageChannels = readInImageChannels;
        this.readInSpectroscopyData = readInSpectroscopyData;       
    }

    public static NIDChannelData getEmptyInstance()
    {
        return NULL_INSTANCE;
    }

    public static NIDChannelData getSpectroscopyOnlyInstance(Map<String, QuantityArray2DExpression> readInSpectroscopyData)
    {
        return new NIDChannelData(Collections.<String, ImageChannel>emptyMap(), readInSpectroscopyData);
    }

    public static NIDChannelData getScanOnlyInstance(Map<String, ImageChannel> readInImageChannels)
    {
        return new NIDChannelData(readInImageChannels, Collections.<String, QuantityArray2DExpression>emptyMap());
    }

    public Map<String, ImageChannel> getReadInImageChannels()
    {
        return readInImageChannels;
    }

    public Map<String, QuantityArray2DExpression> getReadInSpectroscopyData()
    {
        return readInSpectroscopyData;
    }
}