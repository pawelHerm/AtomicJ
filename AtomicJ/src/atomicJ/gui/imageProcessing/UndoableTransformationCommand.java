package atomicJ.gui.imageProcessing;

import java.util.Map;
import java.util.Set;

import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public class UndoableTransformationCommand extends UndoableBasicCommand<Channel2DResource, Channel2D, Channel2DData, String>
{   
    private final Channel2DDataTransformation transformation;

    public UndoableTransformationCommand(ResourceView<Channel2DResource, Channel2D, String> manager, String type, Channel2DResource resource,
            Channel2DDataTransformation transformation)
    {
        this(manager, type, null, resource, transformation, null);    
    }

    public UndoableTransformationCommand(ResourceView<Channel2DResource, Channel2D, String> manager, String type, Set<String> identifiers, Channel2DResource resource,
            Channel2DDataTransformation transformation, CommandIdentifier id)
    {
        super(manager, type, identifiers, resource, id);
        this.transformation = transformation;    
    }

    @Override
    public void execute()
    {
        super.execute();

        Channel2DResource resource = getResource();
        Set<String> identifiers = getIdentifiers();

        Map<String, Channel2D> channelsChanged = (identifiers != null) ? resource.transform(getType(), identifiers, transformation) : resource.transform(getType(), transformation);
        handleChangeOfData(channelsChanged);
    }   
}