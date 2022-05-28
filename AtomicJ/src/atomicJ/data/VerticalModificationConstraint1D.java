package atomicJ.data;

import java.awt.geom.Point2D;
import java.util.Map;

public class VerticalModificationConstraint1D implements ModificationConstraint1D
{
    @Override
    public Point2D getValidItemPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint)
    {
        Point2D correctedPoint = new Point2D.Double(channel.getX(itemIndex), dataPoint.getY());
        return correctedPoint;
    }

    @Override
    public boolean isValidPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint) 
    {
        return true;
    }

}
