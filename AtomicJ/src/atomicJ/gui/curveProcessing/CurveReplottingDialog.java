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

package atomicJ.gui.curveProcessing;


import static atomicJ.gui.curveProcessing.ProcessingBatchModel.*;

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
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.OperationDialog;


public class CurveReplottingDialog extends OperationDialog <CurveReplottingModel> 
{
    private static final long serialVersionUID = 1L;

    private boolean initPlotRecordedCurve;
    private boolean initPlotRecordedCurveFit;

    private boolean initPlotIndentation;
    private boolean initPlotIndentationFit;

    private boolean initPlotModulus;
    private boolean initPlotModulusFit;

    private final JCheckBox boxPlotRecordedCurve = new JCheckBox("Show");
    private final JCheckBox boxPlotIndentation = new JCheckBox("Show");
    private final JCheckBox boxPlotModulus = new JCheckBox("Show"); 

    private final JCheckBox boxPlotRecordedCurveFit = new JCheckBox("Plot fit");
    private final JCheckBox boxPlotIndentationFit = new JCheckBox("Plot fit");
    private final JCheckBox boxPlotModulusFit = new JCheckBox("Plot fit");


    public CurveReplottingDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title, temporary);

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);     
        add(panelButtons, BorderLayout.SOUTH);      

        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initItemListener()
    {
        boxPlotRecordedCurve.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPlotRecordedCurve(selected); 
            }
        });
        boxPlotRecordedCurveFit.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPlotRecordedCurveFit(selected); 
            }
        });
        boxPlotIndentation.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPlotIndentation(selected); 
            }
        });
        boxPlotIndentationFit.addItemListener(new ItemListener()
        {          
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPlotIndentationFit(selected); 
            }
        });
        boxPlotModulus.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPlotModulus(selected); 
            }
        });      
        boxPlotModulusFit.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setPlotModulusFit(selected); 
            }
        });
    }   

    private JPanel buildMainPanel()
    {   
        SubPanel mainPanel = new SubPanel(); 

        JPanel panelOperationRange = buildPanelROIRelative();

        mainPanel.addComponent(new JLabel("Plot: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(panelOperationRange, 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);

        SubPanel panelPlots = new SubPanel();

        JLabel labelRecordedCurve = new JLabel("Recorded cure");
        JLabel labelIndentationCurve = new JLabel("Indentation");
        JLabel labelModuluCurve = new JLabel("Pointwise modulus");

        panelPlots.addComponent(labelRecordedCurve, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotRecordedCurve, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelPlots.addComponent(boxPlotRecordedCurveFit, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelPlots.addComponent(labelIndentationCurve, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotIndentation, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelPlots.addComponent(boxPlotIndentationFit, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelPlots.addComponent(labelModuluCurve, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotModulus, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelPlots.addComponent(boxPlotModulusFit, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(panelPlots, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void ok()
    {
        CurveReplottingModel model = getModel();
        model.apply();

        setVisible(false);
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        CurveReplottingModel model = getModel();

        this.initPlotRecordedCurve = model.isPlotRecordedCurve();
        this.initPlotRecordedCurveFit = model.isPlotRecordedCurveFit();

        this.initPlotIndentation = model.isPlotIndentation();
        this.initPlotIndentationFit = model.isPlotIndentationFit();

        this.initPlotModulus = model.isPlotModulus();
        this.initPlotModulusFit = model.isPlotModulusFit();    
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        CurveReplottingModel model = getModel();

        model.setPlotRecordedCurve(initPlotRecordedCurve);
        model.setPlotRecordedCurveFit(initPlotRecordedCurveFit);

        model.setPlotIndentation(initPlotIndentation);
        model.setPlotIndentationFit(initPlotIndentationFit);

        model.setPlotModulus(initPlotModulus);
        model.setPlotModulusFit(initPlotModulusFit);
    }

    @Override
    protected void resetEditor()
    {   
        super.resetEditor();

        boxPlotRecordedCurve.setSelected(initPlotRecordedCurve);
        boxPlotRecordedCurveFit.setSelected(initPlotRecordedCurveFit);
        boxPlotRecordedCurveFit.setEnabled(initPlotRecordedCurve);

        boxPlotIndentation.setSelected(initPlotIndentation);
        boxPlotIndentationFit.setSelected(initPlotIndentationFit);
        boxPlotIndentationFit.setEnabled(initPlotIndentation);

        boxPlotModulus.setSelected(initPlotModulus);      
        boxPlotModulusFit.setSelected(initPlotModulusFit);
        boxPlotModulusFit.setEnabled(initPlotModulus);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(PLOT_RECORDED_CURVE.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotRecordedCurve.isSelected();
            boxPlotRecordedCurveFit.setEnabled(newVal);

            if(newVal != oldVal)
            {
                boxPlotRecordedCurve.setSelected(newVal);
            }
        }
        else if(PLOT_RECORDED_CURVE_FIT.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotRecordedCurveFit.isSelected();

            if(newVal != oldVal)
            {
                boxPlotRecordedCurveFit.setSelected(newVal);
            }
        }
        else if(PLOT_INDENTATION.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotIndentation.isSelected();
            boxPlotIndentationFit.setEnabled(newVal);

            if(newVal != oldVal)
            {
                boxPlotIndentation.setSelected(newVal);
            }
        }
        else if(PLOT_INDENTATION_FIT.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotIndentationFit.isSelected();

            if(newVal != oldVal)
            {
                boxPlotIndentationFit.setSelected(newVal);
            }
        }
        else if(PLOT_MODULUS.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotModulus.isSelected();
            boxPlotModulusFit.setEnabled(newVal);

            if(newVal != oldVal)
            {
                boxPlotModulus.setSelected(newVal);               
            }
        }
        else if(PLOT_MODULUS_FIT.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotModulusFit.isSelected();

            if(newVal != oldVal)
            {
                boxPlotModulusFit.setSelected(newVal);
            }
        }
    }
}
