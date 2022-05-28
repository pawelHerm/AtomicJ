
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
import java.awt.GridBagConstraints;
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

import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;
import atomicJ.gui.generalProcessing.OperationModel;
import atomicJ.imageProcessing.LocationMeasure;


public class LineMatchingCorrectionDialog extends BatchProcessingDialog<LineMatchingCorrectionModel, String> implements 
PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private LocationMeasure initLocationMeasure;
    private ImageLineOrientation initLineOrientation;

    private double initMinimalLineLength;

    private final JLabel labelLineLength = new JLabel("Min line length (%)");

    private final JSpinner spinnerMinimalLineLength = new JSpinner(new SpinnerNumberModel(1., 0., 100., 1.));

    private final JComboBox<LocationMeasure> comboLocationMeasure = new JComboBox<>(LocationMeasure.values()); 
    private final JComboBox<ImageLineOrientation> comboLineOrientation = new JComboBox<>(ImageLineOrientation.values()); 

    public LineMatchingCorrectionDialog(Window parent, String title, boolean temporary)
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

    private void initChangeListener()
    {
        spinnerMinimalLineLength.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double minimalLineLength = ((SpinnerNumberModel)spinnerMinimalLineLength.getModel()).getNumber().doubleValue();
                LineMatchingCorrectionModel model = getModel();
                model.setMinimalLineLengthPercent(minimalLineLength);
            }
        });
    }

    private void initItemListener()
    {
        comboLocationMeasure.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                LineMatchingCorrectionModel model = getModel();
                LocationMeasure measure = (LocationMeasure)comboLocationMeasure.getSelectedItem();
                model.setLocationMeasure(measure);
            }
        });

        comboLineOrientation.addItemListener(new ItemListener() 
        {     
            @Override
            public void itemStateChanged(ItemEvent evt)
            {      
                LineMatchingCorrectionModel model = getModel();
                ImageLineOrientation lineOrientation = (ImageLineOrientation)comboLineOrientation.getSelectedItem();
                model.setLineOrientation(lineOrientation);
            }
        });
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();         

        SubPanel panelActiveArea = new SubPanel();

        panelActiveArea.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);
        panelActiveArea.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelBatchType = new SubPanel();
        panelBatchType.addComponent(new JLabel("Sources: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelIdentifierType = new SubPanel();
        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelSettings = new SubPanel();        

        panelSettings.addComponent(labelLineLength, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerMinimalLineLength, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);             

        panelSettings.addComponent(new JLabel("Measure"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(comboLocationMeasure, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

        panelSettings.addComponent(new JLabel("Orientation"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(comboLineOrientation, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(Box.createHorizontalGlue(), 0, 0, 2, 3, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(buildPanelPreview(), 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        LineMatchingCorrectionModel model = getModel();

        this.initMinimalLineLength = model.getMinimalLineLengthPercent();
        this.initLocationMeasure = model.getLocationMeasure();
        this.initLineOrientation = model.getLineOrientation();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        LineMatchingCorrectionModel model = getModel();

        model.setMinimalLineLengthPercent(initMinimalLineLength);
        model.setLocationMeasure(initLocationMeasure);
        model.setLineOrientation(initLineOrientation);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        OperationModel model = getModel();
        boolean roisEnabled = model.areROIsAvailable();

        labelLineLength.setEnabled(roisEnabled);
        spinnerMinimalLineLength.setEnabled(roisEnabled);

        spinnerMinimalLineLength.setValue(initMinimalLineLength);
        comboLocationMeasure.setSelectedItem(initLocationMeasure);
        comboLineOrientation.setSelectedItem(initLineOrientation);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(LineMatchingCorrectionModel.LOCATION_MEASURE.equals(property))
        {
            LocationMeasure newVal = (LocationMeasure)evt.getNewValue();
            LocationMeasure oldVal = (LocationMeasure)comboLocationMeasure.getSelectedItem();

            if(!(Objects.equals(newVal, oldVal)))
            {
                comboLocationMeasure.setSelectedItem(newVal);
            }
        }
        else if(LineMatchingCorrectionModel.IMAGE_LINE_ORIENTATION.equals(property))
        {
            ImageLineOrientation newVal = (ImageLineOrientation)evt.getNewValue();
            ImageLineOrientation oldVal = (ImageLineOrientation)comboLineOrientation.getSelectedItem();

            if(!(Objects.equals(newVal, oldVal)))
            {
                comboLineOrientation.setSelectedItem(newVal);
            }
        }
        else if(LineMatchingCorrectionModel.MINIMAL_LINE_LENGTH_PERCENT.equals(property))
        {
            Double newVal = (Double)evt.getNewValue();
            Double oldVal = ((SpinnerNumberModel)spinnerMinimalLineLength.getModel()).getNumber().doubleValue();

            if(!Objects.equals(newVal, oldVal))
            {
                spinnerMinimalLineLength.setValue(newVal);
            }
        }
    }
}
