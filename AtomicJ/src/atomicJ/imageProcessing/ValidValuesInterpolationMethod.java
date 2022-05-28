package atomicJ.imageProcessing;

import atomicJ.utilities.ArrayUtilities;

public class ValidValuesInterpolationMethod implements MissingValuesEstimationMethod
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

        double previousValidValue = Double.NaN;

        for(int i = 0; i<n; i++)
        {
            double value = rawValues[i];
            if(Double.isNaN(value))
            {
                nextValidPosition = findNextValidIndex(rawValues, Math.max(nextValidPosition, i + 1), n);
                double nextValidValue = nextValidPosition > - 1 ? rawValues[nextValidPosition] : Double.NaN;

                value = interpolate(previousValidValue, previousValidPosition, nextValidValue, nextValidPosition, i);              
            }

            previousValidPosition = i;
            previousValidValue = value;

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
