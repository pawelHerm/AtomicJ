package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Kernel2D;
import atomicJ.imageProcessing.KernelConvolution;
import atomicJ.imageProcessing.KernelSampledGaussian2D;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public class GaussianFilter2DModel extends GaussianBasedFilter2DModel
{
    public GaussianFilter2DModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true);
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Kernel2D kernel = new KernelSampledGaussian2D(getSigmaX(), getSigmaY());
        Channel2DDataInROITransformation transformation = new KernelConvolution(kernel);
        return transformation;
    }
}
