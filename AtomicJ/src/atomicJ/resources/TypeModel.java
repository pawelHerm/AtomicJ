
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.utilities.Validation;

import java.util.Set;

public class TypeModel<I>
{
    private final String type;
    private final Map<Integer, List<I>> sourceIdentifiers = new LinkedHashMap<>();

    private int duplicationCount = 0;

    public TypeModel(String type)
    {
        this.type = type;
    }

    public TypeModel(TypeModel<I> resourceOld)
    {
        this.type = resourceOld.type;

        for(Entry<Integer, List<I>> entry: resourceOld.sourceIdentifiers.entrySet())
        {
            Integer index = entry.getKey();
            List<I> ids = new ArrayList<>(entry.getValue());
            this.sourceIdentifiers.put(index, ids);
        }
    }

    public String getType()    
    {
        return type;
    }

    public List<I> getIdentifiers(Integer index)
    {    
        List<I> copy = new ArrayList<>();

        List<I> ids = sourceIdentifiers.get(index);    
        if(ids != null)
        {
            copy.addAll(ids);   
        }

        return copy;
    }

    //regular channel identifiers, not universal identifiers
    public Set<I> getIdentifiers()
    {
        Set<I> allIdentifiers = new LinkedHashSet<>();

        for(List<I> ids : sourceIdentifiers.values())
        {
            allIdentifiers.addAll(ids);
        }

        return allIdentifiers;
    }

    public Map<Integer, List<I>> getIdentifierMap()
    {
        Map<Integer, List<I>> mapCopy = new LinkedHashMap<>();

        for(Entry<Integer, List<I>> entry : sourceIdentifiers.entrySet())
        {
            Integer source = entry.getKey();

            List<I> identifiers = new ArrayList<>(entry.getValue());
            mapCopy.put(source, identifiers);
        }

        return mapCopy;
    }

    public int getDuplicationCount()
    {
        return duplicationCount;
    }

    public String registerDuplication()
    {
        String typeNew = type + " (" + String.valueOf(duplicationCount + 1) + ")";
        this.duplicationCount++;

        return typeNew;
    }

    public String registerDuplication(String typeNew)
    {
        this.duplicationCount++;
        return typeNew;
    }

    public void registerIdentifier(Integer origin, I identifiersNew)
    {
        Validation.requireNonNullParameterName(origin, "origin");
        Validation.requireNonNullParameterName(identifiersNew, "identifiersNew");

        if(sourceIdentifiers.containsKey(origin))
        {
            List<I> channelsForResource = sourceIdentifiers.get(origin);
            channelsForResource.add(identifiersNew);
        }
        else
        {
            List<I> channelsForResource = new ArrayList<>();
            channelsForResource.add(identifiersNew);

            sourceIdentifiers.put(origin, channelsForResource);
        }
    }

    public void registerChannels(Integer index, List<I> identifiersNew)
    {
        Validation.requireNonNullParameterName(index, "index");
        Validation.requireNonNullParameterName(identifiersNew, "identifiersNew");

        if(sourceIdentifiers.containsKey(index))
        {
            List<I> channelsForResource = sourceIdentifiers.get(index);
            channelsForResource.addAll(identifiersNew);
        }
        else
        {
            List<I> channelsForResource = new ArrayList<>(identifiersNew);

            sourceIdentifiers.put(index, channelsForResource);
        }
    }
}
