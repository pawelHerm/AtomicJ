
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


import java.awt.Window;
import java.util.*;

import org.jfree.ui.Layer;

import atomicJ.gui.*;


public class  LiveChartEditorFactory
{
    private static final LiveChartEditorFactory INSTANCE = new LiveChartEditorFactory();

    private LiveChartEditorFactory()
    {}

    public static LiveChartEditorFactory getInstance()
    {
        return INSTANCE;
    }

    public LiveChartEditor getEditor(CustomizableXYBaseChart<?> chart, 
            List<? extends CustomizableXYBaseChart<?>> boundedCharts, Window parent)
    {
        return getEditor(chart, boundedCharts, parent, 4);
    }

    public LiveChartEditor getEditor(CustomizableXYBaseChart<?> chart,
            List<? extends CustomizableXYBaseChart<?>> boundedCharts, Window parent, int maxGroupSize)
    {
        CustomizableXYBasePlot plot = chart.getCustomizablePlot();	

        RoamingLegend mainLegend = chart.getRoamingLegend();

        List<CustomizableXYBasePlot> boundedPlots = new ArrayList<>();
        List<RoamingLegend> boundedLegends = new ArrayList<>();

        for(CustomizableXYBaseChart<?> boundedChart: boundedCharts)
        {
            if(boundedChart != null)
            {
                CustomizableXYBasePlot boundedPlot = boundedChart.getCustomizablePlot();
                boundedPlots.add(boundedPlot);

                RoamingLegend boundedLegend = boundedChart.getRoamingLegend();
                boundedLegends.add(boundedLegend);
            }            
        }

        List<Subeditor> seriesSubeditors = new ArrayList<>();

        for(ChannelRenderer renderer : plot.getRenderers())
        {
            Subeditor subeditor = SeriesSubeditorFactory.getSubeditor(renderer, boundedPlots);
            if(subeditor != null)
            {
                seriesSubeditors.add(subeditor);
            }
        }

        Map<String, Subeditor> markerSubeditors = createMarkerSubeditors(plot);		

        CompositeSubeditor seriesSubeditor = new CompositeSubeditor(seriesSubeditors, maxGroupSize);
        XYPlotSubeditor plotSubeditor = new XYPlotSubeditor(plot, boundedPlots, plot.getPreferences());

        //UGLY HACK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        List<AxisSource> boundedAxisSources = new ArrayList<>();
        boundedAxisSources.addAll(boundedPlots);

        MultipleAxesSubeditor axesSubeditor = new MultipleAxesSubeditor(plot, boundedAxisSources);       

        RoamingTitleSubeditor titleSubeditor = RoamingTitleSubeditor.getInstance(boundedCharts, chart);

        MultipleScaleBarsSubeditor scaleBarsSubeditor = null;
        if(plot instanceof CustomizableXYPlot)
        {
            scaleBarsSubeditor = new MultipleScaleBarsSubeditor((CustomizableXYPlot)plot, boundedPlots);
        }

        ChartSubeditor chartSubeditor = new ChartSubeditor(boundedCharts, chart);

        Map<String, Subeditor> legendSubeditors = new LinkedHashMap<>();

        LegendCompositeSubeditor legendMainSubeditor = new LegendCompositeSubeditor(mainLegend, boundedLegends);
        legendSubeditors.put(mainLegend.getName(), legendMainSubeditor);


        for(RoamingLegend additionalLegend : chart.getRoamingSublegends())
        {
            LegendCompositeSubeditor legendSubeditor = new LegendCompositeSubeditor(additionalLegend, new ArrayList<RoamingLegend>());
            legendSubeditors.put(additionalLegend.getName(), legendSubeditor);
        }

        MultipleTooltipsSubeditor tooltipSubeditor = null;

        if(plot instanceof TooltipManagerSource)
        {

            List<TooltipManagerSource> boundedTooltipSources = new ArrayList<>();

            for(CustomizableXYBaseChart<?> boundedChart: boundedCharts)
            {
                if(boundedChart != null)
                {
                    CustomizableXYBasePlot boundedPlot = boundedChart.getCustomizablePlot();                    
                    if(boundedPlot instanceof TooltipManagerSource)
                    {
                        boundedTooltipSources.add((TooltipManagerSource)boundedPlot);
                    }
                }

            }

            tooltipSubeditor = new MultipleTooltipsSubeditor((TooltipManagerSource)plot, boundedTooltipSources);
        }

        LiveChartEditor dialog = new LiveChartEditor(seriesSubeditor, tooltipSubeditor, plotSubeditor,
                axesSubeditor, markerSubeditors, titleSubeditor, scaleBarsSubeditor,
                legendSubeditors, chartSubeditor, parent);

        return dialog;
    }


    private Map<String, Subeditor> createMarkerSubeditors(CustomizableXYBasePlot plot)
    {
        Map<String, Subeditor> markerSubeditors = new LinkedHashMap<>();


        Collection domainMarkersForeground = plot.getDomainMarkers(Layer.FOREGROUND);
        Collection domainMarkersBackground = plot.getDomainMarkers(Layer.BACKGROUND);

        Collection rangeMarkersForeground = plot.getRangeMarkers(Layer.FOREGROUND);
        Collection rangeMarkersBackground = plot.getRangeMarkers(Layer.BACKGROUND);

        createAndPutMarkerSubeditor(domainMarkersForeground, plot, markerSubeditors, false, true);
        createAndPutMarkerSubeditor(domainMarkersBackground, plot, markerSubeditors, true, true);
        createAndPutMarkerSubeditor(rangeMarkersForeground, plot, markerSubeditors, false, false);
        createAndPutMarkerSubeditor(rangeMarkersBackground, plot, markerSubeditors, true, false);

        return markerSubeditors;
    }       


    private void createAndPutMarkerSubeditor(Collection<?> markers, 
            CustomizableXYBasePlot plot, Map<String, Subeditor> markerSubeditors, boolean background, boolean domain)
    {
        if(markers == null)
        {
            return;
        }

        for(Object marker : markers)
        {
            if(marker instanceof CustomizableIntervalMarker)
            {
                String name = ((CustomizableIntervalMarker) marker).getName();

                IntervalMarkerSubeditor subeditor = new IntervalMarkerSubeditor((CustomizableIntervalMarker)marker, 
                        new ArrayList<CustomizableIntervalMarker>(), plot,  background, domain);
                markerSubeditors.put(name, subeditor);
            }
            else if(marker instanceof CustomizableValueMarker)
            {
                String name = ((CustomizableValueMarker) marker).getName();

                ValueMarkerSubeditor subeditor = new ValueMarkerSubeditor((CustomizableValueMarker)marker, new ArrayList<CustomizableValueMarker>());
                markerSubeditors.put(name, subeditor);
            }
        }      
    }
}
