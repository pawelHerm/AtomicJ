
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

package atomicJ.gui;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import atomicJ.gui.curveProcessing.ProcessingModelInterface;
import atomicJ.gui.curveProcessing.ProcessingWizardModel;
import atomicJ.resources.Resource;



public class ResourceSelectionPage <E extends Resource> extends JPanel implements PropertyChangeListener, WizardPage
{
    private static final long serialVersionUID = 1L;

    private static final Preferences pref = Preferences.userNodeForPackage(ResourceCellRenderer.class).node("ResourceSelectionPage");

    private final boolean isFirst;
    private final boolean isLast;

    private final ResourceList<E> resourceList = new ResourceList<>(pref.node("ResourceCellRenderer"));

    private final JPanel panelControls;
    private ResourceChooserModel<E>  model;

    private boolean necessaryInputProvided;

    public ResourceSelectionPage(ResourceChooserModel<E> model, boolean isFirst, boolean isLast)
    {
        setModel(model);
        this.isFirst = isFirst;
        this.isLast = isLast;
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane  = new JScrollPane(resourceList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        scrollPanePanel.add(scrollPane,BorderLayout.CENTER);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(8,8,8,8));
        scrollPanePanel.setBorder(border);

        panelControls = buildControlPanel();		

        add(scrollPanePanel,BorderLayout.CENTER);

        resourceList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent event)
            {
                if((event.getValueIsAdjusting() == false))
                {
                    E selectedResource = resourceList.getSelectedValue();
                    selectResource(selectedResource);                  
                }
            }
        }); 

    }

    private void selectResource(E resource)
    {
        model.setChosenResource(resource);
    }

    private void pullModelProperties()
    {
        List<E> sources = model.getResources();
        necessaryInputProvided = model.isResourceChosen();

        setSourceListElements(sources);

        E chosenResource = model.getChosenResource();

        if(chosenResource != null)
        {
            resourceList.setSelectedValue(chosenResource, true);
        }
    }

    public void setModel(ResourceChooserModel<E> modelNew)
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);

        pullModelProperties();
    }

    public void cancel()
    {     
    }

    public ResourceChooserModel<E> getModel()
    {
        return model;
    }

    private void setSourceListElements(List<E> sources)
    {
        resourceList.setItems(sources);
        resourceList.revalidate();	
    }

    @Override
    public Component getView()
    {
        return this;
    }

    @Override
    public Component getControls()
    {
        return panelControls;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }	

    @Override
    public void propertyChange(final PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(name.equals(ResourceStandardChooserModel.RESOURCES))
        {
            @SuppressWarnings("unchecked")
            List<E> newVal = (List<E>)evt.getNewValue();
            List<E> oldVal = resourceList.getItems();
            if(!oldVal.equals(newVal))
            {
                setSourceListElements(newVal);
            }
        }
        else if(name.equals(ResourceStandardChooserModel.RESOURCE_IS_CHOSEN))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = necessaryInputProvided;
            if(newVal != oldVal)
            {
                firePropertyChange(ProcessingWizardModel.INPUT_PROVIDED, oldVal, newVal);
                necessaryInputProvided = newVal;
            }
        }
        else if(name.equals(ResourceStandardChooserModel.CHOSEN_RESOURCE))
        {
            Resource newVal = (Resource)evt.getNewValue();
            Resource oldVal = resourceList.getSelectedValue();

            if(!Objects.equals(oldVal, newVal))
            {
                resourceList.setSelectedItem(newVal, true);
            }
        }
        else if(name.equals(ProcessingModelInterface.CURRENT_BATCH_NUMBER))
        {
            pullModelProperties();
        }		
    }

    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel();	

        return panelControl;
    }

    @Override
    public boolean isBackEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isNextEnabled() 
    {
        return model.isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return model.isSkipEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return model.isFinishEnabled();
    }

    @Override
    public String getTaskName() 
    {
        return ((WizardPageModel)getModel()).getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return ((WizardPageModel)getModel()).getTaskDescription();
    }

    @Override
    public String getIdentifier()
    {
        return ResourceStandardChooserModel.RESOURCES;
    }

    @Override
    public boolean isLast() 
    {
        return isLast;
    }

    @Override
    public boolean isFirst()
    {
        return isFirst;
    }

    //this is necessary, because the names of resources are sometimes very long, while
    //the wizard honours preferred sizes of its pages.
    //so if this method were not overriden, the wizard would be very wide
    @Override
    public Dimension getPreferredSize()
    {
        Dimension preferedSize = super.getPreferredSize();
        Dimension currentSize = getSize();

        return new Dimension((int) currentSize.getWidth(), (int)preferedSize.getHeight());
    }

    private static class ShortResourceCellRenderer extends ResourceCellRenderer
    {
        public ShortResourceCellRenderer(Preferences pref)
        {
            super(pref);
        }


    }
}
