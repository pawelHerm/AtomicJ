package atomicJ.gui;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import atomicJ.resources.Resource;
import atomicJ.utilities.ArrayUtilities;

public class ResourceGroupSelectionModel <R extends Resource> extends AbstractModel implements ResourceCollection<R>
{    
    private int resourceCount = 0;
    private boolean resourceEmpty = true;

    private int selectedIndex = -1;
    private int[] selectedIndices = new int[] {};

    private R selectedResource;
    private List<R> selectedResources = new ArrayList<>();

    private final List<SelectionListener<? super R>> listeners = new ArrayList<>();

    private final ResourceGroupModel<R> dataModel;

    public ResourceGroupSelectionModel(ResourceGroupModel<R> dataModel)
    {
        this.dataModel = dataModel;
        initDataListener();
    }

    private void initDataListener()
    {
        this.dataModel.addResultModelListener(new ResourceGroupAdapter<R>()
        {
            @Override
            public void resourceSet(int index, R resourceOld, R resourceNew) 
            {
                if(ArrayUtilities.contains(selectedIndices, index))
                {
                    R selectedResourceOld = selectedResource;                
                    selectedResource = selectedIndex < 0 ? null : dataModel.getResource(selectedIndex);

                    List<R> selectedResourcesOld = selectedResources;
                    selectedResources = dataModel.getResources(selectedIndices);

                    List<PropertyChangeEvent> events = updateProperties(selectedResourcesOld, selectedResources);
                    firePropertyChange(events);

                    SelectionEvent<R> selectionEvent = new SelectionEvent<>(this, selectedResourceOld, selectedResource, 
                            Collections.unmodifiableList(selectedResources), selectedIndex, selectedIndices);

                    fireSelectionChanged(selectionEvent);

                }
            }
        });
    }

    @Override
    public int getResourceCount()
    {
        return resourceCount;
    }

    @Override
    public boolean isEmpty()
    {
        return resourceEmpty;
    }

    public boolean containsResource(Object resource)
    {
        boolean contains = selectedResources.contains(resource);
        return contains;
    }

    public R getSelectedResource()
    {
        return selectedResource;
    }

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    @Override
    public List<R> getResources()
    {
        return new ArrayList<>(selectedResources);
    }

    public int[] getSelectedIndices()
    {
        return selectedIndices;
    }

    public void setSelectedResource(R selectedResource)
    {
        int index = dataModel.getResourceIndex(selectedResource);

        setSelectedResources(index, new int[] {index});
    }

    public void setSelectedResource(int index)
    {
        setSelectedResources(index, new int[] {index});
    }

    public void setSelectedResources(int selectedIndexNew, int[] selectedIndicesNew)
    {              
        if((this.selectedIndex == selectedIndexNew) && (Arrays.equals(this.selectedIndices, selectedIndicesNew)))
        {
            //selection has not changed
            return;
        }

        if(selectedIndexNew < 0 && !dataModel.isEmpty())
        {
            attemptToRestoreSelection();
            return;
        }

        R selectedResourceOld = this.selectedResource;                
        this.selectedResource = selectedIndexNew < 0 ? null : dataModel.getResource(selectedIndexNew);

        List<R> selectedResourcesOld = this.selectedResources;
        this.selectedResources = dataModel.getResources(selectedIndicesNew);

        this.selectedIndex = selectedIndexNew;
        this.selectedIndices = selectedIndicesNew;       

        List<PropertyChangeEvent> events = updateProperties(selectedResourcesOld, this.selectedResources);
        firePropertyChange(events);

        SelectionEvent<R> selectionEvent = new SelectionEvent<>(this, selectedResourceOld, this.selectedResource, 
                Collections.unmodifiableList(this.selectedResources), this.selectedIndex, this.selectedIndices);

        fireSelectionChanged(selectionEvent);

    }

    //should be called when there is no resources selected
    //it tries to restore selection, ex. after the selected resources
    //are deleted by the user
    private void attemptToRestoreSelection()
    {        
        int n = dataModel.getResourceCount();
        int restoredSelectedIndex = Math.min(this.selectedIndex, n - 1);

        if(restoredSelectedIndex >= 0)
        {
            this.selectedIndex = restoredSelectedIndex;
            this.selectedResource = dataModel.getResource(restoredSelectedIndex);

            List<R> selectedResourcesOld = this.selectedResources;
            this.selectedResources = new ArrayList<>();
            this.selectedResources.add(this.selectedResource);
            this.selectedIndices = new int[] {this.selectedIndex};

            List<PropertyChangeEvent> events = updateProperties(selectedResourcesOld, this.selectedResources);
            firePropertyChange(events);

            //this event are called to fire GUI to change its state
            //it is often necessary to fire these events, as user' action may change
            //the GUI, so we need to reverse them firing them

            SelectionEvent<R> selectionEvent = new SelectionEvent<>(this, null, this.selectedResource, 
                    Collections.unmodifiableList(this.selectedResources), this.selectedIndex, this.selectedIndices);

            fireSelectionChanged(selectionEvent);
        }
    }

    protected List<PropertyChangeEvent> updateProperties(List<R> selectedResourcesOld, List<R> selectedResourcesNew)
    {
        List<PropertyChangeEvent> events = new ArrayList<>();
        events.add(checkIfResourcesEmptyChanged());
        events.add(checkIfResourcesCountChanged());

        return events;
    }

    private PropertyChangeEvent checkIfResourcesEmptyChanged()
    {
        boolean emptyOld = this.resourceEmpty;
        this.resourceEmpty = this.selectedResources.isEmpty();

        PropertyChangeEvent evt = new PropertyChangeEvent(this, ResourceCollection.RESOURCES_EMPTY, emptyOld, this.resourceEmpty);
        return evt;
    }

    private PropertyChangeEvent checkIfResourcesCountChanged()
    {
        int resourceCountOld = this.resourceCount;
        this.resourceCount = this.selectedResources.size();

        PropertyChangeEvent evt = new PropertyChangeEvent(this, ResourceCollection.RESOURCE_COUNT, resourceCountOld, this.resourceCount);

        return evt;
    }

    public void addSelectionListener(SelectionListener<? super R> listener)
    {
        listeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener<? super R> listener)
    {
        listeners.remove(listener);
    }

    private void fireSelectionChanged(SelectionEvent<R> event)
    {        
        for(SelectionListener<? super R> listener : listeners)
        {
            listener.selectionChanged(event);
        }
    }
}
