
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

package atomicJ.gui.stack;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.units.Quantity;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.OperationDialog;
import atomicJ.utilities.GeometryUtilities;


public class StackParametersDialogNew extends OperationDialog<StackParametersModel> implements ChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-14;

    private final JCheckBox boxFixContactPoint = new JCheckBox("Fix contact");

    private final JLabel labelMinimum = new JLabel();
    private final JLabel labelMaximum = new JLabel();

    private final JSpinner spinnerMinimum = new JSpinner(new SpinnerNumberModel(0, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerMaximum = new JSpinner(new SpinnerNumberModel(1, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerFrameCount = new JSpinner(new SpinnerNumberModel(100, 0, Integer.MAX_VALUE, 1));

    private String stackName;
    private Quantity initDataQuantity;
    private boolean initFixContact;
    private double initStackMinimum;
    private double initStackMaximum;
    private int initFrameCount;

    public StackParametersDialogNew(Window parent)
    {
        super(parent, "", false);

        setLayout(new BorderLayout());
        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initItemListener();
        initChangeListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initItemListener()
    {
        boxFixContactPoint.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setFixContact(selected);
            }
        });
    }

    private void initChangeListener()
    {
        spinnerMinimum.addChangeListener(this);
        spinnerMaximum.addChangeListener(this);
        spinnerFrameCount.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int frameCountNew = ((Number)spinnerFrameCount.getValue()).intValue();
                getModel().setFrameCount(frameCountNew);
            }
        });
    }	

    //I dont really know why it is necessary to remove and add changeListeners, but it is necessary, otherwise
    //spinners would not increment 
    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerMinimum)
        {
            double minimum = ((SpinnerNumberModel)spinnerMinimum.getModel()).getNumber().doubleValue();
            setMinimumConstraintOnSpinnerMaximum(minimum);
            getModel().setStackMinimum(minimum);
        }
        else if(source == spinnerMaximum)
        {
            double maximum = ((SpinnerNumberModel)spinnerMaximum.getModel()).getNumber().doubleValue();
            setMaximumConstraintOnSpinnerMinimum(maximum);
            getModel().setStackMaximum(maximum);
        }
    }	

    private void setMinimumConstraintOnSpinnerMaximum(double minimumNew)
    {
        spinnerMaximum.removeChangeListener(this);
        ((SpinnerNumberModel)spinnerMaximum.getModel()).setMinimum(minimumNew);
        spinnerMaximum.addChangeListener(this);
    }

    private void setMaximumConstraintOnSpinnerMinimum(double maximumNew)
    {
        spinnerMinimum.removeChangeListener(this);
        ((SpinnerNumberModel)spinnerMinimum.getModel()).setMaximum(maximumNew);
        spinnerMinimum.addChangeListener(this);
    }

    @Override
    protected void ok()
    {
        StackParametersModel model = getModel();
        model.ok();

        setVisible(false);
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        StackParametersModel model = getModel();

        this.initDataQuantity = model.getDataQuantity();
        this.stackName = model.getStackName();

        this.initFixContact = model.isFixContact();
        this.initStackMinimum = model.getStackMinimum();
        this.initStackMaximum = model.getStackMaximum();
        this.initFrameCount = model.getFrameCount();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        StackParametersModel model = getModel();

        model.setFixContact(initFixContact);
        model.setStackRange(initStackMinimum, initStackMaximum);
        model.setFrameCount(initFrameCount);
    }

    @Override
    protected void resetEditor()
    {   
        super.resetEditor();

        setTitle(stackName);

        String unitLabel = initDataQuantity.getFullUnitName();

        labelMinimum.setText(unitLabel);
        labelMaximum.setText(unitLabel);      

        boxFixContactPoint.setSelected(initFixContact);
        spinnerMinimum.setModel(new SpinnerNumberModel(initStackMinimum, -Integer.MAX_VALUE, initStackMaximum, 1));
        spinnerMaximum.setModel(new SpinnerNumberModel(initStackMaximum, initStackMinimum, Integer.MAX_VALUE, 1));
        spinnerFrameCount.setValue(initFrameCount);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(StackParametersModel.FIX_CONTACT.equals(property))
        {
            boolean fixContactNew = (boolean)evt.getNewValue();
            boolean fixContactOld = boxFixContactPoint.isSelected();

            if(fixContactOld != fixContactNew)
            {
                boxFixContactPoint.setSelected(fixContactNew);
            }
        }
        else if(StackParametersModel.STACK_MINIMUM.equals(property))
        {
            double stackMinimumNew = (double)evt.getNewValue();
            double stackMinimumOld = ((Number)spinnerMinimum.getValue()).doubleValue();

            if(!GeometryUtilities.almostEqual(stackMinimumOld, stackMinimumNew, TOLERANCE))
            {
                spinnerMinimum.setValue(stackMinimumNew);
                setMinimumConstraintOnSpinnerMaximum(stackMinimumNew);
            }
        }
        else if(StackParametersModel.STACK_MAXIMUM.equals(property))
        {
            double stackMaximumNew = (double)evt.getNewValue();
            double stackMaximumOld = ((Number)spinnerMaximum.getValue()).doubleValue();

            if(!GeometryUtilities.almostEqual(stackMaximumOld, stackMaximumNew, TOLERANCE))
            {
                spinnerMaximum.setValue(stackMaximumNew);
                setMaximumConstraintOnSpinnerMinimum(stackMaximumNew);
            }
        }
        else if(StackParametersModel.FRAME_COUNT.equals(property))
        {
            int frameCountNew = (int)evt.getNewValue();
            int frameCountOld = ((Number)spinnerFrameCount.getValue()).intValue();

            if(!GeometryUtilities.almostEqual(frameCountOld, frameCountNew, TOLERANCE))
            {
                spinnerFrameCount.setValue(frameCountNew);
            }     
        }
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();    

        JPanel panelOperationRange = buildPanelROIRelative();

        mainPanel.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(panelOperationRange, 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);

        SubPanel panelSettings = new SubPanel();

        panelSettings.addComponent(boxFixContactPoint, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        panelSettings.addComponent(new JLabel("Minimum"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerMinimum, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelMinimum, 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        panelSettings.addComponent(new JLabel("Maximum"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerMaximum, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelMaximum, 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        panelSettings.addComponent(new JLabel("Frame no"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerFrameCount, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        mainPanel.addComponent(panelSettings, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }
}
