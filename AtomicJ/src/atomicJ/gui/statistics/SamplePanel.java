
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


import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.SubPanel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.statistics.DescriptiveStatistics;


import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class SamplePanel <E extends Processed1DPack<E,?>> extends SubPanel implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final JLabel labelSampleName = new JLabel();
    private final JLabel labelVariable = new JLabel();
    private final JLabel labelSize = new JLabel();
    private final JLabel labelMean = new JLabel();
    private final JLabel labelVariance = new JLabel();
    private final JButton buttonEdit = new JButton(new EditAction());

    private final SampleEditor<E> sampleEditor;
    private ProcessedPackSampleModel<E> model;

    public SamplePanel(String title, SampleEditor<E> sampleEditor)
    {
        buildLayout(title);
        this.sampleEditor = sampleEditor;
    }

    public  SamplePanel(ProcessedPackSampleModel<E> model, String title, SampleEditor<E> editionDialog)
    {
        buildLayout(title);
        setModel(model);
        this.sampleEditor = editionDialog;
    }

    private void buildLayout(String title)
    {       
        addComponent(new JLabel("Name:"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(labelSampleName, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(new JLabel("Size:"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);  
        addComponent(labelSize, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(new JLabel("Mean:"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);  
        addComponent(labelMean, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(new JLabel("Variance:"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);  
        addComponent(labelVariance, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(new JLabel("Variable:"), 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(labelVariable, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(buttonEdit, 1, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
    }

    public void setModel(ProcessedPackSampleModel<E> modelNew) 
    {       
        if (model != null) 
        {
            model.removePropertyChangeListener(this);
        }
        this.model = modelNew;
        pullModelProperties();
        modelNew.addPropertyChangeListener(this);
    }

    private void pullModelProperties() 
    {
        String name = model.getSampleName();
        String quantity = model.getVariable();
        String count = Integer.toString(model.getCount());

        String meanString = "Indeterminate";
        String varianceString = "Indeterminate";
        try 
        {
            if(model.isAllInputProvided())
            {
                NumberFormat format = NumberFormat.getInstance(Locale.US);
                double[] data = model.getData();
                double meanValue = DescriptiveStatistics.arithmeticMean(data);
                double varianceValue = DescriptiveStatistics.varianceSample(data);
                if(!Double.isNaN(meanValue))
                {
                    meanString = format.format(meanValue);
                }
                if(!Double.isNaN(varianceValue))
                {
                    varianceString = format.format(varianceValue);
                }
            }   
        } 
        catch (UserCommunicableException e) 
        {
            e.printStackTrace();
        }

        labelSampleName.setText(name);
        labelVariable.setText(quantity);
        labelSize.setText(count);
        labelMean.setText(meanString);
        labelVariance.setText(varianceString);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(name.equals(SAMPLE_NAME))
        {
            String newVal = (String)evt.getNewValue();
            labelSampleName.setText(newVal);
        }
        else if(name.equals(SAMPLE_VARIABLE))
        {
            String newVal = evt.getNewValue().toString();
            labelVariable.setText(newVal);
        }
        else if(name.equals(SAMPLE_DATA))
        {
            double[] data = (double[])evt.getNewValue();

            int size = data.length;
            double meanValue = DescriptiveStatistics.arithmeticMean(data);
            double varianceValue = DescriptiveStatistics.varianceSample(data);

            String meanString = "Indeterminate";
            String varianceString = "Indeterminate";
            NumberFormat format = NumberFormat.getInstance(Locale.US);

            if(!Double.isNaN(meanValue))
            {
                meanString = format.format(meanValue);
            }
            if(!Double.isNaN(varianceValue))
            {
                varianceString = format.format(varianceValue);
            }
            labelSize.setText(Integer.toString(size));
            labelMean.setText(meanString);
            labelVariance.setText(varianceString);
        }
    }

    private class EditAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public EditAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(NAME,"Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            sampleEditor.edit(model);
        }
    }
}