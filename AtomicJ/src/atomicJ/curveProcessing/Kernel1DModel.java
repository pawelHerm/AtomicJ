package atomicJ.curveProcessing;

import java.io.File;
import java.io.IOException;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DMinimumSizeFilter;
import atomicJ.gui.imageProcessing.KernelChangeListener;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public abstract class Kernel1DModel<R extends SpectroscopyResource> extends CurveBatchProcessingModel<R>
{
    public Kernel1DModel(ResourceView<R, Channel1D, String> manager, boolean modifyAllTypes, boolean previewEnabledByDefault)
    {
        super(manager, new Channel1DMinimumSizeFilter(3), modifyAllTypes, previewEnabledByDefault);
    }

    public abstract int getKernelXRadius();
    public abstract void setKernelXRadius(int kernelXRadiusNew);
    public abstract double[] getKernel();
    public abstract void setKernelElement(int column, double valueNew);

    public abstract void setKernelElements(double[] kernelElements);
    public abstract void addKernelChangeListener(KernelChangeListener listener);
    public abstract void removeKernelChangeListener(KernelChangeListener listener);
    public abstract boolean isExportEnabled();
    public abstract void importKernel(File file, String[] selectedExtensions) throws IOException;
    public abstract void exportKernel(File file, String[] selectedExtensions) throws IOException;
}
