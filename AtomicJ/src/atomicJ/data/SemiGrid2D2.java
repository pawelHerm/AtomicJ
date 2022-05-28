
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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;


public class SemiGrid2D2 implements ArraySupport2D
{   
    private final DataAxis1D xAxis;
    private final DataAxis1D yAxis;

    //Immutable class
    public SemiGrid2D2(DataAxis1D xAxis, DataAxis1D yAxis)
    {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    @Override
    public DataAxis1D getXAxis()
    {
        return xAxis;
    }

    @Override
    public DataAxis1D getYAxis()
    {
        return yAxis;
    }

    @Override
    public boolean isWithinGridArea(double x, double y)
    {
        boolean within = xAxis.isWithinDomain(x) && yAxis.isWithinDomain(y);      
        return within;
    }

    @Override
    public int getInsidePointCount(Rectangle2D rectangle)
    {  
        int count = xAxis.getInsideIndicesCount(rectangle.getMinX(), rectangle.getMaxX())*yAxis.getInsideIndicesCount(rectangle.getMinY(), rectangle.getMaxY());

        return count;
    }

    @Override
    public int getXLineInsidePointCount(Rectangle2D rectangle)
    {
        return xAxis.getInsideIndicesCount(rectangle.getMinX(), rectangle.getMaxX());
    }

    @Override
    public int getYLineInsidePointCount(Rectangle2D rectangle)
    {
        return yAxis.getInsideIndicesCount(rectangle.getMinY(), rectangle.getMaxY());
    }

    @Override
    public boolean areQuantitiesEqual()
    {
        boolean equal = Objects.equals(xAxis.getQuantity(), yAxis.getQuantity());
        return equal;
    }

    @Override
    public Quantity getXQuantity()
    {
        return xAxis.getQuantity();
    }

    @Override
    public Quantity getYQuantity()
    {
        return yAxis.getQuantity();
    }

    @Override
    public Point2D getPointFlattenedWithFullReturn(int flattenedPosition)
    {
        int columnCount = xAxis.getIndexCount();

        int row = flattenedPosition/columnCount;
        int column = flattenedPosition%columnCount;

        return getPoint(row, column);
    }


    @Override
    public Point2D getPointFlattenedBackedAndForth(int flattenedPosition)
    {
        int columnCount = xAxis.getIndexCount();

        int row = flattenedPosition/columnCount;

        boolean rowEven = row%2 == 0;
        int columnRaw = flattenedPosition%columnCount;

        int column = rowEven ? columnRaw : columnCount - 1- columnRaw;

        return getPoint(row, column);
    }

    @Override
    public Point2D getPoint(int row, int column)
    {
        double x = xAxis.getArgumentVal(column);
        double y = yAxis.getArgumentVal(row);

        Point2D point = new Point2D.Double(x, y);

        return point;
    }

    @Override
    public Point2D getCornerPoint(int row, int column, int cornerIndex)
    {
        //south-west
        if(cornerIndex == 0)
        {
            double x = xAxis.getArgumentVal(column) - 0.5*xAxis.getIncrementToPreviousIndex(column);
            double y = yAxis.getArgumentVal(row) + 0.5*xAxis.getIncrementToNextIndex(row);

            Point2D point = new Point2D.Double(x, y);

            return point;
        }

        //north-west
        if(cornerIndex == 1)
        {
            double x = xAxis.getArgumentVal(column) - 0.5*xAxis.getIncrementToPreviousIndex(column);
            double y = yAxis.getArgumentVal(row) - 0.5*xAxis.getIncrementToPreviousIndex(row);

            Point2D point = new Point2D.Double(x, y);

            return point;
        }

        //north-east
        if(cornerIndex == 2)
        {
            double x = xAxis.getArgumentVal(column) + 0.5*xAxis.getIncrementToNextIndex(column);
            double y =  yAxis.getArgumentVal(row) - 0.5*xAxis.getIncrementToPreviousIndex(row);

            Point2D point = new Point2D.Double(x, y);

            return point;
        }

        //south-east
        if(cornerIndex == 3)
        {
            double x = xAxis.getArgumentVal(column) + 0.5*xAxis.getIncrementToNextIndex(column);
            double y = yAxis.getArgumentVal(row) + 0.5*xAxis.getIncrementToNextIndex(row);

            Point2D point = new Point2D.Double(x, y);

            return point;
        }
        throw new IllegalArgumentException("'cornerIndex' should be between 0 and 3 inclusive");
    }

    @Override
    public double getX(double column)
    {
        double x = xAxis.getArgumentVal(column);				
        return x;
    }

    @Override
    public double getY(double row)
    {
        return yAxis.getArgumentVal(row);
    }

    // the returned index  i >= 0 i i <= rowCount - 1
    @Override
    public int getRow(double y)
    {
        return yAxis.getIndex(y);
    }

    @Override
    public double getFractionalRow(double y)
    {
        return yAxis.getFractionalIndex(y);

    }

    @Override
    public int getRowFloorWithinBounds(double y)
    {
        return yAxis.getIndexFloorWithinBounds(y);
    }

    @Override
    public int getRowFloor(double y)
    {
        return yAxis.getIndexFloor(y);
    }

    @Override
    public int getRowCeilingWithinBounds(double y)
    {
        return yAxis.getIndexCeilingWithinBounds(y);
    }

    @Override
    public int getRowCeiling(double y)
    {
        return yAxis.getIndexCeiling(y);
    }

    @Override
    public int getRow(Point2D p)
    {
        double y = p.getY();
        int row = getRow(y);

        return row;
    }

    @Override
    public int getColumn(double x)
    {
        int column = xAxis.getIndex(x);

        return column;
    }

    @Override
    public double getFractionalColumn(double x)
    {
        double column = xAxis.getFractionalIndex(x);
        return column;
    }

    @Override
    public int getColumnFloorWithinBounds(double x)
    {
        int column = xAxis.getIndexFloorWithinBounds(x);

        return column;
    }

    @Override
    public int getColumnFloor(double x)
    {
        int column = xAxis.getIndexFloor(x);

        return column;
    }

    @Override
    public int getColumnCeilingWithinBounds(double x)
    {
        int column = xAxis.getIndexCeilingWithinBounds(x);

        return column;
    }

    @Override
    public int getColumnCeiling(double x)
    {
        int column = xAxis.getIndexCeiling(x);

        return column;
    }

    @Override
    public int getColumn(Point2D p)
    {
        double x = p.getX();
        int column = xAxis.getIndex(x);

        return column;
    }

    @Override
    public double getXDataDensity()
    {
        return xAxis.getDataDensity();
    }

    @Override
    public double getYDataDensity()
    {        
        return yAxis.getDataDensity();
    }

    @Override
    public double getGridDensity()
    {
        return Math.max(getXDataDensity(), getYDataDensity());
    }

    @Override
    public GridIndex getGridIndex(Point2D p)
    {
        double x = p.getX();
        int column = getColumn(x);

        double y = p.getY();
        int row = getRow(y);

        GridIndex index = new GridIndex(row, column);

        return index;
    }

    @Override
    public GridBlock getInscribedBlock(Rectangle2D r)
    {	
        double x0 = r.getX();
        double y0 = r.getY();
        double x1 = x0 + r.getWidth();
        double y1 = y0 + r.getHeight();

        int rowMin = getRowCeiling(y0);
        int rowMax = getRowFloor(y1);
        int columnMin = getColumnCeiling(x0);
        int columnMax = getColumnFloor(x1);

        int rowCount = Math.max(0, rowMax - rowMin + 1);
        int columnCount = Math.max(0, columnMax - columnMin + 1);

        GridBlock block = new GridBlock(rowMin, columnMin, rowCount, columnCount);
        return block;
    }

    @Override
    public Rectangle2D getRectangle(int originRow, int originColumn, int pixelWidth, int pixelHeight)
    {
        double x0 = xAxis.getArgumentVal(originColumn);
        double y0 = yAxis.getArgumentVal(originRow);

        double width = xAxis.getArgumentVal(originColumn + pixelWidth) - x0;
        double height = yAxis.getArgumentVal(originRow + pixelHeight) - y0;

        return new Rectangle2D.Double(x0, y0, width, height);
    }

    @Override
    public GridBlock getInscribedBlock(ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return new GridBlock(0, 0, yAxis.getIndexCount(), xAxis.getIndexCount());
        }
        else if(ROIRelativePosition.INSIDE.equals(position))
        {
            return getInscribedBlock(roi.getROIShape().getBounds2D());
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            Area wholeaArea = new Area(getDataArea());
            wholeaArea.subtract(new Area(roi.getROIShape()));

            return getInscribedBlock(wholeaArea.getBounds2D());
        }

        else
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }
    }

    @Override
    public Rectangle2D getDataArea()
    {
        Rectangle2D gridArea = new Rectangle2D.Double(xAxis.getOrigin(), yAxis.getOrigin(), getDomainLength(), getRangeLength());
        return gridArea;  
    }

    @Override
    public double[] getNodeXs()
    {
        return xAxis.getNodes();
    }

    @Override
    public double[] getNodeYs()
    {
        return yAxis.getNodes();
    }

    @Override
    public double getXOrigin()
    {
        return xAxis.getOrigin();
    }

    @Override
    public double getYOrigin()
    {
        return yAxis.getOrigin();
    }

    @Override
    public int getRowCount()
    {
        return yAxis.getIndexCount();
    }

    @Override
    public int getColumnCount()
    {
        return  xAxis.getIndexCount();
    }

    @Override
    public int getItemCount()
    {
        int count = xAxis.getIndexCount()*yAxis.getIndexCount();
        return count;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = getItemCount() == 0;
        return empty;
    }

    @Override
    public double getXMinimum()
    {
        return xAxis.getMinimum();
    }

    @Override
    public double getXCenter()
    {
        return xAxis.getCenter();
    }

    @Override
    public double getXMaximum()
    {
        return xAxis.getMaximum();
    }

    @Override
    public double getYMinimum()
    {
        return yAxis.getMinimum();
    }

    @Override
    public double getYCenter()
    {
        return yAxis.getCenter();
    }

    @Override
    public double getYMaximum()
    {
        return yAxis.getMaximum();
    }

    @Override
    public double getDomainLength()
    {
        return xAxis.getLength();
    }

    @Override
    public double getRangeLength()
    {
        return yAxis.getLength();
    }

    public boolean isEqualUpToPrefixes(SemiGrid2D2 other)
    {
        if(!this.xAxis.isEqualUpToPrefixes(other.xAxis))
        {
            return false;
        }
        if(!this.yAxis.isEqualUpToPrefixes(other.yAxis))
        {
            return false;
        }

        return true;
    }
}
