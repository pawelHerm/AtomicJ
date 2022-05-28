
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

package atomicJ.gui.histogram;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPage;
import atomicJ.utilities.Validation;

import static atomicJ.gui.histogram.HistogramModelProperties.*;

public class HistogramWizard extends JDialog implements PropertyChangeListener, ActionListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private static final String BACK_ACTION_COMMAND = "BackCommand";
    private static final String NEXT_ACTION_COMMAND = "NextCommand";
    private static final String SKIP_ACTION_COMMAND = "SkipCommand";
    private static final String FINISH_ACTION_COMMAND = "FinishCommand";
    private static final String CANCEL_ACTION_COMMAND = "CancelCommand";

    private static final String SELECTION_PAGE = "SelectionPage";
    private static final String BINNING_PAGE = "BinningPage";

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JButton buttonBack = new JButton("< Back");
    private final JButton buttonNext = new JButton("Next >");
    private final JButton buttonSkip = new JButton("Skip");
    private final JButton buttonFinish = new JButton("Finish");
    private final JButton buttonCancel = new JButton("Cancel");

    private HistogramWizardModel wizardModel;
    private HistogramMultiSampleBinningModel binningModel;

    private final JPanel panelPages = new JPanel(new CardLayout());

    private final HistogramBinningPage pageBinning = new HistogramBinningPage();
    private final MultipleSelectionWizardPage pageSelection = new MultipleSelectionWizardPage("KeySelection");

    public HistogramWizard(HistogramWizardModel wizardModel)
    {
        super(wizardModel.getPublicationSite(),"Histogram assistant", ModalityType.APPLICATION_MODAL);

        setWizardModel(wizardModel);
        pullModelProperties();	

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(false);

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	

        SubPanel panelSelectionFull = new SubPanel();
        panelSelectionFull.addComponent(pageSelection.getControls(), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);
        panelSelectionFull.addComponent(pageSelection, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);

        panelPages.add(panelSelectionFull,SELECTION_PAGE);
        panelPages.add(pageBinning, BINNING_PAGE);

        layout.setHorizontalGroup
        (
                layout.createParallelGroup()
                .addComponent(panelButtons, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelPages, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)	
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(panelPages, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        addComponentListener(new ComponentAdapter() 
        { 
            @Override
            public void componentHidden(ComponentEvent evt) 
            {
                cleanUp();                
            }
        });

        pack();
        setLocationRelativeTo(wizardModel.getPublicationSite());
    }

    public void showDialog()
    {	
        setVisible(true);
    }

    public void setWizardModel(HistogramWizardModel modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(wizardModel != null)
        {
            wizardModel.removePropertyChangeListener(this);
        }
        this.wizardModel = modelNew;
        wizardModel.addPropertyChangeListener(this);
        pageSelection.setModel(wizardModel.getSelectionModel());

        CardLayout layoutPages = (CardLayout)(panelPages.getLayout());
        layoutPages.show(panelPages, SELECTION_PAGE);
        pullModelProperties();
    }

    public void cleanUp()
    {
        if(wizardModel != null)
        {
            wizardModel.removePropertyChangeListener(this);
        }
        this.wizardModel = null;

        if(binningModel != null)
        {
            binningModel.removePropertyChangeListener(this);
        }
        this.binningModel = null;

        pageSelection.cleanUp();
        pageBinning.cleanUp();
    }

    private void moveToBinningPage(HistogramMultiSampleBinningModel modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(binningModel != null)
        {
            binningModel.removePropertyChangeListener(this);
        }
        this.binningModel = modelNew;
        modelNew.addPropertyChangeListener(this);
        pageBinning.setModel(modelNew);

        CardLayout layoutPages = (CardLayout)(panelPages.getLayout());
        layoutPages.show(panelPages, BINNING_PAGE);
    }

    private void pullModelProperties()
    {
        boolean backEnabled = wizardModel.isBackEnabled();
        buttonBack.setEnabled(backEnabled);

        boolean nextEnabled = wizardModel.isNextEnabled();
        buttonNext.setEnabled(nextEnabled);

        boolean skipEnabled = wizardModel.isSkipEnabled();
        buttonSkip.setEnabled(skipEnabled);

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
        buttonSkip.addActionListener(this);
        buttonSkip.setActionCommand(SKIP_ACTION_COMMAND);
        buttonSkip.setMnemonic(KeyEvent.VK_S);
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
                .addComponent(buttonSkip).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonFinish).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBack)
                .addComponent(buttonNext)
                .addComponent(buttonSkip)
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonBack, buttonNext, buttonSkip, buttonFinish, buttonCancel);

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
        else if(command.equals(SKIP_ACTION_COMMAND))
        {
            wizardModel.skip();
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
        String property = evt.getPropertyName();

        if(WizardModelProperties.BACK_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();			
            buttonBack.setEnabled(newVal);
        }
        else if(WizardModelProperties.NEXT_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();			
            buttonNext.setEnabled(newVal);			
        }
        else if(WizardModelProperties.SKIP_ENABLED.equals(property))
        {		
            boolean newVal = (boolean)evt.getNewValue();			
            buttonSkip.setEnabled(newVal);			
        }
        else if(WizardModelProperties.FINISH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            buttonFinish.setEnabled(newVal);
        }
        else if(WizardModelProperties.TASK.equals(property))
        {
            String newVal = (String)evt.getNewValue();
            labelTaskDescription.setText(newVal);
        }
        else if(SELECTION_FINISHED.equals(property))
        {
            HistogramMultiSampleBinningModel binningModel = wizardModel.getBinningModel();
            moveToBinningPage(binningModel);
        }
        else if(BINNING_FINISHED.equals(property))
        {
            setVisible(false);
        }
    }
}
