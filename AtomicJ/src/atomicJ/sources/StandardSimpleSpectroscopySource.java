
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

import static atomicJ.data.Datasets.RAW_CURVE;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Quantities;
import atomicJ.data.GridIndex;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.SimpleSpectroscopyCurve;
import atomicJ.data.StandardSample;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.ProcessingBatchMemento;
import atomicJ.readers.MapDelayedCreator;
import atomicJ.readers.ReadingPack;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.MultiMap;


public class StandardSimpleSpectroscopySource extends AbstractChannelSource<Channel1D> implements SimpleSpectroscopySource
{
    private static final String APPROACH_Y = "Approach Y";
    private static final String APPROACH_X = "Approach X";
    private static final String WITHDRAW_Y = "Withdraw Y";
    private static final String WITHDRAW_X = "Withdraw X";

    private double sensitivityReadIn = Double.NaN;
    private double springConstantReadIn = Double.NaN;
    private final CalibrationState calibrationState;

    private PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.VOLTAGE;
    private final Channel1D deflectionApproachChannel;
    private final Channel1D deflectionWithdrawChannel;

    private boolean partOfMap = false;
    private MapSource<?> mapSource;
    private MapDelayedCreator mapDelayedCreator;
    private Point2D recordingPoint;
    private int mapPosition;

    private ReadingPack<ImageSource> accompanyingImageInfo;
    private List<BasicSpectroscopySource> additionalCurveRecordings = Collections.emptyList();

    private ProcessingBatchMemento memento;

    public StandardSimpleSpectroscopySource(File file, Channel1DData deflectionApproachData, Channel1DData deflectionWithdrawData)
    {
        this(file, IOUtilities.getBareName(file), file.getAbsolutePath(), deflectionApproachData, deflectionWithdrawData);
    }

    public StandardSimpleSpectroscopySource(String pathname, String shortName, String longName, Channel1DData deflectionApproachData, Channel1DData deflectionWithdrawData)
    {
        this(new File(pathname), shortName, longName, deflectionApproachData, deflectionWithdrawData);
    }

    public StandardSimpleSpectroscopySource(File f, String shortName, String longName, Channel1DData deflectionApproachData, Channel1DData deflectionWithdrawData)
    {
        super(f, shortName, longName);

        this.deflectionApproachChannel = new Channel1DStandard(deflectionApproachData, APPROACH);
        this.deflectionWithdrawChannel = new Channel1DStandard(deflectionWithdrawData, WITHDRAW);       
        this.calibrationState = CalibrationState.getCalibrationState(deflectionApproachChannel, deflectionWithdrawChannel);
    }

    public StandardSimpleSpectroscopySource(StandardSimpleSpectroscopySource sourceOld)
    {
        super(sourceOld);

        this.deflectionApproachChannel = sourceOld.deflectionApproachChannel.getCopy();
        this.deflectionWithdrawChannel = sourceOld.deflectionWithdrawChannel.getCopy();

        this.calibrationState = sourceOld.calibrationState;
        this.sensitivityReadIn = sourceOld.sensitivityReadIn;
        this.photodiodeSignalType = sourceOld.photodiodeSignalType;
        this.springConstantReadIn = sourceOld.springConstantReadIn;

        this.partOfMap = sourceOld.partOfMap;

        Point2D oldPoint = sourceOld.recordingPoint;
        this.recordingPoint = (oldPoint == null) ? null : new Point2D.Double(oldPoint.getX(), oldPoint.getY());
        this.mapPosition = sourceOld.mapPosition;
        this.mapSource = null;
    }

    @Override
    public StandardSimpleSpectroscopySource copy()
    {
        StandardSimpleSpectroscopySource copy = new StandardSimpleSpectroscopySource(this);
        return copy;
    }

    @Override
    public ProcessingBatchMemento getProcessingMemento()
    {
        return memento;
    }

    @Override
    public void setProcessingMemento(ProcessingBatchMemento memento)
    {
        this.memento = memento;
    }

