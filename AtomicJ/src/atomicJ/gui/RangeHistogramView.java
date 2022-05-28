
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

package atomicJ.gui;


import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.gui.RangeHistogramPanel.RangeHistogramPanelFactory;
import atomicJ.gui.histogram.AbstractHistogramView;
import atomicJ.gui.histogram.HistogramPlot;
import atomicJ.gui.histogram.HistogramResource;
import atomicJ.gui.histogram.HistogramSampleModel;
import atomicJ.statistics.BinningMethod;



public class RangeHistogramView extends AbstractHistogramView<RangeHistogramPanel<ChannelChart<HistogramPlot>>> implements RangeHistogramPanelSupervisor
{	
    private double initRangeMinimum;
    private double initRangeMaximum;

    private final HistogramSampleModel sampleModel;
    private GradientPaintReceiver rangeModel;	

    private final Action showBinningInfoAction = new ShowBinningInfoAction();

    private final Action automaticMarkerColorAction = new AutomaticMarkerColorAction();
    private final Action restoreFullRangeAction = new RestoreFullRangeAction();
    private final Action restoreAutomaticRangeAction = new RestoreAutomaticRangeAction();

    private final SimpleDialog dialogBinningInfo = new SimpleDialog(getAssociatedWindow(), "Binning info", ModalityType.MODELESS);

    private static final String KEY = "Current chart";

    public RangeHistogramView(final Window parent, QuantitativeSample sample, GradientPaintReceiver rangeModel, String title)
    {
        super(parent, RangeHistogramPanelFactory.getInstance(), title, ModalityType.MODELESS, false, false);

        dialogBinningInfo.add(getPanelStatistics(), BorderLayout.CENTER);
        dialogBinningInfo.pack();

        this.initRangeMinimum = rangeModel.getLowerBound();
        this.initRangeMaximum = rangeModel.getUpperBound();

        addChartPanelIfAbsent(KEY);	

        this.sampleModel = new HistogramSampleModel(sample);
        sampleModel.specifyBinningMethod(BinningMethod.FREEDMAN_DIACONIS);

        this.rangeModel = rangeModel;		

        HistogramPlot histogramPlot = sampleModel.getPlot();

        ChannelChart<HistogramPlot> chart = new RangeSelectionChart<>(getPanel(KEY), rangeModel, histogramPlot, Datasets.RANGE_SELECTION_HSTOGRAM_PLOT);	

        Map<String, HistogramSampleModel> sampleModels = new LinkedHashMap<>();
        sampleModels.put(KEY, sampleModel);

        HistogramResource resource = new HistogramResource(sampleModels, "Data histogram", "Data histogram");

        Map<String, ChannelChart<HistogramPlot>> charts = new LinkedHashMap<>();
        charts.put(KEY, chart);

        Map<HistogramResource, Map<String, ChannelChart<HistogramPlot>>> resourceChartMap = new LinkedHashMap<>();
        resourceChartMap.put(resource, charts);

        publishHistograms(resourceChartMap); 	

        buildMenu();
    }

    private void buildMenu()
    {
        JMenuBar menuBar = getMenuBar();

        JMenuItem itemShowBinningInfo = new JMenuItem(showBinningInfoAction);

        JMenu menuData = getDataMenu();
        menuData.add(itemShowBinningInfo);

        JMenu menuRange = new JMenu("Range");
        menuRange.setMnemonic(KeyEvent.VK_G);

        JCheckBoxMenuItem itemAutomaticMarkerColorItem = new JCheckBoxMenuItem(automaticMarkerColorAction);
        JMenuItem itemRestoreAutomaticRange = new JMenuItem(restoreAutomaticRangeAction); 
        JMenuItem itemRestoreFullRange = new JMenuItem(restoreFullRangeAction);

        menuRange.add(itemAutomaticMarkerColorItem);
        menuRange.add(itemRestoreAutomaticRange);
        menuRange.add(itemRestoreFullRange);

        menuBar.add(menuRange);
    }

    @Override
    public String getSelectedType()
    {
        return KEY;
    }

    @Override
    protected boolean includePanelStatistics()
    {
        return false;
    }

