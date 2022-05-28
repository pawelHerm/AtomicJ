
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
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.AtomicJ;

public class LiveChartEditor
{
    private static final String SERIES = "Series";
    private static final String TOOLTIPS = "Tooltips";
    private static final String PLOT = "Plot";
    private static final String AXES = "Axes";
    private static final String TITLE = "Title";
    private static final String SCALE_BAR = "Scale bars";
    private static final String CHART = "Chart";

    private final BatchApplyAction batchApplyAction = new BatchApplyAction();
    private final BatchApplyAllAction batchApplyAllAction = new BatchApplyAllAction();
    private final UndoAction undoAction = new UndoAction();
    private final UndoAllAction undoAllAction = new UndoAllAction();
    private final SaveAsDefaultsAction saveAsDefaultsAction = new SaveAsDefaultsAction();
    private final SaveAsDefaultsAllAction saveAllAsDefaultsAction = new SaveAsDefaultsAllAction();
    private final ResetToDefaultsAction resetToDefaultsAction = new ResetToDefaultsAction();
    private final ResetAllToDefaultsAction resetAllToDefaultsAction = new ResetAllToDefaultsAction();

    private final MultipleAxesSubeditor axesSubeditor;
    private final MultipleScaleBarsSubeditor scaleBarsSubeditor;
    private final Map<String, Subeditor> legendSubeditors;

    private final Map<String, Subeditor> subeditors = new LinkedHashMap<>();

    private final JTabbedPane tabs;
    private final JDialog dialog;

