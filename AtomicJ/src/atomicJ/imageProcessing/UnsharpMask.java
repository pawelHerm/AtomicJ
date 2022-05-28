package atomicJ.imageProcessing;

public class UnsharpMask extends KernelMaskSubtraction
{   
    public UnsharpMask(double sigmaX, double sigmaY, double fraction)
    {
        super(new KernelSampledGaussian2D(sigmaX, sigmaY), fraction);
    }
}
