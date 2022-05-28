
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


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.AbstractWizardPage;
import atomicJ.gui.ItemListMaster;
import atomicJ.gui.ItemPopupMenuList;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.statistics.ResultsChooser;
import atomicJ.utilities.Validation;

import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class BoxSamplePage<E extends Processed1DPack<E,?>> extends AbstractWizardPage implements WizardPage, ItemListMaster<E>, PropertyChangeListener
{
    private static final String IDENTIFIER = "Samples";

    private final JButton buttonAdd = new JButton(new AddAction());
    private final JButton buttonClear = new JButton(new ClearAction());	

    private final JFormattedTextField fieldName = new JFormattedTextField(new DefaultFormatter());
    private final JPanel panelRadioButtons = new JPanel();
    private final JPanel panelControls;

    private final ItemPopupMenuList<E> samplePacksList = new ItemPopupMenuList<>(this);
    private ResultsChooser<E> chooser;

    private BoxSampleModel<E> model;
    private List<Batch<E>> availableData;
    private boolean chooserStateInvalid;

    private final JPanel viewPanel = new JPanel();

    public BoxSamplePage(BoxSampleModel<E> model)
    {
        DefaultFormatter formatter = (DefaultFormatter)fieldName.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        fieldName.addPropertyChangeListener("value", this);

        setModel(model);
        buildGUI();

        this.panelControls =  buildControlPanel();     
    }

    public void setModel(BoxSampleModel<E> modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(this.model != null)
        {
            model.removePropertyChangeListener(this);
        }

        modelNew.addPropertyChangeListener(this);
        this.model = modelNew;

        pullModelProperties();
    }

    private void pullModelProperties()
    {
        List<Batch<E>> availableData = model.getAvilableData();
        setAvailableData(availableData);

        List<E> processedPacks = model.getProcessedPacks();
        setProcessedPacks(processedPacks);

        String initSampleName = model.getCurrentBatchName();
        fieldName.setValue(initSampleName);
    }


    public void setAvailableData(List<Batch<E>> availableData)
    {
        Validation.requireNonNullParameterName(availableData, "availableData");

        this.availableData = availableData;
        this.chooserStateInvalid = true;
    }

    private void setProcessedPacks(List<E> packs)
    {
        samplePacksList.setItems(packs);
        samplePacksList.revalidate();   
    }

    private void buildGUI()
    {
        viewPanel.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(buildNamePanel(), BorderLayout.NORTH);
        mainPanel.add(buildListPanel(), BorderLayout.CENTER);
        mainPanel.add(panelRadioButtons, BorderLayout.EAST);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        this.viewPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel buildListPanel()
    {
        JPanel listPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane  = new JScrollPane(samplePacksList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        return listPanel;
    }

    private JPanel buildNamePanel()
    {
        SubPanel namePanel = new SubPanel();
        namePanel.addComponent(new JLabel("Name:"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        namePanel.addComponent(fieldName, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        namePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return namePanel;
    }

    private JPanel buildControlPanel()
    {		
        JPanel panelControl = new JPanel();	
        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonAdd).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClear).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(buttonAdd)
                .addComponent(buttonClear));

        layout.linkSize(buttonAdd,buttonClear);

        return panelControl;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == fieldName)
        {
            String newVal = evt.getNewValue().toString();
            model.setCurrentBatchName(newVal);
        }	
        else
        {
            String property = evt.getPropertyName();

            if(SAMPLE_NAME.equals(property))
            {
                String newVal = (String)evt.getNewValue();
                String oldVal = (String)fieldName.getValue();
                if(!(newVal.equals(oldVal)))
                {
                    fieldName.setValue(newVal);
                }
            }
            else if(SAMPLE_PROCESSED_PACKS.equals(property))
            {
                @SuppressWarnings("unchecked")
                List<E> newVal = (List<E>)evt.getNewValue();
                List<E> oldVal = samplePacksList.getItems();
                if(!oldVal.equals(newVal))
                {
                    setProcessedPacks(newVal);
                }
            }
            else if(BoxSampleModel.CURRENT_SAMPLE_MODEL.equals(property))
            {
                pullModelProperties();
            }
        }
    }

    private class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ClearAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Clear");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.setProcessedPacks(new ArrayList<>());
        }
    }

    private class AddAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AddAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(NAME,"Add");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(chooser == null || chooserStateInvalid)
            {
                Window parentWindow = SwingUtilities.getWindowAncestor(viewPanel);
                chooser = new ResultsChooser<>(availableData, parentWindow);
            }
            boolean selectionApproved = chooser.showDialog();

            if(selectionApproved)
            {
                List<E> packs = chooser.getSelectedPacks();               
                model.setProcessedPacks(packs);
            }
        }
    }

    @Override
    public void removeItems(List<E> packs) 
    {
        model.removeProcessedPacks(packs);
    }

    @Override
    public void setItems(List<E> packs) 
    {
        model.setProcessedPacks(packs);		
    }

    @Override
    public String getTaskName() 
    {
        return model.getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return model.getTaskDescription();
    }

    @Override
    public String getIdentifier() 
    {
        return IDENTIFIER;
    }

    @Override
    public boolean isFirst() 
    {
        return model.isFirst();
    }

    @Override
    public boolean isLast() 
    {
        return model.isLast();
    }

    @Override
    public boolean isBackEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isNextEnabled() 
    {
        return model.isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return model.isSkipEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return model.isFinishEnabled();
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return model.isNecessaryInputProvided();
    }

    @Override
    public Component getView() 
    {
        return viewPanel;
    }

    @Override
    public Component getControls() 
    {
        return panelControls;
    }
}
