package atomicJ.imageProcessing;

import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;


public class MedianWeightedFilter2D extends ImageFilter
{    
    private final double[][] kernel;
    private final int weightCount;

    private final int kernelColumnCount;
    private final int kernelRowCount;

    private final int kernelCenterRow;
    private final int kernelCenterColumn;

    public MedianWeightedFilter2D(double[][] kernel)
    {     
        this.kernel = kernel;
        this.weightCount = (int) ArrayUtilities.total(kernel);

        this.kernelRowCount = kernel.length;
        this.kernelColumnCount = kernel[0].length;

        this.kernelCenterRow = kernelRowCount/2;
        this.kernelCenterColumn = kernelColumnCount/2;
    }

    @Override
    protected double filter(int i, int j, double[][] matrix, int columnCount, int rowCount)
    {    
        double[] values = new double[weightCount];

        int index = 0;

        for(int k = 0; k<kernelRowCount; k++)
        {
            for(int l = 0; l<kernelColumnCount; l++)
            {      
                int weight = (int) kernel[kernelRowCount - k -1 ][kernelColumnCount - l - 1];

                int row = i + k - kernelCenterRow;
                int column = j + l - kernelCenterColumn;

                double imageValue = getPixel(row, column, matrix, columnCount, rowCount);

                for(int p = 0; p < weight; p++)
                {
                    values[index++] = imageValue;
                }
            }
        }

        return DescriptiveStatistics.median(values);
    }
}
