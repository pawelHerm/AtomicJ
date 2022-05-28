package atomicJ.gui;

public class XYLineAndShapeRendererDataMutable<E extends XYLineAndShapeRendererData> extends AbstractXYRendererDataMutable<E> implements XYLineAndShapeRendererData
{
    private boolean baseLinesVisible;
    private boolean baseShapesVisible;
    private boolean baseShapesFilled;
    private boolean drawOutlines;
    private boolean useFillPaint;
    private boolean useOutlinePaint;
    private boolean drawSeriesLineAsPath;

    public XYLineAndShapeRendererDataMutable()
    {
        this.baseLinesVisible = true;
        this.baseShapesVisible = true;
        this.useFillPaint = false;     // use item paint for fills by default
        this.baseShapesFilled = true;
        this.drawOutlines = true;
        this.useOutlinePaint = false;  // use item paint for outlines by default, not outline paint
        this.drawSeriesLineAsPath = false;
    }

    public XYLineAndShapeRendererDataMutable(XYLineAndShapeRendererData data)
    {
        super(data);

        this.baseLinesVisible = data.getBaseLinesVisible();
        this.baseShapesVisible = data.getBaseShapesVisible();
        this.useFillPaint = data.getUseFillPaint();   
        this.baseShapesFilled = data.getBaseShapesFilled();
        this.drawOutlines = data.getDrawOutlines();
        this.useOutlinePaint = data.getUseOutlinePaint();  
        this.drawSeriesLineAsPath = data.getDrawSeriesLineAsPath();
    }

    @Override
    public void copyState(E data, boolean notify)
    {
        super.copyState(data, notify);

        this.baseLinesVisible = data.getBaseLinesVisible();
        this.baseShapesVisible = data.getBaseShapesVisible();
        this.useFillPaint = data.getUseFillPaint();   
        this.baseShapesFilled = data.getBaseShapesFilled();
        this.drawOutlines = data.getDrawOutlines();
        this.useOutlinePaint = data.getUseOutlinePaint();  
        this.drawSeriesLineAsPath = data.getDrawSeriesLineAsPath();

        if(notify)
        {
            notifyRendererOfDataChange(false);
        }
    }

    @Override
    public XYLineAndShapeRendererDataMutable<E> getMutableCopy()
    {
        XYLineAndShapeRendererDataMutable<E> mutableCopy = new XYLineAndShapeRendererDataMutable<>(this);
        return mutableCopy;
    }

    @Override
    public XYLineAndShapeRendererDataMutable<E> getMutableVersion()
    {
        return this;
    }

    @Override
    public XYLineAndShapeRendererDataImmutable getImmutableVersion()
    {
        XYLineAndShapeRendererDataImmutable immutableCopy = new XYLineAndShapeRendererDataImmutable(this);
        return immutableCopy;
    }


    @Override
    public boolean getDrawSeriesLineAsPath()
    {
        return this.drawSeriesLineAsPath;
    }

    public void setDrawSeriesLineAsPath(boolean flag) {
        if (this.drawSeriesLineAsPath != flag) {
            this.drawSeriesLineAsPath = flag;
            notifyRendererOfDataChange();
        }
    }

    @Override
    public boolean getBaseLinesVisible()
    {
        return this.baseLinesVisible;
    }

    public void setBaseLinesVisible(boolean flag) {
        this.baseLinesVisible = flag;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getBaseShapesVisible()
    {
        return this.baseShapesVisible;
    }

    public void setBaseShapesVisible(boolean flag) {
        this.baseShapesVisible = flag;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getBaseShapesFilled()
    {
        return this.baseShapesFilled;
    }

    public void setBaseShapesFilled(boolean flag) {
        this.baseShapesFilled = flag;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getDrawOutlines()
    {
        return this.drawOutlines;
    }

    public void setDrawOutlines(boolean flag) {
        this.drawOutlines = flag;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getUseFillPaint()
    {
        return this.useFillPaint;
    }

    public void setUseFillPaint(boolean flag) {
        this.useFillPaint = flag;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getUseOutlinePaint()
    {
        return this.useOutlinePaint;
    }

    public void setUseOutlinePaint(boolean flag) {
        this.useOutlinePaint = flag;
        notifyRendererOfDataChange();
    }
}
