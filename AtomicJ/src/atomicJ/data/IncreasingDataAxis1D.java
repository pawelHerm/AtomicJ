
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

import java.util.Arrays;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.MathUtilities;

public class IncreasingDataAxis1D implements DataAxis1D
{
    private static final double TOLERANCE = 1e-10;

    private final int indexCount;
    private final double[] nodes;
    private final Quantity quantity;

    //Immutable class

    //nodes must be non-decreasing
    public IncreasingDataAxis1D(double[] nodes, Quantity quantity)
    {
        this.indexCount = nodes.length;   
        this.nodes = Arrays.copyOf(nodes, nodes.length);
        this.quantity = quantity;
    }

    @Override
    public boolean isWithinDomain(double x)
    {
        boolean within = (nodes[0] - x <=  TOLERANCE && x - nodes[indexCount - 1] <= TOLERANCE);      
        return within;
    }

    @Override
    public int getInsideIndicesCount(double minX, double maxX)
    {
        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int minIndex = Math.max(0, getIndexCeiling(minX));
        int maxIndex = Math.min(getIndexCount() - 1, getIndexFloor(maxX));

        int count = Math.max(0, (maxIndex + 1 - minIndex));

        return count;
    }

    @Override
    public Quantity getQuantity()
    {
        return quantity;
    }

    @Override
    public double getArgumentVal(double indexFractional)
    {
        if(indexFractional >= indexCount - 1)
        {
            return nodes[indexCount - 1];
        }

        if(indexFractional <= 0)
        {
            return nodes[0];
        }

        int index = (int)indexFractional;
        double t = (indexFractional - index);
        double y = (1 - t )*(nodes[index])+t*nodes[index +1];
        return y;
    }

    // the returned index  i >= 0 i i <= rowCount - 1
    @Override
    public int getIndex(double x)
    {
        int binarySearched = Arrays.binarySearch(nodes, x);
        if(binarySearched >= 0)
        {
            return binarySearched;
        }
        int insertionPoint = -binarySearched - 1;
        if(insertionPoint == 0)
        {
            return insertionPoint;
        }
        if(insertionPoint == indexCount)
        {
            return indexCount - 1;
        }

        int closestIndex = Math.abs(nodes[insertionPoint] - x) < Math.abs(nodes[insertionPoint - 1] - x) ? insertionPoint: insertionPoint - 1;
        return closestIndex;
    }

    @Override
    public int getClosestIndexWithinDataBounds(double x)
    {
        return getIndex(x);
    }

    @Override
    public double getFractionalIndex(double x)
    {
        int binarySearched = Arrays.binarySearch(nodes, x);
        if(binarySearched >= 0)
        {
            return binarySearched;
        }
        int insertionPoint = - binarySearched - 1;
        if(insertionPoint == 0)
        {
            return insertionPoint;
        }
        if(insertionPoint == indexCount)
        {
            return indexCount - 1;
        }

        double t = (x - nodes[insertionPoint - 1])/(nodes[insertionPoint] - nodes[insertionPoint - 1]);

        double fractionalIndex = (insertionPoint - 1) + t;
        return fractionalIndex;

    }

    @Override
    public int getIndexFloorWithinBounds(double x)
    {
        int binarySearched = Arrays.binarySearch(nodes, x);
        if(binarySearched >= 0)
        {
            return binarySearched;
        }
        int insertionPoint = - binarySearched - 1;

        return Math.max(0, insertionPoint - 1);
    }

    @Override
    public int getIndexFloor(double x)
    {
        int binarySearched = Arrays.binarySearch(nodes, x);
        if(binarySearched >= 0)
        {
            return binarySearched;
        }
        int insertionPoint = - binarySearched - 1;

        return (insertionPoint - 1);
    }


    @Override
    public int getIndexCeilingWithinBounds(double x)
    {
        int binarySearched = Arrays.binarySearch(nodes, x);
        if(binarySearched >= 0)
        {
            return binarySearched;
        }

        int insertionPoint = -binarySearched - 1;

        return Math.min(indexCount - 1, insertionPoint);
    }

    @Override
    public int getIndexCeiling(double x)
    {
        int binarySearched = Arrays.binarySearch(nodes, x);
        if(binarySearched >= 0)
        {
            return binarySearched;
        }

        int insertionPoint = -binarySearched - 1;

        return insertionPoint;
    }

    @Override
    public double getDataDensity()
    {
        if(indexCount <= 1)
        {
            return 1;
        }

        double density = (nodes[indexCount - 1] - nodes[0])/(indexCount - 1);       
        return density;
    }

    @Override
    public double[] getNodes()
    {
        return Arrays.copyOf(nodes, nodes.length);
    }

    @Override
    public double getOrigin()
    {
        return nodes[0];
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = indexCount == 0;
        return empty;
    }

    @Override
    public int getIndexCount()
    {
        return indexCount;
    }

    public int getItemCount()
    {
        int count = indexCount;
        return count;
    }

    @Override
    public double getMinimum()
    {
        return nodes[0];
    }

    @Override
    public double getCenter()
    {
        double min = nodes[0];
        double max = nodes[nodes.length - 1];
        double center = min + (max - min)/2.;
        return center;
    }

    @Override
    public double getMaximum()
    {
        return nodes[nodes.length - 1];
    }

    @Override
    public double getIncrementToNextIndex(int leftIndex)
    {
        if(nodes.length <= 1)
        {
            return 1;
        }

        if(leftIndex == nodes.length - 1)
        {
            return nodes[nodes.length - 1] - nodes[nodes.length - 2];
        }

        double increment = nodes[leftIndex + 1] - nodes[leftIndex];
        return increment;
    }

    @Override
    public double getIncrementToPreviousIndex(int index)
    {
        if(nodes.length <= 1)
        {
            return 1;
        }

        if(index == 0)
        {
            return nodes[1] - nodes[0];
        }

        double increment = nodes[index] - nodes[index - 1];
        return increment;
    }


    @Override
    public double getLength()
    {
        double minimum = nodes[0];
        double maximum = nodes[nodes.length - 1];
        double length = maximum - minimum;
        return length;
    }

    @Override
    public boolean isEqualUpToPrefixes(DataAxis1D other)
    {      
        if(!(other instanceof IncreasingDataAxis1D)){
            return false;
        }

        IncreasingDataAxis1D that = (IncreasingDataAxis1D)other;

        if(!this.quantity.getUnit().isCompatible(that.quantity.getUnit()))
        {
            return false;
        }

        double factor = that.quantity.getUnit().getConversionFactorTo(this.quantity.getUnit());

        if(!MathUtilities.equalWithinTolerance(this.nodes, MathUtilities.multiply(that.nodes, factor), TOLERANCE))
        {
            return false;
        }        

        if(this.indexCount != that.indexCount)
        {
            return false;
        }

        return true;
    }
}
