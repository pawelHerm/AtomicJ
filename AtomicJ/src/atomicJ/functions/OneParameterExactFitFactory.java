package atomicJ.functions;

import atomicJ.statistics.FittedLinearUnivariateFunction;

public interface OneParameterExactFitFactory extends ExactFitFactory
{
    public FittedLinearUnivariateFunction getExactFit(double x0, double y0);
}