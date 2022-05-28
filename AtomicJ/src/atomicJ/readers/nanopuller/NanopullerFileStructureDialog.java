
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2018 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanopuller;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.readers.nanopuller.NanopullerFileStructureModel.QuantityIncreaseDirection;

public class NanopullerFileStructureDialog extends JDialog implements PropertyChangeListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JLabel labelForceIncreaseDirection = new JLabel("Force increases when cantilever is deflected");
    private final JLabel labelPiezoPositionIncreaseDirection = new JLabel("Piezo position increases when the cantilever is moved");

    private final JComboBox<QuantityIncreaseDirection> comboForceIncreaseDirection = new JComboBox<>(QuantityIncreaseDirection.values());
    private final JComboBox<QuantityIncreaseDirection> comboPiezoPositionIncreaseDirection = new JComboBox<>(QuantityIncreaseDirection.values());

    private final Action applyAction = new ApplyAction();
    private final Action applyToAllAction = new ApplyToAllAction();
    private final Action cancelAction = new CancelAction();

    private final JButton buttonApply = new JButton(applyAction);
    private final JButton buttonApplyToAll = new JButton(applyToAllAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final NanopullerFileStructureModel model;

    public NanopullerFileStructureDialog(Window parent, NanopullerFileStructureModel model)
    {
        super(parent, "Nanopuller structure assistant", ModalityType.APPLICATION_MODAL);

        this.model = model;
        this.model.addPropertyChangeListener(this);

        pullModelProperties();

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	
        JPanel mainPanel = buildISettingsPanel();

        panelTaskInformation.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panelButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));       
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);

        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        initItemListeners();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                NanopullerFileStructureDialog.this.cancel();
            }
        });


        pack();
        setLocationRelativeTo(parent);
    }

    private void pullModelProperties()
    {    
        String taskName = model.getTaskName();
        labelTaskName.setText(taskName);

        String taskDescription = model.getTaskDescription();
        labelTaskDescription.setText(taskDescription);

        QuantityIncreaseDirection forceIncreaseDirection = model.getForceIncreaseDirection();
        QuantityIncreaseDirection piezoPositionIncreaseDirection = model.getPiezoPositionIncreaseDirection();

        comboForceIncreaseDirection.setSelectedItem(forceIncreaseDirection);
        comboPiezoPositionIncreaseDirection.setSelectedItem(piezoPositionIncreaseDirection);

        boolean applyEnabled = model.isApplyEnabled();
        buttonApply.setEnabled(applyEnabled);

        boolean applyToAllEnabled = model.isApplyToAllEnabled();
        buttonApplyToAll.setEnabled(applyToAllEnabled);
    }

    private void initItemListeners()
    {        
        comboForceIncreaseDirection.addItemListener(new ItemListener() {         
            @Override
            public void itemStateChanged(ItemEvent evt) {
                QuantityIncreaseDirection forceIncreaseDirectionNew = (QuantityIncreaseDirection) comboForceIncreaseDirection.getSelectedItem();        
                model.setForceIncreaseDirection(forceIncreaseDirectionNew);          
            }
        });
        comboPiezoPositionIncreaseDirection.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                QuantityIncreaseDirection piezoPositionIncreaseDirectionNew = (QuantityIncreaseDirection) comboPiezoPositionIncreaseDirection.getSelectedItem();        
                model.setPiezoPositionIncreaseDirection(piezoPositionIncreaseDirectionNew);                
            }
        });           
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(NanopullerFileStructureModel.FORCE_INCREASE_DIRECTION.equals(property))
        {            
            QuantityIncreaseDirection valNew = (QuantityIncreaseDirection)evt.getNewValue();
            QuantityIncreaseDirection valOld = (QuantityIncreaseDirection)comboForceIncreaseDirection.getSelectedItem();

            if(!Objects.equals(valNew, valOld))
            {
                comboForceIncreaseDirection.setSelectedItem(valNew);
            }
        }
        else if(NanopullerFileStructureModel.PIEZO_POSITION_INCREASE_DIRECTION.equals(property))
        {
            QuantityIncreaseDirection valNew = (QuantityIncreaseDirection)evt.getNewValue();
            QuantityIncreaseDirection valOld = (QuantityIncreaseDirection)comboPiezoPositionIncreaseDirection.getSelectedItem();

            if(!Objects.equals(valNew, valOld))
            {
                comboPiezoPositionIncreaseDirection.setSelectedItem(valNew);
            }
        }    
        else if(NanopullerFileStructureModel.APPLY_ENABLED.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = buttonApply.isEnabled();
            if(valNew != valOld)
            {
                buttonApply.setEnabled(valNew);
            }
        }
        else if(NanopullerFileStructureModel.APPLY_TO_ALL_ENABLED.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = buttonApplyToAll.isEnabled();
            if(valNew != valOld)
            {
                buttonApplyToAll.setEnabled(valNew);
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
                .addComponent(buttonApply).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE).addComponent(buttonApplyToAll)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonApply)
                .addComponent(buttonApplyToAll)
                .addComponent(buttonCancel));

        layout.linkSize(buttonApply,buttonCancel);

        return panelButtons;
    }

    public void apply()
    {
        model.apply();
        setVisible(false);
    }

    public void applyToAll()
    {
        model.applyToAll();
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

    private class ApplyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyAction()
        {			
            putValue(NAME, "Apply");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            apply();
        }
    }

    private class ApplyToAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyToAllAction()
        {           
            putValue(NAME, "Apply to all");
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            applyToAll();
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

    private JPanel buildISettingsPanel()
    {
        SubPanel settingsPanel = new SubPanel();

        settingsPanel.addComponent(Box.createHorizontalGlue(), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        settingsPanel.addComponent(labelForceIncreaseDirection, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        settingsPanel.addComponent(comboForceIncreaseDirection, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        settingsPanel.addComponent(Box.createHorizontalGlue(), 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        settingsPanel.addComponent(Box.createHorizontalGlue(), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        settingsPanel.addComponent(labelPiezoPositionIncreaseDirection, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        settingsPanel.addComponent(comboPiezoPositionIncreaseDirection, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        settingsPanel.addComponent(Box.createHorizontalGlue(), 5, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        return settingsPanel;
    }
}
