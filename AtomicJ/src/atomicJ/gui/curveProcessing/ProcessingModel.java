
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

package atomicJ.gui.curveProcessing;

import java.awt.Shape;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;

import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.analysis.indentation.AdhesiveEnergyEstimationMethod;
import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.curveProcessing.SpanType;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel2D;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.ResourceSelectionModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIWizardReceiver;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.statistics.LocalRegressionWeightFunction;

public class ProcessingModel extends AbstractModel implements CroppingReceiver, ResourceSelectionModel<SimpleSpectroscopySource>, ROIWizardReceiver, PropertyChangeListener
{
    private final List<ProcessingBatchModel> batchModels = new ArrayList<>();
    private final PreviewDestination previewDestination;
    private final PreprocessCurvesHandler preprocessHandler;
    private final SpectroscopyCurveAveragingHandler averagingHandler; 

    private final SpectroscopyResultDestination resultDestination;

    private ProcessingBatchModel currentBatch;

    private MapSourceHandler mapSourceHandler;
    private CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler;
    private NumericalResultsHandler<ProcessedSpectroscopyPack> resultHandler;

    private boolean sealed;    
    private int index;

    public ProcessingModel(SpectroscopyResultDestination resultDestination, PreviewDestination previewDestination, PreprocessCurvesHandler preprocessHandler)
    {
        this(resultDestination, previewDestination, preprocessHandler, Collections.emptyList(), Integer.toString(resultDestination.getPublishedBatchCount()),resultDestination.getPublishedBatchCount());
    }

    public ProcessingModel(SpectroscopyResultDestination resultDestination, PreviewDestination previewDestination, PreprocessCurvesHandler preprocessHandler, List<SimpleSpectroscopySource> sources, int index)
    {
        this(resultDestination, previewDestination, preprocessHandler, sources, Integer.toString(index), index);
    }

    public ProcessingModel(SpectroscopyResultDestination resultDestination, PreviewDestination previewDestination, PreprocessCurvesHandler preprocessHandler, List<SimpleSpectroscopySource> sources, String name, int number)
    {
        this.resultDestination = resultDestination;
        this.previewDestination = previewDestination;
        this.preprocessHandler = preprocessHandler;
        this.currentBatch = new ProcessingBatchModel(resultDestination, sources, name, number);	
        this.currentBatch.addPropertyChangeListener(this);

        this.mapSourceHandler = resultDestination.getDefaultMapSourceHandler();
        this.curveVisualizationHandler = resultDestination.getDefaultCurveVisualizationHandler();
        this.resultHandler = resultDestination.getDefaultNumericalResultsHandler();
        this.averagingHandler = resultDestination.getDefaultCurveAveragingHandler();

        batchModels.add(currentBatch);
    }

    public ProcessingModel(SpectroscopyResultDestination resultDestination, PreviewDestination previewDestination, PreprocessCurvesHandler preprocessHandler, List<ProcessingBatchModel> batches, boolean restricted)
    {
        this.resultDestination = resultDestination;
        this.previewDestination = previewDestination;
        this.preprocessHandler = preprocessHandler;
        this.sealed = restricted;
        this.currentBatch = batches.get(index);	
        this.currentBatch.addPropertyChangeListener(this);

        this.mapSourceHandler = resultDestination.getDefaultMapSourceHandler();
        this.curveVisualizationHandler = resultDestination.getDefaultCurveVisualizationHandler();
        this.resultHandler = resultDestination.getDefaultNumericalResultsHandler();
        this.averagingHandler = resultDestination.getDefaultCurveAveragingHandler();

        batchModels.addAll(batches);
    }

    //jumps


    public boolean getFindJumps()
    {
        return this.currentBatch.getFindJumps();
    }

    public void setFindJumps(boolean findJumpsNew)
    {
        this.currentBatch.setFindJumps(findJumpsNew);
    }

    public ForceCurveBranch getBranchWithJumps()
    {
        return this.currentBatch.getBranchWithJumps();
    }

    public void setBranchWithJumps(ForceCurveBranch branchWithJumpsNew)
    {
        this.currentBatch.setBranchWithJumps(branchWithJumpsNew);
    }

    public double getJumpMinDistanceFromContact()
    {
        return this.currentBatch.getJumpMinDistanceFromContact();
    }

    public void setJumpsMinDistanceFromContact(double minDistanceNew)
    {
        this.currentBatch.setJumpMinDistanceFromContact(minDistanceNew);
    }

    public LocalRegressionWeightFunction getJumpsWeightFunction()
    {
        return this.currentBatch.getJumpsWeightFunction();
    }

