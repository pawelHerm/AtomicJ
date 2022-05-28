
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
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.CustomizableXYBasePlot;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.ScaleBar;


public class MultipleScaleBarsSubeditor implements Subeditor 
{
    private final JTabbedPane tabs = new JTabbedPane();
    private final JPanel editorPanel = new JPanel(new BorderLayout());

    private final List<ScaleBarSubeditor> subeditors = new ArrayList<>();
    private final List<CustomizableXYBasePlot> boundedPlots;

    public MultipleScaleBarsSubeditor(CustomizableXYPlot plot, List<CustomizableXYBasePlot> boundedPlots) 
    {
        this.boundedPlots = boundedPlots;

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tabs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        CustomizableNumberAxis domainAxis = (CustomizableNumberAxis)plot.getDomainAxis();
        ScaleBar domainScaleBar = plot.getDomainScaleBar();

        ScaleBarSubeditor domainAxisSubeditor = new ScaleBarSubeditor(domainScaleBar, getBoundedDomainScaleBars(), domainAxis);
        domainAxisSubeditor.setEditionComponetBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        subeditors.add(domainAxisSubeditor);

        tabs.add("Domain scale bar", domainAxisSubeditor.getEditionComponent());

        CustomizableNumberAxis rangeAxis = (CustomizableNumberAxis)plot.getRangeAxis();
        ScaleBar rangeScalebar = plot.getRangeScaleBar();
        ScaleBarSubeditor rangeAxisSubeditor = new ScaleBarSubeditor(rangeScalebar, getBoundedRangeScaleBars(), rangeAxis);
        rangeAxisSubeditor.setEditionComponetBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        subeditors.add(rangeAxisSubeditor);

        tabs.add("Range scale bar", rangeAxisSubeditor.getEditionComponent());

        editorPanel.add(tabs,BorderLayout.CENTER);

        JPanel buttons = buildButtonPanel();
        editorPanel.add(buttons, BorderLayout.SOUTH);
    }

    public void editDomainScaleBar()
    {
        tabs.setSelectedIndex(0);
    }

    public void editRangeScaleBar()
    {
        tabs.setSelectedIndex(1);
    }

    private List<ScaleBar> getBoundedDomainScaleBars()
    {
        List<ScaleBar> scaleBars = new ArrayList<>();

        for(CustomizableXYBasePlot p: boundedPlots)
        {
            if(p instanceof CustomizableXYPlot)
            {
                ScaleBar scaleBar = ((CustomizableXYPlot) p).getDomainScaleBar();
                scaleBars.add(scaleBar);
            }
        }
        return scaleBars;
    }

    private List<ScaleBar> getBoundedRangeScaleBars()
    {
        List<ScaleBar> scaleBars = new ArrayList<>();

        for(CustomizableXYBasePlot p: boundedPlots)
        {
            if(p instanceof CustomizableXYPlot)
            {
                ScaleBar scaleBar = ((CustomizableXYPlot) p).getRangeScaleBar();
                scaleBars.add(scaleBar);
            }
        }
        return scaleBars;
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

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(editorPanel);
        root.setVisible(false);
    } 

    @Override
    public void resetToDefaults() 
    {
        for(ScaleBarSubeditor subeditor: subeditors)
        {
            subeditor.resetToDefaults();
        }
    }

    @Override
    public void saveAsDefaults() 
    {
        for(ScaleBarSubeditor subeditor: subeditors)
        {
            subeditor.saveAsDefaults();
        }
    }

    @Override
    public void applyChangesToAll() 
    {
        for(ScaleBarSubeditor subeditor: subeditors)
        {
            subeditor.applyChangesToAll();
        }
    }

    @Override
    public void undoChanges() 
    {
        for(ScaleBarSubeditor subeditor: subeditors)
        {
            subeditor.undoChanges();
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
        return boundedPlots.size() > 1;
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
            JOptionPane.showMessageDialog(editorPanel, "Scale bar style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(editorPanel, "Default scale bar style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
