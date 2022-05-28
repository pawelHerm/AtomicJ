package atomicJ.gui.measurements;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.annotations.AnnotationAnchorSigned;

public interface MeasurementComponent extends DistanceMeasurement
{
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info);
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned innerAnchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint);
    public AnnotationAnchorSigned setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint);

    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);
    public List<DistanceMeasurementDrawable> getDistanceMeasurements();
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint, Point2D j2DCornerPoint, boolean forcedToDrawAbscissa, boolean forcedToDrawOrdinate);
}