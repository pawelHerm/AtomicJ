package atomicJ.gui.imageProcessing;

import java.util.Map;
import java.util.Set;

import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public class UndoableImageROICommand extends UndoableBasicCommand<Channel2DResource, Channel2D, Channel2DData, String>
{   
    private final ROIRelativePosition position;
    private final ROI roi;
    private final Channel2DDataInROITransformation transformation;

    public UndoableImageROICommand(ResourceView<Channel2DResource, Channel2D, String> manager, String type,Set<String> identifiers, Channel2DResource resource,
            Channel2DDataInROITransformation transformation, ROIRelativePosition position, ROI roi)
    {
        this(manager, type, identifiers, resource, transformation, position, roi, null);
    }

    public UndoableImageROICommand(ResourceView<Channel2DResource, Channel2D, String> manager, String type,Set<String> identifiers, Channel2DResource resource,
            Channel2DDataInROITransformation transformation, ROIRelativePosition position, ROI roi, CommandIdentifier id)
    {
        super(manager, type, identifiers, resource, id);
        this.transformation = transformation;  
        this.position = position;
        this.roi = roi;
    }

    @Override
    public void execute()
    {
        super.execute();

        Channel2DResource resource = getResource();
        String type = getType();
        Set<String> identifiers = getIdentifiers();

        Map<String, Channel2D> channelsChanged = identifiers == null ? resource.transform(type, transformation, roi, position) : resource.transform(type, identifiers, transformation, roi, position);            
        handleChangeOfData(channelsChanged);
    }   
}