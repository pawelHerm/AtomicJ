
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
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;

public class UnsharpMaskDialog extends BatchProcessingDialog<UnsharpMaskModel, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private double initSigmaX = 1;
    private double initSigmaY = 1;
    private double initAmount = 0.05;

    private final JSpinner spinnerSigmaX = new JSpinner(new SpinnerNumberModel(initSigmaX, 0, 20, .5));
    private final JSpinner spinnerSigmaY = new JSpinner(new SpinnerNumberModel(initSigmaY, 0, 20, .5));
    private final JSpinner spinnerAmount = new JSpinner(new SpinnerNumberModel(initAmount, 0, 100, 0.01));

    public UnsharpMaskDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

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
        spinnerSigmaX.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double radiusX = ((SpinnerNumberModel)spinnerSigmaX.getModel()).getNumber().doubleValue();
                getModel().setSigmaX(radiusX);
            }
        });
        spinnerSigmaY.addChangeListener(new ChangeListener()
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double radiusY = ((SpinnerNumberModel)spinnerSigmaY.getModel()).getNumber().doubleValue();
                getModel().setSigmaY(radiusY);
            }
        });
        spinnerAmount.addChangeListener(new ChangeListener()
        {        
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double amount = ((SpinnerNumberModel)spinnerAmount.getModel()).getNumber().doubleValue();
                getModel().setSharpeningAmount(amount);                
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
        panelBatchType.addComponent(new JLabel("Sources:   "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelIdentifierType = new SubPanel();
        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelSettings = new SubPanel();        
        panelSettings.addComponent(new JLabel("X sigma"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerSigmaX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(new JLabel("Y sigma"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerSigmaY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(new JLabel("Amount"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerAmount, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(Box.createHorizontalStrut(10), 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(buildPanelPreview(), 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        UnsharpMaskModel model = getModel();

        this.initSigmaX = model.getSigmaX();
        this.initSigmaY = model.getSigmaY();
        this.initAmount = model.getSharpeningAmount();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        UnsharpMaskModel model = getModel();

        model.setSigmaX(initSigmaX);
        model.setSigmaY(initSigmaY);   
        model.setSharpeningAmount(initAmount); 
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerSigmaX.setValue(initSigmaX);
        spinnerSigmaY.setValue(initSigmaY);
        spinnerAmount.setValue(initAmount);        

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(GaussianBasedFilter2DModel.SIGMA_X.equals(property))
        {
            double valueNew = (double)evt.getNewValue();
            double valueOld = ((Number)spinnerSigmaX.getValue()).doubleValue(); 

            if(valueNew != valueOld)
            {
                spinnerSigmaX.setValue(valueNew);
            }
        }
        else if(GaussianBasedFilter2DModel.SIGMA_Y.equals(property))
        {
            double valueNew = (double)evt.getNewValue();
            double valueOld = ((Number)spinnerSigmaY.getValue()).doubleValue(); 

            if(valueNew != valueOld)
            {
                spinnerSigmaY.setValue(valueNew);
            }
        }
        else if(UnsharpMaskModel.SHARPENING_AMOUNT.equals(property))
        {
            double valueNew = (double)evt.getNewValue();
            double valueOld = ((Number)spinnerAmount.getValue()).doubleValue(); 

            if(valueNew != valueOld)
            {
                spinnerAmount.setValue(valueNew);
            }
        }
    }
}
