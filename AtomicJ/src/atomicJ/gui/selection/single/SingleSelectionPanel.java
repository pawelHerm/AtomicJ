
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.Border;

import atomicJ.utilities.Validation;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;

public class SingleSelectionPanel<E, V extends SingleSelectionModel<E>> extends JPanel implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private V model;
    private final Map<E, JRadioButton> radioButtons = new LinkedHashMap<>();
    private final ButtonGroup buttonGroupKeys = new ButtonGroup();

    private final JPanel panelControls = new JPanel();

    private boolean fullWizardLayout;

    public SingleSelectionPanel()
    {
        setLayout(new BorderLayout());
    }

    public SingleSelectionPanel(V model, boolean fullWizardLayout)
    {		
        this.fullWizardLayout = fullWizardLayout;
        setLayout(new BorderLayout());
        setModel(model);
    }

    public V getModel()
    {
        return model;
    }

    public void setModel(V modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);	

        pullModelProperties();
    }

    public void cleanUp()
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = null;        
    }

    private void pullModelProperties()
    {
        updatePanelBoxes();
    }

    private void updatePanelBoxes()
    {		
        updateBoxes();
        JPanel panelBoxes = new JPanel();

        JLabel labelDatasets = new JLabel(model.getSelectionName());
        GroupLayout layout = new GroupLayout(panelBoxes);
        panelBoxes.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        //builds and sets horizontal group
        SequentialGroup horizontalGroup = layout.createSequentialGroup();
        ParallelGroup innerHorizontalGroup = layout.createParallelGroup().addComponent(labelDatasets);

        for(JRadioButton button: radioButtons.values())
        {
            innerHorizontalGroup.addComponent(button);
        }

        horizontalGroup
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(innerHorizontalGroup)
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setHorizontalGroup(horizontalGroup);

        //builds and sets vertical group

        SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(labelDatasets).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

        for(JRadioButton button: radioButtons.values())
        {
            verticalGroup.addComponent(button).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        }

        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setVerticalGroup(verticalGroup);

        if(!radioButtons.isEmpty())
        {
            layout.linkSize(radioButtons.values().toArray(new JRadioButton[] {}));
        }

        removeAll();
        if(fullWizardLayout)
        {
            Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(8,8,8,8));
            panelBoxes.setBorder(border);
        }
        add(panelBoxes, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void updateBoxes()
    {
        for(JRadioButton button: radioButtons.values())
        {
            buttonGroupKeys.remove(button);
        }		

        radioButtons.clear();

        for(final E key: model.getKeys())
        {
            final JRadioButton button = new JRadioButton(key.toString());
            radioButtons.put(key, button);
            buttonGroupKeys.add(button);

            boolean isIncluded = model.isSelected(key);
            button.setSelected(isIncluded);

            button.addItemListener(new ItemListener() {              
                @Override
                public void itemStateChanged(ItemEvent e) 
                {
                    boolean includedNew = button.isSelected();

                    if(includedNew)
                    {
                        model.setSelectedKey(key); 
                    }                    
                }
            });
        }		
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();
        if (SingleSelectionModel.SELECTED_KEY.equals(property)) 
        {
            Object selectedNew = evt.getNewValue();
            if(selectedNew != null)
            {
                JRadioButton box = radioButtons.get(selectedNew);
                box.setSelected(true);
            }
        }
        else if(SingleSelectionModel.KEY_SET_CHANGED.equals(property))
        {
            updatePanelBoxes();
        }
    }

    public Component getControls() 
    {
        return panelControls;
    }
}
