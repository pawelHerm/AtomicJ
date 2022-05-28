package atomicJ.gui;

public class MapMarkerChannelLabelType implements MapMarkerLabelType
{
    private final String channelType;

    public MapMarkerChannelLabelType(String channelType)
    {
        this.channelType = channelType;
    }

    @Override
    public String name()
    {
        return channelType;
    }

    @Override
    public String getLabel(MapMarker marker)
    {
        String label = marker.getValueLabel(channelType);

        return label;
    }

    @Override
    public String toString()
    {
        return channelType;
    }
}
