
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

package atomicJ.analysis;

import java.awt.geom.Point2D;

import atomicJ.data.Channel2DData;


public class TopographyThinSampleModel implements ThinSampleModel 
{
    private final double poissonRatio;
    private final boolean bound;
    private final Channel2DData topographyChannel;

    public TopographyThinSampleModel(double poissonRatio, Channel2DData topographyChannel, boolean bound)
    {
        this.poissonRatio = poissonRatio;
        this.topographyChannel = topographyChannel;
        this.bound = bound;
    }

    @Override
    public boolean isBondedToSubstrate()
    {
        return bound;
    }

    @Override
    public double getThickness(Point2D p)
    {
        if(p == null)
        {
            return Double.POSITIVE_INFINITY;
        }
        else
        {
            return Math.max(0, topographyChannel.getValue(p)); 
        }
    }

    @Override
    public double getPoissonRatio() 
    {
        return poissonRatio;
    }
}
