package atomicJ.gui.editors;

import java.awt.Paint;

public class ChartStyleDataImmutable implements ChartStyleData
{
    private final boolean antialias;
    private final boolean lockAspectRatio;
    private final Paint backgroundPaint;
    private final boolean useGradientPaint;
    private final double paddingTop;
    private final double paddingBottom;
    private final double paddingLeft;
    private final double paddingRight;

    public ChartStyleDataImmutable(ChartStyleData data)
    {
        this.antialias = data.isAntialias();
        this.lockAspectRatio = data.isLockAspectRatio();
        this.backgroundPaint = data.getBackgroundPaint();
        this.useGradientPaint = data.isUseGradientPaint();
        this.paddingTop = data.getPaddingTop();
        this.paddingBottom = data.getPaddingBottom();
        this.paddingLeft = data.getPaddingLeft();
        this.paddingRight = data.getPaddingRight();
    }

    @Override
    public ChartStyleDataMutable getMutableCopy()
    {
        ChartStyleDataMutable mutableCopy = new ChartStyleDataMutable(this);
        return mutableCopy;
    }

    @Override
    public ChartStyleDataMutable getMutableVersion()
    {
        ChartStyleDataMutable mutableCopy = new ChartStyleDataMutable(this);
        return mutableCopy;
    }

    @Override
    public ChartStyleDataImmutable getImmutableVersion()
    {
        return this;
    }

    @Override
    public boolean isAntialias()
    {
        return antialias;
    }

    @Override
    public boolean isLockAspectRatio()
    {
        return lockAspectRatio;
    }        

    @Override
    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }       

    @Override
    public boolean isUseGradientPaint()
    {
        return useGradientPaint;
    }

    @Override
    public double getPaddingTop()
    {
        return paddingTop;
    }         

    @Override
    public double getPaddingBottom()
    {
        return paddingBottom;
    }     

    @Override
    public double getPaddingLeft()
    {
        return paddingLeft;
    }

    @Override
    public double getPaddingRight()
    {
        return paddingRight;
    }
}