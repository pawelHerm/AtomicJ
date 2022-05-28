
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

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;

import atomicJ.gui.AbstractModel;

public class BasicMultipleSelectionModel<E> extends AbstractModel implements MultipleSelectionModel<E>
{
    public final static String AT_LEAST_ONE_KEY_SELECTED = "AtLeastOneKeySelected";
    public final static String KEY_SELECTION_CAN_BE_FINISHED = "KeySelectionCanBeFinished";

    private final String selectionName;

    private Set<E> allKeys = new LinkedHashSet<>();
    private Map<E, Boolean> inclusionMap = new LinkedHashMap<>();

    private final boolean acceptEmptyResult;

    private boolean allKeysDeselected;
    private boolean allKeysSelected;

    private boolean atLeastOneSelected;
    private boolean finishEnabled;

    private final MultipleSelectionListenerChangeSupport<E> listenerSupport = new MultipleSelectionListenerChangeSupport<>();

    public BasicMultipleSelectionModel(Collection<E> keys, String selectionName)
    {
        this(keys, selectionName, false);
    }

    public BasicMultipleSelectionModel(Collection<E> keys, String selectionName, boolean acceptEmptyResult)
    {
        this.selectionName = selectionName;

        this.allKeys.addAll(keys);

        this.acceptEmptyResult = acceptEmptyResult;

        for(E key: allKeys)
        {
            inclusionMap.put(key, false);
        }

        this.allKeysSelected = false;
        this.allKeysDeselected = true;

        checkIfAtLeastOneSelected();
        checkIfFinishEnabled();
    }

    @Override
    public String getSelectionName()
    {
        return selectionName;
    }   

    @Override
    public void setKeys(Set<E> allKeysNew)
    {
        Set<E> allKeysOld = new LinkedHashSet<>(this.allKeys);
        this.allKeys = new LinkedHashSet<>(allKeysNew);

        if(!Objects.equals(allKeysOld, allKeysNew))
        {
            Map<E, Boolean> inclusionMapNew = new Hashtable<>();
            for(E key: allKeys)
            {
                boolean included = inclusionMap.containsKey(key) ? inclusionMap.get(key) : false;
                inclusionMapNew.put(key, included);
            }

            this.inclusionMap = inclusionMapNew;

            listenerSupport.fireKeySetChanged(allKeysOld, new LinkedHashSet<>(allKeysNew));

            checkIfAtLeastOneSelected();
            checkIfFinishEnabled();

        }
    }

    @Override
    public Set<E> getKeys()
    {
        return allKeys;
    }

    @Override
    public Set<E> getSelectedKeys()
    {
        Set<E> includedKeys = new LinkedHashSet<>();

        for(Entry<E, Boolean> entry: inclusionMap.entrySet())
        {
            E key = entry.getKey();
            boolean included = entry.getValue();

            if(included)
            {
                includedKeys.add(key);
            }
        }

        return includedKeys;
    }

    @Override
    public Set<E> getLeftOutKeys()
    {
        Set<E> leftOutKeys = new LinkedHashSet<>();

        for(Entry<E, Boolean> entry: inclusionMap.entrySet())
        {
            E key = entry.getKey();
            boolean included = entry.getValue();

            if(!included)
            {
                leftOutKeys.add(key);
            }
        }

        return leftOutKeys;
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


    private void checkIfAtLeastOneSelected()
    {
        boolean atLeastOneSelectedNew = calculateLeastOneSelected();

        boolean atLeastOneSelectedOld = this.atLeastOneSelected;
        this.atLeastOneSelected = atLeastOneSelectedNew;

        firePropertyChange(AT_LEAST_ONE_KEY_SELECTED, atLeastOneSelectedOld, atLeastOneSelectedNew);
    }

    private boolean calculateLeastOneSelected()
    {
        boolean selected = false;

        for(Boolean s: inclusionMap.values())
        {
            selected = selected || s;

            if(selected)
            {
                break;
            }
        }

        return selected;
    }

    private void checkIfFinishEnabled()
    {
        boolean enabledNew = acceptEmptyResult ? true : calculateLeastOneSelected();

        boolean enabledOld = finishEnabled;
        this.finishEnabled = enabledNew;

        firePropertyChange(KEY_SELECTION_CAN_BE_FINISHED, enabledOld, enabledNew);  	
    }

    @Override
    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    @Override
    public boolean isSelected(E sampleType)
    {
        Boolean included = inclusionMap.get(sampleType);

        if(included == null)
        {
            return false;
        }
        return included;
    }

    @Override
    public void setSelected(E key, boolean includedNew)
    {
        if(!inclusionMap.containsKey(key))
        {
            return;
        }

        boolean includedOld = inclusionMap.get(key);

        if(includedOld != includedNew)
        {
            inclusionMap.put(key, includedNew);

            listenerSupport.fireKeySelectionChanged(key, includedOld, includedNew);

            checkIfAtLeastOneSelected();
            checkIfAllKeysSelected();
            checkIfAllKeysDeselected();
            checkIfFinishEnabled();
        }      
    }

    public void setSelected(Set<E> keys, boolean includedNew)
    {
        for(E key : keys)
        {
            if(!inclusionMap.containsKey(key))
            {
                continue;
            }

            boolean includedOld = inclusionMap.get(key);
            inclusionMap.put(key, includedNew);

            if(includedOld != includedNew)
            {
                listenerSupport.fireKeySelectionChanged(key, includedOld, includedNew);
            }
        }

        checkIfAtLeastOneSelected();
        checkIfAllKeysSelected();
        checkIfAllKeysDeselected();
        checkIfFinishEnabled();
    }

    public boolean isAtLeastOneSelected() 
    {
        return atLeastOneSelected;
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

        if(allKeysSelectedOld != allKeysSelectedNew)
        {
            listenerSupport.fireAllKeysSelectedChanged(allKeysSelectedOld, allKeysSelectedNew);
        }
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

        if(allKeysDeselectedOld != allKeysDeselectedNew)
        {
            listenerSupport.fireAllKeysDeselectedChanged(allKeysDeselectedOld, allKeysDeselectedNew);
        }
    }

    @Override
    public void setAllSelected(boolean includedNew) 
    {
        for(Entry<E, Boolean> entry: inclusionMap.entrySet())
        {
            E key = entry.getKey();
            boolean includedOld = entry.getValue();
            inclusionMap.put(key, includedNew);

            listenerSupport.fireKeySelectionChanged(key, includedOld, includedNew);					
        }	

        boolean allKeysSelectedOld = this.allKeysSelected;
        boolean allKeysSelectedNew = includedNew;
        this.allKeysSelected = allKeysSelectedNew;

        if(allKeysSelectedOld != allKeysSelectedNew)
        {
            listenerSupport.fireAllKeysSelectedChanged(allKeysSelectedOld, allKeysSelectedNew);
        }

        boolean allKeysDeselectedOld = this.allKeysDeselected;
        boolean allKeysDeselectedNew = !includedNew;
        this.allKeysDeselected = allKeysDeselectedNew;

        if(allKeysDeselectedOld != allKeysDeselectedNew)
        {
            listenerSupport.fireAllKeysDeselectedChanged(allKeysDeselectedOld, allKeysDeselectedNew);
        }

        checkIfAtLeastOneSelected();
        checkIfFinishEnabled();
    }

    @Override
    public void addSelectionChangeListener(MultipleSelectionListener<E> listener)
    {
        listenerSupport.addSelectionChangeListener(listener);
    }

    @Override
    public void removeSelectionChangeListener(MultipleSelectionListener<E> listener) {
        listenerSupport.removeSelectionChangeListener(listener);
    }
}
