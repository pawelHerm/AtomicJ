package atomicJ.sources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import atomicJ.data.Channel2D;

public class ProcessedPackReplacementResults
{
    //the keys are universalId 
    private final Map<String, Channel2D> updatedChannels;
    private final Map<String, Channel2D> newChannels;

    public ProcessedPackReplacementResults(Map<String, Channel2D> newChannels, Map<String, Channel2D> updatedChannels)
    {
        this.newChannels = new LinkedHashMap<>(newChannels);
        this.updatedChannels = new LinkedHashMap<>(updatedChannels);
    }

    public Map<String, Channel2D> getUpdatedChannels()
    {
        return Collections.unmodifiableMap(updatedChannels);
    }

    public Map<String, Channel2D> getNewChannels()
    {
        return Collections.unmodifiableMap(newChannels);
    }
}