package atomicJ.gui;



import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Objects;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ShapeUtilities;

import atomicJ.utilities.Validation;


public abstract class AbstractRendererDataMutable <E extends AbstractRendererData> implements AbstractRendererData, Cloneable {

    private boolean baseSeriesVisible;
    private boolean baseSeriesVisibleInLegend;
    private Paint basePaint;
    private Paint baseFillPaint;
    private Paint baseOutlinePaint;
    private Stroke baseStroke;
    private Stroke baseOutlineStroke;
    private Shape baseShape;
    private boolean baseItemLabelsVisible;
    private Font baseItemLabelFont;
    private Paint baseItemLabelPaint;
    private ItemLabelPosition basePositiveItemLabelPosition;
    private ItemLabelPosition baseNegativeItemLabelPosition;
    private double itemLabelAnchorOffset = 2.0;
    private boolean baseCreateEntities;
    private Shape baseLegendShape;
    private boolean treatLegendShapeAsLine;
    private Font baseLegendTextFont;
    private Paint baseLegendTextPaint;
    private boolean dataBoundsIncludesVisibleSeriesOnly = true;
    private int defaultEntityRadius;
    private AbstractRendererLightweight<? extends AbstractRendererData> renderer;

    public AbstractRendererDataMutable() {
        this.baseSeriesVisible = true;
        this.baseSeriesVisibleInLegend = true;
        this.basePaint = DEFAULT_PAINT;
        this.baseFillPaint = Color.white;
        this.baseOutlinePaint = DEFAULT_OUTLINE_PAINT;
        this.baseStroke = DEFAULT_STROKE;
        this.baseOutlineStroke = DEFAULT_OUTLINE_STROKE;
        this.baseShape = DEFAULT_SHAPE;
        this.baseItemLabelsVisible = false;
        this.baseItemLabelFont = DEFAULT_BASE_ITEM_LABEL_FONT;
        this.baseItemLabelPaint = Color.black;
        this.basePositiveItemLabelPosition = DEFAULT_BASE_POSITIVE_ITEM_LABEL_POSITION;
        this.baseNegativeItemLabelPosition = DEFAULT_BASE_NEGATIVE_ITEM_LABEL_POSITION;
        this.baseCreateEntities = true;
        this.defaultEntityRadius = 3;
        this.baseLegendShape = null;
        this.treatLegendShapeAsLine = false;
        this.baseLegendTextFont = null;
        this.baseLegendTextPaint = null;
        this.renderer = null;
    }

    public AbstractRendererDataMutable(AbstractRendererData data) {
        this.baseSeriesVisible = data.getBaseSeriesVisible();
        this.baseSeriesVisibleInLegend = data.getBaseSeriesVisibleInLegend();
        this.basePaint = data.getBasePaint();
        this.baseFillPaint = data.getBaseFillPaint();
        this.baseOutlinePaint = data.getBaseOutlinePaint();
        this.baseStroke = data.getBaseStroke();
        this.baseOutlineStroke = data.getBaseOutlineStroke();
        this.baseShape = data.getBaseShape();
        this.baseItemLabelsVisible = data.getBaseItemLabelsVisible();
        this.baseItemLabelFont = data.getBaseItemLabelFont();
        this.baseItemLabelPaint = data.getBaseItemLabelPaint();
        this.basePositiveItemLabelPosition = data.getBasePositiveItemLabelPosition();
        this.baseNegativeItemLabelPosition = data.getBaseNegativeItemLabelPosition();
        this.baseCreateEntities = data.getBaseCreateEntities();
        this.defaultEntityRadius = data.getDefaultEntityRadius();
        this.baseLegendShape = data.getBaseLegendShape();
        this.treatLegendShapeAsLine = data.getTreatLegendShapeAsLine();
        this.baseLegendTextFont = data.getBaseLegendTextFont();
        this.baseLegendTextPaint = data.getBaseLegendTextPaint();
        this.renderer = null;
    }

