
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

package atomicJ.gui;


import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.analysis.PreviewDestination;
import atomicJ.analysis.SpectroscopyProcessingOrigin;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.data.Channel1D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.MultipleXYChartPanel.MultipleChartPanelFactory;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.curveProcessingActions.Convolve1DAction;
import atomicJ.gui.curveProcessingActions.Crop1DAction;
import atomicJ.gui.curveProcessingActions.GaussianFilter1DAction;
import atomicJ.gui.curveProcessingActions.Gridding1DAction;
import atomicJ.gui.curveProcessingActions.LocalRegression1DAction;
import atomicJ.gui.curveProcessingActions.Median1DFilterAction;
import atomicJ.gui.curveProcessingActions.MedianWeightedFilter1DAction;
import atomicJ.gui.curveProcessingActions.SavitzkyGolayFilter1DAction;
import atomicJ.gui.curveProcessingActions.Translate1DAction;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.readers.SpectroscopyReadingModel;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.MetaMap;


public class SpectroscopyView extends ResourceXYView<SpectroscopyBasicResource, ChannelChart<?>, MultipleXYChartPanel<ChannelChart<?>>>
implements PreviewDestination, ResourceView<SpectroscopyBasicResource, Channel1D, String>
{
    private final static Preferences PREF =  Preferences.userNodeForPackage(SpectroscopyView.class).node("PreviewDialog");

    private final Action cropAction = new Crop1DAction<>(this);
    private final Action translateAction = new Translate1DAction<>(this);
    private final Action griddingAction = new Gridding1DAction<>(this);

    private final Action medianFilterAction = new Median1DFilterAction<>(this);
    private final Action medianWeightedFilterAction = new MedianWeightedFilter1DAction<>(this);
    private final Action gaussianFilterAction = new GaussianFilter1DAction<>(this);
    private final Action savitzkyGolayFilterAction = new SavitzkyGolayFilter1DAction<>(this);
    private final Action localRegressionAction = new LocalRegression1DAction<>(this);
    private final Action convolveAction = new Convolve1DAction<>(this);

    private final Action processAction = new ProcessAction();
    private final Action processSelectedAction = new ProcessSelectedAction();
    private final Action openAction = new OpenAction();

    private final Action undoAction = new UndoAction();
    private final Action undoAllAction = new UndoAllAction();
    private final Action redoAction = new RedoAction();
    private final Action redoAllAction = new RedoAllAction();

    private final FlexibleNumericalTableView distanceMeasurementsView = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Curves distance measurements");
    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(getAssociatedWindow());

    private final FileOpeningWizard<SimpleSpectroscopySource> openingWizard = new FileOpeningWizard<>(new OpeningModelStandard<SimpleSpectroscopySource>(this), SpectroscopyReadingModel.getInstance());

    public SpectroscopyView(Window parent)
    {
        this(parent, false);
    }

    public SpectroscopyView(Window parent, boolean clearWhenClosing)
    {
        super(parent, MultipleChartPanelFactory.getInstance(), "Force curves", PREF, ModalityType.MODELESS, new SpectroscopyViewModel<>());	

        JMenu chartMenu = getChartMenu();
        JMenuItem rawDataItem = new JMenuItem(getRawDataAction());
        chartMenu.add(rawDataItem);

        JPanel panelResources = getPanelResources();
        JTabbedPane panelCharts = getTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panelResources,panelCharts);
        splitPane.setOneTouchExpandable(true);

        setCenterComponent(splitPane);
        setSouthComponent(getMultipleResourcesPanelButtons());

        controlForResourceEmptiness(isEmpty());

        buildMenuBar();

        initInputAndActionMaps();
        createAndRegisterPopupMenu(); 


        if(clearWhenClosing)
        {
            addDataViewListener(new DataViewAdapter() {

                @Override
                public void dataViewVisibilityChanged(boolean visibleNew) 
                {
                    if(!visibleNew)
                    {
                        clear();
                    }
                }
            });
        }
    }

    @Override
    public SpectroscopyViewModel<SpectroscopyBasicResource> getResourceModel()
    {
        return (SpectroscopyViewModel<SpectroscopyBasicResource>)super.getResourceModel();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers()
    {
        return getResourceModel().getAllResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
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
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public void addResourceSelectionListener(SelectionListener<? super SpectroscopyBasicResource> listener)
    {
        getResourceModel().addSelectionListener(listener);
    }

    @Override
    public void removeResourceSelectionListener(SelectionListener<? super SpectroscopyBasicResource> listener)
    {
        getResourceModel().removeSelectionListener(listener);
    }

    @Override
    public void addResourceDataListener(ResourceGroupListener<? super SpectroscopyBasicResource> listener)
    {
        getResourceModel().addDataModelListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super SpectroscopyBasicResource> listener)
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

    private void initInputAndActionMaps()
    {
        List<Action> actions = Arrays.asList(openAction, undoAction, undoAllAction, redoAction, redoAllAction);
        registerActionAcceleratorKeysInInputMaps(actions);
    }

    private void buildMenuBar()
    {
        JMenuBar menuBar = getMenuBar();

        JMenu fileMenu = getFileMenu();

        fileMenu.insert(new JMenuItem(openAction), 0);
        fileMenu.insert(new JMenuItem(processAction), 1);

        JMenu modifyMenu = new JMenu("Preprocess");    

        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem undoAllItem = new JMenuItem(undoAllAction);
        JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem redoAllItem = new JMenuItem(redoAllAction);

        modifyMenu.add(undoItem);
        modifyMenu.add(redoItem);
        modifyMenu.addSeparator();

        modifyMenu.add(undoAllItem);
        modifyMenu.add(redoAllItem);
        modifyMenu.addSeparator();

        JMenuItem cropItem = new JMenuItem(cropAction);
        JMenuItem translateItem = new JMenuItem(translateAction);
        JMenuItem griddingItem = new JMenuItem(griddingAction);

        modifyMenu.add(cropItem);
        modifyMenu.add(translateItem);
        modifyMenu.add(griddingItem);
        modifyMenu.addSeparator();

        JMenuItem medianFilterItem = new JMenuItem(medianFilterAction);
        JMenuItem medianWeightedFilterItem = new JMenuItem(medianWeightedFilterAction);
        JMenuItem gaussianFilterItem = new JMenuItem(gaussianFilterAction);
        JMenuItem savitzkyGolayFilterItem = new JMenuItem(savitzkyGolayFilterAction);
        JMenuItem localRegressionItem = new JMenuItem(localRegressionAction);
        JMenuItem convolveItem = new JMenuItem(convolveAction);  

        modifyMenu.add(medianFilterItem);
        modifyMenu.add(medianWeightedFilterItem);
        modifyMenu.add(gaussianFilterItem);
        modifyMenu.add(savitzkyGolayFilterItem);
        modifyMenu.add(localRegressionItem);
        modifyMenu.addSeparator();

        modifyMenu.add(convolveItem);

        menuBar.add(modifyMenu);
    }

    private void createAndRegisterPopupMenu() 
    {           
        JPopupMenu popup = getResourceListPopupMenu();

        JMenuItem item = new JMenuItem(processSelectedAction);
        popup.insert(item, 0);
    }

    // we need private 'copy' of the controlForResourceEmptiness, because it is
    // called in the constructor
    // we must use private copy, so that it is not overriden
    @Override
    public void controlForResourceEmptiness(boolean empty) {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    private void controlForResourceEmptinessPrivate(boolean empty) {
        boolean enabled = !empty;
        enableActionsSpectroscopyDialog(enabled);
    }

    private void enableActionsSpectroscopyDialog(boolean enabled)
    {
        processAction.setEnabled(enabled);

        cropAction.setEnabled(enabled);
        translateAction.setEnabled(enabled);
        convolveAction.setEnabled(enabled);
        griddingAction.setEnabled(enabled);
        medianFilterAction.setEnabled(enabled);
        medianWeightedFilterAction.setEnabled(enabled);
        gaussianFilterAction.setEnabled(enabled);
        savitzkyGolayFilterAction.setEnabled(enabled);
        localRegressionAction.setEnabled(enabled);

        checkIfUndoRedoEnabled();
    }

    @Override
    public void requestPreviewEnd()
    {
        openingWizard.endPreview();
    }

    @Override
    public void publishPreviewData(Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> charts)
    {
        if(!charts.isEmpty())
        {
            int previousCount = getResourceCount();
                        
            addResources(charts);
            selectResource(previousCount);
        }
    }

    @Override
    public Window getPublicationSite() {
        return getAssociatedWindow();
    }

    public void startPreview()
    {
        openingWizard.setVisible(true);
    }

    @Override
    protected void close()
    {
        setVisible(false);
    }

    private void startProcessingAllSources() 
    {
        List<SimpleSpectroscopySource> originalSources = getResourceModel().getSources();
        List<SimpleSpectroscopySource> sources = new ArrayList<>(StandardSimpleSpectroscopySource.copySources(originalSources).keySet());
        startProcessing(sources);
    }

    private void startProcessingSelectedSources()
    {
        List<SimpleSpectroscopySource> originalSources = getResourceModel().getSourcesFromAllSelectedResources();
        List<SimpleSpectroscopySource> sources = new ArrayList<>(StandardSimpleSpectroscopySource.copySources(originalSources).keySet());
        startProcessing(sources);
    }

    private void startProcessing(List<SimpleSpectroscopySource> sources) 
    {
        SpectroscopyResultDestination resultDestination = AtomicJ.getResultDestination();
        SpectroscopyProcessingOrigin processingOrigin = AtomicJ.getProcessingOrigin();

        int batchNumber = resultDestination.getResultBatchesCoordinator().getPublishedBatchCount();
        String name = Integer.toString(batchNumber);
        ProcessingBatchModel model = new ProcessingBatchModel(resultDestination, sources, name, batchNumber);

        processingOrigin.startProcessing(Collections.singletonList(model));
    }


    ///MEASUREMENTS


    @Override
    public void showDistanceMeasurements() 
    {
        distanceMeasurementsView.setVisible(true);
    }

    @Override
    protected void showMeasurementEditor()
    {
        measurementEditor.setVisible(true);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {
        SpectroscopyBasicResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();

            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            boolean measurementsAvailable = getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            MultipleXYChartPanel<ChannelChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.addOrReplaceDistanceMeasurement(measurement);        

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsView.getTable().getModel();
            model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {                
        SpectroscopyBasicResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();

            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            int measurementsCount = selectedResource.getMeasurementCount(selectedType);
            boolean measurementsAvailable = getMode().isMeasurement() && measurementsCount > 0;
            updateMeasurementsAvailability(measurementsAvailable);

            MultipleXYChartPanel<ChannelChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.removeDistanceMeasurement(measurement);

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsView.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }   
    }

    @Override
    public void handleChangeOfSelectedResource(SpectroscopyBasicResource resourceOld, SpectroscopyBasicResource resourceNew) 
    {
        //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'

        if(resourceNew != null)
        {
            String selectedType = getSelectedType();

            File defaultOutputFile = resourceNew.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resourceNew.getDistanceMeasurements(selectedType);
            boolean measurementsAvailable = !distanceMeasurements.isEmpty() && getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            Map<Object, DistanceShapeFactors> measurementShapeFactors = new LinkedHashMap<>();
            for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
            {
                Object key = entry.getKey();
                DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                measurementShapeFactors.put(key, line);
            }

            PrefixedUnit dataUnitX = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit dataUnitY = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit displayedUnitX = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit displayedUnitY = SimplePrefixedUnit.getNullInstance();

            ChannelChart<?> newChart = getSelectedChart();

            if(newChart != null)
            {
                CustomizableXYPlot plot = newChart.getCustomizablePlot();
                dataUnitX = plot.getDomainDataUnit();
                dataUnitY = plot.getRangeDataUnit();
                displayedUnitX = plot.getDomainDisplayedUnit();
                displayedUnitY = plot.getRangeDisplayedUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();

                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
            distanceMeasurementsModel.addDistances(measurementShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsView.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel);      
        }

        checkIfUndoRedoEnabled();
    }


    @Override
    public void publishPreviewed2DData(
            Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> chartMaps) {
        // TODO Auto-generated method stub

    }

    @Override
    public PrefixedUnit getDataUnit() 
    {
        SpectroscopyBasicResource resource = getSelectedResource();
        String type = getSelectedType();
        return resource.getSingleDataUnit(type);    
    }

    @Override
    public PrefixedUnit getDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        ChannelChart<?> chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getRangeDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        ChannelChart<?> chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDataUnit()
    {
        PrefixedUnit axisUnit = null;
        ChannelChart<?> chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDataUnit();
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

        return domainUnitManagers;
    }

    @Override
    public List<PrefixedUnit> getDomainDataUnits()
    {
        List<PrefixedUnit> domainUnitManagers = new ArrayList<>();

        domainUnitManagers.add(getXAxisDataUnit());

        return domainUnitManagers;
    }

    @Override
    public ROI getROIUnion() 
    {
        return getResourceModel().getROIUnion();
    }

    @Override
    public Map<Object, ROI> getDrawableROIs() 
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<Object, ROI> getAllROIs()
    {
        return Collections.emptyMap();
    }

    @Override
    public List<? extends SpectroscopyBasicResource> getAdditionalResources() 
    {
        return Collections.emptyList();
    }

    @Override
    public void handleChangeOfData(Map<String, Channel1D> channelsChanged, String type, SpectroscopyBasicResource resource) 
    {
        ChannelChart<?> chart = getChart(resource, type);

        for (Object key : channelsChanged.keySet()) 
        {
            chart.notifyOfDataChange(key);
        }

        DataChangeEvent<String> event = new DataChangeEvent<>(this, channelsChanged.keySet());
        fireDataChangeEvent(event);        
    }

    @Override
    public void pushCommand(SpectroscopyBasicResource resource, String type,
            UndoableCommand command) 
    {
        resource.pushCommand(type, command);
        checkIfUndoRedoEnabled();        
    }

    @Override
    public void pushCommands(MetaMap<SpectroscopyBasicResource, String, UndoableCommand> commands)
    {
        getResourceModel().pushCommands(commands);
        checkIfUndoRedoEnabled();        
    }

    @Override
    public void notifyToolsOfMouseClicked(CustomChartMouseEvent evt) 
    {        
    }

    @Override
    public void notifyToolsOfMouseDragged(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void notifyToolsOfMouseMoved(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void notifyToolsOfMousePressed(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void notifyToolsOfMouseReleased(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void useMouseInteractiveTool(MouseInteractiveTool tool) {        
    }

    @Override
    public void stopUsingMouseInteractiveTool(MouseInteractiveTool tool) {        
    }   

    @Override
    public MouseInteractiveTool getCurrentlyUsedInteractiveTool()
    {
        return null;
    }

    @Override
    public boolean isChartElementCaughtByTool()
    {
        return false;
    }

    @Override
    public boolean isComplexElementUnderConstructionByTool()
    {
        return false;
    }

    @Override
    public boolean isRightClickReservedByTool(Rectangle2D dataArea, Point2D dataPoint)
    {
        return false;
    }

    @Override
    public void refreshUndoRedoOperations()
    {
        checkIfUndoRedoEnabled();
    }

    private void checkIfUndoRedoEnabled()
    {
        SpectroscopyViewModel<SpectroscopyBasicResource> model = getResourceModel();

        boolean redoEnabled = model.canRedoBeEnabled();
        boolean undoEnabled = model.canUndoBeEnabled();

        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);

        checkIfUndoRedoAllEnabled();
    } 

    private void checkIfUndoRedoAllEnabled()
    {
        SpectroscopyViewModel<SpectroscopyBasicResource> model = getResourceModel();

        boolean redoAllEnabled = model.canRedoAllBeEnabled();
        boolean undoAllEnabled = model.canUndoAllBeEnabled();

        undoAllAction.setEnabled(undoAllEnabled);
        redoAllAction.setEnabled(redoAllEnabled);
    }

    private class ProcessAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ProcessAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Process");
            putValue(SHORT_DESCRIPTION, "Process force curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startProcessingAllSources();
        }
    }

    private class ProcessSelectedAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ProcessSelectedAction()
        {
            putValue(NAME,"Process");
            putValue(SHORT_DESCRIPTION, "Process force curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startProcessingSelectedSources();
        }
    }

    private class OpenAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OpenAction() 
        {
            putValue(NAME, "Open");

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            startPreview();
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
            SpectroscopyBasicResource resource = getSelectedResource();
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
            getResourceModel().undoAll(SpectroscopyView.this);
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
            SpectroscopyBasicResource resource = getSelectedResource();
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
            getResourceModel().redoAll(SpectroscopyView.this);
        }
    }
}
