package atomicJ.gui;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import javax.swing.event.EventListenerList;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelPosition;
import atomicJ.utilities.Validation;

public abstract class AbstractRendererLightweight <E extends AbstractRendererData> implements Cloneable
{
    private E rendererData;
    private EventListenerList listenerList;

    //renderer data must be immutable
    public AbstractRendererLightweight(E rendererData) 
    {
        if(!rendererData.isImmutable())
        {
            throw new IllegalArgumentException("Renderer data must be immutable");
        }

        this.rendererData = rendererData;
        this.listenerList = new EventListenerList();
    }

    protected E getData()
    {
        return rendererData;
    }

    public abstract void setData(E data);

    protected abstract AbstractRendererDataMutable<?> getDataForModification();

    protected void replaceData(E rendererDataNew)
    {
        this.rendererData.deregisterRendererIfNecessary(this);
        this.rendererData = rendererDataNew;
        rendererDataNew.registerRendererIfNecessary(this);

        notifyListeners(new RendererChangeEvent(this, true));
    }

    public boolean getBaseSeriesVisible() {
        return rendererData.getBaseSeriesVisible();
    }

    public void setBaseSeriesVisible(boolean visible)
    {
        getDataForModification().setBaseSeriesVisible(visible);
    }

    public void setBaseSeriesVisible(boolean visible, boolean notify)
    {
        getDataForModification().setBaseSeriesVisible(visible, notify);
    }

    public boolean getBaseSeriesVisibleInLegend() {
        return rendererData.getBaseSeriesVisibleInLegend();
    }

    public void setBaseSeriesVisibleInLegend(boolean visible)
    {
        setBaseSeriesVisibleInLegend(visible, true);
    }

    public void setBaseSeriesVisibleInLegend(boolean visible, boolean notify)
    {
        getDataForModification().setBaseSeriesVisibleInLegend(visible, notify);
    }

    // PAINT

    public Paint getBasePaint() {
        return rendererData.getBasePaint();
    }

    public void setBasePaint(Paint paint)
    {
        getDataForModification().setBasePaint(paint);
    }

    //// FILL PAINT //////////////////////////////////////////////////////////


    public Paint getBaseFillPaint() {
        return rendererData.getBaseFillPaint();
    }

    public void setBaseFillPaint(Paint paint)
    {
        getDataForModification().setBaseFillPaint(paint);
    }

    // OUTLINE PAINT //////////////////////////////////////////////////////////

    public Paint getBaseOutlinePaint() {
        return rendererData.getBaseOutlinePaint();
    }

    public void setBaseOutlinePaint(Paint paint)
    {
        getDataForModification().setBaseOutlinePaint(paint);
    }

    // STROKE

    public Stroke getBaseStroke() {
        return rendererData.getBaseStroke();
    }

    public void setBaseStroke(Stroke stroke)
    {
        getDataForModification().setBaseStroke(stroke);
    }


    // OUTLINE STROKE

    public Stroke getBaseOutlineStroke() {
        return rendererData.getBaseOutlineStroke();
    }

    public void setBaseOutlineStroke(Stroke stroke)
    {
        setBaseOutlineStroke(stroke, true);
    }
    public void setBaseOutlineStroke(Stroke stroke, boolean notify)
    {
        getDataForModification().setBaseOutlineStroke(stroke, notify);
    }

    // SHAPE

    public Shape getBaseShape() {
        return rendererData.getBaseShape();
    }

    public void setBaseShape(Shape shape)
    {
        setBaseShape(shape, true);
    }

    public void setBaseShape(Shape shape, boolean notify)
    {
        getDataForModification().setBaseShape(shape, notify);
    }

    // ITEM LABEL VISIBILITY...

    public boolean getBaseItemLabelsVisible() {      
        return rendererData.getBaseItemLabelsVisible();
    }

    public void setBaseItemLabelsVisible(boolean visible)
    {
        getDataForModification().setBaseItemLabelsVisible(visible);
    }

    //// ITEM LABEL FONT //////////////////////////////////////////////////////

    public Font getBaseItemLabelFont() {
        return rendererData.getBaseItemLabelFont();
    }

    public void setBaseItemLabelFont(Font font)
    {
        getDataForModification().setBaseItemLabelFont(font);
    }

    //// ITEM LABEL PAINT  ////////////////////////////////////////////////////

