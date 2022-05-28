package atomicJ.gui;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import atomicJ.resources.DataModelResource;
import atomicJ.resources.Resource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.ArrayUtilities;

public class ResourceViewModel <R extends DataModelResource> extends AbstractModel
{    
    public static final String SELECTED_TYPE = "SelectedType";

    public static final String SELECTED_RESOURCES_COUNT = "SelectedResourcesCount";
    public static final String SELECED_RESOURCES_EMPTY = "SelectedResourcesEmpty";

    public static final String ALL_RESOURCES_COUNT = "AllResourcesCount";
    public static final String ALL_RESOURCES_EMPTY = "AllResourcesEmpty";

    private final List<ResourceTypeListener> typeListeners = new ArrayList<>();
    private final ResourceGroupModel<R> dataGroup = new ResourceGroupModel<>();
    private final ResourceGroupSelectionModel<R> selectionGroup = new ResourceGroupSelectionModel<>(dataGroup);

    private String selectedType;

    public ResourceViewModel()
    {
        initDataGroupListeners();
        initSelectionGroupListeners();
    }

    public ResourceViewModel(Collection<R> resources, String selectedType)
    {
        this.dataGroup.addResources(resources);
        this.selectedType = selectedType;

        initDataGroupListeners();
        initSelectionGroupListeners();
    }

    public ResourceGroupModel<R> getDataModel()
    {
        return dataGroup;
    }

    public ResourceGroupSelectionModel<R> getSelectionModel()
    {
        return selectionGroup;
    }

    private void initDataGroupListeners()
    {
        this.dataGroup.addPropertyChangeListener(ResourceCollection.RESOURCES_EMPTY, new PropertyChangeListener() 
        {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                firePropertyChange(ALL_RESOURCES_EMPTY, evt.getOldValue(), evt.getNewValue());
            }
        });

