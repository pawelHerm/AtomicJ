package atomicJ.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public interface MouseInteractiveToolSupervisor
{
    public void notifyToolsOfMouseClicked(CustomChartMouseEvent event);
    public void notifyToolsOfMouseDragged(CustomChartMouseEvent evt);
    public void notifyToolsOfMouseMoved(CustomChartMouseEvent evt);
    public void notifyToolsOfMousePressed(CustomChartMouseEvent evt);
    public void notifyToolsOfMouseReleased(CustomChartMouseEvent evt);
    public void useMouseInteractiveTool(MouseInteractiveTool tool); 
    public void stopUsingMouseInteractiveTool(MouseInteractiveTool tool);    
    public boolean isChartElementCaughtByTool();
    public boolean isRightClickReservedByTool(Rectangle2D dataArea, Point2D dataPoint);
    public boolean isComplexElementUnderConstructionByTool();
    public MouseInteractiveTool getCurrentlyUsedInteractiveTool();
}
