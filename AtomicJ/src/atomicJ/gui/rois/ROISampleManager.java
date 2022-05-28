
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

package atomicJ.gui.rois;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSample;
import atomicJ.data.StandardSampleCollection;
import atomicJ.sources.Channel2DSource;
import atomicJ.utilities.CollectionsUtilities;
import atomicJ.utilities.MetaMap;


/*
 * This class is a collection of samples for various ROIs for a single DensitySource
 * The number of different samples for a source consisting of n channels, with p ROIs selected
 * usually equals (n + 2) x (p + 1), because we calculate a sample for each channel and each ROI, also for a
 * special "union roi", i.e. a union of all ROIs in the set-theoretical sense, and for two additional coordinate
 * "channels" (one for x-coordinates of the points inside the ROI, other for y-coordinates). Usually, 
 * because it really depends on the DensitySource getSampleForROI() method
 * 
 */

public class ROISampleManager
{
    private final MetaMap<String, Object, QuantitativeSample> samples = new MetaMap<>();

    private ROI roiUnion;
    private final Map<Object, ROI> rois;
    private final Channel2DSource<?> source;

    public ROISampleManager(Channel2DSource<?> densitySource)
    {
        this.roiUnion = new ROIComposite(densitySource.getUniversalROIKey("All"));
        this.source = densitySource;
        this.rois = new LinkedHashMap<>();

        samples.putAll(densitySource.getROISamples(rois.values(), true));      
    }

    public ROISampleManager(Channel2DSource<?> densitySource, Map<Object,? extends ROI> rois, boolean includeCoordinates)
    {
        this.source = densitySource;
        this.rois = new LinkedHashMap<>(rois);
        this.roiUnion = ROIUtilities.composeROIs(rois.values(), densitySource.getUniversalROIKey("All"));

        samples.putAll(densitySource.getROISamples(rois.values(), includeCoordinates));		
    }

    public ROISampleManager(Channel2DSource<?> sourceCopy, ROISampleManager that)
    {
        this.source = sourceCopy;
        this.rois = new LinkedHashMap<>(that.rois);
        this.roiUnion = ROIUtilities.composeROIs(rois.values(), source.getUniversalROIKey("All"));

        this.samples.putAll(that.samples);  
    }

    public ROISampleManager(ROISampleManager that)
    {
        this.source = that.source;
        this.rois = new LinkedHashMap<>(that.rois);
        this.roiUnion = ROIUtilities.composeROIs(rois.values(), source.getUniversalROIKey("All"));

        this.samples.putAll(that.samples);  
    }

    public List<ROI> getAllSamplesROIs()
    {
        List<ROI> roisAll = new ArrayList<>();
        roisAll.add(roiUnion);
        roisAll.addAll(this.rois.values());

        return roisAll;
    }

    public Channel2DSource<?> getDensitySource()
    {
        return source;
    }

    public String getShortName()
    {
        return source.getShortName();
    }

    public List<String> getSampleTypes() 
    {
        Set<String> types = samples.keySet();
        return new ArrayList<>(types);
    }

    public List<SampleCollection> getSampleCollections(boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();

        //we have to preserve the order of identifier, i.e. the order of returned list of
        //sample collections must be the sample as the order of identifiers returned by source.getIdentifiers()
        List<String> identifiers = source.getIdentifiers();
        if(includeCoordinates)
        {
            identifiers.add(0, Datasets.X_COORDINATE);
            identifiers.add(1, Datasets.Y_COORDINATE);
        }

        for(String id : identifiers)
        {                     
            Map<Object, QuantitativeSample> samplesForId = samples.get(id);

            if(samplesForId == null)
            {
                continue;
            }

            Map<String, QuantitativeSample> sampleCopy = new LinkedHashMap<>();

            for(Entry<Object, QuantitativeSample> innerEntry : samplesForId.entrySet())
            {
                Object key = innerEntry.getKey();                
                sampleCopy.put(innerEntry.getValue().getSampleName(), innerEntry.getValue());
            }

            SampleCollection collection = new StandardSampleCollection(sampleCopy, id, 
                    id, source.getDefaultOutputLocation());
            sampleCollections.add(collection);           
        }

        return sampleCollections;
    }

