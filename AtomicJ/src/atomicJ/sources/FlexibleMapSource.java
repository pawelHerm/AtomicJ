
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

package atomicJ.sources;


import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.analysis.NumericalSpectroscopyProcessingResults;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.SimpleProcessedPackFunction;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.Channel2DStandard;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.regularImage.Channel2DSourceMetadata;


public class FlexibleMapSource extends AbstractChannel2DSource<Channel2D> implements MapSource<Channel2D>
{
    private final List<SimpleSpectroscopySource> simpleSources;

    private final Map<Point2D, ProcessedSpectroscopyPack> processedPacks = new LinkedHashMap<>();

    private ReadingPack<ImageSource> mapAreaImageInfo;
    private final Quantity xQuantity;
    private final Quantity yQuantity;
    private final ChannelDomainIdentifier dataDomain;

    private boolean processed = false; // if it is processed - at least one position is filled
    private boolean isSealed = false;

    private final Map<ProcessedPackFunction<? super ProcessedSpectroscopyPack>, String> channelPackFunctionMap = new LinkedHashMap<>();

    public FlexibleMapSource(File f, List<SimpleSpectroscopySource> simpleSources, ChannelDomainIdentifier probingDensity, Quantity xAxisQuantity, Quantity yAxisQuantity)
    {
        super(f);	

        this.simpleSources = simpleSources;
        this.dataDomain = probingDensity;
        this.xQuantity = xAxisQuantity;
        this.yQuantity = yAxisQuantity;

        initSourceList();
    }

    public FlexibleMapSource(String pathname, List<SimpleSpectroscopySource> simpleSources, ChannelDomainIdentifier probingDensity, Quantity xAxisQuantity, Quantity yAxisQuantity)
    {
        super(pathname);

        this.simpleSources = simpleSources;
        this.dataDomain = probingDensity;
        this.xQuantity = xAxisQuantity;
        this.yQuantity = yAxisQuantity;

        initSourceList();
    }

    public FlexibleMapSource(File f, Channel2DSourceMetadata metadata, List<SimpleSpectroscopySource> simpleSources, ChannelDomainIdentifier probingDensity, String shortName, String longName, Quantity xAxisQuantity, Quantity yAxisQuantity)
    {
        super(metadata, f, shortName, longName);

        this.simpleSources = simpleSources;
        this.dataDomain = probingDensity;
        this.xQuantity = xAxisQuantity;
        this.yQuantity = yAxisQuantity;

        initSourceList();
    }

    public FlexibleMapSource(String pathname, Channel2DSourceMetadata metadata, List<SimpleSpectroscopySource> simpleSources, ChannelDomainIdentifier probingDensity, String shortName, String longName, Quantity xAxisQuantity,  Quantity yAxisQuantity)
    {
        super(metadata, pathname, shortName, longName);

        this.simpleSources = simpleSources;
        this.dataDomain = probingDensity;
        this.xQuantity = xAxisQuantity;
        this.yQuantity = yAxisQuantity;

        initSourceList();
    }

    public FlexibleMapSource(FlexibleMapSource source)
    {
        super(source);

        this.dataDomain = source.dataDomain;
        this.xQuantity = source.xQuantity;
        this.yQuantity = source.yQuantity;
        this.simpleSources = copyAndInitSimpleSources(source.simpleSources);		
        this.processedPacks.putAll(new LinkedHashMap<>(source.processedPacks));
        this.mapAreaImageInfo = source.mapAreaImageInfo;
    }

    @Override
    public Channel2D duplicateChannel(String identifier)
    {
        Channel2D channel = getChannel(identifier);
        Channel2D channelCopy = channel.duplicate();

        int indexNew = getChannelPosition(identifier) + 1;
        insertChannel(channelCopy, indexNew);

        return channelCopy;
    }

    @Override
    public Channel2D duplicateChannel(String identifier, String identifierNew)
    {
        Channel2D channel = getChannel(identifier);
        Channel2D channelCopy = channel.duplicate(identifierNew);

        int indexNew = getChannelPosition(identifier) + 1;
        insertChannel(channelCopy, indexNew);

        return channelCopy;
    }

