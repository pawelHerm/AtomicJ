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

package atomicJ.analysis;

import java.util.ArrayList;
import java.util.List;

import atomicJ.sources.IdentityTag;
import atomicJ.utilities.Validation;

public final class Batch <E extends Processed1DPack<E,?>>
{   
    private final List<E> packs;    
    private final String name;
    private final int index;
    private final IdentityTag idTag;

    public Batch(String name, int index)
    {
        this(new ArrayList<>(), name, index);  
    }

    public Batch(List<E> packs, int index)
    {
        this(packs, Integer.toString(index), index);    
    }

    public Batch(List<E> packs, String name, int index)
    {
        this.packs = packs;
        this.name = name;
        this.index = index;
        this.idTag = new IdentityTag(index, name);
    }

    public IdentityTag getIdentityTag()
    {
        return idTag;
    }

    public List<E> getPacks()
    {
        List<E> packsCopy = new ArrayList<>(packs);
        return packsCopy;
    }

    public boolean addProcessedPack(E pack)
    {
        boolean isAdded = packs.add(pack);
        if(isAdded)
        {
            pack.setBatchIdTag(this.idTag);
        }
        return isAdded;
    }

    public boolean addProcessedPacks(List<E> packs)
    {
        boolean addedAtLeastOne = false;
        for(E pack:packs)
        {
            boolean isAdded = addProcessedPack(pack);
            addedAtLeastOne = addedAtLeastOne||isAdded;
        }
        return addedAtLeastOne;
    }

    public boolean removeProcessedPack(E pack)
    {
        boolean isRemoved = packs.remove(pack);
        if(isRemoved)
        {
            pack.setBatchIdTag(null);
        }
        return isRemoved;
    }

    public boolean removeProcessedPacks(List<E> packs)
    {
        boolean removedAtLeastOne = false;
        for(E pack: packs)
        {
            boolean isRemoved = removeProcessedPack(pack);
            removedAtLeastOne = removedAtLeastOne||isRemoved;
        }
        return removedAtLeastOne;
    }

    public boolean containsPack(E pack)
    {
        return packs.contains(pack);
    }

    public boolean isEmpty()
    {
        boolean empty = packs.isEmpty();
        return empty;
    }

    public int getPackCount()
    {
        int size = packs.size();
        return size;
    }

    public String getName()
    {
        return name;
    }

    public int getIndex()
    {
        return index;
    }

    public double[] getValues(ProcessedPackFunction<? super E> function)
    {
        return ProcessedPackFunction.<E>getValuesForPacks(packs, function);
    }

    public static <E extends Processed1DPack<E,?>> double[] getValues(List<Batch<E>> batches, ProcessedPackFunction<? super E> function)
    {
        Validation.requireNonNullParameterName(function, "function");

        List<E> allPacks = new ArrayList<>();

        for(Batch<E> batch: batches)
        {
            allPacks.addAll(batch.getPacks());
        }

        return ProcessedPackFunction.<E>getValuesForPacks(allPacks, function);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