    public void setJumpsWeightFunction(LocalRegressionWeightFunction weightFunctionNew)
    {
        this.currentBatch.setJumpsWeightFunction(weightFunctionNew);
    }

    public int getJumpsPolynomialDegree()
    {
        return this.currentBatch.getPolynomialDegree();
    }

    public void setJumpsPolynomialDegree(int polynomialDegreeNew)
    {
        this.currentBatch.setPolynomialDegree(polynomialDegreeNew);
    }

    public double getJumpsSpan()
    {
        return currentBatch.getJumpsSpan();
    }

    public void setJumpsSpan(double spanNew)
    {
        this.currentBatch.setJumpsSpan(spanNew);
    }

    public SpanType getJumpsSpanType()
    {
        return this.currentBatch.getJumpsSpanType();
    }

    public void setJumpsSpanType(SpanType spanTypeNew)
    {
        this.currentBatch.setJumpsSpanType(spanTypeNew);
    }
    //jumps


    public MapSourceHandler getMapSourceHandle()
    {
        return mapSourceHandler;
    }

    public void setMapSourceHandler(MapSourceHandler mapSourceHandle)
    {
        this.mapSourceHandler = mapSourceHandle;
    }

    public CurveVisualizationHandler<VisualizableSpectroscopyPack> getCurveVisualizationHandle()
    {
        return curveVisualizationHandler;
    }

    public void setCurveVisualizationHandler(CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandle)
    {
        this.curveVisualizationHandler = curveVisualizationHandle;
    }

    public NumericalResultsHandler<ProcessedSpectroscopyPack> getNumericalResultsHandle()
    {
        return resultHandler;
    }

    public void setNumericalResultsHandler(NumericalResultsHandler<ProcessedSpectroscopyPack> resultHandleNew)
    {
        this.resultHandler = resultHandleNew;
    }

    public SpectroscopyResultDestination getResultDestination()
    {
        return resultDestination;
    }

    public PreviewDestination getPreviewDestination()
    {
        return previewDestination;
    }

    public Window getPublicationSite()
    {
        return resultDestination.getPublicationSite();
    }

    @Override
    public boolean isRestricted()
    {
        return sealed;
    }

    public void nextBatch()
    {
        int newIndex = index + 1;	
        if(newIndex == batchModels.size())
        {
            addNewBatch();
        }
        setCurrentBatch(newIndex);
    }

    public void previousBatch()
    {
        int newIndex = index - 1;	
        setCurrentBatch(newIndex);
    }

    private void setCurrentBatch(int newIndex)
    {
        int size = batchModels.size();
        boolean withinRange = (newIndex >= 0)&&(newIndex < size);

        if(!withinRange)
        {
            return;
        }

        if(this.index != newIndex)
        {
            this.index = newIndex;
            ProcessingBatchModel newModel = batchModels.get(newIndex);
            setCurrentBatch(newModel);            
        }	
    }

    private void setCurrentBatch(ProcessingBatchModel newModel)
    {
        ProcessingBatchModel oldModel = currentBatch;

        this.currentBatch = newModel;
        this.currentBatch.addPropertyChangeListener(this);

        if(oldModel != null)
        {
            oldModel.removePropertyChangeListener(this);               
            firePropertyChange(ProcessingModelInterface.CURRENT_BATCH_NUMBER, oldModel.getBatchNumber(), newModel.getBatchNumber());
        }        
    }

    public void addNewBatch()
    {
        if(sealed)
        {
            throw new IllegalStateException("Processing model is sealed - new batches cannot be added");
        }
        int batchNumber = batchModels.size() + resultDestination.getResultBatchesCoordinator().getPublishedBatchCount();
        ProcessingBatchModel newBatch = new ProcessingBatchModel(resultDestination,Integer.toString(batchNumber),batchNumber);
        batchModels.add(newBatch);
    }

    public void addNewBatch(List<SimpleSpectroscopySource> sources)
    {
        if(sealed)
        {
            throw new IllegalStateException("Processing model is sealed - new batches cannot be added");
        }
        int batchNumber = batchModels.size() + resultDestination.getResultBatchesCoordinator().getPublishedBatchCount();
        ProcessingBatchModel newBatch = new ProcessingBatchModel(resultDestination, sources, Integer.toString(batchNumber),batchNumber);
        batchModels.add(newBatch);
    }

    public boolean isCurrentBatchLast()
    {
        boolean isLast = sealed && (batchModels.size() - 1 == index);
        return isLast;
    }

    @Override
    public boolean areSourcesSelected()
    {
        return currentBatch.isNonEmpty();
    }

