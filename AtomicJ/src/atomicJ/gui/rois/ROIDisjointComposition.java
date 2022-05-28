
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
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.ShapeFactors;
import atomicJ.sources.IdentityTag;

public class ROIDisjointComposition implements ROI
{
    private String label;
    private final Object key;

    private final Area shape;
    private final List<ROI> componentROIs = new ArrayList<>();

    public ROIDisjointComposition(Object key)
    {	
        this.shape = new Area();
        this.key = key;
        this.label = key.toString();
    }

    public ROIDisjointComposition(Collection<? extends ROI> components, Object key)
    {
        this(components, key, key.toString());
    }

    public ROIDisjointComposition(Collection<? extends ROI> components, Object key, String label)
    {
        Area area = new Area();

        for(ROI roi : components)
        {
            area.add(new Area(roi.getROIShape()));
            componentROIs.add(roi.copy());
        }

        this.shape = area;
        this.key = key;
        this.label = label;
    }

    public ROIDisjointComposition(ROIDisjointComposition other, Object key)
    {
        for(ROI roi : other.componentROIs)
        {
            this.componentROIs.add(roi.copy());
        }

        this.shape = new Area(other.shape);
        this.key = key;
        this.label = key.toString();
    }


    //disjoint ROIs are still disjoint after rotation
    @Override
    public ROIDisjointComposition getRotatedCopy(double angle, double anchorX, double anchorY)
    {
        List<ROI> rotatedComponentROIs = new ArrayList<>();

        for(ROI componentROI : componentROIs)
        {
            rotatedComponentROIs.add(componentROI.getRotatedCopy(angle, anchorX, anchorY));
        }

        ROIDisjointComposition rotated = new ROIDisjointComposition(rotatedComponentROIs, this.key, this.label);

        return rotated;
    }

    @Override
    public ROIDisjointComposition copy()
    {
        return new ROIDisjointComposition(this, key);
    }

    public static ROIDisjointComposition getROIForShapes(Collection<? extends ROI> shapes, Object key)
    {
        ROIDisjointComposition roi = new ROIDisjointComposition(shapes, key);
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
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String labelNew) 
    {
        this.label = labelNew;
    }

    @Override
    public IdentityTag getIdentityTag()
    {
        IdentityTag keyLabelObject = new IdentityTag(getKey(), getLabel());
        return keyLabelObject;
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
        int count = 0;

        for(ROI roi : componentROIs)
        {
            count += GridPositionCalculator.getInsidePointCountUpperBound(grid, roi.getROIShape());
        }

        int upperBound = Math.min(count, grid.getItemCount());

        return upperBound;
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
        for(ROI roi : componentROIs)
        {
            roi.addPointsInside(grid, recepient);
        }
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
}
