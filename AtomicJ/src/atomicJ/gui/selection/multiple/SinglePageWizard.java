
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

package atomicJ.gui.selection.multiple;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPage;
import atomicJ.gui.WizardPageModel;
import atomicJ.utilities.Validation;

public abstract class SinglePageWizard<E extends WizardPage, V extends WizardPageModel> implements PropertyChangeListener, MessageDisplayer
{
    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JButton buttonFinish = new JButton(new FinishAction());
    private final JButton buttonCancel = new JButton(new CancelAction());

    private final E page;
    private V model;

    private final JDialog mainDialog;

    private boolean approved = false;


    //The interface WizardPage do not include a method setModel(). This is a problem for the class SinglePageModel,
    //as it anticipates that the model of WizardPage can be set 

    //there are two solutions - we can either (A) add the method setModel() to the interface WizardPage,
    //or (B) we can introduce a generic subinterface of WizardPage, with the methot setModel().

    //There are two drawbacks of the solution A : (1) the interface WizardPage would have to be made generic,
    //(2) in some, rather rare, cases changing of wizard page model may be ill-advised 

    //Solution B makes the inheritance structure even more complex (it is already too complex)

    public SinglePageWizard(Window parent, E page, String title)
    {
        mainDialog = new JDialog(parent, title, ModalityType.APPLICATION_MODAL);

        this.page = page;

        Container content = mainDialog.getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(false);

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	

        SubPanel panelSelectionFull = new SubPanel();
        panelSelectionFull.addComponent(page.getControls(), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);
        panelSelectionFull.addComponent(page.getView(), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);

        layout.setHorizontalGroup
        (
                layout.createParallelGroup()
                .addComponent(panelButtons, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelSelectionFull, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)	
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(panelSelectionFull, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

    }

    public boolean showDialog(V modelNew)
    {	
        this.approved = false;

        setModel(modelNew);

        mainDialog.pack();
        mainDialog.setLocationRelativeTo(mainDialog.getParent());
        mainDialog.setVisible(true);

        return approved;
    }

    public void dispose()
    {
        this.mainDialog.dispose();
    }

    private void setModel(V modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }
        modelNew.addPropertyChangeListener(this);

        V modelOld = this.model;
        this.model = modelNew;

        handleChangeOfModel(page, modelOld, modelNew);
        pullModelProperties();
    }

    protected abstract void handleChangeOfModel(E wizardPage, V modelOld, V modelNew);

    protected void pullModelProperties()
    {		
        boolean finishEnabled = model.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);

        String task = model.getTaskDescription();
        labelTaskDescription.setText(task);
    }

    private JPanel buildTaskInformationPanel()
    {
        SubPanel panel = new SubPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));	

        Color color = UIManager.getColor("Label.background");
        panel.setBackground(color);
        labelTaskName.setBackground(color);
        labelTaskDescription.setBackground(color);
        labelMessage.setBackground(color);

        Font font = UIManager.getFont("TextField.font");
        labelTaskName.setFont(font.deriveFont(Font.BOLD));
        labelTaskDescription.setFont(font);

        panel.addComponent(labelTaskName, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelTaskDescription, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelMessage, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        panel.setBorder(BorderFactory.createLoweredBevelBorder());

        return panel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonFinish).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()

                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonFinish, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    @Override
    public void publishErrorMessage(String message) 
    {}

    @Override
    public void publishWarningMessage(String message) 
    {}

    @Override
    public void publishInformationMessage(String message) 
    {}

    @Override
    public void clearMessage() {}

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(WizardModelProperties.FINISH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            buttonFinish.setEnabled(newVal);
        }
        else if(WizardModelProperties.TASK.equals(property))
        {
            String newVal = (String)evt.getNewValue();
            labelTaskDescription.setText(newVal);
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(NAME, "Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            SinglePageWizard.this.approved = false;
            model.cancel();
            mainDialog.setVisible(false);
        }       
    }

    private class FinishAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FinishAction()
        {
            putValue(NAME, "Finish");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            SinglePageWizard.this.approved = true;
            model.finish();
            mainDialog.setVisible(false);
        }       
    }
}