    public boolean areSettingsSpecified()
    {
        return currentBatch.areSettingSpecified();
    }

    public boolean areBasicSettingsSpecified()
    {
        return currentBatch.areBasicSettingsSpecified();
    }

    public boolean canProcessingOfCurrentBatchBeFinished()
    {
        boolean nonEmpty = currentBatch.isNonEmpty();
        boolean inputProvided = currentBatch.isNecessaryInputProvided();

        boolean canBeFinished = inputProvided && nonEmpty;
        return canBeFinished;
    }

    public boolean canProcessingBeFinished()
    {
        boolean allInputProvided = true;
        boolean isAtLeastOneBatchNonEmpty = false;
        for(ProcessingBatchModel batch: batchModels)
        {
            boolean nonEmpty = batch.isNonEmpty();
            isAtLeastOneBatchNonEmpty = isAtLeastOneBatchNonEmpty||nonEmpty;
            boolean inputProvided = batch.isNecessaryInputProvided();
            allInputProvided = allInputProvided&&inputProvided;
        }
        boolean canBeFinished = allInputProvided && isAtLeastOneBatchNonEmpty;
        return canBeFinished;
    }

    public int getCurrentBatchNumber()
    {
        return currentBatch.getBatchNumber();
    }

    @Override
    public String getIdentifier()
    {
        return Integer.toString(currentBatch.getBatchNumber());
    }

    public int getBatchNumber()
    {
        return batchModels.size();
    }

    public String getCurrentBatchName()
    {
        return currentBatch.getBatchName();
    }

    public void setBatchName(String batchNameNew)
    {
        currentBatch.setBatchName(batchNameNew);
    }

    public boolean isSensitivityInputEnabled(PhotodiodeSignalType signalType)
    {
        return currentBatch.isSensitivityInputEnabled(signalType);
    }

    public boolean isSensitivityReadInEnabled(PhotodiodeSignalType signalType)
    {
        return currentBatch.isSensitivityReadInEnabled(signalType);
    }

    public boolean getUseReadInSensitivity(PhotodiodeSignalType signalType)
    {
        return currentBatch.getUseReadInSensitivity(signalType);
    }

    public void setUseReadInSensitivity(PhotodiodeSignalType signalType, Boolean useReadInSensitivityNew)
    {
        currentBatch.setUseReadInSensitivity(signalType, useReadInSensitivityNew);      
    }

    public boolean isSpringConstantReadInEnabled()
    {
        return currentBatch.isSpringConstantReadInEnabled();
    }

    public boolean isSpringConstantInputEnabled()
    {
        return currentBatch.isSpringConstantInputEnabled();
    }

    public void setUseReadInSpringConstant(boolean useReadInNew)
    {
        currentBatch.setUseReadInSpringConstant(useReadInNew);   
    }

    public boolean getUseReadInSpringConstant()
    {
        return currentBatch.getUseReadInSpringConstant();
    }

    @Override
    public void setSources(List<SimpleSpectroscopySource> newSources)
    {
        if(sealed)
        {
            throw new IllegalStateException("The ProcessungModel is sealed so that no sources can be added to it");
        }

        currentBatch.setSources(newSources);
    }

    public boolean containsForceMapData()
    {
        return currentBatch.containsForceMapData();
    }

    @Override
    public boolean isSourceFilteringPossible()
    {
        return currentBatch.isSourceFilteringPossible();
    }

    public void filterSources(List<Shape> shapes)
    {
        List<SimpleSpectroscopySource> sourcesOld = new ArrayList<>(getSources());

        List<SimpleSpectroscopySource> sourcesNew = new ArrayList<>();
        List<SimpleSpectroscopySource> sourcesFilteredOut = new ArrayList<>(sourcesOld);

        int n = shapes.size();

        if(n < 1)
        {
            return;
        }

        Shape currentShape = shapes.get(0);

        for(SimpleSpectroscopySource source : sourcesOld)
        {
            Point2D recordingPoint = source.getRecordingPoint();            
            if(recordingPoint == null || currentShape.contains(recordingPoint))
            {
                sourcesNew.add(source);
                sourcesFilteredOut.remove(source);
            }           
        }

        setSources(sourcesNew);

        for(int i = 1; i < n; i++)
        {
            List<SimpleSpectroscopySource> batchSources = new ArrayList<>();

            Shape batchShape = shapes.get(i);

            for(SimpleSpectroscopySource source : sourcesOld)
            {
                Point2D recordingPoint = source.getRecordingPoint();

                if(recordingPoint == null || batchShape.contains(recordingPoint))
                {
                    batchSources.add(source);
                    sourcesFilteredOut.remove(source);
                }               
            }

            addNewBatch(batchSources);
        }

        int filteredOutCount = sourcesFilteredOut.size();

        if(filteredOutCount > 0)
        {
            int answer = JOptionPane.showConfirmDialog(getResultDestination().getPublicationSite(),
                    filteredOutCount + " force curves were not included in any patch." + " Do you want to create another batch for them?",
                    null, JOptionPane.YES_NO_OPTION);

            if(answer == JOptionPane.OK_OPTION)
            {
                addNewBatch(sourcesFilteredOut);
            }
        }      
    }

