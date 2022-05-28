package atomicJ.imageProcessing;

import org.jfree.data.Range;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;

public class FlipVertically implements Channel2DDataTransformation
{
    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData);
        }

        return transformChannelData(channelData);
    }

    private Channel2DData transformChannelData(Channel2DData channelData)
    {
        Range yRange = channelData.getYRange();
        double yOrigin = yRange.getLowerBound();
        double yLength = yRange.getLength();

        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinates();
        double[] zs = channelData.getZCoordinatesCopy();

        int count = channelData.getItemCount();

        double[] transformedYs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedYs[i] = 2*yOrigin + yLength - ys[i];
        }

        double[][] dataNew = new double[][] {xs, transformedYs, zs};

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensity(xs, transformedYs), ChannelDomainIdentifier.getNewDomainKey());

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] original = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] originalRow = original[rowCount - i - 1];
            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = originalRow[j];
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }
}
