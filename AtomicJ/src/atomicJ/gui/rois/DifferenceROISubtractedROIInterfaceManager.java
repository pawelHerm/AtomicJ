
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

package atomicJ.gui.rois;

import java.awt.CardLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JPanel;
import atomicJ.gui.selection.multiple.CompositeSelectionModelB;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionPanel;
import atomicJ.utilities.Validation;


public class DifferenceROISubtractedROIInterfaceManager<V, E> implements PropertyChangeListener
{
    private CompositeSelectionModelB<V, E> model;

    private final CardLayout layoutView = new CardLayout();
    private final CardLayout layoutControls = new CardLayout();

    private final JPanel panelView = new JPanel();
    private final JPanel panelControls = new JPanel();

    private boolean fullWizardLayout;

    //copied
    public DifferenceROISubtractedROIInterfaceManager()
    {
        panelView.setLayout(layoutView);
        panelControls.setLayout(layoutControls);
    }

    //copied
    public DifferenceROISubtractedROIInterfaceManager(CompositeSelectionModelB<V,E> model, boolean fullWizardLayout)
    {		
        this.fullWizardLayout = fullWizardLayout;

        panelView.setLayout(layoutView);
        panelControls.setLayout(layoutControls);

        setModel(model);
    }

    public CompositeSelectionModelB<V,E> getModel()
    {
        return model;
    }

    //copied
    public void setModel(CompositeSelectionModelB<V,E> modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        this.model.addPropertyChangeListener(this);

        buildInterface();
        setSubModelId(model.getSubModelId());
    }

    private void buildInterface()
    {
        panelView.removeAll();
        panelControls.removeAll();

        Map<String, MultipleSelectionModel<E>> subModels = model.getIdSubModelMap();

        for(Entry<String, MultipleSelectionModel<E>> entry : subModels.entrySet())
        {
            MultipleSelectionModel<E> subModel = entry.getValue();
            MultipleSelectionPanel<E, MultipleSelectionModel<E>> subPanel = new MultipleSelectionPanel<>(subModel, fullWizardLayout);

            panelView.add(subPanel, entry.getKey());
            panelControls.add(subPanel.getControls(), entry.getKey());
        }
    }

    //copied
    public Component getControls() 
    {
        return panelControls;
    }


    public Component getView() {
        return panelView;
    }

    private void setSubModelId(String subModelIdNew)
    {
        layoutView.show(panelView, subModelIdNew);
        layoutControls.show(panelControls, subModelIdNew);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        if(CompositeSelectionModelB.SUBMODEL_CHANGED.equals(property))
        {
            String subModelIdNew = (String)evt.getNewValue();
            setSubModelId(subModelIdNew);
        }
    }

}