    @Override
    public void addSources(List<SimpleSpectroscopySource> sourcesToAdd)
    {
        currentBatch.addSources(sourcesToAdd);
    }

    @Override
    public void removeSources(List<SimpleSpectroscopySource> sourcesToRemove)
    {
        currentBatch.removeSources(sourcesToRemove);
    }

    @Override
    public List<SimpleSpectroscopySource> getSources()
    {
        return currentBatch.getSources();
    }

    public Set<ForceCurveBranch> getAvailableBranches()
    {
        return currentBatch.getAvailableBranches();
    }

    public File getCommonSourceDirectory()
    {
        return currentBatch.getCommonSourceDirectory();
    }

    public void setIndentationModel(BasicIndentationModel indentaionModelNew)
    {
        currentBatch.setIndentationModel(indentaionModelNew);
    }

    public BasicIndentationModel getIndentationModel()
    {
        return currentBatch.getIndentationModel();
    }

    public void setPoissonRatio(double poissonRatioNew)
    {
        currentBatch.setPoissonRatio(poissonRatioNew);        
    }

    public double getPoissonRatio()
    {
        return currentBatch.getPoissonRatio();
    }

    public void setSpringConstant(double springConstantNew)
    {
        currentBatch.setSpringConstant(springConstantNew);
    }

    public double getSpringConstant()
    {
        return currentBatch.getSpringConstant();
    }

    public void setBaselineDegree(int baselineDegreeNew)
    {        
        currentBatch.setBaselineDegree(baselineDegreeNew);
    }

    public int getBaselineDegree()
    {
        return currentBatch.getBaselineDegree();
    }

    public void setPostcontactDegree(int postcontactDegreeNew)
    {        
        currentBatch.setPostcontactDegree(postcontactDegreeNew);
    }

    public int getPostcontactDegree()
    {
        return currentBatch.getPostcontactDegree();
    }

    public boolean isPostcontactDegreeInputEnabled()
    {
        return currentBatch.isPosctcontactDegreeInputEnabled();
    }

    public Set<PhotodiodeSignalType> getSensitivityPhotodiodeSignalTypes()
    {
        return currentBatch.getSensitivityPhotodiodeSignalTypes();
    }

    public void setSensitivity(PhotodiodeSignalType signalType, Double sensitivityNew)
    {
        currentBatch.setSensitivity(signalType, sensitivityNew);
    }

    public Double getSensitivity(PhotodiodeSignalType signalType)
    {
        return currentBatch.getSensitivity(signalType);
    }

    public Map<PhotodiodeSignalType, Double> getSensitivity()
    {
        return currentBatch.getSensitivity();
    }

    public void setLoadLimit(double loadLimitNew)
    {
        currentBatch.setLoadLimit(loadLimitNew);
    }

    public double getLoadLimit()
    {
        return currentBatch.getLoadLimit();
    }

    public void setIndentationLimit(double indentationLimitNew)
    {
        currentBatch.setIndentationLimit(indentationLimitNew);
    }

    public double getIndentationLimit()
    {
        return currentBatch.getIndentationLimit();
    }

    public double getFitZMinimum()
    {
        return currentBatch.getFitZMinimum();
    }

    public void setFitZMinimum(double fitZMinimumNew)
    {
        currentBatch.setFitZMinimum(fitZMinimumNew);
    }

    public double getFitZMaximum()
    {
        return currentBatch.getFitZMaximum();
    }

    public void setFitZMaximum(double fitZMaximumNew)
    {
        currentBatch.setFitZMaximum(fitZMaximumNew);
    }

    public double getTipRadius()
    {
        return currentBatch.getTipRadius();
    }

    public void setTipRadius(double tipRadiusNew)
    {
        currentBatch.setTipRadius(tipRadiusNew);
    }

    public double getTipHalfAngle()
    {
        return currentBatch.getTipHalfAngle();
    }

    public void setTipHalfAngle(double tipHalfAngleNew)
    {
        currentBatch.setTipHalfAngle(tipHalfAngleNew);
    }

    public double getTipExponent()
    {
        return currentBatch.getTipExponent();
    }

