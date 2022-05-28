package atomicJ.imageProcessing;

import gnu.trove.list.TDoubleList;

import java.util.List;

import atomicJ.statistics.DescriptiveStatistics;


public enum LocationMeasure implements SampleFunction
{
    MEDIAN("Median") {
        @Override
        public double getValue(double[] data) 
        {
            return DescriptiveStatistics.median(data);
        }

        @Override
        public double getValue(List<Double> data) 
        {
            return DescriptiveStatistics.median(data);
        }

        @Override
        public double getValue(TDoubleList data) 
        {
            return DescriptiveStatistics.median(data);
        }
    }, ARITHMETIC_MEAN("Mean") 
    {
        @Override
        public double getValue(double[] data) 
        {
            return DescriptiveStatistics.arithmeticMean(data);
        }

        @Override
        public double getValue(List<Double> data) 
        {
            return DescriptiveStatistics.arithmeticMean(data);
        }

        @Override
        public double getValue(TDoubleList line)
        {
            return DescriptiveStatistics.arithmeticMean(line);
        }
    }, TRIMMED_MEAN("Trimmed mean (5%)") 
    {
        @Override
        public double getValue(double[] data) 
        {
            return DescriptiveStatistics.trimmedMean(data, 0.05, 0.05);
        }

        @Override
        public double getValue(List<Double> data) 
        {
            return DescriptiveStatistics.trimmedMeanQuickSelect(data, 0.05, 0.05);
        }

        @Override
        public double getValue(TDoubleList data) 
        {
            return DescriptiveStatistics.trimmedMeanQuickSelect(data, 0.05, 0.05);
        }
    };

    private final String prettyName;

    private LocationMeasure(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }  
}
