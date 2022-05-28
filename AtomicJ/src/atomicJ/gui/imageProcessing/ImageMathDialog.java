
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import atomicJ.gui.ResourceCellRenderer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.generalProcessing.OperationDialog;
import atomicJ.sources.Channel2DSource;


public class ImageMathDialog extends OperationDialog <ImageMathModel> implements ItemListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private boolean initCreateNewImage = false;
    private ImageMathOperation initOperation = ImageMathOperation.ADD;

    private final JCheckBox boxNewImage = new JCheckBox("New image");

    private final JComboBox<ImageMathOperation> comboOperation = new JComboBox<>(ImageMathOperation.values());
    private final JComboBox<Channel2DSource<?>> comboSource = new JComboBox<>();
    private final JComboBox<String> comboIdentifier = new JComboBox<>();

    public ImageMathDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

        comboSource.setRenderer(new ResourceCellRenderer(true));

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
        boxNewImage.addItemListener(this);
        comboOperation.addItemListener(this);
        comboSource.addItemListener(this);
        comboIdentifier.addItemListener(this);
    }


    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        ImageMathModel model = getModel();

        if(source == boxNewImage)
        {
            boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
            model.setCreateNewImage(selected);
        }
        else if(source == comboOperation)
        {
            ImageMathOperation operationNew = (ImageMathOperation) comboOperation.getSelectedItem();        
            model.setOperation(operationNew);
        }
        else if(source == comboIdentifier)
        {
            String identifier = (String)comboIdentifier.getSelectedItem();
            model.setIdentifier(identifier);
        }           
        else if(source == comboSource)
        {            
            Channel2DSource<?> densitySource = (Channel2DSource<?>)comboSource.getSelectedItem();
            model.setSource(densitySource);
        }
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();         

        SubPanel panelActiveArea = new SubPanel();
        SubPanel panelMathOperation = new SubPanel();

        panelActiveArea.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);

        mainPanel.addComponent(panelActiveArea, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelMathOperation, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);

        panelMathOperation.addComponent(new JLabel("Operation"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelMathOperation.addComponent(comboOperation, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        JLabel labelSecondImage = new JLabel("Second image");

        panelMathOperation.addComponent(labelSecondImage, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        //        panelMathOperation.addComponent(boxNewImage, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);

        panelMathOperation.addComponent(new JLabel("File"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelMathOperation.addComponent(comboSource, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, .1, 1);
        panelMathOperation.addComponent(new JLabel("Type"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelMathOperation.addComponent(comboIdentifier, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, .1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }


    protected void clearOldModel(ImageMathModel model)
    {
        super.clearOldModel();

    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        ImageMathModel model = getModel();

        this.initCreateNewImage = model.isCreateNewImage();
        this.initOperation = model.getOperation();

        updateAvailableSources();
        updateAvailableIdentifiers();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        ImageMathModel model = getModel();

        model.setCreateNewImage(initCreateNewImage);
        model.setOperation(initOperation);    
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        boxNewImage.setSelected(initCreateNewImage);
        comboOperation.setSelectedItem(initOperation);

        pack();
    }

    private void updateAvailableSources()
    {
        ImageMathModel model = getModel();
        List<Channel2DSource<?>> availableSources = model.getAvailableSources();

        comboSource.setModel(new DefaultComboBoxModel<Channel2DSource<?>>(availableSources.toArray(new Channel2DSource[] {})));       
    }

    private void updateAvailableIdentifiers()
    {
        ImageMathModel model = getModel();

        List<String> availableIdentifiers = model.getAvailableIdentifiers();

        comboIdentifier.setModel(new DefaultComboBoxModel<>(availableIdentifiers.toArray(new String[] {})));       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(ImageMathModel.SOURCE.equals(property))
        {
            Channel2DSource<?> valueNew = (Channel2DSource<?>)evt.getNewValue();
            Channel2DSource<?> valueOld = (Channel2DSource<?>)comboSource.getSelectedItem(); 

            if(valueNew != valueOld)
            {
                comboSource.setSelectedItem(valueNew);
            }

            updateAvailableIdentifiers();
        }
        else if(ImageMathModel.IDENTIFIER.equals(property))
        {
            String valueNew = (String)evt.getNewValue();
            String valueOld = (String)comboIdentifier.getSelectedItem(); 

            if(!Objects.equals(valueNew, valueOld))
            {
                comboIdentifier.setSelectedItem(valueNew);
            }
        }
        else if(ImageMathModel.MATH_OPERATION.equals(property))
        {
            ImageMathOperation valueNew = (ImageMathOperation)evt.getNewValue();
            ImageMathOperation valueOld = (ImageMathOperation)comboOperation.getSelectedItem();
            if(!Objects.equals(valueNew, valueOld))
            {
                comboOperation.setSelectedItem(valueNew);
            }
        }        
        else if(ImageMathModel.CREATE_NEW_IMAGE.equals(property))
        {
            boolean valueNew = (boolean)evt.getNewValue();
            boolean valueOld = boxNewImage.isSelected();

            if(valueOld != valueNew)
            {
                boxNewImage.setSelected(valueNew);  
            }
        }
    }
}
