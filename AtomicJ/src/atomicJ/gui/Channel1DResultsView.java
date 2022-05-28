
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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.*;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.UndoableCurveCommand;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.Datasets;
import atomicJ.data.SampleCollection;
import atomicJ.data.VerticalModificationConstraint1D;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.SpectroscopyResultPanel.SpectroscopyPanelFactory;
import atomicJ.gui.curveProcessingActions.Convolve1DAction;
import atomicJ.gui.curveProcessingActions.Crop1DAction;
import atomicJ.gui.curveProcessingActions.GaussianFilter1DAction;
import atomicJ.gui.curveProcessingActions.LocalRegression1DAction;
import atomicJ.gui.curveProcessingActions.Median1DFilterAction;
import atomicJ.gui.curveProcessingActions.MedianWeightedFilter1DAction;
import atomicJ.gui.curveProcessingActions.SavitzkyGolayFilter1DAction;
import atomicJ.gui.curveProcessingActions.Translate1DAction;
import atomicJ.gui.generalProcessing.ConcurrentTransformationTask;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.Resource;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyProcessedResource;
import atomicJ.resources.SpectroscopyResource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;

import static atomicJ.resources.StandardSpectroscopyProcessedResource.INDENTATION;
import static atomicJ.resources.StandardSpectroscopyProcessedResource.RECORDED_CURVE;
import static atomicJ.resources.StandardSpectroscopyProcessedResource.POINTWISE_MODULUS;;

