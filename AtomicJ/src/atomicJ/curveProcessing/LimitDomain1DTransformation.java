package atomicJ.curveProcessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.IndexRange;
import atomicJ.data.Point1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class LimitDomain1DTransformation implements Channel1DDataInROITransformation
{   
    private final double leftLimit;
    private final double rightLimit;

    public LimitDomain1DTransformation(double leftLimit, double rightLimit)
    {
        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;
    }

    public boolean isLimittedFromLeft()
    {
        return (leftLimit != Double.NEGATIVE_INFINITY);
    }

    public boolean isLimittedFromRight()
    {
        return (rightLimit != Double.POSITIVE_INFINITY);
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {     
        if(!isLimittedFromLeft() && !isLimittedFromRight())
        {
            return channel;
        }

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

        SortedArrayOrder xOrder = channel.getXOrder();
        if(xOrder == null)
        {
            return limitUnorderedChannel(channel);
        }

        SortedArrayOrder order = channel.getXOrder();

        IndexRange indexRange = channel.getIndexRangeBoundedBy(leftLimit, rightLimit);

        int minIndex = indexRange.getMinIndex();
        int maxIndex = indexRange.getMaxIndex();

        double[][] limitedPoints = indexRange.isWellFormed(channel.getItemCount()) ? channel.getPointsCopy(minIndex, maxIndex + 1) : new double[][] {};

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(limitedPoints, channel.getXQuantity(), channel.getYQuantity(), order);

        return channelData;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }

    private Channel1DData limitUnorderedChannel(Channel1DData channel)
    {
        double[][] points = channel.getPoints();
        double[][] croppedPoints = limitUnordererPoints(points);

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(croppedPoints, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());

        return channelData;
    }


    public Channel1DData transformGridChannel(GridChannel1DData channel) 
    {   
        SortedArrayOrder xOrder = channel.getXOrder();
        if(xOrder == null)
        {
            return limitUnorderedChannel(channel);
        }

        Grid1D gridOriginal = channel.getGrid();

        double originOriginal = gridOriginal.getOrigin();
        double increment = gridOriginal.getIncrement();

        double[] dataOriginal = channel.getData();

        IndexRange indexRange = channel.getIndexRangeBoundedBy(leftLimit, rightLimit);

        int minIndex = indexRange.getMinIndex();
        int maxIndex = indexRange.getMaxIndex();

        double[] limitedData = indexRange.isWellFormed(dataOriginal.length) ? Arrays.copyOfRange(dataOriginal, minIndex, maxIndex + 1) : new double[] {};

        double originNew = originOriginal + minIndex*increment;

        Grid1D gridCropped = new Grid1D(increment, originNew, limitedData.length, channel.getXQuantity());

        GridChannel1DData channelData = new GridChannel1DData(limitedData, gridCropped, channel.getYQuantity());

        return channelData;    
    }

    private Channel1DData transformSinusoidalChannel1D(SinusoidalChannel1DData channel)
    {        
        SortedArrayOrder xOrder = channel.getXOrder();
        if(xOrder == null)
        {
            return limitUnorderedChannel(channel);
        }

        double[] dataOriginal = channel.getData();

        IndexRange indexRange = channel.getIndexRangeBoundedBy(leftLimit, rightLimit);

        int minIndex = indexRange.getMinIndex();
        int maxIndex = indexRange.getMaxIndex();

        double[] limitedData = indexRange.isWellFormed(dataOriginal.length) ? Arrays.copyOfRange(dataOriginal, minIndex, maxIndex + 1) : new double[] {};

        int initIndexNew = channel.getInitIndex() + minIndex;

        SinusoidalChannel1DData channelData = new SinusoidalChannel1DData(limitedData, channel.getAmplitude(), channel.getAngleFactor(),
                initIndexNew, channel.getPhaseShift(), channel.getXShift(), channel.getXQuantity(), channel.getYQuantity());

        return channelData;
    }

    private double[][] limitUnordererPoints(double[][] points)
    {
        int n = points.length;

        List<double[]> croppedPoints = new ArrayList<>();
        for(int i = 0; i<n; i++)
        {
            double[] p = points[i];

            double x = p[0];
            double y = p[1];

            if(x <= rightLimit && x >= leftLimit)
            {
                croppedPoints.add(new double[] {x,y});
            }
        }

        return croppedPoints.toArray(new double[][] {});
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
