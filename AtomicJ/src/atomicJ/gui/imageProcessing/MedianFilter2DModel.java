package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.MedianFilter2D;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public class MedianFilter2DModel extends ImageBatchROIProcessingModel
{
    public static final String KERNEL_X_RADIUS = "KernelXRadius";
    public static final String KERNEL_Y_RADIUS = "KernelYRadius";

    private int kernelXRadius = 1;
    private int kernelYRadius = 1;

    public MedianFilter2DModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true, false);
    }

    public int getKernelXRadius()
    {
        return kernelXRadius;
    }

    public void setKernelXRadius(int kernelXRadiusNew)
    {       
        if(kernelXRadiusNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'kernelXRadiusNew' must be grater or equal 0");
        }

        if(this.kernelXRadius != kernelXRadiusNew)
        {
            int kernelWidthOld = this.kernelXRadius;
            this.kernelXRadius = kernelXRadiusNew;

            firePropertyChange(KERNEL_X_RADIUS, kernelWidthOld, kernelXRadiusNew);

            updatePreview();
        }
    }

    public int getKernelYRadius()
    {
        return kernelYRadius;
    }

    public void setKernelYRadius(int kernelYRadiusNew)
    {
        if(kernelYRadiusNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'kernelYRadiusNew' must be grater or equal 0");
        }

        if(this.kernelYRadius != kernelYRadiusNew)
        {
            int kernelYRadiusOld = this.kernelYRadius;
            this.kernelYRadius = kernelYRadiusNew;

            firePropertyChange(KERNEL_Y_RADIUS, kernelYRadiusOld, kernelYRadiusNew);

            updatePreview();
        }
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation()
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Channel2DDataInROITransformation tr = new MedianFilter2D(kernelXRadius, kernelYRadius);
        return tr;
    }
}
