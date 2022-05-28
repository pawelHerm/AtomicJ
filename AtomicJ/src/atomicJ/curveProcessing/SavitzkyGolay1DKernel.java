package atomicJ.curveProcessing;

import java.util.ArrayList;
import java.util.List;

import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SavitzkyGolay;
import atomicJ.utilities.ArrayUtilities;

public class SavitzkyGolay1DKernel extends Kernel1D
{
    private final int derivative;

    private SavitzkyGolay1DKernel(int min, int length, int deg, int derivative, double[] weights)
    {
        super(ArrayUtilities.reverse(SavitzkyGolay.getCoefficients2(min, length, deg, weights, derivative)), -min);

        this.derivative = derivative;        
    }

    public int getDerivative()
    {
        return derivative;
    }

    public static Kernel1DSet<SavitzkyGolay1DKernel> buildKernelSet(int min, int length, int deg, int derivative)
    {
        return buildKernelSet(min, length, deg, LocalRegressionWeightFunction.UNIFORM, derivative);
    }

    public static Kernel1DSet<SavitzkyGolay1DKernel> buildKernelSet(int min, int length, int deg, LocalRegressionWeightFunction weightFunction, int derivative)
    {
        List<SavitzkyGolay1DKernel> leftMarginKernels = new ArrayList<>();
        List<SavitzkyGolay1DKernel> rightMarginKernels = new ArrayList<>();

        for(int i = 0; i < -min; i++)
        {
            leftMarginKernels.add(new SavitzkyGolay1DKernel(-i, length, deg, derivative, weightFunction.getWeights2(-i, length)));
        }

        for(int i = 0; i < length - 1 + min; i++)
        {
            rightMarginKernels.add(new SavitzkyGolay1DKernel(i + 1 - length, length, deg, derivative, weightFunction.getWeights2(i + 1 - length, length)));
        }

        SavitzkyGolay1DKernel mainKernel = new SavitzkyGolay1DKernel(min, length, deg, derivative, weightFunction.getWeights2(min, length));

        Kernel1DSet<SavitzkyGolay1DKernel> kernelSet = new Kernel1DSet<>(mainKernel, leftMarginKernels, rightMarginKernels);

        return kernelSet;
    }
}