    public void setTipExponent(double tipExponentNew)
    {
        currentBatch.setTipExponent(tipExponentNew);
    }

    public double getTipFactor()
    {
        return currentBatch.getTipFactor();
    }

    public void setTipFactor(double tipFactorNew)
    {
        currentBatch.setTipFactor(tipFactorNew);
    }

    public double getTipTransitionRadius()
    {
        return currentBatch.getTipTransitionRadius();
    }

    public void setTipTransitionRadius(double tipTransitionRadiusNew)
    {
        currentBatch.setTipTransitionRadius(tipTransitionRadiusNew);
    }

    public boolean isTipSmoothTransitionRadiusCalculable()
    {
        return currentBatch.isTipSmoothTransitionRadiusCalculable();
    }

    public void setSmoothTransitionRadius()
    {
        currentBatch.setSmoothTransitionRadius();
    }

    public void setContactPointAutomatic(boolean contactPointAutomaticNew)
    {
        currentBatch.setContactPointAutomatic(contactPointAutomaticNew);
    }

    public boolean isContactPointAutomatic()
    {
        return currentBatch.isContactPointAutomatic();
    }

    public void setAutomaticContactEstimator(AutomaticContactEstimatorType estimatorNew)
    {
        currentBatch.setAutomaticContactEstimator(estimatorNew);
    }

    public AutomaticContactEstimatorType getAutomaticContactEstimator()
    {
        return currentBatch.getAutomaticEstimator();
    }

    public void setContactEstimationMethod(ContactEstimationMethod contactEstimationMethodNew)
    {
        currentBatch.setContactEstimationMethod(contactEstimationMethodNew);
    }

    public ContactEstimationMethod getContactEstimationMethod()
    {
        return currentBatch.getContactEstimationMethod(); 
    }

    public void setRegressionStartegy(BasicRegressionStrategy regressionStrategyNew)
    {
        currentBatch.setRegressionStrategy(regressionStrategyNew);
    }

    public BasicRegressionStrategy getRegressionStrategy()
    {
        return currentBatch.getRegressionStrategy();
    }

    public void setFittedBranch(ForceCurveBranch fittedBranchNew)
    {
        currentBatch.setFittedBranch(fittedBranchNew);
    }

    public ForceCurveBranch getFittedBranch()
    {
        return currentBatch.getFittedBranch();
    }

    public boolean isAdhesiveEnergyRequired()
    {
        return currentBatch.isAdhesiveEnergyRequired();
    }

    public void setAdhesiveEnergyEstimationMethod(AdhesiveEnergyEstimationMethod methodNew)
    {
        currentBatch.setAdhesiveEnergyEstimationMethod(methodNew);
    }

    public AdhesiveEnergyEstimationMethod getAdhesiveEnergyEstimationMethod()
    {
        return currentBatch.getAdhesiveEnergyEstimationMethod();
    }

    @Override
    public void setLeftCropping(double leftTrimmingNew)
    {
        currentBatch.setLeftCropping(leftTrimmingNew);
    }

    public double getLeftCropping()
    {
        return currentBatch.getLeftCropping();
    }

    @Override
    public void setRightCropping(double rightTrimmingNew)
    {
        currentBatch.setRightCropping(rightTrimmingNew);
    }

    public double getRightCropping()
    {
        return currentBatch.getRightCropping();
    }

    @Override
    public void setLowerCropping(double lowerTrimmingNew)
    {
        currentBatch.setLowerCropping(lowerTrimmingNew);
    }

    public double getLowerCropping()
    {
        return currentBatch.getLowerCropping();
    }

    @Override
    public void setUpperCropping(double upperTrimmingNew)
    {
        currentBatch.setUpperCropping(upperTrimmingNew);
    }

    public double getUpperCropping()
    {
        return currentBatch.getUpperCropping();
    }

    public void setDomainCropping(boolean domainTrimmedNew)
    {
        currentBatch.setDomainCropped(domainTrimmedNew);
    }

    public boolean isDomainToBeCropped()
    {
        return currentBatch.isDomainToBeCropped();
    }

    public void setRangeCropped(boolean rangeTrimmedNew)
    {		
        currentBatch.setRangeCropped(rangeTrimmedNew);
    }

    public boolean isRangeToBeCropped()
    {
        return currentBatch.isRangeToBeCropped();
    }

    public void setSmootherType(SmootherType smootherNameNew)
    {
        currentBatch.setSmootherType(smootherNameNew);
    }

    public SmootherType getSmootherType()
    {
        return currentBatch.getSmootherType();
    }

