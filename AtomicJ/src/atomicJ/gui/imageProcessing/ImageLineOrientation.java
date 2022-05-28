package atomicJ.gui.imageProcessing;

public enum ImageLineOrientation 
{
    HORIZONTAL("Horizontal"), VERTICAL("Vertical");

    private final String prettyName;

    private ImageLineOrientation(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }
}
