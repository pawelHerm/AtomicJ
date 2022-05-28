
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
import atomicJ.gui.TooltipManagerSource;
import atomicJ.gui.TooltipStyleManager;

public class MultipleTooltipsSubeditor implements Subeditor 
{
    private static final String DOMAIN_TOOLTIP = "Domain tooltip";
    private static final String RANGE_TOOLTIP = "Range tooltip";
    private static final String DEPTH_TOOLIP = "Depth tooltip";

    private final BoundStyleAction linkStyleAction = new BoundStyleAction();

    private final JTabbedPane tabs = new JTabbedPane();

    private final List<TooltipSubeditor> subeditors = new ArrayList<>();

    private final List<TooltipSubeditor> domainSubeditors = new ArrayList<>();
    private final List<TooltipSubeditor> rangeSubeditors = new ArrayList<>();
    private final List<TooltipSubeditor> depthSubeditors = new ArrayList<>();

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    private final boolean boundedSources;
    private boolean boundInternalTooltips = false;

    public MultipleTooltipsSubeditor(TooltipManagerSource tooltipSource, List<TooltipManagerSource> boundedTooltipSources) 
    {
        this.boundedSources = boundedTooltipSources.size() > 1;

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tabs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        initializeBoundedDomainTooltips(tooltipSource, boundedTooltipSources);
        initializeBoundedRangeTooltips(tooltipSource, boundedTooltipSources);
        initializeBoundedDepthAxes(tooltipSource, boundedTooltipSources);

        boundSubeditors();   

        JPanel panelLinkStyle = buildLinkPanel();

        editorPanel.add(tabs,BorderLayout.CENTER);
        editorPanel.add(panelLinkStyle, BorderLayout.WEST);

        JPanel buttons = buildButtonPanel();
        editorPanel.add(buttons, BorderLayout.SOUTH);
    }

    private TooltipSubeditor getTooltipSubeditor(TooltipStyleManager styleManager)
    {
        TooltipSubeditor subeditor = new TooltipSubeditor(styleManager);

        return subeditor;
    }

    private void boundSubeditors()
    {
        int n = subeditors.size();

        for(int i = 0; i<n; i++)
        {
            TooltipSubeditor subEditor = subeditors.get(i);

            for(int j = 0; j<i; j++)
            {
                TooltipSubeditor se = subeditors.get(j);
                se.addBoundedSubeditor(subEditor);
            }

            for(int j = i + 1; j<n; j++)
            {
                TooltipSubeditor se = subeditors.get(j);
                se.addBoundedSubeditor(subEditor);
            }
        }
    }

    public boolean isBoundInternalTooltips()
    {
        return boundInternalTooltips;
    }

    public void setBoundTooltips(boolean boundChartTooltips)
    {
        this.boundInternalTooltips = boundChartTooltips;

        for(TooltipSubeditor subEditor : subeditors)
        {
            subEditor.setBoundTooltips(boundChartTooltips);
        }
    }

