
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

package atomicJ.gui.histogram;

import java.util.prefs.Preferences;


import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import atomicJ.data.Datasets;
import atomicJ.data.units.Quantity;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.AxisType;
import atomicJ.gui.Channel1DRenderer;
import atomicJ.gui.ContinuousSeriesRenderer;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.CustomizableXYBarRenderer;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.ChannelRenderer;
import atomicJ.gui.NameChangeModel;
import atomicJ.gui.PreferredContinuousSeriesRendererStyle;
import atomicJ.gui.NameChangeDialog;
import atomicJ.gui.PreferredScaleBarStyle;
import atomicJ.gui.StandardStyleTag;
import atomicJ.gui.StyleTag;


public class HistogramPlot extends CustomizableXYPlot 
{
    private static final long serialVersionUID = 1L;

    private final CustomizableNumberAxis rangeAxis;
    private final StyleTag style;

    public HistogramPlot(FlexibleHistogramDataset dataset, StyleTag style, String name, Preferences pref, Quantity domainQuantity, Quantity rangeQuantity, boolean onlyIntegersOnY) 
    {
        super(pref, new PreferredScaleBarStyle(pref.node("DomainScaleBar")), new PreferredScaleBarStyle(pref.node("RangeScaleBar")));

        this.style = style;

        setDataset(0, dataset);

        CustomizableXYBarRenderer rendererHistogram = new CustomizableXYBarRenderer(style, name);
        setRenderer(0, rendererHistogram);	

        Preferences prefDomain = pref.node(AxisType.DOMAIN.toString());
        CustomizableNumberAxis domainAxis = new CustomizableNumberAxis(domainQuantity, prefDomain);
        Preferences prefRange = pref.node(AxisType.RANGE.toString());
        rangeAxis = new CustomizableNumberAxis(rangeQuantity, prefRange);

        rangeAxis.setAutoRangeStickyZero(true);
        rangeAxis.setAutoRangeIncludesZero(true);

        if(onlyIntegersOnY)
        {
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        }

        setDomainAxis(domainAxis);
        setRangeAxis(rangeAxis);
    }

    public void setDomainAxisDataQuantity(Quantity dataQuantityNew)
    {
        ValueAxis domainAxis = getDomainAxis();

        if(domainAxis instanceof CustomizableNumberAxis)
        {
            ((CustomizableNumberAxis) domainAxis).setDataQuantity(dataQuantityNew);
        }
    }

    public void setRangeAxisDataQuantity(Quantity dataQuantityNew)
    {
        rangeAxis.setDataQuantity(dataQuantityNew);
    }

    public void setOnlyTicksOnRangeAxis(boolean onlyIntegersOnY)
    {
        TickUnitSource unitSource = onlyIntegersOnY ? NumberAxis.createIntegerTickUnits() : rangeAxis.getDefaultTickUnits();        
        rangeAxis.setStandardTickUnits(unitSource);
    }

    public void setHistogramDataset(FlexibleHistogramDataset dataset)
    {
        if(dataset != null)
        {
            setDataset(0, dataset);
        }
    }

    public void setFitDataset(XYSeriesCollection fitDataset)
    {
        if(fitDataset == null)
        {
            setDataset(1, null);
        }
        setDataset(1, fitDataset);
        if(getRenderer(1) == null)
        {
            StyleTag fitStyleTag = new StandardStyleTag(Datasets.FIT, style.getInitialStyleKey() + Datasets.FIT);
            Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(fitStyleTag.getPreferredStyleKey());

            PreferredContinuousSeriesRendererStyle prefStyle = PreferredContinuousSeriesRendererStyle.getInstance(pref, fitStyleTag);
            ContinuousSeriesRenderer rendererFit = new ContinuousSeriesRenderer(prefStyle, style.getInitialStyleKey() + Datasets.FIT, fitStyleTag, Datasets.FIT);
            setRenderer(1, rendererFit);		
        }		
    }

    public boolean containsHistogramSeries(Comparable<?> key)
    {
        boolean contains = false;

        int n = getDatasetCount();

        for(int i = 0; i<n; i++)
        {
            XYDataset dataset = getDataset(i);
            if(dataset instanceof FlexibleHistogramDataset)
            {
                FlexibleHistogramDataset histogramDataset = (FlexibleHistogramDataset)dataset;

                contains = contains || histogramDataset.containsSeries(key) ;
                if(contains)
                {
                    break;
                }
            }
        }
        return contains;
    }


    public void setHistogramSeriesKey(Comparable<?> keyOld, Comparable<?> keyNew)
    {		
        int n = getDatasetCount();

        for(int i = 0; i<n; i++)
        {
            XYDataset dataset = getDataset(i);
            if(dataset instanceof FlexibleHistogramDataset)
            {
                FlexibleHistogramDataset histogramDataset = (FlexibleHistogramDataset)dataset;

                if(histogramDataset.containsSeries(keyOld))
                {
                    histogramDataset.setSeriesKey(keyOld, keyNew);
                }
            }
        }
    }


    public int findHistogramDataset(Comparable<?> key)
    {
        int n = getDatasetCount();

        for(int i = 0; i<n; i++)
        {
            XYDataset dataset = getDataset(i);
            if(dataset instanceof FlexibleHistogramDataset)
            {
                FlexibleHistogramDataset histogramDataset = (FlexibleHistogramDataset)dataset;

                if(histogramDataset.containsSeries(key))
                {
                    return i;
                }
            }
        }
        return -1;
    }


    @Override
    public CustomizableXYBarRenderer findRenderer(Comparable<?> key)
    {		
        int datasetIndex = findHistogramDataset(key);

        CustomizableXYBarRenderer barRenderer = null;

        boolean rendererFound = (datasetIndex >= 0);
        if(rendererFound)
        {
            ChannelRenderer renderer = getRenderer(datasetIndex);
            if(renderer instanceof CustomizableXYBarRenderer)
            {
                barRenderer = (CustomizableXYBarRenderer)renderer;
            }
        }

        return barRenderer;
    }

    @Override
    public void attemptChangeSeriesKey(final Comparable<?> seriesKey)
    {
        boolean contains = containsHistogramSeries(seriesKey);

        if(contains)
        {
            NameChangeDialog nameDialog = new NameChangeDialog(AtomicJ.getApplicationFrame(), "Series name");
            boolean approved = nameDialog.showDialog(new NameChangeModel() 
            {

                @Override
                public void setName(Comparable<?> keyNew) 
                {
                    setHistogramSeriesKey(seriesKey, keyNew);
                }

                @Override
                public Comparable<?> getName() 
                {
                    return seriesKey;
                }
            });
            if(approved)
            {
                Comparable<?> keyNew = nameDialog.getNewKey();

                CustomizableXYBarRenderer renderer = findRenderer(seriesKey);

                setHistogramSeriesKey(seriesKey, keyNew);
                renderer.setName(keyNew.toString());

                fireChangeEvent();
            }
        }
    }
}
