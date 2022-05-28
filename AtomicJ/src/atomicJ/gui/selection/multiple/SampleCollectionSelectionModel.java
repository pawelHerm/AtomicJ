
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

package atomicJ.gui.selection.multiple;

import static atomicJ.gui.histogram.HistogramModelProperties.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.data.SampleCollection;
import atomicJ.gui.AbstractWizardPageModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.utilities.IOUtilities;


public class SampleCollectionSelectionModel extends AbstractWizardPageModel implements MultipleSelectionWizardPageModel<String>
{
    private final String task;

    private final Map<String, SampleCollection> sampleCollections;	
    private final Set<String> allTypes = new LinkedHashSet<>();
    private final Map<String, Boolean> inclusionMap = new Hashtable<>();

    private final boolean isFirst;
    private final boolean isLast;

    private boolean allKeysDeselected;
    private boolean allKeysSelected;

    private boolean finishEnabled;
    private boolean nextEnabled;

    private final MultipleSelectionListenerChangeSupport<String> listenerSupport = new MultipleSelectionListenerChangeSupport<>();

    public SampleCollectionSelectionModel(List<SampleCollection> sampleCollections, String task,
            boolean isFirst,  boolean isLast)
    {
        this.sampleCollections = buildCollectionMap(sampleCollections);
        this.task = task;

        this.isFirst = isFirst;
        this.isLast = isLast;

        for(SampleCollection collection: sampleCollections)
        {
            collection.setCollectionIncluded(false);
            allTypes.add(collection.getName());
        }

        for(String key: allTypes)
        {
            inclusionMap.put(key, false);
        }

        this.allKeysDeselected = true;
        this.allKeysSelected = false;
    }

    private Map<String, SampleCollection> buildCollectionMap(List<SampleCollection> collections)
    {
        Map<String, SampleCollection> map = new LinkedHashMap<>();

        for(SampleCollection col : collections)
        {
            map.put(col.getName(), col);
        }

        return map;
    }

    //returns the commons directory for output locations of all sample collections 
    public File getDefaultOutputLocation()
    {
        List<File> allOutputLocations = new ArrayList<>();

        for(SampleCollection collection : sampleCollections.values())
        {
            File dir = collection.getDefaultOutputDirectory();
            allOutputLocations.add(dir);
        }

        File defaultOutputLocation = IOUtilities.findLastCommonDirectory(allOutputLocations);

        return defaultOutputLocation;
    }


    @Override
    public Set<String> getKeys()
    {
        return allTypes;
    }

    @Override
    public Set<String> getSelectedKeys()
    {
        Set<String> includedKeys = new LinkedHashSet<>();

        for(Entry<String, Boolean> entry: inclusionMap.entrySet())
        {
            String key = entry.getKey();
            boolean included = entry.getValue();

            if(included)
            {
                includedKeys.add(key);
            }
        }

        return includedKeys;
    }


    @Override
    public Set<String> getLeftOutKeys()
    {
        Set<String> leftOutKeys = new LinkedHashSet<>();

        for(Entry<String, Boolean> entry: inclusionMap.entrySet())
        {
            String key = entry.getKey();
            boolean included = entry.getValue();

            if(!included)
            {
                leftOutKeys.add(key);
            }
        }

        return leftOutKeys;
    }

    @Override
    public boolean isBackEnabled()
    {
        return !isFirst;
    }

    @Override
    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    @Override
    public boolean isFinishEnabled()
    {
        return false;
    }

    @Override
    public boolean isSkipEnabled()
    {
        return false;
    }

    @Override
    public void back() 
    {}

    @Override
    public void next() 
    {
        firePropertyChange(SELECTION_FINISHED, false, true);
    }

    @Override
    public void skip() 
    {		
    }

    @Override
    public void finish() 
    {		
    }

