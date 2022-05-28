package atomicJ.imageProcessing;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.Gridding2DSettings;

public class Gridding2DTransformation implements Channel2DDataTransformation
{ 
    private final Gridding2DSettings settings;

    public Gridding2DTransformation(Gridding2DSettings settings)
    {
        this.settings = settings;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData)
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData);
        }
        return transformChannel(channelData);
    }

    public GridChannel2DData transformGridChannel(GridChannel2DData gridChannelData)
    {
        Quantity zQuantity = gridChannelData.getZQuantity();
        Grid2D originalGrid = gridChannelData.getGrid();

        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();

        int originalRowCount = originalGrid.getRowCount();
        int originalColumnCount = originalGrid.getColumnCount();

        int rowCount = settings.getRowCount();
        int columnCount = settings.getColumnCount();

        if(originalRowCount == rowCount && originalColumnCount == columnCount)
        {
            return gridChannelData;
        }

        double[][] resizedData = interpolationMethod.getGriddedData(gridChannelData, rowCount, columnCount);

        GridChannel2DData resizedChannelData = new GridChannel2DData(resizedData, originalGrid.changeDensity(rowCount, columnCount), zQuantity);

        return resizedChannelData;
    }

    public GridChannel2DData transformChannel(Channel2DData channelData)
    {
        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();

        int rowCount = settings.getRowCount();
        int columnCount = settings.getColumnCount();

        double[][] resizedData = interpolationMethod.getGriddedData(channelData, rowCount, columnCount);

        Range xRange = channelData.getXRange();
        Range yRange = channelData.getYRange();

        double xOrigin = xRange.getLowerBound();
        double yOrigin = yRange.getLowerBound();        

        double xIncrement = xRange.getLength()/(columnCount - 1.);
        double yIncrement = yRange.getLength()/(rowCount - 1.);

        Grid2D gridNew = new Grid2D(xIncrement, yIncrement, xOrigin, yOrigin, rowCount, columnCount, channelData.getXQuantity(), channelData.getYQuantity());

        GridChannel2DData griddedChannelData = new GridChannel2DData(resizedData, gridNew, channelData.getZQuantity());

        return griddedChannelData;
    }
}
