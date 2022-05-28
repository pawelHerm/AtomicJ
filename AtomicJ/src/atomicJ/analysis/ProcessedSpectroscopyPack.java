
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

package atomicJ.analysis;

import static atomicJ.data.Datasets.ADHESION_FORCE;
import static atomicJ.data.Datasets.JUMP_FORCE;
import static atomicJ.data.Datasets.APPROACH_SMOOTHED;
import static atomicJ.data.Datasets.CONTACT_POINT;
import static atomicJ.data.Datasets.FORCE_CURVE;
import static atomicJ.data.Datasets.MODEL_TRANSITION_POINT;
import static atomicJ.data.Datasets.MODEL_TRANSITION_POINT_FORCE_CURVE;
import static atomicJ.data.Datasets.WITHDRAW_SMOOTHED;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.analysis.indentation.ContactModelFit;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.Datasets;
import atomicJ.data.IndentationCurve;
import atomicJ.data.Point1DData;
import atomicJ.data.PointwiseModulusCurve;
import atomicJ.data.Channel1DCollection;
import atomicJ.data.ProcessedStaticSpectroscopyCurve;
import atomicJ.data.ProjectionRightConstraint1D;
import atomicJ.data.Quantities;
import atomicJ.data.RangeConstraint1D;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.data.VerticalModificationConstraint1D;
import atomicJ.gui.curveProcessing.ProcessingBatchMemento;
import atomicJ.sources.IdentityTag;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;

public final class ProcessedSpectroscopyPack implements Processed1DPack<ProcessedSpectroscopyPack, SimpleSpectroscopySource>
{	
    private static final int FIT_POINT_COUNT = 75;

    private final SimpleSpectroscopySource source;
    private final NumericalSpectroscopyProcessingResults results;
    private final ProcessingSettings processingSettings;
    private final ContactEstimator estimator;
    private final ContactModelFit<?> modelFit;
    private IdentityTag batchId;

    public ProcessedSpectroscopyPack(SimpleSpectroscopySource source, NumericalSpectroscopyProcessingResults results, ContactModelFit<?> modelFit, ContactEstimator estimator, ProcessingSettings settings, IdentityTag batchIdTag)
    {
        this.source = source;
        this.results = results;
        this.processingSettings = settings;
        this.estimator = estimator;
        this.modelFit = modelFit;
        this.batchId = batchIdTag;
    }

    public ProcessedSpectroscopyPack(ProcessedSpectroscopyPack that)
    {
        this.source = that.source.copy();
        this.results = that.results;
        this.processingSettings = that.processingSettings;
        this.estimator = that.estimator;
        this.modelFit = that.modelFit;
        this.batchId = null;
    }

    public ProcessedSpectroscopyPack copy()
    {
        return new ProcessedSpectroscopyPack(this);
    }

    public SpectroscopyCurve<Channel1D> getForceDistanceCurve()
    {
        double springConstant = processingSettings.getSpringConstant();
        double s = processingSettings.getSensitivity();

        double springConstantNanoNewtonsPerMicron = 1000*springConstant;

        SpectroscopyCurve<Channel1D> forceCurve = source.getRecordedForceCurve(s, springConstantNanoNewtonsPerMicron);
        return forceCurve;
    }

    public Channel1DData getForceIndentationChannelData()
    {
        Channel1DData forceIndentationData = modelFit.getForceIndentation();       
        return forceIndentationData;
    }

    public Channel1DData getPointwiseModulus()
    {
        return modelFit.getPointwiseModulus();
    }


    public Point2D getDeflectionContactPoint()
    {
        return new Point2D.Double(results.getContactDisplacement(), results.getContactDeflection());
    }

    public Point2D getForceContactPoint()
    {
        return new Point2D.Double(results.getContactDisplacement(), results.getContactForce());
    }

    public ContactModelFit<?> getModelFit()
    {
        return modelFit;
    }

    public List<ForceEventEstimate> getAdhesionEstimates()
    {
        return results.getAdhesionForceEstimates();
    }

    public ForceEventEstimate getAdhesionEstimate(int index)
    {
        return results.getAdhesionForceEstimate(index);
    }

    public int getAdhesionForceEstimateCount()
    {
        return results.getAdhesionForceEstimateCount();
    }

    public List<ForceEventEstimate> getJumpEstimates()
    {
        return results.getJumpForceEstimates();
    }

    public ForceEventEstimate getJumpEstimate(int index)
    {
        return results.getJumpForceEstimate(index);
    }

    public int getJumpForceEstimateCount()
    {
        return results.getJumpForceEstimateCount();
    }

    public Channel1DData getForceIndentationData()
    {
        return modelFit.getForceIndentation();
    }

    public NumericalSpectroscopyProcessingResults getResults()
    {
        return results;
    }

    public ProcessingSettings getProcessingSettings()
    {
        return processingSettings;
    }

    public ContactEstimator getContactEstimator()
    {
        return estimator;
    }

    @Override
    public SimpleSpectroscopySource getSource()
    {
        return source;
    }

    public MapSource<?> getForceMap()
    {
        return source.getForceMap();
    }

