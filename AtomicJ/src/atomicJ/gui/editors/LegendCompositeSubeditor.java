
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.RoamingColorGradientLegend;
import atomicJ.gui.RoamingLegend;
import atomicJ.utilities.Validation;


public class LegendCompositeSubeditor implements Subeditor
{
    private final List<Subeditor> subeditors = new ArrayList<>();

    private final List<RoamingLegend> boundedLegends;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    public LegendCompositeSubeditor(RoamingLegend legend, List<RoamingLegend> boundedLegends)
    {
        this.boundedLegends = Validation.requireNonNullParameterName(boundedLegends, "boundedLegends");

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        Subeditor positionEditor = new LegendPositionSubeditor(legend, boundedLegends);
        Subeditor styleSubeditor = new LegendStyleSubeditor(legend, boundedLegends);

        subeditors.add(positionEditor);
        subeditors.add(styleSubeditor);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        tabbedPane.add("Position", positionEditor.getEditionComponent());
        tabbedPane.add("Style", styleSubeditor.getEditionComponent());

        if(legend instanceof RoamingColorGradientLegend)
        {
            List<RoamingColorGradientLegend> bundedPaintScaleLegends = new ArrayList<>();
            for(RoamingLegend leg: boundedLegends)
            {
                if(leg instanceof RoamingColorGradientLegend)
                {
                    bundedPaintScaleLegends.add((RoamingColorGradientLegend)leg);
                }
            }
            Subeditor stripSubeditor = new LegendStripSubeditor((RoamingColorGradientLegend)legend, bundedPaintScaleLegends);
            subeditors.add(stripSubeditor);

            tabbedPane.add("Strip", stripSubeditor.getEditionComponent());
        }        

        JPanel buttonPanel = buildButtonPanel();

        editorPanel.add(buttonPanel, BorderLayout.SOUTH);
        editorPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        BatchApplyAction batchApplyAction = new BatchApplyAction();
        JButton buttonBatchApplyAll = new JButton(batchApplyAction);
        batchApplyAction.setEnabled(isApplyToAllEnabled());

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonSave = new JButton(new SaveAsDefaultsAction());
        JButton buttonReset = new JButton(new ResetToDefaultsAction());
        JButton buttonUndo = new JButton(new UndoAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBatchApplyAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBatchApplyAll)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose, buttonBatchApplyAll, buttonSave, buttonReset, buttonUndo);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }


    @Override
    public void applyChangesToAll() 
    {
        for(Subeditor s: subeditors)
        {
            s.applyChangesToAll();
        }
    }

    @Override
    public void undoChanges() 
    {
        for(Subeditor s: subeditors)
        {
            s.undoChanges();		
        }
    }

    @Override
    public void resetToDefaults() 
    {
        for(Subeditor s: subeditors)
        {
            s.resetToDefaults();
        }
    }

    @Override
    public void saveAsDefaults() 
    {
        for(Subeditor s: subeditors)
        {
            s.saveAsDefaults();
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
        return boundedLegends.size()>1;
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
            JOptionPane.showMessageDialog(editorPanel, "Legend style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(editorPanel, "Default legend style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
}
