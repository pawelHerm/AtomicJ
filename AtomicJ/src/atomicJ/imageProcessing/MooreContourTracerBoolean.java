package atomicJ.imageProcessing;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Grid2D;
import atomicJ.data.GridIndex;

public class MooreContourTracerBoolean 
{
    private static int[][] neighbourhoodDirs = new int[][] {{1,0},{1,-1},{0, -1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1}};
    private static int[] entranceDirs = new int[] {6, 6, 0, 0, 2, 2, 4, 4};

    public Contour getContour(boolean[][] gridData, Grid2D grid, boolean foreground)
    {
        List<GridIndex> tracePixels = new ArrayList<>();

        int rowCount = grid.getRowCount();
        int columnCount= grid.getColumnCount();


        int[] currentTracePixelData = getInitialBorderPixel(gridData, grid, foreground);

        int minRow =  currentTracePixelData[0];       
        int maxRow = minRow;

        GridIndex startT = new GridIndex(currentTracePixelData[0], currentTracePixelData[1]);

        //if the starting pixel is outside bounds, then there are no foreground pixels in the image
        //the whole image is just empty background
        if(startT.isWithinBounds(rowCount, columnCount))
        {
            tracePixels.add(startT);                

            getNextBlackPixel(gridData, currentTracePixelData, rowCount, columnCount, foreground);         
            GridIndex currentT = new GridIndex(currentTracePixelData[0], currentTracePixelData[1]);

            while(!startT.equals(currentT))
            {
                tracePixels.add(currentT);

                int row = currentTracePixelData[0];
                if(row < minRow)
                {
                    minRow = row;
                }
                if(row > maxRow)
                {
                    maxRow = row;
                }

                getNextBlackPixel(gridData, currentTracePixelData, rowCount, columnCount, foreground);         

                currentT = new GridIndex(currentTracePixelData[0], currentTracePixelData[1]);
            }
        }   

        Contour c = new Contour(tracePixels, minRow, maxRow);
        return c;
    }


    private void getNextBlackPixel(boolean[][] gridData, int[] currentTracePixelData, int rowCount, int columnCount, boolean foreground)
    {
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
