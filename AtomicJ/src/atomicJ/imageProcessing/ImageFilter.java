package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;

public abstract class ImageFilter implements Channel2DDataInROITransformation
{   
    @Override
    public Channel2DData transform(Channel2DData channel) 
    {
        GridChannel2DData gridChannelData = channel.getDefaultGridding();

        Grid2D grid = gridChannelData.getGrid();
        double[][] original = gridChannelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {             
                transformed[i][j] = filter(i, j, original, columnCount, rowCount);
            }
        }


        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channel.getZQuantity());

        return channelDataNew;
    }

    @Override
    public Channel2DData transform(Channel2DData channel, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        GridChannel2DData gridChannelData = channel.getDefaultGridding();


        Grid2D grid = gridChannelData.getGrid();
        final double[][] original = gridChannelData.getData();

        final int rowCount = grid.getRowCount();
        final int columnCount = grid.getColumnCount();

        final double[][] transformed = ArrayUtilities.deepCopy(original);

        roi.addPoints(grid, position, new GridPointRecepient() 
        {
            @Override
            public void addPoint(int row, int column) {
                transformed[row][column] = 
                        filter(row, column, original, columnCount, rowCount);                
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] transformedRow = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        transformedRow[j] = filter(i, j, original, columnCount, rowCount);
                    }
                }                  
            }
        });  

        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channel.getZQuantity());

        return channelDataNew;
    }


    protected abstract double filter(int i, int j, double[][] matrix, int columnCount, int rowCount);

    protected double getPixel(int row, int column, double[][] pixels, int width, int height)
    { 
        if (column<=0) column = 0; 
        if (column>=width) column = width-1; 
        if (row<=0) row = 0; 
        if (row>=height) row = height-1; 
        return pixels[row][column]; 
    }
}
