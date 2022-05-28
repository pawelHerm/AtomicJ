
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

import org.apache.commons.math.distribution.*;

import atomicJ.utilities.Validation;

public class DistributionFitting 
{
    public static NormalDistributionImpl fitNormalDistribution(double values[])
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values");

        double mean = DescriptiveStatistics.arithmeticMean(values);
        double sd = DescriptiveStatistics.standardDeviationPopulation(values);

        return new NormalDistributionImpl(mean, sd);
    }

    public static LogNormalDistribution fitLogNormalDistribution(double values[])
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values");

        int n = values.length;

        double[] logValues = new double[n];

        for(int i = 0;i<n;i++)
        {
            double x = values[i];
            if(x>0)
            {
                logValues[i] = Math.log(x);
            }
            else
            {
                throw new IllegalArgumentException("The argument 'Values' contains a nonpositive element");
            }
        }

        double location = DescriptiveStatistics.arithmeticMean(logValues);
        double scale = DescriptiveStatistics.standardDeviationPopulation(logValues);

        return new LogNormalDistribution(location, scale);
    }

    public static CauchyDistributionImpl fitCauchyDistribution(double values[])
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values"); 

        double location = DescriptiveStatistics.median(values);
        double scale = 0.5*DescriptiveStatistics.interquartileLength(values);

        return new CauchyDistributionImpl(location, scale);
    }

    public static LaplaceDistribution fitLaplaceDistribution(double values[])
    {
        Validation.requireNonNullAndNonEmptyParameterName(values, "values"); 

        double location = DescriptiveStatistics.median(values);
        double scale = DescriptiveStatistics.meanAbsoluteDeviationFromMedian(values);

        return new LaplaceDistribution(location, scale);
    }
}
