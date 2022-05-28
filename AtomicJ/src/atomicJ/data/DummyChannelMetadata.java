package atomicJ.data;

public class DummyChannelMetadata implements ChannelMetadata
{
    private static final DummyChannelMetadata INSTANCE = new DummyChannelMetadata();

    private DummyChannelMetadata()
    {}

    public static DummyChannelMetadata getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Coordinate4D getCoordinates()
    {
        return null;
    }

    @Override
    public ChannelMetadata copyIfNeccesary() {
        return this;
    }
}