
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

package atomicJ.analysis;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import atomicJ.data.Channel1D;
import atomicJ.data.Datasets;
import atomicJ.data.IndentationCurve;
import atomicJ.data.PointwiseModulusCurve;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.ForceCurvePlotFactory;
import atomicJ.gui.IndentationPlotFactory;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.PointwiseModulusPlotFactory;
import atomicJ.resources.StandardSpectroscopyProcessedResource;

public final class VisualizableSpectroscopyPack implements Visualizable
{	
    private final ProcessedSpectroscopyPack pack;

    private final SpectroscopyCurve<?> recordedCurve;
    private final IndentationCurve indentationCurve;
    private final PointwiseModulusCurve modulusCurve;

    private final VisualizationSettings visSettings;

    public VisualizableSpectroscopyPack(ProcessedSpectroscopyPack pack, SpectroscopyCurve<?> recordedCurve, IndentationCurve indentationCurve,
            PointwiseModulusCurve modulusCurve, VisualizationSettings visSettings)
    {
        this.pack = pack;
        this.recordedCurve = recordedCurve;
        this.indentationCurve = indentationCurve;
        this.modulusCurve = modulusCurve;
        this.visSettings = visSettings;
    }

    public ProcessedSpectroscopyPack getPackToVisualize()
    {
        return pack;
    }

    public SpectroscopyCurve<?> getRecordedCurves()
    {
        return recordedCurve;
    }

    public IndentationCurve getIndentationCurve()
    {
        return indentationCurve;
    }

    public PointwiseModulusCurve getPointwiseModulusCurve()
    {
        return modulusCurve;
    }	

    public VisualizationSettings getVisualizationSettings()
    {
        return visSettings;
    }

    public Channel1DChart<?> getForceCurveChart()
    {
        Channel1DChart<?> chart = null;
        if(visSettings.isPlotRecordedCurve())
        {
            Channel1DPlot plot = ForceCurvePlotFactory.getInstance().getPlot(recordedCurve);
            chart = new Channel1DChart<>(plot, Datasets.FORCE_CURVE_PLOT);
            chart.setAutomaticTitles(pack.getSource().getAutomaticChartTitles());
        }

        return chart;                                               
    }

    public Channel1DChart<Channel1DPlot> getIndentationChart()
    {
        Channel1DChart<Channel1DPlot> chart = null;
        if(visSettings.isPlotIndentation())
        {
            Channel1DPlot plot = IndentationPlotFactory.getInstance().getPlot(indentationCurve);
            chart = new Channel1DChart<>(plot, Datasets.INDENTATION_PLOT);
            chart.setAutomaticTitles(pack.getSource().getAutomaticChartTitles());
        }
        return chart;
    }

    public Channel1DChart<?> getPointwiseModulusChart()
    {
        Channel1DChart<?> chart = null;
        if(visSettings.isPlotModulus())
        {
            Channel1DPlot plot = PointwiseModulusPlotFactory.getInstance().getPlot(modulusCurve);
            chart = new Channel1DChart<>(plot, Datasets.POINTWISE_PLOT);
            chart.setAutomaticTitles(pack.getSource().getAutomaticChartTitles());
        }       
        return chart;
    }

    public Map<String, Collection<? extends Channel1D>> getChannels()
    {
        Map<String, Collection<? extends Channel1D>> charts = new LinkedHashMap<>();

        charts.put(StandardSpectroscopyProcessedResource.RECORDED_CURVE, recordedCurve.getChannels());  
        charts.put(StandardSpectroscopyProcessedResource.INDENTATION, indentationCurve.getChannels());
        charts.put(StandardSpectroscopyProcessedResource.POINTWISE_MODULUS, modulusCurve.getChannels());

        return charts;
    }

    @Override
    public Map<String, ChannelChart<?>> visualize()
    {
        Map<String, ChannelChart<?>> charts = new LinkedHashMap<>();

        ChannelChart<?> afmCurveChart = getForceCurveChart();  
        ChannelChart<?> indentationChart = getIndentationChart();
        ChannelChart<?> pointwiseModulusChart = getPointwiseModulusChart();

        charts.put(StandardSpectroscopyProcessedResource.RECORDED_CURVE, afmCurveChart);  
        charts.put(StandardSpectroscopyProcessedResource.INDENTATION, indentationChart);
        charts.put(StandardSpectroscopyProcessedResource.POINTWISE_MODULUS, pointwiseModulusChart);

        return charts;
    }
}