    @Override
    public SimpleSpectroscopyCurve getRecordedCurve()
    {
        SimpleSpectroscopyCurve afmCurve = new SimpleSpectroscopyCurve(deflectionApproachChannel, deflectionWithdrawChannel, RAW_CURVE);
        return afmCurve;
    }

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value, 
    //and sensitivity in micron/V, i.e. 10^6 times more than the SI value
    @Override
    public SimpleSpectroscopyCurve getRecordedPhotodiodeCurve(double sensitivity, double springConstant) throws UserCommunicableException
    {
        Quantity rangeQuantity = getPhotodiodeQuantity();

        double factor = calibrationState.getPhotodiodeValueConversionFactor(sensitivityReadIn, springConstantReadIn, sensitivity, springConstant);
        Channel1D approach = new Channel1DStandard(deflectionApproachChannel.getChannelData().getCopy(factor, rangeQuantity), APPROACH);
        Channel1D withdraw = new Channel1DStandard(deflectionWithdrawChannel.getChannelData().getCopy(factor, rangeQuantity), WITHDRAW);

        SimpleSpectroscopyCurve afmCurve = new SimpleSpectroscopyCurve(approach, withdraw, RAW_CURVE);
        return afmCurve;
    }

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value, 
    //and sensitivity in micron/V or micron/A, i.e. 10^6 times more than the SI value
    @Override
    public Channel1D getRecordedPhotodiodeApproachCurve(double sensitivity, double springConstant)  throws UserCommunicableException
    {       
        double factor = calibrationState.getPhotodiodeValueConversionFactor(sensitivityReadIn, springConstantReadIn, sensitivity, springConstant);
        Channel1D approach = new Channel1DStandard(deflectionApproachChannel.getChannelData().getCopy(factor, getPhotodiodeQuantity()), APPROACH);
        return approach;
    }

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value, 
    //and sensitivity in micron/V or micron/A, i.e. 10^6 times more than the SI value
    @Override
    public Channel1D getRecordedPhotodiodeWithdrawCurve(double sensitivity, double springConstant)  throws UserCommunicableException
    {       
        double factor = calibrationState.getPhotodiodeValueConversionFactor(sensitivityReadIn, springConstantReadIn, sensitivity, springConstant);
        Channel1D withdraw = new Channel1DStandard(deflectionWithdrawChannel.getChannelData().getCopy(factor,getPhotodiodeQuantity()), WITHDRAW);
        return withdraw;
    }

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value, 
    //and sensitivity in micron/V or micron/A, i.e. 10^6 times more than the SI value
    @Override
    public SimpleSpectroscopyCurve getRecordedDeflectionCurve(double sensitivity, double springConstant)
    {
        Quantity rangeQuantity = Quantities.DEFLECTION_MICRONS;

        double factor = calibrationState.getDeflectionConversionFactor(sensitivityReadIn, springConstantReadIn, sensitivity, springConstant);

        Channel1D approach = new Channel1DStandard(deflectionApproachChannel.getChannelData().getCopy(factor, rangeQuantity), APPROACH);
        Channel1D withdraw = new Channel1DStandard(deflectionWithdrawChannel.getChannelData().getCopy(factor, rangeQuantity), WITHDRAW);

        SimpleSpectroscopyCurve afmCurve = new SimpleSpectroscopyCurve(approach, withdraw, RAW_CURVE);
        return afmCurve;
    }

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value, 
    //and sensitivity in micron/V, i.e. 10^6 times more than the SI value

    @Override
    public SimpleSpectroscopyCurve getRecordedForceCurve(double sensitivity, double springConstant)
    {
        Quantity rangeQuantity = Quantities.FORCE_NANONEWTONS;

        double factor = calibrationState.getForceConversionFactor(sensitivityReadIn, springConstantReadIn, sensitivity, springConstant);
        Channel1D approach = new Channel1DStandard(deflectionApproachChannel.getChannelData().getCopy(factor, rangeQuantity), APPROACH);
        Channel1D withdraw = new Channel1DStandard(deflectionWithdrawChannel.getChannelData().getCopy(factor, rangeQuantity), WITHDRAW);

        SimpleSpectroscopyCurve curve = new SimpleSpectroscopyCurve(approach, withdraw, RAW_CURVE);
        return curve;
    }

