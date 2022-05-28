package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class ZeroFunction implements UnivariateFunction
{
    private static final ZeroFunction INSTANCE = new ZeroFunction();

    private ZeroFunction()
    {}

    public static ZeroFunction getInstance()
    {
        return INSTANCE;
    }

    @Override
    public double value(double x) {
        return 0;
    }

}
