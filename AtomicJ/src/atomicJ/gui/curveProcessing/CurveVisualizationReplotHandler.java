package atomicJ.gui.curveProcessing;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.VisualizableSpectroscopyPack;
import atomicJ.data.Channel1D;
import atomicJ.gui.Channel1DResultsView;
import atomicJ.resources.SpectroscopyProcessedResource;
import atomicJ.resources.StandardSpectroscopyProcessedResource;
import atomicJ.sources.SimpleSpectroscopySource;

public class CurveVisualizationReplotHandler implements CurveVisualizationHandler<VisualizableSpectroscopyPack>
{
    private final Channel1DResultsView graphsDialog;
    private final Map<SimpleSpectroscopySource, SimpleSpectroscopySource> mapNewVsOldSource;

    public CurveVisualizationReplotHandler(Channel1DResultsView graphsDialog, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> mapNewVsOldSource)
    {
        this.graphsDialog = graphsDialog;
        this.mapNewVsOldSource = new LinkedHashMap<>(mapNewVsOldSource);
    }

    @Override
    public void handlePublicationRequest(List<VisualizableSpectroscopyPack> visualizablePacks)
    {               
        if(!visualizablePacks.isEmpty())
        {
            Map<SpectroscopyProcessedResource, Map<String, Collection<? extends Channel1D>>> channels = new LinkedHashMap<>();
            Map<SpectroscopyProcessedResource, SpectroscopyProcessedResource> resourcesNewVsOld = new LinkedHashMap<>();

            for(VisualizableSpectroscopyPack visPack : visualizablePacks)
            {
                ProcessedSpectroscopyPack processedPack = visPack.getPackToVisualize();
                SpectroscopyProcessedResource resource = new StandardSpectroscopyProcessedResource(processedPack, visPack.getRecordedCurves(), visPack.getIndentationCurve(), visPack.getPointwiseModulusCurve());
                resourcesNewVsOld.put(resource, graphsDialog.getResourceContainingChannelsFrom(mapNewVsOldSource.get(processedPack.getSource())));

                Map<String, Collection<? extends Channel1D>> charts = visPack.getChannels();
                channels.put(resource, charts);
            }

            graphsDialog.replaceData(channels, resourcesNewVsOld);
        }       
    }
}