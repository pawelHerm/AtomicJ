
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

package atomicJ.gui;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import static atomicJ.gui.MapMarkerStyle.*;


public class RotationCenterMarker extends AbstractXYAnnotation
implements Cloneable, PublicCloneable, Serializable, PropertyChangeListener 
{
    private static final long serialVersionUID = 1L;

    private float markerSize;

    private boolean outlineVisible;
    private Stroke outlineStroke;
    private Paint outlinePaint;

    private boolean filled;
    private Paint fillPaint;

    private boolean visible = true;
    private double x;
    private double y;
    private transient Shape hotArea = new Area();

    private final MapMarkerStyle style;

    public RotationCenterMarker(double x, double y, MapMarkerStyle style) 
    {     
        this.x = x;
        this.y = y;

        this.style = style;

        setConsistentWithStyle(style, false);
        style.addPropertyChangeListener(this);
    }

    public RotationCenterMarker(RotationCenterMarker that)
    {
        this(that, that.getStyle());
    }

    public RotationCenterMarker(RotationCenterMarker that, MapMarkerStyle style) 
    {
        this.visible = that.visible;

        this.x = that.x;
        this.y = that.y;

        this.style = style;

        setConsistentWithStyle(style, false);
        style.addPropertyChangeListener(this);
    }

    public MapMarkerStyle getStyle()
    {
        return style;
    }

    public RotationCenterMarker copy()
    {
        return new RotationCenterMarker(this);
    }

    public RotationCenterMarker copy(MapMarkerStyle style)
    {
        return new RotationCenterMarker(this, style);
    }

    public boolean setPosition(AnnotationAnchorCore anchor, Point2D startPoint, Point2D endPoint)
    {
        if(anchor == null)
        {
            return false;
        }   

        if(BasicAnnotationAnchor.CENTER.equals(anchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x = x + tx;
            this.y = y + ty;

            fireAnnotationChanged();
        }

        return false;
    }

    public AnnotationAnchorCore getCaughtAnchor(Point2D java2DPoint, Rectangle2D r)
    {
        AnnotationAnchorCore anchor = null;

        if(hotArea.contains(java2DPoint))
        {
            anchor = BasicAnnotationAnchor.CENTER;
        }

        return anchor;      
    }

    private void setConsistentWithStyle(MapMarkerStyle style, boolean notify)
    {

        this.filled = style.isFilled();
        this.markerSize = style.getMarkerSize();

        this.outlineVisible = style.isStrokeVisible();
        this.outlineStroke = style.getStroke();
        this.outlinePaint = style.getPaint();
        this.fillPaint = style.getFillPaint();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {
        if(isVisible())
        {
            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            double j2Dx = domainAxis.valueToJava2D(x, dataArea, domainEdge);
            double j2Dy = rangeAxis.valueToJava2D(y, dataArea, rangeEdge);

            double markerSize = 2*this.markerSize;

            Shape circle = new Ellipse2D.Double(j2Dx - markerSize/2, j2Dy - markerSize/2, markerSize, markerSize);
            Shape crosshairVerticleLine = new Line2D.Double(j2Dx, j2Dy - markerSize/2, j2Dx, j2Dy + markerSize/2);
            Shape crosshairHorizontalLine = new Line2D.Double(j2Dx - markerSize/2, j2Dy, j2Dx + markerSize/2, j2Dy);

            if (this.fillPaint != null && filled) 
            {
                g2.setPaint(this.fillPaint);
                g2.fill(circle);
            }

            if (this.outlineStroke != null && this.outlinePaint != null && outlineVisible) 
            {
                g2.setPaint(this.outlinePaint);
                g2.setStroke(this.outlineStroke);
                g2.draw(circle);
                g2.draw(crosshairVerticleLine);
                g2.draw(crosshairHorizontalLine);
            }


            this.hotArea = circle.getBounds2D();
            addEntity(info, circle, rendererIndex, getToolTipText(), getURL());
        }
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visibleNew)
    {
        this.visible = visibleNew;
        fireAnnotationChanged();
    }

    //other annotations - rois and profiles - use data points
    public boolean isClicked(Point2D java2DPoint)
    {
        boolean clicked = isVisible() && hotArea.contains(java2DPoint);

        return clicked;
    }

    //other annotations - rois and profiles - use rectangles in data coordinates
    public boolean isClicked(Rectangle2D java2DProbingArea)
    {
        boolean clicked = isVisible() && hotArea.intersects(java2DProbingArea);

        return clicked;
    }

    public float getMarkerSize()
    {
        return markerSize;
    }

    public void setMarkerSize(float size)
    {
        this.markerSize = size;

        fireAnnotationChanged();
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public void setControlPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
        fireAnnotationChanged();
    }

    public boolean isOutlineVisible()
    {
        return outlineVisible;
    }

    public void setOutlineVisible(boolean outlineVisibleNew)
    {
        this.outlineVisible = outlineVisibleNew;
        fireAnnotationChanged();
    }

    public void setOutlineStroke(Stroke outlineStrokeNew)
    {
        setOutlineStroke(outlineStrokeNew, true);
    }

    public void setOutlineStroke(Stroke outlineStrokeNew, boolean notify)
    {
        this.outlineStroke = outlineStrokeNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public void setOutlinePaint(Paint outlinePaintNew)
    {
        setOutlinePaint(outlinePaintNew, true);
    }

    public void setOutlinePaint(Paint outlinePaintNew, boolean notify)
    {
        this.outlinePaint = outlinePaintNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public boolean isFilled()
    {
        return filled;
    }

    public void setFilled(boolean filledNew)
    {
        this.filled = filledNew;
        fireAnnotationChanged();
    }


    public void setFillPaint(Paint fillPaintNew)
    {
        setFillPaint(fillPaintNew, true);
    }

    public void setFillPaint(Paint fillPaintNew, boolean notify)
    {
        this.fillPaint = fillPaintNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public boolean equalsUpToStyle(RotationCenterMarker that)
    {       
        if(that == null)
        {
            return false;
        }

        if(this.x != that.x)
        {
            return false;
        }
        if(this.y != that.y)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj == this) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof RotationCenterMarker)) {
            return false;
        }
        RotationCenterMarker that = (RotationCenterMarker) obj;

        if(this.x != that.x)
        {
            return false;
        }
        if(this.y != that.y)
        {
            return false;
        }

        if (this.markerSize != that.markerSize) {
            return false;
        }
        if(this.outlineVisible != that.outlineVisible)
        {
            return false;
        }
        if(this.filled != that.filled)
        {
            return false;
        }
        if (!ObjectUtilities.equal(this.outlineStroke, that.outlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
            return false;
        }

        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(MAP_MARKER_SIZE.equals(property))
        {
            float markerSize = (float)evt.getNewValue();
            setMarkerSize(markerSize);
        }
        else if(MAP_MARKER_FILLED.equals(property))
        {
            boolean filled = (boolean)evt.getNewValue();
            setFilled(filled);
        }
        else if(MAP_MARKER_FILL_PAINT.equals(property))
        {
            Paint paint = (Paint)evt.getNewValue();
            setFillPaint(paint);
        }       
        else if(ANNOTATION_STROKE_VISIBLE.equals(property))
        {
            boolean outlineVisible = (boolean)evt.getNewValue();
            setOutlineVisible(outlineVisible);
        }
        else if(ANNOTATION_STROKE_PAINT.equals(property))
        {
            Paint paint = (Paint)evt.getNewValue();
            setOutlinePaint(paint);
        }
        else if(ANNOTATION_STROKE.equals(property))
        {
            Stroke stroke = (Stroke)evt.getNewValue();
            setOutlineStroke(stroke);
        }

        else if(ANNOTATION_STYLE_COMPLETELY_CHANGED.equals(property))
        {
            setConsistentWithStyle(style, true);
        }
    }
}
