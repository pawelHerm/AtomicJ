
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

package atomicJ.gui.imageProcessing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;
import atomicJ.gui.imageProcessing.Gridding2DModel.GridSize;

public class Gridding2DDialog extends BatchProcessingDialog <Gridding2DModel, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private int initRowCount;
    private int initColumnCount;
    private InterpolationMethod2D initInterpolationMethod;
    private GridSize initialUniqueGrid;

    private final JComboBox<InterpolationMethod2D> comboInterpolationMethod = new JComboBox<>(InterpolationMethod2D.values()); 

    private final JSpinner spinnerRowCount = new JSpinner(new SpinnerNumberModel(1, 1, Short.MAX_VALUE, 1));
    private final JSpinner spinnerColumnCount = new JSpinner(new SpinnerNumberModel(1, 1, Short.MAX_VALUE, 1));

    private final JLabel labelOriginalRowCount = new JLabel();
    private final JLabel labelOriginalColumnCount = new JLabel();

    public Gridding2DDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();
        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initItemListener()
    {
        comboInterpolationMethod.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                InterpolationMethod2D interpolationMethodNew = (InterpolationMethod2D) comboInterpolationMethod.getSelectedItem();
                getModel().setInterpolationMethod(interpolationMethodNew);
            }
        });
    }

    private void initChangeListener()
    {
        spinnerRowCount.addChangeListener(new ChangeListener() 
        {            
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int rowCount = ((SpinnerNumberModel)spinnerRowCount.getModel()).getNumber().intValue();
                getModel().setRowCount(rowCount);                
            }
        });
        spinnerColumnCount.addChangeListener(new ChangeListener() 
        {
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int columnCount = ((SpinnerNumberModel)spinnerColumnCount.getModel()).getNumber().intValue();
                getModel().setColumnCount(columnCount);
            }
        });
    }	

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();     

        SubPanel panelBatchType = new SubPanel();
        panelBatchType.addComponent(new JLabel("Sources:   "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelIdentifierType = new SubPanel();
        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        JLabel labelCurrentSize = new JLabel("Original size");
        JLabel labelOldSize = new JLabel("New size");
        JLabel labelInterpolationMethod = new JLabel("Interpolation");

        labelCurrentSize.setFont(labelCurrentSize.getFont().deriveFont(Font.BOLD));
        labelOldSize.setFont(labelOldSize.getFont().deriveFont(Font.BOLD));
        labelInterpolationMethod.setFont(labelInterpolationMethod.getFont().deriveFont(Font.BOLD));

        SubPanel panelSettings = new SubPanel();    

        panelSettings.addComponent(labelCurrentSize, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(6,3,5,5));

        panelSettings.addComponent(new JLabel("X (px)"), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(labelOriginalColumnCount, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(new JLabel("Y (px)"), 3, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(labelOriginalRowCount, 4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);

        panelSettings.addComponent(labelOldSize, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(6,3,5,5));

        panelSettings.addComponent(new JLabel("X (px)"), 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerColumnCount, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(new JLabel("Y (px)"), 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);     
        panelSettings.addComponent(spinnerRowCount, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelSettings.addComponent(labelInterpolationMethod, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(6,3,5,5));
        panelSettings.addComponent(comboInterpolationMethod, 1, 2, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        panelSettings.addComponent(Box.createHorizontalStrut(10), 5, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(buildPanelPreview(), 6, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSettings.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        mainPanel.addComponent(panelBatchType, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        Gridding2DModel model = getModel();

        this.initRowCount = model.getRowCount();
        this.initColumnCount = model.getColumnCount();
        this.initInterpolationMethod = model.getInterpolationMethod();
        this.initialUniqueGrid = model.getUniqueGridSize();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        Gridding2DModel model = getModel();

        model.setGridSize(initRowCount, initColumnCount);
        model.setInterpolationMethod(initInterpolationMethod);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerRowCount.setValue(initRowCount);
        spinnerColumnCount.setValue(initColumnCount);
        comboInterpolationMethod.setSelectedItem(initInterpolationMethod);
        refreshLabels(initialUniqueGrid);

        pack();
    }

    private void refreshLabels(GridSize gridSize)
    {
        String rowCountText = (gridSize != null) ? Integer.toString(gridSize.getRowCount()) : "";
        String columnCountText = (gridSize != null) ? Integer.toString(gridSize.getColumnCount()) : "";

        labelOriginalRowCount.setText(rowCountText);
        labelOriginalColumnCount.setText(columnCountText);

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(Gridding2DModel.ROW_COUNT.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRowCount.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRowCount.setValue(valueNew);
            }
        }
        else if(Gridding2DModel.COLUMN_COUNT.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerColumnCount.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerColumnCount.setValue(valueNew);
            }
        }
        else if(Gridding2DModel.INTERPOLATION_METHOD.equals(property))
        {
            InterpolationMethod2D valueNew = (InterpolationMethod2D)evt.getNewValue();
            InterpolationMethod2D valueOld = (InterpolationMethod2D) comboInterpolationMethod.getSelectedItem();

            if(!Objects.equals(valueOld, valueNew))
            {
                comboInterpolationMethod.setSelectedItem(valueNew);
            }
        }
        else if(Gridding2DModel.UNIQUE_GRID_SIZE.equals(property))
        {
            GridSize valNew = (GridSize)evt.getNewValue();
            refreshLabels(valNew);
        }
    }
}
