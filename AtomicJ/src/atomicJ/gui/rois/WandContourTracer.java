package atomicJ.gui.rois;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import atomicJ.data.Grid2D;
import atomicJ.data.GridIndex;
import atomicJ.data.ImageMatrix;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.imageProcessing.MooreContourROIBoolean;

public class WandContourTracer 
{
    private final double minDifference;
    private final double maxDifference;
    private final PrefixedUnit unit;

    private final boolean isWellFormed;

    private final ROIRelativePosition position;
    private final ROI roi;

    public WandContourTracer(double minDifference, double maxDifference, PrefixedUnit unit)
    {
        this(minDifference, maxDifference, ROIRelativePosition.EVERYTHING, null, unit);
    }

    public WandContourTracer(double minDifference, double maxDifference, ROIRelativePosition position, ROI roi, PrefixedUnit unit)
    {
        this.minDifference = minDifference;
        this.maxDifference = maxDifference;
        this.unit = unit;

        this.position = position;
        this.roi = roi;

        this.isWellFormed = !Double.isNaN(minDifference) && !Double.isNaN(maxDifference)
                && (unit != null) && (ROIRelativePosition.EVERYTHING.equals(position) || roi != null);
    }

    public boolean isWellFormed()
    {
        return isWellFormed;
    }

    public Path2D getContour(ImageMatrix channel, Point2D p)
    {     
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getContourROIIndependent(channel, p);
        }
        else
        {
            return getContourROIZRependent(channel, roi, position, p);
        }
    }

    public Path2D getContourROIIndependent(ImageMatrix channel, Point2D p)
    {     
        if(!isWellFormed)
        {
            return new GeneralPath();
        }


        PrefixedUnit dataUnit = channel.getZQuantity().getUnit();
        double conversionFactor = this.unit.getConversionFactorTo(dataUnit);

        Grid2D grid = channel.getGrid();
        double[][] gridData = channel.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        GridIndex item = grid.getGridIndex(p);

        if(!item.isWithinBounds(rowCount, columnCount))
        {
            return new GeneralPath();
        }

        Deque<GridIndex> queue = new LinkedList<>();    
        queue.addFirst(item);

        double initValue = gridData[item.getRow()][item.getColumn()];
        Set<GridIndex> visitedPixels = new HashSet<>();

        boolean[][] regionLabelArray = new boolean[rowCount][columnCount];

        double convertedMinDifference = conversionFactor*minDifference;
        double convertedMaxDifference = conversionFactor*maxDifference;

        while(!queue.isEmpty())
        {
            GridIndex currentPixel = queue.removeLast();
            int indexRow = currentPixel.getRow();
            int indexColumn = currentPixel.getColumn();

            if(indexRow >= 0 && indexRow < rowCount && 
                    indexColumn >= 0 && indexColumn < columnCount 
                    && !visitedPixels.contains(currentPixel))
            {
                double diff = gridData[indexRow][indexColumn] - initValue;

                if(diff <= convertedMaxDifference && diff >= convertedMinDifference)
                {
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
        contour.closePath();

        return contour;
    }

    private Path2D getContourROIZRependent(ImageMatrix channel, ROI roi, ROIRelativePosition position, Point2D p)
    {     
        if(!isWellFormed)
        {
            return new GeneralPath();
        }

        PrefixedUnit dataUnit = channel.getZQuantity().getUnit();
        double conversionFactor = this.unit.getConversionFactorTo(dataUnit);

        Grid2D grid = channel.getGrid();
        double[][] gridData = channel.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        GridIndex item = grid.getGridIndex(p);

        if(!item.isWithinBounds(rowCount, columnCount))
        {
            return new GeneralPath();
        }

        final boolean[][] positionArray = new boolean[rowCount][columnCount];

        roi.addPoints(grid, position, new GridPointRecepient() {

            @Override
            public void addPoint(int row, int column) 
            {
                positionArray[row][column] = true;                    
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    boolean[] row = positionArray[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        row[j] = true;                    
                    }
                }
            }
        });


        if(!positionArray[item.getRow()][item.getColumn()])
        {
            return new GeneralPath();          
        }

        Deque<GridIndex> queue = new LinkedList<>();    
        queue.addFirst(item);

        double initValue = gridData[item.getRow()][item.getColumn()];
        Set<GridIndex> visitedPixels = new HashSet<>();

        boolean[][] regionLabelArray = new boolean[rowCount][columnCount];

        double convertedMinDifference = conversionFactor*minDifference;
        double convertedMaxDifference = conversionFactor*maxDifference;

        while(!queue.isEmpty())
        {
            GridIndex currentPixel = queue.removeLast();
            int indexRow = currentPixel.getRow();
            int indexColumn = currentPixel.getColumn();

            if(indexRow >= 0 && indexRow < rowCount && 
                    indexColumn >= 0 && indexColumn < columnCount 
                    && positionArray[indexRow][indexColumn] && !visitedPixels.contains(currentPixel))
            {
                double diff = gridData[indexRow][indexColumn] - initValue;

                if(diff <= convertedMaxDifference && diff >= convertedMinDifference)
                {
                    gridData[indexRow][indexColumn] = -2;
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
        contour.closePath();

        return contour;
    }
}
