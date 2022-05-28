
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
import java.awt.geom.Line2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2D;
import atomicJ.data.DataAxis1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSample;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.rois.ROI;
import atomicJ.readers.regularImage.Channel2DSourceMetadata;
import atomicJ.readers.regularImage.DummyDensityMetadata;
import atomicJ.sources.ChannelGroup.ROISamplesResult;
import atomicJ.utilities.Validation;


public abstract class AbstractChannel2DSource<E extends Channel2D> extends AbstractChannelSource<E> implements Channel2DSource<E>
{
    private Channel2DSourceMetadata metadata = DummyDensityMetadata.getInstance();

    //we need this list of identifiers, as it keeps their order. The map channels is not enough.
    private final List<String> identifiers = new ArrayList<>();
    private Map<String, E> channels = new LinkedHashMap<>();

    public AbstractChannel2DSource(File f)
    {
        super(f);	
    }

    public AbstractChannel2DSource(String pathname)
    {
        super(pathname);
    }

    public AbstractChannel2DSource(Channel2DSourceMetadata metadata, File f, String shortName, String longName)
    {
        super(f, shortName, longName);
        this.metadata = metadata;
    }

    public AbstractChannel2DSource(Channel2DSourceMetadata metadata, String pathname, String shortName, String longName)
    {
        super(pathname, shortName, longName);
        this.metadata = metadata;
    }

    public AbstractChannel2DSource(AbstractChannel2DSource<E> that)
    {
        this(that, that.channels.keySet());     
    }

    public AbstractChannel2DSource(AbstractChannel2DSource<E> that, Collection<String> identifiers)
    {
        super(that);

        this.metadata = metadata.copyIfNecessary();

        List<E> channelsOldCopied = that.getChannelCopies(identifiers);
        for(E ch : channelsOldCopied)
        {
            String id = ch.getIdentifier();
            this.identifiers.add(id);
            this.channels.put(id, ch);
        }
    }

    @Override
    public Channel2DSourceMetadata getMetadata()
    {
        return metadata;
    }


