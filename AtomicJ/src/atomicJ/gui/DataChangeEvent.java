package atomicJ.gui;

import java.util.Set;

public class DataChangeEvent<I>
{
    private final Object source;
    private final Set<I> dataTypes;

    public DataChangeEvent(Object source, Set<I> dataTypes)
    {
        this.source = source;
        this.dataTypes = dataTypes;
    }

    public Object getSource()
    {
        return source;
    }

    public Set<I> getDataTypes()
    {
        return dataTypes;
    }
}
