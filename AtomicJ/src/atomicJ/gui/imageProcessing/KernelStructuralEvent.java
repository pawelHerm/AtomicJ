package atomicJ.gui.imageProcessing;

import atomicJ.utilities.ArrayUtilities;

public class KernelStructuralEvent 
{
    private final double[][] kernelOld;
    private final double[][] kernelNew;

    public KernelStructuralEvent(double[][] kernelOld, double[][] kernelNew)
    {
        this.kernelOld = ArrayUtilities.deepCopy(kernelOld);
        this.kernelNew = ArrayUtilities.deepCopy(kernelNew);  
    }

    public double[][] getKernelOld()
    {
        return ArrayUtilities.deepCopy(kernelOld);
    }

    public double[][] getKernelNew()
    {
        return ArrayUtilities.deepCopy(kernelNew);
    }
}
