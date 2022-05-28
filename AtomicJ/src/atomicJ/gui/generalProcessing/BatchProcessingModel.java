package atomicJ.gui.generalProcessing;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.curveProcessing.TransformationBatchType;
import atomicJ.data.Channel;
import atomicJ.data.ChannelFilter2;
import atomicJ.gui.ResourceTypeListener;
import atomicJ.gui.SelectionEvent;
import atomicJ.gui.SelectionListener;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionAdapter;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MultiMap;

public abstract class BatchProcessingModel <R extends ChannelResource<E, ?, I>, E extends Channel, I> extends ProcessingModel
{
    public static final String PREVIEW_ENABLED = "PreviewEnabled";
    public static final String BATCH_TYPE = "BatchType";
    public static final String PROCESSING_FINISHED = "ProcessingFinished";

    private boolean previewEnabled = false;
    private TransformationBatchType batchType = TransformationBatchType.ONLY_SELECTED;

    private final ChannelFilter2<E> channelFilter;
    private final ResourceView<R,E, I> manager;
    private final BasicMultipleSelectionModel<I> identifierSelectionModel;

    private final CustomResourceSelectionListener resourceSelectionListener;
    private final CustomResourceTypeListener resourceTypeListener;

    private final boolean modifyAllTypes;

    public BatchProcessingModel(ResourceView<R, E, I> manager, ChannelFilter2<E> channelFilter)
    {
        this(manager, channelFilter, false, false);
    }

    public BatchProcessingModel(ResourceView<R, E, I> manager, ChannelFilter2<E> channelFilter, boolean modifyAllTypes, boolean previewEnabledByDefault)
    {
        super(manager.getDrawableROIs(), manager.getROIUnion(), manager.getUnitManager());

        this.channelFilter = channelFilter;
        this.manager = manager;
        this.modifyAllTypes = modifyAllTypes;
        this.previewEnabled = previewEnabledByDefault;

        Set<I> identifiersForSelectedType = manager.getSelectedResourcesChannelIdentifiers(manager.getSelectedType(), channelFilter);
        Set<I> identifiersForProcessing = modifyAllTypes ? manager.getAllResourcesChannelIdentifiers(channelFilter) : identifiersForSelectedType;

        this.identifierSelectionModel = new BasicMultipleSelectionModel<>(identifiersForProcessing, "", false); 
        this.identifierSelectionModel.setSelected(identifiersForSelectedType, true);

        this.resourceSelectionListener = new CustomResourceSelectionListener();
        this.resourceTypeListener = new CustomResourceTypeListener();

        this.manager.addResourceSelectionListener(resourceSelectionListener);
        this.manager.addResourceTypeListener(resourceTypeListener);

        initSelectionListener();
    }


    private class CustomResourceSelectionListener implements SelectionListener<R>
    {
        @Override
        public void selectionChanged(SelectionEvent<? extends R> event)
        {
            checkIfApplyEnabled();
            transformNewlySelected();
        }      
    }

    private class CustomResourceTypeListener implements ResourceTypeListener
    {
        @Override
        public void selectedTypeChanged(String oldType, String newType) 
        {            
            checkIfApplyEnabled();
            transformNewlySelected();
        }      
    }

    protected void transformNewlySelected()
    {}


    public void selectIdentifiersToTransform(Set<I> identifiers, boolean transform)
    {
        this.identifierSelectionModel.setSelected(identifiers, true);
    }

    private void initSelectionListener()    
    {
        this.identifierSelectionModel.addSelectionChangeListener(new MultipleSelectionAdapter<I>()
        {
            @Override
            public void allKeysSelectedChanged(boolean allSelectedOld, boolean allSelectedNew) 
            {
                checkIfApplyEnabled();
            }

            @Override
            public void allKeysDeselectedChanged(boolean allDeselectedOld, boolean allDeselectedNew) 
            {                                
                checkIfApplyEnabled();
            }

            @Override
            public void keySelectionChanged(I key, boolean selectedOld, boolean selectedNew)
            {
                //this  checkIfApplyEnabled() call is necessary here
                checkIfApplyEnabled();
                handleIdentifierSelectionChange(key, selectedOld, selectedNew);
            }
        });
    }

    protected abstract void updatePreview();
    protected abstract void resetPreview();

    @Override
    protected void handleChangeOfPosition()
    {
        super.handleChangeOfPosition();     
        updatePreview();
    }

    @Override
    protected void handleChangeOfSelectedROI()
    {
        super.handleChangeOfSelectedROI();
        updatePreview();
    };