    @Override
    public ReadingPack<ImageSource> getMapAreaImageReadingPack()
    {
        return mapAreaImageInfo;
    }

    @Override
    public boolean isMapAreaImagesAvailable()
    {
        boolean available = this.mapAreaImageInfo != null;
        return available;
    }

    @Override
    public void setMapAreaImageReadingPack(ReadingPack<ImageSource> mapAreaImageInfo)
    {
        this.mapAreaImageInfo = mapAreaImageInfo;
    }

    @Override
    public void replaceSpectroscopySource(SimpleSpectroscopySource source, int index)
    {
        SimpleSpectroscopySource oldSource = simpleSources.get(index);
        if(oldSource != null)
        {
            oldSource.setForceMap(null);
        }

        simpleSources.set(index, source);
        source.setForceMap(this);
        source.setMapPosition(index);
        processedPacks.remove(source.getRecordingPoint());
    }

    private List<SimpleSpectroscopySource> copyAndInitSimpleSources(List<SimpleSpectroscopySource> sourcesOld)
    {
        List<SimpleSpectroscopySource> sourcesNew =new ArrayList<>();

        int n = sourcesOld.size();

        for(int i = 0; i<n; i++)
        {
            SimpleSpectroscopySource sourceOld = sourcesOld.get(i);
            SimpleSpectroscopySource sourceNew = sourceOld.copy();
            sourceNew.setForceMap(this);
            sourceNew.setMapPosition(i);
            sourcesNew.add(sourceNew);
        }
        return sourcesNew;
    }


    private void initSourceList()
    {
        int n = simpleSources.size();

        for(int i = 0; i<n; i++)
        {
            SimpleSpectroscopySource source = simpleSources.get(i);
            source.setForceMap(this);
            source.setMapPosition(i);
        }
    }

    @Override
    public boolean isRecordedAsGrid()
    {
        return false;
    }

    @Override
    public boolean isProcessed()
    {
        return processed;
    }

    @Override
    public boolean isProcessedAsFullGrid()
    {
        return false;
    }

    private void checkIfIsProcessed()
    {
        boolean processed = false;

        for(ProcessedSpectroscopyPack pack : processedPacks.values())
        {
            processed = processed || (pack != null);

            if(processed)
            {
                this.processed = processed;
                return;
            }
        }      

        this.processed = processed;
    }

    @Override
    public ProcessedSpectroscopyPack getProcessedPack(int i)
    {
        List<ProcessedSpectroscopyPack> packs = new ArrayList<>(processedPacks.values());
        return packs.get(i);
    }

    @Override
    public SimpleSpectroscopySource getSimpleSpectroscopySource(Point2D p)
    {
        SimpleSpectroscopySource source = null;

        ProcessedSpectroscopyPack pack = getProcessedPack(p);
        if(pack != null)
        {
            source = pack.getSource();
        }

        return source;	
    }

    @Override
    public ProcessedSpectroscopyPack getProcessedPack(Point2D p)
    {
        double minDistance = Double.POSITIVE_INFINITY;
        ProcessedSpectroscopyPack processedPack = null;

        for(Entry<Point2D, ProcessedSpectroscopyPack> entry: processedPacks.entrySet())
        {
            Point2D packPoint = entry.getKey();
            double dist = p.distance(packPoint);
            if(dist<minDistance)
            {
                minDistance = dist;
                processedPack = entry.getValue();
            }
        }

        return processedPack;

    }

    @Override
    public Map<Point2D, ProcessedSpectroscopyPack> getProcessedPacksMap()
    {
        return processedPacks;
    }

    @Override
    public void registerProcessedPack(ProcessedSpectroscopyPack pack)
    {
        SimpleSpectroscopySource source = pack.getSource();

        Point2D p = source.getRecordingPoint();

        if(p != null)
        {
            processedPacks.put(p, pack);
        }
    }

