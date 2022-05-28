
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
import atomicJ.imageProcessing.LineFitRegressionStrategy;


public class LineFitCorrectionDialog extends BatchProcessingDialog<LineFitCorrectionModel, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private int initFitDegree;
    private LineFitRegressionStrategy initRegressionStrategy;
    private ImageLineOrientation initLineOrientation;

    private double initMinimalLineLength;

    private final JLabel labelLineLength = new JLabel("Min line length (%)");

    private final JSpinner spinnerMinimalLineLength = new JSpinner(new SpinnerNumberModel(1., 0., 100., 1.));

    private final JSpinner spinnerFitDegree = new JSpinner(new SpinnerNumberModel(1,0, 100, 1));
    private final JComboBox<LineFitRegressionStrategy> comboRegressionStrategy = new JComboBox<>(LineFitRegressionStrategy.values()); 
    private final JComboBox<ImageLineOrientation> comboLineOrientation = new JComboBox<>(ImageLineOrientation.values()); 


    public LineFitCorrectionDialog(Window parent, String title, boolean temporary)
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
        spinnerFitDegree.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int deg = ((SpinnerNumberModel)spinnerFitDegree.getModel()).getNumber().intValue();
                getModel().setFitDegree(deg);                
            }
        });
        spinnerMinimalLineLength.addChangeListener(new ChangeListener() 
        {  
            @Override
            public void stateChanged(ChangeEvent e)
            {              
                double minimalLineLength = ((SpinnerNumberModel)spinnerMinimalLineLength.getModel()).getNumber().doubleValue();
                getModel().setMinimalLineLengthPercent(minimalLineLength);
            }
        });
    }

    private void initItemListener()
    {
        comboRegressionStrategy.addItemListener(new ItemListener() 
        {     
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                LineFitRegressionStrategy measure = (LineFitRegressionStrategy)comboRegressionStrategy.getSelectedItem();
                getModel().setRegessionStrategy(measure);
            }
        });
        comboLineOrientation.addItemListener(new ItemListener() 
        {       
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                ImageLineOrientation lineOrientation = (ImageLineOrientation)comboLineOrientation.getSelectedItem();
                getModel().setLineOrientation(lineOrientation);                
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

        panelSettings.addComponent(new JLabel("Degree"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerFitDegree, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);             

        panelSettings.addComponent(new JLabel("Method"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(comboRegressionStrategy, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      

        panelSettings.addComponent(new JLabel("Line orientation"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(comboLineOrientation, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);            
        panelSettings.addComponent(Box.createHorizontalGlue(), 0, 0, 2, 4, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(buildPanelPreview(), 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        LineFitCorrectionModel model = getModel();

        this.initMinimalLineLength = model.getMinimalLineLengthPercent();

        this.initRegressionStrategy = model.getRegressionStrategy();
        this.initFitDegree = model.getFitDegree();
        this.initLineOrientation = model.getLineOrientation();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        LineFitCorrectionModel model = getModel();

        model.setMinimalLineLengthPercent(initMinimalLineLength);
        model.setRegessionStrategy(initRegressionStrategy);
        model.setFitDegree(initFitDegree);
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

        spinnerFitDegree.setValue(initFitDegree);
        comboRegressionStrategy.setSelectedItem(initRegressionStrategy);
        comboLineOrientation.setSelectedItem(initLineOrientation);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(LineFitCorrectionModel.REGRESSION_STRATEGY.equals(property))
        {
            LineFitRegressionStrategy newVal = (LineFitRegressionStrategy)evt.getNewValue();
            LineFitRegressionStrategy oldVal = (LineFitRegressionStrategy)comboRegressionStrategy.getSelectedItem();

            if(!(Objects.equals(newVal, oldVal)))
            {
                comboRegressionStrategy.setSelectedItem(newVal);
            }
        }
        else if(LineFitCorrectionModel.FIT_DEGREE.equals(property))
        {
            int newVal = (int)evt.getNewValue();
            int oldVal = ((SpinnerNumberModel)spinnerFitDegree.getModel()).getNumber().intValue();

            if(oldVal != newVal)
            {
                spinnerFitDegree.setValue(newVal);
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
        else if(LineFitCorrectionModel.MINIMAL_LINE_LENGTH_PERCENT.equals(property))
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