    @Override
    public void setBatchIdTag(IdentityTag batchId)
    {
        this.batchId = batchId;
    }

    @Override
    public IdentityTag getBatchIdTag()
    {
        return batchId;
    }

    @Override
    public String toString()
    {
        String result = source.getLongName();
        return result;
    }

    public boolean isFromMap()
    {
        boolean fromMap = source.isFromMap();
        return fromMap;
    }

    public boolean isCorrespondingMapPositionKnown()
    {
        boolean known = source.isCorrespondingMapPositionKnown();
        return known;
    }

    public void registerInMap()
    {
        if(source.isFromMap())
        {
            MapSource<?> mapSource = source.getForceMap();	
            mapSource.registerProcessedPack(this);
        }
    }

    public ProcessingBatchMemento getProcessingMemento()
    {
        return source.getProcessingMemento();
    }

    @Override
    public List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions()
    {
        List<ProcessedPackFunction<ProcessedSpectroscopyPack>> adhesionSpecialFunctions = getAdhesionSpecialFunctions();
        List<ProcessedPackFunction<ProcessedSpectroscopyPack>> jumpSpecialFunctions = getJumpSpecialFunctions();
        List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> modelSpecialFunctions = modelFit.getSpecialFunctions();

        if(adhesionSpecialFunctions.isEmpty() && jumpSpecialFunctions.isEmpty())
        {
            List<ProcessedPackFunction<ProcessedSpectroscopyPack>> allSpecialFuctions = new ArrayList<>(modelSpecialFunctions);
            if(processingSettings.isCalculateAdhesionForce())
            {
                allSpecialFuctions.add(AdhesionForceMainProcessedPackFunction.getInstance());
            }
            if(processingSettings.isCalculateRSquared())
            {
                allSpecialFuctions.add(CoefficientOfDeterminationProcessedPackFunction.getInstance());
            }


            return allSpecialFuctions;           
        }

        List<ProcessedPackFunction<ProcessedSpectroscopyPack>> allSpecialFuctions = new ArrayList<>(adhesionSpecialFunctions);
        allSpecialFuctions.addAll(jumpSpecialFunctions);
        allSpecialFuctions.addAll(modelSpecialFunctions);

        if(processingSettings.isCalculateAdhesionForce())
        {
            allSpecialFuctions.add(AdhesionForceMainProcessedPackFunction.getInstance());
        }
        if(processingSettings.isCalculateRSquared())
        {
            allSpecialFuctions.add(CoefficientOfDeterminationProcessedPackFunction.getInstance());
        }


        return allSpecialFuctions;
    }

    private List<ProcessedPackFunction<ProcessedSpectroscopyPack>> getAdhesionSpecialFunctions()
    {
        int n = results.getAdhesionForceEstimateCount();

        if(n == 0)
        {
            return Collections.emptyList();
        }

        List<ProcessedPackFunction<ProcessedSpectroscopyPack>> specialFunctions = new ArrayList<>();
        for(int i = 1; i<n; i++)
        {
            specialFunctions.add(new AdhesionForceProcessedPackFunction(i));
        }

        return specialFunctions;
    }

    private List<ProcessedPackFunction<ProcessedSpectroscopyPack>> getJumpSpecialFunctions()
    {
        int n = results.getJumpForceEstimateCount();

        if(n == 0)
        {
            return Collections.emptyList();
        }

        List<ProcessedPackFunction<ProcessedSpectroscopyPack>> specialFunctions = new ArrayList<>();
        for(int i = 0; i<n; i++)
        {
            specialFunctions.add(new JumpForceProcessedPackFunction(i));
        }

        return specialFunctions;
    }


    public VisualizableSpectroscopyPack visualize(VisualizationSettings visSettings)
    {        
        if(!visSettings.isAnyResultToBeVisualized())
        {
            return null;
        }

        Channel1DData forceIndentationFit = modelFit.getForceIndentationFit(FIT_POINT_COUNT);

        ProcessedStaticSpectroscopyCurve recordedCurve = buildProcessedStaticSpectroscopyCurve(modelFit, forceIndentationFit, visSettings);
        IndentationCurve indentationCurve = IndentationCurve.buildIndentationCurve(modelFit, forceIndentationFit, visSettings);
        PointwiseModulusCurve modulusCurve = PointwiseModulusCurve.buildPointwiseModulusCurve(modelFit, visSettings);

        VisualizableSpectroscopyPack visualizable = new VisualizableSpectroscopyPack(this, recordedCurve, indentationCurve, modulusCurve, visSettings);

        return visualizable;
    }