    @Override
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod)
    {
        boolean interpolated = true;

        for(E channel : getChannels())
        {
            boolean interpolationFunctionBuilt = channel.isInterpolationPreparationNecessary(interpolationMethod);

            interpolated = interpolated && interpolationFunctionBuilt;

            if(!interpolated)
            {
                break;
            }
        }
        return interpolated;
    }

    @Override
    public List<E> getChannels()
    {
        List<E> channelList = new ArrayList<>();

        for(String id : identifiers)
        {
            E channel = channels.get(id);           
            channelList.add(channel);
        }

        return channelList;
    }

    @Override
    public List<E> getChannels(Collection<String> identifiers)
    {
        List<E> channelsForIdentifiers = new ArrayList<>();

        for(String identifier : identifiers)
        {
            E channel = this.channels.get(identifier);
            if(channel != null)
            {
                channelsForIdentifiers.add(channel);
            }
        }

        return channelsForIdentifiers;
    }

    @Override
    public abstract List<E> getChannelCopies();

    @Override
    public abstract List<E> getChannelCopies(Collection<String> identiftiers);


    @Override
    public E getChannel(String identifier)
    {
        return channels.get(identifier);
    }

    @Override
    public void removeChannel(String identifier)
    {
        Validation.requireNonNullParameterName(identifier, "identifier");

        identifiers.remove(identifier);
        channels.remove(identifier);
    }

    @Override
    public void retainAll(Collection<String> identifiersRetained)
    {
        Validation.requireNonNullParameterName(identifiersRetained, "identifiersRetained");

        Set<String> keySet = channels.keySet();
        keySet.retainAll(identifiersRetained);

        Iterator<String> iterator = this.identifiers.iterator();

        while(iterator.hasNext())
        {
            String id = iterator.next();

            if(!identifiersRetained.contains(id))
            {
                iterator.remove();
            }
        }
    }

    public void insertChannel(E channel, int position)
    {        
        if(position < 0)
        {
            throw new IllegalArgumentException("Negative 'channel'");
        }

        if(position > channels.size())
        {
            throw new IllegalArgumentException("'index' must be smaller or equal the numbe of channels");
        }

        String identifier = channel.getIdentifier();

        identifiers.remove(identifier);
        identifiers.add(position, identifier);

        channels.put(identifier, channel);
    }

    @Override
    public void addChannel(E channel)
    {
        Validation.requireNonNullParameterName(channel, "channel");

        String identifier = channel.getIdentifier();

        //we want identifier of the added channel to be at the end of the list of identifiers
        identifiers.remove(identifier);
        identifiers.add(identifier);

        channels.put(identifier, channel);
    }

    @Override
    public void setChannels(Collection<E> channelsNew)
    {
        this.identifiers.clear();
        this.channels = new LinkedHashMap<>();

        for(E channel : channelsNew)
        {
            String identifier = channel.getIdentifier();
            this.identifiers.add(identifier);
            this.channels.put(identifier, channel);
        }	      
    }

    public int getChannelPosition(String identifier)
    {
        return identifiers.indexOf(identifier);
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
    public Map<String, QuantitativeSample> getSamples(boolean includeCoordinates)
    {		
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        ROITagger<Object> roiTagger = new UniversalKeyTagger(this);
        List<ChannelGroup<String, Object>> chanelGroups =  EqualGridChannelGroup.getEqualDomainChannelGroups(roiTagger, getChannels(), IdentifierTagger.getInstance());

        for(ChannelGroup<String, Object> channelGroup : chanelGroups)
        {
            final List<Channel2D> channels = channelGroup.getChannels();

            if(includeCoordinates)
            {
                samples.putAll(channelGroup.getCoordinateSamples());
            }       

            for(Channel2D channel: channels)
            {
                String channelIdentifier = channel.getIdentifier();
                QuantitativeSample sample = channel.getZSample();

                //we have to change the sample key, as the grid channel does not know about the source it is contained in
                //and the name of the source should be used as the sample's key
                samples.put(channelIdentifier, new StandardSample(sample.getMagnitudes(), getShortName(), sample.getQuantity(), getShortName()));
            }
        }

        return samples;
    }

    @Override
    public QuantitativeSample getSample(String identifier)
    {
        Channel2D channel = getChannel(identifier);
        QuantitativeSample sample = (channel != null) ? channel.getZSample(): null;

        return sample;
    }


    @Override
    public Map<String, Map<Object, QuantitativeSample>> getROISamples(Collection<? extends ROI> rois, boolean includeCoordinates)
    {        
        return getROISamplesForChannels(rois, getChannels(), includeCoordinates);
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> getROISamples(Collection<? extends ROI> rois, List<String> identifiers, boolean includeCoordinates)
    {        
        return getROISamplesForChannels(rois, getChannels(identifiers), includeCoordinates);
    }

    //called after duplication
    private Map<String, Map<Object, QuantitativeSample>> getROISamplesForChannels(Collection<? extends ROI> rois, final List<E> channels, boolean includeCoordinates)
    {        
        Map<String, Map<Object, QuantitativeSample>> samples = new LinkedHashMap<>();

        ROITagger<Object> roiTagger = new UniversalKeyTagger(this);
        List<ChannelGroup<String, Object>> channelGroups = EqualGridChannelGroup.getEqualDomainChannelGroups(roiTagger, channels, IdentifierTagger.getInstance());
        int channelGroupCount = channelGroups.size();

        for(int i = 0; i < channelGroupCount;i++)
        {
            ChannelGroup<String, Object> channelGroup = channelGroups.get(i);
            ROISamplesResult<String, Object> roiSamples = channelGroup.getROISamples(rois, includeCoordinates);
            samples.putAll(roiSamples.getCalculatedCoordinateSamples().getMapCopy());
            samples.putAll(roiSamples.getValueSmples().getMapCopy());
        }

        return samples;
    }

    //is ok
    @Override
    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, String identifier)
    {
        E channel = getChannel(identifier);

        Map<Object, QuantitativeSample> samples = (channel != null) ? channel.getROISamples(rois, getSampleTag()) : new LinkedHashMap<>();

        return samples;
    }

    @Override
    public String getUniversalROIKey(Object roiKey)
    {
        String sampleKey = roiKey + getSampleTag();
        return sampleKey;
    }

    protected String getSampleTag()
    {
        String sampleTag = " (" + getShortName() + ")";

        return sampleTag;
    }

    @Override
    public List<String> getIdentifiers()
    {
        List<String> channelIdentifiers = new ArrayList<>(this.identifiers);

        return channelIdentifiers;
    }

    @Override
    public Map<String, ChannelSectionLine> getVerticalCrossSections(double level, Object key, String name, String identifier) 
    {        
        E channel = getChannel(identifier);
        Range yRange = channel.getYRange();

        Line2D profile = new Line2D.Double(level, yRange.getLowerBound(), level, yRange.getUpperBound());

        Map<String, ChannelSectionLine> crossSections = getCrossSections(profile, key, name, identifier);

        return crossSections;
    }

    @Override
    public Map<String, ChannelSectionLine> getHorizontalCrossSections(double level, Object key, String name, String identifier) 
    {
        E channel = getChannel(identifier);
        Range xRange = channel.getXRange();

        Line2D profile = new Line2D.Double(xRange.getLowerBound(), level, xRange.getUpperBound(), level);

        Map<String, ChannelSectionLine> crossSections =  getCrossSections(profile, key, name, identifier);

        return crossSections;
    }

    @Override
    public List<ChannelSectionLine> getHorizontalCrossSections(DataAxis1D verticalAxis, Object key, String name, String identifier)
    {
        List<ChannelSectionLine> crossSections = new ArrayList<>();

        int indexCount = verticalAxis.getIndexCount();

        for(int i = 0; i<indexCount; i++)
        {
            double level = verticalAxis.getArgumentVal(i);
            ChannelSectionLine crossSection = getHorizontalCrossSections(level, key, name, identifier).values().iterator().next();
            crossSections.add(crossSection);
        }

        return crossSections;
    }

    @Override
    public List<ChannelSectionLine> getVerticalCrossSections(DataAxis1D horizontalAxis, Object key, String name, String type)
    {
        List<ChannelSectionLine> crossSections = new ArrayList<>();

        int indexCount = horizontalAxis.getIndexCount();

        for(int i = 0; i < indexCount; i++)
        {
            double level = horizontalAxis.getArgumentVal(i);
            ChannelSectionLine crossSection = getVerticalCrossSections(level, key, name, type).values().iterator().next();
            crossSections.add(crossSection);
        }
        return crossSections;
    }

    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Shape profile, Object crossSectionKey, String name, String identifier) 
    {
        Map<String, ChannelSectionLine> crossSections = new LinkedHashMap<>();		

        E channel = getChannel(identifier);

        if(channel != null)
        {			
            String channelIdentifier = channel.getIdentifier(); 

            Quantity dQuantity = channel.getXQuantity();
            Quantity zQuantity = channel.getZQuantity();

            ChannelSectionLine profileResource = new ChannelSectionLine(profile, channel, dQuantity, zQuantity, crossSectionKey, name);

            crossSections.put(channelIdentifier, profileResource);
        }


        return crossSections;
    }

    @Override
    public Map<String, ChannelSectionLine> getHorizontalCrossSections(double level, Object key, String name) 
    {               
        Map<String, ChannelSectionLine> allSectionLines = new LinkedHashMap<>();

        for(Channel2D channel : getChannels())
        {
            Range xRange = channel.getXRange();
            Line2D profile = new Line2D.Double(xRange.getLowerBound(), level, xRange.getUpperBound(), level);

            String identifier = channel.getIdentifier();     

            Quantity dQuantity = channel.getXQuantity();
            Quantity zQuantity = channel.getZQuantity();

            ChannelSectionLine sectionLine = new ChannelSectionLine(profile, channel, dQuantity, zQuantity, key, name);

            allSectionLines.put(identifier, sectionLine);
        }

        return allSectionLines;
    }

    @Override
    public Map<String, ChannelSectionLine> getVerticalCrossSections(double level, Object key, String name) 
    {
        Map<String, ChannelSectionLine> allSectionLines = new LinkedHashMap<>();

        for(Channel2D channel : getChannels())
        {
            Range yRange = channel.getYRange();
            Line2D profile = new Line2D.Double(level, yRange.getLowerBound(), level, yRange.getUpperBound());

            String identifier = channel.getIdentifier();     

            Quantity dQuantity = channel.getXQuantity();
            Quantity zQuantity = channel.getZQuantity();

            ChannelSectionLine sectionLine = new ChannelSectionLine(profile, channel, dQuantity, zQuantity, key, name);

            allSectionLines.put(identifier, sectionLine);
        }

        return allSectionLines;
    }

    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Shape profile, Object crossSectionKey, String crossSectionName) 
    {
        List<E> channels = getChannels();

        return getCrossSections(channels, profile, crossSectionKey, crossSectionName);
    }

    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Shape profile, Object crossSectionKey, String crossSectionName, Set<String> identifiers) 
    {
        List<E> channels = getChannels(identifiers);

        return getCrossSections(channels, profile, crossSectionKey, crossSectionName);
    }

    private Map<String, ChannelSectionLine> getCrossSections(List<E> channels, Shape profile, Object crossSectionKey, String crossSectionName) 
    {
        Map<String, ChannelSectionLine> crossSections = new LinkedHashMap<>();      

        int channelCount = channels.size();

        for(int i = 0; i<channelCount; i++)
        {
            E channel = channels.get(i);

            if(channel != null)
            {           
                String channelIdentifier = channel.getIdentifier();     

                Quantity dQuantity = channel.getXQuantity();
                Quantity zQuantity = channel.getZQuantity();

                ChannelSectionLine sectionLine = new ChannelSectionLine(profile, channel, dQuantity, zQuantity, crossSectionKey, crossSectionName);

                crossSections.put(channelIdentifier, sectionLine);
            }
        }

        return crossSections;
    }
}
