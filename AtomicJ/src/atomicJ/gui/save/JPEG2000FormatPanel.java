
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

package atomicJ.gui.save;

import static atomicJ.gui.save.SaveModelProperties.*;

import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.SubPanel;


public class JPEG2000FormatPanel extends SubPanel implements PropertyChangeListener, ChangeListener, ItemListener 
{
    private static final long serialVersionUID = 1L;

    private final JLabel labelCompression = new JLabel("Quality");
    private BasicImageFormatPanel basicPanel;

    private final JCheckBox boxLossless = new JCheckBox("Lossless");
    private final JSpinner spinnerEncodingRate = new JSpinner(new SpinnerNumberModel(0.5, 0.1, 1., 0.1));

    private JPEG2000FormatModel model;

    public JPEG2000FormatPanel(JPEG2000FormatModel model) 
    {
        setModel(model);
        buildLayout();
        initChangeListener();
        initItemListener();
    }

    public JPEG2000FormatModel getModel() 
    {
        return model;
    }

    public void setModel(JPEG2000FormatModel modelNew) 
    {
        if(basicPanel == null)
        {
            this.basicPanel = new BasicImageFormatPanel(modelNew);
        }
        else
        {
            basicPanel.setModel(modelNew);
        }

        if (model != null) 
        {
            model.removePropertyChangeListener(this);
        }
        this.model = modelNew;
        pullModelProperties();
        modelNew.addPropertyChangeListener(this);
    }

    public void specifyDimensions(Number widthNew, Number heightNew) 
    {
        model.specifyDimensions(widthNew, heightNew);
    }

    private void pullModelProperties() 
    {
        boolean lossless = model.isLossless();
        double encodingRate = model.getEncodingRate();
        boolean encodingRateEnabled = model.isEncodingRateEnabled();

        boxLossless.setSelected(lossless);
        spinnerEncodingRate.setValue(encodingRate);
        setEncodingRateEnabled(encodingRateEnabled);
    }

    private void setEncodingRateEnabled(boolean enabled)
    {
        labelCompression.setEnabled(enabled);
        spinnerEncodingRate.setEnabled(enabled);
    }

    private void buildLayout() 
    {
        JPanel panelJPEG2000 = builJPEG2000Panel();
        JPanel basicInternalPanel = basicPanel.getInternalPanel();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(panelJPEG2000)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(panelJPEG2000).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(basicInternalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

        layout.linkSize(SwingConstants.HORIZONTAL,panelJPEG2000, basicInternalPanel);
    }

    private JPanel builJPEG2000Panel()
    {
        SubPanel panel = new SubPanel();

        panel.addComponent(Box.createVerticalGlue(), 0, 0, 2, 1, GridBagConstraints.WEST,GridBagConstraints.BOTH, 0, 1);
        panel.addComponent(boxLossless, 1, 1, 1, 1, GridBagConstraints.WEST,GridBagConstraints.NONE, 0, 0);
        panel.addComponent(labelCompression, 0, 2, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 0);
        panel.addComponent(spinnerEncodingRate, 1, 2, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 0);
        panel.addComponent(Box.createVerticalGlue(), 0, 3, 2, 1, GridBagConstraints.WEST,GridBagConstraints.BOTH, 0, 1);

        return panel;
    }

    private void initChangeListener()
    {
        spinnerEncodingRate.addChangeListener(this);
    }

    private void initItemListener() 
    {
        boxLossless.addItemListener(this);
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();
        if (source == boxLossless) 
        {
            boolean selected = (event.getStateChange()== ItemEvent.SELECTED);
            model.setLossless(selected);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if (ENCODING_RATE.equals(property)) 
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue(); 
            double oldVal = ((SpinnerNumberModel)spinnerEncodingRate.getModel()).getNumber().doubleValue();
            if (oldVal != newVal) 
            {
                spinnerEncodingRate.setValue(newVal);
            }
        } 
        else if(LOSSLESS.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxLossless.isSelected();

            if(oldVal != newVal) 
            {
                boxLossless.setSelected(newVal);
            }
        }
        else if(ENCODING_RATE_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            setEncodingRateEnabled(newVal);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();
        if (source == spinnerEncodingRate) 
        {
            double encodingRateNew = ((SpinnerNumberModel) spinnerEncodingRate.getModel()).getNumber().doubleValue();
            model.setEncodingRate(encodingRateNew);
        }           
    }

}
