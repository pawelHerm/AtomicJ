package atomicJ.gui.curveProcessing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.analysis.VisualizableSpectroscopyPack;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.Channel1DResultsView;
import atomicJ.resources.SpectroscopyProcessedResource;
import atomicJ.resources.StandardSpectroscopyProcessedResource;

public class CurveVisualizationStandardHandler implements CurveVisualizationHandler<VisualizableSpectroscopyPack> 
{
    private final Channel1DResultsView graphsView;

    public CurveVisualizationStandardHandler(Channel1DResultsView graphsView)
    {
        this.graphsView = graphsView;
    }

    @Override
    public void handlePublicationRequest(List<VisualizableSpectroscopyPack> visualizablePacks)
    {
        if(!visualizablePacks.isEmpty())
        {
            ConcurrentCurveVisualizationTask<VisualizableSpectroscopyPack> task = new ConcurrentCurveVisualizationTask<>(visualizablePacks, graphsView.getAssociatedWindow(), new StandardChartDrawingHandler());
            task.execute();
        }
    }

    private class StandardChartDrawingHandler implements ChartDrawingHandler<VisualizableSpectroscopyPack>
    {
        @Override
        public void sendChartsToDestination(Map<VisualizableSpectroscopyPack, Map<String, ChannelChart<?>>> charts)
        {
            Map<SpectroscopyProcessedResource, Map<String, ChannelChart<?>>> resourceChartMap = new LinkedHashMap<>();

            for(Entry<VisualizableSpectroscopyPack, Map<String, ChannelChart<?>>> entry : charts.entrySet())
            {
                VisualizableSpectroscopyPack pack = entry.getKey();
                resourceChartMap.put(new StandardSpectroscopyProcessedResource(pack.getPackToVisualize(), pack.getRecordedCurves(), pack.getIndentationCurve(), pack.getPointwiseModulusCurve()), entry.getValue());
            }

            int previousCount = graphsView.getResourceCount();    

            graphsView.addResources(resourceChartMap);
            graphsView.selectResource(previousCount);
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
