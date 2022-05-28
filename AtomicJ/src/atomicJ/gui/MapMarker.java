
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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.BasicAnnotationAnchor;

import static atomicJ.gui.MapMarkerStyle.*;


public class MapMarker extends AbstractXYAnnotation
implements Cloneable, PublicCloneable, Serializable, PropertyChangeListener 
{
    private static final long serialVersionUID = 1L;

    private final Integer key;

    private String label = "";
    private String curveName = "";
    private String positionDescription = "";

    private boolean labelVisible;
    private float labelOffset;
    private Font labelFont;
    private Paint labelPaint;
    private MapMarkerLabelType labelType;

    private int markerIndex;
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

    private Map<String, String> valueLabels = new LinkedHashMap<>();
    private final MapMarkerStyle style;

    public MapMarker(Point2D controlPoint, Integer key, MapMarkerStyle style) 
    {
        if (controlPoint == null) 
        {
            throw new IllegalArgumentException("Null 'controlPoint' argument.");
        }

        this.x = controlPoint.getX();
        this.y = controlPoint.getY();

        this.key = key;
        this.style = style;

        setConsistentWithStyle(style, false);
        style.addPropertyChangeListener(this);
    }

    public MapMarker(MapMarker that)
    {
        this(that, that.getStyle());
    }

    public MapMarker(MapMarker that, MapMarkerStyle style) 
    {
        this.key = that.key;
        this.visible = that.visible;

        this.curveName = that.curveName;
        this.positionDescription = that.positionDescription;
        this.valueLabels = new LinkedHashMap<>(that.valueLabels);

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

    public MapMarker copy()
    {
        return new MapMarker(this);
    }

    public MapMarker copy(MapMarkerStyle style)
    {
        return new MapMarker(this, style);
    }

    public void updateLabel()
    {        
        String label = labelType.getLabel(this);        
        setLabel(label);
    }  

    public String getCurveName()
    {
        return curveName;
    }

    public void setCurveName(String sourceNameNew)
    {   
        this.curveName = sourceNameNew;
        updateLabel();
    }

    public String getPositionDescription()
    {
        return positionDescription;
    }

    public void setPositionDescription(String positionDescriptionNew)
    {   
        this.positionDescription = positionDescriptionNew;
        updateLabel();
    }

    public String getValueLabel(String channelType)
    {
        String label = "";

        if(valueLabels.containsKey(channelType))
        {
            label = valueLabels.get(channelType);          
        }

        return label;
    }

    public Map<String, String> getValueLabels()
    {
        return new LinkedHashMap<>(valueLabels);
    }

    public void setValueLabels(Map<String, String> valueLabelsNew)
    {
        this.valueLabels.clear();
        this.valueLabels.putAll(valueLabelsNew);

        updateLabel();
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

        if(hotArea != null && hotArea.contains(java2DPoint))
        {
            anchor = BasicAnnotationAnchor.CENTER;
        }

        return anchor;      
    }

    private void setConsistentWithStyle(MapMarkerStyle style, boolean notify)
    {
        this.labelVisible = style.isLabelVisible();
        this.labelOffset = style.getLabelOffset();
        this.labelPaint = style.getLabelPaint();
        this.labelFont = style.getLabelFont();
        this.labelType = style.getLabelType();

        this.filled = style.isFilled();
        this.markerIndex = style.getMarkerIndex();
        this.markerSize = style.getMarkerSize();

        this.outlineVisible = style.isStrokeVisible();
        this.outlineStroke = style.getStroke();
        this.outlinePaint = style.getPaint();
        this.fillPaint = style.getFillPaint();

        updateLabel();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,int rendererIndex, PlotRenderingInfo info) 
    {
        if(isVisible())
        {
            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            double j2Dx = domainAxis.valueToJava2D(x, dataArea, domainEdge);
            double j2Dy = rangeAxis.valueToJava2D(y, dataArea, rangeEdge);

            Shape shape = ShapeSupplier.createShape(markerIndex, markerSize);
            boolean isVertical = (orientation == PlotOrientation.VERTICAL);

            if (isVertical) 
            {
                shape = ShapeUtilities.createTranslatedShape(shape, j2Dx, j2Dy);
            }
            else
            {
                shape = ShapeUtilities.createTranslatedShape(shape, j2Dy, j2Dx);
            }

            if (this.fillPaint != null && filled) 
            {
                g2.setPaint(this.fillPaint);
                g2.fill(shape);
            }

            if (this.outlineStroke != null && this.outlinePaint != null && outlineVisible) 
            {
                g2.setPaint(this.outlinePaint);
                g2.setStroke(this.outlineStroke);
                g2.draw(shape);
            }

            if(this.label != null && isLabelVisible())
            {                
                float textX =  (float) shape.getBounds2D().getCenterX();
                float textY = (float) (shape.getBounds2D().getMinY() - labelOffset);

                g2.setFont(this.labelFont);
                g2.setPaint(this.labelPaint);

                TextUtilities.drawAlignedString(label, g2, textX, textY,TextAnchor.BOTTOM_CENTER);
            }           

            this.hotArea = shape.getBounds2D();
            addEntity(info, shape, rendererIndex, getToolTipText(), getURL());
        }
    }

    public String getLabel()
    {
        return label;
    }

    public float getLabelOffset()
    {
        return labelOffset;
    }

    public void setLabelOffset(float labelOffsetNew)
    {
        this.labelOffset = labelOffsetNew;
        fireAnnotationChanged();
    }

    public void setLabel(String labelNew)
    {
        this.label = labelNew;
        fireAnnotationChanged();
    }

    public boolean isLabelVisible()
    {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisibleNew)
    {
        this.labelVisible = labelVisibleNew;
        fireAnnotationChanged();
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public void setLabelFont(Font labelFontNew)
    {
        this.labelFont = labelFontNew;
        fireAnnotationChanged();
    }

    public Paint getLabelPaint()
    {
        return labelPaint;
    }

    public void setLabelPaint(Paint labelPaintNew)
    {
        this.labelPaint = labelPaintNew;
        fireAnnotationChanged();
    }

    public MapMarkerLabelType getLabelType()
    {
        return labelType;
    }

    public void setLabelType(MapMarkerLabelType labelTypeNew)
    {
        this.labelType = labelTypeNew;
        updateLabel();
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

    public Integer getKey()
    {
        return key;
    }

    //other annotations - rois and profiles - use data points
    public boolean isClicked(Point2D java2DPoint)
    {
        boolean clicked = false;

        if(isVisible())
        {
            return hotArea.contains(java2DPoint); 		
        }

        return clicked;
    }

    //other annotations - rois and profiles - use rectangles in data cordinates
    public boolean isClicked(Rectangle2D java2DProbingArea)
    {
        boolean clicked = false;

        if(isVisible())
        {
            clicked = hotArea.intersects(java2DProbingArea); 		
        }

        return clicked;
    }

    public int getMarkerIndex()
    {
        return markerIndex;
    }

    public void setMarkerIndex(int i)
    {
        this.markerIndex = i;

        fireAnnotationChanged();
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

    public Point2D getControlDataPoint()
    {
        return new Point2D.Double(x, y);
    }

    public void setControlPoint(Point2D controlPointNew)
    {
        this.x = controlPointNew.getX();
        this.y = controlPointNew.getY();
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

    public boolean equalsUpToStyle(MapMarker that)
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
        if (!(obj instanceof MapMarker)) {
            return false;
        }
        MapMarker that = (MapMarker) obj;

        if(this.x != that.x)
        {
            return false;
        }
        if(this.y != that.y)
        {
            return false;
        }
        if(this.labelVisible != that.labelVisible)
        {
            return false;
        }
        if (this.markerIndex != that.markerIndex) {
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
        if (!PaintUtilities.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        if(!ObjectUtilities.equal(this.labelPaint, that.labelPaint))
        {
            return false;
        }
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(MAP_MARKER_INDEX.equals(property))
        {
            int markerIndex = (int)evt.getNewValue();
            setMarkerIndex(markerIndex);
        }
        else if(MAP_MARKER_SIZE.equals(property))
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
        else if(MAP_MARKER_LABEL_TYPE.equals(property))
        {
            MapMarkerLabelType labelType = (MapMarkerLabelType)evt.getNewValue();
            setLabelType(labelType);
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
        else if(ANNOTATION_LABEL_VISIBLE.equals(property))
        {
            boolean visible = (boolean)evt.getNewValue();
            setLabelVisible(visible);
        }
        else if(ANNOTATION_LABEL_OFFSET.equals(property))
        {
            float offset = (float)evt.getNewValue();
            setLabelOffset(offset);
        }
        else if(ANNOTATION_LABEL_FONT.equals(property))
        {
            Font font = (Font)evt.getNewValue();
            setLabelFont(font);
        }
        else if(ANNOTATION_LABEL_PAINT.equals(property))
        {
            Paint paint = (Paint)evt.getNewValue();
            setLabelPaint(paint);
        }
        else if(ANNOTATION_STYLE_COMPLETELY_CHANGED.equals(property))
        {
            setConsistentWithStyle(style, true);
        }
    }
}
