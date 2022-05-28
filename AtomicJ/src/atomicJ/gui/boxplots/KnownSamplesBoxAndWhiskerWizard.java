
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

package atomicJ.gui.boxplots;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import atomicJ.data.SampleCollection;
import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPage;
import atomicJ.gui.selection.multiple.SampleSelectionModel;

import static atomicJ.gui.WizardModelProperties.*;

public class KnownSamplesBoxAndWhiskerWizard implements PropertyChangeListener, MessageDisplayer
{
    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final Action finishAction = new FinishAction();
    private final Action cancelAction = new CancelAction();

    private final JPanel panelPages = new JPanel(new CardLayout());
    private final JPanel panelControls = new JPanel(new CardLayout());

    private final MultipleSelectionWizardPage<String> pageSampleSelection;
    private final List<WizardPage> pages = new ArrayList<>();

    private final KnownSamplesBoxSampleWizardModel wizardModel;
    private final JDialog mainDialog;

    public KnownSamplesBoxAndWhiskerWizard(BoxAndWhiskersDestination desination)
    {
        mainDialog = new JDialog(desination.getBoxPlotPublicationSite(), "Box-and-whisker plot assistant", ModalityType.APPLICATION_MODAL);

        List<SampleCollection> collections = new ArrayList<>();
        SampleSelectionModel sampleSelectionModel = new SampleSelectionModel(collections, "Select samples", true, true);

        this.wizardModel = new KnownSamplesBoxSampleWizardModel(desination, sampleSelectionModel);
        wizardModel.addPropertyChangeListener(this);

        this.pageSampleSelection = new MultipleSelectionWizardPage<>("SampleSelection",sampleSelectionModel, true);
        pages.add(pageSampleSelection);


        pullModelProperties();

        panelPages.add(pageSampleSelection.getView(), pageSampleSelection.getIdentifier());
        panelControls.add(pageSampleSelection.getControls(), pageSampleSelection.getIdentifier());

        mainDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainDialog.addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                KnownSamplesBoxAndWhiskerWizard.this.cancel();
            }
        });


        setupGUI();
        mainDialog.pack();
        mainDialog.setLocationRelativeTo(desination.getBoxPlotPublicationSite());
    }

    private void setupGUI()
    {
        Container content = mainDialog.getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();

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

    }

    public void showDialog(List<SampleCollection> collections)
    {
        SampleSelectionModel sampleSelectionModel = 
                new SampleSelectionModel(collections, "Select samples", true, true);

        this.wizardModel.setKeySelectionModel(sampleSelectionModel);
        this.pageSampleSelection.setModel(sampleSelectionModel);

        pullModelProperties();

        mainDialog.pack();
        mainDialog.setVisible(true);
    }

    private void pullModelProperties()
    {
        boolean finishEnabled = wizardModel.isFinishEnabled();
        finishAction.setEnabled(finishEnabled);

        updateWizard(pageSampleSelection);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(NEXT_ENABLED.equals(property))
        {}

        else if(BACK_ENABLED.equals(property))
        {}

        else if(FINISH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = finishAction.isEnabled();
            if((newVal != oldVal))
            {
                finishAction.setEnabled(newVal);
            }
        }
        else if(WIZARD_PAGE.equals(property))
        {
            int currentPageIndex = (int)evt.getNewValue();
            WizardPage currentPage = pages.get(currentPageIndex);
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
    }

    private JPanel buildButtonPanel()
    {
        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        JButton buttonFinish = new JButton(finishAction);
        JButton buttonCancel = new JButton(cancelAction);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonFinish)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(SwingConstants.HORIZONTAL, buttonFinish,buttonCancel);
        layout.linkSize(SwingConstants.VERTICAL, buttonFinish,buttonCancel);

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
        wizardModel.finish();
        mainDialog.setVisible(false);
    }

    public void cancel()
    {
        wizardModel.cancel();
        mainDialog.setVisible(false);
    }

    @Override
    public void publishErrorMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        mainDialog.pack();
        mainDialog.validate();
    }

    @Override
    public void publishWarningMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        mainDialog.pack();
        mainDialog.validate();
    }

    @Override
    public void publishInformationMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.informationIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        mainDialog.pack();
        mainDialog.validate();
    }

    @Override
    public void clearMessage() 
    {
        labelMessage.setIcon(null);
        labelMessage.setText(null);
        mainDialog.pack();
        mainDialog.validate();
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
            cancel();
        }
    }
}
