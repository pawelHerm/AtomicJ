package atomicJ.imageProcessing;


public class KernelSeparation 
{
    private final Kernel2D first;
    private final Kernel2D second;

    public KernelSeparation(Kernel2D verticalKernel, Kernel2D horizontalKernel)
    {
        this.first = verticalKernel;
        this.second = horizontalKernel;
    }

    public Kernel2D getInitialKernel()
    {
        return first;
    }

    public Kernel2D getFinalKernel()
    {
        return second;
    }
}
