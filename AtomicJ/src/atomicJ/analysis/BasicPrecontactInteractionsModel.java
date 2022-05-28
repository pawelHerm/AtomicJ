package atomicJ.analysis;

import java.awt.geom.Point2D;

import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.utilities.MathUtilities;


public class BasicPrecontactInteractionsModel implements PrecontactInteractionsModel
{
    private final int p;
    private final int deg;
    private final boolean constant;

    public BasicPrecontactInteractionsModel(int degree, boolean constant)
    {
        this.deg = degree;
        this.constant = constant;
        this.p = deg + MathUtilities.boole(constant);
    }

    @Override
    public double getPrecontactObjectiveFunctionMinimum(double[][] forceSeparationData, Point2D recordingPoint, RegressionStrategy regressionStrategy) 
    {
        return getPrecontactObjectiveFunctionMinimum(forceSeparationData, 0, forceSeparationData.length, recordingPoint, regressionStrategy); 
    }

    @Override
    public double getPrecontactObjectiveFunctionMinimum(double[][] forceSeparationData, int from, int to, Point2D recordingPoint, RegressionStrategy regressionStrategy) 
    {
        int n = to - from;

        if(n <= p)
        {
            return 0;
        }

        return regressionStrategy.getObjectiveFunctionMinimum(forceSeparationData, from, to, deg, constant);
    }

    public double getPrecontactObjectiveFunctionMinimum(double[] forceValues, double[] separationValues, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        return getPrecontactObjectiveFunctionMinimum(forceValues, separationValues, 0, forceValues.length, recordingPoint, regressionStrategy);
    }

    @Override
    public double getPrecontactObjectiveFunctionMinimum(double[] forceValues, double[] separationValues, int from, int to, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        int n = to - from;

        if(n <= p)
        {
            return 0;
        }

        return regressionStrategy.getObjectiveFunctionMinimum(forceValues, separationValues, from, to, deg, constant);
    }

    //these function does not modify passed arrays
    @Override
    public FittedUnivariateFunction getPrecontactFit(double[] dValues, double[] zValues, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        FittedUnivariateFunction reg = regressionStrategy.getFitFunction(dValues, zValues, deg, constant);
        return reg;
    }
}
