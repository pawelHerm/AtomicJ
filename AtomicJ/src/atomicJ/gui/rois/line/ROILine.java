
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

package atomicJ.gui.rois.line;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.PublicCloneable;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.ProfilePolyLine;
import atomicJ.gui.profile.ProfileStyle;

public class ROILine extends ROICurve implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    private Shape startHotSpot = new Area();
    private Shape endHotSpot = new Area();

    private final float hotSpotRadius = 5.f;

    public ROILine(Point2D anchor, Point2D end, Integer key, ProfileStyle style) 
    {   
        this(anchor, end, key, key.toString(), style);
    }

    public ROILine(Point2D anchor, Point2D end, Integer key, String label, ProfileStyle style) 
    {	
        super(key, label, style);

        this.x1 = anchor.getX();
        this.y1 = anchor.getY();
        this.x2 = end.getX();
        this.y2 = end.getY();
    }

    public ROILine(ROILine that)
    {
        this(that, that.getStyle());
    }

    public ROILine(ROILine that, ProfileStyle style)
    {
        super(that, style);

        this.x1 = that.x1;
        this.y1 = that.y1;
        this.x2 = that.x2;
        this.y2 = that.y2;

    }

    @Override
    public boolean isComplex()
    {
        return false;
    }

    @Override
    public ROILine copy()
    {
        return new ROILine(this);
    }

    @Override
    public ROILine copy(ProfileStyle style)
    {
        return new ROILine(this, style);
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isProfile())
        {
            return oldMode;
        }
        return MouseInputModeStandard.PROFILE_LINE;
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorCore anchor = null;
        if(isFinished())
        {
            if(startHotSpot.contains(java2DPoint))
            {
                anchor= BasicAnnotationAnchor.START;
            }
            else if(endHotSpot.contains(java2DPoint))
            {
                anchor = BasicAnnotationAnchor.END;
            }
            else if(getDistanceShape().intersects(dataRectangle))
            {
                anchor = BasicAnnotationAnchor.CENTER;
            }
        }

        AnnotationAnchorSigned caughtAnchor = (anchor != null) ? new AnnotationAnchorSourceSigned(anchor, getKey()) : null;
        return caughtAnchor;    	
    }

    @Override
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned caughtProfileAnchor, Point2D startPoint, Point2D endPoint)
    {
        if(caughtProfileAnchor == null)
        {
            return null;
        }	

        AnnotationAnchorCore coreAnchor = caughtProfileAnchor.getCoreAnchor();

        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x1 = x1 + tx;
            this.x2 = x2 + tx;
            this.y1 = y1 + ty;
            this.y2 = y2 + ty;

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.START.equals(coreAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x1 = x1 + tx;
            this.y1 = y1 + ty;

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.END.equals(coreAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x2 = x2 + tx;
            this.y2 = y2 + ty;

            fireAnnotationChanged();
        }

        return caughtProfileAnchor;
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        return false;
    }   

    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.x1,  this.y1, x, y, modifiers);
        moveEndTo(endNew, true);
    }  

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.x1,  this.y1, x, y, modifiers);
        moveEndTo(endNew, true);
    }

    private void moveEndTo(Point2D endNew, boolean notify)
    {
        this.x2 = endNew.getX();
        this.y2 = endNew.getY();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public double getLength()
    {
        double length = DistanceShapeFactors.getLength(getLine());

        return length;
    }

    @Override
    public double getStartX()
    {
        return x1;
    }

    @Override
    public double getStartY()
    {
        return y1;
    }

    @Override
    public double getEndX()
    {
        return x2;
    }

    @Override
    public double getEndY()
    {
        return y2;
    }

    @Override
    public Point2D getStartPoint()
    {
        return new Point2D.Double(x1, y1);
    }

    @Override
    public Point2D getEndPoint()
    {
        return new Point2D.Double(x2, y2);
    }

    @Override
    public Line2D getDistanceShape()
    {
        Line2D line = new Line2D.Double(x1,y1,x2,y2);
        return line;
    }

    public Line2D.Double getLine()
    {
        Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);
        return line;
    }


    @Override
    protected void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint)
    {
        float j2DX1 = (float) j2DStartPoint.getX();
        float j2DY1 = (float) j2DStartPoint.getY();

        float j2DX2 = (float) j2DEndPoint.getX();
        float j2DY2 = (float) j2DEndPoint.getY();

        startHotSpot = new Area();
        endHotSpot = new Area();

        if(isHighlighted())
        {
            startHotSpot = new Ellipse2D.Float(j2DX1 - hotSpotRadius, j2DY1 - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius);
            g2.fill(startHotSpot);       

            endHotSpot = new Ellipse2D.Float(j2DX2 - hotSpotRadius, j2DY2 - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius);
            g2.fill(endHotSpot);
        }

    }

    @Override
    public DistanceShapeFactors getDistanceShapeFactors() {
        return DistanceShapeFactors.getShapeFactors(new Line2D.Double(x1, y1, x2, y2));
    }

    @Override
    public ROICurveSerializationProxy getProxy()
    {
        return new ROILineSerializationProxy(x1, y1, x2, y2, getCustomLabel(), isFinished());
    }

    private static class ROILineSerializationProxy implements ROICurveSerializationProxy
    {
        private static final long serialVersionUID = 1L;

        private final double x1;
        private final double y1;
        private final double x2;
        private final double y2;
        private final String customLabel;
        private final boolean finished;

        private ROILineSerializationProxy(double x1, double y1, double x2, double y2, String customLabel, boolean finished)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.customLabel = customLabel;
            this.finished = finished;
        }

        @Override
        public ROILine recreateOriginalObject(ProfileStyle roiStyle, Integer key)
        {
            Point2D anchor = new Point2D.Double(x1, y1);
            Point2D end = new Point2D.Double(x2, y2);
            ROILine profile = (customLabel != null) ? new ROILine(anchor, end, key, customLabel, roiStyle) : new ROILine(anchor, end, key, roiStyle);
            profile.setFinished(finished);

            return profile;
        }        
    }
}

