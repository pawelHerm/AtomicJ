package atomicJ.imageProcessing;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.GridIndex;

public class Contour 
{
    private final List<GridIndex> trace;
    private final int minRow;
    private final int maxRow;

    public Contour(List<GridIndex> trace, int minRow, int maxRow)
    {
        this.trace = new ArrayList<>(trace);
        this.minRow = minRow;
        this.maxRow = maxRow;              
    }

    public List<GridIndex> getTrace()
    {
        return new ArrayList<>(trace);
    }

    public int getMinRow()
    {
        return minRow;
    }

    public int getMaxRow()
    {
        return maxRow;
    }
}
