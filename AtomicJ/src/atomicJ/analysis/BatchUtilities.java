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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import atomicJ.sources.Channel1DSource;
import atomicJ.sources.ChannelSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.MultiMap;

public class BatchUtilities 
{
    private BatchUtilities()
    {}

    public static File findLastCommonSourceDirectory(Collection<? extends ChannelSource> sources)
    {
        List<File> files = new ArrayList<>();

        for(ChannelSource source: sources)
        {
            files.add(source.getCorrespondingFile());
        }

        return IOUtilities.findLastCommonDirectory(files);
    } 

    public static File findLastCommonDirectory(Collection<? extends Processed1DPack<?,?>> packs)
    {
        List<File> files = new ArrayList<>();

        for(Processed1DPack<?,?> pack: packs)
        {
            if(pack != null)
            {
                Channel1DSource<?> source = pack.getSource();
                files.add(source.getCorrespondingFile());
            }  
        }

        return IOUtilities.findLastCommonDirectory(files);
    }

    public static List<IdentityTag> getIds(List<? extends Batch<?>> batches)
    {
        List<IdentityTag> ids = new ArrayList<>();

        for(Batch<?> batch : batches)
        {
            ids.add(batch.getIdentityTag());
        }

        return ids;
    }

    public static <E extends Processed1DPack<?,?>> MultiMap<IdentityTag, E> segregateIntoBatches(List<E> packs)
    {
        MultiMap<IdentityTag, E> batches = new MultiMap<>();

        for(E pack : packs)
        {
            batches.put(pack.getBatchIdTag(), pack);
        }

        return batches;
    }

    public static <E extends Processed1DPack<E,?>> MultiMap<Batch<E>, E> segregateIntoBatches(Map<IdentityTag, Batch<E>> batchMap, List<E> packs)
    {
        MultiMap<Batch<E>, E> batches = new MultiMap<>();

        for(E pack : packs)
        {
            batches.put(batchMap.get(pack.getBatchIdTag()), pack);
        }

        return batches;
    }
}
