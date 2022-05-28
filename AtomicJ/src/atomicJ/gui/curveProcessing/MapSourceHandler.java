package atomicJ.gui.curveProcessing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.MapProcessingSettings;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.readers.ReadingPack;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;

public interface MapSourceHandler 
{
    public void handleProcessedPackRegistrationRequest(MapProcessingSettings mapSettings, ProcessedSpectroscopyPack pack);
    public void handleSealingRequest(Collection<MapSource<?>> mapSources);
    public void handlePublicationRequest(Map<MapSource<?>, List<ImageSource>>  mapSources);
    public void handleMapSourceAndImageAdditionRequest(MapProcessingSettings mapSettings,
            ProcessedSpectroscopyPack pack, Map<MapSource<?>, ReadingPack<ImageSource>> allMapSourcesTemporary );
}