    protected void handleIdentifierSelectionChange(I identifier, boolean selectedOld, boolean selectedNew)
    {
        ResourceView<R, E, I> manager = getResourceManager();

        String selectedType = manager.getSelectedType();
        R selectedResource = manager.getSelectedResource();

        Set<I> identifiersForSelected = selectedResource.getIdentifiers(selectedType);

        if(identifiersForSelected.contains(identifier))
        {
            updatePreview();
        }   
    }

    protected boolean isModifyAllTypes()
    {
        return modifyAllTypes;
    }


    public boolean isPreviewEnabled()
    {
        return previewEnabled;
    }

    public void setPreviewEnabled(boolean previewEnabledNew)
    {
        if(this.previewEnabled != previewEnabledNew)
        {
            boolean previewEnabledOld = this.previewEnabled;
            this.previewEnabled = previewEnabledNew;

            handlePreviewEnabledChange();
            firePropertyChange(PREVIEW_ENABLED, previewEnabledOld, previewEnabledNew);
        }
    }

    protected void handlePreviewEnabledChange()
    {
        if(isPreviewEnabled())
        {
            updatePreview();
        }
        else
        {
            resetPreview();
        }
    }

    @Override
    public void operationFinished()
    {
        super.operationFinished();

        this.manager.removeResourceSelectionListener(resourceSelectionListener);
        this.manager.removeResourceTypeListener(resourceTypeListener);

        if(!isApplied())
        {
            resetPreview();
        }
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        return super.calculateApplyEnabled() && (this.identifierSelectionModel.isAtLeastOneSelected());
    }

    public MultipleSelectionModel<I> getIdentifierSelectionModel()
    {
        return identifierSelectionModel;
    }

    public ResourceView<R,E,I> getResourceManager()
    {
        return manager;
    }

    public List<? extends R> getResourcesToProcess()
    {
        return batchType.getResources(manager);
    }

    public boolean isMultipleResourcesAvailable()
    {
        return manager.getResourceCount() > 1;
    }

    public TransformationBatchType getBatchType()
    {
        return batchType;
    }

    public void setBatchType(TransformationBatchType batchTypeNew)
    {       
        if(!this.batchType.equals(batchTypeNew))
        {
            TransformationBatchType batchTypeOld = this.batchType;
            this.batchType = batchTypeNew;

            setConsistentWithBatchType();

            firePropertyChange(BATCH_TYPE, batchTypeOld, batchTypeNew);
        }
    }

    public Set<I> getIdentifiersToTransform()
    {
        return Collections.unmodifiableSet(identifierSelectionModel.getSelectedKeys());
    }

    public Set<I> getIdentifiersToTransformFromSelectedType()
    {
        ResourceView<R, E, I> manager = getResourceManager();

        String selectedType = manager.getSelectedType();
        R selectedResource = manager.getSelectedResource();

        Set<I> identifiersForSelected = selectedResource.getIdentifiers(selectedType);
        Set<I> identifiersToTransformForSelected = new LinkedHashSet<>(identifiersForSelected);

        identifiersToTransformForSelected.retainAll(getIdentifiersToTransform());

        return identifiersToTransformForSelected;
    }

    public MultiMap<I, E> getChannelsToProcess()
    { 
        Set<I> identifiers = getIdentifiersToTransform();
        List<? extends R> resourcesToProcess = getResourcesToProcess();

        MultiMap<I, E> channelsToTransform = new MultiMap<>();
        for(R resource : resourcesToProcess)
        {
            channelsToTransform.putAll(resource.getChannelsForIdentifiers(identifiers));
        }

        return channelsToTransform;
    }

    public MultiMap<I, E> getSelectedResourceChannelsToProcess()
    { 
        Set<I> identifiers = getIdentifiersToTransform();
        R resource = manager.getSelectedResource();

        MultiMap<I, E> channelsToTransform = new MultiMap<>(resource.getChannelsForIdentifiers(identifiers));

        return channelsToTransform;
    }

    private void setConsistentWithBatchType()
    {
        String selectedType = manager.getSelectedType();

        Set<I> identifiers = TransformationBatchType.ALL.equals(batchType) ? manager.getAllResourcesChannelIdentifiers(selectedType, channelFilter) : manager.getSelectedResourcesChannelIdentifiers(selectedType, channelFilter);
        identifierSelectionModel.setKeys(identifiers);

        checkIfApplyEnabled();
    }
}
