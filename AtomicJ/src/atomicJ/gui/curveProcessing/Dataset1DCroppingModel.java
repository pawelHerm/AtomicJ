
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

package atomicJ.gui.curveProcessing;

import org.jfree.data.Range;
import atomicJ.data.Data1D;

public class Dataset1DCroppingModel<E extends Data1D> implements CroppingReceiver
{	
    private double left;
    private double right;
    private double lower;
    private double upper;

    private final E curve;

    private final Range domainFullRange;
    private final Range rangeFullRange;

    private final boolean cropDomain;
    private final boolean cropRange;

    public Dataset1DCroppingModel(E curve, Range domainFullRange, Range rangeFullRange, boolean cropDomain, boolean cropRange)
    {
        this.curve = curve;
        this.cropDomain = cropDomain;
        this.cropRange = cropRange;

        this.domainFullRange = domainFullRange;//croppedChannel.getChannelData().getXRange();
        this.rangeFullRange = rangeFullRange;//croppedChannel.getChannelData().getYRange();
    }

    public E getCurve()
    {
        return curve;
    }

    public Range getFullDomainRange()
    {
        return domainFullRange;
    }

    public Range getFullRangeRange()
    {
        return rangeFullRange;
    }

    public boolean isDomainToBeCropped()
    {
        return cropDomain;
    }

    public boolean isRangeToBeCropped()
    {
        return cropRange;
    }

    public double getLeftCropping()
    {
        return left;
    }

    @Override
    public void setLeftCropping(double t)
    {
        left = t;
    }

    public double getRightCropping()
    {
        return right;
    }

    @Override
    public void setRightCropping(double t)
    {
        right = t;
    }

    public double getLowerCropping()
    {
        return lower;
    }

    @Override
    public void setLowerCropping(double t)
    {
        lower = t;
    }

    public double getUpperCropping()
    {
        return upper;
    }

    @Override
    public void setUpperCropping(double t)
    {
        upper = t;
    }
}
