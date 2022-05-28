package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public abstract class CurveFilter implements Channel1DDataInROITransformation
{   
    @Override
    public Channel1DData transform(Channel1DData channel) 
    {
        GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);

        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        int columnCount = grid.getIndexCount();

        double[] transformed = new double[columnCount];

        for(int j = 0; j<columnCount; j++)
        {             
            transformed[j] = filter(j, data, columnCount);
        }

        GridChannel1DData channelData = new GridChannel1DData(transformed, grid, channel.getYQuantity());
        return channelData;
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

        //        roi.addPoints(grid, position, new GridPointRecepient() {
        //
        //            @Override
        //            public void addPoint(int row, int column) {
        //                transformed[row][column] = 
        //                        filter(row, column, matrix, columnCount, rowCount);                
        //            }
        //
        //            @Override
        //            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) {
        //                for(int i = rowFrom; i<rowTo; i++)
        //                {
        //                    double[] transformedRow = transformed[i];
        //
        //                    for(int j = columnFrom; j<columnTo; j++)
        //                    {
        //                        transformedRow[j] = filter(i, j, matrix, columnCount, rowCount);
        //                    }
        //                }                  
        //            }
        //        });  
        //
        //        ImageMatrix dataMatrix = new SimpleDataMatrix(transformed, grid);

        return null;
    }


    protected abstract double filter(int j, double[] matrix, int columnCount);

    protected double getPixel(int column, double[] pixels, int width)
    { 
        if (column<=0) column = 0; 
        if (column>=width) column = width-1; 
        return pixels[column]; 
    }
}
