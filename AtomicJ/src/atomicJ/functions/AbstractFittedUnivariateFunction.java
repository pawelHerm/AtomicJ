package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.Selector;

public abstract class AbstractFittedUnivariateFunction implements FittedUnivariateFunction
{ 
    @Override
    public double residual(double[] p)
    {       
        double x = p[0];
        double y = p[1];
        double r = y - value(x);

        return r;
    }

    @Override
    public double residual(double x, double y)
    {       
        double r = y - value(x);
        return r;
    }

    private void computeSquaredResiduals(double[][] points,double[] arrayToFill)
    {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p);
            arrayToFill[i] = r*r;
        }
    }

    private void computeAbsoluteResiduals(double[][] points,double[] arrayToFill)
    {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p);
            arrayToFill[i] = Math.abs(r);
        }
    }

    private void computeSquaredResiduals(double[] ys, double[] xs, double[] arrayToFill)
    {
        computeSquaredResiduals(ys, xs, ys.length, arrayToFill);
    }

    private void computeSquaredResiduals(double[] ys, double[] xs, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n; i>=0; i--)
        {
            double r = residual(xs[i],ys[i]);
            arrayToFill[i] = r*r;
        }
    }

    private void computeAbsoluteResiduals(double[] ys, double[] xs, double[] arrayToFill)
    {
        int n = ys.length - 1;
        for(int i = n; i>=0; i--)
        {
            double r = residual(xs[i],ys[i]);
            arrayToFill[i] = Math.abs(r);
        }
    }

    private void computeSquaredResiduals(double[] yValues,double[] arrayToFill)
    {
        int n = yValues.length - 1;
        for(int i = n;i>=0;i--)
        {
            double r = residual(i, yValues[i]);
            arrayToFill[i] = r*r;
        }
    }

    private void computeAbsoluteResiduals(double[] yValues,double[] arrayToFill)
    {
        int n = yValues.length - 1;
        for(int i = n;i>=0;i--)
        {
            double r = residual(i, yValues[i]);
            arrayToFill[i] = Math.abs(r);
        }
    }

    private void computeSquaredResiduals(double[][] points, int from, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n;i>=from;i--)
        {
            double[] p = points[i];

            double r = residual(p);
            arrayToFill[i - from] = r*r;
        }
    }

    @Override
    public double[] getSquaredResiduals(double[][] points, int from, int to)
    {
        int n = to - from;
        double[] residuals = new double[n];
        computeSquaredResiduals(points, from, to, residuals);

        return residuals;
    }

    private void computeAbsoluteResiduals(double[][] points, int from, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n;i>=from;i--)
        {
            double[] p = points[i];

            double r = residual(p);
            arrayToFill[i - from] = Math.abs(r);
        }
    }

    @Override
    public double[] getAbsoluteResiduals(double[][] points, int from, int to)
    {
        int n = to - from;
        double[] residuals = new double[n];
        computeAbsoluteResiduals(points, from, to, residuals);

        return residuals;
    }

    public double[] getAbsoluteResiduals(double[][] points)
    {
        double[] residuals = new double[points.length];
        computeAbsoluteResiduals(points,residuals);

        return residuals;
    }

    private void computeSquaredResiduals(double[] ys, double[] xs, int from, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n; i>=from; i--)
        {
            double r = residual(xs[i],ys[i]);
            arrayToFill[i - from] = r*r;
        }
    }

    private void computeAbsoluteResiduals(double[] ys, double[] xs, int from, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n; i>=from; i--)
        {
            double r = residual(xs[i],ys[i]);
            arrayToFill[i - from] = Math.abs(r);
        }
    }

    private void computeSquaredResiduals(double[] yValues, int from, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n;i>=from;i--)
        {
            double r = residual(i, yValues[i]);
            arrayToFill[i - from] = r*r;
        }
    }

    @Override
    public double[] getAbsoluteResiduals(double[] ys, double[] xs, int from, int to)
    {
        int n = to - from;
        double[] residuals = new double[n];
        computeAbsoluteResiduals(ys, xs, from, to, residuals);

        return residuals;
    }

    private void computeAbsoluteResiduals(double[] yValues,int from, int to, double[] arrayToFill)
    {
        int n = to - 1;
        for(int i = n;i>=from;i--)
        {
            double r = residual(i, yValues[i]);
            arrayToFill[i - from] = Math.abs(r);
        }
    }

    @Override
    public double squaresSum(double[][] points)
    {
        return squaresSum(points, 0, points.length);
    }

    @Override
    public double squaresSum(double[][] points, int from, int to)
    {
        double crit = 0;

        for(int i = from; i<to; i++)
        {
            double[] p = points[i];
            double r = residual(p);
            crit += r*r;
        }

        return crit;
    }

    @Override
    public double squaresSum(double[] pointsYs, double[] pointsXs)
    {
        return squaresSum(pointsYs, pointsXs, 0, pointsYs.length);
    }

    @Override
    public double squaresSum(double[] pointsYs, double[] pointsXs, int from, int to)
    {
        double crit = 0;

        for(int i = from;i<to;i++)
        {
            double r = residual(pointsXs[i], pointsYs[i]);
            crit += r*r;
        }

        return crit;
    }

    @Override
    public double absoluteDeviationsSum(double[][] points)
    {
        int s = points.length;
        return absoluteDeviationsSum(points, 0, s);
    }

    @Override
    public double absoluteDeviationsSum(double[][] points, int from, int to)
    {
        double crit = 0;

        for(int i = from;i<to;i++)
        {
            double[] p = points[i];
            double r = residual(p);
            crit += Math.abs(r);
        }

        return crit;
    }


    @Override
    public double absoluteDeviationsSum(double[] pointsYs, double[] pointsXs)
    {
        return absoluteDeviationsSum(pointsYs, pointsXs, 0, pointsYs.length);
    }

    @Override
    public double absoluteDeviationsSum(double[] pointsYs, double[] pointsXs, int from, int to)
    {
        double crit = 0;

        for(int i = from;i<to;i++)
        {
            double r = residual(pointsXs[i], pointsYs[i]);
            crit += Math.abs(r);
        }

        return crit;
    }

    @Override
    public double trimmedSquares(double[][] points, int from, int to, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(points, from, to, arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        return crit;
    }  

    @Override
    public double trimmedAbsoluteDeviations(double[][] points, int from, int to, double[] arrayToFillWithAbsoluteResiduals,int c)
    {
        computeAbsoluteResiduals(points, from, to, arrayToFillWithAbsoluteResiduals);
        Selector.sortSmallest(arrayToFillWithAbsoluteResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithAbsoluteResiduals, c);

        return crit;
    } 

    @Override
    public double trimmedSquares(double[] ys, double[] xs, int from, int to, double[] arrayToFill, int c)
    {
        computeSquaredResiduals(ys, xs, from, to, arrayToFill);
        Selector.sortSmallest(arrayToFill, c);
        double crit = ArrayUtilities.total(arrayToFill, c);

        return crit;
    }

    @Override
    public double trimmedAbsoluteDeviations(double[] ys, double[] xs, int from, int to, double[] arrayToFill, int c)
    {
        computeAbsoluteResiduals(ys, xs, from, to, arrayToFill);
        Selector.sortSmallest(arrayToFill, c);
        double crit = ArrayUtilities.total(arrayToFill, c);

        return crit;
    }

    public double trimmedSquares(double[] yValues, int from, int to, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(yValues, from, to, arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        return crit;
    } 

    public double trimmedAbsoluteDeviations(double[] yValues, int from, int to, double[] arrayToFillWithAbsoluteResiduals,int c)
    {
        computeAbsoluteResiduals(yValues, from, to, arrayToFillWithAbsoluteResiduals);
        Selector.sortSmallest(arrayToFillWithAbsoluteResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithAbsoluteResiduals, c);

        return crit;
    } 

    @Override
    public double trimmedSquares(double[][] points, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(points,arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        return crit;
    }   

    @Override
    public double trimmedAbsoluteDeviations(double[][] points, double[] arrayToFillWithAbsoluteResiduals,int c)
    {
        computeAbsoluteResiduals(points,arrayToFillWithAbsoluteResiduals);
        Selector.sortSmallest(arrayToFillWithAbsoluteResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithAbsoluteResiduals, c);

        return crit;
    }   

    @Override
    public double trimmedSquares(double[] ys, double[] xs, double[] arrayToFill, int c)
    {
        computeSquaredResiduals(ys, xs, arrayToFill);
        Selector.sortSmallest(arrayToFill, c);
        double crit = ArrayUtilities.total(arrayToFill, c);

        return crit;
    }   

    @Override
    public double trimmedAbsoluteDeviations(double[] ys, double[] xs, double[] arrayToFill, int c)
    {
        computeAbsoluteResiduals(ys, xs, arrayToFill);
        Selector.sortSmallest(arrayToFill, c);
        double crit = ArrayUtilities.total(arrayToFill, c);

        return crit;
    }   

    @Override
    public double trimmedSquares(double[] yValues, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(yValues,arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        return crit;
    }   

    @Override
    public double trimmedAbsoluteDeviations(double[] yValues, double[] arrayToFillWithABsoluteResiduals,int c)
    {
        computeAbsoluteResiduals(yValues,arrayToFillWithABsoluteResiduals);
        Selector.sortSmallest(arrayToFillWithABsoluteResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithABsoluteResiduals, c);

        return crit;
    }   

    @Override
    public double trimmedSquares(double[][] optional, double[][] support, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(optional,arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double critAll = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c) + squaresSum(support);
        return critAll;
    }

    @Override
    public double trimmedSquares(double[][] points, int supportLength, double[] arrayToFillWithSquaredResiduals,int c)
    {
        int n = points.length;
        computeSquaredResiduals(points,supportLength,n,arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double critAll = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c) + squaresSum(points, 0, supportLength);
        return critAll;
    }

    @Override
    public double trimmedSquares(double[] optionalY, double[] optionalX, double[] supportY, double[] supportX, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(optionalY, optionalX, arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double critAll = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c) + squaresSum(supportY, supportX);
        return critAll;
    }

    @Override
    public double trimmedSquares(double[] ys, double[] xs, int supportLength, double[] arrayToFillWithSquaredResiduals,int c)
    {
        int n = ys.length;
        computeSquaredResiduals(ys, xs, supportLength, n, arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double critAll = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c) + squaresSum(ys, xs, 0, supportLength);
        return critAll;
    }

    @Override
    public double trimmedAbsoluteDeviations(double[][] points, double[][] support, double[] arrayToFillWithAbsolutedResiduals,int c)
    {
        computeAbsoluteResiduals(points,arrayToFillWithAbsolutedResiduals);
        Selector.sortSmallest(arrayToFillWithAbsolutedResiduals, c);
        double critAll = ArrayUtilities.total(arrayToFillWithAbsolutedResiduals, c) + absoluteDeviationsSum(support);
        return critAll;
    }

    @Override
    public double trimmedAbsoluteDeviations(double[] optionalY, double[] optionalX, double[] supportY, double[] supportX, double[] arrayToFillWithAbsolutedResiduals,int c)
    {
        computeAbsoluteResiduals(optionalY, optionalX,arrayToFillWithAbsolutedResiduals);
        Selector.sortSmallest(arrayToFillWithAbsolutedResiduals, c);
        double critAll = ArrayUtilities.total(arrayToFillWithAbsolutedResiduals, c) + absoluteDeviationsSum(supportY, supportX);
        return critAll;
    }
    public double trimmedWSquares(double[][] points, double[][] support, double[] arrayToFillWithSquaredResiduals,int c)
    {
        computeSquaredResiduals(points,arrayToFillWithSquaredResiduals);
        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        int remnants = points.length - c;
        double winsorized = arrayToFillWithSquaredResiduals[c - 1];
        crit = crit + remnants*winsorized + squaresSum(support);

        return crit;
    }

    public static int getIndexOfPointWithGreatestXAmonngThoseWithAbsoluteResidualSmallerOrEqualToLimit(double[] ys, double[] xs, int from, int to,double limit, FittedUnivariateFunction f)
    {
        int index = -1;
        double maxX = Double.NEGATIVE_INFINITY;

        for(int i = from; i < to; i++)
        {
            double y = ys[i];
            double x = xs[i];
            if(x > maxX)
            {
                double absResidual = Math.abs(f.residual(x, y));

                if(absResidual <= limit )
                {
                    index = i;
                    maxX = x;
                }
            }
        }

        return index;
    }

    public static double absoluteDeviationsSum(double[][] points, UnivariateFunction f) 
    {
        return absoluteDeviationsSum(points, 0, points.length, f);
    }

    public static double absoluteDeviationsSum(double[][] points, int from, int to, UnivariateFunction f) 
    {
        double objective = 0;

        for(int i = from;i<to;i++)
        {
            double[] p = points[i];
            double x = p[0];
            double y = p[1];
            double r = f.value(x) - y;
            objective += Math.abs(r);
        }

        return objective;
    }

    public static double absoluteDeviationsSum(double[] ys, double[] xs, UnivariateFunction f) 
    {
        return absoluteDeviationsSum(ys, xs, 0, ys.length, f);
    }

    public static double absoluteDeviationsSum(double[] ys, double[] xs, int from, int to, UnivariateFunction f) 
    {
        double objective = 0;

        for(int i = from;i<to;i++)
        {
            double r = f.value(xs[i]) - ys[i];
            objective += Math.abs(r);
        }

        return objective;
    }


    public static double squaresSum(double[][] points, UnivariateFunction f) 
    {
        return squaresSum(points, 0, points.length, f);
    }

    public static double squaresSum(double[][] points, int from, int to, UnivariateFunction f) 
    {
        double objective = 0;

        for(int i = from;i<to;i++)
        {
            double[] p = points[i];
            double x = p[0];
            double y = p[1];
            double r = f.value(x) - y;
            objective += r*r;
        }

        return objective;
    }

    public static double squaresSum(double[] ys, double[] xs, UnivariateFunction f) 
    {
        return squaresSum(ys, xs, 0, ys.length, f);
    }

    public static double squaresSum(double[] ys, double[] xs, int from, int to, UnivariateFunction f) 
    {
        double objective = 0;

        for(int i = from;i<to;i++)
        {
            double x = xs[i];
            double y = ys[i];
            double r = f.value(x) - y;
            objective += r*r;
        }

        return objective;
    }
}