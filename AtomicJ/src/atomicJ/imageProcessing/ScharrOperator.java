package atomicJ.imageProcessing;


public class ScharrOperator extends ImageFilter
{   
    private final double[][] gx = {{3, 0, -3}, {10, 0,-10}, {3, 0,-3}};
    private final double[][] gy = {{3, 10, 3}, {0, 0, 0}, {-3, -10, -3}};

    private final int scalingFactor = 32;

    @Override
    protected double filter(int i, int j, double[][] matrix, int columnCount, int rowCount)
    {
        double valueGx = 0;
        double valueGy = 0;

        for(int k = 0; k<3; k++)
        {
            for(int l = 0; l<3; l++)
            {
                double gxKernelElement = gx[3 - k -1 ][3 - l - 1];
                double gyKernalElement = gy[3 - k -1 ][3 - l - 1];

                int row = i + k - 1;
                int column = j + l - 1;

                double imageValue = getPixel(row, column, matrix, columnCount, rowCount);
                valueGx += imageValue*gxKernelElement;
                valueGy += imageValue*gyKernalElement;
            }
        }

        double scaledGx = valueGx/scalingFactor;
        double scaledGy = valueGy/scalingFactor;

        double value = Math.sqrt(scaledGx*scaledGx + scaledGy*scaledGy);

        return value;
    }
}
