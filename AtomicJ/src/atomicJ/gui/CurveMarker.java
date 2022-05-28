
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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

import atomicJ.utilities.Validation;


public class CurveMarker extends AbstractXYAnnotation
implements Cloneable, PublicCloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private final Object seriesKey;

    private int pixelWidth;
    private int pixelHeight;

    private Stroke outlineStroke;
    private Paint outlinePaint;
    private Paint fillPaint;

    private boolean visible = true;
    private Point2D controlPoint;
    private transient Shape hotArea;

    public CurveMarker(Point2D controlPoint, Object seriesKey, int pixelWidth, int pixelHeight) {
        this(controlPoint, seriesKey, pixelWidth, pixelHeight, new BasicStroke(1.0f), Color.black);
    }

    public CurveMarker(Point2D controlPoint, Object seriesKey, int pixelWidth,int pixelHeight, Stroke outlineStroke, Paint outlinePaint) {
        this(controlPoint, seriesKey, pixelWidth, pixelHeight,outlineStroke, outlinePaint, null);
    }

    public CurveMarker(Point2D controlPoint, Object seriesKey, int pixelWidth, int pixelHeight, Stroke outlineStroke, Paint outlinePaint,
            Paint fillPaint) 
    {
        this.controlPoint = Validation.requireNonNullParameterName(controlPoint, "controlPoint");
        this.seriesKey = Validation.requireNonNullParameterName(seriesKey, "seriesKey");
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.outlineStroke = outlineStroke;
        this.outlinePaint = outlinePaint;
        this.fillPaint = fillPaint;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,
            int rendererIndex,
            PlotRenderingInfo info) 
    {

        if(isVisible())
        {
            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            double controlX = domainAxis.valueToJava2D(controlPoint.getX(), dataArea, domainEdge);
            double controlY = rangeAxis.valueToJava2D(controlPoint.getY(), dataArea, rangeEdge);


            boolean isVertical = (orientation == PlotOrientation.VERTICAL);

            double X0 = isVertical ? controlX : controlY;
            double Y0 = isVertical ? controlY : controlX;

            double X1 = isVertical ? X0 - 0.5*pixelWidth: X0 - pixelHeight;
            double Y1 = isVertical ? Y0 + pixelHeight : Y0 - 0.5*pixelWidth;

            double X2 = isVertical ? X0 + 0.5*pixelWidth : X0 - pixelHeight;
            double Y2 = isVertical ? Y0 + pixelHeight: Y0 + 0.5*pixelWidth;

            GeneralPath s = new GeneralPath();
            s.moveTo(X0, Y0);
            s.lineTo(X1, Y1);
            s.lineTo(X2, Y2);
            s.closePath();

            if (this.fillPaint != null) 
            {
                g2.setPaint(this.fillPaint);
                g2.fill(s);
            }

            if (this.outlineStroke != null && this.outlinePaint != null) 
            {
                g2.setPaint(this.outlinePaint);
                g2.setStroke(this.outlineStroke);
                g2.draw(s);
            }

            this.hotArea = s;
            addEntity(info, s, rendererIndex, getToolTipText(), getURL());
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

    public Object getSeriesKey()
    {
        return seriesKey;
    }

    public boolean isClicked(Point2D java2DPoint)
    {
        boolean clicked = false;

        if(isVisible())
        {
            clicked = hotArea.contains(java2DPoint); 		
        }

        return clicked;
    }

    public boolean isClicked(Rectangle2D probingArea)
    {
        boolean clicked = false;

        if(isVisible())
        {
            clicked = hotArea.intersects(probingArea); 		
        }

        return clicked;
    }

    public int getPixelWidth()
    {
        return pixelWidth;
    }

    public void setPixelWidth(int pixelWidthNew)
    {
        this.pixelWidth = pixelWidthNew;
        fireAnnotationChanged();
    }

    public int getPixelHeight()
    {
        return pixelHeight;
    }

    public void setPixelHeight(int pixelHeightNew)
    {
        this.pixelHeight = pixelHeightNew;
        fireAnnotationChanged();
    }

    public Point2D getControlPoint()
    {
        return controlPoint;
    }

    public void setControlPoint(Point2D controlPointNew)
    {
        this.controlPoint = controlPointNew;
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
    @Override
    public boolean equals(Object obj) 
    {
        if (obj == this) 
        {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CurveMarker)) {
            return false;
        }
        CurveMarker that = (CurveMarker) obj;

        if(!this.controlPoint.equals(that.controlPoint))
        {
            return false;
        }

        if (this.pixelWidth != that.pixelWidth) {
            return false;
        }
        if (this.pixelHeight != that.pixelHeight) {
            return false;
        }
        if (!Objects.equals(this.outlineStroke, that.outlineStroke)) {
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
}
