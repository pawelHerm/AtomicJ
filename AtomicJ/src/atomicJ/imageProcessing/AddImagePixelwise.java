package atomicJ.imageProcessing;

import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;

public class AddImagePixelwise
{   
    private final GridChannel2DData otherImage;
    private final double otherFraction;
    private final double fraction;

    public AddImagePixelwise(GridChannel2DData otherImage, double otherFraction, double fraction)
    {
        this.otherImage = otherImage;
        this.otherFraction = otherFraction;
        this.fraction = fraction;
    }

    public GridChannel2DData transform(GridChannel2DData image) 
    {
        Grid2D grid = image.getGrid();
        Quantity zQuantity = image.getZQuantity();
        double[][] matrix = image.getData();

        Grid2D otherGrid = otherImage.getGrid();
        double[][] otherMatrix = otherImage.getData();

        int rowCount = Math.min(grid.getRowCount(), otherGrid.getRowCount());
        int columnCount = Math.min(grid.getColumnCount(), otherGrid.getColumnCount());

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {    
                double otherValue = otherMatrix[i][j];
                double value = matrix[i][j];
                transformed[i][j] = fraction*value + otherFraction*otherValue;
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }

    public GridChannel2DData transform(GridChannel2DData channel, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        Grid2D grid = channel.getGrid();
        Quantity zQuantity = channel.getZQuantity();

        final double[][] matrix = channel.getData();

        Grid2D otherGrid = otherImage.getGrid();
        final double[][] otherMatrix = otherImage.getData();

        int rowCount = Math.min(grid.getRowCount(), otherGrid.getRowCount());
        int columnCount = Math.min(grid.getColumnCount(), otherGrid.getColumnCount());

        Grid2D composedGrid = new Grid2D(grid.getXIncrement(), grid.getYIncrement(), grid.getXOrigin(), 
                grid.getYOrigin(), rowCount, columnCount, grid.getXQuantity(), grid.getYQuantity());


        final double[][] transformed = ArrayUtilities.deepCopy(matrix);

        roi.addPoints(composedGrid, position, new GridPointRecepient() {

            @Override
            public void addPoint(int i, int j)
            {               
                double otherValue = otherMatrix[i][j];
                double value = matrix[i][j];
                transformed[i][j] = fraction*value + otherFraction*otherValue;
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];
                    double[] otherMatrixRow = otherMatrix[i];

                    double[] transformedRow = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        double otherValue = otherMatrixRow[j];
                        double value = matrixRow[j];
                        transformedRow[j] = fraction*value + otherFraction*otherValue;                    }
                }  
            }
        });

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }
}