    @Override
    public boolean canBeUsedForCalibration(PhotodiodeSignalType signalType)
    {
        if(!this.photodiodeSignalType.equals(signalType))
        {
            return false;
        }

        boolean canBeUsed = this.calibrationState.canBeUsedForCalibration(sensitivityReadIn, springConstantReadIn);

        return canBeUsed;
    }

    public Map<String, QuantitativeSample> getSamples()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        if(deflectionApproachChannel != null && !deflectionApproachChannel.isEmpty())
        {
            double[] approachXs = deflectionApproachChannel.getXCoordinates();
            double[] approachYs = deflectionApproachChannel.getYCoordinates();

            QuantitativeSample approachXsSample = new StandardSample(approachXs, APPROACH_X, deflectionApproachChannel.getXQuantity().changeName(APPROACH_X),"","");
            QuantitativeSample approachYsSample = new StandardSample(approachYs, APPROACH_Y, deflectionApproachChannel.getYQuantity().changeName(APPROACH_Y),"","");

            samples.put(APPROACH_X, approachXsSample);
            samples.put(APPROACH_Y, approachYsSample);
        }

        if(deflectionWithdrawChannel != null && !deflectionWithdrawChannel.isEmpty())
        {
            double[] withdrawXs = deflectionWithdrawChannel.getXCoordinates();
            double[] withdrawYs = deflectionWithdrawChannel.getYCoordinates();

            QuantitativeSample withdrawXsSample = new StandardSample(withdrawXs, WITHDRAW_X, deflectionWithdrawChannel.getXQuantity().changeName(WITHDRAW_X),"","");
            QuantitativeSample withdrawYsSample = new StandardSample(withdrawYs, WITHDRAW_Y, deflectionWithdrawChannel.getYQuantity().changeName(WITHDRAW_Y),"","");

            samples.put(WITHDRAW_X, withdrawXsSample);
            samples.put(WITHDRAW_Y, withdrawYsSample);
        }

        for(BasicSpectroscopySource recording : additionalCurveRecordings)
        {
            samples.putAll(recording.getSamples());
        }

        return samples;
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {   
        SampleCollection collection = new StandardSampleCollection(getSamples(), getShortName(), getShortName(), getDefaultOutputLocation());

        List<SampleCollection> collections = Collections.singletonList(collection);
        return collections;
    }

    @Override
    public PrefixedUnit getSingleDataUnit()
    {
        Quantity rangeQuantity = calibrationState.getDefaultYQuantity();
        return rangeQuantity.getUnit();
    }

    private Quantity getPhotodiodeQuantity()
    {
        Quantity quantity = PhotodiodeSignalType.VOLTAGE.equals(photodiodeSignalType) ? Quantities.DEFLECTION_VOLTS: Quantities.DEFLECTION_NANO_AMPERES;
        return quantity;
    }

    @Override
    public boolean isSensitivityCalibrated()
    {
        return CalibrationState.DEFLECTION_SENSITIVITY_CALIBRATED.equals(calibrationState);
    }

    @Override
    public boolean isSensitivityKnown()
    {
        boolean known = !Double.isNaN(sensitivityReadIn);
        return known;
    }

    // sensitivity in microns per volt or per ampere (10^6 times greater than the SI value)
    @Override
    public double getSensitivity()
    {
        return sensitivityReadIn;
    }

    // sensitivity in microns per volt (10^6 times greater than the SI value)
    public void setSensitivity(double sensitivity)
    {
        this.sensitivityReadIn = sensitivity;
    }

    @Override
    public boolean isForceCalibrated()
    {
        return CalibrationState.FORCE_CALIBRATED.equals(calibrationState);
    }

