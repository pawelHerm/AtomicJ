
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

package atomicJ.gui.profile;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;


import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import atomicJ.data.Channel1D;
import atomicJ.data.Datasets;
import atomicJ.data.units.Quantity;
import atomicJ.gui.Channel1DDataset;
import atomicJ.gui.Channel1DRenderer;
import atomicJ.gui.CurveMarker;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.ChannelRenderer;
import atomicJ.gui.IndexedStyleTag;
import atomicJ.gui.MarkerRenderer;
import atomicJ.gui.PreferredContinuousSeriesRendererStyle;
import atomicJ.gui.ProcessableDataset;
import atomicJ.gui.ProcessableXYDataset;
import atomicJ.gui.StandardStyleTag;
import atomicJ.gui.StyleTag;


public class CrossSectionPlot extends CustomizableXYPlot
{
    private static final long serialVersionUID = 1L;

    public CrossSectionPlot(Preferences pref)
    {	
        super(pref, Datasets.CROSS_SECTION_PLOT);
    }

    @Override
    protected CustomizableNumberAxis buildNewRangeAxis(Preferences pref, Quantity quantity)
    {
        CustomizableNumberAxis rangeAxis = new CustomizableNumberAxis(quantity, pref);
        rangeAxis.setLowerMargin(0.07);
        rangeAxis.setUpperMargin(0.07);

        return rangeAxis;
    }

    @Override
    protected CustomizableNumberAxis buildNewDomainAxis(Preferences pref, Quantity quantity)
    {
        CustomizableNumberAxis domainAxis = new CustomizableNumberAxis(quantity, pref);

        domainAxis.setLowerMargin(0.04);
        domainAxis.setUpperMargin(0.04);

        return domainAxis;
    }


    //margin that must be left for annotations before the first point / after the last point
    // for annotations; this value is in pixels (i.e. in Java 2D space, not the data space)

    public int getMarkerMarginWidth()
    {
        int width = 0;

        for(ChannelRenderer renderer : getRenderers())
        {
            if(renderer instanceof MarkerRenderer)
            {
                int currentWidth = ((MarkerRenderer) renderer).getMarkerMarginWidth();
                width = Math.max(width, currentWidth);
            }
        }

        return width;
    }  

    //margin that must be left for annotations beneath the point of lowest vale / above the point of highest value
    // for annotations; this value is in pixels (i.e. in Java 2D space, not the data space)
    public int getMarkerMarginHeight()
    {
        int height = 0;

        for(ChannelRenderer renderer : getRenderers())
        {
            if(renderer instanceof MarkerRenderer)
            {
                int currentHeight = ((MarkerRenderer) renderer).getMarkerMarginHeight();
                height = Math.max(height, currentHeight);
            }
        }

        return height;
    }  

    public void addOrReplaceCrossSectionCurve(Object key, Channel1D curve)
    {	
        if(containsLayer(key))
        {
            replaceLayerData(key, curve);
        }
        else
        {
            addNewLayer(key, curve);
        }		
    }

    private void replaceLayerData(Object key, Channel1D channel)
    {
        XYDataset dataset = new Channel1DDataset(channel, channel.getName());
        replaceLayerDataset(key, dataset);
    }

    private void addNewLayer(Object key, Channel1D channel)
    {
        ProcessableDataset<?> dataset = new Channel1DDataset(channel, channel.getName());	

        StyleTag styleTag = (key instanceof Integer) ? new IndexedStyleTag(Datasets.CROSS_SECTION, (Integer)key) : new StandardStyleTag(channel.getIdentifier());
        Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(styleTag.getPreferredStyleKey());
        PreferredContinuousSeriesRendererStyle prefStyle = PreferredContinuousSeriesRendererStyle.getInstance(pref, styleTag);

        CrossSectionsRenderer renderer = new CrossSectionsRenderer(prefStyle, channel.getIdentifier(), styleTag, channel.getName());

        addOrReplaceLayer(key, dataset, renderer);
    }

    public void removeCrossSection(Object key, Channel1D curve)
    {
        clearLayerData(key);
    }

    public void addOrReplaceCrossSections(Map<Object, Channel1D> crossSections)
    {
        for(Entry<Object, Channel1D> entry: crossSections.entrySet())
        {
            Object key = entry.getKey();
            Channel1D curve = entry.getValue();

            addOrReplaceCrossSectionCurve(key, curve);
        }
    }

    public void addMarker(Object profileKey, CurveMarker marker)
    {	
        XYItemRenderer renderer = getLayerRenderer(profileKey);

        if(renderer != null && renderer instanceof CrossSectionsRenderer)
        {
            CrossSectionsRenderer sectionRenderer = (CrossSectionsRenderer)renderer;
            sectionRenderer.addMarker(marker); 
        }
    }

    public void removeMarker(Object profileKey, CurveMarker marker)
    {
        XYItemRenderer renderer = getLayerRenderer(profileKey);
        if(renderer != null && renderer instanceof CrossSectionsRenderer)
        {
            CrossSectionsRenderer sectionRenderer = (CrossSectionsRenderer)renderer;
            sectionRenderer.removeMarker(marker); 
        }
    }

    @Override
    public List<ChannelRenderer> getRenderers()
    {
        List<ChannelRenderer> renderers = new ArrayList<>();

        for(int i = 0; i<getRendererCount(); i++)
        {
            ProcessableXYDataset<?> dataset = getDataset(i);
            if(dataset != null)
            {
                renderers.add(getRenderer(i));
            }		
        }

        return renderers;
    }

    @Override
    public ProcessableXYDataset<?> getDataset()
    {
        XYDataset dataset = super.getDataset();
        return (ProcessableXYDataset<?>)dataset;
    }

    @Override
    public ProcessableXYDataset<?> getDataset(int i)
    {
        XYDataset dataset = super.getDataset(i);
        return (ProcessableXYDataset<?>)dataset;
    } 

    @Override
    final public void setDataset(XYDataset dataset)
    {
        if(dataset instanceof ProcessableXYDataset<?> || dataset == null)
        {
            super.setDataset(dataset);
        }
        else			
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    final public void setDataset(int i, XYDataset dataset)
    {
        if(dataset instanceof ProcessableXYDataset<?> || dataset == null)
        {
            super.setDataset(i, dataset);
        }
        else			
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Paint getDefaultAnnotationPaint()
    {
        return Color.white;
    }
}
