package atomicJ.gui;

import java.util.Map;
import atomicJ.sources.Channel1DSource;

public interface Channel1DSourceVisualizator 
{
    public Map<String,Channel1DChart<?>> getCharts(Channel1DSource<?> source);
}