    @Override
    public boolean isSpringConstantKnown()
    {
        boolean known = !Double.isNaN(springConstantReadIn);
        return known;
    }

    //spring constant in N/m
    @Override
    public double getSpringConstant()
    {
        return springConstantReadIn;
    }

    //spring constant in N/m
    public void setSpringConstant(double springConstant)
    {
        this.springConstantReadIn = springConstant;
    }

    @Override
    public PhotodiodeSignalType getPhotodiodeSignalType()
    {
        return photodiodeSignalType;
    }

    @Override
    public void setPhotodiodeSignalType(PhotodiodeSignalType photodiodeSignalType)
    {
        this.photodiodeSignalType = photodiodeSignalType;
    }

    @Override
    public boolean containsApproachData()
    {
        boolean contains = (deflectionApproachChannel != null) && !deflectionApproachChannel.isEmpty();
        return contains;
    }

    @Override
    public boolean containsWithdrawData()
    {
        boolean contains = (deflectionWithdrawChannel != null) && !deflectionWithdrawChannel.isEmpty();
        return contains;
    }

    @Override
    public boolean containsData(ForceCurveBranch branch)
    {
        if(ForceCurveBranch.APPROACH.equals(branch))
        {
            return containsApproachData();
        }
        if(ForceCurveBranch.WITHDRAW.equals(branch))
        {
            return containsWithdrawData();
        }

        return false;
    }

    @Override
    public List<SimpleSpectroscopySource> getSimpleSources() 
    {   
        return Collections.<SimpleSpectroscopySource>singletonList(this);
    }

    @Override
    public MapDelayedCreator getMapDelayedCreator()
    {
        return mapDelayedCreator;
    }

    @Override
    public void setMapDelayedCreator(MapDelayedCreator mapDelayedCreator)
    {
        this.mapDelayedCreator = mapDelayedCreator;
    }

    @Override
    public void setForceMap(MapSource<?> fvSource) 
    {
        this.mapSource = fvSource;
        this.partOfMap = fvSource != null;
    }

    @Override
    public MapSource<?> getForceMap() 
    {
        return mapSource;
    }

    @Override
    public boolean isFromMap() 
    {
        return partOfMap;
    }

    @Override
    public Point2D getRecordingPoint() 
    {
        return recordingPoint;
    }

    @Override
    public void setRecordingPoint(Point2D p) 
    {
        this.recordingPoint = p;
    }

    @Override 
    public String getMapPositionDescription()
    {
        if(!partOfMap)
        {
            return null;
        }

        if(mapSource instanceof MapGridSource)
        {
            GridIndex gridIndex = ((MapGridSource) mapSource).getClosestGridIndex(recordingPoint);
            return gridIndex.toString();
        }
        else return Integer.toString(mapPosition);
    }

    @Override
    public void setMapPosition(int mapPosition) 
    {
        this.mapPosition = mapPosition;
    }

    @Override
    public int getMapPosition() 
    {
        return mapPosition;
    }

    @Override
    public ReadingPack<ImageSource> getAccompanyingImageReadingPack()
    {
        return accompanyingImageInfo;
    }

    @Override
    public boolean isAccompanyingImagesAvailable()
    {
        boolean available = this.accompanyingImageInfo != null;
        return available;
    }

    @Override
    public void setAccompanyingImageReadingPack(ReadingPack<ImageSource> accompanyingImageInfo)
    {
        this.accompanyingImageInfo = accompanyingImageInfo;
    }

    public void addAccompanyingCurveRecording(BasicSpectroscopySource recording)
    {
        if(this.additionalCurveRecordings.isEmpty())
        {
            this.additionalCurveRecordings = new ArrayList<>();
        }
        this.additionalCurveRecordings.add(recording);
    }

    @Override
    public List<BasicSpectroscopySource> getAdditionalCurveRecordings() 
    {
        return additionalCurveRecordings;
    }

