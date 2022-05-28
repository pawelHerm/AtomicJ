package atomicJ.gui;

public interface MarkerRenderer 
{
    public boolean getIncludeInDataBounds();

    //margin that must be left for annotations beneath the point of lowest vale / above the point of highest value
    // for annotations; this value is in pixels (i.e. in Java 2D space, not the data space)
    public int getMarkerMarginHeight();

    public int getMarkerMarginWidth();
}
