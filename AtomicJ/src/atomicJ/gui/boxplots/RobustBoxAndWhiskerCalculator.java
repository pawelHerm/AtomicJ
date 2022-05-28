package atomicJ.gui.boxplots;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.QuantitativeSample;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.Validation;



public class RobustBoxAndWhiskerCalculator
{
    //prevent creating instances
    private RobustBoxAndWhiskerCalculator(){};

    public static RobustBoxAndWhiskerItem calculateBoxAndWhiskerStatistics(QuantitativeSample sample) 
    {
        Validation.requireNonNullParameterName(sample, "sample");

        DescriptiveStatistics stats =  sample.getDescriptiveStatistics(); 
        double mean = stats.getArithmeticMean();
        double median = stats.getMedian();
        double q1 = stats.getLowerQuartile();
        double q3 = stats.getUpperQuartile();

        double interQuartileRange = stats.getInterquartileLength();

        double upperOutlierThreshold = q3 + (interQuartileRange * 1.5);
        double lowerOutlierThreshold = q1 - (interQuartileRange * 1.5);

        double minRegularValue = Double.POSITIVE_INFINITY;
        double maxRegularValue = Double.NEGATIVE_INFINITY;
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;

        List<Double> outliers = new ArrayList<>();

        double[] values = sample.getMagnitudes();

        for(double v : values)
        {
            if(v > maxValue)
            {
                maxValue = v;
            }           
            if( v < minValue)
            {
                minValue = v;
            }

            if(v <= upperOutlierThreshold && v>= lowerOutlierThreshold)
            {
                minRegularValue = Math.min(minRegularValue, v);
                maxRegularValue = Math.max(maxRegularValue, v);
            }
            else
            {
                outliers.add(v);
            }
        }

        return new RobustBoxAndWhiskerItem(mean, median, q1, q3, minRegularValue,
                maxRegularValue, minValue, maxValue, outliers);

    }
}

