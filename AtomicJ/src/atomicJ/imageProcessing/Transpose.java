package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;

public class Transpose implements Channel2DDataTransformation
{
    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannelData((GridChannel2DData)channelData);
        }

        return transformChannelData(channelData);
    }

    //transposition for scattered date is understood as a reflection along a vector (1,1), passing through the point (lowest X coordinate, lowest Y coordinate)
    private Channel2DData transformChannelData(Channel2DData channelData)
    {
        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        double xOrigin = channelData.getXRange().getLowerBound();
        double yOrigin = channelData.getYRange().getLowerBound();

        int count = channelData.getItemCount();

        double[] originalXs = channelData.getXCoordinates();
        double[] originalYs = channelData.getYCoordinates();

        double[] transformedXs = new double[count];
        double[] transformedYs = new double[count];

        //we could use just one conversion factor, but then after rotations the units on one axis would change
        //e.x. if we used only YtoXConversionFactor, then after rotation both X- and Y-axis would have the same unit as
        //X-axis before rotation
        double YtoXConversionFactor = yUnit.getConversionFactorTo(xUnit);
        double XtoYConversionFactor = xUnit.getConversionFactorTo(yUnit);

        for(int i = 0; i<count; i++)
        {
            double x = originalXs[i];
            double y = originalYs[i];
            transformedXs[i] = XtoYConversionFactor*xOrigin + y - yOrigin; //after transposition, x-axis will have the same unit as the y-axis before transposition
            transformedYs[i] = x - xOrigin + YtoXConversionFactor*yOrigin;
        }

        double[][] dataNew = new double[][] {transformedXs, transformedYs, channelData.getZCoordinatesCopy()};
        ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensity(transformedXs, transformedYs), ChannelDomainIdentifier.getNewDomainKey());

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, yQuantity, xQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannelData(GridChannel2DData gridChannelData)
    {
        Grid2D grid = gridChannelData.getGrid();
        Quantity xQuantity = gridChannelData.getXQuantity();
        Quantity yQuantity = gridChannelData.getYQuantity();
        Quantity zQuantity = gridChannelData.getZQuantity();
        double[][] matrix = gridChannelData.getData();

        //note that  transposition swaps rowCount and columnCount
        int columnCountNew = grid.getRowCount();
        int rowCountNew = grid.getColumnCount();

        double[][] transformed = new double[rowCountNew][columnCountNew];

        for(int i = 0; i<columnCountNew; i++)
        {
            double[] matrixRow = matrix[i];
            for(int j = 0; j<rowCountNew; j++)
            {
                transformed[j][i] = matrixRow[j];
            }
        }

        Grid2D gridNew = new Grid2D(grid.getYIncrement(), grid.getXIncrement(), grid.getYOrigin(), grid.getXOrigin(), rowCountNew, columnCountNew, yQuantity, xQuantity);

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, gridNew, zQuantity);
        return channelDataTransformed;    
    }
}
