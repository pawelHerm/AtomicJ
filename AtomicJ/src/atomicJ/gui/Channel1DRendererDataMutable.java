package atomicJ.gui;

public class Channel1DRendererDataMutable<E extends Channel1DRendererData> extends XYLineAndShapeRendererDataMutable<E> implements Channel1DRendererData
{
    private int markerIndex;
    private float markerSize;

    public Channel1DRendererDataMutable() 
    {}

    public Channel1DRendererDataMutable(Channel1DRendererData data)
    {
        super(data);

        this.markerIndex = data.getBaseMarkerIndex();      
        this.markerSize = data.getBaseMarkerSize();
    }

    @Override
    public void copyState(E data, boolean notify)
    {
        super.copyState(data, notify);

        this.markerIndex = data.getBaseMarkerIndex();      
        this.markerSize = data.getBaseMarkerSize();

        setBaseShape(ShapeSupplier.createShape(markerIndex,markerSize), notify);
    }

    @Override
    public Channel1DRendererDataMutable<E> getMutableCopy()
    {
        Channel1DRendererDataMutable<E> mutableCopy = new Channel1DRendererDataMutable<>(this);
        return mutableCopy;
    }

    @Override
    public Channel1DRendererDataMutable<E> getMutableVersion()
    {
        return this;
    }

    @Override
    public Channel1DRendererDataImmutable getImmutableVersion()
    {
        Channel1DRendererDataImmutable immutableCopy = new Channel1DRendererDataImmutable(this);
        return immutableCopy;
    }

    @Override
    public int getBaseMarkerIndex() 
    {
        return this.markerIndex;
    }

    public void setBaseMarkerIndex(int i)
    {
        this.markerIndex = i;
        setBaseShape(ShapeSupplier.createShape(markerIndex,markerSize));
    }   

    @Override
    public float getBaseMarkerSize() 
    {
        return this.markerSize;
    }

    public void setBaseMarkerSize(float size)
    {
        this.markerSize = size;
        setBaseShape(ShapeSupplier.createShape(markerIndex, markerSize));
    }

    public void readPreferredStyle(PreferredContinuousSeriesRendererStyle style, boolean notify)
    {
        setBaseSeriesVisible(style.isVisible(), false);
        setBaseSeriesVisibleInLegend(style.isVisibleInLegend(), false);
        setBaseFillPaint(style.getFillPaint(), false);     
        setBasePaint(style.getStrokePaint(), false);
        setBaseStroke(style.getStroke(), false);
        setBaseLinesVisible(style.isJoined());
        setBaseShapesVisible(style.isMarkerVisible());      

        this.markerIndex = style.getMarkerIndex();
        this.markerSize = style.getMarkerSize();

        setBaseShape(ShapeSupplier.createShape(markerIndex,markerSize), notify);
    }

    public void readPreferredStyle(PreferredDiscreteSeriesRendererStyle style, boolean notify) {

        setBaseSeriesVisible(style.isVisible(), false);
        setBaseSeriesVisibleInLegend(style.isVisibleInLegend(), false);
        setBasePaint(style.getPaint(), false);  //discrete datasets i

        this.markerIndex = style.getMarkerIndex();
        this.markerSize = style.getMarkerSize();

        setBaseShape(ShapeSupplier.createShape(markerIndex,markerSize), notify);
    }
}
