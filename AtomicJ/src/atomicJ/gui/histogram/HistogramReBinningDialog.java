
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


import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.Validation;


public class HistogramReBinningDialog extends JDialog implements PropertyChangeListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JButton buttonClose = new JButton(new CloseAction());
    private final JButton buttonUndo = new JButton(new UndoAction());

    private HistogramBinningModel wizardModel;

    private final HistogramBinningPage binningPage  = new HistogramBinningPage();

    public HistogramReBinningDialog(String name, Window parent)
    {
        super(parent, name, ModalityType.MODELESS);

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
                .addComponent(binningPage, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)	
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(binningPage, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
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
        setLocationRelativeTo(parent);
    }

    public void showDialog(HistogramBinningModel modelNew)
    {	
        setWizardModel(modelNew);
        setVisible(true);
    }

    public void setWizardModel(HistogramBinningModel modelNew)
    {
        Validation.requireNonNullParameterName(modelNew, "modelNew");

        if(wizardModel != null)
        {
            wizardModel.removePropertyChangeListener(this);
        }
        this.wizardModel = modelNew;
        modelNew.addPropertyChangeListener(this);
        wizardModel.setUndoPoint();
        binningPage.setModel(modelNew);

        pullModelProperties();
    }


    public void cleanUp()
    {
        if(wizardModel != null)
        {
            wizardModel.removePropertyChangeListener(this);
        }
        this.wizardModel = null;
        this.binningPage.cleanUp();
    }

    private void pullModelProperties()
    {

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
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonUndo).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                );

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonClose)
                .addComponent(buttonUndo)
                );

        layout.linkSize(buttonClose, buttonUndo);

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
    public void propertyChange(PropertyChangeEvent evt) 
    {

    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        }
    }

    private class UndoAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public UndoAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME,"Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            wizardModel.undo();
        }
    }
}
