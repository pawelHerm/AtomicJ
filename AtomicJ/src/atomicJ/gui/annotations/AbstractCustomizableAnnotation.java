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

package atomicJ.gui.annotations;

import static atomicJ.gui.PreferredAnnotationStyle.*;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.Identifiable;
import atomicJ.gui.LabelAutomaticType;
import atomicJ.gui.DistanceShapeFactors.DirectedPosition;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.Validation;

public abstract class AbstractCustomizableAnnotation extends AbstractXYAnnotation implements PropertyChangeListener, Identifiable 
{
    private static final long serialVersionUID = 1L;

    private final Integer key;

    private boolean visible = true;
    private boolean highlighted = false;
    private boolean finished = false;

    private float labelOffset;
    private float lengthwisePosition;

    private boolean labelUnfinishedVisible;
    private boolean labelFinishedVisible;

    private Stroke strokeUnfinished;
    private Stroke strokeFinished;

    private Paint paintUnfinished;
    private Paint paintFinished;

    private String label;
    private LabelAutomaticType labelType;
    private boolean isCustomLabel;

    private Paint paintLabelUnfinished;
    private Paint paintLabelFinished;

    private Font labelFontUnfinished;
    private Font labelFontFinished;

    private final AnnotationStyle<?> style;

    public AbstractCustomizableAnnotation(Integer key, AnnotationStyle<?> style)
    {
        Validation.requireNonNullParameterName(style, "style");

        this.key = key;
        this.style = style;

        setConsistentWithStyle(style, false);
        updateAutomaticLabelPrivate();

        style.addPropertyChangeListener(this);
    }

    public AbstractCustomizableAnnotation(Integer key, String label, AnnotationStyle<?> style) 
    {
        Validation.requireNonNullParameterName(label, "label");
        Validation.requireNonNullParameterName(style, "style");

        this.key = key;
        this.label = label;
        this.style = style;

        setConsistentWithStyle(style, false);
        checkIfCustomLabel(label);

        style.addPropertyChangeListener(this);
    }

    public AbstractCustomizableAnnotation(AbstractCustomizableAnnotation that) {
        this(that, that.getStyle());
    }

    public AbstractCustomizableAnnotation(AbstractCustomizableAnnotation that, AnnotationStyle<?> style) 
    {
        this.key = that.key;
        this.visible = that.visible;
        this.highlighted = that.highlighted;
        this.finished = that.finished;
        this.isCustomLabel = that.isCustomLabel;
        if(isCustomLabel)
        {
            this.label = that.label;
        }

        this.style = style;

        setConsistentWithStyle(style, false);
        style.addPropertyChangeListener(this);

        updateAutomaticLabelPrivate();
    }

    public AbstractCustomizableAnnotation(AbstractCustomizableAnnotation that,
            AnnotationStyle<?> style, Integer key, String label)
    {
        this.key = key;
        this.visible = that.visible;
        this.highlighted = that.highlighted;
        this.finished = that.finished;

        this.style = style;
        this.label = label;

        setConsistentWithStyle(style, false);
        style.addPropertyChangeListener(this);

        checkIfCustomLabel(label);
    }

    private void setConsistentWithStyle(AnnotationStyle<?> style, boolean notify) 
    {
        this.labelType = style.getLabelType();
        this.labelOffset = style.getLabelOffset();
        this.lengthwisePosition = style.getLabelLengthwisePosition();

        this.labelFontUnfinished = style.getLabelFontUnfinished();
        this.labelFontFinished = style.getLabelFontFinished();

        this.labelUnfinishedVisible = style.isLabelVisibleUnfinished();
        this.labelFinishedVisible = style.isLabelVisibleFinished();

        this.strokeUnfinished = style.getStrokeUnfinished();
        this.strokeFinished = style.getStrokeFinished();

        this.paintUnfinished = style.getPaintUnfinished();
        this.paintFinished = style.getPaintFinished();

        this.paintLabelUnfinished = style.getPaintLabelUnfinished();
        this.paintLabelFinished = style.getPaintLabelFinished();

        if (notify) {
            fireAnnotationChanged();
        }
    }
    protected AffineTransform getDataToJava2DTransformation(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis,
            PlotRenderingInfo info) 
    {
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);

