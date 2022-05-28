package atomicJ.imageProcessing;

public class KernelIdentity extends Kernel2D
{
    public KernelIdentity(int xRadius, int yRadius)
    {
        super(buildMatrix(xRadius, yRadius));
    }

    private static double[][] buildMatrix(int xRadius, int yRadius)
    {
        if(xRadius <0)
        {
            throw new IllegalArgumentException("'xRadius' should be greater or equal zero");
        }

        if(yRadius <0)
        {
            throw new IllegalArgumentException("'yRadius' should be greater or equal zero");
        }

        int rowCount = 2*xRadius + 1;
        int columnCount = 2*yRadius + 1;

        double[][] matrix = new double[rowCount][columnCount];

        matrix[xRadius][yRadius] = 1;

        return matrix;
    }
}