    public List<SampleCollection> getSampleCollections2(boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();

        Map<String, Map<Object, QuantitativeSample>> samplesCopy = samples.getMapCopy();

        if(!includeCoordinates)
        {
            samplesCopy.remove(Datasets.X_COORDINATE);
            samplesCopy.remove(Datasets.Y_COORDINATE);
        }

        Map<Object, Map<String, QuantitativeSample>> swapped = CollectionsUtilities.swapNestedHierarchy(samplesCopy);

        for(Entry<Object, String> entry : getROIKeyLabelMap().entrySet())
        {  
            Object roiKey = entry.getKey();
            String roiLabel = entry.getValue();

            Map<String, QuantitativeSample> samplesForROI = swapped.get(source.getUniversalROIKey(roiKey));

            if(samplesForROI == null)
            {
                continue;
            }

            String sampleCollectionName = source.getUniversalROIKey(roiLabel);

            SampleCollection collection = new StandardSampleCollection(samplesForROI, sampleCollectionName, 
                    sampleCollectionName, source.getDefaultOutputLocation());
            sampleCollections.add(collection);           
        }

        return sampleCollections;
    }

    private Map<Object, String> getROIKeyLabelMap() 
    {
        Map<Object, String> map = new LinkedHashMap<>();

        map.put("All", "All");

        for(ROI roi : rois.values())
        {
            map.put(roi.getKey(), roi.getLabel());
        }

        return map;
    }

    public List<SampleCollection> getSampleCollections(Collection<Object> roiIds, boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();

        List<String> identifiers = source.getIdentifiers();
        if(includeCoordinates)
        {
            identifiers.add(0, Datasets.X_COORDINATE);
            identifiers.add(1, Datasets.Y_COORDINATE);

        }

        for(String id : identifiers)
        {  
            Map<Object, QuantitativeSample> samplesForId = samples.get(id);

            if(samplesForId == null)
            {
                continue;
            }

            Map<String, QuantitativeSample> sampleCopy = new LinkedHashMap<>();

            for(Object roiId : roiIds)
            {
                QuantitativeSample sample = samplesForId.get(roiId);
                if(sample != null)
                {
                    sampleCopy.put(roiId.toString(), sample);
                }
            }

            SampleCollection collection = new StandardSampleCollection(sampleCopy, id, 
                    id, source.getDefaultOutputLocation());
            sampleCollections.add(collection);           
        }

        return sampleCollections;
    }



    public List<SampleCollection> getSampleCollections2(Collection<ROI> rois, boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();

        Map<String, Map<Object, QuantitativeSample>> samplesCopy = samples.getMapCopy();
        if(!includeCoordinates)
        {
            samplesCopy.remove(Datasets.X_COORDINATE);
            samplesCopy.remove(Datasets.Y_COORDINATE);
        }

        Map<Object, Map<String, QuantitativeSample>> swapped 
        = CollectionsUtilities.swapNestedHierarchy(samplesCopy);

        for(ROI roi : rois)
        {  
            Object roiKey = roi.getKey();
            String roiLabel = roi.getLabel();

            Map<String, QuantitativeSample> samplesForROI = swapped.get(source.getUniversalROIKey(roiKey));

            if(samplesForROI == null)
            {
                continue;
            }
            String sampleCollectionName = source.getUniversalROIKey(roiLabel);

            SampleCollection collection = new StandardSampleCollection(samplesForROI, sampleCollectionName, 
                    sampleCollectionName, source.getDefaultOutputLocation());
            sampleCollections.add(collection);           
        }

        return sampleCollections;
    }

    public Map<String, Map<Object, QuantitativeSample>> getSamples()
    {
        return this.getSamples(true);
    }


    public Map<String, Map<Object, QuantitativeSample>> getSamples(boolean includeCoordinates)
    {		
        Map<String, Map<Object, QuantitativeSample>> samplesCopy = new LinkedHashMap<>();

        //we have to preserve the order of identifier, i.e. the order of returned list of
        //sample collections must be the sample as the order of identifiers returned by source.getIdentifiers()

        List<String> identifiers = source.getIdentifiers();
        if(includeCoordinates)
        {
            identifiers.add(0, Datasets.X_COORDINATE);
            identifiers.add(1, Datasets.Y_COORDINATE);
        }

        for(String id : identifiers)
        {
            Map<Object, QuantitativeSample> innerMap = samples.get(id);
            Map<Object, QuantitativeSample> sampleCopy = new LinkedHashMap<>(innerMap);
            samplesCopy.put(id, sampleCopy);
        }	

        return samplesCopy;
    }

    public Map<Object, QuantitativeSample> getSample(String identifier)
    {
        return samples.get(identifier);
    }

