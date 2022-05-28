
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
import java.awt.geom.Rectangle2D;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;


public interface ArraySupport2D 
{
    public DataAxis1D getXAxis();
    public DataAxis1D getYAxis();
    public boolean isWithinGridArea(double x, double y);
    public int getInsidePointCount(Rectangle2D rectangle);
    public int getXLineInsidePointCount(Rectangle2D rectangle);
    public int getYLineInsidePointCount(Rectangle2D rectangle);
    public boolean areQuantitiesEqual();
    public Quantity getXQuantity();
    public Quantity getYQuantity();
    public Point2D getPointFlattenedWithFullReturn(int flattenedPosition);
    public Point2D getPointFlattenedBackedAndForth(int flattenedPosition);
    public Point2D getPoint(int row, int column);
    public Point2D getCornerPoint(int row, int column, int cornerIndex);
    public double getGridDensity();
    public double getXDataDensity();
    public double getYDataDensity();
    public double getX(double column);
    public double getY(double row);
    public int getRow(double y);
    public double getFractionalRow(double y);
    public int getRowFloorWithinBounds(double y);
    public int getRowFloor(double y);
    public int getRowCeilingWithinBounds(double y);
    public int getRowCeiling(double y);
    public int getRow(Point2D p);
    public int getColumn(double x);
    public double getFractionalColumn(double x);
    public int getColumnFloorWithinBounds(double x);
    public int getColumnFloor(double x);
    public int getColumnCeilingWithinBounds(double x);
    public int getColumnCeiling(double x);
    public int getColumn(Point2D p);
    public Rectangle2D getRectangle(int originRow, int originColumn, int pixelWidth, int pixelHeight);
    public GridIndex getGridIndex(Point2D p);
    public GridBlock getInscribedBlock(Rectangle2D r);
    public GridBlock getInscribedBlock(ROI roi, ROIRelativePosition position);
    public Rectangle2D getDataArea();
    public double[] getNodeXs();
    public double[] getNodeYs();
    public double getXOrigin();
    public double getYOrigin();
    public int getRowCount();
    public int getColumnCount();
    public int getItemCount();
    public boolean isEmpty();
    public double getXMinimum();
    public double getXCenter();
    public double getXMaximum();
    public double getYMinimum();
    public double getYCenter();
    public double getYMaximum();
    public double getDomainLength();
    public double getRangeLength();
}
