package atomicJ.data;

public class StandardChannelMetadata implements ChannelMetadata
{
    private final Coordinate4D coordinates;

    public StandardChannelMetadata(Coordinate4D coordinates)
    {
        this.coordinates = coordinates;
    }

    @Override
    public Coordinate4D getCoordinates()
    {
        return coordinates;
    }

    @Override
    public ChannelMetadata copyIfNeccesary() 
    {
        return this;
    }
}