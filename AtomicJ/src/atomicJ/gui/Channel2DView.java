
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

package atomicJ.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.PreviewDestination;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.DataAxis1D;
import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.gui.MouseInteractiveTool.MouseInteractiveToolListener;
import atomicJ.gui.annotations.ExportAnnotationModel;
import atomicJ.gui.annotations.ExportAnnotationWizard;
import atomicJ.gui.boxplots.BasicBoxPlotPanel;
import atomicJ.gui.boxplots.BoxAndWhiskerResource;
import atomicJ.gui.boxplots.BoxAndWhiskerXYPlot;
import atomicJ.gui.boxplots.BoxPlotSimpleView;
import atomicJ.gui.boxplots.KnownSamplesBoxAndWhiskerWizard;
import atomicJ.gui.boxplots.LiveBoxPlotView;
import atomicJ.gui.editors.ROIMaskEditor;
import atomicJ.gui.histogram.HistogramResource;
import atomicJ.gui.histogram.HistogramWizard;
import atomicJ.gui.histogram.HistogramWizardModel;
import atomicJ.gui.histogram.HistogramWizardModelDoubleSelection;
import atomicJ.gui.histogram.HistogramWizardModelSingleSelection;
import atomicJ.gui.histogram.LiveHistogramView;
import atomicJ.gui.histogram.TransformableHistogramView;
import atomicJ.gui.imageProcessing.UndoableImageROICommand;
import atomicJ.gui.imageProcessing.UndoableTransformationCommand;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.imageProcessingActions.AddPlaneAction;
import atomicJ.gui.imageProcessingActions.Convolve2DAction;
import atomicJ.gui.imageProcessingActions.FloodFillAction;
import atomicJ.gui.imageProcessingActions.GaussianFilter2DAction;
import atomicJ.gui.imageProcessingActions.ImageMathAction;
import atomicJ.gui.imageProcessingActions.LaplacianOfGaussianFilterAction;
import atomicJ.gui.imageProcessingActions.LineFitCorrectionAction;
import atomicJ.gui.imageProcessingActions.LineMatchingCorrectionAction;
import atomicJ.gui.imageProcessingActions.Median2DFilterAction;
import atomicJ.gui.imageProcessingActions.MedianWeightedFilter2DAction;
import atomicJ.gui.imageProcessingActions.ReplaceDataAction;
import atomicJ.gui.imageProcessingActions.Gridding2DAction;
import atomicJ.gui.imageProcessingActions.RotateByArbitraryAngleAction;
import atomicJ.gui.imageProcessingActions.SubtractPolynomialBackgroundAction;
import atomicJ.gui.imageProcessingActions.ThresholdDataAction;
import atomicJ.gui.imageProcessingActions.UnsharpMaskAction;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementEditor;
import atomicJ.gui.measurements.DistanceMeasurementReceiver;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.GeneralUnionMeasurement;
import atomicJ.gui.measurements.MeasurementProxy;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionsView;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.profile.ProfileEditor;
import atomicJ.gui.profile.ProfileReceiver;
import atomicJ.gui.profile.ProfileProxy;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.gui.rois.ComplementROIModel;
import atomicJ.gui.rois.ComplementROIWizard;
import atomicJ.gui.rois.DifferenceROI;
import atomicJ.gui.rois.DifferenceROIModel;
import atomicJ.gui.rois.DifferenceROIWizard;
import atomicJ.gui.rois.ModifyObjectsModel;
import atomicJ.gui.rois.ModifyObjectsWizard;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIEditor;
import atomicJ.gui.rois.GeneralUnionROI;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.rois.ROIRectangle;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIShapeFactorsTableModel;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.gui.rois.ROIProxy;
import atomicJ.gui.rois.SplitROIsAction;
import atomicJ.gui.rois.WandContourTracer;
import atomicJ.gui.rois.WandROIDialog;
import atomicJ.gui.rois.WandROIModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.SampleSelectionModel;
import atomicJ.gui.stack.AnimationDialog;
import atomicJ.gui.stack.AnimationModel;
import atomicJ.gui.stack.StackView;
import atomicJ.gui.stack.StackMapChart;
import atomicJ.gui.stack.StackModel;
import atomicJ.gui.statistics.StatisticsView;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.gui.statistics.UpdateableStatisticsView;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.imageProcessing.FixMinimumOperation;
import atomicJ.imageProcessing.FlipHorizontally;
import atomicJ.imageProcessing.FlipVertically;
import atomicJ.imageProcessing.FlipZ;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.imageProcessing.Kernel2D;
import atomicJ.imageProcessing.KernelConvolution;
import atomicJ.imageProcessing.KernelSharpen;
import atomicJ.imageProcessing.PolynomialFitCorrection;
import atomicJ.imageProcessing.PrewittOperator;
import atomicJ.imageProcessing.RotateClockwise;
import atomicJ.imageProcessing.RotateCounterClockwise;
import atomicJ.imageProcessing.SobelOperator;
import atomicJ.imageProcessing.Transpose;
import atomicJ.resources.CrossSectionResource;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.resources.Resource;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;

