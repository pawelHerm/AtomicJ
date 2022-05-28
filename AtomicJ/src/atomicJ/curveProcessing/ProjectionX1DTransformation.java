package atomicJ.curveProcessing;

import java.awt.geom.Point2D;

import atomicJ.data.Channel1DData;

public class ProjectionX1DTransformation
{   
    public Point2D transform(Channel1DData projectionChannel, double x) 
    {     
        int columnIndex = projectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(x);      

        Point2D projected = new Point2D.Double(projectionChannel.getX(columnIndex), projectionChannel.getY(columnIndex));
        return projected;
    }
}
