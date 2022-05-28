
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Validation;


public class Grid2D implements ArraySupport2D
{
    private static final double TOLERANCE = 1e-10;

    private final double xIncrement;
    private final double yIncrement;
    private final double xOrigin;
    private final double yOrigin;

    private final int rowCount;
    private final int columnCount;
    private final double[] nodeXs;
    private final double[] nodeYs;
    private final Quantity xQuantity;
    private final Quantity yQuantity;

    //Immutable class

    //if there is only 1 column/row, it does not matter what is the value of this.incrementX as long as it is not NaN. It can't be NaN, because Grid2D will use calculatiosn like origin + 0*NaN.
    //if rowCount < 2 and xIncrement is NaN, then Grid2D will change xIncrement to 1
    //if columnCount < 2 and yIncrement is NaN, the Grid2D will change yIncrement to 1 
    public Grid2D(double xIncrement, double yIncrement, double xOrigin, double yOrigin, int rowCount, int columnCount, Quantity xQuantity, Quantity yQuantity)
    {
        Validation.requireNonNegativeParameterName(rowCount, "rowCount");
        Validation.requireNonNegativeParameterName(columnCount, "columnCount");

        Validation.requireNonNegativeParameterName(xIncrement, "xIncrement");
        Validation.requireNonNegativeParameterName(yIncrement, "yIncrement");

        this.xIncrement = (Double.isNaN(xIncrement) && columnCount < 2) ? 1 :xIncrement;
        this.yIncrement = (Double.isNaN(yIncrement) && rowCount < 2) ? 1 : yIncrement;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.nodeXs = new double[columnCount];
        this.nodeYs = new double[rowCount];

        for(int i = 0; i<columnCount; i++)
        {
            nodeXs[i] = xOrigin + i*xIncrement;
        }
        for(int j = 0; j<rowCount; j++)
        {
            nodeYs[j] = yOrigin + j*yIncrement;
        }

        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;
    }


    public Grid2D(Grid1D xAxis, Grid1D yAxis)
    {
        this.xIncrement = xAxis.getIncrement();
        this.yIncrement = yAxis.getIncrement();
        this.xOrigin = xAxis.getOrigin();
        this.yOrigin = yAxis.getIncrement();
        this.rowCount = yAxis.getIndexCount();
        this.columnCount = xAxis.getIndexCount();
        this.nodeXs = xAxis.getNodes();
        this.nodeYs = yAxis.getNodes();
        this.xQuantity = xAxis.getQuantity();
        this.yQuantity = yAxis.getQuantity();
    }

    @Override
    public Grid1D getXAxis()
    {
        return new Grid1D(xIncrement, xOrigin, columnCount, xQuantity);
    }

    @Override
    public Grid1D getYAxis()
    {
        return new Grid1D(yIncrement, yOrigin, rowCount, yQuantity);
    }

