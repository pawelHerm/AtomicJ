package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface ParametrizedUnivariateFunction extends UnivariateFunction
{
    public int getEstimatedParameterCount();
    public double[] getParameters();
}
