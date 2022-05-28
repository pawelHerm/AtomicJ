package atomicJ.gui;

import java.util.Map;
import atomicJ.sources.Channel2DSource;

public interface Channel2DSourceVisualizator 
{
    public Map<String,Channel2DChart<?>> getCharts(Channel2DSource<?> source);
}
