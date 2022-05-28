package atomicJ.analysis;

import java.util.Map;

import atomicJ.gui.ChannelChart;

public interface Visualizable
{
    public Map<String, ChannelChart<?>> visualize();
}