    @Override
    public ProcessedPackReplacementResults replaceProcessedPacks(List<ProcessedSpectroscopyPack> packsToReplace)
    {     
        updatePacks(packsToReplace);

        Map<String, Channel2D> updatedChannels = updateChannels(packsToReplace);
        Map<String, Channel2D> addedChannels = addPreviouslyAbsentChannelsFromReplacedPacks(packsToReplace);

        ProcessedPackReplacementResults results = new ProcessedPackReplacementResults(addedChannels, updatedChannels);

        return results;
    }

    private Map<String, Channel2D> updateChannels(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        Map<String, Channel2D> changedChannels = new LinkedHashMap<>();

        for(Entry<ProcessedPackFunction<? super ProcessedSpectroscopyPack>, String> entry : channelPackFunctionMap.entrySet())
        {
            ProcessedPackFunction<? super ProcessedSpectroscopyPack> function = entry.getKey();

            String id = entry.getValue();         
            String universalId = getChannelUniversalIdentifier(id);

            Channel2D channel = getChannel(id);

            if(channel != null)
            {
                channel.transform(new ProcessedPackTransformation(packsToReplace, function));
                changedChannels.put(universalId, channel);
            }          
        }

        return changedChannels;
    }

    private void updatePacks(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        for(ProcessedSpectroscopyPack pack : packsToReplace)
        {
            SimpleSpectroscopySource source = pack.getSource();

            Point2D p = source.getRecordingPoint();

            if(p != null)
            {
                ProcessedSpectroscopyPack packOld = processedPacks.get(p);
                processedPacks.put(p, pack);

                if(packOld != null)
                {
                    SimpleSpectroscopySource sourceOld = packOld.getSource();
                    int indexReplaced = sourceOld.getMapPosition();

                    simpleSources.set(indexReplaced, source);
                    sourceOld.setForceMap(null);

                    source.setMapPosition(indexReplaced);
                }

                source.setForceMap(this);
            }
        }
    }


    private Map<String, Channel2D> addPreviouslyAbsentChannelsFromReplacedPacks(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        Map<String, Channel2D> addedChannels = new LinkedHashMap<>();
        Set<ProcessedPackFunction<? super ProcessedSpectroscopyPack>> previouslyAbsentPackFunctions = getSpecialPackFunctionsPreviouslyAbsent(packsToReplace);

        for(ProcessedPackFunction<? super ProcessedSpectroscopyPack> f : previouslyAbsentPackFunctions)
        {
            Quantity zQuantity = f.getEvaluatedQuantity();

            double[][] data = getXYZData(f); 
            Channel2DData channelData = new FlexibleChannel2DData(data, dataDomain, xQuantity, yQuantity, zQuantity);
            Channel2D channel = new Channel2DStandard(channelData, zQuantity.getName());
            addChannel(channel);

            String id = channel.getIdentifier();
            channelPackFunctionMap.put(f, id);

            String universalId = getChannelUniversalIdentifier(id);
            addedChannels.put(universalId, channel);
        }

        return addedChannels;
    }


    private Set<ProcessedPackFunction<? super ProcessedSpectroscopyPack>> getSpecialPackFunctionsPreviouslyAbsent(List<ProcessedSpectroscopyPack> packsToReplace)
    {
        Set<ProcessedPackFunction<? super ProcessedSpectroscopyPack>> newSpecialFunctions = new LinkedHashSet<>();

        for(ProcessedSpectroscopyPack pack : packsToReplace)
        {
            if(pack != null)
            {
                newSpecialFunctions.addAll(pack.getSpecialFunctions());                
            }
        }

        newSpecialFunctions.removeAll(channelPackFunctionMap.keySet());

        return newSpecialFunctions;
    }

    @Override
    public Set<ProcessedSpectroscopyPack> getProcessedPacks()
    {
        Set<ProcessedSpectroscopyPack> packs = new LinkedHashSet<>(processedPacks.values());
        return packs;
    }

    @Override
    public Set<ProcessedSpectroscopyPack> getProcessedPacks(ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getProcessedPacks();
        }

