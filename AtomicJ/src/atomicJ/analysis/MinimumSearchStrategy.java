package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface MinimumSearchStrategy 
{
    public double getMinimum(UnivariateFunction f, double start, double end);
}
