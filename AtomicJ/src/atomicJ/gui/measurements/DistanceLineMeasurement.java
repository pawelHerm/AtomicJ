
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

package atomicJ.gui.measurements;

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
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.ProfilePolyLine;
import atomicJ.utilities.GeometryUtilities;

public class DistanceLineMeasurement extends DistanceMeasurementDrawable
{
    private static final long serialVersionUID = 1L;

    private final float hotSpotRadius = 5.f;

    private Point2D startPoint;
    private Point2D endPoint;

    private Shape startHotSpot = new Area();
    private Shape endHotSpot = new Area();

    public DistanceLineMeasurement(Point2D startPoint, Point2D endPoint, Integer key, DistanceMeasurementStyle style) 
    {	
        super(key, style);

        this.startPoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        this.endPoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
    }

    public DistanceLineMeasurement(Point2D startPoint, Point2D endPoint, Integer key, String label, DistanceMeasurementStyle style) 
    {   
        super(label, key, style);

        this.startPoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        this.endPoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
    }

    public DistanceLineMeasurement(DistanceLineMeasurement that)
    {
        this(that, that.getStyle());
    }

    public DistanceLineMeasurement(DistanceLineMeasurement that, DistanceMeasurementStyle style)
    {
        super(that, style);    

        this.startPoint = new Point2D.Double(that.startPoint.getX(), that.startPoint.getY());
        this.endPoint = new Point2D.Double(that.endPoint.getX(), that.endPoint.getY());
    }

    public DistanceLineMeasurement(DistanceLineMeasurement that, DistanceMeasurementStyle style, Integer key, String label)
    {
        super(that, style, key, label);    

        this.startPoint = new Point2D.Double(that.startPoint.getX(), that.startPoint.getY());
        this.endPoint = new Point2D.Double(that.endPoint.getX(), that.endPoint.getY());
    }

    @Override
    public DistanceLineMeasurement copy()
    {
        return new DistanceLineMeasurement(this);
    }

    @Override
    public DistanceLineMeasurement copy(DistanceMeasurementStyle style)
    {
        return new DistanceLineMeasurement(this, style);
    }

    @Override
    public DistanceLineMeasurement copy(DistanceMeasurementStyle style, Integer key, String label)
    {
        return new DistanceLineMeasurement(this, style, key, label);
    }

    public boolean acceptsNodes()
    {
        return false;
    }

    @Override
    public Point2D getStartPoint()
    {
        return new Point2D.Double(startPoint.getX(), startPoint.getY());
    }

    @Override
    public Point2D getEndPoint()
    {
        return new Point2D.Double(endPoint.getX(), endPoint.getY());
    }

    @Override
    public double getStartX()
    {
        return startPoint.getX();
    }

    @Override
    public double getStartY()
    {
        return startPoint.getY();
    }

    @Override
    public double getEndX()
    {
        return endPoint.getX();
    }

    @Override
    public double getEndY()
    {
        return endPoint.getY();
    }

    public double getLength()
    {
        double length = GeometryUtilities.getDistance(startPoint, endPoint);
        return length;
    }

    @Override
    public Shape getDistanceShape()
    {
        return new Line2D.Double(startPoint, endPoint);
    }

