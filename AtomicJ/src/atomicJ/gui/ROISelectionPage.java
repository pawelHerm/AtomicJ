
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;

import org.jfree.chart.plot.PlotOrientation;
import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.gui.MouseInteractiveTool.MouseInteractiveToolListener;
import atomicJ.gui.curveProcessing.ProcessingModelInterface;
import atomicJ.gui.editors.ROIMaskEditor;
import atomicJ.gui.histogram.HistogramWizard;
import atomicJ.gui.histogram.HistogramWizardModel;
import atomicJ.gui.histogram.HistogramWizardModelDoubleSelection;
import atomicJ.gui.histogram.HistogramWizardModelSingleSelection;
import atomicJ.gui.histogram.TransformableHistogramView;
import atomicJ.gui.imageProcessing.UndoableImageROICommand;
import atomicJ.gui.imageProcessing.UndoableTransformationCommand;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.imageProcessingActions.AddPlaneAction;
import atomicJ.gui.imageProcessingActions.LineFitCorrectionAction;
import atomicJ.gui.imageProcessingActions.LineMatchingCorrectionAction;
import atomicJ.gui.imageProcessingActions.ReplaceDataAction;
import atomicJ.gui.imageProcessingActions.SubtractPolynomialBackgroundAction;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIEditor;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIShapeFactorsTableModel;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.rois.WandContourTracer;
import atomicJ.gui.rois.WandROIDialog;
import atomicJ.gui.rois.WandROIModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.SampleSelectionModel;
import atomicJ.gui.statistics.StatisticsView;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.gui.statistics.UpdateableStatisticsView;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.imageProcessing.PolynomialFitCorrection;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.Channel2DResourceView;
import atomicJ.sources.Channel2DSource;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public class ROISelectionPage<R extends Channel2DResource, V extends Channel2DChart<?>, E extends Channel2DPanel<V>>
extends AbstractWizardPage implements WizardPage,
Channel2DSupervisor, Channel2DResourceView, PropertyChangeListener, ColorGradientReceiver, RawDataDestination
{
    private final Preferences pref = Preferences.userNodeForPackage(ROISelectionPage.class).node(ROISelectionPage.class.getName());

    private static final String IDENTIFIER = "ROISelection";

    private final JLabel labelPatchIndex = new JLabel();

    private final CorectForROIPlaneAction subtractROIFitAction = new CorectForROIPlaneAction();
    private final ShowStatisticsForRoisAction showStatisticsForRoisAction = new ShowStatisticsForRoisAction();
    private final ShowShapeFactorsForRois showROIShapeFactorsAction = new ShowShapeFactorsForRois();

    private final ModifyROIStyleAction modifyROIStyleAction = new ModifyROIStyleAction();

    private final ROIHoleAction roiHoleAction = new ROIHoleAction();
    private final ROIPolygonAction roiPolygonalAction = new ROIPolygonAction();
    private final ROIRectangularAction roiRectangularAction = new ROIRectangularAction();
    private final ROIEllipticAction roiElipticAction = new ROIEllipticAction();
    private final ROIFreeAction roiFreeAction = new ROIFreeAction();
    private final ROIWandAction roiWandAction = new ROIWandAction();
    private final RotateROIAction rotateROIAction = new RotateROIAction();

    private final UndoAction undoAction = new UndoAction();
    private final RedoAction redoAction = new RedoAction();

    private final Window parent;
    private final TransformableHistogramView histogramView;

    private final MultipleSelectionWizard<String> rawDataWizard;

    private HistogramWizard histogramsWizard;

    private final UpdateableStatisticsView roiSamplesDialog;
    private final UpdateableStatisticsView channelSamplesDialog;

    private final FlexibleNumericalTableView roiShapeFactorsDialog;

    private final ROIEditor roiEditor;

    private GradientSelectionDialog gradientSelectionDialog;
    private RangeGradientChooser rangeGradientChooser;
    private RangeHistogramView rangeGradientHistogramChooser;
    private ROIMaskEditor roiMaskEditor;

    private final WandROIDialog wandROIDialog;
    private final WandROIModel wandROIModel = new WandROIModel(this);


    private MouseInputMode mode = MouseInputModeStandard.FREE_HAND_ROI;
    private Map<MouseInputType, MouseInputMode> accessoryModes = new HashMap<>();

    private final Channel2DPanel<V> chartPanel = new Channel2DPanel<>(true);

    private ROISelectionPageModel<R> selectionModel;
    private final Component controls;

    private boolean statisticsForROIsUpdated;
    private boolean changesToROIUnionDeferred;

    private ROIStyle currentRoiStyle;
    private final List<ROIStyle> roiStyles = new ArrayList<>();
    private final Set<ROIReceiver> roiReceivers = new LinkedHashSet<>();

    private MouseInteractiveTool mouseInteractiveTool = null;
    private final CustomMouseInteractiveToolListener mouseInteractiveToolListener = new CustomMouseInteractiveToolListener();

    private final JPanel viewPanel = new JPanel();

    public ROISelectionPage(Window parent, ROISelectionPageModel<R> selectionModel) 
    {        
        this.selectionModel = selectionModel;
        this.selectionModel.addPropertyChangeListener(this);

        this.histogramView = new TransformableHistogramView(parent, "Image histograms");

        this.rawDataWizard = new MultipleSelectionWizard<>(parent, "Raw data assistant");

        this.roiSamplesDialog = new UpdateableStatisticsView(parent, "Statistics for ROIs", Preferences.userRoot().node(getClass().getName()).node("ROISampleStatistics"),true);
        this.channelSamplesDialog = new UpdateableStatisticsView(parent, "Statistics",Preferences.userRoot().node(getClass().getName()).node("ChannelSampleStatistics"),true);

        this.roiShapeFactorsDialog = new FlexibleNumericalTableView(parent, new StandardNumericalTable(new ROIShapeFactorsTableModel(Units.MICRO_METER_UNIT), true, true), "ROI shape factors");
        this.roiEditor = new ROIEditor(parent, false);
        this.wandROIDialog = new WandROIDialog(parent, "Wand ROI", false);

        this.roiReceivers.add(wandROIModel);

        currentRoiStyle = new ROIStyle(pref.node(Integer.toString(0)), DefaultColorSupplier.getPaint(0));
        this.roiStyles.add(currentRoiStyle);

        roiEditor.setModel(currentRoiStyle);

        List<ROIStyle> boundedStyles = new ArrayList<>(roiStyles);
        boundedStyles.remove(currentRoiStyle);        
        roiEditor.setBoundedModels(boundedStyles);

        viewPanel.setLayout(new BorderLayout());

        this.parent = parent;

        this.chartPanel.addPropertyChangeListener(this);
        this.chartPanel.setDensitySupervisor(this);
        this.chartPanel.registerPopupActions("Correct", getDataCorrectionActions());    
        this.chartPanel.setPreferredSize(new Dimension(420, 400));
        viewPanel.add(chartPanel, BorderLayout.CENTER);

        this.controls = buildControlPanel();

        boolean histogramsAvailable = !histogramView.isEmpty();
        setHistogramsAvailable(histogramsAvailable);

        controlForResourceEmptinessPrivate(isEmpty());   
        setConsistentWithMode(this.mode, this.mode);

        Border border = BorderFactory.createRaisedBevelBorder();
        viewPanel.setBorder(border);

        initInputAndActionMaps();
        initViewListener();
    }

    private void initViewListener()
    {
        histogramView.addDataViewListener(new DataViewAdapter() {
            @Override
            public void dataAvailabilityChanged(boolean availableNew)
            {
                setHistogramsAvailable(availableNew);
            }
        });
    }

    private List<Action> getDataCorrectionActions()
    {
        List<Action> actions = new ArrayList<>();

        actions.add(new AddPlaneAction(this));
        actions.add(new ReplaceDataAction(this));
        actions.add(new LineMatchingCorrectionAction(this));
        actions.add(new LineFitCorrectionAction(this));
        actions.add(new SubtractPolynomialBackgroundAction(this));

        return actions;
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = viewPanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW );                
        inputMap.put((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY), undoAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY), redoAction.getValue(Action.NAME));      

        ActionMap actionMap = viewPanel.getActionMap();
        actionMap.put(undoAction.getValue(Action.NAME), undoAction); 
        actionMap.put(redoAction.getValue(Action.NAME), redoAction);    
    }

    @Override
    public Window getAssociatedWindow()
    {
        return parent;
    }

    @Override
    public void pushCommand(Channel2DResource resource, String type, UndoableCommand command)
    {
        resource.pushCommand(type, command);

        checkIfUndoRedoEnabled();
    }

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

    public void setModel(ROISelectionPageModel<R> modelNew)
    {
        ROISelectionPageModel<R> modelOld = this.selectionModel;
        R resourceOld = null;

        if(modelOld != null)
        {
            resourceOld = modelOld.getDensityResource();
            modelOld.removePropertyChangeListener(this);
        }

        this.selectionModel = modelNew;
        modelNew.addPropertyChangeListener(this);

        pullModelProperties();

        R resourceNew = this.selectionModel.getDensityResource();
        selectResource(resourceOld, resourceNew);

        if(roiStyles.isEmpty())
        {
            currentRoiStyle = new ROIStyle(pref.node(Integer.toString(0)), DefaultColorSupplier.getPaint(0));
            this.roiStyles.add(currentRoiStyle);
        }
        else
        {
            currentRoiStyle = roiStyles.get(0);
        }

        roiEditor.setModel(currentRoiStyle);

        List<ROIStyle> boundedStyles = new ArrayList<>(roiStyles);
        boundedStyles.remove(currentRoiStyle);        
        roiEditor.setBoundedModels(boundedStyles);    
    }

    private void pullModelProperties()
    {
        int index = selectionModel.getCurrentBatchIndex();
        labelPatchIndex.setText(Integer.toString(index + 1));
    }

    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel(); 

        boolean labelsVisible = !selectionModel.isRestricted();

        JLabel labelBatch = new JLabel("Patch no ");

        labelPatchIndex.setFont(labelPatchIndex.getFont().deriveFont(Font.BOLD));

        labelPatchIndex.setVisible(labelsVisible);
        labelBatch.setVisible(labelsVisible);

        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setHonorsVisibility(true);

        JToggleButton buttonPolygonalRoi = new JToggleButton(roiPolygonalAction);       
        JToggleButton buttonRectangularRoi = new JToggleButton(roiRectangularAction);     
        JToggleButton buttonElipticRoi = new JToggleButton(roiElipticAction);      
        JToggleButton buttonFreeRoi = new JToggleButton(roiFreeAction);
        JToggleButton buttonWandRoi = new JToggleButton(roiWandAction);

        JToggleButton buttonRoiHole = new JToggleButton(roiHoleAction);
        JToggleButton buttonRotateROI = new JToggleButton(rotateROIAction);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup().addComponent(labelBatch).addComponent(labelPatchIndex))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonPolygonalRoi).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonRectangularRoi).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonElipticRoi).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFreeRoi).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonWandRoi).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonRoiHole).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonRotateROI)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelBatch).addComponent(labelPatchIndex))
                        .addComponent(buttonPolygonalRoi)
                        .addComponent(buttonRectangularRoi)
                        .addComponent(buttonElipticRoi)
                        .addComponent(buttonFreeRoi)
                        .addComponent(buttonWandRoi)
                        .addComponent(buttonRoiHole)
                        .addComponent(buttonRotateROI)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.linkSize(buttonPolygonalRoi,buttonRectangularRoi, buttonElipticRoi, buttonFreeRoi, buttonWandRoi, buttonRoiHole);

        return panelControl;
    }

    private boolean isEmpty()
    {
        boolean isEmpty = (selectionModel.getDensityResource() == null);
        return isEmpty;
    }

    @Override
    public R getSelectedResource()
    {
        R selectedResource = (selectionModel != null) ? selectionModel.getDensityResource() : null;

        return selectedResource;
    }

    @Override
    public List<R> getAllSelectedResources()
    {
        R selectedResource = getSelectedResource();

        List<R> resources = (selectedResource != null) ? Collections.<R>singletonList(selectedResource) : Collections.<R>emptyList();

        return resources;
    }

    @Override
    public String getSelectedType()
    {
        return selectionModel.getType();
    }

    @Override
    public MouseInputMode getMode()
    {
        return mode;
    }

    @Override
    public void setMode(MouseInputMode modeNew)
    {        
        setMode(modeNew, true);
    }

    public void setMode(MouseInputMode modeNew, boolean clearAccessoryModels)
    {        
        if(modeNew.isROI() || MouseInputModeStandard.NORMAL.equals(modeNew))
        {
            MouseInputMode modeOld = this.mode;
            this.mode = modeNew;
            setConsistentWithMode(modeOld, modeNew);

            if(clearAccessoryModels)
            {
                clearAccessoryModels();
            }
        }
    }

    protected void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew) 
    {
        chartPanel.setMode(modeNew);

        boolean isROI = modeNew.isROI();

        boolean isPolygonROI = MouseInputModeStandard.POLYGON_ROI.equals(modeNew);
        boolean isRectangularROI = MouseInputModeStandard.RECTANGULAR_ROI.equals(modeNew);
        boolean isElipticROI = MouseInputModeStandard.ELIPTIC_ROI.equals(modeNew);
        boolean isFreeHandROI = MouseInputModeStandard.FREE_HAND_ROI.equals(modeNew);
        boolean isWandROI = MouseInputModeStandard.WAND_ROI.equals(modeNew);

        roiHoleAction.setEnabled(isROI);

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

    protected void setConsistentWithAccessoryMode(MouseInputType inputType, MouseInputMode modeOld, MouseInputMode modeNew)
    {
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

        chartPanel.setAccessoryMode(inputType, modeNew);
    }

    private void showWandROIDialog()
    {
        PrefixedUnit axisUnitNew = chartPanel.getZAxisUnit();
        wandROIModel.setUnitDifference(axisUnitNew);
        wandROIDialog.showDialog(wandROIModel);
    }

    @Override
    public boolean areHistogramsAvaialable()
    {
        boolean available = !histogramView.isEmpty();
        return available;
    }

    private void setHistogramsAvailable(boolean available)
    {
        chartPanel.setHistogramsAvialable(available);       
    }

    public void selectResource(R resourceOld, R resourceNew) 
    { 
        if(!Objects.equals(resourceOld, resourceNew))
        {
            Container parent = viewPanel.getParent();
            if(parent != null)
            {
                parent.validate();                  
            }
            handleChangeOfSelectedResource(resourceOld, resourceNew);   
        }   
    }

    public void handleChangeOfSelectedResource(R resourceOld, R resourceNew) 
    {
        if (resourceNew != null)
        {
            Channel2D channel = resourceNew.getChannels(getSelectedType()).values().iterator().next();

            List<Channel2DSource<?>> densitySources = new ArrayList<>(resourceNew.getChannel2DSources());
            Channel2DChart<?> chart = ChannelSourceVisualization.getChart(densitySources.get(0), channel);
            chartPanel.setSelectedChartAndClearOld((V) chart);

            File defaultOutputFile = resourceNew.getDefaultOutputLocation();
            PrefixedUnit dataUnit = chart.getDomainDataUnit();
            PrefixedUnit displayedUnit = chart.getDomainPreferredUnit();

            Map<Object, ROIDrawable> rois = resourceNew.getROIs();

            updateROIAvailability();

            ROIShapeFactorsTableModel roiGeometryModel = new ROIShapeFactorsTableModel(defaultOutputFile, dataUnit, displayedUnit);
            roiGeometryModel.addROIs(rois);
            MinimalNumericalTable table = roiShapeFactorsDialog.getTable();
            table.setModel(roiGeometryModel);

            //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'


            Map<String, PrefixedUnit> identifierUnitMap = resourceNew.getIdentifierUnitMap();
            Map<String, Map<Object, QuantitativeSample>> samplesForROIs = resourceNew.getSamplesForROIs(false);

            roiSamplesDialog.resetDialog(identifierUnitMap, defaultOutputFile);
            roiSamplesDialog.setSamples(samplesForROIs);

            Map<String, Map<Object, QuantitativeSample>> samplesForChannels = resourceNew
                    .getSamples(false);

            channelSamplesDialog.resetDialog(identifierUnitMap, defaultOutputFile);
            channelSamplesDialog.setSamples(samplesForChannels);

            updateRangeGradientChoser();
            updateRangeHistogramGradientChooser();
            updateROIMaskEditor();
            updateBuiltInGradientsSelector();
        }

        controlForResourceEmptinessPrivate(isEmpty());
    }

    public void applyROIMask()
    {
        if (!getDrawableROIs().isEmpty() && chartPanel != null) 
        {
            GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();

            if (roiMaskEditor == null) 
            {
                roiMaskEditor = new ROIMaskEditor(parent, receiver);
            } 
            else 
            {
                roiMaskEditor.setReceiver(receiver);
            }
            roiMaskEditor.setVisible(true);
        }
    }


    private void updateBuiltInGradientsSelector()
    {
        if(gradientSelectionDialog != null && gradientSelectionDialog.isVisible())
        {
            if (chartPanel != null) 
            {
                GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();
                gradientSelectionDialog.setReceiver(receiver);
            }
        }
    }

    private void updateRangeGradientChoser()
    {
        if(rangeGradientChooser != null && rangeGradientChooser.isShowing())
        {
            if (chartPanel != null) 
            {
                GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();
                rangeGradientChooser.setReceiver(receiver);
            }
        }
    }

    private void updateRangeHistogramGradientChooser()
    {
        if(rangeGradientHistogramChooser != null && rangeGradientHistogramChooser.isVisible())
        {
            if (chartPanel != null) 
            {
                GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();
                rangeGradientHistogramChooser.setRangeModel(receiver);
            }
        }
    }

    private void updateROIMaskEditor()
    {
        if(roiMaskEditor != null && roiMaskEditor.isShowing())
        {
            if (chartPanel != null) 
            {
                GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();
                roiMaskEditor.setReceiver(receiver);
            }
        }
    }

    @Override
    public void notifyAboutROISampleNeeded()
    {}

    @Override
    public void notifyAboutAspectRatioLock()
    {}

    private void setDrawROIHoles(boolean roiHoleMode)
    {
        chartPanel.setROIHoleMode(roiHoleMode);
    }

    protected void updateROIAvailability() 
    {
        boolean available = !getDrawableROIs().isEmpty();      

        setSingleROIBasedActionsEnabled(available);

        chartPanel.setROIBasedActionsEnabled(available);     
    }


    private void controlForResourceEmptinessPrivate(boolean empty) 
    {
        boolean enabled = !empty;
        enableStandByActionsDensityDialog(enabled);
    }

    public void controlForSelectedChartEmptiness(boolean empty) {

        boolean enabled = !empty;
        enableStandByActionsDensityDialog(enabled);
    }

    protected void setSingleROIBasedActionsEnabled(boolean enabled)
    {      
        showROIShapeFactorsAction.setEnabled(enabled);
        showStatisticsForRoisAction.setEnabled(enabled);
        subtractROIFitAction.setEnabled(enabled);
        rotateROIAction.setEnabled(enabled);
    }

    //standBy actions are the ones that can always be fired, provided that dialog is non-empty
    private void enableStandByActionsDensityDialog(boolean enabled) 
    {
        roiHoleAction.setEnabled(enabled);
        roiPolygonalAction.setEnabled(enabled);
        roiRectangularAction.setEnabled(enabled);
        roiElipticAction.setEnabled(enabled);
        roiFreeAction.setEnabled(enabled);
        roiWandAction.setEnabled(enabled);
        modifyROIStyleAction.setEnabled(enabled);
    }

    @Override
    public Window getPublicationSite() {
        return parent;
    }

    @Override
    public void drawHistograms() 
    {
        R resource = getSelectedResource();
        List<SampleCollection> samples = resource.getSampleCollection(false);

        HistogramWizardModel model = new HistogramWizardModelSingleSelection(histogramView, samples, false);
        drawHistograms(model);
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
        drawHistograms(model);
    }

    @Override
    public void drawROIHistograms() 
    {
        R resource = getSelectedResource();

        List<SampleCollection> samples = resource.getROISampleCollections2(false);

        HistogramWizardModel model = new HistogramWizardModelDoubleSelection(histogramView, samples, true);
        drawHistograms(model);
    }

    public void drawHistograms(HistogramWizardModel model) 
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
        Channel2DResource resource = getSelectedResource();
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
        roiShapeFactorsDialog.setVisible(true);
    }

    @Override
    public void setProfileGeometryVisible(boolean visible) 
    {
    }


    protected List<SampleCollection> getROIUnionSampleCollections()
    {
        R selectedResource = getSelectedResource();
        Map<String, QuantitativeSample> unionROISamples = selectedResource.getROIUnionSamples();

        SampleCollection collection = new StandardSampleCollection(unionROISamples, selectedResource.getShortName(), selectedResource.getShortName(), selectedResource.getDefaultOutputLocation());
        List<SampleCollection> sampleCollections = Collections.singletonList(collection);

        return sampleCollections;	
    }

    protected Map<String, StatisticsTable> getStatisticsTables() 
    {
        R resource = getSelectedResource();
        List<SampleCollection> sampleCollections = resource.getSampleCollection(false);
        return StatisticsTable.getStatisticsTables(sampleCollections, "");
    }

    protected Map<String, StatisticsTable> getStatisticsTablesForRois(Map<Object, ROI> rois) 
    {
        R resource = getSelectedResource();
        List<SampleCollection> sampleCollections = resource.getSampleCollection(rois, false);

        return StatisticsTable.getStatisticsTables(sampleCollections, " ROI");
    }

    @Override
    public void showROIStatistics(Map<Object, ROI> rois) 
    {
        R resource = getSelectedResource();

        Map<String, StatisticsTable> tablesImages = getStatisticsTablesForRois(rois);

        Map<String, StatisticsTable> allTables = new LinkedHashMap<>();
        allTables.putAll(tablesImages);

        String title = "ROI statistics for " + resource.getShortName();
        showStatistics(allTables, title);
    }

    @Override
    public void showROIStatistics() 
    {
        if(!statisticsForROIsUpdated)
        {
            Map<String, Map<Object, QuantitativeSample>> samples = getSelectedResource().getSamplesForROIs(false);

            roiSamplesDialog.refreshSamples(samples);
        }
        roiSamplesDialog.setVisible(true);
    }

    @Override
    public void showStatistics()
    {
        channelSamplesDialog.setVisible(true);
    }

    private void showStatistics(Map<String, StatisticsTable> tables, String title) 
    {
        StatisticsView dialog = new StatisticsView(parent, tables, title, true);
        dialog.setVisible(true);
    }

    @Override
    public WandContourTracer getWandTracer()
    {
        return wandROIModel.getContourTracer();
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
        V chart = chartPanel.getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getZDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = chartPanel.getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDataUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = chartPanel.getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDataUnit();
        }

        return axisUnit;
    }


    private PrefixedUnit getYAxisDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = chartPanel.getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getRangeDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getYAxisDataUnit()
    {
        PrefixedUnit axisUnit = null;
        V chart = chartPanel.getSelectedChart();

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
        List<PrefixedUnit> domainUnitManagers = new ArrayList<>();

        domainUnitManagers.add(getXAxisDisplayedUnit());
        domainUnitManagers.add(getYAxisDisplayedUnit());

        return domainUnitManagers;
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
    public void addProfileKnob(Object profileKey, double knobPositionNew)
    {}

    @Override
    public void moveProfileKnob(Object profileKey, int knobIndex, double knobPositionNew)
    {}

    @Override
    public void removeProfileKnob(Object profileKey, double knobPosition)
    {}

    @Override
    public void setProfileKnobPositions(Object resource, Object profileKey, List<Double> knobPositions)
    {	

    }

    @Override
    public void addOrReplaceProfile(Profile profile) 
    {

    }

    @Override
    public void removeProfile(Profile profile) 
    {

    }

    @Override
    public void setProfiles(Map<Object, Profile> profiles) 
    {

    }

    @Override
    public void setProfileStyleEditorVisible(boolean visible) 
    {
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {

    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {        

    }

    @Override
    public void addOrReplaceROI(ROIDrawable roi) 
    {	    
        Channel2DResource resource = getSelectedResource();

        String labelNew = selectionModel.getROILabel(roi.getKey());
        roi.setLabel(labelNew);

        if(isROISampleUpdateNecessary())
        {
            Map<String, Map<Object, QuantitativeSample>> samplesChanged = resource.addOrReplaceROIAndUpdate(roi);
            samplesChanged.remove(Datasets.X_COORDINATE);
            samplesChanged.remove(Datasets.Y_COORDINATE);

            roiSamplesDialog.refreshSamples(samplesChanged); 
            handleChangeOfROIUnion();

            statisticsForROIsUpdated = true;
        }
        else
        {
            resource.addOrReplaceROI(roi);

            ROI roiUnion = getSelectedResource().getROIUnion();
            //this is only half-update, the minimal and maximal values for rois are not updated
            GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();
            if(receiver != null)
            {
                receiver.setMaskedRegion(roiUnion);
            }

            changesToROIUnionDeferred = true;
            statisticsForROIsUpdated = false;
        }

        updateROIAvailability();

        int index = selectionModel.getIndex(roi);
        ROIStyle roiStyle = index>-1 ? roiStyles.get(index) : currentRoiStyle;

        for(ROIReceiver receiver : roiReceivers)
        {
            receiver.addOrReplaceROI(roi);
        }
        chartPanel.addOrReplaceROI(roi, roiStyle);

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.addOrUpdateROI(roi);

        setMode(roi.getMouseInputMode(getMode()), false);

        selectionModel.addOrReplaceROI(roi, index);    
    }

    @Override
    public void removeROI(ROIDrawable roi) 
    {
        ROIDrawable roiCopy = roi.copy();

        Channel2DResource resource = getSelectedResource();

        if(isROISampleUpdateNecessary())
        {
            Map<String, Map<Object, QuantitativeSample>> samplesChanged = resource.removeROIAndUpdate(roiCopy);

            samplesChanged.remove(Datasets.X_COORDINATE);
            samplesChanged.remove(Datasets.Y_COORDINATE);
            handleChangeOfROIUnion();

            statisticsForROIsUpdated = true;
        }
        else
        {
            resource.removeROI(roi);

            ROI roiUnion = getSelectedResource().getROIUnion();
            //this is only half-update, the minimal and maximal values for rois are not updated
            GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();
            if(receiver != null)
            {
                receiver.setMaskedRegion(roiUnion);
            }

            changesToROIUnionDeferred = true;
            statisticsForROIsUpdated = false;
        }


        updateROIAvailability();

        for(ROIReceiver receiver : getAllROIReceivers())
        {
            receiver.removeROI(roiCopy);
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.removeROI(roiCopy);

        setMode(roi.getMouseInputMode(getMode()), false);
        selectionModel.removeROI(roi);
    }

    @Override
    public void setROIs(Map<Object, ROIDrawable> rois) 
    {
        Channel2DResource resource = getSelectedResource();
        Map<String, Map<Object, QuantitativeSample>> samples = resource.setROIsAndUpdate(rois);
        samples.remove(Datasets.X_COORDINATE);
        samples.remove(Datasets.Y_COORDINATE);
        roiSamplesDialog.setSamples(samples);

        updateROIAvailability();

        for(ROIReceiver receiver : roiReceivers)
        {
            receiver.setROIs(rois);
        }
        chartPanel.setROIs(rois, currentRoiStyle);       

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.setROIs(rois);

        handleChangeOfROIUnion();
        selectionModel.setROIs(rois);
    }

    private List<ROIReceiver> getAllROIReceivers()
    {
        List<ROIReceiver> allProfileReceivers = new ArrayList<>();

        allProfileReceivers.add(chartPanel);
        allProfileReceivers.addAll(roiReceivers);

        return allProfileReceivers;
    }

    private boolean isROISampleUpdateNecessary()
    {
        boolean isNecessary = roiSamplesDialog.isShowing()  
                || chartPanel.getGradientPaintReceiver().isColorROIFullRange();

        return isNecessary;
    }

    private void handleChangeOfROIUnion()
    {
        R selectedResource = getSelectedResource();
        ROI unionNew = selectedResource.getROIUnion();
        Map<String, QuantitativeSample> samplesForUnion = selectedResource.getROIUnionSamples();

        for(Entry<String, QuantitativeSample> entry: samplesForUnion.entrySet())
        {
            String type = entry.getKey();
            QuantitativeSample sample = entry.getValue();

            if (chartPanel != null && type.equals(getSelectedType())) 
            {
                GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();

                if(sample != null)
                {
                    double[] data = sample.getMagnitudes();

                    double min = ArrayUtilities.getMinimum(data);
                    double max = ArrayUtilities.getMaximum(data);

                    receiver.setLowerROIBound(min);
                    receiver.setUpperROIBound(max);	
                }
                else
                {
                    receiver.setLensToFull();
                }		

                receiver.setMaskedRegion(unionNew);
            }
        }	

        changesToROIUnionDeferred = false;
    }

    @Override
    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        for(ROIReceiver receiver : getAllROIReceivers())
        {
            receiver.changeROILabel(roiKey, labelOld, labelNew);          
        }

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.replaceROILabel(roiKey, labelNew);

        Channel2DResource resource = getSelectedResource();
        MetaMap<String, Object, QuantitativeSample> changedSamples = resource.changeROILabel(roiKey, labelOld, labelNew);

        refreshComponentsSampleName(changedSamples.getMapCopy());
    }

    @Override
    public int getCurrentROIIndex()
    {
        int index = -1;

        V selectedChart = chartPanel.getSelectedChart();
        if(selectedChart != null)
        {
            index = selectedChart.getCurrentROIIndex();
        } 

        return index;
    }

    @Override
    public ROI getROIUnion()    
    {
        R selectedResource = getSelectedResource();
        ROI roi = (selectedResource != null) ? selectedResource.getROIUnion() : new ROIComposite("All");
        return roi;
    }

    @Override
    public Map<Object, ROI> getDrawableROIs()
    {
        R selectedResource = getSelectedResource();

        Map<Object, ROI> rois = new LinkedHashMap<>();

        if(selectedResource != null)
        {
            rois.putAll(selectedResource.getROIs());
        }

        return rois;
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
    private void refreshComponentsSampleName(Map<String, Map<Object, QuantitativeSample>> samples)
    {
        samples.remove(Datasets.X_COORDINATE);
        samples.remove(Datasets.Y_COORDINATE);

        roiSamplesDialog.refreshSamples(samples);
    }

    @Override
    public void showHistograms()
    {
        histogramView.setVisible(true);
    }

    @Override
    public void requestCursorChange(Cursor cursor) 
    {
        chartPanel.setCursor(cursor);
    }

    @Override
    public void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor)
    {
        V chart = chartPanel.getSelectedChart();
        boolean isVertical = (chart.getCustomizablePlot().getOrientation() == PlotOrientation.VERTICAL);
        Cursor cursor = isVertical ? verticalCursor : horizontalCursor;
        chartPanel.setCursor(cursor);
    }   

    @Override
    public void setProfilesAvailable(boolean b) 
    {
    }

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
        chartPanel.setROIBasedActionsEnabled(b);      
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


    protected void handleChangeOfData(Map<String, Channel2D> channelsChanged)
    {                        
        R resource = getSelectedResource();
        String selectedChannelType = getSelectedType();
        handleChangeOfData(channelsChanged, selectedChannelType, resource);
    }

    @Override
    public void handleChangeOfData(Map<String, Channel2D> channelsChanged, String type, Channel2DResource resource)
    {
        Channel2DChart<?> chart = chartPanel.getSelectedChart();

        for (String key : channelsChanged.keySet()) 
        {
            chart.notifyOfDataChange(key);
        }

        Map<Object, QuantitativeSample> changedROISamples = resource.getSamplesForROIs(type);
        roiSamplesDialog.refreshSamples(changedROISamples, type);

        Map<Object, QuantitativeSample> changedChannelSamples = resource.getSamples(type);
        channelSamplesDialog.refreshSamples(changedChannelSamples, type);

        updateRangeHistogramGradientChooser();

        if(rangeGradientChooser != null)
        {
            rangeGradientChooser.updateHistogramSample();
        }
        handleChangeOfROIUnion();
    }

    @Override
    public void editGradient() 
    {
        if (chartPanel != null) 
        {
            GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();

            if (rangeGradientChooser == null) 
            {
                rangeGradientChooser = new RangeGradientChooser(parent, receiver);
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
        if (chartPanel != null) 
        {
            GradientPaintReceiver receiver = chartPanel.getGradientPaintReceiver();

            if (rangeGradientHistogramChooser == null) 
            {
                rangeGradientHistogramChooser = new RangeHistogramView(parent, receiver.getPaintedSample(), receiver, "Gradient range");
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
            this.gradientSelectionDialog = new GradientSelectionDialog(parent);
        }
        gradientSelectionDialog.setVisible(true);
    }

    @Override
    public void showGradientChooser(ColorGradientReceiver gradientReceiver)
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(parent);
        }
        gradientSelectionDialog.showDialog(gradientReceiver);
    }

    @Override
    public void showGradientChooser(List<ColorGradientReceiver> gradientReceivers)
    {
        if(gradientSelectionDialog == null)
        {
            this.gradientSelectionDialog = new GradientSelectionDialog(parent);
        }
        gradientSelectionDialog.showDialog(gradientReceivers);
    }


    @Override
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey)
    {}

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(ROISelectionPageModel.CHANNEL2D_RESOURCE.equals(name))
        {
            selectResource((R)evt.getOldValue(), (R)evt.getNewValue());
        }
        else if(ProcessingModelInterface.CURRENT_BATCH_NUMBER.equals(name))
        {
            int newBatchNumber = (int)evt.getNewValue();
            updateROIStyle(newBatchNumber);

            this.labelPatchIndex.setText(Integer.toString(newBatchNumber + 1));
        }
    }

    private void updateROIStyle(int index)
    {
        int n = roiStyles.size();
        for(int i = n; i<=index;i++)
        {
            ROIStyle style = new ROIStyle(pref.node(Integer.toString(index)), DefaultColorSupplier.getPaint(index));
            roiStyles.add(style);
        }

        this.currentRoiStyle = roiStyles.get(index);   

        List<ROIStyle> boundedStyles = new ArrayList<>(roiStyles);
        boundedStyles.remove(currentRoiStyle);

        roiEditor.setBoundedModels(boundedStyles);

        this.roiEditor.setModel(currentRoiStyle);
    }

    @Override
    public ColorGradient getColorGradient() 
    {
        ColorGradient table = null;
        Channel2DChart<? extends Channel2DPlot> chart = chartPanel.getSelectedChart();
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
            Channel2DChart<? extends Channel2DPlot> chart = chartPanel.getSelectedChart();
            ChannelRenderer renderer = chart.getCustomizablePlot().getRenderer();

            if (renderer instanceof ColorGradientReceiver) 
            {
                ColorGradientReceiver paintReceiver = (ColorGradientReceiver) renderer;
                paintReceiver.setColorGradient(gradient);
            }
        }
    }

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


    private class ShowStatisticsForRoisAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowStatisticsForRoisAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Statistics for ROIs");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showROIStatistics();
        }
    }

    private class CorectForROIPlaneAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public CorectForROIPlaneAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Correct ROI plane");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            Channel2DDataInROITransformation tr = new PolynomialFitCorrection(new int[][] {{0,1},{1}});
            transform(tr, ROIRelativePosition.INSIDE);
        }
    }

    private class ModifyROIStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyROIStyleAction() 
        {
            putValue(NAME, "ROI style");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            roiEditor.setVisible(true);
        }
    }

    private class ROIPolygonAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ROIPolygonAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roi.png"));

            putValue(SHORT_DESCRIPTION, "Polygonal ROI");

            putValue(LARGE_ICON_KEY, icon);
            // putValue(NAME, "Polygon ROI");
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
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/rectangleRoi.png"));

            putValue(SHORT_DESCRIPTION, "Rectangular ROI");

            putValue(LARGE_ICON_KEY, icon);
            // putValue(NAME, "Rectangular ROI");
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

    private class ROIEllipticAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ROIEllipticAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/elipticRoi.png"));

            putValue(SHORT_DESCRIPTION, "Elliptic ROI");

            putValue(LARGE_ICON_KEY, icon);
            //putValue(NAME, "Elliptic ROI");
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

    private class ROIFreeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIFreeAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/freeRoi.png"));

            putValue(SHORT_DESCRIPTION, "Free hand ROI");

            putValue(LARGE_ICON_KEY, icon);
            //putValue(NAME, "Free hand ROI");
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

    private class ROIHoleAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIHoleAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roiHolesJoined.png"));

            putValue(SHORT_DESCRIPTION, "Draw ROI hole");
            putValue(LARGE_ICON_KEY, icon);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean drawHoles = (boolean) getValue(SELECTED_KEY);
            setDrawROIHoles(drawHoles);
        }
    }

    private class ROIWandAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIWandAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roiWand.png"));

            putValue(LARGE_ICON_KEY, icon);
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

    @Override
    public void requestNewDomainMarker(double knobPosition) 
    {        
    }

    @Override
    public void publishRawData(List<SampleCollection> rawData) 
    {
        if (!rawData.isEmpty()) 
        {
            Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
            for (SampleCollection collection : rawData) 
            {
                String collectionName = collection.getShortName();
                RawDataTableModel model = new RawDataTableModel(collection, false);
                StandardNumericalTable table = new OrderedNumericalTable(model,true);
                tables.put(collectionName, table);
            }
            MultipleNumericalTableView dialog = new MultipleNumericalTableView(SwingUtilities.getWindowAncestor(viewPanel), tables, "Raw data", true);
            dialog.setVisible(true);
        }
    }

    @Override
    public String getTaskName() 
    {
        return selectionModel.getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return selectionModel.getTaskDescription();
    }

    @Override
    public String getIdentifier() 
    {
        return IDENTIFIER;
    }

    @Override
    public boolean isFirst() 
    {
        return selectionModel.isFirst();
    }

    @Override
    public boolean isLast() 
    {
        return selectionModel.isLast();
    }

    @Override
    public boolean isBackEnabled() 
    {
        return false;
    }

    @Override
    public boolean isNextEnabled()
    {
        return selectionModel.isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return selectionModel.isSkipEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return selectionModel.isFinishEnabled();
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return selectionModel.isNecessaryInputProvided();
    }

    @Override
    public Component getView() 
    {
        return viewPanel;
    }

    @Override
    public Component getControls() 
    {
        return controls;
    }

    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkers) {
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
    public void refreshUndoRedoOperations()
    {
        checkIfUndoRedoEnabled();
    }

    private void checkIfUndoRedoEnabled()
    {
        boolean redoEnabled = false;
        boolean undoEnabled = false;

        R resource = getSelectedResource();
        String type = getSelectedType();

        if(resource != null && type != null)
        {
            redoEnabled = resource.canBeRedone(type);
            undoEnabled = resource.canBeUndone(type);
        }
        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);
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
            R resource = getSelectedResource();
            if(resource != null)
            {
                resource.undo(getSelectedType());
            }            
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
            }        
        }
    }


    @Override
    public List<? extends R> getResources() 
    {
        List<R> resources = new ArrayList<>();
        resources.add(selectionModel.getDensityResource());

        return resources;
    }

    @Override
    public List<? extends Channel2DResource> getAdditionalResources() 
    {
        List<Channel2DResource> additionalResources = new ArrayList<>();

        additionalResources.addAll(AtomicJ.getResultDestination().getImageResources());
        additionalResources.addAll(AtomicJ.getResultDestination().getMapResources());

        return additionalResources;
    }

    @Override
    public MultiMap<String, V> getAllNonEmptyCharts() 
    {
        MultiMap<String, V> allNonEmptyCharts = new MultiMap<>();     
        List<V> nonemptyCharts = chartPanel.getNonEmptyCharts();       
        allNonEmptyCharts.putAll(selectionModel.getType(), nonemptyCharts);

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

        if(this.mouseInteractiveTool != null)
        {
            mouseInteractiveTool.addMouseToolListener(mouseInteractiveToolListener);
            setMouseInputsReservedForTools(mouseInteractiveTool.getUsedMouseInputTypes());

            V selectedChart = chartPanel.getSelectedChart();
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

            V selectedChart = chartPanel.getSelectedChart();
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

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes());
        }

        return identifiers;
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes(filter));
        }

        return identifiers;
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers()
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes());
        }

        return identifiers;
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes(filter));
        }

        return identifiers;
    }


    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiers(type));
        }

        return identifiers;
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiers(type, filter));
        }

        return identifiers;
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiers(type));
        }

        return identifiers;
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel2D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getResources();

        for(Channel2DResource resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiers(type, filter));
        }

        return identifiers;
    }

    @Override
    public int getResourceCount() 
    {
        return 1;
    }

    @Override
    public void addResourceSelectionListener(
            SelectionListener<? super Channel2DResource> listener) {

    }

    @Override
    public void removeResourceSelectionListener(
            SelectionListener<? super Channel2DResource> listener) {        
    }


    @Override
    public void addResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        //        getResourceModel().addDataModelListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super Channel2DResource> listener)
    {
        //        getResourceModel().removeDataModelListener(listener);
    }

    @Override
    public void addResourceTypeListener(ResourceTypeListener listener)
    {
        //        getResourceModel().addResourceTypeListener(listener);
    }

    @Override
    public void removeResourceTypeListener(ResourceTypeListener listener)
    {
        //        getResourceModel().removeResourceTypeListener(listener);
    }

    private class CustomMouseInteractiveToolListener implements MouseInteractiveToolListener
    {
        @Override
        public void toolToRedraw() 
        {
            chartPanel.chartChanged();
        }       
    }
}
