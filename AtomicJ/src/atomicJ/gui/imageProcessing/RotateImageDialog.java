
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

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;
import atomicJ.gui.generalProcessing.ProcessingModel;
import atomicJ.utilities.GeometryUtilities;


public class RotateImageDialog extends BatchProcessingDialog<RotateImageModel, String> implements 
PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private PrefixedUnit displayedXUnit;
    private PrefixedUnit displayedYUnit;
    private PrefixedUnit displayedZUnit;

    private double initAngle;
    private UnitExpression initCenterX;
    private UnitExpression initCenterY;
    private UnitExpression initFillValue;
    private InterpolationMethod2D initInterpolation;

    private final JSpinner spinnerAngle = new JSpinner(new SpinnerNumberModel(0., -360., 360., 1.));
    private final JSpinner spinnerCenterX = new JSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerCenterY = new JSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));
    private final JSpinner spinnerFillValue = new JSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.));

    private final JLabel labelCenterXUnit = new JLabel();
    private final JLabel labelCenterYUnit = new JLabel();
    private final JLabel labelFillValueUnit = new JLabel();

    private final JComboBox<InterpolationMethod2D> comboInterpolationMethod = new JComboBox<>(InterpolationMethod2D.values()); 

    public RotateImageDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary, ModalityType.MODELESS);

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
        spinnerAngle.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double angle = ((SpinnerNumberModel)spinnerAngle.getModel()).getNumber().doubleValue();

                RotateImageModel model = getModel();
                model.setRotationAngle(angle);
            }
        });

        spinnerCenterX.addChangeListener(new ChangeListener() 
        {     
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double centerX = ((SpinnerNumberModel)spinnerCenterX.getModel()).getNumber().doubleValue();

                RotateImageModel model = getModel();
                model.setCenterX(new UnitExpression(centerX, displayedXUnit));
            }
        });

        spinnerCenterY.addChangeListener(new ChangeListener() 
        {     
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double centerY = ((SpinnerNumberModel)spinnerCenterY.getModel()).getNumber().doubleValue();

                RotateImageModel model = getModel();
                model.setCenterY(new UnitExpression(centerY, displayedYUnit));
            }
        });

        spinnerFillValue.addChangeListener(new ChangeListener() 
        {     
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double fillValue = ((SpinnerNumberModel)spinnerFillValue.getModel()).getNumber().doubleValue();

                RotateImageModel model = getModel();
                model.setFillValue(new UnitExpression(fillValue, displayedZUnit));
            }
        });
    }

    private void initItemListener()
    {
        comboInterpolationMethod.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                InterpolationMethod2D interpolation = (InterpolationMethod2D)comboInterpolationMethod.getSelectedItem();

                RotateImageModel model = getModel();
                model.setInterpolationMethod(interpolation);
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

        panelSettings.addComponent(new JLabel("Angle"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerAngle, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);             

        panelSettings.addComponent(new JLabel("Center X"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerCenterX, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(labelCenterXUnit, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

        panelSettings.addComponent(new JLabel("Center Y"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerCenterY, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelCenterYUnit, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        panelSettings.addComponent(new JLabel("Fill"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerFillValue, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelFillValueUnit, 2, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        panelSettings.addComponent(new JLabel("Interpolation"), 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(comboInterpolationMethod, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        mainPanel.addComponent(panelActiveArea, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(buildPanelPreview(), 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        RotateImageModel model = getModel();

        this.initAngle = model.getRotationAngle();
        this.initCenterX = model.getCenterX();
        this.initCenterY = model.getCenterY();
        this.initFillValue = model.getFillValue();
        this.initInterpolation = model.getInterpolationMethod();

        this.displayedXUnit = model.getDomainXAxisDisplayedUnit();
        this.displayedYUnit = model.getDomainYAxisDisplayedUnit();
        this.displayedZUnit = model.getValueAxisDisplayedUnit();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        RotateImageModel model = getModel();

        model.setRotationAngle(initAngle);
        model.setCenterX(initCenterX);
        model.setCenterY(initCenterY);
        model.setFillValue(initFillValue);
        model.setInterpolationMethod(initInterpolation);
    }

    private boolean refresh(JSpinner spinner, PrefixedUnit displayedUnit, UnitExpression exprNew)
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

    private boolean refresh(JSpinner spinner, double valNew)
    {
        boolean refreshed = false;

        double valOld = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();

        if(!GeometryUtilities.almostEqual(valOld, valNew, TOLERANCE))
        {
            spinner.setValue(valNew);
            refreshed = true;
        }

        return refreshed;
    }

    public void refresh(JLabel label, PrefixedUnit unit)
    {
        String unitString = unit != null ? unit.getFullName() : "";
        label.setText(unitString);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        refresh(spinnerCenterX, displayedXUnit, initCenterX);
        refresh(spinnerCenterY, displayedYUnit, initCenterY);
        refresh(spinnerFillValue, displayedZUnit, initFillValue);
        refresh(spinnerAngle, initAngle);

        comboInterpolationMethod.setSelectedItem(initInterpolation);

        refresh(labelCenterXUnit, displayedXUnit);
        refresh(labelCenterYUnit, displayedYUnit);
        refresh(labelFillValueUnit, displayedZUnit);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(RotateImageModel.ROTATION_ANGLE.equals(property))
        {
            double newVal = (double)evt.getNewValue();
            double oldVal = ((SpinnerNumberModel)spinnerAngle.getModel()).getNumber().doubleValue();

            if(!GeometryUtilities.almostEqual(oldVal, newVal, TOLERANCE))
            {
                spinnerAngle.setValue(newVal);
            }
        }
        else if(RotateImageModel.CENTER_X.equals(property))
        {
            UnitExpression newVal = (UnitExpression)evt.getNewValue();
            refresh(spinnerCenterX, displayedXUnit, newVal);
        }
        else if(RotateImageModel.CENTER_Y.equals(property))
        {
            UnitExpression newVal = (UnitExpression)evt.getNewValue();
            refresh(spinnerCenterY, displayedYUnit, newVal);
        }
        else if(RotateImageModel.FILL_VALUE.equals(property))
        {
            UnitExpression newVal = (UnitExpression)evt.getNewValue();
            refresh(spinnerFillValue, displayedZUnit, newVal);
        }
        else if(RotateImageModel.INTERPOLATION_METHOD.equals(property))
        {
            InterpolationMethod2D newVal = (InterpolationMethod2D)evt.getNewValue();
            InterpolationMethod2D oldVal = (InterpolationMethod2D)comboInterpolationMethod.getSelectedItem();

            if(!Objects.equals(oldVal, newVal))
            {
                comboInterpolationMethod.setSelectedItem(newVal);
            }
        }
        else if(ProcessingModel.VALUE_AXIS_DISPLAYED_UNIT.equals(property))
        {
            this.displayedZUnit = (PrefixedUnit)evt.getNewValue();
            refresh(labelFillValueUnit, displayedZUnit);
            refresh(spinnerFillValue, displayedZUnit, getModel().getFillValue());
        }
        else if(ImageBatchProcessingModel.DOMAIN_X_AXIS_DISPLAYED_UNIT.equals(property))
        {
            this.displayedXUnit = (PrefixedUnit)evt.getNewValue();
            refresh(labelCenterXUnit, displayedXUnit);
            refresh(spinnerCenterX, displayedXUnit, getModel().getFillValue());
        }
        else if(ImageBatchProcessingModel.DOMAIN_Y_AXIS_DISPLAYED_UNIT.equals(property))
        {
            this.displayedYUnit = (PrefixedUnit)evt.getNewValue();
            refresh(labelCenterYUnit, displayedYUnit);
            refresh(spinnerCenterY, displayedYUnit, getModel().getFillValue());
        }
    }
}
