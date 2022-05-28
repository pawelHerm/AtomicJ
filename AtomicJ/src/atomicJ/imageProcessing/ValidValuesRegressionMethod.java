package atomicJ.imageProcessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.utilities.ArrayUtilities;


public class ValidValuesRegressionMethod implements MissingValuesEstimationMethod
{
    private final LineFitRegressionStrategy regressionStrategy;

    public ValidValuesRegressionMethod(LineFitRegressionStrategy regressionStrategy)
    {
        this.regressionStrategy = regressionStrategy;
    }

    @Override
    public double[] estimateMissingValues(double[] rawValues)
    {        
        if(!ArrayUtilities.containsNaN(rawValues))
        {
            return rawValues;
        }

        UnivariateFunction f = regressionStrategy.performRegression(getRegressionData(rawValues), 1);

        int n = rawValues.length;
        double[] clearedLocations = new double[n];
        for(int i = 0; i<n; i++)
        {
            double location = rawValues[i];
            clearedLocations[i] = Double.isNaN(location) ? f.value(i) : location;
        }

        return clearedLocations;
    }


    private double[][] getRegressionData(double[] line)
    {
        List<double[]> regressionData = new ArrayList<>();
        int n = line.length;

        for(int i = 0;i<n; i++)
        {
            double item = line[i];
            if(!Double.isNaN(item))
            {
                regressionData.add(new double[] {i, item});
            }
        }

        return regressionData.toArray(new double[][] {});
    }
}
