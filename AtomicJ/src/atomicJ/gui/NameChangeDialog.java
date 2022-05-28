
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultFormatter;

public class NameChangeDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final OKAction okAction = new OKAction();

    private final JButton buttonOK = new JButton(okAction);
    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonCancel = new JButton(new CancelAction());

    private Comparable<?> initName;
    private Comparable<?> name;

    private final JLabel labelOldName = new JLabel();
    private final JFormattedTextField fieldName = new JFormattedTextField(new DefaultFormatter());

    private NameChangeModel model;

    boolean approved = false;


    public NameChangeDialog(Window parent, String title)
    {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        checkIfInputProvided();

        initFieldsListener();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        pack();
        setLocationRelativeTo(parent);
    }

    private void setParametersToInitial()
    {
        this.name = this.initName;
    }

    private void pullModelParameters()
    {
        this.initName = model.getName();
    }


    private void initFieldsListener()
    {
        PropertyChangeListener fieldsListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                NameChangeDialog.this.name = evt.getNewValue().toString();
                checkIfInputProvided();
            }
        };

        fieldName.addPropertyChangeListener("value", fieldsListener);

    }


    public Comparable<?> getNewKey()
    {
        return name;
    }


    public void ensureConsistencyWithReceiver()
    {
        pullModelParameters();
        setParametersToInitial();
        resetEditor();
    }


    private void resetReceiver()
    {
        //receiver.setSeriesKey(key);
    }

    private void resetEditor()
    {		
        labelOldName.setText(initName.toString());
        fieldName.setValue(name.toString());

        checkIfInputProvided();
    }


    public boolean showDialog(NameChangeModel receiver)
    {
        this.approved = false;
        this.model = receiver;

        ensureConsistencyWithReceiver();
        setVisible(true);

        this.model = null;
        return approved;
    }


    private void approve()
    {
        this.approved = true;
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
        this.approved = false;
        reset();
        setVisible(false);
    }

    private void checkIfInputProvided()
    {
        boolean provided = (name != null && !"".equals(name));
        okAction.setEnabled(provided);
    }

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel(); 

        DefaultFormatter formatter = (DefaultFormatter)fieldName.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        labelOldName.setFont(labelOldName.getFont().deriveFont(Font.BOLD));

        mainPanel.addComponent(new JLabel("Current name "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(labelOldName, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("New name "), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(fieldName, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);      

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
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

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {			
            putValue(MNEMONIC_KEY,KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            approve();
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