    public void copyState(E data, boolean notify)
    {
        boolean seriesVisibilityChanged = this.baseSeriesVisible != data.getBaseSeriesVisible();
        this.baseSeriesVisible = data.getBaseSeriesVisible();
        this.baseSeriesVisibleInLegend = data.getBaseSeriesVisibleInLegend();
        this.basePaint = data.getBasePaint();
        this.baseFillPaint = data.getBaseFillPaint();
        this.baseOutlinePaint = data.getBaseOutlinePaint();
        this.baseStroke = data.getBaseStroke();
        this.baseOutlineStroke = data.getBaseOutlineStroke();
        this.baseShape = data.getBaseShape();
        this.baseItemLabelsVisible = data.getBaseItemLabelsVisible();
        this.baseItemLabelFont = data.getBaseItemLabelFont();
        this.baseItemLabelPaint = data.getBaseItemLabelPaint();
        this.basePositiveItemLabelPosition = data.getBasePositiveItemLabelPosition();
        this.baseNegativeItemLabelPosition = data.getBaseNegativeItemLabelPosition();
        this.baseCreateEntities = data.getBaseCreateEntities();
        this.defaultEntityRadius = data.getDefaultEntityRadius();
        this.baseLegendShape = data.getBaseLegendShape();
        this.treatLegendShapeAsLine = data.getTreatLegendShapeAsLine();
        this.baseLegendTextFont = data.getBaseLegendTextFont();
        this.baseLegendTextPaint = data.getBaseLegendTextPaint();

        if(notify)
        {
            notifyRendererOfDataChange(seriesVisibilityChanged);
        }
    }

    @Override
    public boolean isImmutable()
    {
        return false;
    }

    @Override
    public boolean getBaseSeriesVisible() {
        return this.baseSeriesVisible;
    }

    public void setBaseSeriesVisible(boolean visible) {
        setBaseSeriesVisible(visible, true);
    }

    public void setBaseSeriesVisible(boolean visible, boolean notify) 
    {
        this.baseSeriesVisible = visible;
        if (notify) {           
            notifyRendererOfDataChange(true);
        }
    }

    @Override
    public boolean getBaseSeriesVisibleInLegend() {
        return this.baseSeriesVisibleInLegend;
    }

    public void setBaseSeriesVisibleInLegend(boolean visible) {
        setBaseSeriesVisibleInLegend(visible, true);
    }

