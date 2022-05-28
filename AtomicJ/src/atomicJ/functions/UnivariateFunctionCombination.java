package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class UnivariateFunctionCombination implements UnivariateFunction
{
    private final UnivariateFunction first;
    private final double firstFactor;

    private final UnivariateFunction second;
    private final double secondFactor;

    public UnivariateFunctionCombination(UnivariateFunction first, double firstFactor, UnivariateFunction second, double secondFactor)
    {
        this.first = first;
        this.firstFactor = firstFactor;

        this.second = second;
        this.secondFactor = secondFactor;
    }

    @Override
    public double value(double x) 
    {
        double value = firstFactor*first.value(x) + secondFactor*second.value(x);
        return value;
    }
}
