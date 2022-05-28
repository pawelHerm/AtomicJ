
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.data.Range;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.StringUtilities;
import atomicJ.utilities.Validation;

public abstract class CustomizableNumberBaseAxis extends NumberAxis implements PreferencesSource 
{
    private static final long serialVersionUID = 1L;

    private double lowerRangePadding;
    private double upperRangePadding;
    private double labelOuterSpace;
    private double labelInnerSpace;

    private AxisLocation location;
    private final Preferences pref;

    public CustomizableNumberBaseAxis(String label, Preferences pref)
    {
        this(label, PreferredAxisStyle.getInstance(pref));
    }

    public CustomizableNumberBaseAxis(String label, PreferredAxisStyle style)
    {
        this(label, style, NumberAxis.createStandardTickUnits(), true, true);
    }

    public CustomizableNumberBaseAxis(String label, PreferredAxisStyle style, TickUnitSource tickUnitSource,boolean autoRangeIncludesZero, boolean autoRangeStickyZero)
    {
        super(label, tickUnitSource, autoRangeIncludesZero, autoRangeStickyZero);

        this.pref = style.getPreferences();
        setPreferredStyle(style);
    }

    public CustomizableNumberBaseAxis(CustomizableNumberBaseAxis that)
    {
        super(that.getLabel());

        this.pref = that.pref;

        this.lowerRangePadding = that.lowerRangePadding;
        this.upperRangePadding = that.upperRangePadding;
        this.labelOuterSpace = that.labelOuterSpace;
        this.labelInnerSpace = that.labelInnerSpace;
    }

    private void setPreferredStyle(PreferredAxisStyle style)
    {       
        Paint labelPaint = style.getLabelPaint();
        Font labelFont = style.getLabelFont();

        Paint tickLabelPaint = style.getTickLabelPaint();
        Font tickLabelFont = style.getTickLabelFont();

        Paint axisLinePaint = style.getAxisLinePaint();
        Stroke axisLineStroke = style.getAxisLineStroke();

        Paint tickMarkPaint = style.getTickMarkPaint();
        Stroke tickMarkStroke = style.getTickMarkStroke();

        location = style.getLocation();

        float tickMarkLengthInside = style.getTickMarkLengthInside();
        float tickMarkLengthOutside = style.getTickMarkLengthOutside();

        labelOuterSpace = style.getLabelOuterSpace();
        labelInnerSpace = style.getLabelInnerSpace();
        boolean axisLineVisible = style.getAxisLineVisible();
        boolean tickLabelsVisible = style.getTickLabelsVisible();
        boolean tickMarksVisible = style.getTickMarksVisible();
        boolean verticalTickLabel = style.getVerticalTickLabel();

        setLabelPaint(labelPaint);  
        setLabelFont(labelFont);    

        setTickLabelFont(tickLabelFont);    
        setTickLabelPaint(tickLabelPaint);
        setTickLabelsVisible(tickLabelsVisible);
        setTickMarksVisible(tickMarksVisible);  
        setVerticalTickLabels(verticalTickLabel);
        setTickMarkPaint(tickMarkPaint);
        setTickMarkStroke(tickMarkStroke);
        setTickMarkInsideLength(tickMarkLengthInside);
        setTickMarkOutsideLength(tickMarkLengthOutside);

        setLabelOuterSpace(labelOuterSpace);
        setLabelInnerSpace(labelInnerSpace);
        setAxisLineVisible(axisLineVisible);
        setAxisLinePaint(axisLinePaint);
        setAxisLineStroke(axisLineStroke);
    }


    protected abstract String getName();


    public AxisLocation getPreferredAxisLocation()
    {
        return location;
    }

    public void setPreferredAxisLocation(AxisLocation location)
    {
        this.location = location;
        notifyListeners(new AxisChangeEvent(this));
    }

    public double getLowerRangePadding()
    {
        return lowerRangePadding;
    }

    public void setLowerRangePadding(double paddingNew)
    {
        this.lowerRangePadding = paddingNew;
        notifyListeners(new AxisChangeEvent(this));
    }

