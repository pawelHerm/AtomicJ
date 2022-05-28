
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
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;
import atomicJ.gui.generalProcessing.ProcessingModel;
import atomicJ.utilities.GeometryUtilities;



public class PlaneBasedCorrrectionDialog extends BatchProcessingDialog<PlaneBasedCorrectionModel, String> 
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private double initXCoeff;
    private double initYCoeff;
    private double initZCoeff;
    private UnitExpression initIntercept;
    private PrefixedUnit displayedUnit;

    private final JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(0., -Short.MAX_VALUE, Short.MAX_VALUE, 0.05));
    private final JSpinner spinnerY = new JSpinner(new SpinnerNumberModel(0., -Short.MAX_VALUE, Short.MAX_VALUE, 0.05));
    private final JSpinner spinnerZ = new JSpinner(new SpinnerNumberModel(0., -Short.MAX_VALUE, Short.MAX_VALUE, 0.05));
    private final JSpinner spinnerIntercept = new JSpinner(new SpinnerNumberModel(0., -Short.MAX_VALUE, Short.MAX_VALUE, 0.05));
    private final JLabel labelInterceptUnit = new JLabel();

    public PlaneBasedCorrrectionDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title, temporary);      

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();

        pack();
        setLocationRelativeTo(parent);
    }


    private void initChangeListener()
    {
        spinnerX.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                PlaneBasedCorrectionModel model = getModel();
                double xCoeffNew = ((SpinnerNumberModel)spinnerX.getModel()).getNumber().doubleValue();
                model.setXCoefficient(xCoeffNew);
            }
        });
        spinnerY.addChangeListener(new ChangeListener() 
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                PlaneBasedCorrectionModel model = getModel();
                double yCoeffNew = ((SpinnerNumberModel)spinnerY.getModel()).getNumber().doubleValue();
                model.setYCoefficient(yCoeffNew);
            }
        });
        spinnerZ.addChangeListener(new ChangeListener() 
        {      
            @Override
            public void stateChanged(ChangeEvent e)
            {
                PlaneBasedCorrectionModel model = getModel();
                double zCoeffNew = ((SpinnerNumberModel)spinnerZ.getModel()).getNumber().doubleValue();
                model.setZCoefficient(zCoeffNew);                
            }
        });
        spinnerIntercept.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e)
            {
                double interceptNew = ((SpinnerNumberModel)spinnerIntercept.getModel()).getNumber().doubleValue();

                PlaneBasedCorrectionModel model = getModel();
                model.setIntercept(new UnitExpression(interceptNew, displayedUnit));
            }
        });
    }	

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(PlaneBasedCorrectionModel.X_COEFFICIENT.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((SpinnerNumberModel)spinnerX.getModel()).getNumber().doubleValue();
            if(!valueNew.equals(valueOld))
            {
                spinnerX.setValue(valueNew);
            }
        }
        else if(PlaneBasedCorrectionModel.Y_COEFFICIENT.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((SpinnerNumberModel)spinnerY.getModel()).getNumber().doubleValue();
            if(!valueNew.equals(valueOld))
            {
                spinnerY.setValue(valueNew);
            }
        }
        else if(PlaneBasedCorrectionModel.Z_COEFFICIENT.equals(property))
        {
            double valueNew = (Double)evt.getNewValue();
            double valueOld = ((SpinnerNumberModel)spinnerZ.getModel()).getNumber().doubleValue();

            if(!GeometryUtilities.almostEqual(valueNew, valueOld, TOLERANCE))
            {
                spinnerZ.setValue(valueNew);
            }
        }
        else if(PlaneBasedCorrectionModel.INTERCEPT.equals(property))
        {
            UnitExpression interceptNew = (UnitExpression)evt.getNewValue();
            refreshIntercept(interceptNew);
        }
        else if(ProcessingModel.VALUE_AXIS_DISPLAYED_UNIT.equals(property))
        {
            PrefixedUnit unitNew = (PrefixedUnit)evt.getNewValue();
            String unitLabel = unitNew != null ? unitNew.getFullName() : "";
            labelInterceptUnit.setText(unitLabel);

            this.displayedUnit = unitNew;
            refreshIntercept(getModel().getIntercept());
        }
    }

    private void refreshIntercept(UnitExpression exprNew)
    {
        double valOld = ((SpinnerNumberModel)spinnerIntercept.getModel()).getNumber().doubleValue();
        UnitExpression exprOld = new UnitExpression(valOld, displayedUnit);

        if(!GeometryUtilities.almostEqual(exprNew, exprOld, TOLERANCE))
        {
            double valNew = exprNew.derive(displayedUnit).getValue();
            spinnerIntercept.setValue(valNew);
        }
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

        panelSettings.addComponent(new JLabel("Function: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(new JLabel(" \u00D7 X"), 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(new JLabel(" + "), 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerY, 4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(new JLabel(" \u00D7 Y"), 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(new JLabel(" + "), 6, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerZ, 7, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(new JLabel(" \u00D7 Z"), 8, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(new JLabel(" + "), 9, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerIntercept, 10, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(labelInterceptUnit, 11, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      

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

        PlaneBasedCorrectionModel model = getModel();

        this.initXCoeff = model.getXCoefficient();
        this.initYCoeff = model.getYCoefficient();
        this.initZCoeff = model.getZCoefficient();
        this.initIntercept = model.getIntercept();
        this.displayedUnit = model.getValueAxisDisplayedUnit();
    }

    @Override
    public void setModelToInitialState()
    {
        super.setModelToInitialState();

        PlaneBasedCorrectionModel model = getModel();

        model.setXCoefficient(initXCoeff);
        model.setYCoefficient(initYCoeff);
        model.setZCoefficient(initZCoeff);
        model.setIntercept(initIntercept);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerX.setValue(initXCoeff);
        spinnerY.setValue(initYCoeff);
        spinnerZ.setValue(initZCoeff);

        double valNew = initIntercept.derive(displayedUnit).getValue();
        spinnerIntercept.setValue(valNew);

        PrefixedUnit unitNew = getModel().getValueAxisDisplayedUnit();
        String unitLabel = unitNew != null ? unitNew.getFullName() : "";
        labelInterceptUnit.setText(unitLabel);

        pack();
    }
}
