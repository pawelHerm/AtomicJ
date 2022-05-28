
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

import java.io.File;
import java.util.*;

import atomicJ.analysis.*;
import atomicJ.analysis.indentation.AdhesiveEnergyEstimationMethod;
import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.data.Channel2D;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.rois.ROI;

public class ProcessingBatchMemento extends AbstractModel
{
    private final double poissonRatio;
    private final double springConstant;
    private final boolean springConstantUseReadIn;
    private final double tipRadius;
    private final double tipHalfAngle;
    private final double tipTransitionRadius;

    private final double tipExponent;
    private final double tipFactor;

    private final int baselineDegree;
    private final int postcontactDegree;
    private final Map<PhotodiodeSignalType, Double> sensitivity;	
    private final Map<PhotodiodeSignalType, Boolean> useReadInSensitivity;
    private final BasicIndentationModel indentationModel;	
    private final double lowerCropping;
    private final double upperCropping;
    private final double rightCropping;
    private final double leftCropping;	
    private final boolean domainCropped;
    private final boolean rangeCropped;	
    private final double indentationLimit;
    private final double loadLimit;

    private final double fitIndentationLimit;
    private final double fitZMinimum;
    private final double fitZMaximum;

    private final ThicknessCorrectionMethod thicknessCorrectionMethod;
    private final boolean correctSubstrateEffect;
    private final boolean sampleAdherent;
    private final double sampleThickness;
    private final boolean useSampleTopography;
    private final File sampleTopographyFile;
    private final Channel2D sampleTopographyChannel;
    private final List<ROI> sampleROIs;

    private final SmootherType smootherType;
    private final boolean smoothed;
    private final double loessSpan; 
    private final Number loessIterations;
    private final Number savitzkyDegree;
    private final double savitzkySpan;
    private final boolean plotRecordedCurve;
    private final boolean plotRecordedCurveFit;
    private final boolean plotIndentation;
    private final boolean plotIndentationFit;
    private final boolean plotModulus;	
    private final boolean plotModulusFit;
    private final boolean includeCurvesInMaps;
    private final boolean plotMapAreaImages;
    private final boolean mapAreaImagesAvailable;

    private final boolean calculateAdhesionForce;
    private final boolean calculateRSquared;

    private final boolean showAveragedRecordedCurves;
    private final boolean showAveragedIndentationCurves;
    private final boolean showAveragedPointwiseModulusCurves;

    private final ErrorBarType averagedCurvesBarType;

    private final boolean contactPointAutomatic;

    private final boolean tipTransitionRadiusCalculable;
    private final boolean substrateEffectCorrectionKnown;
    private final boolean adhesiveEnergyRequired;
    private final boolean trimmingOnCurveSelectionPossible;

    private final BasicRegressionStrategy regressionStrategy;
    private final AutomaticContactEstimatorType automaticContactEstimator;
    private final ContactEstimationMethod contactEstimationMethod;
    private final ForceCurveBranch fittedBranch;

    private final AdhesiveEnergyEstimationMethod adhesiveEnergyEstimator;

    private final SpectroscopyResultDestination destination;

    private final String batchName;
    private final int batchNumber;

