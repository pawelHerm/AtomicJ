package atomicJ.imageProcessing;

import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;

public class ImageLineRegressionFunction implements SampleFunctional
{
    private final LineFitRegressionStrategy strategy;
    private final int deg;

    public ImageLineRegressionFunction(LineFitRegressionStrategy estimator, int deg)
    {
        this.strategy = estimator;
        this.deg = deg;
    }

    @Override
    public UnivariateFunction getValue(double[] line) 
    {        
        if(line.length < deg + 1)
        {
            return null;
        }

        UnivariateFunction f = strategy.performRegression(line, deg);
        return f;
    }

    @Override
    public UnivariateFunction getValue(double[][] line) 
    {        
        if(line.length < deg + 1)
        {
            return null;
        }

        UnivariateFunction f = strategy.performRegression(line, deg);
        return f;
    }

    @Override
    public UnivariateFunction[] getValues(double[][] equispacedLines, int lineLength)
    {
        if(lineLength < deg + 1)
        {
            return null;
        }

        UnivariateFunction[] functions = strategy.performRegressionsOnEquispacedLines(equispacedLines,lineLength, deg);
        return functions;
    }

    @Override
    public UnivariateFunction getValue(List<double[]> line) 
    {
        if(line.size() < deg + 1)
        {
            return null;
        }

        UnivariateFunction f = strategy.performRegression(line.toArray(new double[][] {}), deg);
        return f;
    }
}
