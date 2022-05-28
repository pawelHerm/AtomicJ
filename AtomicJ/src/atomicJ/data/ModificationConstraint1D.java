package atomicJ.data;

import java.awt.geom.Point2D;
import java.util.Map;

public interface ModificationConstraint1D 
{
    public Point2D getValidItemPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint);
    public boolean isValidPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint);
}