    public LiveChartEditor(CompositeSubeditor seriesSubeditor, MultipleTooltipsSubeditor tooltipSubeditor, PlotSubeditor<?> plotSubeditor,
            MultipleAxesSubeditor axesSubeditor, Map<String, Subeditor> markerSubeditors, 
            RoamingTitleSubeditor textSubeditor, MultipleScaleBarsSubeditor scaleBarsSubeditor, 
            Map<String, Subeditor> legendSubeditors, ChartSubeditor chartSubeditor, Window parent)
    {
        this.dialog = new JDialog(parent, "Live chart style", ModalityType.APPLICATION_MODAL);

        this.axesSubeditor = axesSubeditor;
        this.scaleBarsSubeditor = scaleBarsSubeditor;
        this.legendSubeditors = legendSubeditors;

        this.tabs = buildTabPane(seriesSubeditor, tooltipSubeditor, plotSubeditor, axesSubeditor, markerSubeditors, textSubeditor, scaleBarsSubeditor, legendSubeditors, chartSubeditor);
        Container contentPane = dialog.getContentPane();   
        contentPane.add(tabs, BorderLayout.CENTER);     

        boolean batchApplyAllEnabled = shouldBatchApplyAllBeEnabled();
        batchApplyAllAction.setEnabled(batchApplyAllEnabled);

        JMenuBar menuBar = new JMenuBar();

        JMenu menuStyle = new JMenu("Style");
        menuStyle.setMnemonic(KeyEvent.VK_T);

        JMenuItem batchApplyItem = new JMenuItem(batchApplyAction);
        JMenuItem batchApplyAllItem = new JMenuItem(batchApplyAllAction);
        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem undoAllItem = new JMenuItem(undoAllAction);
        JMenuItem saveAsDefaultsItem = new JMenuItem(saveAsDefaultsAction);
        JMenuItem saveAllAsDefaultsItem = new JMenuItem(saveAllAsDefaultsAction);
        JMenuItem resetToDefaultsItem = new JMenuItem(resetToDefaultsAction);
        JMenuItem resetAllToDefaultsItem = new JMenuItem(resetAllToDefaultsAction);

        menuStyle.add(batchApplyItem);
        menuStyle.add(saveAsDefaultsItem);
        menuStyle.add(resetToDefaultsItem);
        menuStyle.add(undoItem);

        menuStyle.addSeparator();

        menuStyle.add(batchApplyAllItem);
        menuStyle.add(saveAllAsDefaultsItem);
        menuStyle.add(resetAllToDefaultsItem);
        menuStyle.add(undoAllItem);

        menuBar.add(menuStyle);

        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(1,1,1,1)));

        dialog.setJMenuBar(menuBar);

        initChangeListener();

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
    }


    public void setVisible(boolean b) {
        dialog.setVisible(b);
    }


    public void dispose() {
        dialog.dispose();        
    }

    private void initChangeListener()
    {
        tabs.addChangeListener(new ChangeListener() {          
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                int index = tabs.getSelectedIndex();
                String subeditorKey = tabs.getTitleAt(index);
                Subeditor currentSubeditor = subeditors.get(subeditorKey);

                if(currentSubeditor != null)
                {
                    boolean batchApplyAll = currentSubeditor.isApplyToAllEnabled();
                    batchApplyAction.setEnabled(batchApplyAll);
                }                
            }
        });
    }

    private JTabbedPane buildTabPane(CompositeSubeditor seriesSubeditor, MultipleTooltipsSubeditor tooltipSubeditor, PlotSubeditor<?> plotSubeditor,
            MultipleAxesSubeditor axesSubeditor, Map<String, Subeditor> markerSubeditors, 
            RoamingTitleSubeditor textSubeditor, MultipleScaleBarsSubeditor scaleBarsSubeditor, 
            Map<String, Subeditor> legendSubeditors, ChartSubeditor chartSubeditor)
    {
        JTabbedPane tabs = new JTabbedPane();

        List<JLabel> allLabels = new ArrayList<>();

        List<JLabel> labelsMarkers = new ArrayList<>();
        List<JLabel> labelsLegend = new ArrayList<>();

        tabs.add(seriesSubeditor.getEditionComponent(), SERIES);
        subeditors.put(SERIES, seriesSubeditor);

        if(tooltipSubeditor != null)
        {
            tabs.add(tooltipSubeditor.getEditionComponent(), TOOLTIPS);
            subeditors.put(TOOLTIPS, tooltipSubeditor);
        }

        tabs.add(plotSubeditor.getEditionComponent(), PLOT);
        subeditors.put(PLOT, plotSubeditor);

        tabs.add(axesSubeditor.getEditionComponent(), AXES);
        subeditors.put(AXES, axesSubeditor);

        for(Entry<String, Subeditor> entry :markerSubeditors.entrySet()) 
        {
            String name = entry.getKey();
            Subeditor markerSubeditor = entry.getValue();
            tabs.add(markerSubeditor.getEditionComponent(), name);
            subeditors.put(name, markerSubeditor);

            labelsMarkers.add(new JLabel(name));
        }

        tabs.add(textSubeditor.getEditionComponent(), TITLE);
        subeditors.put(TITLE, textSubeditor);

        if(scaleBarsSubeditor != null)
        {              
            tabs.add(scaleBarsSubeditor.getEditionComponent(), SCALE_BAR);
            subeditors.put(SCALE_BAR, scaleBarsSubeditor);
        }

        for(Entry<String, Subeditor> entry : legendSubeditors.entrySet()) 
        {
            String name = entry.getKey();
            Subeditor legendSubeditor = entry.getValue();
            tabs.add(legendSubeditor.getEditionComponent(), name);
            subeditors.put(name, legendSubeditor);

            labelsLegend.add(new JLabel(name));
        }

        tabs.add(chartSubeditor.getEditionComponent(), CHART);
        subeditors.put(CHART, chartSubeditor);

        JLabel tabSeries = new JLabel(SERIES);
        JLabel tabPlot = new JLabel(PLOT);
        JLabel tabTooltips = new JLabel(TOOLTIPS);

        JLabel tabAxes = new JLabel(AXES);
        JLabel tabTitle = new JLabel(TITLE);
        JLabel tabScaleBars = new JLabel(SCALE_BAR);
        JLabel tabChart = new JLabel(CHART);

        allLabels.add(tabSeries);
        allLabels.add(tabPlot);
        allLabels.add(tabAxes);
        allLabels.add(tabTitle);
        allLabels.add(tabScaleBars);
        allLabels.add(tabChart);
        allLabels.addAll(labelsLegend);
        allLabels.addAll(labelsMarkers);

        Dimension size = getLargestPreferredSize(allLabels);

        tabSeries.setPreferredSize(size);
        tabSeries.setHorizontalAlignment(SwingConstants.CENTER);
        tabTooltips.setPreferredSize(size);
        tabTooltips.setHorizontalAlignment(SwingConstants.CENTER);
        tabPlot.setPreferredSize(size);
        tabPlot.setHorizontalAlignment(SwingConstants.CENTER);
        tabAxes.setPreferredSize(size);
        tabAxes.setHorizontalAlignment(SwingConstants.CENTER);
        tabTitle.setPreferredSize(size);
        tabTitle.setHorizontalAlignment(SwingConstants.CENTER);

        tabScaleBars.setPreferredSize(size);
        tabScaleBars.setHorizontalAlignment(SwingConstants.CENTER);

        tabChart.setPreferredSize(size);
        tabChart.setHorizontalAlignment(SwingConstants.CENTER);

        int index = 0;

        tabs.setTabComponentAt(index++, tabSeries);
        if(tooltipSubeditor != null)
        {
            tabs.setTabComponentAt(index++, tabTooltips);
        }
        tabs.setTabComponentAt(index++, tabPlot);
        tabs.setTabComponentAt(index++, tabAxes);


        for(JLabel  label : labelsMarkers)
        {
            label.setPreferredSize(size);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            tabs.setTabComponentAt(index++, label);

        }

        tabs.setTabComponentAt(index++, tabTitle);

        if(scaleBarsSubeditor != null)
        {
            tabs.setTabComponentAt(index++, tabScaleBars);
        }

        for(JLabel  label : labelsLegend)
        {
            label.setPreferredSize(size);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            tabs.setTabComponentAt(index++, label);
        }         

        tabs.setTabComponentAt(index++, tabChart);       
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

        return tabs;
    }

    private boolean shouldBatchApplyAllBeEnabled()
    {
        boolean enabled = false;

        for(Subeditor subeditor : subeditors.values())
        {
            enabled = enabled || subeditor.isApplyToAllEnabled();
        }

        return enabled;
    }

    public void editDomainScaleBar()
    {
        if(scaleBarsSubeditor != null)
        {
            tabs.setSelectedComponent(scaleBarsSubeditor.getEditionComponent());
            scaleBarsSubeditor.editDomainScaleBar();
        }
    }

    public void editRangeScaleBar()
    {
        if(scaleBarsSubeditor != null)
        {
            tabs.setSelectedComponent(scaleBarsSubeditor.getEditionComponent());
            scaleBarsSubeditor.editRangeScaleBar();
        }
    }

    public void editTitle()
    {
        Subeditor subeditor = subeditors.get(TITLE);
        tabs.setSelectedComponent(subeditor.getEditionComponent());
    }

    public void editDomainAxis(int index)
    {
        tabs.setSelectedComponent(axesSubeditor.getEditionComponent());
        axesSubeditor.editDomainAxis(index);
    }

    public void editRangeAxis(int index)
    {
        tabs.setSelectedComponent(axesSubeditor.getEditionComponent());
        axesSubeditor.editRangeAxis(index);
    }

    public void editDepthAxis(int index)
    {
        tabs.setSelectedComponent(axesSubeditor.getEditionComponent());
        axesSubeditor.editDepthAxis(index);
    }

    public void editLegend(String legendNo)
    {
        Subeditor subeditor = legendSubeditors.get(legendNo);

        if(subeditor != null)
        {
            tabs.setSelectedComponent(subeditor.getEditionComponent());
        }
    }

    private Subeditor getCurrentSubeditor()
    {
        int index = tabs.getSelectedIndex();
        if(index < 0)
        {
            return null;
        }
        else
        {
            List<Subeditor> list = new ArrayList<>(subeditors.values());
            return list.get(index);
        }	
    }

    private void applyToBatch()
    {
        Subeditor subeditor = getCurrentSubeditor();
        if(subeditor != null)
        {
            subeditor.applyChangesToAll();
        }
    }

    private void applyAllToBatch()
    {
        for(Subeditor subeditor : subeditors.values())
        {
            subeditor.applyChangesToAll();
        }
    }

    private void undo()
    {
        Subeditor subeditor = getCurrentSubeditor();
        if(subeditor != null)
        {
            subeditor.undoChanges();
        }
    }

    private void undoAll()
    {
        for(Subeditor subeditor : subeditors.values())
        {
            subeditor.undoChanges();
        }
    }

    private void saveAsDefaults()
    {
        Subeditor subeditor = getCurrentSubeditor();
        if(subeditor != null)
        {
            subeditor.saveAsDefaults();
        }
    }

    private void saveAsDefaultsAll()
    {
        for(Subeditor subeditor : subeditors.values())
        {
            subeditor.saveAsDefaults();
        }
    }

    private void resetDefaults()
    {
        Subeditor subeditor = getCurrentSubeditor();
        if(subeditor != null)
        {
            subeditor.saveAsDefaults();
        }
    }

    private void resetToDefaultsAll()
    {
        for(Subeditor subeditor : subeditors.values())
        {
            subeditor.saveAsDefaults();
        }
    }

    private Dimension getLargestPreferredSize(List<JLabel> labels)
    {
        int maxWidth = 0;
        int maxHeight = 0;

        for(JLabel label : labels)
        {
            Dimension preferredSize = label.getPreferredSize();
            int width = (int)Math.rint(preferredSize.getWidth());
            int height = (int)Math.rint(preferredSize.getHeight());

            if(width > maxWidth)
            {
                maxWidth = width;
            }
            if(height > maxHeight)
            {
                maxHeight = height;
            }
        }

        Dimension size = new Dimension(maxWidth, maxHeight);
        return size;
    }

    private class BatchApplyAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyToBatch();
            JOptionPane.showMessageDialog(dialog, "Style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class BatchApplyAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Batch apply all");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyAllToBatch();
            JOptionPane.showMessageDialog(dialog, "Style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            undo();
        }
    }

    private class UndoAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Undo all");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            undoAll();
        }
    }

    private class SaveAsDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAsDefaultsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));       
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Use as defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(dialog, "Default chart style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class SaveAsDefaultsAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAsDefaultsAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Use all as defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            saveAsDefaultsAll();
            JOptionPane.showMessageDialog(dialog, "Default chart style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ResetToDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ResetToDefaultsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME, "Reset to defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            resetDefaults();
        }
    }

    private class ResetAllToDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ResetAllToDefaultsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Reset all to defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            resetToDefaultsAll();
        }
    }
}
