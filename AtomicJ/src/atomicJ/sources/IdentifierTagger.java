package atomicJ.sources;

import atomicJ.data.Channel2D;

public class IdentifierTagger implements ChannelTagger<String>
{
    private static final IdentifierTagger INSTANCE = new IdentifierTagger();

    private IdentifierTagger(){};

    public static IdentifierTagger getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getTag(Channel2D channel, int channelIndex) 
    {
        return channel.getIdentifier();
    }      
}