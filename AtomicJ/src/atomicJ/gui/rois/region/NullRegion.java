package atomicJ.gui.rois.region;

public class NullRegion implements Region
{
    @Override
    public NullRegion rotate(double angle, double anchorX, double anchorY)
    {      
        return new NullRegion();
    }

    @Override
    public boolean contains(double x, double y) {
        return false;
    }

    @Override
    public SerializableRegionInformation getSerializableRegionInformation()
    {
        return new SerializableNullRegionInformation();
    }

    private static class SerializableNullRegionInformation implements SerializableRegionInformation
    {              
        @Override
        public Region getRegion()
        {                 
            return new NullRegion();
        }      
    }
}
