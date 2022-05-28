package atomicJ.imageProcessing;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;


public class KernelConvolutionSequence implements Channel2DDataInROITransformation
{   
    private final KernelSeparation kernel;

    public KernelConvolutionSequence(KernelSeparation kernel)
    {
        this.kernel = kernel;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData) 
    {
        GridChannel2DData griddedChannel = channelData.getDefaultGridding();

        Grid2D grid = griddedChannel.getGrid();
        Quantity zQuantity = griddedChannel.getZQuantity();

        double[][] matrix = griddedChannel.getData();

        Kernel2D initialKernel = kernel.getInitialKernel();
        Kernel2D finalKernel = kernel.getFinalKernel();

        double[][] preTransformed = preTransform(initialKernel, matrix, grid);

        GridChannel2DData channelDataTransformed = finalTransformation(finalKernel, grid, zQuantity, matrix, preTransformed);
        return channelDataTransformed;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {
        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();

        Grid2D grid = griddedChannelData.getGrid();
        Quantity zQuantity = griddedChannelData.getZQuantity();
        double[][] matrix = griddedChannelData.getData();

        Kernel2D initialKernel = kernel.getInitialKernel();
        Kernel2D finalKernel = kernel.getFinalKernel();

        double[][] pretransformed = preTransform(initialKernel, matrix, grid, roi, position, 
                finalKernel.getRowCount(), finalKernel.getColumnCount());

        GridChannel2DData channelDataTransformed = finalTransformation(finalKernel, grid, zQuantity, matrix, pretransformed,roi, position);
        return channelDataTransformed;
    }

    protected GridChannel2DData finalTransformation(Kernel2D kernel, Grid2D grid, Quantity zQuantity,
            double[][] original, double[][] preTransformed) 
    {
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {             
                transformed[i][j] = kernel.convolve(i, j, preTransformed, columnCount, rowCount);
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    protected GridChannel2DData finalTransformation(final Kernel2D kernel, Grid2D grid, Quantity zQuantity, double[][] original, final double[][] preTransformed, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return finalTransformation(kernel, grid, zQuantity, original, preTransformed);
        }

        final int rowCount = grid.getRowCount();
        final int columnCount = grid.getColumnCount();

        final double[][] transformed = ArrayUtilities.deepCopy(original);

        roi.addPoints(grid, position, new GridPointRecepient() 
        {
            @Override
            public void addPoint(int row, int column)
            {
                transformed[row][column] =
                        kernel.convolve(row, column, preTransformed, columnCount, rowCount);
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] transformedRow = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        transformedRow[j] = kernel.convolve(i, j, preTransformed, columnCount, rowCount);
                    }
                }                  
            }
        });

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }

    private double[][] preTransform(Kernel2D kernel, double[][] matrix, Grid2D grid) 
    {
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {             
                transformed[i][j] = kernel.convolve(i, j, matrix, columnCount, rowCount);
            }
        }

        return transformed;
    }

    private double[][] preTransform(Kernel2D kernel, final double[][] matrix, Grid2D grid, ROI roi,
            ROIRelativePosition position, int rowPadding, int columnPadding) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return preTransform(kernel, matrix, grid);
        }

        final int rowCount = grid.getRowCount();
        final int columnCount = grid.getColumnCount();

        Shape shape = roi.getROIShape();
        Rectangle2D bounds = shape.getBounds2D();

        final double[][] transformed = new double[rowCount][columnCount];

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            int minColumn = Math.max(0, grid.getColumn(bounds.getMinX()) - columnPadding - 1);
            int maxColumn = Math.min(grid.getColumnCount(),
                    grid.getColumn(bounds.getMaxX()) + columnPadding + 1);
            int minRow = Math.max(0, grid.getRow(bounds.getMinY()) - rowPadding - 1);
            int maxRow = Math.min(grid.getRowCount(),
                    grid.getRow(bounds.getMaxY()) + rowPadding + 1);

            for (int i = minRow; i < maxRow; i++)
            {
                for (int j = minColumn; j < maxColumn; j++) 
                {                        
                    transformed[i][j] = kernel.convolve(i, j, matrix, columnCount, rowCount);                  
                }
            }
        }
        //this is ok that we do not perform separate calculations for ROIRelativePosition.OUTSIDE
        //as in this method, we only calculate the pretransformed image
        //the transform method will distinguish between the points inside and outside the ROI
        //using only those parts of the pretransformed image, which are outside
        //this should give better performance, at least in the case of complex ROI shapes

        else 
        {
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {             
                    transformed[i][j] = kernel.convolve(i, j, matrix, columnCount, rowCount);
                }
            }
        }

        return transformed;
    }
}
