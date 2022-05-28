package atomicJ.imageProcessing;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.ImageMatrix;
import atomicJ.data.units.Quantity;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;


public abstract class ImageConcurrentFilter implements Channel2DDataInROITransformation
{   
    @Override
    public GridChannel2DData transform(Channel2DData channelData) 
    {
        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();
        ConcurrentFilterTask task = new ConcurrentFilterTask(griddedChannelData);
        try {
            task.execute();
        } catch (UserCommunicableException e) {
            e.printStackTrace();
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(task.getFilteredArray(), griddedChannelData.getGrid(), griddedChannelData.getZQuantity());
        return channelDataTransformed;
    }

    @Override
    public GridChannel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        GridChannel2DData griddedChannelData = channelData.getDefaultGridding();

        Grid2D grid = griddedChannelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = griddedChannelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        Shape shape = roi.getROIShape();
        Rectangle2D bounds = shape.getBounds2D();

        int minColumn = Math.max(0, grid.getColumn(bounds.getMinX()) - 1);
        int maxColumn = Math.min(grid.getColumnCount(),
                grid.getColumn(bounds.getMaxX()) + 1);
        int minRow = Math.max(0, grid.getRow(bounds.getMinY()) - 1);
        int maxRow = Math.min(grid.getRowCount(),
                grid.getRow(bounds.getMaxY()) + 1);


        double[][] transformed = new double[rowCount][columnCount];

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for (int i = 0; i < rowCount; i++)
            {
                for (int j = 0; j < columnCount; j++) 
                {       

                    boolean insideBounds = i>= minRow && i < maxRow && j>= minColumn && j<maxColumn;
                    boolean transform =  false;
                    if(insideBounds)
                    {
                        Point2D point = grid.getPoint(i, j);
                        transform = shape.contains(point);
                    }

                    transformed[i][j] = transform ? 
                            filter(i, j, matrix, columnCount, rowCount) : matrix[i][j];
                }
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for (int i = 0; i < rowCount; i++)
            {
                for (int j = 0; j < columnCount; j++) 
                {       

                    boolean insideBounds = i>= minRow && i < maxRow && j>= minColumn && j<maxColumn;
                    boolean transform =  true;
                    if(insideBounds)
                    {
                        Point2D point = grid.getPoint(i, j);
                        transform = !shape.contains(point);
                    }

                    transformed[i][j] = transform ? 
                            filter(i, j, matrix, columnCount, rowCount) : matrix[i][j];
                }
            }
        }
        else 
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
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

    private class ConcurrentFilterTask
    {
        private final double[][] filteredArray;  
        private final double[][] originalArray;

        private final int rowCount;
        private final int columnCount;

        private final int problemSize;

        private final Grid2D grid;

        private ExecutorService executor;

        public ConcurrentFilterTask(ImageMatrix channel)
        {
            this.grid = channel.getGrid();

            this.rowCount = grid.getRowCount();
            this.columnCount = grid.getColumnCount();

            this.originalArray = channel.getData();
            this.filteredArray = new double[rowCount][columnCount];       
            this.problemSize = originalArray.length;
        }

        public double[][] getFilteredArray()
        {
            return filteredArray;
        }

        public Void execute() throws UserCommunicableException
        {   
            int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();

            int taskNumber = Math.min(Math.max(problemSize/20, 1), maxTaskNumber);
            int basicTaskSize = problemSize/taskNumber;
            int remainingFiles = problemSize%taskNumber;

            executor = Executors.newFixedThreadPool(taskNumber); 

            int currentIndex = 0;

            List<FilterSubtask> tasks = new ArrayList<>();

            for( int i = 0; i <taskNumber; i++ ) 
            {
                int currentTaskSize = basicTaskSize;
                if(i<remainingFiles)
                {
                    currentTaskSize++;
                }

                FilterSubtask task = new FilterSubtask(currentIndex, currentIndex + currentTaskSize);
                tasks.add(task);
                currentIndex = currentIndex + currentTaskSize;
            }
            try 
            {
                CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

                for(FilterSubtask subtask: tasks)
                {
                    completionService.submit(subtask);
                }
                for(int i = 0; i<tasks.size(); i++)
                {
                    completionService.take().get();
                }

            } 
            catch (InterruptedException | ExecutionException e) 
            {
                e.printStackTrace();
            }   
            finally
            {
                executor.shutdown();
            }        
            return null;
        }   

        private class FilterSubtask implements Callable<Void>
        {
            private final int minRow;
            private final int maxRow;

            public FilterSubtask(int minRow, int maxRow)
            {
                this.minRow = minRow;
                this.maxRow = maxRow;
            }

            @Override
            public Void call() throws InterruptedException
            {
                Thread currentThread = Thread.currentThread();

                for(int i = minRow; i < maxRow;i++)
                {
                    if(currentThread.isInterrupted())
                    {
                        throw new InterruptedException();
                    }

                    try
                    {       
                        for(int j = 0; j<columnCount; j++)
                        {             
                            filteredArray[i][j] = filter(i, j, originalArray, columnCount, rowCount);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                }   
                return null;
            }
        }
    }
}
