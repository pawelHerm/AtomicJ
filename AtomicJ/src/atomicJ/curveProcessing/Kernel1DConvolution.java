package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class Kernel1DConvolution implements Channel1DDataInROITransformation
{   
    private final Convolable1D kernel;

    public Kernel1DConvolution(Convolable1D kernel)
    {
        this.kernel = kernel;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {
        if(channel instanceof Point1DData)
        {
            return transformPointChannel((Point1DData)channel);
        }

        GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);
        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        int columnCount = grid.getIndexCount();

        double[] transformed = kernel.convolve(data, columnCount);

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

        return null;
    }

    protected GridChannel1DData finalTransformation(final Kernel1D kernel, Grid1D grid, double[] original, final double[] pretransformed, ROI roi, ROIRelativePosition position, String identifier) 
    {        

        //        final int columnCount = grid.getColumnCount();
        //
        //        final double[][] transformed = ArrayUtilities.deepCopy(original);
        //
        //        roi.addPoints(grid, position, new GridPointRecepient() {
        //
        //            @Override
        //            public void addPoint(int row, int column)
        //            {
        //                transformed[row][column] = kernel.convolve(row, column, pretransformed, columnCount, rowCount);
        //            }
        //
        //            @Override
        //            public void addBlock(int rowFrom, int rowTo, int columnFrom,
        //                    int columnTo) {
        //                for(int i = rowFrom; i<rowTo; i++)
        //                {
        //                    double[] transformedRow = transformed[i];
        //
        //                    for(int j = columnFrom; j<columnTo; j++)
        //                    {
        //                        transformedRow[j] = kernel.convolve(i, j, pretransformed, columnCount, rowCount);
        //                    }
        //                }                   
        //            }
        //        });
        //
        //        ImageMatrix dataMatrix = new SimpleDataMatrix(transformed, grid);     

        return null;
    }
}
