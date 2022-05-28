package atomicJ.imageProcessing;

import org.jfree.data.Range;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;

public class FlipHorizontally implements Channel2DDataTransformation
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
        Range xRange = channelData.getXRange();
        double xOrigin = xRange.getLowerBound();
        double xLength = xRange.getLength();

        double[] xs = channelData.getXCoordinates();
        double[] ys = channelData.getYCoordinatesCopy();
        double[] zs = channelData.getZCoordinatesCopy();

        int count = channelData.getItemCount();

        double[] transformedXs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedXs[i] = 2*xOrigin + xLength - xs[i];
        }

        double[][] dataNew = new double[][] {transformedXs, ys, zs};
        ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensity(transformedXs, ys), ChannelDomainIdentifier.getNewDomainKey());

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    public GridChannel2DData transformGridChannel(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] original = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] rowOriginal = original[i];
            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = rowOriginal[columnCount - j - 1];
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }
}
