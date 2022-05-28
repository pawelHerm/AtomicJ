package atomicJ.readers;

import java.util.List;

import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;

public interface MapDelayedCreator 
{
    public boolean accepts(SimpleSpectroscopySource source);
    public MapSource<?> buildMapSource(List<SimpleSpectroscopySource> sources);
}
