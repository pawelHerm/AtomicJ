package atomicJ.readers;

import atomicJ.data.ChannelFilter;

public class SourceReadingDirectives 
{
    private final int sourceCount;
    private boolean canceled = false;
    private final ChannelFilter filter;

    public SourceReadingDirectives(ChannelFilter filter, int sourceCount)
    {
        this.filter = filter;
        this.sourceCount = sourceCount;
    }

    public int getSourceCount()
    {
        return sourceCount;
    }

    public ChannelFilter getDataFilter()
    {
        return filter;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void setCanceled(boolean canceled)
    {
        this.canceled = canceled;
    }
}
