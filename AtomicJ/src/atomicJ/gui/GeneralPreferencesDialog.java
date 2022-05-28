
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GeneralPreferencesDialog extends JDialog implements ChangeListener
{
    private static final long serialVersionUID = 1L;

    private final int numberOfProcessors = GeneralPreferences.GENERAL_PREFERENCES.getMaximumTaskNumber();
    private int initTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();
    private int taskNumber = initTaskNumber;

    private final ApplyToAllAction applyToAllAction = new ApplyToAllAction();
    private final JButton buttonOK = new JButton(applyToAllAction);
    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonCancel = new JButton(new CancelAction());

    private final JLabel labelAvailablePrecessors = new JLabel(Integer.toString(initTaskNumber)); 

    private final JSpinner spinnerTaskNumber = new JSpinner(new SpinnerNumberModel(initTaskNumber, 1, numberOfProcessors, 1));

    public GeneralPreferencesDialog(Window parent, String title)
    {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();
        pack();
        setLocationRelativeTo(parent);
    }

    private void setParametersToInitial()
    {
        this.taskNumber = this.initTaskNumber;
    }

    private void pullReceiverParameters()
    {
        this.initTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();
    }

    private void initChangeListener()
    {
        spinnerTaskNumber.addChangeListener(this);
    }	

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerTaskNumber)
        {			
            this.taskNumber = ((SpinnerNumberModel)spinnerTaskNumber.getModel()).getNumber().intValue();
        }
    }

    public void ensureConsistencyWithReceiver()
    {
        pullReceiverParameters();
        setParametersToInitial();
        resetEditor();
    }

    public void showDialog()
    {
        ensureConsistencyWithReceiver();
        setVisible(true);		
    }

    private void resetReceiver()
    {	  
        GeneralPreferences.GENERAL_PREFERENCES.setTaskNumber(initTaskNumber);
    }

    private void resetEditor()
    {		
        spinnerTaskNumber.setValue(initTaskNumber);
    }

    private void apply()
    {
        GeneralPreferences.GENERAL_PREFERENCES.setTaskNumber(taskNumber);
        setVisible(false);
    }

    private void reset()
    {
        setParametersToInitial();
        resetReceiver();
        resetEditor();
    }

    private void cancel()
    {
        reset();
        setVisible(false);
    }

    private JPanel buildMainPanel()
    {	
        JPanel outerPanel = new JPanel();

        SubPanel innerPanel = new SubPanel(); 

        labelAvailablePrecessors.setFont(labelAvailablePrecessors.getFont().deriveFont(Font.BOLD));

        innerPanel.addComponent(new JLabel("Available processors "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(labelAvailablePrecessors, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(new JLabel("Used for parallel processing "), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(spinnerTaskNumber, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);      

        innerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        outerPanel.add(innerPanel);

        return outerPanel;
    }

    private JPanel buildButtonPanel()
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

    private class ApplyToAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyToAllAction()
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
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
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
