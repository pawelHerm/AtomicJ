
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

import org.apache.commons.math.distribution.AbstractContinuousDistribution;

public class BasicDistributionFit implements DistributionFit
{
    private final AbstractContinuousDistribution distribution;
    private final double location;
    private final double scale;

    private final FitType fitType;

    public BasicDistributionFit(AbstractContinuousDistribution distribution, double location, double scale, FitType fitType)
    {
        this.distribution = distribution;
        this.location = location;
        this.scale = scale;

        this.fitType = fitType;
    }

    @Override
    public AbstractContinuousDistribution getFittedDistribution()
    {
        return distribution;
    }

    @Override
    public double getLocation()
    {
        return location;
    }

    @Override
    public double getScale()
    {
        return scale;
    }

    @Override
    public FitType getFitType()
    {
        return fitType;
    }
}
