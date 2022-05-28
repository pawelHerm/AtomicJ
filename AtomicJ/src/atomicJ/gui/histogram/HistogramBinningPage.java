
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

package atomicJ.gui.histogram;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;


import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;
import atomicJ.statistics.*;
import atomicJ.utilities.Validation;

import static atomicJ.gui.histogram.HistogramModelProperties.*;

public class HistogramBinningPage extends JPanel implements PropertyChangeListener, ChangeListener
{
    private static final long serialVersionUID = 1L;

    private final JLabel labelDataCount = new JLabel();
    private final JLabel labelDiscardedCount = new JLabel("", JLabel.CENTER);
    private final JLabel labelMinimum = new JLabel();
    private final JLabel labelMaximum = new JLabel();

    private final JCheckBox boxEntitled = new JCheckBox("Use as title");
    private final JCheckBox boxFullRange = new JCheckBox("Full range");
    private final JCheckBox boxFit = new JCheckBox("Add fit");

    private final JFormattedTextField fieldName = new JFormattedTextField(new DefaultFormatter());
    private final NumericalField fieldBinCount = new NumericalField("Bin count must be a positive integer", 1, false);
    private final NumericalField fieldMinimum = new NumericalField("Range minimum must be a number lesser than maximum");
    private final NumericalField fieldMaximum = new NumericalField("Range maximum must be a number greater than minimum");
    private final NumericalField fieldBinWidth = new NumericalField("Bin width must be a positive number", Double.MIN_VALUE);
    private final JSpinner spinnerTrimSmallest = new JSpinner(new SpinnerNumberModel(0.,0.,100.,1.));
    private final JSpinner spinnerTrimLargest = new JSpinner(new SpinnerNumberModel(0.,0.,100.,1.));
    private final JSpinner spinnerBinCount = new JSpinner(new SpinnerNumberModel(1, 1, Double.MAX_VALUE, 1));

    private final JComboBox<BinningMethod> comboBinningMethod = new JComboBox<>(BinningMethod.values());
    private final JComboBox<DistributionType> comboDistributionType = new JComboBox<>(DistributionType.values());
    private final JComboBox<HistogramType> comboHistogramType = new JComboBox<>(HistogramType.values());
    private final JComboBox<FitType> comboFitType = new JComboBox<>(FitType.values()); 

    private HistogramBinningModel model;

