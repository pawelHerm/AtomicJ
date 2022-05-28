
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

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import atomicJ.resources.Resource;



public class ResourceSelectionWizard extends JDialog implements PropertyChangeListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final BackAction backAction = new BackAction();
    private final NextAction nextAction = new NextAction();
    private final FinishAction finishAction = new FinishAction();
    private final CancelAction cancelAction = new CancelAction();

    private final JButton buttonBack = new JButton(backAction);
    private final JButton buttonNext = new JButton(nextAction);
    private final JButton buttonFinish = new JButton(finishAction);
    private final JButton buttonCancel = new JButton(cancelAction);	

    private final JPanel panelPages = new JPanel(new CardLayout());
    private final JPanel panelControls = new JPanel(new CardLayout());

    private final List<WizardPage> pages = new ArrayList<>();

    private final ResourceSelectionWizardModel wizardModel;

    public ResourceSelectionWizard(Window parent, ResourceReceiver resourceReceiver)
    {
        super(parent, "Overlay assistant", ModalityType.APPLICATION_MODAL);

        this.wizardModel = new ResourceSelectionWizardModel(resourceReceiver);
        wizardModel.addPropertyChangeListener(this);
        pullModelProperties();

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	

        pages.addAll(wizardModel.getAvailableWizardPages());

        for(WizardPage page: pages)
        {
            panelPages.add(page.getView(), page.getIdentifier());
            panelControls.add(page.getControls(), page.getIdentifier());
        }

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                ResourceSelectionWizard.this.cancel();
            }
        });

        //Sets the layout for the componet
        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelControls, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(panelPages, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelControls)
                        .addComponent(panelPages, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        pack();
        setLocationRelativeTo(parent);
    }



    public boolean showDialog(Map<Resource, List<String>> allResources)
    {
        reset();
        wizardModel.setResources(allResources);
        setVisible(true);

        //this is to cleanup, it is important that the dialog is modal
        wizardModel.setResources(new LinkedHashMap<Resource, List<String>>());
        return wizardModel.isApproved();
    }

    public void setResourceReceiver(ResourceReceiver resourceReceiver)
    {
        wizardModel.setResourceReceiver(resourceReceiver);
    }

    private void reset()
    {
        wizardModel.reset();
        pullModelProperties();
    }

    public ResourceSelectionWizardModel getModel()
    {
        return wizardModel;
    }

    private void pullModelProperties()
    {
        boolean backEnabled = wizardModel.isBackEnabled();
        buttonBack.setEnabled(backEnabled);

        boolean nextEnabled = wizardModel.isNextEnabled();
        buttonNext.setEnabled(nextEnabled);	

        boolean finishEnabled = wizardModel.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);

        WizardPage currentPage = wizardModel.getCurrentWizardPage();
        updateWizard(currentPage);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(name.equals(WizardModelProperties.NEXT_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonNext.isEnabled();
            if(!(newVal == oldVal))
            {
                buttonNext.setEnabled(newVal);
            }
        }
        else if(name.equals(WizardModelProperties.BACK_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonBack.isEnabled();
            if(!(newVal == oldVal))
            {
                buttonBack.setEnabled(newVal);
            }
        }
        else if(name.equals(WizardModelProperties.FINISH_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonFinish.isEnabled();
            if(!(newVal == oldVal))
            {
                buttonFinish.setEnabled(newVal);
            }
        }
        else if(name.equals(WizardModelProperties.WIZARD_PAGE))
        {
            WizardPage currentPage = (WizardPage)evt.getNewValue();
            updateWizard(currentPage);
        }
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

        return panel;
    }

    private void updateWizard(WizardPage page)
    {
        String id = page.getIdentifier();

        CardLayout layoutPages = (CardLayout)(panelPages.getLayout());
        layoutPages.show(panelPages, id);

        CardLayout layoutControls = (CardLayout)(panelControls.getLayout());
        layoutControls.show(panelControls, id);

        String name = page.getTaskName();
        String description = page.getTaskDescription();

        labelTaskName.setText(name);
        labelTaskDescription.setText(description);

        Dimension preferredSize = getPreferredSize();
        int preferredWidth = (int)Math.rint(preferredSize.getWidth());
        int preferredHeight = (int)Math.rint(preferredSize.getHeight());

        Dimension actualSize = getSize();
        int actualWidth = (int)Math.rint(actualSize.getWidth());
        int actualHeight = (int)Math.rint(actualSize.getHeight());

        setSize(Math.max(preferredWidth, actualWidth), Math.max(preferredHeight,actualHeight));
    }

    private JPanel buildButtonPanel()
    {		
        buttonNext.setMargin(new Insets(0,7,0,7));
        buttonBack.setMargin(new Insets(0,7,0,7));

        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBack, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonNext, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFinish)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBack)
                .addComponent(buttonNext)
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(SwingConstants.HORIZONTAL, buttonFinish,buttonCancel);
        layout.linkSize(SwingConstants.HORIZONTAL, buttonBack, buttonNext);
        layout.linkSize(SwingConstants.VERTICAL, buttonFinish,buttonCancel,buttonBack, buttonNext);

        return panelButtons;
    }

    public void next()
    {		
        wizardModel.next();
    }

    public void back()
    {
        wizardModel.back();
    }

    public void finish()
    {
        setVisible(false);
        wizardModel.finish();
    }

    public void cancel()
    {	
        setVisible(false);
        wizardModel.cancel();
    }

    @Override
    public void publishErrorMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishWarningMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishInformationMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.informationIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void clearMessage() 
    {
        labelMessage.setIcon(null);
        labelMessage.setText(null);
        pack();
        validate();
    }

    private class BackAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BackAction()
        {			
            putValue(NAME,"<<   Back");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            back();
        }
    }

    private class NextAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NextAction()
        {			
            putValue(NAME,"Next   >>");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            next();
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
        public void actionPerformed(ActionEvent event)
        {
            finish();
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
        public void actionPerformed(ActionEvent event)
        {
            finish();
        }
    }
}
