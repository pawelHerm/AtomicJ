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

import atomicJ.data.ArraySupport2D;
import atomicJ.data.Grid2D;
import atomicJ.data.GridBlock;
import atomicJ.gui.GeneralPreferences;
import atomicJ.utilities.ArrayIndex;


public class GridPositionCalculator 
{
    private final int cellHeight;
    private final int cellWidth;

    public GridPositionCalculator(int cellHeight, int cellWidth)
    {
        this.cellHeight = cellHeight;
        this.cellWidth = cellWidth;
    }

    public int getCellWidth()
    {
        return cellWidth;
    }

    public int getCellHeight()
    {
        return cellHeight;
    }

    public void addPoints(ArraySupport2D grid, ROI roi, ROIRelativePosition position, GridPointRecepient recepient)
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

    public void addPoints(ArraySupport2D grid, Shape shape, ROIRelativePosition position, GridPointRecepient recepient)
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

    public void addEveryPoint(ArraySupport2D grid, GridPointRecepient recepient)
    {      
        recepient.addBlock(0, grid.getRowCount(), 0, grid.getColumnCount());
    }

    public void addPointsInside(ArraySupport2D grid, ROI roi, GridPointRecepient recepient)
    {                
        if(roi instanceof ROIRectangle)
        {
            ((ROIRectangle) roi).addPointsInside(grid, recepient);                               
        }
        else
        {
            Shape roiShape = roi.getROIShape();

            addPointsInside(grid, roiShape, recepient);      
        }    
    }

    public static int getInsidePointCountUpperBound(ArraySupport2D grid, Shape roiShape)
    {
        Rectangle2D bounds = roiShape.getBounds2D();

        int insideCountUpperBound = grid.getInsidePointCount(bounds);

        return insideCountUpperBound;
    }

    public static int getInsideXLinePointCountUpperBound(ArraySupport2D grid, Shape roiShape)
    {
        Rectangle2D bounds = roiShape.getBounds2D();
        int upperBound = grid.getXLineInsidePointCount(bounds);

        return upperBound;
    }

    public static int getInsideYLinePointCountUpperBound(ArraySupport2D grid, Shape roiShape)
    {
        Rectangle2D bounds = roiShape.getBounds2D();
        int upperBound = grid.getYLineInsidePointCount(bounds);

        return upperBound;
    }

    public void addPointsInsideRegularGridOptimized(Grid2D grid, Shape roiShape, GridPointRecepient recepient)
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

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = minRow + k*cellHeight;
                int column = minColumn + l*cellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

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


    public void addPointsInside(ArraySupport2D grid, Shape roiShape, GridPointRecepient recepient)
    {         
        if(grid instanceof Grid2D)
        {
            addPointsInsideRegularGridOptimized((Grid2D)grid, roiShape, recepient);
            return;
        }

        Rectangle2D bounds = roiShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        GridBlock block = grid.getInscribedBlock(bounds);

        int minColumn = block.getMinimalColumn();
        int maxColumn = block.getMaximalColumn();
        int minRow = block.getMinimalRow();
        int maxRow = block.getMaximalRow();

        if(minColumn >= columnCount || minRow >= rowCount || maxRow < 0 || maxColumn <0)
        {
            return;
        }

        int boundedRowCount = maxRow - minRow + 1;
        int boundedColumnCount = maxColumn - minColumn + 1;

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = minRow + k*cellHeight;
                int column = minColumn + l*cellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

                boolean contained = roiShape.contains(grid.getRectangle(row, column, pixelWidth, pixelHeight));

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
                            boolean inside = roiShape.contains(grid.getPoint(i, j));

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

        if(roi instanceof ROIRectangle)
        {
            ((ROIRectangle) roi).addPointsInside(grid, recepient);

        }
        else
        {
            Shape roiShape = roi.getROIShape();
            addPointsInsideConcurrent(grid, roiShape, recepient);
        }

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

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);


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

    public void addPointsOutside(ArraySupport2D grid, ROI roi, GridPointRecepient recepient)
    {
        if(roi instanceof ROIRectangle)
        {
            ((ROIRectangle) roi).addPointsOutside(grid, recepient);
        }
        else
        {           
            Shape roiShape = roi.getROIShape();
            addPointsOutside(grid, roiShape, recepient);
        }

    }

    public void addPointsOutsideRegularGridOptimized(Grid2D grid, Shape roiShape, GridPointRecepient recepient)
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

        for(int i = minRow; i< maxRow + 1; i++)
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

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = minRow + k*cellHeight;
                int column = minColumn + l*cellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

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


    public void addPointsOutside(ArraySupport2D grid, Shape roiShape, GridPointRecepient recepient)
    {
        if(grid instanceof Grid2D)
        {
            addPointsOutsideRegularGridOptimized((Grid2D)grid, roiShape, recepient);
            return;
        }

        Rectangle2D bounds = roiShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        GridBlock block = grid.getInscribedBlock(bounds);

        int minColumn = Math.max(0, block.getMinimalColumn() - 1);
        int maxColumn = Math.min(columnCount -1, block.getMaximalColumn() + 1);
        int minRow = Math.max(0, block.getMinimalRow() - 1);
        int maxRow = Math.min(rowCount - 1, block.getMaximalRow() + 1);

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

        for(int i = minRow; i< maxRow + 1; i++)
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

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = minRow + k*cellHeight;
                int column = minColumn + l*cellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

                boolean contained = roiShape.contains(grid.getRectangle(row, column, pixelWidth, pixelHeight));

                if(!contained)
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            boolean outside = !roiShape.contains(grid.getPoint(i, j));

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

    public static Shape getTransformedShape(Grid2D grid, Shape shape)
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
                        int row = minRow + k*cellHeight;
                        int column = minColumn + l*cellWidth;

                        int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                        int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

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
}
