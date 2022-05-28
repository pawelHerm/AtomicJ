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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.ForceContourStackFunction;
import atomicJ.analysis.ForceSeparationStackFunction;
import atomicJ.analysis.ForceStackFunction;
import atomicJ.analysis.GradientSeparationStackFunction;
import atomicJ.analysis.GradientStackFunction;
import atomicJ.analysis.ForceContourSeparationStackFunction;
import atomicJ.analysis.PointwiseModulusSeparationStackFunction;
import atomicJ.analysis.PointwiseModulusStackFunction;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.analysis.StackingPackFunctionFactory;
import atomicJ.analysis.StiffeningSeparationStackFunction;
import atomicJ.analysis.StiffeningStackFunction;
import atomicJ.analysis.VisualizableSpectroscopyPack;
import atomicJ.analysis.VisualizationSettings;
import atomicJ.data.Channel2D;
import atomicJ.data.DataAxis1D;
import atomicJ.data.Grid1D;
import atomicJ.data.Quantities;
import atomicJ.data.Datasets;
import atomicJ.gui.MapPanel.MapPanelFactory;
import atomicJ.gui.curveProcessing.CurveReplottingDialog;
import atomicJ.gui.curveProcessing.CurveReplottingModel;
import atomicJ.gui.curveProcessing.CurveVisualizationHandler;
import atomicJ.gui.curveProcessing.CurveVisualizationReplaceHandler;
import atomicJ.gui.curveProcessing.MapSourceHandler;
import atomicJ.gui.curveProcessing.MapSourceInPlaceRecalculationHandler;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.NumericalResultsReplaceHandler;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.curveProcessing.RecalculateMapCurvesDialog;
import atomicJ.gui.curveProcessing.RecalculateMapCurvesModel;
import atomicJ.gui.curveProcessing.MapSourceStandardHandler;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.stack.StackMapChart;
import atomicJ.gui.stack.StackMapView;
import atomicJ.gui.stack.StackModel;
import atomicJ.gui.stack.StackParametersDialogNew;
import atomicJ.gui.stack.StackParametersModel;
import atomicJ.gui.statistics.StatisticsView;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.MapImageResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.ProcessedPackReplacementResults;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;

public class MapView extends Channel2DView<MapImageResource, MapChart<?>, MapPanel> implements SpectroscopySupervisor 
{
    private static final Preferences PREF = Preferences.userNodeForPackage(MapView.class).node("ForceMapDialog");

    private final Action recalculateAction = new RecalculateAction();

    private final Action replotCurvesAction = new ReplotCurvesAction();

    private final Action forceStackAction = new ForceStackAction();
    private final Action forceGradientStackAction = new ForceGradientStackAction();
    private final Action pointwiseModulusStackAction = new PointwiseModulusStackAction();
    private final Action stiffeningStackAction = new StiffeningStackAction();
    private final Action forceContourMappingActionAction = new ForceCountourMappingAction();

    private final Action linkMarkersWithResultsAction = new LinkMarkersWithResultsAction();

    private final StackParametersDialogNew stackParametersView = new StackParametersDialogNew(getAssociatedWindow());
    private final RecalculateMapCurvesDialog recalculateDialog = new RecalculateMapCurvesDialog(getAssociatedWindow(), false);


    public MapView(final MainView parent) 
    {
        super(parent, MapPanelFactory.getInstance(), parent.getMapHistogramDialog(), "Maps", PREF, ModalityType.MODELESS);

        createAndRegisterPopupMenu();
        buildMenuBar();
        modifyToolBar();

        controlForSelectedChartEmptinessPrivate(isEmpty());
    }