public abstract class Channel2DView<R extends Channel2DResource, V extends Channel2DChart<?>, E extends Channel2DPanel<V>>
extends ResourceXYView<R, V, E> implements Channel2DSupervisor, Channel2DResourceView, 
ColorGradientReceiver, RawDataDestination, PreviewDestination, Overlayable 
{
    private final Action duplicateImageAction = new DuplicateImageAction();

    private final Action editGradientAction = new EditColorGradientAction();
    private final Action editHistogramGradientAction = new EditHistogramGradientAction();
    private final Action roiMaskAction = new ROIMaskAction();

    private final Action overlayAction = new OverlayAction();
    private final Action showRawDataAction = new ShowAllRawDataAction();
    private final Action showROIRawDataAction = new ShowROIRawDataAction();
    private final Action showStatisticsAction = new ShowStatisticsAction();	

    private final Action animateAction = new AnimateAction();

    private final Action resizeAction = new Gridding2DAction(this);
    private final Action fixZeroAction = new FixZeroAction();

    private final Action flipHorizontallyAction = new FlipHorizontallyAction();
    private final Action flipVerticallyAction = new FlipVerticallyAction();
    private final Action transposeAction = new TransposeAction();
    private final Action flipZAction = new FlipZAction();
    private final Action rotateClockwiseAction = new RotateClockwiseAction();
    private final Action rotateCounterClockwiseAction = new RotateCounterClockwiseAction();
    private final Action rotateArbitraryAngleAction = new RotateByArbitraryAngleAction(this);

    private final Action subtractPolynomialBackgroundAction = new SubtractPolynomialBackgroundAction(this);
    private final Action lineMatchingCorrectionAction = new LineMatchingCorrectionAction(this);
    private final Action lineFitCorrectionAction = new LineFitCorrectionAction(this);

    private final Action addPlaneAction = new AddPlaneAction(this);
    private final Action replaceDataAction = new ReplaceDataAction(this);
    private final Action thresholdDataAction = new ThresholdDataAction(this);
    private final Action floodFillAction = new FloodFillAction(this);

    private final Action medianFilterAction = new Median2DFilterAction(this);
    private final Action medianWeightedFilterAction = new MedianWeightedFilter2DAction(this);

    private final Action sharpenAction = new SharpenAction();
    private final Action unsharpMaskAction = new UnsharpMaskAction(this);
    private final Action gaussianAction = new GaussianFilter2DAction(this);
    private final Action sobelAction = new SobelOperatorAction();
    private final Action prewittAction = new PrewittOperatorAction();
    private final Action horizontalDerivativeAction = new HorizontalDerivativeAction();
    private final Action verticalDerivativeAction = new VerticalDerivativeAction();
    private final Action laplacianGaussianAction = new LaplacianOfGaussianFilterAction(this);

    private final Action convolveAction = new Convolve2DAction(this);

    private final Action imageMathAction = new ImageMathAction(this);

    private final Action showHistogramsAction = new ShowHistogramsAction();
    private final Action drawROIHistogramsAction = new DrawROIHistogramsAction();
    private final Action showLiveHistogramAction = new ShowLiveHistogramAction();
    private final Action drawHistogramsAction = new DrawHistogramAction();

    private final Action showBoxPlotsAction = new ShowBoxPlotsAction();
    private final Action showLiveBoxPlotsAction = new ShowLiveBoxPlotsAction();
    private final Action drawROIBoxPlotsAction = new DrawROIBoxPlotsAction();

    private final Action subtractROIFitAction = new SubtractROIPlaneAction();
    private final Action showROIStatisticsAction = new ShowROIStatisticsAction();
    private final Action showROIShapeFactorsAction = new ShowROIShapeFactors();
    private final Action showProfileGeometryAction = new ShowProfileGeometryAction();
    private final Action modifyProfileStyleAction = new ModifyProfileStyleAction();
    private final Action lockAspectRatioAction = new LockAspectRatioAction();

    private final Action addMapMarkerAction = new AddMapMarkerAction();
    private final Action modifyMapMarkerStyleAction = new ModifyMapMarkerStyleAction();

    private final JMenuItem addMapMarkerItem = new JCheckBoxMenuItem(addMapMarkerAction);
    private final JMenuItem modifyMapMarkerStyleItem = new JMenuItem(modifyMapMarkerStyleAction);

    private final Action measurePolyLineAction = new MeasurePolyLineAction();
    private final Action measureFreeHandAction = new MeasureFreeAction();

    private final Action mergeMeasurementsAction = new MergeMeasurementsAction();

    private final Action exportMeasurementsAction = new ExportMeasurementsAction();
    private final Action importMeasurementsAction = new ImportMeasurementsAction();

    private final Action modifyROIStyleAction = new ModifyROIStyleAction();

    private final Action roiHoleAction = new ROIHoleAction();
    private final Action roiPolygonalAction = new ROIPolygonAction();
    private final Action roiRectangularAction = new ROIRectangularAction();
    private final Action roiElipticAction = new ROIElipticAction();
    private final Action roiFreeAction = new ROIFreeAction();
    private final Action roiWandAction = new ROIWandAction();
    private final Action convexHullROIsAction = new ConvexHullROIsAction();
    private final Action mergeROIsAction = new MergeROIsAction();
    private final Action subtractROIsAction = new SubtractROIsAction();
    private final Action complementROIsAction = new ComplementROIsAction();
    private final Action splitROIsAction = new SplitROIsAction(this);
    private final Action exportROIsAction = new ExportROIsAction();
    private final Action importROIsAction = new ImportROIsAction();
    private final Action rotateROIAction = new RotateROIAction();

    private final Action extractLineProfile = new ExtractProfileLine();
    private final Action extractPolyLineProfile = new ExtractProfilePolyline();
    private final Action extractFreeHandProfile = new ExtractProfileFreeHand();

    private final Action exportProfilesAction = new ExportProfilesAction();
    private final Action importProfilesAction = new ImportProfilesAction();

    private final Action showCrossSectionsDialogAction = new ShowCrossSectionsDialogAction();

    private final Action undoAction = new UndoAction();
    private final Action undoAllAction = new UndoAllAction();
    private final Action redoAction = new RedoAction();
    private final Action redoAllAction = new RedoAllAction();


    private final MainView parent;
    private final TransformableHistogramView histogramView;
    private final OverlayWizard overlayWizard;

    private final MultipleSelectionWizard<String> rawDataWizard = new MultipleSelectionWizard<>(getAssociatedWindow(), "Raw data assistant");

    private final LiveHistogramView liveHistogramView = new LiveHistogramView(getAssociatedWindow(), "Live ROI histogram", ModalityType.MODELESS, false, true);

    private HistogramWizard histogramsWizard;

    private final BoxPlotSimpleView<BasicBoxPlotPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> boxPlotView = new BoxPlotSimpleView<>(getAssociatedWindow(), new BasicBoxPlotPanel.BasicBoxPlotPanelFactory(), "Box plots");
    private final LiveBoxPlotView<BasicBoxPlotPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> liveBoxPlotView = new LiveBoxPlotView<>(getAssociatedWindow(), new BasicBoxPlotPanel.BasicBoxPlotPanelFactory(),"Live ROI boxplots", ModalityType.MODELESS, false, true);

    private KnownSamplesBoxAndWhiskerWizard boxPlotWizard;

    private final UpdateableStatisticsView roiStatisticsView = new UpdateableStatisticsView(getAssociatedWindow(), "Statistics for ROIs", Preferences.userRoot().node(getClass().getName()).node("ROISampleStatistics"),true);
    private final UpdateableStatisticsView channelStatisticsView = new UpdateableStatisticsView(getAssociatedWindow(), "Statistics",Preferences.userRoot().node(getClass().getName()).node("ChannelSampleStatistics"),true);
    private final CrossSectionsView crossSectionsView;

    private final FlexibleNumericalTableView roiShapeFactorsView = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new ROIShapeFactorsTableModel(Units.MICRO_METER_UNIT), true, true), "ROI shape factors");
    private final FlexibleNumericalTableView profileGeometryView = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Image profile geometry");
    private final FlexibleNumericalTableView distanceMeasurementsView = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Image distance measurements");

    private final ROIEditor roiEditor = new ROIEditor(getAssociatedWindow());
    private final ProfileEditor profileEditor = new ProfileEditor(getAssociatedWindow(), "Profile style");
    private final MapMarkerEditor mapMarkerEditor = new MapMarkerEditor(getAssociatedWindow());
    private final DistanceMeasurementEditor measurementEditor = new DistanceMeasurementEditor(getAssociatedWindow());

    private GradientSelectionDialog gradientSelectionDialog;
    private RangeGradientChooser rangeGradientChooser;
    private RangeHistogramView rangeGradientHistogramChooser;
    private ROIMaskEditor roiMaskEditor;

    private ModifyObjectsWizard measurementMergeWizard;

    private final WandROIDialog wandROIDialog = new WandROIDialog(getAssociatedWindow(), "Wand ROI", false);
    private final WandROIModel wandROIModel = new WandROIModel(this);
    private ModifyObjectsWizard roiMergeWizard;
    private DifferenceROIWizard roiDifferenceWizard;
    private ComplementROIWizard complementROIWizard;
    private ModifyObjectsWizard roiConvexHullWizard;
    private ExportAnnotationWizard exportAnnotationWizard;

    private final ExtensionFileChooser roiImportChooser;
    private final ExtensionFileChooser profileImportChooser;
    private final ExtensionFileChooser measurementImportChooser;

    private final JToolBar toolBar;

    private final List<ProfileReceiver> profileReceivers = new ArrayList<>();
    private final List<DistanceMeasurementReceiver> distanceMeasurementReceivers = new ArrayList<>();
    private final List<ROIReceiver> roiReceivers = new ArrayList<>();
    private final List<MapMarkerReceiver> mapMarkerReceivers = new ArrayList<>();
    private MouseInteractiveTool mouseInteractiveTool = null;
    private final CustomMouseInteractiveToolListener mouseInteractiveToolListener = new CustomMouseInteractiveToolListener();

    private final ModelChangeListener listener = new ModelChangeListener();


    public Channel2DView(final MainView parent, AbstractChartPanelFactory<E> factory,
            TransformableHistogramView histogramDialog, String title,
            Preferences pref, ModalityType modalityType) 
    {
        super(parent.getPublicationSite(), factory, title, pref, modalityType, new Channel2DDialogModel<R>());

        this.parent = parent;
        this.crossSectionsView = new CrossSectionsView(parent.getPublicationSite(), this);
        this.histogramView = histogramDialog;
        this.overlayWizard = new OverlayWizard(new OverlayPreviewDestination(), this);

        this.roiReceivers.add(wandROIModel);

        this.roiImportChooser = new ExtensionFileChooser(pref, "ROI file (.roi)", "roi", true);
        this.profileImportChooser = new ExtensionFileChooser(pref, "Profile file (.profile)", "profile", true);
        this.measurementImportChooser = new ExtensionFileChooser(pref, "Measurement file (.measurement)", "measurement", true);

        JSplitPane mainPane = buildMainPane();
        this.toolBar = buildToolBar();

        setCenterComponent(mainPane);
        setSouthComponent(getMultipleResourcesPanelButtons());
        setEastComponent(toolBar);

        buildMenuBar();

        boolean histogramsAvailable = !histogramDialog.isEmpty();
        setHistogramsAvailable(histogramsAvailable);

        boolean boxPlotsAvailable = !boxPlotView.isEmpty();
        setBoxPlotsAvailable(boxPlotsAvailable);

        boolean showCrossSectionsEnabled = !crossSectionsView.isEmpty();
        showCrossSectionsDialogAction.setEnabled(showCrossSectionsEnabled);

        initViewListeners();
        createAndRegisterResourceListPopupMenu();
        controlForResourceEmptinessPrivate(isEmpty());

        initInputAndActionMaps();
    }

    @Override
    public Channel2DDialogModel<R> getResourceModel()
    {
        return (Channel2DDialogModel<R>)super.getResourceModel();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers()
    {
        return getResourceModel().getAllResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(filter);
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public void addResourceSelectionListener(SelectionListener<? super Channel2DResource> listener)
    {
        getResourceModel().addSelectionListener(listener);
    }

    @Override
    public void removeResourceSelectionListener(SelectionListener<? super Channel2DResource> listener)
    {
        getResourceModel().removeSelectionListener(listener);
    }

    @Override
    public void addResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        getResourceModel().addDataModelListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        getResourceModel().removeDataModelListener(listener);
    }

    @Override
    public void addResourceTypeListener(ResourceTypeListener listener)
    {
        getResourceModel().addResourceTypeListener(listener);
    }

    @Override
    public void removeResourceTypeListener(ResourceTypeListener listener)
    {
        getResourceModel().removeResourceTypeListener(listener);
    }

    @Override
    public void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew)
    {
        super.setConsistentWithMode(modeOld, modeNew);

        boolean isPolyLineMeasurement = MouseInputModeStandard.DISTANCE_MEASUREMENT_POLYLINE.equals(modeNew);
        boolean isFreeMeasurement = MouseInputModeStandard.DISTANCE_MEASUREMENT_FREEHAND.equals(modeNew);

        boolean isProfiles = modeNew.isProfile();

        boolean isLineProfile = MouseInputModeStandard.PROFILE_LINE.equals(modeNew);
        boolean isPolylineProfile = MouseInputModeStandard.PROFILE_POLYLINE.equals(modeNew);
        boolean isFreeHandProfile = MouseInputModeStandard.PROFILE_FREEHAND.equals(modeNew);

        boolean isROI = modeNew.isROI();

        boolean isPolygonROI = MouseInputModeStandard.POLYGON_ROI.equals(modeNew);
        boolean isRectangularROI = MouseInputModeStandard.RECTANGULAR_ROI.equals(modeNew);
        boolean isElipticROI = MouseInputModeStandard.ELIPTIC_ROI.equals(modeNew);
        boolean isFreeHandROI = MouseInputModeStandard.FREE_HAND_ROI.equals(modeNew);
        boolean isWandROI = MouseInputModeStandard.WAND_ROI.equals(modeNew);

        boolean isInsertMapMarkerMode = MouseInputModeStandard.INSERT_MAP_MARKER.equals(modeNew);

        if(isProfiles && (!(crossSectionsView.isEmpty() || crossSectionsView.isVisible())))
        {
            showCrossSectionsDialog();         
        }

        measurePolyLineAction.putValue(Action.SELECTED_KEY, isPolyLineMeasurement);
        measureFreeHandAction.putValue(Action.SELECTED_KEY, isFreeMeasurement);

        extractLineProfile.putValue(Action.SELECTED_KEY, isLineProfile);
        extractPolyLineProfile.putValue(Action.SELECTED_KEY, isPolylineProfile);
        extractFreeHandProfile.putValue(Action.SELECTED_KEY, isFreeHandProfile);

        roiHoleAction.setEnabled(isROI);
        roiPolygonalAction.putValue(Action.SELECTED_KEY, isPolygonROI);
        roiRectangularAction.putValue(Action.SELECTED_KEY, isRectangularROI);
        roiElipticAction.putValue(Action.SELECTED_KEY, isElipticROI);
        roiFreeAction.putValue(Action.SELECTED_KEY, isFreeHandROI);
        roiWandAction.putValue(Action.SELECTED_KEY, isWandROI);

        addMapMarkerAction.putValue(Action.SELECTED_KEY, isInsertMapMarkerMode);

        boolean wandModeAcquired = !MouseInputModeStandard.WAND_ROI.equals(modeOld)
                && MouseInputModeStandard.WAND_ROI.equals(getMode());

        boolean wandModeLost = MouseInputModeStandard.WAND_ROI.equals(modeOld)
                && !MouseInputModeStandard.WAND_ROI.equals(getMode());


        if(wandModeAcquired)
        {
            showWandROIDialog();
        }
        else if(wandModeLost)
        {
            wandROIDialog.setVisible(false);
        }
    }

    private void showWandROIDialog()
    {
        V chart = getSelectedChart();
        updateWandROIUnits(chart);
        wandROIDialog.showDialog(wandROIModel);
    }

    @Override
    protected void setConsistentWithAccessoryMode(MouseInputType inputType, MouseInputMode modeOld, MouseInputMode modeNew)
    {
        super.setConsistentWithAccessoryMode(inputType, modeOld, modeNew);

        boolean toolModeLost = MouseInputModeStandard.TOOL_MODE.equals(modeOld) && !MouseInputModeStandard.TOOL_MODE.equals(getMode());

        if(toolModeLost && mouseInteractiveTool != null)
        {
            mouseInteractiveTool.notifyOfToolModeLoss();
            mouseInteractiveTool.removeMouseToolListerner(mouseInteractiveToolListener);

            this.mouseInteractiveTool = null;
        }

        if(MouseInputType.DRAGGED.equals(inputType))
        {
            boolean isRotateROI = MouseInputModeStandard.ROTATE_ROI.equals(modeNew);
            rotateROIAction.putValue(Action.SELECTED_KEY, isRotateROI);
        }
    }

    @Override
    public WandContourTracer getWandTracer()
    {
        return wandROIModel.getContourTracer();
    }

    @Override
    public void notifyToolsOfMouseClicked(CustomChartMouseEvent evt)
    {
        if(mouseInteractiveTool != null)
        {
            mouseInteractiveTool.mouseClicked(evt);
        }
    }

    @Override
    public void notifyToolsOfMouseDragged(CustomChartMouseEvent evt)
    {
        if(mouseInteractiveTool != null)
        {
            mouseInteractiveTool.mouseDragged(evt);
        }
    }

    @Override
    public void notifyToolsOfMouseMoved(CustomChartMouseEvent evt)
    {
        if(mouseInteractiveTool != null)
        {
            mouseInteractiveTool.mouseMoved(evt);
        }
    }

    @Override
    public void notifyToolsOfMousePressed(CustomChartMouseEvent evt)
    {
        if(mouseInteractiveTool != null)
        {
            mouseInteractiveTool.mousePressed(evt);
        }
    }

    @Override
    public void notifyToolsOfMouseReleased(CustomChartMouseEvent evt)
    {
        if(mouseInteractiveTool != null)
        {
            mouseInteractiveTool.mouseReleased(evt);
        }
    }

    @Override
    public void useMouseInteractiveTool(MouseInteractiveTool toolNew)
    {
        MouseInteractiveTool toolOld = this.mouseInteractiveTool;

        if(toolOld != null)
        {
            toolOld.removeMouseToolListerner(mouseInteractiveToolListener);
        }

        this.mouseInteractiveTool = toolNew;

        if(this.mouseInteractiveTool != null)
        {
            mouseInteractiveTool.addMouseToolListener(mouseInteractiveToolListener);
            setMouseInputsReservedForTools(mouseInteractiveTool.getUsedMouseInputTypes());

            V selectedChart = getSelectedChart();
            if(selectedChart != null)
            {
                selectedChart.notifyAboutToolChange();
            }
        }

        if(toolOld != null)
        {
            toolOld.notifyOfToolModeLoss();
        }      
    }

    @Override
    public void stopUsingMouseInteractiveTool(MouseInteractiveTool tool)
    {
        if(Objects.equals(this.mouseInteractiveTool, tool))
        {
            this.mouseInteractiveTool = null;
            setMouseInputsReservedForTools(Collections.<MouseInputType>emptySet());

            if(tool != null)
            {
                tool.notifyOfToolModeLoss();
                tool.removeMouseToolListerner(mouseInteractiveToolListener);
            }

            V selectedChart = getSelectedChart();
            if(selectedChart != null)
            {
                selectedChart.notifyAboutToolChange();
            }
        }
    }

    @Override
    public MouseInteractiveTool getCurrentlyUsedInteractiveTool()
    {
        return mouseInteractiveTool;
    }

    @Override
    public boolean isChartElementCaughtByTool()
    {
        boolean caught = this.mouseInteractiveTool != null ? mouseInteractiveTool.isChartElementCaught() : false;
        return caught;
    }

    @Override
    public boolean isComplexElementUnderConstructionByTool()
    {
        boolean underConstruction = this.mouseInteractiveTool != null ? mouseInteractiveTool.isComplexElementUnderConstruction() : false;
        return underConstruction;
    }

    @Override
    public boolean isRightClickReservedByTool(Rectangle2D dataArea, Point2D dataPoint)
    {
        boolean reserved = this.mouseInteractiveTool != null ? mouseInteractiveTool.isRightClickReserved(dataArea, dataPoint) : false;
        return reserved;
    }

    private void initViewListeners()
    {
        crossSectionsView.addDataViewListener(new BasicViewListener(showCrossSectionsDialogAction));
        boxPlotView.addDataViewListener(new BasicViewListener(showBoxPlotsAction));

        histogramView.addDataViewListener(new DataViewListener() {

            @Override
            public void dataViewVisibilityChanged(boolean visibleNew) {
                showHistogramsAction.putValue(Action.SELECTED_KEY, visibleNew);
            }

            @Override
            public void dataAvailabilityChanged(boolean availableNew)
            {
                showHistogramsAction.setEnabled(availableNew);

                for(Channel2DPanel<?> panel : getPanels())
                {
                    panel.setHistogramsAvialable(availableNew);
                }                
            }
        });

        liveHistogramView.addDataViewListener(new DataViewAdapter()
        {
            @Override
            public void dataViewVisibilityChanged(boolean visibleNew)
            {
                showLiveHistogramAction.putValue(Action.SELECTED_KEY, visibleNew);
            }
        });

        liveBoxPlotView.addDataViewListener(new DataViewAdapter()
        {
            @Override
            public void dataViewVisibilityChanged(boolean visibleNew)
            {
                showLiveBoxPlotsAction.putValue(Action.SELECTED_KEY, visibleNew);
            }
        });


        profileGeometryView.addDataViewListener(new BasicViewListener(showProfileGeometryAction));
    }

    private void buildMenuBar()
    {
        JMenuBar menuBar = getMenuBar();

        JMenuItem drawHistogramItem = new JMenuItem(drawHistogramsAction);
        JMenuItem drawHistogramROIItem = new JMenuItem(drawROIHistogramsAction);
        JMenuItem showLiveHistogramItem = new JCheckBoxMenuItem(showLiveHistogramAction);
        JMenuItem lockAspectRatioItem = new JCheckBoxMenuItem(lockAspectRatioAction); 

        JMenuItem duplicateImageItem = new JMenuItem(duplicateImageAction);

        JMenu fileMenu = getFileMenu();
        fileMenu.insert(duplicateImageItem, 6);

        JMenuItem measurePolyLineItem = new JCheckBoxMenuItem(measurePolyLineAction);
        JMenuItem measureFreeItem = new JCheckBoxMenuItem(measureFreeHandAction);
        JMenuItem mergeMeasurementsItem = new JMenuItem(mergeMeasurementsAction);
        JMenuItem exportMeasurementsItem = new JMenuItem(exportMeasurementsAction);
        JMenuItem importMeasurementsItem = new JMenuItem(importMeasurementsAction);

        JMenu measureMenu = getMeasurementMenu();
        measureMenu.add(measurePolyLineItem);
        measureMenu.add(measureFreeItem);

        measureMenu.addSeparator();
        measureMenu.add(mergeMeasurementsItem);
        measureMenu.addSeparator();
        measureMenu.add(exportMeasurementsItem);
        measureMenu.add(importMeasurementsItem);

        JMenu chartMenu = getChartMenu();

        JMenuItem editGradientItem = new JMenuItem(editGradientAction);
        JMenuItem editHistogramGradientItem = new JMenuItem(editHistogramGradientAction);

        chartMenu.add(editGradientItem);
        chartMenu.add(editHistogramGradientItem);
        chartMenu.addSeparator();

        JMenuItem overlayItem = new JMenuItem(overlayAction);
        JMenuItem animateItem = new JMenuItem(animateAction);

        chartMenu.add(overlayItem);
        chartMenu.add(animateItem);

        JMenu processMenu = new JMenu("Process");

        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem undoAllItem = new JMenuItem(undoAllAction);
        JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem redoAllItem = new JMenuItem(redoAllAction);

        processMenu.add(undoItem);
        processMenu.add(redoItem);
        processMenu.addSeparator();

        processMenu.add(undoAllItem);
        processMenu.add(redoAllItem);
        processMenu.addSeparator();

        JMenuItem fixZeroItem = new JMenuItem(fixZeroAction);
        JMenuItem subtractPolynomialBackgroundItem = new JMenuItem(subtractPolynomialBackgroundAction);
        JMenuItem lineMatchingCorrectionItem = new JMenuItem(lineMatchingCorrectionAction);
        JMenuItem lineFitCorrectionItem = new JMenuItem(lineFitCorrectionAction);

        processMenu.add(fixZeroItem);
        processMenu.add(subtractPolynomialBackgroundItem);
        processMenu.add(lineMatchingCorrectionItem);
        processMenu.add(lineFitCorrectionItem);

        processMenu.addSeparator();

        JMenuItem addPlaneItem = new JMenuItem(addPlaneAction);
        JMenuItem thresholdDataItem = new JMenuItem(thresholdDataAction);
        JMenuItem replaceImageDataItem = new JMenuItem(replaceDataAction);
        JMenuItem floodFillItem = new JMenuItem(floodFillAction);

        processMenu.add(thresholdDataItem);
        processMenu.add(addPlaneItem);
        processMenu.add(replaceImageDataItem);
        processMenu.add(floodFillItem);

        processMenu.addSeparator();

        JMenuItem imageMathItem = new JMenuItem(imageMathAction);
        JMenuItem convolveItem = new JMenuItem(convolveAction);
        JMenuItem resizeItem = new JMenuItem(resizeAction);

        processMenu.add(imageMathItem);
        processMenu.add(resizeItem);
        processMenu.add(convolveItem);

        JMenu transformSubMenu = new JMenu("Transform");

        JMenuItem flipHorizontallyItem = new JMenuItem(flipHorizontallyAction);
        JMenuItem flipVerticallyItem = new JMenuItem(flipVerticallyAction);
        JMenuItem flipBothItem = new JMenuItem(transposeAction);
        JMenuItem flipZItem = new JMenuItem(flipZAction);
        JMenuItem rotateClockwiseItem = new JMenuItem(rotateClockwiseAction);
        JMenuItem rotateCounterClockwiseItem = new JMenuItem(rotateCounterClockwiseAction);
        JMenuItem rotateArbitraryAngleItem = new JMenuItem(rotateArbitraryAngleAction);

        transformSubMenu.add(flipHorizontallyItem);
        transformSubMenu.add(flipVerticallyItem);
        transformSubMenu.add(flipBothItem);
        transformSubMenu.add(flipZItem);
        transformSubMenu.add(rotateClockwiseItem);
        transformSubMenu.add(rotateCounterClockwiseItem);
        transformSubMenu.add(rotateArbitraryAngleItem);

        processMenu.add(transformSubMenu);

        JMenu filterSubMenu = new JMenu("Filter");

        JMenuItem medianFilterItem = new JMenuItem(medianFilterAction);
        JMenuItem medianWeightedFilterItem = new JMenuItem(medianWeightedFilterAction);

        JMenuItem sharpenItem = new JMenuItem(sharpenAction);
        JMenuItem unsharpMaskItem = new JMenuItem(unsharpMaskAction);
        JMenuItem gaussianItem = new JMenuItem(gaussianAction);
        JMenuItem sobelItem = new JMenuItem(sobelAction);
        JMenuItem prewittItem = new JMenuItem(prewittAction);
        JMenuItem horizontalDerivativeItem = new JMenuItem(horizontalDerivativeAction);    
        JMenuItem verticalDerivativeItem = new JMenuItem(verticalDerivativeAction);
        JMenuItem laplacianGaussianItem = new JMenuItem(laplacianGaussianAction);


        filterSubMenu.add(medianFilterItem);
        filterSubMenu.add(medianWeightedFilterItem);
        filterSubMenu.add(sharpenItem);
        filterSubMenu.add(unsharpMaskItem);
        filterSubMenu.add(gaussianItem);
        filterSubMenu.add(horizontalDerivativeItem);
        filterSubMenu.add(verticalDerivativeItem);
        filterSubMenu.add(prewittItem);
        filterSubMenu.add(sobelItem);
        filterSubMenu.add(laplacianGaussianItem);

        processMenu.add(filterSubMenu);

        chartMenu.addSeparator();
        chartMenu.add(lockAspectRatioItem);

        JMenu dataMenu = new JMenu("Data");

        JMenuItem showHistogramsItem = new JCheckBoxMenuItem(showHistogramsAction);

        dataMenu.add(drawHistogramItem);
        dataMenu.add(drawHistogramROIItem);
        dataMenu.add(showHistogramsItem);              
        dataMenu.add(showLiveHistogramItem);

        JMenuItem showBoxPlotsItem = new JCheckBoxMenuItem(showBoxPlotsAction);
        JMenuItem drawROIBoxPlotsItem = new JMenuItem(drawROIBoxPlotsAction);
        JMenuItem showLiveBoxPlotsItem = new JCheckBoxMenuItem(showLiveBoxPlotsAction);

        dataMenu.addSeparator();
        dataMenu.add(drawROIBoxPlotsItem);
        dataMenu.add(showBoxPlotsItem);
        dataMenu.add(showLiveBoxPlotsItem);

        JMenuItem statisticsItem = new JMenuItem(showStatisticsAction);
        JMenuItem statisticsROIItem = new JMenuItem(showROIStatisticsAction);

        dataMenu.addSeparator();
        dataMenu.add(statisticsItem);
        dataMenu.add(statisticsROIItem);

        JMenuItem rawDataItem = new JMenuItem(showRawDataAction);
        JMenuItem rawDataROIItem = new JMenuItem(showROIRawDataAction);

        dataMenu.addSeparator();
        dataMenu.add(rawDataItem);
        dataMenu.add(rawDataROIItem);

        JMenu roisMenu = new JMenu("ROI");
        roisMenu.setMnemonic(KeyEvent.VK_R);

        JMenuItem modifyROIStyleItem = new JMenuItem(modifyROIStyleAction);
        JMenuItem roiMaskItem = new JMenuItem(roiMaskAction);

        JMenuItem shapeFactorsROIItem = new JMenuItem(showROIShapeFactorsAction);
        JMenuItem subtractROIFitItem = new JMenuItem(subtractROIFitAction);

        JMenuItem roiHoleItem = new JCheckBoxMenuItem(roiHoleAction);
        JMenuItem polygonROIItem = new JCheckBoxMenuItem(roiPolygonalAction);
        JMenuItem rectangularROIItem = new JCheckBoxMenuItem(roiRectangularAction);
        JMenuItem elipticalROIItem = new JCheckBoxMenuItem(roiElipticAction);
        JMenuItem freeROIItem = new JCheckBoxMenuItem(roiFreeAction);
        JMenuItem wandROIItem = new JCheckBoxMenuItem(roiWandAction);
        JMenuItem convexHullROIsItem = new JMenuItem(convexHullROIsAction);
        JMenuItem mergeROIsItem = new JMenuItem(mergeROIsAction);
        JMenuItem subtractROIsItem = new JMenuItem(subtractROIsAction);
        JMenuItem complementROIsItem = new JMenuItem(complementROIsAction);
        JMenuItem splitROIsItem = new JCheckBoxMenuItem(splitROIsAction);
        JMenuItem exportROIsItem = new JMenuItem(exportROIsAction);
        JMenuItem importROIsItem = new JMenuItem(importROIsAction);
        JMenuItem rotateROIItem = new JCheckBoxMenuItem(rotateROIAction);

        roisMenu.add(modifyROIStyleItem);
        roisMenu.add(roiMaskItem);
        roisMenu.addSeparator();
        roisMenu.add(shapeFactorsROIItem);
        roisMenu.addSeparator();
        roisMenu.add(subtractROIFitItem);
        roisMenu.addSeparator();
        roisMenu.add(polygonROIItem);
        roisMenu.add(rectangularROIItem);
        roisMenu.add(elipticalROIItem);
        roisMenu.add(freeROIItem);
        roisMenu.add(wandROIItem);
        roisMenu.addSeparator();
        roisMenu.add(mergeROIsItem);
        roisMenu.add(subtractROIsItem);    
        roisMenu.add(complementROIsItem);
        roisMenu.add(convexHullROIsItem);
        roisMenu.add(splitROIsItem);
        roisMenu.addSeparator();
        roisMenu.add(roiHoleItem);
        roisMenu.add(rotateROIItem);
        roisMenu.addSeparator();
        roisMenu.add(exportROIsItem);
        roisMenu.add(importROIsItem);

        JMenu profilesMenu = new JMenu("Profiles");
        profilesMenu.setMnemonic(KeyEvent.VK_P);

        JMenuItem profileGeometryItem = new JCheckBoxMenuItem(showProfileGeometryAction);
        JMenuItem crossSectionsDialogItem = new JCheckBoxMenuItem(showCrossSectionsDialogAction);
        JMenuItem modifyProfileStyleItem = new JMenuItem(modifyProfileStyleAction);

        JMenuItem lineProfileItem = new JCheckBoxMenuItem(extractLineProfile);
        JMenuItem polyLineProfileItem = new JCheckBoxMenuItem(extractPolyLineProfile);
        JMenuItem freeHandProfileItem = new JCheckBoxMenuItem(extractFreeHandProfile);

        JMenuItem exportProfilesItem = new JMenuItem(exportProfilesAction);
        JMenuItem importProfilesItem = new JMenuItem(importProfilesAction);

        profilesMenu.add(modifyProfileStyleItem);
        profilesMenu.addSeparator();
        profilesMenu.add(profileGeometryItem);
        profilesMenu.add(crossSectionsDialogItem);
        profilesMenu.addSeparator();
        profilesMenu.add(lineProfileItem);
        profilesMenu.add(polyLineProfileItem);
        profilesMenu.add(freeHandProfileItem);
        profilesMenu.addSeparator();
        profilesMenu.add(exportProfilesItem);
        profilesMenu.add(importProfilesItem);

        menuBar.add(chartMenu);
        menuBar.add(dataMenu);
        menuBar.add(processMenu);
        menuBar.add(roisMenu);
        menuBar.add(profilesMenu);
    }

    private JSplitPane buildMainPane()
    {
        JPanel scrollPanePanel = getPanelResources();
        JTabbedPane tabbedPane = getTabbedPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPanePanel, tabbedPane);
        splitPane.setOneTouchExpandable(true);

        return splitPane;
    }

    JToolBar getToolBar()
    {
        return toolBar;
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(10, 3, 10, 3)));
        toolBar.setFloatable(false);
        toolBar.setLayout(new VerticalWrapLayout(VerticalFlowLayout.BOTTOM, 0,0));

        JButton buttonOverlay = new JButton(overlayAction);
        buttonOverlay.setHideActionText(true);
        buttonOverlay.setMargin(new Insets(0, 0, 0, 0));
        buttonOverlay.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonOverlay.getMaximumSize().getHeight()));
        buttonOverlay.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonShowHistograms = new JButton(showHistogramsAction);
        buttonShowHistograms.setHideActionText(true);
        buttonShowHistograms.setMargin(new Insets(0, 0, 0, 0));
        buttonShowHistograms.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonShowHistograms.getMaximumSize().getHeight()));
        buttonShowHistograms.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonDrawHistograms = new JButton(drawHistogramsAction);
        buttonDrawHistograms.setHideActionText(true);
        buttonDrawHistograms.setMargin(new Insets(0, 0, 0, 0));
        buttonDrawHistograms.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonDrawHistograms.getMaximumSize().getHeight()));
        buttonDrawHistograms.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonDrawROIHistograms = new JButton(drawROIHistogramsAction);
        buttonDrawROIHistograms.setHideActionText(true);
        buttonDrawROIHistograms.setMargin(new Insets(0, 0, 0, 0));
        buttonDrawROIHistograms.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonDrawHistograms.getMaximumSize().getHeight()));
        buttonDrawROIHistograms.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonShowBoxPlots = new JButton(showBoxPlotsAction);
        buttonShowBoxPlots.setHideActionText(true);
        buttonShowBoxPlots.setMargin(new Insets(0, 0, 0, 0));
        buttonShowBoxPlots.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonShowBoxPlots.getMaximumSize().getHeight()));
        buttonShowBoxPlots.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonDrawROIBoxPlots = new JButton(drawROIBoxPlotsAction);
        buttonDrawROIBoxPlots.setHideActionText(true);
        buttonDrawROIBoxPlots.setMargin(new Insets(0, 0, 0, 0));
        buttonDrawROIBoxPlots.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonDrawROIBoxPlots.getMaximumSize().getHeight()));
        buttonDrawROIBoxPlots.setHorizontalAlignment(SwingConstants.LEFT);


        JButton buttonStatistics = new JButton(showStatisticsAction);
        buttonStatistics.setHideActionText(true);
        buttonStatistics.setMargin(new Insets(0, 0, 0, 0));
        buttonStatistics.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonStatistics.getMaximumSize().getHeight()));
        buttonStatistics.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonROIStatistics = new JButton(showROIStatisticsAction);
        buttonROIStatistics.setHideActionText(true);
        buttonROIStatistics.setMargin(new Insets(0, 0, 0, 0));
        buttonROIStatistics.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonROIStatistics.getMaximumSize().getHeight()));
        buttonROIStatistics.setHorizontalAlignment(SwingConstants.LEFT);


        JToggleButton buttonPolygonalRoi = new JToggleButton(roiPolygonalAction);
        buttonPolygonalRoi.setHideActionText(true);
        buttonPolygonalRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonPolygonalRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonPolygonalRoi.getMaximumSize().getHeight()));
        buttonPolygonalRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonRectangularRoi = new JToggleButton(roiRectangularAction);
        buttonRectangularRoi.setHideActionText(true);
        buttonRectangularRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonRectangularRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonRectangularRoi.getMaximumSize().getHeight()));
        buttonRectangularRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonElipticRoi = new JToggleButton(roiElipticAction);
        buttonElipticRoi.setHideActionText(true);
        buttonElipticRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonElipticRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonElipticRoi.getMaximumSize().getHeight()));
        buttonElipticRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonFreeRoi = new JToggleButton(roiFreeAction);
        buttonFreeRoi.setHideActionText(true);
        buttonFreeRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonFreeRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonFreeRoi.getMaximumSize().getHeight()));
        buttonFreeRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonWandRoi = new JToggleButton(roiWandAction);
        buttonWandRoi.setHideActionText(true);
        buttonWandRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonWandRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonWandRoi.getMaximumSize().getHeight()));
        buttonWandRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonRotateRoi = new JToggleButton(rotateROIAction);
        buttonRotateRoi.setHideActionText(true);
        buttonRotateRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonRotateRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonRotateRoi.getMaximumSize().getHeight()));
        buttonRotateRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonSplitRoi = new JToggleButton(splitROIsAction);
        buttonSplitRoi.setHideActionText(true);
        buttonSplitRoi.setMargin(new Insets(0, 0, 0, 0));
        buttonSplitRoi.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonSplitRoi.getMaximumSize().getHeight()));
        buttonSplitRoi.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonMeasureLine = new JToggleButton(getMeasureAction());
        buttonMeasureLine.setHideActionText(true);
        buttonMeasureLine.setMargin(new Insets(0, 0, 0, 0));
        buttonMeasureLine.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonMeasureLine.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonMeasurePolyline = new JToggleButton(measurePolyLineAction);
        buttonMeasurePolyline.setHideActionText(true);
        buttonMeasurePolyline.setMargin(new Insets(0, 0, 0, 0));
        buttonMeasurePolyline.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasurePolyline.getMaximumSize().getHeight()));
        buttonMeasurePolyline.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonMeasureFreeHand = new JToggleButton(measureFreeHandAction);
        buttonMeasureFreeHand.setHideActionText(true);
        buttonMeasureFreeHand.setMargin(new Insets(0, 0, 0, 0));
        buttonMeasureFreeHand.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureFreeHand.getMaximumSize().getHeight()));
        buttonMeasureFreeHand.setHorizontalAlignment(SwingConstants.LEFT);


        JToggleButton buttonLineProfile = new JToggleButton(extractLineProfile);
        buttonLineProfile.setHideActionText(true);
        buttonLineProfile.setMargin(new Insets(0, 0, 0, 0));
        buttonLineProfile.setMaximumSize(new Dimension(Integer.MAX_VALUE,(int) buttonLineProfile.getMaximumSize().getHeight()));
        buttonLineProfile.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonPolylineProfile = new JToggleButton(extractPolyLineProfile);
        buttonPolylineProfile.setHideActionText(true);
        buttonPolylineProfile.setMargin(new Insets(0, 0, 0, 0));
        buttonPolylineProfile.setMaximumSize(new Dimension(Integer.MAX_VALUE,(int) buttonPolylineProfile.getMaximumSize().getHeight()));
        buttonPolylineProfile.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonFreeHandProfile = new JToggleButton(extractFreeHandProfile);
        buttonFreeHandProfile.setHideActionText(true);
        buttonFreeHandProfile.setMargin(new Insets(0, 0, 0, 0));
        buttonFreeHandProfile.setMaximumSize(new Dimension(Integer.MAX_VALUE,(int) buttonFreeHandProfile.getMaximumSize().getHeight()));
        buttonFreeHandProfile.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonConvolve = new JButton(convolveAction);
        buttonConvolve.setHideActionText(true);
        buttonConvolve.setMargin(new Insets(0, 0, 0, 0));
        buttonConvolve.setMaximumSize(new Dimension(Integer.MAX_VALUE,(int) buttonConvolve.getMaximumSize().getHeight()));
        buttonConvolve.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonLockAspectRatio = new JToggleButton(lockAspectRatioAction);
        buttonLockAspectRatio.setHideActionText(true);
        buttonLockAspectRatio.setMargin(new Insets(0, 0, 0, 0));
        buttonLockAspectRatio.setMaximumSize(new Dimension(Integer.MAX_VALUE,(int) buttonLockAspectRatio.getMaximumSize().getHeight()));
        buttonLockAspectRatio.setHorizontalAlignment(SwingConstants.LEFT);

        JButton buttonSmooth = new JButton(resizeAction);
        buttonSmooth.setHideActionText(true);
        buttonSmooth.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonSmooth.getMaximumSize().getHeight()));
        buttonSmooth.setHorizontalAlignment(SwingConstants.LEFT);

        toolBar.add(buttonShowHistograms);
        toolBar.add(buttonDrawHistograms);
        toolBar.add(buttonDrawROIHistograms);
        toolBar.add(buttonShowBoxPlots);
        toolBar.add(buttonDrawROIBoxPlots);
        toolBar.add(buttonStatistics);
        toolBar.add(buttonROIStatistics);
        toolBar.add(buttonPolygonalRoi);
        toolBar.add(buttonRectangularRoi);
        toolBar.add(buttonElipticRoi);
        toolBar.add(buttonFreeRoi);
        toolBar.add(buttonWandRoi);
        toolBar.add(buttonRotateRoi);
        toolBar.add(buttonSplitRoi);
        toolBar.add(buttonMeasureLine);
        toolBar.add(buttonMeasurePolyline);
        toolBar.add(buttonMeasureFreeHand);
        toolBar.add(buttonLineProfile);
        toolBar.add(buttonPolylineProfile);
        toolBar.add(buttonFreeHandProfile);
        toolBar.add(buttonConvolve);
        toolBar.add(buttonOverlay);
        toolBar.add(buttonSmooth);
        toolBar.add(buttonLockAspectRatio);

        toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(0, 3, 0, 3)));
        toolBar.setMargin(new Insets(0,0,0,0));

        return toolBar;
    }

    private void initInputAndActionMaps()
    {
        List<Action> actions = Arrays.asList(undoAction, redoAction, undoAllAction, redoAllAction);
        registerActionAcceleratorKeysInInputMaps(actions);
    }

    @Override
    public void refreshUndoRedoOperations()
    {
        checkIfUndoRedoEnabled();
    }

    private void checkIfUndoRedoEnabled()
    {
        Channel2DDialogModel<R> model = getResourceModel();

        boolean redoEnabled = model.canRedoBeEnabled();
        boolean undoEnabled = model.canUndoBeEnabled();

        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);

        checkIfUndoRedoAllEnabled();
    }

    private void checkIfUndoRedoAllEnabled()
    {
        Channel2DDialogModel<R> model = getResourceModel();

        boolean redoAllEnabled = model.canRedoAllBeEnabled();
        boolean undoAllEnabled = model.canUndoAllBeEnabled();

        undoAllAction.setEnabled(undoAllEnabled);
        redoAllAction.setEnabled(redoAllEnabled);
    }

    protected JMenuItem getMarkerStyleMenuItem()
    {
        return modifyMapMarkerStyleItem;
    }

    protected JMenuItem getAddMapMarkerItem()
    {
        return addMapMarkerItem;
    }

    protected String getMapMarkerSourceName(Point2D position)
    {
        return "";
    }

    protected String getMapMarkerPositionDescription(Point2D position)
    {
        return "";
    }

    public void markSourcePositions(List<SimpleSpectroscopySource> sources) throws UserCommunicableException
    {
        for(SimpleSpectroscopySource source : sources)
        {
            markSourcePosition(source);
        }
    }

    public void markSourcePosition(SimpleSpectroscopySource source) throws UserCommunicableException
    {
        if(source.isFromMap())
        {
            MapSource<?> mapSource = source.getForceMap();
            Point2D location = source.getRecordingPoint();

            if(location == null)
            {
                throw new UserCommunicableException("The curve position is unknown");
            }

            R resource = getResourceContainingChannelsFrom(mapSource);

            if(resource == null)
            {                              
                throw new UserCommunicableException("The map chart was not found");
            }     

            if(!Objects.equals(getSelectedResource(), resource))
            {
                selectResource(resource);
            }

            MapMarkerStyle markerStyle = getSelectedChart().getMapMarkerStyle();
            MapMarker marker = new MapMarker(location, getSelectedChart().getCurrentMapMarkerIndex(), markerStyle);
            addOrReplaceMapMarker(marker);
        }
    }

    @Override
    public boolean areHistogramsAvaialable()
    {
        boolean available = !histogramView.isEmpty();
        return available;
    }

    private void setHistogramsAvailable(boolean available)
    {
        showHistogramsAction.setEnabled(available);

        for(Channel2DPanel<?> panel : getPanels())
        {
            panel.setHistogramsAvialable(available);
        }
    }

    private void setBoxPlotsAvailable(boolean available)
    {
        showBoxPlotsAction.setEnabled(available);
    }


    protected SpectroscopyResultDestination getResultDestination() 
    {
        return parent;
    }

    @Override
    protected void handleChangeOfSelectedType(String typeOld, String typeNew) 
    {        
        super.handleChangeOfSelectedType(typeOld, typeNew);

        if (!Objects.equals(typeOld, typeNew)) 
        {
            V chartNew = getSelectedChart();

            refreshBoundedStyleModels(typeNew);
            handleNewChart(chartNew);

            checkIfUndoRedoEnabled();
        }
    }

    /*
     * chartNew cannot be null
     */
    private void handleNewChart(V chartNew)
    {
        if(chartNew != null)
        {
            boolean aspectRatioLocked = chartNew.getUseFixedChartAreaSize();

            //ugly hack - I don't really now why it is necessary to call this
            chartNew.setUseFixedChartAreaSize(aspectRatioLocked);
            lockAspectRatioAction.putValue(Action.SELECTED_KEY, aspectRatioLocked);
        }

        refreshStyleModels();
        updateWandROIUnits(chartNew);

        updateRangeGradientChoser();
        updateROIMaskEditor();
        updateRangeHistogramGradientChooser();
        updateBuiltInGradientsSelector();
    }

    @Override
    public void handleChangeOfSelectedResource(R resourceOld, R resourceNew) 
    {
        super.handleChangeOfSelectedResource(resourceOld, resourceNew);

        if(resourceOld != null)
        {
            resourceOld.removePropertyChangeListener(listener);
        }

        if (resourceNew != null)
        {       
            resourceNew.addPropertyChangeListener(listener);

            File defaultOutputFile = resourceNew.getDefaultOutputLocation();

            updateROIAvailability();

            refreshROIGeometry(resourceNew);
            updateProfilesAvailability();
            refreshProfileGeometry(resourceNew);

            updateMeasurementsAvailability();
            refreshDistanceMeasurementGeometry(resourceNew);


            Map<String, PrefixedUnit> identifierUnitMap = resourceNew.getIdentifierUnitMap();        
            roiStatisticsView.resetDialog(identifierUnitMap, defaultOutputFile);

            refreshChannelStatistics(resourceNew);

            resourceNew.setAllROIsLagging(true);
            if(areROISamplesNeeded())
            {
                refreshROISamples();
            }

            V chartNew = getSelectedChart();
            handleNewChart(chartNew);
        }

        checkIfUndoRedoEnabled();
    }


    /*
     * resourceNew cannot be null;
     */
    private void refreshChannelStatistics(R resourceNew)
    {
        File defaultOutputFile = resourceNew.getDefaultOutputLocation();

        Map<String, PrefixedUnit> identifierUnitMap = resourceNew.getIdentifierUnitMap();        
        channelStatisticsView.resetDialog(identifierUnitMap, defaultOutputFile);

        Map<String, Map<Object, QuantitativeSample>> samplesForChannels = resourceNew.getSamples(false);
        channelStatisticsView.refreshSamples(samplesForChannels);
    }

    /*
     * resourceNew cannot be null;
     */
    private void refreshROIGeometry(R resourceNew)
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }

        File defaultOutputFile = resourceNew.getDefaultOutputLocation();
        PrefixedUnit dataUnit = selectedChart.getDomainDataUnit();
        PrefixedUnit dataUnitY = selectedChart.getRangeDataUnit();
        PrefixedUnit displayedUnitX = selectedChart.getDomainPreferredUnit();

        Map<Object, ROIDrawable> rois = resourceNew.getROIs();

        ROIShapeFactorsTableModel roiGeometryModel = new ROIShapeFactorsTableModel(defaultOutputFile, dataUnit, displayedUnitX);
        roiGeometryModel.addROIs(rois);

        MinimalNumericalTable table = roiShapeFactorsView.getTable();
        table.setModel(roiGeometryModel);
    }

    /*
     * resourceNew cannot be null;
     */
    private void refreshProfileGeometry(R resourceNew)
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }

        File defaultOutputFile = resourceNew.getDefaultOutputLocation();

        PrefixedUnit dataUnitX = selectedChart.getDomainDataUnit();
        PrefixedUnit dataUnitY = selectedChart.getRangeDataUnit();
        PrefixedUnit displayedUnitX = selectedChart.getDomainPreferredUnit();
        PrefixedUnit displayedUnitY = selectedChart.getRangePreferredUnit();

        Map<Object, DistanceShapeFactors> profiles = resourceNew.getProfileGemetries();
        DistanceGeometryTableModel profileGeometryModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
        profileGeometryModel.addDistances(profiles);

        MinimalNumericalTable profileTable = profileGeometryView.getTable();
        profileTable.setModel(profileGeometryModel);
    }

    /*
     * resourceNew cannot be null;
     */
    private void refreshDistanceMeasurementGeometry(R resourceNew)
    {                
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }

        PrefixedUnit dataUnitX = selectedChart.getDomainDataUnit();
        PrefixedUnit dataUnitY = selectedChart.getRangeDataUnit();
        PrefixedUnit displayedUnitX = selectedChart.getDomainPreferredUnit();
        PrefixedUnit displayedUnitY = selectedChart.getRangePreferredUnit();

        File defaultOutputFile = resourceNew.getDefaultOutputLocation();

        Map<Object, DistanceShapeFactors> distanceMeasurementLines = resourceNew.getDistanceMeasurementGeometries();

        DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
        distanceMeasurementsModel.addDistances(distanceMeasurementLines);

        MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsView.getTable();
        dstanceMeasurementsTable.setModel(distanceMeasurementsModel);
    }

    @Override
    public boolean updateResourceWithNewCharts(R resource, Map<String,V> charts)
    {
        boolean updated = super.updateResourceWithNewCharts(resource, charts);

        for(V chart : charts.values())
        {
            chart.setROIs(resource.getROIs());
            chart.setProfiles(resource.getProfiles());
            chart.setDistanceMeasurements(resource.getDistanceMeasurements());
            chart.setMapMarkers(resource.getMapMarkers());

            //this must be called after addOrUpdateResource()
            chart.setUseFixedChartAreaSize(true);
        }

        return updated;
    }

    @Override
    public void addResource(R resource, Map<String,V> charts)
    {
        super.addResource(resource, charts);

        for(V chart : charts.values())
        {
            chart.setROIs(resource.getROIs());
            chart.setProfiles(resource.getProfiles());
            chart.setDistanceMeasurements(resource.getDistanceMeasurements());
            chart.setMapMarkers(resource.getMapMarkers());
        }

        refreshBoundedStyleModels(getSelectedType());
    }

    @Override
    public void addResources(Map<R, Map<String, V>> chartsMap)
    {
        super.addResources(chartsMap);

        refreshBoundedStyleModels(getSelectedType());
    }

    private void refreshStyleModels()
    {
        V chartNew = getSelectedChart();
        if(chartNew == null)
        {
            return;
        }

        ROIStyle roiStyleModel = chartNew.getROIStyle();
        roiEditor.setModel(roiStyleModel);

        ProfileStyle profileStyleModel = chartNew.getProfileStyle();
        profileEditor.setModel(profileStyleModel);

        MapMarkerStyle mapMarkerStyleModel = chartNew.getMapMarkerStyle();
        mapMarkerStyleModel.setValueLabelTypes(getValueLabelTypes());
        mapMarkerEditor.setModel(mapMarkerStyleModel);

        DistanceMeasurementStyle measurementStyle = chartNew.getDistanceMeasurementStyle();
        measurementEditor.setModel(measurementStyle);
    }

    private void updateWandROIUnits(V chart)
    {
        if(chart == null)
        {
            return;
        }

        PrefixedUnit axisUnitNew = chart.getZDisplayedUnit();

        wandROIModel.setUnitDifference(axisUnitNew);
    }

    @Override
    public PrefixedUnit getDataUnit()
    {
        Channel2DResource resource = getSelectedResource();
        String type = getSelectedType();
        return resource.getSingleDataUnit(type);              
    }

    @Override
    public PrefixedUnit getDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getZDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDataUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDataUnit();
        }

        return axisUnit;
    }


    private PrefixedUnit getYAxisDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getRangeDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getYAxisDataUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getRangeDataUnit();
        }

        return axisUnit;
    }


    @Override
    public UnitManager getUnitManager()
    {
        return new UnitManager(getDataUnit(), getDisplayedUnit());
    }

    @Override
    public List<PrefixedUnit> getDomainDisplayedUnits()
    {
        List<PrefixedUnit> domainUnits = new ArrayList<>();

        domainUnits.add(getXAxisDisplayedUnit());
        domainUnits.add(getYAxisDisplayedUnit());

        return domainUnits;
    }

    @Override
    public List<PrefixedUnit> getDomainDataUnits()
    {
        List<PrefixedUnit> domainUnits = new ArrayList<>();

        domainUnits.add(getXAxisDataUnit());
        domainUnits.add(getYAxisDataUnit());

        return domainUnits;
    }

    private void refreshBoundedStyleModels(String typeNew)
    {
        roiEditor.setBoundedModels(getBoundedROIStyles());        
        profileEditor.setBoundedModels(getBoundedProfileStyles());      
        mapMarkerEditor.setBoundedModels(getBoundedMapMarkerStyles());
        measurementEditor.setBoundedModels(getBoundedDistanceMeasurementStyles());        
    }

    @Override
    public void clear() 
    {
        super.clear();

        // we have to get rid with rangeGradientChooser and rangeHistogramGradientChooser, because they hold
        // reference to renderer
        // which in turn holds reference to plot
        //it also holds the reference to dataset
        if (rangeGradientChooser != null) 
        {
            rangeGradientChooser.cleanUp();
            rangeGradientChooser.dispose();
        }

        if(rangeGradientHistogramChooser != null)
        {
            rangeGradientHistogramChooser.cleanUp();
            rangeGradientHistogramChooser.dispose();
        }		

        if(roiMaskEditor != null)
        {
            roiMaskEditor.cleanUp();
            roiMaskEditor.dispose();
        }

        if(gradientSelectionDialog != null)
        {
            gradientSelectionDialog.cleanUp();
            gradientSelectionDialog.dispose();
        }

        channelStatisticsView.cleanUp();
        roiStatisticsView.cleanUp();

        rangeGradientChooser = null;
        roiMaskEditor = null;
        rangeGradientHistogramChooser = null;
        gradientSelectionDialog = null;
    }

    public void applyROIMask()
    {
        if(getSelectedResource().areROIsAvailable())
        {		
            E panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();

                if (roiMaskEditor == null) 
                {
                    roiMaskEditor = new ROIMaskEditor(getAssociatedWindow(), receiver);
                } 
                else 
                {
                    roiMaskEditor.setReceiver(receiver);
                }
                roiMaskEditor.setVisible(true);
            }
        }
    }


    //updates the dialog for selecting the gradient among the build-in gradients
    //i.e. the dialog with lots of colorful strips
    private void updateBuiltInGradientsSelector()
    {
        if(gradientSelectionDialog != null && gradientSelectionDialog.isVisible())
        {
            E panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                gradientSelectionDialog.setReceiver(receiver);
            }
        }
    }

    //updates the dialog for setting the range (the one with spinners, check boxes etc.)
    private void updateRangeGradientChoser()
    {
        if(rangeGradientChooser != null && rangeGradientChooser.isVisible())
        {
            E panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                rangeGradientChooser.setReceiver(receiver);
            }
        }
    }

    //updates dialog for setting the range using a histogram
    private void updateRangeHistogramGradientChooser()
    {
        if(rangeGradientHistogramChooser != null && rangeGradientHistogramChooser.isVisible())
        {
            E panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                rangeGradientHistogramChooser.setRangeModel(receiver);
            }
        }
    }

    private void updateROIMaskEditor()
    {
        if(roiMaskEditor != null && roiMaskEditor.isShowing())
        {
            E panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                roiMaskEditor.setReceiver(receiver);
            }
        }
    }

    @Override
    public void notifyAboutROISampleNeeded()
    {        
        refreshROISamples();
    }

    @Override
    public void notifyAboutAspectRatioLock()
    {
        V selectedChart = getSelectedChart();

        if (selectedChart != null) 
        {
            boolean aspectRatioLocked = selectedChart.getUseFixedChartAreaSize();
            lockAspectRatioAction.putValue(Action.SELECTED_KEY, aspectRatioLocked);
        }		
    }

    private void updateProfilesAvailability() 
    {
        R resource = getSelectedResource();
        Map<Object, Profile> profiles = resource.getProfiles();
        boolean profilesAvailable = !profiles.isEmpty();

        for (Channel2DPanel<?> panel : getPanels()) 
        {
            panel.setProfilesAvailable(profilesAvailable);
        }

        setSingleProfileBasedActionsEnabled(profilesAvailable);
    }

    private void setSingleProfileBasedActionsEnabled(boolean actionsProfileBasedEnabled)
    {
        if (showProfileGeometryAction != null) 
        {
            showProfileGeometryAction.setEnabled(actionsProfileBasedEnabled);
        }

        exportProfilesAction.setEnabled(actionsProfileBasedEnabled);
    }

    private void updateMapMarkerAvailability(boolean mapMarkersAvailable) 
    {}

    protected void updateROIAvailability() 
    {
        R resource = getSelectedResource();
        Map<Object, ROIDrawable> rois = (resource != null) ? resource.getROIs() : Collections.<Object, ROIDrawable>emptyMap();

        boolean roisAvailable = !rois.isEmpty();
        boolean multipleROIsAvailable = rois.size() > 1;

        boolean actionsBasicEnabled = (getSelectedChart() != null);
        boolean actionsROIBasedEnabled = (roisAvailable && actionsBasicEnabled);
        boolean actionsMultipleROIBasedEnabled = (multipleROIsAvailable && actionsBasicEnabled);
        boolean actionsSubtractabilityROIBasedEnabled = !ROIUtilities.findPossibleDiffrences(rois.values()).isEmpty();

        setSingleROIBasedActionsEnabled(actionsROIBasedEnabled);
        setMultipleROIBasedActionsEnabled(actionsMultipleROIBasedEnabled);
        setROISubtractabilityBasedActionsEnabled(actionsSubtractabilityROIBasedEnabled);

        for (Channel2DPanel<?> panel : getPanels()) {
            panel.setROIBasedActionsEnabled(actionsROIBasedEnabled);
        }
    }


    protected void setMultipleROIBasedActionsEnabled(boolean enabled)
    {
        mergeROIsAction.setEnabled(enabled);
    }

    protected void setROISubtractabilityBasedActionsEnabled(boolean enabled)
    {
        subtractROIsAction.setEnabled(enabled);
    }

    protected void setSingleROIBasedActionsEnabled(boolean enabled)
    {
        roiMaskAction.setEnabled(enabled);
        drawROIHistogramsAction.setEnabled(enabled);
        showLiveHistogramAction.setEnabled(enabled);
        drawROIBoxPlotsAction.setEnabled(enabled);
        showLiveBoxPlotsAction.setEnabled(enabled);
        showROIRawDataAction.setEnabled(enabled);
        showROIShapeFactorsAction.setEnabled(enabled);
        showROIStatisticsAction.setEnabled(enabled);
        subtractROIFitAction.setEnabled(enabled); 
        complementROIsAction.setEnabled(enabled);
        splitROIsAction.setEnabled(enabled);
        convexHullROIsAction.setEnabled(enabled);
        exportROIsAction.setEnabled(enabled);
        rotateROIAction.setEnabled(enabled);
    }

    protected MainView getMainFrame() 
    {
        return parent;
    }

    public void addCharts(Map<? extends R, Map<String, V>> resourceChartsMap) 
    {
        addCharts(resourceChartsMap, false);
    }

    public void addCharts(Map<? extends R, Map<String, V>> resourceChartsMap, boolean select) 
    {
        int previousCount = getResourceCount();

        for (Entry<? extends R, Map<String, V>> entry : resourceChartsMap.entrySet()) 
        {
            R resource = entry.getKey();
            Map<String, V> charts = entry.getValue();
            addResource(resource, charts);
        }
        if (select) 
        {
            selectResource(previousCount);
        }
    }

    @Override
    public void handleNewChartPanel(E panel) 
    {
        super.handleNewChartPanel(panel);
        panel.setDensitySupervisor(this);    
    }

    // we need private 'copy' of the controlForResourceEmptiness, because it is
    // called in the constructor
    // we must use private copy, so that it is not overridden
    @Override
    public void controlForResourceEmptiness(boolean empty) {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    private void controlForResourceEmptinessPrivate(boolean empty)
    {
        boolean enabled = !empty;
        enableStandByActionsDensityDialog(enabled);
        updateROIAvailability();
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty) {
        super.controlForSelectedChartEmptiness(empty);

        boolean enabled = !empty;
        enableStandByActionsDensityDialog(enabled);
        updateROIAvailability();
    }

    //standBy actions are the ones that can always be fired, provided that dialog is non-empty
    private void enableStandByActionsDensityDialog(boolean enabled) 
    {
        duplicateImageAction.setEnabled(enabled);

        editGradientAction.setEnabled(enabled);
        editHistogramGradientAction.setEnabled(enabled);
        roiMaskAction.setEnabled(enabled);
        drawHistogramsAction.setEnabled(enabled);
        showStatisticsAction.setEnabled(enabled);
        showRawDataAction.setEnabled(enabled);
        fixZeroAction.setEnabled(enabled);
        subtractPolynomialBackgroundAction.setEnabled(enabled);
        lineMatchingCorrectionAction.setEnabled(enabled);
        lineFitCorrectionAction.setEnabled(enabled);    

        transposeAction.setEnabled(enabled);
        flipHorizontallyAction.setEnabled(enabled);
        flipZAction.setEnabled(enabled);
        flipVerticallyAction.setEnabled(enabled);   
        rotateClockwiseAction.setEnabled(enabled);
        rotateCounterClockwiseAction.setEnabled(enabled);
        rotateArbitraryAngleAction.setEnabled(enabled);

        medianFilterAction.setEnabled(enabled);
        medianWeightedFilterAction.setEnabled(enabled);

        sharpenAction.setEnabled(enabled);
        unsharpMaskAction.setEnabled(enabled);
        gaussianAction.setEnabled(enabled);
        sobelAction.setEnabled(enabled);
        prewittAction.setEnabled(enabled);
        horizontalDerivativeAction.setEnabled(enabled);
        verticalDerivativeAction.setEnabled(enabled);
        laplacianGaussianAction.setEnabled(enabled);
        convolveAction.setEnabled(enabled);
        importROIsAction.setEnabled(enabled);

        animateAction.setEnabled(enabled);
        overlayAction.setEnabled(enabled);

        extractLineProfile.setEnabled(enabled);
        extractPolyLineProfile.setEnabled(enabled);
        extractFreeHandProfile.setEnabled(enabled);
        importProfilesAction.setEnabled(enabled);

        resizeAction.setEnabled(enabled);
        addPlaneAction.setEnabled(enabled);
        thresholdDataAction.setEnabled(enabled);
        floodFillAction.setEnabled(enabled);

        lockAspectRatioAction.setEnabled(enabled);

        addMapMarkerAction.setEnabled(enabled);
        modifyMapMarkerStyleAction.setEnabled(enabled);

        measurePolyLineAction.setEnabled(enabled);
        measureFreeHandAction.setEnabled(enabled);
        importMeasurementsAction.setEnabled(enabled);

        roiHoleAction.setEnabled(enabled);     
        roiPolygonalAction.setEnabled(enabled);
        roiRectangularAction.setEnabled(enabled);
        roiElipticAction.setEnabled(enabled);
        roiFreeAction.setEnabled(enabled);
        roiWandAction.setEnabled(enabled);

        drawROIHistogramsAction.setEnabled(enabled);
        drawROIBoxPlotsAction.setEnabled(enabled);
        showLiveHistogramAction.setEnabled(enabled);
        showLiveBoxPlotsAction.setEnabled(enabled);
        subtractROIFitAction.setEnabled(enabled);
        showROIRawDataAction.setEnabled(enabled);
        showROIShapeFactorsAction.setEnabled(enabled);
        showROIStatisticsAction.setEnabled(enabled);
        replaceDataAction.setEnabled(enabled);
        imageMathAction.setEnabled(enabled);
        modifyProfileStyleAction.setEnabled(enabled);
        modifyROIStyleAction.setEnabled(enabled);

        undoAction.setEnabled(enabled);
        redoAction.setEnabled(enabled);
    }

    @Override
    public Window getPublicationSite() {
        return getAssociatedWindow();
    }

    public void overlay() 
    {
        overlayWizard.showDialog(getSelectedResource().getDefaultOutputLocation());				
    }

    public abstract R copyResource(R resourceOld, String shortName, String longName);

    public abstract R copyResource(R resourceOld, Set<String> typesToRetain, String shortName, String longName);


    @Override
    public void overlay(List<? extends Channel2DSource<?>> sourcesToOverlay, Set<String> channelToOverlayIdentifiers, Set<String> typesToBeOverlaid)
    {
        R currentResource = getSelectedResource();

        int index = getResourceCount();

        for(Channel2DSource<?> sourceNew: sourcesToOverlay)
        {
            String sourceName = sourceNew.getShortName();
            String sourceLongName = sourceNew.getLongName();

            sourceNew.retainAll(channelToOverlayIdentifiers); //removes channels with other identifiers

            for(String identifier : channelToOverlayIdentifiers)
            {
                Map<String,V> charts = new LinkedHashMap<>();

                String overlayName = sourceName + " (" + identifier +") " + " over " + currentResource.getShortName();
                String longOverlayName = sourceLongName + " (" + identifier +") " + " over " + currentResource.getLongName();

                R resourceNew = copyResource(currentResource, typesToBeOverlaid, overlayName, longOverlayName);

                for(String type: typesToBeOverlaid)
                {
                    Map<Channel2DSource<?>, List<Channel2D>> duplicatedChannels = resourceNew.getSourceChannelMap(type);

                    V originalChart = getSelectedChart(type);

                    ////copies old charts
                    V chartNew = (V) originalChart.getCopy();
                    CustomizableXYBasePlot plotNew = chartNew.getCustomizablePlot();

                    int i = 0;
                    for(Entry<Channel2DSource<?>, List<Channel2D>> entry : duplicatedChannels.entrySet())
                    {
                        Channel2DSource<?> s = entry.getKey();

                        for(Channel2D channel : entry.getValue())
                        {
                            ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, s.getShortName());
                            plotNew.addOrReplaceLayer(dataset.getKey(), dataset, plotNew.getRenderer(i++));
                        }
                    }

                    String identifierNew = identifier + " over " + type;

                    //overlays

                    Channel2D channelOverlay = sourceNew.duplicateChannel(identifier, identifierNew);
                    ProcessableXYZDataset datasetOverlay = Channel2DDataset.getDataset(channelOverlay, sourceNew.getShortName());

                    chartNew.overlay(identifier, datasetOverlay);
                    charts.put(type, chartNew);

                    resourceNew.registerChannel(type, sourceNew, channelOverlay.getIdentifier());             
                }

                sourceNew.removeChannel(identifier);
                addResource(resourceNew, charts);
            }
        }

        selectResource(index);

        refreshStatisticsDialogs();
    }

    @Override
    public void drawHistograms() 
    {
        R resource = getSelectedResource();
        List<SampleCollection> samples = resource.getSampleCollection(false);

        HistogramWizardModel model = new HistogramWizardModelSingleSelection(histogramView, samples, false);
        drawSampleHistograms(model);
    }

    @Override
    public void drawROIHistograms(List<ROI> rois) 
    {
        R resource = getSelectedResource();

        Map<Object, ROI> roiMap = new LinkedHashMap<>();
        for(ROI roi : rois)
        {
            roiMap.put(roi.getKey(), roi);
        }

        List<SampleCollection> samples = resource.getSampleCollection2(roiMap, false);

        HistogramWizardModel model = new HistogramWizardModelDoubleSelection(histogramView, samples, true);
        drawSampleHistograms(model);
    }

    @Override
    public void drawROIHistograms() 
    {
        R resource = getSelectedResource();

        List<SampleCollection> samples = resource.getROISampleCollections2(false);

        HistogramWizardModel model = new HistogramWizardModelDoubleSelection(histogramView, samples, true);
        drawSampleHistograms(model);
    }


    public void drawSampleHistograms(HistogramWizardModel model) 
    {
        if (histogramsWizard == null) 
        {
            histogramsWizard = new HistogramWizard(model);
        } 
        else 
        {
            histogramsWizard.setWizardModel(model);
        }
        histogramsWizard.showDialog();
    }

    public void drawROIBoxPlots()
    {
        R resource = getSelectedResource();
        List<SampleCollection> sampleCollections = resource.getROISampleCollections(false);

        if(boxPlotWizard == null)
        {
            boxPlotWizard = new KnownSamplesBoxAndWhiskerWizard(boxPlotView);
        }

        boxPlotWizard.showDialog(sampleCollections);
    }

    public void showBoxPlots()
    {
        boxPlotView.showBoxPlots(true);
    }

    @Override
    public void showAllRawResourceData() 
    {        
        List<SampleCollection> sampleCollections = new ArrayList<>();

        List<R> selectedResources = getAllSelectedResources();
        for(R resource : selectedResources)
        {
            List<SampleCollection> collections = resource.getSampleCollection(true);
            sampleCollections.addAll(collections);
        }

        SampleSelectionModel selectionModel = new SampleSelectionModel(sampleCollections, "Which datasets would you like to view?",false, true);

        boolean approved = rawDataWizard.showDialog(selectionModel);
        if (approved) 
        {
            List<SampleCollection> includedSampleCollections = selectionModel.getSampleCollections();
            publishRawData(includedSampleCollections);
        }
    }

    @Override
    public void showRawData() 
    {
        R resource = getSelectedResource();
        List<SampleCollection> sampleCollections = resource.getSampleCollection(true);

        SampleSelectionModel selectionModel = new SampleSelectionModel(sampleCollections, "Which datasets would you like to view?",false, true);

        boolean approved = rawDataWizard.showDialog(selectionModel);
        if (approved) 
        {
            List<SampleCollection> includedSampleCollections = selectionModel.getSampleCollections();
            publishRawData(includedSampleCollections);
        }
    }



    @Override
    public void showROIRawData(List<ROI> rois) 
    {        
        R resource = getSelectedResource();

        Map<Object, ROI> roiMap = new LinkedHashMap<>();
        for(ROI roi : rois)
        {
            roiMap.put(roi.getKey(), roi);
        }
        List<SampleCollection> sampleCollections = resource.getSampleCollection2(roiMap, true);

        SampleSelectionModel selectionModel = new SampleSelectionModel(sampleCollections, "Which datasets would you like to view?",false, true);
        boolean approved = rawDataWizard.showDialog(selectionModel);
        if (approved) 
        {
            List<SampleCollection> includedSampleCollections = selectionModel.getSampleCollections();
            publishRawData(includedSampleCollections);
        }
    }

    @Override
    public void showROIRawData() 
    {
        R resource = getSelectedResource();

        List<SampleCollection> sampleCollections = resource.getROISampleCollections2(true);

        SampleSelectionModel selectionModel = new SampleSelectionModel(sampleCollections, "Which datasets would you like to view?",false, true);
        boolean approved = rawDataWizard.showDialog(selectionModel);
        if (approved) 
        {
            List<SampleCollection> includedSampleCollections = selectionModel.getSampleCollections();
            publishRawData(includedSampleCollections);
        }
    }

    @Override
    public void showROIShapeFactors() 
    {
        roiShapeFactorsView.setVisible(true);
    }

    @Override
    public void setProfileGeometryVisible(boolean visible) 
    {
        profileGeometryView.setVisible(visible);
    }

    @Override
    public void showDistanceMeasurements() 
    {
        distanceMeasurementsView.setVisible(true);
    }

    protected List<SampleCollection> getROIUnionSampleCollections()
    {
        return getResourceModel().getROIUnionSampleCollections();	
    }

    protected Map<String, StatisticsTable> getStatisticsTables() 
    {
        R resource = getSelectedResource();
        List<SampleCollection> sampleCollections = resource.getSampleCollection(false);

        return StatisticsTable.getStatisticsTables(sampleCollections, "");
    }

    protected Map<String, StatisticsTable> getROIStatisticsTables(Map<Object, ROI> rois) 
    {                
        R resource = getSelectedResource();
        List<SampleCollection> sampleCollections = resource.getSampleCollection(rois, false);

        return StatisticsTable.getStatisticsTables(sampleCollections, " ROI");
    }

    @Override
    public void showROIStatistics(Map<Object, ROI> rois) 
    {
        R resource = getSelectedResource();

        Map<String, StatisticsTable> tablesImages = getROIStatisticsTables(rois);

        Map<String, StatisticsTable> allTables = new LinkedHashMap<>();
        allTables.putAll(tablesImages);

        String title = "ROI statistics for " + resource.getShortName();
        showStatistics(allTables, title);
    }

    @Override
    public void showROIStatistics() 
    {
        refreshROISamples();
        roiStatisticsView.setVisible(true);
    }

    @Override
    public void showStatistics()
    {
        channelStatisticsView.setVisible(true);
    }

    private void showStatistics(Map<String, StatisticsTable> tables, String title) 
    {
        StatisticsView dialog = new StatisticsView(getAssociatedWindow(), tables, title,  true);
        dialog.setVisible(true);
    }

    protected Map<String, ChannelSectionLine> getCrossSectionsForImages(Profile profile) 
    {
        R resource = getSelectedResource();
        return resource.getCrossSections(profile);
    }

    public void addProfileReceiver(ProfileReceiver receiver)
    {
        profileReceivers.add(receiver);
    }

    public void removeProfileReceiver(ProfileReceiver receiver)
    {
        profileReceivers.remove(receiver);
    }

    public void addMapMarkerReceiver(MapMarkerReceiver receiver)
    {
        mapMarkerReceivers.add(receiver);
    }

    public void removeMapMarkerReceiver(MapMarkerReceiver receiver)
    {
        mapMarkerReceivers.remove(receiver);
    }

    public void addDistanceMeasurementReceiver(DistanceMeasurementReceiver receiver)
    {
        distanceMeasurementReceivers.add(receiver);
    }

    public void removeDistanceMeasurementReceiver(DistanceMeasurementReceiver receiver)
    {
        distanceMeasurementReceivers.remove(receiver);
    }

    public void addROIReceiver(ROIReceiver receiver)
    {
        roiReceivers.add(receiver);
    }

    public void removeROIReceiver(ROIReceiver receiver)
    {
        roiReceivers.remove(receiver);
    }

    @Override
    public void addProfileKnob(Object profileKey, double knobPositionNew)
    {      
        R selectedResource = getSelectedResource();
        boolean added = selectedResource.addProfileKnob(profileKey, knobPositionNew);

        if(added)
        {
            CrossSectionResource crossSectionResource = selectedResource.getCrossSectionResource();
            crossSectionsView.externalAttemptToAddNewMarker(crossSectionResource, profileKey, knobPositionNew);

            for(ProfileReceiver s: getAllProfileReceivers())
            {
                s.addProfileKnob(profileKey, knobPositionNew);
            }

            Profile modifiedProfile = selectedResource.getProfile(profileKey);
            setMode(modifiedProfile.getMouseInputMode(getMode()), false);
        }     
    }

    @Override
    public void moveProfileKnob(Object profileKey, int knobIndex, double knobPositionNew)
    {
        R selectedResource = getSelectedResource();
        boolean moved = selectedResource.moveProfileKnob(profileKey, knobIndex, knobPositionNew);

        if(moved)
        {
            CrossSectionResource crossSectionResource = selectedResource.getCrossSectionResource();
            crossSectionsView.externalRequestMarkerMovement(crossSectionResource, profileKey, knobIndex, knobPositionNew); 

            for(ProfileReceiver s: getAllProfileReceivers())
            {
                s.moveProfileKnob(profileKey, knobIndex, knobPositionNew);
            }

            Profile modifiedProfile = selectedResource.getProfile(profileKey);
            setMode(modifiedProfile.getMouseInputMode(getMode()), false);
        }
    }

    @Override
    public void removeProfileKnob(Object profileKey, double knobPosition)
    {        
        R selectedResource = getSelectedResource();
        boolean removed = selectedResource.removeProfileKnob(profileKey, knobPosition);

        if(removed)
        {
            for(ProfileReceiver s: getAllProfileReceivers())
            {
                s.removeProfileKnob(profileKey, knobPosition);
            }

            CrossSectionResource crossSectionResource = selectedResource.getCrossSectionResource();
            crossSectionsView.externalAttemptToRemoveMarker(crossSectionResource, profileKey, knobPosition);

            Profile modifiedProfile = selectedResource.getProfile(profileKey);
            setMode(modifiedProfile.getMouseInputMode(getMode()), false);
        }
    }

    @Override
    public void setProfileKnobPositions(Object resource, Object profileKey, List<Double> knobPositions)
    {	
        int index = getResourceIndex(resource);

        R densityResource = getResource(index);
        boolean knobPositionsSet = densityResource.setProfileKnobs(profileKey, knobPositions);

        if(knobPositionsSet)
        {
            for (E panel : getPanels()) 
            {
                panel.setProfileKnobPositions(index, profileKey, knobPositions);
            }

            for(ProfileReceiver s: profileReceivers)
            {
                s.setProfileKnobPositions(resource, profileKey, knobPositions);
            }

            Profile modifiedProfile = densityResource.getProfile(profileKey);
            setMode(modifiedProfile.getMouseInputMode(getMode()), false);
        }     
    }

    @Override
    public void addOrReplaceProfile(Profile profile) 
    {
        Profile  profileCopy = profile.copy();	    
        setMode(profile.getMouseInputMode(getMode()), false);

        R resource = getSelectedResource();
        resource.addOrReplaceProfile(profileCopy, crossSectionsView, resource.getCrossSectionResource(), getAssociatedWindow());

        updateProfilesAvailability();

        for(ProfileReceiver s: getAllProfileReceivers())
        {
            s.addOrReplaceProfile(profileCopy);
        }

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) profileGeometryView.getTable().getModel();
        model.addOrUpdateDistance(profileCopy.getKey(), profileCopy.getDistanceShapeFactors());
    }

    @Override
    public void removeProfile(Profile profile) 
    {
        setMode(profile.getMouseInputMode(getMode()), false);

        R resource = getSelectedResource();
        Map<String, ChannelSectionLine> removedCrossSections = resource.removeProfile(profile);
        CrossSectionResource crossSectionResource = resource.getCrossSectionResource();

        crossSectionsView.removeProfiles(crossSectionResource, removedCrossSections);

        updateProfilesAvailability();

        for(ProfileReceiver s: getAllProfileReceivers())
        {
            s.removeProfile(profile);
        }

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) profileGeometryView.getTable().getModel();
        model.removeDistance(profile.getKey(), profile.getDistanceShapeFactors());
    }

    @Override
    public void setProfiles(Map<Object, Profile> profiles) 
    {
        setMode(MouseInputModeStandard.PROFILE_LINE, false);

        Channel2DResource resource = getSelectedResource();
        resource.setProfiles(profiles);

        updateProfilesAvailability();

        for(ProfileReceiver s: getAllProfileReceivers())
        {
            s.setProfiles(profiles);
        }

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) profileGeometryView.getTable().getModel();

        Map<Object, DistanceShapeFactors> profileGeometries = new LinkedHashMap<>();
        for(Entry<Object, Profile> entry :  profiles.entrySet())
        {
            Object key = entry.getKey();
            DistanceShapeFactors shapeFactors = entry.getValue().getDistanceShapeFactors();
            profileGeometries.put(key, shapeFactors);
        }
        model.setDistances(profileGeometries);
    }

    private List<ProfileReceiver> getAllProfileReceivers()
    {
        List<ProfileReceiver> allProfileReceivers = new ArrayList<>();

        allProfileReceivers.addAll(getPanels());
        allProfileReceivers.addAll(profileReceivers);

        return allProfileReceivers;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////MAP MARKERS////////////////////////////////


    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker) 
    {
        MapMarker mapMarkerCopy = new MapMarker(mapMarker);

        Point2D controlDataPoint = mapMarker.getControlDataPoint();

        String sourceName = getMapMarkerSourceName(controlDataPoint);
        String sourcePositionDescription = getMapMarkerPositionDescription(controlDataPoint);


        mapMarkerCopy.setCurveName(sourceName);       
        mapMarkerCopy.setPositionDescription(sourcePositionDescription);

        Map<String, String> valueLabels = getValueLabels(controlDataPoint);
        mapMarkerCopy.setValueLabels(valueLabels);

        R resource = getSelectedResource();
        resource.addOrReplaceMapMarker(mapMarkerCopy);

        updateMapMarkerAvailability(true);

        for(MapMarkerReceiver s: getAllMapMarkerReceivers())
        {
            s.addOrReplaceMapMarker(mapMarkerCopy);
        }
    }

    //value labels cannot be read form resource,
    //because we want its units o be the same as the units of the corresponding
    //depth axes, the only solution - actually an ugly hack - is to read this values
    //from DensityPanels, which indirectly read them from DensityPlot, which in turn
    //has access to axis units, stored in renderer
    public Map<String, String> getValueLabels(Point2D dataPoint)
    {
        Map<String, String> valueLabels = new LinkedHashMap<>();

        for(Channel2DPanel<?> panel : getPanels())
        {
            valueLabels.putAll(panel.getValueLabels(dataPoint));
        }

        return valueLabels;
    }

    public List<String> getValueLabelTypes()
    {
        List<String> valueLabelTypes = new ArrayList<>();

        for(Channel2DPanel<?> panel : getPanels())
        {
            valueLabelTypes.addAll(panel.getValueLabelTypes());
        }

        return valueLabelTypes;
    }

    @Override
    public void removeMapMarker(MapMarker mapMarker) 
    {
        R resource = getSelectedResource();
        resource.removeMapMarker(mapMarker);

        boolean mapMarkersAvailable = !resource.getMapMarkers().isEmpty();
        updateMapMarkerAvailability(mapMarkersAvailable);

        for(MapMarkerReceiver s: getAllMapMarkerReceivers())
        {
            s.removeMapMarker(mapMarker);
        }
    }

    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkers) 
    {
        Channel2DResource resource = getSelectedResource();
        resource.setMapMarkers(mapMarkers);

        boolean mapMarkersAvailable = !mapMarkers.isEmpty();
        updateMapMarkerAvailability(mapMarkersAvailable);

        for(MapMarkerReceiver s: getAllMapMarkerReceivers())
        {
            s.setMapMarkers(mapMarkers);
        }     
    }

    public void setMapMarkerStyleEditorVisible(boolean visible) 
    {
        mapMarkerEditor.setVisible(visible);
    }

    private List<MapMarkerReceiver> getAllMapMarkerReceivers()
    {
        List<MapMarkerReceiver> allProfileReceivers = new ArrayList<>();

        allProfileReceivers.addAll(getPanels());
        allProfileReceivers.addAll(mapMarkerReceivers);

        return allProfileReceivers;
    }

    ////////////////////////////////////////////////////////////////////

    private void showCrossSectionsDialog() 
    {
        crossSectionsView.setSelectedType(getSelectedType());
        crossSectionsView.setVisible(true);
    }

    @Override
    public void setProfileStyleEditorVisible(boolean visible) 
    {
        profileEditor.setVisible(visible);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {        
        R resource = getSelectedResource();
        resource.addOrReplaceDistanceMeasurement(measurement);

        updateMeasurementsAvailability();

        for (Channel2DPanel<?> panel : getPanels()) 
        {
            panel.addOrReplaceDistanceMeasurement(measurement);
        }

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsView.getTable().getModel();
        model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {        
        R resource = getSelectedResource();
        resource.removeDistanceMeasurement(measurement);

        updateMeasurementsAvailability();

        for (Channel2DPanel<?> panel : getPanels())
        {
            panel.removeDistanceMeasurement(measurement);
        }

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsView.getTable().getModel();
        model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
    }

    public void setDistanceMeasurementsEditorVisible(boolean visible) 
    {
        measurementEditor.setVisible(visible);
    }

    public void mergeMeasurements()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }    

        R selectedResource = getSelectedResource();
        Map<Object, DistanceMeasurementDrawable> measurements = selectedResource.getDistanceMeasurements();
        Set<DistanceMeasurementDrawable> measurementSet = new LinkedHashSet<>(measurements.values());

        ModifyObjectsModel<DistanceMeasurementDrawable> model = new ModifyObjectsModel<>(measurementSet,"measurement","Masurement merging", 
                "Select which measurements are to be merged", "Measurements to merge",true, true);

        this.measurementMergeWizard = (measurementMergeWizard != null) ? measurementMergeWizard : new ModifyObjectsWizard(getAssociatedWindow(), "Measurement merging");

        boolean approved = measurementMergeWizard.showDialog(model);

        if(approved)
        {
            boolean deleteOriginalMeasurements = model.isDeleteOriginalObjects();
            Collection<DistanceMeasurementDrawable> selectedMeasurements = model.getSelectedObjects();

            Integer mergedROIKey = deleteOriginalMeasurements ? DistanceMeasurementDrawable.getUnionKey(selectedMeasurements) : selectedChart.getCurrentMeasurementIndex();
            DistanceMeasurementDrawable merged = new GeneralUnionMeasurement(selectedMeasurements, mergedROIKey, mergedROIKey.toString(), selectedChart.getDistanceMeasurementStyle());

            if(deleteOriginalMeasurements)
            {
                for(DistanceMeasurementDrawable measurement : selectedMeasurements)
                {
                    removeDistanceMeasurement(measurement);
                }
            }

            addOrReplaceDistanceMeasurement(merged);
        }
    }

    protected void updateMeasurementsAvailability() 
    {
        R resource = getSelectedResource();
        Map<Object, DistanceMeasurementDrawable> measurements = (resource != null) ? resource.getDistanceMeasurements() : new HashMap<>();
        boolean measurementsAvailable = !measurements.isEmpty();

        updateMeasurementsAvailability(measurementsAvailable);

        boolean actionsSingleMeasurementBasedEnabled = measurementsAvailable;
        boolean multipleMeasurementsAvailable = measurementsAvailable && measurements.size() > 1;

        boolean actionsBasicEnabled = (getSelectedChart() != null);
        boolean actionsMultipleMeasurementBasedEnabled = (multipleMeasurementsAvailable && actionsBasicEnabled);

        setSingleMeasurementBasedActionsEnabled(actionsSingleMeasurementBasedEnabled);
        setMultipleMeasurementActionsEnabled(actionsMultipleMeasurementBasedEnabled);
    }

    private void setSingleMeasurementBasedActionsEnabled(boolean actionsSingleMeasurementBasedEnabled)
    {
        exportMeasurementsAction.setEnabled(actionsSingleMeasurementBasedEnabled);
    }

    protected void setMultipleMeasurementActionsEnabled(boolean enabled)
    {
        mergeMeasurementsAction.setEnabled(enabled);
    }

    //////////////////////////// ROIS ADD/REMOVE/SET

    @Override
    public void addOrReplaceROI(ROIDrawable roi) 
    {	    
        setMode(roi.getMouseInputMode(getMode()), false);

        Channel2DResource resource = getSelectedResource();
        resource.addOrReplaceROI(roi);

        if(areROISamplesNeeded())
        {
            refreshROISamples();
        }

        updateROIAvailability();

        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.addOrReplaceROI(roi);
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsView.getTable().getModel();
        model.addOrUpdateROI(roi);

        refreshGradientPaintReceiverROIMask();
    }

    @Override
    public void removeROI(ROIDrawable roi) 
    {
        setMode(roi.getMouseInputMode(getMode()), false);

        ROIDrawable roiCopy = roi.copy();

        Channel2DResource resource = getSelectedResource();
        resource.removeROI(roiCopy);

        if(areROISamplesNeeded())
        {
            refreshROISamples();
        }

        updateROIAvailability();

        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.removeROI(roiCopy);
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsView.getTable().getModel();
        model.removeROI(roiCopy);

        refreshGradientPaintReceiverROIMask();
    }

    @Override
    public int getCurrentROIIndex()
    {
        int index = -1;

        V selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            index = selectedChart.getCurrentROIIndex();
        } 

        return index;
    }

    public void mergeROIs()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }    

        R selectedResource = getSelectedResource();
        Map<Object, ROIDrawable> rois = selectedResource.getROIs();
        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ModifyObjectsModel<ROIDrawable> model = new ModifyObjectsModel<>(roisSet,"ROI","ROI merging", 
                "Select which ROIs are to be merged", "ROIs to merge",true, true);

        this.roiMergeWizard = (roiMergeWizard == null) ? new ModifyObjectsWizard(getAssociatedWindow(), "ROI merging") : roiMergeWizard;

        boolean approved = roiMergeWizard.showDialog(model);

        if(approved)
        {
            boolean deleteOriginalROIs = model.isDeleteOriginalObjects();
            Collection<ROIDrawable> selectedROIs = model.getSelectedObjects();

            Integer mergedROIKey =  deleteOriginalROIs ? ROIUtilities.getUnionKey(selectedROIs) : getCurrentROIIndex();
            ROIDrawable merged = new GeneralUnionROI(selectedROIs, mergedROIKey, mergedROIKey.toString(), selectedChart.getROIStyle());

            if(deleteOriginalROIs)
            {
                for(ROIDrawable roi : selectedROIs)
                {
                    removeROI(roi);
                }
            }

            addOrReplaceROI(merged);
        }
    }

    public void subtractROIs()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        R selectedResource = getSelectedResource();
        Map<Object, ROIDrawable> rois = selectedResource.getROIs();

        DifferenceROIModel<ROIDrawable> selectionModel = new DifferenceROIModel<>(ROIUtilities.findPossibleDiffrences(rois.values()), true, true);

        this.roiDifferenceWizard = (roiDifferenceWizard == null) ? new DifferenceROIWizard(getAssociatedWindow(), "ROI subtraction") : roiDifferenceWizard;

        boolean approved = roiDifferenceWizard.showDialog(selectionModel);

        if(approved)
        {
            ROIDrawable mainROI = selectionModel.getMainROI();
            Set<ROIDrawable> subtractedROIs = selectionModel.getSubtractedROIs();
            ROIDrawable difference = new DifferenceROI(mainROI, subtractedROIs, mainROI.getKey(), mainROI.getLabel(), selectedChart.getROIStyle());

            if(selectionModel.isDeleteSubtractedROIs())
            {
                for(ROIDrawable roi : subtractedROIs)
                {
                    removeROI(roi);
                }
            }

            addOrReplaceROI(difference);
        }
    }

    public void complementROIs()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        R selectedResource = getSelectedResource();
        Map<Object, ROIDrawable> rois = selectedResource.getROIs();
        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ComplementROIModel<ROIDrawable> model = new ComplementROIModel<>(roisSet, true, true);

        complementROIWizard = (complementROIWizard == null) ? new ComplementROIWizard(getAssociatedWindow(), "ROI complement") : complementROIWizard;

        boolean approved = complementROIWizard.showDialog(model);

        if(approved)
        {
            Set<ROIDrawable> roisToComplement = model.getROIsToComplement();
            Area datasetArea = selectedChart.getCustomizablePlot().getDatasetArea();

            boolean deleteOriginal = model.isDeleteOriginalROIs();

            for(ROIDrawable roi : roisToComplement)
            {
                Set<ROIDrawable> subtractedROIs = new LinkedHashSet<>();
                subtractedROIs.add(roi);

                Integer complementKey = deleteOriginal ? roi.getKey() : getCurrentROIIndex();

                ROIDrawable datasetROI = new ROIRectangle(datasetArea.getBounds2D(), complementKey, roi.getStyle());
                ROIDrawable complement = new DifferenceROI(datasetROI, subtractedROIs, datasetROI.getKey(), datasetROI.getLabel(), selectedChart.getROIStyle());

                if(model.isDeleteOriginalROIs())
                {
                    removeROI(roi);
                }

                addOrReplaceROI(complement);
            }
        }
    }


    public void calculateROIConvexHulls()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }    

        R selectedResource = getSelectedResource();
        Map<Object, ROIDrawable> rois = selectedResource.getROIs();
        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ModifyObjectsModel<ROIDrawable> model
        = new ModifyObjectsModel<>(roisSet,"ROI","ROI convex hull", 
                "Select for which ROIs to create convex hulls", "ROIs for convex hulls",true, true);

        this.roiConvexHullWizard = (roiConvexHullWizard == null) ? new ModifyObjectsWizard(getAssociatedWindow(), "ROI convex hulls") : roiConvexHullWizard;

        boolean approved = roiConvexHullWizard.showDialog(model);

        if(approved)
        {
            Set<ROIDrawable> getSelectedObjects = model.getSelectedObjects();

            boolean deleteOriginal = model.isDeleteOriginalObjects();

            for(ROIDrawable roi : getSelectedObjects)
            {       
                Integer hullKey = deleteOriginal ? roi.getKey() : getCurrentROIIndex();

                ROIDrawable convexHull = roi.getConvexHull(hullKey, Integer.toString(hullKey));
                if(deleteOriginal)
                {
                    removeROI(roi);
                }

                addOrReplaceROI(convexHull);
            }
        }
    }

    public void exportROIs()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        R selectedResource = getSelectedResource();
        Map<Object, ROIDrawable> rois = selectedResource.getROIs();
        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ExportAnnotationModel<ROIDrawable> model = new ExportAnnotationModel<>(roisSet, "ROI", "roi", true, true);

        this.exportAnnotationWizard = (exportAnnotationWizard == null) ? new ExportAnnotationWizard(getAssociatedWindow(), "Export ROIs to file") : exportAnnotationWizard;
        this.exportAnnotationWizard.showDialog(model);
    }

    public void exportProfiles()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        R selectedResource = getSelectedResource();
        Map<Object, Profile> profiles = selectedResource.getProfiles();
        Set<Profile> profileSet = new LinkedHashSet<>(profiles.values());

        ExportAnnotationModel<Profile> model = new ExportAnnotationModel<>(profileSet, "Profile", "profile", true, true);

        this.exportAnnotationWizard = (exportAnnotationWizard == null) ? new ExportAnnotationWizard(getAssociatedWindow(), "Export profiles to file") : exportAnnotationWizard;
        this.exportAnnotationWizard.showDialog(model);
    }

    public void exportMeasurements()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        R selectedResource = getSelectedResource();
        Map<Object, DistanceMeasurementDrawable> measurements = selectedResource.getDistanceMeasurements();
        Set<DistanceMeasurementDrawable> measuremenSet = new LinkedHashSet<>(measurements.values());

        ExportAnnotationModel<DistanceMeasurementDrawable> model = new ExportAnnotationModel<>(measuremenSet, "Measurement", "measurement", true, true);

        this.exportAnnotationWizard = (exportAnnotationWizard == null) ? new ExportAnnotationWizard(getAssociatedWindow(), "Export measurements to file") : exportAnnotationWizard;
        this.exportAnnotationWizard.showDialog(model);
    }

    @Override
    public void setROIs(Map<Object, ROIDrawable> rois) 
    {
        Channel2DResource resource = getSelectedResource();
        resource.setROIs(rois);

        if(areROISamplesNeeded())
        {
            refreshROISamples();
        }

        updateROIAvailability();

        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.setROIs(new LinkedHashMap<>(rois));
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsView.getTable().getModel();
        model.setROIs(rois);

        refreshGradientPaintReceiverROIMask();
    }

    @Override
    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.changeROILabel(roiKey, labelOld, labelNew);
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsView.getTable().getModel();
        model.replaceROILabel(roiKey, labelNew);

        Channel2DResource resource = getSelectedResource();
        MetaMap<String, Object, QuantitativeSample> changedSamples = resource.changeROILabel(roiKey, labelOld, labelNew);

        refreshComponentsSampleName(changedSamples.getMapCopy());
    }

    private void setDrawROIHoles(boolean roiHoleMode)
    {
        for (Channel2DPanel<?> panel : getPanels()) 
        {
            panel.setROIHoleMode(roiHoleMode);
        }
    }

    private List<ROIReceiver> getAllROIReceivers()
    {
        List<ROIReceiver> allProfileReceivers = new ArrayList<>();

        allProfileReceivers.addAll(getPanels());
        allProfileReceivers.addAll(roiReceivers);

        return allProfileReceivers;
    }


    @Override
    public ROI getROIUnion()
    {      
        return getResourceModel().getROIUnion();
    }

    @Override
    public Map<Object, ROI> getDrawableROIs()
    {
        return getResourceModel().getROIs();
    }

    @Override
    public Map<Object, ROI> getAllROIs()
    {
        return getResourceModel().getAvailableROIs();
    }

    /////////////////////////////////// ROI SAMPLES ////////////////////////////////////

    /*
     * When the samples for ROIs are changed, then we need to refresh the following objects:
     * 
     * - roiStatisticsDialog, by calling the method refreshSamples(samplesChanged)
     * - live box plots dialog
     * - live histogram dialog
     * - gradient paint receivers, that are owned by panels
     * 
     * 
     * Each element which requires ROIs should has:
     * 
     * - a method for querying whether the element needs ROI sample know
     * - a method for updating the ROI sample
     * - there should be a way in which the element informs the density dialog that it needs the sample
     */

    private boolean areROISamplesNeeded()
    {
        boolean needed = roiStatisticsView != null && roiStatisticsView.isVisible();
        needed = needed || (liveBoxPlotView != null && liveBoxPlotView.isVisible());
        needed = needed || (liveHistogramView != null && liveHistogramView.isVisible());

        E panel = getPanel(getSelectedType());
        if (panel != null) 
        {
            needed = needed || panel.areROISamplesNeeded();
        }

        return needed;
    }

    private void refreshROISamples()
    {
        Channel2DResource resource = getSelectedResource();        
        resource.refreshLaggingROISamples();

        //resource will fire an event
    }    

    private void refreshComponentsBasedOnROISamples(Map<String, Map<Object, QuantitativeSample>> samples)
    {
        samples.remove(Datasets.X_COORDINATE);
        samples.remove(Datasets.Y_COORDINATE);

        roiStatisticsView.refreshSamples(samples);
        refreshLiveBoxPlots(samples);
        refreshGradientReceiver();
        refreshLiveHistogramDialogIfNecessary(false);     
    }

    private void refreshComponentsSampleName(Map<String, Map<Object, QuantitativeSample>> samples)
    {
        samples.remove(Datasets.X_COORDINATE);
        samples.remove(Datasets.Y_COORDINATE);

        roiStatisticsView.refreshSamples(samples);
        refreshLiveBoxPlots(samples);
    }

    protected void handleChangeOfData(Map<String, Channel2D> channelsChanged)
    {                        
        R resource = getSelectedResource();
        String selectedChannelType = getSelectedType();
        handleChangeOfData(channelsChanged, selectedChannelType, resource);
    }

    @Override
    public void handleChangeOfData(Map<String, Channel2D> channelsChanged, String type, Channel2DResource resource)
    {                        
        Channel2DChart<?> chart = getChart(resource, type);

        for (String key : channelsChanged.keySet()) 
        {
            chart.notifyOfDataChange(key);
        }

        DataChangeEvent<String> event = new DataChangeEvent<>(this, channelsChanged.keySet());
        fireDataChangeEvent(event);

        CrossSectionResource crossSectionResource = resource.getCrossSectionResource();
        crossSectionsView.refreshCrossSections(crossSectionResource, type);

        updateRangeHistogramGradientChooser();

        if(rangeGradientChooser != null)
        {
            rangeGradientChooser.updateHistogramSample();
        }

        channelStatisticsView.refreshSamples(resource.getSamples(type), type);

        if(areROISamplesNeeded())
        {
            refreshROISamples();
        }
    }

    //////////////////////////////////// LIVE HISTOGRAM ////////////////////////////////////


    @Override
    public abstract void showHistograms();

    public void setLiveROIHistogramVisible(boolean visible)
    {	
        if(visible)
        {
            refreshROISamples();
            refreshLiveHistogramDialog(true);
        }

        liveHistogramView.setVisible(visible);
    }

    private void refreshLiveHistogramDialogIfNecessary(boolean selectCurrentResource)
    {        
        if(liveHistogramView != null && liveHistogramView.isVisible())
        {
            refreshLiveHistogramDialog(selectCurrentResource);
        }
    }

    private void refreshLiveHistogramDialog(boolean selectCurrentResource)
    {
        R selectedResource = getSelectedResource();
        String shortName = selectedResource.getShortName();

        Map<String, QuantitativeSample> samples = selectedResource.getROIUnionSamples();

        HistogramResource histogramResource = liveHistogramView.getResource(shortName);

        if(histogramResource != null)
        {
            if(selectCurrentResource)
            {
                liveHistogramView.selectResource(histogramResource);
            }

            liveHistogramView.updateSampleModelsWithCopying(samples, histogramResource);
        }
        else
        {
            String longName = selectedResource.getLongName();
            liveHistogramView.publishHistograms(shortName, longName, samples);       
        }
    }

    ///////////////////////////////////// LIVE BOX PLOTS ////////////////////////////

    public void setLiveROIBoxPlotsVisible(boolean visible)
    {   
        if(visible)
        {
            refreshROISamples();           
            refreshLiveBoxPlots();
        }

        liveBoxPlotView.setVisible(visible);
    }

    private void refreshLiveBoxPlots()
    {
        R resource = getSelectedResource();
        String shortName = resource.getShortName();

        List<SampleCollection> roiSampleCollections =  resource.getROISampleCollections(false);

        BoxAndWhiskerResource boxResource = liveBoxPlotView.getResource(shortName);

        if(boxResource != null)
        {
            liveBoxPlotView.selectResource(boxResource);
            liveBoxPlotView.reset(boxResource, roiSampleCollections);
        }
        else
        {
            addBoxPlotResource();
        }
    }


    private void refreshLiveBoxPlots(Map<String, Map<Object, QuantitativeSample>> samplesChanged)
    {
        R selectedResource = getSelectedResource();
        String shortName = selectedResource.getShortName();
        BoxAndWhiskerResource boxResource = liveBoxPlotView.getResource(shortName);

        if(boxResource != null)
        {
            liveBoxPlotView.selectResource(boxResource);
            liveBoxPlotView.refresh(boxResource, samplesChanged);           
        } 
        else
        {
            addBoxPlotResource();
        }
    }

    private void refreshLiveBoxPlots(String type, Map<Object, QuantitativeSample> samplesChanged)
    {
        R selectedResource = getSelectedResource();
        String shortName = selectedResource.getShortName();
        BoxAndWhiskerResource boxResource = liveBoxPlotView.getResource(shortName);

        if(boxResource != null)
        {
            liveBoxPlotView.refresh(boxResource, type, samplesChanged); 
        } 
        else
        {
            addBoxPlotResource();
        }
    }   


    private void addBoxPlotResource()
    {
        R resource = getSelectedResource();
        String shortName = resource.getShortName();

        String longName = resource.getLongName();

        File defaultOutputLocation = resource.getDefaultOutputLocation();
        Map<String, Map<Object, QuantitativeSample>> roiSampleCollections = resource.getSamplesForROIs(false);

        liveBoxPlotView.publishBoxPlots(defaultOutputLocation, shortName, longName, roiSampleCollections, false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////


    //updates gradient receiver when ROIUnion is changed
    private void refreshGradientReceiver()
    {
        R selectedResource = getSelectedResource();

        Map<String, QuantitativeSample> samplesForUnion = selectedResource.getROIUnionSamples();

        for(Entry<String, QuantitativeSample> entry: samplesForUnion.entrySet())
        {
            String type = entry.getKey();
            QuantitativeSample sample = entry.getValue();

            E panel = getPanel(type);
            if (panel != null) 
            {
                panel.setROISample(sample);
            }
        }   
    }

    private void refreshGradientPaintReceiverROIMask()
    {
        R selectedResource = getSelectedResource();
        ROI unionNew = selectedResource.getROIUnion();

        for(Channel2DPanel<?> panel : getPanels())
        {
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                receiver.setMaskedRegion(unionNew);  
            }
        }
    }

    protected void addCrossSections(CrossSectionResource resource,
            Map<String, ChannelSectionLine> crossSectionsNew) 
    {
        crossSectionsView.addOrReplaceCrossSections(resource,
                crossSectionsNew);
    }

    @Override
    public void setProfilesAvailable(boolean b) 
    {}

    @Override
    public boolean areROIsAvailable()
    {
        boolean available = false;
        if(getMode().isROI())
        {
            R resource = getSelectedResource();
            available = resource.areROIsAvailable();
        }

        return available;
    }

    @Override
    public void setROIsAvailable(boolean b) 
    {
        for (Channel2DPanel<?> panel : getPanels()) 
        {
            panel.setROIBasedActionsEnabled(b);
        }
    }

    @Override
    public void setROIStyleEditorVisible(boolean visible) {
        roiEditor.setVisible(visible);
    }

    @Override
    public void addMarkerKnob(Object markerKey, double knobPosition) 
    {}

    @Override
    public void moveMarkerKnob(Object markerKey, int KnobIndex, double knobPositionNew)
    {}   

    @Override
    public void removeMarkerKnob(Object markerKey, int knobIndex)
    {}

    @Override
    public void pushCommand(Channel2DResource resource, String type, UndoableCommand command)
    {
        resource.pushCommand(type, command);
        checkIfUndoRedoEnabled();
    }

    //this should call the model's method pushCommands()
    @Override
    public void pushCommands(MetaMap<Channel2DResource, String, UndoableCommand> commands)
    {
        for(Entry<Channel2DResource,Map<String,UndoableCommand>> entry : commands.entrySet())
        {
            Channel2DResource resource = entry.getKey();
            for(Entry<String, UndoableCommand> innerEntry : entry.getValue().entrySet())
            {
                UndoableCommand command = innerEntry.getValue();
                String type = innerEntry.getKey();
                if(command != null)
                {
                    resource.pushCommand(type, command);
                }
            }
        }
        checkIfUndoRedoEnabled();        
    }

    @Override
    public void transform(Channel2DDataTransformation tr) 
    {
        R selectedResource = getSelectedResource();
        String selectedType = getSelectedType();

        UndoableCommand command = new UndoableTransformationCommand(this, selectedType, selectedResource, tr);
        command.execute();   

        pushCommand(selectedResource, selectedType, command);
    }

    @Override
    public void transform(Channel2DDataInROITransformation tr, ROIRelativePosition position) 
    {
        R resource = getSelectedResource();
        String type = getSelectedType();
        ROI roi = getROIUnion();

        UndoableCommand command = new UndoableImageROICommand(this, type, null, resource, tr, position, roi);
        command.execute();

        pushCommand(resource, type, command);
    }

    private void lockAspectRatio(boolean lock) 
    {
        for (Channel2DPanel<?> panel : getPanels()) 
        {
            panel.lockAspectRatio(lock);
        }
    }

    @Override
    public List<String> getNonEmptyTypes(int index)
    {
        R resource = getResource(index);
        List<String> types = new ArrayList<>();
        if(resource != null)
        {
            types.addAll(resource.getAllTypes());
        }     

        return types;
    }

    private void duplicate()
    {
        R resource = getSelectedResource();
        String type = getSelectedType();      

        if(resource != null && type != null)
        {            
            String typeNew = resource.duplicate(type);

            if(typeNew == null){
                return;
            }

            V selectedChart = getSelectedChart();
            V chartNew = (V) selectedChart.getCopy();            

            CustomizableXYBasePlot plotNew = chartNew.getCustomizablePlot();

            Map<Channel2DSource<?>, List<Channel2D>> duplicatedChannels = resource.getSourceChannelMap(typeNew);

            int i = 0;
            for(Entry<Channel2DSource<?>, List<Channel2D>> entry : duplicatedChannels.entrySet())
            {
                Channel2DSource<?> source = entry.getKey();

                for(Channel2D channel : entry.getValue())
                {
                    ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, source.getShortName());
                    plotNew.addOrReplaceLayer(dataset.getKey(), dataset, plotNew.getRenderer(i++));
                }
            }

            Map<String, V> charts = new LinkedHashMap<>();
            charts.put(typeNew, chartNew);

            int index = getTypePosition(type) + 1;
            insertChartPanel(typeNew, index);

            addOrUpdateResourceWithNewCharts(resource, charts);

            refreshStatisticsDialogs();
        } 
    }

    protected void refreshStatisticsDialogs()
    {
        R resource = getSelectedResource();

        Map<String, PrefixedUnit> identifierUnitMap = resource.getIdentifierUnitMap();
        roiStatisticsView.resetDialog(identifierUnitMap, resource.getDefaultOutputLocation());

        //we have to call setAllROIsLagging(), because we reset the roiStatisticsDialoh
        //in the future we should change this, we should only partly reset dialog, inserting the
        //necessry table model, and not performing full reset
        resource.setAllROIsLagging(true);


        refreshChannelStatistics(resource);

        //        refreshLiveBoxPlots(samplesForROIs);

        if(areROISamplesNeeded())
        {
            refreshROISamples();
        }
    }

    @Override
    public void editGradient() 
    {
        E panel = getSelectedPanel();
        if (panel != null) 
        {
            GradientPaintReceiver receiver = panel.getGradientPaintReceiver();

            if (rangeGradientChooser != null) 
            {
                rangeGradientChooser.showDialog(receiver);
            } 
            else 
            {
                rangeGradientChooser = new RangeGradientChooser(getAssociatedWindow(), receiver);
                rangeGradientChooser.setVisible(true);
            }
        }
    }

    @Override
    public void editHistogramGradient()
    {
        E panel = getSelectedPanel();
        if (panel != null) 
        {
            GradientPaintReceiver receiver = panel.getGradientPaintReceiver();

            if (rangeGradientHistogramChooser != null) 
            {
                rangeGradientHistogramChooser.setRangeModel(receiver);
            }
            else 
            {
                rangeGradientHistogramChooser = new RangeHistogramView(getAssociatedWindow(), receiver.getPaintedSample(), receiver, "Gradient range");
            }
            rangeGradientHistogramChooser.setVisible(true);
        }
    }

    private void createAndRegisterResourceListPopupMenu() 
    {
        JPopupMenu popup = getResourceListPopupMenu();

        JMenuItem itemAddHistograms = new JMenuItem();
        itemAddHistograms.setAction(drawHistogramsAction);
        popup.insert(itemAddHistograms, 0);

        JMenuItem itemStatistics = new JMenuItem();
        itemStatistics.setAction(showStatisticsAction);
        popup.insert(itemStatistics, 1);
    }

    @Override
    public void showGradientChooser()
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(getAssociatedWindow());
        }
        gradientSelectionDialog.setVisible(true);
    }

    @Override
    public void showGradientChooser(ColorGradientReceiver gradientReceiver)
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(getAssociatedWindow());
        }
        gradientSelectionDialog.showDialog(gradientReceiver);
    }

    @Override
    public void showGradientChooser(List<ColorGradientReceiver> gradientReceivers)
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(getAssociatedWindow());
        }
        gradientSelectionDialog.showDialog(gradientReceivers);
    }

    @Override
    protected void showMeasurementEditor()
    {
        measurementEditor.setVisible(true);
    }

    @Override
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey)
    {}

    @Override
    protected abstract void close();

    protected List<ROIStyle> getROIStylesForCurrentType()
    {
        String currentType = getSelectedType();

        List<ROIStyle> styles = getROIStyles(currentType);   
        return styles;
    }

    protected List<ROIStyle> getBoundedROIStyles()
    {
        List<ROIStyle> boundedStyles = new ArrayList<>();

        for(String type : getTypes())
        {
            boundedStyles.addAll(getROIStyles(type));
        }

        boundedStyles.removeAll(getROIStylesForCurrentType());

        return boundedStyles;
    }

    protected List<ROIStyle> getROIStyles(String type)
    {
        List<ROIStyle> styles = new ArrayList<>();

        Channel2DPanel<V> panel = getPanel(type);

        if(panel != null)
        {
            styles.addAll(panel.getROIStyles());
        }

        return styles;
    }

    protected List<ProfileStyle> getProfileStylesForCurrentType()
    {
        String currentType = getSelectedType();

        List<ProfileStyle> styles = getProfileStyles(currentType);   
        return styles;
    }

    protected List<ProfileStyle> getBoundedProfileStyles()
    {
        List<ProfileStyle> boundedStyles = new ArrayList<>();

        for(String type : getTypes())
        {
            boundedStyles.addAll(getProfileStyles(type));
        }

        boundedStyles.removeAll(getProfileStylesForCurrentType());

        return boundedStyles;
    }


    protected List<ProfileStyle> getProfileStyles(String type)
    {
        List<ProfileStyle> styles = new ArrayList<>();

        Channel2DPanel<V> panel = getPanel(type);

        if(panel != null)
        {
            styles.addAll(panel.getProfileStyles());        
        }

        return styles;
    }

    protected List<MapMarkerStyle> getMapMarkerStylesForCurrentType()
    {
        String currentType = getSelectedType();

        List<MapMarkerStyle> styles = getMapMarkerStyles(currentType);   
        return styles;
    }

    protected List<MapMarkerStyle> getBoundedMapMarkerStyles()
    {
        List<MapMarkerStyle> boundedStyles = new ArrayList<>();

        for(String type : getTypes())
        {
            boundedStyles.addAll(getMapMarkerStyles(type));
        }

        boundedStyles.removeAll(getMapMarkerStylesForCurrentType());

        return boundedStyles;
    }

    protected List<MapMarkerStyle> getMapMarkerStyles(String type)
    {
        List<MapMarkerStyle> styles = new ArrayList<>();

        Channel2DPanel<V> panel = getPanel(type);

        if(panel != null)
        {
            styles.addAll(panel.getMapMarkerStyles());
        }

        return styles;
    }

    protected List<DistanceMeasurementStyle> getBoundedDistanceMeasurementStyles()
    {
        List<DistanceMeasurementStyle> boundedStyles = new ArrayList<>();

        for(String type : getTypes())
        {
            boundedStyles.addAll(getDistanceMeasurementStyles(type));
        }

        boundedStyles.removeAll(getDistanceMeasurementStylesForCurrentType());

        return boundedStyles;
    }

    @Override
    public ColorGradient getColorGradient() 
    {
        ColorGradient table = null;
        Channel2DChart<? extends Channel2DPlot> chart = getSelectedChart();
        ChannelRenderer renderer = chart.getCustomizablePlot().getRenderer();

        if (renderer instanceof ColorGradientReceiver) 
        {
            ColorGradientReceiver paintReceiver = (ColorGradientReceiver) renderer;
            table = paintReceiver.getColorGradient();
        }
        return table;
    }

    @Override
    public void setColorGradient(ColorGradient gradient) 
    {
        if (gradient != null) 
        {
            Channel2DChart<? extends Channel2DPlot> chart = getSelectedChart();
            ChannelRenderer renderer = chart.getCustomizablePlot().getRenderer();

            if (renderer instanceof ColorGradientReceiver) 
            {
                ColorGradientReceiver paintReceiver = (ColorGradientReceiver) renderer;
                paintReceiver.setColorGradient(gradient);
            }
        }
    }


    @Override
    public void publishPreviewData(Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> charts)
    {
        if(charts.isEmpty())
        {
            return;
        }

        for(Entry<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> entry : charts.entrySet())
        {
            Resource source = entry.getKey();
            String resourceName = source.getShortName();
            Map<String,ChannelChart<?>> chartsForType = entry.getValue();

            for(Entry<String,ChannelChart<?>> innerEntry: chartsForType.entrySet())
            {
                String type = innerEntry.getKey();
                ChannelChart<?> chart = innerEntry.getValue();

                String title = resourceName + " " + type;
                JDialog dialog = new SingleChartPresentationDialog(getAssociatedWindow(), chart, title);
                dialog.setVisible(true);
            }
        }
    }

    @Override
    public void requestPreviewEnd(){      
    }

    private class AnimateAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public AnimateAction() {
            putValue(NAME, "Animate");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            R parentResource = getSelectedResource();

            Channel2DDialogModel<R> resourceModel = getResourceModel();
            AnimationModel model = resourceModel.getAnimationModel();          
            AnimationDialog dialog = new AnimationDialog(getAssociatedWindow(), model);

            dialog.setVisible(true);

            if(model.isApproved())
            {
                //we must ensure that the parent resource is chosen properly,
                //i.e. it cannot be the case that the parent resource does not
                //contain any channel included in the map

                List<StackModel<?>> stackModels = new ArrayList<>();
                MultiMap<String, Channel2D> selectedChannels = model.getSelectedChannels();

                for(Entry<String,List<Channel2D>> entry : selectedChannels.entrySet())
                {
                    String stackType = entry.getKey();
                    List<Channel2D> channels = entry.getValue();

                    DataAxis1D stackAxis = model.getStackAxis(stackType);

                    if(!channels.isEmpty())
                    {
                        Channel2D firstChannel = channels.get(0);
                        File defaultOutputFile = parentResource.getDefaultOutputLocation();

                        Quantity depthQuantity = firstChannel.getZQuantity();
                        StackModel<R> stackModel = new StackModel<>(stackAxis, depthQuantity, stackType, defaultOutputFile , parentResource.getShortName(), channels, parentResource);

                        stackModels.add(stackModel);

                        String sourceName = stackModel.getSourceName();

                        Preferences pref = Preferences.userNodeForPackage(ImagePlot.class).node(ImagePlot.class.getName()).node(stackType);
                        ProcessableXYZDataset dataset = new MovieProcessableDataset(Channel2DDataset.getDatasets(channels, sourceName), stackModel.getDepthQuantity().getName());
                        CustomizableImageRenderer renderer = new CustomizableImageRenderer(new StandardStyleTag(stackType), stackType);
                        Channel2DPlot plot = new Channel2DPlot(dataset, renderer, Datasets.DENSITY_PLOT, pref);

                        StackMapChart<Channel2DPlot> chart = new StackMapChart<>(plot, Datasets.DENSITY_PLOT);
                        chart.setStackModel(stackModel);

                        StackView<R,?,?,?> stackDialog = new StackView<>(Channel2DView.this, chart, new Channel2DPanel<>(true, true), stackModel);

                        stackDialog.setVisible(true);
                    }
                }  
            }
        }
    }  


    private class ShowHistogramsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowHistogramsAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/HistogramLarger.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.CTRL_DOWN_MASK));

            putValue(MNEMONIC_KEY, KeyEvent.VK_H);
            putValue(NAME, "Show histograms");
            putValue(SHORT_DESCRIPTION, "Show histograms");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showHistograms();
        }
    }

    private class DrawHistogramAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public DrawHistogramAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/HistogramAddLarger.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
            putValue(NAME, "Draw histogram");
            putValue(SHORT_DESCRIPTION, "Draw histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            drawHistograms();
        }
    }

    private class DrawROIHistogramsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public DrawROIHistogramsAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/DrawROIHistograms.png"));
            putValue(LARGE_ICON_KEY, icon);

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Draw ROI histograms");
            putValue(SHORT_DESCRIPTION, "Draw ROI histograms");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {                   
            drawROIHistograms();
        }
    }

    private class ShowBoxPlotsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowBoxPlotsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/showBoxPlots.png"));

            putValue(LARGE_ICON_KEY, icon);

            putValue(NAME, "Show box plots");
            putValue(SHORT_DESCRIPTION, "Show box plots");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showBoxPlots();
        }
    }

    private class DrawROIBoxPlotsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public DrawROIBoxPlotsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/drawROIBoxPlot.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Draw ROI box plots");
            putValue(SHORT_DESCRIPTION, "Draw ROI box plots");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            drawROIBoxPlots();
        }
    }

    private class ShowROIRawDataAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowROIRawDataAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Raw data ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showROIRawData();
        }
    }

    private class ShowROIShapeFactors extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowROIShapeFactors() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Show ROI shape factors");
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            showROIShapeFactors();
        }
    }

    private class ShowProfileGeometryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowProfileGeometryAction() 
        {			
            putValue(NAME, "Profile geometry");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);            
            setProfileGeometryVisible(visible);
        }
    }

    private class ShowStatisticsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowStatisticsAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/SigmaLarger.png"));
            putValue(LARGE_ICON_KEY, icon);

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Statistics");
            putValue(SHORT_DESCRIPTION, "Statistics");

            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showStatistics();
        }
    }

    private class ShowROIStatisticsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowROIStatisticsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/roiStatistics.png"));

            putValue(LARGE_ICON_KEY, icon);   
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Statistics ROIs");
            putValue(SHORT_DESCRIPTION, "Statistics ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showROIStatistics();
        }
    }

    private class SubtractROIPlaneAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public SubtractROIPlaneAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(NAME, "Correct ROI plane");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            Channel2DDataInROITransformation tr = new PolynomialFitCorrection(new int[][] {{0,1},{1}});
            transform(tr, ROIRelativePosition.INSIDE);
        }
    }


    private class ModifyProfileStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyProfileStyleAction() {
            putValue(NAME, "Profile style");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            profileEditor.setVisible(true);
        }
    }

    private class AddMapMarkerAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public AddMapMarkerAction() 
        {
            putValue(NAME, "Add map marker");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)        
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode inputMode = selected ? MouseInputModeStandard.INSERT_MAP_MARKER : MouseInputModeStandard.NORMAL;        
            setMode(inputMode);
        }
    }   

    private class ModifyROIStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyROIStyleAction() {
            putValue(NAME, "ROI style");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            roiEditor.setVisible(true);
        }
    }

    private class ModifyMapMarkerStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyMapMarkerStyleAction() {
            putValue(NAME, "Map marker style");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            mapMarkerEditor.setVisible(true);
        }
    }  

    private class FixZeroAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixZeroAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Fix zero");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new FixMinimumOperation(0, false));
        }
    }

    private class FlipHorizontallyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FlipHorizontallyAction()
        {
            putValue(NAME, "Flip horizontally");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new FlipHorizontally());
        }
    }

    private class FlipVerticallyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FlipVerticallyAction() {
            putValue(NAME, "Flip vertically");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new FlipVertically());
        }
    }

    private class TransposeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public TransposeAction() {
            putValue(NAME, "Transpose");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new Transpose());
        }
    }

    private class SharpenAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SharpenAction() {
            putValue(NAME, "Sharpen");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {            
            Channel2DDataTransformation tr = new KernelConvolution(new KernelSharpen(1,1));

            transform(tr);
        }
    }

    private class SobelOperatorAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SobelOperatorAction() {
            putValue(NAME, "Sobel");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {            
            Channel2DDataTransformation tr = new SobelOperator();
            transform(tr);
        }
    }

    private class PrewittOperatorAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrewittOperatorAction() {
            putValue(NAME, "Prewitt");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {            
            Channel2DDataTransformation tr = new PrewittOperator();
            transform(tr);
        }
    }

    private class HorizontalDerivativeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public HorizontalDerivativeAction() {
            putValue(NAME, "Horizontal derivative");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {   
            Kernel2D kernel = new Kernel2D(new double[][] {{0.5,0,-0.5}});
            Channel2DDataTransformation tr = new KernelConvolution(kernel);
            transform(tr);
        }
    }

    private class VerticalDerivativeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public VerticalDerivativeAction() 
        {
            putValue(NAME, "Vertical derivative");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {            
            Kernel2D kernel = new Kernel2D(new double[][] {{0.5},{0},{-0.5}});
            Channel2DDataTransformation tr = new KernelConvolution(kernel);
            transform(tr);
        }
    }

    private class FlipZAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FlipZAction() {
            putValue(NAME, "Invert");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new FlipZ());
        }
    }

    private class RotateClockwiseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RotateClockwiseAction() 
        {
            putValue(NAME, "Rotate clockwise");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new RotateClockwise());
        }
    }

    private class RotateCounterClockwiseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RotateCounterClockwiseAction() 
        {
            putValue(NAME, "Rotate counter-clockwise");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transform(new RotateCounterClockwise());
        }
    }

    private class OverlayAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OverlayAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/overlay.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_Y);
            putValue(NAME, "Overlay");
            putValue(SHORT_DESCRIPTION, "Overlay");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            overlay();
        }
    }

    private class ShowAllRawDataAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowAllRawDataAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showAllRawResourceData();
        }
    }

    private class MeasurePolyLineAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MeasurePolyLineAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/measurementPolyline.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Polyline measurement");
            putValue(SHORT_DESCRIPTION, "<html>Polyline measurement<br>Left click to add vertex<br>Right click to end<html>");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.DISTANCE_MEASUREMENT_POLYLINE : MouseInputModeStandard.NORMAL;
            setMode(mode);
        }
    }

    private class MeasureFreeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MeasureFreeAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/measurementFreeHand.png"));

            putValue(LARGE_ICON_KEY, icon);

            putValue(NAME, "Free hand measurement");
            putValue(SHORT_DESCRIPTION, "<html>Free hand measurement<br>Right click to end<html>");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.DISTANCE_MEASUREMENT_FREEHAND : MouseInputModeStandard.NORMAL;
            setMode(mode);
        }
    }

    private class MergeMeasurementsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MergeMeasurementsAction() {

            putValue(NAME, "Merge measurements");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            mergeMeasurements();
        }
    }

    private class ExportMeasurementsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExportMeasurementsAction() 
        {
            putValue(NAME, "Export measurements");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            exportMeasurements();
        }
    }

    private final class ImportMeasurementsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImportMeasurementsAction()
        {
            putValue(NAME, "Import measurements");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            measurementImportChooser.setApproveButtonMnemonic(KeyEvent.VK_O);
            int op = measurementImportChooser.showOpenDialog(getAssociatedWindow());
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = measurementImportChooser.getSelectedFile();

                List<MeasurementProxy> proxies = new ArrayList<>();

                try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(f)))
                {
                    int objectCount = in.readInt();
                    for(int i = 0; i<objectCount;i++)
                    {
                        proxies.add((MeasurementProxy) in.readObject());
                    }

                } catch (IOException | ClassNotFoundException e) 
                {
                    e.printStackTrace();
                    showErrorMessage("Errors occured during importing measurements");
                    return;
                }

                R resource = getSelectedResource();
                V chart = getSelectedChart();

                if(resource == null || chart == null)
                {
                    return;
                }

                DistanceMeasurementStyle style = chart.getDistanceMeasurementStyle();              
                int currentMeasurementIndex = chart.getCurrentMeasurementIndex();

                List<DistanceMeasurementDrawable> importedMeasurements = new ArrayList<>();

                for(MeasurementProxy proxy : proxies)
                {
                    importedMeasurements.add(proxy.recreateOriginalObject(style, currentMeasurementIndex++));
                }

                for(DistanceMeasurementDrawable measurement : importedMeasurements)
                {
                    addOrReplaceDistanceMeasurement(measurement);
                }
            }
        }
    }

    private class ROIHoleAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIHoleAction() 
        {
            putValue(NAME, "Draw ROI hole");
            putValue(SELECTED_KEY, false);

        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);

            setDrawROIHoles(selected);
        }
    }

    private class ROIPolygonAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIPolygonAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Polygon ROI");
            putValue(SHORT_DESCRIPTION, "<html>Polygon ROI<br>Left click to add vertex<br>Right click to end</html>");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.POLYGON_ROI : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class ROIRectangularAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIRectangularAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/rectangleRoi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Rectangular ROI");
            putValue(SHORT_DESCRIPTION, "Rectangular ROI");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.RECTANGULAR_ROI : MouseInputModeStandard.NORMAL;		
            setMode(mode);
        }
    }

    private class ROIElipticAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIElipticAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/elipticRoi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Elliptic ROI");
            putValue(SHORT_DESCRIPTION, "Elliptic ROI");
            putValue(SELECTED_KEY, false);

        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.ELIPTIC_ROI : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class ROIFreeAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ROIFreeAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/freeRoi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Free hand ROI");
            putValue(SHORT_DESCRIPTION, "<html>Free hand ROI<br>Right click to end</html>");
            putValue(SELECTED_KEY, false);

        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.FREE_HAND_ROI : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }


    private class ConvexHullROIsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ConvexHullROIsAction() {

            putValue(NAME, "Convex hull");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            calculateROIConvexHulls();
        }
    }

    private class MergeROIsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MergeROIsAction() {

            putValue(NAME, "Merge ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            mergeROIs();
        }
    }

    private class SubtractROIsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public SubtractROIsAction() {

            putValue(NAME, "Subtract ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            subtractROIs();
        }
    }

    private class ComplementROIsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ComplementROIsAction() {

            putValue(NAME, "Complement ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            complementROIs();
        }
    }


    //
    //    private class ROIWandAction extends AbstractAction 
    //    {
    //        private static final long serialVersionUID = 1L;
    //
    //        public ROIWandAction() {
    //            Toolkit toolkit = Toolkit.getDefaultToolkit();
    //            ImageIcon icon = new ImageIcon(
    //                    toolkit.getImage("Resources/roiWand.png"));
    //
    //            putValue(LARGE_ICON_KEY, icon);
    //            putValue(NAME, "Wand ROI");
    //            putValue(SHORT_DESCRIPTION, "Wand ROI");
    //            putValue(SELECTED_KEY, false);
    //        }
    //
    //        @Override
    //        public void actionPerformed(ActionEvent event) 
    //        {
    //            boolean selected = (boolean) getValue(SELECTED_KEY);
    //            MouseInputMode mode = selected ?  MouseInputModeStandard.WAND_ROI : MouseInputModeStandard.NORMAL;
    //
    //            setMode(mode);
    //        }
    //    }


    private class ExportROIsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExportROIsAction() 
        {
            putValue(NAME, "Export ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            exportROIs();
        }
    }

    private final class ImportROIsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImportROIsAction()
        {
            putValue(NAME, "Import ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            roiImportChooser.setApproveButtonMnemonic(KeyEvent.VK_O);
            int op = roiImportChooser.showOpenDialog(getAssociatedWindow());
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = roiImportChooser.getSelectedFile();

                List<ROIProxy> roiInformation = new ArrayList<>();

                try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(f)))
                {
                    int objectCount = in.readInt();
                    for(int i = 0; i<objectCount;i++)
                    {
                        roiInformation.add((ROIProxy) in.readObject());
                    }

                } catch (IOException | ClassNotFoundException e) 
                {
                    e.printStackTrace();
                    showErrorMessage("Errors occured during importing ROIs");
                    return;
                }

                R resource = getSelectedResource();
                V chart = getSelectedChart();

                if(resource == null || chart == null)
                {
                    return;
                }

                ROIStyle roiStyle = chart.getROIStyle();              
                int currentROIIndex = getCurrentROIIndex();

                List<ROIDrawable> importedROIs = new ArrayList<>();

                for(ROIProxy ri : roiInformation)
                {
                    importedROIs.add(ri.recreateOriginalObject(roiStyle, currentROIIndex++));
                }

                for(ROIDrawable roi : importedROIs)
                {
                    addOrReplaceROI(roi);
                }
            }
        }
    }

    private class ROIWandAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ROIWandAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roiWand.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Wand ROI");
            putValue(SHORT_DESCRIPTION, "Wand ROI");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.WAND_ROI : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class RotateROIAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RotateROIAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/rotateRoi.png"));

            putValue(LARGE_ICON_KEY, icon);

            putValue(NAME, "Rotate ROIs");
            putValue(SHORT_DESCRIPTION, "Rotate ROIs");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.ROTATE_ROI : getMode();

            setAccessoryMode(MouseInputType.DRAGGED, mode);
        }       
    }

    private class ExtractProfileLine extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExtractProfileLine()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/CrossSections.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(NAME, "Line profile");
            putValue(SHORT_DESCRIPTION, "Line profile");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.PROFILE_LINE : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class ExtractProfilePolyline extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExtractProfilePolyline()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/profilePolyline5.png"));

            putValue(LARGE_ICON_KEY, icon);

            putValue(NAME, "Polyline profile");
            putValue(SHORT_DESCRIPTION, "<html>Polyline profile<br>Left click to add vertex<br>Right click to end</html>");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.PROFILE_POLYLINE : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class ExtractProfileFreeHand extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExtractProfileFreeHand()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/profileFreeHand.png"));

            putValue(LARGE_ICON_KEY, icon);

            putValue(NAME, "Free hand profile");
            putValue(SHORT_DESCRIPTION, "<html>Free hand profile<br>Right click to end</html>");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.PROFILE_FREEHAND : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class ExportProfilesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExportProfilesAction() 
        {
            putValue(NAME, "Export profiles");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            exportProfiles();
        }
    }

    private final class ImportProfilesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImportProfilesAction()
        {
            putValue(NAME, "Import profiles");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            profileImportChooser.setApproveButtonMnemonic(KeyEvent.VK_O);
            int op = profileImportChooser.showOpenDialog(getAssociatedWindow());
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = profileImportChooser.getSelectedFile();

                List<ProfileProxy> proxies = new ArrayList<>();

                try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(f)))
                {
                    int objectCount = in.readInt();
                    for(int i = 0; i<objectCount;i++)
                    {
                        proxies.add((ProfileProxy) in.readObject());
                    }

                } catch (IOException | ClassNotFoundException e) 
                {
                    e.printStackTrace();
                    showErrorMessage("Errors occured during importing profiles");
                    return;
                }

                R resource = getSelectedResource();
                V chart = getSelectedChart();

                if(resource == null || chart == null)
                {
                    return;
                }

                ProfileStyle profileStyle = chart.getProfileStyle();              
                int currentProfileIndex = chart.getCurrentProfileIndex();

                List<Profile> importedProfiles = new ArrayList<>();

                for(ProfileProxy proxy : proxies)
                {
                    importedProfiles.add(proxy.recreateOriginalObject(profileStyle, currentProfileIndex++));
                }

                for(Profile profile : importedProfiles)
                {
                    addOrReplaceProfile(profile);
                }
            }
        }
    }

    private class LockAspectRatioAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LockAspectRatioAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/padlock.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);

            putValue(NAME, "Lock aspect");
            putValue(SHORT_DESCRIPTION, "Lock aspect");

            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean locked = (boolean) getValue(SELECTED_KEY);
            lockAspectRatio(locked);
        }
    }

    private class EditColorGradientAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public EditColorGradientAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Edit color gradient");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            editGradient();
        }
    }

    private class EditHistogramGradientAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public EditHistogramGradientAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G,	InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Gradient histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            editHistogramGradient();
        }
    }

    private class ROIMaskAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ROIMaskAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Apply mask");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyROIMask();
        }
    }


    private class ShowCrossSectionsDialogAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowCrossSectionsDialogAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Show profiles");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showCrossSectionsDialog();
        }
    }

    private class DuplicateImageAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public DuplicateImageAction()
        {
            putValue(NAME, "Duplicate image");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            duplicate();
        }
    }


    private class ShowLiveHistogramAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowLiveHistogramAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Live ROI histogram");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean show = (boolean) getValue(SELECTED_KEY);

            setLiveROIHistogramVisible(show);
        }
    }

    private class ShowLiveBoxPlotsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowLiveBoxPlotsAction()
        {
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Live ROI box plots");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);

            setLiveROIBoxPlotsVisible(visible);
        }
    }

    @Override
    public void requestNewDomainMarker(double knobPosition) 
    {}

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            R resource = getSelectedResource();
            if(resource != null)
            {
                resource.undo(getSelectedType());
                checkIfUndoRedoEnabled();
            }            
        }
    }

    private class UndoAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(NAME, "Undo all");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {           
            getResourceModel().undoAll(Channel2DView.this);
        }     
    }

    private class RedoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public RedoAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Redo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            R resource = getSelectedResource();
            if(resource != null)
            {
                resource.redo(getSelectedType());
                checkIfUndoRedoEnabled();
            }        
        }
    }

    private class RedoAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public RedoAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(NAME, "Redo all");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            getResourceModel().redoAll(Channel2DView.this);
        }
    }

    private class OverlayPreviewDestination implements PreviewDestination
    {
        @Override
        public Window getPublicationSite() {
            return getAssociatedWindow();
        }

        @Override
        public void publishPreviewData(
                Map<SpectroscopyBasicResource, Map<String, ChannelChart<?>>> sourceChartsMap)
        {            
        }

        @Override
        public void publishPreviewed2DData(Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> charts) 
        {
            if(!charts.isEmpty())
            {
                for(Entry<StandardChannel2DResource, Map<String, Channel2DChart<?>>> entry : charts.entrySet())
                {
                    Resource source = entry.getKey();
                    String resourceName = source.getShortName();
                    Map<String, Channel2DChart<?>> chartsForType = entry.getValue();

                    for(Entry<String, Channel2DChart<?>> innerEntry: chartsForType.entrySet())
                    {
                        String type = innerEntry.getKey();
                        ChannelChart<?> chart = innerEntry.getValue();

                        String title = resourceName + " " + type;
                        JDialog dialog = new SingleChartPresentationDialog(getAssociatedWindow(), chart, title);
                        dialog.setVisible(true);
                    }
                }
            }       
        }

        @Override
        public void requestPreviewEnd() {
        }
    }

    private class ModelChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt) 
        {
            String property = evt.getPropertyName();

            if(StandardChannel2DResource.ROI_SAMPLES_REFRESHED.equals(property))
            {
                Map<String, Map<Object, QuantitativeSample>> samplesChanged = (Map<String, Map<Object, QuantitativeSample>>) evt.getNewValue();                
                refreshComponentsBasedOnROISamples(samplesChanged);
            }
            else if(Channel2DSupervisor.ROI_SAMPLES_NEEDED.equals(property))
            {     
                refreshROISamples();
            }
        }
    }

    private class CustomMouseInteractiveToolListener implements MouseInteractiveToolListener
    {
        @Override
        public void toolToRedraw() 
        {
            E selectedPanel = getSelectedPanel();
            if(selectedPanel != null)
            {
                selectedPanel.chartChanged();
            }
        }       
    }
}
