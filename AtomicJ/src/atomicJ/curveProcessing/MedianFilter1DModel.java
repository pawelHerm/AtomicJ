package atomicJ.curveProcessing;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DMinimumSizeFilter;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public class MedianFilter1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    public static final String KERNEL_RADIUS = "KernelRadius";

    private int kernelRadius = 1;

    public MedianFilter1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager, new Channel1DMinimumSizeFilter(3), false, false);
    }

    public int getKernelRadius()
    {
        return kernelRadius;
    }

    public void setKernelRadius(int kernelRadiusNew)
    {       
        if(kernelRadiusNew < 0)
        {
            throw new IllegalArgumentException("Parameter 'kernelRadiusNew' must be grater or equal 0");
        }

        if(this.kernelRadius != kernelRadiusNew)
        {
            int kernelWidthOld = this.kernelRadius;
            this.kernelRadius = kernelRadiusNew;

            firePropertyChange(KERNEL_RADIUS, kernelWidthOld, kernelRadiusNew);

            updatePreview();
        }
    }

    @Override
    public Channel1DDataInROITransformation buildTransformation()
    {
        Channel1DDataInROITransformation tr = new MedianFilter1D(kernelRadius);
        return tr;
    }
}
