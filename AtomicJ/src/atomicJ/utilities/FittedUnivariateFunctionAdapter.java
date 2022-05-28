package atomicJ.utilities;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.functions.FittedUnivariateFunction;

public class FittedUnivariateFunctionAdapter extends AbstractFittedUnivariateFunction implements FittedUnivariateFunction
{
    private final UnivariateFunction originalFunction;
    private final int estimatedParameterCount;

    public FittedUnivariateFunctionAdapter(UnivariateFunction originalFunction, int estimatedParameterCount)
    {
        this.originalFunction = originalFunction;
        this.estimatedParameterCount = estimatedParameterCount;
    }

    public static FittedUnivariateFunction convertOrCastToFittedUnivariateFunction(UnivariateFunction originalFunction, int estimatedParameterCount)
    {
        FittedUnivariateFunction fitted = (originalFunction instanceof FittedUnivariateFunction) ? ((FittedUnivariateFunction)originalFunction) : new FittedUnivariateFunctionAdapter(originalFunction, estimatedParameterCount);
        return fitted;
    }

    @Override
    public double value(double x) 
    {
        return originalFunction.value(x);
    }

    @Override
    public int getEstimatedParameterCount() 
    {
        return estimatedParameterCount;
    }
}
