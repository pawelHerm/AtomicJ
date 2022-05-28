package atomicJ.data;

import java.awt.geom.Point2D;
import java.util.Map;

import atomicJ.curveProcessing.ProjectionX1DTransformation;

public class ProjectionConstraint1D implements ModificationConstraint1D
{
    private final Object bindingChannelIdentifier;
    private final int minRightPointDistance;
    private final int minLeftPointDistance;

    public ProjectionConstraint1D(Object boundingChannelIdentifier, int minLeftPointDistance, int minRightPointDistance)
    {
        this.bindingChannelIdentifier = boundingChannelIdentifier;
        this.minLeftPointDistance = minLeftPointDistance;
        this.minRightPointDistance = minRightPointDistance;
    }

    @Override
    public Point2D getValidItemPosition(Channel1D channel, Map<String, Channel1D> channels, int itemIndex, Point2D dataPoint) 
    {
        Channel1D bindingChannel = channels.get(bindingChannelIdentifier);

        Point2D correctedPoint = (bindingChannel != null) ? new ProjectionX1DTransformation().transform(bindingChannel.getChannelData(), dataPoint.getX()) : dataPoint;
        return correctedPoint;
    }

    @Override
    public boolean isValidPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint)
    {
        Channel1D bindingChannel = allChannels.get(bindingChannelIdentifier);
        if(bindingChannel == null)
        {
            return true;
        }

        boolean legal = bindingChannel.getXRange().contains(dataPoint.getX()) && bindingChannel.getIndexCountBoundedBy(bindingChannel.getXMinimum(),dataPoint.getX()) >= minLeftPointDistance
                && bindingChannel.getIndexCountBoundedBy(dataPoint.getX(), bindingChannel.getXMaximum()) >= minRightPointDistance;  

                return legal;
    }
}
