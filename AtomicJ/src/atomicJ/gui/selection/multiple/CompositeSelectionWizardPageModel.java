
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.gui.AbstractWizardPageModel;
import atomicJ.gui.WizardModelProperties;


public class CompositeSelectionWizardPageModel<E> extends AbstractWizardPageModel
implements MultipleSelectionWizardPageModel<E>, MultipleSelectionListener<E>, PropertyChangeListener
{
    private final List<MultipleSelectionWizardPageModel<E>> keySelectionModels;	
    private MultipleSelectionWizardPageModel<E> currentSelectionModel;

    private int modelIndex = 0;

    private final MultipleSelectionListenerChangeSupport<E> listenerSupport = new MultipleSelectionListenerChangeSupport<>();

    public CompositeSelectionWizardPageModel(List<MultipleSelectionWizardPageModel<E>> keySelectionModels)
    {
        if(keySelectionModels.isEmpty())
        {
            throw new IllegalArgumentException("The 'keySelectionModels' list is empty");
        }

        this.keySelectionModels = keySelectionModels;
        this.currentSelectionModel = keySelectionModels.get(0);

        this.currentSelectionModel.addPropertyChangeListener(this);
        this.currentSelectionModel.addSelectionChangeListener(this);
    }

    private void handleCurrentSelectionModelChange(MultipleSelectionWizardPageModel<E> modelOld, MultipleSelectionWizardPageModel<E> modelNew)
    {
        if(modelOld != null)
        {
            modelOld.removePropertyChangeListener(this);
            modelOld.removeSelectionChangeListener(this);
        }

        modelNew.addPropertyChangeListener(this);
        modelNew.addSelectionChangeListener(this);

        listenerSupport.fireKeySetChanged(new LinkedHashSet<>(modelOld.getKeys()), new LinkedHashSet<>(modelNew.getKeys()));

        boolean allKeysSelectedOld = modelOld.areAllKeysSelected();
        boolean allKeysSelectedNew = modelNew.areAllKeysSelected();

        listenerSupport.fireAllKeysSelectedChanged(allKeysSelectedOld, allKeysSelectedNew);

        boolean allKeysDeselectedOld = modelOld.areAllKeysDeselected();
        boolean allKeysDeselectedNew = modelNew.areAllKeysDeselected();

        listenerSupport.fireAllKeysDeselectedChanged(allKeysDeselectedOld, allKeysDeselectedNew);

        boolean nextEnabledOld = modelOld.isNextEnabled();
        boolean nextEnabledNew = modelNew.isNextEnabled();

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, nextEnabledOld, nextEnabledNew);

        boolean finishEnabledOld = modelOld.isFinishEnabled();
        boolean finisEnabledNew = modelNew.isFinishEnabled();

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, finisEnabledNew);
    }

    @Override
    public Set<E> getKeys()
    {
        return currentSelectionModel.getKeys();
    }

    @Override
    public Set<E> getSelectedKeys()
    {
        return currentSelectionModel.getSelectedKeys();
    }


    @Override
    public Set<E> getLeftOutKeys()
    {
        return currentSelectionModel.getLeftOutKeys();
    }

    @Override
    public boolean isBackEnabled()
    {
        return currentSelectionModel.isBackEnabled();
    }

    @Override
    public boolean isNextEnabled()
    {
        return currentSelectionModel.isNextEnabled();
    }

    @Override
    public boolean isFinishEnabled()
    {
        return currentSelectionModel.isFinishEnabled();
    }

    @Override
    public boolean isSkipEnabled()
    {
        return currentSelectionModel.isSkipEnabled();
    }

    @Override
    public void back() 
    {
        this.modelIndex = modelIndex -1;

        if(modelIndex >= 0)
        {
            currentSelectionModel.back();
        }
    }

    @Override
    public void next() 
    {
        this.modelIndex = modelIndex + 1;

        if(keySelectionModels.size() > modelIndex)
        {
            MultipleSelectionWizardPageModel<E> modelOld = this.currentSelectionModel;
            this.currentSelectionModel = keySelectionModels.get(modelIndex);

            handleCurrentSelectionModelChange(modelOld, this.currentSelectionModel);
        }
        else if(keySelectionModels.size() == modelIndex)
        {
            firePropertyChange(SELECTION_FINISHED, false, true);
        }
    }

    @Override
    public void skip() 
    {		
        currentSelectionModel.skip();
    }

    @Override
    public void finish() 
    {		
    }

    @Override
    public boolean isSelected(E key)
    {
        return currentSelectionModel.isSelected(key);
    }

    @Override
    public void setSelected(E key, boolean includedNew)
    {
        currentSelectionModel.setSelected(key, includedNew);
    }

    @Override
    public boolean areAllKeysSelected()
    {
        return currentSelectionModel.areAllKeysSelected();
    }

    @Override
    public boolean areAllKeysDeselected()
    {
        return currentSelectionModel.areAllKeysDeselected();
    }

    @Override
    public String getTaskDescription()
    {
        return currentSelectionModel.getTaskDescription();
    }

    @Override
    public String getTaskName() 
    {
        return currentSelectionModel.getTaskName();
    }

    @Override
    public boolean isFirst() 
    {
        return currentSelectionModel.isFirst();
    }

    @Override
    public boolean isLast() 
    {
        return currentSelectionModel.isLast();
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return currentSelectionModel.isNecessaryInputProvided();
    }

    @Override
    public void cancel() {

    }

    @Override
    public void setAllSelected(boolean includedNew) 
    {
        currentSelectionModel.setAllSelected(includedNew);
    }

    @Override
    public String getSelectionName() 
    {
        return currentSelectionModel.getSelectionName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String property = evt.getPropertyName();

        //we must not pass the SELECTION_FINISHED event if the keySelectionModel is not last
        if(SELECTION_FINISHED.equals(property))
        {
            return; 
        }

        firePropertyChange(evt);
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

    @Override
    public void keySelectionChanged(E key, boolean selectedOld,
            boolean selectedNew) {
        listenerSupport.fireKeySelectionChanged(key, selectedOld, selectedNew);
    }

    @Override
    public void allKeysSelectedChanged(boolean allSelectedOld,
            boolean allSelectedNew) {
        listenerSupport.fireAllKeysSelectedChanged(allSelectedOld, allSelectedNew);
    }

    @Override
    public void allKeysDeselectedChanged(boolean allDeselectedOld,
            boolean allDeselectedNew) {
        listenerSupport.fireAllKeysDeselectedChanged(allDeselectedOld, allDeselectedNew);
    }

    @Override
    public void keySetChanged(Set<E> keysOld, Set<E> keysNew) {
        listenerSupport.fireKeySetChanged(keysOld, keysNew);
    }

    @Override
    public void setKeys(Set<E> allKeysNew) {
        // TODO Auto-generated method stub

    }
}
