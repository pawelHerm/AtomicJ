
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
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;

public class SavitzkyGolayFilter1DDialog extends BatchProcessingDialog<SavitzkyGolayFilter1DModel<?>, String> implements 
PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private int initLeftHalfWidth = 2;
    private int initRightHalfWidth = 2;
    private int initPolynomialDegree = 2;
    private int initDerivative = 0;
    private boolean initEnforceEqualHalfWidths = true;

    private final Action enforceEqualHalfWidthsAction = new EnforceEqualHalfWidthsActionAction();

    private final JSpinner spinnerLeftHalfWidth = new JSpinner(new SpinnerNumberModel(initLeftHalfWidth, 0, 10000, 1));
    private final JSpinner spinnerRightHalfWidth = new JSpinner(new SpinnerNumberModel(initRightHalfWidth, 0, 10000, 1));
    private final JSpinner spinnerPolynomialDegree = new JSpinner(new SpinnerNumberModel(initPolynomialDegree, 0, 10000, 1));
    private final JSpinner spinnerDerivative = new JSpinner(new SpinnerNumberModel(initDerivative, 0, 10000, 1));

    public SavitzkyGolayFilter1DDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListeners();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initChangeListeners()
    {
        spinnerLeftHalfWidth.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int leftHalfWidthNew = ((SpinnerNumberModel)spinnerLeftHalfWidth.getModel()).getNumber().intValue();
                getModel().specifyLeftHalfWidth(leftHalfWidthNew);                
            }
        });

        spinnerRightHalfWidth.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int rightHalfWidthNew = ((SpinnerNumberModel)spinnerRightHalfWidth.getModel()).getNumber().intValue();
                getModel().specifyRightHalfWidth(rightHalfWidthNew);                 
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
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        panelActiveArea.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelBatchType = new SubPanel();
        panelBatchType.addComponent(new JLabel("Curves:   "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelIdentifierType = new SubPanel();
        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelSettings = new SubPanel();        

        JToggleButton buttonEqualHalfWidths = new JToggleButton(enforceEqualHalfWidthsAction);

        buttonEqualHalfWidths.setHideActionText(true);
        buttonEqualHalfWidths.setMargin(new Insets(3, 5, 3, 5));

        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Resources/chainLinksSmall.png"));
        buttonEqualHalfWidths.setSelectedIcon(icon);

        panelSettings.addComponent(new JLabel("Left half width"), 0, 1, 1, 1, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerLeftHalfWidth, 1, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSettings.addComponent(buttonEqualHalfWidths, 3, 1, 1, 2, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, 1, 1);
        panelSettings.addComponent(new JLabel("Degree"), 4, 1, 1, 1, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerPolynomialDegree, 5, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSettings.addComponent(new JLabel("Right half width"), 0, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerRightHalfWidth, 1, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSettings.addComponent(new JLabel("Derivative"), 4, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(spinnerDerivative, 5, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
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

        SavitzkyGolayFilter1DModel model = getModel();

        this.initLeftHalfWidth = model.getLeftHalfWidth();
        this.initRightHalfWidth = model.getRightHalfWidth();
        this.initPolynomialDegree = model.getPolynomialDegree();
        this.initDerivative = model.getDerivative();
        this.initEnforceEqualHalfWidths = model.getEnforceEqualHalfWidths();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        SavitzkyGolayFilter1DModel model = getModel();

        model.specifyEnforceEqualHalfWidths(initEnforceEqualHalfWidths);

        model.specifyLeftHalfWidth(initLeftHalfWidth);
        model.specifyRightHalfWidth(initRightHalfWidth);
        model.specifyPolynomialDegree(initPolynomialDegree);
        model.specifyDerivative(initDerivative);    
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();       

        spinnerLeftHalfWidth.setValue(initLeftHalfWidth);
        spinnerRightHalfWidth.setValue(initRightHalfWidth);
        spinnerPolynomialDegree.setValue(initPolynomialDegree);
        spinnerDerivative.setValue(initDerivative);

        enforceEqualHalfWidthsAction.putValue(Action.SELECTED_KEY, initEnforceEqualHalfWidths);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(SavitzkyGolayFilter1DModel.LEFT_HALF_WIDTH.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerLeftHalfWidth.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerLeftHalfWidth.setValue(valueNew);
            }
        }
        else if(SavitzkyGolayFilter1DModel.RIGHT_HALF_WIDTH.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRightHalfWidth.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRightHalfWidth.setValue(valueNew);
            }
        }  
        else if(SavitzkyGolayFilter1DModel.POLYNOMIAL_DEGREE.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerPolynomialDegree.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerPolynomialDegree.setValue(valueNew);
            }
        }  
        else if(SavitzkyGolayFilter1DModel.DERIVATIVE.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerDerivative.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerDerivative.setValue(valueNew);
            }
        }  
        else if(SavitzkyGolayFilter1DModel.ENFORCE_EQUAL_HALF_WIDTHS.equals(property))
        {
            boolean enforceEqualHalfWidthsNew = (boolean)evt.getNewValue();          
            enforceEqualHalfWidthsAction.putValue(Action.SELECTED_KEY, enforceEqualHalfWidthsNew);
        }
    }

    private class EnforceEqualHalfWidthsActionAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public EnforceEqualHalfWidthsActionAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon iconDeselected = new ImageIcon(
                    toolkit.getImage("Resources/chainLinksBrokenSmall.png"));

            putValue(LARGE_ICON_KEY, iconDeselected);

            putValue(NAME, "Enforce equal half widths");
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean halfWidthsEqual = (boolean) getValue(SELECTED_KEY);

            SavitzkyGolayFilter1DModel model = getModel();
            model.specifyEnforceEqualHalfWidths(halfWidthsEqual);
        }
    }
}
