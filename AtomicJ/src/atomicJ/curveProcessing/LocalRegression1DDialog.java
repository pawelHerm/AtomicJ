
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

package atomicJ.curveProcessing;

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
import atomicJ.utilities.MathUtilities;

public class LocalRegression1DDialog extends BatchProcessingDialog<LocalRegression1DModel<?>, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-12;

    private double initSpan = 2;
    private int initRobustnessIterations = 2;
    private int initPolynomialDegree = 2;
    private int initDerivative = 0;
    private SpanType initSpanType = SpanType.POINT_FRACTION;

    private final JSpinner spinnerSpan = new JSpinner(new SpinnerNumberModel(initSpan, 0, 10000, 1));
    private final JSpinner spinnerRobustnessIterations = new JSpinner(new SpinnerNumberModel(initRobustnessIterations, 0, 10000, 1));
    private final JSpinner spinnerPolynomialDegree = new JSpinner(new SpinnerNumberModel(initPolynomialDegree, 0, 10000, 1));
    private final JSpinner spinnerDerivative = new JSpinner(new SpinnerNumberModel(initDerivative, 0, 10000, 1));
    private final JComboBox<SpanType> comboSpanType = new JComboBox<>(SpanType.values());

    public LocalRegression1DDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListeners();
        initItemListeners();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initItemListeners()
    {
        comboSpanType.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                SpanType spanTypeNew = (SpanType) comboSpanType.getSelectedItem();
                getModel().specifySpanType(spanTypeNew);
            }
        });
    }

    private void initChangeListeners()
    {
        spinnerSpan.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double spanNew = ((SpinnerNumberModel)spinnerSpan.getModel()).getNumber().doubleValue();
                getModel().specifySpan(spanNew);                
            }
        });

        spinnerRobustnessIterations.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int robustnessIerationsNew = ((SpinnerNumberModel)spinnerRobustnessIterations.getModel()).getNumber().intValue();
                getModel().specifyRobustnessIterationCount(robustnessIerationsNew);                 
            }
        });

        spinnerPolynomialDegree.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int polynomialDegreeNew = ((SpinnerNumberModel)spinnerPolynomialDegree.getModel()).getNumber().intValue();
                getModel().specifyPolynomialDegree(polynomialDegreeNew);
            }
        });

        spinnerDerivative.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int derivativeNew = ((SpinnerNumberModel)spinnerDerivative.getModel()).getNumber().intValue();
                getModel().specifyDerivative(derivativeNew);
            }
        });
    }	

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();         

        SubPanel panelActiveArea = new SubPanel();

        panelActiveArea.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0.05, 1);
        panelActiveArea.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


        SubPanel panelBatchType = new SubPanel();

        panelBatchType.addComponent(new JLabel("Curves:   "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0.05, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


        SubPanel panelIdentifierType = new SubPanel();

        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0.05, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


        SubPanel panelSettings = new SubPanel();        

        panelSettings.addComponent(new JLabel("Span"), 0, 1, 1, 1, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerSpan, 1, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(comboSpanType, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, 1, 1);

        panelSettings.addComponent(new JLabel("Degree"), 4, 1, 1, 1, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerPolynomialDegree, 5, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSettings.addComponent(new JLabel("Robust iterations"), 0, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerRobustnessIterations, 1, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSettings.addComponent(new JLabel("Derivative"), 4, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerDerivative, 5, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);


        mainPanel.addComponent(panelActiveArea, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(buildPanelPreview(), 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(Box.createHorizontalGlue(), 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        LocalRegression1DModel<?> model = getModel();

        this.initSpan = model.getSpan();
        this.initRobustnessIterations = model.getRobustnessIterationCount();
        this.initPolynomialDegree = model.getPolynomialDegree();
        this.initDerivative = model.getDerivative();
        this.initSpanType = model.getSpanType();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        LocalRegression1DModel<?> model = getModel();

        model.specifySpanType(initSpanType);
        model.specifySpan(initSpan);
        model.specifyRobustnessIterationCount(initRobustnessIterations);
        model.specifyPolynomialDegree(initPolynomialDegree);
        model.specifyDerivative(initDerivative);    
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();       

        spinnerSpan.setValue(initSpan);
        spinnerRobustnessIterations.setValue(initRobustnessIterations);
        spinnerPolynomialDegree.setValue(initPolynomialDegree);
        spinnerDerivative.setValue(initDerivative);
        comboSpanType.setSelectedItem(initSpanType);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(LocalRegression1DModel.SPAN.equals(property))
        {
            double valueNew = (double)evt.getNewValue();
            double valueOld = ((Number)spinnerSpan.getValue()).doubleValue(); 

            if(!MathUtilities.equalWithinTolerance(valueOld, valueNew, TOLERANCE))
            {
                spinnerSpan.setValue(valueNew);
            }
        }
        else if(LocalRegression1DModel.SPAN_TYPE.equals(property))
        {
            SpanType valueNew = (SpanType)evt.getNewValue();
            SpanType valueOld = (SpanType)comboSpanType.getSelectedItem();

            if(!Objects.equals(valueOld, valueNew))
            {
                comboSpanType.setSelectedItem(valueNew);
            }
        }
        else if(LocalRegression1DModel.ROBUSTNESS_ITERATIONS_COUNT.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRobustnessIterations.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRobustnessIterations.setValue(valueNew);
            }
        }  
        else if(LocalRegression1DModel.POLYNOMIAL_DEGREE.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerPolynomialDegree.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerPolynomialDegree.setValue(valueNew);
            }
        }  
        else if(LocalRegression1DModel.DERIVATIVE.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerDerivative.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerDerivative.setValue(valueNew);
            }
        }  
    }    
}
