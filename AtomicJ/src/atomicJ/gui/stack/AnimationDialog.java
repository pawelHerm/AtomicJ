
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

package atomicJ.gui.stack;

import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.SIPrefix;
import atomicJ.gui.EditableComboBox;
import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionPanel;
import atomicJ.gui.stack.AnimationModel.TypeCollection;
import atomicJ.utilities.MultiMap;


public class AnimationDialog extends JDialog implements PropertyChangeListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final NumericalField fieldStackStart = new NumericalField("Stack start must be a number", -Short.MAX_VALUE, Short.MAX_VALUE);
    private final NumericalField fieldStackEnd = new NumericalField("Stack end must be a number", -Short.MAX_VALUE, Short.MAX_VALUE);
    private final JSpinner spinnerStackStart = new JSpinner(new SpinnerNumberModel(1., -Short.MAX_VALUE, Short.MAX_VALUE, 1.));
    private final JSpinner spinnerStackEnd = new JSpinner(new SpinnerNumberModel(1., -Short.MAX_VALUE, Short.MAX_VALUE, 1.));

    private final MultipleSelectionPanel<String, MultipleSelectionModel<String>> keySelectionPanel = new MultipleSelectionPanel<>();

    private final JFormattedTextField fieldStackingQuantityName = new JFormattedTextField(new DefaultFormatter());
    private final JComboBox<SIPrefix> comboStackingQuantityPrefix = new JComboBox<>(SIPrefix.values());
    private final EditableComboBox comboStackingQuantityUnitName = new EditableComboBox(AnimationModel.getZQuantityUnitNames());
    private final Map<TypeCollection, JComboBox<Quantity>> combosDefaultQuantity = new LinkedHashMap<>();
    private final JCheckBox boxUseDefaultQuantities = new JCheckBox("Use default quantities");

    private final Action finishAction = new FinishAction();
    private final Action cancelAction = new CancelAction();

    private final JButton buttonFinish = new JButton(finishAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final AnimationModel model;

    public AnimationDialog(Window parent, AnimationModel model)
    {
        super(parent, "Image animation assistant", ModalityType.APPLICATION_MODAL);

        this.model = model;
        this.model.addPropertyChangeListener(this);

        DefaultFormatter formatter = (DefaultFormatter)fieldStackingQuantityName.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        pullModelProperties();

        buildGUI();
        initItemListeners();
        initFieldsListener();
        initChangeListeners();  

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                AnimationDialog.this.cancel();
            }
        });


        pack();
        setLocationRelativeTo(parent);
    }

    private void buildGUI()
    {             
        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();   
        JPanel mainPanel = buildMainPanel();

        panelTaskInformation.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panelButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(5, 5, 5, 5)));       
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);

        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

    }

    private void setQuantityEditingControlsEnabled(boolean enabled)
    {
        fieldStackingQuantityName.setEnabled(enabled);
        comboStackingQuantityPrefix.setEnabled(enabled);
        comboStackingQuantityUnitName.setEnabled(enabled);
        spinnerStackStart.setEnabled(enabled);
        spinnerStackEnd.setEnabled(enabled);
    }

    private void setDefaultQuantityControlsEnabled(boolean enabled)
    {
        for(JComboBox<Quantity> quantity : combosDefaultQuantity.values())
        {
            quantity.setEnabled(enabled);
        }
    }

    //has to be called before buildGUI() method, as it builds the GUI components
    //whose number depends on the model
    private void pullModelProperties()
    {            
        String taskName = model.getTaskName();
        labelTaskName.setText(taskName);

        String taskDescription = model.getTaskDescription();
        labelTaskDescription.setText(taskDescription);

        double stackStart = model.getStackMinimum();
        double imageHeight = model.getStackMaximum();

        fieldStackStart.setValue(stackStart);
        fieldStackEnd.setValue(imageHeight);

        String stackingQuantityName = model.getStackingQuantityName();
        SIPrefix stackingQuantityPrefix = model.getStackingQuantityPrefix();
        String stackingQuantityUnitName = model.getStackingQuantityUnitName();

        fieldStackingQuantityName.setValue(stackingQuantityName);
        comboStackingQuantityPrefix.setSelectedItem(stackingQuantityPrefix);
        comboStackingQuantityUnitName.setSelectedItem(stackingQuantityUnitName);

        keySelectionPanel.setModel(model.getSelectionModel());

        MultiMap<TypeCollection, Quantity> availableDefaultQuantities = model.getAvailableDefaultQuantities();

        for(Entry<TypeCollection, List<Quantity>> entry : availableDefaultQuantities.entrySet())
        {
            TypeCollection types = entry.getKey();
            JComboBox<Quantity> combo = new JComboBox<>(entry.getValue().toArray(new Quantity[] {}));
            combosDefaultQuantity.put(types, combo);
        }

        boolean useDefaultQuantities = model.isUseDefaultStackingQuantity();
        boxUseDefaultQuantities.setSelected(useDefaultQuantities);               
        setDefaultQuantityControlsEnabled(useDefaultQuantities);

        for(Entry<TypeCollection, JComboBox<Quantity>> entry : combosDefaultQuantity.entrySet())
        {
            final TypeCollection types = entry.getKey();
            final JComboBox<Quantity> combo = entry.getValue();
            combo.setSelectedItem(model.getSelectedDefaultQuantity(types));
        }

        boolean quantityEditingEnabled = model.isUserQuantitySpecificationEnabled();
        setQuantityEditingControlsEnabled(quantityEditingEnabled);

        boolean finishEnabled = model.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);
    }

    private void initItemListeners()
    {   
        boxUseDefaultQuantities.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                model.setUseDefaultStackingQuantity(selected);
            }
        });

        for(Entry<TypeCollection, JComboBox<Quantity>> entry : combosDefaultQuantity.entrySet())
        {
            final TypeCollection types = entry.getKey();
            final JComboBox<Quantity> combo = entry.getValue();
            combo.addItemListener(new ItemListener() 
            {             
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    Quantity quantity = (Quantity)combo.getSelectedItem();
                    model.setSelectedDefaultQuantity(types, quantity);
                }
            });
        }

        comboStackingQuantityPrefix.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {                
                SIPrefix prefix = (SIPrefix)comboStackingQuantityPrefix.getSelectedItem();
                model.setStackingQuantityPrefix(prefix);
            }
        });
        comboStackingQuantityUnitName.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e) {
                String unitName = (String)comboStackingQuantityUnitName.getSelectedItem();
                model.setStackingQuantityUnitName(unitName);                
            }
        });      
    }


    private void initChangeListeners()
    {
        spinnerStackStart.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) 
            {
                Double stackStartNew = ((SpinnerNumberModel)spinnerStackStart.getModel()).getNumber().doubleValue();
                Number stackStartOld = fieldStackStart.getValue();

                if(!Objects.equals(stackStartNew, stackStartOld))
                {
                    model.specifyStackMinimum(stackStartNew);                
                }            
            }
        });

        spinnerStackEnd.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) 
            {
                Double stackEndNew = ((SpinnerNumberModel)spinnerStackEnd.getModel()).getNumber().doubleValue();
                Number stackEndOld = fieldStackEnd.getValue();

                if(!Objects.equals(stackEndNew, stackEndOld))
                {
                    model.specifyStackMaximum(stackEndNew);
                }            
            }
        });
    }

    private void initFieldsListener()
    {
        fieldStackStart.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                model.specifyStackMinimum(valNew);            
            }
        }); 
        fieldStackStart.addPropertyChangeListener("value",new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                if(!Double.isNaN(valNew))
                {
                    spinnerStackStart.setValue(valNew);
                }            
            }
        }); 
        fieldStackEnd.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();
                model.specifyStackMaximum(valNew);              
            }
        });
        fieldStackEnd.addPropertyChangeListener("value", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                if(!Double.isNaN(valNew))
                {
                    spinnerStackEnd.setValue(valNew);
                }             
            }
        });
        fieldStackingQuantityName.addPropertyChangeListener("value", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                String valNew = (String)evt.getNewValue();
                model.setStackingQuantityName(valNew);
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(AnimationModel.STACK_START.equals(property))
        {            
            double valNew = (double)evt.getNewValue();
            double valOld = fieldStackStart.getValue().doubleValue();

            boolean onlyOneNaN = Double.isNaN(valOld) != Double.isNaN(valNew);

            if(onlyOneNaN || Math.abs(valOld - valNew) > TOLERANCE)
            {
                fieldStackStart.setValue(valNew);
            }
        }
        else if(AnimationModel.STACK_END.equals(property))
        {
            double valNew = (double)evt.getNewValue();
            double valOld = fieldStackEnd.getValue().doubleValue();

            boolean onlyOneNaN = Double.isNaN(valOld) != Double.isNaN(valNew);

            if(onlyOneNaN || Math.abs(valOld - valNew) > TOLERANCE)
            {
                fieldStackEnd.setValue(valNew);
            }
        }
        else if(AnimationModel.STACKING_QUANTITY_NAME.equals(property))
        {
            String valNew = (String) evt.getNewValue();
            String valOld = fieldStackingQuantityName.toString();

            if(!Objects.equals(valOld, valNew))
            {
                fieldStackingQuantityName.setValue(valNew);
            }
        }
        else if(AnimationModel.STACKING_QUANTITY_PREFIX.equals(property))
        {
            SIPrefix valNew = (SIPrefix) evt.getNewValue();
            SIPrefix valOld = (SIPrefix)comboStackingQuantityPrefix.getSelectedItem();

            if(!Objects.equals(valOld, valNew))
            {
                comboStackingQuantityPrefix.setSelectedItem(valNew);
            }
        }
        else if(AnimationModel.STACKING_QUANTITY_UNIT_NAME.equals(property))
        {
            String valNew = (String) evt.getNewValue();
            String valOld = (String)comboStackingQuantityUnitName.getSelectedItem();

            if(!Objects.equals(valOld, valNew))
            {
                comboStackingQuantityUnitName.setSelectedItem(valNew);
            }
        }
        else if(AnimationModel.USER_QUANTITY_SPECIFICATION_ENABLED.equals(property))
        {
            boolean enabledNew = (boolean)evt.getNewValue();
            setQuantityEditingControlsEnabled(enabledNew);
        }
        else if(AnimationModel.USE_DEFAULT_STACKING_QUATITY.equals(property))
        {
            boolean selected = (boolean)evt.getNewValue();
            boolean selectedOld = this.boxUseDefaultQuantities.isSelected();
            if(selected != selectedOld)
            {
                boxUseDefaultQuantities.setSelected(selected);
            }   

            setDefaultQuantityControlsEnabled(selected); //this needs to be outside the if block above, because the box will change its state before the model
            //so even if the selected == selctedOld, the interface should be refreshed
        }
        else if(AnimationModel.FINISH_ENABLED.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = buttonFinish.isEnabled();
            if(valNew != valOld)
            {
                buttonFinish.setEnabled(valNew);
            }
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

    private JPanel buildButtonPanel()
    {
        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonFinish)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonFinish,buttonCancel);

        return panelButtons;
    }

    public void finish()
    {
        model.finish();
        setVisible(false);
    }

    public void cancel()
    {
        model.cancel();
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

    private SubPanel buildMainPanel()
    {            
        SubPanel panel = new SubPanel();  

        panel.addComponent(keySelectionPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);          
        panel.addComponent(keySelectionPanel.getControls(), 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);          

        SubPanel panelZQuantityUnit = new SubPanel();

        panelZQuantityUnit.addComponent(comboStackingQuantityPrefix, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(5,1,5,1));
        panelZQuantityUnit.addComponent(comboStackingQuantityUnitName, 1, 0, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(5,1,5,1));          


        SubPanel panelDefaultUnits = new SubPanel();

        panelDefaultUnits.addComponent(boxUseDefaultQuantities, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        for(Entry<TypeCollection, JComboBox<Quantity>> entry : combosDefaultQuantity.entrySet())
        {
            TypeCollection types = entry.getKey();
            JComboBox<Quantity> comboBox = entry.getValue();

            JLabel label = new JLabel(types.toString());

            panelDefaultUnits.addComponent(label, 0, panelDefaultUnits.getMaxRow(), 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
            panelDefaultUnits.addComponent(comboBox, 1, panelDefaultUnits.getMaxRow(), 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        }

        panel.addComponent(panelDefaultUnits, 0, 1, 2, 2, GridBagConstraints.EAST, GridBagConstraints.BOTH, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(new JLabel("Stacking quantity"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(fieldStackingQuantityName, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);          
        panel.addComponent(panelZQuantityUnit, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);          

        spinnerStackStart.setEditor(fieldStackStart);    
        spinnerStackEnd.setEditor(fieldStackEnd);

        panel.addComponent(new JLabel("Start"), 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panel.addComponent(spinnerStackStart, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panel.addComponent(new JLabel("End"), 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panel.addComponent(spinnerStackEnd, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panel;
    }
}
