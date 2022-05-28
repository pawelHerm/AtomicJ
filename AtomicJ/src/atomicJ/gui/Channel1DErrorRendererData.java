package atomicJ.gui;

import java.awt.Paint;
import java.awt.Stroke;

public interface Channel1DErrorRendererData extends Channel1DRendererData
{    
    public Paint getBarPaint();
    public Stroke getBarStroke();

    public boolean isCapVisible();
    public double getCapWidth();

    public Paint getCapPaint();
    public Stroke getCapStroke();

    public ErrorBarDirection getErrorBarDrawingDirection();

    @Override
    public Channel1DErrorRendererDataMutable getMutableCopy();

    //copies only if necessary, i.e. the instance is immutable
    @Override
    public Channel1DErrorRendererDataMutable getMutableVersion();

    //copies only if necessary, i.e. the instance is mutable
    @Override
    public Channel1DErrorRendererDataImmutable getImmutableVersion();
}
