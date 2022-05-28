package atomicJ.gui.curveProcessing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.analysis.MapProcessingSettings;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.gui.MapView;
import atomicJ.readers.ReadingPack;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;

public class MapSourceStandardHandler implements MapSourceHandler 
{
    private final MapView mapDialog;

    public MapSourceStandardHandler(MapView mapDialog)
    {
        this.mapDialog = mapDialog;
    }

    @Override
    public void handlePublicationRequest(Map<MapSource<?>, List<ImageSource>> mapSources)
    {
        if(containsData(mapSources))
        {
            mapDialog.drawMaps(mapSources);
            mapDialog.setVisible(true);
        }
    }

    @Override
    public void handleProcessedPackRegistrationRequest(MapProcessingSettings mapSettings, ProcessedSpectroscopyPack processedPack)
    {
        if(mapSettings.isIncudeInMaps())
        {
            processedPack.registerInMap();
        }            
    }

    @Override
    public void handleSealingRequest(Collection<MapSource<?>> mapSources)
    {
        for(MapSource<?> forceMap: mapSources)
        {              
            forceMap.seal();
        }            
    }

    @Override
    public void handleMapSourceAndImageAdditionRequest(MapProcessingSettings mapSettings,ProcessedSpectroscopyPack pack,
            Map<MapSource<?>, ReadingPack<ImageSource>> allMapSourcesTemporary)
    {
        boolean plotImages = mapSettings.isPlotMapAreaImages();

        if(pack.isFromMap())
        {
            MapSource<?> mapSource =  pack.getForceMap();
            if(mapSource != null && !allMapSourcesTemporary.containsKey(mapSource))
            {                         
                ReadingPack<ImageSource> readingPack = plotImages ? mapSource.getMapAreaImageReadingPack() : null;
                allMapSourcesTemporary.put(mapSource, readingPack);
            }   
        }
    }

    public static boolean containsData(Map<MapSource<?>, List<ImageSource>> mapAndImages)
    {
        boolean containsData = false;

        for(Entry<MapSource<?>, List<ImageSource>> entry : mapAndImages.entrySet())
        {
            MapSource<?> mapSource = entry.getKey();
            List<ImageSource> imageSource = entry.getValue();

            containsData = containsData || !imageSource.isEmpty() || mapSource.isProcessed();

            if(containsData)
            {
                break;
            }
        }

        return containsData;
    }
}