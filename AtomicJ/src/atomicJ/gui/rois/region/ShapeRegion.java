package atomicJ.gui.rois.region;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class ShapeRegion implements Region
{
    private final Shape shape;

    public ShapeRegion(Shape shape)
    {
        this.shape = shape;
    }

    @Override
    public ShapeRegion rotate(double angle, double anchorX, double anchorY)
    {
        AffineTransform rotationTransform = AffineTransform.getRotateInstance(angle, anchorX, anchorY);

        Shape rotatedROIShape = rotationTransform.createTransformedShape(this.shape);     
        ShapeRegion rotatedRegion = new ShapeRegion(rotatedROIShape);

        return rotatedRegion;
    }

    @Override
    public boolean contains(double x, double y)
    {
        return shape.contains(x, y);
    } 

    @Override
    public SerializableRegionInformation getSerializableRegionInformation() 
    {
        return new SerializableShapeRegionInformation(this.shape);
    }

    private static class SerializableShapeRegionInformation implements SerializableRegionInformation
    {
        private static final long serialVersionUID = 1L;

        private final Shape shape;

        SerializableShapeRegionInformation(Shape shape)
        {
            this.shape = shape;
        }

        @Override
        public ShapeRegion getRegion() 
        {
            return new ShapeRegion(shape);
        }

    }
}
