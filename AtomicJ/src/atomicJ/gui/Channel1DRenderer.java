
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
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;

public class Channel1DRenderer extends XYLineAndShapeRendererLightweight<Channel1DRendererData> implements ChannelRenderer
{	
    private String name;
    private final Object layerKey;
    private final StyleTag styleTag;
    private final Preferences pref;

    public Channel1DRenderer(Channel1DRendererData rendererData, Preferences pref, Object layerKey, StyleTag styleTag, String name)
    {
        super(rendererData);

        this.layerKey = layerKey;
        this.styleTag = styleTag;
        this.name = name;
        this.pref = pref;
    }

    @Override
    public void setData(Channel1DRendererData data)
    {
        Channel1DRendererData dataNew = data.isImmutable() ? data : data.getMutableCopy();
        replaceData(dataNew);
    }

    public Channel1DRendererDataImmutable getImmutableData()
    {
        return getData().getImmutableVersion();
    }

    @Override
    protected Channel1DRendererDataMutable<? extends Channel1DRendererData> getDataForModification()
    {
        Channel1DRendererData rendererData = getData();
        if(!rendererData.isImmutable())
        {
            //current data are mutable
            return rendererData.getMutableVersion();
        }

        //current data are immutable
        Channel1DRendererDataMutable<? extends Channel1DRendererData> rendererDataNew = rendererData.getMutableCopy();
        replaceData(rendererDataNew);

        return rendererDataNew;
    }

    @Override
    public void drawAnnotations(Graphics2D g2,Rectangle2D dataArea,ValueAxis domainAxis,ValueAxis rangeAxis, Layer layer, PlotRenderingInfo info) 
    {
        if(getBaseSeriesVisible())
        {
            super.drawAnnotations(g2, dataArea, domainAxis, rangeAxis, layer, info);
        }
    }

    @Override
    protected void drawPrimaryLineAsPath(XYItemRendererState state,
            Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
            int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis,
            Rectangle2D dataArea) {

        State s = (State) state;

        // if this is the last item, draw the path ...
        if (item == s.getLastItemIndex())
        {
            // draw path
            drawFirstPassShape(g2, pass, series, item, s.seriesPath);
        }
    }


    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int firstItem, int lastItem, CrosshairState crosshairState, int pass)
    {

        if (!getBaseSeriesVisible())
        {
            return;
        }

        boolean baseLinesVisible = getBaseLinesVisible();
        boolean buildLineEntities = dataset.getItemCount(series) < 3;

        // first pass draws the background (lines, for instance)
        if (baseLinesVisible && isLinePass(pass)) 
        {
            boolean drawSeriesLineAsPath = getDrawSeriesLineAsPath();

            for (int item = firstItem; item <= lastItem; item++) 
            {
                if(buildLineEntities || getDrawSeriesLineAsPath())
                {
                    registerLineSegmentWithStateObject((State)state, g2, plot, dataset, pass, series, item, domainAxis, rangeAxis, dataArea);
                }
                if (drawSeriesLineAsPath) 
                {
                    drawPrimaryLineAsPath(state, g2, plot, dataset, pass, series, item, domainAxis, rangeAxis, dataArea);
                }
                else
                {
                    drawPrimaryLine(state, g2, plot, dataset, pass, series, item, domainAxis, rangeAxis, dataArea);
                }

                if (buildLineEntities && item == state.getLastItemIndex())
                {
                    EntityCollection entities = null;
                    if (info != null && info.getOwner() != null) {
                        entities = info.getOwner().getEntityCollection();
                    }
                    if(entities!=null)
                    {
                        // draw path
                        Shape hotspot = getBaseStroke().createStrokedShape(((State)state).seriesPath); 
                        entities.add(new XYLinePathEntity(hotspot, dataset, series,"", ""));
                    }
                }
            }

        }
        // second pass adds shapes where the items are ..
        else if (isItemPass(pass)) 
        {
            // setup for collecting optional entity info...
            EntityCollection entities = null;
            if (info != null && info.getOwner() != null) {
                entities = info.getOwner().getEntityCollection();
            }           

            drawSecondaryPass(g2, plot, dataset, pass, firstItem, lastItem, domainAxis, dataArea, rangeAxis, crosshairState, entities);                   
        }
    }


    private void registerLineSegmentWithStateObject(State s,
            Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
            int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis,
            Rectangle2D dataArea)
    {        
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // update path to reflect latest point
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            float x = (float) transX1;
            float y = (float) transY1;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                x = (float) transY1;
                y = (float) transX1;
            }
            if (s.isLastPointGood()) {
                s.seriesPath.lineTo(x, y);
            }
            else {
                s.seriesPath.moveTo(x, y);
            }
            s.setLastPointGood(true);
        }
        else {
            s.setLastPointGood(false);
        }
    }

    public int getBaseMarkerIndex()
    {
        return getData().getBaseMarkerIndex();
    }	

    public void setBaseMarkerIndex(int markerIndex)
    {
        getDataForModification().setBaseMarkerIndex(markerIndex);
    }

    public float getBaseMarkerSize()
    {
        return getData().getBaseMarkerSize();
    }

    public void setBaseMarkerSize(float markerSize)
    {
        getDataForModification().setBaseMarkerSize(markerSize);
    }

    public Object getLayerKey()
    {
        return layerKey;
    }

    @Override
    public StyleTag getStyleKey()
    {
        return styleTag;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Preferences getPreferences()
    {
        return pref;
    }
}


