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

import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;

public class ConstantChannel1DData implements Channel1DData
{
    private final double value;
    private final Grid1D grid;
    private final Quantity yQuantity;

    public ConstantChannel1DData(double value, Grid1D grid, Quantity yQuantity)
    {
        this.value = value;
        this.grid = grid;
        this.yQuantity = yQuantity;
    }

    public ConstantChannel1DData(ConstantChannel1DData that)
    {
        this.value = that.value;
        this.grid = that.grid;
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
    public ConstantChannel1DData getCopy()
    {
        return new ConstantChannel1DData(this);
    }

    @Override
    public Channel1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public Channel1DData getCopy(double scale, Quantity yQuantityNew)
    {       
        Channel1DData copy = new GridChannel1DData(getDataCopy(scale), grid, yQuantityNew);

        return copy;
    }


    @Override
    public ConstantChannel1DData getCopyWithXQuantityReplaced(double xScale, Quantity xQuantityNew)
    {
        Grid1D gridCopy = this.grid.getGridWithReplacedQuantity(xScale, xQuantityNew);

        ConstantChannel1DData channelDataNew = new ConstantChannel1DData(this.value, gridCopy, this.yQuantity);

        return channelDataNew;
    }

    @Override
    public ConstantChannel1DData getCopyWithXAndYQuantitesReplaced(double xScale, Quantity xQuantityNew, double yScale, Quantity yQuantityNew)
    {       
        Validation.requireNotNaNParameterName(xScale, "xScale");
        Validation.requireNotNaNParameterName(yScale, "yScale");

        double yValueNew = yScale*this.value;
        Grid1D gridCopy = this.grid.getGridWithReplacedQuantity(xScale, xQuantityNew);

        ConstantChannel1DData copy = new ConstantChannel1DData(yValueNew, gridCopy, yQuantityNew);

        return copy;
    }

    public Grid1D getGrid()
    {
        return grid;
    }

    public double[] getData() 
    {
        return getData(0, grid.getItemCount());
    }

    //from inclusive, to exclusive
    public double[] getData(int from, int to) 
    {
        double[] data = ArrayUtilities.createAndPopulateConstantArray(value, to - from);
        return data;
    }

    public double[] getDataCopy()
    {
        return getData();
    }

    public double[] getDataCopy(int from, int to)
    {
        return getData(from, to);
    }

    public double[] getDataCopy(double s) 
    {
        int columnCount = grid.getIndexCount();
        double[] data = ArrayUtilities.createAndPopulateConstantArray(value, columnCount);
        return data;
    }

    @Override
    public double[][] getPoints() 
    {
        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[][] points = new double[columnCount][];

        for(int i = 0; i < columnCount; i++)
        {
            double x = origin + i*increment;
            points[i] = new double[] {x, value};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy(int from, int to) 
    {
        int itemCount = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, itemCount, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, itemCount, "to");

        int n = to - from;
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[][] points = new double[n][];

        for(int i = from; i < to; i++)
        {
            double x = origin + i*increment;
            points[i - from] = new double[] {x, value};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[][] points = new double[columnCount][];

        for(int i = 0; i < columnCount; i++)
        {
            double x = origin + i*increment;
            points[i] = new double[] {x, conversionFactor*value};
        }

        return points;
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


    @Override
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        int lowerBoundIndex = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
        int upperBoundIndex = getIndexOfGreatestXSmallerOrEqualTo(upperBound);
        IndexRange range = SortedArrayOrder.ASCENDING.equals(order) ? new IndexRange(lowerBoundIndex, upperBoundIndex) : new IndexRange(upperBoundIndex, lowerBoundIndex);
        return range;
    }

    @Override
    public int getIndexCountBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        if(SortedArrayOrder.ASCENDING.equals(order) || SortedArrayOrder.DESCENDING.equals(order))
        {
            IndexRange range = getIndexRangeBoundedBy(lowerBound, upperBound);
            return range.getLengthIncludingEdges();
        }

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
        return value;
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
        return value;
    }

    @Override
    public int getYMinimumIndex(int from, int to)
    {
        return 0;
    }

    @Override
    public double getYMaximum()
    {
        return value;
    }

    @Override
    public int getYMaximumIndex(int from, int to)
    {
        return 0;
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return getIndicesOfYExtrema(0, getItemCount());
    }

    //from inclusive, to exclusive
    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        return new OrderedIntegerPair(0, 0);
    }

    @Override
    public Range getYRange()
    {
        return new Range(value, value);
    }

    @Override
    public Range getYRange(Range xRange)
    {
        return new Range(value, value);
    }

    @Override
    public double[] getPoint(int item)
    {
        return new double[] {grid.getArgumentVal(item), value};
    } 

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        return grid.getIndex(x);
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return grid.getOrder();
    }

    @Override
    public double[] getXCoordinates() 
    {
        return grid.getNodes();
    }

    @Override
    public double[] getXCoordinatesCopy() 
    {
        return grid.getNodes();
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
        return getData();
    }

    @Override
    public double[] getYCoordinatesCopy()
    {
        return getDataCopy();
    }

    @Override
    public double[] getYCoordinatesCopy(int from, int to)
    {
        int n = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        return getData(from, to);
    }

    @Override
    public double[][] getXYViewCopy()
    {
        int n = grid.getItemCount();
        return getXYViewCopy(0, n);
    }

    @Override
    public double[][] getXYViewCopy(int from, int to)
    {
        int n = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        for(int i = from; i<to; i++)
        {
            int j = i  - from;
            double x = origin + i*increment;
            xs[j] = x;
            ys[j] = value;
        }

        return new double[][] {xs, ys};
    }

    @Override
    public double getXMinimumIncludingErrors() {
        return value;
    }

    @Override
    public double getXMaximumIncludingErrors() {
        return value;
    }

    @Override
    public double getYMinimumIncludingErrors() {
        return value;
    }

    @Override
    public double getYMaximumIncludingErrors() {
        return value;
    }

    @Override
    public Range getYRangeIncludingErrors(Range xRange)
    {
        return new Range(value, value);
    }
}

