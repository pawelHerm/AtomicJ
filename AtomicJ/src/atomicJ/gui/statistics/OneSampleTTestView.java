
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

package atomicJ.gui.statistics;

import static atomicJ.gui.NumericalField.VALUE_EDITED;
import static atomicJ.gui.statistics.InferenceModelProperties.*;


import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;


public class OneSampleTTestView <E extends Processed1DPack<E,?>> extends SubPanel implements SampleEditor<E>, PropertyChangeListener, ItemListener, ChangeListener 
{
    private static final long serialVersionUID = 1L;

    private final JCheckBox boxTwoTailed = new JCheckBox();
    private final NumericalField fieldTestedMean = new NumericalField();
    private final JSpinner spinnerSignificance = new JSpinner(new SpinnerNumberModel(0.05,0,100,0.01));
    private final SamplePanel<E> firstSamplePanel;
    private OneSampleTTestModel<E> model;
    private SampleEditionDialog<E> sampleEditionDialog;
    private SimpleWizard wizard;

    public OneSampleTTestView(OneSampleTTestModel<E> model) 
    {
        firstSamplePanel = new SamplePanel<>("Sample 1", this);

        setModel(model);
        buildLayout();
        initItemListener();
        initChangeListener();
        initFieldsListener();
    }

    public void setWizard(SimpleWizard wizard)
    {
        this.wizard = wizard;
        if(sampleEditionDialog != null)
        {
            sampleEditionDialog.dispose();
            sampleEditionDialog = new SampleEditionDialog<>(wizard);
            sampleEditionDialog.setAvailableData(model.getAvailableData());
        }
    }

    public OneSampleTTestModel<E> getModel() 
    {
        return model;
    }

    public void setModel(OneSampleTTestModel<E> modelNew) 
    {       
        if (model != null) 
        {
            model.removePropertyChangeListener(this);
        }
        this.model = modelNew;
        firstSamplePanel.setModel(modelNew.getFirstSampleModel());
        pullModelProperties();
        modelNew.addPropertyChangeListener(this);
    }

    private void pullModelProperties() 
    {
        double significanceLevel = model.getSignificanceLevel();
        boolean twoTailed = model.isTwoTailed();
        double  nullHypothesisMean = model.getNullHypothesisMean();

        spinnerSignificance.setValue(significanceLevel);
        boxTwoTailed.setSelected(twoTailed);
        fieldTestedMean.setValue(nullHypothesisMean);
    }

    private JPanel buildTestParametersPanel()
    {
        SubPanel parametersPanel = new SubPanel();

        parametersPanel.addComponent(new JLabel("Two-tailed"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        parametersPanel.addComponent(boxTwoTailed, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        parametersPanel.addComponent(new JLabel("Significance level"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1); 
        parametersPanel.addComponent(spinnerSignificance, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        parametersPanel.addComponent(new JLabel("Tested mean"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        parametersPanel.addComponent(fieldTestedMean, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        parametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Test parameters"));

        return parametersPanel;
    }

    private void buildLayout() 
    {
        JPanel panelTestParameters = buildTestParametersPanel();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTestParameters,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(firstSamplePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                );

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTestParameters, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                        .addComponent(firstSamplePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    }

    private void initItemListener() 
    {
        boxTwoTailed.addItemListener(this);
    }

    private void initChangeListener()
    {
        spinnerSignificance.addChangeListener(this);
    }

    private void initFieldsListener()
    {
        final PropertyChangeListener fieldsListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                Object source = evt.getSource();
                Double newVal = ((Number)evt.getNewValue()).doubleValue();

                if(source == fieldTestedMean)
                {
                    model.setNullHypothesisMean(newVal);
                }
            }
        };

        fieldTestedMean.addPropertyChangeListener(VALUE_EDITED, fieldsListener);
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();

        if(source == boxTwoTailed)
        {
            boolean twoTailedNew = boxTwoTailed.isSelected();
            model.setTwoTailed(twoTailedNew);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == spinnerSignificance)
        {
            double significaneLevel = ((SpinnerNumberModel)spinnerSignificance.getModel()).getNumber().doubleValue();
            model.setSignificanceLevel(significaneLevel);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if (name.equals(SIGNIFICANCE_LEVEL)) 
        {
            Double newVal = (Double) evt.getNewValue();
            Double oldVal = ((SpinnerNumberModel)spinnerSignificance.getModel()).getNumber().doubleValue();;
            if (oldVal.equals(newVal))
            {
                spinnerSignificance.setValue(newVal);
            }
        } 
        else if (name.equals(TWO_TAILED)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boolean oldVal = boxTwoTailed.isSelected();
            if (oldVal != newVal) 
            {
                boxTwoTailed.setSelected(newVal);
            }
        } 
        else if (name.equals(NULL_HYPOTHEISIS_MEAN)) 
        {
            Double newVal = (Double) evt.getNewValue();
            Double oldVal = fieldTestedMean.getValue().doubleValue();
            if (!oldVal.equals(newVal)) 
            {
                fieldTestedMean.setValue(newVal);
            }
        } 
    }

    @Override
    public void edit(ProcessedPackSampleModel<E> sampleModel) 
    {
        if(sampleEditionDialog == null)
        {
            sampleEditionDialog = new SampleEditionDialog<>(wizard);
            sampleEditionDialog.setAvailableData(model.getAvailableData());
        }
        sampleEditionDialog.edit(sampleModel);
    }
}
