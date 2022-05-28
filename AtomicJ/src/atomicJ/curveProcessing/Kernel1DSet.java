package atomicJ.curveProcessing;

import java.util.ArrayList;
import java.util.List;

public class Kernel1DSet<E extends Kernel1D> implements Convolable1D
{
    private final List<E> leftMarginKernels;
    private final List<E> rightMarginKernels;
    private final E mainKernel;

    public Kernel1DSet(E mainKernel, List<E> leftMarginKernels, List<E> rightMarginKernels)
    {
        this.leftMarginKernels = leftMarginKernels;
        this.rightMarginKernels = rightMarginKernels;

        this.mainKernel = mainKernel;
    }

    public E getMainKernel()
    {
        return mainKernel;
    }

    public Kernel1DSet<?> multiply(double factor)
    {
        List<Kernel1D> leftMarginKernelsNew = new ArrayList<>();
        List<Kernel1D> rightMarginKernelsNew = new ArrayList<>();

        for(int i = 0; i<leftMarginKernels.size(); i++)
        {
            leftMarginKernelsNew.add(leftMarginKernels.get(i).multiply(factor));
        }

        for(int i = 0; i<rightMarginKernels.size(); i++)
        {
            rightMarginKernelsNew.add(rightMarginKernels.get(i).multiply(factor));
        }

        Kernel1D mainKernelNew = mainKernel.multiply(factor);

        Kernel1DSet<?> kernelSetNew = new Kernel1DSet<>(mainKernelNew, leftMarginKernelsNew, rightMarginKernelsNew);

        return kernelSetNew;
    }

    public double convolve(int j, double[] image, int imageColumnCount)
    {
        return getKernel(j, imageColumnCount).convolve(j, image, imageColumnCount);
    }  

    private Kernel1D getKernel(int imageIndex, int imageColumnCount)
    {
        int leftRadius = mainKernel.getLeftRadius();
        int rightRadius = mainKernel.getRightRadius();

        if(imageIndex < leftRadius)
        {
            return leftMarginKernels.get(imageIndex);
        }
        if(imageColumnCount - 1 - imageIndex < rightRadius)
        {
            return rightMarginKernels.get(imageColumnCount - 1 - imageIndex);
        }

        return mainKernel;
    }

    @Override
    public double[] convolve(double[] image, int imageColumnCount)
    {      
        int columnCount = mainKernel.getColumnCount();
        int centerColumn = mainKernel.getCenterIndex();

        double[] transformed = new double[imageColumnCount];

        int maxColumnInside = imageColumnCount - columnCount - centerColumn;


        for(int i = 0 ; i<centerColumn; i++)
        {
            transformed[i] = convolve(i, image, imageColumnCount);
        }
        for(int i = maxColumnInside; i<imageColumnCount; i++)
        {
            transformed[i] = convolve(i, image, imageColumnCount);
        }

        mainKernel.convolveApartFromMargins(image, transformed, imageColumnCount);

        return transformed;
    }
}
