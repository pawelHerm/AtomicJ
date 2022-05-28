
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

package atomicJ.gui;

import atomicJ.analysis.InterpolationMethod2D;

public class Gridding2DSettings 
{
    private InterpolationMethod2D interpolationMethod;

    private int columnCount;
    private int rowCount;

    public Gridding2DSettings(int rowCount, int columnCount)
    {
        this(InterpolationMethod2D.BILINEAR, rowCount, columnCount);   
    }

    public Gridding2DSettings(InterpolationMethod2D method, int rowCount, int columnCount)
    {
        this.interpolationMethod = method;

        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public Gridding2DSettings(Gridding2DSettings that)
    {
        this.interpolationMethod = that.interpolationMethod;
        this.columnCount = that.columnCount;
    }

    public InterpolationMethod2D getInterpolationMethod()
    {
        return interpolationMethod;
    }

    public void setInterpolationMethod(InterpolationMethod2D method)
    {
        this.interpolationMethod = method;
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    public void setDomainPointCount(int domainPointCountNew)
    {
        this.columnCount = domainPointCountNew;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public void setRangePointCount(int rangePointCountNew)
    {
        this.rowCount = rangePointCountNew;
    }
}
