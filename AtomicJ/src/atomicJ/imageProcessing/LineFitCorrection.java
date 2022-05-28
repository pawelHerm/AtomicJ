package atomicJ.imageProcessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;


public class LineFitCorrection implements Channel2DDataInROITransformation
{  
    private final double minimalLineLengthFraction;
    private final boolean columnCorrection;
    private final SampleFunctional functional;

    public LineFitCorrection(SampleFunctional functional, double minimalLineLengthFraction, boolean columnCorrection)
    {
        this.functional = functional;
        this.minimalLineLengthFraction = minimalLineLengthFraction;
        this.columnCorrection = columnCorrection;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData) 
    {        
        GridChannel2DData griddedChannel = channelData.getDefaultGridding();

        GridChannel2DData transformed = columnCorrection ? transformColumnWise(griddedChannel) : transformRowWise(griddedChannel);

        return transformed;
    }

    private GridChannel2DData transformRowWise(GridChannel2DData channelData) 
    {        
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        UnivariateFunction[] fits = getFitsToEquispacedLines(matrix);

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            UnivariateFunction f = fits[i];

            double[] matrixRow = matrix[i];
            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = matrixRow[j] - f.value(j);
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }

    private GridChannel2DData transformColumnWise(GridChannel2DData channelData) 
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();


        double[][] transposedMatrix = ArrayUtilities.transpose(matrix, rowCount, columnCount);
        UnivariateFunction[] fits = getFitsToEquispacedLines(transposedMatrix);

        double[][] transformed = new double[rowCount][columnCount];

        for(int j = 0; j<columnCount; j++)
        {
            UnivariateFunction f = fits[j];

            for(int i = 0; i<rowCount; i++)
            {
                transformed[i][j] = matrix[i][j] - f.value(i);           
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channel, ROI roi, ROIRelativePosition position) 
    {      
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        GridChannel2DData griddedChannelData = channel.getDefaultGridding();
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
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        UnivariateFunction[] fits = getRowFits(grid, matrix, roi, position);

        //        double[] intercepts = new double[fits.length];
        //        for(int i = 0; i<fits.length; i++)
        //        {
        //            UnivariateFunction f = fits[i];
        //            intercepts[i]= f.value(0);
        //        }
        //        
        //        double[] slopes = new double[fits.length];
        //        for(int i = 0; i<fits.length; i++)
        //        {
        //            UnivariateFunction f = fits[i];
        //            slopes[i]= 10000*(f.value(1) - intercepts[i]);
        //        }
        //        
        //        ArrayUtilities.print(intercepts);
        //        ArrayUtilities.print(slopes);

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            UnivariateFunction f = fits[i];

            for(int j = 0; j<columnCount; j++)
            {
                transformed[i][j] = matrix[i][j] - f.value(j);
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    private GridChannel2DData transformColumnWise(GridChannel2DData channel, ROI roi, ROIRelativePosition position) 
    {      
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        Grid2D grid = channel.getGrid();
        Quantity zQuantity = channel.getZQuantity();
        double[][] matrix = channel.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        UnivariateFunction[] fits = getColumnFits(grid, matrix, roi, position);

        double[][] transformed = new double[rowCount][columnCount];

        for(int j = 0; j<columnCount; j++)
        {
            UnivariateFunction f = fits[j];
            for(int i = 0; i<rowCount; i++)
            {               
                transformed[i][j] = matrix[i][j] - f.value(i);              
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }

    private UnivariateFunction[] getFitsToEquispacedLines(double[][] data)
    {        

        UnivariateFunction[] fits = functional.getValues(data, data[0].length);

        //        int rowCount = data.length;
        //
        //        UnivariateFunction[] fits = new UnivariateFunction[rowCount];
        //
        //        for(int i = 0; i<rowCount; i++)
        //        {
        //            double[] line = data[i];
        //            fits[i] = functional.getValue(line);
        //        }



        return fits;
    }


    protected UnivariateFunction[] getRowFits(Grid2D grid, final double[][] matrix, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getFitsToEquispacedLines(matrix);
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        final List<List<double[]>> rows = build2DLineDataList(rowCount);   

        roi.addPoints(grid, position, new GridPointRecepient() {        
            @Override
            public void addPoint(int row, int column) 
            {
                rows.get(row).add(new double[] {column, matrix[row][column]});
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];
                    List<double[]> row = rows.get(i);

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        row.add(new double[] {j, matrixRow[j]});
                    }
                }                    
            }
        });

        UnivariateFunction[] fits = new UnivariateFunction[rowCount];

        int minimalLineLength = (int) Math.rint(minimalLineLengthFraction * columnCount);
        for(int i = 0; i<rowCount; i++)
        {
            List<double[]> row = rows.get(i);
            int rowElementCount = row.size();

            fits[i] = rowElementCount >= minimalLineLength ? functional.getValue(row) : null;
        }

        UnivariateFunction[] clearedLocations = estimateMisingValues(fits);

        return clearedLocations;
    }

    protected UnivariateFunction[] getColumnFits(Grid2D grid, final double[][] matrix, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getFitsToEquispacedLines(ArrayUtilities.transpose(matrix, grid.getRowCount(), grid.getColumnCount()));
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        final List<List<double[]>> columns = build2DLineDataList(columnCount);   

        roi.addPoints(grid, position, new GridPointRecepient() {        
            @Override
            public void addPoint(int row, int column) 
            {
                columns.get(column).add(new double[] {row, matrix[row][column]});
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        List<double[]> column = columns.get(j);
                        column.add(new double[] {i, matrixRow[j]});
                    }
                }    
            }
        });

        UnivariateFunction[] fits = new UnivariateFunction[columnCount];

        int minimalLineLength = (int) Math.rint(minimalLineLengthFraction * rowCount);

        for(int i = 0; i<columnCount; i++)
        {
            List<double[]> column = columns.get(i);
            int columnElementCount = column.size();

            fits[i] = columnElementCount >= minimalLineLength ? functional.getValue(column) : null;
        }

        UnivariateFunction[] clearedLocations = estimateMisingValues(fits);

        return clearedLocations;
    }

    private List<List<double[]>> build2DLineDataList(int lineCount)
    {
        List<List<double[]>> list = new ArrayList<>();
        for(int i = 0; i<lineCount; i++)
        {
            list.add(new ArrayList<double[]>());
        }

        return list;
    }

    private UnivariateFunction[] estimateMisingValues(UnivariateFunction[] functions)
    {        
        UnivariateFunctionInterpolationMethod cleaner = new UnivariateFunctionInterpolationMethod();
        return cleaner.estimateMissingValues(functions);
    }
}
