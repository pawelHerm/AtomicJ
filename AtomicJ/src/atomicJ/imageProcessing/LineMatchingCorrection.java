package atomicJ.imageProcessing;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;


public class LineMatchingCorrection implements Channel2DDataInROITransformation
{  
    private final double minimalLineLengthFraction;
    private final boolean columnCorrection;
    private final SampleFunction function;

    public LineMatchingCorrection(SampleFunction measure, double minimalLineLengthFraction, boolean columnCorrection)
    {
        this.function = measure;
        this.minimalLineLengthFraction = minimalLineLengthFraction;
        this.columnCorrection = columnCorrection;       
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData) 
    {        
        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();
        GridChannel2DData transformed = (columnCorrection) ? transformColumnWise(griddedChannelData) : transformRowWise(griddedChannelData);

        return transformed;
    }

    private GridChannel2DData transformRowWise(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[] location = getLocations(matrix);
        double locationOfLocations = function.getValue(location);

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double difference = location[i] - locationOfLocations;

            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = matrix[i][j] - difference;
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    private GridChannel2DData transformColumnWise(GridChannel2DData channelData) 
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] original = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transposedMatrix = ArrayUtilities.transpose(original, rowCount, columnCount);
        double[] locations = getLocations(transposedMatrix);
        double locationOfLocations = function.getValue(locations);

        double[][] transformed = new double[rowCount][columnCount];

        for(int j = 0; j<columnCount; j++)
        {
            double difference = locations[j] - locationOfLocations;
            for(int i = 0; i<rowCount; i++)
            {         
                transformed[i][j] = original[i][j] - difference;               
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    private double[] getLocations(double[][] data)
    {        
        int rowCount = data.length;

        double[] locations = new double[rowCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] row = data[i];
            locations[i] = function.getValue(row);
        }

        return locations;
    }


    protected double[] getRowLocations(Grid2D grid, final double[][] matrix, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getLocations(matrix);
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        final List<TDoubleList> rows = buildLineDataLists(rowCount);   

        roi.addPoints(grid, position, new GridPointRecepient()
        {        
            @Override
            public void addPoint(int row, int column) 
            {
                rows.get(row).add(matrix[row][column]);
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo)
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];
                    TDoubleList row = rows.get(i);

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        row.add(matrixRow[j]);
                    }
                }                   
            }
        });

        double[] locations = new double[rowCount];

        int minimalLineLength = (int) Math.rint(minimalLineLengthFraction * columnCount);

        for(int i = 0; i<rowCount; i++)
        {
            TDoubleList row = rows.get(i);
            int rowElementCount = row.size();

            locations[i] = rowElementCount >= minimalLineLength ? function.getValue(row) : Double.NaN;
        }

        double[] clearedLocations = estimateMisingValues(locations);

        return clearedLocations;
    }

    protected double[] getColumnLocations(Grid2D grid, final double[][] matrix, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getLocations(ArrayUtilities.transpose(matrix, grid.getRowCount(), grid.getColumnCount()));
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        final List<TDoubleList> columns = buildLineDataLists(columnCount);   

        roi.addPoints(grid, position, new GridPointRecepient() {        
            @Override
            public void addPoint(int row, int column) 
            {
                columns.get(column).add(matrix[row][column]);
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        TDoubleList column = columns.get(j);
                        column.add(matrixRow[j]);
                    }
                }                    
            }
        });

        double[] locations = new double[columnCount];

        int minimalLineLength = (int) Math.rint(minimalLineLengthFraction * rowCount);

        for(int i = 0; i<columnCount; i++)
        {
            TDoubleList column = columns.get(i);
            int columnElementCount = column.size();

            locations[i] = columnElementCount >= minimalLineLength ? function.getValue(column) : Double.NaN;
        }

        double[] clearedLocations = estimateMisingValues(locations);

        return clearedLocations;
    }

    private List<TDoubleList> buildLineDataLists(int lineCount)
    {
        List<TDoubleList> list = new ArrayList<>();
        for(int i = 0; i<lineCount; i++)
        {
            list.add(new TDoubleArrayList());
        }

        return list;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {     
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();

        if(columnCorrection)
        {
            return transformColumnWise(griddedChannelData, roi, position);
        }
        else
        {
            return transformRowWise(griddedChannelData, roi, position);
        }
    }

    private GridChannel2DData transformRowWise(GridChannel2DData channelData, ROI roi, ROIRelativePosition position) 
    {              
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[] locations = getRowLocations(grid, matrix, roi, position);
        double locationOfLocations = function.getValue(locations);

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double locationDifference = locations[i] - locationOfLocations;

            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = matrix[i][j] - locationDifference;
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    private GridChannel2DData transformColumnWise(GridChannel2DData channelData, ROI roi, ROIRelativePosition position) 
    {      
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[] locations = getColumnLocations(grid, matrix, roi, position);
        double locationOfLocations = function.getValue(locations);

        double[][] transformed = new double[rowCount][columnCount];


        for(int j = 0; j<columnCount; j++)
        {
            double locationDifference = locations[j] - locationOfLocations;
            for(int i = 0; i<rowCount; i++)
            {
                transformed[i][j] = matrix[i][j] - locationDifference;        
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    private double[] estimateMisingValues(double[] locations)
    {        
        MissingValuesEstimationMethod cleaner = new ValidValuesInterpolationMethod();
        return cleaner.estimateMissingValues(locations);
    }
}
