package atomicJ.curveProcessing;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;

public class SortX1DTransformation implements Channel1DDataInROITransformation
{   
    private final SortedArrayOrder order;

    public SortX1DTransformation(SortedArrayOrder order)
    {
        this.order = order;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {     
        if(order.equals(channel.getXOrder()))
        {
            return channel;
        }

        if(channel instanceof GridChannel1DData)
        {
            return sortGridChannel((GridChannel1DData)channel);
        }

        double[][] points = channel.getPointsCopy();
        double[][] sortedPoints = order.sortX(points);

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(sortedPoints, channel.getXQuantity(), channel.getYQuantity(), order);
        return channelData;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }

    public Channel1DData sortGridChannel(GridChannel1DData channelOriginal) 
    {   
        Grid1D gridOriginal = channelOriginal.getGrid();

        int itemCount = gridOriginal.getIndexCount();
        double increment = gridOriginal.getIncrement();
        double origin = gridOriginal.getOrigin();

        Grid1D gridCropped = new Grid1D(-increment, origin + (itemCount - 1)*increment, itemCount, channelOriginal.getXQuantity());

        GridChannel1DData channelData = new GridChannel1DData(ArrayUtilities.reverse(channelOriginal.getDataCopy()), gridCropped, channelOriginal.getYQuantity());

        return channelData;
    }

    public Channel1DData sortPeakForceChannel(SinusoidalChannel1DData channelOriginal) 
    {  
        return channelOriginal.sortX(order);
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
