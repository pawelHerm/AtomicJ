
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

package atomicJ.analysis;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

import atomicJ.analysis.indentation.ContactModel;
import atomicJ.analysis.indentation.ContactModelFit;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Datasets;
import atomicJ.data.IndentationCurve;
import atomicJ.data.Point1DData;
import atomicJ.data.PointwiseModulusCurve;
import atomicJ.data.ProjectionConstraint1D;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.gui.curveProcessing.LivePreviewPack;
import atomicJ.sources.SimpleSpectroscopySource;
import static atomicJ.data.Datasets.*;

public final class StandardSpectroscopyProcessor implements Processor<ProcessableSpectroscopyPack, SpectroscopyProcessingResult>
{
    private static final StandardSpectroscopyProcessor INSTANCE = new StandardSpectroscopyProcessor();
    private StandardSpectroscopyProcessor(){}

    public static StandardSpectroscopyProcessor getInstance()
    {
        return INSTANCE;
    }

    public LivePreviewPack getPreviewPack(ProcessableSpectroscopyPack processable)
    {
        SimpleSpectroscopySource source = processable.getSourceToProcess();
        ProcessingSettings settings = processable.getProcessingSettings();
        ContactEstimator estimator = processable.getContactEstimator();

        double s = settings.getSensitivity();
        double springConstantSI = settings.getSpringConstant();

        double springConstantNanoNewtonsPerMicron = 1000*springConstantSI;

        ContactModel model = settings.getContactModel();

        SpectroscopyCurve<Channel1D> deflectionCurve = source.getRecordedDeflectionCurve(s, springConstantNanoNewtonsPerMicron);
        Channel1D deflApproach = deflectionCurve.getApproach();
        Channel1D deflWithdraw = deflectionCurve.getWithdraw();

        Channel1DDataTransformation trimmer = settings.getTrimmer();

        Channel1DData trimmedFittedBranchUnsorted = ForceCurveBranch.APPROACH.equals(settings.getFittedBranch()) ? trimmer.transform(deflApproach.getChannelData()) : trimmer.transform(deflWithdraw.getChannelData());

        Channel1DData trimmedFittedBranch = new SortX1DTransformation(SortedArrayOrder.DESCENDING).transform(trimmedFittedBranchUnsorted);

        Channel1DDataTransformation smoother = settings.getSmoother();
        Channel1DData transformedFittedBranch = smoother.transform(trimmedFittedBranch);

        Point2D recordingPoint = source.getRecordingPoint();

        double[] deflectionContactPointEstimated = estimator.getContactPoint(transformedFittedBranch, recordingPoint, springConstantSI);   

        ContactModelFit<?> modelFit = model.getModelFit(transformedFittedBranch, deflectionContactPointEstimated, recordingPoint, settings);
        Channel1DData forceIndentationData = modelFit.getForceIndentation();

        double modulus = modelFit.getYoungModulus();

        Point1DData transitionPoint = modelFit.getForceIndentationTransitionPoint();
        Point1DData pointwiseModulusTransitionPoint = modelFit.getPointwiseModulusTransitionPoint();

        Channel1DData pointwiseModulusData = modelFit.getPointwiseModulus();	

        Channel1D forceIndentation = new Channel1DStandard(forceIndentationData, Datasets.INDENTATION_DATA, Datasets.INDENTATION);
        Channel1D transitionPointIndentation = new Channel1DStandard(transitionPoint, MODEL_TRANSITION_POINT,MODEL_TRANSITION_POINT, new ProjectionConstraint1D(forceIndentation.getIdentifier(), 4, 0));						

        IndentationCurve indentationCurve = new IndentationCurve(forceIndentation, transitionPointIndentation, new Channel1DStandard(modelFit.getForceIndentationFit(75), Datasets.INDENTATION_FIT,  Datasets.FIT)); 

        Channel1D pointwiseModulus = new Channel1DStandard(pointwiseModulusData, Datasets.POINTWISE_MODULUS_DATA,Datasets.POINTWISE_MODULUS);
        Channel1D transitionPointPointwiseModulus = new Channel1DStandard(pointwiseModulusTransitionPoint, MODEL_TRANSITION_POINT, MODEL_TRANSITION_POINT, new ProjectionConstraint1D(pointwiseModulus.getIdentifier(), 4, 0));
        PointwiseModulusCurve pointwiseModulusCurve = new PointwiseModulusCurve(pointwiseModulus,transitionPointPointwiseModulus, new Channel1DStandard(modelFit.getPointwiseModulusFit(20), Datasets.POINTWISE_MODULUS_FIT, Datasets.FIT));

        LivePreviewPack previewPack = new LivePreviewPack(indentationCurve, pointwiseModulusCurve, modulus, transitionPoint.getX(), transitionPoint.getY());

        return previewPack;
    }