        Set<ProcessedSpectroscopyPack> packs = new HashSet<>();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(Entry<Point2D, ProcessedSpectroscopyPack> entry: processedPacks.entrySet())
            {
                Point2D p = entry.getKey();
                if(roi.contains(p))
                {
                    ProcessedSpectroscopyPack pack = entry.getValue();
                    packs.add(pack);
                }                   
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(Entry<Point2D, ProcessedSpectroscopyPack> entry: processedPacks.entrySet())
            {
                Point2D p = entry.getKey();
                if(!roi.contains(p))
                {
                    ProcessedSpectroscopyPack pack = entry.getValue();
                    packs.add(pack);
                }                   
            }
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized ROIRelativePosition " + position);
        }

        return packs;
    }


    @Override
    public List<SimpleSpectroscopySource> getSimpleSources(ROI roi, ROIRelativePosition position) 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return new ArrayList<>(simpleSources);
        }
        else if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(SimpleSpectroscopySource source : simpleSources)
            {
                Point2D p = source.getRecordingPoint();
                if(roi.contains(p))
                {
                    sources.add(source);
                }   
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(SimpleSpectroscopySource source : simpleSources)
            {
                Point2D p = source.getRecordingPoint();
                if(!roi.contains(p))
                {
                    sources.add(source);
                }   
            }
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized ROIRelativePosition " + position);
        }
        return sources;
    }

    @Override
    public double getProbingDensity()
    {
        return dataDomain.getDataDensity();
    }

    @Override
    public FlexibleMapSource copy() 
    {
        return new FlexibleMapSource(this);
    }

    //this method should be rewritten
    @Override
    public FlexibleMapSource copy(Collection<String> identifiers) 
    {
        return new FlexibleMapSource(this);
    }

    @Override
    public List<SimpleSpectroscopySource> getSimpleSources() 
    {
        return simpleSources;
    }

    public double[][] getXYZData(ProcessedPackFunction<? super ProcessedSpectroscopyPack> function) 
    {
        int n = processedPacks.size();

        double[] xs = new double[n];
        double[] ys = new double[n];
        double[] zs = new double[n];

        int i = 0;
        for(Entry<Point2D, ProcessedSpectroscopyPack> entry: processedPacks.entrySet())
        {
            ProcessedSpectroscopyPack pack = entry.getValue();
            Point2D p = entry.getKey();

            xs[i] = p.getX();
            ys[i] = p.getY();
            zs[i] = function.evaluate(pack);

            i++;
        }

        double[][] data = new double[][] {xs, ys, zs};

        return data;
    }

    public double[][] getXYZData(Shape shape, ROIRelativePosition position, ProcessedPackFunction<? super ProcessedSpectroscopyPack> function) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getXYZData(function);
        }

        List<ProcessedSpectroscopyPack> packsContained = new ArrayList<>();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(Entry<Point2D, ProcessedSpectroscopyPack> entry: processedPacks.entrySet())
            {
                ProcessedSpectroscopyPack pack = entry.getValue();
                Point2D p = entry.getKey();

                if(shape.contains(p))
                {
                    packsContained.add(pack);

                }       
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(Entry<Point2D, ProcessedSpectroscopyPack> entry: processedPacks.entrySet())
            {
                ProcessedSpectroscopyPack pack = entry.getValue();
                Point2D p = entry.getKey();

                if(!shape.contains(p))
                {
                    packsContained.add(pack);
                }       
            }
        }


        int n = packsContained.size();

        double[] xs = new double[n];
        double[] ys = new double[n];
        double[] zs = new double[n];

        for(int i = 0; i<n; i++)
        {
            ProcessedSpectroscopyPack pack = packsContained.get(i);
            Point2D p = pack.getSource().getRecordingPoint();

            xs[i] = p.getX();
            ys[i] = p.getY();
            zs[i] = function.evaluate(pack);
        }

        double[][] data = new double[][] {xs, ys, zs};

        return data;
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {
        Map<String, QuantitativeSample> samples = getSamples(true);

        SampleCollection collection = new StandardSampleCollection(samples, getShortName(), getShortName(), getDefaultOutputLocation());

        List<SampleCollection> collections = Collections.singletonList(collection);
        return collections;
    }	

    @Override
    public boolean isSealed()
    {
        return isSealed;
    }

    @Override
    public void seal()
    {
        isSealed = true;
        checkIfIsProcessed();
        initMapChannels();
    }	

    private void initMapChannels()
    {		
        List<Channel2D> channels = new ArrayList<>();

        List<ProcessedPackFunction<? super ProcessedSpectroscopyPack>> allFunctions = new ArrayList<>();
        allFunctions.addAll(Arrays.asList(SimpleProcessedPackFunction.values()));
        allFunctions.addAll(getAdditionalPacks());

        if(processed)
        {
            for(ProcessedPackFunction<? super ProcessedSpectroscopyPack> f: allFunctions)
            {
                Quantity zQuantity = f.getEvaluatedQuantity();

                double[][] data = getXYZData(f); 
                Channel2DData channelData = new FlexibleChannel2DData(data, dataDomain, xQuantity, yQuantity, zQuantity);
                Channel2D channel = new Channel2DStandard(channelData, zQuantity.getName());
                channels.add(channel);

                channelPackFunctionMap.put(f, channel.getIdentifier());
            }   
        }      

        setChannels(channels);
    }

    private List<ProcessedPackFunction<ProcessedSpectroscopyPack>> getAdditionalPacks()
    {
        List<ProcessedPackFunction<ProcessedSpectroscopyPack>> additionalFunctions = new ArrayList<>();

        for(ProcessedSpectroscopyPack pack : processedPacks.values())
        {
            additionalFunctions.addAll(pack.getSpecialFunctions());                
        }

        return additionalFunctions;
    }

    @Override
    public List<Channel2D> getChannelCopies()
    {
        Collection<Channel2D> channelsOriginal = getChannels();
        List<Channel2D> channelsCopied = new ArrayList<>();

        for(Channel2D channel : channelsOriginal)
        {
            channelsCopied.add(channel.getCopy());
        }

        return channelsCopied;
    }

    @Override
    public List<Channel2D> getChannelCopies(Collection<String> identifiers)
    {
        List<Channel2D> channelsForIdentifiers = new ArrayList<>();

        for(String identifier : identifiers)
        {
            Channel2D channel = getChannel(identifier);
            if(channel != null)
            {
                channelsForIdentifiers.add(channel);
            }
        }

        return channelsForIdentifiers;
    }

    @Override
    public Channel2D getChannel(ProcessedPackFunction<? super ProcessedSpectroscopyPack> f) 
    {
        Quantity zQuantity = f.getEvaluatedQuantity();
        double[][] data = getXYZData(f);    
        Channel2DData channelData = new FlexibleChannel2DData(data, dataDomain, xQuantity, yQuantity, zQuantity);
        Channel2D channel = new Channel2DStandard(channelData, zQuantity.getName());
        return channel;
    }

    @Override
    public Channel2D getChannel(ROI roi, ROIRelativePosition position, ProcessedPackFunction<? super ProcessedSpectroscopyPack> f) 
    {
        Quantity zQuantity = f.getEvaluatedQuantity();
        double[][] data = getXYZData(roi.getROIShape(), position, f);   
        Channel2DData channelData = new FlexibleChannel2DData(data, dataDomain, xQuantity, yQuantity, zQuantity);

        Channel2D channel = new Channel2DStandard(channelData, zQuantity.getName());
        return channel;
    }

    @Override
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod) 
    {
        return false;
    }

    @Override
    public Range getHeightRange()
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for(ProcessedSpectroscopyPack pack : processedPacks.values())
        {
            NumericalSpectroscopyProcessingResults result = pack.getResults();
            double contactHeight = result.getContactDisplacement();
            min = min > contactHeight ? contactHeight : min;
            max = max < contactHeight ? contactHeight : max;
        }

        Range range = new Range(min, max);

        return range;
    }
}
