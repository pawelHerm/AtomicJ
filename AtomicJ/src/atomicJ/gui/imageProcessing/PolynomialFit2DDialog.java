
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
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;


public class PolynomialFit2DDialog extends BatchProcessingDialog <PolynomialFit2DModel, String> 
{
    private static final long serialVersionUID = 1L;

    private int initDegX;
    private int initDegY;

    private final JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
    private final JSpinner spinnerY = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));

    private final JSlider sliderX = new JSlider(0, 10, 1);
    private final JSlider sliderY = new JSlider(0, 10, 1);


    public PolynomialFit2DDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

        sliderX.setMajorTickSpacing(1);
        sliderY.setMajorTickSpacing(1);

        sliderX.setMinorTickSpacing(1);
        sliderY.setMinorTickSpacing(1);

        sliderX.setSnapToTicks(true);
        sliderY.setSnapToTicks(true);

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
        spinnerX.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int degXNew = ((SpinnerNumberModel)spinnerX.getModel()).getNumber().intValue();
                sliderX.setValue(degXNew);

                getModel().setDegreeX(degXNew);
            }
        });
        spinnerY.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int degYNew = ((SpinnerNumberModel)spinnerY.getModel()).getNumber().intValue();
                sliderY.setValue(degYNew);

                getModel().setDegreeY(degYNew);                
            }
        });
        sliderX.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int degXNew = sliderX.getValue();
                spinnerX.setValue(degXNew);

                getModel().setDegreeX(degXNew);                
            }
        });
        sliderY.addChangeListener(new ChangeListener() 
        {       
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int degYNew = sliderY.getValue();
                spinnerY.setValue(degYNew);

                getModel().setDegreeY(degYNew);
            }
        });
    }	

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel(); 

        SubPanel panelActiveArea = new SubPanel();

        panelActiveArea.addComponent(new JLabel("Fit: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);
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
        panelSettings.addComponent(new JLabel("X: "), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(sliderX, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(spinnerX, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(new JLabel("Y "), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelSettings.addComponent(sliderY, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(spinnerY, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

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

        PolynomialFit2DModel model = getModel();

        this.initDegX = model.getDegreeX();
        this.initDegY = model.getDegreeY();      
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        PolynomialFit2DModel model = getModel();

        model.setDegrees(initDegX, initDegY);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerX.setValue(initDegX);
        spinnerY.setValue(initDegY);
        sliderX.setValue(initDegX);
        sliderY.setValue(initDegY);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(PolynomialFit2DModel.DEGREE_X.equals(property))
        {
            int valueNew = (int)evt.getNewValue();

            spinnerX.setValue(valueNew);
            sliderX.setValue(valueNew);
        }
        else if(PolynomialFit2DModel.DEGREE_Y.equals(property))
        {
            int valueNew = (int)evt.getNewValue();

            spinnerY.setValue(valueNew);
            sliderY.setValue(valueNew);
        }
    }
}
