package atomicJ.gui.imageProcessing;

import java.io.File;
import java.io.IOException;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;

public abstract class Kernel2DModel extends ImageBatchROIProcessingModel
{
    public Kernel2DModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter, boolean modifyAllTypes)
    {
        super(manager, channelFilter, modifyAllTypes, false);
    }

    public abstract int getKernelXRadius();

    public abstract void setKernelXRadius(int kernelXRadiusNew);
    public abstract int getKernelYRadius();
    public abstract void setKernelYRadius(int kernelYRadiusNew);
    public abstract double[][] getKernel();
    public abstract void setKernelElement(int row, int column, double valueNew);

    public abstract void setKernelElements(double[][] kernelElements);
    public abstract void addKernelChangeListener(KernelChangeListener listener);
    public abstract void removeKernelChangeListener(KernelChangeListener listener);
    public abstract boolean isExportEnabled();
    public abstract void importKernel(File file, String[] selectedExtensions) throws IOException;
    public abstract void exportKernel(File file, String[] selectedExtensions) throws IOException;
}
