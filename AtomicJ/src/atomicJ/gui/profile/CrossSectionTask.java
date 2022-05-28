
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

package atomicJ.gui.profile;

import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.gui.AtomicJ;
import atomicJ.resources.CrossSectionResource;
import atomicJ.sources.Channel2DSource;
import atomicJ.utilities.MultiMap;


public class CrossSectionTask extends MonitoredSwingWorker< Map<String, ChannelSectionLine>, Void> 
{
    private final MultiMap<Channel2DSource<?>, String> sources;
    private final CrossSectionsReceiver receiver;
    private final Window parent;
    private final Profile profile;
    private final CrossSectionResource resource;


    public CrossSectionTask(MultiMap<Channel2DSource<?>, String> types, Profile profile, CrossSectionResource resource, CrossSectionsReceiver receiver, Window parent)
    {
        super(parent, "Extracting crosssections in progress", "extracted", types.getTotalSize());
        this.sources = types;
        this.receiver = receiver;
        this.parent = parent;
        this.profile = profile;
        this.resource = resource;	
    }

    @Override
    public Map<String, ChannelSectionLine> doInBackground() 
    {
        Map<String, ChannelSectionLine> allCrossSections = new LinkedHashMap<>();

        int i = 0;

        for(Entry<Channel2DSource<?>, List<String>> entry : sources.entrySet())
        {
            Channel2DSource<?> source =  entry.getKey();
            List<String> types = entry.getValue();
            for(String type: types)
            {			
                Map<String, ChannelSectionLine> sections = source.getCrossSections(profile.getDistanceShape(), profile.getKey(), profile.getLabel(), type);
                allCrossSections.putAll(sections);
            }

            setStep(++i);
        }
        return allCrossSections;
    }

    @Override
    protected void done()
    {
        super.done();

        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent, "Extracting crosssections terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            Map<String, ChannelSectionLine> allCrossSections;
            try 
            {
                allCrossSections = get();
                resource.addOrReplaceCrossSections(allCrossSections);
                receiver.addOrReplaceCrossSections(resource, allCrossSections);

                receiver.selectResource(resource);
                receiver.externalSetProfileMarkerPositions(profile.getKey(), profile.getKnobPositions()); 

            } catch (InterruptedException | ExecutionException e) 
            {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(true);
    }
}
