package atomicJ.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface MouseInputResponse 
{
    public void mousePressed(CustomChartMouseEvent event);
    public void mouseReleased(CustomChartMouseEvent event);
    public void mouseDragged(CustomChartMouseEvent event);
    public void mouseMoved(CustomChartMouseEvent event);
    public void mouseClicked(CustomChartMouseEvent event);
    public boolean isChartElementCaught();
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint);
}
