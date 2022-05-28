package atomicJ.analysis;

import java.awt.geom.Point2D;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface PrecontactInteractionsModel 
{
    public double getPrecontactObjectiveFunctionMinimum(double[][] forceSeparationData, Point2D recordingPoint, RegressionStrategy regressionStrategy);
    public double getPrecontactObjectiveFunctionMinimum(double[][] forceSeparationData, int from, int to, Point2D recordingPoint, RegressionStrategy regressionStrategy);
    public double getPrecontactObjectiveFunctionMinimum(double[] forceValues, double[] separationValues, int from, int to, Point2D recordingPoint, RegressionStrategy regressionStrategy);

    //these function does not modify passed arrays
    public UnivariateFunction getPrecontactFit(double[] dValues, double[] zValues, Point2D recordingPoint, RegressionStrategy regressionStrategy);
}
