package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;


public class FixMinimumOperation implements Channel2DDataInROITransformation
{   
    private final double minimumNew;
    private final boolean fixOnlyIfOldMinimumLower;

    public FixMinimumOperation(double minimumNew, boolean fixOnlyIfOldMinimumLower)
    {
        this.minimumNew = minimumNew;
        this.fixOnlyIfOldMinimumLower = fixOnlyIfOldMinimumLower;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridData((GridChannel2DData)channelData);
        }

        return transformChannelData(channelData);
    }

    private Channel2DData transformChannelData(Channel2DData channelData)
    {
        double minimum = channelData.getZRange().getLowerBound();
        double difference = minimum - minimumNew;

        if(fixOnlyIfOldMinimumLower && difference >= 0)
        {
            return channelData;
        }

        return transformChannelData(channelData, difference);
    }

    private GridChannel2DData transformGridData(GridChannel2DData channelData)
    {
        double minimum = channelData.getZRange().getLowerBound();
        double difference = minimum - minimumNew;

        if(fixOnlyIfOldMinimumLower && difference >= 0)
        {
            return channelData;
        }

        return transformGridData(channelData, difference);
    }

    @Override
    public Channel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        //minimumWholeChannel cannot be smaller then the minimum of values inside/outside the ROI,
        //so differenceWholeChannel cannot be smaller then the difference minimumROI - minimumNew
        //if differenceWholeChannel >= 0 then minimumROI - minimumNew > 0
        //so we can sometimes avoid CPU-intensive calculations of points inside/outside ROI if (applyOneSided && differenceWholeChannel >= 0)
        double minimumWholeChannel = channelData.getZRange().getLowerBound();
        double differenceWholeChannel = minimumWholeChannel - minimumNew;

        if(fixOnlyIfOldMinimumLower && differenceWholeChannel >= 0)
        {
            return channelData;
        }

        if(channelData instanceof GridChannel2DData)
        {
            return transformGridData((GridChannel2DData)channelData, roi, position);
        }

        return transformChannelData(channelData, roi, position);
    }

    private Channel2DData transformChannelData(Channel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        double[] valuesInside = channelData.getROIData(roi, position);

        double minimum = ArrayUtilities.getNumericMinimum(valuesInside);
        double difference = minimum - minimumNew;

        if(fixOnlyIfOldMinimumLower && difference >= 0)
        {
            return channelData;
        }

        return transformChannelData(channelData, difference);
    }

    private GridChannel2DData transformGridData(GridChannel2DData channelData,  ROI roi, ROIRelativePosition position)
    {
        double[] valuesInside = channelData.getROIData(roi, position);

        double minimum = ArrayUtilities.getNumericMinimum(valuesInside);
        double difference = minimum - minimumNew;

        if(fixOnlyIfOldMinimumLower && difference >= 0)
        {
            return channelData;
        }

        return transformGridData(channelData, difference);

    }

    private Channel2DData transformChannelData(Channel2DData channelData, double difference)
    {
        double[] zValues = channelData.getZCoordinatesCopy();

        for(int i = 0; i < zValues.length; i++)
        {
            zValues[i] = zValues[i] - difference;
        }

        double[][] dataNew = new double[][] {channelData.getXCoordinatesCopy(), channelData.getYCoordinatesCopy(), zValues};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridData(GridChannel2DData channelData, double difference)
    {
        Grid2D grid = channelData.getGrid();
        double[][] original = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] rowOriginal = original[i];
            double[] rowTransformed = transformed[i];

            for(int j = 0; j<columnCount; j++)
            {    
                rowTransformed[j] = rowOriginal[j] - difference;
            }
        }

        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channelData.getZQuantity());

        return channelDataNew;
    }   
}
