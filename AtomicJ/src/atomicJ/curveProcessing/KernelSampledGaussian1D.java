package atomicJ.curveProcessing;

public class KernelSampledGaussian1D extends Kernel1D
{
    public KernelSampledGaussian1D(double sigma)
    {
        super(buildMatrix(sigma));
    }

    private static double[] buildMatrix(double sigma)
    {
        if(sigma <= 0)
        {
            throw new IllegalArgumentException("'sigma' should be greater than zero");
        }       

        int radius = (int)Math.ceil(3*sigma);

        int size = 2*radius + 1;  

        double[] matrix = new double[size];
        double scale = 2*sigma*sigma;

        double factor = 1./(Math.sqrt(2*Math.PI)*sigma);

        for(int i = 0; i<size; i++)
        {
            double x = radius - i;

            matrix[i] = factor*Math.exp(-x*x/scale);
        }

        return matrix;
    }
}
