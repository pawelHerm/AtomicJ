package atomicJ.gui.rois;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.ObjectUtilities;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.ShapeFactors;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.rois.region.CompositeRegion;
import atomicJ.gui.rois.region.HolesRegion;
import atomicJ.gui.rois.region.Region;
import atomicJ.gui.rois.region.WholePlaneRegion;
import atomicJ.sources.IdentityTag;

public class SimpleROIComponent implements ROIComponent
{
    private Region spatialRestriction = new WholePlaneRegion();

    private final ROIDrawable roi;
    private final Integer componentKey;

    public SimpleROIComponent(ROIDrawable originalROI, Integer unionKey, String unionLabel)
    {
        this.componentKey = originalROI.getKey();                        
        this.roi = originalROI.copy(originalROI.getStyle(), unionKey, unionLabel);
    }

    public SimpleROIComponent(SimpleROIComponent that, Integer unionKey, String unionLabel)
    {
        this.componentKey = that.componentKey;
        this.roi = that.roi.copy(that.roi.getStyle(), unionKey, unionLabel);
    }

    public SimpleROIComponent(SimpleROIComponent that, ROIStyle style, Integer unionKey, String unionLabel)
    {
        this.componentKey = that.componentKey;
        this.roi = that.roi.copy(style, unionKey, unionLabel);
    }

    private SimpleROIComponent(ROIDrawable roi, Integer componentKey)
    {
        this.roi = roi;
        this.componentKey = componentKey;
    }

    @Override
    public SimpleROIComponent getRotatedCopy(double angle, double anchorX, double anchorY)
    {
        ROIDrawable rotatedROI = roi.getRotatedCopy(angle, anchorX, anchorY);
        SimpleROIComponent rotated = new SimpleROIComponent(rotatedROI, this.componentKey);

        return rotated;
    }

    public Region getSpatialRestriction()
    {
        return spatialRestriction;
    }

    @Override
    public void setSpatialRestriction(Region spatialRestrictionNew)
    {
        this.spatialRestriction = spatialRestrictionNew;
    }

    public ROIDrawable getROIDrawable()
    {
        return roi;
    }

    @Override
    public List<ROIDrawable> getROIDrawables()
    {
        return Collections.singletonList(roi);
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned anchor = roi.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);
        AnnotationAnchorSigned anchorSigned = anchor != null ? new AnnotationAnchorWrappedSigned(anchor, componentKey) : null;

