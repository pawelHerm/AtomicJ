package atomicJ.imageProcessing;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Grid2D;
import atomicJ.data.GridIndex;

public class MooreContourROIBoolean 
{
    private static int[][] neighbourhoodDirs = new int[][] {{1,0},{1,-1},{0, -1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1}};
    private static int[] entranceDirs = new int[] {6, 6, 0, 0, 2, 2, 4, 4};

    public Path2D getContour(boolean[][] gridData, Grid2D grid, boolean foreground)
    {
        TDoubleList xs = new TDoubleArrayList();
        TDoubleList ys = new TDoubleArrayList();

        List<GridIndex> contour = new ArrayList<>();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();


        int[] contourPixelData = getInitialBorderPixel(gridData, grid, foreground);

        GridIndex startT = new GridIndex(contourPixelData[0], contourPixelData[1]);

        //if the starting pixel is outside bounds, then there are no foreground pixels in the image
        //the whole image is just empty background
        if(startT.isWithinBounds(rowCount, columnCount))
        {
            contour.add(startT);                


            Point2D initCorner = grid.getCornerPoint(startT.getRow(), startT.getColumn(), 3);

            xs.add(initCorner.getX());
            ys.add(initCorner.getY());

            getNextBlackPixel(grid, gridData, contourPixelData, xs, ys, foreground);         
            GridIndex currentT = new GridIndex(contourPixelData[0], contourPixelData[1]);

            while(!startT.equals(currentT))
            {
                contour.add(currentT);   

                getNextBlackPixel(grid, gridData, contourPixelData, xs, ys, foreground);         

                currentT = new GridIndex(contourPixelData[0], contourPixelData[1]);
            }            
        }   

        Path2D path = new GeneralPath();

        if(xs.isEmpty())
        {
            return path;
        }

        path.moveTo(xs.get(0), ys.get(0));

        for(int i = 1; i < xs.size(); i++)
        {
            path.lineTo(xs.get(i), ys.get(i));
        }

        path.closePath();

        return path;
    }


    private void getNextBlackPixel(Grid2D grid, boolean[][] gridData, int[] currentTracePixelData,TDoubleList xs, TDoubleList ys, boolean foreground)
    {
        Point2D cornerA = grid.getCornerPoint(currentTracePixelData[0], currentTracePixelData[1],
                currentTracePixelData[2]/2);
        xs.add(cornerA.getX());
        ys.add(cornerA.getY());

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        for(int i = 0; i < 8 ; i++)
        {
            int dirIndex = (currentTracePixelData[2] + 1 + i)%8;
            int[] deltas = neighbourhoodDirs[dirIndex];

            int rowNeighbour = currentTracePixelData[0] + deltas[0];
            int columnNeighbour = currentTracePixelData[1] + deltas[1];

            if(GridIndex.isWithinBounds(rowNeighbour, columnNeighbour, rowCount, columnCount)
                    && gridData[rowNeighbour][columnNeighbour] == foreground)
            {
                currentTracePixelData[0] = rowNeighbour;
                currentTracePixelData[1] = columnNeighbour;

                // Instead of reading this value from array, we could use 
                //result[2] = (2*(i/2 - 1) + 8) % 8;

                currentTracePixelData[2] = entranceDirs[dirIndex];

                break;
            }                
            else if(dirIndex % 2 == 0) //only in von Neuman neighbourhood
            {
                Point2D cornerB = grid.getCornerPoint(currentTracePixelData[0], currentTracePixelData[1],
                        dirIndex/2);
                xs.add(cornerB.getX());
                ys.add(cornerB.getY());

            }
        }
    }

    private int[] getInitialBorderPixel(boolean[][] gridData, Grid2D grid, boolean foreground)
    {        
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        for(int i = rowCount - 1; i>= 0; i--)
        {
            boolean[] row = gridData[i];

            for(int j = 0; j<columnCount; j++)
            {
                if(row[j] == foreground)
                {
                    return new int[] {i, j, 0};
                }
            }        
        }

        return new int[] {-1, -1, 0};
    }
}
