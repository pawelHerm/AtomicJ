package atomicJ.resources;

import atomicJ.sources.ChannelSource;

public interface DataModelResource extends Resource
{
    public boolean containsChannelsFromSource(ChannelSource source);
}
