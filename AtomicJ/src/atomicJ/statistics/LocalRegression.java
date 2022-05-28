package atomicJ.statistics;


import java.util.Arrays;

import atomicJ.data.IndexRange;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;

public class LocalRegression
{
    public static double[][] smooth(double[][] data, SpanGeometry spanGeometry, int windowWidthInPoints, int robustnessIters, double accuracy, int degree, LocalRegressionWeightFunction weightFunction)
    {
        int n = data.length;

        if (n < 3 || windowWidthInPoints < 2) {
            return data;
        }

        double[][] smoothed = new double[n][2];

        double[] absResiduals = new double[n];

        double[] robustnessWeights = new double[n];
        Arrays.fill(robustnessWeights, 1);

        double[] weights = new double[windowWidthInPoints];        

        for (int iter = 0; iter <= robustnessIters; iter++)
        {
            int ileft = 0;
            int iright = windowWidthInPoints - 1;

            for (int i = 0; i < n; ++i)
            {
                double[] currentPoint = data[i];

                double x = currentPoint[0];
                double y = currentPoint[1];

                //updates fitting range

                IndexRange indexRangeNew = spanGeometry.getNextRange(data, n, i, ileft, iright, windowWidthInPoints);
                ileft = indexRangeNew.getMinIndex();
                iright = indexRangeNew.getMaxIndex();

                double leftDistance = Math.abs(x - data[ileft][0]);
                double rightDistance = Math.abs(data[iright][0] - x);

                double maxDistance = Math.max(leftDistance,rightDistance);

                boolean maxDistanceNonZero = !MathUtilities.equalWithinTolerance(0, maxDistance, 1e-15);

                if(maxDistanceNonZero && (iright - ileft + 1) > degree)
                {
                    for (int k = ileft, w = 0; k <= iright; ++k, ++w) //loop over whole bandwith
                    {
                        double dist = Math.abs(x - data[k][0]); //absolute value of distance from center x to the current point of the bandwidth                

                        weights[w] = weightFunction.value(dist/maxDistance) * robustnessWeights[k]; // weight
                    }
                }
                else
                {
                    weights = Arrays.copyOf(robustnessWeights, robustnessWeights.length);                  
                }

                //there must be one more points then the degree, because there is always a point with weight 0
                int fitDegree = maxDistanceNonZero && (iright - ileft + 1) > degree + 1 ? degree : 0;

                //                containsAtLeastNNonZero(weights, fitDegree + 1, 1e-9)
                double fittedValue = y;
                try
                {
                    fittedValue = L2Regression.findFitedFunction(data, ileft, iright + 1, weights, fitDegree).value(x); 

                    if(Double.isNaN(fittedValue))
                    {
                        fittedValue = y;
                    }
                }
                catch(Exception e) //happens when the rank of design matrix is too small, but I don't want to calculate it beforehand for performance reasons
                {
                }

                smoothed[i][0] = x;
                smoothed[i][1] = fittedValue;

                absResiduals[i] = Math.abs(y - fittedValue);
            }

            if (iter == robustnessIters) {
                break;
            }

            double medianAbsResidual = DescriptiveStatistics.median(absResiduals);

            if (medianAbsResidual < accuracy) {
                break;
            }

            for (int i = 0; i < n; i++)
            {
                double arg = absResiduals[i] / (6 * medianAbsResidual);
                robustnessWeights[i] = LocalRegressionWeightFunction.BISQUARE.value(arg);
            }
        }

        return smoothed;
    }


    public static double[][] smooth(double[][] data, SpanGeometry spanGeometry, int bandwidthInPoints, int degree, int derivative, LocalRegressionWeightFunction weightFunction)
    {        
        int n = data.length;
        if(n == 0)
        {
            return data;
        }

        if(degree < derivative)
        {
            throw new IllegalArgumentException("The polynomial degree should be equal or greater the derivative");
        }

        if (Math.min(n, bandwidthInPoints) <= derivative) 
        {
            throw new IllegalArgumentException("Too few points available in bandwidth to calculate derivative");
        }

        double[][] smoothed = new double[n][2];

        double[] weights = new double[bandwidthInPoints];        

        int ileft = 0;
        int iright = bandwidthInPoints - 1;

        for (int i = 0; i < n; ++i)
        {
            double[] currentPoint = data[i];

            double x = currentPoint[0];
            double y = currentPoint[1];

            //updates fitting range

            IndexRange indexRangeNew = spanGeometry.getNextRange(data, n, i, ileft, iright, bandwidthInPoints);
            ileft = indexRangeNew.getMinIndex();
            iright = indexRangeNew.getMaxIndex();

            double leftDistance = Math.abs(x - data[ileft][0]);
            double rightDistance = Math.abs(data[iright][0] - x);

            double maxDistance = Math.max(leftDistance,rightDistance);

            boolean maxDistanceNonZero = !MathUtilities.equalWithinTolerance(0, maxDistance, 1e-15);

            if(maxDistanceNonZero)
            {
                for (int k = ileft, w = 0; k <= iright; ++k, ++w) //loop over whole bandwith
                {
                    double dist = Math.abs(x - data[k][0]); //absolute value of distance from center x to the current point of the bandwidth                
                    weights[w] = weightFunction.value(dist/maxDistance); // weight
                }
            }

            int fitDegree = maxDistanceNonZero ? degree : 0;

            double derValue = y;
            try
            {
                derValue = L2Regression.findFitedFunction(data, ileft, iright + 1, weights, fitDegree).getDerivative(derivative).value(x);
            }
            catch(Exception e) //happens when the rank of design matrix is too small, but I don't want to calculate it beforehand for performance reasons
            {}

            smoothed[i][0] = x;
            smoothed[i][1] = derValue;
        }

        return smoothed;
    }


