
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

public final class ProcessingModelProperties 
{
    static final String BATCH_NAME = "BatchName";
    static final String SAVITZKY_GOLAY = "SavitzkyGolay";
    static final String LOCAL_REGRESSION = "LocalRegression";

    public static final String SOURCES = "Sources";
    public static final String RESOURCES = "Resources";
    public static final String CHOSEN_RESOURCE = "ChosenResource";
    public static final String RESOURCE_IS_CHOSEN = "ResourceIsChosen";
    public static final String RESOURCE_IDENTIFIERS = "ResourceIdentifiers";

    public static final String SELECTED_ROIS = "SelectedRois";

    static final String PREVIEW_ENABLED = "PreviewEnabled";
    static final String BACK_ENABLED = "BackEnabled";
    static final String NEXT_ENABLED = "NextEnabled";
    static final String FINISH_ENABLED = "FinishEnabled";
    static final String NEXT_BATCH_ENABLED = "NextBatchEnabled";
    static final String PREVIOUS_BATCH_ENABLED = "PreviousBatchEnabled";
    static final String FIRST_PAGE = "FirstPage";
    static final String LAST_PAGE = "LastPage";
    static final String CURRENT_PAGE = "CurrentPage";

    static final String CONTAINS_FORCE_VOLUME_DATA =  "ContainsForceVolumeData";
    static final String SETTINGS_SPECIFIED = "SettingsSpecified";
    static final String BASIC_SETTINGS_SPECIFIED = "BasicSettingsSpecified";
    static final String TRIMMING_ON_CURVE_SELECTION_POSSIBLE = "TrimmingOnCurveSelectionPossible";
    public static final String SOURCES_SELECTED = "SourcesSelected";
    public static final String INPUT_PROVIDED = "InputProvided";
    public static final String FILTERING_POSSIBLE = "FilteringPossible";
    static final String FILTERED_OUT_SOURCES = "FilteredOutSources";

    public static final String CURRENT_BATCH_NUMBER = "CurrentBatchNumber";

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

    static final String DOMAIN_TRIMMED = "DomainTrimmed";
    static final String RANGE_TRIMMED = "RangeTrimmed";
    static final String LEFT_TRIMMING = "LeftTrimming";
    static final String RIGHT_TRIMMING = "RightTrimming";
    static final String UPPER_TRIMMING = "UpperTrimming";
    static final String LOWER_TRIMMING = "LowerTrimming";
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

    static final String INCLUDE_IN_MAPS = "IncludeInMaps";
    static final String INCLUDE_IN_MAPS_ENABLED = "IncludeInMapsEnabled";

    static final String PLOT_MAP_AREA_IMAGES = "PlotMapAreaImages";
    static final String PLOT_MAP_AREA_IMAGES_ENABLED = "PlotMapAreaImagesEnabled";

    static final String PROCESSED_BATCHES = "ProcessedBatches";

    public static final String PARENT_DIRECTORY = "ParentDirectory";

    private ProcessingModelProperties() {}
}
