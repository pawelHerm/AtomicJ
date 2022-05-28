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

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod1D;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;

public class GridChannel1DData implements Channel1DData
{
    private final double[] yValues;
    private final Grid1D grid;
    private final Quantity yQuantity;

    public GridChannel1DData(double[] ys, Grid1D grid, Quantity yQuantity)
    {
        this.grid = grid;
        this.yValues = ys;
        this.yQuantity = yQuantity;
    }

    public GridChannel1DData(GridChannel1DData that)
    {
        this.grid = that.grid;
        this.yValues = Arrays.copyOf(that.yValues, that.yValues.length);
        this.yQuantity = that.yQuantity;
    }

    @Override
    public Quantity getXQuantity()
    {
        return grid.getQuantity();
    }

    @Override
    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    @Override
    public GridChannel1DData getCopy()
    {
        return new GridChannel1DData(this);
    }

    @Override
    public GridChannel1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public GridChannel1DData getCopy(double yScale, Quantity yQuantityNew)
    {
        int n = yValues.length;
        double[] yValuesNew = new double[n];

        for(int i = 0; i<n;i++)
        {
            yValuesNew[i] = yScale*yValues[i];
        }

        GridChannel1DData copy = new GridChannel1DData(yValuesNew, grid, yQuantityNew);

        return copy;
    }

    @Override
    public Channel1DData getCopyWithXQuantityReplaced(double xScale, Quantity xQuantityNew)
    {
        Validation.requireNotNaNParameterName(xScale, "xScale");

        double[] yValuesCopy = Arrays.copyOf(this.yValues, this.yValues.length);
        Grid1D gridCopy = this.grid.getGridWithReplacedQuantity(xScale, xQuantityNew);

        GridChannel1DData channelDataNew = new GridChannel1DData(yValuesCopy, gridCopy, this.yQuantity);

        return channelDataNew;
    }

    @Override
    public GridChannel1DData getCopyWithXAndYQuantitesReplaced(double xScale, Quantity xQuantityNew, double yScale, Quantity yQuantityNew)
    {
        Validation.requireNotNaNParameterName(xScale, "xScale");
        Validation.requireNotNaNParameterName(yScale, "yScale");

        int n = yValues.length;
        double[] yValuesNew = new double[n];

        for(int i = 0; i<n;i++)
        {
            yValuesNew[i] = yScale*yValues[i];
        }

        Grid1D gridCopy = this.grid.getGridWithReplacedQuantity(xScale, xQuantityNew);

        GridChannel1DData copy = new GridChannel1DData(yValuesNew, gridCopy, yQuantityNew);

        return copy;
    }

    public Grid1D getGrid()
    {
        return grid;
    }

    public double[] getData() 
    {
        return yValues;
    }

    public double[] getDataCopy()
    {
        double[] data = Arrays.copyOf(yValues, yValues.length);
        return data;
    }

    public double[] getDataCopy(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");//we allow from to be equal to the length of the array, in such a case, the returned array is empty
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        double[] data = Arrays.copyOfRange(yValues, from, to);
        return data;
    }

    @Override
    public double[][] getPoints() 
    {        
        int n = yValues.length;

        double[][] point = new double[n][];
        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        for(int i = 0; i<n; i++)
        {
            point[i] = new double[] {origin + i*increment, yValues[i]};
        }
        return point;
    }

    //from inclusive, to exclusive
    @Override
    public double[][] getPointsCopy(int from, int to) 
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");//we allow itemCount to be equal to the length of the array, in such a case, the returned array is empty
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        int n = to - from;

        double[][] point = new double[n][];
        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        for(int i = from; i<to; i++)
        {
            point[i - from] = new double[] {origin + i*increment, yValues[i]};
        }
        return point;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        int n = yValues.length;

