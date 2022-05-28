package atomicJ.data;

public class ChannelGroupTag 
{
    private final Object groupId;
    private final int index;
    private final String defaultChannelId;

    public ChannelGroupTag(Object groupId, int index)
    {
        this.groupId = groupId;
        this.index = index;
        this.defaultChannelId = groupId.toString() + " " + Integer.toString(index + 1);
    }

    public Object getGroupId()
    {
        return groupId;
    }

    public int getIndex()
    {
        return index;
    }

    public String getDefaultChannelIdentifier()
    {
        return defaultChannelId;
    }
}
