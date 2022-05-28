package atomicJ.gui;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.data.SampleCollection;
import atomicJ.resources.SpectroscopyProcessedResource;

public class GraphicalResultsViewModel extends SpectroscopyViewModel<SpectroscopyProcessedResource>
{
    public RecalculateSpectroscopyResourcesModel getRecalculationModel()
    {
        return new RecalculateSpectroscopyResourcesModel(getDataModel(), getSelectionModel());
    }

    public List<SampleCollection> getRawDataForSelectedResource()
    {
        SpectroscopyProcessedResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            List<SampleCollection> collections = selectedResource.getSampleCollectionsRawData();
            return collections;
        }  

        return Collections.emptyList();
    }

    public List<SampleCollection> getAllRawResourceData()
    {   
        List<SampleCollection> rawData = new ArrayList<>();

        List<SpectroscopyProcessedResource> selectedResources = getAllSelectedResources();
        for(SpectroscopyProcessedResource resource : selectedResources)
        {
            List<SampleCollection> collections = resource.getSampleCollectionsRawData();
            rawData.addAll(collections);
        }

        return rawData;
    }
}
