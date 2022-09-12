
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

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.jfree.data.Range;

import atomicJ.analysis.*;
import atomicJ.analysis.indentation.AdhesiveEnergyEstimationMethod;
import atomicJ.analysis.indentation.BluntConeWithSphericalCap;
import atomicJ.analysis.indentation.BluntPyramid;
import atomicJ.analysis.indentation.Cone;
import atomicJ.analysis.indentation.ContactModel;
import atomicJ.analysis.indentation.Hyperboloid;
import atomicJ.analysis.indentation.IndentationIndependentContactEstimationGuide;
import atomicJ.analysis.indentation.PowerShapedTip;
import atomicJ.analysis.indentation.Pyramid;
import atomicJ.analysis.indentation.Sphere;
import atomicJ.analysis.indentation.TruncatedCone;
import atomicJ.analysis.indentation.TruncatedPyramid;
import atomicJ.curveProcessing.Crop1DTransformation;
import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.SpanType;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.InputNotProvidedException;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.SmootherType.BasicSmootherModel;
import atomicJ.gui.curveProcessing.SpectroscopyCurveAveragingSettings.AveragingSettings;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.imageProcessing.FixMinimumOperation;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.MultiplyOperation;
import atomicJ.readers.MapDelayedCreator;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.IdentityTag;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.MultiMap;
import atomicJ.utilities.Validation;

public class ProcessingBatchModel extends AbstractModel implements BasicSmootherModel
{
    private static final double TOLERANCE = 1e-10;

    static final String SAVITZKY_GOLAY = "SavitzkyGolay";
    static final String LOCAL_REGRESSION = "LocalRegression";

    public static final String SELECTED_ROIS = "SelectedRois";

    static final String AVAILABLE_FORCE_CURVE_BRANCHES = "AvailableForceCurveBranches";

    static final String CONTAINS_FORCE_VOLUME_DATA =  "ContainsForceVolumeData";
    static final String CROPPING_ON_CURVE_SELECTION_POSSIBLE = "CroppingOnCurveSelectionPossible";
    static final String POISSON_RATIO = "PoissonRatio";
    static final String BASELINE_DEGREE = "BaselineDegree";
    static final String POSTCONTACT_DEGREE = "PostcontactDegree";
    static final String POSTCONTACT_DEGREE_INPUT_ENABLED = "PostcontactDegreeInputEnabled";
    static final String INDENTATION_MODEL = "IndentationModel";
    static final String TIP_RADIUS = "TipRadius";
    static final String TIP_HALF_ANGLE = "TipHalfAngle";
    static final String TIP_TRANSITION_RADIUS = "TipTransitionRadius";
    static final String TIP_EXPONENT = "TipExponent";
    static final String TIP_FACTOR = "TipFactor";
    static final String TIP_TRANSITION_RADIUS_CALCULABLE = "TipTransitionRadiusCalculable";

    static final String SPRING_CONSTANT = "SpringConstant";
    static final String SPRING_CONSTANT_INPUT_ENABLED = "SpringConstantInputEnabled";
    static final String SPRING_CONSTANT_READ_IN = "SpringConstantReadIn";
    static final String SPRING_CONSTANT_USE_READ_IN = "SpringConstantUseReadIn";
    static final String SPRING_CONSTANT_USE_READ_IN_ENABLED = "SpringConstantUseReadInEnabled";

    static final String SENSITIVITY = "Sensitivity";
    static final String SENSITIVITY_INPUT_ENABLED = "SensitivityInputEnabled";
    static final String SENSITIVITY_READ_IN = "SensitivityReadIn";
    static final String SENSITIVITY_USE_READ_IN = "SensitivityUseReadIn";
    static final String SENSITIVITY_USE_READ_IN_ENABLED = "SensitivityUseReadInEnabled";
    static final String SENSITIVITY_PHOTODIODE_SIGNALS = "SensitivityPhotodiodeSignals";

    static final String DOMAIN_CROPPED = "DomainCropped";
    static final String RANGE_CROPPED = "RangeCropped";
    static final String LEFT_CROPPING = "LeftCropping";
    static final String RIGHT_CROPPING = "RightCropping";
    static final String UPPER_CROPPING = "UpperCropping";
    static final String LOWER_CROPPING = "LowerCropping";
    static final String LOAD_LIMIT = "LoadLimit";
    static final String INDENTATION_LIMIT = "IndentationLimit";

    static final String FIT_INDENTATION_LIMIT = "FitIndentationLimit";
    static final String FIT_Z_MINIMUM = "FitZMinimum";
    static final String FIT_Z_MAXIMUM = "FitZMaximum";

    static final String CORRECT_SUBSTRATE_EFFECT = "CorrectSubstrateEffect";
    static final String SUBSTRATE_EFFECT_CORRECTION_KNOWN = "SubstrateEffectCorrectionKnown";
    static final String ADHESIVE_ENERGY_REQUIRED = "AdhesiveEnergyREquired";

    static final String SAMPLE_ADHERENT = "SampleAdherent";
    static final String SAMPLE_THICKNESS = "SampleThickness";
    static final String THICKNESS_CORRECTION_METHOD = "ThicknessCorrectionMethod";
    static final String APPLICABLE_THICKNESS_CORRECTION_METHODS = "ApplicableThicknessCorrectionMethods";
    static final String USE_SAMPLE_TOPOGRAPHY = "UseSampleTopography";
    static final String SAMPLE_TOPOGRAPHY_FILE = "SampleTopographyFile";
    static final String SAMPLE_TOPOGRAPHY_CHANNEL = "SampleTopographyChannel";
    static final String SAMPLE_ROIS = "SampleROIs";

    static final String CURVE_SMOOTHED = "CurveSmoothed";
    static final String SMOOTHER_TYPE = "Smoother";
    static final String LOESS_SPAN = "LoessSpan";
    static final String LOESS_ITERATIONS = "LoessIterations";
    static final String SAVITZKY_DEGREE = "Savitzky_Degree";
    static final String SAVITZKY_SPAN = "Savitzky_Span";

    static final String CONTACT_POINT_AUTOMATIC = "ContactPointAutomatic";
    static final String AUTOMATIC_CONTACT_ESTIMATOR = "ContactEstimator";
    static final String CONTACT_ESTIMATION_METHOD = "ContactEstimationMethod";
    static final String CLASSICAL_CONTACT_ESTIMATOR = "Classical ";
    static final String ROBUST_CONTACT_ESTIMATOR = "Robust ";
    static final String REGRESSION_STRATEGY = "RegressionStrategy";
    static final String FITTED_BRANCH = "FittedBranch";
    static final String ADHESIVE_ENERGY_ESTIMATION_METHOD = "AdhesiveEnergyEstimationMethod";

    static final String PLOT_RECORDED_CURVE = "PlotRecordedCurve";
    static final String PLOT_RECORDED_CURVE_FIT = "PlotRecordedCurveFit";
    static final String PLOT_INDENTATION = "PlotIndentation";
    static final String PLOT_INDENTATION_FIT = "PlotIndentationFit";
    static final String PLOT_MODULUS = "PlotModulus";
    static final String PLOT_MODULUS_FIT = "PlotModulusFit";

    static final String SHOW_AVERAGED_RECORDED_CURVES = "ShowAveragedRecordedCurves";
    static final String SHOW_AVERAGED_INDENTATION_CURVES = "ShowAveragedIndentationCurves";
    static final String SHOW_AVERAGED_POINTWISE_MODULUS_CURVES = "ShowAveragedPointwiseModulusCurves";
    
    static final String CURVE_AVERAGING_ENABLED = "CurveAveragingEnabled";

    static final String AVERAGED_RECORDED_CURVES_POINT_COUNT = "AveragedRecordedCurvesPointCount";
    static final String AVERAGED_INDENTATION_CURVES_POINT_COUNT = "AveragedIndentationCurvesPointCount";
    static final String AVERAGED_POINTWISE_MODULUS_CURVES_POINT_COUNT = "AveragedPointwiseModulusCurvesPointCount";

    static final String AVERAGED_CURVES_ERROR_BAR_TYPE = "AveragedCurvesErrorBarType";

    static final String INCLUDE_IN_MAPS = "IncludeInMaps";
    static final String INCLUDE_IN_MAPS_ENABLED = "IncludeInMapsEnabled";

    static final String CALCULATE_ADHESION_FORCE = "CalculateAdhesionForce";
    static final String CALCULATE_R_SQUARED = "CalculateRSquared";

    static final String PLOT_MAP_AREA_IMAGES = "PlotMapAreaImages";
    static final String PLOT_MAP_AREA_IMAGES_ENABLED = "PlotMapAreaImagesEnabled";

    //jumps
    public static final String FIND_JUMPS = "FindJumps";
    public static final String BRANCH_WITH_JUMPS = "BranchWithJumps";
    public static final String JUMPS_SPAN = "JumpsSpan";
    public static final String JUMPS_SPAN_TYPE = "JumpsSpanType";
    public static final String POLYNOMIAL_DEGREE = "JumpsPolynomialDegree";
    public static final String JUMPS_WEIGHT_FUNCTION = "JumpsWeightFunction";
    public static final String JUMPS_MIN_DISTANCE_FROM_CONTACT = "JumpsMinDistanceFromContact";


    private static volatile AtomicInteger PROCESSING_BATCH_COUNT = new AtomicInteger(0);

    private static final String INDENTER = "Indenter";

    private static final Preferences PREF = Preferences.userNodeForPackage(ProcessingBatchModel.class).node(ProcessingBatchModel.class.getName());

    private List<SimpleSpectroscopySource> sources;
    private Set<ForceCurveBranch> availableBranches = Collections.emptySet();

    private Set<PhotodiodeSignalType> signalTypes = EnumSet.noneOf(PhotodiodeSignalType.class);

    private double poissonRatio;
    private double springConstant;
    private boolean springConstantUseReadIn;
    private double tipRadius;
    private double tipHalfAngle;
    private double tipTransitionRadius;

    private double tipExponent;
    private double tipFactor;

    private int baselineDegree;
    private int postcontactDegree;
    private Map<PhotodiodeSignalType, Double> sensitivity;	
    private Map<PhotodiodeSignalType, Boolean> useReadInSensitivity;
    private BasicIndentationModel indentationModel;	
    private double lowerCropping;
    private double upperCropping;
    private double rightCropping;
    private double leftCropping;	
    private boolean domainCropped;
    private boolean rangeCropped;	
    private double indentationLimit;
    private double loadLimit;

    private double fitIndentationLimit;
    private double fitZMinimum;
    private double fitZMaximum;

    private ThicknessCorrectionMethod thicknessCorrectionMethod;
    private boolean correctSubstrateEffect;
    private boolean sampleAdherent;
    private double sampleThickness;
    private boolean useSampleTopography;
    private File sampleTopographyFile;
    private Channel2D sampleTopographyChannel;
    private List<ROI> sampleROIs;
    private Set<ThicknessCorrectionMethod> applicableThicknessCorrectionMethods;

    private SmootherType smootherType;
    private boolean smoothed;
    private double loessSpan; 
    private Number loessIterations;
    private Number savitzkyDegree;
    private double savitzkySpan;
    private boolean plotRecordedCurve;
    private boolean plotRecordedCurveFit;
    private boolean plotIndentation;
    private boolean plotIndentationFit;
    private boolean plotModulus;	
    private boolean plotModulusFit;
    private boolean includeCurvesInMaps;
    private boolean plotMapAreaImages;
    private boolean mapAreaImagesAvailable;
    
    private boolean curveAveragingEnabled;

    private boolean showAveragedRecordedCurves;
    private boolean showAveragedIndentationCurves;
    private boolean showAveragedPointwiseModulusCurves;

    private int averagedRecordedCurvesPointCount;
    private int averagedIndentationCurvesPointCount;
    private int averagedPointwiseModulusCurvesPointCount;
    
    private ErrorBarType averagedCurvesBarType;

    private boolean calculateAdhesionForce;
    private boolean calculateRSquared;

    private boolean contactPointAutomatic;

    private Map<PhotodiodeSignalType, Boolean> sensitivityInputCanBeUsed;
    private Map<PhotodiodeSignalType, Boolean> sensitivityReadInCanBeUsed;
    private Map<PhotodiodeSignalType, Boolean> sensitivityUseReadInEnabled;
    private Map<PhotodiodeSignalType, Boolean> sensitivityInputEnabled;

    private Map<PhotodiodeSignalType, Boolean> sensitivityInputNecessary;
    private Map<PhotodiodeSignalType, Boolean> sensitivityReadInNecessary;

    private Map<PhotodiodeSignalType, Double> readInSensitivity;
    private double readInSpringConstant;

    private boolean springConstantInputNecessary;
    private boolean springConstantReadInCanBeUsed;
    private boolean springConstantUseReadInEnabled;
    private boolean springConstantInputEnabled;

    private File parentDirectory;

    private boolean containsForceVolumeData;

    private boolean tipTransitionRadiusCalculable;
    private boolean substrateEffectCorrectionKnown;
    private boolean adhesiveEnergyRequired;
    private boolean basicSettingsSpecified;
    private boolean settingsSpecified;
    private boolean croppingOnCurveSelectionPossible;
    private boolean nonEmpty;

    private BasicRegressionStrategy regressionStrategy;
    private ContactEstimator manualContactEstimator;
    private AutomaticContactEstimatorType automaticContactEstimator;
    private ContactEstimationMethod contactEstimationMethod;
    private ForceCurveBranch fittedBranch;

    private ForceEventEstimator adhesionForceEstimator;

    private AdhesiveEnergyEstimationMethod adhesiveEnergyEstimator;

    private final SpectroscopyResultDestination destination;

    //jumps
    
    private boolean findJumps = false;
    private ForceCurveBranch branchWithJumps = ForceCurveBranch.WITHDRAW;
    private int polynomialDegree = 1;
    private double jumpsSpan = 20;
    private SpanType jumpsSpanType = SpanType.POINT_COUNT;
    private LocalRegressionWeightFunction jumpsWeightFunction = LocalRegressionWeightFunction.EPANCHENIKOV;
    private double jumpMinDistanceFromContact;

    //jumps

    private String batchName;
    private final int batchNumber;

    private final int processingBatchUniqueId;

    public ProcessingBatchModel(SpectroscopyResultDestination destination, int batchNumber)
    {
        this(destination, Collections.emptyList(), Integer.toString(batchNumber), batchNumber);
    }

    public ProcessingBatchModel(SpectroscopyResultDestination destination, String name, int batchNumber)
    {
        this(destination, Collections.emptyList(), name, batchNumber);
    }

    public ProcessingBatchModel(SpectroscopyResultDestination destination, List<SimpleSpectroscopySource> sources, String name, int batchNumber)
    {
        this.destination = destination;
        this.sources = new ArrayList<>(sources);
        this.batchName = name;
        this.batchNumber = batchNumber;
        this.processingBatchUniqueId = PROCESSING_BATCH_COUNT.incrementAndGet();

        initDefaults();

        checkIfContainsForceVolumeData();
        checkIfContainsMapAreaImages();
        checkIfNonEmpty();
        checkIfAveragingEnabled();
        checkAvailableBranches();
        initializeSensitivitySpecificationSettings();
        initializeSpringConstantSpecificationSettings();
        checkIfTransitionRadiusCalculable();
        checkIfSubstrateEffectCorrectionKnown();
        checkIfAdhesiveEnergyRequired();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified();
        checkIfCroppingOnCurveSelectionPossible();
    }