    public void setDataSmoothed(boolean dataSmoothedNew)
    {		
        currentBatch.setDataSmoothed(dataSmoothedNew);
    }

    public boolean areDataSmoothed()
    {
        return currentBatch.areDataSmoothed();
    }

    public void setLoessSpan(double loessSpanNew)
    {
        currentBatch.setLoessSpan(loessSpanNew);
    }

    public double getLoessSpan()
    {
        return currentBatch.getLoessSpan();
    }

    public void setLoessIterations(Number loessIterationsNew)
    {
        currentBatch.setLoessIterations(loessIterationsNew);
    }

    public Number getLoessIterations()
    {
        return currentBatch.getLoessIterations();
    }

    public void setSavitzkyDegree(Number savitzkyDegreeNew)
    {
        currentBatch.setSavitzkyDegree(savitzkyDegreeNew);
    }

    public Number getSavitzkyDegree()
    {
        return currentBatch.getSavitzkyDegree();
    }

    public void setSavitzkySpan(double savitzkySpanNew)
    {
        currentBatch.setSavitzkySpan(savitzkySpanNew);
    }

    public double getSavitzkySpan()
    {
        return currentBatch.getSavitzkySpan();
    }

    public boolean isSubstrateCorrectionKnown()
    {
        return currentBatch.isSubstrateEffectCorrectionKnown();
    }

    public boolean getCorrectSubstrateEffect()
    {
        return currentBatch.isCorrectSubstrateEffect();
    }

    public void setCorrectSubstrateEffect(boolean correctSubstrateEffectNew)
    {
        currentBatch.setCorrectSubstrateEffect(correctSubstrateEffectNew);
    }

    public void setThicknessCorrectionMethod(ThicknessCorrectionMethod thicknessCorrectionMethodNew)
    {
        currentBatch.setThicknessCorrectionMethod(thicknessCorrectionMethodNew);
    }

    public ThicknessCorrectionMethod getThicknessCorrectionMethod()
    {
        return currentBatch.getThicknessCorrectionMethod();
    }

    public Set<ThicknessCorrectionMethod> getApplicableThicknessCorrectionMethods()
    {
        return currentBatch.getApplicableThicknessCorrectionMethods();
    }

    public boolean isSampleAdherent()
    {
        return currentBatch.isSampleAdherent();
    }

    public void setSampleAdherent(boolean sampleAdherentNew)
    {
        currentBatch.setSampleAdherent(sampleAdherentNew);
    }

    public double getSampleThickness()
    {
        return currentBatch.getSampleThickness();
    }

    public void setSampleThickness(double sampleThicknessNew)
    {
        currentBatch.setSampleThickness(sampleThicknessNew);
    }

    public boolean getUseSampleTopography()
    {
        return currentBatch.getUseSampleTopography();
    }

    public void setUseSampleTopography(boolean useSampleTopographyNew)
    {
        currentBatch.setUseSampleTopography(useSampleTopographyNew);
    }

    public File getSampleTopographyFile()
    {
        return currentBatch.getSampleTopographyFile();
    }

    public void setSampleTopographyFile(File topographyFileNew)
    {
        currentBatch.setSampleTopographyFile(topographyFileNew);
    }

    public Channel2D getSampleTopographyChannel()
    {
        return currentBatch.getSampleTopographyChannel();
    }

    public void setSampleTopographyChannel(Channel2D topographyChannelNew)
    {
        currentBatch.setSampleTopographyChannel(topographyChannelNew);
    } 

    @Override
    public void setSelectedROIs(Channel2DSource<?> image, Channel2D channel, List<ROI> substrateROIs) 
    {        
        currentBatch.setSelectedROIs(image, channel, substrateROIs);
    }

    public List<ROI> getSampleROIs()
    {
        return currentBatch.getSampleROIs();
    }

    public boolean isPlotRecordedCurve()
    {
        return currentBatch.isPlotRecordedCurve();
    }

    public void setPlotRecordedCurve(boolean plotCurveNew)
    {
        currentBatch.setPlotRecordedCurve(plotCurveNew);
    }

    public boolean isPlotRecordedCurveFit()
    {
        return currentBatch.isPlotRecordedCurveFit();
    }

    public void setPlotRecordedCurveFit(boolean plotCurveFitNew)
    {
        currentBatch.setPlotRecordedCurveFit(plotCurveFitNew);
    }

    public boolean isPlotIndentation()
    {
        return currentBatch.isPlotIndentation();
    }

    public void setPlotIndentation(boolean plotIndentationNew)
    {
        currentBatch.setPlotIndentation(plotIndentationNew);
    }

    public boolean isPlotIndentationFit()
    {
        return currentBatch.isPlotIndentationFit();
    }

