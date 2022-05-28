package atomicJ.data;

public class PermissiveChannel2DFilter implements ChannelFilter2<Channel2D>
{
    private static PermissiveChannel2DFilter INSTANCE = new PermissiveChannel2DFilter();

    public static PermissiveChannel2DFilter getInstance()
    {
        return INSTANCE;
    }

    private PermissiveChannel2DFilter()
    {}

    @Override
    public boolean accepts(Channel2D channel)
    {
        return true;
    }
}
