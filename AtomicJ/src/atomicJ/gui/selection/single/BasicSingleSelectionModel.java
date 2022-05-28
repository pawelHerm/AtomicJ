
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

package atomicJ.gui.selection.single;

import static atomicJ.gui.WizardModelProperties.*;
import static atomicJ.gui.histogram.HistogramModelProperties.SELECTION_FINISHED;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import atomicJ.gui.AbstractWizardPageModel;
import atomicJ.gui.WizardModelProperties;

public class BasicSingleSelectionModel<E> extends AbstractWizardPageModel implements SingleSelectionWizardPageModel<E>
{
    private final String taskName;
    private final String taskDescription;
    private final String selectionName;

    private Set<E> allKeys = new LinkedHashSet<>();
    private E includedKey;
    private final boolean isFirst;
    private final boolean isLast;

    private boolean necessaryInputProvided;
    private boolean finishEnabled;
    private boolean nextEnabled;

    public BasicSingleSelectionModel(Collection<E> keys, String selectionName, String taskName, String taskDescription,
            boolean isFirst, boolean isLast)
    {   
        this.allKeys.addAll(keys);

        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.selectionName = selectionName;

        this.isFirst = isFirst;
        this.isLast = isLast;

        this.includedKey = allKeys.isEmpty() ? null : allKeys.iterator().next();

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    public void setKeys(Set<E> allKeysNew)
    {        
        Set<E> allKeysOld = new LinkedHashSet<>(this.allKeys);
        this.allKeys = new LinkedHashSet<>(allKeysNew);   

        if(!allKeys.contains(includedKey))
        {
            this.includedKey = allKeys.isEmpty() ? null : allKeys.iterator().next();
        }


        firePropertyChange(SingleSelectionModel.KEY_SET_CHANGED, allKeysOld, new LinkedHashSet<>(allKeysNew));

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
    public E getSelectedKey()
    {
        return includedKey;
    }

    @Override
    public Set<E> getLeftOutKeys()
    {
        Set<E> leftOutKeys = new LinkedHashSet<>(allKeys);
        leftOutKeys.remove(includedKey);

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
        boolean necesaryInputProvidedNew = this.includedKey != null;

        boolean necessaryInputProvidedOld = this.necessaryInputProvided;
        this.necessaryInputProvided = necesaryInputProvidedNew;

        firePropertyChange(NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necesaryInputProvidedNew);
    }

    private void checkIfNextEnabled()
    {
        if(!isLast)
        {
            boolean enabledNew = this.includedKey != null;

            boolean enabledOld = nextEnabled;
            this.nextEnabled = enabledNew;

            firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);
        }		
    }

    private void checkIfFinishEnabled()
    {
        if(isLast)
        {
            boolean enabledNew = this.includedKey != null;

            boolean enabledOld = finishEnabled;
            this.finishEnabled = enabledNew;

            firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);		
        }		
    }


    @Override
    public boolean isSelected(E key)
    {
        boolean included = Objects.equals(this.includedKey, key);        
        return included;
    }

    @Override
    public void setSelectedKey(E key)
    {
        if(!allKeys.contains(key))
        {
            return;
        }

        E includedKeyOld = this.includedKey;
        this.includedKey = key;

        firePropertyChange(SingleSelectionModel.SELECTED_KEY, includedKeyOld, this.includedKey);

        checkIfNecessaryInputProvided();
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

    @Override
    public String getSelectionName() 
    {
        return selectionName;
    }
}