    public static Grid2D getGrid(List<Point2D> nodePoints, double tolerance)
    {
        if(nodePoints.isEmpty())
        {
            return null;
        }

        int n = nodePoints.size();

        double[] xs = new double[n];
        double[] ys = new double[n];

        for(int i = 0; i<n;i++)
        {
            Point2D p = nodePoints.get(i);

            xs[i] = p.getX();
            ys[i] = p.getY();
        }

        Arrays.sort(xs);

        double xOrigin = xs[0];
        double xIncrement = getPossibleIncrement(xOrigin, xs, tolerance);  
        int rowCount = xIncrement > tolerance && n > 1 ?  (int)Math.rint((xs[n - 1] - xOrigin)/xIncrement) + 1 : 1;

        Arrays.sort(ys);

        double yOrigin = ys[0];
        double yIncrement = getPossibleIncrement(yOrigin, ys, tolerance);      
        int columnCount = yIncrement > tolerance && n > 1 ?  (int)Math.rint((ys[n - 1] - yOrigin)/yIncrement) + 1 : 1;

        Grid2D grid = new Grid2D(xIncrement, yIncrement, xOrigin, yOrigin, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        boolean fillsNodes = grid.canFillNodes(nodePoints, tolerance);

        if(fillsNodes)
        {
            return grid;
        }

        return null;
    }


    public static double getPossibleIncrement(double origin, double[] values, double tolerance)
    {
        int n = values.length;

        for(int i = 0; i<n; i++)
        {
            double x = values[i];
            double diff = x - origin;
            if(diff > tolerance)
            {
                return diff;
            }
        }

        return 0;
    }

    public boolean canFillNodes(Collection<Point2D> points, double tolerance)
    {        
        boolean[][] gridFilling = new boolean[rowCount][columnCount];

        for(Point2D p : points)
        {
            double x = p.getX();
            double y = p.getY();

            double columnPrecise = (x - xOrigin)/xIncrement;
            int column = (int)Math.rint(columnPrecise);
            double columnImprecision = Math.abs(column - columnPrecise);

            if(columnImprecision > tolerance)
            {
                return false;
            }

            double rowPrecise = (y - yOrigin)/yIncrement;
            int row = (int)Math.rint(rowPrecise);
            double rowImprecision = Math.abs(row - rowPrecise);

            if(rowImprecision > tolerance)
            {
                return false;
            }

            if(column < 0 || column >= columnCount || row < 0 || row >= rowCount)
            {
                return false;
            }

            boolean alreadyPresent = gridFilling[row][column];

            if(alreadyPresent)
            {
                return false;
            }

            gridFilling[row][column] = true;
        }

        boolean onlyTrue = ArrayUtilities.allElementsEqual(gridFilling, true);

        return onlyTrue;
    }

    @Override
    public boolean isWithinGridArea(double x, double y)
    {
        boolean within = (xOrigin - x <= TOLERANCE && x - nodeXs[columnCount - 1] <= TOLERANCE && yOrigin - y <=  TOLERANCE && y - nodeYs[rowCount - 1] <= TOLERANCE);      
        return within;
    }

    @Override
    public int getInsidePointCount(Rectangle2D rectangle)
    {
        double minX = rectangle.getMinX();
        double maxX = rectangle.getMaxX();

        double minY = rectangle.getMinY();
        double maxY = rectangle.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int minRow = Math.max(0, getRowCeiling(minY));
        int maxRow = Math.min(getRowCount() - 1, getRowFloor(maxY));

        int minColumn = Math.max(0, getColumnCeiling(minX));
        int maxColumn = Math.min(getColumnCount() - 1, getColumnFloor(maxX));

        int count = (maxRow + 1 - minRow) > 0 && (maxColumn + 1 - minColumn) > 0 ? Math.max(0, (maxRow + 1 - minRow)*(maxColumn + 1 - minColumn)) : 0;

        return count;
    }

    @Override
    public int getXLineInsidePointCount(Rectangle2D rectangle)
    {
        double minX = rectangle.getMinX();
        double maxX = rectangle.getMaxX();

        int minColumn = Math.max(0, getColumnCeiling(minX));
        int maxColumn = Math.min(getColumnCount() - 1, getColumnFloor(maxX));

        int count = Math.max(0, (maxColumn + 1 - minColumn));

        return count;
    }

    @Override
    public int getYLineInsidePointCount(Rectangle2D rectangle)
    {
        double minY = rectangle.getMinY();
        double maxY = rectangle.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int minRow = Math.max(0, getRowCeiling(minY));
        int maxRow = Math.min(getRowCount() - 1, getRowFloor(maxY));

        int count = Math.max(0, (maxRow + 1 - minRow));

        return count;
    }

    @Override
    public boolean areQuantitiesEqual()
    {
        boolean equal = xQuantity.equals(yQuantity);
        return equal;
    }

    public double getIncrementRatio()
    {
        PrefixedUnit xQuantityUnit = xQuantity.getUnit();
        PrefixedUnit yQuantityUnit = yQuantity.getUnit();

        if(xQuantityUnit.isCompatible(yQuantityUnit))
        {
            UnitExpression yLength = new UnitExpression(yIncrement, yQuantityUnit);
            UnitExpression xLength = new UnitExpression(xIncrement, xQuantityUnit);

            double ratio = xLength.divide(yLength).getValue();
            return ratio;
        }
        else
        {
            double ratio = xIncrement/yIncrement;

            return ratio;
        }
    }

    public Grid2D changeDensity(int rowCountNew, int columnCountNew)
    {	    
        double xIncrementNew = (xIncrement*(columnCount - 1))/(columnCountNew - 1);
        double yIncrementNew = (yIncrement*(rowCount - 1))/(rowCountNew - 1);

        Grid2D newGrid = new Grid2D(xIncrementNew, yIncrementNew, xOrigin, yOrigin, rowCountNew, columnCountNew, xQuantity, yQuantity);

        return newGrid;
    }

    @Override
    public Quantity getXQuantity()
    {
        return xQuantity;
    }

    @Override
    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    @Override
    public double getGridDensity()
    {
        return Math.max(xIncrement, yIncrement);
    }

    @Override
    public Point2D getPointFlattenedWithFullReturn(int flattenedPosition)
    {
        int row = flattenedPosition/columnCount;
        int column = flattenedPosition%columnCount;

        return getPoint(row, column);
    }


    @Override
    public Point2D getPointFlattenedBackedAndForth(int flattenedPosition)
    {
        int row = flattenedPosition/columnCount;

        boolean rowEven = row%2 == 0;
        int columnRaw = flattenedPosition%columnCount;

        int column = rowEven ? columnRaw : columnCount - 1- columnRaw;

        return getPoint(row, column);
    }

    @Override
    public Point2D getPoint(int row, int column)
    {
        double x = xOrigin + column*xIncrement;
        double y = yOrigin + row*yIncrement;

        Point2D point = new Point2D.Double(x, y);

        return point;
    }

    @Override
    public Point2D getCornerPoint(int row, int column, int cornerIndex)
    {
        //south-west
        if(cornerIndex == 0)
        {
            double x = xOrigin + (column - 0.5)*xIncrement;
            double y = yOrigin + (row + 0.5)*yIncrement;

            Point2D point = new Point2D.Double(x, y);

            return point;
        }

        //north-west
        if(cornerIndex == 1)
        {
            double x = xOrigin + (column - 0.5)*xIncrement;
            double y = yOrigin + (row - 0.5)*yIncrement;

            Point2D point = new Point2D.Double(x, y);

            return point;
        }

        //north-east
        if(cornerIndex == 2)
        {
            double x = xOrigin + (column + 0.5)*xIncrement;
            double y = yOrigin + (row - 0.5)*yIncrement;

            Point2D point = new Point2D.Double(x, y);

            return point;

        }

        //south-east
        if(cornerIndex == 3)
        {
            double x = xOrigin + (column + 0.5)*xIncrement;
            double y = yOrigin + (row + 0.5)*yIncrement;

            Point2D point = new Point2D.Double(x, y);

            return point;
        }
        throw new IllegalArgumentException("'cornerIndex' should be between 0 and 3 inclusive");
    }

    @Override
    public double getX(double column)
    {
        double x = xOrigin + column*xIncrement;				
        return x;
    }

    @Override
    public double getY(double row)
    {
        double y = yOrigin + row*yIncrement;
        return y;
    }

    @Override
    public int getRow(double y)
    {
        int row = (int)Math.rint((y - yOrigin)/yIncrement);
        return row;
    }

    @Override
    public double getFractionalRow(double y)
    {
        double row = (y - yOrigin)/yIncrement;
        return row;
    }

    @Override
    public int getRowFloorWithinBounds(double y)
    {
        int row = (int)Math.min(rowCount - 1, Math.max(0, Math.floor((y - yOrigin)/yIncrement)));
        return row;
    }

    @Override
    public int getRowFloor(double x)
    {
        int row = (int)Math.min(rowCount - 1, Math.max(-1, Math.floor((x - yOrigin)/yIncrement)));

        return row;
    }

    @Override
    public int getRowCeilingWithinBounds(double y)
    {
        int row = (int)Math.max(0, Math.min(rowCount - 1, Math.ceil((y - yOrigin)/yIncrement)));
        return row;
    }

    @Override
    public int getRowCeiling(double x)
    {
        int row = (int)Math.max(0, Math.min(rowCount, Math.ceil((x - yOrigin)/yIncrement)));

        return row;
    }

    @Override
    public int getRow(Point2D p)
    {
        double y = p.getY();
        int row = (int)Math.rint((y - yOrigin)/yIncrement);

        return row;
    }

    @Override
    public int getColumn(double x)
    {
        int column = (int)Math.rint((x - xOrigin)/xIncrement);

        return column;
    }

    @Override
    public double getFractionalColumn(double x)
    {
        double column = (x - xOrigin)/xIncrement;
        return column;
    }


    @Override
    public int getColumnFloorWithinBounds(double x)
    {
        int column = (int)Math.min(columnCount - 1, Math.max(0, Math.floor((x - xOrigin)/xIncrement)));

        return column;
    }

    @Override
    public int getColumnFloor(double x)
    {
        int column = (int)Math.min(columnCount - 1, Math.max(-1, Math.floor((x - xOrigin)/xIncrement)));

        return column;
    }

    @Override
    public int getColumnCeilingWithinBounds(double x)
    {
        int column = (int)Math.max(0, Math.min(columnCount - 1, Math.ceil((x - xOrigin)/xIncrement)));

        return column;
    }   

    @Override
    public int getColumnCeiling(double x)
    {
        int column = (int)Math.max(0, Math.min(columnCount, Math.ceil((x - xOrigin)/xIncrement)));

        return column;
    }

    @Override
    public int getColumn(Point2D p)
    {
        double x = p.getX();
        int column = (int)Math.rint((x - xOrigin)/xIncrement);

        return column;
    }

    @Override
    public GridIndex getGridIndex(Point2D p)
    {
        double x = p.getX();
        int column = (int)Math.rint((x - xOrigin)/xIncrement);

        double y = p.getY();
        int row = (int)Math.rint((y - yOrigin)/yIncrement);

        GridIndex index = new GridIndex(row, column);

        return index;
    }

    @Override
    public Rectangle2D getRectangle(int originRow, int originColumn, int pixelWidth, int pixelHeight)
    {
        double x0 = nodeXs[originColumn];
        double y0 = nodeYs[originRow];

        double width = nodeXs[originColumn + pixelWidth] - x0;
        double height = nodeYs[originRow + pixelHeight] - y0;

        return new Rectangle2D.Double(x0, y0, width, height);
    }

    @Override
    public GridBlock getInscribedBlock(Rectangle2D r)
    {	
        double x0 = r.getX();
        double y0 = r.getY();
        double x1 = x0 + r.getWidth();
        double y1 = y0 + r.getHeight();

        int rowMin = (int)Math.max(0, Math.ceil((y0 - yOrigin)/yIncrement));
        int rowMax = (int)Math.min(rowCount - 1, Math.floor((y1 - yOrigin)/yIncrement));
        int columnMin = (int)Math.max(0, Math.ceil((x0 - xOrigin)/xIncrement));
        int columnMax = (int)Math.min(columnCount - 1, Math.floor((x1 - xOrigin)/xIncrement));

        int rowCount = Math.max(0, rowMax - rowMin + 1);
        int columnCount = Math.max(0, columnMax - columnMin + 1);

        GridBlock block = new GridBlock(rowMin, columnMin, rowCount, columnCount);
        return block;
    }

    @Override
    public GridBlock getInscribedBlock(ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return new GridBlock(0, 0, rowCount, columnCount);
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
        Rectangle2D gridArea = new Rectangle2D.Double(xOrigin, yOrigin, getDomainLength(), getRangeLength());
        return gridArea;  
    }

    @Override
    public double[] getNodeXs()
    {
        return Arrays.copyOf(nodeXs, nodeXs.length);
    }

    @Override
    public double[] getNodeYs()
    {
        return Arrays.copyOf(nodeYs, nodeYs.length);
    }

    @Override
    public double getXDataDensity()
    {
        return xIncrement;
    }

    @Override
    public double getYDataDensity()
    {
        return yIncrement;
    }

    public double getXIncrement()
    {
        return xIncrement;
    }

    public double getYIncrement()
    {
        return yIncrement;
    }

    @Override
    public double getXOrigin()
    {
        return xOrigin;
    }

    @Override
    public double getYOrigin()
    {
        return yOrigin;
    }

    @Override
    public int getRowCount()
    {
        return rowCount;
    }

    @Override
    public int getColumnCount()
    {
        return  columnCount;
    }

    @Override
    public int getItemCount()
    {
        int count = rowCount*columnCount;
        return count;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = rowCount*columnCount == 0;
        return empty;
    }

    @Override
    public double getXMinimum()
    {
        return nodeXs[0];
    }

    @Override
    public double getXCenter()
    {
        double min = nodeXs[0];
        double max = nodeXs[nodeXs.length - 1];
        double center = min + (max - min)/2.;
        return center;
    }

    @Override
    public double getXMaximum()
    {
        return nodeXs[nodeXs.length - 1];
    }

    @Override
    public double getYMinimum()
    {
        return nodeYs[0];
    }

    @Override
    public double getYCenter()
    {
        double min = nodeYs[0];
        double max = nodeYs[nodeYs.length - 1];
        double center = min + (max - min)/2.;
        return center;
    }

    @Override
    public double getYMaximum()
    {
        return nodeYs[nodeYs.length - 1];
    }

    @Override
    public double getDomainLength()
    {
        double minimum = nodeXs[0];
        double maximum = nodeXs[nodeXs.length - 1];
        double length = maximum - minimum;
        return length;
    }

    @Override
    public double getRangeLength()
    {
        double minimum = nodeYs[0];
        double maximum = nodeYs[nodeYs.length - 1];
        double length = maximum - minimum;
        return length;
    }

    public boolean isEqualUpToPrefixes(Grid2D other)
    {
        if(!this.xQuantity.getUnit().isCompatible(other.xQuantity.getUnit()))
        {
            return false;
        }
        if(!this.yQuantity.getUnit().isCompatible(other.yQuantity.getUnit()))
        {
            return false;
        }

        double xFactor = other.xQuantity.getUnit().getConversionFactorTo(this.xQuantity.getUnit());
        double yFactor = other.yQuantity.getUnit().getConversionFactorTo(this.yQuantity.getUnit());

        if(!MathUtilities.equalWithinTolerance(this.xIncrement, xFactor*other.xIncrement, TOLERANCE))
        {
            return false;
        }
        if(!MathUtilities.equalWithinTolerance(this.yIncrement, yFactor*other.yIncrement, TOLERANCE))
        {
            return false;
        }        
        if(!MathUtilities.equalWithinTolerance(this.xOrigin, xFactor*other.xOrigin, TOLERANCE))
        {
            return false;
        } 
        if(!MathUtilities.equalWithinTolerance(this.yOrigin, yFactor*other.yOrigin, TOLERANCE))
        {
            return false;
        } 
        if(this.rowCount != other.rowCount)
        {
            return false;
        }
        if(this.columnCount != other.columnCount)
        {
            return false;
        }

        return true;
    }
}
