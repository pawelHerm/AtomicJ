
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

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;

public class Translate1DDialog extends BatchProcessingDialog<Translate1DModel<?>, String> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private double initXTranslation = 0;
    private double initYTranslation = 0;

    private String domainUnitString = "";
    private String rangeUnitString = "";

    private final JSpinner spinnerXTranslation = new JSpinner(new SpinnerNumberModel(initXTranslation, -Short.MAX_VALUE, Short.MAX_VALUE, 1.));
    private final JSpinner spinnerYTranslation = new JSpinner(new SpinnerNumberModel(initYTranslation, -Short.MAX_VALUE, Short.MAX_VALUE, 1.));

    private final JLabel labelUnitDomain = new JLabel();
    private final JLabel labelUnitRange = new JLabel();

    public Translate1DDialog(Window parent, String title, boolean temporary)
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
        spinnerXTranslation.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double translationX = ((SpinnerNumberModel)spinnerXTranslation.getModel()).getNumber().doubleValue();
                getModel().setTranslationX(translationX);
            }
        });

        spinnerYTranslation.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                double translationY = ((SpinnerNumberModel)spinnerYTranslation.getModel()).getNumber().doubleValue();
                getModel().setTranslationY(translationY);
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
        panelSettings.addComponent(new JLabel("X"), 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(spinnerXTranslation, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelUnitDomain, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(new JLabel("Y"), 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelSettings.addComponent(spinnerYTranslation, 4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(labelUnitRange, 5, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      
        panelSettings.addComponent(buildPanelPreview(), 6, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelSettings, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.05, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        Translate1DModel<?> model = getModel();

        this.initXTranslation = model.getTranslationX();
        this.initYTranslation =  model.getTranslationY();

        PrefixedUnit domainUnit = model.getDomainXAxisDisplayedUnit();
        PrefixedUnit rangeUnit = model.getValueAxisDisplayedUnit();

        this.domainUnitString = (domainUnit != null) ? domainUnit.getFullName() : "";
        this.rangeUnitString = (rangeUnit != null) ? rangeUnit.getFullName() : "";
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        Translate1DModel<?> model = getModel();

        model.setTranslationX(initXTranslation);
        model.setTranslationY(initYTranslation);
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerXTranslation.setValue(initXTranslation);
        spinnerYTranslation.setValue(initYTranslation);

        labelUnitDomain.setText(domainUnitString);
        labelUnitRange.setText(rangeUnitString);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(Translate1DModel.TRANSLATION_X.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((Number)spinnerXTranslation.getValue()).doubleValue(); 

            if(!valueOld.equals(valueNew))
            {
                spinnerXTranslation.setValue(valueNew);
            }
        }
        else if(Translate1DModel.TRANSLATION_Y.equals(property))
        {
            Double valueNew = (Double)evt.getNewValue();
            Double valueOld = ((Number)spinnerYTranslation.getValue()).doubleValue(); 

            if(!valueOld.equals(valueNew))
            {
                spinnerYTranslation.setValue(valueNew);
            }
        }       
    }
}
