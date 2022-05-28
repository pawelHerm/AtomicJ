package atomicJ.gui;

import java.awt.Paint;
import java.awt.Stroke;

public class Channel1DErrorRendererDataImmutable extends Channel1DRendererDataImmutable implements Channel1DErrorRendererData
{
    private final Paint barPaint;
    private final Stroke barStroke;
    private final boolean capVisible;
    private final double capWidth;
    private final Paint capPaint;
    private final Stroke capStroke;
    private final ErrorBarDirection drawingDirection;

    public Channel1DErrorRendererDataImmutable(Channel1DErrorRendererData data)
    {
        super(data);

        this.barPaint = data.getBarPaint();
        this.barStroke = data.getBarStroke();
        this.capVisible = data.isCapVisible();
        this.capWidth = data.getCapWidth();
        this.capPaint = data.getCapPaint();
        this.capStroke = data.getCapStroke();
        this.drawingDirection = data.getErrorBarDrawingDirection();
    }

    @Override
    public Channel1DErrorRendererDataMutable getMutableCopy()
    {
        Channel1DErrorRendererDataMutable mutableCopy = new Channel1DErrorRendererDataMutable(this);
        return mutableCopy;
    }

    @Override
    public Channel1DErrorRendererDataMutable getMutableVersion()
    {
        Channel1DErrorRendererDataMutable mutableCopy = new Channel1DErrorRendererDataMutable(this);
        return mutableCopy;
    }

    @Override
    public Channel1DErrorRendererDataImmutable getImmutableVersion()
    {
        return this;
    }

    @Override
    public Paint getBarPaint()
    {
        return barPaint;
    }

    @Override
    public Stroke getBarStroke()
    {
        return barStroke;
    }

    @Override
    public boolean isCapVisible()
    {
        return capVisible;
    }

    @Override
    public double getCapWidth()
    {
        return capWidth;
    }

    @Override
    public Paint getCapPaint()
    {
        return capPaint;
    }

    @Override
    public Stroke getCapStroke()
    {
        return capStroke;
    }

    @Override
    public ErrorBarDirection getErrorBarDrawingDirection() 
    {
        return drawingDirection;
    }
}
