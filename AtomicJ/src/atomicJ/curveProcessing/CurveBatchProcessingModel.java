package atomicJ.curveProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import atomicJ.data.Channel1D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.generalProcessing.BatchProcessingModel;
import atomicJ.gui.generalProcessing.ConcurrentTransformationTask;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.utilities.MetaMap;

public abstract class CurveBatchProcessingModel<R extends SpectroscopyResource> extends BatchProcessingModel<R, Channel1D, String>
{
    public static final String DOMAIN_X_AXIS_DISPLAYED_UNIT = "DomainXAxisDisplayedUnit";

    private PrefixedUnit domainXAxisDisplayedUnit;
    private final MetaMap<R, String, UndoableCurveROICommand<R>> previewCommands = new MetaMap<>();

    public CurveBatchProcessingModel(ResourceView<R, Channel1D, String> manager, ChannelFilter2<Channel1D> channelFilter, boolean modifyAllTypes, boolean previewEnabledByDefault) 
    {
        super(manager, channelFilter, modifyAllTypes, previewEnabledByDefault);

        this.domainXAxisDisplayedUnit = manager.getDomainDisplayedUnits().get(0);
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

    protected abstract Channel1DDataInROITransformation buildTransformation();

    @Override
    public void apply()
    {        
        if(isApplyEnabled())
        {
            Channel1DDataInROITransformation tr = buildTransformation();
            if(tr != null)
            {
                super.apply();
                applyTransformation(tr);
            }
        }
    }

    @Override
    protected void updatePreview()
    {
        updatePreview(buildTransformation());
    }

    protected void updatePreview(Channel1DDataInROITransformation tr)
    {               
        if(!isPreviewEnabled())
        {
            return;
        }

        ResourceView<R, Channel1D, String> manager = getResourceManager();

        String selectedType = manager.getSelectedType();
        R selectedResource = manager.getSelectedResource();

        UndoableCurveROICommand<R> previousCommand = previewCommands.get(selectedResource, selectedType);

        if(previousCommand != null)
        {
            previousCommand.undo();
        }

        if(tr != null)
        {
            ROIRelativePosition position = getROIPosition();
            ROI roi = getSelectedROI();

            Set<String> identifiersToTransform = getIdentifiersToTransform();

            UndoableCurveROICommand<R> command = new UndoableCurveROICommand<>(manager, selectedType, identifiersToTransform, selectedResource, tr, position, roi, null);

            command.execute();

            previewCommands.put(selectedResource, selectedType, command);
        }       
    }

    @Override
    protected void resetPreview()
    {
        List<UndoableCurveROICommand<R>> commands = previewCommands.getAllValuesCopy();

        for(UndoableCurveROICommand<R> command : commands)
        {
            command.undo();
        }
    }

    protected void applyTransformation(Channel1DDataInROITransformation tr) 
    {        
        ResourceView<R, Channel1D, String> manager = getResourceManager();
        String selectedType = manager.getSelectedType();
        ROIRelativePosition position = getROIPosition();
        ROI roi = getSelectedROI();

        Set<String> identifiersToTransform = getIdentifiersToTransform();

        List<? extends R> resorcesToProcess = getResourcesToProcess();

        List<UndoableBasicCommand<R, Channel1D, ?, ?>> commands = new ArrayList<>();

        CommandIdentifier compundCommandId = resorcesToProcess.size() > 1 ? new CommandIdentifier() : null;

        for(R resource : resorcesToProcess)
        {
            UndoableCurveROICommand<R> command = new UndoableCurveROICommand<>(manager, selectedType, identifiersToTransform, resource, tr, position, roi, compundCommandId);
            commands.add(command);
        }

        ConcurrentTransformationTask<R,?> task = new ConcurrentTransformationTask<>(manager, commands);
        task.execute();
    }
}
