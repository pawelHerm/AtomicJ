package atomicJ.gui.curveProcessing;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.analysis.VisualizableSpectroscopyPack;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.Channel1DResultsView;
import atomicJ.resources.SpectroscopyProcessedResource;
import atomicJ.resources.StandardSpectroscopyProcessedResource;
import atomicJ.sources.SimpleSpectroscopySource;

public class CurveVisualizationReplaceHandler implements CurveVisualizationHandler<VisualizableSpectroscopyPack>
{
    private final Channel1DResultsView graphsView;
    private final Map<SimpleSpectroscopySource, SimpleSpectroscopySource> mapNewVsOldSource;

    public CurveVisualizationReplaceHandler(Channel1DResultsView graphsDialog, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> mapNewVsOldSource)
    {
        this.graphsView = graphsDialog;
        this.mapNewVsOldSource = new LinkedHashMap<>(mapNewVsOldSource);
    }

    @Override
    public void handlePublicationRequest(List<VisualizableSpectroscopyPack> visualizablePacks)
    {       
        if(!visualizablePacks.isEmpty())
        {
            ConcurrentCurveVisualizationTask<VisualizableSpectroscopyPack> task = new ConcurrentCurveVisualizationTask<>(visualizablePacks, graphsView.getAssociatedWindow(), new ReplaceChartVisualizationHandler());
            task.execute();
        }
        //even if we do not want to show newly plotted charts, we still have to delete old ones
        else
        {
            graphsView.removeResources(new LinkedHashSet<>(mapNewVsOldSource.values()));
        }
    }

    private class ReplaceChartVisualizationHandler implements ChartDrawingHandler<VisualizableSpectroscopyPack>
    { 
        @Override
        public void sendChartsToDestination(Map<VisualizableSpectroscopyPack, Map<String, ChannelChart<?>>> charts)
        {
            Map<SpectroscopyProcessedResource, Map<String, ChannelChart<?>>> resourceChartMap = new LinkedHashMap<>();
            Map<SpectroscopyProcessedResource, SpectroscopyProcessedResource> resourcesNewVsOld = new LinkedHashMap<>();

            for(Entry<VisualizableSpectroscopyPack, Map<String, ChannelChart<?>>> entry : charts.entrySet())
            {
                VisualizableSpectroscopyPack pack = entry.getKey();
                StandardSpectroscopyProcessedResource resource = new StandardSpectroscopyProcessedResource(pack.getPackToVisualize(), pack.getRecordedCurves(), pack.getIndentationCurve(), pack.getPointwiseModulusCurve());
                resourcesNewVsOld.put(resource, graphsView.getResourceContainingChannelsFrom(mapNewVsOldSource.get(pack.getPackToVisualize().getSource())));
                resourceChartMap.put(resource, entry.getValue());
            }

            graphsView.addOrReplaceResources(resourceChartMap, resourcesNewVsOld);

            int[] publishedChartIndices = graphsView.getIndicesOfPresentResources(charts.keySet());

            if(publishedChartIndices.length > 0)
            {
                graphsView.selectResource(publishedChartIndices[0]);
            }
        }

        @Override
        public void handleFinishDrawingRequest() 
        {
            graphsView.drawingChartsFinished();    
        }

        @Override
        public void reactToFailures(int failureCount)
        {
            if(failureCount > 0)
            {
                graphsView.showErrorMessage("Errors occured during rendering of " + failureCount + " charts");
            }           
        }   

        @Override
        public void reactToCancellation()
        {
            graphsView.showInformationMessage("Rendering terminated");
        }
    }
}