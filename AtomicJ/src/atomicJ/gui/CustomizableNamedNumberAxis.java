
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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CustomizableNamedNumberAxis extends CustomizableNumberBaseAxis implements PreferencesSource
{
    private static final long serialVersionUID = 1L;

    private List<String> categories = new ArrayList<>();
    private final Map<String, String> userSelectedNames = new LinkedHashMap<>();

    private Range autoRange;

    public CustomizableNamedNumberAxis(String label, List<String> categories, Preferences pref)
    {
        super(label, pref);

        this.autoRange = new Range(0.25, categories.size() + 0.75);

        setCategories(categories);

        setAutoTickUnitSelection(false);    
        setAutoRangeIncludesZero(false);
        setAutoRangeStickyZero(false);
        setPreferredStyle(pref);
    }

    public CustomizableNamedNumberAxis(String label, List<String> categories, PreferredAxisStyle preferredStyle)
    {
        super(label, preferredStyle);

        this.autoRange = new Range(0.25, categories.size() + 0.75);

        setCategories(categories);

        setAutoTickUnitSelection(false);    
        setAutoRangeIncludesZero(false);
        setAutoRangeStickyZero(false);
        setPreferredStyle(preferredStyle);
    }

    public CustomizableNamedNumberAxis(CustomizableNamedNumberAxis that)
    {
        super(that);

        setCategories(that.categories);

        setAutoTickUnitSelection(false);    
        setAutoRangeIncludesZero(false);
        setAutoRangeStickyZero(false);        
    }

    public void setCategories(List<String> categories)
    {
        if(this.categories.size() != categories.size())
        {
            this.autoRange = new Range(0.25, categories.size() + 0.75);
            autoAdjustRange();
        }

        this.categories = new ArrayList<>(categories);

        List<String> labels = getLabels();

        NamedTickUnit tickUnit = new NamedTickUnit(labels);
        setTickUnit(tickUnit);      

    }

    private List<String> getLabels()
    {
        List<String> labels = new ArrayList<>();

        for(String category : categories)
        {

            String label = this.userSelectedNames.get(category);

            if(label == null)
            {
                labels.add(category);
            }
            else
            {
                labels.add(label);
            }
        }

        return labels;
    }

    public void putUserSelectedCategoryLabel(String category, String label)
    {        
        this.userSelectedNames.put(category, label);

        List<String> labels = getLabels();

        NamedTickUnit tickUnit = new NamedTickUnit(labels);
        setTickUnit(tickUnit);
    }

    public String getCategory(String label)
    {
        for(Entry<String, String> entry : userSelectedNames.entrySet())
        {
            String value = entry.getValue();
            if(value.equals(label))
            {
                return entry.getKey();
            }
        }

        return label;
    }

    private void setPreferredStyle(PreferredAxisStyle style)
    {             
    }

    private void setPreferredStyle(Preferences pref)
    {		
    }

    @Override
    public Range getDefaultAutoRange() 
    {
        return autoRange;
    }

    @Override
    public void autoAdjustRange()
    {	    	    
        Range r = getDefaultAutoRange();       
        setRange(r, false, false); 
    }

    @Override
    protected String getName() 
    {        
        return getLabel();
    }

    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge,
            PlotRenderingInfo plotState) {

        AxisState state = new AxisState(cursor);
        // if the axis is not visible, don't draw it...
        if (!isVisible()) {

            // even though the axis is not visible, we need ticks for the
            // gridlines...
            List ticks = refreshTicks(g2, state, dataArea, edge);
            state.setTicks(ticks);
            return state;
        }

        createAndAddEntity(cursor, state, dataArea, edge, plotState);

        // draw the tick marks and labels...
        state = drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge, plotState);

        // draw the axis label...
        state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);


        return state;

    }

    //method drawTickMarksAndLabels copied from the ValueAxis source file, by David Gilber and others

    //I added support for entities for labels
    //I had to pass one more parameter - PlotRenderingInfo
    protected AxisState drawTickMarksAndLabels(Graphics2D g2,
            double cursor, Rectangle2D plotArea, Rectangle2D dataArea,
            RectangleEdge edge, PlotRenderingInfo plotState) {

        AxisState state = new AxisState(cursor);

        if (isAxisLineVisible()) {
            drawAxisLine(g2, cursor, dataArea, edge);
        }

        List ticks = refreshTicks(g2, state, dataArea, edge);
        state.setTicks(ticks);
        g2.setFont(getTickLabelFont());
        Iterator iterator = ticks.iterator();
        while (iterator.hasNext()) {
            ValueTick tick = (ValueTick) iterator.next();
            if (isTickLabelsVisible()) {
                g2.setPaint(getTickLabelPaint());
                float[] anchorPoint = calculateAnchorPoint(tick, cursor,
                        dataArea, edge);

                String tickText = tick.getText();
                TextUtilities.drawRotatedString(tick.getText(), g2,
                        anchorPoint[0], anchorPoint[1], tick.getTextAnchor(),
                        tick.getAngle(), tick.getRotationAnchor());

                ////////////ADDED BY P. HERMANOWICZ


                Shape area = TextUtilities.calculateRotatedStringBounds(tickText, g2,
                        anchorPoint[0], anchorPoint[1], tick.getTextAnchor(),
                        tick.getAngle(), tick.getRotationAnchor());

                if(plotState != null && area != null)
                {                   
                    NamedAxisCategoryEntity entity = new NamedAxisCategoryEntity(area, tickText, this);
                    EntityCollection entityCollection = plotState.getOwner().getEntityCollection();

                    if(entityCollection != null)
                    {
                        entityCollection.add(entity);
                    }
                }

                /////////////////////////////////////

            }

            if ((isTickMarksVisible() && tick.getTickType().equals(
                    TickType.MAJOR)) || (isMinorTickMarksVisible()
                            && tick.getTickType().equals(TickType.MINOR))) {

                double ol = (tick.getTickType().equals(TickType.MINOR)) 
                        ? getMinorTickMarkOutsideLength()
                                : getTickMarkOutsideLength();

                        double il = (tick.getTickType().equals(TickType.MINOR)) 
                                ? getMinorTickMarkInsideLength()
                                        : getTickMarkInsideLength();

                                float xx = (float) valueToJava2D(tick.getValue(), dataArea,
                                        edge);
                                Line2D mark = null;
                                g2.setStroke(getTickMarkStroke());
                                g2.setPaint(getTickMarkPaint());
                                if (edge == RectangleEdge.LEFT) {
                                    mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
                                }
                                else if (edge == RectangleEdge.RIGHT) {
                                    mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
                                }
                                else if (edge == RectangleEdge.TOP) {
                                    mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
                                }
                                else if (edge == RectangleEdge.BOTTOM) {
                                    mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
                                }
                                g2.draw(mark);
            }
        }

        // need to work out the space used by the tick labels...
        // so we can update the cursor...
        double used = 0.0;
        if (isTickLabelsVisible()) {
            if (edge == RectangleEdge.LEFT) {
                used += findMaximumTickLabelWidth(ticks, g2, plotArea,
                        isVerticalTickLabels());
                state.cursorLeft(used);
            }
            else if (edge == RectangleEdge.RIGHT) {
                used = findMaximumTickLabelWidth(ticks, g2, plotArea,
                        isVerticalTickLabels());
                state.cursorRight(used);
            }
            else if (edge == RectangleEdge.TOP) {
                used = findMaximumTickLabelHeight(ticks, g2, plotArea,
                        isVerticalTickLabels());
                state.cursorUp(used);
            }
            else if (edge == RectangleEdge.BOTTOM) {
                used = findMaximumTickLabelHeight(ticks, g2, plotArea,
                        isVerticalTickLabels());
                state.cursorDown(used);
            }
        }

        return state;
    }


}
