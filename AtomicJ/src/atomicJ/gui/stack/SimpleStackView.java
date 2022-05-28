
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

package atomicJ.gui.stack;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;
import static atomicJ.gui.stack.StackModel.*;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.DataAxis1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.QuantityArray1DExpression;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitArray1DExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.ColorGradientReceiver;
import atomicJ.gui.ChannelRenderer;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.BasicViewListener;
import atomicJ.gui.Channel1DModificationSupervisor;
import atomicJ.gui.CustomChartMouseEvent;
import atomicJ.gui.DataViewListener;
import atomicJ.gui.DataViewSupport;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.Channel2DPanel;
import atomicJ.gui.Channel2DPlot;
import atomicJ.gui.Channel2DSupervisor;
import atomicJ.gui.DistanceGeometryTableModel;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.FlexibleNumericalTableView;
import atomicJ.gui.GradientPaintReceiver;
import atomicJ.gui.GradientSelectionDialog;
import atomicJ.gui.MapMarker;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.MouseInputType;
import atomicJ.gui.MovieParametersDialog;
import atomicJ.gui.MultipleNumericalTableView;
import atomicJ.gui.NumericalTableModel;
import atomicJ.gui.OrderedNumericalTable;
import atomicJ.gui.MouseInteractiveTool;
import atomicJ.gui.RangeGradientChooser;
import atomicJ.gui.RangeHistogramView;
import atomicJ.gui.RawDataTableModel;
import atomicJ.gui.ResourceGroupListener;
import atomicJ.gui.ResourceTypeListener;
import atomicJ.gui.SelectionListener;
import atomicJ.gui.StackROITableModel;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.MouseInteractiveTool.MouseInteractiveToolListener;
import atomicJ.gui.annotations.ExportAnnotationModel;
import atomicJ.gui.annotations.ExportAnnotationWizard;
import atomicJ.gui.editors.ROIMaskEditor;
import atomicJ.gui.generalProcessing.ConcurrentTransformationTask;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.histogram.HistogramWizard;
import atomicJ.gui.histogram.HistogramWizardModel;
import atomicJ.gui.histogram.HistogramWizardModelSingleSelection;
import atomicJ.gui.histogram.LiveHistogramView;
import atomicJ.gui.histogram.TransformableHistogramView;
import atomicJ.gui.imageProcessing.UndoableImageROICommand;
import atomicJ.gui.imageProcessing.UndoableTransformationCommand;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.imageProcessingActions.AddPlaneAction;
import atomicJ.gui.imageProcessingActions.Convolve2DAction;
import atomicJ.gui.imageProcessingActions.GaussianFilter2DAction;
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
import atomicJ.gui.measurements.DistanceMeasurementSupervisor;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.rois.ComplementROIModel;
import atomicJ.gui.rois.ComplementROIWizard;
import atomicJ.gui.rois.DifferenceROI;
import atomicJ.gui.rois.DifferenceROIModel;
import atomicJ.gui.rois.DifferenceROIWizard;
import atomicJ.gui.rois.ModifyObjectsWizard;
import atomicJ.gui.rois.ModifyObjectsModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIEditor;
import atomicJ.gui.rois.GeneralUnionROI;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.rois.ROIRectangle;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIProxy;
import atomicJ.gui.rois.ROIShapeFactorsTableModel;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.gui.rois.SplitROIsAction;
import atomicJ.gui.rois.WandContourTracer;
import atomicJ.gui.rois.WandROIDialog;
import atomicJ.gui.rois.WandROIModel;
import atomicJ.gui.save.MovieSaveDialog;
import atomicJ.gui.save.MultipleFrameSaveDialog;
import atomicJ.gui.save.SaveableChartSource;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;
import atomicJ.gui.statistics.UpdateableStatisticsView;
import atomicJ.gui.undo.CommandIdentifier;
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
import atomicJ.imageProcessing.PrewittOperator;
import atomicJ.imageProcessing.RotateClockwise;
import atomicJ.imageProcessing.RotateCounterClockwise;
import atomicJ.imageProcessing.SobelOperator;
import atomicJ.imageProcessing.Transpose;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.resources.Resource;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.SampleStatistics;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;

