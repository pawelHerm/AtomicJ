package atomicJ.imageProcessing;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.GridIndex;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.utilities.ArrayUtilities;

public class FloodFillTransformation implements Channel2DDataInROITransformation 
{
    private final double initX;
    private final double initY;

    private final double fillValue;
    private final double minDifference;
    private final double maxDifference;


    public FloodFillTransformation(double initX, double initY, double minDifference, double maxDifference, double fillValue)
    {
        this.initX = initX;
        this.initY = initY;

        this.minDifference = minDifference;
        this.maxDifference = maxDifference;
        this.fillValue = fillValue;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData)
    { 
        GridChannel2DData gridChannelData = channelData.getDefaultGridding();
        Quantity zQuantity = channelData.getZQuantity();
        Grid2D grid = gridChannelData.getGrid();
        double[][] original = gridChannelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();


        int initRow = grid.getRow(initY);
        int initColumn = grid.getColumn(initX);

        if(initRow < 0 || initRow >= rowCount || initColumn < 0 || initColumn >= columnCount)
        {
            return channelData;
        }

        double[][] transformed = ArrayUtilities.deepCopy(original);

        Deque<GridIndex> queue = new LinkedList<>();    
        queue.addFirst(new GridIndex(initRow, initColumn));

        double initValue = original[initRow][initColumn];
        Set<GridIndex> visitedPixels = new HashSet<>();

        while(!queue.isEmpty())
        {
            GridIndex currentPixel = queue.removeLast();
            int indexRow = currentPixel.getRow();
            int indexColumn = currentPixel.getColumn();

            if(indexRow >= 0 && indexRow < rowCount && 
                    indexColumn >= 0 && indexColumn < columnCount && !visitedPixels.contains(currentPixel))
            {
                double diff = original[indexRow][indexColumn] - initValue;

                if(diff <= maxDifference && diff >= minDifference)
                {
                    transformed[indexRow][indexColumn] = fillValue;

                    queue.addFirst(new GridIndex(indexRow + 1, indexColumn));
                    queue.addFirst(new GridIndex(indexRow, indexColumn + 1));
                    queue.addFirst(new GridIndex(indexRow, indexColumn - 1));
                    queue.addFirst(new GridIndex(indexRow - 1, indexColumn));

                    visitedPixels.add(currentPixel);
                }
            }
        }

        GridChannel2DData transformedChannelData = new GridChannel2DData(transformed, grid, zQuantity);

        return transformedChannelData;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData, ROI roi,
            ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        GridChannel2DData gridChannelData = channelData.getDefaultGridding();
        Quantity zQuantity = gridChannelData.getZQuantity();
        final Grid2D grid = gridChannelData.getGrid();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();
        double[][] gridData = gridChannelData.getData();

        int initRow = grid.getRow(initY);
        int initColumn = grid.getColumn(initX);

        if(initRow < 0 || initRow >= rowCount || initColumn < 0 || initColumn >= columnCount)
        {
            return channelData;
        }

        boolean[][] positionArray = ROIUtilities.getInsidnessArray(roi, grid);

        if(!positionArray[initRow][initColumn])
        {
            return channelData;
        }

        double[][] transformed = ArrayUtilities.deepCopy(gridData);

        Deque<GridIndex> queue = new LinkedList<>();    
        queue.addFirst(new GridIndex(initRow, initColumn));

        double initValue = gridData[initRow][initColumn];
        Set<GridIndex> visitedPixels = new HashSet<>();

        while(!queue.isEmpty())
        {
            GridIndex currentPixel = queue.removeLast();
            int indexRow = currentPixel.getRow();
            int indexColumn = currentPixel.getColumn();

            if(indexRow >= 0 && indexRow < rowCount && 
                    indexColumn >= 0 && indexColumn < columnCount && 
                    positionArray[indexRow][indexColumn] && !visitedPixels.contains(currentPixel))
            {
                double diff = gridData[indexRow][indexColumn] - initValue;

                if(diff <= maxDifference && diff >= minDifference)
                {
                    transformed[indexRow][indexColumn] = fillValue;

                    queue.addFirst(new GridIndex(indexRow + 1, indexColumn));
                    queue.addFirst(new GridIndex(indexRow, indexColumn + 1));
                    queue.addFirst(new GridIndex(indexRow, indexColumn - 1));
                    queue.addFirst(new GridIndex(indexRow - 1, indexColumn));

                    visitedPixels.add(currentPixel);
                }
            }
        }

        GridChannel2DData transformedChannelData = new GridChannel2DData(transformed, grid, zQuantity);
        return transformedChannelData;
    }
}
