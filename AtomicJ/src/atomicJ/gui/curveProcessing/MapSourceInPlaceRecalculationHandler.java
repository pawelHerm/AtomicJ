package atomicJ.gui.curveProcessing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.MapProcessingSettings;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.gui.MapView;
import atomicJ.readers.ReadingPack;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;
import atomicJ.utilities.MultiMap;

public class MapSourceInPlaceRecalculationHandler implements MapSourceHandler
{
    private final MultiMap<MapSource<?>, ProcessedSpectroscopyPack> packsToReplace = new MultiMap<>();

    private final MapView mapDialog;

    public MapSourceInPlaceRecalculationHandler(MapView mapDialog)
    {
        this.mapDialog = mapDialog;
    }

    @Override
    public void handleProcessedPackRegistrationRequest(MapProcessingSettings mapSettings, ProcessedSpectroscopyPack pack)
    {
        if(mapSettings.isIncudeInMaps())
        {
            packsToReplace.put(pack.getForceMap(), pack);
        }              
    }

    @Override
    public void handleSealingRequest(Collection<MapSource<?>> mapSources) {            
    }

    @Override
    public void handlePublicationRequest(Map<MapSource<?>, List<ImageSource>> mapSources)
    {
        mapDialog.replace(packsToReplace, mapSources);
    }

    @Override
    public void handleMapSourceAndImageAdditionRequest(MapProcessingSettings mapSettings,
            ProcessedSpectroscopyPack pack, Map<MapSource<?>, ReadingPack<ImageSource>> allMapSourcesTemporary)
    {            
        if(pack.isFromMap())
        {
            MapSource<?> mapSource =  pack.getForceMap();
            if(mapSource != null && !allMapSourcesTemporary.containsKey(mapSource))
            {                         
                allMapSourcesTemporary.put(mapSource, null);
            }   
        }
    }       
}