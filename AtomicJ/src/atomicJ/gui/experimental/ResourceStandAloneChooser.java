
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

package atomicJ.gui.experimental;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import atomicJ.gui.ResourceCellRenderer;
import atomicJ.gui.ResourceList;
import atomicJ.gui.SubPanel;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.Channel2DSourceType;


public class ResourceStandAloneChooser extends JDialog implements PropertyChangeListener, ItemListener
{
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HEIGHT = Math.round(2*Toolkit.getDefaultToolkit().getScreenSize().height/5);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/3);

    private static final Preferences PREF = Preferences.userNodeForPackage(ResourceCellRenderer.class).node("ResourceSelectionPage");

    private final ResourceList<Channel2DSource<?>> resourceList = new ResourceList<>(PREF.node("ResourceCellRenderer"));
    private final JTextField fieldSourceName = new JTextField();
    private final JComboBox<Channel2DSourceType> comboSourceType = new JComboBox<>(Channel2DSourceType.values());

    private final Action finishAction = new FinishAction();
    private final Action cancelAction = new CancelAction();

    private final JButton buttonFinish = new JButton(finishAction);
    private final JButton buttonCancel = new JButton(cancelAction);


    private ResourceStandAloneChooserModel  model;

    private boolean approved = false;

    public ResourceStandAloneChooser(Window parent, ResourceStandAloneChooserModel model)
    {
        super(parent, "Select read-in map or image", ModalityType.APPLICATION_MODAL);

        setModel(model);

        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane  = new JScrollPane(resourceList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        scrollPanePanel.add(scrollPane,BorderLayout.CENTER);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(8,8,8,8));
        scrollPanePanel.setBorder(border);


        SubPanel mainPanel = new SubPanel();
        mainPanel.addComponent(scrollPanePanel, 0, 0, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        mainPanel.addComponent(new JLabel("Name"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 0.05);
        mainPanel.addComponent(fieldSourceName, 1, 1, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 1, 0.05);
        mainPanel.addComponent(buttonFinish, 2, 1, 1, 1, GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, 0.05, 0.05);

        mainPanel.addComponent(new JLabel("Type"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 0.05);
        mainPanel.addComponent(comboSourceType, 1, 2, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 1, 0.05);
        mainPanel.addComponent(buttonCancel, 2, 2, 1, 1, GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, 0.05, 0.05);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));     
        add(mainPanel,BorderLayout.CENTER);

        fieldSourceName.setEditable(false);

        resourceList.addPropertyChangeListener(this);
        resourceList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent event)
            {
                if((event.getValueIsAdjusting() == false))
                {
                    Channel2DSource<?> selectedResource = resourceList.getSelectedValue();
                    selectResource(selectedResource);                  
                }
            }
        }); 

        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)  
            {
                if(e.getClickCount() >= 2)
                {
                    finish();
                }
            }
        };

        resourceList.addMouseListener(listener);

        initItemListener();
        initInputAndActionMaps();

        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(parent);    
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW );                
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), cancelAction.getValue(Action.NAME));      

        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelAction.getValue(Action.NAME), cancelAction); 
    }


    private void initItemListener()
    {
        comboSourceType.addItemListener(this);
    }

    public boolean show(ResourceStandAloneChooserModel model)
    {
        this.approved = false;

        setModel(model);
        setVisible(true);

        return approved;
    }

    private void selectResource(Channel2DSource<?> source)
    {              
        model.setChosenResource(source);

        setTextFieldSource(source);
    }

    private void setTextFieldSource(Channel2DSource<?> source)
    {
        String sourceName = source != null ? resourceList.getDisplayedText(source) : "";
        fieldSourceName.setText(sourceName); 
    }

    private void pullModelProperties()
    {
        boolean finishEnabled = model.isFinishEnabled();
        finishAction.setEnabled(finishEnabled);

        Channel2DSourceType sourceType = model.getDensitySourceType();
        comboSourceType.setSelectedItem(sourceType);

        List<Channel2DSource<?>> sources = model.getCurrentlyAvailableSources();        
        setSourceListElements(sources);

        Channel2DSource<?> chosenResource = model.getChosenResource();

        if(chosenResource != null)
        {
            resourceList.setSelectedValue(chosenResource, true);        
        }

        String sourceName = chosenResource != null ? resourceList.getDisplayedText(chosenResource) : "";
        fieldSourceName.setText(sourceName);
    }

    public void setModel(ResourceStandAloneChooserModel modelNew)
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);

        pullModelProperties();
    }

    public void finish()
    {
        this.approved = true;
        setVisible(false);
    }

    public void cancel()
    {
        this.approved = false;
        setVisible(false);
    }

    public ResourceStandAloneChooserModel getModel()
    {
        return model;
    }

    private void setSourceListElements(List<Channel2DSource<?>> sources)
    {
        resourceList.setItems(sources);
        resourceList.revalidate();	
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(ResourceStandAloneChooserModel.DENSITY_SOURCE_TYPE.equals(name))
        {
            Channel2DSourceType valNew = (Channel2DSourceType)evt.getNewValue();
            Channel2DSourceType valOld = (Channel2DSourceType) comboSourceType.getSelectedItem();
            if(!Objects.equals(valOld, valNew))
            {
                comboSourceType.setSelectedItem(valNew);
            }
        }
        else if(ResourceStandAloneChooserModel.AVAILABLE_SOURCES.equals(name))
        {
            List<Channel2DSource<?>> valNew = (List<Channel2DSource<?>>)evt.getNewValue();
            List<Channel2DSource<?>> valOld = resourceList.getItems();

            if(!valOld.equals(valNew))
            {
                setSourceListElements(valNew);
            }
        }
        else if(ResourceStandAloneChooserModel.FINISH_ENABLED.equals(name))
        {
            boolean valNew = (boolean)evt.getNewValue();
            finishAction.setEnabled(valNew);
        }
        else if(ResourceStandAloneChooserModel.CHOSEN_RESOURCE.equals(name))
        {
            Channel2DSource<?> valNew = (Channel2DSource<?>)evt.getNewValue();
            Channel2DSource<?> valOLd = resourceList.getSelectedValue();

            if(!Objects.equals(valOLd, valNew))
            {
                resourceList.setSelectedItem(valNew, true);
                setTextFieldSource(valNew);           
            }
        }	
        else if(ResourceList.USE_SHORT_RESOURCE_NAME.equals(name))
        {
            setTextFieldSource(resourceList.getSelectedValue());           
        }
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

    private class FinishAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FinishAction()
        {
            putValue(NAME,"Finish");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {           
            finish();
        };
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {           
            cancel();
        };
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        if(source == comboSourceType)
        {
            Channel2DSourceType sourceType = (Channel2DSourceType)comboSourceType.getSelectedItem();
            model.setDensitySourceType(sourceType);
        }
    }
}
