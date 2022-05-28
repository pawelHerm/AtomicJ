
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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import atomicJ.gui.generalProcessing.BasicOperationModel;
import atomicJ.gui.generalProcessing.OperationModel;
import atomicJ.gui.imageProcessingActions.OperationListener;
import atomicJ.gui.rois.ROIRelativePosition;


public class OperationSimpleDialog <E extends OperationModel> extends JDialog implements
ItemListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final ApplyAction applyAction = new ApplyAction();

    private final JRadioButton buttonWholeImage = new JRadioButton("Image");
    private final JRadioButton buttonInside = new JRadioButton("ROI inside");
    private final JRadioButton buttonOutside = new JRadioButton("ROI outside");
    private final ButtonGroup buttonGroup = new ButtonGroup();

    private ROIRelativePosition initPosition;

    private E model;
    private final CustomProcessingListener processingListener = new CustomProcessingListener();

    public OperationSimpleDialog(Window parent, String title, boolean temporary)
    {
        this(parent, title, temporary, ModalityType.APPLICATION_MODAL);
    }

    public OperationSimpleDialog(Window parent, String title, boolean temporary, ModalityType modalityType)
    {
        super(parent, title, modalityType);
        setLayout(new BorderLayout());

        if(temporary)
        {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        initializeButtonGroup();
        initItemListener();
        initWindowListener();
    }

    private void initWindowListener()
    {
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {                
                E model = getModel();
                if(model != null)
                {
                    model.operationFinished();
                }
            }
        });
    }

    private void initializeButtonGroup()
    {
        buttonGroup.add(buttonWholeImage);
        buttonGroup.add(buttonInside);
        buttonGroup.add(buttonOutside);     
    }

    private void initItemListener()
    {
        buttonWholeImage.addItemListener(this);
        buttonInside.addItemListener(this);
        buttonOutside.addItemListener(this);
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        //we respond only to selection, because we don't want to respond to temporary situation
        //when no button is selected
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            if(source == buttonInside || source == buttonOutside || source == buttonWholeImage)
            {
                ROIRelativePosition roiRelativePositionNew = getROIRelativePosition();
                model.setPositionRelativeToROI(roiRelativePositionNew);     
            }
        }       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
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
        else if(OperationModel.ROIS_AVAILABLE.equals(property))
        {
            boolean roisEnabled = (boolean)evt.getNewValue();

            buttonInside.setEnabled(roisEnabled);
            buttonOutside.setEnabled(roisEnabled); 
        }
        else if(BasicOperationModel.APPLY_ENABLED.equals(property))
        {
            boolean applyEnabled = (boolean)evt.getNewValue();
            applyAction.setEnabled(applyEnabled);
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

    public void showDialog(E model)
    {
        setModel(model);        
        setVisible(true);		
    }

    protected E getModel()
    {
        return model;
    }

    protected void setModel(E modelNew)
    {
        clearOldModel();

        modelNew.addPropertyChangeListener(this);
        modelNew.addOperationListener(processingListener);
        this.model = modelNew;

        pullModelParameters();
        resetEditor();
    }

    protected void clearOldModel()
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
            this.model.removeOperationListener(processingListener);
        }      
    }

    protected void pullModelParameters()
    {
        this.initPosition = model.getROIPosition();
    }

    protected boolean getROIsEnabled()
    {
        return model.areROIsAvailable();
    }

    protected void resetEditor()
    {        
        if(model == null)
        {
            return;
        }

        boolean roisEnabled = getROIsEnabled();
        buttonInside.setEnabled(roisEnabled);
        buttonOutside.setEnabled(roisEnabled); 

        boolean applyEnabled = model.isApplyEnabled();
        applyAction.setEnabled(applyEnabled);

        setROIPosition(initPosition);
    }

    protected void setModelToInitialState()
    {
        model.setPositionRelativeToROI(initPosition);
    }

    protected void ok()
    {            
        model.ok();

        setVisible(false);        
    }

    protected void reset()
    {               
        setModelToInitialState();

        model.reset();
    }

    protected void cancel()
    {
        setModelToInitialState();
        model.reset();

        setVisible(false);
    }

    //I made this method final, because it is meant to be called from the constructors of the descendant class;
    //methods called in the constructor should not be overriden, because this may cause problems whwn
    //non-static fields are used in the method body
    protected final JPanel buildPanelROIRelative()
    {
        JPanel panelOperationRange = new JPanel();
        panelOperationRange.add(buttonWholeImage);
        panelOperationRange.add(buttonInside);
        panelOperationRange.add(buttonOutside);

        return panelOperationRange;
    }


    private class ApplyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyAction()
        {			
            putValue(MNEMONIC_KEY,KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            ok();
        }
    }

    private class CustomProcessingListener implements OperationListener
    {
        @Override
        public void finished()
        {
            setVisible(false);
        }

        @Override
        public void applied() {            
        }       
    }
}
