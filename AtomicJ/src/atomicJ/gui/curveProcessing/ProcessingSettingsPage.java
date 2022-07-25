
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 - 2022 by Paweł Hermanowicz
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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.analysis.AutomaticContactEstimatorType;
import atomicJ.analysis.BasicRegressionStrategy;
import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.analysis.indentation.AdhesiveEnergyEstimationMethod;
import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.curveProcessing.SpanType;
import atomicJ.gui.AbstractWizardPage;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.ForceCurvePlotFactory;
import atomicJ.gui.JSpinnerNumeric;
import atomicJ.gui.NumericalField;
import atomicJ.gui.SpinnerDoubleModel;
import atomicJ.gui.SubPanel;
import atomicJ.gui.TopographySelectionWizard;
import atomicJ.gui.WizardPage;
import atomicJ.sources.ChannelSource;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.utilities.IOUtilities;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;


import static atomicJ.gui.NumericalField.VALUE_EDITED;
import static atomicJ.gui.curveProcessing.CalibrationModel.AVAILABLE_FORCE_CURVE_BRANCHES;
import static atomicJ.gui.curveProcessing.ProcessingBatchModel.*;

public class ProcessingSettingsPage extends AbstractWizardPage implements WizardPage
{
    private static final String IDENTIFIER = "Settings";
    private static final String TASK_NAME = "Specify processing settings";
    private static final String DESCRIPTION = "All settings in the general tab are mandatory";

    private static final String CORRECTION_KNOWN = "Correct substrate effect";
    private static final String CORRECTION_UNKNOWN = "Correct substrate effect (correction unknown)";

    private static final String OUTPUT_TAB = "Output";
    private static final String ADVANCED_TAB = "Advanced";
    private static final String GENERAL_TAB = "General";

    private final Action trimmingAction = new CroppingSpecificationAction();
    private final Action smoothTipTransitionAction = new SmoothTipTransitionAction();
    private final Action browseForTopographyAction = new BrowseForSampleTopographyAction();

    private final Map<PhotodiodeSignalType, Action> calibrateActions = new EnumMap<>(PhotodiodeSignalType.class);
    private final Map<PhotodiodeSignalType, JButton> buttonsCalibrate = new EnumMap<>(PhotodiodeSignalType.class);
    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            Action calibrateAction = new CalibrateAction(signalType);
            calibrateActions.put(signalType, calibrateAction);