    public double getUpperRangePadding()
    {
        return upperRangePadding;
    }

    public void setUpperRangePadding(double paddingNew)
    {
        this.upperRangePadding = paddingNew;
        notifyListeners(new AxisChangeEvent(this));
    }

    public double getUpperTickBound()
    {
        double bound =  getUpperBound() - upperRangePadding;
        return bound;
    }

    public double getLowerTickBound()
    {
        double bound =  getLowerBound() + lowerRangePadding;
        return bound;
    }

    @Override
    protected double calculateLowestVisibleTickValue() 
    {	
        double unit = getTickUnit().getSize();
        double index = Math.ceil(getLowerTickBound() / unit);
        return index * unit;	  
    }

    @Override
    protected double calculateHighestVisibleTickValue() 
    {		
        double unit = getTickUnit().getSize();
        double index = Math.floor(getUpperTickBound() / unit);
        return index * unit;		
    }

    @Override
    protected int calculateVisibleTickCount() 
    {		
        double unit = getTickUnit().getSize();
        return (int) (Math.floor(getUpperTickBound() / unit) - Math.ceil(getLowerTickBound() / unit) + 1);	 
    }

    public double getLabelOuterSpace()
    {
        return labelOuterSpace;
    }

    public void setLabelOuterSpace(double space)
    {
        this.labelOuterSpace = space;
        notifyListeners(new AxisChangeEvent(this));
    }

    public double getLabelInnerSpace()
    {
        return labelInnerSpace;
    }

    public void setLabelInnerSpace(double space)
    {
        this.labelInnerSpace = space;
        notifyListeners(new AxisChangeEvent(this));
    }

    @Override
    protected Rectangle2D getLabelEnclosure(Graphics2D g2, RectangleEdge edge) {

        Rectangle2D result = new Rectangle2D.Double();
        String axisLabel = getLabel();
        if (axisLabel != null && !axisLabel.equals("")) {
            FontMetrics fm = g2.getFontMetrics(getLabelFont());
            Rectangle2D bounds = TextUtilities.getTextBounds(axisLabel, g2, fm);
            RectangleInsets insets = getLabelInsets(edge);
            bounds = insets.createOutsetRectangle(bounds);
            double angle = getLabelAngle();
            if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) 
            {
                angle = angle - Math.PI / 2.0;
            }
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            AffineTransform transformer 
            = AffineTransform.getRotateInstance(angle, x, y);
            Shape labelBounds = transformer.createTransformedShape(bounds);
            result = labelBounds.getBounds2D();
        }

