package atomicJ.sources;

import java.util.Collection;
import java.util.List;

import atomicJ.data.Channel1D;

public interface Channel1DSource<E extends Channel1D> extends ChannelSource
{
    @Override
    public List<? extends E> getChannels();
    @Override
    public List<? extends E> getChannels(Collection<String> identifiers);
}
