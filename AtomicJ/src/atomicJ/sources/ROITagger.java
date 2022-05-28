package atomicJ.sources;

public interface ROITagger<R>
{
    public R getTag(IdentityTag roiTag);
    public String getCoordinateSampleTag();
    public String getQuantitativeSampleTag(IdentityTag roiTag);
}