package atomicJ.gui.editors;

import java.awt.Paint;

import atomicJ.gui.GradientPaint;
import atomicJ.gui.PreferredChartStyle;

public class ChartStyleDataMutable implements ChartStyleData
{
    private boolean antialias;
    private boolean lockAspectRatio;
    private Paint backgroundPaint;
    private boolean useGradientPaint;
    private double paddingTop;
    private double paddingBottom;
    private double paddingLeft;
    private double paddingRight;

    public ChartStyleDataMutable()
    {}

    public ChartStyleDataMutable(ChartStyleData styleData)
    {
        this.antialias = styleData.isAntialias();
        this.lockAspectRatio = styleData.isLockAspectRatio();
        this.backgroundPaint = styleData.getBackgroundPaint();
        this.useGradientPaint = styleData.isUseGradientPaint();
        this.paddingTop = styleData.getPaddingTop();
        this.paddingBottom = styleData.getPaddingBottom();
        this.paddingLeft = styleData.getPaddingLeft();
        this.paddingRight = styleData.getPaddingRight();
    }

    public void copyState(ChartStyleData styleData)
    {
        this.antialias = styleData.isAntialias();
        this.lockAspectRatio = styleData.isLockAspectRatio();
        this.backgroundPaint = styleData.getBackgroundPaint();
        this.useGradientPaint = styleData.isUseGradientPaint();
        this.paddingTop = styleData.getPaddingTop();
        this.paddingBottom = styleData.getPaddingBottom();
        this.paddingLeft = styleData.getPaddingLeft();
        this.paddingRight = styleData.getPaddingRight();
    }

    @Override
    public ChartStyleDataMutable getMutableVersion()
    {
        return this;
    }

    @Override
    public ChartStyleDataMutable getMutableCopy()
    {
        ChartStyleDataMutable mutableCopy = new ChartStyleDataMutable(this);
        return mutableCopy;
    }

    @Override
    public ChartStyleDataImmutable getImmutableVersion()
    {
        ChartStyleDataImmutable immutableVersion = new ChartStyleDataImmutable(this);
        return immutableVersion;
    }

    @Override
    public boolean isAntialias()
    {
        return antialias;
    }

    public void setAntialias(boolean antialias)
    {
        this.antialias = antialias;
    }

    @Override
    public boolean isLockAspectRatio()
    {
        return lockAspectRatio;
    }

    public void setLockAspectRatio(boolean lockAspectRatio)
    {
        this.lockAspectRatio = lockAspectRatio;
    }

    @Override
    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    public void setBackgroundPaint(Paint backgroundPaint)
    {
        this.backgroundPaint = backgroundPaint;
    }

    @Override
    public boolean isUseGradientPaint()
    {
        return useGradientPaint;
    }

    public void setUseGradientPaint(boolean useGradientPaint)
    {
        this.useGradientPaint = useGradientPaint;
    }

    @Override
    public double getPaddingTop()
    {
        return paddingTop;
    }     

    public void setPaddingTop(double paddingTop)
    {
        this.paddingTop = paddingTop;
    }     


    @Override
    public double getPaddingBottom()
    {
        return paddingBottom;
    }     

    public void setPaddingBottom(double paddingBottom)
    {
        this.paddingBottom = paddingBottom;
    }    


    @Override
    public double getPaddingLeft()
    {
        return paddingLeft;
    }

    public void setPaddingLeft(double paddingLeft)
    {
        this.paddingLeft = paddingLeft;
    }

    @Override
    public double getPaddingRight()
    {
        return paddingRight;
    }

    public void setPaddingRight(double paddingRight)
    {
        this.paddingRight = paddingRight;
    }

    public void readPreferredStyle(PreferredChartStyle pref)
    {
        this.backgroundPaint = pref.getBackgroundPaint();
        this.lockAspectRatio = pref.isLockAspectRatio();
        this.useGradientPaint = backgroundPaint instanceof GradientPaint;
        this.antialias = pref.isAntialias();
        this.paddingTop = pref.getPaddingTop();
        this.paddingBottom = pref.getPaddingBottom();
        this.paddingLeft = pref.getPaddingLeft();
        this.paddingRight = pref.getPaddingRight();
    }
}