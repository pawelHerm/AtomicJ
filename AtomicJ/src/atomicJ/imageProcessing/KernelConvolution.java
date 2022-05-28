package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class KernelConvolution implements Channel2DDataInROITransformation
{   
    private final Kernel2D kernel;
    private final Channel2DDataInROITransformation transformation;

    public KernelConvolution(Kernel2D kernel)
    {
        this.kernel = kernel;

        boolean separable = kernel.isSeparable();
        this.transformation = separable ? new KernelConvolutionSequence(kernel.getSeparableFilters()) : new KernelSimpleConvolution(kernel);
    }

    public Kernel2D getKernel()
    {
        return kernel;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        return transformation.transform(channelData);
    }

    @Override
    public Channel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {        
        Channel2DData matrix = transformation.transform(channelData, roi, position);        

        return matrix;
    }
}
