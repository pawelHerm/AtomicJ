package atomicJ.data;

public class Channel1DMinimumSizeFilter implements ChannelFilter2<Channel1D>
{
    private final int minSize;

    public Channel1DMinimumSizeFilter(int minSize)
    {
        this.minSize = minSize;
    }

    @Override
    public boolean accepts(Channel1D channel)
    {
        if(channel == null)
        {
            return false;
        }

        int itemCount = channel.getItemCount();

        boolean accepts = itemCount >= minSize;

        return accepts;
    }
}
