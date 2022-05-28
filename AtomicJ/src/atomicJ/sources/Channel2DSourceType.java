package atomicJ.sources;

public enum Channel2DSourceType {

    ALL("All"), MAP("Map"), IMAGE("Image");

    private final String name;

    Channel2DSourceType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
