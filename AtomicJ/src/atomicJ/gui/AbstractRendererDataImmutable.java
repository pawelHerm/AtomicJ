package atomicJ.gui;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------------
 * AbstractRenderer.java
 * ---------------------
 * (C) Copyright 2002-2011, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Nicolas Brodu;
 *
 * Changes:
 * --------
 * 22-Aug-2002 : Version 1, draws code out of AbstractXYItemRenderer to share
 *               with AbstractCategoryItemRenderer (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-Nov-2002 : Moved to the com.jrefinery.chart.renderer package (DG);
 * 21-Nov-2002 : Added a paint table for the renderer to use (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 29-Apr-2003 : Added valueLabelFont and valueLabelPaint attributes, based on
 *               code from Arnaud Lelievre (DG);
 * 29-Jul-2003 : Amended code that doesn't compile with JDK 1.2.2 (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 15-Sep-2003 : Fixed serialization (NB);
 * 17-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Moved PlotRenderingInfo into RendererState to allow for
 *               multiple threads using a single renderer (DG);
 * 20-Oct-2003 : Added missing setOutlinePaint() method (DG);
 * 23-Oct-2003 : Split item label attributes into 'positive' and 'negative'
 *               values (DG);
 * 26-Nov-2003 : Added methods to get the positive and negative item label
 *               positions (DG);
 * 01-Mar-2004 : Modified readObject() method to prevent null pointer exceptions
 *               after deserialization (DG);
 * 19-Jul-2004 : Fixed bug in getItemLabelFont(int, int) method (DG);
 * 04-Oct-2004 : Updated equals() method, eliminated use of NumberUtils,
 *               renamed BooleanUtils --> BooleanUtilities, ShapeUtils -->
 *               ShapeUtilities (DG);
 * 15-Mar-2005 : Fixed serialization of baseFillPaint (DG);
 * 16-May-2005 : Base outline stroke should never be null (DG);
 * 01-Jun-2005 : Added hasListener() method for unit testing (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Minor API doc update (DG);
 * 19-Feb-2007 : Fixes for clone() method (DG);
 * 28-Feb-2007 : Use cached event to signal changes (DG);
 * 19-Apr-2007 : Deprecated seriesVisible and seriesVisibleInLegend flags (DG);
 * 20-Apr-2007 : Deprecated paint, fillPaint, outlinePaint, stroke,
 *               outlineStroke, shape, itemLabelsVisible, itemLabelFont,
 *               itemLabelPaint, positiveItemLabelPosition,
 *               negativeItemLabelPosition and createEntities override
 *               fields (DG);
 * 13-Jun-2007 : Added new autoPopulate flags for core series attributes (DG);
 * 23-Oct-2007 : Updated lookup methods to better handle overridden
 *               methods (DG);
 * 04-Dec-2007 : Modified hashCode() implementation (DG);
 * 29-Apr-2008 : Minor API doc update (DG);
 * 17-Jun-2008 : Added legendShape, legendTextFont and legendTextPaint
 *               attributes (DG);
 * 18-Aug-2008 : Added clearSeriesPaints() and clearSeriesStrokes() (DG);
 * 28-Jan-2009 : Equals method doesn't test Shape equality correctly (DG);
 * 27-Mar-2009 : Added dataBoundsIncludesVisibleSeriesOnly attribute, and
 *               updated renderer events for series visibility changes (DG);
 * 01-Apr-2009 : Factored up the defaultEntityRadius field from the
 *               AbstractXYItemRenderer class (DG);
 * 28-Apr-2009 : Added flag to allow a renderer to treat the legend shape as
 *               a line (DG);
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Objects;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ShapeUtilities;

public abstract class AbstractRendererDataImmutable implements AbstractRendererData, Cloneable {

    private final boolean baseSeriesVisible;
    private final boolean baseSeriesVisibleInLegend;
    private transient Paint basePaint;
    private final transient Paint baseFillPaint;
    private final transient Paint baseOutlinePaint;
    private final transient Stroke baseStroke;
    private final transient Stroke baseOutlineStroke;
    private transient Shape baseShape;
    private final boolean baseItemLabelsVisible;
    private final Font baseItemLabelFont;
    private final transient Paint baseItemLabelPaint;
    private final ItemLabelPosition basePositiveItemLabelPosition;
    private final ItemLabelPosition baseNegativeItemLabelPosition;
    private final double itemLabelAnchorOffset = 2.0;
    private final boolean baseCreateEntities;
    private final transient Shape baseLegendShape;
    private final boolean treatLegendShapeAsLine;
    private final Font baseLegendTextFont;
    private final transient Paint baseLegendTextPaint;
    private final boolean dataBoundsIncludesVisibleSeriesOnly = true;
    private final int defaultEntityRadius;

    public AbstractRendererDataImmutable() {
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
    }

    public AbstractRendererDataImmutable(AbstractRendererData data) {
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
    }

    @Override
    public void registerRendererIfNecessary(AbstractRendererLightweight<?> renderer){}

    @Override
    public void deregisterRendererIfNecessary(AbstractRendererLightweight<?> renderer){}

    @Override
    public boolean isImmutable()
    {
        return true;
    }

    @Override
    public boolean getBaseSeriesVisible() {
        return this.baseSeriesVisible;
    }

    @Override
    public boolean getBaseSeriesVisibleInLegend() {
        return this.baseSeriesVisibleInLegend;
    }

    @Override
    public Paint getBasePaint() {
        return this.basePaint;
    }

    @Override
    public Paint getBaseFillPaint() {
        return this.baseFillPaint;
    }

    @Override
    public Paint getBaseOutlinePaint() {
        return this.baseOutlinePaint;
    }

    @Override
    public Stroke getBaseStroke() {
        return this.baseStroke;
    }

    @Override
    public Stroke getBaseOutlineStroke() {
        return this.baseOutlineStroke;
    }

    @Override
    public Shape getBaseShape() {
        return this.baseShape;
    }

    @Override
    public boolean getBaseItemLabelsVisible() {
        return this.baseItemLabelsVisible;
    }

    @Override
    public Font getBaseItemLabelFont() {
        return this.baseItemLabelFont;
    }

    @Override
    public Paint getBaseItemLabelPaint() {
        return this.baseItemLabelPaint;
    }

    @Override
    public ItemLabelPosition getBasePositiveItemLabelPosition() {
        return this.basePositiveItemLabelPosition;
    }

    @Override
    public ItemLabelPosition getBaseNegativeItemLabelPosition() {
        return this.baseNegativeItemLabelPosition;
    }

    @Override
    public double getItemLabelAnchorOffset() 
    {
        return this.itemLabelAnchorOffset;
    }

    @Override
    public boolean getBaseCreateEntities() 
    {
        return this.baseCreateEntities;
    }

    @Override
    public int getDefaultEntityRadius() 
    {
        return this.defaultEntityRadius;
    }

    @Override
    public Shape getBaseLegendShape() 
    {
        return this.baseLegendShape;
    }

    @Override
    public boolean getTreatLegendShapeAsLine() 
    {
        return this.treatLegendShapeAsLine;
    }

    @Override
    public Font getBaseLegendTextFont() 
    {
        return this.baseLegendTextFont;
    }


    @Override
    public Paint getBaseLegendTextPaint() 
    {
        return this.baseLegendTextPaint;
    }

    @Override
    public boolean getDataBoundsIncludesVisibleSeriesOnly() 
    {
        return this.dataBoundsIncludesVisibleSeriesOnly;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractRendererDataImmutable)) {
            return false;
        }
        AbstractRendererDataImmutable that = (AbstractRendererDataImmutable) obj;
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
        AbstractRendererDataImmutable clone = (AbstractRendererDataImmutable) super.clone();

        if (this.baseShape != null) {
            clone.baseShape = ShapeUtilities.clone(this.baseShape);
        }
        return clone;
    }
}

