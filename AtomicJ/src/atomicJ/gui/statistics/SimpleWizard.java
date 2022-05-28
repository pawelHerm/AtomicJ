
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

package atomicJ.gui.statistics;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.Validation;

import static atomicJ.gui.WizardModelProperties.*;

public class SimpleWizard extends JDialog implements PropertyChangeListener, ActionListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private static final String BACK_ACTION_COMMAND = "BackCommand";
    private static final String NEXT_ACTION_COMMAND = "NextCommand";
    private static final String FINISH_ACTION_COMMAND = "FinishCommand";
    private static final String CANCEL_ACTION_COMMAND = "CancelCommand";

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JButton buttonBack = new JButton("< Back");
    private final JButton buttonNext = new JButton("Next >");
    private final JButton buttonFinish = new JButton("Finish");
    private final JButton buttonCancel = new JButton("Cancel");

    private SimpleWizardModel wizardModel;

    private final JPanel pagePanel = new JPanel();

    public SimpleWizard(SimpleWizardModel wizardModel, String name, Window parent)
    {
        super(parent, name, ModalityType.APPLICATION_MODAL);

        setWizardModel(wizardModel);
        pullModelProperties();	

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(false);

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	

        layout.setHorizontalGroup
        (
                layout.createParallelGroup()
                .addComponent(panelButtons, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(pagePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)	
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(pagePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        pack();
        setLocationRelativeTo(parent);
    }

    public void showDialog()
    {	
        setVisible(true);
    }

    public void setWizardModel(SimpleWizardModel modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(wizardModel != null)
        {
            wizardModel.removePropertyChangeListener(this);
        }
        this.wizardModel = modelNew;
        modelNew.addPropertyChangeListener(this);

        wizardModel.setWizard(this);

        setPagePanelsConsistentWithModel(modelNew);
        setPagePanel(modelNew.getCurrentPageModel());

        pullModelProperties();
    }

    private void setPagePanelsConsistentWithModel(SimpleWizardModel modelNew)
    {
        pagePanel.removeAll();
        CardLayout layout = new CardLayout();
        pagePanel.setLayout(layout);

        for(RichWizardPage pageModel: modelNew.getAllPageModels())
        {
            String pageModelName = pageModel.getName();
            JPanel pageView = pageModel.getPageView();
            pagePanel.add(pageView, pageModelName);
        }
    }

    private void setPagePanel(RichWizardPage pageModel)
    {
        CardLayout layout = (CardLayout)pagePanel.getLayout();
        String pageModelName = pageModel.getName();

        layout.show(pagePanel, pageModelName);		
    }

    private void pullModelProperties()
    {
        boolean backEnabled = wizardModel.isBackEnabled();
        buttonBack.setEnabled(backEnabled);

        boolean nextEnabled = wizardModel.isNextEnabled();
        buttonNext.setEnabled(nextEnabled);

        boolean finishEnabled = wizardModel.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);

        String task = wizardModel.getTask();
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

        buttonBack.addActionListener(this);
        buttonBack.setActionCommand(BACK_ACTION_COMMAND);
        buttonBack.setMnemonic(KeyEvent.VK_B);
        buttonNext.addActionListener(this);
        buttonNext.setActionCommand(NEXT_ACTION_COMMAND);
        buttonNext.setMnemonic(KeyEvent.VK_N);
        buttonFinish.addActionListener(this);
        buttonFinish.setActionCommand(FINISH_ACTION_COMMAND);
        buttonFinish.setMnemonic(KeyEvent.VK_F);
        buttonCancel.addActionListener(this);
        buttonCancel.setActionCommand(CANCEL_ACTION_COMMAND);
        buttonCancel.setMnemonic(KeyEvent.VK_C);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBack).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonNext).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonFinish).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBack)
                .addComponent(buttonNext)
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonBack, buttonNext, buttonFinish, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    @Override
    public void publishErrorMessage(String message) 
    {		
    }

    @Override
    public void publishWarningMessage(String message) 
    {	
    }

    @Override
    public void publishInformationMessage(String message) 
    {
    }

    @Override
    public void clearMessage() 
    {	
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        String command = evt.getActionCommand();
        if(command.equals(BACK_ACTION_COMMAND))
        {
            wizardModel.back();
        }
        else if(command.equals(NEXT_ACTION_COMMAND))
        {
            wizardModel.next();
        }
        else if(command.equals(FINISH_ACTION_COMMAND))
        {
            wizardModel.finish();
        }
        else if(command.equals(CANCEL_ACTION_COMMAND))
        {
            setVisible(false);
        }				
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(name.equals(BACK_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();			
            buttonBack.setEnabled(newVal);
        }
        else if(name.equals(NEXT_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();			
            buttonNext.setEnabled(newVal);			
        }
        else if(name.equals(FINISH_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();
            buttonFinish.setEnabled(newVal);
        }
        else if(name.equals(TASK))
        {
            String newVal = (String)evt.getNewValue();
            labelTaskDescription.setText(newVal);
        }
        else if(name.equals(WIZARD_PAGE))
        {
            RichWizardPage newPageModel = (RichWizardPage)evt.getNewValue();
            setPagePanel(newPageModel);
        }
    }
}
