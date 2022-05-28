package atomicJ.gui.rois.region;

public interface Region
{
    public boolean contains(double x, double y);
    public Region rotate(double angle, double anchorX, double anchorY);
    public SerializableRegionInformation getSerializableRegionInformation();
}
