package atomicJ.gui;

public class XYLineAndShapeRendererDataImmutable extends AbstractXYRendererDataImmutable implements XYLineAndShapeRendererData
{
    private final boolean baseLinesVisible;
    private final boolean baseShapesVisible;
    private final boolean baseShapesFilled;
    private final boolean drawOutlines;
    private final boolean useFillPaint;
    private final boolean useOutlinePaint;
    private final boolean drawSeriesLineAsPath;

    public XYLineAndShapeRendererDataImmutable()
    {
        this.baseLinesVisible = true;
        this.baseShapesVisible = true;
        this.useFillPaint = false;     // use item paint for fills by default
        this.baseShapesFilled = true;
        this.drawOutlines = true;
        this.useOutlinePaint = false;  // use item paint for outlines by default, not outline paint
        this.drawSeriesLineAsPath = false;
    }

    public XYLineAndShapeRendererDataImmutable(XYLineAndShapeRendererData data)
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
    public XYLineAndShapeRendererDataMutable<? extends XYLineAndShapeRendererData> getMutableCopy()
    {
        XYLineAndShapeRendererDataMutable<XYLineAndShapeRendererData> mutableCopy = new XYLineAndShapeRendererDataMutable<>(this);
        return mutableCopy;
    }

    @Override
    public XYLineAndShapeRendererDataMutable<? extends XYLineAndShapeRendererData> getMutableVersion()
    {
        XYLineAndShapeRendererDataMutable<XYLineAndShapeRendererData> mutableCopy = new XYLineAndShapeRendererDataMutable<>(this);
        return mutableCopy;
    }


    @Override
    public XYLineAndShapeRendererDataImmutable getImmutableVersion()
    {
        return this;
    }

    @Override
    public boolean getDrawSeriesLineAsPath()
    {
        return this.drawSeriesLineAsPath;
    }

    @Override
    public boolean getBaseLinesVisible()
    {
        return this.baseLinesVisible;
    }

    @Override
    public boolean getBaseShapesVisible()
    {
        return this.baseShapesVisible;
    }

    @Override
    public boolean getBaseShapesFilled()
    {
        return this.baseShapesFilled;
    }

    @Override
    public boolean getDrawOutlines()
    {
        return this.drawOutlines;
    }

    @Override
    public boolean getUseFillPaint()
    {
        return this.useFillPaint;
    }

    @Override
    public boolean getUseOutlinePaint()
    {
        return this.useOutlinePaint;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode = 31*hashCode + Boolean.hashCode(baseLinesVisible);
        hashCode = 31*hashCode + Boolean.hashCode(baseShapesVisible);
        hashCode = 31*hashCode + Boolean.hashCode(baseShapesFilled);
        hashCode = 31*hashCode + Boolean.hashCode(drawOutlines);
        hashCode = 31*hashCode + Boolean.hashCode(useFillPaint);
        hashCode = 31*hashCode + Boolean.hashCode(useOutlinePaint);
        hashCode = 31*hashCode + Boolean.hashCode(drawSeriesLineAsPath);

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof XYLineAndShapeRendererDataImmutable)
        {
            XYLineAndShapeRendererDataImmutable that = (XYLineAndShapeRendererDataImmutable)other;

            if(this.baseLinesVisible != that.baseLinesVisible)
            {
                return false;
            };
            if(this.baseShapesVisible != that.baseShapesVisible)
            {
                return false;
            };
            if(this.baseShapesFilled != that.baseShapesFilled)
            {
                return false;
            };
            if(this.drawOutlines != that.drawOutlines)
            {
                return false;
            };
            if(this.useFillPaint != that.useFillPaint)
            {
                return false;
            };
            if(this.useOutlinePaint != that.useOutlinePaint)
            {
                return false;
            };
            if(this.drawSeriesLineAsPath != that.drawSeriesLineAsPath)
            {
                return false;
            };

            return true;
        }
        return false;
    }
}
