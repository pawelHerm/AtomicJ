package atomicJ.gui.annotations;

import java.awt.Cursor;

public interface AnnotationAnchorCore 
{
    public boolean isOnEdge();
    public Cursor getCursor(boolean isVertical);
}
