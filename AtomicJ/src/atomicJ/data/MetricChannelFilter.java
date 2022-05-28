package atomicJ.data;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;

public class MetricChannelFilter implements ChannelFilter
{
    private static final MetricChannelFilter INSTANCE = new MetricChannelFilter();

    public static MetricChannelFilter getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean accepts(String identifier, Quantity quantity) 
    {
        PrefixedUnit unit = quantity.getUnit();

        return StandardQuantityTypes.LENGTH.isCompatible(unit);
    }
}
