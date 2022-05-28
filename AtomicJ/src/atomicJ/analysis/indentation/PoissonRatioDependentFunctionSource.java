package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface PoissonRatioDependentFunctionSource 
{
    public UnivariateFunction getFunction(double poissonRatio);
}
