
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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.annotations.CornerAnnotationAnchor;
import atomicJ.gui.annotations.EdgeAnnotationAnchor;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.profile.ProfilePolyLine;

public abstract class ROIRectangularShape extends ROIDrawable implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private static final double MINIMAL_DIMENSION = 10e-9;

    private AffineTransform currentTransform = new AffineTransform();

    public ROIRectangularShape(Integer key,  ROIStyle style) 
    {
        super(key, style);
    }

    public ROIRectangularShape(Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);
    }

    public ROIRectangularShape(AffineTransform transform, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.currentTransform = (transform != null) ? new AffineTransform(transform) : currentTransform;
    }

    public ROIRectangularShape(ROIRectangularShape that)
    {
        this(that, that.getStyle());

        this.currentTransform = new AffineTransform(that.currentTransform);
    }

    public ROIRectangularShape(ROIRectangularShape that, ROIStyle style)
    {
        super(that, style);  	

        this.currentTransform = new AffineTransform(that.currentTransform);
    }

    public ROIRectangularShape(ROIRectangularShape that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);   

        this.currentTransform = new AffineTransform(that.currentTransform);
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D probingRectangle)
    {
        RectangularShape rect = getNonTransformedModifiableShape();

        Point2D nwPoint = currentTransform.transform(new Point2D.Double(rect.getMinX(), rect.getMaxY()), null);
        Point2D nePoint = currentTransform.transform(new Point2D.Double(rect.getMaxX(), rect.getMaxY()), null);
        Point2D sePoint = currentTransform.transform(new Point2D.Double(rect.getMaxX(), rect.getMinY()), null);
        Point2D swPoint = currentTransform.transform(new Point2D.Double(rect.getMinX(), rect.getMinY()), null);

        for(CornerAnnotationAnchor cornerAnchor : CornerAnnotationAnchor.values())
        {
            if(cornerAnchor.isCornerCaught(nwPoint, nePoint, sePoint, swPoint, probingRectangle))
            {
                return new AnnotationAnchorSourceSigned(cornerAnchor, getKey());
            }
        }

        if(isLabelClicked(java2DPoint))
        {
            return new AnnotationAnchorSourceSigned(BasicAnnotationAnchor.LABEL, getKey());
        }

        Shape transformedRect = currentTransform.createTransformedShape(rect);

        if(transformedRect.intersects(probingRectangle))
        {
            double rWidth = probingRectangle.getWidth();
            double rHeight = probingRectangle.getHeight();

            double minNormalizedDistance = Double.POSITIVE_INFINITY;
            EdgeAnnotationAnchor closestEdgeAnchor = null;
            for(EdgeAnnotationAnchor edge : EdgeAnnotationAnchor.values())
            {
                double normalizedDistance = edge.getDistanceFromEdge(dataPoint, nwPoint, nePoint, sePoint, swPoint)/edge.getPerpendicularEdgeLength(rWidth, rHeight);
                if(normalizedDistance < minNormalizedDistance)
                {
                    minNormalizedDistance = normalizedDistance;
                    closestEdgeAnchor = edge;
                }
            }

            AnnotationAnchorCore anchor = (minNormalizedDistance <= 0.5) ? closestEdgeAnchor : BasicAnnotationAnchor.CENTER;

            return new AnnotationAnchorSourceSigned(anchor, getKey());
        }

        return null;
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle) 
    {
        return false;
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        return true;
    }

    public abstract RectangularShape getNonTransformedModifiableShape();

    public Shape getTransformedModifiableShape()
    {
        return currentTransform.createTransformedShape(getNonTransformedModifiableShape());
    }

    protected AffineTransform getTransform()
    {
        return new AffineTransform(currentTransform);
    }

    protected boolean isTransformed()
    {
        return !currentTransform.isIdentity();
    }  

    @Override
    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor)
    {
        Rectangle2D shape = getTransformedModifiableShape().getBounds2D();

        double rotCenterX = shape.getCenterX();
        double rotCenterY = shape.getCenterY();

        return new Point2D.Double(rotCenterX, rotCenterY);
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();

        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {                        
            double rotCenterX = rotationCenter.getX();
            double rotCenterY = rotationCenter.getY();

            double angleInRadians = Math.atan2(endPoint.getY() - rotCenterY, endPoint.getX() - rotCenterX) - Math.atan2(startPoint.getY() - rotCenterY, startPoint.getX() - rotCenterX);

            AffineTransform rotationTransform = AffineTransform.getRotateInstance(angleInRadians, rotCenterX, rotCenterY);
            //            Point2D finalPosition = rotationTransform.transform(startPoint, null);

            rotationTransform.concatenate(currentTransform);
            this.currentTransform = rotationTransform;

            fireAnnotationChanged();

            AnnotationAnchorSigned newAnchor = (ObjectUtilities.equal(getKey(), anchor.getKey())) ? new AnnotationAnchorSourceSigned(anchor.getCoreAnchor(), getKey()) : null;
            AnnotationModificationOperation currentModificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPoint);

            return currentModificationOperation;
        }

        return setPosition(anchor, modifierKeys, pressedPoint, startPoint, endPoint);
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D caughtROICenter, Point2D caughtROICompositeCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        return rotate(anchor, modifierKeys, caughtROICenter, pressedPoint, startPoint, endPoint);
    }

    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D previousPosition, Point2D endPoint)
    {
        if(anchor == null)
        {
            return null;
        }		

        if(isTransformed())
        {
            return setPositionOfTransformed(anchor, pressedPoint, previousPosition, endPoint, modifierKeys, true);
        }

        Point2D finalPosition = previousPosition;               

        AnnotationAnchorCore anchorCore = anchor.getCoreAnchor();
        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();

        if(coreAnchor instanceof EdgeAnnotationAnchor)
        {
            EdgeAnnotationAnchor mainEdgeAnchor = (EdgeAnnotationAnchor)coreAnchor;  
            double movementDistance = mainEdgeAnchor.getMovementDistance(getNonTransformedModifiableShape(), endPoint);

            RectangularShape modShape = getNonTransformedModifiableShape();
            double width = modShape.getWidth();
            double height = modShape.getHeight();

            double ratio = mainEdgeAnchor.getParrallelLength(width, height)/mainEdgeAnchor.getPerpendicularEdgeLength(width, height);

            if(modifierKeys.contains(ModifierKey.ALT_GRAPH) || (modifierKeys.contains(ModifierKey.ALT) && modifierKeys.contains(ModifierKey.CONTROL)))
            {
                mainEdgeAnchor.getOppositeEdge().moveEdge(getNonTransformedModifiableShape(), movementDistance, MINIMAL_DIMENSION);

                for(EdgeAnnotationAnchor edge : mainEdgeAnchor.getOrthogonalEdges())
                {
                    edge.moveEdge(getNonTransformedModifiableShape(), ratio*movementDistance, MINIMAL_DIMENSION);
                }
            }
            else if(modifierKeys.contains(ModifierKey.ALT))
            {               
                for(EdgeAnnotationAnchor edge : mainEdgeAnchor.getOrthogonalEdges())
                {
                    edge.moveEdge(getNonTransformedModifiableShape(), 0.5*ratio*movementDistance, MINIMAL_DIMENSION);
                }                
            }          
            else if(modifierKeys.contains(ModifierKey.CONTROL))
            {                             
                mainEdgeAnchor.getOppositeEdge().moveEdge(getNonTransformedModifiableShape(), movementDistance, MINIMAL_DIMENSION);
            }
            anchorCore = mainEdgeAnchor.moveEdge(getNonTransformedModifiableShape(), movementDistance, MINIMAL_DIMENSION);
            finalPosition = endPoint;
        }
        else if(coreAnchor instanceof CornerAnnotationAnchor) 
        {
            CornerAnnotationAnchor cornerAnchor = (CornerAnnotationAnchor)coreAnchor; 

            EdgeAnnotationAnchor mainEdgeX = cornerAnchor.getEdgeX();
            EdgeAnnotationAnchor mainEdgeY = cornerAnchor.getEdgeY();

            double movementXDistance = mainEdgeX.getMovementDistance(getNonTransformedModifiableShape(), endPoint);
            double movementYDistance = mainEdgeY.getMovementDistance(getNonTransformedModifiableShape(), endPoint);

            if(modifierKeys.contains(ModifierKey.ALT_GRAPH)|| (modifierKeys.contains(ModifierKey.ALT) && modifierKeys.contains(ModifierKey.CONTROL)) || modifierKeys.contains(ModifierKey.CONTROL))
            {
                movementYDistance = movementXDistance;

                mainEdgeX.getOppositeEdge().moveEdge(getNonTransformedModifiableShape(), movementXDistance, MINIMAL_DIMENSION);
                mainEdgeY.getOppositeEdge().moveEdge(getNonTransformedModifiableShape(), movementXDistance, MINIMAL_DIMENSION);
            }
            else if(modifierKeys.contains(ModifierKey.ALT))
            {
                movementYDistance = movementXDistance;
            }

            EdgeAnnotationAnchor edgeNewX = mainEdgeX.moveEdge(getNonTransformedModifiableShape(), movementXDistance, MINIMAL_DIMENSION);
            EdgeAnnotationAnchor edgeNewY = mainEdgeY.moveEdge(getNonTransformedModifiableShape(), movementYDistance, MINIMAL_DIMENSION);

            anchorCore = CornerAnnotationAnchor.getInstanceFor(edgeNewX, edgeNewY);
            finalPosition = endPoint;
        }
        else if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(),  pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);

            RectangularShape rshape = getNonTransformedModifiableShape();

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            double centerX = rshape.getCenterX() + tx;
            double centerY = rshape.getCenterY() + ty;
            double cornerX = rshape.getX() + tx;
            double cornerY = rshape.getY() + ty;

            rshape.setFrameFromCenter(centerX, centerY, cornerX, cornerY);
        }

        fireAnnotationChanged();

        AnnotationAnchorSigned returnedAnchor = ObjectUtilities.equal(anchor.getKey(), getKey()) ? new AnnotationAnchorSourceSigned(anchorCore, getKey()) : null;
        AnnotationModificationOperation currentModificationOperation = new AnnotationModificationOperation(returnedAnchor, pressedPoint, finalPosition);

        return currentModificationOperation;
    }

    private AnnotationModificationOperation setPositionOfTransformed(AnnotationAnchorSigned anchor, Point2D pressedPoint, Point2D previousPosition, Point2D endPoint, Set<ModifierKey> modifierKeys, boolean notify)
    {
        if(anchor == null)
        {
            return null;
        }       

        Point2D finalPosition = previousPosition;               

        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();

        RectangularShape rect = getNonTransformedModifiableShape();

        Point2D nwPoint = currentTransform.transform(new Point2D.Double(rect.getMinX(), rect.getMaxY()), null);
        Point2D nePoint = currentTransform.transform(new Point2D.Double(rect.getMaxX(), rect.getMaxY()), null);
        Point2D sePoint = currentTransform.transform(new Point2D.Double(rect.getMaxX(), rect.getMinY()), null);
        Point2D swPoint = currentTransform.transform(new Point2D.Double(rect.getMinX(), rect.getMinY()), null);

        if(coreAnchor instanceof EdgeAnnotationAnchor)
        {
            EdgeAnnotationAnchor edgeAnchor = (EdgeAnnotationAnchor)coreAnchor;

            AffineTransform tr = edgeAnchor.getEdgeMovementTransformation(nwPoint, nePoint, sePoint, swPoint, endPoint, MINIMAL_DIMENSION);
            currentTransform.preConcatenate(tr);

            finalPosition = endPoint;
        }
        else if(coreAnchor instanceof CornerAnnotationAnchor) 
        {            
            CornerAnnotationAnchor cornerAnchor = (CornerAnnotationAnchor)coreAnchor; 

            EdgeAnnotationAnchor edgeX = cornerAnchor.getEdgeX();
            EdgeAnnotationAnchor edgeY = cornerAnchor.getEdgeY();   

            currentTransform.preConcatenate(edgeX.getEdgeMovementTransformation(nwPoint, nePoint, sePoint, swPoint, endPoint, MINIMAL_DIMENSION));
            currentTransform.preConcatenate(edgeY.getEdgeMovementTransformation(nwPoint, nePoint, sePoint, swPoint, endPoint, MINIMAL_DIMENSION));

            finalPosition = endPoint;
        }
        else if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(), pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            currentTransform.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
        }

        if(notify)
        {
            fireAnnotationChanged();
        }

        AnnotationAnchorSigned returnedAnchor = ObjectUtilities.equal(anchor.getKey(), getKey()) ? new AnnotationAnchorSourceSigned(anchor.getCoreAnchor(), getKey()) : null;
        AnnotationModificationOperation currentModificationOperation = new AnnotationModificationOperation(returnedAnchor, pressedPoint, finalPosition);

        return currentModificationOperation;
    }

    @Override
    public List<ROIDrawable> split(double[][] polylineVertices)
    {
        List<Path2D> paths = ROIPolygon.evaluateCrosssectioning(getROIShape(), polylineVertices);
        if(paths == null)
        {
            return Collections.<ROIDrawable>singletonList(this);
        }

        List<ROIDrawable> splitROIs = new ArrayList<>();

        for(Path2D path : paths)
        {
            ROIPolygon r = new ROIPolygon(path, -1, getStyle());
            splitROIs.add(r);
        }

        return splitROIs;
    }
}
