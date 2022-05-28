package atomicJ.resources;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIProxy;
import atomicJ.gui.rois.ROISampleManager;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.readers.regularImage.Channel2DSourceMetadata;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public class TypeModelManager extends AbstractModel
{
    //keeps the order of types
    private final List<String> types = new ArrayList<>();

    //allows to map type to TypeModelSlim
    private final Map<String, TypeModel<String>> typeModels = new LinkedHashMap<>();   

    private List<ROISampleManager> roiSampleManagers = new ArrayList<>();

    private Map<Object, ROIDrawable> rois = new LinkedHashMap<>();

    private final Map<Object, ROIDrawable> roisToRefresh = new LinkedHashMap<>();
    private final Map<Object, ROIDrawable> roisToRemove = new LinkedHashMap<>();
    private final MultiMap<Channel2DSource<?>, String> sourcesToRefreshSamples = new MultiMap<>();

    private boolean allROIsLagging = false;

    private final Map<Integer, Channel2DSource<?>> sources = new LinkedHashMap<>();
    private final Map<Channel2DSource<?>, Integer> sourcesReverse = new LinkedHashMap<>();

    private int currentSourceIndex = 0;

    public TypeModelManager()
    {}

    public TypeModelManager(Channel2DSource<?> source, List<String> identifiers)
    {
        registerSource(source);
        addROISampleManangerIfNecessary(source);

        Integer key = sourcesReverse.get(source);

        for(String type : identifiers)
        {
            TypeModel<String> model = new TypeModel<>(type);
            model.registerIdentifier(key, type);

            TypeModel<String> old = typeModels.put(type, model);

            if(old == null) //in case identifiers were duplicate
            {
                types.add(type);
            }
        }      

    }

    public TypeModelManager(TypeModelManager that)
    {
        this.types.addAll(that.types);

        for(Entry<String, TypeModel<String>> entry: that.typeModels.entrySet())
        {
            this.typeModels.put(entry.getKey(), new TypeModel<>(entry.getValue()));
        }

        Set<Channel2DSource<?>> sourcesOld = that.getSources();

        for(Channel2DSource<?> sourceOld : sourcesOld)
        {
            Channel2DSource<?> sourceNew = sourceOld.copy();   

            ROISampleManager copy = new ROISampleManager(sourceNew, that.getROIManager(sourceOld));
            roiSampleManagers.add(copy);

            registerSource(sourceNew);
        }

        this.rois.putAll(that.getROIsDeepCopy());  
    }   

    //copies the TypeModelManager 'that', but only retains types from the set 'typesToRetain'
    public TypeModelManager(TypeModelManager that, Set<String> typesToRetain)
    {
        Set<String> typesNew = new LinkedHashSet<>(that.types);
        typesNew.retainAll(typesToRetain);

        this.types.addAll(typesNew);

        for(String type : this.types)
        {
            this.typeModels.put(type, new TypeModel<>(that.typeModels.get(type)));
        }

        for(Channel2DSource<?> sourceOld : that.sources.values())
        {
            Set<String> registeredIdentifiersForSource = that.getRegisteredIdentifiers(sourceOld, new LinkedHashSet<>(this.types));

            if(!registeredIdentifiersForSource.isEmpty())
            {
                Channel2DSource<?> sourceNew = sourceOld.copy(registeredIdentifiersForSource);   

                ROISampleManager copy = new ROISampleManager(sourceNew, that.getROIManager(sourceOld));
                roiSampleManagers.add(copy);

                registerSource(sourceNew);
            }       
        }

        this.rois.putAll(that.getROIsDeepCopy());  
    }   

    private Set<String> getRegisteredIdentifiers(Channel2DSource<?> source, Set<String> types)
    {
        Integer sourceKey = sourcesReverse.get(source);

        Set<String> registeredIdentifiers = new LinkedHashSet<>();

        for(String type : types)
        {
            TypeModel<String> typeModel = typeModels.get(type);
            registeredIdentifiers.addAll(typeModel.getIdentifiers(sourceKey));
        }

        return registeredIdentifiers;        
    }

    private void registerSource(Channel2DSource<?> source)
    {        
        if(!sources.containsValue(source))
        {         
            sourcesReverse.put(source, currentSourceIndex);
            sources.put(currentSourceIndex, source);    
            currentSourceIndex++;

            Channel2DSourceMetadata metadata = source.getMetadata();

            if(metadata.isUseReadInROIs())
            {
                List<ROIProxy> readInROIs = metadata.getReadInROIs();

                ROIStyle style = new ROIStyle(Preferences.userNodeForPackage(getClass()).node("TypeModelManager"), Color.white);

                for(ROIProxy roiReadInProxy : readInROIs)
                {
                    ROIDrawable roi = roiReadInProxy.recreateOriginalObject(style, getNextROIKey());
                    addOrReplaceROI(roi);
                }
            }          
        }
    }

    public Integer getNextROIKey()
    {
        int key = 0;

        for(ROIDrawable roi : rois.values())
        {
            Object currentKey = roi.getKey();
            if(currentKey instanceof Integer)
            {
                key = Math.max(((Integer)currentKey).intValue() + 1, key);
            }
        }

        return key;
    }

    private boolean addROISampleManangerIfNecessary(Channel2DSource<?> source)
    {      
        ROISampleManager manager = getROIManager(source);
        boolean shoudBeAdded = (manager == null);

        if(shoudBeAdded)
        {
            manager = new ROISampleManager(source);
            manager.setROIs(rois);
            roiSampleManagers.add(manager);
        }

        return shoudBeAdded;
    }

    public TypeModel<String> get(String type)
    {
        return typeModels.get(type);
    }

    public List<String> getTypes()
    {
        return new ArrayList<>(types);
    }

    //this method should be removed in future
    public Set<String> getAllIdentifiers()
    {
        Set<String> allTypes = new LinkedHashSet<>();

        for(Channel2DSource<?> s: getSources())
        {
            for(Channel2D ch: s.getChannels())
            {
                allTypes.add(ch.getIdentifier());
            }
        }

        return allTypes;
    }

    //this method is used by statistics dialogs - the order of pairs
    //identifier - unit determines the order of tabs in statistics tables
    public Map<String, PrefixedUnit> getIdentifierUnitMap()
    {
        Map<String, PrefixedUnit> identifierUnitMap = new LinkedHashMap<>();

        for(Channel2DSource<?> s: getSources())
        {
            for(Channel2D ch: s.getChannels())
            {
                String identifier = ch.getIdentifier();

                PrefixedUnit unit = ch.getZQuantity().getUnit();
                identifierUnitMap.put(identifier, unit);
            }
        }


        return identifierUnitMap;
    }

    public int getIndex(String type)
    {
        return types.indexOf(type);
    }

    private void insertModel(String type, TypeModel<String> model, int index)
    {
        types.remove(type);
        types.add(index, type);

        typeModels.put(type, model);

        registerROISampleLaggingType(type);
    }

    private void addModel(String type, TypeModel<String> model)
    {
        TypeModel<String> old = typeModels.put(type, model);

        if(old == null)
        {
            types.add(type);
            registerROISampleLaggingType(type);
        }
    }

    public Set<Channel2DSource<?>> getSources()
    {
        Set<Channel2DSource<?>> sourcesCopy = new LinkedHashSet<>(sources.values());

        return sourcesCopy;
    }

    public boolean containsSource(ChannelSource source)
    {
        return sources.containsValue(source);
    }

    public <E extends Channel2DSource<?>> Set<E> getChannel2DSources(Class<E> classType)
    {
        Set<E> sourcesCopy = new LinkedHashSet<>();

        for(Channel2DSource<?> source : sources.values())
        {
            if(classType.isInstance(source))
            {
                sourcesCopy.add((E)source);
            }
        }

        return sourcesCopy;
    }

    public MultiMap<Channel2DSource<?>, String> getChannelIdentifiers()
    {
        MultiMap<Channel2DSource<?>, String> allChannels = new MultiMap<>(); 

        for(String type : types)
        {
            Map<Channel2DSource<?>, List<String>> sourcesForType = getResourceIdentifierMap(type);
            allChannels.putAll(sourcesForType);
        }

        return allChannels;
    }

    //regular channel identifiers, not universal identifiers
    public Set<String> getIdentifiers(String type)
    {
        TypeModel<String> model = typeModels.get(type);

        Set<String> identifiers = (model != null) ? model.getIdentifiers() : new LinkedHashSet<>();

        return identifiers;
    }


    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        TypeModel<String> typeModel = typeModels.get(type);

        if(typeModel != null)
        {
            Map<Integer, List<String>> identifierMap = typeModel.getIdentifierMap();

            for(Entry<Integer, List<String>> entry : identifierMap.entrySet())
            {
                Integer sourceNo = entry.getKey();
                List<String> ids = entry.getValue();
                Channel2DSource<?> source = sources.get(sourceNo);

                List<?> sourceChannels = source.getChannels(ids);

                for(Object channel : sourceChannels)
                {
                    if(channel instanceof Channel2D && filter.accepts((Channel2D)channel))
                    {
                        identifiers.add(((Channel2D)channel).getIdentifier());
                    }
                }

            }

        }

        return identifiers;
    }


    //returns a map with values being those channels whose identifiers are in the set 'identifiers'. The keys are the corresponding identifiers
    //A multimap is returned, because this TypeModelManager may contain two different sources, with channels of the same identifier
    public MultiMap<String, Channel2D> getChannelsForIdentifiers(Set<String> identifiers)
    {
        MultiMap<String, Channel2D> channels = new MultiMap<>();

        for(TypeModel<String> typeModel : typeModels.values())
        {
            if(typeModel != null)
            {
                Map<Integer, List<String>> identifierMap = typeModel.getIdentifierMap();

                for(Entry<Integer, List<String>> entry : identifierMap.entrySet())
                {
                    Integer sourceNo = entry.getKey();

                    Set<String> ids = new LinkedHashSet<>(entry.getValue());
                    ids.retainAll(identifiers);

                    Channel2DSource<?> source = sources.get(sourceNo);

                    List<?> sourceChannels = source.getChannels(ids);

                    for(Object channel : sourceChannels)
                    {
                        if(channel instanceof Channel2D)
                        {
                            channels.put(((Channel2D) channel).getIdentifier(), (Channel2D)channel);
                        }
                    }

                }

            }
        }


        return channels;
    }


    public Set<String> getIdentifiersForAllTypes()
    {
        Set<String> identifiers = new LinkedHashSet<>();

        for(TypeModel<String> typeModel : typeModels.values())
        {
            identifiers.addAll(typeModel.getIdentifiers());
        }

        return identifiers;
    }


    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        for(TypeModel<String> typeModel : typeModels.values())
        {
            Map<Integer, List<String>> identifierMap = typeModel.getIdentifierMap();

            for(Entry<Integer, List<String>> entry : identifierMap.entrySet())
            {
                Integer sourceNo = entry.getKey();
                List<String> ids = entry.getValue();
                Channel2DSource<?> source = sources.get(sourceNo);

                List<?> sourceChannels = source.getChannels(ids);

                for(Object channel : sourceChannels)
                {
                    if(channel instanceof Channel2D && filter.accepts((Channel2D)channel))
                    {
                        identifiers.add(((Channel2D)channel).getIdentifier());
                    }
                }

            }
        }

        return identifiers;
    }

    private  Map<Channel2DSource<?>, List<String>> getResourceIdentifierMap(String type)
    {
        Map<Channel2DSource<?>, List<String>> map = new LinkedHashMap<>();

        TypeModel<String> model = typeModels.get(type);

        if(model != null)
        {
            for(Entry<Integer, Channel2DSource<?>> entry : sources.entrySet())
            {
                Integer key = entry.getKey();
                Channel2DSource<?> source = entry.getValue();
                List<String> identifiers = model.getIdentifiers(key);
                map.put(source, identifiers);
            }
        }

        return map;
    }

    public Map<Channel2DSource<?>, List<Channel2D>> getResourceChannelMap(String type) 
    {
        Map<Channel2DSource<?>, List<Channel2D>> map = new LinkedHashMap<>();

        TypeModel<String> model = typeModels.get(type);
        if(model != null)
        {
            for(Entry<Integer, Channel2DSource<?>> entry : sources.entrySet())
            {
                Integer key = entry.getKey();
                Channel2DSource<?> source = entry.getValue();
                List<String> identifiers = model.getIdentifiers(key);
                List<Channel2D> channels = new ArrayList<>();
                channels.addAll(source.getChannels(identifiers));
                map.put(source, channels);
            }
        }

        return map;   
    }

    public Map<String, Channel2D> getChannelUniversalIdentifierMap(String type)
    {
        Map<String, Channel2D> channelMap = new LinkedHashMap<>();

        Map<Channel2DSource<?>, List<String>> idMap = getResourceIdentifierMap(type);

        for(Entry<Channel2DSource<?>, List<String>> entry : idMap.entrySet())
        {
            Channel2DSource<?> source = entry.getKey();

            List<String> identifiers = entry.getValue();

            for(String id : identifiers)
            {
                Channel2D channel = source.getChannel(id);
                channelMap.put(source.getChannelUniversalIdentifier(id), channel);
            }
        }

        return channelMap;
    }

    public Map<String, PrefixedUnit> getUnitUniversalIdentifierMap(String type)
    {
        Map<String, PrefixedUnit> units = new LinkedHashMap<>();

        Map<String, Channel2D> channels = getChannelUniversalIdentifierMap(type);

        for(Entry<String, Channel2D> entry : channels.entrySet())
        {
            units.put(entry.getKey(), entry.getValue().getZQuantity().getUnit());
        }

        return units;
    }

    public boolean isSingleDataUnit(String type)
    {       
        return getSingleDataUnit(type) != null;
    }

    public PrefixedUnit getSingleDataUnit(String type)
    {
        Map<String, PrefixedUnit> unitMap = getUnitUniversalIdentifierMap(type);
        if(unitMap.isEmpty())
        {
            return null;
        }

        PrefixedUnit firstUnit = unitMap.values().iterator().next();

        for(PrefixedUnit unit : unitMap.values())
        {
            if(!Objects.equals(firstUnit, unit))
            {
                return null;
            }
        }

        return firstUnit;
    }

    public void registerIdentifier(String type, Channel2DSource<?> source, String identifier)
    {
        registerSource(source);
        boolean added = addROISampleManangerIfNecessary(source);

        TypeModel<String> model = typeModels.get(type);

        if(model == null)
        {           
            model = new TypeModel<>(type);
            types.add(type);
            typeModels.put(type, model);
        }

        model.registerIdentifier(sourcesReverse.get(source), identifier);  

        //if it was created and added above in the method addROISampleManangerIfNecessary(), 
        //there is no to refresh it, as it is correct state was calculated when
        //it was created
        if(!added)
        {
            sourcesToRefreshSamples.put(source, identifier);
        }

    }

    public String duplicate(String type)
    {
        TypeModel<String> model = typeModels.get(type);

        String typeNew = null;
        if(model != null)
        {            
            Map<Channel2DSource<?>, List<String>> sourceIdMap = getResourceIdentifierMap(type);
            typeNew = model.registerDuplication();

            TypeModel<String> duplicatedModel = new TypeModel<>(typeNew);

            for(Entry<Channel2DSource<?>, List<String>> entry : sourceIdMap.entrySet())
            {
                Channel2DSource<?> source = entry.getKey();
                List<String> identifiers = new ArrayList<>(entry.getValue());
                List<String> duplicatedIds = new ArrayList<>();

                for(String id : identifiers)
                {
                    Channel2D duplicate = source.duplicateChannel(id);
                    duplicatedIds.add(duplicate.getIdentifier());
                }
                duplicatedModel.registerChannels(sourcesReverse.get(source), duplicatedIds);
            }

            int index =  types.indexOf(type) + 1;
            insertModel(typeNew, duplicatedModel, index);   
        }

        return typeNew;
    }



    public Map<Object, QuantitativeSample> getSamples(String type)
    {
        Map<Object, QuantitativeSample> allSamples = new LinkedHashMap<>();

        for(Channel2DSource<?> source : sources.values())
        {
            String key = source.getShortName();
            QuantitativeSample sample = source.getSample(type);

            allSamples.put(key, sample);
        }
        return allSamples;
    }

    public Map<String, Map<Object, QuantitativeSample>> getSamples(boolean includeCoordinates)
    {
        MetaMap<String, Object, QuantitativeSample> allSamples = new MetaMap<>();

        for(Channel2DSource<?> source : sources.values())
        {
            Map<String, QuantitativeSample> samplesToAdd = source.getSamples(includeCoordinates);

            for(Entry<String, QuantitativeSample> entry: samplesToAdd.entrySet())
            {
                String type = entry.getKey();
                QuantitativeSample sample = entry.getValue();
                allSamples.put(type, source.getShortName(), sample);
            }
        }

        return allSamples.getMapCopy();
    }

    //////////////////////////////////////// ROIS //////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////




    public Map<Object, ROIDrawable> getROIsDeepCopy()
    {
        Map<Object, ROIDrawable> roisCopy = new LinkedHashMap<>();
        for(Entry<Object, ROIDrawable> entry : rois.entrySet())
        {
            Object roiKey = entry.getKey();
            ROIDrawable roiOriginal = entry.getValue();
            ROIDrawable roiNew = roiOriginal.copy();

            roisCopy.put(roiKey, roiNew);
        }

        return roisCopy;
    }



    public void registerROISampleLaggingType(String type)
    {
        sourcesToRefreshSamples.putAll(getResourceIdentifierMap(type));
    }

    private MetaMap<String, Object, QuantitativeSample> refreshLaggingTypes(List<Object> exceptions)
    {              
        MetaMap<String, Object, QuantitativeSample> metaMap = new MetaMap<>();

        for(Entry<Channel2DSource<?>, List<String>> entry : sourcesToRefreshSamples.entrySet())
        {
            Channel2DSource<?> source = entry.getKey();
            List<String> identifiers = entry.getValue();

            ROISampleManager manager = getROIManager(source);
            Map<String, Map<Object, QuantitativeSample>> refreshed = manager.refreshWithExceptions(identifiers, exceptions);

            metaMap.putAll(refreshed);
        }

        sourcesToRefreshSamples.clear();

        return metaMap;
    }

    /*
     * This method ensures and the rois in the TypeModelManager are consistent with
     * the roi samples managed by ROISampleManagers.
     * 
     * The consistency is necessary when one of the objects displaying ROI samples
     * is shown (i.e. table with roi statistics). However, maintaining this consistency
     * is costly in terms of computation and should be avoided, if possible. For example,
     * there is no point in constant updating the roi samples when the user is dragging a roi
     * and no dialog with roi statistics/histogram/box plots etc. is visible
     */

    public Map<String, Map<Object, QuantitativeSample>> refreshLaggingROISamples()
    {    
        List<Object> laggingRoiKeys = getLaggingROIKeys();

        MetaMap<String, Object, QuantitativeSample> changedSamples = new MetaMap<>();

        for(ROISampleManager manager  : roiSampleManagers)
        {           
            changedSamples.putAll(manager.removeROIs(roisToRemove.values()));         
            changedSamples.putAll(manager.addOrReplaceROIs(roisToRefresh.values()));
        }   

        roisToRemove.clear();
        roisToRefresh.clear();

        changedSamples.putAll(refreshLaggingTypes(laggingRoiKeys));

        Map<String, Map<Object, QuantitativeSample>> map = allROIsLagging ? getSamplesForROIsAndUpdate(true): changedSamples.getMapCopy();

        this.allROIsLagging = false;

        if(!map.isEmpty())
        {
            firePropertyChange(StandardChannel2DResource.ROI_SAMPLES_REFRESHED, null, map);
        }

        return map;
    }

    private List<Object> getLaggingROIKeys()
    {
        List<Object> laggingROIsKeys = new ArrayList<>();

        for(ROI roi : roisToRemove.values())
        {
            laggingROIsKeys.add(roi.getKey());
        }

        for(ROI roi : roisToRefresh.values())
        {
            laggingROIsKeys.add(roi.getKey());
        }  

        return laggingROIsKeys;
    }

    private List<ROISampleManager> getAndUpdateROISampleMangers()
    {
        if(!roisToRefresh.isEmpty() || !roisToRemove.isEmpty())
        {
            refreshLaggingROISamples();
        }
        return roiSampleManagers;
    }

    public List<SampleCollection> getROISampleCollections(boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();

        for (ROISampleManager manager : getAndUpdateROISampleMangers()) 
        {
            sampleCollections.addAll(manager.getSampleCollections(includeCoordinates));
        }

        return sampleCollections;
    }

    public List<SampleCollection> getROISampleCollections2(boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();

        for (ROISampleManager manager : getAndUpdateROISampleMangers()) 
        {
            sampleCollections.addAll(manager.getSampleCollections2(includeCoordinates));
        }

        return sampleCollections;
    }

    public List<SampleCollection> getSampleCollection(Map<Object, ? extends ROI> rois, boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();
        List<Object> roiIds = new ArrayList<>(rois.keySet());        

        for (ROISampleManager manager : getAndUpdateROISampleMangers()) 
        {
            sampleCollections.addAll(manager.getSampleCollections(roiIds, includeCoordinates));
        }

        return sampleCollections;
    }

    public List<SampleCollection> getSampleCollection2(Map<Object, ? extends ROI> roiMap, boolean includeCoordinates)
    {
        List<SampleCollection> sampleCollections = new ArrayList<>();
        List<ROI> rois = new ArrayList<>(roiMap.values());        

        for (ROISampleManager manager : getAndUpdateROISampleMangers()) 
        {
            sampleCollections.addAll(manager.getSampleCollections2(rois, includeCoordinates));
        }

        return sampleCollections;
    }

    public void addOrReplaceROI(ROIDrawable roi)
    {
        rois.put(roi.getKey(), roi);
        roisToRefresh.put(roi.getKey(), roi);
    }

    public Map<String, Map<Object, QuantitativeSample>> addOrReplaceROIAndUpdate(ROIDrawable roi)
    {
        rois.put(roi.getKey(), roi);

        MetaMap<String, Object, QuantitativeSample> changedSamples = new MetaMap<>();

        for(ROISampleManager manager : getAndUpdateROISampleMangers())
        {
            Map<String, Map<Object, QuantitativeSample>> addedOrReplacedSamples = manager.addOrReplaceROI(roi);
            changedSamples.putAll(addedOrReplacedSamples);
        }

        return changedSamples.getMapCopy();
    }

    public void removeROI(ROIDrawable roi)
    {
        Object roiKey = roi.getKey();

        rois.remove(roiKey);

        roisToRemove.put(roiKey, roi);
        roisToRefresh.remove(roiKey);
    }

    public Map<String, Map<Object, QuantitativeSample>> removeROIAndUpdate(ROIDrawable roi)
    {
        rois.remove(roi.getKey());

        MetaMap<String, Object, QuantitativeSample> allRemovedSamples = new MetaMap<>();

        for(ROISampleManager collection  : getAndUpdateROISampleMangers())
        {
            Map<String, Map<Object, QuantitativeSample>> removedSamples = collection.removeROI(roi);
            allRemovedSamples.putAll(removedSamples);
        }
        return allRemovedSamples.getMapCopy();
    }

    public Map<String, Map<Object, QuantitativeSample>> setROIsAndUpdate(Map<Object, ROIDrawable> roisNew) 
    {
        this.rois = new LinkedHashMap<>(roisNew);

        List<ROISampleManager> roiSamplesNew = new ArrayList<>();       

        for(Channel2DSource<?> source: getSources())
        {           
            ROISampleManager collection = new ROISampleManager(source, 
                    new LinkedHashMap<Object, ROI>(roisNew),  true);           
            roiSamplesNew.add(collection);
        }

        this.roiSampleManagers = roiSamplesNew;

        return getSamplesForROIsAndUpdate(true);
    } 

    public void setROIs(Map<Object, ROIDrawable> roisNew)
    {
        roisToRemove.putAll(this.rois);

        this.rois = new LinkedHashMap<>(roisNew);
        roisToRefresh.clear();
        roisToRefresh.putAll(roisNew);
    }

    public MetaMap<String, Object, QuantitativeSample> changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        ROI roi = rois.get(roiKey);
        if(roi != null)
        {
            roi.setLabel(labelNew);
        }

        MetaMap<String, Object, QuantitativeSample> changedSamples = new MetaMap<>();

        for(ROISampleManager manager : roiSampleManagers)
        {
            MetaMap<String, Object, QuantitativeSample> s = manager.changeROILabel(roiKey, labelOld, labelNew);
            changedSamples.putAll(s);
        }

        return changedSamples;
    }

    public Map<String, Map<Object, QuantitativeSample>> getSamplesForROIsAndUpdate(boolean includeCoordinates)
    {
        MetaMap<String, Object, QuantitativeSample> allSamples = new MetaMap<>();

        for(ROISampleManager manager : getAndUpdateROISampleMangers())
        {
            Map<String, Map<Object, QuantitativeSample>> samples = manager.getSamples(includeCoordinates);
            allSamples.putAll(samples);
        }
        return allSamples.getMapCopy();
    }

    public Map<Object, QuantitativeSample> getSamplesForROIsAndUpdate(String type)
    {
        Map<Object, QuantitativeSample> samples = new LinkedHashMap<>();

        for(ROISampleManager manager : getAndUpdateROISampleMangers())
        {
            samples.putAll(manager.getSample(type));
        }

        return samples;
    }


    /*
     * Returns map of QuantitatveSamples for ROIUnion for different types (types are keys in this map, QuamntitativeSamples are values)
     * This map is only for the first DensitySource contained in this ImageResource
     */
    public Map<String, QuantitativeSample> getAndUpdateROIUnionSamples() 
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        if(!getAndUpdateROISampleMangers().isEmpty())
        {
            for(ROISampleManager manager : getAndUpdateROISampleMangers())
            {
                Channel2DSource<?> source = manager.getDensitySource();
                String sourceSpecificAllKey = source.getUniversalROIKey("All");

                Map<String, Map<Object, QuantitativeSample>> allSamples = manager.getSamples(false);

                for(Entry<String, Map<Object, QuantitativeSample>> entry : allSamples.entrySet())
                {
                    String type = entry.getKey();
                    QuantitativeSample sample = entry.getValue().get(sourceSpecificAllKey);                 

                    if(sample != null)
                    {
                        samples.put(type, sample);
                    }
                }
            }

        }

        return samples;
    }

    private ROISampleManager getROIManager(Channel2DSource<?> source)
    {
        ROISampleManager manager = null;

        for(ROISampleManager m : getAndUpdateROISampleMangers())
        {
            if(m.getDensitySource() == source)
            {
                manager = m;
                break;
            }            
        }

        return manager;
    }

    public Map<Object, ROIDrawable> getROIs()
    {
        return new LinkedHashMap<>(rois);
    }

    public List<Shape> getROIShapes() 
    {
        Collection<ROIDrawable> rois = getROIs().values();
        List<Shape> roiShapes = new ArrayList<>();

        for (ROIDrawable roi : rois) {
            roiShapes.add(roi.getROIShape());
        }
        return roiShapes;
    }

    public ROI getROIUnion()
    {
        return ROIUtilities.composeROIs(rois.values(), "All");
    }

    public boolean areROIsAvailable() 
    {
        return !rois.isEmpty();
    }

    public void setAllROIsLagging(boolean lagging) 
    {
        this.allROIsLagging = lagging;
    }
}
