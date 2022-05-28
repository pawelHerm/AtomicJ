package atomicJ.gui.editors;

import java.awt.Paint;

public interface ChartStyleData
{             
    public ChartStyleDataImmutable getImmutableVersion();
    public ChartStyleDataMutable getMutableVersion();
    public ChartStyleDataMutable getMutableCopy();

    public boolean isAntialias();
    public boolean isLockAspectRatio();
    public Paint getBackgroundPaint();
    public boolean isUseGradientPaint();       
    public double getPaddingTop();
    public double getPaddingBottom();
    public double getPaddingLeft();
    public double getPaddingRight();
}