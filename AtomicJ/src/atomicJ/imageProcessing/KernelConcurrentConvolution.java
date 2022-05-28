package atomicJ.imageProcessing;


public class KernelConcurrentConvolution extends ImageConcurrentFilter
{   
    private final double[][] kernel;

    private final int kernelColumnCount;
    private final int kernelRowCount;

    private final int kernelCenterRow;
    private final int kernelCenterColumn;

    public KernelConcurrentConvolution(double[][] kernel)
    {
        this.kernel = kernel;

        this.kernelRowCount = kernel.length;
        this.kernelColumnCount = kernel[0].length;

        this.kernelCenterRow = kernelRowCount/2;
        this.kernelCenterColumn = kernelColumnCount/2;
    }

    @Override
    protected double filter(int i, int j, double[][] matrix, int columnCount, int rowCount)
    {
        double value = 0;

        for(int k = 0; k<kernelRowCount; k++)
        {
            for(int l = 0; l<kernelColumnCount; l++)
            {
                double kernelValue = kernel[kernelRowCount - k -1 ][kernelColumnCount - l - 1];

                int row = i + k - kernelCenterRow;
                int column = j + l - kernelCenterColumn;

                double imageValue = getPixel(row, column, matrix, columnCount, rowCount);
                value = value + imageValue*kernelValue;
            }
        }

        return value;
    }
}
