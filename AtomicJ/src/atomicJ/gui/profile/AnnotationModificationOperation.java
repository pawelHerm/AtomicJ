package atomicJ.gui.profile;

import java.awt.geom.Point2D;

import atomicJ.gui.annotations.AnnotationAnchorSigned;

public class AnnotationModificationOperation
{
    final Point2D pressedPoint;
    final Point2D endPoint;
    private final AnnotationAnchorSigned anchor;

    public AnnotationModificationOperation(AnnotationAnchorSigned caughtProfileAnchor, Point2D pressedPoint, Point2D endPoint)
    {
        this.anchor = caughtProfileAnchor;
        this.pressedPoint = pressedPoint;
        this.endPoint = endPoint;          
    }      

    public AnnotationAnchorSigned getAnchor()
    {
        return anchor;
    }

    public Point2D getEndPoint()
    {
        return new Point2D.Double(endPoint.getX(), endPoint.getY());
    }

    public Point2D getPressedPoint()
    {
        return new Point2D.Double(pressedPoint.getX(), pressedPoint.getY());
    }

    public AnnotationModificationOperation moveEndPoint(Point2D endpointNew)
    {
        return new AnnotationModificationOperation(anchor, new Point2D.Double(pressedPoint.getX(), pressedPoint.getY()), new Point2D.Double(endpointNew.getX(), endpointNew.getY()));
    }    
}