    public void setPlotIndentationFit(boolean plotIndentationFitNew)
    {
        currentBatch.setPlotIndentationFit(plotIndentationFitNew);
    }

    public boolean isPlotModulus()
    {
        return currentBatch.isPlotModulus();
    }

    public void setPlotModulus(boolean plotModulusNew)
    {
        currentBatch.setPlotModulus(plotModulusNew);
    }

    public boolean isPlotModulusFit()
    {
        return currentBatch.isPlotModulusFit();
    }

    public void setPlotModulusFit(boolean plotModulusFitNew)
    {
        currentBatch.setPlotModulusFit(plotModulusFitNew);
    }

    public boolean isAveragingEnabled()
    {
        return currentBatch.isAveragingEnabled();
    }
    
    public boolean isShowAveragedRecordedCurves()   
    {
        return currentBatch.isShowAveragedRecordedCurves();
    }

    public void setShowAveragedRecordedCurves(boolean showAveragedRecordedCurvesNew)   
    {
        currentBatch.setShowAveragedRecordedCurves(showAveragedRecordedCurvesNew);
    }

    public int getAveragedRecordedCurvesPointCount()
    {
        return currentBatch.getAveragedRecordedCurvesPointCount();
    }
    
    public void setAveragedRecordedCurvesPointCount(int averagedRecordedCurvesPointCountNew)   
    {
        currentBatch.setAveragedRecordedCurvesPointCount(averagedRecordedCurvesPointCountNew);
    }
        
    public boolean isShowAveragedIndentationCurves()    
    {
        return currentBatch.isShowAveragedIndentationCurves();
    }

    public void setShowAveragedIndentationCurves(boolean showAveragedIndentationCurvesNew)    
    {
        currentBatch.setShowAveragedIndentationCurves(showAveragedIndentationCurvesNew);
    }
    
    public int getAveragedIndentationCurvesPointCount()
    {
        return currentBatch.getAveragedIndentationCurvesPointCount();
    }
    
    public void setAveragedIndentationCurvesPointCount(int averagedIndentationCurvesPointCountNew)   
    {
        currentBatch.setAveragedIndentationCurvesPointCount(averagedIndentationCurvesPointCountNew);
    }

    public boolean isShowAveragedPointwiseModulusCurves()
    {
        return currentBatch.isShowAveragedPointwiseModulusCurves();
    }

    public void setShowAveragedPointwiseModulusCurves(boolean showAveragedPointwiseModulusCurvesNew)
    {
        currentBatch.setShowAveragedPointwiseModulusCurves(showAveragedPointwiseModulusCurvesNew);
    }

    public int getAveragedPointwiseModulusCurvesPointCount()
    {
        return currentBatch.getAveragedPointwiseModulusCurvesPointCount();
    }
    
    public void setAveragedPointwiseModulusCurvesPointCount(int averagedPointwiseModulusCurvesPointCountNew)   
    {
        currentBatch.setAveragedPointwiseModulusCurvesPointCount(averagedPointwiseModulusCurvesPointCountNew);
    }
    
    public ErrorBarType getAveragedCurvesBarType()
    {
        return currentBatch.getAveragedCurvesBarType();
    }

    public void setAveragedCurvesBarType(ErrorBarType averagedCurvesBarTypeNew)
    {
        currentBatch.setAveragedCurvesBarType(averagedCurvesBarTypeNew);
    }  

    public boolean isIncludeCurvesInMaps()
    {
        return currentBatch.isIncludeCurvesInMaps();
    }

    public void setIncludeCurvesInMaps(boolean includeCurvesInMapsNew)
    {
        currentBatch.setIncludeCurvesInMaps(includeCurvesInMapsNew);
    }

    public boolean isIncludeCurvesInMapsEnabled()
    {
        return currentBatch.isIncludeCurvesInMapsEnabled();
    }

    public boolean isPlotMapAreaImages()
    {
        return currentBatch.isPlotMapAreaImages();
    }

    public void setPlotMapAreaImages(boolean plotMapAreaImagesNew)
    {
        currentBatch.setPlotMapAreaImages(plotMapAreaImagesNew);
    }

    public boolean isPlotMapAreaImagesEnabled()
    {
        return currentBatch.isMapAreaImagesAvailable();
    }

    public boolean isCalculateRSquared()
    {
        return currentBatch.isCalculateRSquared();
    }

    public void setCalculateRSquared(boolean calculateRSquaredNew)
    {
        currentBatch.setCalculateRSquared(calculateRSquaredNew);
    }

