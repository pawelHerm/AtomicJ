package atomicJ.gui.imageProcessing;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.generalProcessing.BatchProcessingModel;
import atomicJ.gui.generalProcessing.ConcurrentTransformationTask;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MetaMap;

public abstract class ImageBatchProcessingModel <E extends Channel2DDataTransformation>  extends BatchProcessingModel<Channel2DResource, Channel2D, String>
{
    public static final String DOMAIN_X_AXIS_DISPLAYED_UNIT = "DomainXAxisDisplayedUnit";
    public static final String DOMAIN_Y_AXIS_DISPLAYED_UNIT = "DomainYAxisDisplayedUnit";

    private PrefixedUnit domainXAxisDisplayedUnit;
    private PrefixedUnit domainYAxisDisplayedUnit;
    private final PrefixedUnit domainXAxisDataUnit;
    private final PrefixedUnit domainYAxisDataUnit;

    private final MetaMap<Channel2DResource, String, UndoableBasicCommand<?, ?, ?, ?>> previewCommands = new MetaMap<>();

    public ImageBatchProcessingModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter) 
    {
        this(manager, channelFilter, false, true);
    }

    public ImageBatchProcessingModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter, boolean modifyAllTypes, boolean previewEnabledByDefault) 
    {
        super(manager, channelFilter, modifyAllTypes, previewEnabledByDefault);

        this.domainXAxisDisplayedUnit = manager.getDomainDisplayedUnits().get(0);
        this.domainYAxisDisplayedUnit = manager.getDomainDisplayedUnits().get(1);
        this.domainXAxisDataUnit = manager.getDomainDataUnits().get(0);
        this.domainYAxisDataUnit = manager.getDomainDataUnits().get(1);
    }

    public PrefixedUnit getDomainXAxisDisplayedUnit()
    {
        return domainXAxisDisplayedUnit;
    }

    public void setDomainXAxisDisplayedUnit(PrefixedUnit displayedUnitNew)
    {
        if(!Objects.equals(this.domainXAxisDisplayedUnit, displayedUnitNew))
        {
            PrefixedUnit displayedUnitOld = this.domainXAxisDisplayedUnit;
            this.domainXAxisDisplayedUnit = displayedUnitNew;

            firePropertyChange(DOMAIN_X_AXIS_DISPLAYED_UNIT, displayedUnitOld, displayedUnitNew);
        }
    }

    public PrefixedUnit getDomainYAxisDisplayedUnit()
    {
        return domainYAxisDisplayedUnit;
    }

    public void setDomainYAxisDisplayedUnit(PrefixedUnit displayedUnitNew)
    {
        if(!Objects.equals(this.domainYAxisDisplayedUnit, displayedUnitNew))
        {
            PrefixedUnit displayedUnitOld = this.domainYAxisDisplayedUnit;
            this.domainYAxisDisplayedUnit = displayedUnitNew;

            firePropertyChange(DOMAIN_Y_AXIS_DISPLAYED_UNIT, displayedUnitOld, displayedUnitNew);
        }
    }

    public PrefixedUnit getDomainXAxisDataUnit()
    {
        return domainXAxisDataUnit;
    }

    public PrefixedUnit getDomainYAxisDataUnit()
    {
        return domainYAxisDataUnit;
    }

    @Override
    protected void updatePreview()
    {       
        if(!isPreviewEnabled())
        {
            return;
        }

        E tr = buildTransformation();

        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();

        String selectedType = manager.getSelectedType();
        Channel2DResource selectedResource = manager.getSelectedResource();

        UndoableBasicCommand<?,?,?,?> previousCommand = previewCommands.get(selectedResource, selectedType);

        if(previousCommand != null)
        {
            previousCommand.undo();
        }

        if(tr != null)
        {
            UndoableBasicCommand<?,?,?,?> command = getCommand(tr, selectedResource, getIdentifiersToTransform());
            command.execute();

            previewCommands.put(selectedResource, selectedType, command);
        }       
    }

    @Override
    protected void transformNewlySelected()
    {
        E tr = buildTransformation();

        if(tr == null)
        {
            return;
        }

        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();

        String selectedType = manager.getSelectedType();
        Channel2DResource selectedResource = manager.getSelectedResource();

        UndoableBasicCommand<?,?,?,?> previousCommand = previewCommands.get(selectedResource, selectedType);

        if(previousCommand == null)
        {
            UndoableBasicCommand<?,?,?,?> command = getCommand(tr, selectedResource, getIdentifiersToTransform());
            command.execute();

            previewCommands.put(selectedResource, selectedType, command);

        }
    }

    protected abstract UndoableBasicCommand<?,?,?,?> getCommand(E tr, Channel2DResource resource, Set<String> identifiersToTransform);

    @Override
    protected void resetPreview()
    {
        List<UndoableBasicCommand<?,?,?,?>> commands = previewCommands.getAllValuesCopy();

        for(UndoableBasicCommand<?,?,?,?> command : commands)
        {
            command.undo();
        }
    }

    protected abstract E buildTransformation();

    @Override
    public void apply()
    {        
        if(isApplyEnabled())
        {
            E tr = buildTransformation();
            if(tr != null)
            {
                super.apply();
                applyTransformation(tr);
            }
        }
    }

    protected void applyTransformation(E tr) 
    {
        resetPreview();

        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();

        Set<String> identifiersToTransform = getIdentifiersToTransform();
        List<? extends Channel2DResource> resorcesToProcess = getResourcesToProcess();

        List<UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?>> commands = getCommands(tr, resorcesToProcess, identifiersToTransform);

        ConcurrentTransformationTask<Channel2DResource,?> task = new ConcurrentTransformationTask<>(manager, commands);
        task.execute();
    }

    protected abstract List<UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?>> getCommands(E tr, List<? extends Channel2DResource> resorcesToProcess, Set<String> identifiersToTransform);
}
