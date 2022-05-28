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

import com.google.common.base.Objects;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.OrderedIntegerPair;

public interface Channel1DData
{
    public Channel1DData getCopy();
    public Channel1DData getCopy(double yScale);
    public Channel1DData getCopy(double yScale, Quantity yQuantityNew);

    public Channel1DData getCopyWithXQuantityReplaced(double xScale, Quantity xQuantityNew);
    public Channel1DData getCopyWithXAndYQuantitesReplaced(double xScale, Quantity xQuantityNew, double yScale, Quantity yQuantityNew);

    /**@returns copy of the Channel1DData if the current X or Y quantity is different from the ones passed as arguments.
    If the X and Y quantities are already the same as the requested, then the method returns original
    Channel1Data
    @throws IllegalArgumentException if either {@code xQuantityNew} or {@code yQuantityNew} is not compatible with the current X quantity or Y quantity (respectively)*/
    public default Channel1DData changeToCompatibleQuantities(Quantity xQuantityNew, Quantity yQuantityNew)
    {
        Quantity xQuantity = getXQuantity();

        if(!xQuantity.isUnitCompatible(xQuantityNew))
        {
            throw new IllegalArgumentException("The X quantity "+xQuantity.toString() + " is not cunit-compatible with the quantity "+xQuantityNew.toString());
        }

        Quantity yQuantity = getYQuantity();

        if(!yQuantity.isUnitCompatible(yQuantityNew))
        {
            throw new IllegalArgumentException("The Y quantity "+yQuantity.toString() + " is not cunit-compatible with the quantity "+yQuantityNew.toString());
        }

        if(Objects.equal(xQuantity, xQuantityNew) && Objects.equal(yQuantity, yQuantityNew))
        {
            return this;
        }

        double xConversionFactor = xQuantity.getUnit().getConversionFactorTo(xQuantityNew.getUnit());
        double yConversionFactor = yQuantity.getUnit().getConversionFactorTo(yQuantityNew.getUnit());

        Channel1DData channelDataModified = getCopyWithXAndYQuantitesReplaced(xConversionFactor, xQuantityNew, yConversionFactor, yQuantityNew);

        return channelDataModified;
    }

    public Quantity getXQuantity();
    public Quantity getYQuantity();

    public default boolean isCompatibleInTermsOfUnitQuantities(Channel1DData that)
    {
        if(!this.getXQuantity().isUnitCompatible(that.getXQuantity()))
        {
            return false;
        }
        if(!this.getYQuantity().isUnitCompatible(that.getYQuantity()))
        {
            return false;
        }

        return true;
    }

    public double[][] getPoints();
    public double[][] getPointsCopy();
    public double[][] getPointsCopy(double yScale);
    //from inclusive, to exclusive
    //it must be the case that from > 0 && to > 0 && from <= pointCount && to <= pointCount
    //otherwise IllegalArgumentException is thrown//we allow for from to be equal to point count
    //so that e.g. it is legal to call getPointsCopy(0,0) even if the channel is empty
    public double[][] getPointsCopy(int from, int to);
    public SortedArrayOrder getXOrder();
    public boolean isEmpty();
    public int getItemCount();  
    public double getX(int item);    
    public double getY(int item);
    public double getXMinimum();
    public double getXMinimumIncludingErrors();
    public double getXMaximum(); 
    public double getXMaximumIncludingErrors();    
    public Range getXRange();    
    public double getYMinimum(); 
    public double getYMinimumIncludingErrors();  
    public int getYMinimumIndex(int from, int to);
    public double getYMaximum();
    public double getYMaximumIncludingErrors();
    public int getYMaximumIndex(int from, int to);
    public OrderedIntegerPair getIndicesOfYExtrema();
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to);
    public Range getYRange();
    public Range getYRange(Range xRange);
    public Range getYRangeIncludingErrors(Range xRange);

    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound);

    /**
     * Returns such an {@code index1} that {@code getX(index1) <=  upperBound} and for any other {@code index2} 
     * that satisfies  {@code getX(index2) <=  upperBound} it holds that {@code getIndex(index2) <= getIndex(index1)}
     * 
     * If no point has an x coordinate smaller or equal to upperBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns -1.
     * If the order of x-coordinates is descending or undetermined, the the method returns {@code getItemCount()}.
     * */
    public int getIndexOfGreatestXSmallerOrEqualTo(double upperBound);
    /**
     * Returns such an {@code index1} that {@code getX(index1) >=  lowerBound}  and for any other {@code index2} 
     * that satisfies  {@code getX(index2) >=  lowerBound} it holds that {@code getIndex(index2) >= getIndex(index1)}
     * 
     * If no point has an x coordinate greater or equal to lowerBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns 
     * {@code getItemCount()}. If the order of x-coordinates is descending or undetermined, the the method returns -1
     * */
    public int getIndexOfSmallestXGreaterOrEqualTo(double lowerBound);

    //also includes points for which x == lowerBound or x == upperBound
    public int getIndexCountBoundedBy(double lowerBound, double upperBound);
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x);
    public double[] getPoint(int item);
    public double[] getXCoordinatesCopy();
    public double[] getXCoordinatesCopy(int from, int to);
    public double[] getYCoordinatesCopy();
    public double[] getYCoordinatesCopy(int from, int to);
    public double[] getXCoordinates();
    public double[] getYCoordinates();
    public double[][] getXYViewCopy();
    public double[][] getXYViewCopy(int from, int to);
}

