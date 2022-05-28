
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

package atomicJ.resources;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import atomicJ.sources.MapSource;


public class MapImageResource extends StandardChannel2DResource
{
    public MapImageResource(MapSource<?> mainSource, List<String> channels)
    {
        this(mainSource, channels, mainSource.getShortName(), mainSource.getLongName(), mainSource.getDefaultOutputLocation());
    }

    public MapImageResource(MapSource<?> mainSource, List<String> channels, String shortName, String longName, File outputLocation)
    {     
        super(mainSource, channels, shortName, longName, outputLocation);
    }

    public MapImageResource(MapImageResource resourceOld, String shortNameNew, String longNameNew)
    {
        super(resourceOld, shortNameNew, longNameNew);
    }

    public MapImageResource(MapImageResource resourceOld, Set<String> typesToRetain, String shortNameNew, String longNameNew)
    {
        super(resourceOld, typesToRetain, shortNameNew, longNameNew);
    }

    public MapSource<?> getMapSource()
    {
        Set<MapSource> mapSources = getMapSources();
        Iterator<MapSource>  it = mapSources.iterator();
        MapSource<?> source = it.hasNext() ? it.next() : null;
        return source;
    }
}