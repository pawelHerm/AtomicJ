
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

import static atomicJ.gui.rois.ROIStyle.*;
import static atomicJ.gui.rois.PreferredROIStyle.*;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import atomicJ.data.ArraySupport2D;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.ShapeFactors;
import atomicJ.gui.annotations.AbstractCustomizableAnnotation;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.region.Region;
import atomicJ.utilities.GeometryUtilities;

public abstract class ROIDrawable extends AbstractCustomizableAnnotation implements ROI,  Serializable
{
    private static final long serialVersionUID = 1L;

    private Paint fillPaintFinishedStandard;
    private Paint fillPaintUnfinishedStandard;

    private boolean isFilledUnfinishedStandard;
    private boolean isFilledFinishedStandard;

    private boolean isOutlineVisible = true;

    private Shape labelHotSpot;

    public ROIDrawable(Integer key, ROIStyle style) 
    {
        super(key, style);

        setConsistentWithStyle(style, false);
    }

    public ROIDrawable(Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        setConsistentWithStyle(style, false);
    }

    public ROIDrawable(ROIDrawable that)
    {
        this(that, that.getStyle());
    }

    public ROIDrawable(ROIDrawable that, ROIStyle style)
    {
        super(that, style);
        setConsistentWithStyle(style, false);
    }

