package atomicJ.gui;

public class Channel1DRendererDataImmutable extends XYLineAndShapeRendererDataImmutable implements Channel1DRendererData
{
    private final int markerIndex;
    private final float markerSize;

    public Channel1DRendererDataImmutable(Channel1DRendererData data)
    {
        super(data);

        this.markerIndex = data.getBaseMarkerIndex();      
        this.markerSize = data.getBaseMarkerSize();
    }

    @Override
    public Channel1DRendererDataMutable<? extends Channel1DRendererData> getMutableCopy()
    {
        Channel1DRendererDataMutable<Channel1DRendererData> mutableCopy = new Channel1DRendererDataMutable<>(this);
        return mutableCopy;
    }

    @Override
    public Channel1DRendererDataMutable<? extends Channel1DRendererData> getMutableVersion()
    {
        Channel1DRendererDataMutable<Channel1DRendererData> mutableCopy = new Channel1DRendererDataMutable<>(this);
        return mutableCopy;
    }

    @Override
    public Channel1DRendererDataImmutable getImmutableVersion()
    {
        return this;
    }

    @Override
    public int getBaseMarkerIndex() 
    {
        return this.markerIndex;
    }

    @Override
    public float getBaseMarkerSize() 
    {
        return this.markerSize;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode = 31*hashCode + Integer.hashCode(markerIndex);
        hashCode = 31*hashCode + Float.hashCode(markerSize);

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof Channel1DRendererDataImmutable)
        {
            Channel1DRendererDataImmutable that = (Channel1DRendererDataImmutable)other;

            if(this.markerIndex != that.markerIndex)
            {
                return false;
            };
            if(this.markerSize != that.markerSize)
            {
                return false;
            };


            return true;
        }
        return false;
    }
}