        return anchorSigned;
    }

    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationModificationOperation returnedOperation = roi.setPosition(anchor.getInnerAnchor(), modifierKeys, pressedPoint, startPoint, endPoint);
        boolean effectorIsSource = returnedOperation.getAnchor() != null && ObjectUtilities.equal(getKey(), anchor.getKey());

        AnnotationAnchorSigned newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        Point2D endNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;

        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, returnedOperation.getPressedPoint(), endNew);
        return modificationOperation;
    }

    @Override
    public AnnotationModificationOperation setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {        
        AnnotationModificationOperation returnedOperation = roi.setPosition(anchor.getInnerAnchor(), modifierKeys, pressedPoint, startPoint, endPoint);
        boolean effectorIsSource = returnedOperation.getAnchor() != null && ObjectUtilities.equal(getKey(), anchor.getKey());
        AnnotationAnchorSigned newAnchor = effectorIsSource ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        Point2D endNew = effectorIsSource ? returnedOperation.getEndPoint() : endPoint;
        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, returnedOperation.getPressedPoint(), endNew);

        return modificationOperation;
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {                
        AnnotationModificationOperation returnedOperation = roi.rotate(anchor.getInnerAnchor(), modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint);
        AnnotationAnchorSigned newAnchor = (returnedOperation.getAnchor() != null && ObjectUtilities.equal(getKey(), anchor.getKey())) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, returnedOperation.getPressedPoint(), returnedOperation.getEndPoint());

        return modificationOperation;
    }

    @Override
    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor)
    {
        return roi.getDefaultRotationCenter(anchor.getInnerAnchor());
    }

    @Override
    public AnnotationModificationOperation rotateAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        AnnotationModificationOperation returnedOperation =  roi.rotate(anchor.getInnerAnchor(), modifierKeys, rotationCenter, pressedPoint, startPoint, endPoint); 
        AnnotationAnchorSigned newAnchor = (returnedOperation.getAnchor() != null && ObjectUtilities.equal(getKey(), anchor.getKey())) ? new AnnotationAnchorWrappedSigned(returnedOperation.getAnchor(), getKey()) : null;
        AnnotationModificationOperation modificationOperation = new AnnotationModificationOperation(newAnchor, returnedOperation.getPressedPoint(), returnedOperation.getEndPoint());

        return modificationOperation;
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        boolean modified = roi.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);

        return modified;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info)
    {
        roi.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {
        roi.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, new HolesRegion(spatialRestriction));
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape) 
    {
        roi.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, new CompositeRegion(excludedShape, new HolesRegion(spatialRestriction)));
    }

    @Override
    public String getLabel()
    {
        return roi.getLabel();
    }

    @Override
    public void setLabel(String labelNew)
    {
        roi.setLabel(labelNew);
    }

    @Override
    public IdentityTag getIdentityTag()
    {
        IdentityTag keyLabelObject = new IdentityTag(componentKey, getLabel());
        return keyLabelObject;
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness) {
        return roi.getShapeFactors(flatness);
    }

    @Override
    public boolean equalsUpToStyle(ROI that) {
        return roi.equalsUpToStyle(that);
    }

    @Override
    public int getPointsInsideCountUpperBound(ArraySupport2D grid) {
        return roi.getPointsInsideCountUpperBound(grid);
    }

    @Override
    public int getPointsOutsideCountUpperBound(ArraySupport2D grid) {
        return roi.getPointsOutsideCountUpperBound(grid);
    }

    @Override
    public int getPointCountUpperBound(ArraySupport2D grid, ROIRelativePosition position){
        return roi.getPointCountUpperBound(grid, position);
    }


    @Override
    public void addPoints(ArraySupport2D grid, ROIRelativePosition position,
            GridPointRecepient recepient) {
        roi.addPoints(grid, position, recepient);
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient) {
        roi.addPointsInside(grid, recepient);
    }

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient) {
        roi.addPointsOutside(grid, recepient);
    }

    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow,
            int imageMinColumn, int imageMaxColumn,
            GridBiPointRecepient recepient) {
        roi.dividePoints(grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
    }

    @Override
    public Shape getROIShape()
    {
        return roi.getROIShape();
    }

    @Override
    public List<ROIDrawable> split(double[][] polylineVertices)
    {        
        return roi.split(polylineVertices);
    }

    @Override
    public boolean contains(Point2D p) {
        return roi.contains(p);
    }

    @Override
    public Integer getKey() {
        return componentKey;
    }

    @Override
    public SimpleROIComponent copy() {
        return new SimpleROIComponent(this, this.roi.getKey(), this.roi.getLabel());
    }

    public SimpleROIComponent copy(ROIStyle style, Integer unionKey, String unionLabel)
    {
        return new SimpleROIComponent(this, style, unionKey, unionLabel);
    }

    @Override
    public boolean isCorrectlyConstructed() 
    {
        return roi.isCorrectlyConstructed();
    }

    public ROISimpleComponentSerializationProxy getSerializableInformation()
    {
        return new ROISimpleComponentSerializationProxy(roi.getProxy(), this.componentKey);
    }

    static class ROISimpleComponentSerializationProxy implements ROIComponentSerializationProxy
    {
        private static final long serialVersionUID = 1L;

        private final Integer componentKey;
        private final ROIProxy roiSerializable;

        private ROISimpleComponentSerializationProxy(ROIProxy roiSerializable, Integer componentKey)
        {
            this.roiSerializable = roiSerializable;
            this.componentKey = componentKey;
        }
        @Override
        public SimpleROIComponent recreateOriginalObject(ROIStyle style, Integer unionKey)
        {
            return new SimpleROIComponent(roiSerializable.recreateOriginalObject(style, unionKey), componentKey);
        }       
    }
}