        return result;
    }


    /*
     * this method is called by drawLabel() and getLabelEnclosure() and it seems that the methods use the returned RectangleInsets
     *object in different ways: getLabelEnclosure() takes into account the rotation of the label (not the same as label Angle!)
     *while drawLabel() does not
     */

    public RectangleInsets getLabelInsets(RectangleEdge edge)
    {        
        RectangleInsets insetsOld = getLabelInsets();
        RectangleInsets insetsNew = null;

        if(RectangleEdge.LEFT.equals(edge))
        {
            insetsNew = new RectangleInsets(labelOuterSpace, insetsOld.getLeft(),labelInnerSpace, labelInnerSpace);
        }
        else if(RectangleEdge.BOTTOM.equals(edge))
        {
            insetsNew = new RectangleInsets(labelInnerSpace, insetsOld.getLeft(), labelOuterSpace, insetsOld.getRight());
        }
        else if(RectangleEdge.RIGHT.equals(edge))
        {
            insetsNew = new RectangleInsets(labelInnerSpace, labelInnerSpace, labelOuterSpace, labelOuterSpace);
        }
        else if(RectangleEdge.TOP.equals(edge))
        {
            insetsNew = new RectangleInsets(labelOuterSpace, insetsOld.getLeft(), labelInnerSpace, insetsOld.getRight());
        }

        return insetsNew;
    }

    /**
     * Draws the axis label.
     *
     * @param label  the label text.
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param dataArea  the area inside the axes.
     * @param edge  the location of the axis.
     * @param state  the axis state (<code>null</code> not permitted).
     *
     * @return Information about the axis.
     */

    //COPIED FROM THE ORIGINAL SOURCE CODEBY D. GILBERT
    //Validation class usage by PH
    @Override
    protected AxisState drawLabel(String label, Graphics2D g2,
            Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
            AxisState state) {

        // it is unlikely that 'state' will be null, but check anyway...
        Validation.requireNonNullParameterName(state, "state");

        if (StringUtilities.isNullOrEmpty(label)) {
            return state;
        }

        Font font = getLabelFont();
        RectangleInsets insets = getLabelInsets(edge);
        g2.setFont(font);
        g2.setPaint(getLabelPaint());
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D labelBounds = TextUtilities.getTextBounds(label, g2, fm);

        if (edge == RectangleEdge.TOP) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle(), labelBounds.getCenterX(),
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = dataArea.getCenterX();
            double labely = state.getCursor() - insets.getBottom() - labelBounds.getHeight() / 2.0;
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER, getLabelAngle(),
                    TextAnchor.CENTER);
            state.cursorUp(insets.getTop() + labelBounds.getHeight()
            + insets.getBottom());
        }
        else if (edge == RectangleEdge.BOTTOM) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle(), labelBounds.getCenterX(),
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = dataArea.getCenterX();
            double labely = state.getCursor()
                    + insets.getTop() + labelBounds.getHeight() / 2.0;
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER, getLabelAngle(),
                    TextAnchor.CENTER);
            state.cursorDown(insets.getTop() + labelBounds.getHeight()
            + insets.getBottom());
        }
        else if (edge == RectangleEdge.LEFT) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle() - Math.PI / 2.0, labelBounds.getCenterX(),
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                    - insets.getRight() - labelBounds.getWidth() / 2.0;
            double labely = dataArea.getCenterY();
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER,
                    getLabelAngle() - Math.PI / 2.0, TextAnchor.CENTER);
            state.cursorLeft(insets.getLeft() + labelBounds.getWidth()
            + insets.getRight());
        }
        else if (edge == RectangleEdge.RIGHT) {

            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle() + Math.PI / 2.0,
                    labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                    + insets.getLeft() + labelBounds.getWidth() / 2.0;
            double labely = dataArea.getY() + dataArea.getHeight() / 2.0;
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER,
                    getLabelAngle() + Math.PI / 2.0, TextAnchor.CENTER);
            state.cursorRight(insets.getLeft() + labelBounds.getWidth()
            + insets.getRight());

        }

        return state;
    }

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    @Override
    protected double estimateMaximumTickLabelWidth(Graphics2D g2, TickUnit unit) 
    {               
        RectangleInsets tickLabelInsets = getTickLabelInsets();
        double result = tickLabelInsets.getLeft() + tickLabelInsets.getRight();

        if (isVerticalTickLabels()) {
            // all tick labels have the same width (equal to the height of the
            // font)...
            FontRenderContext frc = g2.getFontRenderContext();
            LineMetrics lm = getTickLabelFont().getLineMetrics("0", frc);
            result += lm.getHeight();
        }
        else {
            // look at lower and upper bounds...
            FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
            Range range = getRange();
            double lower = MathUtilities.roundToMultiple(range.getLowerBound(), unit.getSize());//changes introduced by PH to avoid instabilities of tick labels when the range is slightly increased, that were caused by fractionalpart of numbers in ticks
            double upper = MathUtilities.roundToMultiple(range.getUpperBound(), unit.getSize());//changes introduced by PH
            String lowerStr = "";
            String upperStr = "";
            NumberFormat formatter = getNumberFormatOverride();

            if (formatter != null) {
                lowerStr = formatter.format(lower);
                upperStr = formatter.format(upper);
            }
            else {
                lowerStr = unit.valueToString(lower);
                upperStr = unit.valueToString(upper);               
            }
            double w1 = fm.stringWidth(lowerStr);
            double w2 = fm.stringWidth(upperStr);
            result += Math.max(w1, w2);
        }

        return result;
    }
}