    @Override
    public SpectroscopyProcessingResult process(ProcessableSpectroscopyPack processable)
    {
        SimpleSpectroscopySource source = processable.getSourceToProcess();
        ProcessingSettings settings = processable.getProcessingSettings();

        ContactEstimator contactEstimator = processable.getContactEstimator();
        ForceEventEstimator adhesionEstimator = processable.getAdhesionForceEstimator();
        ForceEventEstimator jumpEstimator = processable.getJumpEstimator();

        double s = settings.getSensitivity();
        double springConstant = settings.getSpringConstant();

        double springConstantNanoNewtonsPerMicron = 1000*springConstant;

        ContactModel model = settings.getContactModel();

        SpectroscopyCurve<Channel1D> deflectionCurve = source.getRecordedDeflectionCurve(s, springConstantNanoNewtonsPerMicron);
        SpectroscopyCurve<Channel1D> forceCurve = source.getRecordedForceCurve(s, springConstantNanoNewtonsPerMicron);

        Channel1D deflApproach = deflectionCurve.getApproach();
        Channel1D deflWithdraw = deflectionCurve.getWithdraw();

        Channel1D forceApproach = forceCurve.getApproach();
        Channel1D forceWithdraw = forceCurve.getWithdraw();

        Channel1DDataTransformation trimmer = settings.getTrimmer();

        Channel1DData trimmedForceApproach = trimmer.transform(forceApproach.getChannelData());
        Channel1DData trimmedForceWithdraw = trimmer.transform(forceWithdraw.getChannelData());

        Channel1DData trimmedFittedDeflectionBranchUnsorted = ForceCurveBranch.APPROACH.equals(settings.getFittedBranch()) ? trimmer.transform(deflApproach.getChannelData()) : trimmer.transform(deflWithdraw.getChannelData());

        Channel1DDataTransformation descendingSorter = new SortX1DTransformation(SortedArrayOrder.DESCENDING);
        Channel1DData trimmedFittedDeflectionBranch = descendingSorter.transform(trimmedFittedDeflectionBranchUnsorted);                

        Channel1DDataTransformation smoother = settings.getSmoother();

        Channel1DData transformedForceApproach = smoother.transform(trimmedForceApproach);
        Channel1DData transformedForceWithdraw = smoother.transform(trimmedForceWithdraw);

        Channel1DData transformedFittedDeflectionBranch = smoother.transform(trimmedFittedDeflectionBranch);

        Point2D recordingPoint = source.getRecordingPoint();

        double[] deflectionContactPointEstimated = contactEstimator.getContactPoint(transformedFittedDeflectionBranch, recordingPoint, springConstant);	

        ContactModelFit<?> modelFit = model.getModelFit(transformedFittedDeflectionBranch, deflectionContactPointEstimated, recordingPoint, settings);


        Point1DData indentationTansitionPoint = modelFit.getForceIndentationTransitionPoint();
        Point1DData maxDeformationPoint = modelFit.getMaximalDeformationPoint();

        List<ForceEventEstimate> adhesionEvents = settings.isCalculateAdhesionForce() ? adhesionEstimator.getEventEstimates(transformedForceApproach, transformedForceWithdraw, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) : Collections.emptyList();

        double mainAdhesionZ = !adhesionEvents.isEmpty() ? adhesionEvents.get(0).getEventData().getX(0) : Double.NEGATIVE_INFINITY;
        List<ForceEventEstimate > jumpEvents = jumpEstimator.getEventEstimates(transformedForceApproach, transformedForceWithdraw, Math.min(mainAdhesionZ, deflectionContactPointEstimated[0]), Double.POSITIVE_INFINITY);

        double z0 = deflectionContactPointEstimated[0];
        double d0 = deflectionContactPointEstimated[1];
        double F0 = springConstantNanoNewtonsPerMicron*d0;       

        double rSquared = settings.isCalculateRSquared() ? modelFit.getCoefficientOfDetermination() : Double.NaN;
        NumericalSpectroscopyProcessingResults numericalResults = new NumericalSpectroscopyProcessingResults(modelFit.getYoungModulus(), indentationTansitionPoint.getX(), indentationTansitionPoint.getY(), z0, d0, F0, adhesionEvents, jumpEvents, maxDeformationPoint.getX(), rSquared);	
        ProcessedSpectroscopyPack processed = new ProcessedSpectroscopyPack(source, numericalResults, modelFit, contactEstimator, settings, processable.getBatchIdentityTag());

        //builds VisualizablePack 

        VisualizationSettings visSettings = processable.getVisualizationSettings();
        VisualizableSpectroscopyPack visualizable = processed.visualize(visSettings);

        MapProcessingSettings mapSettings = processable.getMapSettings();
        SpectroscopyProcessingResult result = new SpectroscopyProcessingResult(processed, visualizable, mapSettings);

        return result;
    }    
}
