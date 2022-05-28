
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
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;


import org.jfree.util.PublicCloneable;

import atomicJ.data.ArraySupport2D;
import atomicJ.data.Grid2D;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.ShapeFactors;

public class ROIRectangle extends ROIRectangularShape implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Rectangle2D nonTransformedShape; 

    public ROIRectangle(Rectangle2D rectangle, Integer key, ROIStyle style) 
    {
        super(key, style);

        this.nonTransformedShape = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    public ROIRectangle(Rectangle2D rectangle, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.nonTransformedShape = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());    
    }

    public ROIRectangle(Rectangle2D rectangle, AffineTransform transform, Integer key, String label, ROIStyle style) 
    {
        super(transform, key, label, style);

        this.nonTransformedShape = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());    
    }

    public ROIRectangle(ROIRectangle that)
    {
        this(that, that.getStyle());
    }

    public ROIRectangle(ROIRectangle that, ROIStyle style)
    {
        super(that, style);

        this.nonTransformedShape = new Rectangle2D.Double(that.nonTransformedShape.getX(), that.nonTransformedShape.getY(), that.nonTransformedShape.getWidth(), that.nonTransformedShape.getHeight());
    }

    public ROIRectangle(ROIRectangle that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);

        this.nonTransformedShape = new Rectangle2D.Double(that.nonTransformedShape.getX(), that.nonTransformedShape.getY(), that.nonTransformedShape.getWidth(), that.nonTransformedShape.getHeight());
    }


    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        ShapeFactors shapeFactors = isTransformed() ? ShapeFactors.getShapeFactorsForArbitraryShape(getROIShape(), flatness) : ShapeFactors.getShapeFactors(nonTransformedShape);
        return shapeFactors;
    }

    @Override
    public ROIRectangle copy()
    {
        return new ROIRectangle(this);
    }

    @Override
    public ROIRectangle copy(ROIStyle style)
    {
        return new ROIRectangle(this, style);
    }

    @Override
    public ROIRectangle copy(ROIStyle style, Integer key, String label)
    {
        return new ROIRectangle(this, style, key, label);
    }


    @Override
    public ROIDrawable getConvexHull(Integer key, String label) 
    {
        return copy(getStyle(), key, label);
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        return true;
    }

    @Override
    public Rectangle2D getNonTransformedModifiableShape()
    {
        return nonTransformedShape;
    }

    @Override
    public Shape getROIShape() 
    {
        if(isTransformed())
        {
            AffineTransform tr = getTransform();
            return tr.createTransformedShape(nonTransformedShape);
        }

        return nonTransformedShape;
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

    public int getInsideXLinePointCountUpperBound(Grid2D grid)
    {
        return GridPositionCalculator.getInsideXLinePointCountUpperBound(grid, getROIShape());
    }

    public int getInsideYLinePointCountUpperBound(Grid2D grid)
    {
        return GridPositionCalculator.getInsideYLinePointCountUpperBound(grid, getROIShape());
    }

    @Override
    public int getPointsOutsideCountUpperBound(ArraySupport2D grid)
    {
        if(isTransformed())
        {
            return grid.getItemCount();
        }

        int itemCount = grid.getItemCount();
        int insideCount = grid.getInsidePointCount(nonTransformedShape);
        int outsideCount = itemCount - insideCount;

        return outsideCount;
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient)
    {        
        if(isTransformed())
        {
            ROIPolygon.addPointsInside(new Path2D.Double(getROIShape()), grid, recepient);
            return;
        }

        double minX = nonTransformedShape.getMinX();
        double maxX = nonTransformedShape.getMaxX();

        double minY = nonTransformedShape.getMinY();
        double maxY = nonTransformedShape.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int minRow = Math.max(0, grid.getRowCeiling(minY));
        int maxRow = Math.min(grid.getRowCount() - 1, grid.getRowFloor(maxY));

        int minColumn = Math.max(0, grid.getColumnCeiling(minX));
        int maxColumn = Math.min(grid.getColumnCount() - 1, grid.getColumnFloor(maxX));

        recepient.addBlock(minRow, maxRow + 1, minColumn, maxColumn +1);   
    }   

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        if(isTransformed())
        {
            ROIPolygon.addPointsOutside(new GeneralPath(getROIShape()), grid, recepient);
            return;
        }

        double minX = nonTransformedShape.getMinX();
        double maxX = nonTransformedShape.getMaxX();

        double minY = nonTransformedShape.getMinY();
        double maxY = nonTransformedShape.getMaxY();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int minRow = Math.max(0, grid.getRowCeiling(minY));
        int maxRow = Math.min(rowCount - 1, grid.getRowFloor(maxY));

        int minColumn = Math.max(0, grid.getColumnCeiling(minX));
        int maxColumn = Math.min(columnCount - 1, grid.getColumnFloor(maxX));

        recepient.addBlock(0, minRow, 0, columnCount);   
        recepient.addBlock(minRow, maxRow + 1, 0, minColumn);
        recepient.addBlock(minRow, maxRow + 1, maxColumn + 1, columnCount);
        recepient.addBlock(maxRow + 1, rowCount, 0, columnCount);
    }

    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {        
        if(isTransformed())
        {
            ROIPolygon.dividePoints(new GeneralPath(getROIShape()), grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
            return;
        }

        double minX = nonTransformedShape.getMinX();
        double maxX = nonTransformedShape.getMaxX();

        double minY = nonTransformedShape.getMinY();
        double maxY = nonTransformedShape.getMaxY();

        //minColumn,maxColumn, minRow and maxRow are inside the ROI
        int roiMinColumn = Math.max(imageMinColumn, grid.getColumnCeiling(minX));
        int roiMaxColumn = Math.min(imageMaxColumn, grid.getColumnFloor(maxX));
        int roiMinRow = Math.max(imageMinRow, grid.getRowCeiling(minY));
        int roiMaxRow = Math.min(imageMaxRow, grid.getRowFloor(maxY));


        //adds points that are outside bounding box

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow); i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }

        for(int i = Math.max(imageMinRow, roiMinRow); i <= roiMaxRow; i++)
        {
            for(int j = imageMinColumn; j < Math.min(roiMinColumn, imageMaxColumn + 1); j++)
            {
                recepient.addPointOutside(i, j);
            }  

            for(int j = Math.max(roiMaxColumn + 1, imageMinColumn); j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            } 
        }

        for(int i = Math.max(roiMaxRow + 1, imageMinRow); i <= imageMaxRow; i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }


        //adds points that are inside bounding box

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            for(int j = roiMinColumn; j<= roiMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }          
        }
    }

    @Override
    public ROIProxy getProxy()
    {
        return new ROIRectangleProxy(nonTransformedShape, getTransform(), getCustomLabel(), isFinished());
    }

    public static class ROIRectangleProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Rectangle2D roiShape;
        private final AffineTransform transform;
        private final String customLabel;
        private final boolean finished;

        public ROIRectangleProxy(Rectangle2D roiShape, AffineTransform transform, String customLabel, boolean finished)
        {
            this.roiShape = roiShape;
            this.transform = transform;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIRectangle recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            ROIRectangle roi = new ROIRectangle(roiShape, transform, key, label, roiStyle);
            roi.setFinished(finished);

            return roi;
        }
    }
}
