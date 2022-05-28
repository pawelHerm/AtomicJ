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


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.util.ObjectUtilities;

import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BasicOperationDialog;
import atomicJ.gui.rois.line.ROICurveType;


public class SplitROIDialog extends BasicOperationDialog<SplitROIModel> 
{
    private static final long serialVersionUID = 1L;

    private boolean initDeleteOriginalROIs;
    private ROICurveType initROICurveType;

    private final JCheckBox boxDeleteOriginalROIs = new JCheckBox("Delete original ROIs");
    private final JComboBox<ROICurveType> comboROICurveType = new JComboBox<>(ROICurveType.values()); 

    public SplitROIDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title, temporary, ModalityType.MODELESS);

        JPanel view = buildView();
        JPanel panelButtons = buildButtonPanel();

        add(view, BorderLayout.NORTH);     
        add(panelButtons, BorderLayout.SOUTH);      

        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initItemListener()
    {
        comboROICurveType.addItemListener(new ItemListener() 
        {     
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                ROICurveType type = (ROICurveType)comboROICurveType.getSelectedItem();
                getModel().setSplittingCurveType(type);
            }
        });

        boxDeleteOriginalROIs.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setDeleteOriginalROIs(selected);
            }
        });
    }   

    private JPanel buildView()
    {   
        SubPanel view = new SubPanel(); 

        SubPanel panelSettings = new SubPanel();

        JLabel labelSectionType = new JLabel("Section type");

        panelSettings.addComponent(labelSectionType, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelSettings.addComponent(comboROICurveType, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelSettings.addComponent(boxDeleteOriginalROIs, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        view.addComponent(panelSettings, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);

        view.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return view;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        SplitROIModel model = getModel();

        this.initDeleteOriginalROIs = model.isDeleteOriginalROIs();
        this.initROICurveType = model.getSplittingCurveType();    
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        SplitROIModel model = getModel();

        model.setDeleteOriginalROIs(initDeleteOriginalROIs);
        model.setSplittingCurveType(initROICurveType);
    }

    @Override
    protected void resetEditor()
    {   
        super.resetEditor();

        boxDeleteOriginalROIs.setSelected(initDeleteOriginalROIs);
        comboROICurveType.setSelectedItem(initROICurveType);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(SplitROIModel.DELETE_ORIGINAL_ROIS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteOriginalROIs.isSelected();

            if(valOld != valNew)
            {
                boxDeleteOriginalROIs.setSelected(valNew);
            }
        }
        else if(SplitROIModel.SPLITTING_CURVE_TYPE.equals(property))
        {
            ROICurveType newVal = (ROICurveType)evt.getNewValue();
            ROICurveType oldVal = (ROICurveType)comboROICurveType.getSelectedItem();

            if(!ObjectUtilities.equal(newVal, oldVal))
            {
                comboROICurveType.setSelectedItem(newVal);
            }
        }
    }
}