    public ProcessingBatchModel(ProcessingBatchMemento memento, VisualizationSettings visSettings, List<SimpleSpectroscopySource> sources)
    {
        this.batchName = memento.getBatchName();
        this.batchNumber = memento.getBatchNumber();
        this.processingBatchUniqueId = PROCESSING_BATCH_COUNT.incrementAndGet();

        this.destination = memento.getResultDestination();      

        this.poissonRatio = memento.getPoissonRatio();
        this.springConstant = memento.getSpringConstant();
        this.springConstantUseReadIn = memento.getUseReadInSpringConstant();
        this.tipRadius = memento.getTipRadius();
        this.tipHalfAngle = memento.getTipHalfAngle();
        this.tipTransitionRadius = memento.getTipTransitionRadius();

        this.tipExponent = memento.getTipExponent();
        this.tipFactor = memento.getTipFactor();

        this.baselineDegree = memento.getBaselineDegree();
        this.postcontactDegree = memento.getPostcontactDegree();
        this.sensitivity = memento.getSensitivity(); 
        this.useReadInSensitivity = memento.getUseReadInSensitivity();
        this.indentationModel = memento.getIndentationModel(); 
        this.lowerCropping = memento.getLowerCropping();
        this.upperCropping = memento.getUpperCropping();
        this.rightCropping = memento.getRightCropping();
        this.leftCropping = memento.getLeftCropping();    
        this.domainCropped = memento.isDomainCropped();
        this.rangeCropped = memento.isRangeCropped();   
        this.indentationLimit = memento.getIndentationLimit();
        this.loadLimit = memento.getLoadLimit();

        this.fitIndentationLimit = memento.getFitIndentationLimit();
        this.fitZMinimum = memento.getFitZMinimum();
        this.fitZMaximum = memento.getFitZMaximum();

        this.correctSubstrateEffect = memento.getCorrectSubstrateEffect();
        this.thicknessCorrectionMethod = memento.getThicknessCorrectionMethod();
        this.sampleAdherent = memento.isSampleAdherent();
        this.sampleThickness = memento.getSampleThickness();
        this.useSampleTopography = memento.getUseSampleTopography();
        this.sampleTopographyFile = memento.getSampleTopographyFile();
        this.sampleTopographyChannel = memento.getSampleTopographyChannel();
        this.sampleROIs = memento.getSampleROIs();

        this.smootherType = memento.getSmootherName();
        this.smoothed = memento.areDataSmoothed();
        this.loessSpan = memento.getLoessSpan(); 
        this.loessIterations = memento.getLoessIterations();
        this.savitzkyDegree = memento.getSavitzkyDegree();
        this.savitzkySpan = memento.getSavitzkySpan();
        this.plotRecordedCurve = visSettings.isPlotRecordedCurve();
        this.plotRecordedCurveFit = visSettings.isPlotRecordedCurveFit();
        this.plotIndentation = visSettings.isPlotIndentation();
        this.plotIndentationFit = visSettings.isPlotIndentationFit();
        this.plotModulus = visSettings.isPlotModulus();    
        this.plotModulusFit = visSettings.isPlotModulusFit();

        this.showAveragedRecordedCurves = memento.isShowAveragedRecordedCurves();
        this.showAveragedIndentationCurves = memento.isShowAveragedIndentationCurves();
        this.showAveragedPointwiseModulusCurves = memento.isShowAveragedPointwiseModulusCurves();

        this.averagedRecordedCurvesPointCount = memento.getAveragedRecordedCurvesPointCount();
        this.averagedIndentationCurvesPointCount = memento.getAveragedIndentationCurvesPointCount();
        this.averagedPointwiseModulusCurvesPointCount = memento.getAveragedPointwiseModulusCurvesPointCount();
        
        this.averagedCurvesBarType = memento.getAveragedCurvesBarType();

        this.includeCurvesInMaps = memento.isIncludeCurvesInMaps();
        this.plotMapAreaImages = memento.isPlotMapAreaImages();
        this.mapAreaImagesAvailable = memento.isMapAreaImagesAvailable();

        this.calculateRSquared = memento.isCalculateRSquared();
        this.calculateAdhesionForce = memento.isCalculateAdhesionForce();

        this.contactPointAutomatic = memento.isContactPointAutomatic();

        this.tipTransitionRadiusCalculable = memento.isTipSmoothTransitionRadiusCalculable();
        this.substrateEffectCorrectionKnown = memento.isSubstrateEffectCorrectionKnown();
        this.adhesiveEnergyRequired = memento.isAdhesiveEnergyRequired();
        this.croppingOnCurveSelectionPossible = memento.isCroppingOnCurveSelectionPossible();

        this.regressionStrategy = memento.getRegressionStrategy();
        this.automaticContactEstimator = memento.getAutomaticEstimator();
        this.contactEstimationMethod = memento.getContactEstimationMethod();
        this.fittedBranch = memento.getFittedBranch();

        this.adhesionForceEstimator = new UnspecificAdhesionForceEstimator(1);

        this.sources = sources;

        this.parentDirectory = findParentDirectory();

        this.springConstantReadInCanBeUsed = false;
        this.springConstantInputNecessary = false;
        this.springConstantInputEnabled = true;
        this.springConstantUseReadInEnabled = false;
        this.springConstantUseReadIn = false;

        this.sensitivityInputCanBeUsed = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityReadInCanBeUsed = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityInputNecessary = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityReadInNecessary = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityUseReadInEnabled = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityInputEnabled = getPhotodiodeSignalMap(Boolean.FALSE);

        this.readInSensitivity = getPhotodiodeSignalMap(Double.NaN);
        this.readInSpringConstant = Double.NaN;

        this.applicableThicknessCorrectionMethods = calculateWhichSubstrateEffectCorrectionsCanBeApplied();

        checkIfContainsForceVolumeData();
        checkIfContainsMapAreaImages();
        checkIfNonEmpty();
        checkIfAveragingEnabled();
        checkAvailableBranches();
        initializeSensitivitySpecificationSettings();
        initializeSpringConstantSpecificationSettings();
        checkIfTransitionRadiusCalculable();
        checkIfSubstrateEffectCorrectionKnown();
        checkIfAdhesiveEnergyRequired();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified();
        checkIfCroppingOnCurveSelectionPossible();
    }

    //jumps

    public boolean getFindJumps()
    {
        return false;//findJumps;
    }

    public void setFindJumps(boolean findJumpsNew)
    {
        if(this.findJumps != findJumpsNew)
        {
            boolean findJumpsOld = this.findJumps;
            this.findJumps = findJumpsNew;

            firePropertyChange(FIND_JUMPS, findJumpsOld, findJumpsNew);
            //            pref.putBoolean(FIND_JUMPS, findJumps);
            //
            //            try
            //            {
            //                pref.flush();
            //            } catch (BackingStoreException e) 
            //            {
            //                e.printStackTrace();
            //            }
        }
    }

    public ForceCurveBranch getBranchWithJumps()
    {
        return branchWithJumps;
    }

    public void setBranchWithJumps(ForceCurveBranch branchWithJumpsNew)
    {
        if(!Objects.equals(this.branchWithJumps, branchWithJumpsNew))
        {
            ForceCurveBranch branchWithJumpsOld = this.branchWithJumps;
            this.branchWithJumps = branchWithJumpsNew;

            firePropertyChange(BRANCH_WITH_JUMPS, branchWithJumpsOld, branchWithJumpsNew);
        }
    }

    public double getJumpMinDistanceFromContact()
    {
        return jumpMinDistanceFromContact;
    }

    public void setJumpMinDistanceFromContact(double jumpMinDistanceFromContactNew)
    {
        double jumpMinDistanceFromContactOld = this.jumpMinDistanceFromContact;
        this.jumpMinDistanceFromContact = jumpMinDistanceFromContactNew;

        firePropertyChange(JUMPS_MIN_DISTANCE_FROM_CONTACT, jumpMinDistanceFromContactOld, jumpMinDistanceFromContact);

        PREF.putDouble(JUMPS_MIN_DISTANCE_FROM_CONTACT, jumpMinDistanceFromContact);
        flushPreferences();
    }

    public LocalRegressionWeightFunction getJumpsWeightFunction()
    {
        return jumpsWeightFunction;
    }

    public void setJumpsWeightFunction(LocalRegressionWeightFunction jumpsWeightFunctionNew)
    {
        if(!Objects.equals(this.jumpsWeightFunction, jumpsWeightFunctionNew))
        {
            LocalRegressionWeightFunction weightFunctionOld = this.jumpsWeightFunction;
            this.jumpsWeightFunction = jumpsWeightFunctionNew;

            firePropertyChange(JUMPS_WEIGHT_FUNCTION, weightFunctionOld, jumpsWeightFunctionNew);
        }
    }

    public int getPolynomialDegree()
    {
        return polynomialDegree;
    }

    public void setPolynomialDegree(int polynomialDegreeNew)
    {
        Validation.requireNonNegativeParameterName(polynomialDegreeNew, "polynomialDegreeNew");

        List<PropertyChangeEvent> changeEvents = new ArrayList<>();
        if(!jumpsSpanType.isPolynomialDegreeAceptable(this.jumpsSpan, polynomialDegreeNew))
        {
            changeEvents.add(new PropertyChangeEvent(this, POLYNOMIAL_DEGREE, polynomialDegreeNew, this.polynomialDegree));
        }
        else
        {
            changeEvents.addAll(setPolynomialDegreePrivate(polynomialDegreeNew));
        }

        firePropertyChange(changeEvents);
    }

    private List<PropertyChangeEvent> setPolynomialDegreePrivate(int polynomialDegreeNew)
    {
        List<PropertyChangeEvent> changeEvents = new ArrayList<>();

        if(this.polynomialDegree != polynomialDegreeNew)
        {          
            int polynomialDegreeOld = this.polynomialDegree;
            this.polynomialDegree = polynomialDegreeNew;

            changeEvents.add(new PropertyChangeEvent(this, POLYNOMIAL_DEGREE, polynomialDegreeOld, polynomialDegreeNew));
        }

        return changeEvents;
    }

    public double getJumpsSpan()
    {
        return jumpsSpan;
    }

    private List<PropertyChangeEvent> setSpanPrivate(double spanNew)
    {       
        List<PropertyChangeEvent> propertyChangeEvents = new ArrayList<>();

        if(this.jumpsSpan != spanNew)
        {
            double spanOld = this.jumpsSpan;
            this.jumpsSpan = spanNew;

            propertyChangeEvents.add(new PropertyChangeEvent(this, JUMPS_SPAN, spanOld, spanNew));
        }

        return propertyChangeEvents;
    }

    public void setJumpsSpan(double spanNew)
    {       
        Validation.requireValueGreaterThanParameterName(0, spanNew, "spanNew");

        List<PropertyChangeEvent> events = new ArrayList<>();        

        if(!jumpsSpanType.isSpanValueAcceptable(spanNew))
        {
            events.add(new PropertyChangeEvent(this, JUMPS_SPAN, spanNew, this.jumpsSpan));
        }
        else
        {
            events.addAll(setSpanPrivate(spanNew));       
            events.addAll(ensureConsistencyWithSpan()); 
        }

        firePropertyChange(events);
    }

    public SpanType getJumpsSpanType()
    {
        return jumpsSpanType;
    }

    private List<PropertyChangeEvent> setSpanTypePrivate(SpanType spanTypeNew)
    {
        List<PropertyChangeEvent> events = new ArrayList<>();        

        if(!this.jumpsSpanType.equals(spanTypeNew))
        {
            SpanType spanTypeOld = this.jumpsSpanType;
            this.jumpsSpanType = spanTypeNew;

            events.add(new PropertyChangeEvent(this, JUMPS_SPAN_TYPE, spanTypeOld, spanTypeNew));
        }

        return events;
    }

    public void setJumpsSpanType(SpanType spanTypeNew)
    {
        Validation.requireNonNullParameterName(spanTypeNew, "spanTypeNew");

        List<PropertyChangeEvent> events = new ArrayList<>(); 

        events.addAll(setSpanTypePrivate(spanTypeNew));
        events.addAll(ensureConsistencyWithSpanType());

        firePropertyChange(events);
    }


    private List<PropertyChangeEvent> ensureConsistencyWithSpanType()
    {
        double spanNew = this.jumpsSpanType.correctSpanValue(this.jumpsSpan);
        List<PropertyChangeEvent> events = new ArrayList<>();

        events.addAll(setSpanPrivate(spanNew));
        events.addAll(ensureConsistencyWithSpan());

        return events;
    }

    private List<PropertyChangeEvent> ensureConsistencyWithSpan()
    {
        int polynomialDegreeNew = this.jumpsSpanType.correctPolynomialDegree(this.jumpsSpan, this.polynomialDegree);
        List<PropertyChangeEvent> events = setPolynomialDegreePrivate(polynomialDegreeNew);

        return events;
    }

    //jumps

    private static <E> Map<PhotodiodeSignalType, E> getPhotodiodeSignalMap(E val)
    {
        Map<PhotodiodeSignalType, E> map = new EnumMap<>(PhotodiodeSignalType.class);

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            map.put(signalType, val);
        }