    @Override
    public List<String> getIdentifiers()
    {
        List<String> channels = new ArrayList<>();

        if(deflectionApproachChannel != null && !deflectionApproachChannel.isEmpty())
        {
            channels.add(deflectionApproachChannel.getIdentifier());            
        }

        if(deflectionWithdrawChannel != null && !deflectionWithdrawChannel.isEmpty())
        {
            channels.add(deflectionWithdrawChannel.getIdentifier());
        }

        for(BasicSpectroscopySource additionalRecording : additionalCurveRecordings)
        {
            channels.addAll(additionalRecording.getIdentifiers());
        }

        return channels;
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> channels = new ArrayList<>();

        if(deflectionApproachChannel != null && !deflectionApproachChannel.isEmpty())
        {
            channels.add(deflectionApproachChannel);
        }

        if(deflectionWithdrawChannel != null && !deflectionWithdrawChannel.isEmpty())
        {
            channels.add(deflectionWithdrawChannel);
        }

        for(BasicSpectroscopySource additionalRecording : additionalCurveRecordings)
        {
            channels.addAll(additionalRecording.getChannels());
        }

        return channels;
    }

    @Override
    public List<Channel1D> getChannels(Collection<String> identifiers)
    {
        List<Channel1D> channelsForIdentifiers = new ArrayList<>();

        if(deflectionApproachChannel != null && !deflectionApproachChannel.isEmpty() && identifiers.contains(deflectionApproachChannel.getIdentifier()))
        {
            channelsForIdentifiers.add(deflectionApproachChannel);
        }

        if(deflectionWithdrawChannel != null && !deflectionWithdrawChannel.isEmpty() && identifiers.contains(deflectionWithdrawChannel.getIdentifier()))
        {
            channelsForIdentifiers.add(deflectionWithdrawChannel);
        }

        for(BasicSpectroscopySource additionalRecording : additionalCurveRecordings)
        {
            channelsForIdentifiers.addAll(additionalRecording.getChannels(identifiers));
        }

        return channelsForIdentifiers;
    }


    //copies are keys, originals are values
    public static Map<SimpleSpectroscopySource, SimpleSpectroscopySource> copySources(List<SimpleSpectroscopySource> sourcesOld)
    {
        MultiMap<MapSource<?>,SimpleSpectroscopySource> map = new MultiMap<>();
        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> copies = new LinkedHashMap<>();

        for(SimpleSpectroscopySource source: sourcesOld)
        {
            //ensures that the force map is included in the list of force maps and that to source is added to the corresponding source list
            if(source.isFromMap())
            {
                map.put(source.getForceMap(), source);              
            }

            //the current source does not belong to any force map, so we will add it to the copies list right away
            else
            {
                copies.put(source.copy(), source);
            }
        }

        for(Entry<MapSource<?>, List<SimpleSpectroscopySource>> entry: map.entrySet())
        {
            //copies force maps and adds to the copies of spectroscopy sources
            MapSource<?> mapSource = entry.getKey();
            MapSource<?> mapSourceCopy = mapSource.copy(new ArrayList<String>());

            for(SimpleSpectroscopySource src : entry.getValue())
            {
                SimpleSpectroscopySource sourceCopy = src.copy();
                mapSourceCopy.replaceSpectroscopySource(sourceCopy, sourceCopy.getMapPosition());
                copies.put(sourceCopy, src);
            }
        }

        return copies;
    }

    //copies are keys, originals are values
    public static Map<SimpleSpectroscopySource, SimpleSpectroscopySource> copySourcesInPlace(List<SimpleSpectroscopySource> sourcesOld)
    {
        Map<SimpleSpectroscopySource, SimpleSpectroscopySource> copies = new LinkedHashMap<>();

        for(SimpleSpectroscopySource source: sourcesOld)
        {
            SimpleSpectroscopySource sourceCopy = source.copy();
            copies.put(sourceCopy, source);

            MapSource<?> mapSource = source.getForceMap();

            if(source.isFromMap() && mapSource != null)
            {
                mapSource.replaceSpectroscopySource(sourceCopy, sourceCopy.getMapPosition());
            }
        }

        return copies;
    }
}
