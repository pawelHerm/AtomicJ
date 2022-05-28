package atomicJ.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class ToolManager implements MouseInputResponse
{
    private final Channel2DChart<?> densityChart;

    ToolManager(Channel2DChart<?> densityChart) {
        this.densityChart = densityChart;
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        if(!MouseInputModeStandard.TOOL_MODE.equals(this.densityChart.getMode(MouseInputType.PRESSED)))
        {
            return;
        }
        this.densityChart.supervisor.notifyToolsOfMousePressed(event);
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!MouseInputModeStandard.TOOL_MODE.equals(this.densityChart.getMode(MouseInputType.RELEASED)))
        {
            return;
        }
        this.densityChart.supervisor.notifyToolsOfMouseReleased(event);
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event)
    {
        if(!MouseInputModeStandard.TOOL_MODE.equals(this.densityChart.getMode(MouseInputType.DRAGGED)))
        {
            return;
        }
        this.densityChart.supervisor.notifyToolsOfMouseDragged(event);
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {
        if(!MouseInputModeStandard.TOOL_MODE.equals(this.densityChart.getMode(MouseInputType.MOVED)))
        {
            return;
        }

        this.densityChart.supervisor.notifyToolsOfMouseMoved(event);
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        if(!MouseInputModeStandard.TOOL_MODE.equals(this.densityChart.getMode(MouseInputType.CLICKED)))
        {
            return;
        }

        this.densityChart.supervisor.notifyToolsOfMouseClicked(event);
    }

    @Override
    public boolean isChartElementCaught() 
    {        
        return this.densityChart.supervisor.isChartElementCaughtByTool();
    }

    public boolean isComplexElementUnderConstruction()
    {
        return this.densityChart.supervisor.isComplexElementUnderConstructionByTool();
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataArea, Point2D dataPoint)
    {
        return this.densityChart.supervisor.isRightClickReservedByTool(dataArea, dataPoint);
    }
}