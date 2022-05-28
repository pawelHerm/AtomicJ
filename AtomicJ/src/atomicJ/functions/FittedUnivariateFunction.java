package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface FittedUnivariateFunction extends UnivariateFunction
{
    public int getEstimatedParameterCount();
    public double residual(double[] p);
    public double residual(double x, double y);
    public double[] getSquaredResiduals(double[][] points, int from, int to);
    public double[] getAbsoluteResiduals(double[][] points, int from, int to);
    public double[] getAbsoluteResiduals(double[] ys, double[] xs, int from, int to);

    public double squaresSum(double[][] points);
    public double squaresSum(double[][] points, int from, int to);
    public double squaresSum(double[] pointsYs, double[] pointsXs);
    public double squaresSum(double[] pointsYs, double[] pointsXs, int from, int to);

    public double absoluteDeviationsSum(double[][] points);
    public double absoluteDeviationsSum(double[][] points, int from, int to);
    public double absoluteDeviationsSum(double[] pointsYs, double[] pointsXs);
    public double absoluteDeviationsSum(double[] pointsYs, double[] pointsXs, int from,int to);

    public double trimmedSquares(double[][] points, double[] arrayToFill, int c);
    public double trimmedSquares(double[] ys, double[] xs, double[] arrayToFill, int c);

    public double trimmedSquares(double[][] points, int from, int to, double[] arrayToFill, int c);
    public double trimmedSquares(double[] ys, double[] xs, int from, int to, double[] arrayToFill, int c);

    public double trimmedSquares(double[] yValues, double[] arrayToFillWithSquaredResiduals,int c);

    public double trimmedSquares(double[][] points, double[][] support, double[] arrayToFillWithSquaredResiduals,int c);
    public double trimmedSquares(double[][] points, int supportLength, double[] arrayToFillWithSquaredResiduals, int c);
    public double trimmedSquares(double[] optionalY, double[] optionalX, double[] supportY, double[] supportX, double[] arrayToFillWithSquaredResiduals, int c);
    public double trimmedSquares(double[] ys, double[] xs, int supportLength, double[] arrayToFillWithSquaredResiduals, int c);

    public double trimmedAbsoluteDeviations(double[][] points, double[] arrayToFill, int c);
    public double trimmedAbsoluteDeviations(double[] ys, double[] xs, double[] arrayToFill, int c);

    public double trimmedAbsoluteDeviations(double[][] points, int from, int to, double[] arrayToFillWithSquaredResiduals, int c);
    public double trimmedAbsoluteDeviations(double[] ys, double[] xs, int from, int to, double[] arrayToFill, int c);

    public double trimmedAbsoluteDeviations(double[] yValues, double[] arrayToFillWithABsoluteResiduals,int c);

    public double trimmedAbsoluteDeviations(double[][] points, double[][] support, double[] arrayToFillWithAbsolutedResiduals,int c);
    public double trimmedAbsoluteDeviations(double[] optionalY, double[] optionalX, double[] supportY, double[] supportX, double[] arrayToFillWithAbsolutedResiduals, int c);
}
