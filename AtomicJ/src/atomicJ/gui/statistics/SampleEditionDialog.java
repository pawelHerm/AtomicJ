
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


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.text.DefaultFormatter;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.ItemListMaster;
import atomicJ.gui.ItemPopupMenuList;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.GUIUtilities;
import atomicJ.utilities.Validation;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;
import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class SampleEditionDialog<E extends Processed1DPack<E,?>> extends JDialog implements ItemListMaster<E>, PropertyChangeListener, ActionListener
{
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HEIGHT = Math.round(2*Toolkit.getDefaultToolkit().getScreenSize().height/5);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/3);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private String initSampleName;
    private String initVariable;
    private List<E> initSampleData;

    private final JButton buttonOK = new JButton(new OKAction());
    private final JButton buttonCancel = new JButton(new CancelAction());
    private final JButton buttonAdd = new JButton(new AddAction());
    private final JButton buttonClear = new JButton(new ClearAction()); 
    private final JFormattedTextField fieldName = new JFormattedTextField(new DefaultFormatter());
    private final JPanel panelRadioButtons = new JPanel();

    private ButtonGroup buttonGroup;
    private final Map<String, JRadioButton> buttons = new LinkedHashMap<>();

    private final ItemPopupMenuList<E> samplePacksList = new ItemPopupMenuList<>(this);
    private ResultsChooser<E> chooser;

    private ProcessedPackSampleModel<E> model;
    private List<Batch<E>> availableData;
    private boolean chooserStateInvalid;

    public SampleEditionDialog(Window parent)
    {
        super(parent, "Sample editor", ModalityType.APPLICATION_MODAL);

        DefaultFormatter formatter = (DefaultFormatter)fieldName.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        fieldName.addPropertyChangeListener("value", this);

        buildGUI();

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {               
                pref.putInt(WINDOW_HEIGHT, SampleEditionDialog.this.getHeight());
                pref.putInt(WINDOW_WIDTH, SampleEditionDialog.this.getWidth());
            }
        });

        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);

        if(GUIUtilities.isWindowSizeWellSpecified(width, height))
        {
            setSize(width,height);
        }
        else
        {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        setLocationRelativeTo(parent);
    }

    public void setAvailableData(List<Batch<E>> availableData)
    {
        Validation.requireNonNullParameterName(availableData, "availableData");

        this.availableData = availableData;
        this.chooserStateInvalid = true;
    }

    public void edit(ProcessedPackSampleModel<E> model)
    {        
        setModel(model);
        setVisible(true);
    }

    private void pullModelProperties()
    {
        initVariable = model.getVariable();
        selectRadioButton(initVariable);    

        initSampleName = model.getSampleName();
        fieldName.setValue(initSampleName);

        initSampleData = model.getProcessedPacks();
        samplePacksList.setItems(initSampleData);
    }

    private void revertModel()
    {
        model.setVariable(initVariable);
        model.setSampleName(initSampleName);
        model.setProcessedPacks(initSampleData);
    }

    private void setProcessedPacks(List<E> packs)
    {
        samplePacksList.setItems(packs);
        samplePacksList.revalidate();   
    }

    public void setModel(ProcessedPackSampleModel<E> modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        ProcessedPackSampleModel<E> modelOld = this.model;

        if(modelOld != null)
        {
            modelOld.removePropertyChangeListener(this);
            this.model = modelNew;

            if(!modelOld.getAvailableVariables().equals(modelNew.getAvailableVariables()))
            {
                updatePanelBoxes();
            }
        }
        else
        {   
            this.model = modelNew;
            updatePanelBoxes();
        }

        modelNew.addPropertyChangeListener(this);

        pullModelProperties();
    }

    private void updatePanelBoxes()
    {
        this.buttonGroup = new ButtonGroup();

        for(JRadioButton button: buttons.values())
        {
            button.removeActionListener(this);
        }

        buttons.clear();

        for(String variable: model.getAvailableVariables())
        {
            JRadioButton button = new JRadioButton(variable);
            button.setActionCommand(variable);
            button.addActionListener(this);

            buttonGroup.add(button);
            buttons.put(variable, button);
        }
        panelRadioButtons.removeAll();

        JLabel labelDatasets = new JLabel("Variables");

        GroupLayout layout = new GroupLayout(panelRadioButtons);
        panelRadioButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        //builds and sets horizontal group
        SequentialGroup horizontalGroup = layout.createSequentialGroup();
        ParallelGroup innerHorizontalGroup = layout.createParallelGroup().addComponent(labelDatasets);

        for(JRadioButton button: buttons.values())
        {
            innerHorizontalGroup.addComponent(button);
        }

        horizontalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(innerHorizontalGroup).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setHorizontalGroup(horizontalGroup);

        //builds and sets vertical group

        SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(labelDatasets).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);

        for(JRadioButton button: buttons.values())
        {
            verticalGroup.addComponent(button).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        }

        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        layout.setVerticalGroup(verticalGroup);

        layout.linkSize(buttons.values().toArray(new JRadioButton[] {}));

        revalidate();
        repaint();
    }

    private void selectRadioButton(String name)
    {
        JRadioButton button = buttons.get(name);
        if(button == null)
        {
            buttonGroup.clearSelection();
            return;
        }
        else
        {
            if(button.isSelected())
            {
                return;
            }
            else
            {
                buttonGroup.clearSelection();
                buttonGroup.setSelected(button.getModel(), true);
            }
        }
    }   

    private void buildGUI()
    {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(buildNamePanel(), BorderLayout.NORTH);
        mainPanel.add(buildControlPanel(), BorderLayout.WEST);
        mainPanel.add(buildListPanel(), BorderLayout.CENTER);
        mainPanel.add(panelRadioButtons, BorderLayout.EAST);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(mainPanel, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
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

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        Object source = evt.getSource();
        if (buttons.containsValue(source)) 
        {
            JRadioButton button = (JRadioButton)source;
            String selectedVariable = button.getActionCommand();

            model.setVariable(selectedVariable);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == fieldName)
        {
            String newVal = evt.getNewValue().toString();
            model.setSampleName(newVal);

        }   
        else
        {
            String name = evt.getPropertyName();
            if(SAMPLE_NAME.equals(name))
            {
                String newVal = (String)evt.getNewValue();
                String oldVal = (String)fieldName.getValue();
                if(!(newVal.equals(oldVal)))
                {
                    fieldName.setValue(newVal);
                }
            }
            else if(SAMPLE_VARIABLE.equals(name))
            {
                String newVal = (String)evt.getNewValue();
                selectRadioButton(newVal);
            }
            else if(SAMPLE_PROCESSED_PACKS.equals(name))
            {
                @SuppressWarnings("unchecked")
                List<E> newVal = (List<E>)evt.getNewValue();
                List<E> oldVal = samplePacksList.getItems();
                if(!oldVal.equals(newVal))
                {
                    setProcessedPacks(newVal);
                }
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
                chooser = new ResultsChooser<>(availableData, SampleEditionDialog.this);
            }
            boolean selectionApproved = chooser.showDialog();
            if(selectionApproved)
            {
                List<E> packs = chooser.getSelectedPacks();
                model.setProcessedPacks(packs);
            }
        }
    }

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
            revertModel();
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
}
