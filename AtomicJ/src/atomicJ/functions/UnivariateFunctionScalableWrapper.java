package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class UnivariateFunctionScalableWrapper implements UnivariateFunction
{
    private final UnivariateFunction f;
    private final double xScale;
    private final double yScale;

    private UnivariateFunctionScalableWrapper(UnivariateFunction f, double xScale, double yScale)
    {
        this.f = f;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public static UnivariateFunction getScaledFunction(UnivariateFunction f, double xScale, double yScale)
    {
        UnivariateFunctionScalableWrapper scaledFunction = new UnivariateFunctionScalableWrapper(f, xScale, yScale);
        return scaledFunction;
    }

    @Override
    public double value(double x)
    {
        double value = yScale*f.value(xScale*x);
        return value;
    }
}