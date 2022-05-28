package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.imageProcessing.AddLinearFunctionTransformation;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public class AddPlaneModel extends PlaneBasedCorrectionModel
{ 
    public AddPlaneModel(ResourceView<Channel2DResource, Channel2D, String> manager) 
    {
        super(manager, PermissiveChannel2DFilter.getInstance(), true, true);
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation() 
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Channel2DDataInROITransformation tr = new AddLinearFunctionTransformation(getFunction(), true);
        return tr;
    }
}