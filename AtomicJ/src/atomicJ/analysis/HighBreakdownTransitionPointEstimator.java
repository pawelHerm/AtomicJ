package atomicJ.analysis;

import java.util.Arrays;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.FittedUnivariateFunctionAdapter;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.ResidualsComparator;
import atomicJ.utilities.SelectorFlatArrayAbsoluteResiduals;


public class HighBreakdownTransitionPointEstimator 
{
    public static double[] getTransitionPoint(UnivariateFunction f, double[][] data, double k)
    {
        int n = data.length;

        double[][] dataCopy = Arrays.copyOf(data, n);
        Arrays.sort(dataCopy, new ResidualsComparator(f));

        int c = (int)(0.5*n);    
        double[] medianResidualPoint = dataCopy[c];

        if(k == 1)
        {
            return medianResidualPoint;
        }

        double limit = k*(Math.abs(medianResidualPoint[1] - f.value(medianResidualPoint[0])));       

        int initIndex = k >= 1 ? c : 0;
        int endIndex =  k >= 1 ? n : c;
        int residualIndex = ArrayUtilities.binarySearchAscendingResidual(dataCopy, initIndex, endIndex, limit, f);
        int maximumXIndex = ArrayUtilities.getMaximumXIndex(dataCopy, 0, residualIndex);

        return dataCopy[maximumXIndex];
    }

    public static double[] getTransitionPoint(UnivariateFunction f, double[] ys,  double[] xs, double k) 
    {
        int n = ys.length;

        FittedUnivariateFunction fittedFunction = FittedUnivariateFunctionAdapter.convertOrCastToFittedUnivariateFunction(f, 1);
        double h = MathUtilities.minimalCoverage(n, fittedFunction.getEstimatedParameterCount());

        int c = Math.max((int)(h*n), 1);    

        double[] ysCopied = Arrays.copyOf(ys, n);
        double[] xsCopied = Arrays.copyOf(xs, n);

        SelectorFlatArrayAbsoluteResiduals.sortSmallest(ysCopied, xsCopied, c - 1, fittedFunction);

        double limit = k*Math.abs(fittedFunction.residual(xsCopied[c - 1],ysCopied[c - 1]));       

        //here, we need to use the unsorted arrays xs and ys
        int index = AbstractFittedUnivariateFunction.getIndexOfPointWithGreatestXAmonngThoseWithAbsoluteResidualSmallerOrEqualToLimit(ys, xs, 0, n, limit, fittedFunction);
        double[] transitionPoint = index >= 0 ? new double[] {xs[index], ys[index]} : new double[] {xs[0], ys[0]};

        return transitionPoint;
    }
}
