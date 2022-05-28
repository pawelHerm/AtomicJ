
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.utilities.Validation;


public class CompositeSelectionModelB<V, E> extends AbstractModel
implements MultipleSelectionModel<E>, MultipleSelectionListener<E>, PropertyChangeListener
{
    public static String SUBMODEL_CHANGED = "SubModelChanged";

    private final Map<V, MultipleSelectionModel<E>> keySelectionModels;	
    private MultipleSelectionModel<E> currentSelectionModel;

    private final Map<V, String> subModelIds = new LinkedHashMap<>();
    private V modelKey;

    private final MultipleSelectionListenerChangeSupport<E> listenerSupport = new MultipleSelectionListenerChangeSupport<>();

    public CompositeSelectionModelB(Map<V, MultipleSelectionModel<E>> keySelectionModels)
    {
        Validation.requireNonNullAndNonEmptyParameterName(keySelectionModels, "keySelectionModels");

        this.keySelectionModels = keySelectionModels;

        this.modelKey = keySelectionModels.keySet().iterator().next();
        this.currentSelectionModel = keySelectionModels.values().iterator().next();

        this.currentSelectionModel.addSelectionChangeListener(this);

        initSubModelIds();
    }

    private void initSubModelIds()
    {
        int index = 0;

        for(V key : keySelectionModels.keySet())
        {
            subModelIds.put(key, Integer.toString(index++));
        }
    }

    private void handleCurrentSelectionModelChange(MultipleSelectionModel<E> modelOld, MultipleSelectionModel<E> modelNew)
    {
        if(modelOld != null)
        {
            modelOld.removeSelectionChangeListener(this);
        }

        modelNew.addSelectionChangeListener(this);

        listenerSupport.fireKeySetChanged(new LinkedHashSet<>(modelOld.getKeys()), new LinkedHashSet<>(modelNew.getKeys()));

        boolean allKeysSelectedOld = modelOld.areAllKeysSelected();
        boolean allKeysSelectedNew = modelNew.areAllKeysSelected();

        listenerSupport.fireAllKeysSelectedChanged(allKeysSelectedOld, allKeysSelectedNew);

        boolean allKeysDeselectedOld = modelOld.areAllKeysDeselected();
        boolean allKeysDeselectedNew = modelNew.areAllKeysDeselected();

        listenerSupport.fireAllKeysDeselectedChanged(allKeysDeselectedOld, allKeysDeselectedNew);

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
    public boolean isFinishEnabled()
    {
        return currentSelectionModel.isFinishEnabled();
    }

    public String getSubModelId()
    {
        return subModelIds.get(modelKey);
    }

    public Set<String> getSubModelIds()
    {
        return new LinkedHashSet<>(subModelIds.values());
    }

    public Map<String, MultipleSelectionModel<E>> getIdSubModelMap()
    {
        Map<String, MultipleSelectionModel<E>> map = new LinkedHashMap<>();

        for(Entry<V, MultipleSelectionModel<E>> entry : keySelectionModels.entrySet())
        {
            map.put(subModelIds.get(entry.getKey()), entry.getValue());
        }

        return map;
    }

    public void setModel(V modelKeyNew) 
    {   
        if(keySelectionModels.keySet().contains(modelKeyNew))
        {        
            V modelKeyOld = this.modelKey;
            this.modelKey = modelKeyNew;

            MultipleSelectionModel<E> modelOld = this.currentSelectionModel;
            this.currentSelectionModel = keySelectionModels.get(this.modelKey);

            handleCurrentSelectionModelChange(modelOld, this.currentSelectionModel);

            firePropertyChange(SUBMODEL_CHANGED, subModelIds.get(modelKeyOld), subModelIds.get(modelKeyNew));
        }      
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
