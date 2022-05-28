package atomicJ.gui.rois.region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HolesRegion implements Region
{
    private final List<Region> holes = new ArrayList<>();

    public HolesRegion(Region hole)
    {
        this.holes.add(hole);
    }

    public HolesRegion(Region ... holes)
    {
        this.holes.addAll(Arrays.asList(holes));
    }

    public HolesRegion(List<Region> holes)
    {
        this.holes.addAll(holes);
    }

    @Override
    public HolesRegion rotate(double angle, double anchorX, double anchorY)
    {        
        List<Region> rotatedHoles = new ArrayList<>();

        for(Region hole : holes)
        {
            rotatedHoles.add(hole.rotate(angle, anchorX, anchorY));
        }

        HolesRegion rotated = new HolesRegion(rotatedHoles);       

        return rotated;
    }

    @Override
    public boolean contains(double x, double y)
    {
        return !isInHole(x, y);
    }

    private boolean isInHole(double x, double y)
    {
        boolean inHole = false;

        for(Region r : holes)
        {
            inHole = r.contains(x, y);
            if(inHole)
            {
                break;
            }
        }

        return inHole;
    }

    @Override
    public SerializableRegionInformation getSerializableRegionInformation() 
    {
        List<SerializableRegionInformation> serializableHoles = new ArrayList<>();
        for(Region hole : holes)
        {
            serializableHoles.add(hole.getSerializableRegionInformation());
        }

        return new SerializableHolesRegionInformation(serializableHoles);
    }

    private static class SerializableHolesRegionInformation implements SerializableRegionInformation
    {
        private final List<SerializableRegionInformation> serializableHoles;

        private SerializableHolesRegionInformation(List<SerializableRegionInformation> serializableHoles)
        {
            this.serializableHoles = new ArrayList<>(serializableHoles);
        }

        @Override
        public Region getRegion()
        {
            List<Region> holesRegions = new ArrayList<>();

            for(SerializableRegionInformation sri : serializableHoles)
            {
                holesRegions.add(sri.getRegion());
            }

            return new HolesRegion(holesRegions);
        }      
    }
}
