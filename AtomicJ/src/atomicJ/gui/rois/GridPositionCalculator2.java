package atomicJ.gui.rois;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
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

import org.jfree.util.ShapeUtilities;

import atomicJ.data.Grid2D;
import atomicJ.gui.GeneralPreferences;
import atomicJ.utilities.ArrayIndex;


public class GridPositionCalculator2 
{
    private final int minCellHeight;
    private final int minCellWidth;

    public GridPositionCalculator2(int minCellHeight, int minCellWidth)
    {
        this.minCellHeight = minCellHeight;
        this.minCellWidth = minCellWidth;
    }

    public int getCellWidth()
    {
        return minCellWidth;
    }

    public int getCellHeight()
    {
        return minCellHeight;
    }

    public void addPoints(Grid2D grid, ROI roi, ROIRelativePosition position, GridPointRecepient recepient)
    {
        if(ROIRelativePosition.INSIDE.equals(position))
        {
            addPointsInside(grid, roi, recepient);
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            addPointsOutside(grid, roi, recepient);
        }
        else if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            addEveryPoint(grid, recepient);
        }
    }

    public void addPoints(Grid2D grid, Shape shape, ROIRelativePosition position, GridPointRecepient recepient)
    {
        if(ROIRelativePosition.INSIDE.equals(position))
        {
            addPointsInside(grid, shape, recepient);
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            addPointsOutside(grid, shape, recepient);
        }
        else if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            addEveryPoint(grid, recepient);
        }
    }

    public void addEveryPoint(Grid2D grid, GridPointRecepient recepient)
    {
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {
                recepient.addPoint(i, j);
            }
        }
    }

    public void addPointsInside(Grid2D grid, ROI roi, GridPointRecepient recepient)
    {        
        Shape roiShape = roi.getROIShape();

        addPointsInside(grid, roiShape, recepient);     
    }

    public void addPointsInside(Grid2D grid, Shape roiShape, GridPointRecepient recepient)
    {             
        Shape transformedShape = getTransformedShape(grid, roiShape);

        Rectangle2D bounds = transformedShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int minColumn = Math.max(0, (int)bounds.getMinX() - 1);
        int maxColumn = Math.min(columnCount - 1, (int)bounds.getMaxX() + 1);

        int minRow = Math.max(0, (int)bounds.getMinY() - 1);
        int maxRow = Math.min(rowCount - 1, (int)bounds.getMaxY() + 1);

        if(minColumn >= columnCount || minRow >= rowCount || maxRow < 0 || maxColumn <0)
        {
            return;
        }

        int boundedRowCount = maxRow - minRow + 1;
        int boundedColumnCount = maxColumn - minColumn + 1;

        //upper bound for cellRowCount
        int cellRowCount = Math.max(1, boundedRowCount/minCellHeight);
        int realCellRowCount = Math.min(cellRowCount, 15);
        int realCellHeight = boundedRowCount/realCellRowCount;

        //upper bound for cellColumnCounr
        int cellColumnCount = Math.max(1, boundedColumnCount/minCellWidth);
        int realCellColumnCount = Math.min(cellColumnCount, 15);
        int realCellWidth = boundedColumnCount/realCellColumnCount;

        int lastCellHeight = Math.min(boundedRowCount, realCellHeight + boundedRowCount%realCellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, realCellWidth + boundedColumnCount%realCellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = minRow + k*realCellHeight;
                int column = minColumn + l*lastCellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : lastCellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : realCellHeight;

                boolean contained = transformedShape.contains(column, row, pixelWidth, pixelHeight);


                if(contained)
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            recepient.addPoint(i, j);
                        }
                    }
                }
                else
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            boolean inside = transformedShape.contains(j, i);

                            if(inside)
                            {
                                recepient.addPoint(i, j);
                            }
                        }
                    }
                }

            }
        }
    }

    public void addPointsInsideConcurrent(Grid2D grid, ROI roi, GridPointRecepient recepient)
    {     
        Shape roiShape = roi.getROIShape();
        addPointsInsideConcurrent(grid, roiShape, recepient);
    }

    public void addPointsInsideConcurrent(Grid2D grid, Shape roiShape, GridPointRecepient recepient)
    {     

        Shape transformedShape = getTransformedShape(grid, roiShape);

        Rectangle2D bounds = transformedShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int minColumn = Math.max(0, (int)bounds.getMinX() - 1);
        int maxColumn = Math.min(columnCount - 1, (int)bounds.getMaxX() + 1);
        int minRow = Math.max(0, (int)bounds.getMinY() - 1);
        int maxRow = Math.min(rowCount - 1, (int)bounds.getMaxY() + 1);


        if(minColumn >= columnCount || minRow >= rowCount || maxRow < 0 || maxColumn <0)
        {
            return;
        }

        int boundedRowCount = maxRow - minRow + 1;
        int boundedColumnCount = maxColumn - minColumn + 1;

        int cellRowCount = Math.max(1, boundedRowCount/minCellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/minCellWidth);

        int lastCellHeight = Math.min(boundedRowCount, minCellHeight + boundedRowCount%minCellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, minCellWidth + boundedColumnCount%minCellWidth);


        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();

        int taskNumber = Math.min(Math.max(cellRowCount/20, 1), maxTaskNumber);
        int basicTaskSize = cellRowCount/taskNumber;
        int remainingFiles = cellRowCount%taskNumber;

        ExecutorService executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<FilterSubtask> tasks = new ArrayList<>();

        for( int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingFiles)
            {
                currentTaskSize++;
            }

            FilterSubtask task = new FilterSubtask(ShapeUtilities.clone(transformedShape), currentIndex, currentIndex + currentTaskSize, cellRowCount,
                    cellColumnCount, minRow, minColumn, lastCellWidth, lastCellHeight);
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

        for(FilterSubtask task : tasks)
        {
            List<ArrayIndex> indices = task.getIndices();

            for(ArrayIndex index : indices)
            {
                recepient.addPoint(index.getRow(), index.getColumn());
            }
        }      
    }

    public void addPointsOutside(Grid2D grid, ROI roi, GridPointRecepient recepient)
    {


        Shape roiShape = roi.getROIShape();
        addPointsOutside(grid, roiShape, recepient);

    }

    public void addPointsOutside(Grid2D grid, Shape roiShape, GridPointRecepient recepient)
    {
        Shape transformedShape = getTransformedShape(grid, roiShape);

        Rectangle2D bounds = transformedShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int minColumn = Math.max(0, (int)bounds.getMinX() - 1);
        int maxColumn = Math.min(columnCount - 1, (int)bounds.getMaxX() + 1);
        int minRow = Math.max(0, (int)bounds.getMinY() - 1);
        int maxRow = Math.min(rowCount - 1, (int)bounds.getMaxY() + 1);

        if(minColumn >= columnCount || minRow >= rowCount || maxRow < 0 || maxColumn <0)
        {
            return;
        }

        //adds points that are outside bounding box

        ////////////////////////////////

        for(int j = 0; j<columnCount; j++)
        {
            for(int i = 0; i< minRow; i++)
            {
                recepient.addPoint(i, j);
            }
            for(int i = maxRow + 1; i< rowCount; i++)
            {
                recepient.addPoint(i, j);
            }
        }

        //it has to be i <= maxRow, do not change to i< maxRow
        for(int i = minRow; i <= maxRow + 1; i++)
        {
            for(int j = 0; j<minColumn; j++)
            {
                recepient.addPoint(i, j);
            }
            for(int j = maxColumn + 1; j<columnCount; j++)
            {
                recepient.addPoint(i, j);
            }
        }

        ////////////////////////////////

        int boundedRowCount = maxRow - minRow + 1;
        int boundedColumnCount = maxColumn - minColumn + 1;

        int cellRowCount = Math.max(1, boundedRowCount/minCellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/minCellWidth);

        int lastCellHeight = Math.min(boundedRowCount, minCellHeight + boundedRowCount%minCellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, minCellWidth + boundedColumnCount%minCellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = minRow + k*minCellHeight;
                int column = minColumn + l*minCellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : minCellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : minCellHeight;

                boolean contained = transformedShape.contains(column, row, pixelWidth, pixelHeight);

                if(!contained)
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            boolean outside = !transformedShape.contains(j, i);

                            if(outside)
                            {
                                recepient.addPoint(i, j);
                            }
                        }
                    }
                }


            }
        }
    }

    private Shape getTransformedShape(Grid2D grid, Shape shape)
    {
        Point2D origin = grid.getPoint(0, 0);
        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();

        AffineTransform transform = AffineTransform.getScaleInstance(1./xIncrement, 1./yIncrement);
        AffineTransform translate = AffineTransform.getTranslateInstance(-origin.getX(), -origin.getY());        
        transform.concatenate(translate);

        Shape trShape = transform.createTransformedShape(shape);
        return trShape;
    }

    private class FilterSubtask implements Callable<Void>
    {
        private final int minCellRow;
        private final int maxCellRow;

        private final int minRow;
        private final int minColumn;

        private final int lastCellWidth;
        private final int lastCellHeight;

        private final int cellRowCount;
        private final int cellColumnCount;

        private final Shape transformedShape;

        private final List<ArrayIndex> indices = new ArrayList<>();

        public FilterSubtask(Shape transformedShape, int minCellRow, int maxCellRow, int cellRowCount,
                int cellColumnCount, int minRow, int minColumn, int lastCellWidth, int lastCellHeight)
        {
            this.transformedShape = transformedShape;

            this.minCellRow = minCellRow;
            this.maxCellRow = maxCellRow;

            this.minRow = minRow;
            this.minColumn = minColumn;

            this.lastCellHeight = lastCellHeight;
            this.lastCellWidth = lastCellWidth;

            this.cellRowCount = cellRowCount;
            this.cellColumnCount = cellColumnCount;
        }

        public List<ArrayIndex> getIndices()
        {
            return indices;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            try
            {       
                for(int k = minCellRow; k<maxCellRow; k++)
                {
                    if(currentThread.isInterrupted())
                    {
                        throw new InterruptedException();
                    }

                    for(int l = 0; l<cellColumnCount; l++)
                    {               
                        int row = minRow + k*minCellHeight;
                        int column = minColumn + l*minCellWidth;

                        int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : minCellWidth;
                        int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : minCellHeight;

                        boolean contained = transformedShape.contains(column, row, pixelWidth, pixelHeight);

                        if(contained)
                        {
                            for(int i = row; i<row + pixelHeight; i++)
                            {
                                for(int j = column; j<column + pixelWidth; j++)
                                {
                                    indices.add(new ArrayIndex(i, j));
                                }
                            }                           
                        }
                        else
                        {
                            for(int i = row; i<row + pixelHeight; i++)
                            {
                                for(int j = column; j<column + pixelWidth; j++)
                                {
                                    boolean inside = transformedShape.contains(j, i);
                                    if(inside)
                                    {
                                        indices.add(new ArrayIndex(i, j));
                                    }
                                }
                            }    
                        }

                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }      
            return null;
        }
    }


    public static interface GridPointRecepient
    {
        public void addPoint(int row, int column);
    }

}