    public Paint getBaseItemLabelPaint() {
        return rendererData.getBaseItemLabelPaint();
    }

    public void setBaseItemLabelPaint(Paint paint)
    {
        getDataForModification().setBaseItemLabelPaint(paint);
    }

    // POSITIVE ITEM LABEL POSITION...

    public ItemLabelPosition getBasePositiveItemLabelPosition() {
        return rendererData.getBasePositiveItemLabelPosition();
    }

    public void setBasePositiveItemLabelPosition(ItemLabelPosition position)
    {
        setBasePositiveItemLabelPosition(position, true);
    }
    public void setBasePositiveItemLabelPosition(ItemLabelPosition position, boolean notify)
    {
        getDataForModification().setBasePositiveItemLabelPosition(position, notify);
    }

    // NEGATIVE ITEM LABEL POSITION...

    public ItemLabelPosition getBaseNegativeItemLabelPosition() {
        return rendererData.getBaseNegativeItemLabelPosition();
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position)
    {
        setBaseNegativeItemLabelPosition(position, true);
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position, boolean notify)
    {
        getDataForModification().setBaseNegativeItemLabelPosition(position, notify);
    }


    public double getItemLabelAnchorOffset() {
        return rendererData.getItemLabelAnchorOffset();
    }

    public boolean getBaseCreateEntities() {
        return rendererData.getBaseCreateEntities();
    }

    public int getDefaultEntityRadius() {
        return rendererData.getDefaultEntityRadius();
    }

    public Shape lookupLegendShape() {
        Shape result = rendererData.getBaseLegendShape();

        if (result == null) {
            result = rendererData.getBaseShape();
        }

        return result;
    }

    public Shape getBaseLegendShape() {
        return rendererData.getBaseLegendShape();
    }

    protected boolean getTreatLegendShapeAsLine() {
        return rendererData.getTreatLegendShapeAsLine();
    }

    public Font getBaseLegendTextFont() {
        return rendererData.getBaseLegendTextFont();
    }

    public Paint getBaseLegendTextPaint() {
        return rendererData.getBaseLegendTextPaint();
    }

    public boolean getDataBoundsIncludesVisibleSeriesOnly() {
        return rendererData.getDataBoundsIncludesVisibleSeriesOnly();
    }

    public void dataChanged()
    {
        notifyListeners(new RendererChangeEvent(this));
    }

    public void dataChanged(boolean seriesVisibilityChanged)
    {
        notifyListeners(new RendererChangeEvent(this, seriesVisibilityChanged));
    }

    protected void fireChangeEvent() {

        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Registers an object to receive notification of changes to the renderer.
     *
     * @param listener  the listener (<code>null</code> not permitted).
     *
     * @see #removeChangeListener(RendererChangeListener)
     */
    public void addChangeListener(RendererChangeListener listener) {
        Validation.requireNonNullParameterName(listener, "listener");
        this.listenerList.add(RendererChangeListener.class, listener);
    }

    /**
     * Deregisters an object so that it no longer receives
     * notification of changes to the renderer.
     *
     * @param listener  the object (<code>null</code> not permitted).
     *
     * @see #addChangeListener(RendererChangeListener)
     */
    public void removeChangeListener(RendererChangeListener listener) 
    {
        Validation.requireNonNullParameterName(listener, "listener");
        this.listenerList.remove(RendererChangeListener.class, listener);
    }

    /**
     * Returns <code>true</code> if the specified object is registered with
     * the dataset as a listener.  Most applications won't need to call this
     * method, it exists mainly for use by unit testing code.
     *
     * @param listener  the listener.
     *
     * @return A boolean.
     */
    public boolean hasListener(EventListener listener) {
        List<Object> list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }

    public void notifyListeners(RendererChangeEvent event) {
        Object[] ls = this.listenerList.getListenerList();
        for (int i = ls.length - 2; i >= 0; i -= 2) {
            if (ls[i] == RendererChangeListener.class) {
                ((RendererChangeListener) ls[i + 1]).rendererChanged(event);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractRendererLightweight)) {
            return false;
        }

        AbstractRendererLightweight<?> that = (AbstractRendererLightweight<?>)obj;
        if (!Objects.equals(this.rendererData, that.rendererData)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode(result, this.rendererData);

        return result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        AbstractRendererLightweight<?> clone = (AbstractRendererLightweight<?>) super.clone();


        clone.listenerList = new EventListenerList();
        return clone;
    }
}

