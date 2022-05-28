package atomicJ.sources;

public class UniversalKeyTagger implements ROITagger<Object>
{
    private final Channel2DSource<?> source;

    public UniversalKeyTagger(Channel2DSource<?> source)
    {
        this.source = source;
    }

    @Override
    public String getCoordinateSampleTag()
    {
        return source.getShortName();
    }

    @Override
    public String getQuantitativeSampleTag(IdentityTag roiTag)
    {
        return source.getUniversalROIKey(roiTag);
    }

    @Override
    public String getTag(IdentityTag roiTag) 
    {
        return source.getUniversalROIKey(roiTag);
    }
}