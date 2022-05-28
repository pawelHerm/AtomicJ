package atomicJ.curveProcessing;

import java.util.Objects;

import org.jfree.data.Range;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;

public class MultiplePointsAddition1DTransformation implements Channel1DDataInROITransformation
{   
    private final double[][] newPoints;
    private final SortedArrayOrder newPointsOrder;

    public MultiplePointsAddition1DTransformation(double[][] newPoints, SortedArrayOrder newPointsOrder)
    {
        this.newPoints = newPoints;
        this.newPointsOrder = newPointsOrder;
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

    //the order of the result channel is the same as the order of the input
    private Channel1DData transformAnyChannel(Channel1DData channel)
    {
        int newPointCount = newPoints.length;
        if(newPointCount == 0)
        {
            return channel.getCopy();
        }

        if(channel.isEmpty())
        {
            FlexibleChannel1DData channelModified = new FlexibleChannel1DData(ArrayUtilities.deepCopy(newPoints), channel.getXQuantity(), channel.getYQuantity(), newPointsOrder);
            return channelModified;
        }

        int inputLength = channel.getItemCount();
        SortedArrayOrder inputOrder = channel.getXOrder();

        //it is better to use getPointsCopy(), because then we can use the coordinate pairs in the pointsNew {} 
        //getPoints() for some channels also return copy, but then we could not use the pairs of coordinates, because we would not know whether it is a copy or not
        double[][] inputPointsCopy = channel.getPointsCopy();
        int nOld = inputPointsCopy.length;

        boolean newPointsAreSorted = newPointsOrder != null;
        boolean oldPointsAreSorted = inputOrder != null;

        double[][] pointsJoined;

        if(oldPointsAreSorted)
        {
            double[][] newPointsSorted = newPointsAreSorted ? newPoints : inputOrder.sortX(newPoints);

            double newDataMin = Math.min(newPoints[0][0], newPoints[newPointCount - 1][0]);
            double newDataMax = Math.max(newPoints[0][0], newPoints[newPointCount - 1][0]);

            Range oldPointsRange = channel.getXRange();
            boolean overlaps = oldPointsRange.intersects(newDataMin, newDataMax);


            if(!overlaps)
            {
                pointsJoined = new double[nOld + newPointCount][];

                boolean newAndOldOrderTheSame = Objects.equals(inputOrder, newPointsOrder);

                if((oldPointsRange.getUpperBound() < newDataMin && SortedArrayOrder.ASCENDING.equals(inputOrder))
                        ||(newDataMax < oldPointsRange.getLowerBound() && SortedArrayOrder.DESCENDING.equals(inputOrder)))
                {
                    for(int i = 0; i<inputLength; i++)
                    {
                        pointsJoined[i] = inputPointsCopy[i];
                    }
                    if(newAndOldOrderTheSame)
                    {
                        for(int i = 0; i<newPointCount;i++)
                        {
                            pointsJoined[i + inputLength] = new double[] {newPointsSorted[i][0], newPointsSorted[i][1]};
                        }
                    }
                    else
                    {
                        for(int i = 0; i<newPointCount;i++)
                        {
                            pointsJoined[i + inputLength] = new double[] {newPointsSorted[newPointCount - 1 - i][0], newPointsSorted[newPointCount - 1 - i][1]};
                        }
                    }
                }     
                else if((newDataMax < oldPointsRange.getLowerBound() && SortedArrayOrder.ASCENDING.equals(inputOrder)) 
                        || (oldPointsRange.getUpperBound() < newDataMin && SortedArrayOrder.DESCENDING.equals(inputOrder)))
                {
                    if(newAndOldOrderTheSame)
                    {
                        for(int i = 0; i<newPointCount;i++)
                        {
                            pointsJoined[i] = new double[] {newPointsSorted[i][0], newPointsSorted[i][1]};
                        }
                    }
                    else
                    {
                        for(int i = 0; i<newPointCount;i++)
                        {
                            pointsJoined[i] = new double[] {newPointsSorted[newPointCount - 1 - i][0], newPointsSorted[newPointCount - 1 - i][1]};
                        }
                    }
                    for(int i = 0; i<inputPointsCopy.length; i++)
                    {
                        pointsJoined[i + newPointCount] = inputPointsCopy[i];
                    }
                }
                else //cannot happen
                {
                    double[][] pointsJoinedUnsorted = join(inputPointsCopy, newPoints);
                    pointsJoined = inputOrder.sortX(pointsJoinedUnsorted);
                }
            }
            else
            {
                double[][] pointsJoinedUnsorted = join(inputPointsCopy, newPoints);
                pointsJoined = inputOrder.sortX(pointsJoinedUnsorted);
            }
        }
        else
        {
            pointsJoined = join(inputPointsCopy, newPoints);
        }

        FlexibleChannel1DData channelModified = new FlexibleChannel1DData(pointsJoined, channel.getXQuantity(), channel.getYQuantity(), inputOrder);
        return channelModified;
    }

    private double[][] join(double[][] inputPointsCopy, double[][] pointsToAdd)
    {
        int n1 = inputPointsCopy.length;
        int n2 = pointsToAdd.length;
        double[][] pointsJoined = new double[n1 + n2][];

        for(int i = 0; i< n1; i++)
        {
            pointsJoined[i] = inputPointsCopy[i];
        }
        for(int i = 0; i< n2; i++)
        {
            double[] p = pointsToAdd[i];
            pointsJoined[i + n1] = new double[] {p[0], p[1]};
        }

        return pointsJoined;
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
