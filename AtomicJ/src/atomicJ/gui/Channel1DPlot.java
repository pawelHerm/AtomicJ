
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

import java.util.List;
import java.util.prefs.Preferences;


import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import atomicJ.data.Channel1D;
import atomicJ.data.Data1D;
import atomicJ.utilities.TwoTypePair;

/*
 * Because AtomicJ generates many charts, we have to save memory. That is why the datasets in the list 'data'
 * contains the same objects double[] (points in the graph) as the original LeafDatasets
 */

public class Channel1DPlot extends CustomizableXYPlot
{
    private static final long serialVersionUID = 1L;

    public Channel1DPlot(Data1D dataset, Preferences pref, PreferredScaleBarStyle domainScaleBarStyle, PreferredScaleBarStyle rangeScaleBarStyle)
    {	
        super(pref, domainScaleBarStyle, rangeScaleBarStyle);
        createAndSetDatasetsAndRenderers(dataset);
    }

    public Channel1DPlot(List<TwoTypePair<Channel1DDataset, Channel1DRenderer>> layers, Preferences pref, PreferredScaleBarStyle domainScaleBarStyle, PreferredScaleBarStyle rangeScaleBarStyle)
    {   
        super(pref, domainScaleBarStyle, rangeScaleBarStyle);

        for(TwoTypePair<Channel1DDataset, Channel1DRenderer> layer : layers)
        {
            addOrReplaceLayer(layer.getFirst(), layer.getSecond());
        }
    }

    private void createAndSetDatasetsAndRenderers(Data1D data)
    {
        List<? extends Channel1D> channels = data.getChannels();

        for(Channel1D channel : channels)
        {
            if(!channel.isEmpty())
            {
                Object key = channel.getIdentifier();
                ProcessableXYDataset<?> dataset = new Channel1DDataset(channel, channel.getName());
                XYItemRenderer oldRender = getLayerRenderer(key);
                XYItemRenderer renderer = (oldRender != null) ? oldRender : RendererFactory.getChannel1DRenderer(channel);
                addOrReplaceLayer(dataset, renderer);
            }
        }
    }

    public void addOrReplaceData(Data1D data)
    {        
        if(data != null)
        {
            createAndSetDatasetsAndRenderers(data);      
        } 
    }

    @Override
    public ProcessableXYDataset<?> getDataset()
    {
        XYDataset dataset = super.getDataset();
        return (ProcessableXYDataset<?>)dataset;
    }

    @Override
    public final void setDataset(XYDataset dataset)
    {
        if(dataset == null || dataset instanceof ProcessableXYDataset)
        {
            super.setDataset(dataset);
        }
        else			
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final void setDataset(int i, XYDataset dataset)
    {
        if(dataset == null || dataset instanceof ProcessableXYDataset)
        {
            super.setDataset(i, dataset);            
        }
        else			
        {
            throw new IllegalArgumentException();
        }
    }

    public interface Channel1DPlotFactory
    {
        public Channel1DPlot getPlot(Data1D dataset);
    }
}
