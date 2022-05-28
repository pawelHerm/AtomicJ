
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

import static atomicJ.gui.WizardModelProperties.*;
import static atomicJ.gui.histogram.HistogramModelProperties.SELECTION_FINISHED;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import atomicJ.gui.AbstractWizardPageModel;
import atomicJ.gui.WizardModelProperties;

public class BasicMultipleSelectionWizardPageModel<E> extends AbstractWizardPageModel implements MultipleSelectionWizardPageModel<E>
{
    private final String taskName;
    private final String taskDescription;
    private final String selectionName;

    private Set<E> allKeys = new LinkedHashSet<>();
    private Map<E, Boolean> inclusionMap = new LinkedHashMap<>();
    private final boolean isFirst;
    private final boolean isLast;

    private final boolean acceptEmptyResult;

    private boolean allKeysDeselected;
    private boolean allKeysSelected;

    private boolean necessaryInputProvided;
    private boolean finishEnabled;
    private boolean nextEnabled;

    private final MultipleSelectionListenerChangeSupport<E> listenerSupport = new MultipleSelectionListenerChangeSupport<>();

    public BasicMultipleSelectionWizardPageModel(Collection<E> keys, String selectionName, 
            String taskName, String taskDescription, boolean isFirst, boolean isLast)
    {
        this(keys, selectionName, taskName, taskDescription, isFirst, isLast, false);
    }

    public BasicMultipleSelectionWizardPageModel(Collection<E> keys, String selectionName, String taskName, String taskDescription,
            boolean isFirst, boolean isLast, boolean acceptEmptyResult)
    {
        this.allKeys.addAll(keys);

        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.selectionName = selectionName;

        this.isFirst = isFirst;
        this.isLast = isLast;

        this.acceptEmptyResult = acceptEmptyResult;

        for(E key: allKeys)
        {
            inclusionMap.put(key, false);
        }

        this.allKeysSelected = false;
        this.allKeysDeselected = true;

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    @Override
    public void setKeys(Set<E> allKeysNew)
    {
        Set<E> allKeysOld = new LinkedHashSet<>(this.allKeys);
        this.allKeys = new LinkedHashSet<>(allKeysNew);

        Map<E, Boolean> inclusionMapNew = new Hashtable<>();
        for(E key: allKeys)
        {
            boolean included = inclusionMap.containsKey(key) ? inclusionMap.get(key) : false;
            inclusionMapNew.put(key, included);
        }

        this.inclusionMap = inclusionMapNew;

        listenerSupport.fireKeySetChanged(allKeysOld, new LinkedHashSet<>(allKeysNew));

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
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
        return finishEnabled;
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

    private void checkIfNecessaryInputProvided()
    {
        boolean necesaryInputProvidedNew = atLeastOneSelected();

        boolean necessaryInputProvidedOld = this.necessaryInputProvided;
        this.necessaryInputProvided = necesaryInputProvidedNew;

        firePropertyChange(NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necesaryInputProvidedNew);

    }

    private void checkIfNextEnabled()
    {
        if(!isLast)
        {
            boolean enabledNew = atLeastOneSelected();

            boolean enabledOld = nextEnabled;
            this.nextEnabled = enabledNew;

            firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);
        }		
    }

    public boolean atLeastOneSelected()
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
        if(isLast)
        {
            boolean enabledNew = acceptEmptyResult ? true : atLeastOneSelected();

            boolean enabledOld = finishEnabled;
            this.finishEnabled = enabledNew;

            firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);		
        }		
    }


    @Override
    public boolean isSelected(E key)
    {
        Boolean included = inclusionMap.get(key);

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
        inclusionMap.put(key, includedNew);

        listenerSupport.fireKeySelectionChanged(key, includedOld, includedNew);

        checkIfNecessaryInputProvided();
        checkIfAllKeysSelected();
        checkIfAllKeysDeselected();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    @Override
    public String getTaskDescription()
    {
        return taskDescription;
    }

    @Override
    public String getTaskName() 
    {
        return taskName;
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
        return necessaryInputProvided;
    }

    @Override
    public void cancel() {
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
        return selectionName;
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
