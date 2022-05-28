
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

package atomicJ.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;


public class ResourcesBasedDialog <E extends ResourceBasedModel<?>> extends JDialog implements
PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final ApplyAction applyAction = new ApplyAction();
    private final ResetAction resetAction = new ResetAction();
    private final CancelAction cancelAction = new CancelAction();

    private final JButton buttonOK = new JButton(applyAction);
    private final JButton buttonReset = new JButton(resetAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final JCheckBox boxRestrictToSelection = new JCheckBox("Restrict to selected results");

    private boolean initRestrictToSelection;

    private E model;

    public ResourcesBasedDialog(Window parent, String title, ModalityType modality, boolean temporary)
    {
        super(parent, title, modality);
        setLayout(new BorderLayout());

        if(temporary)
        {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        initItemListener();
    }


    private void initItemListener()
    {

        boxRestrictToSelection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                model.setRestrictToSelection(selected);    
            }
        });
    }

    protected void setResourcesAvailable(boolean available)
    {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String property = evt.getPropertyName();

        if(ResourceBasedModel.RESTRICT_TO_SELECTION.equals(property))
        {
            boolean restrictedNew = (boolean)evt.getNewValue();
            boxRestrictToSelection.setSelected(restrictedNew);
        }
        else if(ResourceBasedModel.DATA_AVAILABLE.equals(property))
        {                  
            boolean available = (boolean)evt.getNewValue();
            setResourcesAvailable(available);
        }
        else if(ResourceBasedModel.SELECTED_DATA_AVAILABLE.equals(property))
        {
            boolean selectedPacksAvailable = (boolean)evt.getNewValue();
            boxRestrictToSelection.setEnabled(selectedPacksAvailable);
        }
        else if(ResourceBasedModel.APPLY_ENABLED.equals(property))
        {
            boolean applyEnabled = (boolean)evt.getNewValue();
            applyAction.setEnabled(applyEnabled);
        }
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
        this.model = modelNew;

        pullModelParameters();
        resetEditor();
    }

    protected void clearOldModel()
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }      
    }

    protected void pullModelParameters()
    {
        this.initRestrictToSelection = model.isRestrictToSelection();
    }

    protected void resetEditor()
    {        
        if(model == null)
        {
            return;
        }


        boolean applyEnabled = model.isApplyEnabled();
        applyAction.setEnabled(applyEnabled);

        boolean selectedDataAvailable = model.isSelectedDataAvailable();
        boxRestrictToSelection.setEnabled(selectedDataAvailable);

        boolean resourcesAvailable = model.isDataAvailable();
        setResourcesAvailable(resourcesAvailable);

        boxRestrictToSelection.setSelected(initRestrictToSelection);
    }

    protected void setModelToInitialState()
    {
        model.setRestrictToSelection(initRestrictToSelection);
    }

    protected void apply()
    {            
        model.apply();
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
        SubPanel panelOperationRange = new SubPanel();


        SubPanel panelBatch = new SubPanel();

        panelOperationRange.addComponent(panelBatch, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelOperationRange.addComponent(boxRestrictToSelection, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1); 

        return panelOperationRange;
    }

    //I made this method final, because it is meant to be called from the constructors of the descendant class;
    //methods called in the constructor should not be overriden, because this may cause problems whwn
    //non-static fields are used in the method body
    protected final JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonReset)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonReset, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
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
            apply();
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            reset();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        }
    }
}