    public ROIDrawable(ROIDrawable that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);
        setConsistentWithStyle(style, false);
    }

    @Override
    public ROIPolygon getRotatedCopy(double angleInRadians, double anchorX, double anchorY)
    {
        return new ROIPolygon(this).getRotatedCopy(angleInRadians, anchorX, anchorY);
    }

    @Override
    public  AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint,
            Point2D dataPoint, Rectangle2D dataRectangle)
    {
        if(isLabelClicked(java2DPoint))
        {
            return new AnnotationAnchorSourceSigned(BasicAnnotationAnchor.LABEL, getKey());
        }

        return null;
    }

    private void setConsistentWithStyle(ROIStyle style, boolean notify)
    {
        this.isFilledUnfinishedStandard = style.isFilledUnfinishedStandard();
        this.isFilledFinishedStandard = style.isFilledFinishedStandard();

        this.fillPaintUnfinishedStandard = style.getPaintFillUnfinishedStandard();
        this.fillPaintFinishedStandard = style.getPaintFillFinishedStandard();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }



    public abstract MouseInputMode getMouseInputMode(MouseInputMode oldMode);
    public abstract AnnotationModificationOperation setPosition(AnnotationAnchorSigned caughtRoiAnchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);
    public abstract AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);
    public abstract AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D caughtROICenter, Point2D caughtROICompositeCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);    

    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers){}
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers){}

    //returns true if ROI was modified
    public abstract boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);

    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor)
    {
        Rectangle2D shape = getROIShape().getBounds2D();

        double rotCenterX = shape.getCenterX();
        double rotCenterY = shape.getCenterY();

        return new Point2D.Double(rotCenterX, rotCenterY);
    }

    public Point2D getDefaultCompositeRotationCenter(AnnotationAnchorSigned anchor)
    {
        return getDefaultRotationCenter(anchor);
    }

    @Override
    public abstract ROIDrawable copy();
    public abstract ROIDrawable copy(ROIStyle style);
    public abstract ROIDrawable copy(ROIStyle style, Integer key, String label);

    public ROIDrawable getConvexHull(Integer key, String label) 
    {        
        Shape roiShape = getROIShape();

        PathIterator it = roiShape.getPathIterator(null, 0);
        double[][] points = GeometryUtilities.getPolygonVertices(it);
        double[][] hull = GeometryUtilities.getConvexHull(points);

        Path2D hullShape = GeometryUtilities.convertToClosedPath(hull);

        ROIPolygon roiShapeHull = new ROIPolygon(hullShape, key, label, getStyle());
        roiShapeHull.setFinished(isFinished());

        return roiShapeHull;
    }

    @Override
    public abstract ROIProxy getProxy();

    @Override
    public ROIStyle getStyle()
    {
        return (ROIStyle)super.getStyle();
    }

    public abstract boolean isCorrectlyConstructed();

    public boolean isBoundaryClicked(Rectangle2D dataRectangle)
    {
        boolean clicked = false;
        if(isVisible())
        {
            //we want the dataRectangle to intersect the outline of the roi shape
            Shape roiShape = getROIShape();
            clicked = roiShape.intersects(dataRectangle) && !roiShape.contains(dataRectangle);
        }
        return clicked;
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        return ShapeFactors.getShapeFactorsForPolygon(getROIShape());
    }

    @Override
    public abstract Shape getROIShape();

    @Override
    public boolean contains(Point2D p)
    {
        Shape shape = getROIShape();
        boolean contains = shape.contains(p);
        return contains;
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

    public boolean isFilled()
    {
        boolean isFilled = isFinished() ? isFilledFinishedStandard : isFilledUnfinishedStandard;
        return isFilled;
    }   

    public boolean isFilledUnfinishedStandard()
    {
        return isFilledUnfinishedStandard;
    }

    public void setFilledUnfinishedStandard(boolean filledNew)
    {
        setFilledUnfinishedStandard(filledNew, true);
    }

    public void setFilledUnfinishedStandard(boolean filledNew, boolean notify)
    {
        this.isFilledUnfinishedStandard = filledNew;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }    

    public boolean isFilledFinishedStandard()
    {
        return isFilledFinishedStandard;
    }

    public void setFilledFinishedStandard(boolean filledNew)
    {
        setFilledFinishedStandard(filledNew, true);
    }

    public void setFilledFinishedStandard(boolean filledNew, boolean notify)
    {	    
        this.isFilledFinishedStandard = filledNew;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public boolean isOutlineVisible()
    {
        return isOutlineVisible;
    }

    public void setOutlineVisible(boolean visibleNew)
    {
        setDrawOutlines(visibleNew, true);
    }

    public void setDrawOutlines(boolean visibleNew, boolean notify)
    {
        this.isOutlineVisible = visibleNew;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public Paint getFillPaint()
    {
        Paint paint = isFinished() ? fillPaintFinishedStandard : fillPaintUnfinishedStandard;		
        return paint;
    }

    public Paint getFillPaintUnfinishedStandard()
    {
        return fillPaintUnfinishedStandard;
    }

    public void setFillPaintUnfinishedStandard(Paint paint)
    {
        setFillPaintUnfinishedStandard(paint, true);
    }

    public void setFillPaintUnfinishedStandard(Paint paint, boolean notify)
    {
        this.fillPaintUnfinishedStandard = paint;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public Paint getFillPaintFinishedStandard()
    {
        return fillPaintFinishedStandard;
    }

    public void setFillPaintFinishedStandard(Paint paint)
    {
        setFillPaintFinishedStandard(paint,  true);
    }

    public void setFillPaintFinishedStandard(Paint paint, boolean notify)
    {
        this.fillPaintFinishedStandard = paint;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public boolean isLabelClicked(Point2D java2DPoint)
    {
        boolean labelCaught = false;

        if(labelHotSpot != null)
        {
            labelCaught = labelHotSpot.contains(java2DPoint);
        }

        return labelCaught;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(ROI_STYLE_COMPLETELY_CHANGED.equals(property))
        {
            setConsistentWithStyle(getStyle(), true);
        }
        else if(ROI_FILLED_UNFINISHED_STANDARD.equals(property))
        {
            boolean filled = (Boolean)evt.getNewValue();
            setFilledUnfinishedStandard(filled);
        }			
        else if(ROI_FILLED_FINISHED_STANDARD.equals(property))
        {
            boolean filled = (Boolean)evt.getNewValue();
            setFilledFinishedStandard(filled);
        }

        else if(ROI_OUTLINE_VISIBLE.equals(property))
        {

        }

        else if(ROI_PAINT_FILL_FINISHED_STANDARD.equals(property))
        {
            Paint paint = (Paint)evt.getNewValue();
            setFillPaintFinishedStandard(paint);
        }
        else if(ROI_PAINT_FILL_UNFINISHED_STANDARD.equals(property))
        {
            Paint paint = (Paint)evt.getNewValue();
            setFillPaintUnfinishedStandard(paint);
        }
    }



    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis,
            int rendererIndex,
            PlotRenderingInfo info) 
    {
        if(!isVisible())
        {
            return;
        }

        AffineTransform tr = getDataToJava2DTransformation(g2, plot, dataArea, domainAxis, rangeAxis, info);
        Shape roiShape = tr.createTransformedShape(getROIShape());

        if (isFilled()) 
        {
            g2.setPaint(getFillPaint());
            g2.fill(roiShape);
        }

        if(isOutlineVisible())
        {
            g2.setPaint(getStrokePaint());
            g2.setStroke(getStroke());
            g2.draw(roiShape);
        }      

        if(isLabelFinishedVisible())
        {
            //double slope = deltaY/deltaX;

            float lengthwisePosition= getLabelLengthwisePosition();

            g2.setFont(getLabelFont());	   
            g2.setPaint(getLabelPaint());

            Rectangle2D shapeBounds = roiShape.getBounds2D();	    		

            FontMetrics fontMetrics = g2.getFontMetrics();
            Rectangle2D stringBounds = fontMetrics.getStringBounds(getLabel(), g2).getBounds2D();       	

            float labelX = (float) (shapeBounds.getX() + lengthwisePosition*shapeBounds.getWidth() - stringBounds.getWidth()/2);
            float labelY = (float) (shapeBounds.getY() + lengthwisePosition*shapeBounds.getHeight() + fontMetrics.getAscent()/3);


            this.labelHotSpot = new Rectangle2D.Double(labelX, labelY - stringBounds.getHeight(), stringBounds.getWidth(), stringBounds.getHeight());

            g2.drawString(getLabel(), labelX, labelY);
        }

        addEntity(info, getStroke().createStrokedShape(roiShape), rendererIndex, getToolTipText(), getURL());      
        drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
    }


    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {}


    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape) 
    {}

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

    public abstract List<ROIDrawable> split(double[][] polylineVertices);
}