    public boolean isCalculateAdhesionForce()
    {
        return currentBatch.isCalculateAdhesionForce();
    }

    public void setCalculateAdhesionForce(boolean calculateAdhesionForceNew)
    {
        currentBatch.setCalculateAdhesionForce(calculateAdhesionForceNew);
    }

    public boolean isTrimmingOnCurveSelectionPossible()
    {
        return currentBatch.isCroppingOnCurveSelectionPossible();
    }

    public Dataset1DCroppingModel<SpectroscopyCurve<Channel1D>> getCroppingModel()
    {
        return currentBatch.getCroppingModel();
    }

    @Override
    public void showPreview()
    {
        List<SimpleSpectroscopySource> sources = currentBatch.getSources();
        showPreview(sources);
    }

    @Override
    public void showPreview(List<SimpleSpectroscopySource> sources)
    {
        showPreprocessing(sources);
    }

    public void showPreprocessing(List<SimpleSpectroscopySource> sources)
    {
        preprocessHandler.preprocess(sources);
    }

    @Override
    public void cancel()
    {
        resultDestination.endProcessing();
    }

    public void processCurvesConcurrently()
    {         
        Processor<ProcessableSpectroscopyPack, SpectroscopyProcessingResult> processor = StandardSpectroscopyProcessor.getInstance();
        List<ProcessableSpectroscopyPack> allPacks = getAllProcessablePacks();

        for(ProcessingBatchModel batchModel : batchModels)
        {
            averagingHandler.registerAveragingSettings(batchModel.getBatchIdentityTag(), batchModel.getCurveAveragingSettings());
        }

        if(!allPacks.isEmpty())
        {
            ConcurrentProcessingTask<ProcessableSpectroscopyPack, SpectroscopyProcessingResult> task = new ConcurrentProcessingTask<>(allPacks, processor, new SpectroscopyProcessingHandler(resultDestination, mapSourceHandler, curveVisualizationHandler, resultHandler, averagingHandler));
            task.execute();
        }
        else
        {
            resultDestination.withdrawPublication();
            resultDestination.endProcessing();
        }
    }

    public int processCurves()
    {
        Processor<ProcessableSpectroscopyPack, SpectroscopyProcessingResult> processor = StandardSpectroscopyProcessor.getInstance();
        List<ProcessableSpectroscopyPack> allPacks = getAllProcessablePacks();


        for(ProcessingBatchModel batchModel : batchModels)
        {
            averagingHandler.registerAveragingSettings(batchModel.getBatchIdentityTag(), batchModel.getCurveAveragingSettings());
        }

        int failures = 0;

        if(!allPacks.isEmpty())
        {
            SpectroscopyProcessingHandler processingHandle = new SpectroscopyProcessingHandler(resultDestination, mapSourceHandler, curveVisualizationHandler, resultHandler, averagingHandler);
            List<SpectroscopyProcessingResult> allProcessingResult = new ArrayList<>();

            for(ProcessableSpectroscopyPack pack : allPacks)
            {
                try
                {
                    SpectroscopyProcessingResult result = processor.process(pack);                  
                    allProcessingResult.add(result);                                                 
                }           

                catch(Exception e)
                {
                    e.printStackTrace();
                    failures++;
                }
            }

            processingHandle.acceptAndSegregateResults(allProcessingResult);
            processingHandle.reactToFailures(failures);
            processingHandle.sendResultsToDestination();
            processingHandle.endProcessing();          
        }
        else
        {
            resultDestination.withdrawPublication();
            resultDestination.endProcessing();
        }

        return failures;
    }

    public List<ProcessableSpectroscopyPack> getAllProcessablePacks()
    {
        List<ProcessableSpectroscopyPack> allPacks = new ArrayList<>();
        for(ProcessingBatchModel model: batchModels)
        {       
            if(model.isNonEmpty())
            {               
                List<ProcessableSpectroscopyPack> packs = model.buildProcessingBatch();

                allPacks.addAll(packs);
            }
        }

        return allPacks;
    }

    public Properties getProperties()
    {
        return currentBatch.getProperties();
    }

    public void loadProperties(Properties properties)
    {
        currentBatch.loadProperties(properties);
    }

    @Override
    public Window getParent() 
    {
        return getResultDestination().getPublicationSite();
    }

    @Override
    public String getTaskName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTaskDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFirst() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLast() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void back() {
        // TODO Auto-generated method stub

    }

    @Override
    public void next() {
        // TODO Auto-generated method stub

    }

    @Override
    public void skip() {
        // TODO Auto-generated method stub

    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isBackEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNextEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSkipEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFinishEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNecessaryInputProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        firePropertyChange(evt);
    }
}
