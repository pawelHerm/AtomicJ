
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
import java.util.Arrays;
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

import atomicJ.analysis.AdhesionEventEstimate;
import atomicJ.analysis.ForceEventEstimate;
import atomicJ.analysis.ForceEventEstimator;
import atomicJ.analysis.AdhesionForceNullEstimate;
import atomicJ.analysis.ManualAdhesionForceEstimator;
import atomicJ.analysis.ManualContactEstimator;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.analysis.VisualizableSpectroscopyPack;
import atomicJ.analysis.VisualizationChartSettings;
import atomicJ.analysis.VisualizationSettings;
import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.SeparationTransformation;
import atomicJ.curveProcessing.Translate1DTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.Datasets;
import atomicJ.data.IndentationCurve;
import atomicJ.data.PointwiseModulusCurve;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.curveProcessing.ProcessingBatchMemento;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoManager;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public class StandardSpectroscopyProcessedResource implements SpectroscopyProcessedResource
{
    public static final String POINTWISE_MODULUS = "Pointwise modulus";
    public static final String RECORDED_CURVE = "Recorded curve";
    public static final String INDENTATION = "Indentatation";

    private final File file;
    private final String shortName;
    private final String longName;

    private final UndoManager undoManager = new UndoManager(2);

    private final MetaMap<String, Object, DistanceMeasurementDrawable> distanceMeasurements = new MetaMap<>();
    //meta map would be better here
    private final MultiMap<String, Channel1D> channelsTypeMap = new MultiMap<>();
    private final MetaMap<String, Object, Integer> channelGroupNextIdNumbers = new MetaMap<>();

    private final ProcessedSpectroscopyPack processedPack;

    public StandardSpectroscopyProcessedResource(VisualizableSpectroscopyPack visualizablePack)
    {
        this(visualizablePack.getPackToVisualize(), visualizablePack.getRecordedCurves(), visualizablePack.getIndentationCurve(), visualizablePack.getPointwiseModulusCurve());
    }

    public StandardSpectroscopyProcessedResource(ProcessedSpectroscopyPack processedPack, SpectroscopyCurve<?> recordedCurve, IndentationCurve indentationCurve,
            PointwiseModulusCurve modulusCurve)
    {
        this.file = processedPack.getSource().getCorrespondingFile();
        this.shortName = processedPack.getSource().getShortName();
        this.longName = processedPack.getSource().getLongName();

        if(recordedCurve != null)
        {
            this.channelsTypeMap.putAll(RECORDED_CURVE, recordedCurve.getChannels());
        }        
        if(indentationCurve != null)
        {
            this.channelsTypeMap.putAll(INDENTATION, indentationCurve.getChannels());
        }    
        if(modulusCurve != null)
        {
            this.channelsTypeMap.putAll(POINTWISE_MODULUS, modulusCurve.getChannels());
        }

        registerChannelGroupTags();

        this.processedPack = processedPack;
    }

    public StandardSpectroscopyProcessedResource(StandardSpectroscopyProcessedResource that)
    {
        this.file = that.file;
        this.longName = that.getLongName();
        this.shortName = that.getShortName();
        this.processedPack = that.processedPack.copy();
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

    private VisualizationChartSettings getRecordedCurveSettings()
    {
        boolean show = !getIdentifiers(RECORDED_CURVE).isEmpty();
        boolean plotFit = getIdentifiers(RECORDED_CURVE).contains(Datasets.FIT);
        VisualizationChartSettings settings = new VisualizationChartSettings(show, plotFit);

        return settings;
    }

    private VisualizationChartSettings getIndentationSettings()
    {
        boolean show = !getIdentifiers(INDENTATION).isEmpty();
        boolean plotFit = getIdentifiers(INDENTATION).contains(Datasets.INDENTATION_FIT);
        VisualizationChartSettings settings = new VisualizationChartSettings(show, plotFit);

        return settings;
    }

    private VisualizationChartSettings getPointwiseModulusSettings()
    {
        boolean show = !getIdentifiers(POINTWISE_MODULUS).isEmpty();
        boolean plotFit = getIdentifiers(POINTWISE_MODULUS).contains(Datasets.POINTWISE_MODULUS_FIT);
        VisualizationChartSettings settings = new VisualizationChartSettings(show, plotFit);

        return settings;
    }

    private VisualizationSettings getVisualizationSettings()
    {
        return new VisualizationSettings(getRecordedCurveSettings(), getIndentationSettings(), getPointwiseModulusSettings());
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
    public Optional<Channel1DDataTransformation> getTransformationToForceSeparationIfPossible()
    {
        UnitExpression springConstant = processedPack.getProcessingSettings().getSpringConstantWithUnit();
        Channel1D pointChannel = getChannel(RECORDED_CURVE, Datasets.CONTACT_POINT);

        if(pointChannel != null && !pointChannel.isEmpty() & springConstant.isWellFormed())
        {
            double xContact = pointChannel.getX(0);
            double yContact = pointChannel.getY(0);

            Channel1DDataTransformation tr = new SeparationTransformation(xContact, yContact, springConstant.getInverse());
            return Optional.of(tr);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Channel1DDataTransformation> getFixContactXTransformationIfPossible()
    {
        Channel1D pointChannel = getChannel(RECORDED_CURVE, Datasets.CONTACT_POINT);

        if(pointChannel != null && !pointChannel.isEmpty())
        {
            Channel1DDataTransformation tr = new Translate1DTransformation(-pointChannel.getX(0), 0);
            return Optional.of(tr);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Channel1DDataTransformation> getFixContactYTransformationIfPossible()
    {
        Channel1D pointChannel = getChannel(RECORDED_CURVE, Datasets.CONTACT_POINT);

        if(pointChannel != null && !pointChannel.isEmpty())
        {
            Channel1DDataTransformation tr = new Translate1DTransformation(0, -pointChannel.getY(0));
            return Optional.of(tr);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Channel1DDataTransformation> getFixContactAllAxesTransformationIfPossible()
    {
        Channel1D pointChannel = getChannel(RECORDED_CURVE, Datasets.CONTACT_POINT) ;

        if(pointChannel != null && !pointChannel.isEmpty())
        {
            Channel1DDataTransformation tr = new Translate1DTransformation(-pointChannel.getX(0), -pointChannel.getY(0));
            return Optional.of(tr);
        }

        return Optional.empty();
    }

    @Override
    public boolean isCorrespondingMapPositionKnown()
    {
        return processedPack.isCorrespondingMapPositionKnown();
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
        return file;
    }

    @Override
    public boolean containsChannelsFromSource(ChannelSource source)
    {
        boolean contains = (source != null) ? this.processedPack.getSource().equals(source) : false;
        return contains;
    }

    public StandardSpectroscopyProcessedResource copy()
    {
        StandardSpectroscopyProcessedResource copy = new StandardSpectroscopyProcessedResource(this);
        return copy;
    }

    @Override
    public SimpleSpectroscopySource getSource()
    {
        return processedPack.getSource();
    }

    @Override
    public List<SampleCollection> getSampleCollectionsRawData()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        for(Channel1D channel : channelsTypeMap.allValues())
        {
            samples.putAll(channel.getSamples());
        }

        SampleCollection collection = new StandardSampleCollection(samples, getShortName(), getShortName(), getDefaultOutputLocation());
        collection.setKeysIncluded(true);

        return Collections.singletonList(collection);
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {        
        return processedPack.getSource().getSampleCollections();
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
        return processedPack.getSource().getAutomaticChartTitles();
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
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {            
            if(Objects.equals(identifier, ch.getIdentifier()))
            {
                return ch;
            }
        }

        return null;
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
    public void itemMoved(String selectedType, Channel1D channel, int itemIndex, double[] newValue)
    {       
        Object channelIdentifier = channel.getIdentifier();

        ChannelGroupTag groupTag = channel.getGroupTag();
        Object groupTagId = (groupTag != null) ? groupTag.getGroupId() : null;

        if(Datasets.CONTACT_POINT.equals(channelIdentifier))
        {
            modifyContactPoint(newValue[0], newValue[1]);
        }
        else if(Arrays.asList(Datasets.MODEL_TRANSITION_POINT,Datasets.MODEL_TRANSITION_POINT_FORCE_CURVE,Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE,Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS).contains(channelIdentifier))
        {
            modifyTransitionIndentation(selectedType, newValue[0], newValue[1]);
        }
        else if(Datasets.ADHESION_FORCE.equals(groupTagId))
        {
            int adhesionEventIndex = groupTag.getIndex();            
            modifyAdhesionEstimate(adhesionEventIndex, itemIndex, newValue[0], newValue[1]);
        }
    }

    private void modifyTransitionIndentation(String type, double transitionX, double transitionY)
    {
        double contactXDisplayed = getChannel(RECORDED_CURVE, Datasets.CONTACT_POINT).getX(0);
        Point2D contactPointTrue = processedPack.getDeflectionContactPoint();

        double displayXShift = contactXDisplayed - contactPointTrue.getX();

        SimpleSpectroscopySource source = processedPack.getSource(); 

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(contactPointTrue.getX(), contactPointTrue.getY()));

        double fitZMinimum = RECORDED_CURVE.equals(type) ? transitionX - displayXShift: Double.NEGATIVE_INFINITY;
        double fitIndentationLimit = RECORDED_CURVE.equals(type) ? Double.POSITIVE_INFINITY : transitionX;

        model.setFitZMinimum(fitZMinimum);
        model.setFitIndentationLimit(fitIndentationLimit);

        int failures = recalculate(model, sourcesNewVsOld);

        if(failures > 0)
        {
            revertModifications();
        }
    }

    private void modifyContactPoint(double x, double y)
    {   
        SimpleSpectroscopySource source = processedPack.getSource();

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(x, y/(1000*memento.getSpringConstant())));

        int failures = recalculate(model, sourcesNewVsOld);

        if(failures > 0)
        {
            revertModifications();
        }
    }

    private void modifyAdhesionEstimate(int adhesionEventIndex, int itemIndex, double z, double f)
    {
        //        double contactXDisplayed = getChannel(RECORDED_CURVE, DatasetNames.CONTACT_POINT).getX(0);
        Point2D contactPointTrue = processedPack.getDeflectionContactPoint();

        //        double displayXShift = contactXDisplayed - contactPointTrue.getX();

        SimpleSpectroscopySource source = processedPack.getSource();

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        ForceEventEstimate adhesionEstimateOld = processedPack.getAdhesionEstimate(adhesionEventIndex);

        ForceEventEstimate adhesionEstimateNew = (itemIndex == 0) ? adhesionEstimateOld.shiftMarkerStart(z, f) : adhesionEstimateOld.shiftMarkerEnd(z, f);

        List<ForceEventEstimate> adhesionEstimatesNew = new ArrayList<>(processedPack.getAdhesionEstimates());
        adhesionEstimatesNew.set(adhesionEventIndex, adhesionEstimateNew);

        ForceEventEstimator adhesionForceEstimator = new ManualAdhesionForceEstimator(adhesionEstimatesNew);
        model.setAdhesionForceEstimator(adhesionForceEstimator);

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(contactPointTrue.getX(), contactPointTrue.getY()));

        int failures = recalculate(model, sourcesNewVsOld);

        if(failures > 0)
        {
            revertModifications();
        }
    }

    @Override
    public void channelTranslated(Channel1D channel)
    {
        ChannelGroupTag groupTag = channel.getGroupTag();
        Object groupTagId = (groupTag != null) ? groupTag.getGroupId() : null;

        if(Datasets.ADHESION_FORCE.equals(groupTagId))
        {
            int adhesionEventIndex = groupTag.getIndex();

            modifyAdhesionEstimate(adhesionEventIndex, channel.getX(0), channel.getY(0),channel.getX(1), channel.getY(1));
        }
    }

    private void modifyAdhesionEstimate(int adhesionEventIndex, double adhesionZ, double adhesionF, double liftOffZ, double liftOffF)
    {
        Point2D contactPointTrue = processedPack.getDeflectionContactPoint();

        SimpleSpectroscopySource source = processedPack.getSource();

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        AdhesionEventEstimate adhesionEstimateNew = new AdhesionEventEstimate(adhesionF, adhesionZ, liftOffZ, liftOffF);
        List<ForceEventEstimate> adhesionEstimatesNew = new ArrayList<>(processedPack.getAdhesionEstimates());
        adhesionEstimatesNew.set(adhesionEventIndex, adhesionEstimateNew);

        ForceEventEstimator adhesionForceEstimator = new ManualAdhesionForceEstimator(adhesionEstimatesNew);
        model.setAdhesionForceEstimator(adhesionForceEstimator);

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(contactPointTrue.getX(), contactPointTrue.getY()));

        int failures = recalculate(model, sourcesNewVsOld);

        if(failures > 0)
        {
            revertModifications();
        }
    }

    private void addAdhesionEstimate(double adhesionZ, double adhesionF, double liftOffZ, double liftOffF)
    {
        Point2D contactPointTrue = processedPack.getDeflectionContactPoint();

        SimpleSpectroscopySource source = processedPack.getSource();

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        AdhesionEventEstimate adhesionEstimateNew = new AdhesionEventEstimate(adhesionF, adhesionZ, liftOffZ, liftOffF);
        List<ForceEventEstimate> adhesionEstimatesNew = new ArrayList<>(processedPack.getAdhesionEstimates());
        adhesionEstimatesNew.add(adhesionEstimateNew);

        ForceEventEstimator adhesionForceEstimator = new ManualAdhesionForceEstimator(adhesionEstimatesNew);
        model.setAdhesionForceEstimator(adhesionForceEstimator);

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(contactPointTrue.getX(), contactPointTrue.getY()));

        int failures = recalculate(model, sourcesNewVsOld);

        if(failures > 0)
        {
            revertModifications();
        }
    }

    private void removeAdhesionEstimate(int index)
    {
        Point2D contactPointTrue = processedPack.getDeflectionContactPoint();

        SimpleSpectroscopySource source = processedPack.getSource();

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        ForceEventEstimate adhesionEstimateNew = AdhesionForceNullEstimate.getInstance();
        List<ForceEventEstimate> adhesionEstimatesNew = new ArrayList<>(processedPack.getAdhesionEstimates());
        adhesionEstimatesNew.set(index, adhesionEstimateNew);

        ForceEventEstimator adhesionForceEstimator = new ManualAdhesionForceEstimator(adhesionEstimatesNew);
        model.setAdhesionForceEstimator(adhesionForceEstimator);

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(contactPointTrue.getX(), contactPointTrue.getY()));

        int failures = recalculate(model, sourcesNewVsOld);

        if(failures > 0)
        {
            revertModifications();
        }
    }


    private void revertModifications()
    {
        SimpleSpectroscopySource source = processedPack.getSource(); 
        Point2D contactPointTrue = processedPack.getDeflectionContactPoint();

        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld = StandardSimpleSpectroscopySource.copySourcesInPlace(Collections.singletonList(source));

        ProcessingBatchMemento memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        model.setContactPointAutomatic(false);
        model.setManualContactEstimator(new ManualContactEstimator(contactPointTrue.getX(), contactPointTrue.getY()));

        recalculate(model, sourcesNewVsOld);
    }

    @Override
    public Point2D getValidItemPosition(String type, String channelIdentifier, int itemIndex, Point2D dataPoint)
    {
        Channel1D channel = getChannel(type, channelIdentifier);

        return (channel != null) ? channel.constrain(getChannels(type), itemIndex, dataPoint) : dataPoint;
    }

    @Override
    public boolean isValidPosition(String type, String channelIdentifier, int itemIndex, Point2D dataPoint)
    {
        Channel1D channel = getChannel(type, channelIdentifier);

        return (channel != null) ? channel.isValidPosition(channel, getChannels(type), itemIndex, dataPoint) : true;
    }

    private int recalculate(ProcessingBatchModel model, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld)
    {
        SpectroscopyResultDestination resultDestination = AtomicJ.getResultDestination();
        int failureCount = resultDestination.recalculate(model, sourcesNewVsOld);
        return failureCount;
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
    public void addChannel(String type, Channel1D channel) 
    {
        this.channelsTypeMap.put(type, channel);

        registerChannelGroupTags();

        ChannelGroupTag groupTag = channel.getGroupTag();
        Object groupTagId = (groupTag != null) ? groupTag.getGroupId() : null;

        if(Datasets.ADHESION_FORCE.equals(groupTagId))
        {
            addAdhesionEstimate(channel.getX(0), channel.getY(0), channel.getX(1), channel.getY(1));
        }
    }

    @Override
    public void removeChannel(String type, Channel1D channel)
    {
        this.channelsTypeMap.remove(type, channel);

        ChannelGroupTag groupTag = channel.getGroupTag();
        if(groupTag == null)
        {
            return;
        }

        Object groupTagId = groupTag.getGroupId();
        int index = groupTag.getIndex();

        if(Datasets.ADHESION_FORCE.equals(groupTagId))
        {
            removeAdhesionEstimate(index);
        }
    }

    @Override
    public ChannelGroupTag  getNextGroupMemberIdentity(String type, Object groupKey)
    {
        int index = channelGroupNextIdNumbers.containsKeyPair(type, groupKey) ? channelGroupNextIdNumbers.get(type, groupKey): 0;
        return new ChannelGroupTag(groupKey, index);
    }
}
