package atomicJ.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import atomicJ.imageProcessing.MissingValuesEstimationMethod;
import atomicJ.utilities.ArrayUtilities;


public class LoessMissingValuesEstimationMethod implements MissingValuesEstimationMethod
{
    @Override
    public double[] estimateMissingValues(double[] rawValues)
    {  
        if(!ArrayUtilities.containsNaN(rawValues))
        {
            return rawValues;
        }

        int n = rawValues.length;

        double[][] points = getPointsForInterpolation(rawValues);

        LoessInterpolator interpolator = new LoessInterpolator();
        try
        {
            PolynomialSplineFunction f = interpolator.interpolate(points[0], points[1]);

            double[] cleared = new double[n];

            for(int i = 0; i<n; i++)
            {
                double raw = rawValues[i];
                cleared[i] = Double.isNaN(raw) ? f.value(i) : raw;               
            }

            return cleared;
        } 
        catch (MathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return new double[n];
        }       
    }

    private double[][] getPointsForInterpolation(double[] rawValues)
    {
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        for(int i = 0; i<rawValues.length; i++)
        {
            double d = rawValues[i];
            if(!Double.isNaN(d))
            {
                xs.add((double) i);
                ys.add(d);
            }
        }


        double[] xsArray = ArrayUtilities.getDoubleArray(xs);
        double[] ysArray = ArrayUtilities.getDoubleArray(ys);

        double[][] points = new double[][] {xsArray, ysArray};

        return points;
    }
}
