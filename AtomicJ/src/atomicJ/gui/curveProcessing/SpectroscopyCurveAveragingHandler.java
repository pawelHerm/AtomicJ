package atomicJ.gui.curveProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import atomicJ.analysis.Batch;
import atomicJ.analysis.InterpolationMethod1D;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.curveProcessing.Channel1DAveragingInDomainIntersection;
import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DCollection;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DDataWithErrors;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Data1D;
import atomicJ.data.Datasets;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.Channel1DRenderer;
import atomicJ.gui.PreferredScaleBarStyle;
import atomicJ.gui.RendererFactory;
import atomicJ.gui.StandardStyleTag;
import atomicJ.gui.StyleTag;
import atomicJ.gui.curveProcessing.SpectroscopyCurveAveragingSettings.AveragingSettings;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.Channel1DDataset;
import atomicJ.gui.Channel1DResultsView;
import atomicJ.resources.SpectroscopyAveragedProcessedResource;
import atomicJ.resources.SpectroscopyProcessedResource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.TwoTypePair;

public class SpectroscopyCurveAveragingHandler implements CurveAveragingHandler<ProcessedSpectroscopyPack>
{
    private static final Preferences PREF = Preferences.userNodeForPackage(SpectroscopyCurveAveragingHandler.class).node(SpectroscopyCurveAveragingHandler.class.getName());

    private static final Preferences PREF_RECORDED_CURVE = PREF.node(Datasets.AVERAGED_RECORDED_CURVE);

