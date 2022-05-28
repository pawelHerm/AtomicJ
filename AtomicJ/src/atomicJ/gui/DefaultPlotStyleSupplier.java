
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;


import org.jfree.chart.plot.XYPlot;

import atomicJ.data.Datasets;


public class DefaultPlotStyleSupplier implements PlotStyleSupplier
{
    private static final DefaultPlotStyleSupplier INSTANCE = new DefaultPlotStyleSupplier();

    private final Map<String, Stroke> domainStrokes = new LinkedHashMap<>();
    private final Map<String, Stroke> rangeStrokes = new LinkedHashMap<>();

    private final Map<String, Boolean> domainGridlinesVisible = new LinkedHashMap<>();
    private final Map<String, Boolean> rangeGridlinesVisible = new LinkedHashMap<>();

    private final Map<String, Paint> gridlinePaints = new LinkedHashMap<>();

    private final Map<String, Paint> backgroundPaints = new LinkedHashMap<>();

    private  DefaultPlotStyleSupplier()
    {	
        Stroke stroke = new BasicStroke(1.f);

        domainStrokes.put(Datasets.MAP_PLOT, stroke);
        rangeStrokes.put(Datasets.MAP_PLOT,  stroke);

        domainGridlinesVisible.put(Datasets.MAP_PLOT, Boolean.FALSE);
        rangeGridlinesVisible.put(Datasets.MAP_PLOT,  Boolean.FALSE);
        gridlinePaints.put(Datasets.MAP_PLOT, Color.black);

        domainStrokes.put(Datasets.IMAGE_PLOT, stroke);
        rangeStrokes.put(Datasets.IMAGE_PLOT,  stroke);

        domainGridlinesVisible.put(Datasets.IMAGE_PLOT, Boolean.FALSE);
        rangeGridlinesVisible.put(Datasets.IMAGE_PLOT,  Boolean.FALSE);		
        gridlinePaints.put(Datasets.IMAGE_PLOT, Color.black);

        domainStrokes.put(Datasets.DENSITY_PLOT, stroke);
        rangeStrokes.put(Datasets.DENSITY_PLOT,  stroke);

        domainGridlinesVisible.put(Datasets.DENSITY_PLOT, Boolean.FALSE);
        rangeGridlinesVisible.put(Datasets.DENSITY_PLOT,  Boolean.FALSE);
        gridlinePaints.put(Datasets.DENSITY_PLOT, Color.black);

        gridlinePaints.put(Datasets.CROSS_SECTION_PLOT, Color.white);
        backgroundPaints.put(Datasets.CROSS_SECTION_PLOT, Color.black);		

        domainStrokes.put(Datasets.BOX_AND_WHISKER_PLOT, stroke);
        rangeStrokes.put(Datasets.BOX_AND_WHISKER_PLOT,  stroke);

        domainGridlinesVisible.put(Datasets.BOX_AND_WHISKER_PLOT, Boolean.FALSE);
        rangeGridlinesVisible.put(Datasets.BOX_AND_WHISKER_PLOT,  Boolean.FALSE);
    }

    public static DefaultPlotStyleSupplier getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Stroke getDefaultDomainGridlineStroke(String key)
    {
        Stroke stroke = domainStrokes.get(key);
        if(stroke != null)
        {
            return stroke;
        }
        else
        {
            return XYPlot.DEFAULT_GRIDLINE_STROKE;
        }
    }

    @Override
    public Stroke getDefaultRangeGridlineStroke(String key)
    {
        Stroke stroke = rangeStrokes.get(key);
        if(stroke != null)
        {
            return stroke;
        }
        else
        {
            return XYPlot.DEFAULT_GRIDLINE_STROKE;
        }
    }

    @Override
    public boolean getDefaultDomainGridlineVisible(String key) 
    {
        Boolean visible = domainGridlinesVisible.get(key);
        if(visible != null)
        {
            return visible;
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean getDefaultRangeGridlineVisible(String key) 
    {
        Boolean visible = rangeGridlinesVisible.get(key);
        if(visible != null)
        {
            return visible;
        }
        else
        {
            return true;
        }
    }

    @Override
    public Paint getDefaultDomainGridlinePaint(String key)
    {
        Paint gridlinePaint = gridlinePaints.get(key);
        if(gridlinePaint != null)
        {
            return gridlinePaint;
        }
        else
        {
            return Color.red;
        }
    }

    @Override
    public Paint getDefaultRangeGridlinePaint(String key)
    {
        Paint gridlinePaint = gridlinePaints.get(key);
        if(gridlinePaint != null)
        {
            return gridlinePaint;
        }
        else
        {
            return Color.red;
        }
    }

    @Override
    public Paint getDefaultBackgroundPaint(String key)
    {
        Paint backgroundPaint = backgroundPaints.get(key);
        if(backgroundPaint != null)
        {
            return backgroundPaint;
        }
        else
        {
            return Color.white;
        }
    }
}
