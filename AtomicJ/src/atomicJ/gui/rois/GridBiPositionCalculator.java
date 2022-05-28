package atomicJ.gui.rois;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import atomicJ.data.ArraySupport2D;
import atomicJ.data.Grid2D;

public class GridBiPositionCalculator 
{
    private final int cellHeight;
    private final int cellWidth;

    public GridBiPositionCalculator(int cellHeight, int cellWidth)
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

    public void dividePoints(ArraySupport2D grid, ROI roi, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        Shape roiShape = roi.getROIShape();
        dividePoints(grid, roiShape, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
    }

    //imageMaxRow and imageMaxColumn inclusive
    public void dividePointsRegularGridOptimized(Grid2D grid, Shape roiShape, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        Shape transformedShape = getTransformedShape(grid, roiShape);

        Rectangle2D bounds = transformedShape.getBounds2D();

        //minColumn,maxColumn, minRow and maxRow are inside the ROI
        int roiMinColumn = Math.max(imageMinColumn, (int)Math.ceil(bounds.getMinX()));
        int roiMaxColumn = Math.min(imageMaxColumn, (int)Math.floor(bounds.getMaxX()));
        int roiMinRow = Math.max(imageMinRow, (int)Math.ceil(bounds.getMinY()));
        int roiMaxRow = Math.min(imageMaxRow, (int)Math.floor(bounds.getMaxY()));

        //adds points that are outside bounding box

        ////////////////////////////////

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow); i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }

        for(int i = Math.max(imageMinRow, roiMinRow); i <= roiMaxRow; i++)
        {
            for(int j = imageMinColumn; j < Math.min(roiMinColumn, imageMaxColumn + 1); j++)
            {
                recepient.addPointOutside(i, j);
            }  

            for(int j = Math.max(roiMaxColumn + 1, imageMinColumn); j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            } 
        }

        for(int i = Math.max(roiMaxRow + 1, imageMinRow); i <= imageMaxRow; i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }

        ////////////////////////////////

        int boundedRowCount = roiMaxRow - roiMinRow + 1;
        int boundedColumnCount = roiMaxColumn - roiMinColumn + 1;

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = roiMinRow + k*cellHeight;
                int column = roiMinColumn + l*cellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

                boolean contained = transformedShape.contains(column, row, pixelWidth, pixelHeight);

                if(contained)
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            recepient.addPointInside(i, j);
                        }
                    }
                }
                else
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            boolean outside = !transformedShape.contains(j, i);

                            if(outside)
                            {
                                recepient.addPointOutside(i, j);
                            }
                            else
                            {
                                recepient.addPointInside(i, j);
                            }
                        }
                    }
                }
            }
        }
    }



    //imageMaxRow and imageMaxColumn inclusive
    public void dividePoints(ArraySupport2D grid, Shape roiShape, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        if(grid instanceof Grid2D)
        {
            dividePointsRegularGridOptimized((Grid2D)grid, roiShape, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);          
            return;
        }

        Rectangle2D bounds = roiShape.getBounds2D();

        //minColumn,maxColumn, minRow and maxRow are inside the ROI
        int roiMinColumn = Math.max(imageMinColumn, grid.getColumnCeiling(bounds.getMinX()));
        int roiMaxColumn = Math.min(imageMaxColumn, grid.getColumnFloor(bounds.getMaxX()));
        int roiMinRow = Math.max(imageMinRow, grid.getRowCeiling(bounds.getMinY()));
        int roiMaxRow = Math.min(imageMaxRow, grid.getRowFloor(bounds.getMaxY()));

        //adds points that are outside bounding box

        ////////////////////////////////

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow); i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }

        for(int i = Math.max(imageMinRow, roiMinRow); i <= roiMaxRow; i++)
        {
            for(int j = imageMinColumn; j < Math.min(roiMinColumn, imageMaxColumn + 1); j++)
            {
                recepient.addPointOutside(i, j);
            }  

            for(int j = Math.max(roiMaxColumn + 1, imageMinColumn); j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            } 
        }

        for(int i = Math.max(roiMaxRow + 1, imageMinRow); i <= imageMaxRow; i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }

        ////////////////////////////////

        int boundedRowCount = roiMaxRow - roiMinRow + 1;
        int boundedColumnCount = roiMaxColumn - roiMinColumn + 1;

        int cellRowCount = Math.max(1, boundedRowCount/cellHeight);
        int cellColumnCount = Math.max(1, boundedColumnCount/cellWidth);

        int lastCellHeight = Math.min(boundedRowCount, cellHeight + boundedRowCount%cellHeight);      
        int lastCellWidth = Math.min(boundedColumnCount, cellWidth + boundedColumnCount%cellWidth);

        for(int k = 0; k<cellRowCount; k++)
        {
            for(int l = 0; l<cellColumnCount; l++)
            {               
                int row = roiMinRow + k*cellHeight;
                int column = roiMinColumn + l*cellWidth;

                int pixelWidth = (l == cellColumnCount - 1) ? lastCellWidth : cellWidth;
                int pixelHeight = (k == cellRowCount - 1) ? lastCellHeight : cellHeight;

                boolean contained = roiShape.contains(grid.getRectangle(row, column, pixelWidth, pixelHeight));

                if(contained)
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            recepient.addPointInside(i, j);
                        }
                    }
                }
                else
                {
                    for(int i = row; i<row + pixelHeight; i++)
                    {
                        for(int j = column; j<column + pixelWidth; j++)
                        {
                            boolean outside = !roiShape.contains(grid.getPoint(i, j));

                            if(outside)
                            {
                                recepient.addPointOutside(i, j);
                            }
                            else
                            {
                                recepient.addPointInside(i, j);
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
}
