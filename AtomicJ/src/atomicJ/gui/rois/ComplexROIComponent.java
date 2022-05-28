
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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.PublicCloneable;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.region.HolesRegion;
import atomicJ.gui.rois.region.CompositeRegion;
import atomicJ.gui.rois.region.Region;
import atomicJ.gui.rois.region.ShapeRegion;
import atomicJ.gui.rois.region.WholePlaneRegion;

public class ComplexROIComponent extends ROIDrawable implements ROIComponent, Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private Region spatialRestriction = new WholePlaneRegion();
    private final Map<Object, ROIComponent> componentROIs = new LinkedHashMap<>();

    public ComplexROIComponent(Set<? extends ROIComponent> rois, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        for(ROIComponent roi : rois)
        {
            componentROIs.put(roi.getKey(), roi);
        }
    }

    public ComplexROIComponent(ComplexROIComponent that, ROIStyle style)
    {
        super(that, style);

        for(ROIComponent roi : componentROIs.values())
        {
            componentROIs.put(roi.getKey(), (ROIComponent) roi.copy());
        }
    }

    public ComplexROIComponent(ComplexROIComponent that)
    {
        this(that, that.getStyle());
    }

    public Region getSpatialRestriction()
    {
        return spatialRestriction;
    }

    @Override
    public void setSpatialRestriction(Region spatialRestrictionNew)
    {
        this.spatialRestriction = spatialRestrictionNew;
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned newAnchor = null;

        for(ROIComponent roi : componentROIs.values())
        {
            Region excludedShape = getExcludedShape(roi);
            if(!excludedShape.contains(dataPoint.getX(), dataPoint.getY()))
            {
                AnnotationAnchorSigned componentAnchor = roi.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);

                if(componentAnchor != null)
                {
                    newAnchor = new AnnotationAnchorWrappedSigned(componentAnchor, getKey());
                    break;
                }
            }
        }

        return newAnchor;
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        Object key = innerAnchor.getKey();
        Object outerKey = anchor.getKey();

        ROIComponent roi = componentROIs.get(key);

        if(roi != null)
        {
            AnnotationModificationOperation returnedOperation = roi.rotate(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);

            boolean effectorIsSource = returnedOperation.getAnchor() != null && Objects.equals(getKey(), outerKey);
            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        }     
        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);

        return modificationOperation;
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D caughtROICenter, Point2D caughtROICompositeCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;  
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        Object innerKey = innerAnchor.getKey();

        Object outerKey = anchor.getKey();

        ROIComponent roi = componentROIs.get(innerKey);

        if(roi != null)
        {
            AnnotationModificationOperation returnedOperation = roi.rotate(innerAnchor, modifierKeys, caughtROICenter,pressedPoint, startPoint, endPoint);

            boolean effectorIsSource = returnedOperation.getAnchor() != null && Objects.equals(getKey(), outerKey);

            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        }   

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);

        return modificationOperation;
    }

    @Override
    public AnnotationModificationOperation rotateAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        Object outerKey = anchor.getKey();

        for(ROIComponent r : componentROIs.values())
        {
            AnnotationModificationOperation returnedOperation = r.rotate(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = returnedOperation.getAnchor() != null && Objects.equals(getKey(), outerKey);
            newAnchor = (effectorIsSource) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        }

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
        return modificationOperation;       
    }

    @Override
    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor)
    {
        Point2D center = null; 

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        Object key = innerAnchor.getKey();

        ROIComponent roi = componentROIs.get(key);

        if(roi != null)
        {
            center = roi.getDefaultRotationCenter(innerAnchor);
        }        

        return center;
    }


    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        Object outerKey = anchor.getKey();
        Object innerKey = innerAnchor.getKey();

        ROIComponent roi = componentROIs.get(innerKey);

        if(roi != null)
        {
            AnnotationModificationOperation returnedOperation = roi.setPosition(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = returnedOperation.getAnchor() != null && Objects.equals(getKey(), outerKey);

            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        }      

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
        return modificationOperation; 
    }

    @Override
    public AnnotationModificationOperation setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        for(ROIComponent r : componentROIs.values())
        {
            AnnotationModificationOperation returnedOperation = r.setPositionInAll(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = returnedOperation.getAnchor() != null && Objects.equals(getKey(), anchor.getKey());

            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        }

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
        return modificationOperation; 
    }  

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        boolean modified = false;

        for(ROIComponent r : componentROIs.values())
        {
            modified = modified || r.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);
            if(modified)
            {
                break;
            }
        }

        return modified;
    }

    @Override
    public List<ROIDrawable> split(double[][] polylineVertices)
    {
        List<Path2D> paths = ROIPolygon.evaluateCrosssectioning(getROIShape(), polylineVertices);
        if(paths == null)
        {

            ROIDrawable roi = new GeneralUnionROI(getROIDrawables(), -1, "", getStyle());
            return Collections.<ROIDrawable>singletonList(roi);
        }

        List<ROIDrawable> splitROIs = new ArrayList<>();

        for(Path2D path : paths)
        {
            ROIPolygon r = new ROIPolygon(path, -1, getStyle());
            splitROIs.add(r);
        }

        return splitROIs;
    }

    @Override
    public ComplexROIComponent copy()
    {
        return new ComplexROIComponent(this);
    }

    @Override
    public ComplexROIComponent copy(ROIStyle style)
    {
        return new ComplexROIComponent(this, style);
    }

    @Override
    public ComplexROIComponent copy(ROIStyle style, Integer key, String label)
    {
        return new ComplexROIComponent(this, style);
    }


    @Override
    public boolean isCorrectlyConstructed()
    {
        boolean correctlyConstructed = true;
        for(ROIComponent roi : componentROIs.values())
        {
            correctlyConstructed = correctlyConstructed && roi.isCorrectlyConstructed();

            if(!correctlyConstructed)
            {
                break;
            }
        }
        return correctlyConstructed;
    }

    @Override
    public Shape getROIShape()
    {
        return buildROIShape();
    }

    private Shape buildROIShape()
    {
        Area shape = new Area();

        for(ROIComponent roi : componentROIs.values())
        {
            shape.add(new Area(roi.getROIShape()));
        }

        return shape;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {
        super.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);

        for(ROIComponent roi : componentROIs.values())
        {
            Region excludedShape = getExcludedShape(roi);
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, excludedShape);
            }
        }
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {
        for(ROIComponent roi : componentROIs.values())
        {
            Region excludedShape = getExcludedShape(roi);
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, excludedShape);
            }
        }
    }

    private Region getExcludedShape(ROI roi)
    {
        List<ROIComponent> componentROIsCopy = new ArrayList<>(componentROIs.values());
        componentROIsCopy.remove(roi);

        Area shape = new Area();

        for(ROI otherRoi : componentROIsCopy)
        {
            shape.add(new Area(otherRoi.getROIShape()));
        }

        Region excludedShape = new CompositeRegion(new ShapeRegion(shape), new HolesRegion(spatialRestriction));
        return excludedShape;
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }
        return MouseInputModeStandard.POLYGON_ROI;
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
        return GridPositionCalculator.getInsidePointCountUpperBound(grid, getROIShape());
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
    public List<ROIDrawable> getROIDrawables() 
    {
        List<ROIDrawable> roiDrawable = new ArrayList<>();
        roiDrawable.add(this);

        for(ROIComponent rc : componentROIs.values())
        {
            roiDrawable.addAll(rc.getROIDrawables());
        }

        return roiDrawable;
    }

    @Override
    public ROIProxy getProxy() {
        // TODO Auto-generated method stub
        return null;
    }
}