    public void editDomainTooltips(int index)
    {
        TooltipSubeditor subeditor = domainSubeditors.get(index);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }    
    }

    public void editRangeTooltips(int index)
    {
        TooltipSubeditor subeditor = rangeSubeditors.get(index);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }
    }

    public void editDepthTooltip(int index)
    {
        TooltipSubeditor subeditor = depthSubeditors.get(index);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }
    }

    private void initializeBoundedDomainTooltips(TooltipManagerSource tooltipSource, List<TooltipManagerSource> boundedAxisSources)
    {
        int tooltipCount = tooltipSource.getDomainTooltipManagerCount();

        boolean multipleTooltips = tooltipCount > 1;

        for(int i = 0; i<tooltipCount; i++)
        {
            List<TooltipStyleManager> boundedManagers = getBoundedDomainManagers(boundedAxisSources, i);
            TooltipStyleManager manager = tooltipSource.getDomainTooltipManager(i);

            TooltipSubeditor subeditor = null;
            if(manager != null)
            {
                subeditor = getTooltipSubeditor(manager);
                subeditor.addTypeBoundedManagers(boundedManagers);

                subeditor.setEditionComponetBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                subeditors.add(subeditor);

                String name = multipleTooltips ? DOMAIN_TOOLTIP + " " + manager.getName(): DOMAIN_TOOLTIP;               
                tabs.add(name, subeditor.getEditionComponent());
            }        

            domainSubeditors.add(subeditor);
        }
    }

    private void initializeBoundedRangeTooltips(TooltipManagerSource tooltipSource, List<TooltipManagerSource> boundedAxisSources)
    {
        int tooltipCount = tooltipSource.getRangeTooltipManagerCount();

        boolean multipleTooltips = tooltipCount > 1;

        for(int i = 0; i<tooltipCount; i++)
        {
            List<TooltipStyleManager> boundedManagers = getBoundedRangeManagers(boundedAxisSources, i);
            TooltipStyleManager manager = tooltipSource.getRangeTooltipManager(i);

            TooltipSubeditor subeditor = null;

            if(manager != null)
            {
                subeditor = getTooltipSubeditor(manager);
                subeditor.addTypeBoundedManagers(boundedManagers);

                subeditor.setEditionComponetBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                subeditors.add(subeditor);

                String name = multipleTooltips ? RANGE_TOOLTIP + " " + manager.getName(): RANGE_TOOLTIP;               
                tabs.add(name, subeditor.getEditionComponent());
            }    

            rangeSubeditors.add(subeditor);
        }
    }

    private void initializeBoundedDepthAxes(TooltipManagerSource axisSource, List<TooltipManagerSource> boundedAxisSources)
    {
        int tooltipCount = axisSource.getDepthTooltipManagerCount();
        boolean multipleTooltips = tooltipCount > 1;

        for(int i = 0; i<tooltipCount; i++)
        {
            List<TooltipStyleManager> boundedManagers = getBoundedDepthManagers(boundedAxisSources, i);
            TooltipStyleManager manager = axisSource.getDepthTooltipManager(i);

            TooltipSubeditor subeditor = null;

            if(manager != null)
            {
                subeditor = getTooltipSubeditor(manager);
                subeditor.addTypeBoundedManagers(boundedManagers);

                subeditor.setEditionComponetBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                subeditors.add(subeditor);

                String name = multipleTooltips ? DEPTH_TOOLIP + " " + manager.getName(): DEPTH_TOOLIP;               
                tabs.add(name, subeditor.getEditionComponent());
            }          

            depthSubeditors.add(subeditor);
        }
    }


    private List<TooltipStyleManager> getBoundedDomainManagers(List<TooltipManagerSource> boundedTooltipSources, int index)
    {
        List<TooltipStyleManager> managers = new ArrayList<>();

        for(TooltipManagerSource p: boundedTooltipSources)
        {
            if(p.getDomainTooltipManagerCount()>index)
            {
                TooltipStyleManager manager = p.getDomainTooltipManager(index);              
                managers.add(manager);
            }       
        }

        return managers;
    }

    private List<TooltipStyleManager> getBoundedRangeManagers(List<TooltipManagerSource> boundedTooltipSources, int index)
    {
        List<TooltipStyleManager> managers = new ArrayList<>();

        for(TooltipManagerSource p: boundedTooltipSources)
        {
            if(p.getRangeTooltipManagerCount()>index)
            {
                TooltipStyleManager manager = p.getRangeTooltipManager(index);               
                managers.add(manager);
            }       
        }
        return managers;
    }

    private List<TooltipStyleManager> getBoundedDepthManagers(List<TooltipManagerSource> boundedTooltipSources, int index)
    {
        List<TooltipStyleManager> managers = new ArrayList<>();

        for(TooltipManagerSource p: boundedTooltipSources)
        {
            if(p.getDepthTooltipManagerCount()>index)
            {
                TooltipStyleManager manager = p.getDepthTooltipManager(index);           
                managers.add(manager);
            }       
        }
        return managers;
    }

    private JPanel buildLinkPanel()
    {
        JToggleButton buttonLinkStyle = new JToggleButton(linkStyleAction);
        buttonLinkStyle.setHideActionText(true);
        buttonLinkStyle.setMargin(new Insets(3, 5, 3, 5));

        JPanel panelLink = new JPanel();
        panelLink.add(buttonLinkStyle);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(
                toolkit.getImage("Resources/chainLinks.png"));

        buttonLinkStyle.setSelectedIcon(icon);

        panelLink.setLayout(new BoxLayout(panelLink, BoxLayout.PAGE_AXIS));
        panelLink.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelLink.add(Box.createVerticalGlue());
        panelLink.add(buttonLinkStyle);
        panelLink.add(Box.createVerticalGlue());

        return panelLink;
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

    private class BoundStyleAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public BoundStyleAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/chainLinksBroken.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Link style");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean bound = (boolean) getValue(SELECTED_KEY);
            setBoundTooltips(bound);
        }
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
            JOptionPane.showMessageDialog(editorPanel, "Axes style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(editorPanel, "Default axis style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
    public void resetToDefaults() 
    {
        for(Subeditor subeditor: subeditors)
        {
            subeditor.resetToDefaults();
        }
    }

    @Override
    public void saveAsDefaults() 
    {
        for(Subeditor subeditor: subeditors)
        {
            subeditor.saveAsDefaults();
        }
    }

    @Override
    public void applyChangesToAll() 
    {
        for(Subeditor subeditor: subeditors)
        {
            subeditor.applyChangesToAll();
        }
    }

    @Override
    public void undoChanges() 
    {
        for(Subeditor subeditor: subeditors)
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
        return boundedSources;
    }

    @Override
    public String getSubeditorName() {
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {

    }

}
