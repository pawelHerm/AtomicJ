package atomicJ.gui.rois.region;


public class WholePlaneRegion implements Region
{
    @Override
    public WholePlaneRegion rotate(double angle, double anchorX, double anchorY)
    {      
        return new WholePlaneRegion();
    }

    @Override
    public boolean contains(double x, double y) {
        return true;
    }

    @Override
    public SerializableRegionInformation getSerializableRegionInformation()
    {
        return new SerializableWholePlaneRegionInformation();
    }

    private static class SerializableWholePlaneRegionInformation implements SerializableRegionInformation
    {              
        @Override
        public Region getRegion()
        {                 
            return new WholePlaneRegion();
        }      
    }
}
