package atomicJ.gui;

import java.awt.Paint;
import java.awt.Stroke;

public class Channel1DErrorRendererDataMutable extends Channel1DRendererDataMutable<Channel1DErrorRendererData> implements Channel1DErrorRendererData
{
    private Paint barPaint;
    private Stroke barStroke;
    private boolean capVisible;
    private double capWidth;
    private Paint capPaint;
    private Stroke capStroke;
    private ErrorBarDirection drawingDirection;

    public Channel1DErrorRendererDataMutable() 
    {}

    public Channel1DErrorRendererDataMutable(Channel1DErrorRendererData data)
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
    public void copyState(Channel1DErrorRendererData data, boolean notify)
    {
        super.copyState(data, false);

        this.barPaint = data.getBarPaint();
        this.barStroke = data.getBarStroke();
        this.capVisible = data.isCapVisible();
        this.capWidth = data.getCapWidth();
        this.capPaint = data.getCapPaint();
        this.capStroke = data.getCapStroke();
        this.drawingDirection = data.getErrorBarDrawingDirection();

        if(notify)
        {
            notifyRendererOfDataChange(false);
        }
    }

    @Override
    public Paint getBarPaint()
    {
        return barPaint;
    }

    public void setBarPaint(Paint barPaintNew)
    {
        this.barPaint = barPaintNew;
        notifyRendererOfDataChange();
    }


    @Override
    public Stroke getBarStroke()
    {
        return barStroke;
    }

    public void setBarStroke(Stroke barStrokeNew)
    {
        this.barStroke = barStrokeNew;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean isCapVisible()
    {
        return capVisible;
    }

    public void setCapVisible(boolean capVisibleNew)
    {
        this.capVisible = capVisibleNew;
        notifyRendererOfDataChange();
    }

    @Override
    public double getCapWidth()
    {
        return capWidth;
    }

    public void setCapWidth(double capWidthNew)
    {
        this.capWidth = capWidthNew;
        notifyRendererOfDataChange();
    }

    @Override
    public Paint getCapPaint()
    {
        return capPaint;
    }

    public void setCapPaint(Paint capPaintNew)
    {
        this.capPaint = capPaintNew;
        notifyRendererOfDataChange();
    }


    @Override
    public Stroke getCapStroke()
    {
        return capStroke;
    }

    public void setCapStroke(Stroke capStrokeNew)
    {
        this.capStroke = capStrokeNew;
        notifyRendererOfDataChange();
    }

    @Override
    public ErrorBarDirection getErrorBarDrawingDirection()
    {
        return drawingDirection;
    }

    public void setErrorBarDrawingDirection(ErrorBarDirection drawingDirectionNew)
    {
        this.drawingDirection = drawingDirectionNew;
        notifyRendererOfDataChange();
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
        return this;
    }

    @Override
    public Channel1DErrorRendererDataImmutable getImmutableVersion()
    {
        Channel1DErrorRendererDataImmutable immutableCopy = new Channel1DErrorRendererDataImmutable(this);
        return immutableCopy;
    }


    public void readPreferredStyle(PreferredContinuousSeriesErrorRendererStyle style, boolean notify)
    {
        setBaseSeriesVisible(style.isVisible(), false);
        setBaseSeriesVisibleInLegend(style.isVisibleInLegend(), false);
        setBaseFillPaint(style.getFillPaint(), false);     
        setBasePaint(style.getStrokePaint(), false);
        setBaseStroke(style.getStroke(), false);
        setBaseLinesVisible(style.isJoined());
        setBaseShapesVisible(style.isMarkerVisible());      

        setBaseMarkerIndex(style.getMarkerIndex());
        setBaseMarkerSize(style.getMarkerSize());

        this.barPaint = style.getBarPaint();
        this.barStroke = style.getBarStroke();
        this.capVisible = style.isCapVisible();
        this.capWidth = style.getCapWidth();
        this.capPaint = style.getCapPaint();
        this.capStroke = style.getCapStroke();
        this.drawingDirection = style.getErrorBarDrawingDirection();

        if(notify)
        {
            notifyRendererOfDataChange();
        }
    }
}
