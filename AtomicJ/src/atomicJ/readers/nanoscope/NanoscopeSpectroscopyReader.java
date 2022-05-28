
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.readers.nanoscope;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;

public class NanoscopeSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String FILE_LIST_END = "\\*File list end";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"[0-9]++", "spm", "pfc"};
    private static final String DESCRIPTION = "Nanoscope curve file (.001,..., .spm, .pfc)";

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(ACCEPTED_EXTENSIONS);
        return filter.accept(f);       
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException 
    {	               
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1"));) 
        {            
            List<NanoscopeSpectroscopyData> allSpectroscopyData = new ArrayList<>();

            List<NanoscopeImageData> allImageData = new ArrayList<>();

            NanoscopeFileList fileList = new NanoscopeFileList();
            NanoscopeScanList scanList = new NanoscopeScanList();
            NanoscopeScannerList scannerList = new NanoscopeScannerList();
            NanoscopeForceList forceList = new NanoscopeForceList();

            String line;
            while((line = reader.readLine()) != null)
            {                  
                if(fileList.isSectionBeginning(line))
                {
                    line = fileList.readInFieldsToHeaderSection(reader);
                }
                if(scannerList.isSectionBeginning(line))
                {   
                    line = scannerList.readInFieldsToHeaderSection(reader);
                } 
                if(scanList.isSectionBeginning(line))
                {              
                    line = scanList.readInFieldsToHeaderSection(reader);
                }  
                if(forceList.isSectionBeginning(line))
                {              
                    line = forceList.readInFieldsToHeaderSection(reader);
                }
                if(NanoscopeImageData.isSectionBeginningStatic(line))
                {                                        
                    while(line != null && NanoscopeImageData.isSectionBeginningStatic(line))
                    {
                        NanoscopeImageData data = new NanoscopeImageData();
                        line = data.readInFieldsToHeaderSection(reader);
                        allImageData.add(data);
                    }
                }
                if(NanoscopeSpectroscopyData.isSectionBeginningStatic(line))
                {                    
                    while(line != null && NanoscopeSpectroscopyData.isSectionBeginningStatic(line))
                    {
                        NanoscopeSpectroscopyData data = new NanoscopeSpectroscopyData();
                        line = data.readInFieldsToHeaderSection(reader);
                        allSpectroscopyData.add(data);
                    }
                }

                if(readingDirectives.isCanceled())
                {
                    return Collections.emptyList();
                }

                if(FILE_LIST_END.equals(line))
                {                    
                    break;
                }
            }

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            List<NanoscopeSpectroscopyData> allSpectroscopyXLinearRampData = new ArrayList<>();
            List<NanoscopeSpectroscopyData> allSpectroscopyIndependentData = new ArrayList<>();

            for(NanoscopeSpectroscopyData d : allSpectroscopyData)
            {                    
                d.readInScannerList(scannerList);
                d.readInScanList(scanList);
                d.readInForceList(forceList);
                d.setNanoscopeVersion(fileList.getVersion());

                //this must be here, don't move it up, because scan list contains data abot sinusoudal ramp, so we cannot call isSinusoidalRamp() becore be readInScalList               
                if(d.isSinusoidalRamp() || d.getDataType().isForceCurveY())
                {
                    allSpectroscopyIndependentData.add(d);
                }
                else if(d.getDataType().isForceCurveX())
                {
                    allSpectroscopyXLinearRampData.add(d);
                }
            }

            for(NanoscopeSpectroscopyData d : allSpectroscopyIndependentData)
            {                    
                List<SimpleSpectroscopySource> currentSources = Collections.emptyList();

                if(d.isSinusoidalRamp())
                {
                    currentSources = buildSourcesSinusoidalRamp(f,readingDirectives,d);
                }
                else
                {
                    currentSources =  allSpectroscopyXLinearRampData.isEmpty() || (!AtomicJ.getPreferencesModel().getNanoscopePreferences().isUseHeightSensorDataWheneverAvailable()) ?  buildSourcesLinearRamp(f,readingDirectives, d) : buildSourcesLinearRamp(f, readingDirectives, allSpectroscopyXLinearRampData.get(0), d);
                }

                handleSourceCreation(currentSources, scanList, scannerList);

                if(!currentSources.isEmpty())
                {
                    sources.addAll(currentSources);

                    if(d.isForceMap())
                    {
                        handleForceMapCreation(f, d, currentSources, scanList, allImageData);
                    }       
                }                          
            }


            return sources;	
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);		
        } 
    }

    private void handleForceMapCreation(File f, NanoscopeSpectroscopyData d, List<SimpleSpectroscopySource> currentSources, NanoscopeScanList scanList, List<NanoscopeImageData> allImageData)
    {
        ReadingPack<ImageSource> readingPack = null;
        if(!allImageData.isEmpty())
        {
            NanoscopeImageReader imageReader = new NanoscopeImageReader();
            readingPack = new FileReadingPack<>(f, imageReader);               
        }   

        Grid2D scanGrid = scanList.getGrid();
        int spectroscopyGridSize = (int)Math.ceil(Math.sqrt(currentSources.size()));

        Grid2D spectroscopyGrid = scanGrid.changeDensity(spectroscopyGridSize, spectroscopyGridSize);

        for(int i = 0; i<currentSources.size(); i++)
        {
            SimpleSpectroscopySource source = currentSources.get(i);
            source.setRecordingPoint(spectroscopyGrid.getPointFlattenedWithFullReturn(i));
        }

        MapSource<?> mapSource = new MapGridSource(f, currentSources, spectroscopyGrid);   
        mapSource.setMapAreaImageReadingPack(readingPack);
    }

    private List<SimpleSpectroscopySource> buildSourcesLinearRamp(File f, SourceReadingDirectives readingDirectives,NanoscopeSpectroscopyData deflectionSensor) throws UserCommunicableException
    {                    
        if(!deflectionSensor.isFullySpecified() || !deflectionSensor.getDataType().isForceCurveY())
        {
            return Collections.emptyList();
        }

        int curveCount = deflectionSensor.calculateCurveCount();

        SourceReadingState state  = curveCount > 10  ? new SourceReadingStateMonitored(curveCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
            new SourceReadingStateMute(curveCount);

        try{
            Quantity yQuantity = Quantities.DEFLECTION_MICRONS;

            UnitExpression zScale = deflectionSensor.getZScale();
            double factor = zScale.derive(Units.MICRO_METER_UNIT).getValue();

            boolean multipleCurves = curveCount > 1;

            ByteBuffer byteBuffer = deflectionSensor.readInBytes(f);

            Grid1D approachGrid = deflectionSensor.getApproachGrid();
            Grid1D withdrawGrid = deflectionSensor.getWithdrawGrid();

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            for(int j = 0; j < curveCount; j++)
            {   
                double[][] channelData = deflectionSensor.readInCurveChannels(byteBuffer, factor);

                if(readingDirectives.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return Collections.emptyList();
                }

                Channel1DData approach = new GridChannel1DData(channelData[0], approachGrid, yQuantity);
                Channel1DData withdraw = new GridChannel1DData(channelData[1], withdrawGrid, yQuantity);

                String suffix = multipleCurves ? " (" + Integer.toString(j) + ")" : "";

                SimpleSpectroscopySource source = buildSpectroscopySource(f, approach, withdraw, suffix, deflectionSensor);

                sources.add(source);

                state.incrementAbsoluteProgress();
            }              

            return sources;

        } catch (Exception e) 
        {
            state.setOutOfJob();
            throw new UserCommunicableException("Error has occured while reading a force voulme recording", e);
        } 
    }   

    private List<SimpleSpectroscopySource> buildSourcesLinearRamp(File f, SourceReadingDirectives readingDirectives, NanoscopeSpectroscopyData heightSensor, NanoscopeSpectroscopyData deflectionSensor) throws UserCommunicableException
    {      
        if(!deflectionSensor.isFullySpecified() || !heightSensor.isFullySpecified())
        {
            return Collections.emptyList();
        }

        int curveCount = deflectionSensor.calculateCurveCount();

        SourceReadingState state  = curveCount > 10  ? new SourceReadingStateMonitored(curveCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
            new SourceReadingStateMute(curveCount);

        try
        {
            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = Quantities.DEFLECTION_MICRONS;

            UnitExpression deflectionZScale = deflectionSensor.getZScale();
            double deflectionFactor = deflectionZScale.derive(yQuantity.getUnit()).getValue();

            UnitExpression heightSensorScale = heightSensor.getZScale();
            double heightSensorFactor = -heightSensorScale.derive(xQuantity.getUnit()).getValue();

            boolean multipleCurves = curveCount > 1;

            ByteBuffer deflectionByteBuffer = deflectionSensor.readInBytes(f);
            ByteBuffer heightSensorByteBuffer = heightSensor.readInBytes(f);

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            for(int j = 0; j < curveCount; j++)
            {         
                double[][] deflectionChannelData = deflectionSensor.readInCurveChannels(deflectionByteBuffer, deflectionFactor);
                double[][] heightSensorChannelData = heightSensor.readInCurveChannels(heightSensorByteBuffer, heightSensorFactor);

                Channel1DData approach = FlexibleChannel1DData.getInstance(heightSensorChannelData[0],deflectionChannelData[0], xQuantity, yQuantity, SortedArrayOrder.DESCENDING);
                Channel1DData withdraw = FlexibleChannel1DData.getInstance(heightSensorChannelData[1], deflectionChannelData[1], xQuantity, yQuantity, SortedArrayOrder.ASCENDING);

                String suffix = multipleCurves ? " (" + Integer.toString(j) + ")" : "";

                SimpleSpectroscopySource source = buildSpectroscopySource(f, approach, withdraw, suffix, deflectionSensor);

                sources.add(source);

                state.incrementAbsoluteProgress();

                if(readingDirectives.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return Collections.emptyList();
                }          
            }       

            return sources;       
        } catch (Exception e) 
        {
            state.setOutOfJob();
            throw new UserCommunicableException("Error has occured while reading a force voulme recording", e);
        }  
    }

    private List<SimpleSpectroscopySource> buildSourcesSinusoidalRamp(File f, SourceReadingDirectives readingDirectives, NanoscopeSpectroscopyData deflectionSensor) throws UserCommunicableException
    {                      
        if(!deflectionSensor.isFullySpecified())
        {
            return Collections.emptyList();
        }

        int firstIndex = deflectionSensor.getFirstIndex();
        int curveCount = deflectionSensor.calculateCurveCount();

        SourceReadingState state  = curveCount > 10  ? new SourceReadingStateMonitored(curveCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
            new SourceReadingStateMute(curveCount);

        try {
            UnitExpression zScale = deflectionSensor.getZScale();
            double factor = zScale.derive(Units.MICRO_METER_UNIT).getValue();

            boolean multipleCurves = curveCount > 1;

            int withdrawLength = deflectionSensor.getReadInWithdrawLength();

            int approachLength = withdrawLength;// Bruker's Matlab toolbox ignores the length of approach and assumes that withdraw curve and approach curve are of the same length
            //in addition, I saw PFC files that have wrong approach length specified (provided by Denis, approach is, surprisingly, the second value in Samps/line)

            int[] lsbs = deflectionSensor.getReversedLSBValues(f);

            int curveLength = 2*withdrawLength;// Bruker's Matlab toolbox ignores the length of approach and assumes that withdraw curve and approach curve are of the same length
            //in addition, I saw PFC files that have wrong approach length specified (provided by Denis)

            double numFcPoints = curveLength;

            double phaseCorrection = deflectionSensor.getSyncDistanceFractionCorrectionFactor();

            double phaseShift = -Math.PI/2.0 + 2*Math.PI*(0.5 - phaseCorrection/numFcPoints); 

            double angleFactor = 2*Math.PI/numFcPoints;
            double peakForceAmplitude = deflectionSensor.getPeakForceAmplitude();

            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = Quantities.DEFLECTION_MICRONS;

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            for(int j = 0; j < curveCount; j++)
            {
                double[] approachData = new double[approachLength];
                double[] withdrawData = new double[withdrawLength];

                int peakIndex = firstIndex + 2*approachLength*j;

                for(int i = 0; i < withdrawLength; i++)
                {
                    int pointIndex = peakIndex + i;
                    int index = (pointIndex < (j+1)*2*approachLength) ? pointIndex : (j*2 + 1)*approachLength - (pointIndex % approachLength) - 1; 
                    int readIn = lsbs[index];

                    withdrawData[i] = readIn*factor; 
                }  

                for(int i = 0; i < approachLength; i++)
                {
                    int pointIndex = peakIndex - i - 1;
                    int index = (pointIndex >= (j*2 + 1)*approachLength) ? pointIndex : j*2*approachLength + (j*2*approachLength + approachLength - pointIndex) - 1;
                    int readIn = lsbs[index];

                    approachData[approachLength - i - 1] = readIn*factor; 

                }  
                //this interpolation is performed by Matlab Toolkit, it is performed when int pointIndex = peakIndex - i - 1; pointIndex = (j*2 + 1)*approachLength
                if((2*approachLength - firstIndex + 1) < approachLength && (2*approachLength - firstIndex - 1) > 0)
                {
                    approachData[2*approachLength - firstIndex] = 0.5*(approachData[2*approachLength - firstIndex + 1] + approachData[2*approachLength - firstIndex - 1]); 
                }

                Channel1DData approach = new SinusoidalChannel1DData(approachData, peakForceAmplitude, angleFactor, 1, -phaseShift, xQuantity, yQuantity);
                Channel1DData withdraw = new SinusoidalChannel1DData(withdrawData, peakForceAmplitude, angleFactor, 1 - approachLength, 2*Math.PI - phaseShift, xQuantity, yQuantity);

                String suffix = multipleCurves ? " (" + Integer.toString(curveCount - j - 1) + ")" : "";

                SimpleSpectroscopySource source = buildSpectroscopySource(f, approach, withdraw, suffix,deflectionSensor);

                sources.add(0, source);//the source needs to be added at the beginning of the list of sources, because lsbs are reversed

                state.incrementAbsoluteProgress();

                if(readingDirectives.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return Collections.emptyList();
                }
            }

            return sources;
        } catch (Exception e) 
        {
            state.setOutOfJob();
            throw new UserCommunicableException("Error has occured while reading peak force capture data", e);
        }   
    }

    protected SimpleSpectroscopySource buildSpectroscopySource(File f, Channel1DData approach, Channel1DData withdraw, String suffix, NanoscopeSpectroscopyData deflectionSensor)
    {
        String longName = f.getAbsolutePath() + suffix;
        String shortName = IOUtilities.getBareName(f) + suffix;

        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approach, withdraw);
        source.setSensitivity(deflectionSensor.getDeflectionSensitivity());
        source.setSpringConstant(deflectionSensor.getSpringConstant());

        return source;
    }


    protected void handleSourceCreation(List<SimpleSpectroscopySource> sources, NanoscopeScanList scanList, NanoscopeScannerList scannerList) throws UserCommunicableException
    {}
}