        this.dataGroup.addPropertyChangeListener(ResourceCollection.RESOURCE_COUNT, new PropertyChangeListener() 
        {           
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                firePropertyChange(ALL_RESOURCES_COUNT, evt.getOldValue(), evt.getNewValue());
            }
        });
    }

    private void initSelectionGroupListeners()
    {
        this.selectionGroup.addPropertyChangeListener(ResourceCollection.RESOURCES_EMPTY, new PropertyChangeListener() 
        {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                firePropertyChange(SELECED_RESOURCES_EMPTY, evt.getOldValue(), evt.getNewValue());
            }
        });

        this.selectionGroup.addPropertyChangeListener(ResourceCollection.RESOURCE_COUNT, new PropertyChangeListener() 
        {           
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                firePropertyChange(SELECTED_RESOURCES_COUNT, evt.getOldValue(), evt.getNewValue());
            }
        });
    }

    public String getSelectedType()
    {
        return selectedType;
    }

    public void setSelectedType(String selectedTypeNew)
    {        
        if(!Objects.equals(this.selectedType, selectedTypeNew))
        {
            String selectedTypeOld = this.selectedType;
            this.selectedType = selectedTypeNew;

            firePropertyChange(SELECTED_TYPE, selectedTypeOld, selectedTypeNew);
            fireSelectedTypeChanged(selectedTypeOld, selectedTypeNew);
        }
    }

    public void addResourceTypeListener(ResourceTypeListener listener)
    {
        this.typeListeners.add(listener);
    }

    public void removeResourceTypeListener(ResourceTypeListener listener)
    {
        this.typeListeners.remove(listener);
    }

    protected void fireSelectedTypeChanged(String selectedTypeOld, String selectedTypeNew)
    {
        for(ResourceTypeListener listener : typeListeners)  
        {
            listener.selectedTypeChanged(selectedTypeOld, selectedTypeNew);
        }
    }

    public R getSelectedResource()
    {
        return this.selectionGroup.getSelectedResource();
    }

    public List<R> getAllSelectedResources()
    {
        return this.selectionGroup.getResources();
    }

    public int getSelectedIndex()
    {
        return this.selectionGroup.getSelectedIndex();
    }

    public int[] getIndicesOfAllSelectedResources()
    {
        return this.selectionGroup.getSelectedIndices();                
    }

    public void setSelectedResource(R selectedResource)
    {
        if(!containsResource(selectedResource))
        {
            throw new IllegalArgumentException("Resource not found");
        }

        this.selectionGroup.setSelectedResource(selectedResource);
    }

    public void setSelectedResource(int selectedIndex)
    {
        this.selectionGroup.setSelectedResource(selectedIndex);
    }

    public void setSelectedResources(int selectedIndexNew, int[] selectedIndicesNew)
    {
        this.selectionGroup.setSelectedResources(selectedIndexNew, selectedIndicesNew);
    }

    public void setSelectedResource(String shortName)
    {
        R resource = getResource(shortName);

        if(resource == null)
        {
            throw new IllegalArgumentException("Resource not found");
        }

        setSelectedResource(resource);
    }

    public boolean containsResource(Object resource)
    {
        return this.dataGroup.containsResource(resource);
    }

    public int getResourceCount()
    {
        return dataGroup.getResourceCount();
    }

    public boolean isDataEmpty()
    {
        return this.dataGroup.isEmpty();
    }

    public R getResource(int index)
    {
        return dataGroup.getResource(index);
    }

    public List<R> getResources(int[] indices)
    {
        return this.dataGroup.getResources(indices);
    }

    public R getResource(String shortName)
    {
        return this.dataGroup.getResource(shortName);
    }

    public int getResourceIndex(Object resource)
    {
        return this.dataGroup.getResourceIndex(resource);
    }

    //returns indices in increasing order,
    //ONLY of THOSE RESOURCES THAT ARE CONTAINED IN THE RESOURCE LIST OF THIS OBJECT
    public int[] getIndicesOfPresentResources(Set<?> resources)
    {
        List<Integer> indices = new ArrayList<>(); 
        for(Object resource : resources)
        {
            int index = getResourceIndex(resource);
            if(index > -1)
            {
                indices.add(index);
            }
        }

        int[] indicesArray = ArrayUtilities.getIntArray(indices);
        Arrays.sort(indicesArray);

        return indicesArray;
    }

    public List<R> getResources()
    {
        return this.dataGroup.getResources();
    }

    public void setResource(int index, R resourceNew)
    {
        this.dataGroup.setResource(index, resourceNew);
    }

    public void addResource(R resource)
    {
        this.dataGroup.addResource(resource);
    }

    public void addResources(Collection<R> resourcesAdded)
    {
        this.dataGroup.addResources(resourcesAdded);
    }

    public void removeResource(int index)
    {
        this.dataGroup.removeResource(index);
    }

    //indices must be sorted in increasing order
    public void removeResources(int[] indices)
    {
        this.dataGroup.removeResources(indices);
    }

    public void removeResources(Collection<R> resourcesRemoved)
    {
        this.dataGroup.removeResources(resourcesRemoved);
    }

    public R getResourceContainingChannelsFrom(ChannelSource source)
    {
        for(R resource : getResources())
        {
            if(resource.containsChannelsFromSource(source))  
            {
                return resource;
            }
        }

        return null;
    }

    public List<R> getResourcesContainingChannelsFrom(ChannelSource source)
    {
        List<R> containingResources = new ArrayList<>();

        for(R resource : getResources())
        {
            if(resource.containsChannelsFromSource(source))  
            {
                containingResources.add(resource);
            }
        }

        return containingResources;
    }

    public boolean containsChannelsFromSource(ChannelSource source)
    {
        boolean contains = (getResourceContainingChannelsFrom(source) != null);
        return contains;
    }


    public void clearResources()
    {
        this.dataGroup.clearResources();
    }

    public Map<String, String> getDefaultChartTitles()
    {                
        Resource resource = getSelectedResource();       
        return resource.getAutomaticChartTitles();
    }

    public void addSelectionListener(SelectionListener<? super R> listener)
    {
        this.selectionGroup.addSelectionListener(listener);
    }

    public void removeSelectionListener(SelectionListener<? super R> listener)
    {
        this.selectionGroup.removeSelectionListener(listener);
    }

    public void addDataModelListener(ResourceGroupListener<? super R> listener)
    {
        this.dataGroup.addResultModelListener(listener);
    }

    public void removeDataModelListener(ResourceGroupListener<? super R> listener)
    {
        this.dataGroup.removeResultModelListener(listener);
    }
}