    /*
     * Calculates the samples for each ROI for the channel of 'identifier' identifier. This is necessary when DensitySource data
     * are changed and we want to keep the ROISampleHierarchy updated (consistent with its DensitySource)
     */
    public Map<Object, QuantitativeSample> refresh(String identifier)
    {
        List<ROI> changedROIs = new ArrayList<>();
        changedROIs.add(roiUnion);
        changedROIs.addAll(rois.values()); 

        Map<Object, QuantitativeSample> samplesForType = source.getROISamples(changedROIs, identifier);
        this.samples.putAll(identifier, samplesForType);

        Map<Object, QuantitativeSample> samplesForTypeCopy = new LinkedHashMap<>(samplesForType);
        return samplesForTypeCopy;
    }

    public Map<String, Map<Object, QuantitativeSample>> refresh(List<String> identifiers)
    {
        List<ROI> changedROIs = new ArrayList<>();

        changedROIs.add(roiUnion);
        changedROIs.addAll(rois.values()); 

        return refresh(identifiers, changedROIs);
    }

    public Map<String, Map<Object, QuantitativeSample>>
    refresh(List<String> identifiers,List<ROI> changedROIs)
    {
        Map<String, Map<Object, QuantitativeSample>> refreshed = source.getROISamples(changedROIs, identifiers, false);

        this.samples.putAll(refreshed);


        //IS COPYING REALLY NECESSARY ?
        Map<String, Map<Object, QuantitativeSample>> refreshedCopy = CollectionsUtilities.deepCopy(refreshed);

        return refreshedCopy;
    }

    public Map<String, Map<Object, QuantitativeSample>> refreshWithExceptions(List<String> identifiers, List<Object> exceptions)
    {       
        List<ROI> changedROIs = new ArrayList<>();

        if(!exceptions.contains(roiUnion.getKey()))
        {
            changedROIs.add(roiUnion);
        }

        for(Entry<Object, ROI> entry : rois.entrySet())
        {
            Object key = entry.getKey();
            if(!exceptions.contains(key))
            {
                changedROIs.add(entry.getValue());
            }
        }

        return refresh(identifiers, changedROIs);
    }

    public Map<String, Map<Object, QuantitativeSample>> addOrReplaceROI(ROI roi)
    {        
        Object key = roi.getKey();

        this.rois.put(key, roi);
        this.roiUnion = ROIUtilities.composeROIs(rois.values(), "All");

        List<ROI> changedROIs = new ArrayList<>();

        Map<String, Map<Object,QuantitativeSample>> samplesToAddOrReplace;
        if(rois.values().size() > 1)
        {            
            changedROIs.add(roiUnion);
            changedROIs.add(roi);

            samplesToAddOrReplace = source.getROISamples(changedROIs, true);           
        }
        else
        {
            changedROIs.add(roi);

            samplesToAddOrReplace = source.getROISamples(changedROIs, true);

            String roiKey = source.getUniversalROIKey(key);
            String allKey = source.getUniversalROIKey(roiUnion.getKey());

            for(Entry<String, Map<Object,QuantitativeSample>> entry : samplesToAddOrReplace.entrySet())
            {
                String type = entry.getKey();
                Map<Object,QuantitativeSample> samplesForType = entry.getValue();

                //we have to copy the content of samplesForType to a new map samplesForTypeNew, which will replace it, because we want the QuantitativeSample
                //for all to be the first one
                Map<Object,QuantitativeSample> samplesForTypeNew = new LinkedHashMap<>();

                QuantitativeSample sample = samplesForType.get(roiKey);
                QuantitativeSample sampleCopy = new StandardSample(sample);
                sampleCopy.setNameRoot("All");

                samplesForTypeNew.put(allKey, sampleCopy);
                samplesForTypeNew.putAll(samplesForType);
                samplesToAddOrReplace.put(type, samplesForTypeNew);
            }
        }

        this.samples.putAll(samplesToAddOrReplace);

        return samplesToAddOrReplace;
    }

    public Map<String, Map<Object, QuantitativeSample>> addOrReplaceROIs(Collection<? extends ROI> roisNew)
    {        
        if(roisNew.isEmpty())
        {
            return new LinkedHashMap<>();
        }

        if(roisNew.size() == 1)
        {
            return addOrReplaceROI(roisNew.iterator().next());
        }

        for(ROI roi : roisNew)
        {
            Object key = roi.getKey();

            this.rois.put(key, roi);
        }

        this.roiUnion = ROIUtilities.composeROIs(rois.values(), "All");

        List<ROI> changedROIs = new ArrayList<>();

        changedROIs.add(roiUnion);
        changedROIs.addAll(roisNew);

        Map<String, Map<Object,QuantitativeSample>> samplesToAddOrReplace = source.getROISamples(changedROIs, true);   
        samples.putAll(samplesToAddOrReplace);

        return samplesToAddOrReplace;
    }

