
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

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.ui.Size2D;

import atomicJ.utilities.SerializationUtilities;


public abstract class CustomizableCategoryPlot extends CategoryPlot implements AxisSource, PreferencesSource, RoamingTitleChangeListener
{
    private static final long serialVersionUID = 1L;

    private Preferences pref;

    public CustomizableCategoryPlot(Preferences pref, PreferredScaleBarStyle domainScaleBarStyle, PreferredScaleBarStyle rangeScaleBarStyle)
    {
        this.pref = pref;

        setPreferredStyle(pref, null);
        setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        setRangeZeroBaselineVisible(false);

        setRangePannable(true);
    }

    public CustomizableCategoryPlot(Preferences pref, String styleKey)
    {
        this.pref = pref;
        setFixedDataAreaSize(new Size2D(500,500));
        setUseFixedDataAreaSize(true);

        setPreferredStyle(pref, styleKey);
        setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        setRangeZeroBaselineVisible(false);

        setRangePannable(true);
    }


    @Override
    public void axisChanged(AxisChangeEvent event)
    {
        Axis axis = event.getAxis();

        if(axis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis)axis;

            AxisLocation preferredLocation = customizableAxis.getPreferredAxisLocation();

            if(preferredLocation != null)
            {
                if(axis.equals(getDomainAxis()))
                {
                    setDomainAxisLocation(preferredLocation);
                }
                else if(axis.equals(getRangeAxis()))
                {
                    setRangeAxisLocation(preferredLocation);
                }
            }			
        }
        super.axisChanged(event);
    }

    @Override
    public CustomizableCategoryPlot clone()
    {
        CustomizableCategoryPlot copy;
        try 
        {
            copy = (CustomizableCategoryPlot) super.clone();

            copy.pref = pref;	

        } catch (CloneNotSupportedException e) 
        {
            e.printStackTrace();
            return null;
        }
        return copy;
    }

    public void updateFixedDataAreaSize(Graphics2D g2, Rectangle2D area)
    {}

    private void setPreferredStyle(Preferences pref, String key)
    {		
        PlotStyleSupplier supplier = DefaultPlotStyleSupplier.getInstance();
        Stroke defaultDomainGridline = (key == null) ? XYPlot.DEFAULT_GRIDLINE_STROKE : supplier.getDefaultDomainGridlineStroke(key);
        Stroke defaultRangeGridline = (key == null) ? XYPlot.DEFAULT_GRIDLINE_STROKE : supplier.getDefaultRangeGridlineStroke(key);

        boolean defaultDomainGridlineVisible = (key == null) ? true : supplier.getDefaultDomainGridlineVisible(key);
        boolean defaultRangeGridlineVisible = (key == null) ? true : supplier.getDefaultRangeGridlineVisible(key);

        Paint defaultDomainGridlinePaint = (key == null) ? Color.red : supplier.getDefaultDomainGridlinePaint(key);
        Paint defaultRangeGridlinePaint = (key == null) ? Color.red : supplier.getDefaultRangeGridlinePaint(key);

        Paint defaultBackgroundPaint = (key == null) ? Color.white : supplier.getDefaultBackgroundPaint(key);

        Paint backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, defaultBackgroundPaint);
        Paint outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_OUTLINE_PAINT, Color.black);
        Paint domainGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_PAINT, defaultDomainGridlinePaint);
        Paint rangeGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_PAINT, defaultRangeGridlinePaint);
        Stroke outlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_OUTLINE_STROKE, Plot.DEFAULT_OUTLINE_STROKE);
        Stroke domainGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_STROKE, defaultDomainGridline);
        Stroke rangeGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE, defaultRangeGridline);
        PlotOrientation plotOrientation = pref.getBoolean(PreferredBasePlotStyle.PLOT_VERTICAL,true) ? PlotOrientation.VERTICAL: PlotOrientation.HORIZONTAL;

        boolean outlineVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_OUTLINE_VISIBLE, true);
        boolean domainGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_VISIBLE, defaultDomainGridlineVisible);
        boolean rangeGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_VISIBLE, defaultRangeGridlineVisible);

        setBackgroundPaint(backgroundPaint);

        setOutlineStroke(outlineStroke);
        setOutlinePaint(outlinePaint);
        setOutlineVisible(outlineVisible);

        setDomainGridlineStroke(domainGridlineStroke);
        setDomainGridlinesVisible(domainGridlinesVisible);
        setDomainGridlinePaint(domainGridlinePaint);

        setRangeGridlineStroke(rangeGridlineStroke);	    
        setRangeGridlinePaint(rangeGridlinePaint);
        setRangeGridlinesVisible(rangeGridlinesVisible);    	

        setOrientation(plotOrientation);
    }	

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    public List<CategoryItemRenderer> getRenderers()
    {
        List<CategoryItemRenderer> renderers = new ArrayList<>();

        for(int i = 0; i<getRendererCount(); i++)
        {
            renderers.add(getRenderer(i));
        }

        return renderers;
    }



    @Override
    public void roamingTitleChanged(RoamingTitleChangeEvent event)
    {
        fireChangeEvent();
    }

    public abstract String getName();


    public Paint getDefaultAnnotationPaint()
    {
        return Color.black;
    }
}
