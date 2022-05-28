package atomicJ.data;

import atomicJ.data.units.Quantity;

public interface ChannelFilter 
{
    boolean accepts(String identifier, Quantity quantity);
}
