package atomicJ.imageProcessing;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.UnivariateFunctionCombination;
import atomicJ.functions.ZeroFunction;
import atomicJ.utilities.ArrayUtilities;


public class UnivariateFunctionInterpolationMethod 
{
    public UnivariateFunction[] estimateMissingValues(UnivariateFunction[] rawValues)
    {        
        if(!ArrayUtilities.containsNull(rawValues))
        {
            return rawValues;
        }

        int n = rawValues.length;

        UnivariateFunction[] clearedValues = new UnivariateFunction[n];

        int previousValidPosition = - 1;
        int nextValidPosition = 0;

        UnivariateFunction previousValidValue = null;

        for(int i = 0; i<n; i++)
        {
            UnivariateFunction value = rawValues[i];
            if(value == null)
            {
                nextValidPosition = findNextValidIndex(rawValues, Math.max(nextValidPosition, i + 1), n);
                UnivariateFunction nextValidValue = nextValidPosition > - 1 ? rawValues[nextValidPosition] : null;

                value = interpolate(previousValidValue, previousValidPosition, nextValidValue, nextValidPosition, i);              
            }
            else{
                previousValidPosition = i;
                previousValidValue = value;               
            }

            clearedValues[i] = value;

        }

        return clearedValues;
    }

    private int findNextValidIndex(UnivariateFunction[] array, int from, int to)
    {
        int index = - 1;
        for(int j = from; j<to; j++)
        {
            UnivariateFunction value = array[j];
            if(value != null)
            {
                index = j;
                break;
            }
        }        
        return index;
    }

    private UnivariateFunction interpolate(UnivariateFunction leftValue, double leftPosition, UnivariateFunction rightValue, double rightPosition, double x)
    {        
        UnivariateFunction interpolated = ZeroFunction.getInstance();
        if(leftValue != null && rightValue != null)
        {            
            double gap = rightPosition - leftPosition;
            double factor = (x - leftPosition)/gap;
            interpolated = new UnivariateFunctionCombination(leftValue, (1 - factor), rightValue, factor);
        }
        else if(leftValue != null)
        {
            interpolated = leftValue;
        }
        else if(rightValue != null)
        {
            interpolated = rightValue;
        }

        return interpolated;
    }
}
