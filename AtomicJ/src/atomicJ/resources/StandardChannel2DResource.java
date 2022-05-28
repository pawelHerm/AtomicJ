package atomicJ.resources;

import java.awt.Shape;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.DataAxis1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.MapMarker;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionTask;
import atomicJ.gui.profile.CrossSectionsReceiver;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoManager;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public class StandardChannel2DResource extends AbstractResource implements Channel2DResource, PropertyChangeListener
{
    public final static String ROI_SAMPLES_REFRESHED = "ROISamplesRefreshed";

    private Map<Object, MapMarker> mapMarkers = new LinkedHashMap<>();
    private Map<Object, DistanceMeasurementDrawable> distanceMeasurements = new LinkedHashMap<>();

    private final CrossSectionResource crossSectionResource;

    private Map<Object, Profile> profiles = new LinkedHashMap<>();

    private final TypeModelManager typeManager;
    private final UndoManager undoManager = new UndoManager(2);

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public StandardChannel2DResource(String shortName, String longName, File outputLocation)
    {
        super(outputLocation, shortName, longName);
        this.typeManager = new TypeModelManager();
        typeManager.addPropertyChangeListener(this);
        this.crossSectionResource = new CrossSectionResource(this, shortName, longName, outputLocation);
    }

    public StandardChannel2DResource(Channel2DSource<?> source, List<String> identifiers)
    {
        super(source.getDefaultOutputLocation(), source.getShortName(), source.getLongName());

        this.typeManager = new TypeModelManager(source, identifiers); 
        typeManager.addPropertyChangeListener(this);

        this.crossSectionResource = new CrossSectionResource(this, source.getShortName(), source.getLongName(), source.getDefaultOutputLocation());
    }

    public StandardChannel2DResource(Channel2DSource<?> source, List<String> identifiers, String shortName, String longName, File outputLocation)
    {
        super(outputLocation, shortName, longName);

        this.typeManager = new TypeModelManager(source, identifiers); 
        typeManager.addPropertyChangeListener(this);

        this.crossSectionResource = new CrossSectionResource(this, shortName, longName, outputLocation);
    }

    public StandardChannel2DResource(StandardChannel2DResource resourceOld, String shortNameNew, String longNameNew)
    {
        super(resourceOld);

        this.mapMarkers.putAll(resourceOld.getMapMarkersCopy());
        this.distanceMeasurements.putAll(resourceOld.getMeasurementsDeepCopy());
        this.profiles.putAll(resourceOld.getProfilesDeepCopy());

        this.crossSectionResource = new CrossSectionResource(resourceOld.crossSectionResource, this);

        this.typeManager = new TypeModelManager(resourceOld.typeManager);
        typeManager.addPropertyChangeListener(this);
    }

    public StandardChannel2DResource(StandardChannel2DResource resourceOld, Set<String> typesToRetain, String shortNameNew, String longNameNew)
    {
        super(resourceOld);

        this.mapMarkers.putAll(resourceOld.getMapMarkersCopy());
        this.distanceMeasurements.putAll(resourceOld.getMeasurementsDeepCopy());
        this.profiles.putAll(resourceOld.getProfilesDeepCopy());

        this.crossSectionResource = new CrossSectionResource(resourceOld.crossSectionResource, this, typesToRetain);

        this.typeManager = new TypeModelManager(resourceOld.typeManager, typesToRetain);
        typeManager.addPropertyChangeListener(this);
    }

    public Map<Object, MapMarker> getMapMarkersCopy()
    {
        Map<Object, MapMarker>  markersCopy = new LinkedHashMap<>();
        for(Entry<Object, MapMarker> entry : mapMarkers.entrySet())
        {
            Object key = entry.getKey();
            MapMarker mapMarker = entry.getValue();
            MapMarker mapMarkerCopy = mapMarker.copy();

            markersCopy.put(key, mapMarkerCopy);
        }

        return markersCopy;
    }

    public Map<Object, DistanceMeasurementDrawable> getMeasurementsDeepCopy()
    {
        Map<Object, DistanceMeasurementDrawable> measurementsCopy = new LinkedHashMap<>();

        for(Entry<Object, DistanceMeasurementDrawable> entry : distanceMeasurements.entrySet())
        {
            Object key = entry.getKey();
            DistanceMeasurementDrawable mapMarker = entry.getValue();
            DistanceMeasurementDrawable mapMarkerCopy = mapMarker.copy();

            measurementsCopy.put(key, mapMarkerCopy);
        }

        return measurementsCopy;
    }

    public Map<Object, Profile> getProfilesDeepCopy()
    {
        Map<Object, Profile> profilesCopy = new LinkedHashMap<>();

        for(Entry<Object, Profile> entry : profiles.entrySet())
        {
            Object profileKey = entry.getKey();
            Profile profile = entry.getValue();
            Profile profileCopy = profile.copy();

            profilesCopy.put(profileKey, profileCopy);
        }

        return profilesCopy;
    }

    @Override
    public CrossSectionResource getCrossSectionResource() 
    {
        return crossSectionResource;
    }

    @Override
    public Map<Object, Profile> getProfiles()
    {
        return new LinkedHashMap<>(profiles);
    }

    @Override
    public Profile getProfile(Object key)
    {
        return profiles.get(key);
    }

    @Override
    public void addOrReplaceProfile(Profile profile, CrossSectionsReceiver receiver, CrossSectionResource resource,  Window taskParent)
    {
        profiles.put(profile.getKey(), profile);

        if(isInterpolationPreparationNecessary(InterpolationMethod2D.BICUBIC_SPLINE))
        {
            Map<String, ChannelSectionLine> newCrossSections = getCrossSections(profile);
            crossSectionResource.addOrReplaceCrossSections(newCrossSections);

            receiver.addOrReplaceCrossSections(resource, newCrossSections);
            receiver.selectResource(crossSectionResource);
            receiver.externalSetProfileMarkerPositions(profile.getKey(), profile.getKnobPositions());       
        }
        else
        {
            CrossSectionTask task = new CrossSectionTask(getSourceChannelIdentifierMaps(), profile, resource, receiver, taskParent);
            task.execute();
        }
    }

    @Override
    public Map<String, ChannelSectionLine> addOrReplaceProfile(Profile profile)
    {        
        profiles.put(profile.getKey(), profile);

        Map<String, ChannelSectionLine> newCrossSections = getCrossSections(profile);


        crossSectionResource.addOrReplaceCrossSections(newCrossSections);

        return newCrossSections;
    }

    @Override
    public Map<String, ChannelSectionLine> removeProfile(Profile profile)
    {
        profiles.remove(profile.getKey());
        Object key = profile.getKey();
        return crossSectionResource.removeCrossSections(key);
    }

    @Override
    public Map<String, Map<Object, ChannelSectionLine>> setProfiles(Map<Object, Profile> profiles) 
    {
        this.profiles = new LinkedHashMap<>(profiles);
        Map<String, Map<Object, ChannelSectionLine>> crossSections = getCrossSections(profiles);
        crossSectionResource.setCrossSections(crossSections);
        return crossSections;
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    {
        return getSampleCollection(true);
    }

    @Override
    public List<SampleCollection> getSampleCollection(boolean includeCoordinates) 
    {
        Set<Channel2DSource<?>> sources = getChannel2DSources();
        List<SampleCollection> sampleCollections = new ArrayList<>();

        for (Channel2DSource<?> source : sources) 
        {
            SampleCollection sampleCollection = new StandardSampleCollection(source.getSamples(includeCoordinates), source.getLongName(),source.getShortName(), source.getDefaultOutputLocation());
            sampleCollections.add(sampleCollection);
        }

        return sampleCollections;   
    }

    @Override
    public List<SampleCollection> getROISampleCollections(boolean includeCoordinates)
    {
        return typeManager.getROISampleCollections(includeCoordinates);
    }

    @Override
    public List<SampleCollection> getROISampleCollections2(boolean includeCoordinates)
    {
        return typeManager.getROISampleCollections2(includeCoordinates);
    }

    @Override
    public boolean addProfileKnob(Object profileKey, double knobPosition) 
    {
        Profile profile = profiles.get(profileKey);

        boolean knobAdded = false;
        if(profile != null)
        {
            knobAdded = profile.addKnob(knobPosition);
        }

        return knobAdded;
    }

    @Override
    public boolean setProfileKnobs(Object profileKey, List<Double> positionsNew)
    {
        Profile profile = profiles.get(profileKey);

        boolean newPositionsSet = false;

        if(profile != null)
        {
            newPositionsSet = profile.setKnobPositions(positionsNew);
        }

        return newPositionsSet;
    }

    @Override
    public boolean moveProfileKnob(Object profileKey, int knobIndex, double knobPositionNew) 
    {
        boolean positionChanged = false;
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {       
            positionChanged = profile.moveKnob(knobIndex, knobPositionNew);     
        }  

        return positionChanged;
    }

    @Override
    public boolean removeProfileKnob(Object profileKey, double knobPosition) 
    {
        boolean knobRemoved = false;

        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {       
            knobRemoved = profile.removeKnob(knobPosition);     
        }  

        return knobRemoved;
    }


    @Override
    public Map<String, Channel2D> getChannels(String type)
    {
        Map<String, Channel2D> channelMap = typeManager.getChannelUniversalIdentifierMap(type);
        return channelMap;
    }

    @Override
    public MultiMap<String, Channel2D> getChannelsForIdentifiers(Set<String> identifiers)
    {
        return typeManager.getChannelsForIdentifiers(identifiers);
    }

    @Override
    public Map<String, PrefixedUnit> getDataUnits(String type)
    {
        return typeManager.getUnitUniversalIdentifierMap(type);
    }

    @Override
    public PrefixedUnit getSingleDataUnit(String type)
    {
        return typeManager.getSingleDataUnit(type);
    }

    public boolean isSingleDataUnit(String type)
    {
        return typeManager.isSingleDataUnit(type);
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
    public void undo(String type)
    {
        undoManager.undo(type);
    }

    ////////////////// DISTANCE MEASUREMENT /////////////////////////


    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.put(measurement.getKey(), measurement);
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.remove(measurement.getKey());
    }

    @Override
    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements()
    {
        return new LinkedHashMap<>(distanceMeasurements);
    }

    @Override
    public void setDistanceMeasurements(Map<Object, DistanceMeasurementDrawable> measurements)
    {
        this.distanceMeasurements = new LinkedHashMap<>(measurements);
    }

    @Override
    public Map<Object, DistanceShapeFactors> getDistanceMeasurementGeometries()
    {
        Map<Object, DistanceShapeFactors> distanceMeasurementLines = new LinkedHashMap<>();
        for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
        {
            Object key = entry.getKey();
            DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
            distanceMeasurementLines.put(key, line);
        }

        return distanceMeasurementLines;
    }

    //////////////////// END OF DISTANCE MEASUREMENTS ///////////////////////////////////////

    ///////////////////////////// MAP MARKERS ///////////////////////////////////////////////


    @Override
    public Map<Object, MapMarker> getMapMarkers()
    {
        return new LinkedHashMap<>(mapMarkers);
    }

    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker)
    {
        mapMarkers.put(mapMarker.getKey(), mapMarker);
    }

    @Override
    public boolean removeMapMarker(MapMarker mapMarker)
    {
        MapMarker removedMarker = mapMarkers.remove(mapMarker.getKey());
        boolean sccessfullRemoval = (removedMarker != null);
        return sccessfullRemoval;
    }

    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkersNew) 
    {
        this.mapMarkers = new LinkedHashMap<>(mapMarkersNew);
    }

    //////////////////////// END OF MAP MARKERS ///////////////////////////////////


    ///////////////////////////////// ROIS ////////////////////////////////


    ////////////////////////// CHANNEL PROCESSING ///////////////////////////

    @Override
    public Range getChannelDataRange(String type) 
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        Map<String, Channel2D> channelsMap = getChannels(type);

        for (Channel2D channel : channelsMap.values()) 
        {
            double[][] xyzData = channel.getXYZView();

            Range range = ArrayUtilities.getRange(xyzData[2]);
            min = Math.min(min, range.getLowerBound());
            max = Math.max(max, range.getUpperBound());
        }

        Range range = new Range(min, max);
        return range;
    }

    public TypeModelManager getTypeModelManager()
    {
        return typeManager;
    }

    //this method is called when the channel data are modified, so that the ROI changes
    //handleChangeOfData() is called on DensityDialog after refreshROISamples()
    @Override
    public void notifyAboutROISampleChange(String type)
    {
        typeManager.registerROISampleLaggingType(type);
    }

    //adds or removed rois from the ROIManager, and in this way refreshes rois
    @Override
    public Map<String, Map<Object, QuantitativeSample>> refreshLaggingROISamples()
    {
        return typeManager.refreshLaggingROISamples();
    }

    @Override
    public void setAllROIsLagging(boolean lagging)
    {
        typeManager.setAllROIsLagging(lagging);
    }



    //ugly hack
    public MetaMap<String, String, Channel2D> notifyOfChanges(Collection<String> universalIdentifiers) 
    {
        MetaMap<String, String, Channel2D> changedTypes = new MetaMap<>();

        for(String type : getAllTypes())
        {
            Map<String, Channel2D> channelsForType = getChannels(type);

            for(String universalId : universalIdentifiers)
            {
                Channel2D channel = channelsForType.get(universalId);
                if(channel != null)
                {
                    changedTypes.put(type, universalId, channel);                   
                }
            }
        }

        for(String type : changedTypes.keySet())
        {
            notifyAboutROISampleChange(type);
        }

        return changedTypes;
    }

    public MetaMap<String, String, Channel2D> transformByIdentifier(Map<String, Channel2DDataTransformation> identifierTransformationMap) 
    {
        MetaMap<String, String, Channel2D> changedChannels = new MetaMap<>();

        Set<String> changedTypes = new LinkedHashSet<>();

        for(String type : getAllTypes())
        {
            Map<String, Channel2D> channelsForType = getChannels(type);

            for(Entry<String, Channel2DDataTransformation> entry : identifierTransformationMap.entrySet())
            {
                String universalId = entry.getKey();
                Channel2D channel = channelsForType.get(universalId);
                channel.transform(entry.getValue());

                changedTypes.add(type);                   
                changedChannels.put(type, universalId, channel);
            }
        }

        for(String type : changedTypes)
        {
            notifyAboutROISampleChange(type);
        }

        return changedChannels;
    }

    @Override
    public Map<String, Channel2D> transform(String type, Channel2DDataTransformation tr) 
    {
        Map<String, Channel2D> changedChannels = getChannels(type);


        for(Channel2D channel : changedChannels.values())
        {
            channel.transform(tr);
        }        

        notifyAboutROISampleChange(type);

        return changedChannels;
    }


    @Override
    public Map<String, Channel2D> transform(String type, Set<String> identifiers, Channel2DDataTransformation tr) 
    {
        Map<String, Channel2D> channelsForType = getChannels(type);
        Map<String, Channel2D> changedChannels = new LinkedHashMap<>();

        for(Entry<String, Channel2D> entry : channelsForType.entrySet())
        {
            Channel2D channel = entry.getValue();

            if(identifiers.contains(channel.getIdentifier()))
            {
                channel.transform(tr);
                changedChannels.put(entry.getKey(), channel);
            }
        }

        notifyAboutROISampleChange(type);


        return changedChannels;
    }

    @Override
    public Map<String, Channel2D> transform(String type, Channel2DDataInROITransformation tr, ROIRelativePosition position) 
    {
        ROI roi = new ROIComposite(getROIShapes(), "All");

        return transform(type, tr, roi, position);
    }

    @Override
    public Map<String, Channel2D> transform(String type, Channel2DDataInROITransformation tr, ROI roi, ROIRelativePosition position) 
    {
        Map<String, Channel2D> changedChannels = getChannels(type);

        for(Channel2D channel : changedChannels.values())
        {
            channel.transform(tr, roi, position);
        }

        notifyAboutROISampleChange(type);

        return changedChannels;
    }

    @Override
    public Map<String, Channel2D> transform(String type, Set<String> identifiers, Channel2DDataInROITransformation tr, ROI roi, ROIRelativePosition position) 
    {
        Map<String, Channel2D> channelsForType = getChannels(type);
        Map<String, Channel2D> changedChannels = new LinkedHashMap<>();

        for(Entry<String, Channel2D> entry : channelsForType.entrySet())
        {
            Channel2D channel = entry.getValue();

            if(identifiers.contains(channel.getIdentifier()))
            {
                channel.transform(tr, roi, position);
                changedChannels.put(entry.getKey(), channel);
            }
        }

        notifyAboutROISampleChange(type);


        return changedChannels;
    }

    @Override
    public Map<String, Channel2DData> getChannelData(String type)
    {
        Map<String, Channel2D> channels = getChannels(type);
        Map<String, Channel2DData> dataMap = new LinkedHashMap<>();

        for (Entry<String, Channel2D> entry : channels.entrySet()) 
        {
            String key = entry.getKey();

            Channel2D channel = entry.getValue();
            Channel2DData basicData = channel.getChannelData();
            dataMap.put(key, basicData);
        }

        return dataMap;
    }

    @Override
    public Map<String, Channel2D> setChannelData(String type, Map<String, Channel2DData> dataMap)
    {
        Map<String, Channel2D> changedChannels = getChannels(type);

        for (Entry<String, Channel2D> entry : changedChannels.entrySet()) 
        {
            String key = entry.getKey();
            Channel2D channel = entry.getValue();

            Channel2DData basicData = dataMap.get(key);
            if(basicData != null)
            {
                channel.setChannelData(basicData);           
            }
        }

        notifyAboutROISampleChange(type);

        return changedChannels;
    }

    /////////////////////////////// SAMPLES //////////////////////////////////////


    @Override
    public Map<Object, QuantitativeSample> getSamples(String type)
    {
        return typeManager.getSamples(type);
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> getSamples(boolean includeCoordinates)
    {
        return typeManager.getSamples(includeCoordinates);
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> getSamplesForROIs(boolean includeCoordinates)
    {
        return typeManager.getSamplesForROIsAndUpdate(includeCoordinates);
    }

    @Override
    public Map<Object, QuantitativeSample> getSamplesForROIs(String type)
    {
        return typeManager.getSamplesForROIsAndUpdate(type);
    }
    @Override
    public List<String> getAllTypes()
    {
        return typeManager.getTypes();
    }

    @Override
    public Map<String, QuantitativeSample> getROIUnionSamples() 
    {
        return typeManager.getAndUpdateROIUnionSamples();
    }

    @Override
    public Set<Channel2DSource<?>> getChannel2DSources() 
    {
        return typeManager.getSources();
    }

    @Override
    public boolean containsChannelsFromSource(ChannelSource source)
    {
        return typeManager.containsSource(source);
    }

    @Override
    public Set<ImageSource> getImageSources()
    {
        return typeManager.getChannel2DSources(ImageSource.class);
    }


    public Set<MapSource> getMapSources()
    {
        return typeManager.getChannel2DSources(MapSource.class);
    }

    @Override
    public MultiMap<Channel2DSource<?>, String> getSourceChannelIdentifierMaps()
    {
        MultiMap<Channel2DSource<?>, String> channels = typeManager.getChannelIdentifiers();

        return channels;
    }

    @Override
    public Map<Channel2DSource<?>, List<Channel2D>> getSourceChannelMap(String type)
    {
        return typeManager.getResourceChannelMap(type);
    }

    @Override
    public String duplicate(String type)
    {
        String typeNew = typeManager.duplicate(type);
        return typeNew;
    }

    @Override
    public void registerChannel(String type, Channel2DSource<?> source, String identifier)
    {
        typeManager.registerIdentifier(type, source, identifier);         
    }

    public void registerChannels(Channel2DSource<?> source, List<String> identifiers)
    {
        for(String id : identifiers)
        {
            registerChannel(id, source, id);
        }
    }

    @Override
    public Set<String> getIdentifiers(String type)
    {
        return typeManager.getIdentifiers(type);
    }

    @Override
    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return typeManager.getIdentifiers(type, filter);
    }

    @Override
    public Set<String> getIdentifiersForAllTypes()
    {
        return typeManager.getIdentifiersForAllTypes();
    }

    @Override
    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel2D> filter)
    {
        return typeManager.getIdentifiersForAllTypes(filter);
    }

    @Override 
    public Set<String> getAllIdentifiers()
    {
        return typeManager.getAllIdentifiers();
    }

    @Override
    public Map<String, PrefixedUnit> getIdentifierUnitMap()
    {
        return typeManager.getIdentifierUnitMap();
    }

    @Override
    public Map<Object, ROIDrawable> getROIs()
    {
        return typeManager.getROIs();
    }

    @Override
    public ROI getROIUnion()
    {
        return typeManager.getROIUnion();
    }

    @Override
    public List<Shape> getROIShapes() 
    {       
        return typeManager.getROIShapes();
    }

    @Override
    public boolean areROIsAvailable()
    {
        return typeManager.areROIsAvailable();
    }

    @Override
    public void addOrReplaceROI(ROIDrawable roi)
    {
        typeManager.addOrReplaceROI(roi);
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> addOrReplaceROIAndUpdate(ROIDrawable roi)
    {
        return typeManager.addOrReplaceROIAndUpdate(roi);
    }

    @Override
    public void removeROI(ROIDrawable roi)
    {
        typeManager.removeROI(roi);
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> removeROIAndUpdate(ROIDrawable roi)
    {
        return typeManager.removeROIAndUpdate(roi);
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> setROIsAndUpdate(Map<Object, ROIDrawable> rois) 
    {
        return typeManager.setROIsAndUpdate(rois);
    } 

    @Override
    public void setROIs(Map<Object, ROIDrawable> rois)
    {
        typeManager.setROIs(rois);
    }

    @Override
    public MetaMap<String, Object, QuantitativeSample> changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        return typeManager.changeROILabel(roiKey, labelOld, labelNew);
    }

    @Override
    public List<SampleCollection> getSampleCollection(Map<Object, ? extends ROI> rois, boolean includeCoordinates)
    {
        return typeManager.getSampleCollection(rois, includeCoordinates);
    }

    @Override
    public List<SampleCollection> getSampleCollection2(Map<Object, ? extends ROI> rois, boolean includeCoordinates)
    {
        return typeManager.getSampleCollection2(rois, includeCoordinates);
    }

    /////////////////////////////// CROSS-SECTIONS ///////////////////////////////

    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Profile profile, String type)
    {
        Map<String, ChannelSectionLine> allCrossSections = new LinkedHashMap<>();

        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {           
            Map<String, ChannelSectionLine> sections = densitySource.getCrossSections(profile.getDistanceShape(), profile.getKey(), profile.getLabel(), type);
            allCrossSections.putAll(sections);
        }

        return allCrossSections;
    }

    @Override
    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Map<Object, Profile> profiles, String type)
    {
        Map<String, Map<Object, ChannelSectionLine>> allCrossSections = new LinkedHashMap<>();

        for(Channel2DSource<?> source: getChannel2DSources())
        {           
            for(Profile profile: profiles.values())
            {
                Object key = profile.getKey();

                Map<String, ChannelSectionLine> sections =
                        source.getCrossSections(profile.getDistanceShape(), profile.getKey(),
                                profile.getLabel(), type);
                for(Entry<String, ChannelSectionLine> entry : sections.entrySet())
                {
                    ChannelSectionLine section = entry.getValue();

                    Map<Object, ChannelSectionLine> crossSectionsForType = allCrossSections.get(type);
                    if(crossSectionsForType == null)
                    {
                        crossSectionsForType = new LinkedHashMap<>();
                        allCrossSections.put(type, crossSectionsForType);
                    }

                    crossSectionsForType.put(key, section);
                }
            }
        }
        return allCrossSections;
    }

    @Override
    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Map<Object, Profile> profiles, Set<String> types)
    {
        Map<String, Map<Object, ChannelSectionLine>> allCrossSections = new LinkedHashMap<>();

        for(Channel2DSource<?> imageSource: getChannel2DSources())
        {   
            for(String type : types)
            {
                for(Profile profile: profiles.values())
                {
                    Object key = profile.getKey();
                    Map<String, ChannelSectionLine> sections = imageSource.getCrossSections(profile.getDistanceShape(), profile.getKey(), profile.getLabel(), type);
                    for(Entry<String, ChannelSectionLine> entry : sections.entrySet())
                    {
                        ChannelSectionLine section = entry.getValue();

                        Map<Object, ChannelSectionLine> crossSectionsForType = allCrossSections.get(type);
                        if(crossSectionsForType == null)
                        {
                            crossSectionsForType = new LinkedHashMap<>();
                            allCrossSections.put(type, crossSectionsForType);
                        }
                        crossSectionsForType.put(key, section);
                    }
                }
            }         
        }
        return allCrossSections;
    }

    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Map<Object, Profile> profiles)
    {
        Map<String, Map<Object, ChannelSectionLine>> allCrossSections = new LinkedHashMap<>();

        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {           
            for(Profile profile: profiles.values())
            {
                Object key = profile.getKey();
                Map<String, ChannelSectionLine> sections = 
                        densitySource.getCrossSections(profile.getDistanceShape(), profile.getKey(), profile.getLabel());
                for(Entry<String, ChannelSectionLine> entry : sections.entrySet())
                {
                    String type = entry.getKey();
                    ChannelSectionLine section = entry.getValue();

                    Map<Object, ChannelSectionLine> crossSectionsForType = allCrossSections.get(type);
                    if(crossSectionsForType == null)
                    {
                        crossSectionsForType = new LinkedHashMap<>();
                        allCrossSections.put(type, crossSectionsForType);
                    }
                    crossSectionsForType.put(key, section);
                }
            }
        }
        return allCrossSections;
    }


    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Profile profile)
    {
        Map<String, ChannelSectionLine> crossSections = new LinkedHashMap<>();
        MultiMap<Channel2DSource<?>, String> sourceIdentifierMap = typeManager.getChannelIdentifiers();

        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {           
            Set<String> identifiers = new LinkedHashSet<>(sourceIdentifierMap.get(densitySource));
            Map<String, ChannelSectionLine> sections = densitySource.getCrossSections(profile.getDistanceShape(), profile.getKey(), profile.getLabel(), identifiers);
            crossSections.putAll(sections);
        }

        return crossSections;
    }

    @Override
    public Map<String, ChannelSectionLine> getHorizontalCrossSections(double level, Object key, String name, String type)
    {
        Map<String, ChannelSectionLine> allCrossSections = new LinkedHashMap<>();

        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {           
            Map<String, ChannelSectionLine> sections = densitySource.getHorizontalCrossSections(level, key, name, type);
            allCrossSections.putAll(sections);
        }

        return allCrossSections;
    }

    @Override
    public List<ChannelSectionLine> getHorizontalCrossSections(DataAxis1D verticalAxis, Object key, String name, String type)
    {
        List<ChannelSectionLine> sections = null;
        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {      
            if(densitySource.getIdentifiers().contains(type))
            {
                sections = densitySource.getHorizontalCrossSections(verticalAxis, key, name, type);
            }
        }

        return sections;
    }

    @Override
    public List<ChannelSectionLine> getVerticalCrossSections(DataAxis1D horizontalAxis, Object key, String name, String type)
    {
        List<ChannelSectionLine> sections = null;
        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {           
            if(densitySource.getIdentifiers().contains(type))
            {
                sections = densitySource.getVerticalCrossSections(horizontalAxis, key, name, type);
            }
        }

        return sections;
    }  

    @Override
    public Map<String, ChannelSectionLine> getVerticalCrossSections(double level, Object key, String name, String type)
    {
        Map<String, ChannelSectionLine> allCrossSections = new LinkedHashMap<>();

        for(Channel2DSource<?> densitySource: getChannel2DSources())
        {           
            Map<String, ChannelSectionLine> sections = densitySource.getVerticalCrossSections(level, key, name, type);
            allCrossSections.putAll(sections);
        }

        return allCrossSections;
    }

    @Override
    public Map<Object, DistanceShapeFactors> getProfileGemetries()
    {
        Map<Object, DistanceShapeFactors> geometries = new LinkedHashMap<>();
        Map<Object, Profile> profiles = getProfiles();

        for(Entry<Object, Profile> entry :  profiles.entrySet())
        {
            Object key = entry.getKey();
            DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
            geometries.put(key, line);
        }

        return geometries;
    }

    //copied
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod)
    {
        boolean interpolated = true;

        for(Channel2DSource<?> source : getChannel2DSources())
        {
            boolean interpolationFunctionBuilt = source.isInterpolationPreparationNecessary(interpolationMethod);

            interpolated = interpolated && interpolationFunctionBuilt;

            if(!interpolated)
            {
                break;
            }
        }
        return interpolated;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        propertyChangeSupport.firePropertyChange(evt);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);

    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);

    }
}
