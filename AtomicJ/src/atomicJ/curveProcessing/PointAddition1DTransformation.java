package atomicJ.curveProcessing;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class PointAddition1DTransformation implements Channel1DDataInROITransformation
{   
    private final double xNew;
    private final double yNew;

    public PointAddition1DTransformation(double xNew, double yNew)
    {
        this.xNew = xNew;
        this.yNew = yNew;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {                
        if(channel instanceof Point1DData)
        {
            return transformPointChannel((Point1DData)channel);
        }

        return transformAnyChannel(channel);
    }

    private Channel1DData transformAnyChannel(Channel1DData channel)
    {
        SortedArrayOrder orderOld = channel.getXOrder();
        boolean orderConserved = (SortedArrayOrder.ASCENDING.equals(orderOld) && channel.getXMaximum() <= xNew) || (SortedArrayOrder.DESCENDING.equals(orderOld) && channel.getXMinimum() >= xNew);

        SortedArrayOrder orderNew = orderConserved ? orderOld : null;

        //it is better to use getPointsCopy(), because then we can use the coordinate pairs in the pointsNew {} 
        //getPoints() for some channels also return copy, bet then we could not use the pairs of coordinates, because we would not know whether it is a copy or not
        double[][] dataOld = channel.getPointsCopy();
        int nOld = dataOld.length;

        double[][] pointsNew = new double[nOld + 1][];

        for(int i = 0; i<dataOld.length; i++)
        {
            pointsNew[i] = dataOld[i];
        }
        pointsNew[nOld] = new double[] {xNew, yNew};

        FlexibleChannel1DData channelModified = new FlexibleChannel1DData(pointsNew, channel.getXQuantity(), channel.getYQuantity(), orderNew);
        return channelModified;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
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
