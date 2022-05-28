
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

package atomicJ.gui.boxplots;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.SampleUtilities;
import atomicJ.data.StandardSampleCollection;
import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.MultipleChartPanel;
import atomicJ.gui.MultipleNumericalTableView;
import atomicJ.gui.OrderedNumericalTable;
import atomicJ.gui.RawDataTableModel;
import atomicJ.gui.ResourcePresentationView;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.SampleSelectionModel;
import atomicJ.utilities.CollectionsUtilities;
import atomicJ.utilities.MetaMap;


public class BoxPlotSimpleView <E extends MultipleChartPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>>
extends ResourcePresentationView<BoxAndWhiskerResource, CustomizableXYBaseChart<BoxAndWhiskerXYPlot>, E>
implements BoxAndWhiskersDestination
{
    private static final NumberFormat DEFAULT_FORMAT = NumberFormat.getInstance(Locale.US);

    static 
    {
        DEFAULT_FORMAT.setMaximumFractionDigits(4);
    }

    private  static final Preferences PREF = Preferences.userNodeForPackage(BoxPlotSimpleView.class).node(BoxPlotSimpleView.class.getName());

    //    private final ShowStatisticsAction showStatisticsAction = new ShowStatisticsAction();
    private final ShowRawDataAction showRawDataAction = new ShowRawDataAction();
    //    private final JMenuItem statisticsItem = new JMenuItem(showStatisticsAction); 

    private final MultipleSelectionWizard<String> rawDataWizard = new MultipleSelectionWizard<>(getAssociatedWindow(), "Raw data assistant");

    private JPanel panelStatistics;
    private final JMenu dataMenu;

    public BoxPlotSimpleView(final Window parent, AbstractChartPanelFactory<E> factory, String title)
    {
        this(parent, factory, title, ModalityType.MODELESS, true, true);	
    }

    public BoxPlotSimpleView(final Window parent, AbstractChartPanelFactory<E> factory, String title, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        super(parent, factory ,title, PREF, modalityType, allowsMultipleResources, allowsMultipleTypes);	

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

        controlForResourceEmptiness(isEmpty());

        JMenuBar menuBar = getMenuBar();
        JMenuItem rawDataItem = new JMenuItem(showRawDataAction);

        this.dataMenu = new JMenu("Data");
        dataMenu.setMnemonic(KeyEvent.VK_D);
        dataMenu.add(rawDataItem);
        //        dataMenu.add(statisticsItem); 

        menuBar.add(dataMenu);
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

    private JPanel buildHistogramPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panelStatistics = new JPanel();

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

    public void addCharts(Map<? extends BoxAndWhiskerResource, 
            Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> resourceChartMap)
    {		
        for(Entry<? extends BoxAndWhiskerResource, Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> entry : resourceChartMap.entrySet())
        {
            BoxAndWhiskerResource model = entry.getKey();
            Map<String, CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> charts = entry.getValue();
            addOrUpdateResourceWithNewCharts(model, charts);
        }
    }

    @Override
    public void clear()
    {
        super.clear();      
    }

    public void publishBoxPlots(File defaultOutputLocation, String shortName, String longName, List<SampleCollection> sampleCollections, boolean show) 
    {
        Map<String, Map<String, QuantitativeSample>> includedSamples = SampleUtilities.extractSamples(sampleCollections);    

        //ugly hack, but works
        MetaMap<String, Object, QuantitativeSample> m = new MetaMap<>();
        m.putAll(includedSamples);

        publishBoxPlots(defaultOutputLocation, shortName, longName, m.getMapCopy(), show);      
    }

    @Override
    public void publishBoxPlots(File defaultOutputLocation, String shortName, String longName, Map<String, Map<Object, QuantitativeSample>> samples, boolean show) 
    {    
        Map<String, BoxAndWhiskersTypeModel> boxSamples = BoxAndWhiskersTypeModel.getBoxAndWhiskerTypeModels(samples);    
        BoxAndWhiskerResource resource = new BoxAndWhiskerResource(boxSamples, shortName, longName, defaultOutputLocation);

        Map<BoxAndWhiskerResource, Map<String, CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> resourceMap = new LinkedHashMap<>();   
        resourceMap.put(resource, resource.draw());

        publishBoxPlots(resourceMap, show);        
    }

    @Override
    public void publishBoxPlots(Map<? extends BoxAndWhiskerResource,
            Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> boxPlots, boolean show)
    {
        if(boxPlots.isEmpty())
        {
            return;
        }

        addCharts(boxPlots);

        List<BoxAndWhiskerResource> newResources =  new ArrayList<>(boxPlots.keySet());          
        selectResource(newResources.get(0));

        if(show && isMapDeeplyNonEmpty(boxPlots))
        {
            showBoxPlots(true);
        }
    }

    private boolean isMapDeeplyNonEmpty(Map<? extends BoxAndWhiskerResource, 
            Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> boxPlots) 
    {
        boolean nonEmpty = false;

        for(Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> histogamsForResource : boxPlots.values())
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
    public void showBoxPlots(boolean show)
    {
        setVisible(true);
    }

    @Override
    public Window getBoxPlotPublicationSite() 
    {
        return getAssociatedWindow();
    }


    @Override
    protected void close()
    {
        setVisible(false);
    }

    @Override
    protected void showAllRawResourceData()
    {
        List<BoxAndWhiskerResource> sourcesSelected = getAllSelectedResources();
        List<SampleCollection> rawData = new ArrayList<>();

        for(BoxAndWhiskerResource resource : sourcesSelected)
        {
            List<SampleCollection> collections = resource.getSampleCollections();
            for(SampleCollection collection : collections)
            {
                collection.setKeysIncluded(true);
                rawData.add(collection);
            }				
        }

        SampleSelectionModel selectionModel = new SampleSelectionModel(rawData, "Which datasets would you like to view?", true, true);

        boolean approved = rawDataWizard.showDialog(selectionModel);
        if (approved) 
        {
            List<SampleCollection> sampleCollections = selectionModel.getSampleCollections();
            publishRawData(sampleCollections, true);
        }
    }	

    public void showRawData() 
    {        
        BoxAndWhiskerResource resource = getSelectedResource();

        String key = getSelectedType();
        BoxAndWhiskersTypeModel sampleModel = resource.getModel(key);

        Map<Object, QuantitativeSample> samples = sampleModel.getSamples();
        SampleCollection sampleCollection = 
                new StandardSampleCollection(CollectionsUtilities.convertKeysToStrings(samples), key, key, 
                        new File(System.getProperty("user.home")));
        sampleCollection.setKeysIncluded(true);

        Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
        RawDataTableModel model = new RawDataTableModel(sampleCollection, true);
        StandardNumericalTable table = new OrderedNumericalTable(model, true);
        tables.put(key, table);	

        MultipleNumericalTableView dialog = new MultipleNumericalTableView(getAssociatedWindow(), tables, "Raw data", true);
        dialog.setVisible(true);
    }

    protected void setNonPositiveValuesPresent(boolean nonPositve)
    {
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
}
