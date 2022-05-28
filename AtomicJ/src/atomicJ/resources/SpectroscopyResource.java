package atomicJ.resources;

import java.util.Collection;

import atomicJ.sources.SimpleSpectroscopySource;

public interface SpectroscopyResource extends Channel1DResource<SimpleSpectroscopySource>
{
    public boolean isCorrespondingMapPositionKnown();

    static boolean containsResourcesFromMap(Collection<? extends SpectroscopyResource> resources)
    {
        for(SpectroscopyResource resource : resources)
        {
            if(resource.isCorrespondingMapPositionKnown())
            {
                return true;
            }
        }

        return false;
    }
}
