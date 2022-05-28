
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

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;

import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;


public class BasicImageFormatPanel extends SubPanel implements PropertyChangeListener, ItemListener 
{
    private static final long serialVersionUID = 1L;

    private final NumericalField fieldWidth = new NumericalField("Width numbers must be a positive integer", 1, false);
    private final NumericalField fieldHeight = new NumericalField("Height numbers must be a positive integer", 1, false);

    private final JLabel labelWidth = new JLabel("Width");
    private final JLabel labelHeight = new JLabel("Height");
    private final JCheckBox boxSaveDataArea = new JCheckBox("Save only data area");
    private final JCheckBox boxAspectRatio = new JCheckBox("Keep Aspect Ratio");
    private SubPanel internalPanel;  
    private BasicFormatModel model;

    public BasicImageFormatPanel(BasicFormatModel model) 
    {
        setModel(model);
        buildLayout();
        initFieldsListener();
        initItemListener();
    }

    public BasicFormatModel getModel() 
    {
        return model;
    }

    public void setModel(BasicFormatModel modelNew) 
    {
        if (model != null) 
        {
            model.removePropertyChangeListener(this);
        }
        this.model = modelNew;
        pullModelProperties();
        modelNew.addPropertyChangeListener(this);
    }

    SubPanel getInternalPanel()
    {
        return internalPanel;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        boxAspectRatio.setEnabled(enabled);
        labelWidth.setEnabled(enabled);
        labelHeight.setEnabled(enabled);
        fieldWidth.setEnabled(enabled);
        fieldHeight.setEnabled(enabled);
    }

    public void specifyDimensions(Number widthNew, Number heightNew) 
    {
        model.specifyDimensions(widthNew, heightNew);
    }

    private void pullModelProperties() 
    {
        Double width = model.getWidth();
        Double height = model.getHeight();
        boolean saveDataArea = model.getSaveDataArea();
        boolean aspectConstant = model.isAspectRatioConstant();

        fieldWidth.setValue(width);
        fieldHeight.setValue(height);
        boxSaveDataArea.setSelected(saveDataArea);
        boxAspectRatio.setSelected(aspectConstant);
    }

    private void buildLayout() 
    {
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        internalPanel = new SubPanel();
        internalPanel.addComponent(labelWidth, 0, 2, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        internalPanel.addComponent(labelHeight, 0, 3, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0, 1);
        internalPanel.addComponent(boxSaveDataArea, 0, 0, 2, 1, GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, 0, 1);
        internalPanel.addComponent(boxAspectRatio, 0, 1, 2, 1, GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, 0, 1);
        internalPanel.addComponent(fieldWidth, 1, 2, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);
        internalPanel.addComponent(fieldHeight, 1, 3, 1, 1, GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, 0, 1);

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(internalPanel,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(internalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));	
    }

    private void initItemListener() 
    {
        boxSaveDataArea.addItemListener(this);
        boxAspectRatio.addItemListener(this);
    }

    private void initFieldsListener() 
    {
        final PropertyChangeListener fieldsListener = new PropertyChangeListener() 
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                Object source = evt.getSource();

                Double newVal = ((Number) evt.getNewValue()).doubleValue();

                if (source == fieldWidth) 
                {
                    model.specifyWidth(newVal);
                } 
                else if (source == fieldHeight) 
                {
                    model.specifyHeight(newVal);
                }

            }
        };

        fieldWidth.addPropertyChangeListener(NumericalField.VALUE_EDITED, fieldsListener);
        fieldHeight.addPropertyChangeListener(NumericalField.VALUE_EDITED, fieldsListener);
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();

        if(source == boxSaveDataArea)
        {
            boolean saveDataAreaNew = boxSaveDataArea.isSelected();
            model.setSaveDataArea(saveDataAreaNew);

        }
        else if (source == boxAspectRatio) 
        {
            boolean aspectConstantNew = boxAspectRatio.isSelected();
            model.setAspectRatioConstant(aspectConstantNew);
        } 
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if (name.equals(SAVE_DATA_AREA)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boolean oldVal = boxAspectRatio.isSelected();

            if (newVal != oldVal) 
            {
                boxSaveDataArea.setSelected(newVal);
            }
        } 
        else if (name.equals(SAVED_AREA_WIDTH)) 
        {
            Double newVal = ((Number) evt.getNewValue()).doubleValue();
            Double oldVal = fieldWidth.getValue().doubleValue();

            if (!newVal.equals(oldVal)) 
            {
                fieldWidth.setValue(newVal);
            }
        } 
        else if (name.equals(SAVED_AREA_HEIGHT)) 
        {
            Double newVal = ((Number) evt.getNewValue()).doubleValue();
            Double oldVal = fieldHeight.getValue().doubleValue();

            if (!newVal.equals(oldVal)) 
            {
                fieldHeight.setValue(newVal);
            }
        } 
        else if (name.equals(ASPECT_CONSTANT)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boolean oldVal = boxAspectRatio.isSelected();

            if (oldVal != newVal) 
            {
                boxAspectRatio.setSelected(newVal);
            }
        } 
        else if (name.equals(DIMENSIONS_SPECIFIED)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boxAspectRatio.setEnabled(newVal);
        }
    }

}
