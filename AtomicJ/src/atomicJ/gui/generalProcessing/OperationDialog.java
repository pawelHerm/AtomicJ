
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

package atomicJ.gui.generalProcessing;

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import atomicJ.gui.SubPanel;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.sources.IdentityTag;


public class OperationDialog <E extends OperationModel> extends BasicOperationDialog<E> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final JRadioButton buttonWholeImage = new JRadioButton("All");
    private final JRadioButton buttonInside = new JRadioButton("ROI inside");
    private final JRadioButton buttonOutside = new JRadioButton("ROI outside");
    private final ButtonGroup buttonGroup = new ButtonGroup();

    private final JLabel labelROIIdentity = new JLabel("Selected ROI");
    private final JComboBox<IdentityTag> comboROIIdentity = new JComboBox<>();

    private ROIRelativePosition initPosition;
    private IdentityTag initROIIdentity;


    public OperationDialog(Window parent, String title, boolean temporary)
    {
        this(parent, title, temporary, ModalityType.APPLICATION_MODAL);
    }

    public OperationDialog(Window parent, String title, boolean temporary, ModalityType modalityType)
    {
        super(parent, title, temporary, modalityType);

        initializeButtonGroup();
        initItemListener();
    }

    private void initializeButtonGroup()
    {
        buttonGroup.add(buttonWholeImage);
        buttonGroup.add(buttonInside);
        buttonGroup.add(buttonOutside);     
    }

    private void initItemListener()
    {
        comboROIIdentity.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                IdentityTag idTag = (IdentityTag)comboROIIdentity.getSelectedItem();
                getModel().setSelectedROIIdentity(idTag);                
            }
        });

        //we respond only to selection, because we don't want to respond to temporary situation
        //when no button is selected
        buttonWholeImage.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    ROIRelativePosition roiRelativePositionNew = getROIRelativePosition();
                    getModel().setPositionRelativeToROI(roiRelativePositionNew);    
                }
            }
        });

        buttonInside.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    ROIRelativePosition roiRelativePositionNew = getROIRelativePosition();
                    getModel().setPositionRelativeToROI(roiRelativePositionNew);    
                }
            }
        });

        buttonOutside.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    ROIRelativePosition roiRelativePositionNew = getROIRelativePosition();
                    getModel().setPositionRelativeToROI(roiRelativePositionNew);    
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        E model = getModel();

        String property = evt.getPropertyName();

        if(OperationModel.ROI_RELATIVE_POSITION.equals(property))
        {
            ROIRelativePosition positionOld = getROIRelativePosition();
            ROIRelativePosition positionNew = (ROIRelativePosition)evt.getNewValue();

            if(!Objects.equals(positionOld, positionNew))
            {
                setROIPosition(positionNew);
            }
        }
        else if(OperationModel.ROI_SELECTED.equals(property))
        {
            Object identityOld = comboROIIdentity.getSelectedItem();
            IdentityTag identityNew = (IdentityTag)evt.getNewValue();

            if(!Objects.equals(identityOld, identityNew))
            {
                comboROIIdentity.setSelectedItem(identityNew);
            }
        }
        else if(OperationModel.ROI_SELECTION_ENABLED.equals(property))
        {                  
            boolean roiSelectionEnabled = (boolean)evt.getNewValue();

            labelROIIdentity.setEnabled(roiSelectionEnabled);
            comboROIIdentity.setEnabled(roiSelectionEnabled);
        }
        else if(OperationModel.AVAILABLE_ROI_IDENTITIES.equals(property))
        {
            ComboBoxModel<IdentityTag> comboModel = new DefaultComboBoxModel<>(model.getAvailableROIIdentities().toArray(new IdentityTag[]{}));
            comboROIIdentity.setModel(comboModel);

            comboROIIdentity.setSelectedItem(model.getSelectedROIIdentity());
        }
        else if(OperationModel.ROIS_AVAILABLE.equals(property))
        {
            boolean roisEnabled = (boolean)evt.getNewValue();

            buttonInside.setEnabled(roisEnabled);
            buttonOutside.setEnabled(roisEnabled); 
        }
    }

    private ROIRelativePosition getROIRelativePosition()
    {
        ROIRelativePosition position = null;

        if(buttonWholeImage.isSelected())
        {
            position = ROIRelativePosition.EVERYTHING;
        }
        else if(buttonInside.isSelected())
        {
            position = ROIRelativePosition.INSIDE;
        }
        else if(buttonOutside.isSelected())
        {
            position = ROIRelativePosition.OUTSIDE;
        }

        return position;
    }

    private ROIRelativePosition setROIPosition(ROIRelativePosition position)
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            buttonGroup.setSelected(buttonWholeImage.getModel(), true);
        }
        else if(ROIRelativePosition.INSIDE.equals(position))
        {
            buttonGroup.setSelected(buttonInside.getModel(), true);
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            buttonGroup.setSelected(buttonOutside.getModel(), true);
        }

        return position;
    }


    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        E model = getModel();

        this.initPosition = model.getROIPosition();
        this.initROIIdentity = model.getSelectedROIIdentity();
    }

    @Override
    protected void resetEditor()
    {  
        super.resetEditor();

        E model = getModel();

        if(model == null)
        {
            return;
        }

        boolean roisEnabled = model.areROIsAvailable();

        buttonInside.setEnabled(roisEnabled);
        buttonOutside.setEnabled(roisEnabled); 

        boolean roiSelectionEnabled = model.isROISelectionEnabled();

        labelROIIdentity.setEnabled(roiSelectionEnabled);
        comboROIIdentity.setEnabled(roiSelectionEnabled);


        ComboBoxModel<IdentityTag> comboModel = new DefaultComboBoxModel<>(model.getAvailableROIIdentities().toArray(new IdentityTag[]{}));
        comboROIIdentity.setModel(comboModel);

        comboROIIdentity.setSelectedItem(initROIIdentity);
        setROIPosition(initPosition);
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        E model = getModel();

        model.setPositionRelativeToROI(initPosition);
        model.setSelectedROIIdentity(initROIIdentity);
    }

    //I made this method final, because it is meant to be called from the constructors of the descendant class;
    //methods called in the constructor should not be overridden, because this may cause problems when
    //non-static fields are used in the method body
    protected final JPanel buildPanelROIRelative()
    {
        SubPanel panelOperationRange = new SubPanel();

        panelOperationRange.addComponent(buttonWholeImage, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1); 
        panelOperationRange.addComponent(buttonInside, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelOperationRange.addComponent(buttonOutside, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        SubPanel panelROI = new SubPanel();

        panelROI.addComponent(labelROIIdentity, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelROI.addComponent(comboROIIdentity, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelOperationRange.addComponent(panelROI, 0, 2, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panelOperationRange;
    }
}
