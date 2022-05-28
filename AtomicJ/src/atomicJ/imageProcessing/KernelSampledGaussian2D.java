package atomicJ.imageProcessing;

public class KernelSampledGaussian2D extends Kernel2D
{
    public KernelSampledGaussian2D(double sigma)
    {
        super(buildMatrix(sigma));
    }

    public KernelSampledGaussian2D(double sigmaX, double sigmaY)
    {
        super(buildMatrix(sigmaX, sigmaY));
    }


    private static double[][] buildMatrix(double sigma)
    {
        if(sigma <= 0)
        {
            throw new IllegalArgumentException("'sigma' should be greater than zero");
        }       

        return buildMatrix(sigma, sigma);
    }

    private static double[][] buildMatrix(double sigmaX, double sigmaY)
    {
        if(sigmaX <= 0)
        {
            throw new IllegalArgumentException("'sigmaX' should be greater than zero");
        }       

        if(sigmaY <= 0)
        {
            throw new IllegalArgumentException("'sigmaY' should be greater than zero");
        }  

        int radiusX = (int)Math.ceil(3*sigmaX);
        int radiusY = (int)Math.ceil(3*sigmaY);

        int sizeX = 2*radiusX + 1;  
        int sizeY = 2*radiusX + 1;  

        double[][] matrix = new double[sizeY][sizeX];

        double scaleX = 2*sigmaX*sigmaX;
        double scaleY = 2*sigmaY*sigmaY;

        double factorX = 1./(Math.sqrt(2*Math.PI)*sigmaX);
        double factorY = 1./(Math.sqrt(2*Math.PI)*sigmaY);

        for(int i = 0; i<sizeY;i++)
        {
            for(int j = 0; j<sizeX; j++)
            {
                double x = radiusY - i;
                double y = radiusX - j;

                matrix[i][j] = factorX*Math.exp(-x*x/scaleX) * factorY*Math.exp(-y*y/scaleY);
            }
        }

        return matrix;
    }
}