    public static double[] getDerivative(double[][] data, int index, SpanGeometry spanGeometry, int bandwidthInPoints, int degree, int derivative, LocalRegressionWeightFunction weightFunction)
    {        
        IndexRange indexRange = spanGeometry.getRange(data, data.length, index, bandwidthInPoints);

        int ileft = indexRange.getMinIndex();
        int iright = indexRange.getMaxIndex();

        return getDerivative(data, index, ileft, iright, degree, derivative, weightFunction);
    }

    public static double[] getDerivative(double[][] data, int index, int ileft, int iright, int degree, int derivative, LocalRegressionWeightFunction weightFunction)
    {        
        int n = data.length;
        if(n == 0)
        {
            return null;
        }

        int bandwidthInPoints = iright - ileft + 1;

        if(degree < derivative)
        {
            throw new IllegalArgumentException("The polynomial degree should be equal or greater the derivative");
        }

        if (Math.min(n, bandwidthInPoints) <= derivative) 
        {
            throw new IllegalArgumentException("Too few points available in bandwidth to calculate derivative");
        }


        double[] p = data[index];

        double x = p[0];
        double y = p[1];

        double maxDistance = Math.max(Math.abs(x - data[ileft][0]),Math.abs(data[iright][0] - x));

        boolean maxDistanceNonZero = !MathUtilities.equalWithinTolerance(0, maxDistance, 1e-15);

        double[] weights = new double[bandwidthInPoints];        

        if(maxDistanceNonZero)
        {
            for (int k = ileft, w = 0; k <= iright; ++k, ++w) //loop over whole bandwidth
            {
                double dist = Math.abs(x - data[k][0]); //absolute value of distance from center x to the current point of the bandwidth                
                weights[w] = weightFunction.value(dist/maxDistance); // weight
            }

            int fitDegree = maxDistanceNonZero ? degree : 0;

            double derValue = derivative > 0 ? Double.NaN : y; // defaults, used when fitted funcion cannot be found, i.e. there is an exception thrown below
            try
            {
                derValue = L2Regression.findFitedFunction(data, ileft, iright + 1, weights, fitDegree).getDerivative(derivative).value(x);
            }
            catch(Exception e) //happens when the rank of design matrix is too small, but I don't want to calculate it beforehand for performance reasons
            {}

            return new double[] {x, derValue};
        }

        if(derivative == 0)
        {
            double[][] pointsInBandwith = Arrays.copyOfRange(data, ileft, iright);
            double[] ys = ArrayUtilities.getColumn(pointsInBandwith, 1);
            return new double[] {x, DescriptiveStatistics.arithmeticMean(ys)};
        }

        return null;
    }

    public static double[] smooth(double[] data, double h, SpanGeometry spanGeometry, int bandwidthInPoints, int robustnessIters, double accuracy, int degree, LocalRegressionWeightFunction weightFunction)
    {
        int n = data.length;

        if (n < 3 || bandwidthInPoints < degree + 2) {
            return data;
        }

        double[] smoothed = new double[n];

        double[] absResiduals = new double[n];

        double[] robustnessWeights = new double[n];
        Arrays.fill(robustnessWeights, 1);

        double[] weights = new double[bandwidthInPoints];        

        for (int iter = 0; iter <= robustnessIters; iter++)
        {
            int ileft = 0;
            int iright = bandwidthInPoints - 1;

            for (int i = 0; i < n; ++i)
            {
                double y = data[i];

                //updates fitting range

                IndexRange indexRangeNew = spanGeometry.getNextRange(data, n, i, ileft, iright, bandwidthInPoints);
                ileft = indexRangeNew.getMinIndex();
                iright = indexRangeNew.getMaxIndex();

                double leftDistance = Math.abs(i - ileft); //it is really h times greater, but we will skip h in denom and dist variables, ad there would cancel out in weightFunction.value()
                double rightDistance = Math.abs(i - iright);

                double denom = 1./Math.max(leftDistance,rightDistance);

                for (int k = ileft, w = 0; k <= iright; ++k, ++w) //loop over whole bandwith
                {
                    double dist = Math.abs(i - k); //absolute value of distance from center x to the current point of the bandwidth                

                    double weight = weightFunction.value(dist * denom) * robustnessWeights[k]; // waga                    
                    weights[w] = weight;
                }

                double fittedValue = ArrayUtilities.containsAtLeastNNonZero(weights, degree + 1, 1e-9)  ? L2Regression.findFitedFunction(data, ileft, iright + 1, weights, degree).value(i) : y;

                smoothed[i] = fittedValue;

                absResiduals[i] = Math.abs(y - fittedValue);
            }

            if (iter == robustnessIters) {
                break;
            }

            double medianAbsResidual = DescriptiveStatistics.median(absResiduals);

            if (medianAbsResidual < accuracy) {
                break;
            }

            for (int i = 0; i < n; i++)
            {
                double arg = absResiduals[i] / (6 * medianAbsResidual);
                robustnessWeights[i] = LocalRegressionWeightFunction.BISQUARE.value(arg);
            }
        }

        return smoothed;
    }
}
