
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

public class MedianFilter2DDialog extends  BatchProcessingDialog <MedianFilter2DModel, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private int initRadiusX = 1;
    private int initRadiusY = 1;


    private final JSpinner spinnerRadiusX = new JSpinner(new SpinnerNumberModel(initRadiusX, 0, 100, 1));
    private final JSpinner spinnerRadiusY = new JSpinner(new SpinnerNumberModel(initRadiusY, 0, 100, 1));


    public MedianFilter2DDialog(Window parent, String title, boolean temporary)
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
        spinnerRadiusX.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int radiusX = ((SpinnerNumberModel)spinnerRadiusX.getModel()).getNumber().intValue();
                getModel().setKernelXRadius(radiusX);                
            }
        });
        spinnerRadiusY.addChangeListener(new ChangeListener() 
        {            
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int radiusY = ((SpinnerNumberModel)spinnerRadiusY.getModel()).getNumber().intValue();
                getModel().setKernelYRadius(radiusY);
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

        panelSettings.addComponent(new JLabel("X radius"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerRadiusX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(new JLabel("Y radius"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(spinnerRadiusY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(Box.createHorizontalStrut(10), 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(buildPanelPreview(), 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        MedianFilter2DModel model = getModel();

        this.initRadiusX = model.getKernelXRadius();
        this.initRadiusY = model.getKernelYRadius();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        MedianFilter2DModel model = getModel();

        model.setKernelXRadius(initRadiusX);
        model.setKernelYRadius(initRadiusY);   
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerRadiusX.setValue(initRadiusX);
        spinnerRadiusY.setValue(initRadiusY);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(Convolution2DModel.KERNEL_X_RADIUS.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRadiusX.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRadiusX.setValue(valueNew);
            }
        }
        else if(Convolution2DModel.KERNEL_Y_RADIUS.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRadiusY.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRadiusY.setValue(valueNew);
            }
        }
    }
}
