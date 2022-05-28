package atomicJ.curveProcessing;

import java.util.Map;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public class UndoableCurveCommand<R extends SpectroscopyResource> extends UndoableBasicCommand<R, Channel1D, Channel1DData, String>
{   
    private final Channel1DDataTransformation transformation;

    public UndoableCurveCommand(ResourceView<R,Channel1D, String> manager, String type,R resource,
            Channel1DDataTransformation transformation, CommandIdentifier id)
    {
        super(manager, type, null, resource, id);
        this.transformation = transformation;  
    }

    @Override
    public void execute()
    {
        super.execute();

        R resource = getResource();
        String type = getType();

        Map<String, Channel1D> channelsChanged = resource.transform(type, transformation);   

        handleChangeOfData(channelsChanged);
    }   
}