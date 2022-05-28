
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

package atomicJ.gui.statistics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import atomicJ.utilities.Validation;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;

import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class InferenceSelectionPageView extends JPanel implements PropertyChangeListener, ActionListener
{
    private static final long serialVersionUID = 1L;

    private InferenceSelectionPage model;

    private ButtonGroup buttonGroup;
    private final Map<String, JRadioButton> buttons = new LinkedHashMap<>();

    public InferenceSelectionPageView()
    {
        setLayout(new BorderLayout());
    }	

    public void setPageModel(InferenceSelectionPage modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.buttonGroup = new ButtonGroup();
        this.model = modelNew;
        model.addPropertyChangeListener(this);

        for(JRadioButton button: buttons.values())
        {
            button.removeActionListener(this);
        }

        buttons.clear();

        for(Entry<String,StatisticalTestPage> entry: model.getTests().entrySet())
        {
            String name = entry.getKey();
            JRadioButton button = new JRadioButton(name);
            button.setActionCommand(name);
            button.addActionListener(this);

            buttonGroup.add(button);
            buttons.put(name, button);
        }

        String selectedTestName = model.getCurrentTestPage().getName();
        selectRadioButton(selectedTestName);	

        updatePanelBoxes();
    }

    private void updatePanelBoxes()
    {		
        JPanel panelRadioButtons = new JPanel();

        JLabel labelDatasets = new JLabel("Available tests");

        GroupLayout layout = new GroupLayout(panelRadioButtons);
        panelRadioButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        //builds and sets horizontal group
        SequentialGroup horizontalGroup = layout.createSequentialGroup();
        ParallelGroup innerHorizontalGroup = layout.createParallelGroup().addComponent(labelDatasets);

        for(JRadioButton box: buttons.values())
        {
            innerHorizontalGroup.addComponent(box);
        }

        horizontalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(innerHorizontalGroup).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setHorizontalGroup(horizontalGroup);

        //builds and sets vertical group

        SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(labelDatasets).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

        for(JRadioButton button: buttons.values())
        {
            verticalGroup.addComponent(button).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        }

        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setVerticalGroup(verticalGroup);

        layout.linkSize(buttons.values().toArray(new JRadioButton[] {}));

        removeAll();
        add(panelRadioButtons, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if (SELECTED_TEST.equals(name)) 
        {
            String selectedTestName = (String)evt.getNewValue();
            selectRadioButton(selectedTestName);
        }
    }

    private void selectRadioButton(String name)
    {
        JRadioButton button = buttons.get(name);
        if(button == null)
        {
            buttonGroup.clearSelection();
            return;
        }

        if(!button.isSelected())
        {
            buttonGroup.clearSelection();
            buttonGroup.setSelected(button.getModel(), true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        Object source = evt.getSource();
        if (buttons.containsValue(source)) 
        {
            JRadioButton button = (JRadioButton)source;
            String selectedTest = button.getActionCommand();

            model.setTest(selectedTest);
        }
    }
}
