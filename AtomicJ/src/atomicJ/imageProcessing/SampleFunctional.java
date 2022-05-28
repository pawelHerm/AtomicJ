package atomicJ.imageProcessing;

import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;

public interface SampleFunctional 
{
    public UnivariateFunction getValue(double[] line);
    public UnivariateFunction getValue(double[][] line);
    public UnivariateFunction[] getValues(double[][] lines, int lineLength);
    public UnivariateFunction getValue(List<double[]> line);
}
