
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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.SubPanel;
import atomicJ.gui.imageProcessing.OperationSimpleDialog;


public class WandROIDialog extends OperationSimpleDialog <WandROIModel> implements ItemListener
{
    private static final long serialVersionUID = 1L;

    private double initMinDifference;
    private double initMaxDifference;
    private boolean initFillHoles;
    private PrefixedUnit initDifferenceUnit;

    private final JSpinner spinnerMinDifference = new JSpinner();
    private final JSpinner spinnerMaxDifference = new JSpinner();

    private final JLabel labelMinDifferenceUnit = new JLabel();
    private final JLabel labelMaxDifferenceUnit = new JLabel();

    private final JCheckBox boxFillHoles = new JCheckBox();

    public WandROIDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title, temporary, ModalityType.MODELESS);

        add(buildMainPanel(), BorderLayout.NORTH);   	

        initChangeListener();
        initItemListener();

        ensureCommitOnEdit(spinnerMinDifference);
        ensureCommitOnEdit(spinnerMaxDifference);

        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(WandROIModel.MIN_DIFFERENCE.equals(property))
        {
            double valueOld = ((Number)spinnerMinDifference.getValue()).doubleValue();
            double valueNew = (double)evt.getNewValue();

            if(valueOld != valueNew)
            {
                spinnerMinDifference.setValue(valueNew);
            }
        }
        else if(WandROIModel.MAX_DIFFERENCE.equals(property))
        {
            double valueOld = ((Number)spinnerMaxDifference.getValue()).doubleValue();
            double valueNew = (double)evt.getNewValue();

            if(valueOld != valueNew)
            {
                spinnerMaxDifference.setValue(valueNew);
            }
        }
        else if(WandROIModel.DIFFERENCE_UNIT.equals(property))
        {
            PrefixedUnit unitNew = (PrefixedUnit)evt.getNewValue();
            setConsistentWithDifferenceUnit(unitNew);
            pack();
        }
        else if(WandROIModel.FILL_HOLES.equals(property))
        {
            boolean valueOld = boxFillHoles.isSelected();
            boolean valueNew = (boolean)evt.getNewValue();

            if(valueOld != valueNew)
            {
                boxFillHoles.setSelected(valueNew);
            }
        }
    }

    private void initItemListener()
    {
        boxFillHoles.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (ItemEvent.SELECTED == e.getStateChange());
                WandROIModel model = getModel();

                if(model != null)
                {
                    model.setFillHoles(selected);
                }
            }
        });
    }

    private void initChangeListener()
    {      
        spinnerMinDifference.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent e) 
            {                
                double minDifferenceNew = ((SpinnerNumberModel)spinnerMinDifference.getModel()).getNumber().doubleValue();
                ((SpinnerNumberModel)spinnerMaxDifference.getModel()).setMinimum(minDifferenceNew);    

                WandROIModel model = getModel();
                if(model != null)
                {
                    model.setMinDifference(minDifferenceNew);
                }                
            }
        });
        spinnerMaxDifference.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                double maxDifferenceNew = ((SpinnerNumberModel)spinnerMaxDifference.getModel()).getNumber().doubleValue();
                ((SpinnerNumberModel)spinnerMinDifference.getModel()).setMaximum(maxDifferenceNew);

                WandROIModel model = getModel();
                if(model != null)
                {

                    model.setMaxDifference(maxDifferenceNew);
                }                
            }
        });
    }	

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        WandROIModel model = getModel();

        this.initMinDifference = model.getMinDifference();
        this.initMaxDifference = model.getMaxDifference();
        this.initDifferenceUnit = model.getDifferenceUnit();
        this.initFillHoles = model.isFillHoles();
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

        mainPanel.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, .2, 1);
        mainPanel.addComponent(panelInsideOutside, 1, 0, 5, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        //      
        //        mainPanel.addComponent(new JLabel("Holes filled"), 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        //        mainPanel.addComponent(boxFillHoles, 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        //      
        mainPanel.addComponent(new JLabel("Min difference"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        mainPanel.addComponent(spinnerMinDifference, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelMinDifferenceUnit, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        mainPanel.addComponent(new JLabel("Max difference"), 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        mainPanel.addComponent(spinnerMaxDifference, 4, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelMaxDifferenceUnit, 5, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void resetEditor()
    {
        super.resetEditor();

        //        int exp = (int)Math.rint(Math.floor(Math.log10(initMaxDifference - initMinDifference))) - 2;
        //        double step = Math.pow(10, exp);

        double step = 1;
        SpinnerNumberModel spinnerMinDifferenceModel = new SpinnerNumberModel(initMinDifference, -Short.MAX_VALUE, initMaxDifference, step);
        spinnerMinDifference.setModel(spinnerMinDifferenceModel);

        SpinnerNumberModel spinnerMaxDifferenceModel = new SpinnerNumberModel(initMaxDifference, initMinDifference, Short.MAX_VALUE, step);
        spinnerMaxDifference.setModel(spinnerMaxDifferenceModel);

        boxFillHoles.setSelected(initFillHoles);

        ensureCommitOnEdit(spinnerMinDifference);
        ensureCommitOnEdit(spinnerMaxDifference);

        setConsistentWithDifferenceUnit(initDifferenceUnit);

        pack();
    }


    private void setConsistentWithDifferenceUnit(PrefixedUnit unitNew)
    {
        String labelText = (unitNew != null) ? unitNew.getFullName() : "";
        labelMinDifferenceUnit.setText(labelText);
        labelMaxDifferenceUnit.setText(labelText);
    }
}
