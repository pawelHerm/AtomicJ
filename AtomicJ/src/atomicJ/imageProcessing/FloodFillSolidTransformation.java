package atomicJ.imageProcessing;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.prefs.Preferences;

import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.GridIndex;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIPolygon;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.utilities.ArrayUtilities;

public class FloodFillSolidTransformation implements Channel2DDataInROITransformation 
{
    private final double initX;
    private final double initY;

    private final double fillValue;
    private final double minDifference;
    private final double maxDifference;


    public FloodFillSolidTransformation(double initX, double initY, double minDifference, double maxDifference, double fillValue)
    {
        this.initX = initX;
        this.initY = initY;

        this.minDifference = minDifference;
        this.maxDifference = maxDifference;
        this.fillValue = fillValue;
    }
    //        FlexibleDataDomain dataDomain = channelData.getFlexibleDataDomain();

    @Override
    public Channel2DData transform(Channel2DData channelData)
    {         
        GridChannel2DData gridChannelData = channelData.getDefaultGridding();

        Grid2D grid = gridChannelData.getGrid();
        Quantity zQuantity = gridChannelData.getZQuantity();
        double[][] gridData = gridChannelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int initRow = grid.getRow(initY);
        int initColumn = grid.getColumn(initX);

        if(initRow < 0 || initRow >= rowCount || initColumn < 0 || initColumn >= columnCount)
        {
            return channelData;
        }

        final double[][] transformed = ArrayUtilities.deepCopy(gridData);


        Deque<GridIndex> queue = new LinkedList<>();    
        queue.addFirst(new GridIndex(initRow, initColumn));

        double initValue = gridData[initRow][initColumn];
        Set<GridIndex> visitedPixels = new HashSet<>();

        boolean[][] regionLabelArray = new boolean[rowCount][columnCount];

        while(!queue.isEmpty())
        {
            GridIndex currentPixel = queue.removeLast();
            int indexRow = currentPixel.getRow();
            int indexColumn = currentPixel.getColumn();

            if(indexRow >= 0 && indexRow < rowCount && 
                    indexColumn >= 0 && indexColumn < columnCount && !visitedPixels.contains(currentPixel))
            {
                double diff = gridData[indexRow][indexColumn] - initValue;

                if(diff <= maxDifference && diff >= minDifference)
                {
                    transformed[indexRow][indexColumn] = fillValue;
                    regionLabelArray[indexRow][indexColumn] = true;

                    queue.addFirst(new GridIndex(indexRow + 1, indexColumn));
                    queue.addFirst(new GridIndex(indexRow, indexColumn + 1));
                    queue.addFirst(new GridIndex(indexRow, indexColumn - 1));
                    queue.addFirst(new GridIndex(indexRow - 1, indexColumn));

                    visitedPixels.add(currentPixel);
                }
            }
        }

        MooreContourROIBoolean contourTracer = new MooreContourROIBoolean();
        Path2D contour = contourTracer.getContour(regionLabelArray, grid, true);

        ROI roi = new ROIPolygon(contour, 0, "Contour", new ROIStyle(Preferences.userRoot(), Color.red));

        roi.addPoints(grid, ROIRelativePosition.INSIDE, new GridPointRecepient() {

            @Override
            public void addPoint(int row, int column) 
            {
                transformed[row][column] = fillValue;                    
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] row = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        row[j] = fillValue;                    
                    }
                }
            }
        });


        Channel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position)
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

        if(initRow < 0 || initRow >= rowCount || 
                initColumn < 0 || initColumn >= columnCount)
        {
            return channelData;

        }


        boolean[][] positionArray = ROIUtilities.getInsidnessArray(roi, grid);

        if(!positionArray[initRow][initColumn])
        {
            return channelData;
        }

        final double[][] transformed = ArrayUtilities.deepCopy(gridData);

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

        MooreContourROIBoolean contourTracer = new MooreContourROIBoolean();
        Path2D contour = contourTracer.getContour(positionArray, grid, true);

        ROI roiContour = new ROIPolygon(contour, 0, "Contour", new ROIStyle(Preferences.userRoot(), Color.red));      
        roiContour.addPoints(grid, ROIRelativePosition.INSIDE, new GridPointRecepient() {

            @Override
            public void addPoint(int row, int column) 
            {
                transformed[row][column] = fillValue;                    
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] row = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        row[j] = fillValue;                    
                    }
                }
            }
        });

        Channel2DData dataMatrix = new GridChannel2DData(transformed, grid, zQuantity);


        return dataMatrix;
    }
}