    private void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        this.nextEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);
    }

    private void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        this.finishEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);
    }

    private void checkIfNecessaryInputProvided()
    {
        //TODO
    }

    private void checkIfNextEnabled()
    {
        if(!isLast)
        {
            boolean enabledNew = false;

            for(Boolean included: inclusionMap.values())
            {
                enabledNew = enabledNew || included;
            }	

            setNextEnabled(enabledNew);
        }		
    }

    private void checkIfFinishEnabled()
    {
        if(isLast)
        {
            boolean enabledNew = false;

            for(Boolean included: inclusionMap.values())
            {
                enabledNew = enabledNew || included;
            }	

            setFinishEnabled(enabledNew);
        }		
    }

    @Override
    public boolean isSelected(String sampleType)
    {
        Boolean included = inclusionMap.get(sampleType);
        if(included == null)
        {
            return false;
        }
        return included;
    }

    @Override
    public void setSelected(String key, boolean includedNew)
    {
        if(!inclusionMap.containsKey(key))
        {
            return;
        }

        boolean includedOld = inclusionMap.get(key);
        inclusionMap.put(key, includedNew);

        SampleCollection collection = sampleCollections.get(key);
        collection.setCollectionIncluded(includedNew);

        listenerSupport.fireKeySelectionChanged(key, includedOld, includedNew);  

        checkIfAllKeysSelected();
        checkIfAllKeysDeselected();
        checkIfNextEnabled();
        checkIfFinishEnabled();	
    }

    @Override
    public boolean areAllKeysSelected()
    {
        return allKeysSelected;
    }

    @Override
    public boolean areAllKeysDeselected()
    {
        return allKeysDeselected;
    }

    @Override
    public String getTaskDescription()
    {
        return task;
    }

    @Override
    public String getTaskName() 
    {
        return task;
    }

    @Override
    public boolean isFirst() 
    {
        return isFirst;
    }

    @Override
    public boolean isLast() 
    {
        return isLast;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return false;
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }
    private void checkIfAllKeysSelected()
    {
        boolean allKeysSelectedNew = true;

        for(boolean b : inclusionMap.values())
        {
            allKeysSelectedNew = allKeysSelectedNew && b;
            if(!allKeysSelectedNew)
            {
                break;
            }
        }

        boolean allKeysSelectedOld = this.allKeysSelected;
        this.allKeysSelected = allKeysSelectedNew;

        listenerSupport.fireAllKeysSelectedChanged(allKeysSelectedOld, allKeysSelectedNew);
    }

    private void checkIfAllKeysDeselected()
    {
        boolean allKeysDeselectedNew = true;

        for(boolean b : inclusionMap.values())
        {
            allKeysDeselectedNew = allKeysDeselectedNew && !b;
            if(!allKeysDeselectedNew)
            {
                break;
            }
        }

        boolean allKeysDeselectedOld = this.allKeysDeselected;
        this.allKeysDeselected = allKeysDeselectedNew;

        listenerSupport.fireAllKeysDeselectedChanged(allKeysDeselectedOld, allKeysDeselectedNew);
    }

    @Override
    public void setAllSelected(boolean includedNew) 
    {
        for(Entry<String, Boolean> entry: inclusionMap.entrySet())
        {
            String key = entry.getKey();
            boolean includedOld = entry.getValue();
            inclusionMap.put(key, includedNew);

            for(SampleCollection collection: sampleCollections.values())
            {
                collection.setCollectionIncluded(includedNew);
            }

            listenerSupport.fireKeySelectionChanged(key, includedOld, includedNew);					
        }	

        boolean allKeysSelectedOld = this.allKeysSelected;
        boolean allKeysSelectedNew = includedNew;
        this.allKeysSelected = allKeysSelectedNew;

        listenerSupport.fireAllKeysSelectedChanged(allKeysSelectedOld, allKeysSelectedNew);


        boolean allKeysDeselectedOld = this.allKeysDeselected;
        boolean allKeysDeselectedNew = !includedNew;
        this.allKeysDeselected = allKeysDeselectedNew;

        listenerSupport.fireAllKeysDeselectedChanged(allKeysDeselectedOld, allKeysDeselectedNew);

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    @Override
    public String getSelectionName() 
    {
        return "Datasets to plot";
    }

    @Override
    public void addSelectionChangeListener(MultipleSelectionListener<String> listener)
    {
        listenerSupport.addSelectionChangeListener(listener);
    }

    @Override
    public void removeSelectionChangeListener(MultipleSelectionListener<String> listener) {
        listenerSupport.removeSelectionChangeListener(listener);
    }

    @Override
    public void setKeys(Set<String> allKeysNew) {
        // TODO Auto-generated method stub

    }
}
