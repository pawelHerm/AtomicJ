package atomicJ.gui.stack;

import java.awt.Shape;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;

import org.jfree.data.Range;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.DataAxis1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.MapMarker;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.profile.ChannelSectionLine;
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
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.CrossSectionResource;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public class StackQuasiResource extends AbstractModel implements PropertyChangeListener, Channel2DResource
{
    private final String type;
    private final Channel2D channel;
    private final UndoManager undoManager = new UndoManager(2);

    public StackQuasiResource(Channel2D channel, String type)
    {
        this.channel = channel;
        this.type = type;
    }

    @Override
    public Set<String> getIdentifiers(String type)
    {
        Set<String> identifiers = Objects.equals(this.type, type) ? Collections.singleton(channel.getIdentifier()) : Collections.<String>emptySet();

        return identifiers;
    }

    @Override
    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = Objects.equals(this.type, type) && filter.accepts(channel) ? Collections.singleton(channel.getIdentifier()) : Collections.<String>emptySet();

        return identifiers;
    }

    @Override
    public Set<String> getIdentifiersForAllTypes()
    {
        return Collections.singleton(channel.getIdentifier());
    }

    @Override
    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = filter.accepts(channel) ? Collections.singleton(channel.getIdentifier()) : Collections.<String>emptySet();

        return identifiers;
    }


    @Override
    public MultiMap<String, Channel2D> getChannelsForIdentifiers(Set<String> identifiers) 
    {
        MultiMap<String, Channel2D> channelIdentifierMap = new MultiMap<>();
        String channelIdentifier = channel.getIdentifier();

        if(identifiers.contains(channelIdentifier))
        {
            channelIdentifierMap.put(channelIdentifier, channel);
        }

        return channelIdentifierMap;
    }

    @Override
    public Map<String, Channel2D> getChannels(String type)
    {  
        Map<String, Channel2D> channelMap = Objects.equals(this.type, type) ? Collections.singletonMap(channel.getIdentifier(), channel) : Collections.<String, Channel2D>emptyMap();
        return channelMap;
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
    public Map<String, Channel2D> transform(String type,Channel2DDataInROITransformation tr, ROIRelativePosition position) {
        ROI roi = new ROIComposite(getROIShapes(), "All");

        return transform(type, tr, roi, position);    }


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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
    }

    @Override
    public String getShortName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLongName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsChannelsFromSource(ChannelSource source) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public File getDefaultOutputLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getAutomaticChartTitles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SampleCollection> getSampleCollections() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getAllIdentifiers() 
    {
        return Collections.singleton(channel.getIdentifier());
    }

    @Override
    public List<String> getAllTypes() 
    {
        return Collections.singletonList(type);
    }

    @Override
    public Map<String, PrefixedUnit> getIdentifierUnitMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerChannel(String type, Channel2DSource<?> source,
            String identifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public MultiMap<Channel2DSource<?>, String> getSourceChannelIdentifierMaps() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Channel2DSource<?>, List<Channel2D>> getSourceChannelMap(
            String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String duplicate(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Channel2DSource<?>> getChannel2DSources() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<ImageSource> getImageSources() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Range getChannelDataRange(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, PrefixedUnit> getDataUnits(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrefixedUnit getSingleDataUnit(String type) 
    {
        PrefixedUnit unit = Objects.equals(type, this.type) ? channel.getZQuantity().getUnit() : null;
        return unit;
    }

    @Override
    public ROI getROIUnion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean areROIsAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<Object, ROIDrawable> getROIs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Shape> getROIShapes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOrReplaceROI(ROIDrawable roi) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeROI(ROIDrawable roi) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setROIs(Map<Object, ROIDrawable> rois) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAllROIsLagging(boolean allROIsLagging) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> addOrReplaceROIAndUpdate(
            ROIDrawable roi) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> removeROIAndUpdate(
            ROIDrawable roi) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> setROIsAndUpdate(
            Map<Object, ROIDrawable> rois) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> refreshLaggingROISamples() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void notifyAboutROISampleChange(String type) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<Object, QuantitativeSample> getSamples(String type) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public Map<String, QuantitativeSample> getROIUnionSamples() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MetaMap<String, Object, QuantitativeSample> changeROILabel(
            Object roiKey, String labelOld, String labelNew) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SampleCollection> getSampleCollection(boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SampleCollection> getSampleCollection(
            Map<Object, ? extends ROI> shapes, boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SampleCollection> getSampleCollection2(
            Map<Object, ? extends ROI> shapes, boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SampleCollection> getROISampleCollections(
            boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SampleCollection> getROISampleCollections2(
            boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> getSamples(
            boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> getSamplesForROIs(
            boolean includeCoordinates) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Object, QuantitativeSample> getSamplesForROIs(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Object, DistanceShapeFactors> getDistanceMeasurementGeometries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOrReplaceDistanceMeasurement(
            DistanceMeasurementDrawable measurement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeDistanceMeasurement(
            DistanceMeasurementDrawable measurement) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDistanceMeasurements(
            Map<Object, DistanceMeasurementDrawable> measurements) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<Object, Profile> getProfiles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Profile getProfile(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Object, DistanceShapeFactors> getProfileGemetries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOrReplaceProfile(Profile profile,
            CrossSectionsReceiver receiver, CrossSectionResource resource,
            Window taskParent) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, ChannelSectionLine> addOrReplaceProfile(Profile profile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, ChannelSectionLine> removeProfile(Profile profile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, ChannelSectionLine>> setProfiles(
            Map<Object, Profile> profiles) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addProfileKnob(Object profileKey, double knobPosition) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean moveProfileKnob(Object profileKey, int knobIndex,
            double knobPositionNew) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeProfileKnob(Object profileKey, double knobPosition) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setProfileKnobs(Object profileKey, List<Double> knobPositions) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<String, ChannelSectionLine> getHorizontalCrossSections(
            double level, Object key, String name, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, ChannelSectionLine> getVerticalCrossSections(
            double level, Object key, String name, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CrossSectionResource getCrossSectionResource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChannelSectionLine> getHorizontalCrossSections(DataAxis1D verticalAxis, Object key, String name, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChannelSectionLine> getVerticalCrossSections(DataAxis1D horizontalAxis, Object key, String name, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Profile profile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, ChannelSectionLine> getCrossSections(Profile profile,
            String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(
            Map<Object, Profile> profiles, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(
            Map<Object, Profile> profiles, Set<String> types) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Object, MapMarker> getMapMarkers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean removeMapMarker(MapMarker mapMarker) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkersNew) {
        // TODO Auto-generated method stub

    }

}
