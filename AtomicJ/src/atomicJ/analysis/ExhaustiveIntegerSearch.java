package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;


public class ExhaustiveIntegerSearch 
{
    public double getMinimum(UnivariateFunction f, double min, double max)
    {       
        int startIndex = (int)Math.rint(min);
        int endIndex = (int)Math.rint(max);

        if(startIndex>= endIndex)
        {
            throw new IllegalArgumentException("The closest integer to the parameter 'min' is" +
                    " larger or equal to the closes integer to 'max'");
        }

        double minCriterion = Double.POSITIVE_INFINITY;
        double minCriterionIndex = startIndex;

        for(int i = startIndex; i < max; i++)
        {
            double criterion = f.value(i);
            if(minCriterion > criterion)
            {
                minCriterion = criterion;
                minCriterionIndex = i;
            }
        }

        return minCriterionIndex;
    }

}
