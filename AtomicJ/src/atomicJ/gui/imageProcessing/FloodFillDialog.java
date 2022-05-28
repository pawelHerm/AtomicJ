
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.ProcessingModel;
import atomicJ.utilities.GeometryUtilities;


public class FloodFillDialog extends OperationSimpleDialog <FloodFillModel> implements ItemListener
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private UnitExpression initFillValue;
    private UnitExpression initMinDifference;
    private UnitExpression initMaxDifference;
    private boolean initFillHoles;

    private PrefixedUnit displayedUnit;

    private final JSpinner spinnerFillValue = new JSpinner(new SpinnerNumberModel(0, -Short.MAX_VALUE, Short.MAX_VALUE, 0.01));

    private final JSpinner spinnerMinDifference = new JSpinner();
    private final JSpinner spinnerMaxDifference = new JSpinner();

    private final JLabel labelFillValueUnit = new JLabel();
    private final JLabel labelMinDifferenceUnit = new JLabel();
    private final JLabel labelMaxDifferenceUnit = new JLabel();

    private final JCheckBox boxFillHoles = new JCheckBox();

    public FloodFillDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title, temporary, ModalityType.MODELESS);

        add(buildMainPanel(), BorderLayout.NORTH);   	

        ensureCommitOnEdit(spinnerFillValue);
        ensureCommitOnEdit(spinnerMinDifference);
        ensureCommitOnEdit(spinnerMaxDifference);

        initChangeListener();
        initItemListener();
        initWindowListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initWindowListener()
    {
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {                
                FloodFillModel model = getModel();
                if(model != null)
                {
                    model.operationFinished();
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(FloodFillModel.FILL_VALUE.equals(property))
        {
            UnitExpression valueNew = (UnitExpression)evt.getNewValue();
            refresh(spinnerFillValue, valueNew);
        }
        else if(FloodFillModel.MIN_DIFFERENCE.equals(property))
        {
            UnitExpression valueNew = (UnitExpression)evt.getNewValue();
            refresh(spinnerMinDifference, valueNew);
        }
        else if(FloodFillModel.MAX_DIFFERENCE.equals(property))
        {
            UnitExpression valueNew = (UnitExpression)evt.getNewValue();
            refresh(spinnerMaxDifference, valueNew);
        }
        else if(FloodFillModel.FILL_HOLES.equals(property))
        {
            boolean valueOld = boxFillHoles.isSelected();
            boolean valueNew = (boolean)evt.getNewValue();

            if(valueOld != valueNew)
            {
                boxFillHoles.setSelected(valueNew);
            }
        }
        else if(ProcessingModel.VALUE_AXIS_DISPLAYED_UNIT.equals(property))
        {
            this.displayedUnit = (PrefixedUnit)evt.getNewValue();
            refreshUnitLabels();

            refresh(spinnerFillValue, getModel().getFillValue());
            refresh(spinnerMinDifference, getModel().getMinDifference());
            refresh(spinnerMaxDifference, getModel().getMaxDifference());

            pack();
        }
    }

    private void refreshUnitLabels()
    {
        String unitLabel = displayedUnit != null ? displayedUnit.getFullName() : "";

        this.labelFillValueUnit.setText(unitLabel);
        this.labelMinDifferenceUnit.setText(unitLabel);
        this.labelMaxDifferenceUnit.setText(unitLabel);
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

    private void initItemListener()
    {
        boxFillHoles.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (ItemEvent.SELECTED == e.getStateChange());
                FloodFillModel model = getModel();

                if(model != null)
                {
                    model.setFillHoles(selected);
                }
            }
        });
    }

    private void initChangeListener()
    {
        spinnerFillValue.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                double fillValueNew = ((SpinnerNumberModel)spinnerFillValue.getModel()).getNumber().doubleValue();

                FloodFillModel model = getModel();
                if(model != null)
                {
                    model.setFillValue(new UnitExpression(fillValueNew, displayedUnit));
                }                
            }
        });
        spinnerMinDifference.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent e) 
            {                
                double minDifferenceNew = ((SpinnerNumberModel)spinnerMinDifference.getModel()).getNumber().doubleValue();
                ((SpinnerNumberModel)spinnerMaxDifference.getModel()).setMinimum(minDifferenceNew);    

                FloodFillModel model = getModel();
                if(model != null)
                {
                    model.setMinDifference(new UnitExpression(minDifferenceNew, displayedUnit));
                }                
            }
        });
        spinnerMaxDifference.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                double maxDifferenceNew = ((SpinnerNumberModel)spinnerMaxDifference.getModel()).getNumber().doubleValue();
                ((SpinnerNumberModel)spinnerMinDifference.getModel()).setMaximum(maxDifferenceNew);

                FloodFillModel model = getModel();
                if(model != null)
                {
                    model.setMaxDifference(new UnitExpression(maxDifferenceNew, displayedUnit));
                }                
            }
        });
    }	

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        FloodFillModel model = getModel();

        this.initFillValue = model.getFillValue(); 
        this.initMinDifference = model.getMinDifference();
        this.initMaxDifference = model.getMaxDifference();
        this.initFillHoles = model.isFillHoles();

        this.displayedUnit = model.getValueAxisDisplayedUnit();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        FloodFillModel model = getModel();

        model.setFillValue(initFillValue); 
        model.setMinDifference(initMinDifference);
        model.setMaxDifference(initMaxDifference);
        model.setFillHoles(initFillHoles);
    }

    private void ensureCommitOnEdit(JSpinner spinner)
    { 
        JFormattedTextField field = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);     
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();    

        JPanel panelInsideOutside = buildPanelROIRelative();

        mainPanel.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .2, 1);
        mainPanel.addComponent(panelInsideOutside, 1, 0, 5, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("Fill value"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(spinnerFillValue, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelFillValueUnit, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("Holes filled"), 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(boxFillHoles, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("Min difference"), 5, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(spinnerMinDifference, 6, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelMinDifferenceUnit, 7, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("Max difference"), 8, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(spinnerMaxDifference, 9, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelMaxDifferenceUnit, 10, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void resetEditor()
    {
        super.resetEditor();

        double initMinDifferenceValue = initMinDifference.derive(displayedUnit).getValue();
        double initMaxDifferenceValue = initMaxDifference.derive(displayedUnit).getValue();
        double initFillValueValue = initFillValue.derive(displayedUnit).getValue();

        double step = 1;
        SpinnerNumberModel spinnerMinDifferenceModel = new SpinnerNumberModel(initMinDifferenceValue, -Short.MAX_VALUE, initMaxDifferenceValue, step);
        spinnerMinDifference.setModel(spinnerMinDifferenceModel);

        SpinnerNumberModel spinnerMaxDifferenceModel = new SpinnerNumberModel(initMaxDifferenceValue, initMinDifferenceValue, Short.MAX_VALUE, step);
        spinnerMaxDifference.setModel(spinnerMaxDifferenceModel);

        SpinnerNumberModel spinnerFillValueModel = new SpinnerNumberModel(initFillValueValue, -Short.MAX_VALUE, Short.MAX_VALUE, step);
        spinnerFillValue.setModel(spinnerFillValueModel);

        ensureCommitOnEdit(spinnerFillValue);
        ensureCommitOnEdit(spinnerMinDifference);
        ensureCommitOnEdit(spinnerMaxDifference);

        boxFillHoles.setSelected(initFillHoles);

        refreshUnitLabels();

        pack();
    }
}