public class Channel1DResultsView extends ResourceXYView<SpectroscopyProcessedResource, ChannelChart<?>, SpectroscopyResultPanel>
implements ResourceReceiver<SpectroscopyProcessedResource>, SpectroscopyGraphsSupervisor, ResourceView<SpectroscopyProcessedResource, Channel1D, String>
{
    private static final Preferences PREF = Preferences.userNodeForPackage(Channel1DResultsView.class).node("GraphicalResultsDialog");

    private final Action markSourcePositionAction = new MarkSourcePositionsAction();
    private final Action recalculateAction = new RecalculateAction();
    private final Action overlayAction = new OverlayAction();

    private final Action convertToSeparationAction = new ConvertToSeparationAction();
    private final Action convertAllToSeparationAction = new ConvertAllToSeparationAction();

    private final Action fixContactAllAxesAction = new FixContactAllAxesAction();
    private final Action fixContactXAxisAction  = new FixContactXAxisAction();
    private final Action fixContactYAxisAction = new FixContactYAxisAction();

    private final Action fixAllContactsXAction = new FixAllContactsXAxisAction();
    private final Action fixAllContactsYAction = new FixAllContactsYAxisAction();
    private final Action fixAllContactsAllAxesAction = new FixAllContactsAllAxesAction();

    private final Action correctContactPointAction = new CorrectContactPointAction();
    private final Action correctTransitionIndentationAction = new CorrectTransitionIndentationAction();
    private final Action correctAdhesionAction = new CorrectAdhesionAction();
    private final Action addAdhesionMeasurementAction = new AddAdhesionMeasurementAction();

    //copied

    private final Action cropAction = new Crop1DAction<>(this);
    private final Action translateAction = new Translate1DAction<>(this);

    private final Action medianFilterAction = new Median1DFilterAction<>(this);
    private final Action medianWeightedFilterAction = new MedianWeightedFilter1DAction<>(this);
    private final Action gaussianFilterAction = new GaussianFilter1DAction<>(this);
    private final Action savitzkyGolayFilterAction = new SavitzkyGolayFilter1DAction<>(this);
    private final Action localRegressionAction = new LocalRegression1DAction<>(this);
    private final Action convolveAction = new Convolve1DAction<>(this);
    private final Action undoAction = new UndoAction();
    private final Action undoAllAction = new UndoAllAction();
    private final Action redoAction = new RedoAction();
    private final Action redoAllAction = new RedoAllAction();

    ///


    private final SpectroscopyResultDestination destination;	
    private final ResourceSelectionWizard resourceSelectionWizard;

    private final FlexibleNumericalTableView distanceMeasurementsDialog = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Graphs distance measurements");
    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(getAssociatedWindow());

    private final RecalculateSpectroscopyResourcesDialog recalculateDialog;

    public Channel1DResultsView(final SpectroscopyResultDestination destination)
    {
        super(destination.getPublicationSite(), SpectroscopyPanelFactory.getInstance(), "Graphical results", PREF, ModalityType.MODELESS, new GraphicalResultsViewModel());

        this.resourceSelectionWizard = new ResourceSelectionWizard(getAssociatedWindow(), this);
        this.destination = destination;
        this.recalculateDialog = new RecalculateSpectroscopyResourcesDialog(getAssociatedWindow(), false);

        addChartPanelIfAbsent(RECORDED_CURVE);
        addChartPanelIfAbsent(INDENTATION);
        addChartPanelIfAbsent(POINTWISE_MODULUS);

        buildMenuBar();

        JPanel panelResources = getPanelResources();

        JTabbedPane graphicsPane = getTabbedPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelResources, graphicsPane);
        splitPane.setOneTouchExpandable(true);

        setCenterComponent(splitPane);
        setSouthComponent(getMultipleResourcesPanelButtons());		

        JToolBar toolBar = buildToolBar();
        setEastComponent(toolBar);

        initInputAndActionMaps();

        initSelectionListener();
        createAndRegisterPopupMenu();
        controlForResourceEmptinessPrivate(isEmpty());	
        checkIfActionsShouldBeEnabled();
    }

    private void initInputAndActionMaps()
    {
        List<Action> actions = Arrays.asList(undoAction, undoAllAction, redoAction, redoAllAction);
        registerActionAcceleratorKeysInInputMaps(actions);      
    }

    private void buildMenuBar()
    {

        JMenu chartMenu = getChartMenu();
        JMenuItem rawDataItem = new JMenuItem(getRawDataAction());
        JMenuItem overlayItem = new JMenuItem(overlayAction);

        chartMenu.add(rawDataItem);
        chartMenu.add(overlayItem);

        JMenu modifyMenu = new JMenu("Data");
        modifyMenu.setMnemonic(KeyEvent.VK_D);

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

        JMenu convertToSeparationMenu = new JMenu("Convert to separation");


        JMenuItem convertToSeparationItem = new JMenuItem(convertToSeparationAction);
        JMenuItem convertAllToSeparationItem = new JMenuItem(convertAllToSeparationAction);

        convertToSeparationMenu.add(convertToSeparationItem);
        convertToSeparationMenu.add(convertAllToSeparationItem);

        modifyMenu.add(convertToSeparationMenu);
        modifyMenu.addSeparator();

        JMenu fixContactMenu = new JMenu("Fix contact");

        JMenuItem fixContactAllAxesItem = new JMenuItem(fixContactAllAxesAction);
        JMenuItem fixContactXAxisItem = new JMenuItem(fixContactXAxisAction);
        JMenuItem fixContactYAxisItem = new JMenuItem(fixContactYAxisAction);

        fixContactMenu.add(fixContactAllAxesItem);
        fixContactMenu.addSeparator();
        fixContactMenu.add(fixContactXAxisItem);
        fixContactMenu.add(fixContactYAxisItem);

        JMenu fixAllContactsMenu = new JMenu("Fix all contacts");

        JMenuItem fixAllContactsAllAxesItem = new JMenuItem(fixAllContactsAllAxesAction);
        JMenuItem fixAllContactsXItem = new JMenuItem(fixAllContactsXAction);
        JMenuItem fixAllContactsYItem = new JMenuItem(fixAllContactsYAction);

        JCheckBoxMenuItem correctContactPointItem = new JCheckBoxMenuItem(correctContactPointAction);
        JCheckBoxMenuItem correctTransitionIndentationItem = new JCheckBoxMenuItem(correctTransitionIndentationAction);
        JCheckBoxMenuItem correctAdhesionMeasurementItem = new JCheckBoxMenuItem(correctAdhesionAction);
        JCheckBoxMenuItem addAdhesionMeasurementItem = new JCheckBoxMenuItem(addAdhesionMeasurementAction);

        fixAllContactsMenu.add(fixAllContactsAllAxesItem);
        fixAllContactsMenu.addSeparator();
        fixAllContactsMenu.add(fixAllContactsXItem);
        fixAllContactsMenu.add(fixAllContactsYItem);

        modifyMenu.add(fixContactMenu);
        modifyMenu.add(fixAllContactsMenu);
        modifyMenu.addSeparator();

        //copied
        JMenuItem cropItem = new JMenuItem(cropAction);
        JMenuItem translateItem = new JMenuItem(translateAction);

        JMenuItem medianFilterItem = new JMenuItem(medianFilterAction);
        JMenuItem medianWeightedFilterItem = new JMenuItem(medianWeightedFilterAction);
        JMenuItem gaussianFilterItem = new JMenuItem(gaussianFilterAction);
        JMenuItem savitzkyGolayFilterItem = new JMenuItem(savitzkyGolayFilterAction);
        JMenuItem localRegressionItem = new JMenuItem(localRegressionAction);
        JMenuItem convolveItem = new JMenuItem(convolveAction); 

        modifyMenu.add(cropItem);
        modifyMenu.add(translateItem);
        modifyMenu.addSeparator();

        modifyMenu.add(medianFilterItem);
        modifyMenu.add(medianWeightedFilterItem);
        modifyMenu.add(gaussianFilterItem);
        modifyMenu.add(savitzkyGolayFilterItem);
        modifyMenu.add(localRegressionItem);
        modifyMenu.addSeparator();
        modifyMenu.add(convolveItem);

        JMenuItem recalculateItem = new JMenuItem(recalculateAction);

        JMenu curveMenu = new JMenu("Curves");        
        curveMenu.add(recalculateItem);
        curveMenu.add(correctContactPointItem);
        curveMenu.add(correctTransitionIndentationItem);
        curveMenu.add(correctAdhesionMeasurementItem);

        JMenuItem markSourcePositionItem = new JMenuItem(markSourcePositionAction);

        curveMenu.addSeparator();
        curveMenu.add(markSourcePositionItem);

        JMenuBar menuBar = getMenuBar();
        menuBar.add(modifyMenu);
        menuBar.add(curveMenu);
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(10, 3, 10, 3)));
        toolBar.setFloatable(false);
        toolBar.setLayout(new VerticalWrapLayout(VerticalFlowLayout.TOP, 0,0));

        JButton buttonRecalculate = new JButton(recalculateAction);
        buttonRecalculate.setHideActionText(true);
        buttonRecalculate.setMargin(new Insets(0, 0, 0, 0));
        buttonRecalculate.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonRecalculate.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonCorrectContactPoint = new JToggleButton(correctContactPointAction);
        buttonCorrectContactPoint.setHideActionText(true);
        buttonCorrectContactPoint.setMargin(new Insets(0, 0, 0, 0));
        buttonCorrectContactPoint.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonCorrectContactPoint.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonCorrectTransitionIndentation = new JToggleButton(correctTransitionIndentationAction);
        buttonCorrectTransitionIndentation.setHideActionText(true);
        buttonCorrectTransitionIndentation.setMargin(new Insets(0, 0, 0, 0));
        buttonCorrectTransitionIndentation.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonCorrectTransitionIndentation.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonCorrectAdhesion = new JToggleButton(correctAdhesionAction);
        buttonCorrectAdhesion.setHideActionText(true);
        buttonCorrectAdhesion.setMargin(new Insets(0, 0, 0, 0));
        buttonCorrectAdhesion.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonCorrectAdhesion.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonAddAdhesion = new JToggleButton(addAdhesionMeasurementAction);
        buttonAddAdhesion.setHideActionText(true);
        buttonAddAdhesion.setMargin(new Insets(0, 0, 0, 0));
        buttonAddAdhesion.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonAddAdhesion.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonMarkPosition = new JToggleButton(markSourcePositionAction);
        buttonMarkPosition.setHideActionText(true);
        buttonMarkPosition.setMargin(new Insets(0, 0, 0, 0));
        buttonMarkPosition.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonMarkPosition.setHorizontalAlignment(SwingConstants.LEFT);

        JToggleButton buttonMeasureLine = new JToggleButton(getMeasureAction());
        buttonMeasureLine.setHideActionText(true);
        buttonMeasureLine.setMargin(new Insets(0, 0, 0, 0));
        buttonMeasureLine.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonMeasureLine.setHorizontalAlignment(SwingConstants.LEFT);  

        JToggleButton buttonCrop = new JToggleButton(cropAction);
        buttonCrop.setHideActionText(true);
        buttonCrop.setMargin(new Insets(0, 0, 0, 0));
        buttonCrop.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonCrop.setHorizontalAlignment(SwingConstants.LEFT);  

        JButton buttonGaussianSmoothing = new JButton(gaussianFilterAction);
        buttonGaussianSmoothing.setHideActionText(true);
        buttonGaussianSmoothing.setMargin(new Insets(0, 0, 0, 0));
        buttonGaussianSmoothing.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonGaussianSmoothing.setHorizontalAlignment(SwingConstants.LEFT);  


        JButton buttonMedianSmoothing = new JButton(medianFilterAction);
        buttonMedianSmoothing.setHideActionText(true);
        buttonMedianSmoothing.setMargin(new Insets(0, 0, 0, 0));
        buttonMedianSmoothing.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonMedianSmoothing.setHorizontalAlignment(SwingConstants.LEFT);  

        JButton buttonConvolve = new JButton(convolveAction);
        buttonConvolve.setHideActionText(true);
        buttonConvolve.setMargin(new Insets(0, 0, 0, 0));
        buttonConvolve.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonConvolve.setHorizontalAlignment(SwingConstants.LEFT);  


        toolBar.add(buttonRecalculate);
        toolBar.add(buttonCorrectContactPoint);
        toolBar.add(buttonCorrectTransitionIndentation);
        toolBar.add(buttonCorrectAdhesion);
        toolBar.add(buttonAddAdhesion);
        toolBar.add(buttonMarkPosition);
        toolBar.add(buttonMeasureLine);
        toolBar.add(buttonCrop);
        toolBar.add(buttonGaussianSmoothing);
        toolBar.add(buttonMedianSmoothing);
        toolBar.add(buttonConvolve);

        toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(0, 3, 0, 3)));
        toolBar.setMargin(new Insets(0,0,0,0));

        return toolBar;
    }

    private void initSelectionListener()
    {
        getResourceModel().addPropertyChangeListener(SpectroscopyViewModel.SELECTED_SOURCES_FROM_MAPS, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                boolean sourcesFromMap = (boolean)evt.getNewValue();
                setMarkSourcesEnabled(sourcesFromMap);
            }
        });
    }

    public void selectResourceContainingChannelsFrom(ChannelSource source)
    {
        SpectroscopyProcessedResource resource = getResourceContainingChannelsFrom(source);

        if(resource != null)
        {
            selectResource(resource);
        }
    }

    @Override
    public void handleNewChartPanel(SpectroscopyResultPanel panel) 
    {
        super.handleNewChartPanel(panel);

        panel.setSpectroscopyGraphSupervisor(this);
    }

    @Override
    public void controlForResourceEmptiness(boolean empty) 
    {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    // we need private 'copy' of the controlForResourceEmptiness, because it is
    // called in the constructor
    // we must copy private copy, so that it is not overridden

    private void controlForResourceEmptinessPrivate(boolean empty)
    {
        checkIfActionsShouldBeEnabled();
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty) 
    {
        super.controlForSelectedChartEmptiness(empty);

        checkIfActionsShouldBeEnabled();
    }

    public void overlay()
    {       
        SpectroscopyProcessedResource selectedResource = getSelectedResource();
        List<String> currentTypes = getNonemptyTypes(selectedResource);

        Map<Resource, List<String>> resources = getResourceTypeMap(currentTypes);
        resources.remove(getSelectedResource());

        resourceSelectionWizard.showDialog(resources);
    }

    public void overlay(SpectroscopyProcessedResource resourceThis, SpectroscopyProcessedResource resourceThat, Set<String> newTypes)
    {
        for(String type : newTypes)
        {
            ChannelChart<?> chartThis = getChart(resourceThis, type);

            if(chartThis == null)
            {
                break;
            }

            CustomizableXYPlot plotThis = chartThis.getCustomizablePlot();

            ChannelChart<?> chartThat = getChart(resourceThat, type);
            CustomizableXYPlot plotThat = chartThat.getCustomizablePlot();

            String resourceNewName  = resourceThat.getShortName();

            Map<String, Channel1D> channelsFromNew = resourceThat.getChannels(type);

            for(Entry<String, Channel1D> entry : channelsFromNew.entrySet())
            {
                Channel1D channel = entry.getValue();

                String idOld = entry.getKey();
                String idNew = idOld.toString() + " (" + resourceNewName + ")";

                String nameNew = channel.getName() + " (" + resourceNewName + ")";

                Channel1D channelCopy = channel.getCopy(idNew, nameNew);

                resourceThis.addChannel(type, channelCopy);

                int layerIndex = plotThat.getLayerIndex(idOld);                
                ChannelRenderer renderer;
                try {
                    renderer = (ChannelRenderer) (layerIndex > -1 ? plotThat.getRenderer(layerIndex).clone() : RendererFactory.getChannel1DRenderer(channelCopy));
                } catch (CloneNotSupportedException e) 
                {
                    e.printStackTrace();
                    renderer = RendererFactory.getChannel1DRenderer(channelCopy);
                }

                renderer.setName(nameNew);

                plotThis.addOrReplaceLayer(idNew, new Channel1DDataset(channelCopy, channelCopy.getName()), renderer);
            }
        }
    }

    @Override
    protected void showAllRawResourceData()
    {	
        List<SampleCollection> rawData = getResourceModel().getAllRawResourceData();
        publishRawData(rawData);
    }

    @Override
    public void showRawResourceData()
    {
        List<SampleCollection> rawData = getResourceModel().getRawDataForSelectedResource();
        publishRawData(rawData);    
    }

    @Override
    public void drawingChartsFinished()
    {
        setVisible(true);
    }

    private void setMarkSourcesEnabled(boolean enabled)
    {
        markSourcePositionAction.setEnabled(enabled);

        for(SpectroscopyResultPanel p : getPanels())
        {
            p.setMapPositionMarkingEnabled(enabled);
        }
    }

    @Override
    protected void close()
    {
        setVisible(false);
    }

    @Override
    public void jumpToResults()
    {
        try
        {
            SimpleSpectroscopySource source = getResourceModel().getSourceFromSelectedResource();
            destination.showResults(source);
        }
        catch(UserCommunicableException e)
        {
            showInformationMessage( e.getMessage());
        }
    }

    public void jumpToAllResults()
    {
        try
        {
            List<SimpleSpectroscopySource> allSources = getResourceModel().getSourcesFromAllSelectedResources();
            destination.showResults(allSources);
        }
        catch(UserCommunicableException e)
        {
            showInformationMessage(e.getMessage());
        }
    }

    @Override
    public void markSourcePosition()
    {
        SimpleSpectroscopySource source = getResourceModel().getSourceFromSelectedResource();
        destination.markSourcePositions(source);           
    }

    public void markAllSourcePositions()
    {
        List<SimpleSpectroscopySource> allSources = getResourceModel().getSourcesFromAllSelectedResources();
        destination.markSourcePositions(allSources);           
    }

    private void createAndRegisterPopupMenu() 
    {	        
        JPopupMenu popup = getResourceListPopupMenu();

        JMenuItem itemJumpToResults = new JMenuItem(new JumpToResultsAction());
        popup.insert(itemJumpToResults, 0);

        JMenuItem itemRecalculate = new JMenuItem(recalculateAction);
        popup.insert(itemRecalculate, 1);

        JMenuItem itemMarkSources = new JMenuItem(markSourcePositionAction);
        popup.insert(itemMarkSources, 2);
    }

    /// ITEM MOVEMENTS /////


    @Override
    public void itemMoved(Channel1D channel, int itemIndex, double[] newValue)
    {
        SpectroscopyProcessedResource selectedResource = getSelectedResource();
        selectedResource.itemMoved(getSelectedType(), channel, itemIndex, newValue);
    }

    @Override
    public void channelTranslated(Channel1D channel)
    {
        SpectroscopyProcessedResource selectedResource = getSelectedResource();
        selectedResource.channelTranslated(channel);
    }

    @Override
    public boolean isValidValue(Channel1D channel, int itemIndex, double[] newValue)
    {        
        SpectroscopyProcessedResource resource = getSelectedResource();
        return resource.isValidPosition(getSelectedType(), channel.getIdentifier(), itemIndex, new Point2D.Double(newValue[0], newValue[1]));       
    }

    @Override
    public Point2D correctPosition(Channel1D channel, int itemIndex, Point2D dataPoint)
    {
        SpectroscopyProcessedResource resource = getSelectedResource();       
        return resource.getValidItemPosition(getSelectedType(), channel.getIdentifier(), itemIndex, dataPoint);
    }

    @Override
    public ChannelGroupTag getNextGroupMemberTag(Object groupKey)
    {
        SpectroscopyProcessedResource resource = getSelectedResource();       
        return resource.getNextGroupMemberIdentity(getSelectedType(), groupKey);
    }

    @Override
    public void channelAdded(Channel1D channel)
    {
        SpectroscopyProcessedResource resource = getSelectedResource();     
        resource.addChannel(getSelectedType(), channel);
    }

    @Override
    public void channelRemoved(Channel1D channel)
    {
        SpectroscopyProcessedResource resource = getSelectedResource();     
        resource.removeChannel(getSelectedType(), channel);
    }

    ///MEASUREMENTS

    @Override
    public void showDistanceMeasurements() 
    {
        distanceMeasurementsDialog.setVisible(true);
    }

    @Override
    protected void showMeasurementEditor()
    {
        measurementEditor.setVisible(true);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {
        SpectroscopyResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            boolean measurementsAvailable = getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            MultipleXYChartPanel<ChannelChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.addOrReplaceDistanceMeasurement(measurement);        

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {                
        SpectroscopyResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            int measurementsCount = selectedResource.getMeasurementCount(selectedType);
            boolean measurementsAvailable = getMode().isMeasurement() && measurementsCount > 0;
            updateMeasurementsAvailability(measurementsAvailable);

            MultipleXYChartPanel<ChannelChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.removeDistanceMeasurement(measurement);

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }   
    }

    @Override
    public void handleChangeOfSelectedType(String typeOld, String typeNew) 
    {
        super.handleChangeOfSelectedType(typeOld, typeNew);

        //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'

        SpectroscopyResource resource = getSelectedResource();

        if(!Objects.equals(typeOld, typeNew) && resource != null)
        {
            File defaultOutputFile = resource.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resource.getDistanceMeasurements(typeNew);
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
                dataUnitX = newChart.getDomainDataUnit();
                dataUnitY = newChart.getRangeDataUnit();
                displayedUnitX = newChart.getDomainDisplayedUnit();
                displayedUnitY = newChart.getRangeDisplayedUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();
                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
            distanceMeasurementsModel.addDistances(measurementShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel); 
        }

        checkIfActionsShouldBeEnabled();
        checkIfUndoRedoEnabled();
        checkIfUndoRedoAllEnabled();
    }

    @Override
    public void handleChangeOfSelectedResource(SpectroscopyProcessedResource resourceOld, SpectroscopyProcessedResource resourceNew) 
    {
        //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'

        if(resourceNew != null)
        {
            String selectedType = getSelectedType();
            File defaultOutputFile = resourceNew.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resourceNew.getDistanceMeasurements(selectedType);
            boolean measurementsAvailable = distanceMeasurements.size()>0 && getMode().equals(MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE);
            updateMeasurementsAvailability(measurementsAvailable);

            Map<Object, DistanceShapeFactors> measurementShapeFactors = new LinkedHashMap<>();
            for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
            {
                Object key = entry.getKey();
                DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                measurementShapeFactors.put(key, line);
            }

            PrefixedUnit unitX = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit unitY = SimplePrefixedUnit.getNullInstance();

            ChannelChart<?> newChart = getSelectedChart();

            if(newChart != null)
            {
                unitX = newChart.getDomainDataUnit();
                unitY = newChart.getRangeDataUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();
                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, unitX, unitY);
            distanceMeasurementsModel.addDistances(measurementShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel);

        }

        checkIfActionsShouldBeEnabled();
        checkIfUndoRedoEnabled();
        checkIfUndoRedoAllEnabled();
    }

    @Override
    public void setResource(SpectroscopyProcessedResource r, Set<String> types) 
    {
        overlay(getSelectedResource(), r, types);
    }

    private void checkIfActionsShouldBeEnabled()
    {
        boolean fixAllEnabled = (!getAllNonemptyCharts(RECORDED_CURVE).isEmpty());
        fixAllContactsAllAxesAction.setEnabled(fixAllEnabled);
        fixAllContactsAllAxesAction.setEnabled(fixAllEnabled);
        fixAllContactsXAction.setEnabled(fixAllEnabled);
        fixAllContactsYAction.setEnabled(fixAllEnabled);

        ChannelChart<?> curveChart = getSelectedChart(RECORDED_CURVE);
        boolean recordedCurveActionsEnabled = (curveChart != null);

        fixContactAllAxesAction.setEnabled(recordedCurveActionsEnabled);
        fixContactXAxisAction.setEnabled(recordedCurveActionsEnabled);
        fixContactYAxisAction.setEnabled(recordedCurveActionsEnabled);

        correctContactPointAction.setEnabled(recordedCurveActionsEnabled);
        correctAdhesionAction.setEnabled(recordedCurveActionsEnabled);
        addAdhesionMeasurementAction.setEnabled(recordedCurveActionsEnabled);

        ChannelChart<?> selectedChart = getSelectedChart(); 
        boolean chartSelected = (selectedChart != null);

        enableStandByActions(chartSelected);

        boolean overlayEnabled = chartSelected && getResourceCount() > 1;

        overlayAction.setEnabled(overlayEnabled);

        boolean sourcesFromMap = getResourceModel().isSelectedSourcesFromMap();
        setMarkSourcesEnabled(sourcesFromMap);
    }

    //standBy actions are the ones that can always be fired, provided that there is a selected chart 
    private void enableStandByActions(boolean enabled) 
    {
        recalculateAction.setEnabled(enabled);
        correctTransitionIndentationAction.setEnabled(enabled);

        cropAction.setEnabled(enabled);
        translateAction.setEnabled(enabled);

        medianFilterAction.setEnabled(enabled);
        medianWeightedFilterAction.setEnabled(enabled);
        gaussianFilterAction.setEnabled(enabled);
        savitzkyGolayFilterAction.setEnabled(enabled);
        localRegressionAction.setEnabled(enabled);
        convolveAction.setEnabled(enabled);
    }

    @Override
    public void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew)
    {
        super.setConsistentWithMode(modeOld, modeNew);

        boolean isCorrectContact = modeNew.isMoveDataItems(Datasets.CONTACT_POINT);
        boolean isCorrectTransitionIndentation = modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT)|| modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT_FORCE_CURVE) || modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE) || modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS);
        boolean isCorrectAdhesion = modeNew.isMoveDataItems(Datasets.ADHESION_FORCE);
        boolean isAddAdhesion = modeNew.isDrawDataset(Datasets.ADHESION_FORCE);

        correctContactPointAction.putValue(Action.SELECTED_KEY, isCorrectContact);
        correctTransitionIndentationAction.putValue(Action.SELECTED_KEY, isCorrectTransitionIndentation);
        correctAdhesionAction.putValue(Action.SELECTED_KEY, isCorrectAdhesion);
        addAdhesionMeasurementAction.putValue(Action.SELECTED_KEY, isAddAdhesion);
    }

    @Override
    public void handleChangeOfData(Map<String, Channel1D> channelsChanged, String type, SpectroscopyProcessedResource resource)
    {                        
        ChannelChart<?> chart = getChart(resource, type);

        for(Object key : channelsChanged.keySet()) 
        {            
            chart.notifyOfDataChange(key);
        }

        DataChangeEvent<String> event = new DataChangeEvent<>(this, channelsChanged.keySet());
        fireDataChangeEvent(event); 
    }

    private class JumpToResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JumpToResultsAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_J);
            putValue(NAME,"Jump to results");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {   
            jumpToAllResults();
        };
    }

    private class MarkSourcePositionsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public MarkSourcePositionsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/MarkOnMap.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_M);
            putValue(SHORT_DESCRIPTION, "Mark on map");
            putValue(NAME,"Mark on map");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {     
            markAllSourcePositions();
        };
    }

    private class OverlayAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OverlayAction() 
        {           
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Overlay");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            overlay();
        }
    }

    private void performTransformation(Channel1DDataTransformation tr, String type, SpectroscopyProcessedResource resource)
    {
        UndoableCurveCommand<SpectroscopyProcessedResource> command = new UndoableCurveCommand<>(Channel1DResultsView.this, type, resource, tr, null);

        command.execute();
        resource.pushCommand(RECORDED_CURVE, command);    

        refreshUndoRedoOperations();
    }

    private class FixContactAllAxesAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixContactAllAxesAction() 
        {
            putValue(NAME, "All axes");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            SpectroscopyProcessedResource resource = getSelectedResource();
            Optional<Channel1DDataTransformation> optionalOfTr = resource.getFixContactAllAxesTransformationIfPossible();

            optionalOfTr.ifPresent(tr -> performTransformation(tr, RECORDED_CURVE, resource));       
        }
    }

    private class FixContactXAxisAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixContactXAxisAction() 
        {
            putValue(NAME, "Domain axis");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            SpectroscopyProcessedResource resource = getSelectedResource();
            Optional<Channel1DDataTransformation> optionalOfTr = resource.getFixContactXTransformationIfPossible();

            optionalOfTr.ifPresent(tr -> performTransformation(tr, RECORDED_CURVE, resource));        
        }
    }

    private class FixContactYAxisAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixContactYAxisAction() 
        {
            putValue(NAME, "Range axis");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            SpectroscopyProcessedResource resource = getSelectedResource();
            Optional<Channel1DDataTransformation> optionalOfTr = resource.getFixContactYTransformationIfPossible();

            optionalOfTr.ifPresent(tr -> performTransformation(tr, RECORDED_CURVE, resource));        
        }
    }

    private class FixAllContactsXAxisAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixAllContactsXAxisAction() 
        {
            putValue(NAME, "Domain axis");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            List<SpectroscopyProcessedResource> resorcesToProcess = getResources();

            List<UndoableBasicCommand<SpectroscopyProcessedResource, Channel1D, ?, ?>> commands = new ArrayList<>();

            CommandIdentifier compundCommandId = new CommandIdentifier();

            for(SpectroscopyProcessedResource resource : resorcesToProcess)
            {
                Optional<Channel1DDataTransformation> optionalOfTr = resource.getFixContactXTransformationIfPossible();

                if(optionalOfTr.isPresent())
                {
                    Channel1DDataTransformation tr = optionalOfTr.get();
                    commands.add(new UndoableCurveCommand<>(Channel1DResultsView.this, RECORDED_CURVE, resource, tr, compundCommandId));
                }
            }

            ConcurrentTransformationTask<SpectroscopyProcessedResource,?> task = new ConcurrentTransformationTask<>(Channel1DResultsView.this, commands);
            task.execute();
        }
    }

    private class FixAllContactsYAxisAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixAllContactsYAxisAction() 
        {
            putValue(NAME, "Range axis");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            List<SpectroscopyProcessedResource> resorcesToProcess = getResources();

            List<UndoableBasicCommand<SpectroscopyProcessedResource, Channel1D, ?, ?>> commands = new ArrayList<>();

            CommandIdentifier compundCommandId = new CommandIdentifier();

            for(SpectroscopyProcessedResource resource : resorcesToProcess)
            {
                Optional<Channel1DDataTransformation> optionalOfTr = resource.getFixContactYTransformationIfPossible();
                if(optionalOfTr.isPresent())
                {
                    Channel1DDataTransformation tr = optionalOfTr.get();
                    commands.add(new UndoableCurveCommand<>(Channel1DResultsView.this, RECORDED_CURVE, resource, tr, compundCommandId));
                }
            }

            ConcurrentTransformationTask<SpectroscopyProcessedResource,?> task = new ConcurrentTransformationTask<>(Channel1DResultsView.this, commands);
            task.execute();
        }
    }

    private class FixAllContactsAllAxesAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public FixAllContactsAllAxesAction() 
        {
            putValue(NAME, "All axes");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            List<SpectroscopyProcessedResource> resorcesToProcess = getResources();

            List<UndoableBasicCommand<SpectroscopyProcessedResource, Channel1D, ?, ?>> commands = new ArrayList<>();

            CommandIdentifier compundCommandId = new CommandIdentifier();

            for(SpectroscopyProcessedResource resource : resorcesToProcess)
            {
                Optional<Channel1DDataTransformation> optionalOfTr = resource.getFixContactAllAxesTransformationIfPossible();

                if(optionalOfTr.isPresent())
                {
                    Channel1DDataTransformation tr = optionalOfTr.get();
                    commands.add(new UndoableCurveCommand<>(Channel1DResultsView.this, RECORDED_CURVE, resource, tr, compundCommandId));
                }
            }

            ConcurrentTransformationTask<SpectroscopyProcessedResource,?> task = new ConcurrentTransformationTask<>(Channel1DResultsView.this, commands);
            task.execute();
        }
    }

    private class ConvertToSeparationAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ConvertToSeparationAction() 
        {
            putValue(NAME, "Current curve");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            SpectroscopyProcessedResource resource = getSelectedResource();
            Optional<Channel1DDataTransformation> optionalOfTr = resource.getTransformationToForceSeparationIfPossible();

            optionalOfTr.ifPresent(tr -> performTransformation(tr, RECORDED_CURVE, resource));            
        }
    }

    private class ConvertAllToSeparationAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ConvertAllToSeparationAction() 
        {
            putValue(NAME, "All curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            List<SpectroscopyProcessedResource> resorcesToProcess = getResources();

            List<UndoableBasicCommand<SpectroscopyProcessedResource, Channel1D, ?, ?>> commands = new ArrayList<>();

            CommandIdentifier compundCommandId = new CommandIdentifier();

            for(SpectroscopyProcessedResource resource : resorcesToProcess)
            {
                Optional<Channel1DDataTransformation> optionalOfTr = resource.getTransformationToForceSeparationIfPossible();
                if(optionalOfTr.isPresent())
                {
                    Channel1DDataTransformation tr = optionalOfTr.get();
                    commands.add(new UndoableCurveCommand<>(Channel1DResultsView.this, RECORDED_CURVE, resource, tr, compundCommandId));
                }
            }

            ConcurrentTransformationTask<SpectroscopyProcessedResource,?> task = new ConcurrentTransformationTask<>(Channel1DResultsView.this, commands);
            task.execute();
        }
    }


    private class RecalculateAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RecalculateAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/RecalculateCurves.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(SHORT_DESCRIPTION, "Recalculate");
            putValue(NAME, "Recalculate");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            recalculateFullDialog();
        }
    }

    private void recalculateFullDialog()
    {
        recalculateDialog.showDialog(getResourceModel().getRecalculationModel());
    }


    private class CorrectContactPointAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CorrectContactPointAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/CorrectContactPoint.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Correct contact point");
            putValue(NAME, "Correct contact point");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ? new DataModificationMouseInputMode(Datasets.CONTACT_POINT, Collections.singleton(DataModificationType.POINT_MOVEABLE)) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class CorrectTransitionIndentationAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CorrectTransitionIndentationAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/CorrectTransitionPoint.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Correct transition point");
            putValue(NAME, "Correct transition point");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ? new DataModificationMouseInputMode(Arrays.asList(Datasets.MODEL_TRANSITION_POINT_FORCE_CURVE,Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE,Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS), Collections.singleton(DataModificationType.POINT_MOVEABLE)) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class CorrectAdhesionAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CorrectAdhesionAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/CorrectAdhesion.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Correct adhesion");
            putValue(NAME, "Correct adhesion");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  new DataModificationMouseInputMode(Datasets.ADHESION_FORCE, Collections.unmodifiableSet(EnumSet.of(DataModificationType.POINT_MOVEABLE, DataModificationType.WHOLE_DATASET_MOVEABLE))) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class AddAdhesionMeasurementAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public AddAdhesionMeasurementAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/AddAdhesion.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Add adhesion measurement");
            putValue(NAME, "Add adhesion");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ? new DataDrawingMouseInputMode(Datasets.ADHESION_FORCE, new VerticalModificationConstraint1D(), 2) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    //copied
    @Override
    public List<? extends SpectroscopyProcessedResource> getAdditionalResources() 
    {
        return Collections.emptyList();
    }

    //copied
    @Override
    public GraphicalResultsViewModel getResourceModel()
    {
        return (GraphicalResultsViewModel)super.getResourceModel();
    }

    //copied
    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers();
    }

    //copied
    @Override
    public Set<String> getAllResourcesChannelIdentifiers()
    {
        return getResourceModel().getAllResourcesChannelIdentifiers();
    }

    //copied
    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(filter);
    }

    //copied
    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type);
    }

    //copied
    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(filter);
    }

    //copied

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type, filter);
    }

    //copied
    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public void addResourceSelectionListener(SelectionListener<? super SpectroscopyProcessedResource> listener)
    {
        getResourceModel().addSelectionListener(listener);
    }

    @Override
    public void removeResourceSelectionListener(SelectionListener<? super SpectroscopyProcessedResource> listener)
    {
        getResourceModel().removeSelectionListener(listener);
    }

    @Override
    public void addResourceDataListener(ResourceGroupListener<? super SpectroscopyProcessedResource> listener)
    {
        getResourceModel().addDataModelListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super SpectroscopyProcessedResource> listener)
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

    //copied
    @Override
    public PrefixedUnit getDataUnit() 
    {
        SpectroscopyResource resource = getSelectedResource();
        String type = getSelectedType();
        return (resource != null) ? resource.getSingleDataUnit(type) : null;    
    }

    //copied
    @Override
    public PrefixedUnit getDisplayedUnit() 
    {
        ChannelChart<?> chart = getSelectedChart();
        PrefixedUnit axisUnit = (chart != null) ? chart.getRangeDisplayedUnit() : null;

        return axisUnit;
    }

    //copied
    protected PrefixedUnit getXAxisDisplayedUnit()
    {
        ChannelChart<?> chart = getSelectedChart();
        PrefixedUnit axisUnit = (chart != null) ? chart.getDomainDisplayedUnit() : null;

        return axisUnit;
    }

    protected PrefixedUnit getXAxisDataUnit()
    {
        ChannelChart<?> chart = getSelectedChart();
        PrefixedUnit axisUnit = (chart != null) ? chart.getDomainDataUnit() : null;

        return axisUnit;
    }

    //copied
    @Override
    public UnitManager getUnitManager()
    {
        return new UnitManager(getDataUnit(), getDisplayedUnit());
    }

    //copied
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

    //copied
    @Override
    public ROI getROIUnion() 
    {
        return getResourceModel().getROIUnion();
    }

    //copied
    @Override
    public Map<Object, ROI> getDrawableROIs() 
    {
        return Collections.emptyMap();
    }
    //copied}

    @Override
    public Map<Object, ROI> getAllROIs() 
    {
        return Collections.emptyMap();
    }

    //copied
    @Override
    public void pushCommand(SpectroscopyProcessedResource resource, String type, UndoableCommand command)
    {
        resource.pushCommand(type, command);
        checkIfUndoRedoEnabled();   
        checkIfUndoRedoAllEnabled();
    }

    @Override
    public void pushCommands(MetaMap<SpectroscopyProcessedResource, String, UndoableCommand> commands)
    {
        getResourceModel().pushCommands(commands);
        checkIfUndoRedoEnabled();      
        checkIfUndoRedoAllEnabled();
    }

    @Override
    public void notifyToolsOfMouseClicked(CustomChartMouseEvent evt) {        
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
        checkIfUndoRedoAllEnabled();
    }

    private void checkIfUndoRedoEnabled()
    {
        GraphicalResultsViewModel model = getResourceModel();

        boolean redoEnabled = model.canRedoBeEnabled();
        boolean undoEnabled = model.canUndoBeEnabled();

        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);
    } 

    private void checkIfUndoRedoAllEnabled()
    {
        GraphicalResultsViewModel model = getResourceModel();

        boolean redoAllEnabled = model.canRedoAllBeEnabled();
        boolean undoAllEnabled = model.canUndoAllBeEnabled();

        undoAllAction.setEnabled(undoAllEnabled);
        redoAllAction.setEnabled(redoAllEnabled);
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
            ChannelResource<Channel1D, Channel1DData, String> resource = getSelectedResource();
            if(resource != null)
            {
                resource.undo(getSelectedType());
                checkIfUndoRedoEnabled();
                checkIfUndoRedoAllEnabled();
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
            getResourceModel().undoAll(Channel1DResultsView.this);
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
            ChannelResource<Channel1D, Channel1DData, String> resource = getSelectedResource();
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
            getResourceModel().redoAll(Channel1DResultsView.this);
        }
    }
}
