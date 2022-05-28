
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

import org.apache.commons.math.distribution.CauchyDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;

public enum FitType 
{
    NORMAL("Normal") 
    {
        @Override
        public DistributionFit getFittedDistribution(double[] data) 
        {			
            NormalDistributionImpl distribution = DistributionFitting.fitNormalDistribution(data);
            DistributionFit fit = new BasicDistributionFit(distribution, distribution.getMean(), distribution.getStandardDeviation(), this);

            return fit;
        }

        @Override
        public boolean requiresPositiveValues() 
        {
            return false;
        }
    }, 
    LOG_NORMAL("Lognormal") 
    {
        @Override
        public DistributionFit getFittedDistribution(double[] data) 
        {
            LogNormalDistribution distribution = DistributionFitting.fitLogNormalDistribution(data);

            DistributionFit fit = new BasicDistributionFit(distribution, distribution.getLocationParameter(), distribution.getScaleParameter(), this);

            return fit;
        }

        @Override
        public boolean requiresPositiveValues() 
        {
            return true;
        }
    },
    LORENTZ("Lorentz") 
    {
        @Override
        public DistributionFit getFittedDistribution(double[] data) 
        {
            CauchyDistributionImpl distribution = DistributionFitting.fitCauchyDistribution(data);
            DistributionFit fit = new BasicDistributionFit(distribution, distribution.getMedian(), distribution.getScale(), this);

            return fit;
        }

        @Override
        public boolean requiresPositiveValues() 
        {
            return false;
        }
    },
    LAPLACE("Laplace") 
    {
        @Override
        public DistributionFit getFittedDistribution(double[] data) 
        {
            LaplaceDistribution distribution = DistributionFitting.fitLaplaceDistribution(data);

            DistributionFit fit = new BasicDistributionFit(distribution, distribution.getLocationParameter(), distribution.getScaleParameter(), this);

            return fit;
        }

        @Override
        public boolean requiresPositiveValues() 
        {
            return false;
        }
    };



    private final String prettyName;

    private FitType(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public abstract DistributionFit getFittedDistribution(double[] data);
    public abstract boolean requiresPositiveValues();
}
