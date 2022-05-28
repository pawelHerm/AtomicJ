
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

import javax.swing.*;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.SampleUtilities;
import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.MultipleChartPanel;
import atomicJ.utilities.MetaMap;


public class LiveBoxPlotView<E extends MultipleChartPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> extends BoxPlotSimpleView<E>
{
    public LiveBoxPlotView(Window parent, AbstractChartPanelFactory<E> factory, String title, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        super(parent, factory, title, modalityType, allowsMultipleResources, allowsMultipleTypes); 
    }

    @Override
    protected JPanel getButtonsPanel()
    {
        return buildSingleResourcesButtonPanel();
    }

    public void reset(BoxAndWhiskerResource resource, List<SampleCollection> sampleCollections)
    {
        Map<String, Map<String, QuantitativeSample>> samples = SampleUtilities.extractSamples(sampleCollections);

        //ugly hack, but works
        MetaMap<String, Object, QuantitativeSample> m = new MetaMap<>();
        m.putAll(samples);

        reset(resource, m.getMapCopy());
    }

    public void reset(BoxAndWhiskerResource resource, Map<String, Map<Object, QuantitativeSample>> samples)
    {
        resource.reset(samples);
        refreshCharts(resource, resource.getSamples());
    }

    @Override
    public void setVisible(boolean visible)
    {   
        if(visible)
        {
            refresh();
        }

        super.setVisible(visible);
    }

    public void refresh()
    {
        for(BoxAndWhiskerResource resource : getResources())
        {
            refreshSamplesInternal(resource);
        }
    }

    public void refresh(BoxAndWhiskerResource resource,
            Map<String, Map<Object, QuantitativeSample>> samplesChanged)
    {
        resource.registerSamplesToRefresh(samplesChanged);
        if(isVisible())
        {
            refreshSamplesInternal(resource);
        }
    }

    public void refresh(BoxAndWhiskerResource resource,
            String type, Map<Object, QuantitativeSample> samplesChanged)
    {
        resource.registerSamplesToRefresh(type, samplesChanged);
        if(isVisible())
        {
            refreshSamplesInternal(resource);
        }
    }


    public void refreshSamplesInternal(BoxAndWhiskerResource resource)
    {
        if(!resource.isRefreshNeeded())
        {
            return;
        }

        resource.refresh();
        refreshCharts(resource, resource.getSamples());
    }

    private void refreshCharts(BoxAndWhiskerResource resource, Map<String, Map<Object, QuantitativeSample>> samples)
    {
        Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> chartsNew = new LinkedHashMap<>();

        for(Entry<String, Map<Object, QuantitativeSample>> entry : samples.entrySet())
        {
            String type = entry.getKey();
            Map<Object, QuantitativeSample> innerSample = entry.getValue();

            CustomizableXYBaseChart<BoxAndWhiskerXYPlot> chart = getChart(resource, type);

            if(chart != null)
            {
                XYBoxAndWhiskerIndexDataset dataset = XYBoxAndWhiskerIndexDataset.getDataset(type, innerSample);
                BoxAndWhiskerXYPlot plot = chart.getCustomizablePlot();
                plot.setDataset(0, dataset);
            }
            else 
            {
                chartsNew.put(type, BoxAndWhiskerResource.buildChart(type, innerSample));
            }
        }

        if(!chartsNew.isEmpty())
        {
            Map<BoxAndWhiskerResource, Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> map = new LinkedHashMap<>();

            map.put(resource, chartsNew);

            addCharts(map);
        }
    }

    private JPanel buildSingleResourcesButtonPanel()
    {	
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 5));	
        JPanel buttonPanelOuter = new JPanel();

        if(getAllowsMultipleTypes())
        {
            JButton buttonSaveAll = new JButton(getSaveAllAction());
            buttonPanel.add(buttonSaveAll);
        }

        JButton buttonClose = new JButton(getCloseAction());

        buttonPanel.add(buttonClose);

        buttonPanelOuter.add(buttonPanel);
        buttonPanelOuter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanelOuter;
    }
}