    public HistogramBinningPage()
    {
        spinnerBinCount.setEditor(fieldBinCount);
        initItemListener();
        initFieldsListener();
        initKeyListener();
        initChangeListener();

        SubPanel namePanel = buildNamePanel();
        SubPanel binsPanel = buildBinsPanel();
        SubPanel rangePanel = buildRangePanel();
        SubPanel typePanel = buildTypePanel();
        SubPanel fitPanel = buildFitPanel();
        SubPanel trimPanel = buildTrimPanel();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(
                        layout.createSequentialGroup()
                        .addComponent(binsPanel)
                        .addComponent(rangePanel))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(namePanel)
                        .addComponent(typePanel)
                        )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(trimPanel)
                        .addComponent(fitPanel)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(typePanel)
                        .addComponent(namePanel)
                        )
                .addGroup(layout.createParallelGroup()
                        .addComponent(binsPanel)
                        .addComponent(rangePanel))
                .addGroup(layout.createParallelGroup()
                        .addComponent(trimPanel)
                        .addComponent(fitPanel))
                );

        layout.linkSize(SwingConstants.VERTICAL, binsPanel, rangePanel,trimPanel);
        layout.linkSize(SwingConstants.VERTICAL, namePanel, typePanel, fitPanel);
        layout.linkSize(SwingConstants.HORIZONTAL, namePanel, typePanel, binsPanel, rangePanel, fitPanel, trimPanel);
    }

    public void setModel(HistogramBinningModel modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);
        pullModelProperties();
    }

    public void cleanUp()
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = null;
    }


    public Component getControls() 
    {
        return new JPanel();
    }

    private void pullModelProperties()
    {
        boolean fullRange = model.isFullRange();

        String name = model.getName();
        String unit = model.getUnit();
        boolean entitled = model.isEntitled();

        int dataCount = model.getAllDataCount();
        double binCount = model.getBinCount();
        double binWidth = model.getBinWidth();
        double rangeMin = model.getRangeMinimum();
        double rangeMax = model.getRangeMaximum();
        boolean rangeExtensive = model.isRangeExtensive();
        double trimSmallest = 100*model.getFractionOfSmallestTrimmed();
        double trimLargest = 100*model.getFractionOfLargestTrimmed();
        int discardedCount = model.getDiscardedDataCount();
        boolean fitted = model.isFitted();
        BinningMethod method = model.getBinningMethod();
        HistogramType histogramType = model.getHistogramType();
        DistributionType distributionType = model.getDistributionType();
        FitType fitType = model.getFitType();

        boolean isManual = method.equals(BinningMethod.MANUAL);
        boolean isDensityToBeRemoved  = (distributionType.equals(DistributionType.CUMULATIVE));
        boolean isCumulativeToBeRemoved  = (histogramType.equals(HistogramType.PROBABILITY_DENSITY));
        boolean isLognormalToBeRemoved = (model.containsNonpositiveValues());

        if(isDensityToBeRemoved)
        {
            comboHistogramType.removeItem(HistogramType.PROBABILITY_DENSITY);
            comboHistogramType.revalidate();
        }

        if(isCumulativeToBeRemoved)
        {
            comboDistributionType.removeItem(DistributionType.CUMULATIVE);
            comboDistributionType.revalidate();
        }

        if(isLognormalToBeRemoved)
        {
            comboFitType.removeItem(FitType.LOG_NORMAL);
        }
        else
        {
            comboFitType.removeItem(FitType.LOG_NORMAL);
            comboFitType.addItem(FitType.LOG_NORMAL);
        }
        comboFitType.revalidate();		

        boxEntitled.setSelected(entitled);
        boxFullRange.setSelected(fullRange);
        boxFit.setSelected(fitted);
        fieldName.setValue(name);
        fieldMinimum.setEnabled(!fullRange);
        fieldMinimum.setValue(rangeMin);
        fieldMaximum.setEnabled(!fullRange);
        fieldMaximum.setValue(rangeMax);
        fieldMinimum.setMaximum(rangeMax);
        fieldMaximum.setMinimum(rangeMin);
        labelDataCount.setText(Integer.toString(dataCount));
        fieldBinCount.setValue(binCount);
        if(!Double.isNaN(binCount))
        {
            spinnerBinCount.setValue(binCount);
        }
        fieldBinWidth.setValue(binWidth);
        spinnerBinCount.setEnabled(isManual);
        fieldBinCount.setEnabled(isManual);
        fieldBinCount.setEnabled(rangeExtensive);
        fieldBinWidth.setEnabled(isManual);
        labelDiscardedCount.setText(Integer.toString(discardedCount));
        spinnerTrimSmallest.setValue(trimSmallest);
        spinnerTrimLargest.setValue(trimLargest);
        comboBinningMethod.setSelectedItem(method);
        comboHistogramType.setSelectedItem(histogramType);
        comboDistributionType.setSelectedItem(distributionType);
        comboFitType.setSelectedItem(fitType);
        comboFitType.setEnabled(fitted);
        labelMinimum.setText("Minimum ("+ unit + ")");
        labelMaximum.setText("Maximum ("+ unit + ")");
    }


    private void initChangeListener()
    {
        spinnerBinCount.addChangeListener(this);
        spinnerTrimSmallest.addChangeListener(this);
        spinnerTrimLargest.addChangeListener(this);
    }

    private SubPanel buildNamePanel()
    {
        DefaultFormatter formatter = (DefaultFormatter)fieldName.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        SubPanel namePanel = new SubPanel();

        namePanel.addComponent(new JLabel("Name"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        namePanel.addComponent(fieldName, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        namePanel.addComponent(boxEntitled, 0, 1, 2, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Name"));

        return namePanel;
    }

    private SubPanel buildBinsPanel()
    {
        SubPanel binsPanel = new SubPanel();

        binsPanel.addComponent(new JLabel("Data count"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        binsPanel.addComponent(labelDataCount, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        binsPanel.addComponent(new JLabel("Method"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        binsPanel.addComponent(comboBinningMethod, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        binsPanel.addComponent(new JLabel("Bin count"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);	
        binsPanel.addComponent(spinnerBinCount, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        binsPanel.addComponent(new JLabel("Bin width"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);	
        binsPanel.addComponent(fieldBinWidth, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        binsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Binning"));

        return binsPanel;
    }

    private SubPanel buildRangePanel()
    {
        SubPanel rangePanel = new SubPanel();

        fieldMinimum.setEnabled(false);
        fieldMaximum.setEnabled(false);

        rangePanel.addComponent(boxFullRange, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        rangePanel.addComponent(labelMinimum, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        rangePanel.addComponent(labelMaximum, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        rangePanel.addComponent(fieldMinimum, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        rangePanel.addComponent(fieldMaximum, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);	

        rangePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Range"));

        return rangePanel;
    }

    private SubPanel buildTypePanel()
    {
        SubPanel typePanel = new SubPanel();

        typePanel.addComponent(new JLabel("Distribution"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        typePanel.addComponent(comboDistributionType, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        typePanel.addComponent(new JLabel("Histogram"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        typePanel.addComponent(comboHistogramType, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        typePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Type"));

        return typePanel;
    }

    private SubPanel buildFitPanel()
    {
        SubPanel fitPanel = new SubPanel();

        fitPanel.addComponent(boxFit, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        fitPanel.addComponent(new JLabel("Distribution"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        fitPanel.addComponent(comboFitType, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        fitPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Fit"));

        return fitPanel;
    }

    private SubPanel buildTrimPanel()
    {
        JSpinner.NumberEditor spinnerEditorSmallest = (JSpinner.NumberEditor) spinnerTrimSmallest.getEditor();
        JFormattedTextField textFieldSmallest = spinnerEditorSmallest.getTextField();
        NumberFormat formatSmallest = NumberFormat.getInstance(Locale.US);
        NumberFormatter formatterSmallest = new NumberFormatter(formatSmallest);
        formatterSmallest.setValueClass(Double.class);

        textFieldSmallest.setFormatterFactory(new DefaultFormatterFactory(formatterSmallest));

        JSpinner.NumberEditor spinnerEditorLargest = (JSpinner.NumberEditor) spinnerTrimLargest.getEditor();
        JFormattedTextField textFieldLargest = spinnerEditorLargest.getTextField();
        NumberFormat formatLargest= NumberFormat.getInstance(Locale.US);
        NumberFormatter formatterLargest = new NumberFormatter(formatLargest);
        formatterLargest.setValueClass(Double.class);

        textFieldLargest.setFormatterFactory(new DefaultFormatterFactory(formatterLargest));

        SubPanel trimmingPanel = new SubPanel();

        trimmingPanel.addComponent(new JLabel("Discard"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(spinnerTrimSmallest, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(new JLabel("% smallest"), 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        trimmingPanel.addComponent(new JLabel("Discard"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(spinnerTrimLargest, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(new JLabel("% largest"), 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        trimmingPanel.addComponent(new JLabel("Discarded"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelDiscardedCount, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(new JLabel("results"), 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        trimmingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data trimming"));

        return trimmingPanel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(name.equals(BATCH_CHANGED))
        {
            pullModelProperties();
        }
        else if(name.equals(BIN_COUNT))
        {
            Double newVal = (Double)evt.getNewValue();
            Double oldVal = fieldBinCount.getValue().doubleValue();
            if(!(newVal.equals(oldVal)))
            {
                fieldBinCount.setValue(newVal);
            }			
        }
        else if(name.equals(BIN_WIDTH))
        {
            Double newVal = (Double)evt.getNewValue();
            Double oldVal = (Double)fieldBinWidth.getValue();
            if(!(newVal.equals(oldVal)))
            {
                fieldBinWidth.setValue(newVal);
            }
        }
        else if(name.equals(BINNING_METHOD))
        {
            BinningMethod newVal = (BinningMethod)evt.getNewValue();
            BinningMethod oldVal = (BinningMethod)comboBinningMethod.getSelectedItem();
            if(!(newVal.equals(oldVal))){
                comboBinningMethod.setSelectedItem(newVal);
            }
            boolean isManual = newVal.equals(BinningMethod.MANUAL);
            spinnerBinCount.setEnabled(isManual);
            fieldBinCount.setEnabled(isManual);
            fieldBinWidth.setEnabled(isManual);			
        }
        else if(name.equals(DISCARDED_COUNT))
        {
            int dicardedCount = (int)evt.getNewValue();
            labelDiscardedCount.setText(Integer.toString(dicardedCount));
        }
        else if(name.equals(TRIM_SMALLEST))
        {
            Double trimSmallestNew = 100*(Double)evt.getNewValue();
            Double trimSmallestOld = ((SpinnerNumberModel)spinnerTrimSmallest.getModel()).getNumber().doubleValue();

            if(!trimSmallestOld.equals(trimSmallestNew))
            {
                spinnerTrimSmallest.setValue(trimSmallestNew);
            }
        }
        else if(name.equals(TRIM_LARGEST))
        {
            Double trimLargestNew = 100*(Double)evt.getNewValue();
            Double trimLargestOld = ((SpinnerNumberModel)spinnerTrimLargest.getModel()).getNumber().doubleValue();

            if(!trimLargestOld.equals(trimLargestNew))
            {
                spinnerTrimLargest.setValue(trimLargestNew);
            }
        }
        else if(name.equals(NAME))
        {
            String newVal = (String)evt.getNewValue();
            String oldVal = (String)fieldName.getValue();
            if(!(newVal.equals(oldVal)))
            {
                fieldName.setValue(newVal);
            }
        }
        else if(name.equals(ENTITLED))
        {
            Boolean newVal = (Boolean)evt.getNewValue();
            Boolean oldVal = boxEntitled.isSelected();
            if(!(newVal.equals(oldVal)))
            {
                boxEntitled.setSelected(newVal);
            }
        }
        else if(name.equals(FULL_RANGE))
        {
            Boolean newVal = (Boolean)evt.getNewValue();
            Boolean oldVal = boxFullRange.isSelected();

            fieldMaximum.setEnabled(!newVal);
            fieldMinimum.setEnabled(!newVal);

            if(!(newVal.equals(oldVal)))
            {
                boxFullRange.setSelected(newVal);
            }
        }
        else if(name.equals(RANGE_MIN))
        {
            Double newVal = (Double)evt.getNewValue();
            Double oldVal = (Double)fieldMinimum.getValue();
            if(!(newVal.equals(oldVal)))
            {
                fieldMinimum.setValue(newVal);
                fieldMaximum.setMinimum(newVal);
            }
        }
        else if(name.equals(RANGE_MAX))
        {
            Double newVal = (Double)evt.getNewValue();
            Double oldVal = (Double)fieldMaximum.getValue();

            if(!(newVal.equals(oldVal)))
            {
                fieldMaximum.setValue(newVal);
                fieldMinimum.setMaximum(newVal);
            }
        }
        else if(RANGE_EXTENSIVE.equals(name))
        {
            boolean newVal = (Boolean)evt.getNewValue();
            fieldBinCount.setEnabled(newVal);
        }
        else if(name.equals(NONPOSITIVE_VALUES))
        {
            Boolean newVal = (Boolean)evt.getNewValue();

            if(newVal)
            {
                comboFitType.removeItem(FitType.LOG_NORMAL);
            }
            else
            {
                comboFitType.addItem(FitType.LOG_NORMAL);
            }
            comboFitType.revalidate();		
        }
        else if(name.equals(HISTOGRAM_TYPE))
        {
            HistogramType newVal = (HistogramType)evt.getNewValue();
            HistogramType oldVal = (HistogramType)comboHistogramType.getSelectedItem();

            if(!(newVal.equals(oldVal)))
            {
                if(newVal.equals(HistogramType.PROBABILITY_DENSITY))
                {
                    comboDistributionType.removeItem(DistributionType.CUMULATIVE);
                    comboDistributionType.revalidate();
                }
                else if(oldVal.equals(HistogramType.PROBABILITY_DENSITY))
                {
                    comboDistributionType.addItem(DistributionType.CUMULATIVE);
                    comboDistributionType.revalidate();
                }

                comboHistogramType.setSelectedItem(newVal);
            }
        }
        else if(name.equals(DISTRIBUTION_TYPE))
        {
            DistributionType newVal = (DistributionType)evt.getNewValue();
            DistributionType oldVal = (DistributionType)comboDistributionType.getSelectedItem();
            if(!(newVal.equals(oldVal)))
            {
                if(newVal.equals(DistributionType.CUMULATIVE))
                {
                    comboHistogramType.removeItem(HistogramType.PROBABILITY_DENSITY);
                    comboHistogramType.revalidate();
                }
                else if(oldVal.equals(DistributionType.CUMULATIVE))
                {
                    comboHistogramType.addItem(HistogramType.PROBABILITY_DENSITY);
                    comboHistogramType.revalidate();
                }

                comboDistributionType.setSelectedItem(newVal);
            }
        }
        else if(name.equals(FITTED))
        {
            Boolean newVal = (Boolean)evt.getNewValue();
            Boolean oldVal = boxFit.isSelected();

            comboFitType.setEnabled(newVal);

            if(!(newVal.equals(oldVal)))
            {
                boxFit.setSelected(newVal);
            }
        }
        else if(name.equals(FIT_TYPE))
        {
            FitType newVal = (FitType)evt.getNewValue();
            FitType oldVal = (FitType)comboFitType.getSelectedItem();
            if(!(newVal.equals(oldVal)))
            {
                comboFitType.setSelectedItem(newVal);
            }
        }
    }

    private void initFieldsListener()
    {
        fieldName.addPropertyChangeListener("value", new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String newVal = evt.getNewValue().toString();
                model.setName(newVal);                
            }
        });

        fieldBinCount.addPropertyChangeListener("value", new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                Double newVal = ((Number)evt.getNewValue()).doubleValue();

                if(!Double.isNaN(newVal))
                {
                    spinnerBinCount.setValue(newVal);
                }                
            }
        });

        fieldBinCount.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {    
                Double newVal = ((Number)evt.getNewValue()).doubleValue();
                model.specifyBinCount(newVal);
            }
        });

        fieldBinWidth.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {     
                Double newVal = ((Number)evt.getNewValue()).doubleValue();
                model.specifyBinWidth(newVal);
            }
        });

        fieldMinimum.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {  
                Double newVal = ((Number)evt.getNewValue()).doubleValue();
                model.specifyRangeMinimum(newVal);
            }
        });	

        fieldMaximum.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Double newVal = ((Number)evt.getNewValue()).doubleValue();
                model.specifyRangeMaximum(newVal);  
            }
        });
    }

    private void initItemListener()
    {
        boxEntitled.addItemListener(new ItemListener() {         
            @Override
            public void itemStateChanged(ItemEvent e) { 
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setEntitled(selected);
            }
        });

        boxFullRange.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setFullRange(selected);                
            }
        });

        boxFit.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setFitted(selected);                
            }
        });

        comboBinningMethod.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                BinningMethod method = (BinningMethod) comboBinningMethod.getSelectedItem();
                model.setBinningMethod(method);                
            }
        });

        comboDistributionType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                DistributionType item = (DistributionType)e.getItem();
                boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);

                if(isSelected)
                {
                    model.setDistributionType(item);
                    if(item.equals(DistributionType.CUMULATIVE))
                    {
                        comboHistogramType.removeItem(HistogramType.PROBABILITY_DENSITY);
                    }
                }
                else
                {
                    if(item.equals(DistributionType.CUMULATIVE))
                    {
                        comboHistogramType.addItem(HistogramType.PROBABILITY_DENSITY);
                    }
                }                   
            }
        });
        comboHistogramType.addItemListener(new ItemListener() 
        {

            @Override
            public void itemStateChanged(ItemEvent e) {
                HistogramType item = (HistogramType)e.getItem();
                boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);

                if(isSelected)
                {
                    model.setHistogramType(item);
                    if(item.equals(HistogramType.PROBABILITY_DENSITY))
                    {
                        comboDistributionType.removeItem(DistributionType.CUMULATIVE);
                    }
                }
                else
                {
                    if(item.equals(HistogramType.PROBABILITY_DENSITY))
                    {
                        comboDistributionType.addItem(DistributionType.CUMULATIVE);
                    }
                }                       
            }
        });

        comboFitType.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                FitType type = (FitType)comboFitType.getSelectedItem();
                model.setFitType(type);                
            }
        });
    }

    public void initKeyListener() 
    {
        KeyListener listener = new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent evt)
            {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    Object source = evt.getSource();

                    if(source == fieldBinWidth)
                    {
                        model.setCountConsistentWithWidth();
                    }
                    else if(source == fieldBinCount)
                    {
                        model.setWidthConsistentWithRangeAndCount();
                    }
                }
            }
        };

        fieldBinCount.addKeyListener(listener);
        fieldBinWidth.addKeyListener(listener);		
    }

    @Override
    public void stateChanged(ChangeEvent evt)
    {
        Object source = evt.getSource();
        if(source == spinnerBinCount)
        {
            Double binCountNew = ((SpinnerNumberModel)spinnerBinCount.getModel()).getNumber().doubleValue();
            Number binCountOld = fieldBinCount.getValue();
            if(!Objects.equals(binCountNew, binCountOld))
            {
                model.specifyBinCount(binCountNew);
            }
        }
        else if(source == spinnerTrimSmallest)
        {
            Double trimSmallestNew = 0.01*((SpinnerNumberModel)spinnerTrimSmallest.getModel()).getNumber().doubleValue();
            model.setFractionOfSmallestTrimmed(trimSmallestNew);
        }	
        else if(source == spinnerTrimLargest)
        {
            Double trimLargestNew = 0.01*((SpinnerNumberModel)spinnerTrimLargest.getModel()).getNumber().doubleValue();
            model.setFractionOfLargestTrimmed(trimLargestNew);
        }
    }
}
