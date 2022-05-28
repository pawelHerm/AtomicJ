package atomicJ.statistics;

import atomicJ.data.units.PrefixedUnit;

public enum SampleStatistics 
{
    MEAN("Mean") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics)
        {
            return statistics.getArithmeticMean();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit) 
        {
            return sampleUnit;
        }
    }, 
    TRIMMED_MEAN("Trim 5%") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics)
        {
            return statistics.getTrimmedArithmeticMean();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit)
        {
            return sampleUnit;
        }
    }, 
    MEDIAN("Median")
    {
        @Override
        public double getValue(DescriptiveStatistics statistics)
        {
            return statistics.getMedian();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit)
        {
            return sampleUnit;
        }
    }, 
    QUARTILE_FIRST("Q1")
    {
        @Override
        public double getValue(DescriptiveStatistics statistics) 
        {
            return statistics.getLowerQuartile();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit)
        {
            return sampleUnit;
        }
    },
    QUARTILE_THIRD("Q3")
    {
        @Override
        public double getValue(DescriptiveStatistics statistics)
        {
            return statistics.getUpperQuartile();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit) {
            return sampleUnit;
        }
    }, 
    STANDARD_DEVIATION("SD") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics) {
            return statistics.getStandardDeviation();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit) {
            return sampleUnit;
        }
    },
    STANDARD_ERROR("SE") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics)
        {
            return statistics.getStandardError();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit) {
            return sampleUnit;
        }
    },
    INTERQUARTILE_RANGE("IQR") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics) 
        {
            return statistics.getInterquartileLength();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit) {
            return sampleUnit;
        }
    },
    SKEWNESS("Skewness") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics) 
        {
            return statistics.getSkewness();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit) 
        {
            return null;
        }
    }, 
    KURTOSIS("Kurtosis") 
    {
        @Override
        public double getValue(DescriptiveStatistics statistics) 
        {
            return statistics.getKurtosis();
        }

        @Override
        public PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit)
        {
            return null;
        }
    };
    /*
     *  new IdentityTag("Mean"),new IdentityTag("Trim 5%"),new IdentityTag("Median"),new IdentityTag("Q1"),
            new IdentityTag("Q3"),new IdentityTag("SD"), new IdentityTag("SE"), new IdentityTag("IQR"), new IdentityTag("Skewness"),new IdentityTag("Kurtosis"))
     */

    private final String prettyName;

    SampleStatistics(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public abstract double getValue(DescriptiveStatistics statistics);

    public abstract PrefixedUnit getStatisticsUnit(PrefixedUnit sampleUnit);

    @Override
    public String toString()
    {
        return prettyName;
    }

    public String getPrettyName() {
        return prettyName;
    }
}
