package atomicJ.functions;

import java.util.Random;

import atomicJ.statistics.FittedLinearUnivariateFunction;

public interface ExactFitFactory 
{
    public FittedLinearUnivariateFunction getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n);
    public FittedLinearUnivariateFunction getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to);
    public FittedLinearUnivariateFunction getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n);
    public FittedLinearUnivariateFunction getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to);
    public FittedLinearUnivariateFunction getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n);
    public FittedLinearUnivariateFunction getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBs, double[] pointsBXs, int n);

    public int getEstimatedParameterCount();
}
