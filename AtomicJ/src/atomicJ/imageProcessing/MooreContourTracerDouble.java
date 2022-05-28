package atomicJ.imageProcessing;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Grid2D;
import atomicJ.data.GridIndex;

public class MooreContourTracerDouble 
{
    private static int[][] neighbourhoodDirs = new int[][] {{1,0},{1,-1},{0, -1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1}};
    private static int[] entranceDirs = new int[] {6, 6, 0, 0, 2, 2, 4, 4};

    public List<GridIndex> getContour(double[][] gridData, Grid2D grid, double foreground)
    {
        List<GridIndex> contour = new ArrayList<>();

        int rowCount = grid.getRowCount();
        int columnCount= grid.getColumnCount();


        int[] currentTracePixelData = getInitialBorderPixel(gridData, grid, foreground);

        GridIndex startT = new GridIndex(currentTracePixelData[0], currentTracePixelData[1]);

        //if the starting pixel is outside bounds, then there are no foreground pixels in the image
        //the whole image is just empty background
        if(startT.isWithinBounds(rowCount, columnCount))
        {
            contour.add(startT);                

            getNextBlackPixel(gridData, currentTracePixelData, rowCount, columnCount, foreground);         
            GridIndex currentT = new GridIndex(currentTracePixelData[0], currentTracePixelData[1]);

            while(!startT.equals(currentT))
            {
                contour.add(currentT);                
                getNextBlackPixel(gridData, currentTracePixelData, rowCount, columnCount, foreground);         

                currentT = new GridIndex(currentTracePixelData[0], currentTracePixelData[1]);
            }
        }   

        markContour(gridData, contour);
        return contour;
    }

    private void markContour(double[][] gridData, List<GridIndex> contour)
    {                
        for(GridIndex contourItem : contour)
        {
            gridData[contourItem.getRow()][contourItem.getColumn()] = -2;
        }
    }

    private void getNextBlackPixel(double[][] gridData, int[] currentTracePixelData, int rowCount, int columnCount, double foreground)
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

    private int[] getInitialBorderPixel(double[][] gridData, Grid2D grid, double foreground)
    {        
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        for(int i = rowCount - 1; i>= 0; i--)
        {
            double[] row = gridData[i];

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