public class SimpleStackView<E extends StackModel<?>, V extends Channel2DChart<?>, K extends Channel2DPanel<V>> 
implements Channel2DSupervisor, Channel2DResourceView, DistanceMeasurementSupervisor, Channel1DModificationSupervisor,
ColorGradientReceiver, SaveableChartSource<Channel2DChart<?>>
{
    private static final Preferences DEFAULT_PREF = Preferences.userNodeForPackage(StackMapView.class).node(StackMapView.class.getName());

    private final SaveAction saveAction = new SaveAction();
    private final SaveAllAction saveAllAction = new SaveAllAction();
    private final PrintAction printAction = new PrintAction();
    private final CloseAction closeAction = new CloseAction();

    private final JMenuBar menuBar = new JMenuBar();

    private final Action editChartAction = new EditChartAction();
    private final Action editGradientAction = new EditGradientAction();
    private final Action editHistogramGradientAction = new EditHistogramGradientAction();
    private final Action showStatisticsAction = new ShowStatisticsAction();
    private final Action showROIStatisticsAction = new ShowROIStatisticsAction();
    private final Action resizeAction = new Gridding2DAction(this);
    private final Action showHistogramsAction = new ShowHistogramsAction();
    private final Action drawHistogramAction = new DrawHistogramAction();
    private final Action showLiveHistogramAction = new ShowLiveHistogramAction();
    private final Action showRawDataAction = new ShowRawDataAction();
    private final Action lockAspectRatioAction = new LockAspectRatioAction();

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

    private final Action nextFrameAction = new NextFrameAction();
    private final Action previousFrameAction = new PreviousFrameAction();
    private final Action firstFrameAction = new FirstFrameAction();
    private final Action lastFrameAction = new LastFrameAction();
    private final PlayMovieAction playMovieAction = new PlayMovieAction();
    private final Action playForwardAction = new PlayForwardAction();
    private final Action editMovieParametersAction = new EditMovieParametersAction();
    private final Action saveAsMovieAction = new SaveAsMovieAction();
    private final Action stopMovieAction = new StopMovieAction();

    private final Action modifyROIStyleAction = new ModifyROIStyleAction();
    private final Action roiMaskAction = new ROIMaskAction();
    private final Action showROIShapeFactorsAction = new ShowShapeFactorsForRois();
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

    private final Action modifyDistanceMeasurementStyle = new ModifyDistanceStyleAction();
    private final Action showDistanceMeasurementsAction = new ShowDistanceMeasurementsAction();
    private final Action measureLineAction = new MeasureDistancesAction();

    private final Action undoAction = new UndoAction();
    private final Action undoAllAction = new UndoAllAction();
    private final Action redoAction = new RedoAction();
    private final Action redoAllAction = new RedoAllAction();

    private final StackModelPropertyListener stackModelProperyListener = new StackModelPropertyListener();

    private JMenu measureMenu;
    private JMenu roisMenu;

    private final UpdateableStatisticsView frameSamplesDialog;

    private final K stackPanel;
    private final JSlider frameSlider;

    private E stackModel;

    private final MultipleFrameSaveDialog frameSaveDialog;
    private final MovieSaveDialog aviSaveDialog;
    private final MovieParametersDialog movieParametersDialog;	

    private GradientSelectionDialog gradientSelectionDialog;
    private RangeGradientChooser rangeGradientChooser;
    private RangeHistogramView rangeGradientHistogramChooser;

    private final TransformableHistogramView frameHistogramView;
    private final LiveHistogramView liveHistogramView;

    private HistogramWizard histogramsWizard;

    private MouseInputMode mode = MouseInputModeStandard.NORMAL;
    private Map<MouseInputType, MouseInputMode> accessoryModes = new HashMap<>();

    ////////////////////////////DISTANCE MEASUREMENT///////////////////////////////////

    private final Map<Object, DistanceMeasurementDrawable> distanceMeasurements = new LinkedHashMap<>();
    private final FlexibleNumericalTableView distanceMeasurementsDialog;
    private final DistanceMeasurementEditor measurementEditor;

    ////////////////////////////ROIS////////////////////////////////////////////////////

    private Map<Object, ROIDrawable> rois = new LinkedHashMap<>();
    private final FlexibleNumericalTableView roiShapeFactorsDialog;
    private final ROIEditor roiEditor;
    private ROIMaskEditor roiMaskEditor;

    private WandROIDialog wandROIDialog;
    private final WandROIModel wandROIModel = new WandROIModel(this);
    private ModifyObjectsWizard roiMergeWizard;
    private DifferenceROIWizard roiDifferenceWizard;
    private ComplementROIWizard complementROIWizard;
    private ModifyObjectsWizard roiConvexHullWizard;
    private ExportAnnotationWizard exportAnnotationWizard;
    private MultipleSelectionWizard<SampleStatistics> roiStatisticsWizard;

    private final ExtensionFileChooser roiImportChooser;
    ////////////////////////////////////////////////////////////////////////////////////

    private MouseInteractiveTool mouseInteractiveTool;
    private final CustomMouseInteractiveToolListener mouseInteractiveToolListener = new CustomMouseInteractiveToolListener();

    private final Preferences pref;

    private final Set<DistanceMeasurementReceiver> distanceMeasurementReceivers = new LinkedHashSet<>();
    private final Set<ROIReceiver> roiReceivers = new LinkedHashSet<>();

    private final DataViewSupport dataViewSupport = new DataViewSupport();
    private final JDialog viewDialog ;

    private final boolean temporary;

    public SimpleStackView(Window parent, V chart, K stackPanel, E stackModel, boolean temporary)
    {
        this(parent, chart, stackPanel, stackModel, DEFAULT_PREF, temporary);
    }

    public SimpleStackView(Window parent, V chart, K stackPanel, E stackModel, final Preferences pref, boolean temporary)
    {
        viewDialog = new JDialog(parent, stackModel.getStackType(), ModalityType.MODELESS);	

        this.temporary = temporary;

        this.frameHistogramView = new TransformableHistogramView(viewDialog, "Stack frame histogram");
        this.liveHistogramView = new LiveHistogramView(viewDialog, "Live stack histogram", ModalityType.MODELESS, false, false);

        this.measurementEditor = new DistanceMeasurementEditor(viewDialog);
        this.roiEditor = new ROIEditor(viewDialog);
        this.wandROIDialog = new WandROIDialog(viewDialog, "Wand ROI", false);

        this.roiShapeFactorsDialog = new FlexibleNumericalTableView(viewDialog, new StandardNumericalTable(new ROIShapeFactorsTableModel(Units.MICRO_METER_UNIT), true, true), "ROI shape factors");
        this.frameSamplesDialog = new UpdateableStatisticsView(viewDialog, "Frame statistics", Preferences.userRoot().node(getClass().getName()).node("FrameSampleStatistics"), true);

        this.pref = pref;
        this.stackPanel = stackPanel;
        initPanel(stackPanel, chart);

        this.stackModel = stackModel;
        stackModel.addPropertyChangeListener(stackModelProperyListener);

        playForwardAction.putValue(Action.SELECTED_KEY, stackModel.isPlayedForward());
        this.movieParametersDialog = new MovieParametersDialog(viewDialog, stackModel);//

        this.frameSaveDialog = new MultipleFrameSaveDialog(viewDialog, this, stackModel, pref);
        frameSaveDialog.setKey(stackModel.getDepthQuantity().getName());
        frameSaveDialog.pack();

        this.aviSaveDialog = MovieSaveDialog.getInstance(viewDialog, stackPanel);

        File defaultOutputDirectory = stackModel.getDefaultOutputDirectory();
        PrefixedUnit xUnit = stackModel.getXQuantity().getUnit();
        PrefixedUnit yUnit = stackModel.getYQuantity().getUnit();
        this.distanceMeasurementsDialog = new FlexibleNumericalTableView(viewDialog, new StandardNumericalTable(new DistanceGeometryTableModel(defaultOutputDirectory, xUnit, yUnit), true, true), "Distance measurements");

        DistanceMeasurementStyle measurementsStyle = chart.getDistanceMeasurementStyle();
        this.measurementEditor.setModel(measurementsStyle);

        ROIStyle roiStyleModel = chart.getROIStyle();
        this.roiEditor.setModel(roiStyleModel);

        this.roiReceivers.add(wandROIModel);

        this.roiImportChooser = new ExtensionFileChooser(pref, "ROI file (.roi)", "roi", true);

        frameSamplesDialog.resetDialog(stackModel.getIdentifierUnitMap(), stackModel.getDefaultOutputDirectory());
        frameSamplesDialog.refreshSamples(stackModel.getSamples());

        PrefixedUnit axisUnitNew = stackPanel.getZAxisUnit();
        wandROIModel.setUnitDifference(axisUnitNew);

        this.frameSlider = buildFrameSlider();

        JPanel outerPanel = new JPanel(new BorderLayout());

        outerPanel.add(stackPanel, BorderLayout.CENTER);
        outerPanel.add(buildControlPanel(), BorderLayout.SOUTH);            

        viewDialog.add(outerPanel);

        buildMenuBar();

        showFrame(stackModel.getCurrentFrame());

        showHistogramsAction.setEnabled(!frameHistogramView.isEmpty());

        initViewListener();
        initDialogListeners();

        viewDialog.setSize(getDefaultWidth(),getDefaultHeight());
        viewDialog.setLocationRelativeTo(parent);

        checkIfUndoRedoEnabled();
        //don't know why this in necessary to call lockAspectRatio() here
        lockAspectRatio(chart.getUseFixedChartAreaSize());		
        updateROIAvailabilityPrivate();

        if(temporary)                   
        {
            viewDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    private void initDialogListeners()
    {
        viewDialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {      
                pref.putInt(WINDOW_HEIGHT, viewDialog.getHeight());
                pref.putInt(WINDOW_WIDTH, viewDialog.getWidth());

                dataViewSupport.fireDataViewVisiblityChanged(false);

                if(temporary)                   
                {
                    cleanUp();
                    viewDialog.dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent evt)
            {
                if(temporary)                   
                {
                    cleanUp();
                }
            }
        });     

        viewDialog.addComponentListener(new ComponentAdapter() 
        {
            @Override
            public void componentShown(ComponentEvent evt)
            {
                dataViewSupport.fireDataViewVisiblityChanged(true);               
            }
        });
    }

    protected JMenuBar getMenuBar()
    {
        return menuBar;
    }

    private void initPanel(K panel, V chart)
    {
        panel.setDensitySupervisor(this);
        panel.setDistanceMeasurementSupervisor(this);
        panel.setDataModificationSupervisor(this);

        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5),BorderFactory.createBevelBorder(BevelBorder.LOWERED)));                
        panel.setSelectedChart(chart); 
    }

    private JSlider buildFrameSlider()
    {
        int frameCount = stackModel.getFrameCount();
        JSlider frameSlider = new JSlider(0, frameCount - 1, 0);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(Integer.valueOf(0), new JLabel(stackModel.getStackLowerLabel()));
        labelTable.put(Integer.valueOf(frameCount - 1), new JLabel(stackModel.getStackUpperLabel()));

        frameSlider.setLabelTable(labelTable);
        frameSlider.setPaintLabels(true);
        frameSlider.setSnapToTicks(true);
        frameSlider.setMajorTickSpacing(1);       
        frameSlider.setFocusable(false);
        frameSlider.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e)
            {
                JSlider source = (JSlider)e.getSource();       
                int frame = source.getValue();
                stackModel.setCurrentFrame(frame);                      
            }
        });

        return frameSlider;
    }

    private void initViewListener()
    {
        frameHistogramView.addDataViewListener(new BasicViewListener(showHistogramsAction));
        liveHistogramView.addDataViewListener(new BasicViewListener(showLiveHistogramAction)
        {
            @Override
            public void dataViewVisibilityChanged(boolean visibleNew)
            {
                showLiveHistogramAction.putValue(Action.SELECTED_KEY, visibleNew);
            }
        });
    }

    public boolean isVisible()
    {
        return viewDialog.isVisible();
    }

    public void setVisible(boolean visible)
    {
        viewDialog.setVisible(visible);
    }

    public void addDataViewListener(DataViewListener listener)
    {
        dataViewSupport.addDataViewListener(listener);
    }

    public void removeDataViewListener(DataViewListener listener)
    {
        dataViewSupport.removeDataViewListener(listener);
    }

    protected void fireDataAvailabilityChanged(boolean dataAvailableNew)
    {
        dataViewSupport.fireDataAvailabilityChanged(dataAvailableNew);
    }

    protected void showErrorMessageDilog(String message)
    {
        JOptionPane.showMessageDialog(viewDialog, message, AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);   
    }

    private void buildMenuBar()
    {
        JMenuItem saveItem = new JMenuItem(saveAction);
        JMenuItem saveAllItem = new JMenuItem(saveAllAction);
        JMenuItem saveAsMovieItem = new JMenuItem(saveAsMovieAction);
        JMenuItem printItem = new JMenuItem(printAction);
        JMenuItem closeItem = new JMenuItem(closeAction);

        JMenuItem nextFrameItem = new JMenuItem(nextFrameAction);
        JMenuItem previousFrameItem = new JMenuItem(previousFrameAction);
        JMenuItem firstFrameItem = new JMenuItem(firstFrameAction);
        JMenuItem lastFrameItem = new JMenuItem(lastFrameAction);
        JMenuItem playMovieItem = new JCheckBoxMenuItem(playMovieAction);
        JMenuItem playForwardItem = new JCheckBoxMenuItem(playForwardAction);
        JMenuItem editMovieParametersItem = new JMenuItem(editMovieParametersAction);

        JMenuItem editChartItem = new JMenuItem(editChartAction);
        JMenuItem editHistogramGradientItem = new JMenuItem(editHistogramGradientAction);
        JMenuItem editGradientItem = new JMenuItem(editGradientAction);
        JMenuItem resizeItem = new JMenuItem(resizeAction);      
        JMenuItem showHistogramsItem = new JCheckBoxMenuItem(showHistogramsAction);
        JMenuItem drawHistogramItem = new JMenuItem(drawHistogramAction);
        JMenuItem showLiveHistogramItem = new JCheckBoxMenuItem(showLiveHistogramAction);
        JMenuItem showRawDataItem = new JMenuItem(showRawDataAction);
        JMenuItem statisticsItem = new JMenuItem(showStatisticsAction);   
        JMenuItem statisticsROIItem = new JMenuItem(showROIStatisticsAction);

        JMenuItem fixZeroItem = new JMenuItem(fixZeroAction);
        JMenuItem flipHorizontallyItem = new JMenuItem(flipHorizontallyAction);
        JMenuItem flipVerticallyItem = new JMenuItem(flipVerticallyAction);
        JMenuItem flipBothItem = new JMenuItem(transposeAction);
        JMenuItem flipZItem = new JMenuItem(flipZAction);
        JMenuItem rotateClockwiseItem = new JMenuItem(rotateClockwiseAction);
        JMenuItem rotateCounterClockwiseItem = new JMenuItem(rotateCounterClockwiseAction);
        JMenuItem rotateArbitraryAngleItem = new JMenuItem(rotateArbitraryAngleAction);

        JMenuItem addPlaneItem = new JMenuItem(addPlaneAction);
        JMenuItem thresholdDataItem = new JMenuItem(thresholdDataAction);

        JMenuItem subtractPolynomialBackgroundItem = new JMenuItem(subtractPolynomialBackgroundAction);
        JMenuItem lineMatchingCorrectionItem = new JMenuItem(lineMatchingCorrectionAction);
        JMenuItem lineFitCorrectionItem = new JMenuItem(lineFitCorrectionAction);
        JMenuItem replaceImageDataItem = new JMenuItem(replaceDataAction);

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

        JMenuItem modifyROIStyleItem = new JMenuItem(modifyROIStyleAction);
        JMenuItem roiMaskItem = new JMenuItem(roiMaskAction);
        JMenuItem shapeFactorsROIItem = new JMenuItem(showROIShapeFactorsAction);
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

        JMenuItem modifyMeasurementsStyleItem = new JMenuItem(modifyDistanceMeasurementStyle);
        JMenuItem showMeasurementsItem = new JMenuItem(showDistanceMeasurementsAction);
        JMenuItem measureLineItem = new JCheckBoxMenuItem(measureLineAction);

        JMenuItem convolveItem = new JMenuItem(convolveAction);

        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem undoAllItem = new JMenuItem(undoAllAction);
        JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem redoAllItem = new JMenuItem(redoAllAction);

        JMenuItem lockAspectRatioItem = new JCheckBoxMenuItem(lockAspectRatioAction);


        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        fileMenu.add(saveItem);
        fileMenu.add(saveAllItem);
        fileMenu.add(saveAsMovieItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);

        JMenu chartMenu = new JMenu("Chart");
        chartMenu.setMnemonic(KeyEvent.VK_C);

        chartMenu.add(editChartItem);
        chartMenu.add(editGradientItem);
        chartMenu.add(editHistogramGradientItem);

        chartMenu.addSeparator();

        chartMenu.add(lockAspectRatioItem);

        JMenu processMenu = new JMenu("Process");

        processMenu.add(undoItem);
        processMenu.add(redoItem);
        processMenu.addSeparator();

        processMenu.add(undoAllItem);
        processMenu.add(redoAllItem);
        processMenu.addSeparator();
        processMenu.add(fixZeroItem);
        processMenu.add(subtractPolynomialBackgroundItem);
        processMenu.add(lineMatchingCorrectionItem);
        processMenu.add(lineFitCorrectionItem);

        processMenu.addSeparator();

        processMenu.add(thresholdDataItem);
        processMenu.add(addPlaneItem);
        processMenu.add(replaceImageDataItem);
        processMenu.add(resizeItem);  
        processMenu.add(convolveItem);

        JMenu transformSubMenu = new JMenu("Transform");

        transformSubMenu.add(flipHorizontallyItem);
        transformSubMenu.add(flipVerticallyItem);
        transformSubMenu.add(flipBothItem);
        transformSubMenu.add(flipZItem);
        transformSubMenu.add(rotateClockwiseItem);
        transformSubMenu.add(rotateCounterClockwiseItem);
        transformSubMenu.add(rotateArbitraryAngleItem);

        processMenu.add(transformSubMenu);

        JMenu filterSubMenu = new JMenu("Filter");

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

        JMenu dataMenu = new JMenu("Data");

        dataMenu.add(drawHistogramItem);
        dataMenu.add(showHistogramsItem);
        dataMenu.add(showLiveHistogramItem);
        dataMenu.addSeparator();
        dataMenu.add(statisticsItem);  
        dataMenu.add(statisticsROIItem);
        dataMenu.add(showRawDataItem);

        this.measureMenu = new JMenu("Measure");
        measureMenu.add(modifyMeasurementsStyleItem);
        measureMenu.add(showMeasurementsItem);

        measureMenu.addSeparator();
        measureMenu.add(measureLineItem);

        this.roisMenu = new JMenu("ROI");
        roisMenu.setMnemonic(KeyEvent.VK_R);

        roisMenu.add(modifyROIStyleItem);
        roisMenu.add(roiMaskItem);
        roisMenu.addSeparator();
        roisMenu.add(shapeFactorsROIItem);

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
        roisMenu.add(rotateROIItem);

        roisMenu.addSeparator();
        roisMenu.add(exportROIsItem);
        roisMenu.add(importROIsItem);

        JMenu movieMenu = new JMenu("Movie");
        movieMenu.setMnemonic(KeyEvent.VK_M);

        movieMenu.add(playMovieItem);
        movieMenu.add(playForwardItem);
        movieMenu.add(editMovieParametersItem);

        movieMenu.add(previousFrameItem);
        movieMenu.add(nextFrameItem);
        movieMenu.add(firstFrameItem);
        movieMenu.add(lastFrameItem);       

        menuBar.add(fileMenu);  
        menuBar.add(chartMenu);
        menuBar.add(dataMenu);
        menuBar.add(processMenu);
        menuBar.add(measureMenu);
        menuBar.add(roisMenu);
        menuBar.add(movieMenu);

        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(1,1,1,1)));
        viewDialog.setJMenuBar(menuBar);
    }

    public void setStackModel(E stackModelNew)
    {
        StackModel<?> stackModelOld = this.stackModel;

        if(stackModelOld != null)
        {
            stackModelOld.removePropertyChangeListener(stackModelProperyListener);
            stackModelOld.cleanup();
        }

        this.stackModel = stackModelNew;
        stackModelNew.addPropertyChangeListener(stackModelProperyListener);

        playForwardAction.putValue(Action.SELECTED_KEY, stackModelNew.isPlayedForward());
        this.movieParametersDialog.setModel(stackModelNew);

        this.frameSaveDialog.setConsistentWithStackModel(stackModelNew);

        //this should be done in the future... As for now, changing stack model works ok only for
        //similar models, i.e. if new one is derived from the old one by smoothing

        //frameSaveDialog.setKey(stackModel.getDepthQuantity().getName()); 
        //File defaultOutputDirectory = stackModel.getDefaultOutputDirectory();
        //String xUnit = stackModel.getXQuantity().getUnit();
        //String yUnit = stackModel.getYQuantity().getUnit();
        //this.distanceMeasurementsDialog = new FlexibleNumericalTableDialog(this, new StandardNumericalTable(new LineGeometryTableModel(defaultOutputDirectory, xUnit, yUnit), true, true), "Distance measurements");

        int frameCount = stackModelNew.getFrameCount();
        frameSlider.setMaximum(frameCount);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(Integer.valueOf(0), new JLabel(stackModelNew.getStackLowerLabel()));
        labelTable.put(Integer.valueOf(frameCount - 1), new JLabel(stackModelNew.getStackUpperLabel()));

        frameSlider.setLabelTable(labelTable);
        viewDialog.revalidate();
    }

    public StackModel<?> getHorizontalSectionsStackModel()
    {
        StackModel<?> horizontalSectionsStackModel = stackModel.getHorizontalSections();
        return horizontalSectionsStackModel;
    }

    public StackModel<?> getVerticalSectionsStackModel()
    {
        StackModel<?> verticalSectionsStackModel = stackModel.getVerticalSections();
        return verticalSectionsStackModel;
    }

    public V getSelectedChart()
    {
        V chart = stackPanel.getSelectedChart();
        return chart;
    }

    public V getChart(String type, int index)
    {
        V chart = Objects.equals(stackModel.getStackType(), type) ? stackPanel.getChartAt(index) : null;

        return chart;
    }

    public V getChart(Resource resource, String type)
    {
        V chart = stackModel.containsResource(resource) ? stackPanel.getSelectedChart() : null;

        return chart;
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

    protected void copyDistanceMeasurementsToReceiver(DistanceMeasurementReceiver receiver)
    {
        for(Entry<Object, DistanceMeasurementDrawable> entry: distanceMeasurements.entrySet())
        {
            DistanceMeasurementDrawable measurement = entry.getValue();
            receiver.addOrReplaceDistanceMeasurement(measurement);
        }
    }

    protected void copyROIsToReceiver(ROIReceiver receiver)
    {
        for(Entry<Object, ROIDrawable> entry: rois.entrySet())
        {
            ROIDrawable roi = entry.getValue();
            receiver.addOrReplaceROI(roi);
        }
    }

    @Override
    public MouseInputMode getMode()
    {
        return mode;
    }

    @Override
    public void setMode(MouseInputMode modeNew)
    { 
        setMode(modeNew, false);
    }

    public void setMode(MouseInputMode modeNew, boolean clearAccessoryModels)
    { 
        MouseInputMode modeOld = this.mode;
        this.mode = modeNew;

        setConsistentWithMode(modeOld, modeNew);
        if(clearAccessoryModels)
        {
            clearAccessoryModels();
        }
    }

    protected void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew) 
    {
        boolean isMeasurements = modeNew.isMeasurement();

        boolean isLineMeasurement = MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE.equals(modeNew);

        if(isMeasurements)
        {
            showDistanceMeasurements();
        }

        boolean isPolygonROI = MouseInputModeStandard.POLYGON_ROI.equals(modeNew);
        boolean isRectangularROI = MouseInputModeStandard.RECTANGULAR_ROI.equals(modeNew);
        boolean isElipticROI = MouseInputModeStandard.ELIPTIC_ROI.equals(modeNew);
        boolean isFreeHandROI = MouseInputModeStandard.FREE_HAND_ROI.equals(modeNew);
        boolean isWandROI = MouseInputModeStandard.WAND_ROI.equals(modeNew);

        measureLineAction.putValue(Action.SELECTED_KEY, isLineMeasurement);

        roiPolygonalAction.putValue(Action.SELECTED_KEY, isPolygonROI);
        roiRectangularAction.putValue(Action.SELECTED_KEY, isRectangularROI);
        roiElipticAction.putValue(Action.SELECTED_KEY, isElipticROI);
        roiFreeAction.putValue(Action.SELECTED_KEY, isFreeHandROI);
        roiWandAction.putValue(Action.SELECTED_KEY, isWandROI);

        boolean wandModeAcquired = !MouseInputModeStandard.WAND_ROI.equals(modeOld) && isWandROI;
        boolean wandModeLost = MouseInputModeStandard.WAND_ROI.equals(modeOld) && !isWandROI;

        if(wandModeAcquired)
        {
            showWandROIDialog();
        }
        else if(wandModeLost)
        {
            wandROIDialog.setVisible(false);
        }
        stackPanel.setMode(mode);
    }

    protected void setConsistentWithAccessoryMode(MouseInputType inputType, MouseInputMode modeOld, MouseInputMode modeNew)
    {
        stackPanel.setAccessoryMode(inputType, modeNew);

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


    private void showWandROIDialog()
    {
        PrefixedUnit axisUnitNew = stackPanel.getZAxisUnit();
        wandROIModel.setUnitDifference(axisUnitNew);
        wandROIDialog.showDialog(wandROIModel);
    }

    @Override
    public MouseInputMode getMode(MouseInputType inputType)
    {
        MouseInputMode accessoryMode = this.accessoryModes.get(inputType);

        if(accessoryMode != null)
        {
            return accessoryMode;
        }

        return this.mode;
    }

    @Override
    public void setAccessoryMode(MouseInputType inputType, MouseInputMode modeNew)
    {
        MouseInputMode modeOld = getMode(inputType);

        this.accessoryModes.put(inputType, modeNew);

        setConsistentWithAccessoryMode(inputType, modeOld, modeNew);
    }

    private void clearAccessoryModels()
    {
        Map<MouseInputType, MouseInputMode> acessoryModesOld = new HashMap<>(accessoryModes);
        this.accessoryModes = new HashMap<>();

        for(Entry<MouseInputType, MouseInputMode> entry : acessoryModesOld.entrySet())
        {
            setConsistentWithAccessoryMode(entry.getKey(), entry.getValue(), this.mode);
        }
    }

    protected int getDefaultWidth()
    {	    
        int width =  Math.max(30, pref.getInt(WINDOW_WIDTH, 500));

        return width;
    }

    protected int getDefaultHeight()
    {  	    
        int height =  Math.max(30, pref.getInt(WINDOW_HEIGHT, 500));

        return height;
    }

    protected Channel2DChart<?> getChart()
    {
        return stackPanel.getSelectedChart();
    }

    protected K getStackPanel()
    {
        return stackPanel;
    }

    protected JMenu getMeasureMenu()
    {
        return measureMenu;
    }

    protected JMenu getROIsMenu()
    {
        return roisMenu;
    }	

    public E getStackModel()
    {
        return stackModel;
    }

    @Override
    public boolean areHistogramsAvaialable()
    {
        boolean available = !frameHistogramView.isEmpty();
        return available;
    }

    private void setHistogramsAvailable(boolean available)
    {
        showHistogramsAction.setEnabled(available);
        stackPanel.setHistogramsAvialable(available);
    }

    public void addHistograms(HistogramWizardModel model)
    {
        if(histogramsWizard == null)
        {
            histogramsWizard = new HistogramWizard(model);
        }
        else
        {
            histogramsWizard.setWizardModel(model);
        }
        histogramsWizard.showDialog();

        setHistogramsAvailable(areHistogramsAvaialable());
    }

    @Override
    public void drawHistograms()
    {		
        List<SampleCollection> samples = stackModel.getSampleCollections();	

        HistogramWizardModel model = new HistogramWizardModelSingleSelection(frameHistogramView, samples);

        addHistograms(model);		
    }

    public void setLiveHistogramVisible(boolean visible)
    {	
        if(liveHistogramView.isEmpty() && visible)
        {
            List<SampleCollection> sampleCollections = stackModel.getCurrentSampleCollections();	

            HistogramWizardModel model = new HistogramWizardModelSingleSelection(liveHistogramView, sampleCollections);

            if(histogramsWizard == null)
            {
                histogramsWizard = new HistogramWizard(model);
            }
            else
            {
                histogramsWizard.setWizardModel(model);
            }

            histogramsWizard.showDialog();
        }
        else
        {
            if(visible)
            {
                QuantitativeSample sample = stackModel.getCurrentSample();
                liveHistogramView.updateSampleModel(sample);
            }

            liveHistogramView.setVisible(visible);    
        }
    }

    @Override
    public void showHistograms()
    {
        setHistogramsVisible(true);
    }

    public void  setHistogramsVisible(boolean visible)
    {
        frameHistogramView.setVisible(visible);
    }

    public void cleanUp()
    {
        stackModel.cleanup();
        stackPanel.clear();

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

        if(roiMergeWizard != null)
        {
            roiMergeWizard.dispose();
        }

        if(wandROIDialog != null)
        {
            wandROIDialog.dispose();
        }

        if(gradientSelectionDialog != null)
        {
            gradientSelectionDialog.cleanUp();
            gradientSelectionDialog.dispose();
        }

        gradientSelectionDialog = null;
        rangeGradientChooser = null;
        roiMaskEditor = null;
        wandROIDialog = null;
        rangeGradientHistogramChooser = null;
        roiMergeWizard = null;

        if(this.mouseInteractiveTool != null)
        {
            mouseInteractiveTool.notifyOfToolModeLoss();
            mouseInteractiveTool.removeMouseToolListerner(mouseInteractiveToolListener);
        }

        frameSamplesDialog.clear();
    }

    private JPanel buildControlPanel()
    {
        JPanel controlPanel = new JPanel();
        GroupLayout layout = new GroupLayout(controlPanel);
        controlPanel.setLayout(layout);
        layout.setAutoCreateGaps(false);
        layout.setAutoCreateContainerGaps(true);		

        JButton buttonFirstFrame = new JButton(firstFrameAction);
        buttonFirstFrame.setMargin(new Insets(2,2,2,4));
        JButton buttonLastFrame = new JButton(lastFrameAction);
        buttonLastFrame.setMargin(new Insets(2,4,2,2));
        JButton buttonPreviousFrame = new JButton(previousFrameAction);
        buttonPreviousFrame.setMargin(new Insets(2,2,2,4));
        JButton buttonNextFrame = new JButton(nextFrameAction);
        buttonNextFrame.setMargin(new Insets(2,4,2,2));
        JToggleButton buttonPlayMovie = new JToggleButton(playMovieAction);
        buttonPlayMovie.setMargin(new Insets(2,2,2,2));

        buttonFirstFrame.setHideActionText(true);
        buttonLastFrame.setHideActionText(true);
        buttonPreviousFrame.setHideActionText(true);
        buttonNextFrame.setHideActionText(true);	
        buttonPlayMovie.setHideActionText(true);

        layout.setHorizontalGroup(layout.createParallelGroup().addComponent(frameSlider)

                .addGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonFirstFrame)
                        .addComponent(buttonPreviousFrame)
                        .addComponent(buttonPlayMovie)
                        .addComponent(buttonNextFrame)
                        .addComponent(buttonLastFrame).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(frameSlider)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(buttonFirstFrame)
                        .addComponent(buttonPreviousFrame)
                        .addComponent(buttonPlayMovie)
                        .addComponent(buttonNextFrame)
                        .addComponent(buttonLastFrame)));

        layout.linkSize(SwingConstants.HORIZONTAL, buttonFirstFrame, buttonLastFrame);
        layout.linkSize(SwingConstants.HORIZONTAL, buttonPreviousFrame, buttonNextFrame);
        layout.linkSize(SwingConstants.VERTICAL, buttonFirstFrame, buttonLastFrame, buttonPreviousFrame, buttonNextFrame);

        controlPanel.setBorder(BorderFactory.createEmptyBorder(0,40,0,40));
        return controlPanel;
    }

    private void setMoviePlayedForward(boolean playedForward)
    {
        playMovieAction.setPlayForward(playedForward);
        stackModel.setPlayedForward(playedForward);
    }

    private void showFrame(int frameIndex)
    {
        Channel2DChart<?> chart = stackPanel.getSelectedChart();

        if(chart != null)
        {            
            frameSlider.setValue(frameIndex);
            chart.showFrame(frameIndex);

            double currentValue = stackModel.getCurrentStackingValue();
            PrefixedUnit unit = stackModel.getStackingQuantity().getUnit();

            chart.setFrameTitle(currentValue, unit);
        }
    }  

    private void editMovieParameters()
    {
        this.movieParametersDialog.setVisible(true);
    }

    protected void setSaveDialogVisible(boolean vis)
    {
        frameSaveDialog.setVisible(vis);
    }

    @Override
    public void drawROIHistograms() {
        // TODO Auto-generated method stub

    }
    @Override
    public void drawROIHistograms(List<ROI> list) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showRawData() 
    {
        List<SampleCollection> sampleCollection = getSampleCollections(true);
        publishRawData(sampleCollection);
    }
    @Override
    public void showROIRawData() {
        // TODO Auto-generated method stub

    }
    @Override
    public void showROIRawData(List<ROI> list) {
        // TODO Auto-generated method stub

    }

    public void publishRawData(List<SampleCollection> rawData)
    {
        if(!rawData.isEmpty())
        {
            Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
            for(SampleCollection collection: rawData)
            {
                ///////INCLUDES ALL SAMPLES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                collection.setKeysIncluded(true);

                /////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                String collectionName = collection.getShortName();
                RawDataTableModel model = new RawDataTableModel(collection, false);

                StandardNumericalTable table = new OrderedNumericalTable(model, true);
                tables.put(collectionName, table);				
            }

            MultipleNumericalTableView dialog = new MultipleNumericalTableView(viewDialog, tables, "Raw data", true);
            dialog.setVisible(true);
        }
    }

    protected List<SampleCollection> getSampleCollections(boolean includeCoordinates)
    {
        Map<String, QuantitativeSample> samples = stackModel.getSamples(includeCoordinates);

        SampleCollection sampleCollection = new StandardSampleCollection(samples, "Stack sample", "Stack sample", stackModel.getDefaultOutputDirectory());
        return Collections.singletonList(sampleCollection);
    }

    @Override
    public void showStatistics() 
    {
        frameSamplesDialog.setVisible(true);
    }

    @Override
    public void showROIStatistics(Map<Object, ROI> rois) 
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void showROIStatistics() 
    {
        UpdateableStatisticsView roiStatisticsDialog = new UpdateableStatisticsView(viewDialog, "Statistics for ROIs", Preferences.userRoot().node(getClass().getName()).node("ROISampleStatistics"),true);

    }

    @Override
    public void setROIStyleEditorVisible(boolean visible) 
    {
        roiEditor.setVisible(visible);		
    }

    @Override
    public void setProfileStyleEditorVisible(boolean visible) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProfiles(Map<Object, Profile> profiles) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addOrReplaceProfile(Profile profile) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeProfile(Profile profile) {
        // TODO Auto-generated method stub

    }



    @Override
    public WandContourTracer getWandTracer()
    {
        return wandROIModel.getContourTracer();
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {
        Object measurementKey = measurement.getKey();
        DistanceMeasurementDrawable oldMeasurement = distanceMeasurements.get(measurementKey);

        boolean newMeasurement = !measurement.equalsUpToStyle(oldMeasurement);

        if(newMeasurement)
        {            
            DistanceMeasurementDrawable measurementCopy = measurement.copy();
            distanceMeasurements.put(measurementKey, measurementCopy);

            boolean measurementsAvailable = mode.isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            stackPanel.addOrReplaceDistanceMeasurement(measurementCopy);

            for(DistanceMeasurementReceiver receiver : distanceMeasurementReceivers)
            {
                receiver.addOrReplaceDistanceMeasurement(measurementCopy);
            }


            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }

    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {        
        DistanceMeasurementDrawable measurementRemoved = distanceMeasurements.remove(measurement.getKey());

        if(measurementRemoved != null)
        {
            boolean measurementsAvailable = mode.isMeasurement() && !distanceMeasurements.isEmpty();
            updateMeasurementsAvailability(measurementsAvailable);

            stackPanel.removeDistanceMeasurement(measurement);

            for(DistanceMeasurementReceiver receiver : distanceMeasurementReceivers)
            {
                receiver.removeDistanceMeasurement(measurementRemoved);
            }
            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }       
    }

    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements()
    {
        return new LinkedHashMap<>(distanceMeasurements);
    }

    public void showDistanceMeasurements() 
    {
        distanceMeasurementsDialog.setVisible(true);
    }

    protected void updateMeasurementsAvailability(boolean available) 
    {
        if (showDistanceMeasurementsAction != null) {
            showDistanceMeasurementsAction.setEnabled(available);
        }
        stackPanel.setDistanceMeasurementsAvailable(available);
        boolean multipleMeasurementsAvailable = distanceMeasurements.size() > 1;

        boolean actionsBasicEnabled = (getChart() != null);
        boolean actionsMultipleMeasurementBasedEnabled = (multipleMeasurementsAvailable && actionsBasicEnabled);

        setMultipleMeasurementActionsEnabled(actionsMultipleMeasurementBasedEnabled);
    }

    protected void setMultipleMeasurementActionsEnabled(boolean enabled)
    {}

    @Override
    public void setROIs(Map<Object, ROIDrawable> rois) 
    {
        this.rois = new LinkedHashMap<>(rois);
        updateROIAvailability();

        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.setROIs(rois);
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.setROIs(rois);

        updateROIRange();
    }

    @Override
    public void addOrReplaceROI(ROIDrawable roi) 
    {
        Object roiKey = roi.getKey();
        ROIDrawable oldROI = rois.get(roiKey);

        boolean roiIsNew = !roi.equalsUpToStyle(oldROI);

        if(roiIsNew)
        {
            ROIDrawable roiCopy = roi.copy();
            rois.put(roiKey, roiCopy);

            updateROIAvailability();

            for(ROIReceiver roiReceiver : getAllROIReceivers())
            {
                roiReceiver.addOrReplaceROI(roiCopy);
            }

            ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
            model.addOrUpdateROI(roiCopy);

            updateROIRange();       
        }
    }

    @Override
    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.changeROILabel(roiKey, labelOld, labelNew);
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.replaceROILabel(roiKey, labelNew);
    }

    @Override
    public void removeROI(ROIDrawable roi) 
    {
        ROIDrawable removedROI = rois.remove(roi.getKey());
        if(removedROI != null)
        {
            updateROIAvailability();

            for(ROIReceiver roiReceiver : getAllROIReceivers())
            {
                roiReceiver.removeROI(roi);
            }

            ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
            model.removeROI(roi);

            updateROIRange();
        }
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
        Channel2DChart<?> chart = getSelectedChart();
        if(chart == null)
        {
            return;
        }    

        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());
        ModifyObjectsModel<ROIDrawable> model
        = new ModifyObjectsModel<>(roisSet,"ROI","ROI merging", 
                "Select which ROIs are to be merged", "ROIs to merge",true, true);


        if(roiMergeWizard == null)
        {
            roiMergeWizard = new ModifyObjectsWizard(viewDialog, "ROI merging");
        }

        boolean approved = roiMergeWizard.showDialog(model);

        if(approved)
        {
            boolean deleteOriginalROIs = model.isDeleteOriginalObjects();
            Collection<ROIDrawable> selectedROIs = model.getSelectedObjects();

            Integer mergedROIKey =  deleteOriginalROIs ? ROIUtilities.getUnionKey(selectedROIs) : getCurrentROIIndex();
            ROIDrawable merged = new GeneralUnionROI(selectedROIs, mergedROIKey, mergedROIKey.toString(), chart.getROIStyle());

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

    public void complementROIs()
    {
        V selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ComplementROIModel<ROIDrawable> model = new ComplementROIModel<>(roisSet, true, true);

        if(complementROIWizard == null)
        {
            complementROIWizard = new ComplementROIWizard(viewDialog, "ROI complement");
        }

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
                ROIDrawable complement = new DifferenceROI(datasetROI, subtractedROIs,
                        datasetROI.getKey(), datasetROI.getLabel(), selectedChart.getROIStyle());
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

        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ModifyObjectsModel<ROIDrawable> model
        = new ModifyObjectsModel<>(roisSet,"ROI","ROI convex hull", 
                "Select for which ROIs to create convex hulls", "ROIs for convex hulls",true, true);

        this.roiConvexHullWizard = (roiConvexHullWizard == null) ? new ModifyObjectsWizard(viewDialog, "ROI convex hulls") : roiConvexHullWizard;

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

        Set<ROIDrawable> roisSet = new LinkedHashSet<>(rois.values());

        ExportAnnotationModel<ROIDrawable> model = new ExportAnnotationModel<>(roisSet, "ROI", "roi", true, true);

        this.exportAnnotationWizard = (exportAnnotationWizard == null) ? new ExportAnnotationWizard(viewDialog, "Export ROIs to file") : exportAnnotationWizard;
        this.exportAnnotationWizard.showDialog(model);
    }

    public void subtractROIs()
    {
        Channel2DChart<?> chart = getSelectedChart();
        if(chart == null)
        {
            return;
        }    


        DifferenceROIModel<ROIDrawable> selectionModel = new DifferenceROIModel<>(ROIUtilities.findPossibleDiffrences(rois.values()), true, true);

        this.roiDifferenceWizard = (roiDifferenceWizard == null) ? roiDifferenceWizard: new DifferenceROIWizard(viewDialog, "ROI subtraction");

        boolean approved = roiDifferenceWizard.showDialog(selectionModel);

        if(approved)
        {
            ROIDrawable mainROI = selectionModel.getMainROI();
            Set<ROIDrawable> subtractedROIs = selectionModel.getSubtractedROIs();
            ROIDrawable difference = new DifferenceROI(mainROI, subtractedROIs,
                    mainROI.getKey(), mainROI.getLabel(), chart.getROIStyle());

            for(ROIDrawable roi : subtractedROIs)
            {
                removeROI(roi);
            }

            addOrReplaceROI(difference);
        }
    }

    private List<ROIReceiver> getAllROIReceivers()
    {
        List<ROIReceiver> allProfileReceivers = new ArrayList<>();

        allProfileReceivers.add(stackPanel);
        allProfileReceivers.addAll(roiReceivers);

        return allProfileReceivers;
    }

    protected void updateROIAvailability( ) 
    {        
        updateROIAvailabilityPrivate();
    }

    //we need a private method to call it from the constructor. We also need a method that can be overridden,because
    //we want to call it whenever new ROIs are added or deleted, and the required behavior may be different in subclasses
    //so that is why we have two methods that differs only in visibility
    private void updateROIAvailabilityPrivate()
    {
        boolean roisAvailable = !rois.isEmpty();
        boolean multipleROIsAvailable = rois.size() > 1;

        boolean actionsBasicEnabled = (stackPanel.getSelectedChart() != null);
        boolean actionsROIBasedEnabled = (roisAvailable && actionsBasicEnabled);
        boolean actionsMultipleROIBasedEnabled = (multipleROIsAvailable && actionsBasicEnabled);
        boolean actionsSubtractabilityROIBasedEnabled = !ROIUtilities.findPossibleDiffrences(rois.values()).isEmpty();

        roiMaskAction.setEnabled(actionsROIBasedEnabled);
        showROIShapeFactorsAction.setEnabled(actionsROIBasedEnabled);
        showROIStatisticsAction.setEnabled(actionsROIBasedEnabled);
        stackPanel.setROIBasedActionsEnabled(actionsROIBasedEnabled);
        complementROIsAction.setEnabled(actionsROIBasedEnabled);
        splitROIsAction.setEnabled(actionsROIBasedEnabled);
        convexHullROIsAction.setEnabled(actionsROIBasedEnabled);
        exportROIsAction.setEnabled(actionsROIBasedEnabled);
        rotateROIAction.setEnabled(actionsROIBasedEnabled);

        mergeROIsAction.setEnabled(actionsMultipleROIBasedEnabled);
        subtractROIsAction.setEnabled(actionsSubtractabilityROIBasedEnabled);
    }

    private void updateROIRange()
    {	
        GradientPaintReceiver receiver = stackPanel.getGradientPaintReceiver();			

        ROI roi = getROIUnion();
        receiver.setMaskedRegion(roi);				
    }

    @Override
    public ROI getROIUnion()
    {
        ROI roiUnion = ROIUtilities.composeROIs(new ArrayList<ROI>(rois.values()), "All");
        return roiUnion;
    }

    public void applyROIMask()
    {
        if(!rois.isEmpty())
        {					
            GradientPaintReceiver receiver = stackPanel.getGradientPaintReceiver();

            if (roiMaskEditor == null) 
            {
                roiMaskEditor = new ROIMaskEditor(viewDialog, receiver);
            } 
            else 
            {
                roiMaskEditor.setReceiver(receiver);
            }
            roiMaskEditor.setVisible(true);	
        }
    }

    @Override
    public void setProfilesAvailable(boolean b) 
    {
        stackPanel.setProfilesAvailable(b);
    }

    @Override
    public boolean areROIsAvailable()
    {
        boolean available = getMode().isROI() && !rois.isEmpty();
        return available;
    }

    @Override
    public void setROIsAvailable(boolean b) 
    {
        stackPanel.setROIBasedActionsEnabled(b);
    }

    @Override
    public void requestCursorChange(Cursor cursor) 
    {
        stackPanel.setCursor(cursor);
    }

    @Override
    public void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor)
    {
        V chart = stackPanel.getSelectedChart();
        boolean isVertical = (chart.getCustomizablePlot().getOrientation() == PlotOrientation.VERTICAL);
        Cursor cursor = isVertical ? verticalCursor : horizontalCursor;
        stackPanel.setCursor(cursor);
    }   

    @Override
    public void setProfileGeometryVisible(boolean visible) 
    {
    }

    @Override
    public void showROIShapeFactors() 
    {
        roiShapeFactorsDialog.setVisible(true);
    }

    private void lockAspectRatio(boolean lock)
    {
        stackPanel.lockAspectRatio(lock);
    }

    private void editChartProperties()
    {
        stackPanel.doEditChartProperties();
    }

    @Override
    public void editGradient()
    {
        if(stackPanel != null)
        {
            GradientPaintReceiver receiver = stackPanel.getGradientPaintReceiver();

            if(rangeGradientChooser == null)
            {
                rangeGradientChooser = new RangeGradientChooser(viewDialog, receiver, false);
                rangeGradientChooser.setVisible(true);
            }
            else
            {
                rangeGradientChooser.showDialog(receiver);
            }

        }
    }

    @Override
    public void editHistogramGradient()
    {
        if (stackPanel != null) 
        {
            GradientPaintReceiver receiver = stackPanel.getGradientPaintReceiver();

            if (rangeGradientHistogramChooser == null) 
            {
                rangeGradientHistogramChooser = new RangeHistogramView(viewDialog, receiver.getPaintedSample(), receiver, "Gradient range");
            }
            else 
            {
                rangeGradientHistogramChooser.setRangeModel(receiver);
            }
            rangeGradientHistogramChooser.setVisible(true);
        }
    }

    @Override
    public void showGradientChooser() 
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(viewDialog);
        }
        gradientSelectionDialog.showDialog(this);		
    }

    @Override
    public void showGradientChooser(ColorGradientReceiver gradientReceiver) 
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(viewDialog);
        }
        gradientSelectionDialog.showDialog(gradientReceiver);		
    }


    @Override
    public void showGradientChooser(List<ColorGradientReceiver> gradientReceivers)
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(viewDialog);
        }
        gradientSelectionDialog.showDialog(gradientReceivers);
    }	

    @Override
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey)
    {       
    }

    @Override
    public Window getPublicationSite() 
    {
        return viewDialog;
    }

    @Override
    public ColorGradient getColorGradient()
    {
        ColorGradient table = null;
        Channel2DChart<? extends Channel2DPlot> chart = stackPanel.getSelectedChart();
        ChannelRenderer renderer = chart.getCustomizablePlot().getRenderer();

        if(renderer instanceof ColorGradientReceiver)
        {
            ColorGradientReceiver paintReceiver = (ColorGradientReceiver)renderer;
            table = paintReceiver.getColorGradient();
        }
        return table;
    }

    @Override
    public void setColorGradient(ColorGradient table)
    {
        if(table != null)
        {
            Channel2DChart<? extends Channel2DPlot> chart = stackPanel.getSelectedChart();
            ChannelRenderer renderer = chart.getCustomizablePlot().getRenderer();

            if(renderer instanceof ColorGradientReceiver)
            {
                ColorGradientReceiver paintReceiver = (ColorGradientReceiver)renderer;
                paintReceiver.setColorGradient(table);
            }
        }		
    }

    public GradientPaintReceiver getGradientPaintReceiver()
    {
        GradientPaintReceiver receiver = null;
        Channel2DChart<? extends Channel2DPlot> chart = stackPanel.getSelectedChart();
        ChannelRenderer renderer = chart.getCustomizablePlot().getRenderer();

        if(renderer instanceof GradientPaintReceiver)
        {
            receiver = (GradientPaintReceiver)renderer;
        }
        return receiver;
    }

    protected void save()
    {
        try 
        {
            stackPanel.doSaveAs();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    protected void saveAsMovie()
    {
        aviSaveDialog.showDialog(stackPanel.getSelectedChart(), stackModel, stackPanel.getChartRenderingInfo().getChartArea(),stackPanel.getScreenDataArea());
    }

    protected void close()
    {
        viewDialog.dispose();
    }

    protected void print()
    {	
        stackPanel.createChartPrintJob();
    }


    private void handleChangeOfSelectedFrame(int frameIndex)
    {
        showFrame(frameIndex);
        checkIfUndoRedoEnabled();
        handleDataChange();
    }

    @Override
    public Rectangle2D getChartArea()
    {
        ChartRenderingInfo info = stackPanel.getChartRenderingInfo();
        Rectangle2D chartArea = info.getChartArea();
        return chartArea;
    }

    @Override
    public Rectangle2D getDataArea()
    {
        PlotRenderingInfo info = stackPanel.getChartRenderingInfo().getPlotInfo();
        Rectangle2D dataArea = info.getDataArea();
        return dataArea;
    }

    @Override
    public int getChartWidth() 
    {
        ChartRenderingInfo info = stackPanel.getChartRenderingInfo();
        Rectangle2D chartArea = info.getChartArea();
        int width = (int)Math.rint(chartArea.getWidth());
        return width;
    }

    @Override
    public int getChartHeight() 
    {
        ChartRenderingInfo info = stackPanel.getChartRenderingInfo();
        Rectangle2D chartArea = info.getChartArea();
        int height = (int)Math.rint(chartArea.getHeight());
        return height;	
    }

    @Override
    public int getDataWidth() 
    {
        PlotRenderingInfo info = stackPanel.getChartRenderingInfo().getPlotInfo();
        Rectangle2D dataArea = info.getDataArea();
        int width = (int)Math.rint(dataArea.getWidth());
        return width;	
    }

    @Override
    public int getDataHeight() 
    {
        PlotRenderingInfo info = stackPanel.getChartRenderingInfo().getPlotInfo();
        Rectangle2D dataArea = info.getDataArea();
        int height = (int)Math.rint(dataArea.getHeight());
        return height;			
    }

    @Override
    public List<String> getDefaultOutputNames(String key) 
    {
        return stackModel.getDefaultOutputNames();
    }

    @Override
    public List<File> getDefaultOutputLocations(String key) 
    {
        int frameCount = stackModel.getFrameCount();
        List<File> defaultOutputLocations = Collections.nCopies(frameCount, stackModel.getDefaultOutputDirectory());
        return defaultOutputLocations;
    }

    @Override
    public List<Channel2DChart<?>> getAllNonemptyCharts(String key) 
    {
        Channel2DChart<?> chart = stackPanel.getSelectedChart();
        List<Channel2DChart<?>> charts = new ArrayList<>();
        charts.add(chart);
        return charts;
    }

    @Override
    public void notifyAboutROISampleNeeded()
    {}

    @Override
    public void notifyAboutAspectRatioLock() 
    {
        Channel2DChart<?> chart = stackPanel.getSelectedChart();

        if (chart != null) 
        {           
            boolean aspectRatioLocked = chart.getUseFixedChartAreaSize();
            lockAspectRatioAction.putValue(Action.SELECTED_KEY, aspectRatioLocked);		
        }
    }

    public boolean isRoisAvailable()
    {
        boolean available = !rois.isEmpty();
        return available;
    }

    @Override
    public Map<Object, ROI> getDrawableROIs() 
    {
        Map<Object, ROI> roiMap = new LinkedHashMap<>();
        roiMap.putAll(rois);

        return roiMap;
    }

    @Override
    public Map<Object, ROI> getAllROIs()
    {
        Map<Object, ROI> rois = new LinkedHashMap<>();

        ROI roiUnion = getROIUnion();
        rois.put(roiUnion.getKey(), roiUnion);
        rois.putAll(getDrawableROIs());

        return rois;
    }

    @Override
    public void moveProfileKnob(Object profileKey, int markerIndex, double markerPositionNew) 
    {}

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
    public void addProfileKnob(Object profileKey, double knobPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeProfileKnob(Object profileKey, double markerPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProfileKnobPositions(Object resource, Object profileKey,
            List<Double> knobPositions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void requestNewDomainMarker(double knobPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMapMarker(MapMarker mapMarker) {
        // TODO Auto-generated method stub

    }


    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkers) {
        // TODO Auto-generated method stub

    }

    @Override
    public MultiMap<String, ? extends Channel2DChart<?>> getAllNonEmptyCharts() 
    {
        MultiMap<String, V> allNonEmptyCharts = new MultiMap<>();     
        List<V> nonemptyCharts = stackPanel.getNonEmptyCharts();       
        allNonEmptyCharts.putAll(stackModel.getStackType(), nonemptyCharts);

        return allNonEmptyCharts;
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
    public void useMouseInteractiveTool(MouseInteractiveTool tool)
    {
        MouseInteractiveTool oldTool = this.mouseInteractiveTool;
        this.mouseInteractiveTool = tool;

        if(oldTool != null)
        {
            oldTool.removeMouseToolListerner(mouseInteractiveToolListener);
        }

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

        if(oldTool != null)
        {
            oldTool.notifyOfToolModeLoss();
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

    public Set<MouseInputType> getMouseInputTypesForAccessoryMode(MouseInputMode mode)
    {
        Set<MouseInputType> mouseInputTypes = new LinkedHashSet<>();

        for(Entry<MouseInputType, MouseInputMode> entry : accessoryModes.entrySet())
        {
            if(Objects.equals(mode, entry.getValue()))
            {
                mouseInputTypes.add(entry.getKey());
            }
        }

        return mouseInputTypes;
    }

    public void setMouseInputsReservedForTools(Set<MouseInputType> types)
    {
        Set<MouseInputType> previouslyReservedTyps = getMouseInputTypesForAccessoryMode(MouseInputModeStandard.TOOL_MODE);

        for(MouseInputType mouseInputType : types)
        {
            setAccessoryMode(mouseInputType, MouseInputModeStandard.TOOL_MODE);
        }

        Set<MouseInputType> previouslyButNotNowReserved = new LinkedHashSet<>(previouslyReservedTyps);
        previouslyButNotNowReserved.removeAll(types);

        for(MouseInputType mouseInputType : previouslyButNotNowReserved)
        {
            setAccessoryMode(mouseInputType, null);
        }
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

    @Override
    public void itemMoved(Channel1D channel, int itemIndex, double[] newValue) 
    {}

    @Override
    public void channelTranslated(Channel1D channel) 
    {}

    @Override
    public boolean isValidValue(Channel1D channel, int itemIndex, double[] newValue)
    {
        return true;
    }

    @Override
    public Point2D correctPosition(Channel1D channel, int itemIndex, Point2D dataPoint)
    {
        return dataPoint;
    }

    @Override
    public void itemAdded(Channel1D channel, double[] itemNew)
    {}

    @Override
    public ChannelGroupTag getNextGroupMemberTag(Object groupKey)
    {
        return new ChannelGroupTag(groupKey, 0);
    }

    @Override
    public void channelAdded(Channel1D channel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelRemoved(Channel1D channel) {
        // TODO Auto-generated method stub

    }

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
        StackQuasiResource selectedResource = stackModel.getSelectedResource();
        String selectedType = stackModel.getSelectedType();

        UndoableCommand command = new UndoableTransformationCommand(this, selectedType, selectedResource, tr);
        command.execute();   

        pushCommand(selectedResource, selectedType, command);
    }

    public void transformAll(Channel2DDataTransformation tr)
    {
        List<? extends Channel2DResource> resorcesToProcess = stackModel.getResources();
        String selectedType = stackModel.getSelectedType();

        List<UndoableBasicCommand<Channel2DResource, Channel2D, ?, ?>> commands = new ArrayList<>();

        CommandIdentifier compundCommandId = resorcesToProcess.size() > 1 ? new CommandIdentifier() : null;

        for(Channel2DResource resource : resorcesToProcess)
        {
            UndoableTransformationCommand command =  new UndoableTransformationCommand(this, selectedType,  null, resource, tr, compundCommandId);
            commands.add(command);
        }

        ConcurrentTransformationTask<Channel2DResource,?> task = new ConcurrentTransformationTask<>(this, commands);
        task.execute();
    }

    @Override
    public void transform(Channel2DDataInROITransformation tr, ROIRelativePosition position) 
    {
        StackQuasiResource resource = stackModel.getSelectedResource();
        String type = stackModel.getSelectedType();
        ROI roi = getROIUnion();

        UndoableCommand command = new UndoableImageROICommand(this, type, null, resource, tr, position, roi);
        command.execute();

        pushCommand(resource, type, command);
    }

    private void checkIfUndoRedoEnabled()
    {
        boolean redoEnabled = stackModel.canRedoBeEnabled();
        boolean undoEnabled = stackModel.canUndoBeEnabled();

        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);

        checkIfUndoRedoAllEnabled();
    }

    private void checkIfUndoRedoAllEnabled()
    {
        boolean redoAllEnabled = stackModel.canRedoAllBeEnabled();
        boolean undoAllEnabled = stackModel.canUndoAllBeEnabled();

        undoAllAction.setEnabled(undoAllEnabled);
        redoAllAction.setEnabled(redoAllEnabled);
    }

    @Override
    public String getSelectedType()
    {
        return stackModel.getSelectedType();
    }

    @Override
    public Channel2DResource getSelectedResource()
    {
        return stackModel.getSelectedResource();
    }

    @Override
    public List<? extends Channel2DResource> getAllSelectedResources()
    {
        return stackModel.getAllSelectedResources();
    }

    @Override
    public List<? extends Channel2DResource> getResources()
    {
        return stackModel.getResources();
    }

    @Override
    public List<? extends Channel2DResource> getAdditionalResources() 
    {
        return stackModel.getAdditionalResources();
    }

    @Override
    public int getResourceCount() 
    {
        return stackModel.getResourceCount();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return stackModel.getSelectedResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        return stackModel.getSelectedResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return stackModel.getSelectedResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        return stackModel.getSelectedResourcesChannelIdentifiers(filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers() 
    {
        return stackModel.getAllResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type) 
    {
        return stackModel.getAllResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        return stackModel.getAllResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        return stackModel.getAllResourcesChannelIdentifiers(filter);
    }

    @Override
    public void addResourceSelectionListener(SelectionListener<? super Channel2DResource> listener)
    {
        stackModel.addResourceSelectionListener(listener);
    }

    @Override
    public void removeResourceSelectionListener(SelectionListener<? super Channel2DResource> listener) 
    {
        stackModel.removeResourceSelectionListener(listener);        
    }

    @Override
    public void addResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        stackModel.addResourceDataListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        stackModel.removeResourceDataListener(listener);
    }

    @Override
    public void addResourceTypeListener(ResourceTypeListener listener)
    {
        stackModel.addResourceTypeListener(listener);
    }

    @Override
    public void removeResourceTypeListener(ResourceTypeListener listener) 
    {
        stackModel.removeResourceTypeListener(listener);
    }

    @Override
    public PrefixedUnit getDataUnit() 
    {
        return stackModel.getSelectedResource().getSingleDataUnit(stackModel.getSelectedType());
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

    @Override
    public void handleChangeOfData(Map<String, Channel2D> channelsChanged, String type, Channel2DResource resource)
    {
        Channel2DChart<?> chart = getChart(resource, type);
        int resourceIndex = stackModel.getResourceIndex(resource);

        for (String key : channelsChanged.keySet()) 
        {
            chart.notifyOfDataChange(key);

        }
        frameSamplesDialog.refreshSamples(stackModel.getSamples(resourceIndex));

        handleDataChange();
    }

    private void handleDataChange()
    {       
        if(liveHistogramView != null && liveHistogramView.isVisible())
        {
            QuantitativeSample sample = stackModel.getCurrentSample();
            liveHistogramView.updateSampleModel(sample);
        }

        if(rangeGradientHistogramChooser != null && rangeGradientHistogramChooser.isVisible())
        {
            rangeGradientHistogramChooser.setRangeModel(stackPanel.getGradientPaintReceiver());
        }     
    }

    @Override
    public void refreshUndoRedoOperations()
    {
        checkIfUndoRedoEnabled();
    }

    @Override
    public Window getAssociatedWindow() {
        return viewDialog;
    }

    private class FixZeroAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public FixZeroAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,
                    InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Fix zero");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            transformAll(new FixMinimumOperation(0, false));
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
            transformAll(new FlipHorizontally());
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
            transformAll(new FlipVertically());
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
            transformAll(new Transpose());
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

            transformAll(tr);
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
            transformAll(tr);
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
            transformAll(tr);
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
            transformAll(tr);
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
            transformAll(tr);
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
            transformAll(new FlipZ());
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
            transformAll(new RotateClockwise());
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
            transformAll(new RotateCounterClockwise());
        }
    }


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
            StackQuasiResource resource = stackModel.getSelectedResource();
            if(resource != null)
            {
                resource.undo(stackModel.getSelectedType());
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
            stackModel.undoAll(SimpleStackView.this);
        }     
    }


    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            close();
        };
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            save();
        };
    }

    private class SaveAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAllAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save all frames");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setSaveDialogVisible(true);
        };
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            print();
        };
    }


    private class ShowStatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowStatisticsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/SigmaLarger.png"));

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Statistics");

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
            putValue(NAME,"Statistics");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showStatistics();
        }
    }   

    private class ShowHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowHistogramsAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Frame histograms");
            putValue(SELECTED_KEY, false);

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);

            setHistogramsVisible(visible);
        }
    }

    private class ShowLiveHistogramAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowLiveHistogramAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Live histogram");
            putValue(SELECTED_KEY, false);

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);

            setLiveHistogramVisible(visible);          
            putValue(SELECTED_KEY, liveHistogramView.isVisible());
        }
    }
    private class DrawHistogramAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public DrawHistogramAction()
        {           
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));

            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
            putValue(NAME,"Draw frame histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            drawHistograms();
        }
    }   


    private class ShowRawDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showRawData();
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

            putValue(NAME,"Lock aspect");

            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean locked = (boolean) getValue(SELECTED_KEY);
            lockAspectRatio(locked);
        }
    }

    private class EditChartAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public EditChartAction()
        {           
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Live chart style");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            editChartProperties();
        }
    }

    private class EditGradientAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public EditGradientAction()
        {           
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Edit color gradient");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            editGradient();
        }
    }

    private class EditHistogramGradientAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public EditHistogramGradientAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Gradient histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            editHistogramGradient();
        }
    }


    private class NextFrameAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NextFrameAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/NextFrame2.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK));

            putValue(NAME,"Next frame");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            stackModel.moveToNextFrame();
        }
    }

    private class PreviousFrameAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PreviousFrameAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/PreviousFrame2.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK));


            putValue(NAME,"Previous frame");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            stackModel.moveToPreviousFrame();
        }
    }

    private class FirstFrameAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FirstFrameAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/FirstFrame2.png"));
            putValue(LARGE_ICON_KEY, icon);

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));

            putValue(NAME,"First frame");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            stackModel.moveToFirstFrame();
        }
    }

    private class LastFrameAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public LastFrameAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/LastFrame2.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));

            putValue(NAME,"Last frame");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            stackModel.moveToLastFrame();
        }
    }

    private class PlayForwardAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PlayForwardAction()
        {

            putValue(NAME,"Forward");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);

            setMoviePlayedForward(selected);
        }
    }

    private class StopMovieAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public StopMovieAction()
        {           
            putValue(NAME,"Stop movie");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            playMovieAction.putValue(Action.SELECTED_KEY, false);
        }
    }

    private class PlayMovieAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        private final ImageIcon playForwardIcon;
        private final ImageIcon playBackwardIcon;
        private final ImageIcon stopIcon;

        private boolean playForward = true;

        public PlayMovieAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            playForwardIcon = new ImageIcon(toolkit.getImage("Resources/PlayMovie2.png"));
            playBackwardIcon = new ImageIcon(toolkit.getImage("Resources/PlayReverseMovie2.png"));
            stopIcon = new ImageIcon(toolkit.getImage("Resources/StopMovie2.png"));

            putValue(LARGE_ICON_KEY, playForwardIcon);
            putValue(NAME,"Play");          
            putValue(SELECTED_KEY, false);
        }

        public void setPlayForward(boolean playForward)
        {
            this.playForward = playForward;
            ImageIcon icon = getCurrentIcon();
            putValue(LARGE_ICON_KEY, icon);
        }

        private ImageIcon getCurrentIcon()
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            ImageIcon newIcon;

            if(selected)
            {
                newIcon = stopIcon;
            }
            else
            {
                newIcon = playForward ? playForwardIcon : playBackwardIcon;
            }
            return newIcon;
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);

            if(selected)
            {
                stackModel.run();
            }
            else
            {
                stackModel.stop();
            }
            ImageIcon newIcon = getCurrentIcon();
            putValue(LARGE_ICON_KEY, newIcon);
        }
    }

    private class EditMovieParametersAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public EditMovieParametersAction()
        {           
            putValue(NAME,"Edit movie parametes");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            editMovieParameters();
        }
    }

    private class SaveAsMovieAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAsMovieAction()
        {           
            putValue(NAME,"Save as movie");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            saveAsMovie();
        }
    }

    //copied from DensityDialog
    private class ModifyDistanceStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyDistanceStyleAction()
        {
            putValue(NAME, "Measurements style");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            measurementEditor.setVisible(true);
        }
    }

    //copied from DensityDialog
    private class ShowDistanceMeasurementsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowDistanceMeasurementsAction() 
        {           
            putValue(NAME, "Show measurements");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showDistanceMeasurements();
        }
    }

    private class MeasureDistancesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MeasureDistancesAction() 
        {
            putValue(NAME, "Line measurement");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }
    //copied
    private class ShowShapeFactorsForRois extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowShapeFactorsForRois() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Shape factors for ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            showROIShapeFactors();
        }
    }

    //copied
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

    //copied
    private class ROIRectangularAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIRectangularAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/rectangleRoi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Rectangular ROI");
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

    //copied
    private class ROIElipticAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIElipticAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/elipticRoi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Eliptic ROI");
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

    //copied
    private class ROIFreeAction extends AbstractAction {
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

    //copied from DensityDialog
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


    //copied from DensityDialog
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

    //copied from DensityDialog
    private class SubtractROIsAction extends AbstractAction 
    {
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

    //copied
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
            int op = roiImportChooser.showOpenDialog(viewDialog);
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
                    JOptionPane.showMessageDialog(viewDialog, "Errors occured during importing ROIs", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                    return;
                }

                V chart = getSelectedChart();

                if(chart == null)
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

    //copied from DensityDialog
    private class ROIWandAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ROIWandAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/roiWand.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Wand ROI");
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

    //copied from DensityDialog
    private class RotateROIAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RotateROIAction()
        {
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

    private class ModifyROIStyleAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ModifyROIStyleAction() {
            putValue(NAME, "ROI style");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            roiEditor.setVisible(true);
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
            StackQuasiResource resource = stackModel.getSelectedResource();
            if(resource != null)
            {
                resource.redo(stackModel.getSelectedType());
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
            stackModel.redoAll(SimpleStackView.this);
        }
    }

    private class ShowROIStatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowROIStatisticsAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Statistics ROIs");
            putValue(SHORT_DESCRIPTION, "Statistics ROIs");

        }

        @Override
        public void actionPerformed(ActionEvent e) 
        {

            MultipleSelectionWizardPageModel<SampleStatistics> selectionModel = new BasicMultipleSelectionWizardPageModel<>(Arrays.asList(SampleStatistics.values()), "Statistics to calculate", 
                    "ROI statistics", "Select which statistics to calculate for ROIs", true, true);

            roiStatisticsWizard = (roiStatisticsWizard == null) ?  new MultipleSelectionWizard<>(viewDialog, "ROI statistics") : roiStatisticsWizard;
            boolean approved = roiStatisticsWizard.showDialog(selectionModel);
            if (approved) 
            {
                Set<SampleStatistics> selectedSampleStatistics = selectionModel.getSelectedKeys();

                if(selectedSampleStatistics.isEmpty())
                {
                    return;
                }

                MetaMap<SampleStatistics, IdentityTag /*column - i.e. ROI*/, UnitArray1DExpression> calculatedStatistics = getStackModel().getROIStatistics(rois.values(), selectedSampleStatistics);

                DataAxis1D stackAxis = getStackModel().getStackAxis();
                QuantityArray1DExpression stackLevels = new QuantityArray1DExpression(stackAxis.getNodes(), stackAxis.getQuantity());

                Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
                for (SampleStatistics sampleStatistics : selectedSampleStatistics) 
                {
                    NumericalTableModel model = new StackROITableModel(calculatedStatistics.get(sampleStatistics), stackLevels, stackModel.getDefaultOutputDirectory());
                    StandardNumericalTable table = new StandardNumericalTable(model, true, true);
                    tables.put(sampleStatistics.getPrettyName(), table);
                }

                MultipleNumericalTableView dialog = new MultipleNumericalTableView(viewDialog, tables, "ROI statistics", true);
                dialog.setVisible(true);

                /*
                 *  R resource = getSelectedResource();

        Map<String, StatisticsTable> tablesImages = getROIStatisticsTables(rois);

        Map<String, StatisticsTable> allTables = new LinkedHashMap<>();
        allTables.putAll(tablesImages);

        String title = "ROI statistics for " + resource.getShortName();
        showStatistics(allTables, title);
                 */
            }
        }

    }

    private class StackModelPropertyListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            String property = evt.getPropertyName();

            if(FRAME_INDEX.equals(property))
            {
                int frameIndex = (int)evt.getNewValue();

                handleChangeOfSelectedFrame(frameIndex);
            }
            else if(PLAYED_FORWARD.equals(property))
            {
                boolean playedForward = (boolean)evt.getNewValue();
                playMovieAction.setPlayForward(playedForward);
            }      
            else if(STACK_EMPTY.equals(property))
            {
                fireDataAvailabilityChanged(!(Boolean)evt.getNewValue());
            }
        }   
    }


    private class CustomMouseInteractiveToolListener implements MouseInteractiveToolListener
    {
        @Override
        public void toolToRedraw() 
        {
            if(stackPanel != null)
            {
                stackPanel.chartChanged();
            }
        }       
    }


    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }
}
