package atomicJ.sources;

import atomicJ.data.Channel2D;

public class IdentityTagger implements ChannelTagger<Channel2D>
{
    private static final IdentityTagger INSTANCE = new IdentityTagger();

    private IdentityTagger(){};

    public static IdentityTagger getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Channel2D getTag(Channel2D channel, int channelIndex) 
    {
        return channel;
    }      
}