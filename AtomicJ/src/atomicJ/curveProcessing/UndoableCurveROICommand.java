package atomicJ.curveProcessing;

import java.util.Map;
import java.util.Set;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public class UndoableCurveROICommand<R extends SpectroscopyResource> extends UndoableBasicCommand<R, Channel1D, Channel1DData, String>
{   
    private final ROIRelativePosition position;
    private final ROI roi;
    private final Channel1DDataInROITransformation transformation;

    public UndoableCurveROICommand(ResourceView<R,Channel1D, String> manager, String type, Set<String> identifiers, R resource, Channel1DDataInROITransformation transformation, ROIRelativePosition position, ROI roi)
    {
        this(manager, type, identifiers, resource, transformation, position, roi, null);
    }

    public UndoableCurveROICommand(ResourceView<R,Channel1D, String> manager, String type, Set<String> identifiers,R resource,
            Channel1DDataInROITransformation transformation, ROIRelativePosition position, ROI roi, CommandIdentifier id)
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

        R resource = getResource();
        String type = getType();

        Map<String, Channel1D> channelsChanged = resource.transform(type, getIdentifiers(), transformation, roi, position);      

        handleChangeOfData(channelsChanged);
    }   
}