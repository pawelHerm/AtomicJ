package atomicJ.imageProcessing;

import atomicJ.statistics.DescriptiveStatistics;


public class MedianFilter2D extends ImageFilter
{    
    private final int kernelColumnCount;
    private final int kernelRowCount;

    private final int kernelCenterRow;
    private final int kernelCenterColumn;

    public MedianFilter2D(int radiusX, int radiusY)
    {     
        this.kernelRowCount = 2*radiusY + 1;
        this.kernelColumnCount = 2*radiusX + 1;

        this.kernelCenterRow = kernelRowCount/2;
        this.kernelCenterColumn = kernelColumnCount/2;
    }

    @Override
    protected double filter(int i, int j, double[][] matrix, int columnCount, int rowCount)
    {    
        double[] values = new double[kernelRowCount*kernelColumnCount];

        int index = 0;

        for(int k = 0; k<kernelRowCount; k++)
        {
            for(int l = 0; l<kernelColumnCount; l++)
            {                
                int row = i + k - kernelCenterRow;
                int column = j + l - kernelCenterColumn;

                double imageValue = getPixel(row, column, matrix, columnCount, rowCount);
                values[index++] = imageValue;
            }
        }

        return DescriptiveStatistics.median(values);
    }
}
