package atomicJ.data;

import java.awt.geom.Point2D;
import java.util.Map;

import atomicJ.curveProcessing.ProjectionX1DTransformation;

public class ProjectionRightConstraint1D implements ModificationConstraint1D
{
    private final Object projectionChannelId;
    private final Object rightLimitChannelId;

    private final int minRightPointDistance;

    public ProjectionRightConstraint1D(Object projectionChannelId, Object rightLimitChannelId, int minRightPointDistance)
    {
        this.projectionChannelId = projectionChannelId;
        this.rightLimitChannelId = rightLimitChannelId;
        this.minRightPointDistance = minRightPointDistance;
    }

    @Override
    public Point2D getValidItemPosition(Channel1D channel, Map<String, Channel1D> channels, int itemIndex, Point2D dataPoint) 
    {
        Channel1D bindingChannel = channels.get(projectionChannelId);

        Point2D correctedPoint = (bindingChannel != null) ? new ProjectionX1DTransformation().transform(bindingChannel.getChannelData(), dataPoint.getX()) : dataPoint;
        return correctedPoint;
    }

    @Override
    public boolean isValidPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint)
    {
        Channel1D projectChannel = allChannels.get(projectionChannelId);
        Channel1D rightLimitChannel = allChannels.get(rightLimitChannelId);

        if(projectChannel == null || rightLimitChannel == null)
        {
            return true;
        }

        int length = projectChannel.getIndexCountBoundedBy(dataPoint.getX(), rightLimitChannel.getXMinimum());          
        boolean legal = length >= minRightPointDistance;       

        return legal;
    }
}
