
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
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.GeneralUnionROI.ROIGeneralUnionSerializationProxy;
import atomicJ.gui.rois.region.Region;
import atomicJ.gui.rois.region.ShapeRegion;
import atomicJ.utilities.GeometryUtilities;

public class DifferenceROI extends ROIDrawable implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final ROIDrawable mainROI;
    private final GeneralUnionROI holesUnion;

    public DifferenceROI(ROIDrawable mainROI, Collection<ROIDrawable> holes, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.holesUnion = new GeneralUnionROI(holes, -1*key, label, style);
        this.mainROI = mainROI;
        this.holesUnion.setSpatialRestriction(new ShapeRegion(mainROI.getROIShape()));

        setFinished(true);
    }

    public DifferenceROI(DifferenceROI that)
    {
        this(that, that.getStyle());
    }

    public DifferenceROI(DifferenceROI that, ROIStyle style)
    {
        this(that, style, that.getKey(), that.getLabel());
    }

    public DifferenceROI(DifferenceROI that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);

        this.holesUnion = that.holesUnion.copy(style, -1*key, label);    
        this.mainROI = that.mainROI.copy(style, key, label);

        setFinished(true);
    }

    private DifferenceROI(ROIDrawable mainROI, GeneralUnionROI holesUnion, ROIStyle style, Integer key, String label)
    {
        super(key, label, style);

        this.holesUnion = holesUnion;    
        this.mainROI = mainROI;

        setFinished(true);
    }


    @Override
    public ROIDrawable getConvexHull(Integer key, String label) 
    {
        Shape mainROIShape = mainROI.getROIShape();
        Shape holesShape = holesUnion.getROIShape();


        if(!GeometryUtilities.isShapeAIntersectingB(holesShape, mainROIShape))
        {
            return mainROI.copy(getStyle(), key, label);
        }

        if(GeometryUtilities.isShapeAContainedInB(holesShape, mainROIShape))
        {
            return mainROI.getConvexHull(key, label);
        }

        Shape roiShape = getROIShape();

        PathIterator it = roiShape.getPathIterator(null, 0);
        double[][] points = GeometryUtilities.getPolygonVertices(it);
        double[][] hull = GeometryUtilities.getConvexHull(points);

        Path2D hullShape = GeometryUtilities.convertToClosedPath(hull);

        ROIPolygon roiShapeHull = new ROIPolygon(hullShape, key, label, getStyle());
        roiShapeHull.setFinished(isFinished());

        return roiShapeHull;
        //                
        //        GeneralUnionROI holesNew = holesUnion.subtract(roiShapeHull, key, label, getStyle());
        //        ROIDrawable differenceROIConvexHull = new DifferenceROI(mainROIConvexHull, holesNew, getStyle(), key, label);
        //        
        //        holesNew.setSpatialRestriction(new ShapeRegion(differenceROIConvexHull.getROIShape()));
        //        differenceROIConvexHull.setFinished(isFinished());
        //        ROIDrawable mainROIConvexHull = mainROI.getConvexHull(key, label);
        //        return differenceROIConvexHull;
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {        
        AnnotationAnchorSigned anchor = null;

        //we query the holes union for anchor even when the holesUnion does not contain the data point
        //it is enough that mainROI contains it
        //this is on purpose, as some anchor point in the holes may stick outside its roi shape
        //ex. parts the circles on vertices of polygon hole will be outside the hole itself (albeit close to its edges), but inside the mainROI
        //the bottom line: don't change the condition to "&& if(mainROI.contains(dataPoint) && holesUnion.contains(dataPoint))"
        if(mainROI.contains(dataPoint))
        {            
            anchor = holesUnion.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);           
        }

        //label
        if(anchor == null)
        {
            AnnotationAnchorSigned labelAnchor = super.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
            if(labelAnchor != null)
            {
                return labelAnchor;
            }
        }

        //shape specific anchors
        if(anchor == null)
        {
            anchor = mainROI.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
        }

        AnnotationAnchorSigned caughtAnchor = (anchor != null) ? new AnnotationAnchorWrappedSigned(anchor, getKey()) : null;
        return caughtAnchor;
    }

    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationModificationOperation returnedOperation;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        boolean outerKeysCompatible = Objects.equals(anchor.getKey(), getKey());
        boolean innerKeysCompatible = Objects.equals(innerAnchor.getKey(), holesUnion.getKey());

        if(outerKeysCompatible && innerKeysCompatible)
        {
            returnedOperation = holesUnion.setPosition(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);            
        }	       
        else
        {            
            returnedOperation = mainROI.setPosition(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);

            if(BasicAnnotationAnchor.CENTER.equals(anchor.getCoreAnchor()))
            {
                holesUnion.setPositionInAll(innerAnchor, modifierKeys, pressedPoint, startPoint, endPoint);
            }

        }

        holesUnion.setSpatialRestriction(new ShapeRegion(mainROI.getROIShape()));

        fireAnnotationChanged();   

        boolean effctorIsSource = returnedOperation != null && returnedOperation.getAnchor() != null && outerKeysCompatible;
        AnnotationAnchorSigned anchorSigned = (effctorIsSource) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        Point2D endPointNew = effctorIsSource ? returnedOperation.getEndPoint(): endPoint;

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(anchorSigned, returnedOperation.getPressedPoint(), endPointNew);

        return modificationOperation;
    }


    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        boolean modified = false;

        AnnotationAnchorSigned anchor = getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
        if(anchor == null)
        {
            return false;
        }

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        if(Objects.equals(innerAnchor.getKey(), holesUnion.getKey()))
        {                        
            modified = holesUnion.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);
        }          
        else
        {            
            modified = mainROI.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);
            holesUnion.setSpatialRestriction(new ShapeRegion(mainROI.getROIShape()));
        }

        if(modified)
        {
            fireAnnotationChanged();   
        }

        return modified;
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationModificationOperation returnedOperation;
        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        boolean outerKeysCompatible = Objects.equals(anchor.getKey(), getKey());
        boolean innerKeysCompatible = Objects.equals(innerAnchor.getKey(), holesUnion.getKey());

        if(outerKeysCompatible && innerKeysCompatible)
        {
            returnedOperation = holesUnion.rotate(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);
        }          
        else
        {            
            returnedOperation = mainROI.rotate(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);

            if(BasicAnnotationAnchor.CENTER.equals(anchor.getCoreAnchor()))
            {
                holesUnion.rotateAll(innerAnchor, modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);
            }
        }

        holesUnion.setSpatialRestriction(new ShapeRegion(mainROI.getROIShape()));

        fireAnnotationChanged();   

        boolean effctorIsSource = returnedOperation != null && returnedOperation.getAnchor() != null && outerKeysCompatible;
        AnnotationAnchorSigned anchorSigned = (effctorIsSource) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        Point2D endPointNew = effctorIsSource ? returnedOperation.getEndPoint(): endPoint;

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(anchorSigned, returnedOperation.getPressedPoint(), endPointNew);

        return modificationOperation;
    }

    @Override
    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor)
    {       
        boolean outerKeysCompatible = Objects.equals(anchor.getKey(), getKey());
        boolean innerKeysCompatible = Objects.equals(anchor.getInnerAnchor().getKey(), holesUnion.getKey());

        if(outerKeysCompatible && innerKeysCompatible)
        {
            return holesUnion.getDefaultRotationCenter(anchor.getInnerAnchor());
        }   

        Rectangle2D mainROIBounds = mainROI.getROIShape().getBounds2D();
        double rotationCenterX = mainROIBounds.getCenterX();
        double rotationCenterY = mainROIBounds.getCenterY();

        return new Point2D.Double(rotationCenterX, rotationCenterY);
    }

    @Override
    public Point2D getDefaultCompositeRotationCenter(AnnotationAnchorSigned anchor)
    {
        boolean outerKeysCompatible = Objects.equals(anchor.getKey(), getKey());
        boolean innerKeysCompatible = Objects.equals(anchor.getInnerAnchor().getKey(), holesUnion.getKey());

        if(outerKeysCompatible && innerKeysCompatible)
        {
            return holesUnion.getDefaultCompositeRotationCenter(anchor.getInnerAnchor());
        }   

        Rectangle2D mainROIBounds = mainROI.getROIShape().getBounds2D();
        double rotationCenterX = mainROIBounds.getCenterX();
        double rotationCenterY = mainROIBounds.getCenterY();

        return new Point2D.Double(rotationCenterX, rotationCenterY);
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D caughtROICenter, Point2D caughtROICompositeCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationModificationOperation returnedOperation;

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        boolean outerKeysCompatible = Objects.equals(anchor.getKey(), getKey());
        boolean innerKeysCompatible = Objects.equals(innerAnchor.getKey(), holesUnion.getKey());

        if(outerKeysCompatible && innerKeysCompatible)
        {
            if(BasicAnnotationAnchor.CENTER.equals(anchor.getCoreAnchor()) && modifierKeys.contains(ModifierKey.CONTROL))
            {
                returnedOperation = holesUnion.rotateAll(innerAnchor, modifierKeys, caughtROICompositeCenter, pressedPoint, startPoint, endPoint);
            }
            else if(BasicAnnotationAnchor.CENTER.equals(anchor.getCoreAnchor()) && modifierKeys.contains(ModifierKey.SHIFT))
            {
                returnedOperation = holesUnion.rotateAll(innerAnchor, modifierKeys, caughtROICenter, pressedPoint, startPoint, endPoint);
            }
            else
            {
                returnedOperation = holesUnion.rotate(innerAnchor, modifierKeys, caughtROICenter, pressedPoint, startPoint, endPoint);
            }
        }          
        else
        {            
            returnedOperation = mainROI.rotate(anchor, modifierKeys, caughtROICenter, caughtROICompositeCenter, startPoint, endPoint);

            if(BasicAnnotationAnchor.CENTER.equals(anchor.getCoreAnchor()))
            {
                holesUnion.rotateAll(anchor, modifierKeys, caughtROICenter, pressedPoint, startPoint, endPoint);
            }
        }

        holesUnion.setSpatialRestriction(new ShapeRegion(mainROI.getROIShape()));

        fireAnnotationChanged();   

        boolean effctorIsSource = returnedOperation != null && returnedOperation.getAnchor() != null && outerKeysCompatible;
        AnnotationAnchorSigned anchorSigned = (effctorIsSource) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        Point2D endPointNew = effctorIsSource ? returnedOperation.getEndPoint(): endPoint;

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(anchorSigned, returnedOperation.getPressedPoint(), endPointNew);

        return modificationOperation;
    }

    @Override
    public DifferenceROI copy()
    {
        return new DifferenceROI(this);
    }

    @Override
    public DifferenceROI copy(ROIStyle style)
    {
        return new DifferenceROI(this, style);
    }

    @Override
    public DifferenceROI copy(ROIStyle style, Integer key, String label) {
        return new DifferenceROI(this, style, key, label);
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        boolean correctlyConstructed = mainROI.isCorrectlyConstructed() && holesUnion.isCorrectlyConstructed();

        return correctlyConstructed;
    }

    @Override
    public Shape getROIShape()
    {     
        return buildROIShape();
    }

    private Shape buildROIShape()
    {
        Area roiShape = new Area(mainROI.getROIShape());
        roiShape.subtract(new Area(holesUnion.getROIShape()));

        return roiShape;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {      
        super.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);

        holesUnion.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
        mainROI.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, new ShapeRegion(holesUnion.getROIShape()));
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape) 
    {
        holesUnion.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, excludedShape);
        mainROI.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, excludedShape);
    }

    //holes have no labels
    @Override
    public boolean isLabelClicked(Point2D java2DPoint)
    {
        return super.isLabelClicked(java2DPoint);
    }

    //holes have no labels
    @Override
    public void setLabel(String labelNew, boolean notify)
    {
        super.setLabel(labelNew, notify);
    }

    @Override
    public void setVisible(boolean visibleNew, boolean notify)
    {
        holesUnion.setVisible(visibleNew, notify);
        super.setVisible(visibleNew, notify);
    }

    @Override
    public void setHighlighted(boolean highlightedNew, boolean notify)
    {        
        holesUnion.setHighlighted(highlightedNew, notify);
        mainROI.setHighlighted(highlightedNew, notify);
        super.setHighlighted(highlightedNew, notify);
    }

    //holes have no labels
    @Override
    public void setLabelUnfinishedVisible(boolean visible, boolean notify)
    {
        super.setLabelUnfinishedVisible(visible, notify);
    }

    //holes have no labels
    @Override
    public void setLabelFinishedVisible(boolean visible, boolean notify)
    {
        super.setLabelFinishedVisible(visible, notify);
    }

    //holes have no labels
    @Override
    public void setPaintLabelFinished(Paint paintNew, boolean notify)
    {
        super.setPaintLabelFinished(paintNew, notify);
    }

    //holes have no labels
    @Override
    public void setPaintLabelUnfinished(Paint paintNew, boolean notify)
    {
        super.setPaintLabelUnfinished(paintNew, notify);
    }

    @Override
    public void setPaintFinished(Paint paintNew, boolean notify)
    {
        super.setPaintFinished(paintNew, notify);
    }

    @Override
    public void setPaintUnfinished(Paint paintNew, boolean notify)
    {
        super.setPaintUnfinished(paintNew, notify);
    }

    @Override
    public void setStrokeFinished(Stroke strokeNew, boolean notify)
    {
        holesUnion.setStrokeFinished(strokeNew, notify);
        super.setStrokeFinished(strokeNew, notify);
    }

    @Override
    public void setStrokeUnfinished(Stroke strokeNew, boolean notify)
    {
        holesUnion.setStrokeUnfinished(strokeNew, notify);
        super.setStrokeUnfinished(strokeNew, notify);
    }

    //holes have no labels
    @Override
    public void setLabelFontUnfinished(Font fontNew, boolean notify)
    {
        super.setLabelFontUnfinished(fontNew, notify);
    }

    //holes have no labels
    @Override
    public void setLabelFontFinished(Font fontNew, boolean notify)
    {
        super.setLabelFontFinished(fontNew, notify);
    }

    //holes have no labels
    @Override
    public void setLabelLengthwisePosition(float lengthwisePosition, boolean notify)
    {
        super.setLabelLengthwisePosition(lengthwisePosition, notify);
    }

    //holes have no labels
    @Override
    public void setLabelOffset(float labelOffset, boolean notify)
    {
        super.setLabelOffset(labelOffset, notify);
    }

    //holes never filled
    @Override
    public void setFilledUnfinishedStandard(boolean filledNew, boolean notify)
    {
        super.setFilledUnfinishedStandard(filledNew, notify);
    }    

    //holes never filled
    @Override
    public void setFilledFinishedStandard(boolean filledNew, boolean notify)
    {   
        super.setFilledFinishedStandard(filledNew, notify);
    }


    @Override
    public void setDrawOutlines(boolean visibleNew, boolean notify)
    {
        holesUnion.setDrawOutlines(visibleNew, notify);
        super.setDrawOutlines(visibleNew, notify);
    }

    //holes never filled
    @Override
    public void setFillPaintUnfinishedStandard(Paint paintNew, boolean notify)
    {
        super.setFillPaintUnfinishedStandard(paintNew, notify);
    }

    //holes never filled
    @Override
    public void setFillPaintFinishedStandard(Paint paintNew, boolean notify)
    {
        super.setFillPaintFinishedStandard(paintNew, notify);
    }

    @Override
    public void setFinished(boolean finishedNew, boolean notify)
    {
        super.setFinished(finishedNew, notify);
        holesUnion.setFinished(finishedNew, notify);
        mainROI.setFinished(finishedNew, notify);
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
    public List<ROIDrawable> split(double[][] polylineVertices)
    {
        List<ROIDrawable> roisNew = new ArrayList<>();

        List<ROIDrawable> mainROISplit = mainROI.split(polylineVertices);

        for(ROIDrawable r : mainROISplit)
        {
            DifferenceROI diffRoi = new DifferenceROI(r, holesUnion.copyIndividualROIs(getStyle()), -1, "", getStyle());
            roisNew.add(diffRoi);
        }

        return roisNew;
    }

    @Override
    public ROIProxy getProxy()
    {
        return new ROIDifferenceSerializationProxy(mainROI.getProxy(), holesUnion.getProxy(), getCustomLabel(), isFinished());
    }

    private static class ROIDifferenceSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final ROIProxy mainROISerializable;
        private final ROIGeneralUnionSerializationProxy holesUnionSerializable;
        private final String customLabel;
        private final boolean finished;

        private ROIDifferenceSerializationProxy(ROIProxy mainROISerializable, ROIGeneralUnionSerializationProxy holesUnionSerializable, String customLabel, boolean finished)
        {
            this.mainROISerializable = mainROISerializable;
            this.holesUnionSerializable = holesUnionSerializable;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public DifferenceROI recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            DifferenceROI roi = new DifferenceROI(mainROISerializable.recreateOriginalObject(roiStyle, key), holesUnionSerializable.recreateOriginalObject(roiStyle, key), roiStyle, key, label);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
