
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

public class CropSettings 
{
    private final double top;	
    private final double left;	
    private final double bottom;	
    private final double right;

    public CropSettings(double top, double left, double bottom, double right)
    {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public boolean isDomainCropped()
    {
        boolean domainCropped = (left > 0) || (right > 0);
        return domainCropped;
    }

    public boolean isRangeCropped()
    {
        boolean rangeCropped = (bottom > 0) || (top > 0);
        return rangeCropped;
    }

    public boolean isAnythingCropped()
    {
        boolean cropped = isRangeCropped() || isDomainCropped();
        return cropped;
    }

    public double getLeft()
    {
        return left;
    }


    public double getRight()
    {
        return right;
    }


    public double getTop()
    {
        return top;
    }


    public double getBottom()
    {
        return bottom;
    }
}
