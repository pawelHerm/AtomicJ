package atomicJ.curveProcessing;

import atomicJ.statistics.DescriptiveStatistics;


public class MedianFilter1D extends CurveFilter
{    
    private final int kernelColumnCount;

    private final int kernelCenterColumn;

    public MedianFilter1D(int radius)
    {     
        this.kernelColumnCount = 2*radius + 1;
        this.kernelCenterColumn = kernelColumnCount/2;
    }

    @Override
    protected double filter(int j, double[] data, int columnCount)
    {    
        double[] values = new double[kernelColumnCount];

        for(int k = 0, index = 0; k<kernelColumnCount; k++)
        {                
            int column = j + k - kernelCenterColumn;

            double imageValue = getPixel(column, data, columnCount);
            values[index++] = imageValue;
        }

        return DescriptiveStatistics.median(values);
    }
}