    @Override
    public DistanceShapeFactors getDistanceShapeFactors()
    {
        return DistanceShapeFactors.getShapeFactors(new Line2D.Double(startPoint, endPoint));
    }

    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.startPoint.getX(),  this.startPoint.getY(), x, y, modifiers);
        moveEndTo(endNew, true);
    }  

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.startPoint.getX(),  this.startPoint.getY(), x, y, modifiers);
        moveEndTo(endNew, true);
    }

    private void moveEndTo(Point2D endNew, boolean notify)
    {
        this.endPoint = new Point2D.Double(endNew.getX(), endNew.getY());

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorCore anchor = null;
        if(isFinished())
        {
            if(startHotSpot.contains(java2DPoint))
            {
                anchor = BasicAnnotationAnchor.START;
            }
            else if(endHotSpot.contains(java2DPoint))
            {
                anchor = BasicAnnotationAnchor.END;
            }
            else if(isClicked(dataRectangle))
            {
                anchor = BasicAnnotationAnchor.CENTER;
            }
        }

        AnnotationAnchorSigned caughtAnchor = (anchor != null) ?  new AnnotationAnchorSourceSigned(anchor, getKey()) : null;
        return caughtAnchor;      
    }

    @Override
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned caughtProfileAnchor, Set<ModifierKey> modifierKeys, Point2D p1, Point2D p2)
    {
        if(caughtProfileAnchor == null)
        {
            return null;
        }   

        AnnotationAnchorCore coreAnchor = caughtProfileAnchor.getCoreAnchor();
        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {
            double x1 = p1.getX();
            double y1 = p1.getY();
            double x2 = p2.getX();
            double y2 = p2.getY();

            double tx = x2 - x1;
            double ty = y2 - y1;

            this.startPoint = new Point2D.Double(this.startPoint.getX() + tx, this.startPoint.getY() + ty);
            this.endPoint = new Point2D.Double(this.endPoint.getX() + tx, this.endPoint.getY() + ty);            

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.START.equals(coreAnchor))
        {
            double x1 = p1.getX();
            double y1 = p1.getY();
            double x2 = p2.getX();
            double y2 = p2.getY();

            double tx = x2 - x1;
            double ty = y2 - y1;

            this.startPoint = new Point2D.Double(this.startPoint.getX() + tx, this.startPoint.getY() + ty);
            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.END.equals(coreAnchor))
        {
            double x1 = p1.getX();
            double y1 = p1.getY();
            double x2 = p2.getX();
            double y2 = p2.getY();

            double tx = x2 - x1;
            double ty = y2 - y1;
            this.endPoint = new Point2D.Double(this.endPoint.getX() + tx, this.endPoint.getY() + ty);            

            fireAnnotationChanged();
        }

        AnnotationAnchorSigned returnedAnchor = new AnnotationAnchorSourceSigned(coreAnchor, getKey());
        return returnedAnchor;
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        return false;
    }

    @Override
    protected void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint, Point2D j2DCornerPoint, boolean forcedToDrawAbscissa, boolean forcedToDrawOrdinate)
    { 

        if(isHighlighted())
        {
            float j2DX1 = (float)j2DStartPoint.getX();
            float j2DY1 = (float)j2DStartPoint.getY();

            float j2DX2 = (float)j2DEndPoint.getX();
            float j2DY2 = (float)j2DEndPoint.getY();       

            float j2DX3 = (float)j2DCornerPoint.getX();
            float j2DY3 = (float)j2DCornerPoint.getY();

            float startHotSpotX = j2DX1 - hotSpotRadius;
            float startHotSpotY = j2DY1 - hotSpotRadius;
            float endHotspotX = j2DX2 - hotSpotRadius;
            float endHotspotY = j2DY2 - hotSpotRadius;

            if(forcedToDrawAbscissa)
            {
                startHotSpotX = j2DX1 - hotSpotRadius;
                startHotSpotY = j2DY3 - hotSpotRadius;
                endHotspotX = j2DX2 - hotSpotRadius;
                endHotspotY = j2DY3 - hotSpotRadius;
            }
            else if(forcedToDrawOrdinate)
            {
                startHotSpotX = j2DX3 - hotSpotRadius;
                startHotSpotY = j2DY1 - hotSpotRadius;
                endHotspotX = j2DX3 - hotSpotRadius;
                endHotspotY = j2DY3 - hotSpotRadius;

            }

            Shape startHotSpot = new Ellipse2D.Float(startHotSpotX, startHotSpotY, 2*hotSpotRadius, 2*hotSpotRadius);
            g2.fill(startHotSpot);       

            Shape endHotSpot = new Ellipse2D.Float(endHotspotX, endHotspotY, 2*hotSpotRadius, 2*hotSpotRadius);
            g2.fill(endHotSpot);

            this.startHotSpot = startHotSpot;
            this.endHotSpot = endHotSpot;
        } 
    }

    @Override
    public boolean isComplex() {
        return false;
    }

    @Override
    public MeasurementProxy getProxy() 
    {
        return new LineMeasurementProxy(startPoint, endPoint, getCustomLabel(), isFinished());
    }

    private static class LineMeasurementProxy implements MeasurementProxy
    {
        private static final long serialVersionUID = 1L;

        private final Point2D startPoint;
        private final Point2D endPoint;
        private final String customLabel;
        private final boolean finished;

        private LineMeasurementProxy(Point2D startPoint, Point2D endPoint, String customLabel, boolean finished)
        {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.customLabel = customLabel;
            this.finished = finished;
        }

        @Override
        public DistanceMeasurementDrawable recreateOriginalObject(DistanceMeasurementStyle style, Integer key)
        {
            boolean isCustomLabel = (customLabel != null);
            DistanceLineMeasurement measurement = isCustomLabel ? new DistanceLineMeasurement(startPoint, endPoint, key, customLabel, style) : new DistanceLineMeasurement(startPoint, endPoint, key, style);
            measurement.setFinished(finished);

            return measurement;
        }       
    }
}

