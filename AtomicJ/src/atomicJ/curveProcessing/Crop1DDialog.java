
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;

public class Crop1DDialog extends BatchProcessingDialog<Crop1DModel<?>, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private double initCropLeft = 0;
    private double initCropRight = 0;
    private double initCropBottom = 0;
    private double initCropTop = 0;

    private String domainUnitString = "";
    private String rangeUnitString = "";

    private final JSpinner spinnerCropLeft = new JSpinner(new SpinnerNumberModel(initCropLeft, 0., 10000., 1.));
    private final JSpinner spinnerCropRight = new JSpinner(new SpinnerNumberModel(initCropRight, 0., 10000., 1.));
    private final JSpinner spinnerCropBottom = new JSpinner(new SpinnerNumberModel(initCropBottom, 0., 10000., 1.));
    private final JSpinner spinnerCropTop = new JSpinner(new SpinnerNumberModel(initCropTop, 0., 10000., 1.));

    private final JLabel labelUnitCropLeft = new JLabel();
    private final JLabel labelUnitCropRight = new JLabel();
    private final JLabel labelUnitCropBottom = new JLabel();
    private final JLabel labelUnitCropTop = new JLabel();

    public Crop1DDialog(Window parent, String title, boolean temporary)
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
        spinnerCropLeft.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                Crop1DModel<?> model = getModel();

                double cropLeft = ((SpinnerNumberModel)spinnerCropLeft.getModel()).getNumber().doubleValue();
                model.setCropLeft(cropLeft);
            }
        });

        spinnerCropRight.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                Crop1DModel<?> model = getModel();

                double cropRight = ((SpinnerNumberModel)spinnerCropRight.getModel()).getNumber().doubleValue();
                model.setCropRight(cropRight);
            }
        });

        spinnerCropBottom.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                Crop1DModel<?> model = getModel();

                double cropBottom = ((SpinnerNumberModel)spinnerCropBottom.getModel()).getNumber().doubleValue();
                model.setCropBottom(cropBottom);
            }
        });

        spinnerCropTop.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                Crop1DModel<?> model = getModel();

                double cropTop = ((SpinnerNumberModel)spinnerCropTop.getModel()).getNumber().doubleValue();
                model.setCropTop(cropTop);
            }
        });
    }	


    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();         

        SubPanel panelActiveArea = new SubPanel();

        panelActiveArea.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelActiveArea.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelBatchType = new SubPanel();
        panelBatchType.addComponent(new JLabel("Curves:   "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


        SubPanel panelIdentifierType = new SubPanel();

        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


        SubPanel panelSettings = new SubPanel();        

        JLabel labelRange = new JLabel("Crop range");
        JLabel labelDomain = new JLabel("Crop domain");

        labelRange.setHorizontalAlignment(SwingConstants.LEFT);
        labelDomain.setHorizontalAlignment(SwingConstants.LEFT);

        Font font = UIManager.getFont("TextField.font");

        labelRange.setFont(font.deriveFont(Font.BOLD));    
        labelDomain.setFont(font.deriveFont(Font.BOLD));

        panelSettings.addComponent(labelRange, 0, 0, 2, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(10, 0, 10, 10));

        panelSettings.addComponent(new JLabel("Bottom"), 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(spinnerCropBottom, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelUnitCropBottom, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(new JLabel("Top"), 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(spinnerCropTop, 4, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelUnitCropTop, 5, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        panelSettings.addComponent(labelDomain, 0, 2, 2, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(10, 0, 10, 10));

        panelSettings.addComponent(new JLabel("Left"), 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(spinnerCropLeft, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelUnitCropLeft, 2, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(new JLabel("Right"), 3, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(spinnerCropRight, 4, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelUnitCropRight, 5, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(buildPanelPreview(), 6, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        Crop1DModel<?> model = getModel();

        this.initCropLeft = model.getCropLeft();
        this.initCropRight =  model.getCropRight();
        this.initCropBottom = model.getCropBottom();
        this.initCropTop = model.getCropTop();

        PrefixedUnit domainUnit = model.getDomainCroppingUnit();
        PrefixedUnit rangeUnit = model.getRangeCroppingUnit();

        this.domainUnitString = (domainUnit != null) ? domainUnit.getFullName() : "";
        this.rangeUnitString = (rangeUnit != null) ? rangeUnit.getFullName() : "";
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        Crop1DModel<?> model = getModel();

        model.setCropLeft(initCropLeft);
        model.setCropRight(initCropRight);
        model.setCropBottom(initCropBottom);
        model.setCropTop(initCropTop);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerCropLeft.setValue(initCropLeft);
        spinnerCropRight.setValue(initCropRight);
        spinnerCropBottom.setValue(initCropBottom);
        spinnerCropTop.setValue(initCropTop);

        labelUnitCropLeft.setText(domainUnitString);
        labelUnitCropRight.setText(domainUnitString);

        labelUnitCropBottom.setText(rangeUnitString);
        labelUnitCropTop.setText(rangeUnitString);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(Crop1DModel.CROP_LEFT.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((Number)spinnerCropLeft.getValue()).doubleValue(); 

            if(!valueOld.equals(valueNew))
            {
                spinnerCropLeft.setValue(valueNew);
            }
        }
        else if(Crop1DModel.CROP_RIGHT.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((Number)spinnerCropRight.getValue()).doubleValue(); 

            if(!valueOld.equals(valueNew))
            {
                spinnerCropRight.setValue(valueNew);
            }
        }
        else if(Crop1DModel.CROP_BOTTOM.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((Number)spinnerCropBottom.getValue()).doubleValue(); 

            if(!valueOld.equals(valueNew))
            {
                spinnerCropBottom.setValue(valueNew);
            }
        }
        else if(Crop1DModel.CROP_TOP.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((Number)spinnerCropTop.getValue()).doubleValue(); 

            if(!valueOld.equals(valueNew))
            {
                spinnerCropTop.setValue(valueNew);
            }
        }
    }
}
