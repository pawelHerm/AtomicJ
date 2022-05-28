
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

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DCollection;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
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


public class SpectroscopyAveragedProcessedResource implements SpectroscopyProcessedResource
{
    public static final String AVERAGED_POINTWISE_MODULUS = "Avaraged pointwise modulus";
    public static final String AVERAGED_RECORDED_CURVE = "Avaraged recorded curve";
    public static final String AVERAGED_INDENTATION = "Avaraged indentatation";

    private final File defaultOutputFile;
    private final String shortName;
    private final String longName;

    private final UndoManager undoManager = new UndoManager(2);

    private final MetaMap<String, Object, DistanceMeasurementDrawable> distanceMeasurements = new MetaMap<>();
    //meta map would be better here
    private final MultiMap<String, Channel1D> channelsTypeMap = new MultiMap<>();
    private final MetaMap<String, Object, Integer> channelGroupNextIdNumbers = new MetaMap<>();

    public SpectroscopyAveragedProcessedResource(File defaultOutPutFile, String longName, String shortName, Channel1DCollection recordedCurve, Channel1DCollection indentationCurve,
            Channel1DCollection modulusCurve)
    {
        this.defaultOutputFile = defaultOutPutFile;
        this.shortName = shortName;
        this.longName =  longName;

        this.channelsTypeMap.putAll(AVERAGED_INDENTATION, recordedCurve.getChannels());
        this.channelsTypeMap.putAll(AVERAGED_INDENTATION, indentationCurve.getChannels());
        this.channelsTypeMap.putAll(AVERAGED_POINTWISE_MODULUS, modulusCurve.getChannels());

        registerChannelGroupTags();

    }

    public SpectroscopyAveragedProcessedResource(SpectroscopyAveragedProcessedResource that)
    {
        this.defaultOutputFile = that.defaultOutputFile;
        this.longName = that.getLongName();
        this.shortName = that.getShortName();
    }

    private void registerChannelGroupTags()
    {
        for(Entry<String, List<Channel1D>> outerEntry : channelsTypeMap.entrySet())
        {
            String type = outerEntry.getKey();
            List<Channel1D> channels = outerEntry.getValue();

            for(Channel1D ch : channels)
            {
                registerChannelGroupTags(type, ch);        
            }
        }
    }

    private void registerChannelGroupTags(String type, Channel1D ch)
    {
        ChannelGroupTag groupTag = ch.getGroupTag();
        if(groupTag == null)
        {
            return;
        }

        Object groupId = groupTag.getGroupId();
        int index = groupTag.getIndex();

        Integer nextIndex = channelGroupNextIdNumbers.containsKeyPair(type, groupId) ? Math.max(index + 1, channelGroupNextIdNumbers.get(type, groupId)) : Integer.valueOf(index + 1);
        channelGroupNextIdNumbers.put(type, groupId, nextIndex);
    }

    @Override
    public PrefixedUnit getSingleDataUnit(String type)
    {
        Map<String, Channel1D> channels = getChannels(type);

        if(channels.isEmpty())
        {
            return null;
        }

        Iterator<Channel1D> it = channels.values().iterator();
        PrefixedUnit unit = it.next().getYQuantity().getUnit();

        while(it.hasNext())
        {
            PrefixedUnit currentUnit = it.next().getYQuantity().getUnit();
            if(!Objects.equals(unit, currentUnit))
            {
                return null;
            }
        }

        return unit;
    }

    @Override
    public boolean isCorrespondingMapPositionKnown()
    {
        return false;
    }

    @Override
    public String getLongName()
    {
        return longName;
    }

    @Override
    public String getShortName() 
    {
        return shortName;
    }

    @Override 
    public String toString()
    {
        return longName;
    }

    @Override
    public File getDefaultOutputLocation()
    {
        return defaultOutputFile;
    }

    @Override
    public boolean containsChannelsFromSource(ChannelSource source)
    {
        return false;
    }

    public SpectroscopyAveragedProcessedResource copy()
    {
        SpectroscopyAveragedProcessedResource copy = new SpectroscopyAveragedProcessedResource(this);
        return copy;
    }

    @Override
    public SimpleSpectroscopySource getSource()
    {
        return null;
    }

    @Override
    public List<SampleCollection> getSampleCollectionsRawData()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        for(Channel1D channel : channelsTypeMap.allValues())
        {
            samples.putAll(channel.getSamples());
        }

