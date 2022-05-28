package atomicJ.data;

import atomicJ.data.units.Quantity;

public class PermissiveChannelFilter implements ChannelFilter
{
    private static final PermissiveChannelFilter INSTANCE = new PermissiveChannelFilter();

    public static PermissiveChannelFilter getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean accepts(String identifier, Quantity quantity)
    {
        return true;
    }

}