            JButton buttonCalibrate = new JButton(calibrateAction);
            buttonsCalibrate.put(signalType, buttonCalibrate);
        }
    }

    private final JLabel labelBatchNumber = new JLabel();
    private final JLabel labelContactEstimator = new JLabel("Contact estimator");
    private final JLabel labelContactEstimationMethod = new JLabel("Estimation method");

    private final JLabel labelPostcontactDegree = new JLabel("In-contact degree");

    private final JLabel labelThicknessCorrectionMethod = new JLabel("Method");
    private final JLabel labelTopographyFile = new JLabel("Topography image");
    private final JLabel labelThickness = new JLabel("Thickness (μm)");
    private final JLabel labelTipRadius = new JLabel("Radius (μm)");
    private final JLabel labelTipAngle = new JLabel("Half-angle (°)");
    private final JLabel labelTipExponent = new JLabel("Exponent");
    private final JLabel labelTipFactor = new JLabel("Factor");

    private final JLabel labelTipTransitionRadius = new JLabel("Transition radius (μm)");
    private final JLabel labelAdhesiveEnergy = new JLabel("Adhesive energy");

    private final JComboBox<AutomaticContactEstimatorType> comboContactEstimator = new JComboBox<>(AutomaticContactEstimatorType.values()); 
    private final JComboBox<ContactEstimationMethod> comboContactEstimationMethod = new JComboBox<>(ContactEstimationMethod.values());
    private final JComboBox<BasicRegressionStrategy> comboRegressionStrategy = new JComboBox<>(BasicRegressionStrategy.values()); 
    private final JComboBox<ForceCurveBranch> comboFittedBranch = new JComboBox<>(ForceCurveBranch.values()); 
    private final JComboBox<AdhesiveEnergyEstimationMethod> comboAdhesiveEnergy = new JComboBox<>(AdhesiveEnergyEstimationMethod.values()); 

    private final JComboBox<BasicIndentationModel> comboIndentationModel = new JComboBox<>(BasicIndentationModel.values()); 
    private final JComboBox<SmootherType> comboSmoothers = new JComboBox<>(SmootherType.values());	
    private final JComboBox<ThicknessCorrectionMethod> comboThicknessCorrectionType = new JComboBox<>(ThicknessCorrectionMethod.values()); 

    private final JSpinner spinnerBaselineDegree = new JSpinner(new SpinnerNumberModel(1,0,Integer.MAX_VALUE,1));
    private final JSpinner spinnerPostcontactDegree = new JSpinner(new SpinnerNumberModel(2,0,Integer.MAX_VALUE,1));

    private final JCheckBox boxPlotRecordedCurve = new JCheckBox("Show");
    private final JCheckBox boxPlotIndentation = new JCheckBox("Show");
    private final JCheckBox boxPlotModulus = new JCheckBox("Show");	

    private final JCheckBox boxPlotRecordedCurveFit = new JCheckBox("Plot fit");
    private final JCheckBox boxPlotIndentationFit = new JCheckBox("Plot fit");
    private final JCheckBox boxPlotModulusFit = new JCheckBox("Plot fit");

    private final JCheckBox boxShowAveragedRecordedCurve = new JCheckBox("Recorded curve");
    private final JCheckBox boxShowAveragedIndentation = new JCheckBox("Indentation");
    private final JCheckBox boxShowAveragedPointwiseModulus = new JCheckBox("Pointwise modulus");

    private final JComboBox<ErrorBarType> comboErrorBarType = new JComboBox<>(ErrorBarType.values());

    private final JCheckBox boxIncludeCurvesInMaps = new JCheckBox("Include in maps");
    private final JCheckBox boxPlotMapAreaImages = new JCheckBox("Plot map area scans");

    private final JCheckBox boxCalculateRSquared = new JCheckBox("Calculate R Squared");
    private final JCheckBox boxCalculateAdhesionForce = new JCheckBox("Calculate adhesion force");

    private final JCheckBox boxTrimDomain = new JCheckBox("Crop domain");
    private final JCheckBox boxTrimRange = new JCheckBox("Crop range");
    private final JCheckBox boxSmooth = new JCheckBox("Smooth");
    private final JCheckBox boxCorrectSubstrate = new JCheckBox();
    private final JCheckBox boxUseTopography = new JCheckBox("Use topography");
    private final JCheckBox boxSampleAdherent = new JCheckBox("Adherent");
    private final JCheckBox boxUseReadInSpringConstant = new JCheckBox("Read-in");

    private final EnumMap<PhotodiodeSignalType, JCheckBox> boxesUseReadInSensitivity = new EnumMap<>(PhotodiodeSignalType.class);
    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            JCheckBox boxUseReadInSensitivity = new JCheckBox("Read-in");
            boxesUseReadInSensitivity.put(signalType, boxUseReadInSensitivity);
        }
    }      

    private final JRadioButton buttonAutomatic = new JRadioButton();
    private final JRadioButton buttonManual = new JRadioButton();
    private final ButtonGroup buttonGroupContactEstimator = new ButtonGroup();

    private final JButton buttonImport = new JButton(new ImportAction());
    private final JButton buttonExport = new JButton(new ExportAction());
    private final JButton buttonSelectTrimming = new JButton(trimmingAction);
    private final JButton buttonLoadInfinity = new JButton(new LoadToInfinityAction());
    private final JButton buttonIndentInfinity = new JButton(new IndentationToInfinityAction());
    private final JButton buttonBrowseTopography = new JButton(browseForTopographyAction);
    private final JButton buttonSmoothTransitionRadius = new JButton(smoothTipTransitionAction);

    private final JFormattedTextField fieldName = new JFormattedTextField(new DefaultFormatter());
    private final JFormattedTextField fieldTopographyFile = new JFormattedTextField(new DefaultFormatter());

    private final JSpinnerNumeric fieldTipRadius =new JSpinnerNumeric(new SpinnerDoubleModel("Tip radius must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldTipAngle = new JSpinnerNumeric(new SpinnerDoubleModel("Tip angle must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));

    private final JSpinnerNumeric fieldTipExponent = new JSpinnerNumeric(new SpinnerDoubleModel("Tip exponent must be a positive number",Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldTipFactor = new JSpinnerNumeric(new SpinnerDoubleModel("Tip factor must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));

    private final JSpinnerNumeric fieldTipTransitionRadius = new JSpinnerNumeric(new SpinnerDoubleModel("Tip transition radius must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldSpringConstant = new JSpinnerNumeric(new SpinnerDoubleModel("Spring constant must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));	
    private final JSpinnerNumeric fieldTrimLeft = new JSpinnerNumeric(new SpinnerDoubleModel("Left cropping must be a nonnegative number", Double.NaN, 0, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldTrimRight = new JSpinnerNumeric(new SpinnerDoubleModel("Right cropping must be a nonnegative number", Double.NaN, 0, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldTrimLower = new JSpinnerNumeric(new SpinnerDoubleModel("Lower cropping must be a nonnegative number", Double.NaN, 0, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldTrimUpper = new JSpinnerNumeric(new SpinnerDoubleModel("Upper cropping must be a nonnegative number", Double.NaN, 0, Double.MAX_VALUE, 1.0));	
    private final JSpinnerNumeric fieldPoissonRatio = new JSpinnerNumeric(new SpinnerDoubleModel("Poisson ratio must fall between -1 and 0.5",Double.NaN, -1, 0.5, 0.1));
    private final JSpinnerNumeric fieldLoadMaximum = new JSpinnerNumeric(new SpinnerDoubleModel("Load maximum must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));
    private final JSpinnerNumeric fieldIndentMaximum = new JSpinnerNumeric(new SpinnerDoubleModel("Indentation maximum must be a positive number", Double.NaN, Double.MIN_VALUE, Double.MAX_VALUE, 1.0));
    private final NumericalField fieldSavitzkySpan = new NumericalField("Span must be a nonnegative integer", 0, false);
    private final NumericalField fieldSavitzkyDegree = new NumericalField("Degree must be a nonnegative integer", 0, false);	
    private final JSpinnerNumeric fieldLoessSpan = new JSpinnerNumeric(new SpinnerDoubleModel("Span must be a positive number between 0 and 100", Double.NaN, Double.MIN_VALUE, 100, 1.0));
    private final NumericalField fieldIterLocal = new NumericalField("Iteration numbers must be a nonnegative integer", 0, false);
    private final JSpinnerNumeric fieldSampleThickness = new JSpinnerNumeric(new SpinnerDoubleModel("Sample thickness must be a positive number",  Double.NaN, Double.MIN_VALUE, 100, 1.0));

    private final EnumMap<PhotodiodeSignalType, JSpinnerNumeric> fieldsSensitivity = new EnumMap<>(PhotodiodeSignalType.class);
    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            JSpinnerNumeric fieldSensitivity = new JSpinnerNumeric(new SpinnerDoubleModel("Sensitivity must be a positive number",Double.NaN, Double.MIN_VALUE,Double.MAX_VALUE, 1.0));
            fieldsSensitivity.put(signalType, fieldSensitivity);
        }
    }

    private final EnumMap<PhotodiodeSignalType, JLabel> labelsSensitivity = new EnumMap<>(PhotodiodeSignalType.class);

    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            JLabel labelSensitivity = new JLabel("InvOLS (" + signalType.getDefaultUnit().getFullName() + ")");
            labelsSensitivity.put(signalType, labelSensitivity);
        }
    }

    //jumps

    private final JCheckBox boxFindJumps = new JCheckBox("Find jumps");
    private final JSpinner spinnerJumpsSpan = new JSpinner(new SpinnerNumberModel(0., 0., 10000., 1.));
    private final JSpinner spinnerJumpsPolynomialDegree = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spinnerJumpsMinDistanceFromContact = new JSpinner(new SpinnerNumberModel(0., 0., 10000., 1.));

    private final JComboBox<SpanType> comboJumpsSpanType = new JComboBox<>(SpanType.values());
    private final JComboBox<LocalRegressionWeightFunction> comboJumpsRegressionWeights = new JComboBox<>(LocalRegressionWeightFunction.values());

    //jumps

    private final ExtensionFileChooser propertiesChooser = new ExtensionFileChooser(Preferences.userRoot().node(getClass().getName()), "Properties file (.properties)", "properties", true);

    private final CalibrationDialog calibrationDialog;
    private final CroppingDialog croppingDialog;

    private final TopographySelectionWizard substrateSelectionWizard;

    private ProcessingModel model;

    private final JPanel panelControls;

    private boolean settingsSpecified;
    private boolean basicSettingsSpecified;

    private final JPanel mainPanel = new JPanel();
    private final PropertyChangeListener modelListener;

    public ProcessingSettingsPage(ProcessingModel model)
    {	
        this.model = model;
        this.calibrationDialog = new CalibrationDialog(model);
        this.croppingDialog = new CroppingDialog(model.getResultDestination().getPublicationSite());
        this.modelListener = buildModelListener();
        this.model.addPropertyChangeListener(modelListener);
        this.substrateSelectionWizard = new TopographySelectionWizard("Sample topography assistant", model.getPreviewDestination(), model);

        pullModelProperties();		
        initNumericFieldListeners();
        initTextFieldListener();
        initItemListener();
        initChangeListener();

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel generalPanel = buildGeneralPanel();
        JPanel advancedPanel = buildAdvancedPanel();
        JPanel outputPanel = buildOutputPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(generalPanel, GENERAL_TAB);
        tabbedPane.add(advancedPanel, ADVANCED_TAB);
        tabbedPane.add(outputPanel, OUTPUT_TAB);

        panelControls = buildControlPanel();

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);	
    }

    public void setProcessingModel(ProcessingModel modelNew)
    {
        if(model != null)
        {
            model.removePropertyChangeListener(modelListener);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(modelListener);

        substrateSelectionWizard.setROIImageReceiver(model.getPreviewDestination(), model);
        calibrationDialog.setModel(modelNew);

        pullModelProperties();
    }

    public ProcessingModel getModel()
    {
        return model;
    }

    private void pullModelProperties()
    {
        settingsSpecified = model.areSettingsSpecified();
        basicSettingsSpecified = model.areBasicSettingsSpecified();

        boolean trimmingOnCurvePossible = model.isTrimmingOnCurveSelectionPossible();

        String batchName = model.getCurrentBatchName();
        int baselineDegree = model.getBaselineDegree();
        int postcontactDegree = model.getPostcontactDegree();

        boolean smoothed = model.areDataSmoothed();
        boolean domainTrimmed = model.isDomainToBeCropped();
        boolean rangeTrimmed = model.isRangeToBeCropped();	

        boolean plotRecordedCurve = model.isPlotRecordedCurve();
        boolean plotRecordedCurveFit = model.isPlotRecordedCurveFit();
        boolean plotIndentation = model.isPlotIndentation();
        boolean plotIndentationFit = model.isPlotIndentationFit();
        boolean plotModulus = model.isPlotModulus();	
        boolean plotModulusFit = model.isPlotModulusFit();

        boolean includeInMapsEnabled = model.isIncludeCurvesInMapsEnabled();
        boolean includeInMaps = model.isIncludeCurvesInMaps();

        boolean calculateRSquared = model.isCalculateRSquared();
        boolean calculateAdhesionForce = model.isCalculateAdhesionForce();

        boolean plotImages = model.isPlotMapAreaImages();
        boolean plotImagesEnabled = model.isPlotMapAreaImagesEnabled();

        boolean automatic = model.isContactPointAutomatic();

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            boolean sensitivityInputEnabled = model.isSensitivityInputEnabled(signalType);
            boolean sensitivityReadInEnabled = model.isSensitivityReadInEnabled(signalType);
            boolean useReadInSensitivity = model.getUseReadInSensitivity(signalType);
            double sensitivity = model.getSensitivity(signalType);

            JCheckBox boxUseReadInSensitivity = boxesUseReadInSensitivity.get(signalType);
            boxUseReadInSensitivity.setSelected(useReadInSensitivity);
            boxUseReadInSensitivity.setEnabled(sensitivityReadInEnabled);

            JSpinnerNumeric fieldSensitivity = fieldsSensitivity.get(signalType);
            fieldSensitivity.setValue(sensitivity);

            setSensitivityInputEnabled(signalType, sensitivityInputEnabled);
        }

        Set<PhotodiodeSignalType> signalPresent = model.getSensitivityPhotodiodeSignalTypes();
        setSensitivityPanelConsistentWithPhotodiodeSignals(signalPresent);

        boolean springConstantInputEnabled = model.isSpringConstantInputEnabled();
        boolean springConstantReadInEnabled = model.isSpringConstantReadInEnabled();
        boolean useReadInSpringConstant = model.getUseReadInSpringConstant();

        AutomaticContactEstimatorType automaticContactEstimator = model.getAutomaticContactEstimator();
        ContactEstimationMethod contactEstimationMethod = model.getContactEstimationMethod();
        BasicRegressionStrategy regressionStrategy = model.getRegressionStrategy();
        ForceCurveBranch fittedBranch = model.getFittedBranch();
        Set<ForceCurveBranch> availableBranches = model.getAvailableBranches();
        AdhesiveEnergyEstimationMethod adhesiveEnergyMethod = model.getAdhesiveEnergyEstimationMethod();
        BasicIndentationModel indentationModel = model.getIndentationModel();	
        SmootherType smootherType = model.getSmootherType();

        boolean postcontactDegreeEnabled = model.isPostcontactDegreeInputEnabled();

        double savitzkyDegree = model.getSavitzkyDegree().doubleValue();
        double iterations = model.getLoessIterations().doubleValue();
        double poissonRatio = model.getPoissonRatio();
        double springConstant = model.getSpringConstant();
        double tipRadius = model.getTipRadius();
        double tipHalfAngle = model.getTipHalfAngle();
        double tipTransitionRadius = model.getTipTransitionRadius();

        double tipExponent = model.getTipExponent();
        double tipFactor = model.getTipFactor();

        double lowerTrimming = model.getLowerCropping();
        double upperTrimming = model.getUpperCropping();
        double rightTrimming = model.getRightCropping();
        double leftTrimming = model.getLeftCropping();	
        double indentationLimit = model.getIndentationLimit();
        double loadLimit = model.getLoadLimit();
        double span = model.getLoessSpan(); 
        double savitzkyWindow = model.getSavitzkySpan();

        boolean smoothTransitionCalculable = model.isTipSmoothTransitionRadiusCalculable();
        boolean adhesiveEnergyRequired = model.isAdhesiveEnergyRequired();
        boolean substrateCorrectionKnown = model.isSubstrateCorrectionKnown();
        boolean correctSubstrate = model.getCorrectSubstrateEffect();
        boolean useTopography = model.getUseSampleTopography();
        boolean sampleAdherent = model.isSampleAdherent();
        double sampleThickness = model.getSampleThickness();
        ThicknessCorrectionMethod thicknessCorrectionMethod = model.getThicknessCorrectionMethod();
        Set<ThicknessCorrectionMethod> applicableThicknessCorrectionMethods = model.getApplicableThicknessCorrectionMethods();
        File topographyFile = model.getSampleTopographyFile();

        Integer batchNumber = model.getCurrentBatchNumber();

        File sourceParentDir = model.getCommonSourceDirectory();

        fieldName.setValue(batchName);

        spinnerBaselineDegree.setValue(baselineDegree);	
        spinnerPostcontactDegree.setValue(postcontactDegree); 
        spinnerPostcontactDegree.setEnabled(postcontactDegreeEnabled);
        labelPostcontactDegree.setEnabled(postcontactDegreeEnabled);

        boxPlotRecordedCurve.setSelected(plotRecordedCurve);
        boxPlotRecordedCurveFit.setSelected(plotRecordedCurveFit);
        boxPlotRecordedCurveFit.setEnabled(plotRecordedCurve);

        boolean showAveragedRecordedCurve = model.isShowAveragedRecordedCurves();
        boolean showAveragedIndentationCurve = model.isShowAveragedIndentationCurves();
        boolean showAveragedPointwiseModulusCurve = model.isShowAveragedPointwiseModulusCurves();
        ErrorBarType averagedCurvesErrorBarType = model.getAveragedCurvesBarType();

        boxShowAveragedRecordedCurve.setSelected(showAveragedRecordedCurve);
        boxShowAveragedIndentation.setSelected(showAveragedIndentationCurve);
        boxShowAveragedPointwiseModulus.setSelected(showAveragedPointwiseModulusCurve);

        comboErrorBarType.setSelectedItem(averagedCurvesErrorBarType);

        boxPlotIndentation.setSelected(plotIndentation);
        boxPlotIndentationFit.setSelected(plotIndentationFit);
        boxPlotIndentationFit.setEnabled(plotIndentation);

        boxPlotModulus.setSelected(plotModulus);
        boxPlotModulusFit.setSelected(plotModulusFit);
        boxPlotModulusFit.setEnabled(plotModulus);

        boxPlotMapAreaImages.setSelected(plotImages);
        boxPlotMapAreaImages.setEnabled(plotImagesEnabled);

        boxIncludeCurvesInMaps.setSelected(includeInMaps);
        boxIncludeCurvesInMaps.setEnabled(includeInMapsEnabled);

        boxCalculateRSquared.setSelected(calculateRSquared);
        boxCalculateAdhesionForce.setSelected(calculateAdhesionForce);

        boxTrimDomain.setSelected(domainTrimmed);
        boxTrimRange.setSelected(rangeTrimmed);
        boxSmooth.setSelected(smoothed);
        boxCorrectSubstrate.setSelected(correctSubstrate);
        boxUseTopography.setSelected(useTopography);
        boxSampleAdherent.setSelected(sampleAdherent);

        boxUseReadInSpringConstant.setSelected(useReadInSpringConstant);
        boxUseReadInSpringConstant.setEnabled(springConstantReadInEnabled);

        smoothTipTransitionAction.setEnabled(smoothTransitionCalculable);
        buttonAutomatic.setSelected(automatic);
        trimmingAction.setEnabled(trimmingOnCurvePossible);

        fieldTipRadius.setValue(tipRadius);
        fieldTipAngle.setValue(tipHalfAngle);
        fieldTipTransitionRadius.setValue(tipTransitionRadius);

        fieldTipExponent.setValue(tipExponent);
        fieldTipFactor.setValue(tipFactor);

        fieldSpringConstant.setValue(springConstant);	
        fieldTrimLeft.setValue(leftTrimming);
        fieldTrimRight.setValue(rightTrimming);
        fieldTrimLower.setValue(lowerTrimming);
        fieldTrimUpper.setValue(upperTrimming);	
        fieldSpringConstant.setEnabled(springConstantInputEnabled);

        fieldPoissonRatio.setValue(poissonRatio);
        fieldLoadMaximum.setValue(loadLimit);;
        fieldIndentMaximum.setValue(indentationLimit);
        fieldSavitzkySpan.setValue(savitzkyWindow);
        fieldSavitzkyDegree.setValue(savitzkyDegree);	
        fieldLoessSpan.setValue(span);
        fieldIterLocal.setValue(iterations);
        fieldSampleThickness.setValue(sampleThickness);
        fieldTopographyFile.setValue(topographyFile);

        comboContactEstimator.setSelectedItem(automaticContactEstimator);
        comboContactEstimationMethod.setSelectedItem(contactEstimationMethod);
        comboRegressionStrategy.setSelectedItem(regressionStrategy);
        comboFittedBranch.setModel(new DefaultComboBoxModel<>(availableBranches.toArray(new ForceCurveBranch[] {})));
        comboFittedBranch.setSelectedItem(fittedBranch);
        comboAdhesiveEnergy.setSelectedItem(adhesiveEnergyMethod);
        comboIndentationModel.setSelectedItem(indentationModel);
        comboSmoothers.setSelectedItem(smootherType);
        comboThicknessCorrectionType.setModel(new DefaultComboBoxModel<>(applicableThicknessCorrectionMethods.toArray(new ThicknessCorrectionMethod[] {})));
        comboThicknessCorrectionType.setSelectedItem(thicknessCorrectionMethod);

        labelBatchNumber.setText(batchNumber.toString());

        calibrationDialog.setChooserSelectedFile(sourceParentDir);

        //sets the interface controls consistent with the state

        fieldTrimLeft.setEnabled(domainTrimmed);
        fieldTrimRight.setEnabled(domainTrimmed);
        fieldTrimLower.setEnabled(rangeTrimmed);
        fieldTrimUpper.setEnabled(rangeTrimmed);

        //jumps

        boxFindJumps.setSelected(model.getFindJumps());
        spinnerJumpsMinDistanceFromContact.setValue(model.getJumpMinDistanceFromContact());
        spinnerJumpsSpan.setValue(model.getJumpsSpan());
        spinnerJumpsPolynomialDegree.setValue(model.getJumpsPolynomialDegree());
        comboJumpsSpanType.setSelectedItem(model.getJumpsSpanType());
        comboJumpsRegressionWeights.setSelectedItem(model.getJumpsWeightFunction());

        //jumps

        setConsistentWithIndentationModel(indentationModel);
        setAdhesiveEnergyRequired(adhesiveEnergyRequired);
        setSubstrateCorrectionKnown(substrateCorrectionKnown);
        setSubstrateCorrectionUsed(correctSubstrate, useTopography);
    }

    private void setSubstrateCorrectionKnown(boolean substrateCorrectionKnown)
    {
        boxCorrectSubstrate.setEnabled(substrateCorrectionKnown);
        String correctSubstrateLabel = substrateCorrectionKnown ? CORRECTION_KNOWN : CORRECTION_UNKNOWN;
        boxCorrectSubstrate.setText(correctSubstrateLabel);

        setSubstrateCorrectionUsed(substrateCorrectionKnown && model.getCorrectSubstrateEffect(), model.getUseSampleTopography());
    }

    private void setAdhesiveEnergyRequired(boolean adhesiveEnergyRequired)
    {
        comboAdhesiveEnergy.setEnabled(adhesiveEnergyRequired);
        labelAdhesiveEnergy.setEnabled(adhesiveEnergyRequired);
    }

    private void setSubstrateCorrectionUsed(boolean correctSubstrate, boolean useTopography)
    {
        fieldTopographyFile.setEnabled(correctSubstrate && useTopography);
        fieldSampleThickness.setEnabled(correctSubstrate && !useTopography);
        boxSampleAdherent.setEnabled(correctSubstrate);
        boxUseTopography.setEnabled(correctSubstrate);
        browseForTopographyAction.setEnabled(correctSubstrate && useTopography);   
        labelTopographyFile.setEnabled(correctSubstrate && useTopography);
        labelThickness.setEnabled(correctSubstrate);
        labelThicknessCorrectionMethod.setEnabled(correctSubstrate);
        comboThicknessCorrectionType.setEnabled(correctSubstrate);
    }

    private void setSensitivityInputEnabled(PhotodiodeSignalType signalType, boolean sensitivityEnabled)
    {
        JSpinnerNumeric fieldSensitivity = fieldsSensitivity.get(signalType);
        fieldSensitivity.setEnabled(sensitivityEnabled);

        Action calibrateAction = calibrateActions.get(signalType);
        calibrateAction.setEnabled(sensitivityEnabled);
    }

    private void setConsistentWithIndentationModel(BasicIndentationModel indentationModel)
    {
        boolean radiusNeeded = indentationModel.isTipParameterNeeded(TipShapeParameter.RADIUS);
        boolean angleNeeded = indentationModel.isTipParameterNeeded(TipShapeParameter.HALF_ANGLE);
        boolean transitionRadiusNeeded = indentationModel.isTipParameterNeeded(TipShapeParameter.TRANSITION_RADIUS);

        boolean exponentNeeded = indentationModel.isTipParameterNeeded(TipShapeParameter.EXPONENT);
        boolean factorNeeded = indentationModel.isTipParameterNeeded(TipShapeParameter.FACTOR);

        //tip radius
        fieldTipRadius.setVisible(!(!radiusNeeded && exponentNeeded));
        labelTipRadius.setVisible(!(!radiusNeeded && exponentNeeded));

        fieldTipRadius.setEnabled(radiusNeeded);
        labelTipRadius.setEnabled(radiusNeeded);

        //tip half angle
        fieldTipAngle.setVisible(!(!angleNeeded && factorNeeded));
        labelTipAngle.setVisible(!(!angleNeeded && factorNeeded));

        fieldTipAngle.setEnabled(angleNeeded);
        labelTipAngle.setEnabled(angleNeeded);

        //tip exponent
        labelTipExponent.setVisible(exponentNeeded);
        fieldTipExponent.setVisible(exponentNeeded);

        fieldTipExponent.setEnabled(exponentNeeded);
        labelTipExponent.setEnabled(exponentNeeded);


        //tip factor
        fieldTipFactor.setEnabled(factorNeeded);
        labelTipFactor.setEnabled(factorNeeded);

        fieldTipFactor.setVisible(factorNeeded);
        labelTipFactor.setVisible(factorNeeded);

        //transition radius
        fieldTipTransitionRadius.setEnabled(transitionRadiusNeeded);
        labelTipTransitionRadius.setEnabled(transitionRadiusNeeded);
    }

    private PropertyChangeListener buildModelListener() 
    {
        PropertyChangeListener listener = new PropertyChangeListener() 
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                String property = evt.getPropertyName();

                if(ProcessingBatchModelInterface.BATCH_NAME.equals(property))
                {
                    String newName = evt.getNewValue().toString();
                    fieldName.setValue(newName);
                }
                else if(ProcessingBatchModelInterface.SOURCES.equals(property))
                {            
                    List<?> valNew = (List<?>)evt.getNewValue();

                    if(!valNew.isEmpty())
                    {
                        ChannelSource source = (ChannelSource) valNew.get(0);

                        File dir =  IOUtilities.findClosestDirectory(source.getCorrespondingFile(), ".properties", 3);
                        if(dir != null)
                        {
                            propertiesChooser.setCurrentDirectory(dir);
                        }           
                    }
                }
                else if(CONTACT_POINT_AUTOMATIC.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = buttonAutomatic.isSelected();
                    if(newVal != oldVal)
                    {
                        buttonGroupContactEstimator.clearSelection();
                        ButtonModel selectedModel = newVal ? buttonAutomatic.getModel() :buttonManual.getModel();
                        buttonGroupContactEstimator.setSelected(selectedModel, true);
                    }
                }
                else if(DOMAIN_CROPPED.equals(property))
                {
                    boolean trimDomain = (boolean)evt.getNewValue();
                    fieldTrimLeft.setEnabled(trimDomain);
                    fieldTrimRight.setEnabled(trimDomain);
                    boxTrimDomain.setSelected(trimDomain);
                }
                else if(RANGE_CROPPED.equals(property))
                {
                    boolean trimRange = (boolean)evt.getNewValue();
                    fieldTrimLower.setEnabled(trimRange);
                    fieldTrimUpper.setEnabled(trimRange);
                    boxTrimRange.setSelected(trimRange);
                }
                else if(SMOOTHER_TYPE.equals(property))
                {
                    SmootherType newVal = (SmootherType)evt.getNewValue();
                    SmootherType oldVal = (SmootherType)comboSmoothers.getSelectedItem();
                    if(!(newVal.equals(oldVal)))
                    {
                        comboSmoothers.setSelectedItem(newVal);
                    }
                }
                else if(CURVE_SMOOTHED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxSmooth.isSelected();
                    if(newVal != oldVal)
                    {
                        boxSmooth.setSelected(newVal);
                    }
                }
                else if(LOESS_SPAN.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldLoessSpan.getDoubleValue();
                    if(Double.compare(newVal,oldVal)!= 0)
                    {
                        fieldLoessSpan.setValue(newVal);
                    }
                }
                else if(LOESS_ITERATIONS.equals(property))
                {
                    Double newVal = ((Number)evt.getNewValue()).doubleValue();
                    Double oldVal = fieldIterLocal.getValue().doubleValue() ;
                    if(!(newVal.equals(oldVal)))
                    {
                        fieldIterLocal.setValue(newVal);
                    }
                }
                else if(SAVITZKY_SPAN.equals(property))
                {
                    Double newVal = ((Number)evt.getNewValue()).doubleValue();
                    Double oldVal = fieldSavitzkySpan.getValue().doubleValue();
                    if(!(newVal.equals(oldVal)))
                    {
                        fieldSavitzkySpan.setValue(newVal);
                    }
                }
                else if(SAVITZKY_DEGREE.equals(property))
                {
                    Double newVal = ((Number)evt.getNewValue()).doubleValue();
                    Double oldVal = fieldSavitzkyDegree.getValue().doubleValue();
                    if(!(newVal.equals(oldVal)))
                    {
                        fieldSavitzkyDegree.setValue(newVal);
                    }
                }
                else if(SUBSTRATE_EFFECT_CORRECTION_KNOWN.equals(property))
                {
                    boolean valNew = (boolean)evt.getNewValue();                        
                    setSubstrateCorrectionKnown(valNew);
                }
                else if(CORRECT_SUBSTRATE_EFFECT.equals(property))
                {
                    boolean valNew = (boolean)evt.getNewValue();
                    boolean valOld = boxCorrectSubstrate.isSelected();

                    if(valNew != valOld)
                    {
                        boxCorrectSubstrate.setSelected(valNew);
                    }

                    //this must be outside if! 
                    setSubstrateCorrectionUsed(valNew, boxUseTopography.isSelected());
                }
                else if(SAMPLE_ADHERENT.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxSampleAdherent.isSelected();
                    if(newVal != oldVal)
                    {
                        boxSampleAdherent.setSelected(newVal);
                    }
                }
                else if(USE_SAMPLE_TOPOGRAPHY.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxUseTopography.isSelected();
                    if(newVal != oldVal)
                    {
                        boxUseTopography.setSelected(newVal);
                    }

                    //this must be outside if
                    setSubstrateCorrectionUsed(boxCorrectSubstrate.isSelected(), newVal);
                }
                else if(THICKNESS_CORRECTION_METHOD.equals(property))
                {
                    ThicknessCorrectionMethod  newVal = (ThicknessCorrectionMethod)evt.getNewValue();
                    ThicknessCorrectionMethod oldVal = comboThicknessCorrectionType.getItemAt(comboThicknessCorrectionType.getSelectedIndex());
                    if(!Objects.equals(oldVal, newVal))
                    {
                        comboThicknessCorrectionType.setSelectedItem(newVal);
                    }
                }
                else if(APPLICABLE_THICKNESS_CORRECTION_METHODS.equals(property))
                {
                    Set<ThicknessCorrectionMethod> applicableMethodsNew = (Set<ThicknessCorrectionMethod>)evt.getNewValue();
                    comboThicknessCorrectionType.setModel(new DefaultComboBoxModel<>(applicableMethodsNew.toArray(new ThicknessCorrectionMethod[] {})));
                    comboThicknessCorrectionType.setSelectedItem(model.getThicknessCorrectionMethod());          
                }
                else if(SAMPLE_THICKNESS.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldSampleThickness.getDoubleValue();
                    if(Double.compare(oldVal, newVal) != 0)
                    {
                        fieldSampleThickness.setValue(newVal);
                    }
                }
                else if(SAMPLE_TOPOGRAPHY_FILE.equals(property))
                {
                    File newVal = (File)evt.getNewValue();
                    File oldVal = (File)fieldTopographyFile.getValue();
                    if(!Objects.equals(newVal, oldVal))
                    {
                        fieldTopographyFile.setValue(newVal);
                    }
                }
                else if(PLOT_RECORDED_CURVE.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotRecordedCurve.isSelected();
                    boxPlotRecordedCurveFit.setEnabled(newVal);

                    if(newVal != oldVal)
                    {
                        boxPlotRecordedCurve.setSelected(newVal);
                    }
                }
                else if(PLOT_RECORDED_CURVE_FIT.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotRecordedCurveFit.isSelected();
                    if(newVal != oldVal)
                    {
                        boxPlotRecordedCurveFit.setSelected(newVal);
                    }
                }
                else if(PLOT_INDENTATION.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotIndentation.isSelected();
                    boxPlotIndentationFit.setEnabled(newVal);

                    if(newVal != oldVal)
                    {
                        boxPlotIndentation.setSelected(newVal);
                    }
                }
                else if(PLOT_INDENTATION_FIT.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotIndentationFit.isSelected();
                    if(newVal != oldVal)
                    {
                        boxPlotIndentationFit.setSelected(newVal);
                    }
                }
                else if(PLOT_MODULUS.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotModulus.isSelected();
                    boxPlotModulusFit.setEnabled(newVal);

                    if(newVal != oldVal)
                    {
                        boxPlotModulus.setSelected(newVal);               
                    }
                }
                else if(PLOT_MODULUS_FIT.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotModulusFit.isSelected();
                    if(newVal != oldVal)
                    {
                        boxPlotModulusFit.setSelected(newVal);
                    }
                }
                else if(SHOW_AVERAGED_RECORDED_CURVES.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxShowAveragedRecordedCurve.isSelected();
                    if(newVal != oldVal)
                    {
                        boxShowAveragedRecordedCurve.setSelected(newVal);
                    }
                }
                else if(SHOW_AVERAGED_INDENTATION_CURVES.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxShowAveragedIndentation.isSelected();
                    if(newVal != oldVal)
                    {
                        boxShowAveragedIndentation.setSelected(newVal);
                    }
                }
                else if(SHOW_AVERAGED_POINTWISE_MODULUS_CURVES.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxShowAveragedPointwiseModulus.isSelected();
                    if(newVal != oldVal)
                    {
                        boxShowAveragedPointwiseModulus.setSelected(newVal);
                    }
                }
                else if(AVERAGED_CURVES_ERROR_BAR_TYPE.equals(property))
                {
                    ErrorBarType  newVal = (ErrorBarType)evt.getNewValue();
                    ErrorBarType oldVal = comboErrorBarType.getItemAt(comboErrorBarType.getSelectedIndex());
                    if(!Objects.equals(oldVal, newVal))
                    {
                        comboErrorBarType.setSelectedItem(newVal);
                    }
                }
                else if(PLOT_MAP_AREA_IMAGES.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxPlotMapAreaImages.isSelected();
                    if(newVal != oldVal)
                    {
                        boxPlotMapAreaImages.setSelected(newVal);
                    }
                }
                else if(PLOT_MAP_AREA_IMAGES_ENABLED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boxPlotMapAreaImages.setEnabled(newVal);            
                }
                else if(INCLUDE_IN_MAPS.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxIncludeCurvesInMaps.isSelected();
                    if(newVal != oldVal)
                    {
                        boxIncludeCurvesInMaps.setSelected(newVal);
                    }
                }
                else if(INCLUDE_IN_MAPS_ENABLED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boxIncludeCurvesInMaps.setEnabled(newVal);
                }
                else if(CALCULATE_R_SQUARED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxCalculateRSquared.isSelected();
                    if(newVal != oldVal)
                    {
                        boxCalculateRSquared.setSelected(newVal);
                    }
                }
                else if(CALCULATE_ADHESION_FORCE.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxCalculateAdhesionForce.isSelected();
                    if(newVal != oldVal)
                    {
                        boxCalculateAdhesionForce.setSelected(newVal);
                    }
                }
                else if(POISSON_RATIO.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldPoissonRatio.getDoubleValue();

                    if(Double.compare(newVal,oldVal)!=0)
                    {
                        fieldPoissonRatio.setValue(newVal);
                    }
                }
                else if(SPRING_CONSTANT.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldSpringConstant.getDoubleValue();
                    if(Double.compare(newVal,oldVal)!=0)
                    {
                        fieldSpringConstant.setValue(newVal);
                    }
                }
                else if(SPRING_CONSTANT_INPUT_ENABLED.equals(property))
                {
                    boolean newVal = (Boolean)evt.getNewValue();
                    fieldSpringConstant.setEnabled(newVal);
                }
                else if(SPRING_CONSTANT_USE_READ_IN.equals(property))
                {
                    boolean newVal = (Boolean)evt.getNewValue();
                    boolean oldVal = boxUseReadInSpringConstant.isSelected();
                    if(newVal != oldVal)
                    {
                        boxUseReadInSpringConstant.setSelected(newVal);
                    }
                }
                else if(SPRING_CONSTANT_USE_READ_IN_ENABLED.equals(property))
                {
                    boolean newVal = (Boolean)evt.getNewValue();
                    boxUseReadInSpringConstant.setEnabled(newVal);
                }
                else if(INDENTATION_MODEL.equals(property))
                {
                    BasicIndentationModel newVal = (BasicIndentationModel)evt.getNewValue();
                    BasicIndentationModel oldVal = (BasicIndentationModel)comboIndentationModel.getSelectedItem();
                    if(!Objects.equals(newVal, oldVal))
                    {
                        comboIndentationModel.setSelectedItem(newVal);
                    }
                }
                else if(AUTOMATIC_CONTACT_ESTIMATOR.equals(property))
                {
                    AutomaticContactEstimatorType newVal = (AutomaticContactEstimatorType)evt.getNewValue();
                    AutomaticContactEstimatorType oldVal = (AutomaticContactEstimatorType)comboContactEstimator.getSelectedItem();
                    if(!Objects.equals(newVal, oldVal))
                    {
                        comboContactEstimator.setSelectedItem(newVal);
                    }
                }
                else if(CONTACT_ESTIMATION_METHOD.equals(property))
                {
                    ContactEstimationMethod newVal = (ContactEstimationMethod)evt.getNewValue();
                    ContactEstimationMethod oldVal = (ContactEstimationMethod)comboContactEstimationMethod.getSelectedItem();

                    if(!Objects.equals(newVal, oldVal))
                    {
                        comboContactEstimationMethod.setSelectedItem(newVal);
                    }
                }
                else if(REGRESSION_STRATEGY.equals(property))
                {
                    BasicRegressionStrategy newVal = (BasicRegressionStrategy)evt.getNewValue();
                    BasicRegressionStrategy oldVal = (BasicRegressionStrategy)comboRegressionStrategy.getSelectedItem();
                    if(!Objects.equals(newVal, oldVal))
                    {
                        comboRegressionStrategy.setSelectedItem(newVal);
                    }
                }
                else if(FITTED_BRANCH.equals(property))
                {
                    ForceCurveBranch  newVal = (ForceCurveBranch)evt.getNewValue();
                    ForceCurveBranch oldVal = (ForceCurveBranch)comboFittedBranch.getSelectedItem();
                    if(!Objects.equals(newVal, oldVal))
                    {
                        comboFittedBranch.setSelectedItem(newVal);
                    }
                }
                else if(AVAILABLE_FORCE_CURVE_BRANCHES.equals(property))
                {
                    Set<ForceCurveBranch> branchesNew = (Set<ForceCurveBranch>)evt.getNewValue();
                    comboFittedBranch.setModel(new DefaultComboBoxModel<>(branchesNew.toArray(new ForceCurveBranch[] {})));
                    comboFittedBranch.setSelectedItem(model.getFittedBranch());
                }
                else if(ADHESIVE_ENERGY_ESTIMATION_METHOD.equals(property))
                {
                    AdhesiveEnergyEstimationMethod  newVal = (AdhesiveEnergyEstimationMethod)evt.getNewValue();
                    AdhesiveEnergyEstimationMethod oldVal = (AdhesiveEnergyEstimationMethod)comboAdhesiveEnergy.getSelectedItem();
                    if(!Objects.equals(newVal, oldVal))
                    {
                        comboAdhesiveEnergy.setSelectedItem(newVal);
                    }
                }
                else if(ADHESIVE_ENERGY_REQUIRED.equals(property))
                {
                    boolean newVal = (Boolean)evt.getNewValue();                        
                    setAdhesiveEnergyRequired(newVal);
                }
                else if(BASELINE_DEGREE.equals(property))
                {
                    Integer newVal = ((Number)evt.getNewValue()).intValue();
                    Integer oldVal = ((Number)spinnerBaselineDegree.getValue()).intValue();
                    if(!(newVal.equals(oldVal)))
                    {
                        spinnerBaselineDegree.setValue(newVal);
                    }
                }
                else if(POSTCONTACT_DEGREE.equals(property))
                {
                    Integer newVal = ((Number)evt.getNewValue()).intValue();
                    Integer oldVal = ((Number)spinnerPostcontactDegree.getValue()).intValue();
                    if(!(newVal.equals(oldVal)))
                    {
                        spinnerPostcontactDegree.setValue(newVal);
                    }
                }
                else if(POSTCONTACT_DEGREE_INPUT_ENABLED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    spinnerPostcontactDegree.setEnabled(newVal);
                    labelPostcontactDegree.setEnabled(newVal);
                }
                else if(SENSITIVITY.equals(property))
                {
                    Map<PhotodiodeSignalType, Double> sensitivitiesMap = (Map<PhotodiodeSignalType, Double>) evt.getNewValue();

                    for(Entry<PhotodiodeSignalType, Double> entry : sensitivitiesMap.entrySet())
                    {
                        double valNew = entry.getValue();

                        JSpinnerNumeric fieldSensitivity = fieldsSensitivity.get(entry.getKey());
                        double valOld = fieldSensitivity.getDoubleValue();

                        if(Double.compare(valNew,valOld) != 0)
                        {
                            fieldSensitivity.setValue(valNew);
                        }
                    }            
                }
                else if(SENSITIVITY_USE_READ_IN.equals(property))
                {
                    Map<PhotodiodeSignalType, Boolean> valueMap = (Map<PhotodiodeSignalType, Boolean>) evt.getNewValue();

                    for(Entry<PhotodiodeSignalType, Boolean> entry : valueMap.entrySet())
                    {
                        boolean valNew = entry.getValue();

                        JCheckBox boxUseReadInSensitivity = boxesUseReadInSensitivity.get(entry.getKey());
                        boolean valOld = boxUseReadInSensitivity.isSelected();
                        if(valNew != valOld)
                        {
                            boxUseReadInSensitivity.setSelected(valNew);
                        }
                    }
                }
                else if(SENSITIVITY_INPUT_ENABLED.equals(property))
                {
                    Map<PhotodiodeSignalType, Boolean> valueMap = (Map<PhotodiodeSignalType, Boolean>) evt.getNewValue();

                    for(Entry<PhotodiodeSignalType, Boolean> entry : valueMap.entrySet())
                    {
                        setSensitivityInputEnabled(entry.getKey(), entry.getValue());
                    }
                }
                else if(SENSITIVITY_USE_READ_IN_ENABLED.equals(property))
                {
                    Map<PhotodiodeSignalType, Boolean> valueMap = (Map<PhotodiodeSignalType, Boolean>) evt.getNewValue();

                    for(Entry<PhotodiodeSignalType, Boolean> entry : valueMap.entrySet())
                    {
                        JCheckBox boxUseReadInSensitivity = boxesUseReadInSensitivity.get(entry.getKey());
                        boolean newVal = entry.getValue();
                        boxUseReadInSensitivity.setEnabled(newVal);
                    }           
                }
                else if(SENSITIVITY_PHOTODIODE_SIGNALS.equals(property))
                {
                    Set<PhotodiodeSignalType> signalPresent = (Set<PhotodiodeSignalType>)evt.getNewValue();
                    setSensitivityPanelConsistentWithPhotodiodeSignals(signalPresent);
                }
                else if(TIP_RADIUS.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTipRadius.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTipRadius.setValue(newVal);
                    }
                }
                else if(TIP_HALF_ANGLE.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTipAngle.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTipAngle.setValue(newVal);
                    }
                }
                else if(TIP_TRANSITION_RADIUS.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTipTransitionRadius.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTipTransitionRadius.setValue(newVal);
                    }
                }
                else if(TIP_TRANSITION_RADIUS_CALCULABLE.equals(property))
                {
                    boolean newVal = (Boolean)evt.getNewValue();                      
                    smoothTipTransitionAction.setEnabled(newVal);            
                }
                else if(TIP_EXPONENT.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTipExponent.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTipExponent.setValue(newVal);
                    }
                }
                else if(TIP_FACTOR.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTipFactor.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTipFactor.setValue(newVal);
                    }
                }
                else if(LEFT_CROPPING.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTrimLeft.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTrimLeft.setValue(newVal);
                    }
                }
                else if(RIGHT_CROPPING.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTrimRight.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTrimRight.setValue(newVal);
                    }
                }
                else if(LOWER_CROPPING.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTrimLower.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTrimLower.setValue(newVal);
                    }
                }
                else if(UPPER_CROPPING.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldTrimUpper.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldTrimUpper.setValue(newVal);
                    }
                }
                else if(LOAD_LIMIT.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldLoadMaximum.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldLoadMaximum.setValue(newVal);
                    }
                }
                else if(INDENTATION_LIMIT.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = fieldIndentMaximum.getDoubleValue();
                    if(Double.compare(newVal,oldVal) != 0)
                    {
                        fieldIndentMaximum.setValue(newVal);
                    }
                }
                else if(ProcessingModelInterface.CURRENT_BATCH_NUMBER.equals(property))
                {
                    pullModelProperties();
                }
                else if(ProcessingBatchModelInterface.SETTINGS_SPECIFIED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    if(settingsSpecified != newVal)
                    {     
                        //we should create an event with the source specified as the whole page
                        //and not the inner class, because the wizard that uses this pages
                        //checks whether the event source is equal to the currently active page
                        PropertyChangeEvent event = new PropertyChangeEvent(ProcessingSettingsPage.this, ProcessingWizardModel.INPUT_PROVIDED, settingsSpecified, newVal);
                        firePropertyChange(event);
                        settingsSpecified = newVal;
                    }
                }
                else if(ProcessingBatchModelInterface.BASIC_SETTINGS_SPECIFIED.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();

                    if(basicSettingsSpecified != newVal)
                    {
                        basicSettingsSpecified = newVal;
                    }
                }
                else if(CROPPING_ON_CURVE_SELECTION_POSSIBLE.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();

                    trimmingAction.setEnabled(newVal);
                }
                else if(ProcessingBatchModelInterface.PARENT_DIRECTORY.equals(property))
                {
                    File newParent = (File)evt.getNewValue();
                    calibrationDialog.setChooserSelectedFile(newParent);
                }

                //jumps

                else if(ProcessingBatchModel.FIND_JUMPS.equals(property))
                {
                    boolean newVal = (boolean)evt.getNewValue();
                    boolean oldVal = boxFindJumps.isSelected();

                    if(oldVal != newVal)
                    {
                        boxFindJumps.setSelected(newVal);
                    }
                }
                else if(ProcessingBatchModel.JUMPS_MIN_DISTANCE_FROM_CONTACT.equals(property))
                {
                    double newVal  = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = ((Number)spinnerJumpsMinDistanceFromContact.getValue()).doubleValue();

                    if(!Objects.equals(newVal, oldVal))
                    {
                        spinnerJumpsMinDistanceFromContact.setValue(newVal);
                    }
                }
                else if(ProcessingBatchModel.JUMPS_SPAN.equals(property))
                {            
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = ((Number)spinnerJumpsSpan.getValue()).doubleValue();

                    if(Double.compare(oldVal, newVal) != 0)
                    {
                        spinnerJumpsSpan.setValue(newVal);
                    }
                }
                else if(ProcessingBatchModel.POLYNOMIAL_DEGREE.equals(property))
                {
                    double newVal = ((Number)evt.getNewValue()).doubleValue();
                    double oldVal = ((Number)spinnerJumpsPolynomialDegree.getValue()).doubleValue();

                    if(Double.compare(oldVal, newVal) != 0)
                    {
                        spinnerJumpsPolynomialDegree.setValue(newVal);
                    }
                }
                else if(ProcessingBatchModel.JUMPS_SPAN_TYPE.equals(property))
                {
                    SpanType newVal = (SpanType)evt.getNewValue();
                    SpanType oldVal = (SpanType)comboJumpsSpanType.getSelectedItem();

                    if(!Objects.equals(oldVal, newVal))
                    {
                        comboJumpsSpanType.setSelectedItem(newVal);
                    }
                }
                else if(ProcessingBatchModel.JUMPS_WEIGHT_FUNCTION.equals(property))
                {
                    LocalRegressionWeightFunction newVal = (LocalRegressionWeightFunction)evt.getNewValue();
                    LocalRegressionWeightFunction oldValue = (LocalRegressionWeightFunction)comboJumpsRegressionWeights.getSelectedItem();

                    if(!Objects.equals(oldValue, newVal))
                    {
                        comboJumpsRegressionWeights.setSelectedItem(newVal);
                    }
                }                
            }
        };

        return listener;
    }

    private void initTextFieldListener()
    {
        fieldName.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                String batchName = evt.getNewValue().toString();
                model.setBatchName(batchName);
            }
        });
    }

    private void initNumericFieldListeners()
    {
        fieldTipRadius.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTipRadius.getDoubleValue();
                model.setTipRadius(valueNew);                
            }
        });
        fieldTipAngle.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTipAngle.getDoubleValue();
                model.setTipHalfAngle(valueNew);
            }
        });
        fieldTipTransitionRadius.addChangeListener( new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTipTransitionRadius.getDoubleValue();
                model.setTipTransitionRadius(valueNew);
            }
        });
        fieldTipExponent.addChangeListener(new ChangeListener() {         
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTipExponent.getDoubleValue();
                model.setTipExponent(valueNew);
            }
        });
        fieldTipFactor.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTipFactor.getDoubleValue();
                model.setTipFactor(valueNew);
            }
        });
        fieldSpringConstant.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldSpringConstant.getDoubleValue();
                model.setSpringConstant(valueNew);
            }
        });	
        for(Entry<PhotodiodeSignalType, JSpinnerNumeric> entry : fieldsSensitivity.entrySet())
        {
            JSpinnerNumeric fieldSensitivity = entry.getValue();
            final PhotodiodeSignalType signalType = entry.getKey();

            fieldSensitivity.addChangeListener(new ChangeListener() 
            {            
                @Override
                public void stateChanged(ChangeEvent evt) {
                    double valueNew = fieldSensitivity.getDoubleValue();
                    model.setSensitivity(signalType, valueNew);
                }
            });
        }

        fieldTrimLeft.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTrimLeft.getDoubleValue();
                model.setLeftCropping(valueNew);
            }
        });
        fieldTrimRight.addChangeListener(new ChangeListener() {            
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTrimRight.getDoubleValue();
                model.setRightCropping(valueNew);
            }
        });
        fieldTrimLower.addChangeListener(new ChangeListener() {            
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTrimLower.getDoubleValue();
                model.setLowerCropping(valueNew);
            }
        });
        fieldTrimUpper.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldTrimUpper.getDoubleValue();
                model.setUpperCropping(valueNew);
            }
        });		

        fieldPoissonRatio.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldPoissonRatio.getDoubleValue();
                model.setPoissonRatio(valueNew);
            }
        });		
        fieldLoadMaximum.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldLoadMaximum.getDoubleValue();
                model.setLoadLimit(valueNew);
            }
        });
        fieldIndentMaximum.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldIndentMaximum.getDoubleValue(); 
                model.setIndentationLimit(valueNew);
            }
        });		
        fieldLoessSpan.addChangeListener(new ChangeListener() {            
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldLoessSpan.getDoubleValue();
                model.setLoessSpan(valueNew);
            }
        });
        fieldIterLocal.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setLoessIterations(valueNew);
            }
        });
        fieldSavitzkySpan.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setSavitzkySpan(valueNew);
            }
        });
        fieldSavitzkyDegree.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setSavitzkyDegree(valueNew);
            }
        });
        fieldSampleThickness.addChangeListener(new ChangeListener() {           
            @Override
            public void stateChanged(ChangeEvent evt) {
                double valueNew = fieldSampleThickness.getDoubleValue();
                model.setSampleThickness(valueNew);
            }
        });
    }

    private void initItemListener()
    {		
        boxTrimDomain.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setDomainCropping(selected);                
            }
        });
        boxTrimRange.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setRangeCropped(selected);
            }
        });
        boxSmooth.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                fieldSavitzkySpan.setEnabled(selected);
                fieldSavitzkyDegree.setEnabled(selected);
                fieldLoessSpan.setEnabled(selected);
                fieldIterLocal.setEnabled(selected);
                comboSmoothers.setEnabled(selected);        

                model.setDataSmoothed(selected);
            }
        });
        boxCorrectSubstrate.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setCorrectSubstrateEffect(selected);
            }
        });
        boxUseTopography.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setUseSampleTopography(selected);
            }
        });
        boxSampleAdherent.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setSampleAdherent(selected);
            }
        });
        boxPlotRecordedCurve.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotRecordedCurve(selected); 
            }
        });
        boxPlotRecordedCurveFit.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotRecordedCurveFit(selected); 
            }
        });
        boxPlotIndentation.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotIndentation(selected); 
            }
        });
        boxPlotIndentationFit.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotIndentationFit(selected); 
            }
        });
        boxPlotModulus.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotModulus(selected); 
            }
        });      
        boxPlotModulusFit.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotModulusFit(selected); 
            }
        });

        boxShowAveragedRecordedCurve.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setShowAveragedRecordedCurves(selected); 
            }
        });
        boxShowAveragedIndentation.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setShowAveragedIndentationCurves(selected); 
            }
        });
        boxShowAveragedPointwiseModulus.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setShowAveragedPointwiseModulusCurves(selected); 
            }
        });

        comboErrorBarType.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                ErrorBarType errorBarTypeNew = comboErrorBarType.getItemAt(comboErrorBarType.getSelectedIndex());
                model.setAveragedCurvesBarType(errorBarTypeNew);
            }
        });

        boxPlotMapAreaImages.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotMapAreaImages(selected); 
            }
        });
        boxIncludeCurvesInMaps.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setIncludeCurvesInMaps(selected); 
            }
        });
        boxCalculateRSquared.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setCalculateRSquared(selected); 
            }
        });
        boxCalculateAdhesionForce.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setCalculateAdhesionForce(selected); 
            }
        });

        for(Entry<PhotodiodeSignalType, JCheckBox> entry : boxesUseReadInSensitivity.entrySet())
        {
            JCheckBox boxUseReadInSensitivity = entry.getValue();
            final PhotodiodeSignalType signalType = entry.getKey();

            boxUseReadInSensitivity.addItemListener(new ItemListener() {           
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                    model.setUseReadInSensitivity(signalType, selected);
                }
            });
        }

        boxUseReadInSpringConstant.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setUseReadInSpringConstant(selected);
            }
        });
        buttonAutomatic.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                comboContactEstimator.setEnabled(selected);
                comboContactEstimationMethod.setEnabled(selected);
                labelContactEstimator.setEnabled(selected);
                labelContactEstimationMethod.setEnabled(selected);

                model.setContactPointAutomatic(selected);
            }
        });
        comboContactEstimator.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                AutomaticContactEstimatorType estimator = comboContactEstimator.getItemAt(comboContactEstimator.getSelectedIndex());
                model.setAutomaticContactEstimator(estimator);                
            }
        });
        comboContactEstimationMethod.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                ContactEstimationMethod contactEstimationMethodNew = comboContactEstimationMethod.getItemAt(comboContactEstimationMethod.getSelectedIndex());
                model.setContactEstimationMethod(contactEstimationMethodNew);                
            }
        });
        comboRegressionStrategy.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                BasicRegressionStrategy estimator = (BasicRegressionStrategy)comboRegressionStrategy.getSelectedItem();         
                model.setRegressionStartegy(estimator);                
            }
        });
        comboFittedBranch.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                ForceCurveBranch fittedBranch = (ForceCurveBranch)comboFittedBranch.getSelectedItem();         
                model.setFittedBranch(fittedBranch);                
            }
        });
        comboThicknessCorrectionType.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                ThicknessCorrectionMethod thicknessCorrectionMethod = comboThicknessCorrectionType.getItemAt(comboThicknessCorrectionType.getSelectedIndex());         
                model.setThicknessCorrectionMethod(thicknessCorrectionMethod);                
            }
        });
        comboIndentationModel.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                BasicIndentationModel indenter = comboIndentationModel.getItemAt(comboIndentationModel.getSelectedIndex());

                model.setIndentationModel(indenter);
                setConsistentWithIndentationModel(indenter);                
            }
        });
        comboAdhesiveEnergy.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                AdhesiveEnergyEstimationMethod method = comboAdhesiveEnergy.getItemAt(comboAdhesiveEnergy.getSelectedIndex());         
                model.setAdhesiveEnergyEstimationMethod(method);                
            }
        });
        comboSmoothers.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                SmootherType smoother = (SmootherType) comboSmoothers.getSelectedItem();        
                model.setSmootherType(smoother);                
            }
        });

        boxFindJumps.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setFindJumps(selected); 
            }
        });

        comboJumpsRegressionWeights.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt)
            {                
                LocalRegressionWeightFunction weightNew = (LocalRegressionWeightFunction)comboJumpsRegressionWeights.getSelectedItem();
                model.setJumpsWeightFunction(weightNew);
            }
        });

        comboJumpsSpanType.addItemListener(new ItemListener() 
        {
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                SpanType spanType = (SpanType)comboJumpsSpanType.getSelectedItem();              
                model.setJumpsSpanType(spanType);
            }
        });
    }

    private void initChangeListener()
    {
        spinnerBaselineDegree.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int baselineDegreeNew = ((SpinnerNumberModel)spinnerBaselineDegree.getModel()).getNumber().intValue();
                model.setBaselineDegree(baselineDegreeNew);                
            }
        });

        spinnerPostcontactDegree.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int postcontactDegreeNew = ((SpinnerNumberModel)spinnerPostcontactDegree.getModel()).getNumber().intValue();
                model.setPostcontactDegree(postcontactDegreeNew);                
            }
        });

        spinnerJumpsSpan.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double spanNew = ((SpinnerNumberModel)spinnerJumpsSpan.getModel()).getNumber().doubleValue();
                getModel().setJumpsSpan(spanNew);                
            }
        });

        spinnerJumpsPolynomialDegree.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int polynomialDegreeNew = ((SpinnerNumberModel)spinnerJumpsPolynomialDegree.getModel()).getNumber().intValue();
                getModel().setJumpsPolynomialDegree(polynomialDegreeNew);
            }
        });

        spinnerJumpsMinDistanceFromContact.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double minDistanceNew = ((SpinnerNumberModel)spinnerJumpsMinDistanceFromContact.getModel()).getNumber().doubleValue();
                getModel().setJumpsMinDistanceFromContact(minDistanceNew);
            }
        });
    }

    private void saveProperties(File f)
    {
        try(FileOutputStream out = new FileOutputStream(f))
        {
            Properties properties = model.getProperties();
            properties.store(out, "AtomicJ properties file");
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error occured during saving", "", JOptionPane.ERROR_MESSAGE);  
        }
    }

    private void loadProperties(File f)
    {
        try(FileInputStream in = new FileInputStream(f))
        {
            Properties properties = new Properties();
            properties.load(in);
            model.loadProperties(properties);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred during import", "", JOptionPane.ERROR_MESSAGE);  
        }
    }	

    @Override
    public String getTaskName() 
    {
        return TASK_NAME;
    }

    @Override
    public String getTaskDescription() 
    {
        return DESCRIPTION;
    }

    @Override
    public Component getView()
    {
        return mainPanel;
    }

    @Override
    public Component getControls()
    {
        return panelControls;
    }

    @Override
    public String getIdentifier()
    {
        return IDENTIFIER; 
    }

    @Override
    public boolean isLast() 
    {
        return true;
    }

    @Override
    public boolean isFirst()
    {
        return false;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return settingsSpecified;
    }

    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel();	
        JLabel labelBatch = new JLabel("Batch no ");

        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonImport).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonExport).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addComponent(buttonImport)
                .addComponent(buttonExport));

        layout.linkSize(buttonImport,buttonExport);

        return panelControl;
    }

    private SubPanel buildProcessingPanel()
    {
        SubPanel processingPanel = new SubPanel();
        JPanel panelAutomatic = new JPanel();

        buttonGroupContactEstimator.add(buttonAutomatic);
        buttonGroupContactEstimator.add(buttonManual);		
        buttonAutomatic.setSelected(true);

        JLabel labelAutomatic = new JLabel("Automatic");
        JLabel labelManual = new JLabel("Manual");
        JLabel labelRegressionStrategy = new JLabel("Model fit");
        JLabel labelFittedBranch = new JLabel("Fit to");

        labelManual.setDisplayedMnemonic(KeyEvent.VK_M);
        labelAutomatic.setDisplayedMnemonic(KeyEvent.VK_A);
        labelRegressionStrategy.setDisplayedMnemonic(KeyEvent.VK_D);
        labelContactEstimator.setDisplayedMnemonic(KeyEvent.VK_O);

        labelRegressionStrategy.setLabelFor(comboRegressionStrategy);
        labelContactEstimator.setLabelFor(comboContactEstimator);
        labelContactEstimationMethod.setLabelFor(comboContactEstimationMethod);

        labelContactEstimator.setHorizontalAlignment(SwingConstants.RIGHT);
        labelContactEstimationMethod.setHorizontalAlignment(SwingConstants.RIGHT);
        labelRegressionStrategy.setHorizontalAlignment(SwingConstants.RIGHT);
        labelFittedBranch.setHorizontalAlignment(SwingConstants.RIGHT);

        buttonAutomatic.setMnemonic(KeyEvent.VK_A);
        buttonManual.setMnemonic(KeyEvent.VK_M);

        panelAutomatic.add(labelAutomatic);
        panelAutomatic.add(buttonAutomatic);
        panelAutomatic.add(labelManual);
        panelAutomatic.add(buttonManual);

        processingPanel.addComponent(panelAutomatic, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0);	
        processingPanel.addComponent(labelContactEstimator, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
        processingPanel.addComponent(comboContactEstimator, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0);
        processingPanel.addComponent(labelContactEstimationMethod, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
        processingPanel.addComponent(comboContactEstimationMethod, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0);
        processingPanel.addComponent(labelRegressionStrategy, 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
        processingPanel.addComponent(comboRegressionStrategy, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0);
        processingPanel.addComponent(labelFittedBranch, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0, new Insets(3,3,7,3));
        processingPanel.addComponent(comboFittedBranch, 1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0, new Insets(3,3,7,3));
        processingPanel.addComponent(Box.createVerticalGlue(), 0, 5, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1);

        processingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Processing"));

        return processingPanel;
    }

    private JPanel buildNamePanel()
    {
        DefaultFormatter formatter = (DefaultFormatter)fieldName.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        JLabel labelName = new JLabel("Batch name");
        labelName.setLabelFor(fieldName);
        labelName.setDisplayedMnemonic(KeyEvent.VK_T);
        //labelName.setHorizontalAlignment(SwingConstants.RIGHT);

        SubPanel namePanel = new SubPanel();
        namePanel.addComponent(labelName, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1);
        namePanel.addComponent(fieldName, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));

        return namePanel;
    }

    private JPanel buildSamplePanel()
    {
        JLabel labelPoissonRatio = new JLabel("Poisson ratio");
        labelPoissonRatio.setLabelFor(fieldPoissonRatio);
        labelPoissonRatio.setDisplayedMnemonic(KeyEvent.VK_P);
        labelPoissonRatio.setHorizontalAlignment(SwingConstants.RIGHT);

        SubPanel samplePanel = new SubPanel();

        samplePanel.addComponent(labelPoissonRatio, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0, new Insets(5,3,3,3));
        samplePanel.addComponent(labelAdhesiveEnergy, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0);
        samplePanel.addComponent(fieldPoissonRatio, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0,new Insets(5,3,3,3));
        samplePanel.addComponent(comboAdhesiveEnergy, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);

        samplePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sample"));

        return samplePanel;
    }

    private JPanel buildBaselinePanel()
    {
        JLabel labelBaselineDegree = new JLabel("Baseline degree");
        labelBaselineDegree.setHorizontalAlignment(SwingConstants.RIGHT);
        labelBaselineDegree.setLabelFor(spinnerBaselineDegree);
        labelBaselineDegree.setDisplayedMnemonic(KeyEvent.VK_Y);

        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)spinnerBaselineDegree.getEditor();
        JFormattedTextField ftf = editor.getTextField();
        ftf.setColumns(4);

        labelPostcontactDegree.setHorizontalAlignment(SwingConstants.RIGHT);
        labelPostcontactDegree.setLabelFor(spinnerPostcontactDegree);

        SubPanel baselinePanel = new SubPanel();

        baselinePanel.addComponent(labelBaselineDegree, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .15, 0, new Insets(5,1,3,3));
        baselinePanel.addComponent(spinnerBaselineDegree, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0,new Insets(5,3,3,3));

        baselinePanel.addComponent(labelPostcontactDegree, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .15, 0, new Insets(5,1,3,3));
        baselinePanel.addComponent(spinnerPostcontactDegree, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0,new Insets(5,3,3,3));

        baselinePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Curve"));

        return baselinePanel;
    }	

    private SubPanel buildModelPanel()
    {
        JLabel labelTipShape = new JLabel("Model");
        labelTipShape.setLabelFor(comboIndentationModel);
        labelTipShape.setDisplayedMnemonic(KeyEvent.VK_H);

        labelTipRadius.setLabelFor(fieldTipRadius);
        labelTipRadius.setDisplayedMnemonic(KeyEvent.VK_U);

        labelTipAngle.setLabelFor(fieldTipAngle);
        labelTipAngle.setDisplayedMnemonic(KeyEvent.VK_L);

        labelTipTransitionRadius.setLabelFor(fieldTipTransitionRadius);
        //labelTipRadius.setDisplayedMnemonic(KeyEvent.VK_U);

        buttonSmoothTransitionRadius.setHideActionText(true);
        buttonSmoothTransitionRadius.setMargin(new Insets(1, 2, 1, 2));

        SubPanel modelPanel = new SubPanel();

        modelPanel.addComponent(labelTipShape, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0, new Insets(5,3,3,3));
        modelPanel.addComponent(labelTipRadius, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0);
        modelPanel.addComponent(labelTipAngle, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0);
        modelPanel.addComponent(labelTipExponent, 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0);
        modelPanel.addComponent(labelTipFactor, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0);
        modelPanel.addComponent(labelTipTransitionRadius, 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 0);
        modelPanel.addComponent(comboIndentationModel, 1, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0,new Insets(5,3,3,3));
        modelPanel.addComponent(fieldTipRadius, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);
        modelPanel.addComponent(fieldTipAngle, 1, 2, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);
        modelPanel.addComponent(fieldTipExponent, 1, 3, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);
        modelPanel.addComponent(fieldTipFactor, 1, 4, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);

        modelPanel.addComponent(fieldTipTransitionRadius, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);
        modelPanel.addComponent(buttonSmoothTransitionRadius, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);
        modelPanel.addComponent(Box.createVerticalGlue(), 0, 6, 5, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1);

        modelPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Model"));

        return modelPanel;
    }

    private SubPanel buildJumpsPanel()
    {
        SubPanel panelJumps = new SubPanel();

        panelJumps.addComponent(boxFindJumps, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);

        panelJumps.addComponent(new JLabel("<html>Min distance<br>from contact</html>"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 1);
        panelJumps.addComponent(spinnerJumpsMinDistanceFromContact, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelJumps.addComponent(new JLabel("nm"), 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelJumps.addComponent(new JLabel("Weights"), 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelJumps.addComponent(comboJumpsRegressionWeights, 4, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelJumps.addComponent(new JLabel("Span"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 1);
        panelJumps.addComponent(spinnerJumpsSpan, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelJumps.addComponent(comboJumpsSpanType, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelJumps.addComponent(new JLabel("Degree"), 3, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelJumps.addComponent(spinnerJumpsPolynomialDegree, 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelJumps.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Curve discontinuities"));

        return panelJumps;
    }

    private SubPanel buildSubstrateCorrectionPanel()
    {
        labelThicknessCorrectionMethod.setLabelFor(comboThicknessCorrectionType);

        labelThickness.setLabelFor(fieldSampleThickness);
        labelThickness.setDisplayedMnemonic(KeyEvent.VK_H);

        labelTopographyFile.setLabelFor(fieldTopographyFile);
        labelTopographyFile.setDisplayedMnemonic(KeyEvent.VK_U);

        fieldTopographyFile.setEditable(false);

        SubPanel adherenceThicknessPanel = new SubPanel();

        adherenceThicknessPanel.addComponent(labelThicknessCorrectionMethod, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .025, 1);
        adherenceThicknessPanel.addComponent(comboThicknessCorrectionType, 1, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        adherenceThicknessPanel.addComponent(labelThickness, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .025, 1);
        adherenceThicknessPanel.addComponent(fieldSampleThickness, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);  
        adherenceThicknessPanel.addComponent(boxSampleAdherent, 3, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .325, 1);

        adherenceThicknessPanel.addComponent(boxUseTopography, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .025, 1);
        adherenceThicknessPanel.addComponent(fieldTopographyFile, 1, 2, 2, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        adherenceThicknessPanel.addComponent(buttonBrowseTopography, 3, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .325, 1);

        SubPanel substrateCorrectionPanel = new SubPanel();

        substrateCorrectionPanel.addComponent(boxCorrectSubstrate, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        substrateCorrectionPanel.addComponent(adherenceThicknessPanel, 0, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        substrateCorrectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sample"));


        return substrateCorrectionPanel;
    }

    private SubPanel buildCalibrationPanel()
    {		
        JLabel labelCantileverSpringConstant= new JLabel("Spring (N/m)");
        labelCantileverSpringConstant.setHorizontalAlignment(SwingConstants.RIGHT);
        labelCantileverSpringConstant.setLabelFor(fieldSpringConstant);
        labelCantileverSpringConstant.setDisplayedMnemonic(KeyEvent.VK_G);

        SubPanel calibrationPanel = new SubPanel();
        calibrationPanel.addComponent(labelCantileverSpringConstant, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0, new Insets(5,3,3,3));
        calibrationPanel.addComponent(fieldSpringConstant, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0, new Insets(5,3,3,3));
        calibrationPanel.addComponent(boxUseReadInSpringConstant, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0);

        int row = 1;

        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            JSpinnerNumeric fieldSensitivity = fieldsSensitivity.get(signalType);
            JCheckBox boxUseReadInSensitivity = boxesUseReadInSensitivity.get(signalType);
            JLabel labelSensitivity = labelsSensitivity.get(signalType);
            JButton buttonCalibrate = buttonsCalibrate.get(signalType);

            labelSensitivity.setHorizontalAlignment(SwingConstants.RIGHT);
            labelSensitivity.setLabelFor(fieldSensitivity);
            labelSensitivity.setDisplayedMnemonic(KeyEvent.VK_S);

            calibrationPanel.addComponent(labelSensitivity, 0, row, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
            calibrationPanel.addComponent(fieldSensitivity, 1, row, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0);
            calibrationPanel.addComponent(boxUseReadInSensitivity, 2, row, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0);

            calibrationPanel.addComponent(buttonCalibrate, 1, row + 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0);

            row = row + 2;
        }

        calibrationPanel.addComponent(Box.createVerticalGlue(), 0, row + 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1);

        calibrationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Calibration"));

        return calibrationPanel;
    }

    private void setSensitivityPanelConsistentWithPhotodiodeSignals(Set<PhotodiodeSignalType> signalsPresent)
    {
        for(PhotodiodeSignalType signalType : PhotodiodeSignalType.values())
        {
            JSpinnerNumeric fieldSensitivity = fieldsSensitivity.get(signalType);
            JCheckBox boxUseReadInSensitivity = boxesUseReadInSensitivity.get(signalType);
            JLabel labelSensitivity = labelsSensitivity.get(signalType);
            JButton buttonCalibrate = buttonsCalibrate.get(signalType);

            //if the signalsPresent set is empty, we will show all
            boolean signalPresent = signalsPresent.contains(signalType) || (signalsPresent.isEmpty());

            fieldSensitivity.setVisible(signalPresent);
            boxUseReadInSensitivity.setVisible(signalPresent);
            labelSensitivity.setVisible(signalPresent);
            buttonCalibrate.setVisible(signalPresent);
        }
    }

    private SubPanel buildTrimmingPanel()
    {
        JLabel labelLeft = new JLabel("Left (μm)");
        JLabel labelRight = new JLabel("Right (μm)");
        JLabel labelLower = new JLabel("Lower (nN)");
        JLabel labelUpper = new JLabel("Upper (nN)");

        boxTrimDomain.setMnemonic(KeyEvent.VK_D);
        boxTrimRange.setMnemonic(KeyEvent.VK_R);
        boxTrimRange.setDisplayedMnemonicIndex(5);

        SubPanel trimmingPanel = new SubPanel();

        trimmingPanel.addComponent(labelLeft, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelRight, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(boxTrimDomain, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(fieldTrimLeft, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(fieldTrimRight, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);			
        trimmingPanel.addComponent(buttonSelectTrimming, 2, 1, 1, 5, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelLower, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelUpper, 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(boxTrimRange, 0, 3, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(fieldTrimLower, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(fieldTrimUpper, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);			
        trimmingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Cropping"));

        return trimmingPanel;
    }

    private SubPanel buildConstraintsPanel()
    {
        JLabel labelForceMaximum = new JLabel("Max load (nN)");
        JLabel labelIndentMaximum = new JLabel("Max indent (μm)");

        fieldLoadMaximum.setValue(Double.POSITIVE_INFINITY);
        fieldIndentMaximum.setValue(Double.POSITIVE_INFINITY);

        SubPanel constraintsPanel = new SubPanel();

        constraintsPanel.addComponent(labelForceMaximum, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        constraintsPanel.addComponent(fieldLoadMaximum, 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        constraintsPanel.addComponent(buttonLoadInfinity, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        constraintsPanel.addComponent(labelIndentMaximum, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        constraintsPanel.addComponent(fieldIndentMaximum, 1, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        constraintsPanel.addComponent(buttonIndentInfinity, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        constraintsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Constraints"));

        return constraintsPanel;
    }

    private JPanel buildSmoothingPanel()
    {			
        final JPanel smoothingDetails = new JPanel(new CardLayout());
        JPanel panelCombo = new JPanel();
        SubPanel cardSavitzkyGolay = new SubPanel();
        SubPanel cardLocalRegression = new SubPanel();

        boxSmooth.setMnemonic(KeyEvent.VK_M);

        smoothingDetails.add(cardLocalRegression, SmootherType.LOCAL_REGRESSION.toString());
        smoothingDetails.add(cardSavitzkyGolay, SmootherType.SAVITZKY_GOLAY.toString());

        fieldSavitzkySpan.setEnabled(false);
        fieldSavitzkyDegree.setEnabled(false);
        fieldLoessSpan.setEnabled(false);
        fieldIterLocal.setEnabled(false);
        comboSmoothers.setEnabled(false);

        panelCombo.add(boxSmooth);
        panelCombo.add(comboSmoothers);
        comboSmoothers.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent event) 
            {
                CardLayout cl = (CardLayout)(smoothingDetails.getLayout());
                cl.show(smoothingDetails, event.getItem().toString());
            }

        });

        JPanel smoothingPanel = new JPanel(new BorderLayout());

        smoothingPanel.add(smoothingDetails,BorderLayout.CENTER);
        smoothingPanel.add(panelCombo,BorderLayout.SOUTH);

        JLabel labelSpanSavitzky = new JLabel("Half-width (pts)", SwingConstants.RIGHT);
        JLabel labelDegreeSavitzky = new JLabel("Degree", SwingConstants.RIGHT);

        JLabel labelSpanLoess = new JLabel("Span (%)", SwingConstants.RIGHT);
        JLabel labelIterLoess = new JLabel("Iterations", SwingConstants.RIGHT);

        cardSavitzkyGolay.addComponent(labelSpanSavitzky, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardSavitzkyGolay.addComponent(fieldSavitzkySpan, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        cardSavitzkyGolay.addComponent(labelDegreeSavitzky, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardSavitzkyGolay.addComponent(fieldSavitzkyDegree, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        cardLocalRegression.addComponent(labelSpanLoess, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardLocalRegression.addComponent(fieldLoessSpan, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        cardLocalRegression.addComponent(labelIterLoess, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardLocalRegression.addComponent(fieldIterLocal, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        smoothingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Smoothing"));

        return smoothingPanel;
    }

    private JPanel buildGeneralPanel()
    {
        SubPanel generalPanel = new SubPanel();

        JPanel namePanel = buildNamePanel();
        JPanel samplePanel = buildSamplePanel();
        JPanel baselinePanel = buildBaselinePanel();
        SubPanel calibrationPanel = buildCalibrationPanel();
        SubPanel modelPanel = buildModelPanel();
        SubPanel contactPanel = buildProcessingPanel();

        generalPanel.addComponent(namePanel, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        generalPanel.addComponent(contactPanel, 0, 1, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        generalPanel.addComponent(modelPanel, 0, 3, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);	
        generalPanel.addComponent(samplePanel, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        generalPanel.addComponent(baselinePanel, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        generalPanel.addComponent(calibrationPanel, 1, 3, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 1, 1);

        return generalPanel;
    }

    private JPanel buildAdvancedPanel()
    {
        JPanel advancedPanel = new JPanel(new BorderLayout());
        SubPanel inner = new SubPanel();

        JPanel smoothingPanel = buildSmoothingPanel();
        JPanel trimmingPanel = buildTrimmingPanel();
        JPanel constraintsPanel = buildConstraintsPanel();
        //        JPanel jumpsPanel = buildJumpsPanel();
        JPanel substrateCorrectionPanel = buildSubstrateCorrectionPanel();

        inner.addComponent(trimmingPanel, 0, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        inner.addComponent(smoothingPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        inner.addComponent(constraintsPanel, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        //        inner.addComponent(jumpsPanel, 0, 3, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        inner.addComponent(substrateCorrectionPanel, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        advancedPanel.add(inner, BorderLayout.NORTH);
        return advancedPanel;
    }

    private JPanel buildOutputPanel()
    {
        SubPanel panelPlots = new SubPanel();

        JLabel labelRecordedCurve = new JLabel("Recorded cure");
        JLabel labelIndentationCurve = new JLabel("Indentation");
        JLabel labelModuluCurve = new JLabel("Pointwise modulus");

        panelPlots.addComponent(labelRecordedCurve, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotRecordedCurve, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelPlots.addComponent(boxPlotRecordedCurveFit, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelPlots.addComponent(labelIndentationCurve, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotIndentation, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelPlots.addComponent(boxPlotIndentationFit, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelPlots.addComponent(labelModuluCurve, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotModulus, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);
        panelPlots.addComponent(boxPlotModulusFit, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelPlots.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Graphs"));

        boxPlotRecordedCurve.setMnemonic(KeyEvent.VK_R);
        boxPlotIndentation.setMnemonic(KeyEvent.VK_D);
        boxPlotModulus.setMnemonic(KeyEvent.VK_P);

        SubPanel panelAveragedCurves = new SubPanel();

        panelAveragedCurves.addComponent(boxShowAveragedRecordedCurve, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelAveragedCurves.addComponent(boxShowAveragedIndentation, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        panelAveragedCurves.addComponent(boxShowAveragedPointwiseModulus, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelAveragedCurves.addComponent(new JLabel("Error bars"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelAveragedCurves.addComponent(comboErrorBarType, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        panelAveragedCurves.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Averaged curves"));

        SubPanel panelMaps = new SubPanel();

        panelMaps.addComponent(boxIncludeCurvesInMaps, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelMaps.addComponent(boxPlotMapAreaImages, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelMaps.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Maps"));

        SubPanel panelOptionalCalculations = new SubPanel();

        panelOptionalCalculations.addComponent(boxCalculateAdhesionForce, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelOptionalCalculations.addComponent(boxCalculateRSquared, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelOptionalCalculations.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Optional calculations"));

        JPanel outputPanel = new JPanel(new BorderLayout());

        SubPanel innerPanel = new SubPanel();
        innerPanel.addComponent(panelPlots, 0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(panelMaps, 0, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(panelAveragedCurves, 0, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(panelOptionalCalculations, 0, 3, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        outputPanel.add(innerPanel, BorderLayout.NORTH);

        return outputPanel;
    }

    public void browseForSampleTopography() 
    {
        File dir = model.getCommonSourceDirectory();
        substrateSelectionWizard.showDialog(dir);             
    }

    private class BrowseForSampleTopographyAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BrowseForSampleTopographyAction() 
        {
            putValue(NAME, "Browse");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            browseForSampleTopography();
        }
    }

    private class SmoothTipTransitionAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SmoothTipTransitionAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/SmoothTransitionRadiusSimple.png"));

            putValue(LARGE_ICON_KEY, icon);            
            putValue(SHORT_DESCRIPTION, "Calculate smooth transition");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            model.setSmoothTransitionRadius();
        }
    }

    private class ImportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ImportAction() 
        {           
            putValue(NAME, "Import");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            propertiesChooser.setApproveButtonMnemonic(KeyEvent.VK_O);
            int op = propertiesChooser.showOpenDialog(mainPanel);
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = propertiesChooser.getSelectedFile();
                loadProperties(f);              
            }
        }
    }

    private class ExportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExportAction() 
        {           
            putValue(NAME, "Export");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            propertiesChooser.setApproveButtonMnemonic(KeyEvent.VK_S);

            int op = propertiesChooser.showSaveDialog(mainPanel);
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = propertiesChooser.getSelectedFile();
                saveProperties(f);
            }
        }
    }

    private class CalibrateAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;
        private final PhotodiodeSignalType photodiodeSignalType;

        public CalibrateAction(PhotodiodeSignalType photodiodeSignalType) 
        {           
            putValue(NAME, "Calibrate");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);

            this.photodiodeSignalType = photodiodeSignalType;
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            calibrationDialog.showDialog(photodiodeSignalType);
        }
    }

    private class CroppingSpecificationAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public CroppingSpecificationAction() 
        {           
            putValue(NAME, "Select");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            croppingDialog.showDialog(ForceCurvePlotFactory.getInstance(), model.getCroppingModel(), model);
            croppingDialog.setVisible(true);
        }
    }

    private class LoadToInfinityAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LoadToInfinityAction() 
        {
            putValue(NAME, "\u221E");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {                   
            model.setLoadLimit(Double.POSITIVE_INFINITY);
        }
    }

    private class IndentationToInfinityAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public IndentationToInfinityAction() 
        {
            putValue(NAME, "\u221E");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {                   
            model.setIndentationLimit(Double.POSITIVE_INFINITY);
        }
    }


    @Override
    public boolean isBackEnabled() {
        return false;
    }

    @Override
    public boolean isNextEnabled() {
        return false;
    }

    @Override
    public boolean isSkipEnabled() {
        return false;
    }

    @Override
    public boolean isFinishEnabled() {
        return false;
    }
}
