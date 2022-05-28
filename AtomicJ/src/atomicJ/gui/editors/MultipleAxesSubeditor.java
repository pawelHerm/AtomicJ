
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
import java.util.prefs.Preferences;

import javax.swing.*;

import org.jfree.chart.axis.Axis;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.AxisSource;
import atomicJ.gui.AxisType;
import atomicJ.gui.CustomizableNumberAxis;

public class MultipleAxesSubeditor implements Subeditor 
{
    private static final String DOMAIN_AXIS = "Domain axis";
    private static final String RANGE_AXIS = "Range axis";
    private static final String DEPTH_AXIS = "Depth axis";

    private final Action linkStyleAction = new BoundStyleAction();

    private final JTabbedPane tabs = new JTabbedPane();

    private final List<AxisSubeditor<?>> subeditors = new ArrayList<>();

    private final List<AxisSubeditor<?>> domainSubeditors = new ArrayList<>();
    private final List<AxisSubeditor<?>> rangeSubeditors = new ArrayList<>();
    private final List<AxisSubeditor<?>> depthSubeditors = new ArrayList<>();

    private final boolean boundedAxes;
    private boolean boundChartAxes = false;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    public MultipleAxesSubeditor(AxisSource axisSource, List<AxisSource> boundedAxisSources) 
    {
        this.boundedAxes = boundedAxisSources.size() > 1;

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tabs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        initializeBoundedDomainAxes(axisSource, boundedAxisSources);
        initializeBoundedRangeAxes(axisSource, boundedAxisSources);
        initializeBoundedDepthAxes(axisSource, boundedAxisSources);

        boundSubeditors();   

        JPanel panelLinkStyle = buildLinkPanel();

        editorPanel.add(tabs,BorderLayout.CENTER);
        editorPanel.add(panelLinkStyle, BorderLayout.WEST);

        JPanel buttons = buildButtonPanel();
        editorPanel.add(buttons, BorderLayout.SOUTH);
    }

    private AxisSubeditor<?> getAxisSubeditor(Axis axis, AxisType axisType, Preferences pref)
    {
        AxisSubeditor<?> subeditor = null;

        if(axis instanceof CustomizableNumberAxis)
        {
            subeditor = new NumberAxisSubeditor((CustomizableNumberAxis) axis, axisType);
        }
        else
        {
            subeditor = new AxisSubeditor<>(axis, axisType, pref);
        }

        return subeditor;
    }

    private void boundSubeditors()
    {
        int n = subeditors.size();
        for(int i = 0; i<n; i++)
        {
            AxisSubeditor<?> subEditor = subeditors.get(i);

            for(int j = 0; j<i; j++)
            {
                AxisSubeditor<?> se = subeditors.get(j);
                se.addBoundedSubeditor(subEditor);
            }

            for(int j = i + 1; j<n; j++)
            {
                AxisSubeditor<?> se = subeditors.get(j);
                se.addBoundedSubeditor(subEditor);
            }
        }
    }

    public boolean isBoundChartAxes()
    {
        return boundChartAxes;
    }

    public void setBoundChartAxes(boolean boundChartAxes)
    {
        this.boundChartAxes = boundChartAxes;

        for(AxisSubeditor<?> subEditor : subeditors)
        {
            subEditor.setBoundChartAxes(boundChartAxes);
        }
    }