        return map;
    }

    private void initDefaults()
    {  
        String preferredIndentationModel = PREF.get(INDENTER, BasicIndentationModel.CONE.getIdentifier());
        this.indentationModel = BasicIndentationModel.getValue(preferredIndentationModel, BasicIndentationModel.CONE);
        this.poissonRatio = Double.NaN;
        this.springConstant = Double.NaN;
        this.springConstantUseReadIn = false;
        this.useReadInSensitivity = getPhotodiodeSignalMap(Boolean.FALSE);
        this.tipRadius = Double.NaN;
        this.tipHalfAngle = Double.NaN;
        this.tipTransitionRadius = Double.NaN;
        this.tipExponent = Double.NaN;
        this.tipFactor = Double.NaN;

        this.baselineDegree = PREF.getInt(BASELINE_DEGREE, 2);
        this.postcontactDegree = PREF.getInt(POSTCONTACT_DEGREE, 1);
        this.sensitivity = getPhotodiodeSignalMap(Double.NaN);	
        this.lowerCropping = Double.NaN;
        this.upperCropping = Double.NaN;
        this.rightCropping = Double.NaN;
        this.leftCropping = Double.NaN;	
        this.domainCropped = false;
        this.rangeCropped = false;
        this.indentationLimit = Double.POSITIVE_INFINITY;
        this.loadLimit = Double.POSITIVE_INFINITY;
        this.fitIndentationLimit = Double.POSITIVE_INFINITY;
        this.fitZMinimum = Double.NEGATIVE_INFINITY;
        this.fitZMaximum = Double.POSITIVE_INFINITY;

        this.useSampleTopography = false;
        String preferredThicknessCorrectionMethodId = PREF.get(THICKNESS_CORRECTION_METHOD, ThicknessCorrectionMethod.LEBEDEV_CHEBYSHEV.getIdentifier());
        this.thicknessCorrectionMethod = ThicknessCorrectionMethod.getValue(preferredThicknessCorrectionMethodId,ThicknessCorrectionMethod.LEBEDEV_CHEBYSHEV);
        this.correctSubstrateEffect = false;
        this.sampleAdherent = true;
        this.sampleThickness = Double.NaN;       

        this.smoothed = false;
        this.smootherType = SmootherType.LOCAL_REGRESSION;
        this.loessSpan = Double.NaN; 
        this.loessIterations = Double.NaN;
        this.savitzkySpan = Double.NaN;
        this.savitzkyDegree = Double.NaN;

        this.plotRecordedCurve = PREF.getBoolean(PLOT_RECORDED_CURVE, true);
        this.plotRecordedCurveFit = PREF.getBoolean(PLOT_RECORDED_CURVE_FIT, true);
        this.plotIndentation = PREF.getBoolean(PLOT_INDENTATION, true);
        this.plotIndentationFit = PREF.getBoolean(PLOT_INDENTATION_FIT, true);
        this.plotModulus = PREF.getBoolean(PLOT_MODULUS, true);	
        this.plotModulusFit = PREF.getBoolean(PLOT_MODULUS_FIT, true);  

        this.showAveragedRecordedCurves = PREF.getBoolean(SHOW_AVERAGED_RECORDED_CURVES, true);
        this.showAveragedIndentationCurves = PREF.getBoolean(SHOW_AVERAGED_INDENTATION_CURVES, true);
        this.showAveragedPointwiseModulusCurves = PREF.getBoolean(SHOW_AVERAGED_POINTWISE_MODULUS_CURVES, true);

        this.averagedRecordedCurvesPointCount = PREF.getInt(AVERAGED_RECORDED_CURVES_POINT_COUNT, 100);
        this.averagedIndentationCurvesPointCount = PREF.getInt(AVERAGED_INDENTATION_CURVES_POINT_COUNT, 100);
        this.averagedPointwiseModulusCurvesPointCount = PREF.getInt(AVERAGED_POINTWISE_MODULUS_CURVES_POINT_COUNT, 100);
     
        String averagedCurvesBarTypeId = PREF.get(AVERAGED_CURVES_ERROR_BAR_TYPE, ErrorBarType.STANDARD_DEVIATION.getIdentifier());
        this.averagedCurvesBarType = ErrorBarType.getValue(averagedCurvesBarTypeId, ErrorBarType.STANDARD_DEVIATION);

        this.contactPointAutomatic = true;
        this.includeCurvesInMaps = true;
        this.plotMapAreaImages = true;
        this.mapAreaImagesAvailable = false;
        this.calculateAdhesionForce = PREF.getBoolean(CALCULATE_ADHESION_FORCE, true);
        this.calculateRSquared = PREF.getBoolean(CALCULATE_R_SQUARED, true);

        this.parentDirectory = findParentDirectory();

        this.springConstantReadInCanBeUsed = false;
        this.springConstantInputNecessary = false;
        this.springConstantInputEnabled = true;
        this.springConstantUseReadInEnabled = false;
        this.springConstantUseReadIn = false;

        this.sensitivityInputCanBeUsed = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityReadInCanBeUsed = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityInputNecessary = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityReadInNecessary = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityUseReadInEnabled = getPhotodiodeSignalMap(Boolean.FALSE);
        this.sensitivityInputEnabled = getPhotodiodeSignalMap(Boolean.FALSE);

        this.readInSensitivity = getPhotodiodeSignalMap(Double.NaN);
        this.readInSpringConstant = Double.NaN;

        String preferredRegressionStrategy = PREF.get(REGRESSION_STRATEGY, BasicRegressionStrategy.CLASSICAL_L2.getIdentifier());
        this.regressionStrategy = BasicRegressionStrategy.getValue(preferredRegressionStrategy, BasicRegressionStrategy.CLASSICAL_L2);

        String preferredAutomaticContactEstimator = PREF.get(AUTOMATIC_CONTACT_ESTIMATOR, AutomaticContactEstimatorType.CLASSICAL_GOLDEN.getIdentifier());
        this.automaticContactEstimator = AutomaticContactEstimatorType.getValue(preferredAutomaticContactEstimator, AutomaticContactEstimatorType.CLASSICAL_GOLDEN);

        String preferredContactEstimationMethod = PREF.get(CONTACT_ESTIMATION_METHOD, ContactEstimationMethod.CONTACT_MODEL_BASED.getIdentifier());
        this.contactEstimationMethod = ContactEstimationMethod.getValue(preferredContactEstimationMethod, ContactEstimationMethod.CONTACT_MODEL_BASED);

        String preferredFittedBranch = PREF.get(FITTED_BRANCH, ForceCurveBranch.APPROACH.getIdentifier());
        this.fittedBranch = ForceCurveBranch.getValue(preferredFittedBranch, ForceCurveBranch.APPROACH);

        this.adhesionForceEstimator = new UnspecificAdhesionForceEstimator(1);

        String preferredAdhesiveEnergyEstimator = PREF.get(ADHESIVE_ENERGY_ESTIMATION_METHOD, AdhesiveEnergyEstimationMethod.FROM_FIT.getIdentifier());
        this.adhesiveEnergyEstimator = AdhesiveEnergyEstimationMethod.getValue(preferredAdhesiveEnergyEstimator, AdhesiveEnergyEstimationMethod.FROM_FIT);
        this.sampleROIs = new ArrayList<>();
        this.applicableThicknessCorrectionMethods = calculateWhichSubstrateEffectCorrectionsCanBeApplied();

        //jumps

        this.findJumps = false;//pref.getBoolean(FIND_JUMPS, false);
        this.jumpMinDistanceFromContact = 5.;
    }

    public int getProcessingBatchUniqueId()
    {
        return processingBatchUniqueId;
    }

    public SpectroscopyResultDestination getResultDestination()
    {
        return destination;
    }

    public List<SimpleSpectroscopySource> getSources()
    {
        return sources;
    }

    public void addSources(List<SimpleSpectroscopySource> sourcesToAdd)
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>(this.sources);
        sources.addAll(sourcesToAdd);

        setSources(sources);
    }

    public void removeSources(List<SimpleSpectroscopySource> sourcesToRemove)
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>(this.sources);
        sources.removeAll(sourcesToRemove);

        setSources(sources);
    }

    private void buildDelayedMaps()
    {        
        MultiMap<MapDelayedCreator, SimpleSpectroscopySource> mapDelayedCreators = new MultiMap<>();

        for(SimpleSpectroscopySource source : sources)
        {
            MapDelayedCreator mapDelayedCreator = source.getMapDelayedCreator();
            if(mapDelayedCreator != null)
            {
                mapDelayedCreators.put(mapDelayedCreator, source);
            }
        }

        for(Entry<MapDelayedCreator, List<SimpleSpectroscopySource>> entry : mapDelayedCreators.entrySet())
        {
            MapDelayedCreator mapDelayedCreator = entry.getKey();
            mapDelayedCreator.buildMapSource(entry.getValue());
        }
    }

    public void setSources(List<SimpleSpectroscopySource> sourcesNew)
    {
        List<SimpleSpectroscopySource> sourcesOld = sources;
        this.sources = sourcesNew;

        firePropertyChange(ProcessingBatchModelInterface.SOURCES, sourcesOld, sourcesNew);      

        checkIfParentDirectoryChanged();
        checkIfContainsForceVolumeData();
        checkIfContainsMapAreaImages();
        checkIfNonEmpty();
        checkIfAveragingEnabled();
        checkAvailableBranches();
        initializeSensitivitySpecificationSettings();
        initializeSpringConstantSpecificationSettings();

        buildDelayedMaps();
    }

    public Set<ForceCurveBranch> getAvailableBranches()
    {
        return new LinkedHashSet<>(availableBranches);
    }

    private void checkAvailableBranches()
    {
        Set<ForceCurveBranch> availableBranchesNew = new LinkedHashSet<>();

        for(ForceCurveBranch branch : ForceCurveBranch.values())
        {
            for(SimpleSpectroscopySource currentSource : sources)
            {                
                boolean contains = currentSource.containsData(branch);
                if(contains)
                {
                    availableBranchesNew.add(branch);
                    break;
                }
            }
        }

        Set<ForceCurveBranch> availableBranchesOld = this.availableBranches;
        this.availableBranches = availableBranchesNew;

        firePropertyChange(AVAILABLE_FORCE_CURVE_BRANCHES, availableBranchesOld, this.availableBranches);

        if(!this.availableBranches.isEmpty() && !this.availableBranches.contains(fittedBranch))
        {
            setFittedBranch(availableBranches.iterator().next());
        }
    }

    public boolean containsForceMapData()
    {
        return containsForceVolumeData;
    }

    public boolean isSourceFilteringPossible()
    {
        return containsForceVolumeData;
    }

    public File getCommonSourceDirectory()
    {
        return parentDirectory;
    }

    private File findParentDirectory()
    {
        return BatchUtilities.findLastCommonSourceDirectory(sources);
    }

    public int getBatchNumber()
    {
        return batchNumber;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public void setBatchName(String batchNameNew)
    {
        if(!Objects.equals(this.batchName, batchNameNew))
        {
            String batchNameOld = this.batchName;
            this.batchName = batchNameNew;

            firePropertyChange(ProcessingBatchModelInterface.BATCH_NAME, batchNameOld, batchNameNew);

            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
    }

    public boolean isSpringConstantReadInEnabled()
    {
        return springConstantReadInCanBeUsed;
    }

    public BasicIndentationModel getIndentationModel()
    {
        return indentationModel;
    }

    public void setIndentationModel(BasicIndentationModel indentationModelNew)
    {
        Validation.requireNonNullParameterName(indentationModelNew, "indentationModelNew");

        if(!Objects.equals(this.indentationModel, indentationModelNew))
        {
            BasicIndentationModel indentationModelOld = this.indentationModel;
            this.indentationModel = indentationModelNew;

            firePropertyChange(INDENTATION_MODEL, indentationModelOld, indentationModelNew);

            checkIfTransitionRadiusCalculable();
            checkIfSubstrateEffectCorrectionKnown();
            checkIfApplicableSubstrateEffectCorrectionsChanged();
            checkIfAdhesiveEnergyRequired();
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified();

            PREF.put(INDENTER, indentationModelNew.getIdentifier());
            flushPreferences();
        }
    }

    public double getPoissonRatio()
    {
        return poissonRatio;
    }

    public void setPoissonRatio(double poissonRatioNew)
    {
        if(Double.compare(this.poissonRatio, poissonRatioNew) != 0)
        {
            double poissonRatioOld = this.poissonRatio;
            this.poissonRatio = poissonRatioNew;

            firePropertyChange(POISSON_RATIO, poissonRatioOld, poissonRatioNew);

            checkIfSubstrateEffectCorrectionKnown();
            checkIfApplicableSubstrateEffectCorrectionsChanged();
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
    }

    public boolean isCorrectSubstrateEffect()
    {
        return correctSubstrateEffect;
    }

    public void setCorrectSubstrateEffect(boolean correctSubstrateEffectNew)
    {
        if(this.correctSubstrateEffect != correctSubstrateEffectNew)
        {
            boolean correctSubstrateOld = this.correctSubstrateEffect;
            this.correctSubstrateEffect = correctSubstrateEffectNew;

            firePropertyChange(CORRECT_SUBSTRATE_EFFECT, correctSubstrateOld, correctSubstrateEffectNew);

            checkIfSettingsSpecified();  
        }     
    }

    public ThicknessCorrectionMethod getThicknessCorrectionMethod()
    {
        return thicknessCorrectionMethod;
    }

    public void setThicknessCorrectionMethod(ThicknessCorrectionMethod thicknessCorrectionMethodNew)
    {
        Validation.requireNonNullParameterName(thicknessCorrectionMethodNew, "thicknessCorrectionMethodNew");

        if(!Objects.equals(this.thicknessCorrectionMethod, thicknessCorrectionMethodNew))
        {
            ThicknessCorrectionMethod thicknessCorrectionMethodOld = this.thicknessCorrectionMethod;
            this.thicknessCorrectionMethod = thicknessCorrectionMethodNew;

            firePropertyChange(THICKNESS_CORRECTION_METHOD, thicknessCorrectionMethodOld, thicknessCorrectionMethodNew);

            PREF.put(THICKNESS_CORRECTION_METHOD, thicknessCorrectionMethodNew.getIdentifier());
            flushPreferences();
        }
    }

    public Set<ThicknessCorrectionMethod> getApplicableThicknessCorrectionMethods()
    {
        return Collections.unmodifiableSet(this.applicableThicknessCorrectionMethods);
    }

    public boolean isSampleAdherent()
    {
        return sampleAdherent;
    }

    public void setSampleAdherent(boolean sampleAdherentNew)
    {
        if(this.sampleAdherent != sampleAdherentNew)
        {
            boolean sampleAdherentOld = this.sampleAdherent;
            this.sampleAdherent = sampleAdherentNew;

            firePropertyChange(SAMPLE_ADHERENT, sampleAdherentOld, sampleAdherentNew);

            checkIfSubstrateEffectCorrectionKnown();
            checkIfApplicableSubstrateEffectCorrectionsChanged();
        }
    }

    public double getSampleThickness()
    {
        return sampleThickness;
    }

    public void setSampleThickness(double sampleThicknessNew)
    {
        if(Double.compare(this.sampleThickness, sampleThicknessNew) != 0)
        {
            double sampleThicknessOld = this.sampleThickness;
            this.sampleThickness = sampleThicknessNew;

            firePropertyChange(SAMPLE_THICKNESS, sampleThicknessOld, sampleThicknessNew);

            checkIfSettingsSpecified();
        }
    }

    public boolean getUseSampleTopography()
    {
        return useSampleTopography;
    }

    public void setUseSampleTopography(boolean useSampleTopographyNew)
    {
        if(this.useSampleTopography != useSampleTopographyNew)
        {
            boolean useSampleTopographyOld = this.useSampleTopography;
            this.useSampleTopography = useSampleTopographyNew;

            firePropertyChange(USE_SAMPLE_TOPOGRAPHY, useSampleTopographyOld, useSampleTopographyNew);

            checkIfSettingsSpecified();
        }
    }

    public File getSampleTopographyFile()
    {
        return sampleTopographyFile;
    }

    public void setSampleTopographyFile(File topographyFileNew)
    {
        if(!Objects.equals(this.sampleTopographyFile, topographyFileNew))
        {
            File topographyFileOld = this.sampleTopographyFile;
            this.sampleTopographyFile = topographyFileNew;

            firePropertyChange(SAMPLE_TOPOGRAPHY_FILE, topographyFileOld, topographyFileNew);

            checkIfSettingsSpecified();
        }
    }

    public Channel2D getSampleTopographyChannel()
    {
        return sampleTopographyChannel;
    }

    public void setSampleTopographyChannel(Channel2D topographyChannelNew)
    {
        Channel2D topographyChannelOld = this.sampleTopographyChannel;
        this.sampleTopographyChannel = topographyChannelNew;

        firePropertyChange(SAMPLE_TOPOGRAPHY_CHANNEL, topographyChannelOld, topographyChannelNew);

        checkIfSettingsSpecified();
    }

    public ThinSampleModel buildThinSampleModel()
    {
        ThinSampleModel sampleModel = useSampleTopography ? new TopographyThinSampleModel(poissonRatio, getCorrectedTopography(), sampleAdherent): new BasicThinSampleModel(poissonRatio, sampleThickness, sampleAdherent);
        return sampleModel;
    }

    public List<ROI> getSampleROIs()
    {
        return new ArrayList<>(sampleROIs);
    }

    private void setSampleROIs(List<ROI> sampleROIsNew)
    {
        List<ROI> sampleROIsOld = new ArrayList<>(sampleROIs);

        this.sampleROIs.clear();
        this.sampleROIs.addAll(sampleROIsNew);

        firePropertyChange(SAMPLE_ROIS, sampleROIsOld, sampleROIsNew);
    }

    public void setSelectedROIs(Channel2DSource<?> image, Channel2D channel, List<ROI> substrateROIs) 
    {        
        setSampleTopographyFile(image.getCorrespondingFile());
        setSampleTopographyChannel(channel);
        setSampleROIs(substrateROIs);
    }

    public double getSpringConstant()
    {
        return springConstant;
    }

    public void setSpringConstant(double springConstantNew)
    {
        if(springConstantInputEnabled)
        {
            setSpringConstantPrivate(springConstantNew);
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
            checkIfCroppingOnCurveSelectionPossible();
        }
    }

    private void setSpringConstantPrivate(double springConstantNew)
    {        
        double springConstantOld = this.springConstant;
        this.springConstant = springConstantNew;

        firePropertyChange(SPRING_CONSTANT, springConstantOld, springConstantNew);
    }

    public boolean isSpringConstantInputEnabled()
    {
        return springConstantInputEnabled;
    }

    public boolean getUseReadInSpringConstant()
    {
        return springConstantUseReadIn;
    }

    public void setUseReadInSpringConstant(boolean useReadInSpringConstantNew)
    {
        if(springConstantUseReadInEnabled)
        {
            setUseReadInSpringConstantPrivate(useReadInSpringConstantNew);
        }
    }

    private void setUseReadInSpringConstantPrivate(boolean useReadInSpringConstantNew)
    {
        boolean useReadInSpringConstantOld = this.springConstantUseReadIn;
        this.springConstantUseReadIn = useReadInSpringConstantNew;    

        firePropertyChange(SPRING_CONSTANT_USE_READ_IN, useReadInSpringConstantOld, useReadInSpringConstantNew);

        checkIfSpringConstantInputEnabled();
        ensureConsistencyOfSpringConstantValues();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified(); 
        checkIfCroppingOnCurveSelectionPossible();
    }

    public int getBaselineDegree()
    {
        return baselineDegree;
    }

    public void setBaselineDegree(int baselineDegreeNew)
    {        
        Validation.requireNonNegativeParameterName(baselineDegreeNew, "baselineDegreeNew");

        if(this.baselineDegree != baselineDegreeNew)
        {
            int baselineDegreeOld = this.baselineDegree;
            this.baselineDegree = baselineDegreeNew;

            firePropertyChange(BASELINE_DEGREE, baselineDegreeOld, baselineDegreeNew);

            PREF.putInt(BASELINE_DEGREE, baselineDegreeNew);
            flushPreferences();
        }
    }

    public int getPostcontactDegree()
    {
        return postcontactDegree;
    }

    public void setPostcontactDegree(int postcontactDegreeNew)
    {        
        Validation.requireNonNegativeParameterName(postcontactDegreeNew, "postcontactDegreeNew");

        if(this.postcontactDegree != postcontactDegreeNew)
        {
            int postcontactDegreeOld = this.postcontactDegree;
            this.postcontactDegree = postcontactDegreeNew;

            firePropertyChange(POSTCONTACT_DEGREE, postcontactDegreeOld, postcontactDegreeNew);

            PREF.putInt(POSTCONTACT_DEGREE, postcontactDegreeNew);
            flushPreferences();
        }
    }

    public boolean isPosctcontactDegreeInputEnabled()
    {
        return contactEstimationMethod.isPostcontactDegreeInputRequired();
    }

    public Set<PhotodiodeSignalType> getSensitivityPhotodiodeSignalTypes()
    {
        return signalTypes;
    }

    public Double getSensitivity(PhotodiodeSignalType signalType)
    {
        return sensitivity.get(signalType);
    }

    public Map<PhotodiodeSignalType, Double> getSensitivity()
    {
        return new EnumMap<>(sensitivity); 
    }

    public void setSensitivity(PhotodiodeSignalType signalType, Double sensitivityNew)
    {
        if(sensitivityNew == null)
        {
            return;
        }

        if(sensitivityInputEnabled.get(signalType))
        {
            setSensitivityPrivate(signalType, sensitivityNew);
            checkIfBasicSettingsSpecified();   
            checkIfSettingsSpecified(); 
            checkIfCroppingOnCurveSelectionPossible();
        }
    }

    private void setSensitivityPrivate(PhotodiodeSignalType signalType, Double sensitivityNew)
    {
        Map<PhotodiodeSignalType, Double> sensitivityOld = new EnumMap<>(this.sensitivity);
        this.sensitivity.put(signalType, sensitivityNew);

        firePropertyChange(SENSITIVITY, sensitivityOld, new EnumMap<>(this.sensitivity));
    }

    public Map<PhotodiodeSignalType, Boolean> getUseReadInSensitivity()
    {
        return new EnumMap<>(useReadInSensitivity);
    }  

    public Boolean getUseReadInSensitivity(PhotodiodeSignalType signalType)
    {
        return useReadInSensitivity.get(signalType);
    }   

    public void setUseReadInSensitivity(PhotodiodeSignalType signalType, Boolean valueNew)
    {        
        if(valueNew == null)
        {
            return;
        }

        if(sensitivityUseReadInEnabled.get(signalType))
        {
            setUseReadInSensitivityPrivate(signalType, valueNew);
        }
    }   

    private void setUseReadInSensitivityPrivate(PhotodiodeSignalType signalType, boolean valueNew)
    {        
        Map<PhotodiodeSignalType, Boolean> useReadInSensitivityOld = new EnumMap<>(this.useReadInSensitivity);
        this.useReadInSensitivity.put(signalType, valueNew);

        firePropertyChange(SENSITIVITY_USE_READ_IN, useReadInSensitivityOld, new EnumMap<>(this.useReadInSensitivity));

        checkIfSensitivityInputEnabled();
        ensureConsistencyOfSensitivityValues();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified(); 
        checkIfCroppingOnCurveSelectionPossible();
    }

    public Boolean isSensitivityInputNecessary(PhotodiodeSignalType signalType)
    {
        return sensitivityInputNecessary.get(signalType);
    }

    public Boolean isSensitivityInputEnabled(PhotodiodeSignalType signalType)
    {       
        return sensitivityInputEnabled.get(signalType);
    }

    public Boolean isSensitivityReadInEnabled(PhotodiodeSignalType signalType)
    {        
        return sensitivityUseReadInEnabled.get(signalType);
    }

    public double getLoadLimit()
    {
        return loadLimit;
    }

    public void setLoadLimit(double loadLimitNew)
    {
        if(Double.compare(this.loadLimit, loadLimitNew) != 0)
        {
            double loadLimitOld = this.loadLimit;
            this.loadLimit = loadLimitNew;

            firePropertyChange(LOAD_LIMIT, loadLimitOld, loadLimitNew);

            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified();
        }
    }

    public double getIndentationLimit()
    {
        return indentationLimit;
    }

    public void setIndentationLimit(double indentationLimitNew)
    {
        if(Double.compare(this.indentationLimit, indentationLimitNew) != 0)
        {
            double indentationLimitOld = this.indentationLimit;
            this.indentationLimit = indentationLimitNew;

            firePropertyChange(INDENTATION_LIMIT, indentationLimitOld, indentationLimitNew);    

            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified();
        }
    }

    public double getFitZMinimum()
    {
        return fitZMinimum;
    }

    public void setFitZMinimum(double fitZMinimumNew)
    {
        if(Double.compare(this.fitZMinimum, fitZMinimumNew) != 0)
        {
            double fitZMinimumOld = this.fitZMinimum;
            this.fitZMinimum = fitZMinimumNew;

            firePropertyChange(FIT_Z_MINIMUM, fitZMinimumOld, fitZMinimum);
        }
    }

    public double getFitZMaximum()
    {
        return fitZMaximum;
    }

    public void setFitZMaximum(double fitZMaximumNew)
    {
        if(Double.compare(this.fitZMaximum, fitZMaximumNew) != 0)
        {
            double fitZMaximumOld = this.fitZMaximum;
            this.fitZMaximum = fitZMaximumNew;

            firePropertyChange(FIT_Z_MAXIMUM, fitZMaximumOld, fitZMaximum);
        }
    }

    public double getFitIndentationLimit()
    {
        return fitIndentationLimit;
    }

    public void setFitIndentationLimit(double fitIndentationLimitNew)
    {
        if(Double.compare(this.fitIndentationLimit, fitIndentationLimitNew) != 0)
        {
            double fitIndentationLimitOld = this.fitIndentationLimit;
            this.fitIndentationLimit = fitIndentationLimitNew;

            firePropertyChange(FIT_INDENTATION_LIMIT, fitIndentationLimitOld, this.fitIndentationLimit);
        }
    }

    public double getTipRadius()
    {
        return tipRadius;
    }

    public void setTipRadius(double tipRadiusNew)
    {
        if(Double.compare(this.tipRadius, tipRadiusNew) != 0)
        {
            double tipRadiusOld = this.tipRadius;
            this.tipRadius = tipRadiusNew;

            firePropertyChange(TIP_RADIUS, tipRadiusOld, tipRadiusNew);

            checkIfTransitionRadiusCalculable();
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
    }

    public double getTipHalfAngle()
    {
        return tipHalfAngle;
    }

    public void setTipHalfAngle(double tipHalfAngleNew)
    {
        if(Double.compare(this.tipHalfAngle, tipHalfAngleNew) != 0)
        {
            double tipHalfAngleOld = this.tipHalfAngle;
            this.tipHalfAngle = tipHalfAngleNew;

            firePropertyChange(TIP_HALF_ANGLE, tipHalfAngleOld, tipHalfAngleNew);

            checkIfTransitionRadiusCalculable();
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
    }

    public double getTipTransitionRadius()
    {
        return tipTransitionRadius;
    }

    public void setTipTransitionRadius(double tipTransitionRadiusNew)
    {
        if(Double.compare(this.tipTransitionRadius, tipTransitionRadiusNew) != 0)
        {
            double tipTransitionRadiusOld = this.tipTransitionRadius;
            this.tipTransitionRadius = tipTransitionRadiusNew;

            firePropertyChange(TIP_TRANSITION_RADIUS, tipTransitionRadiusOld, tipTransitionRadiusNew);

            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
    }

    public double getTipExponent()
    {
        return tipExponent;
    }

    public void setTipExponent(double tipExponentNew)
    {
        if(Double.compare(this.tipExponent, tipExponentNew) != 0)
        {
            double tipExponentOld = this.tipExponent;
            this.tipExponent = tipExponentNew;

            firePropertyChange(TIP_EXPONENT, tipExponentOld, tipExponentNew);

            checkIfTransitionRadiusCalculable();
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
    }

    public double getTipFactor()
    {
        return tipFactor;
    }

    public void setTipFactor(double tipFactorNew)
    {
        if(Double.compare(this.tipFactor, tipFactorNew) != 0)
        {
            double tipFactorOld = this.tipFactor;
            this.tipFactor = tipFactorNew;

            firePropertyChange(TIP_FACTOR, tipFactorOld, tipFactorNew);

            checkIfTransitionRadiusCalculable();
            checkIfBasicSettingsSpecified();
            checkIfSettingsSpecified(); 
        }
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

    public void setSmoothTransitionRadius()
    {
        double transitionRadiusNew = getSmoothTransitionRadius(); 

        setTipTransitionRadius(transitionRadiusNew);
    }

    public boolean isContactPointAutomatic()
    {
        return contactPointAutomatic;
    }

    public void setContactPointAutomatic(boolean contactPointAutomaticNew)
    {
        if(this.contactPointAutomatic != contactPointAutomaticNew)
        {
            boolean contactPointAutomaticOld = this.contactPointAutomatic;
            this.contactPointAutomatic = contactPointAutomaticNew;

            firePropertyChange(CONTACT_POINT_AUTOMATIC, contactPointAutomaticOld, contactPointAutomaticNew);
        }
    }

    public AutomaticContactEstimatorType getAutomaticEstimator()
    {
        return automaticContactEstimator;
    }

    public void setAutomaticContactEstimator(AutomaticContactEstimatorType estimatorNew)
    {
        Validation.requireNonNullParameterName(estimatorNew, "estimatorNew");

        if(!Objects.equals(this.automaticContactEstimator, estimatorNew))
        {
            AutomaticContactEstimatorType estimatorOld = this.automaticContactEstimator;
            this.automaticContactEstimator = estimatorNew;

            firePropertyChange(AUTOMATIC_CONTACT_ESTIMATOR, estimatorOld, estimatorNew);

            PREF.put(AUTOMATIC_CONTACT_ESTIMATOR, estimatorNew.getIdentifier());
            flushPreferences();
        }
    }

    public void setManualContactEstimator(ContactEstimator manualContactEstimator)
    {
        this.manualContactEstimator = manualContactEstimator;
    }

    public ContactEstimationMethod getContactEstimationMethod()
    {
        return contactEstimationMethod;
    }

    public void setContactEstimationMethod(ContactEstimationMethod contactEstimationMethodNew)
    {
        Validation.requireNonNullParameterName(contactEstimationMethodNew, "contactEstimationMethodNew");

        if(!Objects.equals(this.contactEstimationMethod, contactEstimationMethodNew))
        {
            ContactEstimationMethod contactEstimationMethodOld = this.contactEstimationMethod;
            this.contactEstimationMethod = contactEstimationMethodNew;

            boolean postcontactDegreeEnabledOld = contactEstimationMethodOld.isPostcontactDegreeInputRequired();
            boolean postcontactDegreeEnabledNew = contactEstimationMethodNew.isPostcontactDegreeInputRequired();

            firePropertyChange(CONTACT_ESTIMATION_METHOD, contactEstimationMethodOld, contactEstimationMethodNew);
            firePropertyChange(POSTCONTACT_DEGREE_INPUT_ENABLED, postcontactDegreeEnabledOld, postcontactDegreeEnabledNew);

            PREF.put(CONTACT_ESTIMATION_METHOD, contactEstimationMethodNew.getIdentifier());
            flushPreferences();
        }
    }

    public BasicRegressionStrategy getRegressionStrategy()
    {
        return regressionStrategy;
    }

    public void setRegressionStrategy(BasicRegressionStrategy regressionStrategyNew)
    {
        Validation.requireNonNullParameterName(regressionStrategyNew, "regressionStrategyNew");

        if(!Objects.equals(this.regressionStrategy, regressionStrategyNew))
        {
            BasicRegressionStrategy regressionStrategyOld = this.regressionStrategy;
            this.regressionStrategy = regressionStrategyNew;

            firePropertyChange(REGRESSION_STRATEGY, regressionStrategyOld, regressionStrategyNew);
            PREF.put(REGRESSION_STRATEGY, regressionStrategyNew.getIdentifier());
            flushPreferences();
        }
    }

    public ForceCurveBranch getFittedBranch()
    {
        return fittedBranch;
    }

    public void setFittedBranch(ForceCurveBranch fittedBranchNew)
    {
        Validation.requireNonNullParameterName(fittedBranchNew, "fittedBranchNew");

        if(!Objects.equals(this.fittedBranch, fittedBranchNew))
        {
            ForceCurveBranch fittedBranchOld = this.fittedBranch;
            this.fittedBranch = fittedBranchNew;

            firePropertyChange(FITTED_BRANCH, fittedBranchOld, fittedBranchNew);

            PREF.put(FITTED_BRANCH, fittedBranchNew.getIdentifier());
            flushPreferences();
        }
    }

    public AdhesiveEnergyEstimationMethod getAdhesiveEnergyEstimationMethod()
    {
        return adhesiveEnergyEstimator;
    }

    public void setAdhesiveEnergyEstimationMethod(AdhesiveEnergyEstimationMethod methodNew)
    {
        Validation.requireNonNullParameterName(methodNew, "methodNew");

        if(!Objects.equals(this.adhesiveEnergyEstimator, methodNew))
        {
            AdhesiveEnergyEstimationMethod methodOld = this.adhesiveEnergyEstimator;
            this.adhesiveEnergyEstimator = methodNew;

            firePropertyChange(ADHESIVE_ENERGY_ESTIMATION_METHOD, methodOld, methodNew);

            PREF.put(ADHESIVE_ENERGY_ESTIMATION_METHOD, methodNew.getIdentifier());
            flushPreferences();
        }
    }

    public void setAdhesionForceEstimator(ForceEventEstimator adhesionForceEstimator)
    {
        this.adhesionForceEstimator = adhesionForceEstimator;
    }

    public double getLeftCropping()
    {
        return leftCropping;
    }

    public void setLeftCropping(double leftCroppingNew)
    {
        if(Double.compare(this.leftCropping, leftCroppingNew) != 0)
        {
            double leftCroppingOld = this.leftCropping;
            this.leftCropping = leftCroppingNew;

            firePropertyChange(LEFT_CROPPING, leftCroppingOld, leftCroppingNew);

            checkIfSettingsSpecified();
        }
    }

    public double getRightCropping()
    {
        return rightCropping;
    }

    public void setRightCropping(double rightCroppingNew)
    {
        if(Double.compare(this.rightCropping, rightCroppingNew) != 0)
        {
            double rightCroppingOld = this.rightCropping;
            this.rightCropping = rightCroppingNew;

            firePropertyChange(RIGHT_CROPPING, rightCroppingOld, rightCroppingNew);

            checkIfSettingsSpecified();
        }
    }

    public double getLowerCropping()
    {
        return lowerCropping;
    }

    public void setLowerCropping(double lowerCroppingNew)
    {
        if(Double.compare(this.lowerCropping, lowerCroppingNew) != 0)
        {
            double lowerCroppingOld = this.lowerCropping;
            this.lowerCropping = lowerCroppingNew;

            firePropertyChange(LOWER_CROPPING, lowerCroppingOld, lowerCroppingNew);

            checkIfSettingsSpecified();
        }
    }

    public double getUpperCropping()
    {
        return upperCropping;
    }

    public void setUpperCropping(double upperCroppingNew)
    {
        if(Double.compare(this.upperCropping, upperCroppingNew) != 0)
        {
            double upperCroppingOld = this.upperCropping;
            this.upperCropping = upperCroppingNew;

            firePropertyChange(UPPER_CROPPING, upperCroppingOld, upperCroppingNew);

            checkIfSettingsSpecified();
        }
    }

    public boolean isDomainToBeCropped()
    {
        return domainCropped; 
    }

    public void setDomainCropped(boolean domainCroppedNew)
    {
        if(this.domainCropped != domainCroppedNew)
        {
            boolean domainCroppedOld = this.domainCropped;
            this.domainCropped = domainCroppedNew;

            firePropertyChange(DOMAIN_CROPPED, domainCroppedOld, domainCroppedNew);

            checkIfSettingsSpecified();
            checkIfCroppingOnCurveSelectionPossible();
        }
    }

    public boolean isRangeToBeCropped()
    {
        return rangeCropped; 
    }

    public void setRangeCropped(boolean rangeCroppedNew)
    {		
        if(this.rangeCropped != rangeCroppedNew)
        {
            boolean rangeCroppedOld = this.rangeCropped;
            this.rangeCropped = rangeCroppedNew;

            firePropertyChange(RANGE_CROPPED, rangeCroppedOld, rangeCroppedNew);

            checkIfSettingsSpecified();
            checkIfCroppingOnCurveSelectionPossible();
        }
    }

    public SmootherType getSmootherType()
    {
        return smootherType;
    }

    public void setSmootherType(SmootherType smootherTypeNew)
    {
        Validation.requireNonNullParameterName(smootherTypeNew, "smootherTypeNew");

        if(!Objects.equals(this.smootherType, smootherTypeNew))
        {
            SmootherType smootherTypeOld = this.smootherType;
            this.smootherType = smootherTypeNew;

            firePropertyChange(SMOOTHER_TYPE, smootherTypeOld, smootherTypeNew);

            checkIfSettingsSpecified();
        }
    }

    public boolean areDataSmoothed()
    {
        return smoothed;
    }

    public void setDataSmoothed(boolean dataSmoothedNew)
    {		
        if(this.smoothed != dataSmoothedNew)
        {
            boolean dataSmoothedOld = this.smoothed;
            this.smoothed = dataSmoothedNew;

            firePropertyChange(CURVE_SMOOTHED, dataSmoothedOld, dataSmoothedNew);

            checkIfSettingsSpecified();
        }
    }

    @Override
    public double getLoessSpan()
    {
        return loessSpan;
    }

    @Override
    public void setLoessSpan(double loessSpanNew)
    {
        if(Double.compare(this.loessSpan, loessSpanNew) != 0)
        {
            double loessSpanOld = this.loessSpan;
            this.loessSpan = loessSpanNew;

            firePropertyChange(LOESS_SPAN, loessSpanOld, loessSpanNew);

            checkIfSettingsSpecified();
        }
    }

    @Override
    public Number getLoessIterations()
    {
        return loessIterations;
    }

    @Override
    public void setLoessIterations(Number loessIterationsNew)
    {        
        if(loessIterationsNew == null)
        {
            return;
        }

        Double loessIterationsNewDouble = Math.rint(loessIterationsNew.doubleValue());  
        Double loessIterationsOld = this.loessIterations.doubleValue(); 
        this.loessIterations = loessIterationsNewDouble;

        firePropertyChange(LOESS_ITERATIONS, loessIterationsOld, loessIterationsNewDouble);

        checkIfSettingsSpecified();
    }

    @Override
    public Number getSavitzkyDegree()
    {
        return savitzkyDegree;
    }

    @Override
    public void setSavitzkyDegree(Number savitzkyDegreeNew)
    {
        if(savitzkyDegreeNew == null)
        {
            return;
        }       

        Double savitzkyDegreeNewDouble = Math.rint(savitzkyDegreeNew.doubleValue());
        Double savitzkyDegreeOld = this.savitzkyDegree.doubleValue();      
        this.savitzkyDegree = savitzkyDegreeNewDouble;

        firePropertyChange(SAVITZKY_DEGREE, savitzkyDegreeOld, savitzkyDegreeNewDouble);

        checkIfSettingsSpecified();
    }

    @Override
    public double getSavitzkySpan()
    {
        return savitzkySpan;
    }

    @Override
    public void setSavitzkySpan(double savitzkySpanNew)
    {
        if(Double.compare(this.savitzkySpan, savitzkySpanNew) != 0)
        {
            double savitzkySpanOld = this.savitzkySpan;
            this.savitzkySpan = savitzkySpanNew;

            firePropertyChange(SAVITZKY_SPAN, savitzkySpanOld, savitzkySpanNew);

            checkIfSettingsSpecified();
        }
    }

    public boolean isPlotRecordedCurve()
    {
        return plotRecordedCurve;
    }

    public void setPlotRecordedCurve(boolean plotRecordedCurveNew)
    {
        if(this.plotRecordedCurve != plotRecordedCurveNew)
        {
            boolean plotRecordedCurveOld = this.plotRecordedCurve;
            this.plotRecordedCurve = plotRecordedCurveNew;

            firePropertyChange(PLOT_RECORDED_CURVE, plotRecordedCurveOld, plotRecordedCurveNew);

            PREF.putBoolean(PLOT_RECORDED_CURVE, this.plotRecordedCurve);
            flushPreferences();
        }
    }

    public boolean isPlotRecordedCurveFit()
    {
        return plotRecordedCurveFit;
    }

    public void setPlotRecordedCurveFit(boolean plotRecordedCurveFitNew)
    {
        if(this.plotRecordedCurveFit != plotRecordedCurveFitNew)
        {
            boolean plotRecordedCurveFitOld = this.plotRecordedCurveFit;
            this.plotRecordedCurveFit = plotRecordedCurveFitNew;

            firePropertyChange(PLOT_RECORDED_CURVE_FIT, plotRecordedCurveFitOld, plotRecordedCurveFitNew);

            PREF.putBoolean(PLOT_RECORDED_CURVE_FIT, this.plotRecordedCurveFit);
            flushPreferences();
        }
    }

    public boolean isPlotIndentation()
    {
        return plotIndentation;
    }

    public void setPlotIndentation(boolean plotIndentationNew)
    {
        if(this.plotIndentation != plotIndentationNew)
        {
            boolean plotIndentationOld = this.plotIndentation;
            this.plotIndentation = plotIndentationNew;

            firePropertyChange(PLOT_INDENTATION, plotIndentationOld, plotIndentationNew);

            PREF.putBoolean(PLOT_INDENTATION, this.plotIndentation);
            flushPreferences();
        }
    }

    public boolean isPlotIndentationFit()
    {
        return plotIndentationFit;
    }

    public void setPlotIndentationFit(boolean plotIndentationFitNew)
    {
        if(this.plotIndentationFit != plotIndentationFitNew)
        {
            boolean plotIndentationFitOld = this.plotIndentationFit;
            this.plotIndentationFit = plotIndentationFitNew;

            firePropertyChange(PLOT_INDENTATION_FIT, plotIndentationFitOld, plotIndentationFitNew);

            PREF.putBoolean(PLOT_INDENTATION_FIT, this.plotIndentationFit);
            flushPreferences();
        }
    }

    public boolean isPlotModulus()
    {
        return plotModulus;
    }

    public void setPlotModulus(boolean plotModulusNew)
    {
        if(this.plotModulus != plotModulusNew)
        {
            boolean plotModulusOld = this.plotModulus;
            this.plotModulus = plotModulusNew;

            firePropertyChange(PLOT_MODULUS, plotModulusOld, plotModulusNew);

            PREF.putBoolean(PLOT_MODULUS, this.plotModulus);
            flushPreferences();
        }
    }

    public boolean isPlotModulusFit()
    {
        return plotModulusFit;
    }

    public void setPlotModulusFit(boolean plotModulusFitNew)
    {
        if(this.plotModulusFit != plotModulusFitNew)
        {
            boolean plotModulusFitOld = this.plotModulusFit;
            this.plotModulusFit = plotModulusFitNew;

            firePropertyChange(PLOT_MODULUS_FIT, plotModulusFitOld, plotModulusFitNew);

            PREF.putBoolean(PLOT_MODULUS_FIT, this.plotModulusFit);

            flushPreferences();
        }
    }
    
    public boolean isAveragingEnabled()
    {
        return curveAveragingEnabled;
    }

    public boolean isShowAveragedRecordedCurves()   
    {
        return showAveragedRecordedCurves;
    }

    public void setShowAveragedRecordedCurves(boolean showAveragedRecordedCurvesNew)   
    {
        if(!this.showAveragedRecordedCurves != showAveragedRecordedCurvesNew)
        {
            boolean showAveragedRecordedCurvesOld = this.showAveragedRecordedCurves;
            this.showAveragedRecordedCurves = showAveragedRecordedCurvesNew;

            firePropertyChange(SHOW_AVERAGED_RECORDED_CURVES, showAveragedRecordedCurvesOld, showAveragedRecordedCurvesNew);
            PREF.putBoolean(SHOW_AVERAGED_RECORDED_CURVES, showAveragedRecordedCurvesNew);

            flushPreferences();
        }
    }

    public int getAveragedRecordedCurvesPointCount()
    {
        return averagedRecordedCurvesPointCount;
    }
    
    public void setAveragedRecordedCurvesPointCount(int averagedRecordedCurvesPointCountNew)   
    {
        if(this.averagedRecordedCurvesPointCount != averagedRecordedCurvesPointCountNew)
        {
            int averagedRecordedCurvesPointCountOld = this.averagedRecordedCurvesPointCount;
            this.averagedRecordedCurvesPointCount = averagedRecordedCurvesPointCountNew;

            firePropertyChange(AVERAGED_RECORDED_CURVES_POINT_COUNT, averagedRecordedCurvesPointCountOld, averagedRecordedCurvesPointCountNew);
            PREF.putInt(AVERAGED_RECORDED_CURVES_POINT_COUNT, averagedRecordedCurvesPointCountNew);

            flushPreferences();
        }
    }
    
    public boolean isShowAveragedIndentationCurves()    
    {
        return showAveragedIndentationCurves;
    }

    public void setShowAveragedIndentationCurves(boolean showAveragedIndentationCurvesNew)    
    {
        if(this.showAveragedIndentationCurves != showAveragedIndentationCurvesNew)
        {
            boolean showAveragedIndentationCurvesOld = this.showAveragedIndentationCurves;
            this.showAveragedIndentationCurves = showAveragedIndentationCurvesNew;

            firePropertyChange(SHOW_AVERAGED_INDENTATION_CURVES, showAveragedIndentationCurvesOld, showAveragedIndentationCurvesNew);
            PREF.putBoolean(SHOW_AVERAGED_INDENTATION_CURVES, showAveragedIndentationCurvesNew);

            flushPreferences();
        }
    }   

    public int getAveragedIndentationCurvesPointCount()
    {
        return averagedIndentationCurvesPointCount;
    }
    
    public void setAveragedIndentationCurvesPointCount(int averagedIndentationCurvesPointCountNew)   
    {
        if(this.averagedIndentationCurvesPointCount != averagedIndentationCurvesPointCountNew)
        {
            int averagedIndentationCurvesPointCountOld = this.averagedRecordedCurvesPointCount;
            this.averagedIndentationCurvesPointCount = averagedIndentationCurvesPointCountNew;

            firePropertyChange(AVERAGED_INDENTATION_CURVES_POINT_COUNT, averagedIndentationCurvesPointCountOld, averagedIndentationCurvesPointCountNew);
            PREF.putInt(AVERAGED_INDENTATION_CURVES_POINT_COUNT, averagedIndentationCurvesPointCountNew);

            flushPreferences();
        }
    }

    public boolean isShowAveragedPointwiseModulusCurves()
    {
        return showAveragedPointwiseModulusCurves;
    }

    public void setShowAveragedPointwiseModulusCurves(boolean showAveragedPointwiseModulusCurvesNew)
    {
        if(this.showAveragedPointwiseModulusCurves != showAveragedPointwiseModulusCurvesNew)
        {
            boolean showAveragedPointwiseModulusCurvesOld = this.showAveragedPointwiseModulusCurves;
            this.showAveragedPointwiseModulusCurves = showAveragedPointwiseModulusCurvesNew;

            firePropertyChange(SHOW_AVERAGED_POINTWISE_MODULUS_CURVES, showAveragedPointwiseModulusCurvesOld, showAveragedPointwiseModulusCurvesNew);
            PREF.putBoolean(SHOW_AVERAGED_POINTWISE_MODULUS_CURVES, showAveragedPointwiseModulusCurvesNew);

            flushPreferences();
        }
    }
  

    public int getAveragedPointwiseModulusCurvesPointCount()
    {
        return averagedPointwiseModulusCurvesPointCount;
    }
    
    public void setAveragedPointwiseModulusCurvesPointCount(int averagedPointwiseModulusCurvesPointCountNew)   
    {
        if(this.averagedPointwiseModulusCurvesPointCount != averagedPointwiseModulusCurvesPointCountNew)
        {
            int averagedPointwiseModulusCurvesPointCountOld = this.averagedPointwiseModulusCurvesPointCount;
            this.averagedPointwiseModulusCurvesPointCount = averagedPointwiseModulusCurvesPointCountNew;

            firePropertyChange(AVERAGED_POINTWISE_MODULUS_CURVES_POINT_COUNT, averagedPointwiseModulusCurvesPointCountOld, averagedPointwiseModulusCurvesPointCountNew);
            PREF.putInt(AVERAGED_POINTWISE_MODULUS_CURVES_POINT_COUNT, averagedPointwiseModulusCurvesPointCountNew);

            flushPreferences();
        }
    }
    
    public ErrorBarType getAveragedCurvesBarType()
    {
        return averagedCurvesBarType;
    }

    public void setAveragedCurvesBarType(ErrorBarType averagedCurvesBarTypeNew)
    {
        Validation.requireNonNullParameterName(averagedCurvesBarTypeNew, "averagedCurvesBarTypeNew");

        if(!Objects.equals(this.averagedCurvesBarType, averagedCurvesBarTypeNew))
        {
            ErrorBarType averagedErrorBarTypeOld = this.averagedCurvesBarType;
            this.averagedCurvesBarType = averagedCurvesBarTypeNew;

            firePropertyChange(AVERAGED_CURVES_ERROR_BAR_TYPE, averagedErrorBarTypeOld, averagedCurvesBarTypeNew);
            PREF.put(AVERAGED_CURVES_ERROR_BAR_TYPE, this.averagedCurvesBarType.getIdentifier());

            flushPreferences();
        }
    }

    public boolean isIncludeCurvesInMaps()
    {
        return includeCurvesInMaps;
    }

    public void setIncludeCurvesInMaps(boolean plotMapsNew)
    {
        boolean plotMapsOld = this.includeCurvesInMaps;

        if(this.includeCurvesInMaps != plotMapsNew)
        {
            this.includeCurvesInMaps = plotMapsNew;
            firePropertyChange(INCLUDE_IN_MAPS, plotMapsOld, plotMapsNew);
        }
    }

    public boolean isIncludeCurvesInMapsEnabled()
    {
        return containsForceVolumeData;
    }

    public boolean isPlotMapAreaImages()
    {
        return plotMapAreaImages;
    }

    public void setPlotMapAreaImages(boolean plotMapAreaImagesNew)
    {
        boolean plotMapAreaImagesOld = this.plotMapAreaImages;

        if(this.plotMapAreaImages != plotMapAreaImagesNew)
        {
            this.plotMapAreaImages = plotMapAreaImagesNew;

            firePropertyChange(PLOT_MAP_AREA_IMAGES, plotMapAreaImagesOld, plotMapAreaImagesNew);
        }
    }

    public boolean isMapAreaImagesAvailable()
    {
        return mapAreaImagesAvailable;
    }

    public boolean isCalculateRSquared()
    {
        return calculateRSquared;
    }

    public void setCalculateRSquared(boolean calculateRSquaredNew)
    {
        if(this.calculateRSquared != calculateRSquaredNew)
        {
            boolean calculateRSquaredOld = this.calculateRSquared;
            this.calculateRSquared = calculateRSquaredNew;

            firePropertyChange(CALCULATE_R_SQUARED, calculateRSquaredOld, calculateRSquaredNew);

            PREF.putBoolean(CALCULATE_R_SQUARED, this.calculateRSquared);

            flushPreferences();
        }
    }

    public boolean isCalculateAdhesionForce()
    {
        return calculateAdhesionForce;
    }

    public void setCalculateAdhesionForce(boolean calculateAdhesionForceNew)
    {        
        if(this.calculateAdhesionForce != calculateAdhesionForceNew)
        {
            boolean calculateAdhesionForceOld = this.calculateAdhesionForce;
            this.calculateAdhesionForce = calculateAdhesionForceNew;

            firePropertyChange(CALCULATE_ADHESION_FORCE, calculateAdhesionForceOld, calculateAdhesionForceNew);
            PREF.putBoolean(CALCULATE_ADHESION_FORCE, this.calculateAdhesionForce);

            flushPreferences();
        }
    }

    public boolean isCroppingOnCurveSelectionPossible()
    {
        return croppingOnCurveSelectionPossible;
    }

    private void checkIfCroppingOnCurveSelectionPossible()
    {
        boolean croppingOnCurveSelectionPossibleNew = calculateIfCroppingOnCurveSelectionPossible();
        boolean trimmingOnCurveSelectionPossibleOld = croppingOnCurveSelectionPossible;     

        if(croppingOnCurveSelectionPossibleNew != croppingOnCurveSelectionPossible)
        {
            croppingOnCurveSelectionPossible = croppingOnCurveSelectionPossibleNew;
            firePropertyChange(CROPPING_ON_CURVE_SELECTION_POSSIBLE, trimmingOnCurveSelectionPossibleOld, croppingOnCurveSelectionPossibleNew);
        }
    }   

    public boolean calculateIfCroppingOnCurveSelectionPossible()
    {
        boolean springConstantShouldBeSpecified = springConstantInputNecessary || !springConstantUseReadIn;
        boolean isSpringConstantSpecified = !(Double.isNaN(springConstant) && springConstantShouldBeSpecified);

        boolean isSensitivitySpecified = calculateSensitivityIsSpecifiedIfNecessary();

        boolean trimmingSelectionPossible = (domainCropped || rangeCropped) && isSpringConstantSpecified && isSensitivitySpecified;

        return trimmingSelectionPossible;
    }

    public Dataset1DCroppingModel<SpectroscopyCurve<Channel1D>> getCroppingModel()
    {
        SimpleSpectroscopySource source = sources.get(0).getSimpleSources().get(0);

        double sensitivity = (source.getPhotodiodeSignalType() != null) ? this.sensitivity.get(source.getPhotodiodeSignalType()) : 1;
        SpectroscopyCurve<Channel1D> curve = source.getRecordedForceCurve(sensitivity, 1000*springConstant);

        Channel1D croppedChannel = ForceCurveBranch.APPROACH.equals(fittedBranch) ? curve.getApproach() : curve.getWithdraw();

        Channel1DData channelData = croppedChannel.getChannelData();
        Range xRange = channelData.getXRange();
        Range yRange = channelData.getYRange();

        Dataset1DCroppingModel<SpectroscopyCurve<Channel1D>> model = new Dataset1DCroppingModel<>(curve, xRange, yRange, domainCropped, rangeCropped);
        model.setLeftCropping(leftCropping);
        model.setRightCropping(rightCropping);
        model.setLowerCropping(lowerCropping);
        model.setUpperCropping(upperCropping);

        return model;
    }

    boolean isNonEmpty()
    {
        return nonEmpty;
    }

    boolean isTipSmoothTransitionRadiusCalculable()
    {
        return tipTransitionRadiusCalculable;
    }

    boolean areSettingSpecified()
    {
        return settingsSpecified;
    }

    boolean isNecessaryInputProvided()
    {
        boolean specifiedSettings = areSettingSpecified();
        boolean isNonempty = isNonEmpty();
        boolean necessaryInputProvided = ((isNonempty&&specifiedSettings)||(!isNonempty));

        return necessaryInputProvided;
    }

    private boolean isCroppingInputProvided()
    {
        boolean inputProvided = true;
        if(domainCropped)
        {
            inputProvided = (!Double.isNaN(leftCropping))&&(!Double.isNaN(rightCropping));
        }
        if(rangeCropped)
        {
            inputProvided = inputProvided&&(!Double.isNaN(lowerCropping))&&(!Double.isNaN(upperCropping));
        }
        return inputProvided;
    }

    private boolean isSubstrateCorrectionInputProvided()
    {
        boolean inputProvided = true;

        if(correctSubstrateEffect)
        {
            if(useSampleTopography)
            {
                inputProvided = (sampleTopographyFile != null) && (sampleTopographyChannel != null);
            }
            else
            {
                inputProvided = !Double.isNaN(sampleThickness);
            }
        }

        return inputProvided;
    }

    private boolean isSmoothingInputProvided()
    {
        boolean inputProvided = smoothed ? smootherType.isInputProvided(this) : true;

        return inputProvided;
    }

    boolean areBasicSettingsSpecified()
    {
        return basicSettingsSpecified;
    }

    public boolean isSubstrateEffectCorrectionKnown()
    {
        return substrateEffectCorrectionKnown;
    }

    public boolean isAdhesiveEnergyRequired()
    {
        return adhesiveEnergyRequired;
    }

    public Channel2DData getCorrectedTopography()
    {       
        ROI roi = ROIUtilities.composeROIs(sampleROIs, "Sample");

        Channel2DData topographyChannel = getSampleTopographyChannel().getChannelData();

        double conversionFactor = topographyChannel.getZQuantity().getUnit().getConversionFactorTo(Units.MICRO_METER_UNIT);

        Channel2DData correctedUnit = (conversionFactor != 1) ? new MultiplyOperation(conversionFactor).transform(topographyChannel) : topographyChannel;

        Channel2DDataInROITransformation tr = new FixMinimumOperation(0, true);
        Channel2DData zeroFixed = tr.transform(correctedUnit, roi, ROIRelativePosition.INSIDE);

        return zeroFixed;
    }

    public Cone getCone()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        Cone pyramid = new Cone(angle);

        return pyramid;
    }

    public boolean isConeAvailable()
    {
        return !Double.isNaN(tipHalfAngle);
    }

    public Pyramid getPyramid()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        Pyramid pyramid = new Pyramid(angle);

        return pyramid;
    }

    public boolean isPyramidAvailable()
    {
        return !Double.isNaN(tipHalfAngle);
    }

    public Sphere getSphere()
    {
        double r = getTipRadius();
        Sphere indenter = new Sphere(r);

        return indenter;
    }

    public boolean isSphereAvailable()
    {
        return !Double.isNaN(tipRadius);
    }

    public PowerShapedTip getPowerShapedTip()
    {
        double exponent = getTipExponent();
        double factor = getTipFactor();

        PowerShapedTip powerShapedTip = new PowerShapedTip(exponent, factor);

        return powerShapedTip;
    }

    public boolean isPowerShapedTipAvailable()
    {
        return !Double.isNaN(tipExponent) && !Double.isNaN(tipFactor);
    }

    public BluntConeWithSphericalCap getBluntCone()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        double r = getTipRadius();
        double tr = getTipTransitionRadius();
        BluntConeWithSphericalCap bluntCone = new BluntConeWithSphericalCap(angle, r, tr);

        return bluntCone;
    }

    public boolean isBluntConeAvailable()
    {
        return !Double.isNaN(tipHalfAngle) && !Double.isNaN(tipRadius) && !Double.isNaN(tipTransitionRadius);
    }

    public BluntPyramid getBluntPyramid()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        double r = getTipRadius();
        double tr = getTipTransitionRadius();
        BluntPyramid bluntPyramid = new BluntPyramid(angle, r, tr);

        return bluntPyramid;
    }

    public boolean isBluntPyramidAvailable()
    {
        return !Double.isNaN(tipHalfAngle) && !Double.isNaN(tipRadius) && !Double.isNaN(tipTransitionRadius);
    }


    public TruncatedCone getTruncatedCone()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        double tr = getTipTransitionRadius();
        TruncatedCone truncatedCone = new TruncatedCone(angle,tr);

        return truncatedCone;
    }

    public boolean isTruncatedConeAvailable()
    {
        return !Double.isNaN(tipHalfAngle) && !Double.isNaN(tipTransitionRadius);
    }

    public TruncatedPyramid getTruncatedPyramid()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        double tr = getTipTransitionRadius();
        TruncatedPyramid truncatedPyramid = new TruncatedPyramid(angle, tr);

        return truncatedPyramid;
    }

    public boolean isTruncatedPyramidAvailable()
    {
        return !Double.isNaN(tipHalfAngle) && !Double.isNaN(tipTransitionRadius);
    }

    public Hyperboloid getHyperboloid()
    {
        double angle = Math.PI* getTipHalfAngle() / 180.;
        double r = getTipRadius();
        Hyperboloid hyperboloid = new Hyperboloid(angle, r);

        return hyperboloid;
    }

    public boolean isHyperboloidAvailable()
    {
        return !Double.isNaN(tipHalfAngle) && !Double.isNaN(tipRadius);
    }

    private ContactModel buildContactModel()
    {        
        return indentationModel.getModel(this);
    }

    public IdentityTag  getBatchIdentityTag()
    {
        IdentityTag batchId = new IdentityTag(batchNumber, batchName);
        return batchId;
    }


    private MapProcessingSettings buildMapProcessingSettings()
    {
        MapProcessingSettings settings = new MapProcessingSettings(includeCurvesInMaps, plotMapAreaImages);
        return settings;
    }

    private VisualizationSettings buildVisualizationSettings()
    {
        VisualizationChartSettings recordedChartSettings = new VisualizationChartSettings(plotRecordedCurve, plotRecordedCurveFit);
        VisualizationChartSettings indentationChartSettings = new VisualizationChartSettings(plotIndentation, plotIndentationFit);
        VisualizationChartSettings modulusChartSettings = new VisualizationChartSettings(plotModulus, plotModulusFit);

        VisualizationSettings visSettings = new VisualizationSettings(recordedChartSettings, indentationChartSettings, modulusChartSettings);
        return visSettings;
    }

    public SpectroscopyCurveAveragingSettings getCurveAveragingSettings()
    {
        AveragingSettings recordedCurveSettings = new AveragingSettings(averagedRecordedCurvesPointCount, averagedCurvesBarType, showAveragedRecordedCurves);
        AveragingSettings indentationCurveSettings = new AveragingSettings(averagedIndentationCurvesPointCount, averagedCurvesBarType, showAveragedIndentationCurves);
        AveragingSettings pointwiseModulusCurveSettings = new AveragingSettings(averagedPointwiseModulusCurvesPointCount, averagedCurvesBarType, showAveragedPointwiseModulusCurves);

        SpectroscopyCurveAveragingSettings averagedCurves = new SpectroscopyCurveAveragingSettings(curveAveragingEnabled, recordedCurveSettings, indentationCurveSettings, pointwiseModulusCurveSettings);
        return averagedCurves;
    }

    private ProcessingSettings buildProcessingSettings(ContactModel contactModel, ContactEstimationGuide contactEstimationGuide, SimpleSpectroscopySource source) throws InputNotProvidedException, UserCommunicableException
    {        
        PhotodiodeSignalType rawSignalType = source.getPhotodiodeSignalType();

        double k = springConstantUseReadIn && source.isSpringConstantKnown() ? source.getSpringConstant(): getSpringConstant();
        double sens = useReadInSensitivity.get(rawSignalType) && source.isSensitivityKnown() ? source.getSensitivity(): this.sensitivity.get(rawSignalType);

        boolean automatic = isContactPointAutomatic();

        double upperCrop = rangeCropped ? upperCropping/(1000*k) : 0;
        double bottomCrop = rangeCropped ? lowerCropping/(1000*k) : 0;
        double leftCrop = domainCropped ? leftCropping : 0;
        double rightCrop = domainCropped ? rightCropping : 0;

        CropSettings cropSettings = new CropSettings(upperCrop, leftCrop,bottomCrop,rightCrop);
        Channel1DDataTransformation trimmer = new Crop1DTransformation(cropSettings);

        ProcessingSettings.Builder builder = new ProcessingSettings.Builder(contactModel, contactEstimationGuide, regressionStrategy, fittedBranch, adhesiveEnergyEstimator, automatic,k,sens);
        builder.trimmer(trimmer).trimmed(cropSettings.isAnythingCropped()).indentationLimit(indentationLimit).loadLimit(loadLimit).fitIndentationLimit(fitIndentationLimit).fitZMinimum(fitZMinimum).fitZMaximum(fitZMaximum)
        .calculateRSquared(calculateRSquared).calculateAdhesionForce(calculateAdhesionForce);

        if(smoothed && isSmoothingInputProvided())
        {
            try 
            {
                Channel1DDataTransformation smoother = smootherType.getSmoothingTransformation(this);
                builder.smoother(smoother);
                builder.smoothed(true);
            } 
            catch (Exception e) 
            {                   
                throw new UserCommunicableException("Due to error, smoothing cannot be carried out", e);
            }
        };

        ProcessingSettings settings = builder.build();
        return settings;
    }

    public List<ProcessableSpectroscopyPack> buildProcessingBatch()
    {        
        try
        {
            VisualizationSettings visualizationSettings = buildVisualizationSettings();
            MapProcessingSettings mapSettings = buildMapProcessingSettings();

            List<SimpleSpectroscopySource> sourcesToProcess = new ArrayList<>(sources);

            //we create two list, because when the manual method is used,
            //we may allow for the possibility that they will reject some sources

            List<ProcessableSpectroscopyPack> allPacks = new ArrayList<>();
            List<ProcessableSpectroscopyPack> packsInitial = new ArrayList<>();

            ContactModel contactModel = buildContactModel();
            ContactEstimationGuide contactEstimationGuide = ContactEstimationMethod.CONTACT_MODEL_BASED.equals(contactEstimationMethod) ? contactModel : new IndentationIndependentContactEstimationGuide(new BasicPrecontactInteractionsModel(baselineDegree, false) , postcontactDegree);

            ForceEventEstimator jumpEstimator = findJumps ? new MinimalJumpEstimator(polynomialDegree, 3, new UnitExpression(jumpMinDistanceFromContact, Units.NANO_METER_UNIT), jumpsSpan, jumpsSpanType, jumpsWeightFunction) : ForceEventEstimatorNull.getInstance();

            ProcessingBatchMemento memento = new ProcessingBatchMemento(this);

            IdentityTag batchId = getBatchIdentityTag();

            for(SimpleSpectroscopySource source: sourcesToProcess)
            {
                ProcessingSettings procSettings = buildProcessingSettings(contactModel, contactEstimationGuide, source);

                ProcessableSpectroscopyPack pack = new ProcessableSpectroscopyPack(source, procSettings, mapSettings, visualizationSettings, batchId);
                pack.setAdhesionForceEstimator(adhesionForceEstimator);
                pack.setJumpEstimator(jumpEstimator);
                source.setProcessingMemento(memento);

                packsInitial.add(pack);                        
            }

            if(isContactPointAutomatic())
            {                
                for(ProcessableSpectroscopyPack pack: packsInitial)
                {
                    ContactEstimator estimator = getContactEstimator(pack);
                    pack.setContactEstimator(estimator);
                    allPacks.add(pack); 
                }
            }
            else if(manualContactEstimator != null)
            {
                for(ProcessableSpectroscopyPack pack: packsInitial)
                {
                    pack.setContactEstimator(manualContactEstimator);
                    allPacks.add(pack); 
                }
            }
            else
            {
                ContactSelectionDialog dialog = destination.getContactPointSelectionDialog();

                ContactSelectionModel selectionModel = new ContactSelectionModel(packsInitial);

                List<ProcessableSpectroscopyPack> packsManual = dialog.setModelAndShow(selectionModel);
                allPacks.addAll(packsManual);
            }

            return allPacks;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(destination.getPublicationSite(), "An error occured during the computation", "", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private ContactEstimator getContactEstimator(ProcessableSpectroscopyPack pack) 
    {
        ContactEstimationGuide estimationGuide = pack.getProcessingSettings().getContactEstimationGuide();
        ContactEstimator estimator = automaticContactEstimator.getContactEstimator(baselineDegree, estimationGuide);

        return estimator;
    }

    /*
     * This methods check whether:
     * - sensitivityInputCanBeUsed - at least one source contains enough information to use user - specified sensitivity (i.e. is not calibrated or if it is - then the sensitivity is know)
     * - sensitivityReadInCanBeUsed - at least one source contains enough information to use read - in sensitivity (i.e. is calibrated or sensitivity is known)
     * - sensitivityInputNecessary - at least one source does not contain enough information to use read in sensitivity (i.e. it is not calibrated and the read-in sensitivity is not known)
     * - sensitivityReadInNecessary - at least one source does not contain enough information to use user - specified sensitivity (i.e. it is calibrated, but sensitivity used for calibration is not known, so that it cannot be converted back to raw voltage readings)
     */

    private void initializeSensitivitySpecificationSettings()
    {      
        Set<PhotodiodeSignalType> signalTypesNew = EnumSet.noneOf(PhotodiodeSignalType.class);

        Map<PhotodiodeSignalType, Boolean> sensitivityInputCanBeUsed = getPhotodiodeSignalMap(Boolean.FALSE);
        Map<PhotodiodeSignalType, Boolean> sensitivityInputNecessary = getPhotodiodeSignalMap(Boolean.FALSE);
        Map<PhotodiodeSignalType, Boolean> sensitivityReadInCanBeUsed = getPhotodiodeSignalMap(Boolean.FALSE);
        Map<PhotodiodeSignalType, Boolean> sensitivityReadInNecessary = getPhotodiodeSignalMap(Boolean.FALSE); 

        for(SimpleSpectroscopySource simpleSource : sources)
        {
            PhotodiodeSignalType signalType = simpleSource.getPhotodiodeSignalType();
            signalTypesNew.add(signalType);

            boolean calibrated = simpleSource.isSensitivityCalibrated() || simpleSource.isForceCalibrated();

            boolean readInSensitivityKnown = simpleSource.isSensitivityKnown();

            boolean onlyReadInCanBeUsed = (calibrated && !readInSensitivityKnown);
            boolean onlyUserInputCanBeUsed = (!calibrated && !readInSensitivityKnown);


            sensitivityInputCanBeUsed.put(signalType, sensitivityInputCanBeUsed.get(signalType) || !onlyReadInCanBeUsed);
            sensitivityReadInNecessary.put(signalType, sensitivityReadInNecessary.get(signalType) || onlyReadInCanBeUsed);
            sensitivityInputNecessary.put(signalType, sensitivityInputNecessary.get(signalType) || onlyUserInputCanBeUsed);
            sensitivityReadInCanBeUsed.put(signalType, sensitivityReadInCanBeUsed.get(signalType) || !onlyUserInputCanBeUsed);

            //            boolean allInformationCollected = sensitivityInputCanBeUsed.get(signalType) && sensitivityReadInNecessary.get(signalType)
            //                    && sensitivityInputNecessary.get(signalType) && sensitivityReadInCanBeUsed.get(signalType);
            //
            //            if(allInformationCollected)
            //            {
            //                break;
            //            }
        }

        Set<PhotodiodeSignalType> signalTypesOld = EnumSet.copyOf(this.signalTypes);
        this.signalTypes = signalTypesNew;

        firePropertyChange(SENSITIVITY_PHOTODIODE_SIGNALS, signalTypesOld, signalTypesNew);

        this.sensitivityInputCanBeUsed = sensitivityInputCanBeUsed;
        this.sensitivityInputNecessary = sensitivityInputNecessary;
        this.sensitivityReadInNecessary = sensitivityReadInNecessary;
        this.sensitivityReadInCanBeUsed = sensitivityReadInCanBeUsed;

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            if(sensitivityReadInNecessary.get(signalType))
            {
                setUseReadInSensitivityPrivate(signalType, sensitivityReadInNecessary.get(signalType));
            }

            //if sensitivityInputNecessary or sensitivityReadInNecessary, then for sure sensitivity is not specified
            //for all source files
            if(!sensitivityInputNecessary.get(signalType) && !sensitivityReadInNecessary.get(signalType))
            {
                this.readInSensitivity.put(signalType, calculateReadInSensitivity(signalType));
            }
        }


        checkIfSensitivityInputEnabled();
        checkIfSensitivityUseReadInEnabled();

        ensureConsistencyOfReadInSensitivityUse();
        ensureConsistencyOfSensitivityValues();                    
    } 

    private void initializeSpringConstantSpecificationSettings()
    {
        boolean springConstantInputNecessary = false;
        boolean springConstantReadInCanBeUsed = false;

        for(SimpleSpectroscopySource simpleSource : sources)
        {
            boolean readInSpringConstantKnown = simpleSource.isSpringConstantKnown();

            boolean onlyUserInputCanBeUsed = (!readInSpringConstantKnown);

            springConstantInputNecessary = springConstantInputNecessary || onlyUserInputCanBeUsed;
            springConstantReadInCanBeUsed = springConstantReadInCanBeUsed || !onlyUserInputCanBeUsed;

            boolean allInformationCollected = springConstantInputNecessary && springConstantReadInCanBeUsed;

            if(allInformationCollected)
            {
                break;
            }
        }

        this.springConstantInputNecessary = springConstantInputNecessary;
        this.springConstantReadInCanBeUsed = springConstantReadInCanBeUsed;

        //if sensitivityInputNecessary or sensitivityReadInNecessary, then for sure sensitivity is not specified
        //for all source files

        if(!springConstantInputNecessary && springConstantReadInCanBeUsed)
        {
            this.readInSpringConstant = calculateReadInSpringConstant();
        }

        checkIfSpringConstantInputEnabled();
        setSpringConstantUseReadInEnabled(springConstantReadInCanBeUsed);

        ensureConsistencyOfReadInSpringConstantUse();
        ensureConsistencyOfSpringConstantValues();
    }   

    private void ensureConsistencyOfReadInSensitivityUse()
    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            if(sensitivityReadInNecessary.get(signalType))
            {
                setUseReadInSensitivityPrivate(signalType, true);
            }
            else if(!sensitivityReadInCanBeUsed.get(signalType))
            {
                setUseReadInSensitivityPrivate(signalType, false);
            }
        }

    }

    private void ensureConsistencyOfReadInSpringConstantUse()
    {
        if(!springConstantReadInCanBeUsed)
        {            
            setUseReadInSpringConstantPrivate(false);
        }
    }


    //we impose read in sensitivity only if useReadInSensitivity is true AND 
    //EITHER (sensitivity is NaN OR sensitivityInput is not necessary)
    private void ensureConsistencyOfSensitivityValues()
    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            if(useReadInSensitivity.get(signalType))
            {
                boolean enforceReadInSensitivity = Double.isNaN(sensitivity.get(signalType)) || !sensitivityInputNecessary.get(signalType);

                if(enforceReadInSensitivity)
                {
                    setSensitivityPrivate(signalType, readInSensitivity.get(signalType));
                }
            }
        }

    }

    //we impose read in spring constant only if useReadInSensitivity is true AND 
    //EITHER (springConstant is NaN OR spring constant input is not necessary)
    private void ensureConsistencyOfSpringConstantValues()
    {
        if(springConstantUseReadIn)
        {
            boolean enforceReadInSpringConstant = Double.isNaN(springConstant) || !springConstantInputNecessary;

            if(enforceReadInSpringConstant)
            {
                setSpringConstantPrivate(readInSpringConstant);
            }
        }
    }

    public Map<PhotodiodeSignalType, Double> getReadInSensitivity()
    {
        return readInSensitivity;
    }

    /*
     * This method return the sensitivity if all read in sources have the same known read in sensitivity,
     * and Double.NaN otherwise
     */
    private double calculateReadInSensitivity(PhotodiodeSignalType signalType)
    {
        double sensitivity = Double.NaN;
        for(SimpleSpectroscopySource source : sources)
        {
            PhotodiodeSignalType currentSignalType = source.getPhotodiodeSignalType();
            if(!signalType.equals(currentSignalType))
            {
                continue;
            }

            if(source.isSensitivityKnown())
            {
                sensitivity = source.getSensitivity();
            }
            else
            {
                break;
            }
        }

        if(Double.isNaN(sensitivity))
        {
            return sensitivity;
        }

        //check whether all other sources have the sensitivity the same as the first one
        for(SimpleSpectroscopySource source : sources)
        {            
            PhotodiodeSignalType currentSignalType = source.getPhotodiodeSignalType();
            if(!signalType.equals(currentSignalType))
            {
                continue;
            }

            if(source.isSensitivityKnown())
            {
                double currentSensitivity = source.getSensitivity();
                if(Math.abs(currentSensitivity - sensitivity) > TOLERANCE)
                {
                    sensitivity = Double.NaN;
                    break;
                }
            }            
            else
            {
                sensitivity = Double.NaN;
                break;
            }
        }

        return sensitivity;
    }

    public double getReadInSpringConstant()
    {
        return readInSpringConstant;
    }

    /*
     * This method return the spring constant if all read in sources have the same known read in spring constant,
     * and Double.NaN otherwise
     */
    private double calculateReadInSpringConstant()
    {
        if(sources.isEmpty())
        {
            return Double.NaN;
        }

        double springConstant = sources.get(0).getSpringConstant();

        for(SimpleSpectroscopySource source : sources)
        {
            if(source.isSpringConstantKnown())
            {
                double currentSpringConstant = source.getSpringConstant();
                if(Math.abs(currentSpringConstant - springConstant) > TOLERANCE)
                {
                    springConstant = Double.NaN;
                    break;
                }
            }

            else
            {
                springConstant = Double.NaN;
                break;
            }
        }

        return springConstant;
    }

    private void checkIfSensitivityInputEnabled()
    {
        Map<PhotodiodeSignalType, Boolean> sensitivityInputEnabledOld = new EnumMap<>(this.sensitivityInputEnabled);

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            boolean sensitivityEnabledNew = sensitivityInputCanBeUsed.get(signalType) ? 
                    (!useReadInSensitivity.get(signalType) || sensitivityInputNecessary.get(signalType)) : false;

                    this.sensitivityInputEnabled.put(signalType, sensitivityEnabledNew);
        }

        firePropertyChange(SENSITIVITY_INPUT_ENABLED, sensitivityInputEnabledOld, new EnumMap<>(this.sensitivityInputEnabled));    
    }

    private void checkIfSpringConstantInputEnabled()
    {        
        boolean springConstantInputEnabledNew = !springConstantUseReadIn;       

        if(this.springConstantInputEnabled != springConstantInputEnabledNew)
        {
            boolean springConstantInputEnabledOld = this.springConstantInputEnabled;
            this.springConstantInputEnabled = springConstantInputEnabledNew;
            firePropertyChange(SPRING_CONSTANT_INPUT_ENABLED, springConstantInputEnabledOld, springConstantInputEnabledNew);
        }
    }

    private void checkIfSensitivityUseReadInEnabled()
    {
        Map<PhotodiodeSignalType, Boolean> sensitivityUseReadInEnabledOld = new EnumMap<>(this.sensitivityUseReadInEnabled);

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            //in both cases the user could not change whether to use read-in sensitivity
            boolean sensitivityUseReadInEnabledNew = sensitivityReadInCanBeUsed.get(signalType) && !sensitivityReadInNecessary.get(signalType);

            this.sensitivityUseReadInEnabled.put(signalType, sensitivityUseReadInEnabledNew);
        }       

        firePropertyChange(SENSITIVITY_USE_READ_IN_ENABLED, sensitivityUseReadInEnabledOld, new EnumMap<>(this.sensitivityUseReadInEnabled));
    }

    private void setSpringConstantUseReadInEnabled(boolean springConstantReadInEnabledNew)
    {
        boolean springConstantUseReadInEnabledOld = this.springConstantUseReadInEnabled;

        if(springConstantReadInEnabledNew != this.springConstantUseReadInEnabled)
        {
            this.springConstantUseReadInEnabled = springConstantReadInEnabledNew;
            firePropertyChange(SPRING_CONSTANT_USE_READ_IN_ENABLED,springConstantUseReadInEnabledOld,springConstantReadInEnabledNew);
        }
    }

    private void checkIfTransitionRadiusCalculable()
    {
        boolean tipTransitionCalculableNew = false;
        if(indentationModel.isTipParameterNeeded(TipShapeParameter.TRANSITION_RADIUS))
        {
            tipTransitionCalculableNew = !(Double.isNaN(tipRadius) || Double.isNaN(tipHalfAngle));
        }

        boolean transitionRadiusCalculableOld = tipTransitionRadiusCalculable;

        if(tipTransitionCalculableNew != tipTransitionRadiusCalculable)
        {
            tipTransitionRadiusCalculable = tipTransitionCalculableNew;
            firePropertyChange(TIP_TRANSITION_RADIUS_CALCULABLE, transitionRadiusCalculableOld, tipTransitionCalculableNew);
        }   
    }   

    private void checkIfSubstrateEffectCorrectionKnown()
    {
        boolean substrateEffectCorrectionKnownNew = indentationModel.isSubstrateEffectCorrectionKnown(thicknessCorrectionMethod, sampleAdherent);
        boolean substrateEffectCorrectionKnownOld = substrateEffectCorrectionKnown;

        if(substrateEffectCorrectionKnownNew != substrateEffectCorrectionKnown)
        {
            substrateEffectCorrectionKnown = substrateEffectCorrectionKnownNew;
            firePropertyChange(SUBSTRATE_EFFECT_CORRECTION_KNOWN, substrateEffectCorrectionKnownOld, substrateEffectCorrectionKnownNew);
        }
    }

    private Set<ThicknessCorrectionMethod> calculateWhichSubstrateEffectCorrectionsCanBeApplied()
    {
        Set<ThicknessCorrectionMethod> applicableCorrectionMethods = new LinkedHashSet<>();

        for(ThicknessCorrectionMethod method : ThicknessCorrectionMethod.values())
        {
            if(indentationModel.isSubstrateEffectCorrectionKnown(method, sampleAdherent))
            {
                applicableCorrectionMethods.add(method);
            }
        }

        //if we were to use Enum set, we would have to take into account that
        //the method EnumSet.copyOf(Collection) throws and exception if the collection is empty
        return Collections.unmodifiableSet(applicableCorrectionMethods);
    }

    private void checkIfApplicableSubstrateEffectCorrectionsChanged()
    {
        Set<ThicknessCorrectionMethod> applicableCorrectionMethodsNew = calculateWhichSubstrateEffectCorrectionsCanBeApplied();
        Set<ThicknessCorrectionMethod> applicableCorrectionMethodsOld = this.applicableThicknessCorrectionMethods;

        if(!Objects.equals(applicableCorrectionMethodsOld, applicableCorrectionMethodsNew))
        {
            this.applicableThicknessCorrectionMethods = applicableCorrectionMethodsNew;
            firePropertyChange(APPLICABLE_THICKNESS_CORRECTION_METHODS, applicableCorrectionMethodsOld, applicableCorrectionMethodsNew);
        }
    }

    private void checkIfAdhesiveEnergyRequired()
    {
        boolean adhesiveEnergyRequiredNew = indentationModel.requiresAdhesiveEnergy();
        boolean adhesiveEnergyRequiredOld = adhesiveEnergyRequired;

        if(adhesiveEnergyRequiredNew != adhesiveEnergyRequired)
        {
            adhesiveEnergyRequired = adhesiveEnergyRequiredNew;
            firePropertyChange(ADHESIVE_ENERGY_REQUIRED, adhesiveEnergyRequiredOld, adhesiveEnergyRequiredNew);
        }
    }

    private void checkIfNonEmpty()
    {
        boolean batchNonEmptyNew = !sources.isEmpty();
        boolean batchNonEmptyOld = this.nonEmpty;

        if(batchNonEmptyNew != this.nonEmpty)
        {
            this.nonEmpty = batchNonEmptyNew;
            firePropertyChange(ProcessingBatchModelInterface.SOURCES_SELECTED, batchNonEmptyOld, batchNonEmptyNew);
        }
    }

    private void checkIfAveragingEnabled()
    {
        boolean curveAveragingEnabledOld = this.curveAveragingEnabled;
        this.curveAveragingEnabled = sources.size() > 1;
        
        firePropertyChange(CURVE_AVERAGING_ENABLED, curveAveragingEnabledOld, this.curveAveragingEnabled);           
    }

    private void checkIfContainsForceVolumeData()
    {
        boolean containsForceVolumeDataNew = findIfContainsForceVolumeData(); 
        boolean containsForceVolumeDataOld = this.containsForceVolumeData;

        if(containsForceVolumeDataOld != containsForceVolumeDataNew)
        {
            this.containsForceVolumeData = containsForceVolumeDataNew;

            firePropertyChange(CONTAINS_FORCE_VOLUME_DATA, containsForceVolumeDataOld, containsForceVolumeDataNew);           
            firePropertyChange(INCLUDE_IN_MAPS_ENABLED, containsForceVolumeDataOld, containsForceVolumeDataNew);           
            firePropertyChange(ProcessingBatchModelInterface.FILTERING_POSSIBLE, containsForceVolumeDataOld, containsForceVolumeDataNew);           
        }
    }

    private void checkIfParentDirectoryChanged()
    {
        File parentDirectoryOld = this.parentDirectory;
        File parentDirectoryNew = BatchUtilities.findLastCommonSourceDirectory(sources);

        if(!Objects.equals(parentDirectoryOld, parentDirectoryNew))
        {
            this.parentDirectory = parentDirectoryNew;
            firePropertyChange(ProcessingBatchModelInterface.PARENT_DIRECTORY, parentDirectoryOld, parentDirectoryNew);
        }
    }

    private void checkIfContainsMapAreaImages()
    {
        boolean mapAreaImagesAvailableNew = findIfMapAreaImagesAvailable(); 
        boolean mapAreaImagesAvailableOld = this.mapAreaImagesAvailable;

        if(mapAreaImagesAvailableOld != mapAreaImagesAvailableNew)
        {
            this.mapAreaImagesAvailable = mapAreaImagesAvailableNew;

            firePropertyChange(PLOT_MAP_AREA_IMAGES_ENABLED, mapAreaImagesAvailableOld, mapAreaImagesAvailableNew);           
        }
    }

    private boolean findIfContainsForceVolumeData()
    {
        boolean containsForceVolumeDataNew = false;

        for(SimpleSpectroscopySource source : sources)
        {
            containsForceVolumeDataNew = containsForceVolumeDataNew || source.isFromMap();

            if(containsForceVolumeDataNew)
            {
                break;
            }
        }

        return containsForceVolumeDataNew;
    }

    private boolean findIfMapAreaImagesAvailable()
    {
        boolean mapAreaImagesAvailableaNew = false;

        for(SimpleSpectroscopySource source : sources)
        {
            boolean isFromMap = source.isFromMap();

            if(isFromMap)
            {
                MapSource<?> mapSource = source.getForceMap();
                mapAreaImagesAvailableaNew = mapAreaImagesAvailableaNew || mapSource.isMapAreaImagesAvailable();

                if(mapAreaImagesAvailableaNew)
                {
                    break;
                }
            }

        }

        return mapAreaImagesAvailableaNew;
    }


    private void checkIfBasicSettingsSpecified()
    {
        boolean basicSettingsSpecifiedNew = calculateBasicSettingsSpecified();
        boolean basicSettingsSpecifiedOld = basicSettingsSpecified;     

        if(basicSettingsSpecifiedNew != basicSettingsSpecified)
        {
            basicSettingsSpecified = basicSettingsSpecifiedNew;
            firePropertyChange(ProcessingBatchModelInterface.BASIC_SETTINGS_SPECIFIED, basicSettingsSpecifiedOld, basicSettingsSpecifiedNew);
        }
    }   

    private void checkIfSettingsSpecified()
    {
        boolean settingsSpecifiedNew = calculateSettingSpecified();
        boolean settingsSpecifiedOld = this.settingsSpecified;

        if(settingsSpecifiedNew != this.settingsSpecified)
        {
            this.settingsSpecified = settingsSpecifiedNew;
            firePropertyChange(ProcessingBatchModelInterface.SETTINGS_SPECIFIED, settingsSpecifiedOld, settingsSpecifiedNew);
        }   
    }


    private boolean calculateSettingSpecified()
    {
        boolean nameProvied = (batchName != null) && (!batchName.isEmpty());
        boolean basicSettingsSpecified = calculateBasicSettingsSpecified();
        boolean trimmingInputProvided = isCroppingInputProvided();
        boolean smoothingInputProvided = isSmoothingInputProvided();
        boolean substrateCorrectionInputProvided = isSubstrateCorrectionInputProvided();

        boolean settingsSpecified = nameProvied && basicSettingsSpecified 
                && trimmingInputProvided && smoothingInputProvided && substrateCorrectionInputProvided;

        return settingsSpecified;       
    }

    private boolean calculateBasicSettingsSpecified()
    {
        boolean isPoisonRatioSpecified = !Double.isNaN(poissonRatio);
        boolean springConstantShouldBeSpecified = springConstantInputNecessary || !springConstantUseReadIn;
        boolean isSpringConstantSpecified = !(Double.isNaN(springConstant) && springConstantShouldBeSpecified);

        boolean isSensitivitySpecified = calculateSensitivityIsSpecifiedIfNecessary();

        boolean isIndentLimitSpecified = !Double.isNaN(indentationLimit);
        boolean isLoadLimitSpecified = !Double.isNaN(loadLimit);

        boolean isIndenterSizeSpecified = indentationModel.isNecessaryInformationProvided(this);

        boolean basicSettingsSpecified = isPoisonRatioSpecified && isSpringConstantSpecified 
                && isSensitivitySpecified && isIndenterSizeSpecified 
                && isIndentLimitSpecified && isLoadLimitSpecified;  

        return basicSettingsSpecified;
    }

    private boolean calculateSensitivityIsSpecifiedIfNecessary()
    {
        boolean isSensitivitySpecified = true;
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            if(!signalTypes.contains(signalType))
            {
                continue;
            }

            isSensitivitySpecified = isSensitivitySpecified && !(sensitivity.get(signalType).isNaN()
                    &&(sensitivityInputNecessary.get(signalType) || !useReadInSensitivity.get(signalType)));

        }

        return isSensitivitySpecified;
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();

        properties.setProperty(CONTACT_POINT_AUTOMATIC, Boolean.toString(contactPointAutomatic));
        properties.setProperty(AUTOMATIC_CONTACT_ESTIMATOR, automaticContactEstimator.getIdentifier());
        properties.setProperty(REGRESSION_STRATEGY, regressionStrategy.getIdentifier());
        properties.setProperty(FITTED_BRANCH, fittedBranch.getIdentifier());
        properties.setProperty(ADHESIVE_ENERGY_ESTIMATION_METHOD, adhesiveEnergyEstimator.getIdentifier());

        properties.setProperty(POISSON_RATIO, Double.toString(poissonRatio));

        properties.setProperty(INDENTATION_MODEL,indentationModel.getIdentifier());
        properties.setProperty(TIP_RADIUS, Double.toString(tipRadius));
        properties.setProperty(TIP_HALF_ANGLE, Double.toString(tipHalfAngle));
        properties.setProperty(TIP_TRANSITION_RADIUS, Double.toString(tipTransitionRadius));      

        properties.setProperty(TIP_EXPONENT, Double.toString(tipExponent));
        properties.setProperty(TIP_FACTOR, Double.toString(tipFactor));

        properties.setProperty(SPRING_CONSTANT, Double.toString(springConstant));
        properties.setProperty(BASELINE_DEGREE, Integer.toString(baselineDegree));
        properties.setProperty(POSTCONTACT_DEGREE, Integer.toString(postcontactDegree));

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            properties.setProperty(SENSITIVITY + signalType.toString(), sensitivity.get(signalType).toString());
        }
        properties.setProperty(DOMAIN_CROPPED, Boolean.toString(domainCropped));
        properties.setProperty(RANGE_CROPPED, Boolean.toString(rangeCropped));
        properties.setProperty(LEFT_CROPPING, Double.toString(leftCropping));
        properties.setProperty(RIGHT_CROPPING, Double.toString(rightCropping));
        properties.setProperty(UPPER_CROPPING, Double.toString(upperCropping));
        properties.setProperty(LOWER_CROPPING, Double.toString(lowerCropping));
        properties.setProperty(LOAD_LIMIT, Double.toString(loadLimit));
        properties.setProperty(INDENTATION_LIMIT, Double.toString(indentationLimit));
        properties.setProperty(FIT_INDENTATION_LIMIT, Double.toString(fitIndentationLimit));
        properties.setProperty(FIT_Z_MINIMUM, Double.toString(fitZMinimum));
        properties.setProperty(FIT_Z_MAXIMUM, Double.toString(fitZMaximum));

        properties.setProperty(CURVE_SMOOTHED, Boolean.toString(smoothed));
        properties.setProperty(LOESS_SPAN, Double.toString(loessSpan));
        if(loessIterations != null){properties.setProperty(LOESS_ITERATIONS,loessIterations.toString());}
        if(savitzkyDegree != null){properties.setProperty(SAVITZKY_DEGREE, savitzkyDegree.toString());}
        properties.setProperty(SAVITZKY_SPAN, Double.toString(savitzkySpan));
        properties.setProperty(PLOT_RECORDED_CURVE, Boolean.toString(plotRecordedCurve));
        properties.setProperty(PLOT_RECORDED_CURVE_FIT, Boolean.toString(plotRecordedCurveFit));
        properties.setProperty(PLOT_INDENTATION, Boolean.toString(plotIndentation));
        properties.setProperty(PLOT_INDENTATION_FIT, Boolean.toString(plotIndentationFit));
        properties.setProperty(PLOT_MODULUS, Boolean.toString(plotModulus));
        properties.setProperty(PLOT_MODULUS_FIT, Boolean.toString(plotModulusFit));

        properties.setProperty(SHOW_AVERAGED_RECORDED_CURVES, Boolean.toString(showAveragedRecordedCurves));
        properties.setProperty(SHOW_AVERAGED_INDENTATION_CURVES, Boolean.toString(showAveragedIndentationCurves));
        properties.setProperty(SHOW_AVERAGED_POINTWISE_MODULUS_CURVES, Boolean.toString(showAveragedPointwiseModulusCurves));

        properties.setProperty(AVERAGED_RECORDED_CURVES_POINT_COUNT, Integer.toString(averagedRecordedCurvesPointCount));
        properties.setProperty(AVERAGED_INDENTATION_CURVES_POINT_COUNT, Integer.toString(averagedIndentationCurvesPointCount));
        properties.setProperty(AVERAGED_POINTWISE_MODULUS_CURVES_POINT_COUNT, Integer.toString(averagedPointwiseModulusCurvesPointCount));
        
        properties.setProperty(AVERAGED_CURVES_ERROR_BAR_TYPE, this.averagedCurvesBarType.getIdentifier());

        properties.setProperty(CALCULATE_R_SQUARED, Boolean.toString(calculateRSquared));
        properties.setProperty(CALCULATE_ADHESION_FORCE, Boolean.toString(calculateAdhesionForce));

        properties.setProperty(CORRECT_SUBSTRATE_EFFECT, Boolean.toString(correctSubstrateEffect));
        properties.setProperty(THICKNESS_CORRECTION_METHOD, thicknessCorrectionMethod.getIdentifier());
        properties.setProperty(USE_SAMPLE_TOPOGRAPHY, Boolean.toString(useSampleTopography));
        properties.setProperty(SAMPLE_THICKNESS, Double.toString(sampleThickness));
        properties.setProperty(SAMPLE_ADHERENT, Boolean.toString(sampleAdherent));

        return properties;
    } 

    public void loadProperties(Properties properties)
    {
        boolean contactPointAutomatic = FileInputUtilities.parseSafelyBoolean(properties.getProperty(CONTACT_POINT_AUTOMATIC), this.contactPointAutomatic);
        setContactPointAutomatic(contactPointAutomatic);

        AutomaticContactEstimatorType contactEstimator = AutomaticContactEstimatorType.getValue(properties.getProperty(AUTOMATIC_CONTACT_ESTIMATOR), this.automaticContactEstimator);
        setAutomaticContactEstimator(contactEstimator);

        BasicRegressionStrategy regressionStrategy = BasicRegressionStrategy.getValue(properties.getProperty(REGRESSION_STRATEGY), this.regressionStrategy);
        setRegressionStrategy(regressionStrategy);

        ForceCurveBranch branch = ForceCurveBranch.getValue(properties.getProperty(FITTED_BRANCH), this.fittedBranch);
        setFittedBranch(branch);

        ThicknessCorrectionMethod thicknessCorrectionMethod = ThicknessCorrectionMethod.getValue(properties.getProperty(THICKNESS_CORRECTION_METHOD), this.thicknessCorrectionMethod);
        setThicknessCorrectionMethod(thicknessCorrectionMethod);

        AdhesiveEnergyEstimationMethod adhesiveMethod = AdhesiveEnergyEstimationMethod.getValue(properties.getProperty(ADHESIVE_ENERGY_ESTIMATION_METHOD), this.adhesiveEnergyEstimator);
        setAdhesiveEnergyEstimationMethod(adhesiveMethod);

        BasicIndentationModel indentationModel = BasicIndentationModel.getValue(properties.getProperty(INDENTATION_MODEL), this.indentationModel);
        setIndentationModel(indentationModel);

        double poissonRatio = FileInputUtilities.parseSafelyDouble(properties.getProperty(POISSON_RATIO), this.poissonRatio);
        setPoissonRatio(poissonRatio);

        double tipRadius = FileInputUtilities.parseSafelyDouble(properties.getProperty(TIP_RADIUS), this.tipHalfAngle);
        setTipRadius(tipRadius);

        double tipHalfAngle = FileInputUtilities.parseSafelyDouble(properties.getProperty(TIP_HALF_ANGLE), this.tipHalfAngle);
        setTipHalfAngle(tipHalfAngle);

        double tipTransitionRadius = FileInputUtilities.parseSafelyDouble(properties.getProperty(TIP_TRANSITION_RADIUS), this.tipTransitionRadius);
        setTipTransitionRadius(tipTransitionRadius);

        double tipExponent = FileInputUtilities.parseSafelyDouble(properties.getProperty(TIP_EXPONENT), this.tipExponent);
        setTipExponent(tipExponent);

        double tipFactor = FileInputUtilities.parseSafelyDouble(properties.getProperty(TIP_FACTOR), this.tipFactor);
        setTipFactor(tipFactor);

        double springConstant = FileInputUtilities.parseSafelyDouble(properties.getProperty(SPRING_CONSTANT), this.springConstant);
        setSpringConstant(springConstant);

        int baselineDegree = FileInputUtilities.parseSafelyInt(properties.getProperty(BASELINE_DEGREE), this.baselineDegree);
        setBaselineDegree(baselineDegree);

        int postcontactDegree = FileInputUtilities.parseSafelyInt(properties.getProperty(POSTCONTACT_DEGREE), this.postcontactDegree);
        setPostcontactDegree(postcontactDegree);

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            Double sensitivity = FileInputUtilities.parseSafelyDouble(properties.getProperty(SENSITIVITY + signalType.toString()));
            setSensitivity(signalType, sensitivity);
        }

        boolean domainCropped = FileInputUtilities.parseSafelyBoolean(properties.getProperty(DOMAIN_CROPPED), this.domainCropped);
        setDomainCropped(domainCropped);

        boolean rangeCropped = FileInputUtilities.parseSafelyBoolean(properties.getProperty(RANGE_CROPPED), this.rangeCropped);
        setRangeCropped(rangeCropped);

        double leftCropping = FileInputUtilities.parseSafelyDouble(properties.getProperty(LEFT_CROPPING), this.leftCropping);
        setLeftCropping(leftCropping);

        double rightCropping = FileInputUtilities.parseSafelyDouble(properties.getProperty(RIGHT_CROPPING), this.rightCropping);
        setRightCropping(rightCropping);

        double upperCropping = FileInputUtilities.parseSafelyDouble(properties.getProperty(UPPER_CROPPING), this.upperCropping);
        setUpperCropping(upperCropping);

        double lowerCropping = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOWER_CROPPING), this.lowerCropping);
        setLowerCropping(lowerCropping);

        double loadLimit = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOAD_LIMIT), this.loadLimit);
        setLoadLimit(loadLimit);

        double indentationLimit = FileInputUtilities.parseSafelyDouble(properties.getProperty(INDENTATION_LIMIT), this.indentationLimit);
        setIndentationLimit(indentationLimit);

        double fitIndentationLimit = FileInputUtilities.parseSafelyDouble(properties.getProperty(FIT_INDENTATION_LIMIT), this.fitIndentationLimit);
        setFitIndentationLimit(fitIndentationLimit);

        double fitZMinimum = FileInputUtilities.parseSafelyDouble(properties.getProperty(FIT_Z_MINIMUM), this.fitZMinimum);
        setFitZMinimum(fitZMinimum);

        double fitZMaximum = FileInputUtilities.parseSafelyDouble(properties.getProperty(FIT_Z_MAXIMUM), this.fitZMaximum);
        setFitZMaximum(fitZMaximum);

        boolean smoothed = FileInputUtilities.parseSafelyBoolean(properties.getProperty(CURVE_SMOOTHED), this.smoothed);
        setDataSmoothed(smoothed);

        double loessSpan = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOESS_SPAN), this.loessSpan);
        setLoessSpan(loessSpan);

        Double loessIterations = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOESS_ITERATIONS));
        setLoessIterations(loessIterations);

        Double savitzkyDegree = FileInputUtilities.parseSafelyDouble(properties.getProperty(SAVITZKY_DEGREE));
        setSavitzkyDegree(savitzkyDegree);

        double savitzkySpan = FileInputUtilities.parseSafelyDouble(properties.getProperty(SAVITZKY_SPAN), this.savitzkySpan);
        setSavitzkySpan(savitzkySpan);

        boolean ploRecordedCurve = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_RECORDED_CURVE), this.plotRecordedCurve);
        setPlotRecordedCurve(ploRecordedCurve);

        boolean plotRecordedCurveFit = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_RECORDED_CURVE_FIT), this.plotRecordedCurveFit);
        setPlotRecordedCurveFit(plotRecordedCurveFit);

        boolean plotIndentation = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_INDENTATION), this.plotIndentation);
        setPlotIndentation(plotIndentation);

        boolean plotIndentationFit = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_INDENTATION_FIT), this.plotIndentationFit);
        setPlotIndentationFit(plotIndentationFit);

        boolean plotModulus = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_MODULUS), this.plotModulus);
        setPlotModulus(plotModulus);

        boolean plotModulusFit = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_MODULUS_FIT), this.plotModulusFit);
        setPlotModulusFit(plotModulusFit);


        boolean showAveragedRecordedCurves = FileInputUtilities.parseSafelyBoolean(properties.getProperty(SHOW_AVERAGED_RECORDED_CURVES), this.showAveragedRecordedCurves);
        setShowAveragedRecordedCurves(showAveragedRecordedCurves);

        boolean showAveragedIndentationCurves = FileInputUtilities.parseSafelyBoolean(properties.getProperty(SHOW_AVERAGED_INDENTATION_CURVES), this.showAveragedIndentationCurves);
        setShowAveragedIndentationCurves(showAveragedIndentationCurves);

        boolean showAveragedPointwiseModulusCurves = FileInputUtilities.parseSafelyBoolean(properties.getProperty(SHOW_AVERAGED_POINTWISE_MODULUS_CURVES), this.showAveragedPointwiseModulusCurves);
        setShowAveragedPointwiseModulusCurves(showAveragedPointwiseModulusCurves);

        int averagedRecordedCurvesPointCount = FileInputUtilities.parseSafelyInt(properties.getProperty(AVERAGED_RECORDED_CURVES_POINT_COUNT), this.averagedRecordedCurvesPointCount);
        setAveragedRecordedCurvesPointCount(averagedRecordedCurvesPointCount);

        int averagedIndentationCurvesPointCount = FileInputUtilities.parseSafelyInt(properties.getProperty(AVERAGED_INDENTATION_CURVES_POINT_COUNT), this.averagedIndentationCurvesPointCount);
        setAveragedIndentationCurvesPointCount(averagedIndentationCurvesPointCount);

        int averagedPointwiseModulusCurvesPointCount = FileInputUtilities.parseSafelyInt(properties.getProperty(AVERAGED_POINTWISE_MODULUS_CURVES_POINT_COUNT), this.averagedPointwiseModulusCurvesPointCount);
        setAveragedPointwiseModulusCurvesPointCount(averagedPointwiseModulusCurvesPointCount);

        
        ErrorBarType averagedCurvesBarType = ErrorBarType.getValue(properties.getProperty(AVERAGED_CURVES_ERROR_BAR_TYPE), this.averagedCurvesBarType);
        setAveragedCurvesBarType(averagedCurvesBarType);

        boolean calculateRSquared = FileInputUtilities.parseSafelyBoolean(properties.getProperty(CALCULATE_R_SQUARED), this.calculateRSquared);
        setCalculateRSquared(calculateRSquared);

        boolean calculateAdhesionForce = FileInputUtilities.parseSafelyBoolean(properties.getProperty(CALCULATE_ADHESION_FORCE), this.calculateAdhesionForce);
        setCalculateAdhesionForce(calculateAdhesionForce);

        boolean correctSubstrateEffect = FileInputUtilities.parseSafelyBoolean(properties.getProperty(CORRECT_SUBSTRATE_EFFECT), this.correctSubstrateEffect);
        setCorrectSubstrateEffect(correctSubstrateEffect);

        boolean useSampleTopography = FileInputUtilities.parseSafelyBoolean(properties.getProperty(USE_SAMPLE_TOPOGRAPHY), this.useSampleTopography);
        setUseSampleTopography(useSampleTopography);

        double sampleThickness = FileInputUtilities.parseSafelyDouble(properties.getProperty(SAMPLE_THICKNESS), this.sampleThickness);
        setSampleThickness(sampleThickness);

        boolean sampleAdherent = FileInputUtilities.parseSafelyBoolean(properties.getProperty(SAMPLE_ADHERENT), this.sampleAdherent);
        setSampleAdherent(sampleAdherent);
    }

    private void flushPreferences()
    {
        try
        {
            PREF.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }
}
