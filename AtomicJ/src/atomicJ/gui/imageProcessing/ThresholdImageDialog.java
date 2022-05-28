
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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.OperationDialog;
import atomicJ.gui.generalProcessing.ProcessingModel;
import atomicJ.utilities.GeometryUtilities;



public class ThresholdImageDialog extends OperationDialog <ThresholdFunctionModel> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private boolean initUseLowerThreshold;
    private boolean initUseUpperThreshold;

    private UnitExpression initLowerThreshold;
    private UnitExpression initUpperThreshold;
    private UnitExpression initLowerValue;
    private UnitExpression initUpperValue;

    private PrefixedUnit displayedUnit;

    private final JCheckBox boxUseLowerThreshold = new JCheckBox();
    private final JCheckBox boxUseUpperThreshold = new JCheckBox();

    private final JLabel labelLowerThreshold = new JLabel("Threshold");
    private final JLabel labelUpperThreshold = new JLabel("Threshold");

    private final JLabel labelLowerValue = new JLabel("Value");
    private final JLabel labelUpperValue = new JLabel("Value");

    private final JSpinner spinnerLowerValues = new JSpinner(new SpinnerNumberModel(0, -Integer.MAX_VALUE, Integer.MAX_VALUE, .001));
    private final JSpinner spinnerUpperValues = new JSpinner(new SpinnerNumberModel(0, -Integer.MAX_VALUE, Integer.MAX_VALUE, .001));
    private final JLabel labelLowerValueUnit = new JLabel();
    private final JLabel labelUpperValueUnit = new JLabel();

    private final JSpinner spinnerLowerThreshold = new JSpinner();
    private final JSpinner spinnerUpperThreshold = new JSpinner();
    private final JLabel labelLowerThresholdUnit = new JLabel();
    private final JLabel labelUpperThresholdUnit = new JLabel();

    public ThresholdImageDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title, temporary);

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListeners();
        initItemListeners();

        pack();
        setLocationRelativeTo(parent);
    }


    private void initChangeListeners()
    {
        spinnerLowerValues.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                ThresholdFunctionModel model = getModel();
                if(model != null)
                {
                    double valNew = ((SpinnerNumberModel)spinnerLowerValues.getModel()).getNumber().doubleValue();
                    model.setLowerValue(new UnitExpression(valNew, displayedUnit));
                }                
            }
        });
        spinnerUpperValues.addChangeListener(new ChangeListener() 
        {       
            @Override
            public void stateChanged(ChangeEvent e)
            {
                ThresholdFunctionModel model = getModel();
                if(model != null)
                {
                    double valNew = ((SpinnerNumberModel)spinnerUpperValues.getModel()).getNumber().doubleValue();
                    model.setUpperValue(new UnitExpression(valNew, displayedUnit));
                }                
            }
        });
        spinnerLowerThreshold.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e)
            {
                ThresholdFunctionModel model = getModel();
                double lowerThresholdNew = ((SpinnerNumberModel)spinnerLowerThreshold.getModel()).getNumber().doubleValue();

                ((SpinnerNumberModel)spinnerUpperThreshold.getModel()).setMinimum(lowerThresholdNew);    

                if(model != null)
                {
                    model.setLowerThreshold(new UnitExpression(lowerThresholdNew, displayedUnit));
                }
            }
        });
        spinnerUpperThreshold.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) 
            {
                ThresholdFunctionModel model = getModel();
                double upperThresholdNew = ((SpinnerNumberModel)spinnerUpperThreshold.getModel()).getNumber().doubleValue();

                ((SpinnerNumberModel)spinnerLowerThreshold.getModel()).setMaximum(upperThresholdNew);

                if(model != null)
                {
                    model.setUpperThreshold(new UnitExpression(upperThresholdNew, displayedUnit));
                }
            }
        });
    }	

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(ThresholdFunctionModel.USE_LOWER_THRESHOLD.equals(property))
        {
            boolean valNew = (boolean) evt.getNewValue();
            boolean valOld = boxUseLowerThreshold.isSelected();

            if(valOld != valNew)
            {
                boxUseLowerThreshold.setSelected(valNew);
            }

            //this must be outside the if!
            setEditorConsistentWithThresholdUse();
        }
        else if(ThresholdFunctionModel.USE_UPPER_THRSHOLD.equals(property))
        {
            boolean valNew = (boolean) evt.getNewValue();
            boolean valOld = boxUseUpperThreshold.isSelected();

            if(valOld != valNew)
            {
                boxUseUpperThreshold.setSelected(valNew);
            }

            //this must be outside the if!
            setEditorConsistentWithThresholdUse();
        }
        else if(ThresholdFunctionModel.LOWER_VALUE.equals(property))
        {
            UnitExpression lowerValueNew = (UnitExpression)evt.getNewValue();
            refresh(spinnerLowerValues, lowerValueNew);
        }
        else if(ThresholdFunctionModel.LOWER_THRESHOLD.equals(property))
        {
            //there is no reason to change the model limits of spinnerUpperThreshold
            //as the spinnerLowerThreshold will fire ChangeEvent, which in turn will
            //be listened to and will lead to change spinnerUpperThreshold model limits

            UnitExpression lowerThresholdNew = (UnitExpression)evt.getNewValue();
            refresh(spinnerLowerThreshold, lowerThresholdNew);

        }
        else if(ThresholdFunctionModel.UPPER_VALUE.equals(property))
        {
            UnitExpression upperValueNew = (UnitExpression)evt.getNewValue();
            refresh(spinnerUpperValues, upperValueNew);

        }
        else if(ThresholdFunctionModel.UPPER_THRESHOLD.equals(property))
        {
            //there is no reason to change the model limits of spinnerLowerThreshold
            //as the spinnerUpperThreshold will fire ChangeEvent, which in turn will
            //be listened to and will lead to change spinnerLowerThreshold model limits
            //
            UnitExpression upperThresholdNew = (UnitExpression)evt.getNewValue();           
            refresh(spinnerUpperThreshold, upperThresholdNew);
        }
        else if(ProcessingModel.VALUE_AXIS_DISPLAYED_UNIT.equals(property))
        {
            this.displayedUnit = (PrefixedUnit)evt.getNewValue();
            refreshUnitLabels();

            refresh(spinnerLowerValues, getModel().getLowerValue());
            refresh(spinnerLowerThreshold, getModel().getLowerThreshold());
            refresh(spinnerUpperValues, getModel().getUpperValue());
            refresh(spinnerUpperThreshold, getModel().getUpperThreshold());

            pack();
        }
    }

    private void refreshUnitLabels()
    {
        String unitLabel = displayedUnit != null ? displayedUnit.getFullName() : "";

        labelLowerValueUnit.setText(unitLabel);
        labelUpperValueUnit.setText(unitLabel);
        labelLowerThresholdUnit.setText(unitLabel);
        labelUpperThresholdUnit.setText(unitLabel);
    }

    private boolean refresh(JSpinner spinner, UnitExpression exprNew)
    {
        boolean refreshed = false;

        double valOld = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
        UnitExpression exprOld = new UnitExpression(valOld, displayedUnit);

        if(!GeometryUtilities.almostEqual(exprNew, exprOld, TOLERANCE))
        {
            double valNew = exprNew.derive(displayedUnit).getValue();
            spinner.setValue(valNew);

            refreshed = true;
        }

        return refreshed;
    }

    private void initItemListeners()
    {
        boxUseLowerThreshold.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                ThresholdFunctionModel model = getModel();

                setEditorConsistentWithThresholdUse();

                if(model != null)
                {
                    model.setUseLowerThreshold(selected);
                }             
            }
        });

        boxUseUpperThreshold.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                ThresholdFunctionModel model = getModel();
                setEditorConsistentWithThresholdUse();

                if(model != null)
                {
                    model.setUseUpperThreshold(selected);
                }            
            }
        });
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        ThresholdFunctionModel model = getModel();

        this.initUseLowerThreshold = model.isUseLowerThreshold();
        this.initUseUpperThreshold = model.isUseUpperThreshold();
        this.initLowerValue = model.getLowerValue();
        this.initUpperValue = model.getUpperValue();

        this.initLowerThreshold = model.getLowerThreshold();
        this.initUpperThreshold = model.getUpperThreshold();

        this.displayedUnit = model.getValueAxisDisplayedUnit();
    }

    @Override
    public void setModelToInitialState()
    {
        super.setModelToInitialState();

        ThresholdFunctionModel model = getModel();

        model.setUseLowerThreshold(initUseLowerThreshold);
        model.setUseUpperThreshold(initUseUpperThreshold);
        model.setLowerValue(initLowerValue);
        model.setUpperValue(initUpperValue);

        model.setLowerThreshold(initLowerThreshold);
        model.setUpperThreshold(initUpperThreshold);
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();    

        JPanel panelInsideOutside = buildPanelROIRelative();

        mainPanel.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(panelInsideOutside, 1, 0, 4, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        mainPanel.addComponent(new JLabel("Lower threshold"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxUseLowerThreshold, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

        mainPanel.addComponent(labelLowerThreshold, 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(spinnerLowerThreshold, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelLowerThresholdUnit, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(labelLowerValue, 5, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        mainPanel.addComponent(spinnerLowerValues, 6, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelLowerValueUnit, 7, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Upper threshold"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxUseUpperThreshold, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

        mainPanel.addComponent(labelUpperThreshold, 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(spinnerUpperThreshold, 3, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelUpperThresholdUnit, 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(labelUpperValue, 5, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        mainPanel.addComponent(spinnerUpperValues, 6, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelUpperValueUnit, 7, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }


    private void setEditorConsistentWithThresholdUse()
    {
        boolean useLower = boxUseLowerThreshold.isSelected();
        boolean useUpper = boxUseUpperThreshold.isSelected();

        labelLowerValue.setEnabled(useLower);
        labelLowerThreshold.setEnabled(useLower);      
        labelLowerValueUnit.setEnabled(useLower);
        labelLowerThresholdUnit.setEnabled(useLower);
        spinnerLowerThreshold.setEnabled(useLower);
        spinnerLowerValues.setEnabled(useLower);

        labelUpperValue.setEnabled(useUpper);
        labelUpperThreshold.setEnabled(useUpper);
        labelUpperValueUnit.setEnabled(useUpper);
        labelUpperThresholdUnit.setEnabled(useUpper);
        spinnerUpperThreshold.setEnabled(useUpper);
        spinnerUpperValues.setEnabled(useUpper);
    }

    @Override
    protected void resetEditor()
    {
        super.resetEditor();

        double initUpperThresholdValue = initUpperThreshold.derive(displayedUnit).getValue();
        double initLowerThresholdValue = initLowerThreshold.derive(displayedUnit).getValue();
        double initLowerValueValue = initLowerValue.derive(displayedUnit).getValue();
        double initUpperValueValue = initUpperValue.derive(displayedUnit).getValue();

        double span = initUpperThresholdValue - initLowerThresholdValue;

        int exp = (int)Math.rint(Math.floor(Math.log10(span))) - 2;
        double step = Math.pow(10, exp);

        SpinnerNumberModel spinnerLowerThresholdModel = new SpinnerNumberModel(initLowerThresholdValue, -Integer.MAX_VALUE, initUpperThresholdValue, step);
        SpinnerNumberModel spinnerUpperThresholdModel = new SpinnerNumberModel(initUpperThresholdValue, initLowerThresholdValue, Integer.MAX_VALUE, step);

        spinnerLowerThreshold.setModel(spinnerLowerThresholdModel);
        spinnerUpperThreshold.setModel(spinnerUpperThresholdModel);


        SpinnerNumberModel spinnerLowerValueModel = new SpinnerNumberModel(initLowerValueValue, -Integer.MAX_VALUE, Integer.MAX_VALUE, step);
        SpinnerNumberModel spinnerUpperValueModel = new SpinnerNumberModel(initUpperValueValue, -Integer.MAX_VALUE, Integer.MAX_VALUE, step);

        spinnerLowerValues.setModel(spinnerLowerValueModel);
        spinnerUpperValues.setModel(spinnerUpperValueModel);

        boxUseLowerThreshold.setSelected(initUseLowerThreshold);
        boxUseUpperThreshold.setSelected(initUseUpperThreshold);

        setEditorConsistentWithThresholdUse();

        refreshUnitLabels();

        pack();
    }
}
