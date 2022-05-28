
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
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.SimpleProcessedPackFunction;
import atomicJ.gui.KeyMapSelectionModel;
import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPage;

import static atomicJ.gui.WizardModelProperties.*;

public class BoxAndWhiskerWizard <E extends Processed1DPack<E,?>> extends JDialog implements PropertyChangeListener, MessageDisplayer
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

    private final BoxSamplePage<E> pageSamples;
    private final MultipleSelectionWizardPage<String> pageQuantityTypes;
    private final List<WizardPage> pages = new ArrayList<>();

    private final BoxSampleWizardModel<E> wizardModel;

    //SimpleProcessedPackFunction.getAvailableQuantitiesFunctionMap()
    public BoxAndWhiskerWizard(BoxAndWhiskersDestination desination)
    {
        super(desination.getBoxPlotPublicationSite(), "Box-and-whisker plot assistant", ModalityType.APPLICATION_MODAL);

        KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel = new KeyMapSelectionModel<>(Collections.emptyMap(),
                "Quantity", "Quantity selection ", "Select quantities that you want to show in box plots",
                true, false);

        BoxSampleModel<E> boxSampleModel = new BoxSampleModel<>(Collections.emptyList(), Collections.emptyList(), false, true);

        this.pageQuantityTypes = new MultipleSelectionWizardPage<>("QuantityTypeSelection",keySelectionModel, true);
        this.pageSamples = new BoxSamplePage<>(boxSampleModel);

        pages.add(pageQuantityTypes);
        pages.add(pageSamples);

        this.wizardModel = new BoxSampleWizardModel<>(desination, keySelectionModel, boxSampleModel);
        wizardModel.addPropertyChangeListener(this);

        pullModelProperties();

        panelPages.add(pageSamples.getView(), pageSamples.getIdentifier());
        panelPages.add(pageQuantityTypes.getView(), pageQuantityTypes.getIdentifier());
        panelControls.add(pageSamples.getControls(), pageSamples.getIdentifier());
        panelControls.add(pageQuantityTypes.getControls(), pageQuantityTypes.getIdentifier());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                BoxAndWhiskerWizard.this.cancel();
            }
        });


        setupGUI();
        pack();
        setLocationRelativeTo(desination.getBoxPlotPublicationSite());
    }

    private void setupGUI()
    {
        Container content = getContentPane();
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

    public void showDialog(Collection<ProcessedPackFunction<? super E>> functions, List<Batch<E>> availableData)
    {
        KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel = 
                new KeyMapSelectionModel<>(SimpleProcessedPackFunction.getAvailableQuantitiesFunctionMap(functions),
                        "Quantity", "Quantity selection ", "Select quantities that you want to show in box plots",
                        true, false);

        BoxSampleModel<E> boxSampleModel = new BoxSampleModel<>(functions, availableData, false, true);

        setModels(keySelectionModel, boxSampleModel);
        pack();
        setVisible(true);
    }

    //this method assumes that pageSamples, pageQuantityTypes and wizardModel
    //are already initialized, so it cannot be called from the constructor
    private void setModels(KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel, BoxSampleModel<E> boxSampleModel)
    {
        this.pageSamples.setModel(boxSampleModel);
        this.pageQuantityTypes.setModel(keySelectionModel);

        this.wizardModel.setPageModels(keySelectionModel, boxSampleModel);

        pullModelProperties();
    }

    private void pullModelProperties()
    {
        boolean backEnabled = wizardModel.isBackEnabled();
        buttonBack.setEnabled(backEnabled);

        boolean nextEnabled = wizardModel.isNextEnabled();
        buttonNext.setEnabled(nextEnabled); 

        boolean finishEnabled = wizardModel.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);

        int pageIndex = wizardModel.getCurrentPageIndex();
        WizardPage currentPage = pages.get(pageIndex);
        updateWizard(currentPage);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(NEXT_ENABLED.equals(name))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonNext.isEnabled();
            if((newVal != oldVal))
            {
                buttonNext.setEnabled(newVal);
            }
        }

        else if(BACK_ENABLED.equals(name))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonBack.isEnabled();
            if((newVal != oldVal))
            {
                buttonBack.setEnabled(newVal);
            }
        }

        else if(FINISH_ENABLED.equals(name))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = buttonFinish.isEnabled();
            if((newVal != oldVal))
            {
                buttonFinish.setEnabled(newVal);
            }
        }
        else if(WIZARD_PAGE.equals(name))
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
        layout.linkSize(SwingConstants.HORIZONTAL, buttonNext,buttonNext);
        layout.linkSize(SwingConstants.VERTICAL, buttonFinish,buttonCancel,buttonBack,buttonNext);

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
        setVisible(false);
    }

    public void cancel()
    {
        wizardModel.cancel();
        setVisible(false);
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
            cancel();
        }
    }
}
