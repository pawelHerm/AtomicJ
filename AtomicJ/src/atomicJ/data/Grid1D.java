
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

import java.awt.geom.Point2D;
import java.util.Arrays;
import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Validation;

public class Grid1D implements DataAxis1D
{
    private static final double TOLERANCE = 1e-10;

    private final double increment;
    private final double origin;

    private final double xMinimum;
    private final double xMaximum;

    private final int indexCount;

    private final Quantity quantity;

    //IMMUTABLE CLASS

    public Grid1D(double increment, double origin, int columnCount, Quantity quantity)
    {
        Validation.requireNonNegativeParameterName(columnCount, "columnCount");

        this.increment = increment;
        this.origin = origin;

        this.xMinimum = increment > 0 ? origin : origin + increment*(columnCount - 1);
        this.xMaximum = increment > 0 ? origin + increment*(columnCount - 1) : origin;

        this.indexCount = columnCount;
        this.quantity = quantity;
    }

    public Grid1D getGridWithUnitCompatibleQuantity(Quantity compatibleQuantity)
    {
        Validation.requireNonNullParameterName(compatibleQuantity, "compatibleQuantity");

        if(!this.quantity.isUnitCompatible(compatibleQuantity))
        {
            throw new IllegalArgumentException("Quantities "+this.toString() + " and "+compatibleQuantity.toString() + " do not have compatible units");
        }

        double conversionFactor = this.quantity.getUnit().getConversionFactorTo(compatibleQuantity.getUnit());

        double originNew = conversionFactor*origin;
        double incrementNew = conversionFactor*increment;

        Grid1D derivedGrid = new Grid1D(incrementNew, originNew, indexCount, compatibleQuantity);
        return derivedGrid;
    }

    public Grid1D getGridWithReplacedQuantity(double scale, Quantity quantityNew)
    {
        double originNew = scale*origin;
        double incrementNew = scale*increment;

        Grid1D derivedGrid = new Grid1D(incrementNew, originNew, indexCount, quantityNew);
        return derivedGrid;
    }

    //returns grid with increment > 0, if the values can represent Grid1D. Otherwise it returns null.
    public static Grid1D getGrid(double[] values, Quantity quantity, double tolerance)
    {
        int n = values.length;

        double[] xs = Arrays.copyOf(values, n);

        Arrays.sort(xs);

        double origin = xs[0];
        double increment = Grid2D.getPossibleIncrement(origin, xs, tolerance);  
        int rowCount = increment > tolerance && n > 1 ?  (int)Math.rint((xs[n - 1] - origin)/increment) + 1 : 1;

        Grid1D grid = new Grid1D(increment, origin, rowCount, quantity);

        boolean fillsNodes = grid.canFillNodes(xs, tolerance);

        if(fillsNodes)
        {
            return grid;
        }

        return null;
    }

    public boolean canFillNodes(double[] values, double tolerance)
    {        
        boolean[] gridFilling = new boolean[indexCount];

        for(double x : values)
        {
            double columnPrecise = (x - origin)/increment;
            int column = (int)Math.rint(columnPrecise);
            double columnImprecision = Math.abs(column - columnPrecise);

            if(columnImprecision > tolerance)
            {
                return false;
            }

            if(column < 0 || column >= indexCount)
            {
                return false;
            }

            boolean alreadyPresent = gridFilling[column];

            if(alreadyPresent)
            {
                return false;
            }

            gridFilling[column] = true;
        }

        boolean onlyTrue = ArrayUtilities.allElementsEqual(gridFilling, true);

        return onlyTrue;
    }

    @Override
    public boolean isWithinDomain(double x)
    {
        boolean within = (x > xMinimum - TOLERANCE && x < xMaximum + TOLERANCE);      
        return within;
    }

    public Grid1D changeDensity(int columnCountNew)
    {       
        double xIncrementNew = (increment*(indexCount - 1))/(columnCountNew - 1);
        Grid1D newGrid = new Grid1D(xIncrementNew, origin, columnCountNew, quantity);

        return newGrid;
    }

    @Override
    public Quantity getQuantity()
    {
        return quantity;
    }

    @Override
    public double getDataDensity()
    {
        return increment;
    }

    public SortedArrayOrder getOrder()
    {
        SortedArrayOrder order = increment > 0 ? SortedArrayOrder.ASCENDING : SortedArrayOrder.DESCENDING;
        return order;
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
    public double getArgumentVal(double index)
    {
        double x = origin + index*increment;                
        return x;
    }

    @Override
    public int getIndex(double argumentVal)
    {
        int index = (int)Math.rint((argumentVal - origin)/increment);

        return index;
    }

    //the result is guaranteed to be within the data bounds
    @Override
    public int getClosestIndexWithinDataBounds(double x)
    {
        int column = (int)Math.min(indexCount - 1,  Math.max(0, Math.rint((x - origin)/increment)));

        return column;
    }

    @Override
    public int getIndexFloorWithinBounds(double x)
    {
        int index = (int)Math.min(indexCount - 1, Math.max(0, Math.floor((x - origin)/increment)));

        return index;
    }

    @Override
    public int getIndexFloor(double x)
    {
        int index = (int)Math.min(indexCount - 1, Math.max(-1, Math.floor((x - origin)/increment)));

        return index;
    }

    @Override
    public int getIndexCeilingWithinBounds(double x)
    {
        int column = (int)Math.max(0, Math.min(indexCount - 1, Math.ceil((x - origin)/increment)));

        return column;
    }

    @Override
    public int getIndexCeiling(double x)
    {
        int index = (int)Math.max(0, Math.min(indexCount, Math.ceil((x - origin)/increment)));

        return index;
    }

    public int getIndex(Point2D p)
    {
        double x = p.getX();
        int column = (int)Math.rint((x - origin)/increment);

        return column;
    }

    @Override
    public double getFractionalIndex(double x)
    {
        double index = (x - origin)/increment;
        return index;
    }

    public double getIncrement()
    {
        return increment;
    }

    @Override
    public double getIncrementToNextIndex(int index)
    {
        return increment;
    }

    @Override
    public double getIncrementToPreviousIndex(int ndex)
    {
        return increment;
    }

    @Override
    public double getOrigin()
    {
        return origin;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = (indexCount == 0);
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
        return xMinimum;
    }

    @Override
    public double getCenter()
    {
        double center = origin + 0.5*increment*(indexCount - 1);
        return center;
    }

    @Override
    public double getMaximum()
    {
        return xMaximum;
    }

    public Range getXRange()
    {
        return new Range(getMinimum(), getMaximum());
    }

    @Override
    public double getLength()
    {
        double length = increment*(indexCount - 1);
        return length;
    }

    @Override
    public double[] getNodes()
    {
        return getNodes(0,indexCount);
    }

    public double[] getNodes(int from, int to)
    {
        double[] xs = new double[to - from];

        for(int i = from; i<to; i++)
        {
            xs[i] = origin + i*increment;
        }

        return xs;
    }

    @Override
    public boolean isEqualUpToPrefixes(DataAxis1D other)
    {
        if(!(other instanceof Grid1D)){
            return false;
        }

        Grid1D that = (Grid1D)other;

        if(!this.quantity.getUnit().isCompatible(that.quantity.getUnit()))
        {
            return false;
        }

        double xFactor = that.quantity.getUnit().getConversionFactorTo(this.quantity.getUnit());

        if(!MathUtilities.equalWithinTolerance(this.increment, xFactor*that.increment, TOLERANCE))
        {
            return false;
        }

        if(!MathUtilities.equalWithinTolerance(this.origin, xFactor*that.origin, TOLERANCE))
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
