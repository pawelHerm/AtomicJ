
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import atomicJ.data.BasicSpectroscopyCurve;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel2D;
import atomicJ.data.Datasets;
import atomicJ.data.ImageChannel;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.sources.BasicSpectroscopySource;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.DynamicSpectroscopySource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;


public class ChannelSourceVisualization 
{
    public static Map<String,Channel2DChart<?>> getCharts(Channel2DSource<?> source)
    {
        Map<String,Channel2DChart<?>> charts = new LinkedHashMap<>();

        List<? extends Channel2D> channels = source.getChannels();

        for(Channel2D channel: channels)
        {
            charts.put(channel.getIdentifier(), getChart(source, channel));
        }

        return charts;
    }

    public static Channel2DChart<?> getChart(Channel2DSource<?> source, Channel2D channel)
    {
        String channelIdentifier = channel.getIdentifier();
        Preferences pref = Preferences.userNodeForPackage(ImagePlot.class).node(ImagePlot.class.getName()).node(channelIdentifier);
        ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, source.getShortName());

        ImagePlot plot = new ImagePlot(dataset, channelIdentifier /*plotName*/, Datasets.DENSITY_PLOT /*styleKey*/,  pref, channelIdentifier /*rendererKey*/);

        Channel2DChart<?> chart = new ImageChart<>(plot, Datasets.DENSITY_PLOT);

        return chart;
    }

    public static Map<String, ChannelChart<?>> getCharts(SimpleSpectroscopySource spectroscopySource)
    {        
        SpectroscopyCurve<Channel1D> afmCurve = spectroscopySource.getRecordedCurve();
        Channel1DPlot plot = RawCurvePlotFactory.getInstance().getPlot(afmCurve);
        ChannelChart<Channel1DPlot> chart = new Channel1DChart<>(plot, Datasets.AFM_CURVE_PREVIEW_PLOT);
        chart.setAutomaticTitles(spectroscopySource.getAutomaticChartTitles());

        Map<String, ChannelChart<?>> charts = new LinkedHashMap<>();
        charts.put(SpectroscopyBasicResource.RECORDED_CURVE, chart);

        for(BasicSpectroscopySource recording : spectroscopySource.getAdditionalCurveRecordings())
        {
            BasicSpectroscopyCurve<?> curve = recording.getRecordedCurve();
            Channel1DPlot amplitudePlot = RawCurvePlotFactory.getInstance().getPlot(curve);
            ChannelChart<Channel1DPlot> amplitudeChart = new Channel1DChart<>(amplitudePlot, Datasets.AMPLITUDE_CURVE_PREVIEW_PLOT);

            charts.put(recording.getShortName(), amplitudeChart);
        }

        if(spectroscopySource instanceof DynamicSpectroscopySource)
        {
            DynamicSpectroscopySource dynamicSpectroscopySource = (DynamicSpectroscopySource)spectroscopySource;
            SpectroscopyCurve<Channel1D> amplitudeCurve = dynamicSpectroscopySource.getRecordedAmplitudeCurve();
            Channel1DPlot amplitudePlot = RawCurvePlotFactory.getInstance().getPlot(amplitudeCurve);
            ChannelChart<Channel1DPlot> amplitudeChart = new Channel1DChart<>(amplitudePlot, Datasets.AMPLITUDE_CURVE_PREVIEW_PLOT);

            charts.put(DynamicSpectroscopySource.AMPLITUDE_CURVE, amplitudeChart);

            SpectroscopyCurve<Channel1D> phaseCurve = dynamicSpectroscopySource.getRecordedPhaseCurve();
            Channel1DPlot phasePlot = RawCurvePlotFactory.getInstance().getPlot(phaseCurve);
            ChannelChart<Channel1DPlot> phaseChart = new Channel1DChart<>(phasePlot, Datasets.PHASE_CURVE_PREVIEW_PLOT);

            charts.put(DynamicSpectroscopySource.PHASE_CURVE, phaseChart);
        }

        return charts;
    }

    public static Map<String,MapChart<?>> getMapChartsFromImageSource(ImageSource source)
    {
        Map<String,MapChart<?>> charts = new LinkedHashMap<>();

        List<ImageChannel> channels = source.getChannels();

        for(ImageChannel channel: channels)
        {
            String channelIdentifier = channel.getIdentifier();
            Preferences pref = Preferences.userNodeForPackage(ImagePlot.class).node(ImagePlot.class.getName()).node(channelIdentifier);
            ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, source.getShortName());

            ImagePlot plot = new ImagePlot(dataset, channelIdentifier /*plotName*/, Datasets.DENSITY_PLOT /*styleKey*/,  pref, channelIdentifier /*rendererKey*/);
            MapChart<?> chart = new MapChart<>(plot, Datasets.DENSITY_PLOT);

            charts.put(channel.getIdentifier(),chart);
        }

        return charts;
    }

    public static Map<String, MapChart<?>> getMapCharts(MapSource<?> mapSource, Collection<? extends Channel2D> channels, Preferences prefParentNode)
    {
        Map<String, MapChart<?>> charts = new LinkedHashMap<>();
        for(Channel2D channel: channels)
        {           
            String identifier = channel.getIdentifier();

            Preferences pref = prefParentNode.node(identifier);
            ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, mapSource.getShortName());

            MapPlot mapPlot = new MapPlot(dataset, identifier, pref);

            MapChart<?> chart = new MapChart<>(mapPlot, Datasets.MAP_PLOT);
            charts.put(identifier, chart);
        }

        return charts;
    }
}
