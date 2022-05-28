package atomicJ.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import atomicJ.resources.Resource;

public abstract class ResourceBasedModel<R extends Resource> extends AbstractModel implements ResourceGroupListener<R>, SelectionListener<R>
{
    public static final String BATCH_IDENTITY_ALL = "All";

    public static final String RESTRICT_TO_SELECTION = "RestrictToSelection";
    public static final String DATA_AVAILABLE = "DataAvailable";
    public static final String SELECTED_DATA_AVAILABLE = "SelectedDataAvailable";
    public static final String APPLY_ENABLED = "ApplyEnabled";

    private boolean restrictToSelection;

    private boolean selectedDataAvailable;
    private boolean dataAvailable;

    private final ResourceGroupModel<R> dataModel;
    private final ResourceGroupSelectionModel<R> selectionModel;

    private ResourceCollection<R> currentResourceCollection;

    private boolean applied;
    private boolean applyEnabled;

    public ResourceBasedModel(ResourceGroupModel<R> dataModel, ResourceGroupSelectionModel<R> selectionModel)
    {     
        this.dataModel = dataModel;
        this.selectionModel = selectionModel;
        this.currentResourceCollection = restrictToSelection ? selectionModel : dataModel;

        this.selectedDataAvailable = !selectionModel.isEmpty();
        this.dataAvailable = !dataModel.isEmpty();

        this.applyEnabled = !currentResourceCollection.isEmpty();

        this.dataModel.addResultModelListener(this);
        this.selectionModel.addSelectionListener(this);

        initPropertyListeners();
    }

    public boolean isDataAvailable()
    {
        return dataAvailable;
    }

    public boolean isSelectedDataAvailable()
    {
        return selectedDataAvailable;
    }   

    //made final so that it can be called from subclass constructors
    public final List<R> getData()
    {
        return currentResourceCollection.getResources();
    }

    private void initPropertyListeners()
    {
        this.dataModel.addPropertyChangeListener(ResourceCollection.RESOURCES_EMPTY, 
                new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                boolean dataAvailableOld = ResourceBasedModel.this.dataAvailable;
                ResourceBasedModel.this.dataAvailable = !(boolean)evt.getNewValue();

                firePropertyChange(checkIfApplyEnabled());

                firePropertyChange(DATA_AVAILABLE, dataAvailableOld, ResourceBasedModel.this.dataAvailable);
            }
        });

        this.selectionModel.addPropertyChangeListener(ResourceCollection.RESOURCES_EMPTY, 
                new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                boolean selectedDataAvailableOld = ResourceBasedModel.this.selectedDataAvailable;
                ResourceBasedModel.this.selectedDataAvailable = !(boolean)evt.getNewValue();

                firePropertyChange(checkIfApplyEnabled());

                firePropertyChange(SELECTED_DATA_AVAILABLE, selectedDataAvailableOld, ResourceBasedModel.this.selectedDataAvailable);

                if(!selectedDataAvailable)
                {
                    setRestrictToSelection(false);
                }
            }
        });
    }

    public boolean isRestrictToSelection() {
        return restrictToSelection;
    }

    public void setRestrictToSelection(boolean restrictToSelectionNew) 
    {
        if(this.restrictToSelection != restrictToSelectionNew)
        {
            if(!selectedDataAvailable && restrictToSelectionNew)
            {
                //this firing of property change may seem artificial, as the model property
                //did not really changed. However, it is necessary, as it may be the case
                //that setRestrictToSelectedPacks() was called by a Swing component,
                //whose state changed immediately upon user's action (ex. mouse click)
                //then the model must enforce reversion of its state by calling this 'artificial'
                //event. In this way we prevent inconsistency between the model and the user's interface

                firePropertyChange(RESTRICT_TO_SELECTION, restrictToSelectionNew, this.restrictToSelection);
                return;
            }

            boolean restrictToSelectionOld = this.restrictToSelection;
            this.restrictToSelection = restrictToSelectionNew;

            this.currentResourceCollection = restrictToSelection ? selectionModel : dataModel;

            List<PropertyChangeEvent> evts = updateProperties(getData());
            firePropertyChange(evts);

            firePropertyChange(RESTRICT_TO_SELECTION, restrictToSelectionOld, restrictToSelectionNew);
        }
    }

    protected List<PropertyChangeEvent> updateProperties(List<R> resourcesNew)
    {
        List<PropertyChangeEvent> events = new ArrayList<>();
        events.add(checkIfApplyEnabled());
        return events;
    }    

    private PropertyChangeEvent checkIfApplyEnabled()
    {
        boolean applyEnabledOld = this.applyEnabled;

        this.applyEnabled = !currentResourceCollection.isEmpty();

        PropertyChangeEvent evt = new PropertyChangeEvent(this, APPLY_ENABLED, applyEnabledOld, this.dataAvailable);
        return evt;
    }




    public abstract void reset();

    public boolean isApplied()
    {
        return applied;
    }

    public void apply()
    {
        this.applied = true;
    }

    public boolean isApplyEnabled()
    {
        return applyEnabled;
    }

    @Override
    public void resourceCleared() 
    {
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }

    @Override
    public void resourceSet(int index, R resourceOld, R resourceNew) 
    {
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }

    @Override
    public void resourceAdded(R resource)
    {
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }
    @Override
    public void resourcesAdded(List<? extends R> resourcesAdded)
    {
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }
    @Override
    public void resourceRemoved(int index, R removedResource) 
    {
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }

    @Override
    public void resourcesRemoved(List<? extends R> resourcesRemoved) 
    {
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }


    @Override
    public void selectionChanged(SelectionEvent<? extends R> event)
    {      
        List<PropertyChangeEvent> evts = updateProperties(getData());
        firePropertyChange(evts);
    }
}