    private static final PreferredScaleBarStyle PREFERRED_RECORDED_CURVE_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF_RECORDED_CURVE.node("DomainScaleBar"));
    private static final PreferredScaleBarStyle PREFERRED_RECORDED_CURVE_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF_RECORDED_CURVE.node("RangeScaleBar"));

    private static final Preferences PREF_INDENTATION_CURVE = PREF.node(Datasets.AVERAGED_INDENTATION_DATA);

    private static final PreferredScaleBarStyle PREFERRED_INDENTATION_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF_INDENTATION_CURVE.node("DomainScaleBar"));
    private static final PreferredScaleBarStyle PREFERRED_INDENTATION_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF_INDENTATION_CURVE.node("RangeScaleBar"));

    private static final Preferences PREF_POINTWISE_MODULUS = PREF.node(Datasets.AVERAGED_POINTWISE_MODULUS_DATA);

    private static final PreferredScaleBarStyle PREFERRED_POINTWISE_MODULUS_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF_POINTWISE_MODULUS.node("DomainScaleBar"));
    private static final PreferredScaleBarStyle PREFERRED_POINTWISE_MODULUS_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF_POINTWISE_MODULUS.node("RangeScaleBar"));

    private final Map<IdentityTag, SpectroscopyCurveAveragingSettings> allAveragingSettings = new HashMap<>();
    private final Channel1DResultsView graphsView;

    public SpectroscopyCurveAveragingHandler(Channel1DResultsView graphsDialog)
    {
        this.graphsView = graphsDialog;
    }

    @Override
    public void handleAveragingRequest(Map<IdentityTag, Batch<ProcessedSpectroscopyPack>> curves) 
    {
        Map<SpectroscopyProcessedResource, Map<String, ChannelChart<?>>> resourceChartMap = new LinkedHashMap<>();

        for(Entry<IdentityTag, SpectroscopyCurveAveragingSettings> entry : allAveragingSettings.entrySet())
        {
            IdentityTag tag = entry.getKey();
            SpectroscopyCurveAveragingSettings settings = entry.getValue();

            if(!settings.isAveragingEnabled())
            {
                continue;
            }
            
            Batch<ProcessedSpectroscopyPack> batch = curves.get(tag);
            List<ProcessedSpectroscopyPack> packs = batch.getPacks();
            
            if(packs.isEmpty())
            {
                continue;
            }

            Channel1DCollection recordedCurveAveraged = getAveragedRecordedCurve(packs, settings.getRecordedCurveSettings());
            Channel1DCollection forceIndentationAveraged = getAveragedIndentationCurve(packs, settings.getIndentationSettings());
            Channel1DCollection pointwiseModulusAveraged = getAveragedPointwiseModulusCurve(packs, settings.getPointwiseModulusSettings());

            File defaultOutputFile = Processed1DPack.getDefaultOutputFile(packs);
            String name = "Average "+ tag.getLabel();

            SpectroscopyProcessedResource resource = new SpectroscopyAveragedProcessedResource(defaultOutputFile, name, name, recordedCurveAveraged, forceIndentationAveraged, pointwiseModulusAveraged);

            Map<String, ChannelChart<?>> chartsForResource = new LinkedHashMap<>();
            chartsForResource.put(SpectroscopyAveragedProcessedResource.AVERAGED_RECORDED_CURVE, getForceCurveChart(resource, recordedCurveAveraged));
            chartsForResource.put(SpectroscopyAveragedProcessedResource.AVERAGED_INDENTATION, getIndentationChart(resource, forceIndentationAveraged));
            chartsForResource.put(SpectroscopyAveragedProcessedResource.AVERAGED_POINTWISE_MODULUS, getPointwiseModulusChart(resource, pointwiseModulusAveraged));

            resourceChartMap.put(resource, chartsForResource);
        }

        if(!resourceChartMap.isEmpty())
        {
            int previousCount = graphsView.getResourceCount();    

            graphsView.addResources(resourceChartMap);
            graphsView.selectResource(previousCount);

            graphsView.drawingChartsFinished();
        }
    }

    private Channel1DChart<?> getForceCurveChart(SpectroscopyProcessedResource resource, Channel1DCollection recordedCurveAveraged)
    {
        Channel1DChart<?> chart = null;
        if(!recordedCurveAveraged.isEmpty())
        {
            List<TwoTypePair<Channel1DDataset, Channel1DRenderer>> layers = buildChartLayers(recordedCurveAveraged);

            Channel1DPlot plot = new Channel1DPlot(layers, PREF_RECORDED_CURVE, PREFERRED_RECORDED_CURVE_DOMAIN_SCALE_BAR, PREFERRED_RECORDED_CURVE_RANGE_SCALE_BAR);
            chart = new Channel1DChart<>(plot, Datasets.AVERAGED_FORCE_CURVE_PLOT);
            chart.setAutomaticTitles(resource.getAutomaticChartTitles());
        }

        return chart;                                               
    }

    private Channel1DChart<Channel1DPlot> getIndentationChart(SpectroscopyProcessedResource resource, Channel1DCollection forceIndentationAveraged)
    {
        Channel1DChart<Channel1DPlot> chart = null;
        if(!forceIndentationAveraged.isEmpty())
        {
            List<TwoTypePair<Channel1DDataset, Channel1DRenderer>> layers = buildChartLayers(forceIndentationAveraged);

            Channel1DPlot plot = new Channel1DPlot(layers, PREF_INDENTATION_CURVE, PREFERRED_INDENTATION_DOMAIN_SCALE_BAR, PREFERRED_INDENTATION_RANGE_SCALE_BAR);
            chart = new Channel1DChart<>(plot, Datasets.AVERAGED_INDENTATION_PLOT);
            chart.setAutomaticTitles(resource.getAutomaticChartTitles());
        }
        return chart;
    }

    private Channel1DChart<?> getPointwiseModulusChart(SpectroscopyProcessedResource resource, Channel1DCollection pointwiseModulusAveraged)
    {
        Channel1DChart<?> chart = null;
        if(!pointwiseModulusAveraged.isEmpty())
        {
            List<TwoTypePair<Channel1DDataset, Channel1DRenderer>> layers = buildChartLayers(pointwiseModulusAveraged);

            Channel1DPlot plot = new Channel1DPlot(layers, PREF_POINTWISE_MODULUS, PREFERRED_POINTWISE_MODULUS_DOMAIN_SCALE_BAR, PREFERRED_POINTWISE_MODULUS_RANGE_SCALE_BAR);
            chart = new Channel1DChart<>(plot, Datasets.AVERAGED_POINTWISE_PLOT);
            chart.setAutomaticTitles(resource.getAutomaticChartTitles());
        }       
        return chart;
    }

    private static List<TwoTypePair<Channel1DDataset, Channel1DRenderer>> buildChartLayers(Data1D avaregedData)
    {
        List<TwoTypePair<Channel1DDataset, Channel1DRenderer>> layers = new ArrayList<>();
        List<? extends Channel1D> channels = avaregedData.getChannels();

        for(Channel1D ch: channels)
        {
            Channel1DDataset dataset = new Channel1DDataset(ch, ch.getName());
            StyleTag styleTag = new StandardStyleTag(ch.getIdentifier());       

            Channel1DRenderer renderer = RendererFactory.getChannel1DErrorBarRenderer(ch, styleTag);

            TwoTypePair<Channel1DDataset, Channel1DRenderer> layer = new TwoTypePair<>(dataset, renderer);
            layers.add(layer);
        }

        return layers;
    }

    private Channel1DCollection getAveragedRecordedCurve(List<ProcessedSpectroscopyPack> packs, AveragingSettings settings)
    {
        String name = SpectroscopyAveragedProcessedResource.AVERAGED_RECORDED_CURVE;
        boolean showAveragedRecordedCurves = settings.isShown();
        if(showAveragedRecordedCurves)
        {
            int noOfPoints = settings.getPointCount();
            ErrorBarType errorBarType = settings.getErrorBarType();

            List<Channel1DData> approachChannelDataAll = new ArrayList<>();
            List<Channel1DData> withdrawChannelDataAll = new ArrayList<>();

            for(ProcessedSpectroscopyPack pack : packs)
            {
                SpectroscopyCurve<Channel1D> spectroscopyCurve = pack.getForceDistanceCurve();
                approachChannelDataAll.add(spectroscopyCurve.getApproach().getChannelData());
                withdrawChannelDataAll.add(spectroscopyCurve.getWithdraw().getChannelData());
            }

            Channel1DAveragingInDomainIntersection tr = new Channel1DAveragingInDomainIntersection(InterpolationMethod1D.LINEAR, noOfPoints, errorBarType);

            Channel1DDataWithErrors averagedApproach = tr.transform(approachChannelDataAll);
            Channel1D averagedApproachChannel = new Channel1DStandard(averagedApproach, Datasets.AVERAGED_APPROACH);

            Channel1DDataWithErrors averagedWithdraw = tr.transform(withdrawChannelDataAll);
            Channel1D averagedWithdrawChannel = new Channel1DStandard(averagedWithdraw, Datasets.AVERAGED_WITHDRAW);

            Channel1DCollection recordedCurvesAveraged = new Channel1DCollection(name, name);
            recordedCurvesAveraged.addChannel(averagedApproachChannel);
            recordedCurvesAveraged.addChannel(averagedWithdrawChannel);

            return recordedCurvesAveraged;
        }

        Channel1DCollection emptyInstance = new Channel1DCollection(name);
        return emptyInstance;
    }

    private Channel1DCollection getAveragedIndentationCurve(List<ProcessedSpectroscopyPack> packs, AveragingSettings settings)
    {
        String name = SpectroscopyAveragedProcessedResource.AVERAGED_INDENTATION;

        boolean showAveragedIndentationCurves = settings.isShown();
        if(showAveragedIndentationCurves)
        {
            int noOfPoints = settings.getPointCount();
            ErrorBarType errorBarType = settings.getErrorBarType();

            List<Channel1DData> forceIndentationAll = new ArrayList<>();

            for(ProcessedSpectroscopyPack pack : packs)
            {
                Channel1DData forceIndentation = pack.getForceIndentationData();
                forceIndentationAll.add(forceIndentation);
            }

            Channel1DAveragingInDomainIntersection tr = new Channel1DAveragingInDomainIntersection(InterpolationMethod1D.LINEAR, noOfPoints, errorBarType);
            Channel1DDataWithErrors forceIndentationAveraged = tr.transform(forceIndentationAll);

            Channel1D channel = new Channel1DStandard(forceIndentationAveraged, Datasets.AVERAGED_INDENTATION_DATA);
            Channel1DCollection indentationAveragedCollection = new Channel1DCollection(name, name);
            indentationAveragedCollection.addChannel(channel);

            return indentationAveragedCollection;
        }

        Channel1DCollection emptyInstance = new Channel1DCollection(name);
        return emptyInstance;
    }

    private Channel1DCollection getAveragedPointwiseModulusCurve(List<ProcessedSpectroscopyPack> packs, AveragingSettings settings)
    {
        String name = SpectroscopyAveragedProcessedResource.AVERAGED_POINTWISE_MODULUS;

        boolean showAveragedPointwiseModulusCurves = settings.isShown();
        if(showAveragedPointwiseModulusCurves)
        {
            int noOfPoints = settings.getPointCount();
            ErrorBarType errorBarType = settings.getErrorBarType();

            List<Channel1DData> pointwiseModulusAll = new ArrayList<>();

            for(ProcessedSpectroscopyPack pack : packs)
            {
                Channel1DData pointwiseModulus = pack.getPointwiseModulus();
                pointwiseModulusAll.add(pointwiseModulus);
            }

            Channel1DAveragingInDomainIntersection tr = new Channel1DAveragingInDomainIntersection(InterpolationMethod1D.LINEAR, noOfPoints, errorBarType);
            Channel1DDataWithErrors pointwiseModulusAveraged = tr.transform(pointwiseModulusAll);

            Channel1D channel = new Channel1DStandard(pointwiseModulusAveraged, Datasets.AVERAGED_POINTWISE_MODULUS_DATA);
            Channel1DCollection pointwiseModulusAveragedCollection = new Channel1DCollection(name, name);
            pointwiseModulusAveragedCollection.addChannel(channel);

            return pointwiseModulusAveragedCollection;
        }

        Channel1DCollection emptyInstance = new Channel1DCollection(name);
        return emptyInstance;
    }

    public void registerAveragingSettings(IdentityTag batchIdentityTag, SpectroscopyCurveAveragingSettings averagingSettings)
    {
        allAveragingSettings.put(batchIdentityTag, averagingSettings);
    }
}
