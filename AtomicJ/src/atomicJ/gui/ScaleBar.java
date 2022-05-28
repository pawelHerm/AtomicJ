
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.prefs.Preferences;


import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.LineUtilities;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

import atomicJ.data.units.Quantity;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;


public class ScaleBar extends AbstractXYAnnotation
{
    private static final long serialVersionUID = 1L;

    private boolean visible = true;

    private float labelOffset;
    private float lengthwisePosition;

    private boolean labelVisible;	
    private Stroke stroke;
    private Paint strokePaint;		
    private Paint labelPaint;	
    private Font labelFont;

    private double x;
    private double y;

    private boolean lengthAutomatic;
    private double length;

    private final boolean vertical;

    private final Preferences pref;

    public ScaleBar(boolean vertical, Preferences pref) 
    {	
        super();

        Validation.requireNonNullParameterName(pref, "pref");

        this.pref = pref;
        this.vertical = vertical;

        setConsistentWithStyle(pref, false);

    }

    public ScaleBar(boolean vertical, PreferredScaleBarStyle style) 
    {   
        super();

        Validation.requireNonNullParameterName(style, "style");

        this.pref = style.getPreferences();
        this.vertical = vertical;

        setConsistentWithStyle(style, false);
    }

    public ScaleBar(ScaleBar that, Preferences pref)
    {
        this.visible = that.visible;

        this.pref = pref;
        this.vertical = that.vertical;

        setConsistentWithStyle(pref, false);
    }

    public ScaleBar(ScaleBar that, PreferredScaleBarStyle style)
    {
        this.visible = that.visible;

        this.pref = style.getPreferences();
        this.vertical = that.vertical;

        setConsistentWithStyle(style, false);
    }

