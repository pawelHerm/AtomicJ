
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

package atomicJ.gui.rois;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;


import org.jfree.util.PublicCloneable;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.ShapeFactors;

public class ROIRectangleHole extends ROIRectangularShape implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Area datasetArea;
    private final Rectangle2D holeShape; 

    public ROIRectangleHole(Area datasetArea, Rectangle2D rectangle, Integer key, ROIStyle style) 
    {
        super(key, style);
        this.datasetArea = new Area(datasetArea);
        this.holeShape = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());    
    }

    public ROIRectangleHole(Area datasetArea, Rectangle2D rectangle, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.datasetArea = new Area(datasetArea);
        this.holeShape = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());    
    }

    public ROIRectangleHole(Area datasetArea, Rectangle2D rectangle, AffineTransform transform, Integer key, String label, ROIStyle style) 
    {
        super(transform, key, label, style);

        this.datasetArea = new Area(datasetArea);
        this.holeShape = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());    
    }

    public ROIRectangleHole(ROIRectangleHole that)
    {
        this(that, that.getStyle());
    }

    public ROIRectangleHole(ROIRectangleHole that, ROIStyle style)
    {
        super(that, style);     
        this.datasetArea = new Area(that.datasetArea);
        this.holeShape = new Rectangle2D.Double(that.holeShape.getX(), that.holeShape.getY(), that.holeShape.getWidth(), that.holeShape.getHeight());
    }

    public ROIRectangleHole(ROIRectangleHole that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);     
        this.datasetArea = new Area(that.datasetArea);
        this.holeShape = new Rectangle2D.Double(that.holeShape.getX(), that.holeShape.getY(), that.holeShape.getWidth(), that.holeShape.getHeight());
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        return ShapeFactors.getShapeFactorsForArbitraryShape(getROIShape(), flatness);
    }

    @Override
    public ROIRectangleHole copy()
    {
        return new ROIRectangleHole(this);
    }

    @Override
    public ROIRectangleHole copy(ROIStyle style)
    {
        return new ROIRectangleHole(this, style);
    }

    @Override
    public ROIRectangleHole copy(ROIStyle style, Integer key, String label)
    {
        return new ROIRectangleHole(this, style, key, label);
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        return true;
    }

    @Override
    public Rectangle2D getNonTransformedModifiableShape()
    {
        return holeShape;
    }

    @Override
    public Shape getROIShape() 
    {
        Area roiShape = new Area(this.datasetArea);

        Shape hole = isTransformed() ? getTransform().createTransformedShape(this.holeShape) : this.holeShape;
        roiShape.subtract(new Area(hole));

        return roiShape;
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }
        return MouseInputModeStandard.RECTANGULAR_ROI;
    }

    @Override
    public int getPointsInsideCountUpperBound(ArraySupport2D grid)
    {
        return GridPositionCalculator.getInsidePointCountUpperBound(grid, getROIShape());
    }

    //almost all points inside ROIRectangle are outside the corresponding ROREctangleHole
    //except points that lie exactly at the boundary
    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient)
    {        
        if(isTransformed())
        {
            ROIPolygon.addPointsInside(new Path2D.Double(getROIShape()), grid, recepient);
            return;
        }

        double minX = holeShape.getMinX();
        double maxX = holeShape.getMaxX();

        double minY = holeShape.getMinY();
        double maxY = holeShape.getMaxY();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. just OUTSIDE the hole

        //minRow is -1 if the top boundary of the hole is above the upper edge of the data area
        int minRow = Math.max(-1, grid.getRowFloor(minY));

        //max row is rowCount if the borrom boundary of the hole is below the lower edge of the data area
        int maxRow = Math.min(rowCount, grid.getRowCeiling(maxY));

        int minColumn = Math.max(-1, grid.getColumnFloor(minX));
        int maxColumn = Math.min(columnCount, grid.getColumnCeiling(maxX));

        recepient.addBlock(0, minRow + 1, 0, columnCount);   
        recepient.addBlock(minRow + 1, maxRow, 0, minColumn + 1);
        recepient.addBlock(minRow + 1, maxRow, maxColumn, columnCount);
        recepient.addBlock(maxRow, rowCount, 0, columnCount);
    }

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        if(isTransformed())
        {
            ROIPolygon.addPointsOutside(new Path2D.Double(getROIShape()), grid, recepient);
            return;
        }

        double minX = holeShape.getMinX();
        double maxX = holeShape.getMaxX();

        double minY = holeShape.getMinY();
        double maxY = holeShape.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI, 
        //i.e. outside the hole shape

        //minRow is -1 when the  top boundary of the hole is above the whole image
        int minRow = Math.max(-1, grid.getRowFloor(minY));

        //maxRow is rowCount when the bottom boundary of the hole is below the whole image
        int maxRow = Math.min(grid.getRowCount(), grid.getRowCeiling(maxY));

        int minColumn = Math.max(-1, grid.getColumnFloor(minX));
        int maxColumn = Math.min(grid.getColumnCount(), grid.getColumnCeiling(maxX));

        recepient.addBlock(minRow + 1, maxRow, minColumn + 1, maxColumn); 
    }

    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {      
        ROIPolygon.dividePoints(new Path2D.Double(getROIShape()), grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
    }

    @Override
    public ROIProxy getProxy()
    {
        return new ROIRectangleHoleSerializationProxy(holeShape, datasetArea, getTransform(), getCustomLabel(), isFinished());
    }

    private static class ROIRectangleHoleSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Rectangle2D holeShape;
        private final Path2D datasetArea;
        private final AffineTransform transform;
        private final String customLabel;
        private final boolean finished;

        private ROIRectangleHoleSerializationProxy(Rectangle2D holeShape, Area datasetArea, AffineTransform transform, String customLabel,boolean finished)
        {
            this.holeShape = holeShape;
            this.datasetArea = new Path2D.Double(datasetArea); //we must use Path2D.Double, as area is not serializable
            this.transform = transform;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIRectangleHole recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();

            ROIRectangleHole roi = new ROIRectangleHole(new Area(datasetArea), holeShape, transform, key, label, roiStyle);
            roi.setFinished(finished);

            return roi;
        }
    }
}
