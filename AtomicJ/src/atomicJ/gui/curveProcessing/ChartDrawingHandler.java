package atomicJ.gui.curveProcessing;

import java.util.Map;

import atomicJ.analysis.Visualizable;
import atomicJ.gui.ChannelChart;

public interface ChartDrawingHandler <E extends Visualizable>
{
    public void sendChartsToDestination(Map<E, Map<String, ChannelChart<?>>> charts);
    public void handleFinishDrawingRequest();
    public void reactToFailures(int failureCount);
    public void reactToCancellation();
}