    private void setConsistentWithStyle(PreferredScaleBarStyle style, boolean notify)
    {
        this.visible = style.isVisible();
        this.lengthAutomatic = style.isLengthAutomatic();

        this.strokePaint = style.getStrokePaint();
        this.stroke = style.getStroke();

        this.labelPaint = style.getLabelPaint();
        this.labelFont = style.getLabelFont();


        this.x = style.getPositionX();
        this.y = style.getPositionY();

        this.labelVisible = style.isLabelVisible();
        this.labelOffset = style.getLabelOffset();
        this.lengthwisePosition = style.getLabelLengthwisePosition();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    private void setConsistentWithStyle(Preferences pref, boolean notify)
    {
        this.visible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_VISIBLE, false);
        this.lengthAutomatic = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LENGTH_AUTOMATIC, true);

        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_STROKE_PAINT, Color.black);
        this.stroke = SerializationUtilities.getStroke(pref, PreferredScaleBarStyle.SCALEBAR_STROKE, new BasicStroke(3.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));


        this.x = pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_X, 0.8);
        this.y = pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_Y, 0.1);

        this.labelVisible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LABEL_VISIBLE, true);
        this.labelOffset = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_OFFSET, 0.f);
        this.lengthwisePosition = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_POSITION, 0.5f);

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public boolean isVertical()
    {
        return vertical;
    }

    // POSITION X

    public double getPositionX()
    {
        return x;
    }

    public void setPositionX(double x)
    {
        setPositionX(x, true);
    }

    public void setPositionX(double x, boolean notify)
    {
        this.x = x;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    // POSITION Y

    public double getPositionY()
    {
        return y;
    }

    public void setPositionY(double y)
    {
        setPositionY(y, true);
    }

    public void setPositionY(double y, boolean notify)
    {
        this.y = y;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    // LENGTH

    public double getLength()
    {
        return length;
    }

    public void setLength(double length)
    {
        setLength(length, true);
    }

    public void setLength(double length, boolean notify)
    {
        this.length = length;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    // SCALE BAR LENGTH AUTOMATIC

    public boolean isLengthAutomatic()
    {
        return lengthAutomatic;
    }

    public void setLengthAutomatic(boolean lengthAutomatic)
    {
        setLengthAutomatic(lengthAutomatic, true);
    }

    public void setLengthAutomatic(boolean lengthAutomatic, boolean notify)
    {
        this.lengthAutomatic = lengthAutomatic;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    // SCALE BAR VISIBLE

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visibleNew)
    {
        setVisible(visibleNew, true);
    }

    public void setVisible(boolean visibleNew, boolean notify)
    {
        this.visible = visibleNew;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    // LABEL VISIBLE

    public boolean isLabelVisible()
    {
        return labelVisible;
    }	

    public void setLabelVisible(boolean labelVisible)
    {
        setLabelVisible(labelVisible, true);
    }

    public void setLabelVisible(boolean labelVisible, boolean notify)
    {

        this.labelVisible = labelVisible;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    //LABEL PAINT

    public Paint getLabelPaint()
    {		
        return labelPaint;
    }

    public void setLabelPaint(Paint paint)
    {
        setLabelPaint(paint, true);
    }

    public void setLabelPaint(Paint paint, boolean notify)
    {
        Validation.requireNonNullParameterName(paint, "paint");

        this.labelPaint = paint;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    //STROKE PAINT

    public Paint getStrokePaint()
    {
        return strokePaint;
    }

    public void setStrokePaint(Paint paint)
    {
        setStrokePaint(paint, true);
    }

    public void setStrokePaint(Paint paint, boolean notify)
    {
        Validation.requireNonNullParameterName(paint, "paint");

        this.strokePaint = paint;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    //STROKE

    public Stroke getStroke()
    {
        return stroke;
    }

    public void setStroke(Stroke stroke)
    {
        setStroke(stroke, true);
    }

    public void setStroke(Stroke stroke, boolean notify)
    {
        Validation.requireNonNullParameterName(stroke, "stroke");

        this.stroke = stroke;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    //LABEL FONT

    public Font getLabelFont()
    {
        return labelFont;
    }

    public void setLabelFont(Font font)
    {
        setLabelFont(font, true);
    }

    public void setLabelFont(Font font, boolean notify)
    {
        Validation.requireNonNullParameterName(font, "font");

        this.labelFont = font;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }
    //LABEL LENGTHWISE POSITION

    public float getLabelLengthwisePosition()
    {
        return lengthwisePosition;
    }

    public void setLabelLengthwisePosition(float lengthwisePosition)
    {
        setLabelLengthwisePosition(lengthwisePosition, true);
    }

    public void setLabelLengthwisePosition(float lengthwisePosition, boolean notify)
    {
        this.lengthwisePosition = lengthwisePosition;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    //LABEL OFFSET

    public float getLabelOffset()
    {
        return labelOffset;
    }

    public void setLabelOffset(float labelOffset)
    {
        setLabelOffset(labelOffset, true);
    }

    public void setLabelOffset(float labelOffset, boolean notify)
    {
        this.labelOffset = labelOffset;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public boolean equals(Object that)
    {
        return this == that;
    }

    public void move(Point2D caughtPoint, Point2D currentPoint, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis)
    {
        double dX = currentPoint.getX() - caughtPoint.getX();
        double dY = currentPoint.getY() - caughtPoint.getY();


        Range xRange = domainAxis.getRange();
        Range yRange = rangeAxis.getRange();

        double dx = dX/xRange.getLength();
        double dy = dY/yRange.getLength();

        double x = this.x + dx;
        double y = this.y + dy;

        setPositionX(x, false);
        setPositionY(y, true);

    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,int rendererIndex, PlotRenderingInfo info) 
    {
        if(!isVisible())
        {
            return;
        }
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

        Range xRange = domainAxis.getRange();
        Range yRange = rangeAxis.getRange();

        double anchorX = xRange.getLowerBound() + (this.x * xRange.getLength());
        double anchorY = yRange.getLowerBound() + (this.y * yRange.getLength());    

        double actualLength = lengthAutomatic ? (vertical ? ((NumberAxis)rangeAxis).getTickUnit().getSize() :((NumberAxis)domainAxis).getTickUnit().getSize()): length;

        double x1 = vertical ? anchorX : anchorX - 0.5*actualLength;
        double x2 = vertical ? anchorX : anchorX + 0.5*actualLength;
        double y1 = vertical ? anchorY - 0.5*actualLength : anchorY;
        double y2 = vertical ? anchorY + 0.5*actualLength : anchorY;

        float j2DX1 = 0.0f;
        float j2DX2 = 0.0f;
        float j2DY1 = 0.0f;
        float j2DY2 = 0.0f;

        float j2DCenterX = 0.0f;
        float j2DCenterY = 0.0f;

        boolean plotOrientationVertical = (orientation == PlotOrientation.VERTICAL);

        if (plotOrientationVertical) 
        {
            j2DX1 = (float) domainAxis.valueToJava2D(x1, dataArea, domainEdge);
            j2DY1 = (float) rangeAxis.valueToJava2D(y1, dataArea, rangeEdge);
            j2DX2 = (float) domainAxis.valueToJava2D(x2, dataArea, domainEdge);
            j2DY2 = (float) rangeAxis.valueToJava2D(y2, dataArea, rangeEdge);

            j2DCenterX =(float) domainAxis.valueToJava2D(anchorX, dataArea, domainEdge);
            j2DCenterY =(float) rangeAxis.valueToJava2D(anchorY, dataArea, rangeEdge);

        }
        else 
        {
            j2DY1 = (float) domainAxis.valueToJava2D(x1, dataArea, domainEdge);
            j2DX1 = (float) rangeAxis.valueToJava2D(y1, dataArea, rangeEdge);
            j2DY2 = (float) domainAxis.valueToJava2D(x2, dataArea, domainEdge);
            j2DX2 = (float) rangeAxis.valueToJava2D(y2, dataArea, rangeEdge);

            j2DCenterX =(float) rangeAxis.valueToJava2D(anchorY, dataArea, rangeEdge);
            j2DCenterY =(float) domainAxis.valueToJava2D(anchorX, dataArea, domainEdge);

        }

        g2.setPaint(getStrokePaint());
        g2.setStroke(getStroke());
        Line2D line = new Line2D.Float(j2DX1, j2DY1, j2DX2, j2DY2);

        boolean visible = LineUtilities.clipLine(line, dataArea);

        if (visible) 
        {
            g2.draw(line);
        }

        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) 
        {
            addEntity(info, ShapeUtilities.createLineRegion(line, 1.0f), rendererIndex, toolTip, url);
        }

        Shape lineShape = stroke.createStrokedShape(line);
        Rectangle2D lineBounds = lineShape.getBounds2D();

        Area area = new Area(lineBounds);

        //draw the label

        if(isLabelVisible())
        {
            Quantity quantity = vertical ? ((CustomizableNumberAxis)rangeAxis).getDataQuantity() : ((CustomizableNumberAxis)domainAxis).getDataQuantity();

            int fractionCount = MathUtilities.getFractionCount(actualLength);

            NumberFormat format = NumberFormat.getInstance(Locale.US);
            format.setMaximumFractionDigits(fractionCount);

            String label = format.format(actualLength) + " " + quantity.getFullUnitName();


            g2.setFont(getLabelFont());	   
            g2.setPaint(getLabelPaint());

            Rectangle2D labelArea;

            boolean labelVertical = (vertical == plotOrientationVertical);

            if(labelVertical)
            {
                FontMetrics fontMetrics = g2.getFontMetrics();
                Rectangle2D stringBounds = fontMetrics.getStringBounds(label, g2).getBounds2D();

                double X = j2DCenterX - labelOffset - fontMetrics.getAscent() - lineBounds.getWidth()/2;
                double Y = j2DCenterY - stringBounds.getWidth()/2.;

                AffineTransform trOld = g2.getTransform();
                g2.translate(X, Y);
                g2.rotate(Math.PI/2.0);
                g2.drawString(label, 0, 0);
                g2.setTransform(trOld);

                labelArea = new Rectangle2D.Double(X - fontMetrics.getDescent(), Y, stringBounds.getHeight() + fontMetrics.getDescent(), stringBounds.getWidth());	        	
            }
            else
            {
                FontMetrics fontMetrics = g2.getFontMetrics();
                Rectangle2D stringBounds = fontMetrics.getStringBounds(label, g2).getBounds2D();

                int X = (int) Math.rint(j2DCenterX - stringBounds.getWidth()/2);
                int Y = (int) Math.rint(j2DCenterY + labelOffset + fontMetrics.getAscent() + lineBounds.getHeight()/2);

                g2.drawString(label, X, Y);
                labelArea = new Rectangle2D.Double(X, Y - fontMetrics.getAscent(), stringBounds.getWidth(), stringBounds.getHeight() + fontMetrics.getDescent());

            }

            area.add(new Area(labelArea));

        } 

        if (info != null) 
        {
            EntityCollection entities = info.getOwner().getEntityCollection();
            if (entities == null) 
            {
                return;
            }
            ScaleBarEntity entity = new ScaleBarEntity(area.getBounds2D(), this) ;
            entities.add(entity);	    
        }

    }

}

