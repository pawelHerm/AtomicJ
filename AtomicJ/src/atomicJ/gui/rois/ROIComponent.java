package atomicJ.gui.rois;

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
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.region.Region;

public interface ROIComponent extends ROI
{
    public boolean isCorrectlyConstructed();
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info);
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);
    public AnnotationModificationOperation setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);

    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);
    public AnnotationModificationOperation rotateAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint);
    //returns true if ROI was modified
    public abstract boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);
    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor);

    public List<ROIDrawable> split(double[][] polylineVertices);
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);
    public List<ROIDrawable> getROIDrawables();
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info);
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape);
    public void setSpatialRestriction(Region spatialRestrictionNew);
}