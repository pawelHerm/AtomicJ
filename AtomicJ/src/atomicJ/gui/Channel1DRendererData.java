package atomicJ.gui;

public interface Channel1DRendererData extends XYLineAndShapeRendererData
{
    public int getBaseMarkerIndex();

    public float getBaseMarkerSize();

    @Override
    public Channel1DRendererDataMutable<?> getMutableCopy();

    //copies only if necessary, i.e. the instance is immutable
    @Override
    public Channel1DRendererDataMutable<?> getMutableVersion();

    //copies only if necessary, i.e. the instance is mutable
    @Override
    public Channel1DRendererDataImmutable getImmutableVersion();
}