    private void showBinningInfo()
    {
        dialogBinningInfo.pack();
        //  setLocationRelativeTo(this);
        dialogBinningInfo.setVisible(true);
    }

    @Override
    public void handleNewChartPanel(RangeHistogramPanel<ChannelChart<HistogramPlot>> panel)
    {
        super.handleNewChartPanel(panel);
        panel.setRangeSupervisor(this);
    }

    //this may be useful, because rangeModel is usually a renderer, i.e. potentially a large class, and the RangeHistogramDialog may live quite long,
    //so it may be helpful to reclaim memory
    public void cleanUp()
    {
        this.rangeModel = null;
        RangeSelectionChart<?> chart = (RangeSelectionChart<?>) getSelectedChart();
        chart.cleanUp();
        clear();
    }

    public void setRangeModel(GradientPaintReceiver rangeModel)
    {        
        this.initRangeMinimum = rangeModel.getLowerBound();
        this.initRangeMaximum = rangeModel.getUpperBound();

        this.rangeModel = rangeModel;       

        updateSample();

        RangeSelectionChart<?> chart = (RangeSelectionChart<?>) getSelectedChart();
        chart.setRangeModel(rangeModel); 
    }

    public void updateSample()
    {
        QuantitativeSample paintedSample = rangeModel.getPaintedSample();
        updateSampleModel(paintedSample);
        ((RangeSelectionChart<?>)getSelectedChart()).updateAxisAutoRange();                    
    }

    public void setNewSample(QuantitativeSample sample)
    {
        updateSampleModel(sample);
        sampleModel.specifyBinningMethod(BinningMethod.FREEDMAN_DIACONIS);	
        ((RangeSelectionChart<?>)getSelectedChart()).updateAxisAutoRange();
    }

    @Override
    public void setRangeSelector(GradientRangeSelector rangeSelector)
    {
        rangeModel.setGradientRangeSelector(rangeSelector);
    }

    public void useAutomaticGradientColor(boolean automatic)
    {
        ChannelChart<?> selectedChart = getSelectedChart();
        if(selectedChart instanceof RangeSelectionChart)
        {
            ((RangeSelectionChart<?>)selectedChart).setUseReceiverGradient(automatic);
        }
    }


    public void cancel()
    {
        rangeModel.setLowerBound(initRangeMinimum);
        rangeModel.setUpperBound(initRangeMaximum);
        setVisible(false);
    }

    public void reset()
    {
        rangeModel.setLowerBound(initRangeMinimum);
        rangeModel.setUpperBound(initRangeMaximum);
    }

    public void approve()
    {
        setVisible(false);
    }

    @Override
    protected JPanel getButtonsPanel()
    {
        return buildSingleResourcesButtonPanel();
    }

    private JPanel buildSingleResourcesButtonPanel()
    {	
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 5));	
        JPanel buttonPanelOuter = new JPanel();

        JButton buttonOK = new JButton(new OKAction());
        JButton buttonReset = new JButton(new ResetAction());

        JButton buttonCancel = new JButton(new CancelAction());

        buttonPanel.add(buttonOK);
        buttonPanel.add(buttonReset);
        buttonPanel.add(buttonCancel);

        buttonPanelOuter.add(buttonPanel);
        buttonPanelOuter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanelOuter;
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
            approve();
        };
    }


    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {			
            reset();
        };
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
            cancel();
        };
    }

    private class RestoreAutomaticRangeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RestoreAutomaticRangeAction()
        {           
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Restore automatic");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setRangeSelector(GradientRangeSelector.AUTOMATIC);
        }
    }

    private class AutomaticMarkerColorAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AutomaticMarkerColorAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Automatic marker color");

            putValue(SELECTED_KEY, true);

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean automatic = (boolean) getValue(SELECTED_KEY);

            useAutomaticGradientColor(automatic);
        }
    }


    private class RestoreFullRangeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RestoreFullRangeAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,	InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Restore full");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setRangeSelector(GradientRangeSelector.FULL);       	
        }
    }

    private class ShowBinningInfoAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowBinningInfoAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Binning info");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showBinningInfo();       	
        }
    }
}
