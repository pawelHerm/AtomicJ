
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

package atomicJ.resources;

import atomicJ.analysis.InterpolationMethod2D;

public class CrossSectionSettings 
{
    private InterpolationMethod2D interpolationMethod;
    private int pointCount;

    public CrossSectionSettings()
    {
        this(200);
    }

    public CrossSectionSettings(int pointCount)
    {
        this(InterpolationMethod2D.BILINEAR, pointCount);   
    }

    public CrossSectionSettings(InterpolationMethod2D method, int pointCount)
    {
        this.interpolationMethod = method;
        this.pointCount = pointCount;
    }

    public CrossSectionSettings(CrossSectionSettings that)
    {
        this.interpolationMethod = that.interpolationMethod;
        this.pointCount = that.pointCount;
    }

    public InterpolationMethod2D getInterpolationMethod()
    {
        return interpolationMethod;
    }

    public void setInterpolationMethod(InterpolationMethod2D method)
    {
        this.interpolationMethod = method;
    }

    public int getPointCount()
    {
        return pointCount;
    }

    public void setPointCount(int pointCountNew)
    {
        this.pointCount = pointCountNew;
    }
}
