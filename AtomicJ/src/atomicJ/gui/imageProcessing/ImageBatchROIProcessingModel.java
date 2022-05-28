package atomicJ.gui.imageProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public abstract class ImageBatchROIProcessingModel  extends ImageBatchProcessingModel<Channel2DDataInROITransformation>
{
    public ImageBatchROIProcessingModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter) 
    {
        this(manager, channelFilter, false, true);
    }

    public ImageBatchROIProcessingModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter, boolean modifyAllTypes, boolean previewEnabledByDefault) 
    {
        super(manager, channelFilter, modifyAllTypes, previewEnabledByDefault);
    }

    @Override
    protected UndoableBasicCommand<?,?,?,?> getCommand(Channel2DDataInROITransformation tr, Channel2DResource resource, Set<String> identifiersToTransform)
    {
        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();
        String selectedType = manager.getSelectedType();

        ROIRelativePosition position = getROIPosition();
        ROI roi = getSelectedROI();

        UndoableImageROICommand command = new UndoableImageROICommand(manager, selectedType, identifiersToTransform, resource, tr, position, roi, null);

        return command;
    }

    @Override
    protected List<UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?>> getCommands(Channel2DDataInROITransformation tr, List<? extends Channel2DResource> resorcesToProcess, Set<String> identifiersToTransform)
    {
        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();
        ROIRelativePosition position = getROIPosition();
        ROI roi = getSelectedROI();

        List<UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?>> commands = new ArrayList<>();

        if(isModifyAllTypes())
        {
            int commandCount = 0;
            for(Channel2DResource resource : resorcesToProcess)
            {
                commandCount += resource.getAllTypes().size();
            }

            CommandIdentifier compoundCommandId = commandCount > 1 ? new CommandIdentifier() : null;

            for(Channel2DResource resource : resorcesToProcess)
            {
                for(String type : resource.getAllTypes())
                {
                    UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?> command =  new UndoableImageROICommand(manager, type, identifiersToTransform, resource, tr, position, roi, compoundCommandId);
                    commands.add(command);
                }
            }
        }
        else
        {
            String selectedType = manager.getSelectedType();

            int commandCount = resorcesToProcess.size();
            CommandIdentifier compoundCommandId = commandCount > 1 ? new CommandIdentifier() : null;

            for(Channel2DResource resource : resorcesToProcess)
            {
                UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?> command =  new UndoableImageROICommand(manager, selectedType, identifiersToTransform, resource, tr, position, roi, compoundCommandId);
                commands.add(command);
            }
        }

        return commands;
    }
}
