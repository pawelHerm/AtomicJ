
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

package atomicJ.resources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.curveProcessing.ProcessingBatchMemento;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoManager;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public class SpectroscopyBasicResource extends AbstractResource implements SpectroscopyResource
{
    public static final String RECORDED_CURVE = "Recorded curve";

    private final MetaMap<String, Object, DistanceMeasurementDrawable> distanceMeasurements = new MetaMap<>();

    private final UndoManager undoManager = new UndoManager(2);

    private final SimpleSpectroscopySource spectroscopySource;

    public SpectroscopyBasicResource(SimpleSpectroscopySource spectroscopySource)
    {
        super(spectroscopySource.getCorrespondingFile(), spectroscopySource.getShortName(), spectroscopySource.getLongName());
        this.spectroscopySource = spectroscopySource;
    }

    public SpectroscopyBasicResource(SpectroscopyBasicResource resourceOld)
    {
        super(resourceOld);
        this.spectroscopySource = resourceOld.spectroscopySource.copy();
    }

    @Override
    public boolean containsChannelsFromSource(ChannelSource source)
    {
        boolean contains = (source != null) ? this.spectroscopySource.equals(source) : false;
        return contains;
    }

    public SpectroscopyBasicResource copy()
    {
        SpectroscopyBasicResource copy = new SpectroscopyBasicResource(this);
        return copy;
    }

    public ProcessingBatchMemento getProcessingMemento()
    {
        return spectroscopySource.getProcessingMemento();
    }

    @Override
    public SimpleSpectroscopySource getSource()
    {
        return spectroscopySource;
    }

    @Override
    public boolean isCorrespondingMapPositionKnown()
    {
        boolean known = spectroscopySource.isCorrespondingMapPositionKnown();
        return known;
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {        
        return spectroscopySource.getSampleCollections();
    }

    //MEASUREMENTS

    @Override
    public int getMeasurementCount(String type)
    {
        int count = distanceMeasurements.size(type);
        return count;     
    }

    @Override
    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements(String type)
    {
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(type);         
        return new LinkedHashMap<>(measurtementsForType);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.put(type, measurement.getKey(), measurement);    
    }

    @Override
    public void removeDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.remove(type, measurement.getKey());
    }


    @Override
    public Map<String, String> getAutomaticChartTitles()
    {
        return spectroscopySource.getAutomaticChartTitles();
    }

    @Override
    public Set<String> getIdentifiers(String type)
    {
        return new LinkedHashSet<>(spectroscopySource.getIdentifiers());
    }

    @Override
    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        List<Channel1D> allChannels = spectroscopySource.getChannels();
        Set<String> filteredIdentifiers = new LinkedHashSet<>();

        for(Channel1D channel : allChannels)
        {
            if(filter.accepts(channel))
            {
                filteredIdentifiers.add(channel.getIdentifier());
            }
        }

        return filteredIdentifiers;
    }

    @Override
    public Set<String> getIdentifiersForAllTypes()
    {
        return new LinkedHashSet<>(spectroscopySource.getIdentifiers());
    }

    @Override
    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel1D> filter)
    {
        List<Channel1D> allChannels = spectroscopySource.getChannels();
        Set<String> filteredIdentifiers = new LinkedHashSet<>();

        for(Channel1D channel : allChannels)
        {
            if(filter.accepts(channel))
            {
                filteredIdentifiers.add(channel.getIdentifier());
            }
        }

        return filteredIdentifiers;
    }



    //returns a map with values being those channels whose identifiers are in the set 'identifiers'. The keys are the corresponding identifiers
    //A multimap is returned, because this TypeModelManager may contain two different sources, with channels of the same identifier
    @Override
    public MultiMap<String, Channel1D> getChannelsForIdentifiers(Set<String> identifiers)
    {
        MultiMap<String, Channel1D> channelsForIdentifiers = new MultiMap<>();
        List<Channel1D> channels = spectroscopySource.getChannels(identifiers);

        for(Channel1D channel : channels)
        {
            channelsForIdentifiers.put(channel.getIdentifier(), channel);
        }

        return channelsForIdentifiers;
    }

    @Override
    public PrefixedUnit getSingleDataUnit(String type)
    {
        return spectroscopySource.getSingleDataUnit();
    }

    @Override
    public Map<String, Channel1D> getChannels(String type)
    {
        Map<String, Channel1D> map = new LinkedHashMap<>();
        List<Channel1D> channels = spectroscopySource.getChannels();

        for(Channel1D channel : channels)
        {
            map.put(channel.getIdentifier(), channel);
        }

        return map;
    }

    @Override
    public Map<String, Channel1DData> getChannelData(String type)
    {
        Map<String, Channel1D> channels = getChannels(type);
        Map<String, Channel1DData> dataMap = new LinkedHashMap<>();

        for (Entry<String, Channel1D> entry : channels.entrySet()) 
        {
            String key = entry.getKey();

            Channel1D channel = entry.getValue();
            Channel1DData basicData = channel.getChannelData();
            dataMap.put(key, basicData);
        }

        return dataMap;
    }

    @Override
    public Map<String, Channel1D> setChannelData(String type, Map<String, Channel1DData> dataMap)
    {
        Map<String, Channel1D> changedChannels = getChannels(type);

        for (Entry<String, Channel1D> entry : changedChannels.entrySet()) 
        {
            String key = entry.getKey();
            Channel1D channel = entry.getValue();
            changedChannels.put(key, channel);

            Channel1DData basicData = dataMap.get(key);
            if(basicData != null)
            {
                channel.setChannelData(basicData);           
            }
        }

        return changedChannels;
    }

    @Override
    public Map<String, Channel1D> transform(String type, Channel1DDataTransformation tr) 
    {
        Map<String, Channel1D> channelsForType = getChannels(type);

        for(Entry<String, Channel1D> entry : channelsForType.entrySet())
        {
            Channel1D channel = entry.getValue();
            channel.transform(tr);
        }        

        return channelsForType;
    }

    @Override
    public Map<String, Channel1D> transform(String type, Set<String> identifiers, Channel1DDataTransformation tr) 
    {
        Map<String, Channel1D> channelsForType = getChannels(type);
        Map<String, Channel1D> changedChannels = new LinkedHashMap<>();

        for(Entry<String, Channel1D> entry : channelsForType.entrySet())
        {
            Channel1D channel = entry.getValue();
            if(identifiers.contains(channel.getIdentifier()))
            {
                channel.transform(tr);
                changedChannels.put(entry.getKey(), channel);
            }
        }        

        return changedChannels;
    }

    @Override
    public Map<String, Channel1D> transform(String type, Set<String> identifiers, Channel1DDataInROITransformation tr, ROI roi, ROIRelativePosition position) 
    {
        Map<String, Channel1D> channelsForType = getChannels(type);
        Map<String, Channel1D> changedChannels = new LinkedHashMap<>();

        for(Entry<String, Channel1D> entry : channelsForType.entrySet())
        {
            Channel1D channel = entry.getValue();

            if(identifiers.contains(channel.getIdentifier()))
            {
                channel.transform(tr, roi, position);
                changedChannels.put(entry.getKey(), channel);
            }
        }

        return changedChannels;
    }

    @Override
    public ROI getROIUnion()
    {
        return new ROIComposite("All");
    }

    @Override
    public void setUndoSizeLimit(int sizeLimit)
    {
        undoManager.setSizeLimit(sizeLimit);
    }

    @Override
    public void pushCommand(String type, UndoableCommand command)
    {
        undoManager.push(type, command);
    }

    @Override
    public boolean canBeRedone(String type)
    {
        return undoManager.canBeRedone(type);
    }

    @Override
    public void redo(String type)
    {
        undoManager.redo(type);
    }

    @Override
    public boolean canBeUndone(String type)
    {
        return undoManager.canBeUndone(type);
    }

    @Override
    public void undo(String type)
    {
        undoManager.undo(type);
    }

    @Override
    public CommandIdentifier getCommandToRedoCompundIdentifier(String type)
    {
        return undoManager.getCommandToRedoCompundIdentifier(type);
    }

    @Override
    public CommandIdentifier getCommandToUndoCompundIdentifier(String type) 
    {
        return undoManager.getCommandToUndoCompundIdentifier(type);
    }

    @Override
    public List<String> getAllTypes() 
    {
        return Collections.singletonList(RECORDED_CURVE);
    }
}
