package atomicJ.gui.rois;

import java.awt.Shape;
import java.awt.geom.Point2D;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.ShapeFactors;
import atomicJ.sources.IdentityTag;

public class ROIWrapper implements ROI
{
    private String label;
    private final Object key;
    private final ROI wrappedROI;

    public ROIWrapper(ROI otherROI, Object key)
    {
        this(otherROI, key, key.toString());
    }

    public ROIWrapper(ROI otherROI, Object key, String label)
    {
        this.key = key;
        this.wrappedROI = otherROI.copy();
        this.label = label;
    }

    @Override
    public ROIWrapper getRotatedCopy(double angle, double anchorX, double anchorY)
    {
        ROIWrapper rotated = new ROIWrapper(wrappedROI.getRotatedCopy(angle, anchorX, anchorY), key, label);

        return rotated;
    }

    @Override
    public ROIWrapper copy()
    {
        return new ROIWrapper(this.wrappedROI.copy(), key);
    }

    @Override
    public Shape getROIShape() {
        return wrappedROI.getROIShape();
    }

    @Override
    public boolean contains(Point2D p) {
        return wrappedROI.contains(p);
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String labelNew)
    {
        this.label = labelNew;
    }

    @Override
    public IdentityTag getIdentityTag()
    {
        IdentityTag keyLabelObject = new IdentityTag(getKey(), getLabel());
        return keyLabelObject;
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness) {
        return wrappedROI.getShapeFactors(flatness);
    }

    @Override
    public boolean equalsUpToStyle(ROI that) {
        return wrappedROI.equalsUpToStyle(that);
    }

    @Override
    public int getPointsInsideCountUpperBound(ArraySupport2D grid) {
        return wrappedROI.getPointsInsideCountUpperBound(grid);
    }

    @Override
    public int getPointsOutsideCountUpperBound(ArraySupport2D grid) {
        return wrappedROI.getPointsOutsideCountUpperBound(grid);
    }

    @Override
    public int getPointCountUpperBound(ArraySupport2D grid, ROIRelativePosition position){
        return wrappedROI.getPointCountUpperBound(grid, position);
    }

    @Override
    public void addPoints(ArraySupport2D grid, ROIRelativePosition position, GridPointRecepient recepient) {
        wrappedROI.addPoints(grid, position, recepient);
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient) {
        wrappedROI.addPointsInside(grid, recepient);
    }

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient) {
        wrappedROI.addPointsOutside(grid, recepient);
    }

    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow,
            int imageMinColumn, int imageMaxColumn,
            GridBiPointRecepient recepient) {
        wrappedROI.dividePoints(grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
    }
}