    public ProcessingBatchMemento(ProcessingBatchModel model)
    {
        this.batchName = model.getBatchName();
        this.batchNumber = model.getBatchNumber();

        this.poissonRatio = model.getPoissonRatio();
        this.springConstant = model.getSpringConstant();
        this.springConstantUseReadIn = model.getUseReadInSpringConstant();
        this.tipRadius = model.getTipRadius();
        this.tipHalfAngle = model.getTipHalfAngle();
        this.tipTransitionRadius = model.getTipTransitionRadius();

        this.tipExponent = model.getTipExponent();
        this.tipFactor = model.getTipFactor();

        this.baselineDegree = model.getBaselineDegree();
        this.postcontactDegree = model.getPostcontactDegree();
        this.sensitivity = model.getSensitivity(); 
        this.useReadInSensitivity = model.getUseReadInSensitivity();
        this.indentationModel = model.getIndentationModel(); 
        this.lowerCropping = model.getLowerCropping();
        this.upperCropping = model.getUpperCropping();
        this.rightCropping = model.getRightCropping();
        this.leftCropping = model.getLeftCropping();    
        this.domainCropped = model.isDomainToBeCropped();
        this.rangeCropped = model.isRangeToBeCropped();   
        this.indentationLimit = model.getIndentationLimit();
        this.loadLimit = model.getLoadLimit();

        this.fitIndentationLimit = model.getFitIndentationLimit();
        this.fitZMinimum = model.getFitZMinimum();
        this.fitZMaximum = model.getFitZMaximum();

        this.correctSubstrateEffect = model.isCorrectSubstrateEffect();
        this.thicknessCorrectionMethod = model.getThicknessCorrectionMethod();
        this.sampleAdherent = model.isSampleAdherent();
        this.sampleThickness = model.getSampleThickness();
        this.useSampleTopography = model.getUseSampleTopography();
        this.sampleTopographyFile = model.getSampleTopographyFile();
        this.sampleTopographyChannel = model.getSampleTopographyChannel();
        this.sampleROIs = model.getSampleROIs();

        this.smootherType = model.getSmootherType();
        this.smoothed = model.areDataSmoothed();
        this.loessSpan = model.getLoessSpan(); 
        this.loessIterations = model.getLoessIterations();
        this.savitzkyDegree = model.getSavitzkyDegree();
        this.savitzkySpan = model.getSavitzkySpan();
        this.plotRecordedCurve = model.isPlotRecordedCurve();
        this.plotRecordedCurveFit = model.isPlotRecordedCurveFit();
        this.plotIndentation = model.isPlotIndentation();
        this.plotIndentationFit = model.isPlotIndentationFit();
        this.plotModulus = model.isPlotModulus();    
        this.plotModulusFit = model.isPlotModulusFit();

        this.showAveragedRecordedCurves = model.isShowAveragedRecordedCurves();
        this.showAveragedIndentationCurves = model.isShowAveragedIndentationCurves();
        this.showAveragedPointwiseModulusCurves = model.isShowAveragedPointwiseModulusCurves();

        this.averagedCurvesBarType= model.getAveragedCurvesBarType();

        this.includeCurvesInMaps = model.isIncludeCurvesInMaps();
        this.plotMapAreaImages = model.isPlotMapAreaImages();
        this.mapAreaImagesAvailable = model.isMapAreaImagesAvailable();

        this.calculateRSquared = model.isCalculateRSquared();
        this.calculateAdhesionForce = model.isCalculateAdhesionForce();

        this.contactPointAutomatic = model.isContactPointAutomatic();

        this.tipTransitionRadiusCalculable = model.isTipSmoothTransitionRadiusCalculable();
        this.substrateEffectCorrectionKnown = model.isSubstrateEffectCorrectionKnown();
        this.adhesiveEnergyRequired = model.isAdhesiveEnergyRequired();
        this.trimmingOnCurveSelectionPossible = model.isCroppingOnCurveSelectionPossible();

        this.regressionStrategy = model.getRegressionStrategy();
        this.automaticContactEstimator = model.getAutomaticEstimator();
        this.contactEstimationMethod = model.getContactEstimationMethod();
        this.fittedBranch = model.getFittedBranch();

        this.adhesiveEnergyEstimator = model.getAdhesiveEnergyEstimationMethod();

        this.destination = model.getResultDestination();

        //        this.batchName = this.model.get;
        //        this.destination = this.model.getR;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public int getBatchNumber()
    {
        return batchNumber;
    }

    public SpectroscopyResultDestination getResultDestination()
    {
        return destination;
    }

    public BasicIndentationModel getIndentationModel()
    {
        return indentationModel;
    }


    public double getPoissonRatio()
    {
        return poissonRatio;
    }

    public boolean getCorrectSubstrateEffect()
    {
        return correctSubstrateEffect;
    }

    public boolean isSampleAdherent()
    {
        return sampleAdherent;
    }


    public double getSampleThickness()
    {
        return sampleThickness;
    }



    public boolean getUseSampleTopography()
    {
        return useSampleTopography;
    }


    public File getSampleTopographyFile()
    {
        return sampleTopographyFile;
    }


    public Channel2D getSampleTopographyChannel()
    {
        return sampleTopographyChannel;
    }

    public List<ROI> getSampleROIs()
    {
        return new ArrayList<>(sampleROIs);
    }

    public double getSpringConstant()
    {
        return springConstant;
    }

    public boolean getUseReadInSpringConstant()
    {
        return springConstantUseReadIn;
    }

    public int getBaselineDegree()
    {
        return baselineDegree;
    } 

    public int getPostcontactDegree() 
    {
        return postcontactDegree;
    }

    public Map<PhotodiodeSignalType, Double> getSensitivity()
    {
        return new EnumMap<>(sensitivity); 
    }

    public Map<PhotodiodeSignalType, Boolean> getUseReadInSensitivity()
    {
        return new EnumMap<>(useReadInSensitivity);
    }   

    public double getLoadLimit()
    {
        return loadLimit;
    }

    public double getIndentationLimit()
    {
        return indentationLimit;
    }


    public double getFitIndentationLimit()
    {
        return fitIndentationLimit;
    }

    public double getFitZMinimum()
    {
        return fitZMinimum;
    }

    public double getFitZMaximum()
    {
        return fitZMaximum;
    }

    public double getTipRadius()
    {
        return tipRadius;
    }



    public double getTipHalfAngle()
    {
        return tipHalfAngle;
    }

    public double getTipTransitionRadius()
    {
        return tipTransitionRadius;
    }


    public double getTipExponent()
    {
        return tipExponent;
    }

    public double getTipFactor()
    {
        return tipFactor;
    }

    public double getSmoothTransitionRadius()
    {
        double tr = Double.NaN;

        if(isTipSmoothTransitionRadiusCalculable())
        {
            double r = getTipRadius();
            double angle = Math.PI* getTipHalfAngle() / 180.;

            tr = r*Math.cos(angle);
        }

        return tr;
    }


    public boolean isContactPointAutomatic()
    {
        return contactPointAutomatic;
    }

    public AutomaticContactEstimatorType getAutomaticEstimator()
    {
        return automaticContactEstimator;
    }

    public ContactEstimationMethod getContactEstimationMethod()
    {
        return contactEstimationMethod;
    }


    public BasicRegressionStrategy getRegressionStrategy()
    {
        return regressionStrategy;
    }


    public ForceCurveBranch getFittedBranch()
    {
        return fittedBranch;
    }

    public AdhesiveEnergyEstimationMethod getAdhesiveEnergyEstimationMethod()
    {
        return adhesiveEnergyEstimator;
    }

    public double getLeftCropping()
    {
        return leftCropping;
    }

    public double getRightCropping()
    {
        return rightCropping;
    }


    public double getLowerCropping()
    {
        return lowerCropping;
    }

    public double getUpperCropping()
    {
        return upperCropping;
    }


    public boolean isDomainCropped()
    {
        return domainCropped; 
    }

    public boolean isRangeCropped()
    {
        return rangeCropped; 
    }

    public SmootherType getSmootherName()
    {
        return smootherType;
    }

    public boolean areDataSmoothed()
    {
        return smoothed;
    }

    public double getLoessSpan()
    {
        return loessSpan;
    }

    public Number getLoessIterations()
    {
        return loessIterations;
    }


    public Number getSavitzkyDegree()
    {
        return savitzkyDegree;
    }


    public double getSavitzkySpan()
    {
        return savitzkySpan;
    }

    public boolean isPlotRecordedCurve()
    {
        return plotRecordedCurve;
    }

    public boolean isPlotRecordedCurveFit()
    {
        return plotRecordedCurveFit;
    }


    public boolean isPlotIndentation()
    {
        return plotIndentation;
    }



    public boolean isPlotIndentationFit()
    {
        return plotIndentationFit;
    }


    public boolean isPlotModulus()
    {
        return plotModulus;
    }

    public boolean isPlotModulusFit()
    {
        return plotModulusFit;
    }


    public boolean isShowAveragedRecordedCurves()
    {
        return showAveragedRecordedCurves;
    }

    public boolean isShowAveragedIndentationCurves()
    {
        return showAveragedIndentationCurves;
    }

    public boolean isShowAveragedPointwiseModulusCurves()
    {
        return showAveragedPointwiseModulusCurves;
    }

    public ErrorBarType getAveragedCurvesBarType()
    {
        return averagedCurvesBarType;
    }


    public boolean isIncludeCurvesInMaps()
    {
        return includeCurvesInMaps;
    }

    public boolean isPlotMapAreaImages()
    {
        return plotMapAreaImages;
    }

    public boolean isMapAreaImagesAvailable()
    {
        return mapAreaImagesAvailable;
    }

    public boolean isCalculateRSquared()
    {
        return calculateRSquared;
    }

    public boolean isCalculateAdhesionForce()
    {
        return calculateAdhesionForce;
    }

    public boolean isCroppingOnCurveSelectionPossible()
    {
        return trimmingOnCurveSelectionPossible;
    }

    public boolean isTipSmoothTransitionRadiusCalculable()
    {
        return tipTransitionRadiusCalculable;
    }

    public boolean isSubstrateEffectCorrectionKnown()
    {
        return substrateEffectCorrectionKnown;
    }

    public boolean isAdhesiveEnergyRequired()
    {
        return adhesiveEnergyRequired;
    }

    public ThicknessCorrectionMethod getThicknessCorrectionMethod() 
    {
        return thicknessCorrectionMethod;
    }
}