        // compute transform matrix elements via sample points. Assume no
        // rotation or shear.
        //        Rectangle2D bounds = shape.getBounds2D();
        double x0 = 0;
        double x1 = 1;
        double xx0 = domainAxis.valueToJava2D(x0, dataArea, domainEdge);
        double xx1 = domainAxis.valueToJava2D(x1, dataArea, domainEdge);

        double m00 = (xx1 - xx0) / (x1 - x0);
        double m02 = xx0 - x0 * m00;

        double y0 = 0;
        double y1 = 1;
        double yy0 = rangeAxis.valueToJava2D(y0, dataArea, rangeEdge);
        double yy1 = rangeAxis.valueToJava2D(y1, dataArea, rangeEdge);

        double m11 = (yy1 - yy0) / (y1 - y0);
        double m12 = yy0 - m11 * y0;

        //  create transform & transform shape
        AffineTransform transform = null;
        if (orientation == PlotOrientation.HORIZONTAL) 
        {
            AffineTransform t1 = new AffineTransform(0.0f, 1.0f, 1.0f, 0.0f,
                    0.0f, 0.0f);
            transform = new AffineTransform(m11, 0.0f, 0.0f, m00,
                    m12, m02);
            transform.concatenate(t1);
        }
        else if (orientation == PlotOrientation.VERTICAL) 
        {
            transform = new AffineTransform(m00, 0, 0, m11, m02, m12);
        }