    private void buildMenuBar()
    {
        JMenuBar menuBar = getMenuBar();
        JMenu curvesMenu = new JMenu("Curves");
        curvesMenu.setMnemonic(KeyEvent.VK_U);

        JCheckBoxMenuItem linkMarkersWithResultsItem = new JCheckBoxMenuItem(linkMarkersWithResultsAction);

        curvesMenu.add(getMarkerStyleMenuItem());
        curvesMenu.add(getAddMapMarkerItem());
        curvesMenu.add(linkMarkersWithResultsItem);

        JMenuItem recalculateROIsItem = new JMenuItem(recalculateAction);

        curvesMenu.addSeparator();
        curvesMenu.add(recalculateROIsItem);

        JMenuItem replotCurvesItem = new JMenuItem(replotCurvesAction);

        curvesMenu.addSeparator();
        curvesMenu.add(replotCurvesItem);

        menuBar.add(curvesMenu);

        JMenu stackMenu = new JMenu("Stacks");
        stackMenu.setMnemonic(KeyEvent.VK_T);

        JMenuItem forceStackItem = new JMenuItem(forceStackAction);
        JMenuItem pointwiseModulusStackItem = new JMenuItem(pointwiseModulusStackAction);
        JMenuItem pointwiseModulusExcessStackItem = new JMenuItem(stiffeningStackAction);
        JMenuItem forceContourMappingStackItem = new JMenuItem(forceContourMappingActionAction);

        stackMenu.add(forceStackItem);
        stackMenu.add(pointwiseModulusStackItem);
        stackMenu.add(pointwiseModulusExcessStackItem);

        stackMenu.add(forceContourMappingStackItem);

        menuBar.add(stackMenu);
    }

    private void modifyToolBar()
    {
        JToolBar toolBar = getToolBar();

        JButton buttonRecalculate = new JButton(recalculateAction);
        buttonRecalculate.setHideActionText(true);
        buttonRecalculate.setMargin(new Insets(0, 0, 0, 0));
        buttonRecalculate.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonRecalculate.setHorizontalAlignment(SwingConstants.LEFT);

        toolBar.add(buttonRecalculate, 0);
    }

    @Override
    public void handleNewChartPanel(MapPanel panel) 
    {
        super.handleNewChartPanel(panel);
        panel.setSpectroscopySupervisor(this);
    }

    public void drawMaps(List<MapSource<?>> forceMaps) 
    {
        int previousCount = getResourceCount();

        for (MapSource<?> mapSource : forceMaps) 
        {
            Map<String, MapChart<?>> charts = buildMapCharts(mapSource);
            MapImageResource resource = new MapImageResource(mapSource, mapSource.getIdentifiers());
            addResource(resource, charts);
        }

        selectResource(previousCount);
    }

    public void drawMaps(Map<MapSource<?>, List<ImageSource>> forceMaps) 
    {        
        int previousCount = getResourceCount();

        for(Entry<MapSource<?>, List<ImageSource>> entry : forceMaps.entrySet())
        {
            MapSource<?> map = entry.getKey();

            Map<String, MapChart<?>> charts = buildMapCharts(map);
            MapImageResource resource = new MapImageResource(map, map.getIdentifiers());

            List<ImageSource> imageSources = entry.getValue();

            for(ImageSource imageSource : imageSources)
            {
                Map<String, MapChart<?>> chartsForImage = ChannelSourceVisualization.getMapChartsFromImageSource(imageSource);
                charts.putAll(chartsForImage);

                resource.registerChannels(imageSource, imageSource.getIdentifiers());
            }

            addResource(resource, charts);
        }

        selectResource(previousCount);
    }

    private Map<String, MapChart<?>> buildMapCharts(MapSource<?> mapSource)
    {
        List<? extends Channel2D> allChannels = mapSource.getChannels();
        Preferences prefParentNode = Preferences.userNodeForPackage(getClass()).node(getClass().getName());

        Map<String, MapChart<?>> charts = ChannelSourceVisualization.getMapCharts(mapSource, allChannels, prefParentNode);

        return charts;
    }

    @Override
    public MapImageResource copyResource(MapImageResource resourceOld, String shortName, String longName)
    {
        return new MapImageResource(resourceOld, shortName, longName);
    }

    @Override
    public MapImageResource copyResource(MapImageResource resourceOld, Set<String> typesToRetain, String shortName, String longName)
    {
        return new MapImageResource(resourceOld, typesToRetain, shortName, longName);
    }

    @Override
    public void controlForResourceEmptiness(boolean empty) {
        super.controlForResourceEmptiness(empty);

        boolean enabled = !empty;
        enableAllActionsMapDialog(enabled);
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty) {
        super.controlForSelectedChartEmptiness(empty);

        controlForSelectedChartEmptinessPrivate(empty);
    }

    public void controlForSelectedChartEmptinessPrivate(boolean empty) {
        boolean enabled = !empty;
        enableAllActionsMapDialog(enabled);
    }

    private void enableAllActionsMapDialog(boolean enabled) 
    {
        recalculateAction.setEnabled(enabled);
        replotCurvesAction.setEnabled(enabled);
        linkMarkersWithResultsAction.setEnabled(enabled);
        forceStackAction.setEnabled(enabled);
        forceGradientStackAction.setEnabled(enabled);
        pointwiseModulusStackAction.setEnabled(enabled);
        stiffeningStackAction.setEnabled(enabled);
        forceContourMappingActionAction.setEnabled(enabled);     
    }

