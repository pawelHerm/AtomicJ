
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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.ShapeFactors;
import atomicJ.sources.IdentityTag;

public class ROIComposite implements ROI
{
    private String label;

    private final Area shape;
    private final Object key;

    public ROIComposite(Object key)
    {	
        this.shape = new Area();
        this.key = key;
        this.label = key.toString();
    }

    public ROIComposite(Collection<? extends Shape> shapeComponents, Object key)
    {
        Area area = new Area();

        for(Shape shape : shapeComponents)
        {
            area.add(new Area(shape));
        }

        this.shape = area;
        this.key = key;
        this.label = key.toString();
    }

    public ROIComposite(ROIComposite other, Object key)
    {
        this.shape = new Area(other.shape);
        this.key = key;
        this.label = key.toString();
    }

    private ROIComposite(Shape shape, Object key, String label)
    {
        this.shape = new Area(shape);
        this.key = key;
        this.label = label;
    }

    @Override
    public ROIComposite getRotatedCopy(double angle, double anchorX, double anchorY)
    {
        AffineTransform rotationTransform = AffineTransform.getRotateInstance(angle, anchorX, anchorY);        
        Shape rotatedROIShape = rotationTransform.createTransformedShape(this.shape);

        return new ROIComposite(rotatedROIShape, this.key, this.label);
    }

    @Override
    public IdentityTag getIdentityTag()
    {
        IdentityTag keyLabelObject = new IdentityTag(getKey(), getLabel());
        return keyLabelObject;
    }

    @Override
    public ROIComposite copy()
    {
        return new ROIComposite(this, key);
    }

    public static ROIComposite getROIForShapes(Collection<? extends Shape> shapes, Object key)
    {
        ROIComposite roi = new ROIComposite(shapes, key);
        return roi;
    }

    public static ROIComposite getROIForRois(Collection<? extends ROI> rois, Object key)
    {
        List<Shape> shapeComponents = new ArrayList<>();

        for(ROI roi : rois)
        {            
            Shape shape = roi.getROIShape();

            shapeComponents.add(shape);
        }

        ROIComposite roi = new ROIComposite(shapeComponents, key);

        return roi;
    }

    public static ROIComposite getROIForRois(Map<Object, ? extends ROI> rois, Object key)
    {
        List<Shape> shapeComponents = new ArrayList<>();
        for(Entry<Object, ? extends ROI> entry : rois.entrySet())
        {
            ROI roi = entry.getValue();
            Shape shape = roi.getROIShape();
            shapeComponents.add(shape);
        }

        ROIComposite roi = new ROIComposite(shapeComponents, key);
        return roi;
    }

    @Override	
    public Shape getROIShape() 
    {
        return shape;
    }

    @Override
    public Object getKey() 
    {
        return key;
    }

    @Override
    public boolean contains(Point2D p)
    {
        return shape.contains(p);
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness) 
    {
        return ShapeFactors.getShapeFactorsForArbitraryShape(getROIShape(), flatness);
    }

    @Override
    public boolean equalsUpToStyle(ROI that)
    {
        if(that == null)
        {
            return false;
        }

        boolean equalsUpToStyle = Objects.equals(getROIShape(), that.getROIShape());
        return equalsUpToStyle;
    }

    protected void addEveryPoint(ArraySupport2D grid, GridPointRecepient recepient)
    {
        recepient.addBlock(0, grid.getRowCount(), 0, grid.getColumnCount());
    }

    @Override
    public void addPoints(ArraySupport2D grid, ROIRelativePosition position, GridPointRecepient recepient)
    {
        if(ROIRelativePosition.INSIDE.equals(position))
        {
            addPointsInside(grid, recepient);
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            addPointsOutside(grid, recepient);
        }
        else if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            addEveryPoint(grid, recepient);
        }
    }
    @Override
    public int getPointsInsideCountUpperBound(ArraySupport2D grid)
    {
        return GridPositionCalculator.getInsidePointCountUpperBound(grid, shape);
    }

    @Override
    public int getPointsOutsideCountUpperBound(ArraySupport2D grid)
    {
        return grid.getItemCount();
    }

    @Override
    public int getPointCountUpperBound(ArraySupport2D grid, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position)) 
        {
            return grid.getItemCount();
        }
        else if(ROIRelativePosition.INSIDE.equals(position))
        {
            return getPointsInsideCountUpperBound(grid);
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            return getPointsOutsideCountUpperBound(grid);
        }
        else
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        GridPositionCalculator positionCalculator = new GridPositionCalculator(25, 25);
        positionCalculator.addPointsInside(grid, this, recepient);
    }

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        GridPositionCalculator positionCalculator = new GridPositionCalculator(25, 25);
        positionCalculator.addPointsOutside(grid, this, recepient);
    }
    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        GridBiPositionCalculator calculator = new GridBiPositionCalculator(25, 25);
        calculator.dividePoints(grid, this, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String labelNew) 
    {   
        this.label = labelNew;
    }
}