        return transform;
    }

    protected void drawKeyLabel(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Shape supportingTransformedShape)
    {        
        if(isLabelVisible())
        { 
            float pos = getLabelLengthwisePosition();
            double measurementLength = DistanceShapeFactors.getLength(supportingTransformedShape, 0.f);

            DirectedPosition directedPosition = DistanceShapeFactors.getDirectedPosition(supportingTransformedShape, pos*measurementLength);
            Point2D centerPoint = directedPosition.getPoint();
            double angle = directedPosition.getAngle();

            FontMetrics fontMetrics = g2.getFontMetrics();
            double offset = getLabelOffset() + fontMetrics.getDescent(); 

            //////////////////////CALCULATES POSITION OF THE MAIN LABEL///////////////////////////////

            String mainLabel = getLabel();

            Rectangle2D mainLabelBounds = fontMetrics.getStringBounds(mainLabel, g2).getBounds2D();


            int j2DMainX = (int)Math.rint(centerPoint.getX());
            int j2DMainY = (int)Math.rint(centerPoint.getY());

            double mainLabelHalfWidth = mainLabelBounds.getWidth()/2.;          

            int xMainLabel = (int) Math.rint(j2DMainX - Math.cos(angle)*mainLabelHalfWidth + Math.sin(angle)*offset);
            int yMainLabel = (int) Math.rint(j2DMainY - Math.sin(angle)*mainLabelHalfWidth - Math.cos(angle)*offset);

            ////////////////DRAWS MAIN AND DISTANCE LABELS/////////////////////////////////

            AffineTransform trOld = g2.getTransform();

            g2.setPaint(getLabelPaint());


            AffineTransform transformNew2 = new AffineTransform(trOld);      
            transformNew2.rotate(angle, xMainLabel, yMainLabel);         
            g2.setTransform(transformNew2);
            g2.drawString(mainLabel, xMainLabel, yMainLabel);

            g2.setTransform(trOld);
        }        
    }


    @Override
    public IdentityTag getIdentityTag()
    {
        IdentityTag keyLabelObject = new IdentityTag(getKey(), getLabel());
        return keyLabelObject;
    }

    public abstract Serializable getProxy();

    public abstract AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);

    public AnnotationStyle<?> getStyle() 
    {
        return style;
    }

    public Integer getKey()
    {
        return key;
    }

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
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlightedNew) {
        setHighlighted(highlightedNew, true);
    }

    public void setHighlighted(boolean highlightedNew, boolean notify) {
        this.highlighted = highlightedNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    //should be final, because only than it can be called from the constructors of inheriting classes
    public  final void setFinished(boolean finishedNew) {
        setFinished(finishedNew, true);
    }

    public void setFinished(boolean finishedNew, boolean notify) {
        this.finished = finishedNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        setLabel(label, true);
    }

    public void setLabel(String label, boolean notify) {
        Validation.requireNonNullParameterName(label, "label");

        this.label = label;
        checkIfCustomLabel(label);

        if (notify) {
            fireAnnotationChanged();
        }
    }

    public LabelAutomaticType getLabelType() {
        return labelType;
    }

    public void setAutomaticLabelType(LabelAutomaticType labelTypeNew) {
        this.labelType = labelTypeNew;
        updateAutomaticLabel();
    }

    public void updateAutomaticLabel() 
    {
        if (!isCustomLabel) {
            String label = labelType.getLabel(this);
            setLabel(label);
        }
    }

    private void updateAutomaticLabelPrivate()
    {
        if (!isCustomLabel) 
        {
            String label = labelType.getLabel(this);
            this.label = label;

            checkIfCustomLabel(label);
            fireAnnotationChanged();
        }
    }

    private void checkIfCustomLabel(String labelNew)
    {
        this.isCustomLabel = !Objects.equals(labelNew, labelType.getLabel(this));
    }

    public String getCustomLabel()
    {
        String customLabel = isCustomLabel ? this.label : null;
        return customLabel;
    }

    public boolean isLabelVisible() 
    {
        boolean visible = finished ? labelFinishedVisible : labelUnfinishedVisible;
        return visible;
    }

    public boolean isLabelUnfinishedVisible() 
    {
        return labelUnfinishedVisible;
    }

    public void setLabelUnfinishedVisible(boolean visible) {
        setLabelUnfinishedVisible(visible, true);
    }

    public void setLabelUnfinishedVisible(boolean visible, boolean notify) {
        this.labelUnfinishedVisible = visible;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public boolean isLabelFinishedVisible() {
        return labelFinishedVisible;
    }

    public void setLabelFinishedVisible(boolean visible) {
        setLabelFinishedVisible(visible, true);
    }

    public void setLabelFinishedVisible(boolean visible, boolean notify) {
        this.labelFinishedVisible = visible;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public Paint getLabelPaint() {
        Paint paint = finished ? paintLabelFinished : paintLabelUnfinished;
        return paint;
    }

    public Paint getPaintLabelFinished() {
        return paintLabelFinished;
    }

    public void setPaintLabelFinished(Paint paint) {
        setPaintLabelFinished(paint, true);
    }

    public void setPaintLabelFinished(Paint paintNew, boolean notify) {
        this.paintLabelFinished = paintNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public Paint getPaintLabelUnfinished() {
        return paintLabelUnfinished;
    }

    public void setPaintLabelUnfinished(Paint paint) {
        setPaintUnfinished(paint, true);
    }

    public void setPaintLabelUnfinished(Paint paint, boolean notify) {
        this.paintLabelUnfinished = paint;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    // ///////////////////////////////

    public Paint getStrokePaint() {
        Paint paint = finished ? paintFinished : paintUnfinished;
        return paint;
    }

    public Paint getPaintFinished() {
        return paintFinished;
    }

    public void setPaintFinished(Paint paintNew) {
        setPaintFinished(paintNew, true);
    }

    public void setPaintFinished(Paint paintNew, boolean notify) {
        this.paintFinished = paintNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public Paint getPaintUnfinished() {
        return paintUnfinished;
    }

    public void setPaintUnfinished(Paint paint) {
        setPaintUnfinished(paint, true);
    }

    public void setPaintUnfinished(Paint paintNew, boolean notify) {
        this.paintUnfinished = paintNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public Stroke getStroke() {
        Stroke stroke = finished ? strokeFinished : strokeUnfinished;
        return stroke;
    }

    public Stroke getStrokeFinished() {
        return strokeFinished;
    }

    public void setStrokeFinished(Stroke strokeNew) {
        setStrokeFinished(strokeNew, true);
    }

    public void setStrokeFinished(Stroke strokeNew, boolean notify) {
        this.strokeFinished = strokeNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public Stroke getStrokeUnfinished() {
        return strokeUnfinished;
    }

    public void setStrokeUnfinished(Stroke strokeNew) {
        setStrokeUnfinished(strokeNew, true);
    }

    public void setStrokeUnfinished(Stroke strokeNew, boolean notify) {
        this.strokeUnfinished = strokeNew;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    // LABEL FONT

    public Font getLabelFont() {
        Font labelFont = finished ? labelFontFinished : labelFontUnfinished;
        return labelFont;
    }

    public Font getLabelFontUnfinished() {
        return labelFontUnfinished;
    }

    public void setLabelFontUnfinished(Font font) {
        setLabelFontUnfinished(font, true);
    }

    public void setLabelFontUnfinished(Font font, boolean notify) {
        this.labelFontUnfinished = font;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public Font getLabelFontFinished() {
        return labelFontFinished;
    }

    public void setLabelFontFinished(Font font) {
        setLabelFontFinished(font, true);
    }

    public void setLabelFontFinished(Font font, boolean notify) {
        this.labelFontFinished = font;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    // LABEL LENGTHWISE POSITION

    public float getLabelLengthwisePosition() {
        return lengthwisePosition;
    }

    public void setLabelLengthwisePosition(float lengthwisePosition) {
        setLabelLengthwisePosition(lengthwisePosition, true);
    }

    public void setLabelLengthwisePosition(float lengthwisePosition,
            boolean notify) {
        this.lengthwisePosition = lengthwisePosition;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    public float getLabelOffset() {
        return labelOffset;
    }

    public void setLabelOffset(float labelOffset) {
        setLabelLengthwisePosition(labelOffset, true);
    }

    public void setLabelOffset(float labelOffset, boolean notify) {
        this.labelOffset = labelOffset;
        if (notify) {
            fireAnnotationChanged();
        }
    }

    @Override
    public int hashCode()
    {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object that) {
        return this == that;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        if (ANNOTATION_STYLE_COMPLETELY_CHANGED.equals(property)) {
            setConsistentWithStyle(style, true);
            updateAutomaticLabelPrivate();
        } else if (ANNOTATION_PAINT_FINISHED.equals(property)) {
            Paint paint = (Paint) evt.getNewValue();
            setPaintFinished(paint);
        } else if (ANNOTATION_PAINT_UNFINISHED.equals(property)) {
            Paint paint = (Paint) evt.getNewValue();
            setPaintUnfinished(paint);
        } else if (ANNOTATION_STROKE_FINISHED.equals(property)) {
            Stroke stroke = (Stroke) evt.getNewValue();
            setStrokeFinished(stroke);
        } else if (ANNOTATION_STROKE_UNFINISHED.equals(property)) {
            Stroke stroke = (Stroke) evt.getNewValue();
            setStrokeUnfinished(stroke);
        } else if (ANNOTATION_VISIBLE.equals(property)) {
            boolean visible = (boolean) evt.getNewValue();
            setVisible(visible);
        } else if (ANNOTATION_LABEL_VISIBLE_UNFINISHED.equals(property)) {
            boolean visible = (Boolean) evt.getNewValue();
            setLabelUnfinishedVisible(visible);
        } else if (ANNOTATION_LABEL_VISIBLE_FINISHED.equals(property)) {
            boolean visible = (Boolean) evt.getNewValue();
            setLabelFinishedVisible(visible);
        } else if (ANNOTATION_PAINT_LABEL_UNFINISHED.equals(property)) {
            Paint paint = (Paint) evt.getNewValue();
            setPaintLabelUnfinished(paint);
        } else if (ANNOTATION_PAINT_LABEL_FINISHED.equals(property)) {
            Paint paint = (Paint) evt.getNewValue();
            setPaintLabelFinished(paint);
        } else if (ANNOTATION_LABEL_FONT_UNFINISHED.equals(property)) {
            Font font = (Font) evt.getNewValue();
            setLabelFontUnfinished(font);
        } else if (ANNOTATION_LABEL_FONT_FINISHED.equals(property)) {
            Font font = (Font) evt.getNewValue();
            setLabelFontFinished(font);
        } else if (ANNOTATION_LABEL_TYPE.equals(property)) {
            LabelAutomaticType labelType = (LabelAutomaticType) evt
                    .getNewValue();
            setAutomaticLabelType(labelType);
        } else if (ANNOTATION_LABEL_LENGTHWISE_POSITION.equals(property)) {

        } else if (ANNOTATION_LABEL_OFFSET.equals(property)) {

        }
    }
}