    private ProcessedStaticSpectroscopyCurve buildProcessedStaticSpectroscopyCurve(ContactModelFit<?> modelFit, Channel1DData forceIndentationFit, VisualizationSettings visSettings)
    {
        ProcessedStaticSpectroscopyCurve recordedCurve = null;

        if(visSettings.isPlotRecordedCurve())
        {   
            SpectroscopyCurve<Channel1D> forceCurve = getForceDistanceCurve();

            Channel1D approachForceChannel = forceCurve.getApproach();
            Channel1D withdrawForceChannel = forceCurve.getWithdraw();     

            double z0 = results.getContactDisplacement();
            double F0 = results.getContactForce();
            Channel1D fittedForceChannel = ForceCurveBranch.APPROACH.equals(processingSettings.getFittedBranch()) ? approachForceChannel : withdrawForceChannel;
            Channel1D contactForce = new Channel1DStandard(new Point1DData(z0, F0, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS), CONTACT_POINT, CONTACT_POINT, new RangeConstraint1D(fittedForceChannel.getIdentifier(), 4, 4));
            Point1DData forceCurveTransitionPointData = modelFit.getForceCurveTransitionPoint();
            Channel1D transitionPointForce = new Channel1DStandard(forceCurveTransitionPointData, MODEL_TRANSITION_POINT_FORCE_CURVE, MODEL_TRANSITION_POINT, new ProjectionRightConstraint1D(fittedForceChannel.getIdentifier(), Datasets.CONTACT_POINT, 4));

            Channel1DCollection approachForceChannels = new Channel1DCollection(Datasets.APPROACH);
            Channel1DCollection withdrawForceChannels = new Channel1DCollection(Datasets.WITHDRAW);  

            boolean trimmed = processingSettings.areDataTrimmed();
            Channel1DDataTransformation trimmer = processingSettings.getTrimmer();

            Channel1DData approachForceChannelTrimmed = trimmed ? trimmer.transform(approachForceChannel.getChannelData()) : approachForceChannel.getChannelData();
            Channel1DData withdrawForceChannelTrimmed = trimmed ? trimmer.transform(withdrawForceChannel.getChannelData()) : withdrawForceChannel.getChannelData();

            approachForceChannels.addChannel(approachForceChannelTrimmed, Datasets.APPROACH);
            withdrawForceChannels.addChannel(withdrawForceChannelTrimmed, Datasets.WITHDRAW);

            if(processingSettings.areDataSmoothed())
            {      
                Channel1DDataTransformation smoother = processingSettings.getSmoother();

                Channel1DData smoothedForceApproach = smoother.transform(approachForceChannelTrimmed);
                Channel1DData smoothedForceWithdraw = smoother.transform(withdrawForceChannelTrimmed);

                approachForceChannels.addChannel(smoothedForceApproach, APPROACH_SMOOTHED);
                withdrawForceChannels.addChannel(smoothedForceWithdraw, WITHDRAW_SMOOTHED);
            } 

            List<Channel1D> adhesionMarkers = buildAdhesionMarkers(results);           
            List<Channel1D> jumpMarkers = buildJumpMarkers(results);

            recordedCurve = new ProcessedStaticSpectroscopyCurve(approachForceChannels, withdrawForceChannels,
                    contactForce, adhesionMarkers, jumpMarkers, transitionPointForce, FORCE_CURVE);

            if(visSettings.isPlotRecordedCurveFit())
            {
                Channel1DData forceCurveFit = modelFit.convertToForceCurvePoints(forceIndentationFit.getYCoordinates(), forceIndentationFit.getXCoordinates());
                recordedCurve.setFit(forceCurveFit);
            }
        }

        return recordedCurve;
    }

    private static List<Channel1D> buildAdhesionMarkers(NumericalSpectroscopyProcessingResults results)
    {
        List<Channel1D> adhesionMarkers = new ArrayList<>();
        int adhesionEstimateCount = results.getAdhesionForceEstimateCount();

        for(int i = 0; i < adhesionEstimateCount; i++)
        {
            ForceEventEstimate adhesionEstimate = results.getAdhesionForceEstimate(i);
            ChannelGroupTag groupTag = new ChannelGroupTag(ADHESION_FORCE, i);
            String identifier = i > 0 ? groupTag.getDefaultChannelIdentifier() : ADHESION_FORCE;
            Channel1D adhesionMarker = new Channel1DStandard(adhesionEstimate.getEventData(), identifier, identifier, new VerticalModificationConstraint1D(), groupTag);
            adhesionMarkers.add(adhesionMarker);
        }

        return adhesionMarkers;
    }

    private static List<Channel1D> buildJumpMarkers(NumericalSpectroscopyProcessingResults results)
    {
        List<Channel1D> jumpMarkers = new ArrayList<>();
        int jumpEstimateCount = results.getJumpForceEstimateCount();

        for(int i = 0; i < jumpEstimateCount; i++)
        {
            ForceEventEstimate jumpEstimate = results.getJumpForceEstimate(i);
            ChannelGroupTag groupTag = new ChannelGroupTag(JUMP_FORCE, i);
            String identifier = i > 0 ? groupTag.getDefaultChannelIdentifier() : JUMP_FORCE;
            Channel1D jumpMarker = new Channel1DStandard(jumpEstimate.getEventData(), identifier, identifier, new VerticalModificationConstraint1D(), groupTag);
            jumpMarkers.add(jumpMarker);
        }

        return jumpMarkers;
    }


    public static boolean containsPacksOfKnownPositionInMap(List<ProcessedSpectroscopyPack> packs)
    {
        for(ProcessedSpectroscopyPack pack : packs)
        {
            if(pack.isCorrespondingMapPositionKnown())
            {
                return true;
            }
        }

        return false;
    }
}
