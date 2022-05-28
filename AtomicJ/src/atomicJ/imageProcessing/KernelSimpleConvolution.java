package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;

public class KernelSimpleConvolution implements Channel2DDataInROITransformation
{   
    private final Kernel2D kernel;

    public KernelSimpleConvolution(Kernel2D kernel)
    {
        this.kernel = kernel;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData) 
    {
        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();
        Grid2D grid = griddedChannelData.getGrid();
        Quantity zQuantity = griddedChannelData.getZQuantity();
        double[][] matrix = griddedChannelData.getData();

        GridChannel2DData channelDataTransformed = finalTransformation(kernel, grid, zQuantity, matrix, matrix);
        return channelDataTransformed;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {
        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();
        Grid2D grid = griddedChannelData.getGrid();
        Quantity zQuantity = griddedChannelData.getZQuantity();
        double[][] matrix = griddedChannelData.getData();

        GridChannel2DData channelDataTransformed = finalTransformation(kernel, grid, zQuantity, matrix, matrix, roi, position );
        return channelDataTransformed;
    }

    protected GridChannel2DData finalTransformation(Kernel2D kernel, Grid2D grid, Quantity zQuantity, double[][] original, double[][] pretransformed) 
    {        
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = kernel.convolve(pretransformed, columnCount, rowCount);

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);     
        return channelDataTransformed;
    }

    protected GridChannel2DData finalTransformation(final Kernel2D kernel, Grid2D grid, Quantity zQuantity, double[][] original, final double[][] pretransformed, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return finalTransformation(kernel, grid, zQuantity, original, pretransformed);
        }

        final int rowCount = grid.getRowCount();
        final int columnCount = grid.getColumnCount();

        final double[][] transformed = ArrayUtilities.deepCopy(original);

        roi.addPoints(grid, position, new GridPointRecepient() {

            @Override
            public void addPoint(int row, int column)
            {
                transformed[row][column] = kernel.convolve(row, column, pretransformed, columnCount, rowCount);
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] transformedRow = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        transformedRow[j] = kernel.convolve(i, j, pretransformed, columnCount, rowCount);
                    }
                }                   
            }
        });

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);     

        return channelDataTransformed;
    }
}
