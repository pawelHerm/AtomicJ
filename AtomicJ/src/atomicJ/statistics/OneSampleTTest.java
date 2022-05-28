
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.statistics;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;

public class OneSampleTTest implements SignificanceTest
{
    private static final String TEST_NAME = "Student's t test";
    private static final String STATISTICS_NAME = "t";

    private final double significanceLevel;
    private final double nullMean;
    private final boolean twoTailed;

    private final DescriptiveStatistics stats;

    private final double t;
    private final double df;
    private final double se;
    private final double pValue;

    private final double confidanceIntervalLowerLimit;
    private final double confidanceIntervalUpperLimit;

    public OneSampleTTest(DescriptiveStatistics stats, double nullMean, double significanceLevel, boolean twoTailed) throws MathException 
    { 
        this.stats = stats;
        this.nullMean = nullMean;

        this.significanceLevel = significanceLevel;
        this.twoTailed = twoTailed;

        double mean = stats.getArithmeticMean();
        double n = stats.getSize();

        se = stats.getStandardError();		
        t = (mean - nullMean) / se;		
        df = n - 1;

        TDistribution distribution = new TDistributionImpl(df);

        //calculates confidence interval for the difference between mean

        double criticalTValue = distribution.inverseCumulativeProbability(significanceLevel/2);
        confidanceIntervalLowerLimit = mean + criticalTValue*se;
        confidanceIntervalUpperLimit = mean - criticalTValue*se;

        //calculates type I error probability (i.e. p value)

        double sidenessFactor = twoTailed ? 2.0 : 1.0;		
        pValue = sidenessFactor * distribution.cumulativeProbability(-Math.abs(t));
    }

    public double getNullHypotheisisMean()
    {
        return nullMean;
    }

    public double getSampleMean()
    {
        return stats.getArithmeticMean();
    }

    public double getSampleSize()
    {
        return stats.getSize();
    }

    public DescriptiveStatistics getSampleStatistics()
    {
        return stats;
    }

    public double getConfidanceIntervalUpperLimit()
    {
        return confidanceIntervalUpperLimit;
    }

    public double getConfidanceIntervalLowerLimit()
    {
        return confidanceIntervalLowerLimit;
    }

    public double getSignificanceLevel()
    {
        return significanceLevel;
    }

    public boolean isTwoTailed()
    {
        return twoTailed;
    }

    public boolean isNullHypothesisRejected()
    {
        return significanceLevel >= pValue;
    }

    @Override
    public double getPValue() 
    {
        return pValue;
    }

    @Override
    public String getTestStatisticsName() 
    {
        return STATISTICS_NAME;
    }

    @Override
    public String getName() 
    {
        return TEST_NAME;
    }

    @Override
    public double getTestStatistics() 
    {
        return t;
    }

}