    public void editDomainAxis(int index)
    {
        AxisSubeditor<?> subeditor = domainSubeditors.get(index);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }    
    }

    public void editRangeAxis(int index)
    {
        AxisSubeditor<?> subeditor = rangeSubeditors.get(index);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }
    }

    public void editDepthAxis(int index)
    {
        AxisSubeditor<?> subeditor = depthSubeditors.get(index);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }
    }

    private void initializeBoundedDomainAxes(AxisSource axisSource, List<AxisSource> boundedAxisSources)
    {
        int axesCount = axisSource.getDomainAxisCount();
        boolean multipleAxes = axesCount > 1;

        for(int i = 0; i<axesCount; i++)
        {
            List<Axis> boundedAxes = getBoundedDomainAxes(boundedAxisSources, i);
            Axis axis = axisSource.getDomainAxis(i);
            Preferences pref = axisSource.getDomainAxisPreferences(i);

            AxisSubeditor<?> subeditor = null;
            if(axis != null)
            {
                subeditor = getAxisSubeditor(axis, AxisType.DOMAIN, pref);
                subeditor.addBoundedAxes(boundedAxes);

                subeditor.setEmptyBorderForEditionComponent(2, 2, 2, 2);
                subeditors.add(subeditor);

                String name = multipleAxes ? DOMAIN_AXIS + " " + axisSource.getDomainAxisName(i): DOMAIN_AXIS;               
                tabs.add(name, subeditor.getEditionComponent());
            }        

            domainSubeditors.add(subeditor);
        }
    }

    private void initializeBoundedRangeAxes(AxisSource axisSource, List<AxisSource> boundedAxisSources)
    {
        int axesCount = axisSource.getRangeAxisCount();
        boolean multipleAxes = axesCount > 1;

        for(int i = 0; i<axesCount; i++)
        {
            List<Axis> boundedAxes = getBoundedRangeAxes(boundedAxisSources, i);
            Axis axis = axisSource.getRangeAxis(i);
            Preferences pref = axisSource.getRangeAxisPreferences(i);

            AxisSubeditor<?> subeditor = null;

            if(axis != null)
            {
                subeditor = getAxisSubeditor(axis, AxisType.RANGE, pref);
                subeditor.addBoundedAxes(boundedAxes);

                subeditor.setEmptyBorderForEditionComponent(2, 2, 2, 2);
                subeditors.add(subeditor);

                String name = multipleAxes ? RANGE_AXIS + " " + axisSource.getRangeAxisName(i): RANGE_AXIS;               
                tabs.add(name, subeditor.getEditionComponent());
            }    

            rangeSubeditors.add(subeditor);
        }
    }

    private void initializeBoundedDepthAxes(AxisSource axisSource, List<AxisSource> boundedAxisSources)
    {
        int axesCount = axisSource.getDepthAxisCount();
        boolean multipleAxes = axesCount > 1;

        for(int i = 0; i<axesCount; i++)
        {
            List<Axis> boundedAxes = getBoundedDepthAxes(boundedAxisSources, i);
            Axis axis = axisSource.getDepthAxis(i);
            Preferences pref = axisSource.getDepthAxisPreferences(i);

            AxisSubeditor<?> subeditor = null;

            if(axis != null)
            {
                subeditor = getAxisSubeditor(axis, AxisType.DEPTH, pref);
                subeditor.addBoundedAxes(boundedAxes);

                subeditor.setEmptyBorderForEditionComponent(2, 2, 2, 2);
                subeditors.add(subeditor);

                String name = multipleAxes ? DEPTH_AXIS + " " + axisSource.getDepthAxisName(i): DEPTH_AXIS;               
                tabs.add(name, subeditor.getEditionComponent());
            }          

            depthSubeditors.add(subeditor);
        }
    }


    private List<Axis> getBoundedDomainAxes(List<AxisSource> boundedAxisSources, int index)
    {
        List<Axis> axes = new ArrayList<>();

        for(AxisSource p: boundedAxisSources)
        {
            if(p.getDomainAxisCount()>index)
            {
                Axis axis = p.getDomainAxis(index);               
                axes.add(axis);
            }       
        }
        return axes;
    }

    private List<Axis> getBoundedRangeAxes(List<AxisSource> boundedAxisSources, int index)
    {
        List<Axis> axes = new ArrayList<>();

        for(AxisSource p: boundedAxisSources)
        {
            if(p.getRangeAxisCount()>index)
            {
                Axis axis = p.getRangeAxis(index);               
                axes.add(axis);
            }       
        }
        return axes;
    }

    private List<Axis> getBoundedDepthAxes(List<AxisSource> boundedAxisSources, int index)
    {
        List<Axis> axes = new ArrayList<>();

        for(AxisSource p: boundedAxisSources)
        {
            if(p.getDepthAxisCount()>index)
            {
                Axis axis = p.getDepthAxis(index);               
                axes.add(axis);
            }       
        }
        return axes;
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
            setBoundChartAxes(bound);
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
        return boundedAxes;
    }

    @Override
    public String getSubeditorName() {
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {

    }

}