    public Map<String, Map<Object, QuantitativeSample>> removeROI(ROI roi)
    {
        Object roiKey = roi.getKey();

        this.rois.remove(roiKey);
        this.roiUnion = ROIUtilities.composeROIs(rois.values(), "All");

        String removedSampleKey = source.getUniversalROIKey(roiKey);
        String roiUnionKey = source.getUniversalROIKey(roiUnion.getKey());


        List<ROI> changedROIs = new ArrayList<>();
        changedROIs.add(this.roiUnion);

        Map<String, Map<Object, QuantitativeSample>> changedSamples = source.getROISamples(changedROIs, true);

        this.samples.removeInAllInnerMaps(removedSampleKey);
        this.samples.copyInnerValues(roiUnionKey, changedSamples);

        boolean roisEmpty = rois.isEmpty();

        for(String type : getSampleTypes())
        {
            Map<Object, QuantitativeSample> changedSamplesForType = changedSamples.get(type);
            changedSamplesForType.put(removedSampleKey, null);	
            if(roisEmpty)			
            {
                changedSamplesForType.put(roiUnionKey, null);
            }
        }	

        return changedSamples;
    }

    public Map<String, Map<Object, QuantitativeSample>> removeROIs(Collection<? extends ROI> roisToRemove)
    {
        if(roisToRemove.isEmpty())
        {
            return new LinkedHashMap<>();
        }

        if(roisToRemove.size() == 1)
        {
            return removeROI(roisToRemove.iterator().next());
        }

        for(ROI roi : roisToRemove)
        {
            Object roiKey = roi.getKey();
            this.rois.remove(roiKey);
        }

        this.roiUnion = ROIUtilities.composeROIs(rois.values(), "All");


        boolean roisEmpty = rois.isEmpty();

        List<ROI> changedROIs = new ArrayList<>();
        changedROIs.add(this.roiUnion);

        String roiUnionKey = source.getUniversalROIKey(roiUnion.getKey());

        Map<String, Map<Object, QuantitativeSample>> changedSamples = source.getROISamples(changedROIs, true);

        this.samples.copyInnerValues(roiUnionKey, changedSamples);

        for(ROI roi : roisToRemove)
        {
            String removedSampleKey = source.getUniversalROIKey(roi.getKey());
            this.samples.removeInAllInnerMaps(removedSampleKey);
        }

        for(String type : getSampleTypes())
        {
            Map<Object, QuantitativeSample> changedSamplesForType = changedSamples.get(type);

            for(ROI roi : roisToRemove)
            {
                Object roiKey = roi.getKey();
                String removedSampleKey = source.getUniversalROIKey(roiKey);
                changedSamplesForType.put(removedSampleKey, null);  
            }

            if(roisEmpty)           
            {
                changedSamplesForType.put(roiUnionKey, null);
            }
        }   

        return changedSamples;
    }

    public Map<String, Map<Object, QuantitativeSample>> setROIs(Map<Object, ? extends ROI> rois)
    {
        this.rois.clear();
        this.rois.putAll(rois);
        this.roiUnion = ROIUtilities.composeROIs(rois.values(), "All");

        List<ROI> changedROIs = new ArrayList<>();
        changedROIs.add(roiUnion);
        changedROIs.addAll(rois.values());

        Map<String, Map<Object, QuantitativeSample>> samplesNew = source.getROISamples(changedROIs, true);

        this.samples.clear();
        this.samples.putAll(samplesNew);	

        return this.samples.getMapCopy();
    }

    public MetaMap<String, Object, QuantitativeSample> changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        MetaMap<String, Object, QuantitativeSample> changedSamples = new MetaMap<>();

        ROI roi = rois.get(roiKey);
        Object sampleKey = source.getUniversalROIKey(roiKey);

        if(roi != null)
        {
            roi.setLabel(labelNew);

            List<QuantitativeSample> samplesAfected = this.samples.getInnerKeyValuesCopy(sampleKey);

            for(QuantitativeSample sample : samplesAfected)
            {
                sample.setNameRoot(labelNew);
            }

            changedSamples.putAll(this.samples.getSubMapCopy(sampleKey));
        }

        return changedSamples;
    }

    public File getDefaultOutputDirectory() {
        return source.getDefaultOutputLocation();
    }
}