    public void setBaseSeriesVisibleInLegend(boolean visible, boolean notify) {
        this.baseSeriesVisibleInLegend = visible;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBasePaint() {
        return this.basePaint;
    }

    public void setBasePaint(Paint paint) {
        // defer argument checking...
        setBasePaint(paint, true);
    }

    public void setBasePaint(Paint paint, boolean notify) {
        this.basePaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBaseFillPaint() {
        return this.baseFillPaint;
    }

    public void setBaseFillPaint(Paint paint) {
        // defer argument checking...
        setBaseFillPaint(paint, true);
    }

    public void setBaseFillPaint(Paint paint, boolean notify) {

        Validation.requireNonNullParameterName(paint, "paint");
        this.baseFillPaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBaseOutlinePaint() {
        return this.baseOutlinePaint;
    }

    public void setBaseOutlinePaint(Paint paint) {
        // defer argument checking...
        setBaseOutlinePaint(paint, true);
    }

    public void setBaseOutlinePaint(Paint paint, boolean notify) 
    {
        Validation.requireNonNullParameterName(paint, "paint");
        this.baseOutlinePaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Stroke getBaseStroke() {
        return this.baseStroke;
    }

    public void setBaseStroke(Stroke stroke) {
        // defer argument checking...
        setBaseStroke(stroke, true);
    }

    public void setBaseStroke(Stroke stroke, boolean notify)
    {
        Validation.requireNonNullParameterName(stroke, "stroke");

        this.baseStroke = stroke;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Stroke getBaseOutlineStroke() {
        return this.baseOutlineStroke;
    }

    public void setBaseOutlineStroke(Stroke stroke) {
        setBaseOutlineStroke(stroke, true);
    }

    public void setBaseOutlineStroke(Stroke stroke, boolean notify)
    {
        Validation.requireNonNullParameterName(stroke, "stroke");

        this.baseOutlineStroke = stroke;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }


    @Override
    public Shape getBaseShape() {
        return this.baseShape;
    }

    public void setBaseShape(Shape shape) {
        // defer argument checking...
        setBaseShape(shape, true);
    }

    public void setBaseShape(Shape shape, boolean notify) 
    {
        Validation.requireNonNullParameterName(shape, "shape");

        this.baseShape = shape;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public boolean getBaseItemLabelsVisible() {
        return this.baseItemLabelsVisible;
    }

    public void setBaseItemLabelsVisible(boolean visible) {
        setBaseItemLabelsVisible(BooleanUtilities.valueOf(visible));
    }

    public void setBaseItemLabelsVisible(Boolean visible, boolean notify) {
        this.baseItemLabelsVisible = visible;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Font getBaseItemLabelFont() {
        return this.baseItemLabelFont;
    }

    public void setBaseItemLabelFont(Font font) 
    {
        Validation.requireNonNullParameterName(font, "font");

        setBaseItemLabelFont(font, true);
    }

    public void setBaseItemLabelFont(Font font, boolean notify) {
        this.baseItemLabelFont = font;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBaseItemLabelPaint() {
        return this.baseItemLabelPaint;
    }

    public void setBaseItemLabelPaint(Paint paint) {
        // defer argument checking...
        setBaseItemLabelPaint(paint, true);
    }

    public void setBaseItemLabelPaint(Paint paint, boolean notify) 
    {
        Validation.requireNonNullParameterName(paint, "paint");

        this.baseItemLabelPaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }


    @Override
    public ItemLabelPosition getBasePositiveItemLabelPosition() {
        return this.basePositiveItemLabelPosition;
    }

    public void setBasePositiveItemLabelPosition(ItemLabelPosition position) {
        // defer argument checking...
        setBasePositiveItemLabelPosition(position, true);
    }

    public void setBasePositiveItemLabelPosition(ItemLabelPosition position,
            boolean notify) {

        Validation.requireNonNullParameterName(position, "position");

        this.basePositiveItemLabelPosition = position;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public ItemLabelPosition getBaseNegativeItemLabelPosition() {
        return this.baseNegativeItemLabelPosition;
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position) {
        setBaseNegativeItemLabelPosition(position, true);
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position, boolean notify) 
    {
        Validation.requireNonNullParameterName(position, "position");

        this.baseNegativeItemLabelPosition = position;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public double getItemLabelAnchorOffset() {
        return this.itemLabelAnchorOffset;
    }

    public void setItemLabelAnchorOffset(double offset) {
        this.itemLabelAnchorOffset = offset;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getBaseCreateEntities() {
        return this.baseCreateEntities;
    }

    public void setBaseCreateEntities(boolean create) {
        setBaseCreateEntities(create, true);
    }

    public void setBaseCreateEntities(boolean create, boolean notify) {
        this.baseCreateEntities = create;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public int getDefaultEntityRadius() {
        return this.defaultEntityRadius;
    }

    public void setDefaultEntityRadius(int radius) {
        this.defaultEntityRadius = radius;
    }

    @Override
    public Shape getBaseLegendShape() {
        return this.baseLegendShape;
    }

    public void setBaseLegendShape(Shape shape) {
        this.baseLegendShape = shape;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getTreatLegendShapeAsLine() {
        return this.treatLegendShapeAsLine;
    }

    public void setTreatLegendShapeAsLine(boolean treatAsLine) {
        if (this.treatLegendShapeAsLine != treatAsLine) {
            this.treatLegendShapeAsLine = treatAsLine;
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Font getBaseLegendTextFont() {
        return this.baseLegendTextFont;
    }

    public void setBaseLegendTextFont(Font font) {
        this.baseLegendTextFont = font;
        notifyRendererOfDataChange();
    }

    @Override
    public Paint getBaseLegendTextPaint() {
        return this.baseLegendTextPaint;
    }

    public void setBaseLegendTextPaint(Paint paint) {
        this.baseLegendTextPaint = paint;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getDataBoundsIncludesVisibleSeriesOnly() {
        return this.dataBoundsIncludesVisibleSeriesOnly;
    }

    public void setDataBoundsIncludesVisibleSeriesOnly(boolean visibleOnly) {
        this.dataBoundsIncludesVisibleSeriesOnly = visibleOnly;
        notifyRendererOfDataChange(true);
    }


    @Override
    public void registerRendererIfNecessary(AbstractRendererLightweight<?> renderer){
        this.renderer = renderer;
    }


    @Override
    public void deregisterRendererIfNecessary(AbstractRendererLightweight<?> renderer)
    {
        if(this.renderer == renderer)
        {
            this.renderer = null;
        }
    }

    protected void notifyRendererOfDataChange() 
    {
        if(this.renderer != null)
        {
            this.renderer.dataChanged();
        }
    }

    protected void notifyRendererOfDataChange(boolean seriesVisibilityChanged) 
    {
        if(this.renderer != null)
        {
            this.renderer.dataChanged(seriesVisibilityChanged);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractRendererDataMutable)) {
            return false;
        }
        AbstractRendererDataMutable that = (AbstractRendererDataMutable) obj;
        if (this.dataBoundsIncludesVisibleSeriesOnly
                != that.dataBoundsIncludesVisibleSeriesOnly) {
            return false;
        }
        if (this.treatLegendShapeAsLine != that.treatLegendShapeAsLine) {
            return false;
        }
        if (this.defaultEntityRadius != that.defaultEntityRadius) {
            return false;
        }


        if (this.baseSeriesVisible != that.baseSeriesVisible) {
            return false;
        }

        if (this.baseSeriesVisibleInLegend != that.baseSeriesVisibleInLegend) {
            return false;
        }

        if (!PaintUtilities.equal(this.basePaint, that.basePaint)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseFillPaint, that.baseFillPaint)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseOutlinePaint,
                that.baseOutlinePaint)) {
            return false;
        }

        if (!Objects.equals(this.baseStroke, that.baseStroke)) {
            return false;
        }

        if (!Objects.equals(
                this.baseOutlineStroke, that.baseOutlineStroke)
                ) {
            return false;
        }

        if (!ShapeUtilities.equal(this.baseShape, that.baseShape)) {
            return false;
        }


        if (this.baseItemLabelsVisible !=
                that.baseItemLabelsVisible) {
            return false;
        }

        if (!Objects.equals(this.baseItemLabelFont,
                that.baseItemLabelFont)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseItemLabelPaint,
                that.baseItemLabelPaint)) {
            return false;
        }

        if (!Objects.equals(this.basePositiveItemLabelPosition,
                that.basePositiveItemLabelPosition)) {
            return false;
        }

        if (!Objects.equals(this.baseNegativeItemLabelPosition,
                that.baseNegativeItemLabelPosition)) {
            return false;
        }
        if (this.itemLabelAnchorOffset != that.itemLabelAnchorOffset) {
            return false;
        }


        if (this.baseCreateEntities != that.baseCreateEntities) {
            return false;
        }

        if (!ShapeUtilities.equal(this.baseLegendShape,
                that.baseLegendShape)) {
            return false;
        }

        if (!Objects.equals(this.baseLegendTextFont,
                that.baseLegendTextFont)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseLegendTextPaint,
                that.baseLegendTextPaint)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hashcode for the renderer.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode(result, this.baseSeriesVisible);
        result = HashUtilities.hashCode(result, this.baseSeriesVisibleInLegend);
        result = HashUtilities.hashCode(result, this.basePaint);
        result = HashUtilities.hashCode(result, this.baseFillPaint);
        result = HashUtilities.hashCode(result, this.baseOutlinePaint);
        result = HashUtilities.hashCode(result, this.baseStroke);
        result = HashUtilities.hashCode(result, this.baseOutlineStroke);

        return result;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        AbstractRendererDataMutable clone = (AbstractRendererDataMutable) super.clone();

        if (this.baseShape != null) {
            clone.baseShape = ShapeUtilities.clone(this.baseShape);
        }

        clone.renderer = null;
        return clone;
    }
}

