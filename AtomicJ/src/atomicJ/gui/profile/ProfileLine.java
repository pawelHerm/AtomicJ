
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

package atomicJ.gui.profile;

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

public class ProfileLine extends Profile implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    private Shape startHotSpot = new Area();
    private Shape endHotSpot = new Area();

    private final float hotSpotRadius = 5.f;

    public ProfileLine(Point2D anchor, Point2D end, Integer key, ProfileStyle style) 
    {   
        this(anchor, end, key, key.toString(), style);
    }

    public ProfileLine(Point2D anchor, Point2D end, Integer key, String label, ProfileStyle style) 
    {	
        super(key, label, style);

        this.x1 = anchor.getX();
        this.y1 = anchor.getY();
        this.x2 = end.getX();
        this.y2 = end.getY();
    }

    public ProfileLine(ProfileLine that)
    {
        this(that, that.getStyle());
    }

    public ProfileLine(ProfileLine that, ProfileStyle style)
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
    public ProfileLine copy()
    {
        return new ProfileLine(this);
    }

    @Override
    public ProfileLine copy(ProfileStyle style)
    {
        return new ProfileLine(this, style);
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
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned caughtProfileAnchor, Point2D pressedPoint, Point2D previousPosition, Point2D endPoint, Set<ModifierKey> modifierKeys)
    {
        if(caughtProfileAnchor == null)
        {
            return null;
        }	

        AnnotationAnchorCore coreAnchor = caughtProfileAnchor.getCoreAnchor();

        Point2D finalPosition = previousPosition;    


        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {           
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(), pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);           

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            this.x1 = x1 + tx;
            this.x2 = x2 + tx;
            this.y1 = y1 + ty;
            this.y2 = y2 + ty;

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.START.equals(coreAnchor))
        {
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(), pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);           

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            this.x1 = x1 + tx;
            this.y1 = y1 + ty;

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.END.equals(coreAnchor))
        {  
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(), pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);           

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            this.x2 = x2 + tx;
            this.y2 = y2 + ty;


            fireAnnotationChanged();
        }

        AnnotationModificationOperation currentModificationOperation = new AnnotationModificationOperation(caughtProfileAnchor, pressedPoint, finalPosition);

        return currentModificationOperation;
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
    public Shape getDistanceShape()
    {
        Line2D line = new Line2D.Double(x1,y1,x2,y2);
        return line;
    }

    public Line2D getLine()
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
    public ProfileProxy getProxy()
    {
        return new ProfileLineProxy(x1, y1, x2, y2, getCustomLabel(), isFinished());
    }

    private static class ProfileLineProxy implements ProfileProxy
    {
        private static final long serialVersionUID = 1L;

        private final double x1;
        private final double y1;
        private final double x2;
        private final double y2;
        private final String customLabel;
        private final boolean finished;

        private ProfileLineProxy(double x1, double y1, double x2, double y2, String customLabel, boolean finished)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.customLabel = customLabel;
            this.finished = finished;
        }

        @Override
        public Profile recreateOriginalObject(ProfileStyle roiStyle, Integer key)
        {
            Point2D anchor = new Point2D.Double(x1, y1);
            Point2D end = new Point2D.Double(x2, y2);
            Profile profile = (customLabel != null) ? new ProfileLine(anchor, end, key, customLabel, roiStyle) : new ProfileLine(anchor, end, key, roiStyle);
            profile.setFinished(finished);

            return profile;
        }        
    }
}

