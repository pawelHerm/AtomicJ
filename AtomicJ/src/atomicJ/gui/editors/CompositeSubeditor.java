
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

package atomicJ.gui.editors;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import atomicJ.gui.*;

import java.util.List;


public class CompositeSubeditor implements Subeditor 
{
    private final JPanel editorPanel = new JPanel(new BorderLayout());
    private final List<Subeditor> seriesSubeditors;   

    public CompositeSubeditor(List<Subeditor> subeditors, int maxGroupSize) 
    {
        this.seriesSubeditors = subeditors;

        int n = subeditors.size();
        boolean addTabs = n > maxGroupSize;

        JComponent interior = null;

        if(addTabs)
        {
            JTabbedPane tabPane = new JTabbedPane();

            for(int i = 0; i<subeditors.size(); i++)
            {
                Subeditor subeditor = subeditors.get(i);
                subeditor.setNameBorder(false);
                Component comp = subeditor.getEditionComponent();
                JPanel outerPanel = new JPanel(new BorderLayout());
                outerPanel.add(comp, BorderLayout.NORTH);
                tabPane.addTab(subeditor.getSubeditorName(), outerPanel);    				 
            }
            interior = tabPane;
        }
        else
        {
            SubPanel subPanel = new SubPanel();

            for(int i = 0; i<subeditors.size(); i++)
            {
                Subeditor subeditor = subeditors.get(i);
                Component comp = subeditor.getEditionComponent();
                subPanel.addComponent(comp, i%2, i/2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      				 
            }
            interior = subPanel;
        }

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        editorPanel.add(interior,BorderLayout.NORTH);

        JPanel buttons = buildButtonPanel();
        editorPanel.add(buttons, BorderLayout.SOUTH);
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        BatchApplyAction batchApplyAction = new BatchApplyAction();
        JButton buttonBatchApply = new JButton(batchApplyAction);
        batchApplyAction.setEnabled(isApplyToAllEnabled());

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonSave = new JButton(new SaveAsDefaultsAction());
        JButton buttonReset = new JButton(new ResetToDefaultsAction());
        JButton buttonUndo = new JButton(new UndoAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBatchApply).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBatchApply)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose,buttonBatchApply, buttonSave, buttonReset, buttonUndo);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    @Override
    public void resetToDefaults() 
    {
        for(Subeditor subedit: seriesSubeditors)
        {
            subedit.resetToDefaults();
        }
    }

    @Override
    public void saveAsDefaults() 
    {
        for(Subeditor subedit: seriesSubeditors)
        {
            subedit.saveAsDefaults();
        }		
    }

    @Override
    public void applyChangesToAll() 
    {
        for(Subeditor subedit: seriesSubeditors)
        {
            subedit.applyChangesToAll();
        }		
    }

    @Override
    public void undoChanges() 
    {
        for(Subeditor subedit: seriesSubeditors)
        {
            subedit.undoChanges();
        }	
    }

    @Override
    public Component getEditionComponent()
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        boolean enabled = false;

        for(Subeditor subeditor: seriesSubeditors)
        {
            boolean subeditorEnabled = subeditor.isApplyToAllEnabled();
            enabled = enabled || subeditorEnabled;
        }
        return enabled;
    }

    private class BatchApplyAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(editorPanel, "Series style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            undoChanges();
        }
    }

    private class SaveAsDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAsDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Use as defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(editorPanel, "Default series style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ResetToDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ResetToDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME, "Reset to defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            resetToDefaults();
        }
    }

    private class CloseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CloseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            hideRoot();
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(editorPanel);
        root.setVisible(false);
    }

    @Override
    public String getSubeditorName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {
        // TODO Auto-generated method stub

    }
}
