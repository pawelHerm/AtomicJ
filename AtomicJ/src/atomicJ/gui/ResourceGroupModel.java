package atomicJ.gui;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import atomicJ.resources.Resource;

public class ResourceGroupModel <R extends Resource> extends AbstractModel implements ResourceCollection<R>
{
    private int resourceCount = 0;
    private boolean resourcesEmpty = true;

    private List<R> resources = new ArrayList<>();
    private final List<ResourceGroupListener<? super R>> listeners = new ArrayList<>();


    @Override
    public int getResourceCount()
    {
        return resourceCount;
    }

    @Override
    public boolean isEmpty()
    {
        return resourcesEmpty;
    }

    public R getResource(String shortName)
    {
        R resource = null;

        for(int i = 0;  i < resources.size(); i++)
        {
            R currentResource = resources.get(i);

            String resourceShortName = currentResource.getShortName();

            if(shortName.equals(resourceShortName))
            {
                resource = currentResource;
                break;
            }
        }

        return resource;
    }

    public boolean containsResource(Object resource)
    {
        return resources.contains(resource);
    }

    public R getResource(int index)
    {
        return resources.get(index);
    }

    public List<R> getResources(int[] indices)
    {
        List<R> correspondingResources = new ArrayList<>();

        for(int index : indices)
        {
            correspondingResources.add(this.resources.get(index));
        }

        return correspondingResources;
    }

    public int getResourceIndex(Object resource)
    {
        return resources.indexOf(resource);
    }

    @Override
    public List<R> getResources()
    {
        return new ArrayList<>(resources);
    }

    public void setSelectedResources(List<R> resourcesNew)
    {
        List<R> resourcesRemoved = new ArrayList<>(this.resources);
        resourcesRemoved.removeAll(resourcesNew);

        List<R> resourcesAdded = new ArrayList<>(resourcesNew);
        resourcesAdded.removeAll(this.resources);

        this.resources = new ArrayList<>(resourcesNew);

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);

        fireResourcesAdded(Collections.unmodifiableList(resourcesAdded));
        fireResourcesRemoved(Collections.unmodifiableList(resourcesRemoved));
    }

    public void setResource(int index, R resourceNew)
    {
        R resourceOld = resources.set(index, resourceNew);
        fireResourceSet(index, resourceOld, resourceNew);
    }

    public void addResource(R resource)
    {
        this.resources.add(resource);

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);
        fireResourceAdded(resource);
    }

    public void addResources(Collection<R> resourcesAdded)
    {
        this.resources.addAll(resourcesAdded);

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);
        fireResourcesAdded(Collections.unmodifiableList(new ArrayList<>(resourcesAdded)));
    }

    public void removeResource(int index)
    {
        R removedResource = this.resources.remove(index);

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);
        fireResourceRemoved(index, removedResource);
    }

    //indices must be sorted in increasing order
    public void removeResources(int[] indices)
    {
        List<R> resourcesRemoved = new ArrayList<>();

        for(int i = indices.length - 1; i>= 0; i--)
        {     
            int indexToRemove = indices[i];
            resourcesRemoved.add(resources.remove(indexToRemove));
        }

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);
        fireResourcesRemoved(Collections.unmodifiableList(new ArrayList<>(resourcesRemoved)));
    }

    public void removeResources(Collection<R> resourcesRemoved)
    {
        this.resources.removeAll(resourcesRemoved);

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);
        fireResourcesRemoved(Collections.unmodifiableList(new ArrayList<>(resourcesRemoved)));
    }

    public void clearResources()
    {
        this.resources.clear();

        List<PropertyChangeEvent> events = updateProperties(this.resources);
        firePropertyChange(events);

        fireResourcesCleared();
    }

    protected List<PropertyChangeEvent> updateProperties(List<R> selectedResourcesNew)
    {
        List<PropertyChangeEvent> events = new ArrayList<>();
        events.add(checkIfResourcesEmptyChanged());
        events.add(checkIfResourcesCountChanged());

        return events;
    }

    private PropertyChangeEvent checkIfResourcesEmptyChanged()
    {
        boolean emptyOld = this.resourcesEmpty;
        this.resourcesEmpty = this.resources.isEmpty();

        PropertyChangeEvent evt = new PropertyChangeEvent(this, RESOURCES_EMPTY, emptyOld, this.resourcesEmpty);
        return evt;
    }

    private PropertyChangeEvent checkIfResourcesCountChanged()
    {
        int resourceCountOld = this.resourceCount;
        this.resourceCount = this.resources.size();

        PropertyChangeEvent evt = new PropertyChangeEvent(this, RESOURCE_COUNT, resourceCountOld, this.resourceCount);
        return evt;
    }

    public void addResultModelListener(ResourceGroupListener<? super R> listener) 
    {
        listeners.add(listener);
    }

    public void removeResultModelListener(ResourceGroupListener<? super R> listener) 
    {
        listeners.remove(listener);
    }

    private void fireResourceAdded(R resource)
    {
        for(ResourceGroupListener<? super R> listener : listeners)
        {
            listener.resourceAdded(resource);
        }
    }

    private void fireResourcesAdded(List<R> resources)
    {
        for(ResourceGroupListener<? super R> listener : listeners)
        {
            listener.resourcesAdded(resources);
        }
    }

    private void fireResourceRemoved(int index, R removedResource)
    {
        for(ResourceGroupListener<? super R> listener : listeners)
        {
            listener.resourceRemoved(index, removedResource);
        }
    }

    private void fireResourcesRemoved(List<R> resources)
    {
        for(ResourceGroupListener<? super R> listener : listeners)
        {
            listener.resourcesRemoved(resources);
        }
    }

    private void fireResourcesCleared()
    {
        for(ResourceGroupListener<? super R> listener : listeners)
        {
            listener.resourceCleared();
        }
    }

    private void fireResourceSet(int index, R resourceOld, R resourceNew)
    {
        for(ResourceGroupListener<? super R> listener : listeners)
        {
            listener.resourceSet(index, resourceOld, resourceNew);
        }
    }
}
