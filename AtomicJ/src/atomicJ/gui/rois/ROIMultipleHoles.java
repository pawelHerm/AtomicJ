
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
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import org.jfree.util.PublicCloneable;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.ShapeFactors;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;
import atomicJ.gui.profile.AnnotationModificationOperation;

public class ROIMultipleHoles extends ROIRectangle implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final List<ROIDrawable> roiHoles = new ArrayList<>();

    public ROIMultipleHoles(Area datasetArea, Integer key, ROIStyle style) 
    {
        super(datasetArea.getBounds2D(), key, style);
    }

    public ROIMultipleHoles(Area datasetArea, Integer key, String label, ROIStyle style) 
    {
        super(datasetArea.getBounds2D(), key, label, style);
    }

    public ROIMultipleHoles(ROIMultipleHoles that)
    {
        this(that, that.getStyle());
    }

    public ROIMultipleHoles(ROIMultipleHoles that, ROIStyle style)
    {
        super(that, style); 

        for(ROIDrawable hole : that.roiHoles)
        {
            ROIDrawable holeCopy = hole.copy();
            this.roiHoles.add(holeCopy);
        }
    }

    public ROIMultipleHoles(ROIMultipleHoles that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label); 

        for(ROIDrawable hole : that.roiHoles)
        {
            ROIDrawable holeCopy = hole.copy();
            this.roiHoles.add(holeCopy);
        }
    }

    @Override
    public ROIMultipleHoles copy()
    {
        return new ROIMultipleHoles(this);
    }

    @Override
    public ROIMultipleHoles copy(ROIStyle style)
    {
        return new ROIMultipleHoles(this, style);
    }

    @Override
    public ROIMultipleHoles copy(ROIStyle style, Integer key, String label)
    {
        return new ROIMultipleHoles(this, style, key, label);
    }

    @Override
    public Rectangle2D getNonTransformedModifiableShape()
    {
        return super.getNonTransformedModifiableShape();
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        return ShapeFactors.getShapeFactorsForArbitraryShape(getROIShape(), flatness);
    }

    @Override
    public Shape getROIShape() 
    {
        Area dataArea = new Area(super.getNonTransformedModifiableShape());

        for(ROIDrawable hole : roiHoles)
        {
            Area holeArea = new Area(hole.getROIShape());
            dataArea.subtract(holeArea);
        }

        return dataArea;
    }


    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned caughtAnchor = null;

        ROIDrawable hole = getROIForRectangle(dataRectangle);

        if(hole != null)
        {
            AnnotationAnchorSigned anch = hole.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
            if(anch != null)
            {
                caughtAnchor = new AnnotationAnchorWrappedSigned(anch, getKey());
            }
        }        

        if(caughtAnchor == null)
        {
            caughtAnchor = super.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
        }

        return caughtAnchor;

    }



    @Override
    public boolean isCorrectlyConstructed()
    {
        boolean correctlyConstructed = super.isCorrectlyConstructed();

        for(ROIDrawable hole : roiHoles)
        {
            correctlyConstructed = correctlyConstructed && hole.isCorrectlyConstructed();
        }
        return correctlyConstructed;
    }

    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;
        if(anchor == null)
        {
            return null;
        }		

        ROIDrawable roi = getROIForPoint(startPoint);

        if(roi != null)
        {
            AnnotationModificationOperation returnedOperation = roi.setPosition(anchor, modifierKeys, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = (returnedOperation.getAnchor() != null && Objects.equals(anchor.getKey(), getKey()));
            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
            fireAnnotationChanged();
        }

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);

        return modificationOperation;
    }

    public ROIDrawable getROIForPoint(Point2D dataPoint)
    {
        Rectangle2D hotArea = getDataSquare(dataPoint, 0.01);   

        for(ROIDrawable hole: roiHoles)
        {           
            boolean isClicked = hole.isBoundaryClicked(hotArea);
            if(isClicked)
            {
                return hole;
            }
        }
        return null;    
    }

    public ROIDrawable getROIForRectangle(Rectangle2D dataRectangle)
    {
        for(ROIDrawable hole: roiHoles)
        {           
            boolean isClicked = hole.isBoundaryClicked(dataRectangle);
            if(isClicked)
            {
                return hole;
            }
        }
        return null;    
    }

    protected Rectangle2D getDataSquare(Point2D  dataPoint, double axisFraction)
    {

        Rectangle2D dataArea = getNonTransformedModifiableShape();

        double xRangeLength = dataArea.getWidth();
        double yRangeLength = dataArea.getHeight();

        double xRadius = axisFraction*xRangeLength;
        double yRadius = axisFraction*yRangeLength;

        Rectangle2D square = new Rectangle2D.Double(dataPoint.getX() - xRadius, dataPoint.getY() - yRadius, 2*xRadius, 2*yRadius);
        return square;
    }
}