    @Override
    public void showStatistics() 
    {
        MapImageResource resource = getSelectedResource();

        Map<String, StatisticsTable> tablesPacks = getStatisticsTables();

        String title = "Statistics for " + resource.getShortName();
        showStatistics(tablesPacks, title);
    }

    private void showStatistics(Map<String, StatisticsTable> tables,String title) 
    {
        StatisticsView dialog = new StatisticsView(getAssociatedWindow(), tables, title, true);
        dialog.setVisible(true);
    }

    private void createAndRegisterPopupMenu()
    {
        JPopupMenu popup = getResourceListPopupMenu();

        JMenuItem itemJumpToResults = new JMenuItem("Find results");
        itemJumpToResults.setAction(new JumpToResultsAction());
        popup.insert(itemJumpToResults, 0);
    }

    @Override
    public void showHistograms() {
        getResultDestination().showMapHistograms(true);
    }

    @Override
    protected void close() {
        getResultDestination().showMaps(false);
    }

    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker) 
    {
        super.addOrReplaceMapMarker(mapMarker);

        boolean link = (boolean) linkMarkersWithResultsAction.getValue(Action.SELECTED_KEY);

        if(link)
        {
            Point2D controlPoint = mapMarker.getControlDataPoint();
            ProcessedSpectroscopyPack pack = getProcessedPack(controlPoint);

            jumpToResults(pack.getSource());
            jumpToFigures(pack);
        }
    }

    @Override
    protected String getMapMarkerSourceName(Point2D controlPoint) 
    {
        SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(controlPoint);
        String sourceName = (simpleSource != null) ? simpleSource.getShortName() : "";
        return sourceName;
    }

    @Override
    protected String getMapMarkerPositionDescription(Point2D controlPoint)
    {
        SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(controlPoint);
        String description = (simpleSource != null) ? simpleSource.getMapPositionDescription() : "";
        return description;
    }

    private void linkMarkersWithResults(boolean link)
    {        
        Channel2DResource resource = getSelectedResource();

        if(link && resource != null)
        {            
            List<MapMarker> mapMarkers = new ArrayList<>(resource.getMapMarkers().values());
            if(!mapMarkers.isEmpty())
            {
                Point2D controlPoint = mapMarkers.get(0).getControlDataPoint();
                SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(controlPoint);

                jumpToResults(simpleSource);
                jumpToFigures(simpleSource);
            }
        }
    }

    @Override
    public void jumpToResults(Point2D p)
    {
        SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(p);
        jumpToResults(simpleSource);
    }

    @Override
    public void jumpToResults(MapImageResource resource, Point2D p)
    {
        SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(resource, p);
        jumpToResults(simpleSource);
    }

    @Override
    public void jumpToResults(SimpleSpectroscopySource source)
    {
        if (source != null) 
        {
            try 
            {
                SpectroscopyResultDestination destination = getResultDestination();
                destination.showResults(source);
            } 
            catch (UserCommunicableException e) 
            {
                showInformationMessage("Results for the position not found");
            }
        }
    }

    private void jumpToResults()
    {
        try 
        {
            List<MapImageResource> resources = getAllSelectedResources();
            List<SimpleSpectroscopySource> simpleSources = new ArrayList<>();

            for (MapImageResource resource : resources) 
            {
                MapSource<?> forceMap = resource.getMapSource();
                simpleSources.addAll(forceMap.getSimpleSources());
            }

            SpectroscopyResultDestination destination = getResultDestination();
            destination.showResults(simpleSources);
            destination.showCalculations(true);
        } 
        catch (UserCommunicableException e)
        {
            showInformationMessage(e.getMessage());
        }
    }

    @Override
    public void jumpToFigures(Point2D p) 
    {
        ProcessedSpectroscopyPack pack = getProcessedPack(p);
        jumpToFigures(pack);
    }

    @Override
    public void jumpToFigures(MapImageResource resource, Point2D p) 
    {
        ProcessedSpectroscopyPack pack = getProcessedPack(resource, p);
        jumpToFigures(pack);
    }

    @Override
    public void jumpToFigures(SimpleSpectroscopySource source) 
    {
        if (source == null) {
            return;
        }

        SpectroscopyResultDestination destination = getResultDestination();

        try 
        {
            if (destination.containsFiguresForSource(source)) {
                destination.showFigures(source);
            } else {
                handleSourceWithoutFigures(source);
            }
        } catch (UserCommunicableException e) {
            handleSourceWithoutFigures(source);
        }
    }

    private void handleSourceWithoutFigures(SimpleSpectroscopySource source) 
    {
        SpectroscopyResultDestination destination = getResultDestination();

        List<SimpleSpectroscopySource> sources = Collections.singletonList(source);
        destination.startPreview(sources);
    }

    public void jumpToFigures(ProcessedSpectroscopyPack pack) 
    {
        if (pack == null) {
            return;
        }

        SpectroscopyResultDestination destination = getResultDestination();

        try 
        {
            if (destination.containsFiguresForSource(pack.getSource())) 
            {
                destination.showFigures(pack.getSource());
            } else
            {
                drawFigures(pack);
            }
        } catch (UserCommunicableException e) {
            drawFigures(pack);
        }
    }

    private void drawFigures()
    {                
        MapImageResource selectedResource = getSelectedResource();
        CurveReplottingModel replottingModel = new CurveReplottingModel(getDrawableROIs(), getROIUnion());

        CurveReplottingDialog replottingDialog = new CurveReplottingDialog(getAssociatedWindow(), "Plot settings", true);

        replottingDialog.showDialog(replottingModel);

        if(replottingModel.isApplied())
        {
            ROIRelativePosition position = replottingModel.getROIPosition();
            ROI roi = replottingModel.getSelectedROI();

            VisualizationSettings visualizationSettings = replottingModel.getVisualizationSettings();

            MapSource<?> mapSource = selectedResource.getMapSource();

            Set<ProcessedSpectroscopyPack> packs = mapSource.getProcessedPacks(roi, position);
            drawFigures(packs, visualizationSettings);
        }
    }

    private void drawFigures(ProcessedSpectroscopyPack pack) 
    {
        SpectroscopyResultDestination destination = getResultDestination();

        List<VisualizableSpectroscopyPack> visualizablePacks = new ArrayList<>();
        visualizablePacks.add(pack.visualize(new VisualizationSettings()));

        destination.getDefaultCurveVisualizationHandler().handlePublicationRequest(visualizablePacks);
    }

    private void drawFigures(Collection<? extends ProcessedSpectroscopyPack> packs,VisualizationSettings visualizationSettings) 
    {
        SpectroscopyResultDestination destination = getResultDestination();

        List<VisualizableSpectroscopyPack> visualizablePacks = new ArrayList<>();

        for(ProcessedSpectroscopyPack pack : packs)
        {
            visualizablePacks.add(pack.visualize(visualizationSettings));
        }

        destination.getDefaultCurveVisualizationHandler().handlePublicationRequest(visualizablePacks);
    }



    private SimpleSpectroscopySource getSimpleSpectroscopySource(Point2D p)
    {
        MapImageResource selectedResource = getSelectedResource();
        return getSimpleSpectroscopySource(selectedResource, p);
    }

    private SimpleSpectroscopySource getSimpleSpectroscopySource(MapImageResource resource, Point2D p)
    {
        MapSource<?> mapSource = resource.getMapSource();
        SimpleSpectroscopySource simpleSource = mapSource.getSimpleSpectroscopySource(p);

        return simpleSource;
    }

    private ProcessedSpectroscopyPack getProcessedPack(Point2D p)
    {
        MapImageResource selectedResource = getSelectedResource();

        return getProcessedPack(selectedResource, p);
    }

    private ProcessedSpectroscopyPack getProcessedPack(MapImageResource resource, Point2D p)
    {
        MapSource<?> mapSource = resource.getMapSource();
        ProcessedSpectroscopyPack simpleSource = mapSource.getProcessedPack(p);

        return simpleSource;
    }

    @Override
    public void recalculate(Point2D p) 
    {
        SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(p);
        recalculate(simpleSource);
    }

    @Override
    public void recalculate(MapImageResource resource, Point2D p) 
    {
        SimpleSpectroscopySource simpleSource = getSimpleSpectroscopySource(resource, p);
        recalculate(simpleSource);
    }

    @Override
    public void recalculate(SimpleSpectroscopySource source)
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();
        sources.add(source);

        recalculate(source.getForceMap(), sources);
    }   

    public void recalculate() 
    {
        recalculate(null, ROIRelativePosition.EVERYTHING);
    }

    @Override
    public void recalculate(ROI roi, ROIRelativePosition position) 
    {
        recalculate(getSelectedResource(), roi, position);
    }

    @Override
    public void recalculate(MapImageResource resource, ROI roi, ROIRelativePosition position) 
    {
        MapSource<?> mapSource = resource.getMapSource();
        List<SimpleSpectroscopySource> sources = mapSource.getSimpleSources(roi, position);

        recalculate(mapSource, sources);
    }

    private void recalculate(MapSource<?> mapSource, List<SimpleSpectroscopySource> sources)
    {
        MainView parent = getMainFrame();

        List<SimpleSpectroscopySource> copies = new ArrayList<>();
        MapSource<?> mapSourceCopy = mapSource.copy(new ArrayList<String>());

        for (SimpleSpectroscopySource src : sources) 
        {
            SimpleSpectroscopySource sourceCopy = src.copy();
            mapSourceCopy.replaceSpectroscopySource(sourceCopy, sourceCopy.getMapPosition());
            copies.add(sourceCopy);
        }

        int batchNumber = parent.getResultBatchesCoordinator().getPublishedBatchCount();
        String name = Integer.toString(batchNumber);
        ProcessingBatchModel model = new ProcessingBatchModel(parent, copies, name, batchNumber);

        List<ProcessingBatchModel> models = Collections.singletonList(model);
        parent.startProcessing(models);
    }


    private void recalculateInPlace(List<SimpleSpectroscopySource> sourceCopies, MapSourceHandler mapSourceHandler, CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler, NumericalResultsHandler<ProcessedSpectroscopyPack> resultsHandle)
    {
        MainView parent = getMainFrame();

        int batchNumber = parent.getResultBatchesCoordinator().getPublishedBatchCount();
        String name = Integer.toString(batchNumber);
        ProcessingBatchModel model = new ProcessingBatchModel(parent, sourceCopies, name, batchNumber);

        List<ProcessingBatchModel> models = Collections.singletonList(model);
        parent.startProcessing(models, mapSourceHandler, curveVisualizationHandler, resultsHandle);
    }

    @Override
    public void recalculateFullDialog()
    {
        RecalculateMapCurvesModel recalculateModel = new RecalculateMapCurvesModel(getDrawableROIs(), getROIUnion());

        recalculateDialog.showDialog(recalculateModel);

        if(recalculateModel.isApplied())
        {            
            if(recalculateModel.isModifyMapsInPlace())
            {
                MainView mainFrame = getMainFrame();

                boolean deleteOldCurveCharts = recalculateModel.isDeleteOldCurveCharts();

                final MapImageResource resource = getSelectedResource();              

                MapSource<?> mapSource = resource.getMapSource();
                List<SimpleSpectroscopySource> sources = mapSource.getSimpleSources(recalculateModel.getSelectedROI(), recalculateModel.getROIPosition());


                List<SimpleSpectroscopySource> copies = new ArrayList<>();
                Map<SimpleSpectroscopySource, SimpleSpectroscopySource> mapNewVsOldSource = new LinkedHashMap<>();
                for (SimpleSpectroscopySource src : sources) 
                {
                    SimpleSpectroscopySource sourceCopy = src.copy();
                    sourceCopy.setForceMap(src.getForceMap());
                    copies.add(sourceCopy);
                    mapNewVsOldSource.put(sourceCopy, src);
                }

                MapSourceHandler mapSourceHandle = new MapSourceInPlaceRecalculationHandler(this);
                CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandle = deleteOldCurveCharts ? new CurveVisualizationReplaceHandler(getMainFrame().getGraphicalResultsDialog(), mapNewVsOldSource) : mainFrame.getDefaultCurveVisualizationHandler();

                recalculateInPlace(copies, mapSourceHandle, curveVisualizationHandle, getResultsHandler(recalculateModel, mapSource));
            }
            else
            {
                recalculate(recalculateModel.getSelectedROI(), recalculateModel.getROIPosition());
            }
        }
    }

    private NumericalResultsHandler<ProcessedSpectroscopyPack> getResultsHandler(RecalculateMapCurvesModel model, MapSource<?> mapSource)
    {
        boolean deleteOldNumericalResults = model.isDeleteOldNumericalResults();

        if(deleteOldNumericalResults)
        {
            Set<ProcessedSpectroscopyPack> packs = mapSource.getProcessedPacks(model.getSelectedROI(), model.getROIPosition());

            return new NumericalResultsReplaceHandler<>(getMainFrame().getResultDialog(), packs);
        }

        return getMainFrame().getDefaultNumericalResultsHandler();
    }

    public void replace(MultiMap<MapSource<?>, ProcessedSpectroscopyPack> packsToReplace, Map<MapSource<?>, List<ImageSource>> mapSources)
    {        
        if(!MapSourceStandardHandler.containsData(mapSources))
        {
            return;
        }        

        Preferences prefParentNode = Preferences.userNodeForPackage(getClass()).node(getClass().getName());

        boolean channelsAdded = false;
        for(MapSource<?> mapSource : mapSources.keySet())
        {            
            List<ProcessedSpectroscopyPack> packs = packsToReplace.get(mapSource);

            if(!packs.isEmpty())
            {
                List<MapImageResource> containingResources = getResourcesContainingChannelsFrom(mapSource);

                for(MapImageResource resource : containingResources)
                {
                    ProcessedPackReplacementResults replacementResults = mapSource.replaceProcessedPacks(packs);    

                    Map<String, Channel2D> updatedChannelsUniversalIdMap = replacementResults.getUpdatedChannels();                 
                    MetaMap<String, String, Channel2D> updatedChannelsForTypes = resource.notifyOfChanges(updatedChannelsUniversalIdMap.keySet());

                    for(String type : updatedChannelsForTypes.keySet())
                    {
                        handleChangeOfData(updatedChannelsForTypes.get(type), type, resource);
                    }   

                    Map<String, Channel2D> newChannelsUniversalIdMap = replacementResults.getNewChannels();                 
                    channelsAdded = channelsAdded || !newChannelsUniversalIdMap.isEmpty();
                    Map<String, MapChart<?>> newChannelsCharts = ChannelSourceVisualization.getMapCharts(mapSource, newChannelsUniversalIdMap.values(), prefParentNode);
                    updateResourceWithNewCharts(resource, newChannelsCharts);
                    for(Channel2D ch : newChannelsUniversalIdMap.values())
                    {
                        String identifier = ch.getIdentifier();
                        resource.registerChannel(identifier, mapSource, identifier);
                    }

                }
            }
        }
        if(channelsAdded)
        {
            refreshStatisticsDialogs();
        }
    }

    public void showFStack() {
        MapImageResource resource = getSelectedResource();
        MapSource<?> mapSource = resource.getMapSource();

        StackParametersModel stackParametersModel = new StackParametersModel(getDrawableROIs(), getROIUnion(), "Force stack parameters",  Quantities.INDENTATION_MICRONS);    
        stackParametersView.showDialog(stackParametersModel);

        if (stackParametersModel.isApplied()) 
        {
            boolean fixContact = stackParametersModel.isFixContact();
            int frameCount = stackParametersModel.getFrameCount();
            double minimum = stackParametersModel.getStackMinimum();
            double maximum = stackParametersModel.getStackMaximum();

            ROI roi = stackParametersModel.getSelectedROI();
            ROIRelativePosition position = stackParametersModel.getROIPosition();

            StackingPackFunctionFactory<?> factory = null;
            if (fixContact) 
            {
                factory = new ForceStackFunction.ForceStackFunctionFactory();
            }
            else 
            {
                double sampleHighestPoint = mapSource.getHeightRange().getUpperBound();
                factory = new ForceSeparationStackFunction.ForceSeparationStackFunctionFactory(sampleHighestPoint);
            }

            initStack(factory, roi, position, minimum, maximum, frameCount, resource, mapSource);
        }
    }

    public void showForceGradientStack() 
    {
        MapImageResource resource = getSelectedResource();
        MapSource<?> mapSource = resource.getMapSource();

        StackParametersModel stackParametersModel = new StackParametersModel(getDrawableROIs(), getROIUnion(), "Force gradient stack parameters",  Quantities.INDENTATION_MICRONS);    
        stackParametersView.showDialog(stackParametersModel);

        if (stackParametersModel.isApplied()) 
        {
            boolean fixContact = stackParametersModel.isFixContact();
            int frameCount = stackParametersModel.getFrameCount();
            double minimum = stackParametersModel.getStackMinimum();
            double maximum = stackParametersModel.getStackMaximum();

            ROI roi = stackParametersModel.getSelectedROI();
            ROIRelativePosition position = stackParametersModel.getROIPosition();

            StackingPackFunctionFactory<?> factory = null;
            if (fixContact) 
            {
                factory = new GradientStackFunction.GradientStackFunctionFactory();
            } else {
                double sampleHighestPoint = mapSource.getHeightRange().getUpperBound();
                factory = new GradientSeparationStackFunction.GradientSeparationStackFunctionFactory(
                        sampleHighestPoint);
            }

            initStack(factory, roi, position, minimum, maximum, frameCount, resource,mapSource);
        }
    }

    private void showPointwiseModulusStack()
    {
        MapImageResource resource = getSelectedResource();
        MapSource<?> mapSource = resource.getMapSource();

        StackParametersModel stackParametersModel = new StackParametersModel(getDrawableROIs(), getROIUnion(), "Pointwise modulus stack parameters",  Quantities.INDENTATION_MICRONS);    
        stackParametersView.showDialog(stackParametersModel);

        if (stackParametersModel.isApplied()) 
        {
            boolean fixContact = stackParametersModel.isFixContact();
            int frameCount = stackParametersModel.getFrameCount();
            double minimum = stackParametersModel.getStackMinimum();
            double maximum = stackParametersModel.getStackMaximum();

            ROI roi = stackParametersModel.getSelectedROI();
            ROIRelativePosition position = stackParametersModel.getROIPosition();

            StackingPackFunctionFactory<?> factory = null;
            if (fixContact) {
                factory = new PointwiseModulusStackFunction.PointwiseModulusFunctionFactory();
            } else {
                double sampleHighestPoint = mapSource.getHeightRange().getUpperBound();
                factory = new PointwiseModulusSeparationStackFunction.PointwiseModulusSeparationStackFunctionFactory(
                        sampleHighestPoint);
            }

            initStack(factory, roi, position, minimum, maximum, frameCount, resource, mapSource);
        }
    }

    private void showStiffeningStack()
    {
        MapImageResource resource = getSelectedResource();
        MapSource<?> mapSource = resource.getMapSource();

        StackParametersModel stackParametersModel = new StackParametersModel(getDrawableROIs(), getROIUnion(), "Stiffening stack parameters",  Quantities.INDENTATION_MICRONS);    
        stackParametersView.showDialog(stackParametersModel);

        if (stackParametersModel.isApplied()) 
        {
            boolean fixContact = stackParametersModel.isFixContact();
            int frameCount = stackParametersModel.getFrameCount();
            double minimum = stackParametersModel.getStackMinimum();
            double maximum = stackParametersModel.getStackMaximum();

            ROI roi = stackParametersModel.getSelectedROI();
            ROIRelativePosition position = stackParametersModel.getROIPosition();

            StackingPackFunctionFactory<?> factory = null;
            if (fixContact) 
            {
                factory = new StiffeningStackFunction.StiffeningStackFunctionFactory();
            } 
            else 
            {
                double sampleHighestPoint = mapSource.getHeightRange().getUpperBound();
                factory = new StiffeningSeparationStackFunction.StiffeningSeparationFunctionFactory(
                        sampleHighestPoint);
            }

            initStack(factory, roi, position, minimum, maximum, frameCount, resource, mapSource);
        }
    }

    public void showForceCountourMapping() 
    {
        MapImageResource resource = getSelectedResource();
        MapSource<?> mapSource = resource.getMapSource();

        StackParametersModel stackParametersModel = new StackParametersModel(getDrawableROIs(), getROIUnion(),  "Force countour mapping parameters",  Quantities.FORCE_NANONEWTONS);    
        stackParametersView.showDialog(stackParametersModel);

        if (stackParametersModel.isApplied()) 
        {
            boolean fixContact = stackParametersModel.isFixContact();
            int frameCount = stackParametersModel.getFrameCount();
            double minimum = stackParametersModel.getStackMinimum();
            double maximum = stackParametersModel.getStackMaximum();

            ROI roi = stackParametersModel.getSelectedROI();
            ROIRelativePosition position = stackParametersModel.getROIPosition();
            StackingPackFunctionFactory<?> factory = null;
            if (fixContact) {
                factory = new ForceContourStackFunction.IndentationStackFunctionFactory();
            } else {
                double sampleLowestPoint = mapSource.getHeightRange()
                        .getLowerBound();
                factory = new ForceContourSeparationStackFunction.IndentationStackFunctionFactory(
                        sampleLowestPoint);
            }

            initStack(factory, roi, position, minimum, maximum, frameCount, resource, mapSource);
        }
    }

    private void showStack(StackModel<MapImageResource> stackModel) 
    {
        String stackType = stackModel.getStackType();
        String sourceName = stackModel.getSourceName();

        Preferences pref = Preferences.userNodeForPackage(ImagePlot.class).node(ImagePlot.class.getName()).node(stackType);

        List<Channel2D> channels = stackModel.getChannels();

        ProcessableXYZDataset dataset = new MovieProcessableDataset(Channel2DDataset.getDatasets(channels, sourceName), stackModel.getDepthQuantity().getName());
        CustomizableImageRenderer renderer = new CustomizableImageRenderer(new StandardStyleTag(stackType), stackType);
        Channel2DPlot plot = new Channel2DPlot(dataset, renderer, Datasets.DENSITY_PLOT, pref);

        StackMapChart<Channel2DPlot> chart = new StackMapChart<>(plot, Datasets.DENSITY_PLOT);
        chart.setStackModel(stackModel);

        StackMapView dialog = new StackMapView(AtomicJ.getApplicationFrame(), this, chart, stackModel);

        dialog.setVisible(true);
    }

    @Override
    public List<? extends Channel2DResource> getAdditionalResources() 
    {
        return AtomicJ.getResultDestination().getImageResources();
    }

    private class JumpToResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JumpToResultsAction() {
            putValue(MNEMONIC_KEY, KeyEvent.VK_J);
            putValue(NAME, "Jumpt to results");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            jumpToResults();
        };
    }

    private class RecalculateAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public RecalculateAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/RecalculateCurves.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Recalculate");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            recalculateFullDialog();
        }
    }

    private class ReplotCurvesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ReplotCurvesAction() {
            putValue(NAME, "Replot curves");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            drawFigures();
        }
    }

    private class ForceStackAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ForceStackAction() {
            putValue(NAME, "Force stack");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showFStack();
        }
    }

    private class ForceGradientStackAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ForceGradientStackAction() {
            putValue(NAME, "Force gradient stack");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showForceGradientStack();
        }
    }

    private class PointwiseModulusStackAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public PointwiseModulusStackAction() {
            putValue(NAME, "Pointwise modulus stack");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showPointwiseModulusStack();
        }
    }

    private class StiffeningStackAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public StiffeningStackAction() {
            putValue(NAME, "Stiffening stack");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showStiffeningStack();
        }
    }

    private class ForceCountourMappingAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ForceCountourMappingAction() {
            putValue(NAME, "Force contour mapping");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showForceCountourMapping();
        }
    }

    private class LinkMarkersWithResultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LinkMarkersWithResultsAction() 
        {
            putValue(NAME, "Link with results");

            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean link = (boolean) getValue(SELECTED_KEY);
            linkMarkersWithResults(link);
        }
    }


    private void initStack(final StackingPackFunctionFactory<?> factory,
            ROI roi, ROIRelativePosition position, final double minimum, final double maximum,
            final int frameCount, final MapImageResource resource,
            final MapSource<?> mapSource)
    {
        final DataAxis1D stackAxis = new Grid1D((maximum - minimum)/(frameCount - 1.), minimum, frameCount, factory.getStackingQuantity());

        final ConcurrentStackTask task = new ConcurrentStackTask(factory, roi, position,
                minimum, maximum, frameCount, mapSource, getAssociatedWindow());

        // we store the task as a instance variable, so that it can be cancelled
        // when the user cancels the dialog containing source selection page
        task.getPropertyChangeSupport().addPropertyChangeListener("state",
                new PropertyChangeListener() 
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                    try {
                        boolean cancelled = task.isCancelled();

                        if (!cancelled) 
                        {
                            List<Channel2D> channels = task.getChannels();

                            StackModel<MapImageResource> stackModel = new StackModel<>(stackAxis,factory.getEvaluatedQuantity(),mapSource.getShortName(),
                                    mapSource.getDefaultOutputLocation(), mapSource.getShortName(), channels, resource);

                            int failureCount = task.getFailuresCount();
                            reactToFailures(failureCount);
                            showStack(stackModel);
                        }
                        else
                        {
                            reactToCancellation();
                        }
                    }

                    finally {
                    }
                }
            }
        });
        task.execute();

    }

    private void reactToCancellation()
    {
        showInformationMessage("Stack generation terminated");
    }

    private void reactToFailures(int failureCount)
    {
        if(failureCount > 0)
        {
            String errorMessage = "Errors occured during generation of " + failureCount + " frames";
            showErrorMessage(errorMessage);
        }
    }

    @Override
    public void publishPreviewed2DData(
            Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> chartMaps) {
        // TODO Auto-generated method stub

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
