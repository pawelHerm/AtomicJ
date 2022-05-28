package atomicJ.gui.imageProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public abstract class ImageBatchSimpleProcessingModel  extends ImageBatchProcessingModel<Channel2DDataTransformation>
{
    public ImageBatchSimpleProcessingModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter) 
    {
        this(manager, channelFilter, false, true);
    }

    public ImageBatchSimpleProcessingModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter, boolean modifyAllTypes, boolean previewEnabledByDefault) 
    {
        super(manager, channelFilter, modifyAllTypes, previewEnabledByDefault);
    }

    @Override
    protected UndoableBasicCommand<?,?,?,?> getCommand(Channel2DDataTransformation tr, Channel2DResource resource, Set<String> identifiersToTransform)
    {
        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();
        String selectedType = manager.getSelectedType();

        UndoableTransformationCommand command = new UndoableTransformationCommand(manager, selectedType, identifiersToTransform, resource, tr, null);

        return command;
    }

    @Override
    protected List<UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?>> getCommands(Channel2DDataTransformation tr, List<? extends Channel2DResource> resorcesToProcess, Set<String> identifiersToTransform)
    {
        ResourceView<Channel2DResource, Channel2D, String> manager = getResourceManager();

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
                    UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?> command =  new UndoableTransformationCommand(manager, type, identifiersToTransform, resource, tr, compoundCommandId);
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
                UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?> command =  new UndoableTransformationCommand(manager, selectedType, identifiersToTransform, resource, tr, compoundCommandId);
                commands.add(command);
            }
        }

        return commands;
    }

}
