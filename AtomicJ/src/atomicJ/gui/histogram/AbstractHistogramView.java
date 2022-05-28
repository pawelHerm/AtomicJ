
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

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSample;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.DistanceGeometryTableModel;
import atomicJ.gui.FlexibleNumericalTableView;
import atomicJ.gui.MultipleNumericalTableView;
import atomicJ.gui.OrderedNumericalTable;
import atomicJ.gui.RawDataTableModel;
import atomicJ.gui.ResourceXYView;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.SubPanel;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.SampleSelectionModel;
import atomicJ.gui.statistics.StatisticsView;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.gui.statistics.UpdateableStatisticsModel;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.DistributionFit;


public class AbstractHistogramView <E extends BasicHistogramPanel<ChannelChart<HistogramPlot>>>
extends ResourceXYView<HistogramResource, ChannelChart<HistogramPlot>, E>
implements BasicHistogramPanelSupervisor, HistogramDestination, PropertyChangeListener
{
    private static final NumberFormat DEFAULT_FORMAT = NumberFormat.getInstance(Locale.US);

    static 
    {
        DEFAULT_FORMAT.setMaximumFractionDigits(4);
    }

    private static final Preferences PREF = Preferences.userNodeForPackage(BasicHistogramView.class).node("HistogramDialog");;

    private final JLabel labelFit = new JLabel("Fit");
    private final JLabel labelFitName = new JLabel("");
    private final JLabel labelMu = new JLabel("\u03BC");
    private final JLabel labelSigma = new JLabel("\u03C3");

    private final JLabel labelData = new JLabel("Data");
    private final JLabel labelCount = new JLabel("Count");
    private final JLabel labelMean = new JLabel("Mean");
    private final JLabel labelSD = new JLabel("SD");

    private final JLabel labelBins = new JLabel("Bins");
    private final JLabel labelBinCount = new JLabel("Count");
    private final JLabel labelBinWidth = new JLabel("Width");
    private final JLabel labelRange = new JLabel("Range");

    private final JLabel fieldFitName = new JLabel();
    private final JLabel fieldMu = new JLabel();
    private final JLabel fieldSigma = new JLabel();

    private final JLabel fieldCount = new JLabel();
    private final JLabel fieldMean = new JLabel();
    private final JLabel fieldSD = new JLabel();

    private final JLabel labelBinCountValue = new JLabel();
    private final JLabel labelBinWdthValue = new JLabel();
    private final JLabel labelBinRangeValue = new JLabel();

    private final Action showRawDataAction = new ShowRawDataAction();
    private final Action showStatisticsAction = new ShowStatisticsAction();
    private final Action modifyBinningAction = new ChangeBinningAction();

    private final JMenuItem modifyBinningItem = new JMenuItem(modifyBinningAction);
    private final JMenuItem rawDataItem = new JMenuItem(showRawDataAction);
    private final JMenuItem statisticsItem = new JMenuItem(showStatisticsAction); 

    private final HistogramReBinningDialog rebinningDialog = new HistogramReBinningDialog("Binning", getAssociatedWindow());
    private final MultipleSelectionWizard<String> rawDataWizard = new MultipleSelectionWizard<>(getAssociatedWindow(), "Raw data assistant");

    private JPanel panelStatistics;
    private final JMenu dataMenu;

    private final FlexibleNumericalTableView distanceMeasurementsDialog = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Image distance measurements");
    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(getAssociatedWindow());

    public AbstractHistogramView(final Window parent, AbstractChartPanelFactory<E> factory, String title)
    {
        this(parent, factory, title, ModalityType.MODELESS, true, true);	
    }

    public AbstractHistogramView(final Window parent, AbstractChartPanelFactory<E> factory, String title, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        super(parent, factory, title, PREF, modalityType, allowsMultipleResources, allowsMultipleTypes);	

        initNumericalFields();

        JPanel panelCharts = buildHistogramPanel();

        if(allowsMultipleResources)
        {
            JPanel panelResources = getPanelResources();

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panelResources,panelCharts);
            splitPane.setOneTouchExpandable(true);

            setCenterComponent(splitPane);			
        }
        else
        {
            panelCharts.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setCenterComponent(panelCharts);			
        }

        setSouthComponent(getButtonsPanel());

        controlForResourceEmptinessPrivate(isEmpty());

        JMenuBar menuBar = getMenuBar();

        this.dataMenu = new JMenu("Data");
        dataMenu.setMnemonic(KeyEvent.VK_D);
        dataMenu.add(rawDataItem);
        dataMenu.add(statisticsItem); 
        dataMenu.add(modifyBinningItem);

        menuBar.add(dataMenu);
    }

    @Override
    public void controlForResourceEmptiness(boolean empty) 
    {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    private void controlForResourceEmptinessPrivate(boolean empty)
    {
        boolean enabled = !empty;

        showRawDataAction.setEnabled(enabled);
        showStatisticsAction.setEnabled(enabled);
        modifyBinningAction.setEnabled(enabled);
    }

    protected JPanel getPanelStatistics()
    {
        return panelStatistics;
    }

    protected JPanel getButtonsPanel()
    {
        return getMultipleResourcesPanelButtons();
    }

    protected JMenu getDataMenu()
    {
        return dataMenu;
    }

    private void initNumericalFields()
    {
        labelFit.setFont(labelFit.getFont().deriveFont(Font.BOLD));
        labelData.setFont(labelData.getFont().deriveFont(Font.BOLD));
        labelBins.setFont(labelBins.getFont().deriveFont(Font.BOLD));

        setFitStatisticsVisible(false);
        setDataStatisticsVisible(false);
        setBinningDetailsVisible(false);
    }

    private void setFitStatisticsVisible(boolean visible)
    {
        labelFit.setVisible(visible);
        labelFitName.setVisible(visible);
        labelMu.setVisible(visible);
        labelSigma.setVisible(visible);

        fieldFitName.setVisible(visible);
        fieldMu.setVisible(visible);
        fieldSigma.setVisible(visible);
    }

    private void setDataStatisticsVisible(boolean visible)
    {
        labelData.setVisible(visible);
        labelCount.setVisible(visible);
        labelMean.setVisible(visible);
        labelSD.setVisible(visible);

        fieldCount.setVisible(visible);
        fieldMean.setVisible(visible);
        fieldSD.setVisible(visible);
    }

    private void setBinningDetailsVisible(boolean visible)
    {
        labelBins.setVisible(visible);
        labelBinCount.setVisible(visible);
        labelBinWidth.setVisible(visible);
        labelRange.setVisible(visible);

        labelBinCountValue.setVisible(visible);
        labelBinWdthValue.setVisible(visible);
        labelBinRangeValue.setVisible(visible);
    }

    @Override
    public void handleNewChartPanel(E panel)
    {
        super.handleNewChartPanel(panel);          
        panel.setBasicSupervisor(this);
    }

    @Override
    public void handleChangeOfSelectedResource(HistogramResource resourceOld, HistogramResource resourceNew)
    {
        super.handleChangeOfSelectedResource(resourceOld, resourceNew);

        String selectedType = getSelectedType();
        HistogramSampleModel sampleModelOld = resourceOld == null ? null : resourceOld.getModel(selectedType);
        HistogramSampleModel sampleModelNew = resourceNew == null ? null : resourceNew.getModel(selectedType);

        setSampleModel(sampleModelOld, sampleModelNew);

        updateRebinningDialog();
    }

    @Override
    protected void handleChangeOfSelectedType(String typeOld, String typeNew)
    {	
        super.handleChangeOfSelectedType(typeOld, typeNew);

        if(!Objects.equals(typeOld, typeNew))
        {
            HistogramResource resource = getSelectedResource();

            HistogramSampleModel sampleModelOld = null;
            HistogramSampleModel sampleModelNew = null;

            if(resource != null && typeOld != null)
            {
                sampleModelOld = resource.getModel(typeOld);
            }
            if(resource != null && typeNew != null)
            {			
                sampleModelNew = resource.getModel(typeNew);
            }		
            setSampleModel(sampleModelOld, sampleModelNew);

            updateRebinningDialog();
        }

    }

    private void setSampleModel(HistogramSampleModel sampleModelOld, HistogramSampleModel sampleModelNew)	
    {	
        if(!Objects.equals(sampleModelOld, sampleModelNew))
        {
            if(sampleModelOld != null)
            {
                sampleModelOld.removePropertyChangeListener(this);
            }
            if(sampleModelNew != null)
            {
                sampleModelNew.addPropertyChangeListener(this);
            }
            pullSampleModel(sampleModelNew);
        }		
    }

    public void updateSampleModel(QuantitativeSample sample)
    {
        updateSampleModel(sample, getSelectedType());
    }

    public void updateSampleModel(QuantitativeSample sample, String type)
    {
        HistogramResource resource = getSelectedResource();

        updateSampleModel(sample, resource, type);
    }

    public void updateSampleModel(QuantitativeSample sample, HistogramResource resource, String type)
    {
        HistogramSampleModel sampleModel = resource.getModel(type);

        if(sampleModel != null)
        {
            sampleModel.setSample(sample);
            updateHistogramChart(resource, type);

            if(type == getSelectedType())
            {
                pullSampleModel(sampleModel);
            }
        }		
        else
        {
            //we must add sample model

            addOrReplaceSampleModel(sample, resource, type);
        }
    }

    public void addOrReplaceSampleModel(QuantitativeSample sample, HistogramResource resource, String type)
    {
        HistogramSampleModel model = HistogramSampleModel.getSampleModelFromCopy(sample);            
        resource.addOrReplaceModel(type, model);

        Map<String,ChannelChart<HistogramPlot>> charts = new LinkedHashMap<>();
        charts.put(type, model.getHistogram());

        updateResourceWithNewCharts(resource, charts);
    }    

    public void updateSampleModelsWithCopying(Map<String, QuantitativeSample> samples, HistogramResource resource)
    {
        for(Entry<String, QuantitativeSample> entry : samples.entrySet())
        {
            String type = entry.getKey();
            QuantitativeSample sample = entry.getValue();

            QuantitativeSample sampleCopy = new StandardSample(sample);

            updateSampleModel(sampleCopy, resource, type);
        }
    }

    protected void setConsistentWithCurrentSampleModel()
    {
        HistogramResource resource = getSelectedResource();
        HistogramSampleModel sampleModel = resource.getModel(getSelectedType());  

        updateHistogramChart(resource, getSelectedType());
        pullSampleModel(sampleModel);
    }

    private void updateHistogramChart(HistogramResource resource, String type)
    {        
        HistogramSampleModel sampleModel = resource.getModel(type);
        ChannelChart<HistogramPlot> chart = getSelectedChart(type);

        if(chart != null)
        {
            chart.setRoamingTitleText(sampleModel.getName());

            HistogramPlot plot = chart.getCustomizablePlot();

            plot.setFitDataset(sampleModel.getFitDataset());
            plot.setHistogramDataset(sampleModel.getHistogramDataset());

            plot.setRangeAxisDataQuantity(sampleModel.getRangeQuantity());
            plot.setDomainAxisDataQuantity(sampleModel.getDomainQuantity());
            plot.setOnlyTicksOnRangeAxis(sampleModel.isOnlyIntegerOnRange());
        }
    }

    protected void pullSampleModel(HistogramSampleModel model)
    {
        if(model != null)
        {
            if(model.isFitted())
            {
                DistributionFit fit = model.getDistributionFit();
                double location = fit.getLocation();
                double scale = fit.getScale();
                fieldFitName.setText(model.getFitType().toString());
                fieldMu.setText(DEFAULT_FORMAT.format(location));
                fieldSigma.setText(DEFAULT_FORMAT.format(scale));
            }

            setFitStatisticsVisible(model.isFitted());

            double count = model.getDataCount();
            double mean = model.getDataMean();
            double sd = model.getDataSD();

            fieldCount.setText(DEFAULT_FORMAT.format(count));
            fieldMean.setText(DEFAULT_FORMAT.format(mean));
            fieldSD.setText(DEFAULT_FORMAT.format(sd));

            int binCount = (int) Math.rint(model.getBinCount());
            double binWidth = model.getBinWidth();
            double binningRange = model.getRangeLength();

            labelBinCountValue.setText(Integer.toString(binCount));
            labelBinWdthValue.setText(DEFAULT_FORMAT.format(binWidth));
            labelBinRangeValue.setText(DEFAULT_FORMAT.format(binningRange));

            setDataStatisticsVisible(true);
            setBinningDetailsVisible(true);

            boolean containsNonpositive = model.containsNonpositive();
            setNonPositiveValuesPresent(containsNonpositive);
        }
        else
        {
            setFitStatisticsVisible(false);
            setDataStatisticsVisible(false);
            setBinningDetailsVisible(false);
        }
    }

    private JPanel buildStatisticsPanel()
    {
        JPanel panelStatistics = new JPanel(new BorderLayout());

        Box outer = Box.createVerticalBox();		
        SubPanel inner = new SubPanel();

        inner.addComponent(labelFit, 1,0,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelFitName, 0,1,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(fieldFitName, 1,1,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelMu, 0,2,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(fieldMu, 1,2,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);		
        inner.addComponent(labelSigma, 0,3,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(fieldSigma, 1,3,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);		

        inner.addComponent(Box.createVerticalStrut(10), 0,4,2,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);

        inner.addComponent(labelData, 1,5,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelCount, 0,6,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(fieldCount, 1,6,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelMean, 0,7,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(fieldMean, 1,7,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);		
        inner.addComponent(labelSD, 0,8,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(fieldSD, 1,8,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);		

        inner.addComponent(Box.createVerticalStrut(10), 0,9,2,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);

        inner.addComponent(labelBins, 1,10,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelBinCount, 0,11,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelBinCountValue, 1,11,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelBinWidth, 0,12,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelBinWdthValue, 1,12,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);		
        inner.addComponent(labelRange, 0,13,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelBinRangeValue, 1,13,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,1,0);		

        inner.setBorder(BorderFactory.createEmptyBorder(10,3,10,5));

        outer.add(inner);
        outer.add(Box.createVerticalStrut(60));

        panelStatistics.add(outer,BorderLayout.SOUTH);	
        panelStatistics.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));

        return panelStatistics;
    }

    private JPanel buildHistogramPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        this.panelStatistics = buildStatisticsPanel();

        panel.add(getTabbedPane(),BorderLayout.CENTER);

        if(includePanelStatistics())
        {
            panel.add(panelStatistics,BorderLayout.EAST);
        }

        return panel;
    }

    protected boolean includePanelStatistics()
    {
        return true;
    }

    @Override
    public void clear()
    {
        super.clear();

        setFitStatisticsVisible(false);
        setDataStatisticsVisible(false);
        setBinningDetailsVisible(false);

        if(rebinningDialog != null)
        {
            rebinningDialog.setVisible(false);
        }
    }

    public void publishHistograms(String shortName, String longName, Map<String, QuantitativeSample> samples)
    {
        HistogramResource histogramResource = 
                HistogramResource.buildHistogramResource(shortName, longName, samples);

        Map<HistogramResource, Map<String, ChannelChart<HistogramPlot>>> resourceChartMap = new LinkedHashMap<>();
        resourceChartMap.put(histogramResource, histogramResource.draw());

        publishHistograms(resourceChartMap);
    }

    @Override
    public void publishHistograms(Map<? extends HistogramResource, Map<String,ChannelChart<HistogramPlot>>> histograms) 
    {
        int previousCount = getResourceCount();

        addCharts(histograms);
        selectResource(previousCount);

        if(isMapDeeplyNonEmpty(histograms))
        {
            showHistograms(true);
        }
    }

    public void addCharts(Map<? extends HistogramResource, Map<String,ChannelChart<HistogramPlot>>> histograms)
    {       
        for(Entry<? extends HistogramResource, Map<String,ChannelChart<HistogramPlot>>> entry : histograms.entrySet())
        {
            HistogramResource model = entry.getKey();
            Map<String, ChannelChart<HistogramPlot>> charts = entry.getValue();
            addResource(model, charts);
        }
    }

    private boolean isMapDeeplyNonEmpty(Map<? extends HistogramResource, Map<String,ChannelChart<HistogramPlot>>> histograms) 
    {
        boolean nonEmpty = false;

        for(Map<String,ChannelChart<HistogramPlot>> histogamsForResource : histograms.values())
        {
            boolean histogamsForResourceNonEmpty = !histogamsForResource.isEmpty();
            nonEmpty= nonEmpty || histogamsForResourceNonEmpty;

            if(nonEmpty)
            {
                break;
            }
        }

        return nonEmpty;
    }

    @Override
    public void showHistograms(boolean show)
    {
        setVisible(true);
    }

    @Override
    public Window getHistogramPublicationSite() 
    {
        return getAssociatedWindow();
    }

    private void updateRebinningDialog()
    {
        if(rebinningDialog != null && rebinningDialog.isShowing())
        {
            HistogramBinningModel binningModel = getCurrentRebinningModel();
            rebinningDialog.setWizardModel(binningModel);
        }
    }

    private HistogramBinningModel getCurrentRebinningModel()
    {
        ChannelChart<HistogramPlot> chart = getSelectedChart();

        HistogramResource resource = getSelectedResource();
        String key = getSelectedType();

        HistogramSampleModel sampleModel = resource.getModel(key);
        HistogramBinningModel binningModel = new HistogramInstantUpdateBinningModel(this, sampleModel, chart);

        return binningModel;
    }

    @Override
    public void startRebinning()
    {
        HistogramBinningModel binningModel = getCurrentRebinningModel();
        rebinningDialog.showDialog(binningModel);
    }

    @Override
    protected void close()
    {
        setVisible(false);
    }

    @Override
    protected void showAllRawResourceData()
    {
        List<HistogramResource> sourcesSelected = getAllSelectedResources();
        List<SampleCollection> rawData = new ArrayList<>();

        for(HistogramResource resource : sourcesSelected)
        {
            List<SampleCollection> collections = resource.getSampleCollections();
            for(SampleCollection collection : collections)
            {
                collection.setKeysIncluded(true);
                rawData.add(collection);
            }				
        }

        SampleSelectionModel selectionModel = new SampleSelectionModel(rawData, "Which datasets would you like to view?",false, true);

        boolean approved = rawDataWizard.showDialog(selectionModel);
        if (approved) 
        {
            List<SampleCollection> includedSampleCollections = selectionModel.getSampleCollections();
            publishRawData(includedSampleCollections);
        }
    }	

    @Override
    public void showRawData() 
    {
        HistogramResource resource = getSelectedResource();

        String key = getSelectedType();
        HistogramSampleModel sampleModel = resource.getModel(key);

        QuantitativeSample sample = sampleModel.getSample();
        String sampleName = sampleModel.getName();

        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();
        samples.put(sampleName, sample);
        SampleCollection sampleCollection = new StandardSampleCollection(samples, sampleName, sampleName, new File(System.getProperty("user.home")));
        sampleCollection.setKeysIncluded(true);

        Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
        RawDataTableModel model = new RawDataTableModel(sampleCollection, false);
        StandardNumericalTable table = new OrderedNumericalTable(model, true);
        tables.put(sampleName, table);	

        MultipleNumericalTableView dialog = new MultipleNumericalTableView(getAssociatedWindow(), tables, "Raw data", true);
        dialog.setVisible(true);
    }

    @Override
    public void showStatistics() 
    {
        Map<String, StatisticsTable> tables = new LinkedHashMap<>();

        HistogramResource resource = getSelectedResource();
        String key = getSelectedType();

        HistogramSampleModel sampleModel = resource.getModel(key);
        QuantitativeSample sample = sampleModel.getSample();
        String sampleName = sampleModel.getName();
        PrefixedUnit unit = sampleModel.getDomainQuantity().getUnit();

        UpdateableStatisticsModel model = new UpdateableStatisticsModel(new File(System.getProperty("user.home")), unit);
        DescriptiveStatistics stats = sample.getDescriptiveStatistics();	
        String sampleKey = "Selected resource";
        model.addOrUpdateSample(sampleKey, sampleKey, stats);

        StatisticsTable table = new StatisticsTable(model);

        tables.put(sampleName, table);

        StatisticsView dialog = new StatisticsView(getAssociatedWindow(), tables, "Histogram data statistics", true);
        dialog.setVisible(true);	
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        Object source = evt.getSource();

        if(HistogramModelProperties.HISTOGRAM_DATA_CHANGED.equals(name))
        {
            HistogramResource parametersModel = getSelectedResource();
            if(parametersModel != null)
            {
                HistogramSampleModel sampleModel = parametersModel.getModel(getSelectedType());
                if(source == sampleModel)
                {
                    pullSampleModel(sampleModel);
                }

                boolean containsNonpositive = sampleModel.containsNonpositive();
                setNonPositiveValuesPresent(containsNonpositive);
            }
        }		
    }

    protected void setNonPositiveValuesPresent(boolean nonPositve)
    {
        for(BasicHistogramPanel<?> panel : getPanels())
        {
            panel.setNonPositiveValuesPresent(nonPositve);
        }

    }

    private class ShowRawDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"All raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showAllRawResourceData();
        }
    }

    private class ShowStatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowStatisticsAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Statistics");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showStatistics();
        }
    }

    private class ChangeBinningAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ChangeBinningAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Modify binning");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            startRebinning();
        }
    }


    ///MEASUREMENTS


    @Override
    public void showDistanceMeasurements() 
    {
        distanceMeasurementsDialog.setVisible(true);
    }

    @Override
    protected void showMeasurementEditor()
    {
        measurementEditor.setVisible(true);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {
        HistogramResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            boolean measurementsAvailable = getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            E selectedPanel = getSelectedPanel();
            selectedPanel.addOrReplaceDistanceMeasurement(measurement);        

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());

        }
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {                
        HistogramResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            int measurementsCount = selectedResource.getMeasurementCount(selectedType);
            boolean measurementsAvailable = (getMode().isMeasurement() && measurementsCount>0);
            updateMeasurementsAvailability(measurementsAvailable);

            E selectedPanel = getSelectedPanel();
            selectedPanel.removeDistanceMeasurement(measurement);

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }   
    }
}
