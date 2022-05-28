package atomicJ.gui;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.sources.SimpleSpectroscopySource;

public class SpectroscopyViewModel<R extends SpectroscopyResource> extends ChannelResourceDialogModel<Channel1D,Channel1DData,String, R>
{     
    public static final String SELECTED_SOURCES_FROM_MAPS = "SelectedSourcesFromMap";

    private boolean selectedSourcesFromMap;

    public SpectroscopyViewModel()
    {
        this.selectedSourcesFromMap = SpectroscopyResource.containsResourcesFromMap(getAllSelectedResources());
        initSelectionListeners();
    }

    public boolean isSelectedSourcesFromMap()
    {
        return selectedSourcesFromMap;
    }

    public SimpleSpectroscopySource getSourceFromSelectedResource()
    {
        R resource = getSelectedResource();
        SimpleSpectroscopySource source = (resource != null) ? resource.getSource() : null;
        return source;
    }

    public List<SimpleSpectroscopySource> getSourcesFromAllSelectedResources()
    {
        return Channel1DResource.getSources(getAllSelectedResources());
    }  

    public List<SimpleSpectroscopySource> getSources()
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();
        List<R> resources = getResources();

        for(R resource : resources)
        {
            sources.add(resource.getSource());
        }

        return sources;
    }

    private void initSelectionListeners()
    {
        addSelectionListener(new SelectionListener<R>()
        {
            @Override
            public void selectionChanged(SelectionEvent<? extends R> event) 
            {
                boolean selectedPacksFromMapOld = selectedSourcesFromMap;
                SpectroscopyViewModel.this.selectedSourcesFromMap = SpectroscopyResource.containsResourcesFromMap(event.getSelectedItems()); 

                firePropertyChange(SELECTED_SOURCES_FROM_MAPS, selectedPacksFromMapOld, SpectroscopyViewModel.this.selectedSourcesFromMap);

            }         
        });
    }
}
