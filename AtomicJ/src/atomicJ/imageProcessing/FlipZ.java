package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;

public class FlipZ implements Channel2DDataTransformation
{
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
        double zMax = channelData.getZRange().getUpperBound();

        double[] originalZs = channelData.getZCoordinates();
        int count = originalZs.length;

        double[] transformedZs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedZs[i] = zMax - originalZs[i];
        }

        double[][] dataNew = new double[][] {channelData.getXCoordinatesCopy(), channelData.getYCoordinatesCopy(), transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridData(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        double[][] original = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double zMax = channelData.getZRange().getUpperBound();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] rowOriginal = original[i];
            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = zMax - rowOriginal[j];
            }
        }

        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channelData.getZQuantity());
        return channelDataNew;
    }
}
