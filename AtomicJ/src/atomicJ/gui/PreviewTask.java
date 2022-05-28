
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;


public class PreviewTask extends MonitoredSwingWorker<Void, Void> 
{
    private final List<? extends ChannelSource> sources;
    private final Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> curveChartMap = new LinkedHashMap<>();
    private final Map<StandardChannel2DResource, Map<String,Channel2DChart<?>>> imageChartMap = new LinkedHashMap<>();
    private final PreviewDestination parent;

    public PreviewTask(List<? extends ChannelSource> sources, PreviewDestination parent)
    {
        super(parent.getPublicationSite(), "Rendering in progress", "Rendered", sources.size());
        this.sources = sources;
        this.parent = parent;
    }

    @Override
    public Void doInBackground() 
    {
        int n = sources.size();

        for(int i = 0; i < n; i++)
        {
            ChannelSource source = sources.get(i);
            if(source instanceof ImageSource)
            {
                ImageSource imageSource = (ImageSource)source;

                Map<String,Channel2DChart<?>> charts = ChannelSourceVisualization.getCharts(imageSource);        

                StandardChannel2DResource overlayImageResource = new StandardChannel2DResource(imageSource, imageSource.getIdentifiers());
                imageChartMap.put(overlayImageResource, charts);
            }
            else if(source instanceof SimpleSpectroscopySource)
            {
                SimpleSpectroscopySource spectroscopySource = (SimpleSpectroscopySource)source;

                Map<String,ChannelChart<?>> charts = ChannelSourceVisualization.getCharts(spectroscopySource);
                curveChartMap.put(new SpectroscopyBasicResource(spectroscopySource), charts);
            }

            setStep(i + 1);
        }

        return null;
    }

    @Override
    protected void done()
    {
        super.done();

        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent.getPublicationSite(), "Preview terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            parent.publishPreviewData(curveChartMap);
            parent.publishPreviewed2DData(imageChartMap);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(true);
    }
}
