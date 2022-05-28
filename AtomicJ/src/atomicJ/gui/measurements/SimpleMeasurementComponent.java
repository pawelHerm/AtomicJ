package atomicJ.gui.measurements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;
import atomicJ.sources.IdentityTag;

public class SimpleMeasurementComponent implements MeasurementComponent
{
    private final DistanceMeasurementDrawable measurement;
    private final Integer componentKey;

    public SimpleMeasurementComponent(DistanceMeasurementDrawable originalMeasurement, Integer unionKey, String unionLabel)
    {
        this.componentKey = originalMeasurement.getKey();                        
        this.measurement = originalMeasurement.copy(originalMeasurement.getStyle(), unionKey, unionLabel);
    }

    public SimpleMeasurementComponent(SimpleMeasurementComponent that, Integer unionKey, String unionLabel)
    {
        this.componentKey = that.componentKey;
        this.measurement = that.measurement.copy(that.measurement.getStyle(), unionKey, unionLabel);
    }

    public SimpleMeasurementComponent(SimpleMeasurementComponent that, DistanceMeasurementStyle style, Integer unionKey, String unionLabel)
    {
        this.componentKey = that.componentKey;
        this.measurement = that.measurement.copy(style, unionKey, unionLabel);
    }

    @Override
    public List<DistanceMeasurementDrawable> getDistanceMeasurements()
    {
        return Collections.singletonList(measurement);
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned anchor = measurement.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
        AnnotationAnchorSigned anchorSigned = anchor != null ? new AnnotationAnchorWrappedSigned(anchor, componentKey) : null;

        return anchorSigned;
    }

    @Override
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();        
        AnnotationAnchorSigned returnedAnchor = measurement.setPosition(innerAnchor, modifierKeys, startPoint, endPoint);
        AnnotationAnchorSigned caughtAnchor = (returnedAnchor != null && Objects.equals(anchor.getKey(), getKey())) ? new AnnotationAnchorWrappedSigned(returnedAnchor, getKey()) : null;
        return caughtAnchor;
    }

    @Override
    public AnnotationAnchorSigned setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint)
    {
        return measurement.setPosition(anchor.getInnerAnchor(), modifierKeys, startPoint, endPoint);       
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        boolean reshaped = measurement.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);

        return reshaped;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info)
    {
        measurement.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
    }

    @Override
    public String getLabel()
    {
        return measurement.getLabel();
    }

    public void setLabel(String labelNew)
    {
        measurement.setLabel(labelNew);
    }


    @Override
    public IdentityTag getIdentityTag()
    {
        IdentityTag keyLabelObject = new IdentityTag(componentKey, getLabel());
        return keyLabelObject;
    }

    @Override
    public DistanceShapeFactors getDistanceShapeFactors() {
        return measurement.getDistanceShapeFactors();
    }

    public boolean equalsUpToStyle(DistanceMeasurementDrawable that) {
        return measurement.equalsUpToStyle(that);
    }

    @Override
    public Shape getDistanceShape() {
        return measurement.getDistanceShape();
    }

    @Override
    public Integer getKey() {
        return componentKey;
    }

    @Override
    public SimpleMeasurementComponent copy() {
        return new SimpleMeasurementComponent(this, this.measurement.getKey(), this.measurement.getLabel());
    }

    public SimpleMeasurementComponent copy(DistanceMeasurementStyle style, Integer unionKey, String unionLabel)
    {
        return new SimpleMeasurementComponent(this, style, unionKey, unionLabel);
    }

    @Override
    public boolean isComplex() {
        return measurement.isComplex();
    }

    @Override
    public boolean isClicked(Rectangle2D dataRectangle) {
        return measurement.isClicked(dataRectangle);
    }

    @Override
    public boolean isBoundaryClicked(Rectangle2D dataRectangle) {
        return measurement.isBoundaryClicked(dataRectangle);
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint,
            Point2D j2DCornerPoint, boolean forcedToDrawAbscissa,
            boolean forcedToDrawOrdinate) {
        measurement.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, j2DStartPoint, j2DEndPoint, j2DCornerPoint, forcedToDrawAbscissa, forcedToDrawOrdinate);
    }

    public MeasurementSimpleComponentSerializationProxy getSerializationProxy()
    {
        return new MeasurementSimpleComponentSerializationProxy(measurement.getProxy(), componentKey);
    }

    static class MeasurementSimpleComponentSerializationProxy implements MeasurementComponentSerializationProxy
    {
        private static final long serialVersionUID = 1L;

        private final Integer componentKey;
        private final MeasurementProxy measurementSerializable;

        private MeasurementSimpleComponentSerializationProxy(MeasurementProxy measurementProxy, Integer componentKey)
        {
            this.measurementSerializable = measurementProxy;
            this.componentKey = componentKey;
        }
        @Override
        public SimpleMeasurementComponent recreateOriginalObject(DistanceMeasurementStyle style, Integer unionKey)
        {
            return new SimpleMeasurementComponent(measurementSerializable.recreateOriginalObject(style, unionKey), unionKey, unionKey.toString());
        }       
    }
}