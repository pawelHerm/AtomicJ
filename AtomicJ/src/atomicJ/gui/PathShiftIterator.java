package atomicJ.gui;

import java.awt.geom.PathIterator;

public class PathShiftIterator implements PathIterator
{
    private final PathIterator originalIterator;

    private final int index;
    private final double dx;
    private final double dy;

    private int currentPointIndex = 0;

    public PathShiftIterator(PathIterator originalIteator, int index, double dx, double dy)
    {
        this.originalIterator = originalIteator;

        this.index = index;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public int currentSegment(float[] coords) 
    {
        int segmentType = originalIterator.currentSegment(coords);

        int diff = index - currentPointIndex;

        if(diff >= 0 && diff < PathSegment.instanceFor(segmentType).getPointCount())
        {
            coords[diff*2] = coords[diff*2] + (float)dx;
            coords[diff*2 + 1] = coords[diff*2 + 1] + (float)dy;
        }
        else
        {
            originalIterator.currentSegment(coords);
        }

        return segmentType;
    }

    @Override
    public int currentSegment(double[] coords) 
    {
        int segmentType = originalIterator.currentSegment(coords);

        if(currentPointIndex == index)
        {
            for(int i = 0; i<coords.length; i++)
            {
                boolean even = (i % 2 == 0);
                coords[i] = (even ? coords[i] + dx : coords[i] + dy);
            }
        }
        else
        {
            originalIterator.currentSegment(coords);
        }

        return segmentType;
    }

    @Override
    public int getWindingRule() 
    {
        return originalIterator.getWindingRule();
    }

    @Override
    public boolean isDone() 
    {
        return originalIterator.isDone();
    }

    @Override
    public void next() 
    {
        int segmentType = originalIterator.currentSegment(new float[6]);

        this.currentPointIndex = currentPointIndex + PathSegment.instanceFor(segmentType).getPointCount();

        originalIterator.next();       
    }
}
