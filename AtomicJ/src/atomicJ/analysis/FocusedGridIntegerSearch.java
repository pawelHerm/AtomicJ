package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;


public class FocusedGridIntegerSearch 
{
    public double getMinimum(UnivariateFunction f, double min, double max)
    {  
        double length = max - min;

        int minIndex = (int)Math.ceil(min);
        int maxIndex = (int)Math.floor(max);

        int factor = (int)Math.floor(length/(Math.log(length)/Math.log(2)));
        int space = Math.max(4, factor);

        double[] criteriumValues = new double[maxIndex - minIndex + 1];

        return getMinimum(f, minIndex, minIndex, maxIndex, space, criteriumValues);
    }

    private double getMinimum(UnivariateFunction f, int absoluteMinIndex, int min, int max, int space, double[] criteriumValues)
    {       
        double length = max - min;


        double minCriterion = Double.POSITIVE_INFINITY;
        double minCriterionIndex = min;

        for(int i = min ; i < max; i += space)
        {
            double precombutedCriterion = criteriumValues[i - absoluteMinIndex];

            double criterion = precombutedCriterion;
            if(precombutedCriterion == 0)
            {
                criterion = f.value(i);
                criteriumValues[i - absoluteMinIndex] = criterion;
            }

            if(minCriterion > criterion)
            {
                minCriterion = criterion;
                minCriterionIndex = i;
            }
        }

        if(space <= 1)
        {
            return minCriterionIndex;
        }

        int minIndexNew = Math.max(min, (int)Math.rint(minCriterionIndex - length/4));
        int maxIndexNew = Math.min(max, (int)Math.rint(minCriterionIndex + length/4));

        int spaceNew = Math.max(1, (int)Math.rint(((double)space/2)));

        return getMinimum(f, absoluteMinIndex, minIndexNew, maxIndexNew, spaceNew, criteriumValues);
    }
}
