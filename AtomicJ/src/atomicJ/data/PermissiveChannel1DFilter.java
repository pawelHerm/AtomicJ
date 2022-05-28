package atomicJ.data;

public class PermissiveChannel1DFilter implements ChannelFilter2<Channel1D>
{
    private static PermissiveChannel1DFilter INSTANCE = new PermissiveChannel1DFilter();

    public static PermissiveChannel1DFilter getInstance()
    {
        return INSTANCE;
    }

    private PermissiveChannel1DFilter()
    {}

    @Override
    public boolean accepts(Channel1D channel)
    {
        return true;
    }
}
