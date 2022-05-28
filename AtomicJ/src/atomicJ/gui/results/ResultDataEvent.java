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

package atomicJ.gui.results;

import java.util.*;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.sources.Channel1DSource;


public class ResultDataEvent <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> extends EventObject
{

    private static final long serialVersionUID = 1L;    

    public static final int BATCHES_ADDED = 0;  
    public static final int BATCHES_REMOVED = 1;    
    public static final int BATCHES_CLEARED = 2;    
    public static final int PACKS_ADDED = 3;
    public static final int PACKS_REMOVED = 4;  

    private final List<Batch<E>> batches;
    private final List<E> packs;

    private final int type;

    public ResultDataEvent(ResultDataModel<S,E> source, List<Batch<E>> batches, int type)
    {
        super(source);
        this.batches = batches;
        this.type = type;

        List<E> allPacks = new ArrayList<>();
        for(Batch<E> batch: batches)
        {
            List<E> packs = batch.getPacks();
            allPacks.addAll(packs);
        }
        this.packs = allPacks;
    }

    public ResultDataEvent(ResultDataModel<S,E> source, List<E> packs, Batch<E> batch, int type)
    {
        super(source);
        this.packs = packs;
        this.type = type;

        List<Batch<E>> batches = new ArrayList<>();
        batches.add(batch);
        this.batches = batches;
    }

    public List<Batch<E>> getBatches()
    {
        return batches;
    }

    public List<E> getPacks()
    {
        return packs;
    }

    public int getType()
    {
        return type;
    }
}

