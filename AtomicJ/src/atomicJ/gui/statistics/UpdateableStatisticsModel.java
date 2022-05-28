
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

package atomicJ.gui.statistics;


import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;


public class UpdateableStatisticsModel extends StatisticsTableModel
{
    private static final long serialVersionUID = 1L;

    private final Map<Object, DescriptiveStatistics> statistics = new Hashtable<>();

    public UpdateableStatisticsModel(File defaultOutputFile, PrefixedUnit unit)
    {	
        super(defaultOutputFile, unit);
    }

    public void addOrUpdateSample(Object sampleKey, String sampleName, QuantitativeSample sample)
    {
        DescriptiveStatistics stats = sample.getDescriptiveStatistics();

        addOrUpdateSample(sampleKey, sampleName, stats);
    }

    public void addOrUpdateSample(Object sampleKey, String sampleName, DescriptiveStatistics stats)
    {
        boolean alreadyPresent = statistics.containsKey(sampleKey);			
        statistics.put(sampleKey, stats);

        Object[] row = convertDatasetToRows(sampleKey, sampleName, stats);

        if(alreadyPresent)
        {
            replaceRow(row, sampleKey);
        }
        else
        {
            addRow(row);
            updateDefaultPrefix();           
        }
    }

    public void addSamples(Map<String, DescriptiveStatistics> samples)
    {		   
        for(Entry<String, DescriptiveStatistics> entry: samples.entrySet())
        {
            String sampleName = entry.getKey();
            DescriptiveStatistics stats = entry.getValue();

            boolean presentBefore = statistics.containsKey(sampleName);			
            statistics.put(sampleName, stats);

            Object[] row = convertDatasetToRows(sampleName, sampleName, stats);

            if(presentBefore)
            {
                replaceRow(row, sampleName);
            }
            else
            {
                addRow(row);
                updateDefaultPrefix();           
            }
        }		
        checkIfEmpty();
    }

    public boolean replaceSample(String name, DescriptiveStatistics stats)
    {
        boolean tableChanged = false;

        boolean presentBefore = statistics.containsKey(name);			
        if(presentBefore)
        {
            Object[] row = convertDatasetToRows(name, name, stats);
            replaceRow(row, name);
            tableChanged = true;
        }

        return tableChanged;
    }

    public void removeSample(Object key)
    {
        statistics.remove(key);
        int i = getSampleIndex(key);
        if(i>-1)
        {
            removeRow(i);
        }
    }

    public void removeSamples(List<String> sampleNames) 
    {
        for(String name: sampleNames)
        {
            int i = getSampleIndex(name);
            if(i>-1)
            {
                removeRow(i);
            }

            statistics.remove(name);
        }
        checkIfEmpty();
    }

    private void replaceRow(Object[] rowNew, Object sampleKey)
    {
        int i = getSampleIndex(sampleKey);
        if(i>-1)
        {		
            removeRow(i);
            insertRow(i, rowNew);
        }
        checkIfEmpty();
    }

    protected int getSampleIndex(Object sampleKey)
    {
        int result = -1;
        Vector<?> data = getDataVector();
        int r = getRowCount();
        for(int i = 0;i<r;i++)
        {
            Vector<?> row = (Vector<?>)data.elementAt(i);
            IdentityTag p = (IdentityTag)row.elementAt(0);
            if(p.getKey().equals(sampleKey))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    @Override
    public Set<Object> getAvailableSamples()
    {        
        return statistics.keySet();
    }
}