
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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.SimpleROIComponent.ROISimpleComponentSerializationProxy;
import atomicJ.gui.rois.region.CompositeRegion;
import atomicJ.gui.rois.region.HolesRegion;
import atomicJ.gui.rois.region.Region;
import atomicJ.gui.rois.region.SerializableRegionInformation;
import atomicJ.gui.rois.region.WholePlaneRegion;

public class GeneralUnionROI extends ROIDrawable implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Map<Object, SimpleROIComponent> simpleComponents = new LinkedHashMap<>();   
    private List<ROIComponent> componentROIs = new ArrayList<>();

    private Region spatialRestriction = new WholePlaneRegion();

    public GeneralUnionROI(Collection<ROIDrawable> rois, Integer unionKey, String unionLabel, ROIStyle style) 
    {
        super(unionKey, unionLabel, style);

        for(ROIDrawable roi : rois)
        {     
            SimpleROIComponent simpleComponent = new SimpleROIComponent(roi, unionKey, unionLabel);
            this.simpleComponents.put(simpleComponent.getKey(), simpleComponent);
        }

        buildComponentROIs();

        setFinished(true);
    }

    public GeneralUnionROI(GeneralUnionROI that)
    {
        this(that, that.getStyle());
    }

    public GeneralUnionROI(GeneralUnionROI that, ROIStyle style)
    {
        this(that, style, that.getKey(), that.getLabel());
    }

    public GeneralUnionROI(GeneralUnionROI that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);

        for(SimpleROIComponent roi : that.simpleComponents.values())
        {
            SimpleROIComponent componentCopy = roi.copy(style, key, label);
            this.simpleComponents.put(componentCopy.getKey(), componentCopy);
        }

        this.spatialRestriction = that.spatialRestriction;

        buildComponentROIs();

        setFinished(true);
    }

    private GeneralUnionROI(Collection<SimpleROIComponent> components, Region spatialRestriction, ROIStyle style, Integer key, String label)
    {
        super(key, label, style);

        for(SimpleROIComponent roi : components)
        {
            SimpleROIComponent componentCopy = roi.copy(style, key, label);
            this.simpleComponents.put(componentCopy.getKey(), componentCopy);
        }

        this.spatialRestriction = spatialRestriction;

        buildComponentROIs();

        setFinished(true);
    }

    //    public GeneralUnionROI subtract(ROIDrawable toSubtract, Integer key, String label, ROIStyle style)
    //    {
    //        Collection<ROIDrawable> componentsNew = new ArrayList<>();
    //        
    //        for(SimpleROIComponent sc : simpleComponents.values())
    //        {
    //            Integer componentKey = sc.getKey();
    //            DifferenceROI scNew = new DifferenceROI(sc.getROIDrawable().copy(style, componentKey, label), Collections.singletonList(toSubtract.copy(style, componentKey, label)), componentKey, label, style);
    //            componentsNew.add(scNew);
    //        }
    //        
    //        return new GeneralUnionROI(componentsNew, key, label, style);
    //    }



    public void setSpatialRestriction(Region spatialRestrictionNew)
    {
        this.spatialRestriction = spatialRestrictionNew;

        refreshSpatialRestriction();
    }

    private void refreshSpatialRestriction()
    {
        for(ROIComponent component : componentROIs)
        {
            component.setSpatialRestriction(spatialRestriction);
        }
    }

    private void buildComponentROIs()
    {
        this.componentROIs = new ArrayList<>();

        Set<Set<SimpleROIComponent>> partitionROI = ROIUtilities.partitionConnectedROIs(simpleComponents.values());

        for(Set<SimpleROIComponent> overlappingROIs : partitionROI)
        {
            if(overlappingROIs.isEmpty())
            {
                continue;
            }

            if(overlappingROIs.size() == 1)
            {
                ROIComponent roi = overlappingROIs.iterator().next();
                componentROIs.add(roi);
            }
            else
            {
                ComplexROIComponent roi = new ComplexROIComponent(overlappingROIs, getKey(), getLabel(), getStyle());
                roi.setFinished(true);

                componentROIs.add(roi);
            }
        }  

        refreshSpatialRestriction();
    }


    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned anchor = null;

        for(ROIComponent roi : simpleComponents.values())
        {
            AnnotationAnchorSigned currentAnchor = roi.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);

            if(currentAnchor != null)
            {
                anchor = new AnnotationAnchorWrappedSigned(currentAnchor, getKey());
                break;
            }
        }

        return anchor;
    }

    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {        
        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();

        Point2D endPointNew = endPoint;

        boolean keysCompatible = Objects.equals(getKey(), anchor.getKey());

        AnnotationAnchorSigned newAnchor = null;

        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor) && modifierKeys.contains(ModifierKey.CONTROL))
        {            
            AnnotationModificationOperation returnedOperation = setPositionInAll(anchor, modifierKeys, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = returnedOperation.getAnchor() != null && keysCompatible;
            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;

            AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
            return modificationOperation;  
        }


        ROIComponent roi = simpleComponents.get(innerAnchor.getKey());

        if(roi != null)
        {
            AnnotationModificationOperation returnedOperation = roi.setPosition(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = returnedOperation.getAnchor() != null && keysCompatible;

            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;

            buildComponentROIs();
            fireAnnotationChanged();
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

        ROIComponent roi = simpleComponents.get(key);

        if(roi != null)
        {
            center = roi.getDefaultRotationCenter(innerAnchor);
        }      

        return center;
    }

    @Override
    public Point2D getDefaultCompositeRotationCenter(AnnotationAnchorSigned anchor)
    {
        Rectangle2D bounds = getROIShape().getBounds2D();

        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();

        return new Point2D.Double(centerX, centerY);
    }

    public AnnotationModificationOperation setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {        
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        Object key = innerAnchor.getKey();
        for(ROIComponent r : simpleComponents.values())
        {
            AnnotationModificationOperation returnedOperation = r.setPositionInAll(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = returnedOperation.getAnchor() != null && Objects.equals(anchor.getKey(), key);

            newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        }

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
        return modificationOperation;     
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {        
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        Object key = innerAnchor.getKey();

        ROIComponent roi = simpleComponents.get(key);

        if(roi != null)
        {
            AnnotationModificationOperation returnedOperation = roi.rotate(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = (returnedOperation.getAnchor() != null && Objects.equals(anchor.getKey(), getKey()));
            newAnchor =  effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
            endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;

            buildComponentROIs();
            fireAnnotationChanged();
        }

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
        return modificationOperation; 
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D caughtROICenter, Point2D caughtROICompositeCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    { 
        AnnotationAnchorSigned currentAnchor = null;
        Point2D endPointNew = endPoint;
        AnnotationModificationOperation returnedOperation = null;

        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();

        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor) && modifierKeys.contains(ModifierKey.CONTROL))
        {
            returnedOperation = rotateAll(anchor, modifierKeys, caughtROICompositeCenter, pressedPoint, startPoint, endPoint);
        }
        else if(BasicAnnotationAnchor.CENTER.equals(coreAnchor) && modifierKeys.contains(ModifierKey.SHIFT))
        {
            returnedOperation = rotateAll(anchor, modifierKeys, caughtROICenter, pressedPoint,startPoint, endPoint);
        }
        else
        {
            AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

            ROIComponent roi = simpleComponents.get(innerAnchor.getKey());

            if(roi != null)
            {
                returnedOperation = roi.rotate(innerAnchor, modifierKeys, caughtROICenter, pressedPoint, startPoint, endPoint);

                buildComponentROIs();
                fireAnnotationChanged();
            }  
        }

        boolean effectorIsSource = (returnedOperation != null && returnedOperation.getAnchor() != null && Objects.equals(anchor.getKey(), getKey()));
        AnnotationAnchorSigned newAnchor = (effectorIsSource) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        endPointNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, endPointNew);
        return modificationOperation; 
    }

    public AnnotationModificationOperation rotateAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        Point2D endPointNew = endPoint;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        for(ROIComponent r : simpleComponents.values())
        {
            AnnotationModificationOperation returnedOperation = r.rotateAll(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);
            boolean effectorIsSource = (returnedOperation.getAnchor() != null && Objects.equals(anchor.getKey(), getKey()));
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

        for(ROIComponent r : simpleComponents.values())
        {
            modified = modified || r.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);
            if(modified)
            {
                buildComponentROIs();
                fireAnnotationChanged();
                break;
            }
        }

        return modified;
    }

    @Override
    public List<ROIDrawable> split(double[][] polylineVertices)
    {
        List<ROIDrawable> roisNew = new ArrayList<>();

        for(ROIComponent component : componentROIs)
        {
            roisNew.addAll(component.split(polylineVertices));
        }

        return roisNew;
    }

    @Override
    public GeneralUnionROI copy()
    {
        return new GeneralUnionROI(this);
    }

    @Override
    public GeneralUnionROI copy(ROIStyle style)
    {
        return new GeneralUnionROI(this, style);
    }

    @Override
    public GeneralUnionROI copy(ROIStyle style, Integer key, String label) {
        return new GeneralUnionROI(this, style, key, label);
    }


    public List<ROIDrawable> copyIndividualROIs(ROIStyle style)
    {
        List<ROIDrawable> copies = new ArrayList<>();

        for(ROIComponent roi : simpleComponents.values())
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                copies.add(dr.copy(style));
            }
        }

        return copies;

    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        boolean correctlyConstructed = true;
        for(ROIComponent roi : componentROIs)
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
        Area roiShape = new Area();

        for(ROIComponent roi : simpleComponents.values())
        {
            roiShape.add(new Area(roi.getROIShape()));
        }

        return roiShape;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {        
        for(ROIComponent roi : componentROIs)
        {
            roi.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
        }
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
            PlotRenderingInfo info)
    {
        for(ROIComponent roi : componentROIs)
        {
            roi.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
        }
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape) 
    {
        for(ROIComponent roi : componentROIs)
        {
            roi.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, new CompositeRegion(excludedShape, new HolesRegion(spatialRestriction)));
        }
    }

    @Override
    public boolean isLabelClicked(Point2D java2DPoint)
    {
        boolean labelClicked = false;

        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                labelClicked = labelClicked || dr.isLabelClicked(java2DPoint);
                if(labelClicked)
                {
                    return labelClicked;
                }
            }
        }

        return labelClicked;
    }

    @Override
    public void setLabel(String labelNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabel(labelNew, notify);
            }
        }

        super.setLabel(labelNew, notify);
    }

    @Override
    public void setVisible(boolean visibleNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setVisible(visibleNew, notify);
            }
        }

        super.setVisible(visibleNew, notify);
    }

    @Override
    public void setHighlighted(boolean highlightedNew, boolean notify)
    {        
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setHighlighted(highlightedNew, notify);
            }
        }

        super.setHighlighted(highlightedNew, notify);
    }

    @Override
    public void setLabelUnfinishedVisible(boolean visible, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabelUnfinishedVisible(visible, notify);
            }
        }

        super.setLabelUnfinishedVisible(visible, notify);
    }

    @Override
    public void setLabelFinishedVisible(boolean visible, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabelFinishedVisible(visible, notify);
            }
        }

        super.setLabelFinishedVisible(visible, notify);
    }

    @Override
    public void setPaintLabelFinished(Paint paintNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setPaintLabelFinished(paintNew, notify);
            }
        }

        super.setPaintLabelFinished(paintNew, notify);
    }

    @Override
    public void setPaintLabelUnfinished(Paint paintNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setPaintLabelUnfinished(paintNew, notify);
            }
        }

        super.setPaintLabelUnfinished(paintNew, notify);
    }

    @Override
    public void setPaintFinished(Paint paintNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setPaintFinished(paintNew, notify);
            }
        }

        super.setPaintFinished(paintNew, notify);
    }

    @Override
    public void setPaintUnfinished(Paint paintNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setPaintUnfinished(paintNew, notify);
            }
        }

        super.setPaintUnfinished(paintNew, notify);
    }

    @Override
    public void setStrokeFinished(Stroke strokeNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setStrokeFinished(strokeNew, notify);
            }
        }

        super.setStrokeFinished(strokeNew, notify);
    }

    @Override
    public void setStrokeUnfinished(Stroke strokeNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setStrokeUnfinished(strokeNew, notify);
            }
        }

        super.setStrokeUnfinished(strokeNew, notify);
    }

    @Override
    public void setLabelFontUnfinished(Font fontNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabelFontUnfinished(fontNew, notify);
            }
        }

        super.setLabelFontUnfinished(fontNew, notify);
    }



    @Override
    public void setLabelFontFinished(Font fontNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabelFontFinished(fontNew, notify);
            }
        }

        super.setLabelFontFinished(fontNew, notify);
    }




    @Override
    public void setLabelLengthwisePosition(float lengthwisePosition, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabelLengthwisePosition(lengthwisePosition, notify);
            }
        }

        super.setLabelLengthwisePosition(lengthwisePosition, notify);
    }



    @Override
    public void setLabelOffset(float labelOffset, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setLabelOffset(labelOffset, notify);
            }
        }

        super.setLabelOffset(labelOffset, notify);
    }

    @Override
    public void setFilledUnfinishedStandard(boolean filledNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setFilledUnfinishedStandard(filledNew, notify);
            }
        }

        super.setFilledUnfinishedStandard(filledNew, notify);
    }    

    @Override
    public void setFilledFinishedStandard(boolean filledNew, boolean notify)
    {   
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setFilledFinishedStandard(filledNew, notify);
            }
        }

        super.setFilledFinishedStandard(filledNew, notify);
    }

    @Override
    public void setDrawOutlines(boolean visibleNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {       
                dr.setDrawOutlines(visibleNew, notify);
            }
        }
        super.setDrawOutlines(visibleNew, notify);
    }

    @Override
    public void setFillPaintUnfinishedStandard(Paint paintNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {
                dr.setFillPaintUnfinishedStandard(paintNew, notify);
            }
        }
        super.setFillPaintUnfinishedStandard(paintNew, notify);
    }

    @Override
    public void setFillPaintFinishedStandard(Paint paintNew, boolean notify)
    {
        for(ROIComponent roi : componentROIs)
        {
            for(ROIDrawable dr : roi.getROIDrawables())
            {        
                dr.setFillPaintFinishedStandard(paintNew, notify);
            }
        }
        super.setFillPaintFinishedStandard(paintNew, notify);
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
        int count = 0;

        for(ROI roi : componentROIs)
        {
            count = count + GridPositionCalculator.getInsidePointCountUpperBound(grid, roi.getROIShape());
        }

        return count;
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

    @Override
    public ROIGeneralUnionSerializationProxy getProxy()
    {
        List<ROISimpleComponentSerializationProxy> componentSerInfos = new ArrayList<>();

        for(SimpleROIComponent component : simpleComponents.values())
        {
            componentSerInfos.add(component.getSerializableInformation());
        }

        return new ROIGeneralUnionSerializationProxy(componentSerInfos, spatialRestriction.getSerializableRegionInformation(), getCustomLabel(), isFinished());
    }

    static class ROIGeneralUnionSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Collection<ROISimpleComponentSerializationProxy> componentsSerInfos;
        private final SerializableRegionInformation spatialRestriction;
        private final boolean finished;
        private final String customLabel;

        private ROIGeneralUnionSerializationProxy(Collection<ROISimpleComponentSerializationProxy> components, SerializableRegionInformation spatialRestriction, String customLabel, boolean finished)
        {
            this.componentsSerInfos = components;
            this.spatialRestriction = spatialRestriction;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public GeneralUnionROI recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            List<SimpleROIComponent> simpleComponents = new ArrayList<>();

            for(ROISimpleComponentSerializationProxy info : componentsSerInfos)
            {
                simpleComponents.add(info.recreateOriginalObject(roiStyle, key));
            }

            String label = (customLabel != null) ? customLabel : key.toString();
            GeneralUnionROI roi = new GeneralUnionROI(simpleComponents, spatialRestriction.getRegion(), roiStyle, key, label);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
