package atomicJ.gui.rois.region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeRegion implements Region
{
    private final List<Region> islands = new ArrayList<>();

    public CompositeRegion(Region island)
    {
        this.islands.add(island);
    }

    public CompositeRegion(Region ... island)
    {
        this.islands.addAll(Arrays.asList(island));
    }

    public CompositeRegion(List<Region> island)
    {
        this.islands.addAll(island);
    }

    @Override
    public CompositeRegion rotate(double angle, double anchorX, double anchorY)
    {        
        List<Region> rotatedIslands = new ArrayList<>();

        for(Region island : islands)
        {
            rotatedIslands.add(island.rotate(angle, anchorX, anchorY));
        }

        CompositeRegion rotated = new CompositeRegion(rotatedIslands);       

        return rotated;
    }

    @Override
    public boolean contains(double x, double y)
    {
        return isOnIsland(x, y);
    }

    private boolean isOnIsland(double x, double y)
    {
        boolean onIsland = false;

        for(Region r : islands)
        {
            onIsland = r.contains(x, y);
            if(onIsland)
            {
                break;
            }
        }

        return onIsland;
    }

    @Override
    public SerializableRegionInformation getSerializableRegionInformation() 
    {
        List<SerializableRegionInformation> serializableIslands = new ArrayList<>();
        for(Region island : islands)
        {
            serializableIslands.add(island.getSerializableRegionInformation());
        }

        return new SerializableCompositeRegionInformation(serializableIslands);
    }

    private static class SerializableCompositeRegionInformation implements SerializableRegionInformation
    {
        private final List<SerializableRegionInformation> serializableIslands;

        private SerializableCompositeRegionInformation(List<SerializableRegionInformation> serializableIslands)
        {
            this.serializableIslands = new ArrayList<>(serializableIslands);
        }

        @Override
        public Region getRegion()
        {
            List<Region> islandRegions = new ArrayList<>();

            for(SerializableRegionInformation sri : serializableIslands)
            {
                islandRegions.add(sri.getRegion());
            }

            return new CompositeRegion(islandRegions);
        }      
    }
}
