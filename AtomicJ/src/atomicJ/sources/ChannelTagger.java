package atomicJ.sources;

import atomicJ.data.Channel2D;

public interface ChannelTagger<E>
{
    public E getTag(Channel2D channel, int channelIndex);
}