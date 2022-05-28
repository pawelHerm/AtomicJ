package atomicJ.imageProcessing;

import atomicJ.utilities.ArrayUtilities;

public class ValidValuesComplexInterpolationMethod implements MissingValuesEstimationMethod
{
    @Override
    public double[] estimateMissingValues(double[] rawValues)
    {        
        if(!ArrayUtilities.containsNaN(rawValues))
        {
            return rawValues;
        }

        int n = rawValues.length;
        double[] clearedValues = new double[n];

        int previousValidPosition = - 1;
        int nextValidPosition = 0;

        for(int i = 0; i<n; i++)
        {
            double value = rawValues[i];
            if(Double.isNaN(value))
            {
                nextValidPosition = findNextValidIndex(rawValues, Math.max(nextValidPosition, i + 1), n);

                value = interpolate(getLeftMean(rawValues, previousValidPosition, 10), previousValidPosition, getRightMean(rawValues, nextValidPosition, 10), nextValidPosition, i);              
            }

            previousValidPosition = i;

            clearedValues[i] = value;

        }

        return clearedValues;
    }

    private int findNextValidIndex(double[] array, int from, int to)
    {
        int index = - 1;
        for(int j = from; j<to; j++)
        {
            double value = array[j];
            if(!Double.isNaN(value))
            {
                index = j;
                break;
            }
        }        
        return index;
    }

    private double getLeftMean(double[] array, int index, int radius)
    {
        int n = array.length;

        double sum = 0;
        int count = 0;

        for(int i = index - radius; i<= index; i++)
        {
            double val = getValue(i, array, n);
            if(!Double.isNaN(val))
            {
                sum += val;
                count++;
            }
        }

        double  mean = count > 0 ? sum/count : Double.NaN;       
        return mean;
    }

    private double getRightMean(double[] array, int index, int radius)
    {
        if(index < 0)
        {
            return Double.NaN;
        }

        int n = array.length;

        double sum = 0;
        int count = 0;

        for(int i = index; i<= index + radius; i++)
        {
            double val = getValue(i, array, n);
            if(!Double.isNaN(val))
            {
                sum += val;
                count++;
            }
        }

        double  mean = count > 0 ? sum/count : Double.NaN;       
        return mean;
    }

    private double getValue(int index, double[] array,  int length)
    { 
        if (index<=0) index = 0; 
        if (index>=length) index = length-1; 
        return array[index]; 
    }

    private double interpolate(double leftValue, double leftPosition, double rightValue, double rightPosition, double x)
    {        
        double interpolated = 0;
        if(!Double.isNaN(leftValue) && !Double.isNaN(rightValue))
        {
            double gap = rightPosition - leftPosition;
            interpolated = leftValue + (rightValue - leftValue)*(x - leftPosition)/gap;
        }
        else if(!Double.isNaN(leftValue))
        {
            interpolated = leftValue;
        }
        else if(!Double.isNaN(rightValue))
        {
            interpolated = rightValue;
        }

        return interpolated;
    }
}
