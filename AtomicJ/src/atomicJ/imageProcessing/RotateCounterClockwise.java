package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;

public class RotateCounterClockwise implements Channel2DDataTransformation
{
    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData);
        }

        return transformChannel(channelData);
    }

    private Channel2DData transformChannel(Channel2DData channelData)
    {             
        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        double centerXValue = channelData.getXRange().getCentralValue();
        double centerYValue = channelData.getYRange().getCentralValue();

        int count = channelData.getItemCount();

        double[] originalXs = channelData.getXCoordinates();
        double[] originalYs = channelData.getYCoordinates();

        double[] transformedXs = new double[count];
        double[] transformedYs = new double[count];

        //we could use just one conversion factor, but then after rotations the units on one axis would change
        //e.x. if we used only YtoXConversionFactor, then after rotation both X- and Y-axis would have the same unit as
        //X-axis before rotation
        double YtoXUnitConversionFactor = yUnit.getConversionFactorTo(xUnit);
        double XtoYUnitConversionFactor = xUnit.getConversionFactorTo(yUnit);

        for(int i = 0; i<count; i++)
        {
            double x = originalXs[i];
            double y = originalYs[i];
            transformedXs[i] = centerXValue + YtoXUnitConversionFactor*centerYValue - YtoXUnitConversionFactor*y;
            transformedYs[i] = centerYValue - XtoYUnitConversionFactor*centerXValue + XtoYUnitConversionFactor*x;
        }

        double[][] dataNew = new double[][] {transformedXs, transformedYs, channelData.getZCoordinatesCopy()};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData gridChannelData)
    {
        Grid2D grid = gridChannelData.getGrid();

        Quantity xQuantity = gridChannelData.getXQuantity();
        Quantity yQuantity = gridChannelData.getYQuantity();
        Quantity zQuantity = gridChannelData.getZQuantity();       

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        double[][] original = gridChannelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[columnCount][rowCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] rowOriginal = original[i];
            for(int j = 0; j<columnCount; j++)
            {
                transformed[j][rowCount - 1 - i] = rowOriginal[j];
            }
        }

        double xOriginOriginal = grid.getXOrigin();
        double yOriginOriginal = grid.getYOrigin();
        double xLength = grid.getDomainLength();
        double yLength = grid.getRangeLength();

        //we could use just one conversion factor, but then after rotations the units on one axis would change
        //e.x. if we used only YtoXConversionFactor, then after rotation both X- and Y-axis would have the same unit as
        //X-axis before rotation
        double YtoXConversionFactor = yUnit.getConversionFactorTo(xUnit);
        double XtoYConversionFactor = xUnit.getConversionFactorTo(yUnit);

        double xOriginNew = XtoYConversionFactor*xOriginOriginal + 0.5*XtoYConversionFactor*xLength - 0.5*yLength; //after rotation, X-axis will have the old y-unit
        double yOriginNew = -0.5*xLength + YtoXConversionFactor*yOriginOriginal +0.5*YtoXConversionFactor*yLength; //after rotation, Y-axis will have the old x-unit

        Grid2D gridNew = new Grid2D(grid.getYIncrement(), grid.getXIncrement(), xOriginNew, yOriginNew, columnCount, rowCount, grid.getYQuantity(), grid.getXQuantity());

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, gridNew, zQuantity);
        return channelDataTransformed;
    }
}
