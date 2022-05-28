
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

package atomicJ.data;

import atomicJ.data.units.Quantity;

public interface DataAxis1D
{
    public boolean isWithinDomain(double x);
    public int getInsideIndicesCount(double minX, double maxX);
    public Quantity getQuantity();
    public double getArgumentVal(double indexFractional);
    public double getIncrementToNextIndex(int index);
    public double getIncrementToPreviousIndex(int index);
    // the returned index  i >= 0 i i <= rowCount - 1
    public int getIndex(double x);
    public int getClosestIndexWithinDataBounds(double x);
    public double getFractionalIndex(double x);
    public int getIndexFloorWithinBounds(double x);
    public int getIndexFloor(double x);
    public int getIndexCeilingWithinBounds(double x);
    public int getIndexCeiling(double x);
    public double getDataDensity();
    public double[] getNodes();
    public double getOrigin();
    public boolean isEmpty();
    public int getIndexCount();
    public double getMinimum();
    public double getCenter();
    public double getMaximum();
    public double getLength();
    public boolean isEqualUpToPrefixes(DataAxis1D other);
}
