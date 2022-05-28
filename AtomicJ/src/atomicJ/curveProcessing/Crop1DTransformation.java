package atomicJ.curveProcessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atomicJ.analysis.CropSettings;
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
import atomicJ.utilities.ArrayUtilities;

public class Crop1DTransformation implements Channel1DDataInROITransformation
{   
    private final CropSettings cropSettings;

    public Crop1DTransformation(CropSettings cropSettings)
    {
        this.cropSettings = cropSettings;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {     
        if(!cropSettings.isDomainCropped() && !cropSettings.isRangeCropped())
        {
            return channel;
        }

        if(channel instanceof GridChannel1DData)
        {
            return cropGridChannel((GridChannel1DData)channel);
        }

        if(channel instanceof SinusoidalChannel1DData)
        {
            return cropSinusoidalChannel((SinusoidalChannel1DData)channel);
        }

        SortedArrayOrder order = channel.getXOrder();

        Channel1DData channelData = order != null ? cropSortedChannel(channel, order) : cropUnordererChannel(channel);

        return channelData;
    }

    private Channel1DData cropSortedChannel(Channel1DData sortedChannel, SortedArrayOrder order)
    {
        Channel1DData croppedChannel = sortedChannel;

        if(cropSettings.isDomainCropped())
        {
            croppedChannel = cropDomainSortedChannel(croppedChannel, order);
        }
        if(cropSettings.isRangeCropped())
        {
            croppedChannel = cropRange(croppedChannel);
        }

        return croppedChannel;        
    }

    public Channel1DData cropDomainSortedChannel(Channel1DData channel, SortedArrayOrder order)
    {
        double leftLimit = channel.getXMinimum() + cropSettings.getLeft();
        double rightLimit = channel.getXMaximum() - cropSettings.getRight();

        IndexRange indexRange = channel.getIndexRangeBoundedBy(leftLimit, rightLimit);

        int minIndex = indexRange.getMinIndex();
        int maxIndex = indexRange.getMaxIndex();

        double[][] limitedPoints = indexRange.isWellFormed(channel.getItemCount()) ? channel.getPointsCopy(minIndex, maxIndex + 1) : new double[][] {};

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(limitedPoints, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());

        return channelData;
    }

    private double[][] cropRange(double[] data, double origin, double increment)
    {
        int n = data.length;

        double lowerLimit = ArrayUtilities.getMinimum(data) + cropSettings.getBottom();
        double upperLimit = ArrayUtilities.getMaximum(data) - cropSettings.getTop();

        List<double[]> trimmedData = new ArrayList<>();
        for(int i = 0; i<n; i++)
        {            
            double y = data[i];

            if(upperLimit >= y && y >= lowerLimit)
            {
                trimmedData.add(new double[] {origin + i*increment,y});
            }
        }

        return trimmedData.toArray(new double[][] {});
    }

    private Channel1DData cropRange(Channel1DData channel)
    {
        double lowerLimit = channel.getYMinimum() + cropSettings.getBottom();
        double upperLimit = channel.getYMaximum() - cropSettings.getTop();

        double[][] points = channel.getPoints();
        int n = points.length;

        double[][] cropped = cropPointRange(points, lowerLimit, upperLimit, 0, n);
        FlexibleChannel1DData channelData = new FlexibleChannel1DData(cropped, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());

        return channelData;
    }

    //from inclusive, to exclusive
    private static double[][] cropPointRange(double[][] points, double lowerLimit, double upperLimit, int from, int to)
    {        
        int validFrom = Math.max(0, from);
        int validTo = Math.min(points.length, to);

        List<double[]> cropped = new ArrayList<>();
        for(int i = validFrom; i<validTo; i++)
        {
            double[] p = points[i];

            double x = p[0];
            double y = p[1];

            if(y <= upperLimit && y >= lowerLimit)
            {
                cropped.add(new double[] {x,y});
            }
        }

        return cropped.toArray(new double[][] {});
    }

    public Channel1DData cropGridChannel(GridChannel1DData channel) 
    {   
        Grid1D gridOriginal = channel.getGrid();
        double[] dataOriginal = channel.getData();

        double increment = gridOriginal.getIncrement();
        double origin = gridOriginal.getOrigin();

        double leftLimit = channel.getXMinimum() + cropSettings.getLeft();
        double rightLimit = channel.getXMaximum() - cropSettings.getRight();

        IndexRange indexRange = channel.getIndexRangeBoundedBy(leftLimit, rightLimit);

        int minIndex = indexRange.getMinIndex();
        int maxIndex = indexRange.getMaxIndex();

        double[] croppedData = indexRange.isWellFormed(dataOriginal.length) ? Arrays.copyOfRange(dataOriginal, minIndex, maxIndex + 1) : new double[] {};
        double originNew = origin + minIndex*increment;

        if(!cropSettings.isRangeCropped())
        {
            Grid1D gridCropped = new Grid1D(increment, originNew, croppedData.length, channel.getXQuantity());

            GridChannel1DData channelData = new GridChannel1DData(croppedData, gridCropped, channel.getYQuantity());

            return channelData;
        }

        double[][] croppedPoints = cropRange(croppedData, originNew, increment);
        FlexibleChannel1DData channelData = new FlexibleChannel1DData(croppedPoints, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());

        return channelData;
    }

    private Channel1DData cropSinusoidalChannel(SinusoidalChannel1DData channel)
    {        
        SortedArrayOrder order = channel.getXOrder();

        if(order == null)
        {
            return cropUnordererChannel(channel);
        }

        if(cropSettings.isDomainCropped())
        {
            double leftLimit = channel.getXMinimum() + cropSettings.getLeft();
            double rightLimit = channel.getXMaximum() - cropSettings.getRight();

            IndexRange indexRange = channel.getIndexRangeBoundedBy(leftLimit, rightLimit);

            int minIndex = indexRange.getMinIndex();
            int maxIndex = indexRange.getMaxIndex();

            if(cropSettings.isRangeCropped())
            {
                double[][] pointsAfterDomainCropping = channel.getPointsCopy(minIndex, maxIndex + 1);

                double lowerLimit = channel.getYMinimum() + cropSettings.getBottom();
                double upperLimit = channel.getYMaximum() - cropSettings.getTop();

                double[][] pointsAfterDomainAndRangeCropping = indexRange.isWellFormed(channel.getItemCount()) ? cropPointRange(pointsAfterDomainCropping, lowerLimit, upperLimit, 0, pointsAfterDomainCropping.length) : new double[][] {};
                return new FlexibleChannel1DData(pointsAfterDomainAndRangeCropping, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());
            }
            else
            {
                double[] domainCroppedData = indexRange.isWellFormed(channel.getItemCount()) ? Arrays.copyOfRange(channel.getData(), minIndex, maxIndex + 1) : new double[] {};
                int initIndexNew = channel.getInitIndex() + minIndex;           

                return new SinusoidalChannel1DData(domainCroppedData, channel.getAmplitude(), channel.getAngleFactor(),
                        initIndexNew, channel.getPhaseShift(), channel.getXShift(), channel.getXQuantity(), channel.getYQuantity());

            }
        }

        if(cropSettings.isRangeCropped())
        {
            return cropRange(channel);
        }

        return channel;    
    }

    private Channel1DData cropUnordererChannel(Channel1DData channel)
    {
        double leftLimit = channel.getXMinimum() + cropSettings.getLeft();
        double rightLimit = channel.getXMaximum() - cropSettings.getRight();

        double lowerLimit = channel.getYMinimum() + cropSettings.getBottom();
        double upperLimit = channel.getYMaximum() - cropSettings.getTop();

        double[][] points = channel.getPoints();
        int n = points.length;

        List<double[]> croppedPoints = new ArrayList<>();
        for(int i = 0; i<n; i++)
        {
            double[] p = points[i];

            double x = p[0];
            double y = p[1];

            if(x <= rightLimit && x >= leftLimit && y >= lowerLimit && y <= upperLimit)
            {
                croppedPoints.add(new double[] {x,y});
            }
        }

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(croppedPoints.toArray(new double[][] {}), channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());

        return channelData;
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
