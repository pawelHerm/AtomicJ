package atomicJ.curveProcessing;

import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;


public class MedianWeightedFilter1D extends CurveFilter
{    
    private final double[] kernel;
    private final int weightCount;

    private final int kernelColumnCount;

    private final int kernelCenterColumn;

    public MedianWeightedFilter1D(double[] kernel)
    {     
        this.kernel = kernel;
        this.weightCount = (int) ArrayUtilities.total(kernel);

        this.kernelColumnCount = kernel.length;
        this.kernelCenterColumn = kernelColumnCount/2;
    }

    @Override
    protected double filter(int j, double[] matrix, int columnCount)
    {    
        double[] values = new double[weightCount];


        for(int k = 0, index = 0; k<kernelColumnCount; k++)
        {      
            int weight = (int) kernel[kernelColumnCount - k - 1];

            int column = j + k - kernelCenterColumn;

            double imageValue = getPixel(column, matrix, columnCount);

            for(int p = 0; p < weight; p++)
            {
                values[index++] = imageValue;
            }
        }

        return DescriptiveStatistics.median(values);
    }
}
