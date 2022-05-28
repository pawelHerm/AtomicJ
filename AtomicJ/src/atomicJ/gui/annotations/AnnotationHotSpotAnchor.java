package atomicJ.gui.annotations;

import java.awt.Cursor;


public class AnnotationHotSpotAnchor implements AnnotationAnchorCore
{
    private final int index;

    public AnnotationHotSpotAnchor(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public Cursor getCursor(boolean isVertical) 
    {
        return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }

    @Override
    public boolean isOnEdge() 
    {
        return true;
    }
}
