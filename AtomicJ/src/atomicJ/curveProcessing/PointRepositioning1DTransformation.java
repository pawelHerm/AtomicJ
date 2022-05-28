package atomicJ.curveProcessing;

import java.util.Arrays;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.MathUtilities;

public class PointRepositioning1DTransformation implements Channel1DDataInROITransformation
{   
    private static final double TOLERANCE = 1e-15;

    private final int pointIndex;
    private final double xNew;
    private final double yNew;

    public PointRepositioning1DTransformation(int pointIndex, double xNew, double yNew)
    {
        this.pointIndex = pointIndex;
        this.xNew = xNew;
        this.yNew = yNew;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {           
        if(channel instanceof GridChannel1DData)
        {
            return transformGridChannel((GridChannel1DData)channel);
        }

        if(channel instanceof SinusoidalChannel1DData)
        {
            return transformSinusoidalChannel1D((SinusoidalChannel1DData)channel);
        }

        if(channel instanceof Point1DData)
        {
            return transformPointChannel((Point1DData)channel);
        }

        return transformAnyChannel(channel);
    }

    private Channel1DData transformAnyChannel(Channel1DData channel)
    {
        double xOriginal = channel.getX(pointIndex);
        double yOriginal = channel.getY(pointIndex);     

        if(MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE) && MathUtilities.equalWithinTolerance(yOriginal, yNew, TOLERANCE))
        {
            return channel;
        }

        boolean onlyYMoved = MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE);

        double[][] repositionedPoints = channel.getPointsCopy();
        repositionedPoints[pointIndex] = new double[] {xNew, yNew};

        SortedArrayOrder orderNew = onlyYMoved ? channel.getXOrder() : guessOrder(repositionedPoints, channel.getXOrder());

        FlexibleChannel1DData channelModified = new FlexibleChannel1DData(repositionedPoints, channel.getXQuantity(), channel.getYQuantity(), orderNew);
        return channelModified;
    }

    private static SortedArrayOrder guessOrder(double[][] points, SortedArrayOrder originalOrder)
    {
        return null;
    }

    public Channel1DData transformGridChannel(GridChannel1DData channel) 
    {         
        double xOriginal = channel.getX(pointIndex);
        double yOriginal = channel.getY(pointIndex);

        if(MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE) && MathUtilities.equalWithinTolerance(yOriginal, yNew, TOLERANCE))
        {
            return channel;
        }

        boolean onlyYMoved = MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE);

        if(onlyYMoved)
        {
            Grid1D gridOriginal = channel.getGrid();
            double[] dataOriginal = channel.getData();

            double[] dataModified = Arrays.copyOf(dataOriginal, dataOriginal.length);
            dataModified[pointIndex] = yNew;

            GridChannel1DData channelData = new GridChannel1DData(dataModified, gridOriginal, channel.getYQuantity());

            return channelData;
        }

        double[][] repositionedPoints = channel.getPointsCopy();
        repositionedPoints[pointIndex] = new double[] {xNew, yNew};

        SortedArrayOrder orderNew = onlyYMoved ? channel.getXOrder() : guessOrder(repositionedPoints, channel.getXOrder());

        FlexibleChannel1DData channelModified = new FlexibleChannel1DData(repositionedPoints, channel.getXQuantity(), channel.getYQuantity(), orderNew);
        return channelModified;
    }

    private Channel1DData transformSinusoidalChannel1D(SinusoidalChannel1DData channel)
    {        
        double xOriginal = channel.getX(pointIndex);
        double yOriginal = channel.getY(pointIndex);

        if(MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE) && MathUtilities.equalWithinTolerance(yOriginal, yNew, TOLERANCE))
        {
            return channel;
        }

        boolean onlyYMoved = MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE);

        if(onlyYMoved)
        {
            double[] dataOriginal = channel.getData();

            double[] dataModified = Arrays.copyOf(dataOriginal, dataOriginal.length);
            dataModified[pointIndex] = yNew;

            SinusoidalChannel1DData channelData = new SinusoidalChannel1DData(dataModified, channel.getAmplitude(), channel.getAngleFactor(),
                    channel.getInitIndex(), channel.getPhaseShift(), channel.getXShift(), channel.getXQuantity(), channel.getYQuantity());

            return channelData;
        }

        double[][] repositionedPoints = channel.getPointsCopy();
        repositionedPoints[pointIndex] = new double[] {xNew, yNew};

        SortedArrayOrder orderNew = onlyYMoved ? channel.getXOrder() : guessOrder(repositionedPoints, channel.getXOrder());

        FlexibleChannel1DData channelModified = new FlexibleChannel1DData(repositionedPoints, channel.getXQuantity(), channel.getYQuantity(), orderNew);
        return channelModified;

    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        if(pointIndex < 0 || pointIndex > 1)
        {
            return channel;
        }

        double xOriginal = channel.getX();
        double yOriginal = channel.getY();

        if(MathUtilities.equalWithinTolerance(xOriginal, xNew, TOLERANCE) && MathUtilities.equalWithinTolerance(yOriginal, yNew, TOLERANCE))
        {
            return channel;
        }

        return new Point1DData(xNew, yNew, channel.getXQuantity(), channel.getYQuantity());
    }


    @Override
    public Channel1DData transform(Channel1DData channel, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);
        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        int columnCount = grid.getIndexCount();

        double[] transformed = new double[columnCount];

        return null;
    }
}