        double[][] point = new double[n][];
        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        for(int i = 0; i<n; i++)
        {
            point[i] = new double[] {origin + i*increment, conversionFactor*yValues[i]};
        }
        return point;
    }

    /**
     * Returns such an {@code index1} that {@code getX(index1) <=  upperBound} and for any other {@code index2} 
     * that satisfies  {@code getX(index2) <=  upperBound} it holds that {@code getIndex(index2) <= getIndex(index1)}
     * 
     * If no point has an x coordinate smaller or equal to upperBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending or undetermined order in respect to their x-coordinates, then the method returns -1.
     * If the order of x-coordinates is descending, the the method returns {@code getItemCount()}.
     * */
    @Override
    public int getIndexOfGreatestXSmallerOrEqualTo(double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return columnCount - 1;
            }
            return Math.min(columnCount - 1, Math.max(-1, (int)Math.floor((upperBound - origin)/increment)));
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return 0;
            }
            return Math.min(columnCount, Math.max(0, (int)Math.ceil((upperBound - origin)/increment)));
        }
        //cannot happen
        return - 1;
    }

    /**
     * Returns such an {@code index1} that {@code getX(index1) < upperBound} and for any other {@code index2} 
     * that satisfies  {@code getX(index2) < upperBound} it holds that {@code getIndex(index2) < getIndex(index1)}
     * 
     * If no point has an x coordinate smaller than upperBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending or undetermined order in respect to their x-coordinates, then the method returns -1.
     * If the order of x-coordinates is descending, the the method returns {@code getItemCount()}.
     * */
    public int getIndexOfGreatestXSmallerThan(double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return columnCount - 1;
            }
            return Math.min(columnCount - 1, Math.max(-1, (int)Math.ceil((upperBound - origin)/increment - 1)));
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return 0;
            }
            return Math.min(columnCount, Math.max(0, (int)Math.floor((upperBound - origin)/increment + 1)));
        }
        //cannot happen
        return - 1;
    }

    /**
     * Returns such an {@code index1} that {@code getX(index1) >=  lowerBound}  and for any other {@code index2} 
     * that satisfies  {@code getX(index2) >=  lowerBound} it holds that {@code getIndex(index2) >= getIndex(index1)}
     * 
     * If no point has an x coordinate greater or equal to lowerBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns 
     * {@code getItemCount()}. If the order of x-coordinates is descending or undetermined, the the method returns -1
     * */
    @Override
    public int getIndexOfSmallestXGreaterOrEqualTo(double lowerBound)
    {
        SortedArrayOrder order = getXOrder();

        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return 0;
            }

            return Math.min(columnCount, Math.max(0, (int)Math.ceil((lowerBound - origin)/increment)));
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return columnCount - 1;
            }
            return Math.min(columnCount - 1, Math.max(-1, (int)Math.floor((lowerBound - origin)/increment)));
        }
        //cannot happen
        return -1;
    }

    /**
     * Returns such an {@code index1} that {@code getX(index1) >  lowerBound}  and for any other {@code index2} 
     * that satisfies  {@code getX(index2) >  lowerBound} it holds that {@code getIndex(index2) > getIndex(index1)}
     * 
     * If no point has an x coordinate greater than lowerBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns 
     * {@code getItemCount()}. If the order of x-coordinates is descending or undetermined, the the method returns -1
     * */

    public int getIndexOfSmallestXGreaterThan(double lowerBound)
    {
        SortedArrayOrder order = getXOrder();

        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return 0;
            }

            int ceilingPlusOneIfInteger = (int)Math.floor((lowerBound - origin)/increment + 1);//we use Math.floor(x + 1), which is equal to Math.ceil(x) if x is not equal to integer. If x is an integer, than we get the x + 1
            return Math.min(columnCount, Math.max(0, ceilingPlusOneIfInteger));
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return columnCount - 1;
            }
            return Math.min(columnCount - 1, Math.max(-1, (int)Math.ceil((lowerBound - origin)/increment - 1)));
        }
        //cannot happen
        return -1;
    }

    //also includes points for which x == lowerBound or x == upperBound
    @Override
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        int lowerBoundIndex = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
        int upperBoundIndex = getIndexOfGreatestXSmallerOrEqualTo(upperBound);
        IndexRange range = SortedArrayOrder.ASCENDING.equals(order) ? new IndexRange(lowerBoundIndex, upperBoundIndex) : new IndexRange(upperBoundIndex, lowerBoundIndex);
        return range;
    }

    //returns the widest possible indexRange(from, to) for which lowerBound <= getX(from) <= upperBound but !(lowerBound <= getX(to) <= upperBound)
    //this method is intended to  with methods that accept two indices from array, from and to, and perform some operation on the array, starting
    //at the from index (inclusive) and finishing at the to index (exclusive)
    protected IndexRange getFromInclusiveToExclusiveIndexRangeBoundedBy(double lowerBound, double upperBound)
    {            
        SortedArrayOrder order = getXOrder();

        int to = 0;
        int from = 0;

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            from = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
            to = getIndexOfSmallestXGreaterThan(upperBound);
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            from = getIndexOfGreatestXSmallerOrEqualTo(upperBound);
            to = getIndexOfGreatestXSmallerThan(lowerBound);
        }

        IndexRange indexRange = new IndexRange(from, to);

        return indexRange;
    }

    //returns the widest possible indexRange(from, to) for which lowerBound <= getX(from) <= upperBound and  lowerBound <= getX(to) <= upperBound
    //this method is intended to  with methods that accept two indices from array, from and to, and perform some operation on the array, starting
    //at the from index (inclusive) and finishing at the to index (exclusive)
    protected IndexRange getFromInclusiveToInclusiveIndexRangeBoundedBy(double lowerBound, double upperBound)
    {            
        SortedArrayOrder order = getXOrder();

        int to = 0;
        int from = 0;

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            from = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
            to = getIndexOfGreatestXSmallerOrEqualTo(upperBound);}
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            from = getIndexOfGreatestXSmallerOrEqualTo(upperBound);
            to = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
        }

        IndexRange indexRange = new IndexRange(from, to);

        return indexRange;
    }


    //also includes points for which x == lowerBound or x == upperBound
    @Override
    public int getIndexCountBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        if(SortedArrayOrder.ASCENDING.equals(order) || SortedArrayOrder.DESCENDING.equals(order))
        {
            IndexRange range = getFromInclusiveToInclusiveIndexRangeBoundedBy(lowerBound, upperBound);
            return range.getLengthIncludingEdges();
        }

        //cannot happen
        return ArrayUtilities.countXValuesWithinRange(getPoints(), lowerBound, upperBound);
    }

    @Override
    public boolean isEmpty()
    {
        return grid.isEmpty();
    }

    @Override
    public int getItemCount()
    {
        return grid.getItemCount();
    }

    @Override
    public double getX(int item)
    {
        return grid.getArgumentVal(item);
    }

    @Override
    public double getY(int item)
    {
        return yValues[item];
    }

    @Override
    public double getXMinimum()
    {
        return grid.getMinimum();
    }

    @Override
    public double getXMaximum()
    {
        return grid.getMaximum();
    }

    @Override
    public Range getXRange()
    {
        return grid.getXRange();
    }

    @Override
    public double getYMinimum()
    {
        return ArrayUtilities.getMinimum(yValues);
    }

    @Override
    public int getYMinimumIndex(int from, int to) 
    {
        int n = yValues.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        return ArrayUtilities.getMinimumIndex(yValues, from, to);
    }

    @Override
    public double getYMaximum()
    {
        return ArrayUtilities.getMaximum(yValues);
    }

    @Override
    public int getYMaximumIndex(int from, int to)
    {
        int n = yValues.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        return ArrayUtilities.getMaximumIndex(yValues, from, to);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return ArrayUtilities.getIndicesOfExtrema(yValues);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        return ArrayUtilities.getIndicesOfExtrema(yValues, from, to);
    }

    @Override
    public Range getYRange()
    {
        return ArrayUtilities.getBoundedRange(yValues);
    }

    @Override
    public Range getYRange(Range xRange)
    {
        IndexRange indexRange = getFromInclusiveToExclusiveIndexRangeBoundedBy(xRange.getLowerBound(), xRange.getUpperBound());
        return ArrayUtilities.getBoundedRange(yValues, indexRange.getMinIndex(), indexRange.getMaxIndex());
    }

    @Override
    public double[] getPoint(int item)
    {
        return new double[] {grid.getArgumentVal(item), yValues[item]};
    } 

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        return grid.getClosestIndexWithinDataBounds(x);
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return grid.getOrder();
    }

    @Override
    public double[] getXCoordinates() 
    {
        return grid.getNodes(0, yValues.length);
    }    

    @Override
    public double[] getXCoordinatesCopy() 
    {
        return grid.getNodes(0, yValues.length);
    }

    @Override
    public double[] getXCoordinatesCopy(int from, int to) 
    {
        int itemCount = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, itemCount, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, itemCount, "to");

        return grid.getNodes(from, to);
    }

    @Override
    public double[] getYCoordinates()
    {
        return yValues;
    }

    @Override
    public double[] getYCoordinatesCopy()
    {
        return Arrays.copyOf(yValues, yValues.length);
    }

    //from - inclusive, to - exclusive
    @Override
    public double[] getYCoordinatesCopy(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        return Arrays.copyOfRange(yValues, from, to);
    }

    @Override
    public double[][] getXYViewCopy()
    {
        return getXYViewCopy(0, yValues.length);
    }

    //from - inclusive, to - exclusive
    @Override
    public double[][] getXYViewCopy(int from, int to)
    {
        int n = yValues.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        for(int i = from; i < to; i++)
        {
            int j = i - from;
            xs[j] = origin + i*increment;
            ys[j] = yValues[i];
        }

        return new double[][] {xs, ys};
    }

    @Override
    public double getXMinimumIncludingErrors() {
        return getXMinimum();
    }

    @Override
    public double getXMaximumIncludingErrors() {
        return getXMaximum();
    }

    @Override
    public double getYMinimumIncludingErrors() {
        return getYMinimum();
    }

    @Override
    public double getYMaximumIncludingErrors() {
        return getYMaximum();
    }

    @Override
    public Range getYRangeIncludingErrors(Range xRange)
    {
        return getYRange(xRange);
    }

    public static GridChannel1DData convert(Channel1DData channelOriginal)
    {
        return InterpolationMethod1D.LINEAR.getGriddedData(channelOriginal, channelOriginal.getItemCount());
    }

    public static class GridChannel1DDataWithErrorBars extends GridChannel1DData implements Channel1DDataWithErrors
    {
        private final ErrorBarType errorType;
        private final double[] errorValues;

        //error values should be non-negative (we do not check this)
        public GridChannel1DDataWithErrorBars(double[] ys, double[] errorValues, Grid1D grid, Quantity yQuantity, ErrorBarType errorType) 
        {
            super(ys, grid, yQuantity);

            Validation.requireTwoArraysNonNullAndOfEqualLengthParameterName(ys, errorValues, "ys", "errorValues");

            this.errorType = Validation.requireNonNullParameterName(errorType, "errorType");
            this.errorValues = errorValues;
        }

        @Override
        public ErrorBarType getErrorType() 
        {
            return errorType;
        }

        @Override
        public double getErrorValue(int item) 
        {
            Validation.requireValueEqualToOrBetweenBounds(item, 0, errorValues.length - 1, "item");

            double error = errorValues[item];
            return error;
        }

        @Override
        public double getYPlusError(int item) 
        {
            Validation.requireValueEqualToOrBetweenBounds(item, 0, errorValues.length - 1, "item");

            double error = errorValues[item];
            double y = getY(item);

            double yPlusError = y + error;          
            return yPlusError;
        }

        @Override
        public double[] getYCoordinatesPlusErrorCopy()
        {
            double[] yCoordinates = getYCoordinates();
            int n = yCoordinates.length;

            double[] yCoordinatesPlusError = new double[n];

            for(int i = 0; i<n; i++)
            {
                yCoordinatesPlusError[i] = yCoordinates[i] + errorValues[i];
            }

            return yCoordinatesPlusError;
        }

        @Override
        public double getYMinusError(int item) 
        {
            Validation.requireValueEqualToOrBetweenBounds(item, 0, errorValues.length-1, "item");

            double error = errorValues[item];
            double y = getY(item);

            double yPlusError = y - error;          
            return yPlusError;
        }

        @Override
        public double[] getYCoordinatesMinusErrorCopy() 
        {
            double[] yCoordinates = getYCoordinates();
            int n = yCoordinates.length;

            double[] yCoordinatesPlusError = new double[n];

            for(int i = 0; i<n;i++)
            {
                yCoordinatesPlusError[i] = yCoordinates[i] - errorValues[i];
            }

            return yCoordinatesPlusError;
        }

        @Override
        public Range getYRangeForDataWithErrors()
        {
            double min = ArrayUtilities.getNumericFiniteMinimum(getYCoordinatesMinusErrorCopy());
            double max = ArrayUtilities.getNumericFiniteMaximum(getYCoordinatesPlusErrorCopy());

            Range combined = new Range(min, max);

            return combined;
        }

        @Override
        public double getXMinimumIncludingErrors() {
            return getXMinimum();
        }

        @Override
        public double getXMaximumIncludingErrors() {
            return getXMaximum();
        }

        @Override
        public double getYMinimumIncludingErrors() 
        {
            return ArrayUtilities.getNumericFiniteMinimum(getYCoordinatesMinusErrorCopy());
        }

        @Override
        public double getYMaximumIncludingErrors() 
        {
            return ArrayUtilities.getNumericFiniteMaximum(getYCoordinatesPlusErrorCopy());
        }

        @Override
        public Range getYRangeIncludingErrors(Range xRange)
        {            
            IndexRange indices = getFromInclusiveToExclusiveIndexRangeBoundedBy(xRange.getLowerBound(), xRange.getUpperBound());

            int from = indices.getMinIndex();
            int to = indices.getMaxIndex();

            if(from >= to)
            {
                return null;
            }

            double min = ArrayUtilities.getNumericFiniteMinimum(getYCoordinatesMinusErrorCopy(), from, to);
            double max = ArrayUtilities.getNumericFiniteMaximum(getYCoordinatesPlusErrorCopy(), from, to);

            Range range = new Range(min,max);
            return range;
        }
    }
}

