package atomicJ.imageProcessing;

public class KernelLaplacianOfGaussian extends Kernel2D
{
    public KernelLaplacianOfGaussian(double sigma)
    {
        super(buildMatrix(sigma));
    }

    public KernelLaplacianOfGaussian(double sigmaX, double sigmaY)
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

    /*
     * 

     */

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

        double sigmaX2 = sigmaX*sigmaX;
        double sigmaY2 = sigmaY*sigmaY;

        double sigmaX4 = sigmaX2*sigmaX2;
        double sigmaY4 = sigmaY2*sigmaY2;

        double scaleX = 2*sigmaX2;
        double scaleY = 2*sigmaY2;

        double factorX = 1./(Math.sqrt(2*Math.PI)*sigmaX);
        double factorY = 1./(Math.sqrt(2*Math.PI)*sigmaY);

        for(int i = 0; i<sizeY;i++)
        {
            for(int j = 0; j<sizeX; j++)
            {
                double x = radiusY - i;
                double y = radiusX - j;

                double gaussian = factorX*Math.exp(-x*x/scaleX) * factorY*Math.exp(-y*y/scaleY);
                double lofg = gaussian*(x*x*sigmaY4 - sigmaX2*sigmaY4 + sigmaX4*(y*y - sigmaY2))/(sigmaX4*sigmaY4);

                matrix[i][j] = lofg;
            }
        }

        return matrix;
    }
}
