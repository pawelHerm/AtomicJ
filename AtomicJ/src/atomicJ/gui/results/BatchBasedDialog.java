
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

package atomicJ.gui.results;

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
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import atomicJ.gui.SubPanel;
import atomicJ.sources.IdentityTag;


public class BatchBasedDialog <E extends BatchBasedModel<?,?>> extends JDialog implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final ApplyAction applyAction = new ApplyAction();
    private final ResetAction resetAction = new ResetAction();
    private final CancelAction cancelAction = new CancelAction();

    private final JButton buttonOK = new JButton(applyAction);
    private final JButton buttonReset = new JButton(resetAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final JCheckBox boxRestrictToSelectedPacks = new JCheckBox("Restrict to selected results");

    private final JLabel labelBatchIdentity = new JLabel("Selected batch");
    private final JComboBox<IdentityTag> comboBatchIdentity = new JComboBox<>();

    private boolean initRestrictToSelectedPacks;
    private IdentityTag initSelectedBatchIdentity;

    private E model;

    public BatchBasedDialog(Window parent, String title, ModalityType modality, boolean temporary)
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
        comboBatchIdentity.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                IdentityTag id = comboBatchIdentity.getItemAt(comboBatchIdentity.getSelectedIndex());
                model.setSelectedBatchIdentity(id);                
            }
        });

        boxRestrictToSelectedPacks.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                model.setRestrictToSelection(selected);    
            }
        });
    }

    protected void setResultsAvailable(boolean available)
    {
        labelBatchIdentity.setEnabled(available);
        comboBatchIdentity.setEnabled(available);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String property = evt.getPropertyName();

        if(BatchBasedModel.RESTRICT_TO_SELECTED_PACKS.equals(property))
        {
            boolean restrictedNew = (boolean)evt.getNewValue();
            boxRestrictToSelectedPacks.setSelected(restrictedNew);
        }
        else if(BatchBasedModel.BATCH_IDENTITY.equals(property))
        {
            Object identityOld = comboBatchIdentity.getSelectedItem();
            String identityNew = (String)evt.getNewValue();

            if(!Objects.equals(identityOld, identityNew))
            {
                comboBatchIdentity.setSelectedItem(identityNew);
            }
        }
        else if(BatchBasedModel.RESULTS_AVAILABLE.equals(property))
        {                  
            boolean available = (boolean)evt.getNewValue();
            setResultsAvailable(available);
        }
        else if(BatchBasedModel.AVAILABLE_BATCH_IDENTITIES.equals(property))
        {
            ComboBoxModel<IdentityTag> comboModel = new DefaultComboBoxModel<>(model.getAvailableBatchIdentities().toArray(new IdentityTag[]{}));
            comboBatchIdentity.setModel(comboModel);
        }
        else if(BatchBasedModel.SELECTED_PACKS_AVAILABLE.equals(property))
        {
            boolean selectedPacksAvailable = (boolean)evt.getNewValue();
            boxRestrictToSelectedPacks.setEnabled(selectedPacksAvailable);
        }
        else if(BatchBasedModel.APPLY_ENABLED.equals(property))
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
        this.initRestrictToSelectedPacks = model.isRestrictToSelection();
        this.initSelectedBatchIdentity = model.getSelectedBatchIdentity();
    }

    protected void resetEditor()
    {        
        if(model == null)
        {
            return;
        }


        boolean applyEnabled = model.isApplyEnabled();
        applyAction.setEnabled(applyEnabled);

        boolean selectedPacksAvailable = model.isSelectedPacksAvailable();
        boxRestrictToSelectedPacks.setEnabled(selectedPacksAvailable);

        boolean resultsAvailable = model.isResultsAvailable();
        setResultsAvailable(resultsAvailable);

        ComboBoxModel<IdentityTag> comboModel = new DefaultComboBoxModel<>(model.getAvailableBatchIdentities().toArray(new IdentityTag[]{}));
        comboBatchIdentity.setModel(comboModel);       
        comboBatchIdentity.setSelectedItem(initSelectedBatchIdentity);

        boxRestrictToSelectedPacks.setSelected(initRestrictToSelectedPacks);
    }

    protected void setModelToInitialState()
    {
        model.setRestrictToSelection(initRestrictToSelectedPacks);
        model.setSelectedBatchIdentity(initSelectedBatchIdentity);
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
    //methods called in the constructor should not be overriden, because this may cause problems when
    //non-static fields are used in the method body
    protected final JPanel buildPanelROIRelative()
    {
        SubPanel panelOperationRange = new SubPanel();


        SubPanel panelBatch = new SubPanel();

        panelBatch.addComponent(labelBatchIdentity, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelBatch.addComponent(comboBatchIdentity, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelOperationRange.addComponent(panelBatch, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelOperationRange.addComponent(boxRestrictToSelectedPacks, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1); 

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