        SampleCollection collection = new StandardSampleCollection(samples, shortName, shortName, defaultOutputFile);
        collection.setKeysIncluded(true);

        return Collections.singletonList(collection);
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {  
        return getSampleCollectionsRawData();
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
        Map<Object, DistanceMeasurementDrawable> measurementsForType = distanceMeasurements.get(type);         
        return new LinkedHashMap<>(measurementsForType);
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
        Map<String, String> titles = Collections.singletonMap(AbstractResource.RESOURCE_NAME, getShortName());    

        return titles;
    }

    @Override
    public List<String> getAllTypes()
    {
        List<String> types = new ArrayList<>(channelsTypeMap.keySet());

        return types;
    }

    @Override
    public Set<String> getIdentifiers(String type)
    {
        Set<String> identifiers = new LinkedHashSet<>();
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {
            identifiers.add(ch.getIdentifier());
        }

        return identifiers;
    }

    @Override
    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {
            if(filter.accepts(ch))
            {
                identifiers.add(ch.getIdentifier());
            }
        }

        return identifiers;
    }

    @Override
    public Set<String> getIdentifiersForAllTypes()
    {
        Set<String> identifiers = new LinkedHashSet<>();
        List<Channel1D> channels = this.channelsTypeMap.allValues();

        for(Channel1D ch : channels)
        {
            identifiers.add(ch.getIdentifier());
        }

        return identifiers;
    }

    //returns a map with values being those channels whose identifiers are in the set 'identifiers'. The keys are the corresponding identifiers
    //A multimap is returned, because this TypeModelManager may contain two different sources, with channels of the same identifier
    @Override
    public MultiMap<String, Channel1D> getChannelsForIdentifiers(Set<String> identifiers)
    {
        MultiMap<String, Channel1D> channelsForIdentifiers = new MultiMap<>();
        List<Channel1D> channels = this.channelsTypeMap.allValues();

        for(Channel1D channel : channels)
        {
            String identifier = channel.getIdentifier();
            if(identifiers.contains(identifier))
            {
                channelsForIdentifiers.put(identifier, channel);
            }
        }

        return channelsForIdentifiers;
    }


    @Override
    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel1D> filter)
    {
        List<Channel1D> channels = this.channelsTypeMap.allValues();
        Set<String> filteredIdentifiers = new LinkedHashSet<>();

        for(Channel1D channel : channels)
        {
            if(filter.accepts(channel))
            {
                filteredIdentifiers.add(channel.getIdentifier());
            }
        }

        return filteredIdentifiers;
    }

    @Override
    public Map<String, Channel1D> getChannels(String type)
    {
        Map<String, Channel1D> channelMap = new LinkedHashMap<>();
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {            
            channelMap.put(ch.getIdentifier(), ch);
        }

        return channelMap;
    }

    @Override
    public Channel1D getChannel(String type, Object identifier)
    {
        return getChannels(type).get(identifier);
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
    public ChannelGroupTag  getNextGroupMemberIdentity(String type, Object groupKey)
    {
        int index = channelGroupNextIdNumbers.containsKeyPair(type, groupKey) ? channelGroupNextIdNumbers.get(type, groupKey): 0;
        return new ChannelGroupTag(groupKey, index);
    }

    @Override
    public Point2D getValidItemPosition(String type, String channelIdentifier,
            int itemIndex, Point2D dataPoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValidPosition(String type, String channelIdentifier,
            int itemIndex, Point2D dataPoint) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void itemMoved(String selectedType, Channel1D channel, int itemIndex,
            double[] newValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelTranslated(Channel1D channel) {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<Channel1DDataTransformation> getFixContactXTransformationIfPossible() {
        return Optional.empty();
    }

    @Override
    public Optional<Channel1DDataTransformation> getFixContactYTransformationIfPossible() {
        return Optional.empty();
    }

    @Override
    public Optional<Channel1DDataTransformation> getFixContactAllAxesTransformationIfPossible() {
        return Optional.empty();
    }

    @Override
    public Optional<Channel1DDataTransformation> getTransformationToForceSeparationIfPossible() {
        return Optional.empty();
    }

    @Override
    public void addChannel(String type, Channel1D channel) 
    {
        this.channelsTypeMap.put(type, channel);
        registerChannelGroupTags();
    }

    @Override
    public void removeChannel(String type, Channel1D channel)
    {
        this.channelsTypeMap.remove(type, channel);
    }

}
