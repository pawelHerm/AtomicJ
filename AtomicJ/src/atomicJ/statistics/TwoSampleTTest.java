
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

public class TwoSampleTTest implements SignificanceTest
{
    private static final String TEST_NAME = "Student's two-sample t test";
    private static final String STATISTICS_NAME = "t";

    private final double significanceLevel;
    private final boolean twoTailed;
    private final boolean variancesEqual;

    private final DescriptiveStatistics stat1;
    private final DescriptiveStatistics stat2;

    private final double t;
    private final double df;
    private final double se;
    private final double pValue;

    private final double confidanceIntervalLowerLimit;
    private final double confidanceIntervalUpperLimit;

    public TwoSampleTTest(DescriptiveStatistics stat1, DescriptiveStatistics stat2, double significanceLevel, boolean twoTailed, boolean variancesEqual) throws MathException 
    { 
        this.stat1 = stat1;
        this.stat2 = stat2;

        this.significanceLevel = significanceLevel;
        this.twoTailed = twoTailed;
        this.variancesEqual = variancesEqual;

        double m1 = stat1.getArithmeticMean();
        double m2 = stat2.getArithmeticMean();
        double v1 = stat1.getVariance();
        double v2 = stat2.getVariance();
        double n1 = stat1.getSize();
        double n2 = stat2.getSize();

        // calculates standard error for the difference between means

        if(variancesEqual)
        {
            double pooledVariance = ((n1  - 1) * v1 + (n2 -1) * v2 ) / (n1 + n2 - 2);
            se = Math.sqrt(pooledVariance * (1/ n1 + 1/ n2)); 
        }
        else
        {
            se = Math.sqrt((v1 / n1) + (v2 / n2));
        }
        t = (m1 - m2) / se;

        // finds the right t distribution

        if(variancesEqual)
        {
            df = n1 + n2 - 2;
        }
        else
        {
            df = (((v1 / n1) + (v2 / n2)) * ((v1 / n1) + (v2 / n2))) /
                    ((v1 * v1) / (n1 * n1 * (n1 - 1d)) + (v2 * v2) /
                            (n2 * n2 * (n2 - 1d)));
        }

        TDistribution distribution = new TDistributionImpl(df);

        //calculates confidence interval for the difference between mean

        double criticalTValue = distribution.inverseCumulativeProbability(significanceLevel/2);
        confidanceIntervalLowerLimit = (m1 - m2) + criticalTValue*se;
        confidanceIntervalUpperLimit = (m1 - m2) - criticalTValue*se;

        //calculates type I error probability (i.e. p value)

        double sidenessFactor = twoTailed ? 2.0 : 1.0;		
        pValue = sidenessFactor * distribution.cumulativeProbability(-Math.abs(t));
    }

    public double getFirstSampleMean()
    {
        return stat1.getArithmeticMean();
    }

    public double getSecondSampleMean()
    {
        return stat2.getArithmeticMean();
    }
    public double getMeanDifference()
    {
        double m1 = stat1.getArithmeticMean();
        double m2 = stat2.getArithmeticMean();

        return m1 - m2;
    }

    public double getFirstSampleSize()
    {
        return stat1.getSize();
    }

    public double getSecondSampleSize()
    {
        return stat2.getSize();
    }

    public DescriptiveStatistics getFirstSampleStatistics()
    {
        return stat1;
    }

    public DescriptiveStatistics getSecondSampleStatistics()
    {
        return stat2;
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

    public boolean isVariancesAssuemdEqual()
    {
        return variancesEqual;
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
