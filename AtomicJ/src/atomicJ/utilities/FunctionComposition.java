package atomicJ.utilities;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class FunctionComposition implements UnivariateFunction
{

    private final UnivariateFunction outerFunction;
    private final UnivariateFunction innerFunction;

    public FunctionComposition(UnivariateFunction outerFunction, UnivariateFunction innerFunction)
    {
        this.outerFunction = outerFunction;
        this.innerFunction = innerFunction;
    }

    @Override
    public double value(double x) 
    {
        double innerVal = innerFunction.value(x);
        double outerVal = outerFunction.value(innerVal);
        return outerVal;
    }

}
