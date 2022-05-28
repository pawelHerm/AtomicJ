package atomicJ.imageProcessing;

public class KernelSharpen extends Kernel2D
{
    public KernelSharpen(int xRadius, int yRadius)
    {
        super(buildMatrix(xRadius, yRadius));
    }

    private static double[][] buildMatrix(int xRadius, int yRadius)
    {
        if(xRadius < 1)
        {
            throw new IllegalArgumentException("'xRadius' should be greater or equal to one");
        }

        if(yRadius < 1)
        {
            throw new IllegalArgumentException("'yRadius' should be greater or equal to one");
        }

        int rowCount = 2*xRadius + 1;
        int columnCount = 2*yRadius + 1;

        double periferalElements = -1./(rowCount*columnCount);

        double[][] matrix = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount;i++)
        {
            for(int j = 0; j<columnCount; j++)
            {
                matrix[i][j] = periferalElements;
            }
        }

        //sets the center
        matrix[xRadius][yRadius] = 1;

        return matrix;
    }
}
