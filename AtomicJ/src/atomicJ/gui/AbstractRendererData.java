package atomicJ.gui;

import java.awt.BasicStroke;
import java.awt.Color;

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

import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.TextAnchor;

public interface AbstractRendererData
{
    public static final Paint DEFAULT_PAINT = Color.blue;
    public static final Paint DEFAULT_OUTLINE_PAINT = Color.gray;
    public static final Stroke DEFAULT_STROKE = new BasicStroke(1.0f);
    public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0f);
    public static final Shape DEFAULT_SHAPE = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);
    public static final Font DEFAULT_BASE_ITEM_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
    public static final ItemLabelPosition DEFAULT_BASE_POSITIVE_ITEM_LABEL_POSITION = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
    public static final ItemLabelPosition DEFAULT_BASE_NEGATIVE_ITEM_LABEL_POSITION = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);

    static final double ADJ = Math.cos(Math.PI / 6.0);
    static final double OPP = Math.sin(Math.PI / 6.0);

    public boolean getBaseSeriesVisible();
    public boolean getBaseSeriesVisibleInLegend();
    public Paint getBasePaint();
    public Paint getBaseFillPaint();
    public Paint getBaseOutlinePaint();
    public Stroke getBaseStroke();
    public Stroke getBaseOutlineStroke();
    public Shape getBaseShape();
    public boolean getBaseItemLabelsVisible();
    public Font getBaseItemLabelFont();
    public Paint getBaseItemLabelPaint();
    public ItemLabelPosition getBasePositiveItemLabelPosition();
    public ItemLabelPosition getBaseNegativeItemLabelPosition();
    public double getItemLabelAnchorOffset();
    public boolean getBaseCreateEntities();    
    public int getDefaultEntityRadius();
    public Shape getBaseLegendShape();
    public boolean getTreatLegendShapeAsLine();
    public Font getBaseLegendTextFont();
    public Paint getBaseLegendTextPaint(); 
    public boolean getDataBoundsIncludesVisibleSeriesOnly();
    public boolean isImmutable();
    public void registerRendererIfNecessary(AbstractRendererLightweight<?> renderer);
    public void deregisterRendererIfNecessary(AbstractRendererLightweight<?> renderer);
    public AbstractRendererDataMutable<?> getMutableCopy();
    //copies only if necessary, i.e. the instance is immutable
    public AbstractRendererDataMutable<?> getMutableVersion();
    //copy if the current instance is mutable 
    public AbstractRendererDataImmutable getImmutableVersion();

    /**
     code from AbstractRenderer class from JFReeChart 1.0.14, by D. Gilbert
     */
    public static Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, double x, double y, double itemLabelAnchorOffset, PlotOrientation orientation) {
        Point2D result = null;
        if (anchor == ItemLabelAnchor.CENTER) {
            result = new Point2D.Double(x, y);
        }
        else if (anchor == ItemLabelAnchor.INSIDE1) {
            result = new Point2D.Double(x + OPP * itemLabelAnchorOffset,
                    y - ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE2) {
            result = new Point2D.Double(x + ADJ * itemLabelAnchorOffset,
                    y - OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE3) {
            result = new Point2D.Double(x + itemLabelAnchorOffset, y);
        }
        else if (anchor == ItemLabelAnchor.INSIDE4) {
            result = new Point2D.Double(x + ADJ * itemLabelAnchorOffset,
                    y + OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE5) {
            result = new Point2D.Double(x + OPP * itemLabelAnchorOffset,
                    y + ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE6) {
            result = new Point2D.Double(x, y + itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE7) {
            result = new Point2D.Double(x - OPP * itemLabelAnchorOffset,
                    y + ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE8) {
            result = new Point2D.Double(x - ADJ * itemLabelAnchorOffset,
                    y + OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE9) {
            result = new Point2D.Double(x - itemLabelAnchorOffset, y);
        }
        else if (anchor == ItemLabelAnchor.INSIDE10) {
            result = new Point2D.Double(x - ADJ * itemLabelAnchorOffset,
                    y - OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE11) {
            result = new Point2D.Double(x - OPP * itemLabelAnchorOffset,
                    y - ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.INSIDE12) {
            result = new Point2D.Double(x, y - itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE1) {
            result = new Point2D.Double(
                    x + 2.0 * OPP * itemLabelAnchorOffset,
                    y - 2.0 * ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE2) {
            result = new Point2D.Double(
                    x + 2.0 * ADJ * itemLabelAnchorOffset,
                    y - 2.0 * OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE3) {
            result = new Point2D.Double(x + 2.0 * itemLabelAnchorOffset,
                    y);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE4) {
            result = new Point2D.Double(
                    x + 2.0 * ADJ * itemLabelAnchorOffset,
                    y + 2.0 * OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE5) {
            result = new Point2D.Double(
                    x + 2.0 * OPP * itemLabelAnchorOffset,
                    y + 2.0 * ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE6) {
            result = new Point2D.Double(x,
                    y + 2.0 * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE7) {
            result = new Point2D.Double(
                    x - 2.0 * OPP * itemLabelAnchorOffset,
                    y + 2.0 * ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE8) {
            result = new Point2D.Double(
                    x - 2.0 * ADJ * itemLabelAnchorOffset,
                    y + 2.0 * OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE9) {
            result = new Point2D.Double(x - 2.0 * itemLabelAnchorOffset,
                    y);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE10) {
            result = new Point2D.Double(
                    x - 2.0 * ADJ * itemLabelAnchorOffset,
                    y - 2.0 * OPP * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE11) {
            result = new Point2D.Double(
                    x - 2.0 * OPP * itemLabelAnchorOffset,
                    y - 2.0 * ADJ * itemLabelAnchorOffset);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE12) {
            result = new Point2D.Double(x,
                    y - 2.0 * itemLabelAnchorOffset);
        }
        return result;
    }
}

