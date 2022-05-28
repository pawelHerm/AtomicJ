package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class KernelMaskSubtraction implements Channel2DDataInROITransformation
{   
    private final Kernel2D kernel;
    private final Channel2DDataInROITransformation maskTransformation;
    private final double fraction;

    public KernelMaskSubtraction(Kernel2D kernel, double fraction)
    {
        this.kernel = kernel;  
        this.maskTransformation = new KernelConvolution(kernel);
        this.fraction = fraction;
    }

    public Kernel2D getKernel()
    {
        return kernel;
    }

    @Override
    public Channel2DData transform(Channel2DData image) 
    {
        Channel2DData mask = maskTransformation.transform(image);
        AddImagePixelwise add = new AddImagePixelwise(mask.getDefaultGridding(), -fraction, 1 + fraction);

        return add.transform(image.getDefaultGridding());
    }

    @Override
    public Channel2DData transform(Channel2DData image, ROI roi, ROIRelativePosition position) 
    {        
        Channel2DData mask = maskTransformation.transform(image);
        AddImagePixelwise add = new AddImagePixelwise(mask.getDefaultGridding(), -fraction, 1 + fraction);

        return add.transform(image.getDefaultGridding(), roi, position);
    }
}
