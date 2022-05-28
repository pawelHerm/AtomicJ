package atomicJ.gui.stack;

import static atomicJ.gui.stack.StackModel.FRAME_INDEX;

import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel2D;
import atomicJ.data.Datasets;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.gui.Channel1DDataset;
import atomicJ.gui.Channel1DRenderer;
import atomicJ.gui.Channel2DDataset;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.ColorGradientReceiver;
import atomicJ.gui.CustomChartMouseEvent;
import atomicJ.gui.CustomizableValueMarker;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.ChannelRenderer;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.Channel2DPanel;
import atomicJ.gui.Channel2DPlot;
import atomicJ.gui.Channel2DSupervisor;
import atomicJ.gui.DistanceGeometryTableModel;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.FlexibleNumericalTableView;
import atomicJ.gui.GradientPaintReceiver;
import atomicJ.gui.GradientSelectionDialog;
import atomicJ.gui.Channel2DReceiver;
import atomicJ.gui.ImageChart;
import atomicJ.gui.ImagePlot;
import atomicJ.gui.MapMarker;
import atomicJ.gui.MinimalNumericalTable;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.MouseInteractiveTool;
import atomicJ.gui.PreferredContinuousSeriesRendererStyle;
import atomicJ.gui.MouseInputType;
import atomicJ.gui.PreferredStandardRoamingLegendTitleStyle;
import atomicJ.gui.ProcessableXYZDataset;
import atomicJ.gui.RangeGradientChooser;
import atomicJ.gui.RangeHistogramView;
import atomicJ.gui.ResourceXYView;
import atomicJ.gui.RoamingLegend;
import atomicJ.gui.RoamingStandardTitleLegend;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.StandardStyleTag;
import atomicJ.gui.StyleTag;
import atomicJ.gui.MouseInteractiveTool.MouseInteractiveToolListener;
import atomicJ.gui.annotations.ExportAnnotationModel;
import atomicJ.gui.annotations.ExportAnnotationWizard;
import atomicJ.gui.editors.ROIMaskEditor;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionSettingsView;
import atomicJ.gui.profile.CrossSectionSettingsReceiver;
import atomicJ.gui.profile.CrossSectionsRenderer;
import atomicJ.gui.profile.KnobSpecification;
import atomicJ.gui.profile.Profile;
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
import atomicJ.gui.rois.ROIProxy;
import atomicJ.gui.rois.ROIShapeFactorsTableModel;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.gui.rois.SplitROIsAction;
import atomicJ.gui.rois.WandContourTracer;
import atomicJ.gui.rois.WandROIDialog;
import atomicJ.gui.rois.WandROIModel;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.resources.CrossSectionSettings;
import atomicJ.resources.CrossSectionSettingsManager;
import atomicJ.resources.SimpleResource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MetaMap;



public class FlexibleSectionView extends ResourceXYView<SimpleResource, Channel2DChart<?>, Channel2DPanel<Channel2DChart<?>>> implements Channel2DSupervisor, PropertyChangeListener
{
    private static final double TOLERANCE = 1e-12;

    private static final String STACK_LEVEL_MARKER = "Stack level marker";

    private static final int DEFAULT_HEIGHT = (int)Math.round(0.5*Toolkit.getDefaultToolkit().getScreenSize().height);
    private static final int DEFAULT_WIDTH = (int)Math.min(0.85*Toolkit.getDefaultToolkit().getScreenSize().height, Toolkit.getDefaultToolkit().getScreenSize().width);

    private static final Preferences PREF = Preferences.userNodeForPackage(FlexibleSectionView.class).node(FlexibleSectionView.class.getName()).node(Datasets.STACK_SLICE);
    private static final PreferredStandardRoamingLegendTitleStyle LEGEND_STYLE = PreferredStandardRoamingLegendTitleStyle.getInstance(PREF, Datasets.STACK_SLICE);

    private final ShowShapeFactorsForRois showROIShapeFactorsAction = new ShowShapeFactorsForRois();

    private final Action modifyROIStyleAction = new ModifyROIStyleAction();
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

    private final Action editGradientAction = new EditColorGradientAction();
    private final Action editHistogramGradientAction = new EditHistogramGradientAction(); 
    private final Action roiMaskAction = new ROIMaskAction();

    private final Action overlayAction = new OverlayAction();

    private final Action changeSectionSectionSettingsAction = new ChangeSectionSettingsAction();
    private final Action changeOverlaySectionSettingsAction = new ChangeOverlaySettingsAction();

    private final Action addDomainMarkerAction = new AddDomainMarkerAction();

    private final Map<Object, List<CustomizableValueMarker>> domainMarkers = new LinkedHashMap<>();
    private final Map<Object, CustomizableValueMarker> rangeMarkers = new LinkedHashMap<>();

    //MODEL

    private final CrossSectionSettingsManager sectionSettingsManager = new CrossSectionSettingsManager();
    private final CrossSectionSettingsManager overlaySettingsManager = new CrossSectionSettingsManager();

    private final Map<String, Map<Object, DistanceMeasurementDrawable>> distanceMeasurements = new LinkedHashMap<>();
    private final MetaMap<String, Object, ROIDrawable> rois = new MetaMap<>();
    private final List<ROIReceiver> roiReceivers = new ArrayList<>();

    private MouseInteractiveTool mouseInteractiveTool;
    private final CustomMouseInteractiveToolListener mouseInteractiveToolListener = new CustomMouseInteractiveToolListener();

    //////////////////////////////

    private final String sourceName;

    private final StackView<?,?,?,?> stackView;

    private final FlexibleNumericalTableView distanceMeasurementsDialog;
    private final FlexibleNumericalTableView roiShapeFactorsDialog = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new ROIShapeFactorsTableModel(Units.MICRO_METER_UNIT), true, true), "ROI shape factors");

    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(getAssociatedWindow());

    private final ROIEditor roiEditor = new ROIEditor(getAssociatedWindow());
    private ROIMaskEditor roiMaskEditor;

    private final WandROIDialog wandROIDialog = new WandROIDialog(getAssociatedWindow(), "Wand ROI", false);
    private final WandROIModel wandROIModel = new WandROIModel(this);

    private MultipleSelectionWizard<IdentityTag> roiMergeSelectionWizard;
    private DifferenceROIWizard roiDifferenceWizard;
    private ComplementROIWizard complementROIWizard;
    private ModifyObjectsWizard roiConvexHullWizard;
    private ExportAnnotationWizard exportAnnotationWizard;

    private GradientSelectionDialog gradientSelectionDialog;
    private RangeGradientChooser rangeGradientChooser;
    private RangeHistogramView rangeGradientHistogramChooser;

    private final MultipleSelectionWizard<String> overlayWizard = new MultipleSelectionWizard<>(getAssociatedWindow(), "Overlay assistant");
    private final MultipleSelectionWizardPageModel<String> overlayModel;
    private Set<String> overlayChannels = new LinkedHashSet<>(); 

    private final ExtensionFileChooser roiImportChooser;

    private final SectionSettingsReceiver sectionSettingsReceiver = new SectionSettingsReceiver();
    private final OverlaySectionSettingsReceiver overlaySettingsReceiver = new OverlaySectionSettingsReceiver();
    private final CrossSectionSettingsView sectionSettingsDialog = new CrossSectionSettingsView(getAssociatedWindow(), "Section interpolation");


    public FlexibleSectionView(StackView<?,?,?,?> stackView, ModalityType modalityType, SimpleResource resource) 
    {
        super(stackView.getAssociatedWindow(), Channel2DPanel.DensityPanelFactory.getInstance(true, false, false), "Flexible sections", PREF, modalityType, true, true);

        this.stackView = stackView;

        Set<String> allIdentifiers = stackView.getStackModel().getCorrespondingResource().getAllIdentifiers();
        this.overlayModel =  new BasicMultipleSelectionWizardPageModel<>(allIdentifiers, "Map profiles to overlay", "Channel selection", "Select the map profiles you want to overlay", false, true, true);

        this.roiImportChooser = new ExtensionFileChooser(PREF, "ROI file (.roi)", "roi", true);

        this.roiReceivers.add(wandROIModel);

        JTabbedPane tabbedPane = getTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setCenterComponent(tabbedPane);
        setSouthComponent(getMultipleResourcesPanelButtons());

        this.sourceName = resource.getShortName();
        addResource(resource, new LinkedHashMap<String,Channel2DChart<?>>());
        selectResource(resource);

        StackModel<?> parentStackModel = stackView.getStackModel();
        parentStackModel.addPropertyChangeListener(this);

        File defaultOutputDirectory = resource.getDefaultOutputLocation();
        PrefixedUnit xUnit = parentStackModel.getXQuantity().getUnit();
        PrefixedUnit yUnit = parentStackModel.getStackingQuantity().getUnit();
        this.distanceMeasurementsDialog = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(defaultOutputDirectory, xUnit, yUnit), true, true), "Flexible sections - distance measurements");

        buildMenuBar();

        updateROIAvailabilityPrivate();
    }

    private void buildMenuBar()
    {
        JMenuItem editGradientItem = new JMenuItem(editGradientAction);
        JMenuItem editHistogramGradientItem = new JMenuItem(editHistogramGradientAction);
        JMenuItem roiMaskItem = new JMenuItem(roiMaskAction);

        JMenuItem modifyROIStyleItem = new JMenuItem(modifyROIStyleAction);
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
        JCheckBoxMenuItem rotateROIItem = new JCheckBoxMenuItem(rotateROIAction);

        JMenuItem overlayItem = new JMenuItem(overlayAction);

        JMenuItem changeSectionSettingsItem = new JMenuItem(changeSectionSectionSettingsAction);
        JMenuItem changeOverlaySectionSettingsItem = new JMenuItem(changeOverlaySectionSettingsAction);
        JMenuItem addDomainMarkerItem = new JCheckBoxMenuItem(addDomainMarkerAction);

        JMenu chartMenu = getChartMenu();

        chartMenu.add(editGradientItem);
        chartMenu.add(editHistogramGradientItem);        

        JMenu roisMenu = new JMenu("ROI");
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

        JMenu overlayMenu = new JMenu("Overlay");
        overlayMenu.setMnemonic(KeyEvent.VK_O);
        overlayMenu.add(overlayItem);

        JMenu menuInterpolation = new JMenu("Interpolation");
        menuInterpolation.setMnemonic(KeyEvent.VK_I);

        menuInterpolation.add(changeSectionSettingsItem);
        menuInterpolation.add(changeOverlaySectionSettingsItem);

        JMenu menuMarkers = new JMenu("Markers");
        menuMarkers.add(addDomainMarkerItem);
        menuMarkers.setMnemonic(KeyEvent.VK_K);

        JMenuBar menuBar = getMenuBar();
        menuBar.add(roisMenu);
        menuBar.add(overlayMenu);
        menuBar.add(menuInterpolation);
        menuBar.add(menuMarkers);
    }

    @Override
    public void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew)
    {
        super.setConsistentWithMode(modeOld, modeNew);

        boolean isPolygonROI = MouseInputModeStandard.POLYGON_ROI.equals(modeNew);
        boolean isRectangularROI = MouseInputModeStandard.RECTANGULAR_ROI.equals(modeNew);
        boolean isElipticROI = MouseInputModeStandard.ELIPTIC_ROI.equals(modeNew);
        boolean isFreeHandROI = MouseInputModeStandard.FREE_HAND_ROI.equals(modeNew);
        boolean isWandROI = MouseInputModeStandard.WAND_ROI.equals(modeNew);

        roiPolygonalAction.putValue(Action.SELECTED_KEY, isPolygonROI);
        roiRectangularAction.putValue(Action.SELECTED_KEY, isRectangularROI);
        roiElipticAction.putValue(Action.SELECTED_KEY, isElipticROI);
        roiFreeAction.putValue(Action.SELECTED_KEY, isFreeHandROI);
        roiWandAction.putValue(Action.SELECTED_KEY, isWandROI);

        boolean wandModeAcquired = !MouseInputModeStandard.WAND_ROI.equals(modeOld)
                && isWandROI;

        boolean wandModeLost = MouseInputModeStandard.WAND_ROI.equals(modeOld)
                && !isWandROI;


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

    private void showWandROIDialog()
    {
        Channel2DChart<?> chart = getSelectedChart();
        updateWandROIUnits(chart);
        wandROIDialog.showDialog(wandROIModel);
    }

    @Override
    public WandContourTracer getWandTracer()
    {
        return wandROIModel.getContourTracer();
    }

    @Override
    public void handleNewChartPanel(Channel2DPanel<Channel2DChart<?>> panel)
    {
        panel.setDensitySupervisor(this);        
    }

    public void setAddDomainMarkersMode(boolean add)
    {
        MouseInputMode inputMode = add ? MouseInputModeStandard.INSERT_DOMAIN_MARKER : MouseInputModeStandard.NORMAL;        
        setMode(inputMode);
    }

    @Override
    protected int getDefaultWidth()
    {
        return DEFAULT_WIDTH;
    }

    @Override
    protected int getDefaultHeight()
    {
        return DEFAULT_HEIGHT;
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty) {
        super.controlForSelectedChartEmptiness(empty);

        boolean enabled = !empty;
        enableAllActionsFlexibleSection(enabled);
    }

    private void enableAllActionsFlexibleSection(boolean enabled) 
    {
        editGradientAction.setEnabled(enabled);
        editHistogramGradientAction.setEnabled(enabled);
    }

    @Override
    protected void showMeasurementEditor()
    {
        measurementEditor.setVisible(true);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {
        String selectedType = getSelectedType();
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(selectedType);        

        if(measurtementsForType == null)
        {   
            measurtementsForType = new LinkedHashMap<>();
            distanceMeasurements.put(selectedType, measurtementsForType);
        }       

        measurtementsForType.put(measurement.getKey(), measurement);

        boolean measurementsAvailable = getMode().isMeasurement();
        updateMeasurementsAvailability(measurementsAvailable);

        Channel2DPanel<Channel2DChart<?>> selectedPanel = getSelectedPanel();
        selectedPanel.addOrReplaceDistanceMeasurement(measurement);        

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
        model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {                
        String selectedType = getSelectedType();
        Map<Object, DistanceMeasurementDrawable> measurementsForType = distanceMeasurements.get(selectedType);

        if(measurementsForType != null)
        {
            measurementsForType.remove(measurement.getKey());

            int measurementsCount = measurementsForType.size();
            boolean measurementsAvailable = (getMode().isMeasurement() && measurementsCount>0);
            updateMeasurementsAvailability(measurementsAvailable);

            Channel2DPanel<Channel2DChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.removeDistanceMeasurement(measurement);

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }
    }

    public void removeProfileSection(Object profileKey)
    {        
        removeChartPanel(profileKey.toString());
        removeDomainMarkers(profileKey);
    }

    public void addOrReplaceProfileSection(final Profile profile)
    {
        final Object profileKey = profile.getKey();
        sectionSettingsManager.addTypeIfNecessary(profileKey.toString());
        overlaySettingsManager.addTypeIfNecessary(profileKey.toString());

        addChartPanelIfAbsent(profileKey.toString());

        final boolean profileIsNew = !containsType(profileKey.toString());

        CrossSectionSettings settings = sectionSettingsManager.getCrossSectionSettings(profileKey.toString());

        if(profileIsNew)
        {
            stackView.getSectionParallelized(profileKey, settings, new Channel2DReceiver() 
            {         
                @Override
                public void setGridChannel(Channel2D channel) 
                {
                    setSectionData(profile, profileIsNew, channel);          
                }

                @Override
                public Window getParent() 
                {
                    return getAssociatedWindow();
                }
            });
        }
        else 
        {
            Channel2D channel = stackView.getSection(profileKey,  settings);
            setSectionData(profile, profileIsNew, channel);          
        }  
    }

    private void setSectionData(Profile profile, boolean newProfile, Channel2D channel)
    {
        String type = profile.getKey().toString();

        Channel2DPanel<Channel2DChart<?>> panel = getPanel(type);

        if(panel == null)
        {
            return;
        }

        Channel2DChart<?> chart = panel.getSelectedChart();

        if(chart != null)
        {
            Channel2DPlot plot = chart.getCustomizablePlot();

            ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, sourceName);

            String layerKey = channel.getIdentifier() + sourceName;
            plot.replaceLayerDataset(layerKey, dataset); 
        }
        else
        {
            chart = createChart(type, channel);

            CustomizableValueMarker forceLevelMarker =
                    new CustomizableValueMarker(STACK_LEVEL_MARKER, stackView.getStackModel().getCurrentStackingValue(), PREF, 1.0f, STACK_LEVEL_MARKER);
            chart.addRangeValueMarker(forceLevelMarker, Layer.FOREGROUND);           

            rangeMarkers.put(type, forceLevelMarker);

            panel.setSelectedChart(chart);
        }

        //refreshes overlay
        Map<String, Map<Object, ChannelSectionLine>> crossSections = stackView.getCrossSections(profile, overlayChannels);
        setOverlayCrossSections(crossSections);
        refreshDomainMarkers(profile);

        chart.setUseFixedChartAreaSize(false);

        setSelectedType(type);
        setVisible(true);   
    }

    private Channel2DChart<?> createChart(String profileKey, Channel2D channel)
    {
        ImagePlot plot = new ImagePlot(Channel2DDataset.getDataset(channel, sourceName),  Datasets.STACK_SLICE,  Datasets.DENSITY_PLOT, PREF, Datasets.STACK_SLICE);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.getRenderer(0).setBaseSeriesVisibleInLegend(false);    

        ChannelRenderer renderer = plot.getRenderer();
        if(renderer instanceof GradientPaintReceiver)
        {
            GradientPaintReceiver receiver = stackView.getGradientPaintReceiver();
            if(receiver != null)
            {
                ColorGradient gradient = receiver.getColorGradient();
                double lowerBound = receiver.getLowerBound();
                double upperBound = receiver.getUpperBound();
                ((GradientPaintReceiver)renderer).setColorGradient(gradient);
                ((GradientPaintReceiver)renderer).setLowerBound(lowerBound);
                ((GradientPaintReceiver)renderer).setUpperBound(upperBound);
            }
        }
        Channel2DChart<?> chart = new ImageChart<>(plot,  Datasets.DENSITY_PLOT);

        RoamingLegend legend = new RoamingStandardTitleLegend("Legend",chart.createDefaultLegend(), LEGEND_STYLE);
        chart.addRoamingSubLegend(legend);
        return chart;
    }

    private void selectTypesAndOverlay()
    {
        boolean approved = overlayWizard.showDialog(overlayModel);
        if (approved) 
        {
            overlayChannels = overlayModel.getSelectedKeys();

            Map<String, Map<Object, ChannelSectionLine>> crossSections = stackView.getCrossSections(overlayChannels);       

            clearOverlayCrossSections(overlayModel.getLeftOutKeys());
            setOverlayCrossSections(crossSections);
        }
    }

    public void refreshOverlayCrossSections()
    {
        Map<String, Map<Object, ChannelSectionLine>> crossSections = stackView.getCrossSections(overlayChannels);       

        clearOverlayCrossSections(overlayModel.getLeftOutKeys());
        setOverlayCrossSections(crossSections);
    }

    private void clearOverlayCrossSections(Set<String> overlaysToClear)
    {
        for(String sectionType: getTypes())
        {
            Channel2DChart<?> chart = getChart(sectionType);
            Channel2DPlot plot = chart.getCustomizablePlot();

            for(String overlayType : overlaysToClear)
            {
                plot.removeLayer(overlayType, true);
            }
        }
    }

    private void setOverlayCrossSections(Map<String, Map<Object, ChannelSectionLine>> crossSections)
    {
        for(Entry<String, Map<Object, ChannelSectionLine>> entry: crossSections.entrySet())
        {
            String mapType = entry.getKey();

            Map<Object, ChannelSectionLine> newCrossSection = entry.getValue();

            for(Entry<Object, ChannelSectionLine> innerEntry : newCrossSection.entrySet())
            {
                String type = innerEntry.getKey().toString();
                ChannelSectionLine crossSection = innerEntry.getValue();

                CrossSectionSettings settings = overlaySettingsManager.getCrossSectionSettings(type);
                Channel1D curve = crossSection.getCrossSection(settings);

                Channel2DChart<?> chart = getChart(type);
                Channel2DPlot plot = chart.getCustomizablePlot();

                XYDataset dataset = new Channel1DDataset(curve, mapType);                    

                if(plot.containsLayer(mapType))
                {
                    plot.replaceLayerDataset(mapType, dataset);
                }
                else
                {
                    StyleTag styleTag = new StandardStyleTag(mapType);
                    Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(styleTag.getPreferredStyleKey());
                    PreferredContinuousSeriesRendererStyle prefStyle = PreferredContinuousSeriesRendererStyle.getInstance(pref, styleTag);
                    CrossSectionsRenderer renderer = new CrossSectionsRenderer(prefStyle, mapType, styleTag, mapType);
                    plot.addOrReplaceLayerWithOwnAxis(mapType, dataset, renderer, curve.getYQuantity());
                }
            }
        }
    }

    public void changeSectionSettings()
    {
        this.sectionSettingsDialog.showDialog(sectionSettingsReceiver);
    }

    public void changeOverlaySettings()
    {
        this.sectionSettingsDialog.showDialog(overlaySettingsReceiver);
    }

    public void refreshOverlaysForProfile(String type)
    {
        Object profileKey = Integer.parseInt(type);

        Map<String, Map<Object, ChannelSectionLine>> crossSections = stackView.getCrossSections(profileKey, overlayChannels);       

        clearOverlayCrossSections(overlayModel.getLeftOutKeys());
        setOverlayCrossSections(crossSections);
    }

    public void refreshSection(String type)
    {
        Channel2DPanel<Channel2DChart<?>> panel = getPanel(type);

        Channel2DChart<?> chart = panel.getSelectedChart();

        if(chart != null)
        {
            final Channel2DPlot plot = chart.getCustomizablePlot();

            CrossSectionSettings settings = sectionSettingsManager.getCrossSectionSettings(type);


            stackView.getSectionParallelized(Integer.parseInt(type), settings, new Channel2DReceiver() {

                @Override
                public void setGridChannel(Channel2D channel) 
                {
                    ProcessableXYZDataset dataset = Channel2DDataset.getDataset(channel, sourceName);
                    plot.replaceLayerDataset(channel.getIdentifier() + sourceName, dataset);
                }

                @Override
                public Window getParent() {
                    return getAssociatedWindow();
                }
            });

        }
    }

    @Override
    protected void close() 
    {
        setVisible(false);
    }

    public void lockAspectRatio(boolean lock) 
    {
        for (Channel2DPanel<?> panel : getPanels()) 
        {
            Channel2DChart<?> chart = panel.getSelectedChart();
            if (chart != null) 
            {
                chart.setUseFixedChartAreaSize(lock);
            }
        }
    }

    public void moveDomainMarker(Object profileKey, int markerIndex, double positionNew)
    {
        List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);

        if(markersForType != null)
        {
            int n = markersForType.size();

            if(markerIndex >= 0 && markerIndex <n)
            {
                CustomizableValueMarker marker = markersForType.get(markerIndex);
                marker.setValue(positionNew);          
            }
        }    

        setSelectedType(profileKey.toString());
    }


    public void removeDomainMarker(Object profileKey, double markerPosition)
    {
        List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);

        if(markersForType != null)
        {
            CustomizableValueMarker removedMarker = null;

            for(CustomizableValueMarker marker : markersForType)
            {
                double currentMarkerPosition = marker.getValue();

                boolean found = Math.abs(currentMarkerPosition - markerPosition) < TOLERANCE;
                if(found)
                {
                    removedMarker = marker;
                    break;
                }
            }

            if(removedMarker != null)
            {
                Channel2DChart<?> chart = getChart(profileKey);
                if(chart != null)
                {
                    chart.removeDomainValueMarker(removedMarker);
                }  

                markersForType.remove(removedMarker);               
            } 
        }       
    }

    private void updateMarkerKeys(Object profileKey, Object keyRemoved)
    {
        List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);

        if(keyRemoved instanceof Integer)
        {
            for(CustomizableValueMarker marker : markersForType)
            {
                Object key = marker.getKey();
                if(key instanceof Integer)
                {
                    Integer numberKey = (Integer)key;
                    boolean larger = numberKey.compareTo((Integer)keyRemoved)>0;

                    key = larger ? Integer.valueOf(numberKey.intValue() - 1) : key;

                    marker.setKey(key);
                }

            }           
        }
    }

    public void removeDomainMarkers(Object profileKey)
    {
        List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);

        if(markersForType != null)
        {
            for(CustomizableValueMarker marker : markersForType)
            {
                Channel2DChart<?> chart = getChart(profileKey);
                if(chart != null)
                {
                    chart.removeDomainValueMarker(marker);
                }      

            }
            markersForType.clear();  
        }       
    }

    private void refreshDomainMarkers(Profile profile)
    {
        Object profileKey = profile.getKey();

        List<Double> markerPositions = profile.getKnobPositions();
        List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);

        Channel2DChart<?> chart = getChart(profileKey);

        int n = markerPositions.size();

        if(markersForType != null)
        {
            for(int i = 0; i<n; i++)
            {
                double position = markerPositions.get(i);
                CustomizableValueMarker marker = markersForType.get(i);
                marker.setValue(position);
            }
        }
        else
        {
            markersForType = new ArrayList<>();
            domainMarkers.put(profileKey, markersForType);        

            int markerCount = markerPositions.size();
            for(int i = 0; i<markerCount; i++)
            {
                double position = markerPositions.get(i);
                KnobSpecification knobSpecification = new KnobSpecification(profileKey, i, position);

                String markerName = "Section marker " + Integer.toString(i);
                CustomizableValueMarker markerNew = 
                        new CustomizableValueMarker(markerName, position, PREF.node("MARKER"), 1.f, knobSpecification);
                markersForType.add(markerNew);
                chart.addDomainValueMarker(markerNew, Layer.FOREGROUND);
            }            
        }
    }

    public Channel2DChart<?> getChart(Object profileKey)
    {
        Channel2DChart<?> chart = null;
        Channel2DPanel<Channel2DChart<?>> panel = getPanel(profileKey.toString());
        if(panel != null)
        {
            chart = panel.getChartAt(0);
        }
        return chart;
    }

    @Override
    public void requestNewDomainMarker(double knobPosition)
    {
        String type = getSelectedType();
        Object profileKey = Integer.parseInt(type);

        stackView.addProfileKnob(profileKey, knobPosition);
    }

    public void addDomainMarker(Object profileKey, double position)
    {       
        Channel2DChart<?> chart = getChart(profileKey);
        if(chart != null)
        {           
            List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);
            if(markersForType == null)
            {
                markersForType = new ArrayList<>();
                domainMarkers.put(profileKey, markersForType);
            }

            KnobSpecification knobSpecification = new KnobSpecification(profileKey, markersForType.size(), position);

            String markerName = "Section marker " + profileKey.toString();
            CustomizableValueMarker markerNew = 
                    new CustomizableValueMarker(markerName, position, PREF.node("MARKER"), 1.f, knobSpecification);
            chart.addDomainValueMarker(markerNew, Layer.FOREGROUND);

            markersForType.add(markerNew);
        }       
    }

    public void addDomainMarkers(Object profileKey, List<Double> positions)
    {       
        Channel2DChart<?> chart = getChart(profileKey);
        if(chart != null)
        {           
            List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);
            if(markersForType == null)
            {
                markersForType= new ArrayList<>();
                domainMarkers.put(profileKey, markersForType);
            }

            int positionCount = positions.size();

            for(int i = 0; i<positionCount; i++)
            {
                double position = positions.get(i);
                KnobSpecification knobSpecification = new KnobSpecification(profileKey, markersForType.size(), position);
                String markerName = "Section marker " + Integer.toString(i);
                CustomizableValueMarker markerNew = new CustomizableValueMarker(markerName, position, PREF.node("MARKER"), 1.f, knobSpecification);
                chart.addDomainValueMarker(markerNew, Layer.FOREGROUND);

                markersForType.add(markerNew);
            }
        }       
    }

    public void setDomainMarkers(Object profileKey, List<Double> positions)
    {
        removeDomainMarkers(profileKey);
        addDomainMarkers(profileKey, positions);
    }

    @Override
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey)
    {  
        if(markerKey instanceof KnobSpecification)
        {            
            KnobSpecification knobSpecification = (KnobSpecification)markerKey;

            Object profileKey = knobSpecification.getKey();
            int index = knobSpecification.getKnobIndex();

            List<CustomizableValueMarker> markersForType = domainMarkers.get(profileKey);
            if(markersForType != null)
            {
                CustomizableValueMarker marker = markersForType.get(index);

                double d = newPosition.getX();
                marker.setValue(d);

                stackView.moveProfileKnob(profileKey, index, d);
            }
        }
        else if(STACK_LEVEL_MARKER.equals(markerKey))
        {
            double d = newPosition.getY();
            stackView.getStackModel().setFrameClosestTo(d);
        }
    } 

    @Override
    public boolean areHistogramsAvaialable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void showHistograms() {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawHistograms() {
        // TODO Auto-generated method stub
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
    public void showRawData() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showROIRawData() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showROIRawData(List<ROI> list) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showStatistics() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showROIStatistics(Map<Object, ROI> rois) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showROIStatistics() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setROIStyleEditorVisible(boolean visible) {
        // TODO Auto-generated method stub

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
    public void moveProfileKnob(Object profileKey, int knobIndex,
            double knobPositionNew) {
        // TODO Auto-generated method stub

    }


    @Override
    public void setROIs(Map<Object, ROIDrawable> rois) 
    {
        String selectedType = getSelectedType();

        this.rois.remove(selectedType);
        this.rois.putAll(selectedType, rois);

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
        String selectedType = getSelectedType();

        rois.put(selectedType, roi.getKey(), roi);

        updateROIAvailability();

        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.addOrReplaceROI(roi);
        }  

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.addOrUpdateROI(roi);

        updateROIRange(); 
    }

    @Override
    public void removeROI(ROIDrawable roi) 
    {
        String selectedType = getSelectedType();
        ROIDrawable removed = rois.remove(selectedType, roi.getKey());

        if(removed != null)
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

        Channel2DChart<?> selectedChart = getSelectedChart();
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

        String selectedType = getSelectedType();
        Map<Object, ROIDrawable> roisForType = rois.get(selectedType);

        MultipleSelectionWizardPageModel<IdentityTag> selectionModel = new BasicMultipleSelectionWizardPageModel<>(ROIUtilities.getIds(roisForType.values()), "ROIs to merge", 
                "Merge ROI", "Select which ROIs are to be merged", true, true);


        if(roiMergeSelectionWizard == null)
        {
            roiMergeSelectionWizard = new MultipleSelectionWizard<>(getAssociatedWindow(), "ROI merging");
        }

        boolean approved = roiMergeSelectionWizard.showDialog(selectionModel);

        if(approved)
        {
            Collection<ROIDrawable> selectedROIs = ROIUtilities.selectROIsFromIds(roisForType, selectionModel.getSelectedKeys());
            ROIDrawable merged = new GeneralUnionROI(selectedROIs, ROIUtilities.getUnionKey(selectedROIs), ROIUtilities.getUnionKey(selectedROIs).toString(), chart.getROIStyle());

            for(ROIDrawable roi : selectedROIs)
            {
                removeROI(roi);
            }

            addOrReplaceROI(merged);
        }
    }

    //copied from FlexibleMapSectionDialog
    public void subtractROIs()
    {
        Channel2DChart<?> chart = getSelectedChart();
        if(chart == null)
        {
            return;
        }    

        String selectedType = getSelectedType();
        Map<Object, ROIDrawable> roisForType = rois.get(selectedType);

        DifferenceROIModel<ROIDrawable> selectionModel = new DifferenceROIModel<>(ROIUtilities.findPossibleDiffrences(roisForType.values()), true, true);

        if(roiDifferenceWizard == null)
        {
            roiDifferenceWizard = new DifferenceROIWizard(getAssociatedWindow(), "ROI subtraction");
        }

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

    public void complementROIs()
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        String selectedType = getSelectedType();
        Map<Object, ROIDrawable> roisForType = rois.get(selectedType);

        Set<ROIDrawable> roisSet = new LinkedHashSet<>(roisForType.values());

        ComplementROIModel<ROIDrawable> model = new ComplementROIModel<>(roisSet, true, true);

        if(complementROIWizard == null)
        {
            complementROIWizard = new ComplementROIWizard(getAssociatedWindow(), "ROI complement");
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
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }    

        String selectedType = getSelectedType();
        Map<Object, ROIDrawable> roisForType = rois.get(selectedType);
        Set<ROIDrawable> roisSet = new LinkedHashSet<>(roisForType.values());

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
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        String selectedType = getSelectedType();
        Map<Object, ROIDrawable> roisForType = rois.get(selectedType);

        Set<ROIDrawable> roisSet = new LinkedHashSet<>(roisForType.values());

        ExportAnnotationModel<ROIDrawable> model = new ExportAnnotationModel<>(roisSet, "ROI", "roi", true, true);

        this.exportAnnotationWizard = (exportAnnotationWizard == null) ? new ExportAnnotationWizard(getAssociatedWindow(), "Export ROIs to file") : exportAnnotationWizard;
        this.exportAnnotationWizard.showDialog(model);
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

                Channel2DChart<?> chart = getSelectedChart();

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

    @Override
    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        String selectedType = getSelectedType();
        ROIDrawable roi = rois.get(selectedType, roiKey);
        if(roi == null)
        {
            return;
        }
        roi.setLabel(labelNew);

        ROIShapeFactorsTableModel model = (ROIShapeFactorsTableModel) roiShapeFactorsDialog.getTable().getModel();
        model.replaceROILabel(roiKey, labelNew);

        for(ROIReceiver roiReceiver : getAllROIReceivers())
        {
            roiReceiver.changeROILabel(roiKey, labelOld, labelNew);
        }
    }

    private List<ROIReceiver> getAllROIReceivers()
    {
        List<ROIReceiver> allProfileReceivers = new ArrayList<>();

        allProfileReceivers.add(getSelectedPanel());
        allProfileReceivers.addAll(roiReceivers);

        return allProfileReceivers;
    }
    protected void updateROIAvailability( ) 
    {        
        updateROIAvailabilityPrivate();
    }

    protected void updateROIAvailabilityPrivate() 
    {
        String selectedType = getSelectedType();

        boolean roisAvailable = !rois.isEmpty(selectedType);
        boolean multipleROIsAvailable = rois.size(selectedType) > 1;

        boolean actionsBasicEnabled = (getSelectedChart() != null);
        boolean actionsROIBasedEnabled = (roisAvailable && actionsBasicEnabled);
        boolean actionsMultipleROIBasedEnabled = (multipleROIsAvailable && actionsBasicEnabled);
        boolean actionsSubtractabilityROIBasedEnabled = !ROIUtilities.findPossibleDiffrences(rois.get(selectedType).values()).isEmpty();

        roiMaskAction.setEnabled(actionsROIBasedEnabled);
        showROIShapeFactorsAction.setEnabled(actionsROIBasedEnabled);
        complementROIsAction.setEnabled(actionsROIBasedEnabled);
        splitROIsAction.setEnabled(actionsROIBasedEnabled);
        convexHullROIsAction.setEnabled(actionsROIBasedEnabled);
        exportROIsAction.setEnabled(actionsROIBasedEnabled);
        rotateROIAction.setEnabled(actionsROIBasedEnabled);
        mergeROIsAction.setEnabled(actionsMultipleROIBasedEnabled);
        subtractROIsAction.setEnabled(actionsSubtractabilityROIBasedEnabled);

        Channel2DPanel<Channel2DChart<?>> selectedPanel = getSelectedPanel();
        if(selectedPanel != null)
        {
            selectedPanel.setROIBasedActionsEnabled(roisAvailable);
        }
    }

    private void updateROIRange()
    {   
        Channel2DPanel<Channel2DChart<?>> selectedPanel = getSelectedPanel();
        GradientPaintReceiver receiver = selectedPanel.getGradientPaintReceiver();         

        ROI roi = getROIUnion();
        receiver.setMaskedRegion(roi);              
    }

    @Override
    public ROI getROIUnion()
    {
        String selectedType = getSelectedType();        
        List<? extends ROI> roiList = rois.getOuterKeyValuesCopy(selectedType);
        ROI roiUnion = ROIUtilities.composeROIs(roiList, "All");
        return roiUnion;
    }

    @Override
    public Map<Object, ROI> getDrawableROIs()
    {
        String selectedType = getSelectedType();

        Map<Object, ROI> roisForType = new LinkedHashMap<>();
        roisForType.putAll(this.rois.get(selectedType));

        return roisForType;
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
    public void setProfilesAvailable(boolean b) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean areROIsAvailable()
    {
        boolean available = false;
        if(getMode().isROI())
        {
            String selectedType = getSelectedType();
            available = !rois.isEmpty(selectedType);
        }

        return available;
    }

    @Override
    public void setROIsAvailable(boolean b) {
        // TODO Auto-generated method stub

    }

    @Override
    public void requestCursorChange(Cursor cursor) {
        Channel2DPanel<?> panel = getSelectedPanel();
        panel.setCursor(cursor);
    }

    @Override
    public void setProfileGeometryVisible(boolean visible) {

    }

    @Override
    public void showDistanceMeasurements() 
    {
        distanceMeasurementsDialog.setVisible(true);
    }

    @Override
    public void showROIShapeFactors() {
        roiShapeFactorsDialog.setVisible(true);
    }

    @Override
    public void addMarkerKnob(Object markerKey, double knobPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMarkerKnob(Object markerKey, int knobIndex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void moveMarkerKnob(Object markerKey, int knobIndex,
            double knobPositionNew) {
        // TODO Auto-generated method stub

    }
    @Override
    public void editGradient() 
    {
        Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
        if (panel != null) 
        {
            GradientPaintReceiver receiver = panel.getGradientPaintReceiver();

            if (rangeGradientChooser == null) 
            {
                rangeGradientChooser = new RangeGradientChooser(getAssociatedWindow(), receiver, true);
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
        Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
        if (panel != null) 
        {
            GradientPaintReceiver receiver = panel.getGradientPaintReceiver();

            if (rangeGradientHistogramChooser == null) 
            {
                rangeGradientHistogramChooser = new RangeHistogramView(getAssociatedWindow(), receiver.getPaintedSample(), receiver, "Gradient range");
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
    protected void handleChangeOfSelectedType(String typeOld, String typeNew) 
    {
        super.handleChangeOfSelectedType(typeOld, typeNew);

        if (!Objects.equals(typeOld, typeNew)) 
        {
            Channel2DChart<?> chartNew = getSelectedChart();

            if (chartNew != null) 
            {
                ROIStyle roiModel = chartNew.getROIStyle();
                roiEditor.setModel(roiModel);

                updateWandROIUnits(chartNew);
                updateROIAvailabilityPrivate();


                DistanceMeasurementStyle measurementStyle = chartNew.getDistanceMeasurementStyle();               
                measurementEditor.setModel(measurementStyle);

                updateRangeGradientChoser();
                updateROIMaskEditor();
                updateRangeHistogramGradientChoser();
                updateBuiltInGradientsSelector();
                SimpleResource resource = getSelectedResource();

                if(resource == null)
                {
                    return;
                }

                refreshROIGeometry(typeNew, resource);

                File defaultOutputFile = resource.getDefaultOutputLocation();

                boolean measurementsAvailable = distanceMeasurements.size()>0 && getMode().isMeasurement();
                updateMeasurementsAvailability(measurementsAvailable);

                Map<Object, DistanceMeasurementDrawable> measurementsForType = distanceMeasurements.get(typeNew);

                Map<Object, DistanceShapeFactors> measurementShapeFactors = new LinkedHashMap<>();

                if(measurementsForType != null)
                {
                    for(Entry<Object, DistanceMeasurementDrawable> entry :  measurementsForType.entrySet())
                    {
                        Object key = entry.getKey();
                        DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                        measurementShapeFactors.put(key, line);
                    }
                }


                CustomizableXYPlot plot = chartNew.getCustomizablePlot();
                PrefixedUnit dataUnitX = plot.getDomainDataUnit();
                PrefixedUnit dataUnitY = plot.getRangeDataUnit();
                PrefixedUnit displayedUnitX = plot.getDomainDisplayedUnit();
                PrefixedUnit displayedUnitY = plot.getRangeDisplayedUnit();

                DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
                distanceMeasurementsModel.addDistances(measurementShapeFactors);

                MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
                dstanceMeasurementsTable.setModel(distanceMeasurementsModel);       
            }
        }
    }

    /*
     * resource cannot be null;
     */
    private void refreshROIGeometry(String selectedTypeNew, SimpleResource resource)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }
        File defaultOutputFile = resource.getDefaultOutputLocation();
        PrefixedUnit dataUnit = selectedChart.getDomainDataUnit();
        PrefixedUnit displayedUnit = selectedChart.getDomainPreferredUnit();

        Map<Object, ROIDrawable> roisForType = this.rois.get(selectedTypeNew);

        ROIShapeFactorsTableModel roiGeometryModel = new ROIShapeFactorsTableModel(defaultOutputFile, dataUnit, displayedUnit);
        roiGeometryModel.addROIs(roisForType);

        MinimalNumericalTable table = roiShapeFactorsDialog.getTable();
        table.setModel(roiGeometryModel);
    }

    private void updateWandROIUnits(Channel2DChart<?> chart)
    {
        if(chart == null)
        {
            return;
        }

        PrefixedUnit axisUnitNew = chart.getZDisplayedUnit();
        wandROIModel.setUnitDifference(axisUnitNew);
    }

    private void updateBuiltInGradientsSelector()
    {
        if(gradientSelectionDialog != null && gradientSelectionDialog.isVisible())
        {
            Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                gradientSelectionDialog.setReceiver(receiver);
            }
        }
    }

    private void updateRangeGradientChoser()
    {
        if(rangeGradientChooser != null && rangeGradientChooser.isVisible())
        {
            Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                rangeGradientChooser.setReceiver(receiver);
            }
        }
    }

    private void updateRangeHistogramGradientChoser()
    {
        if(rangeGradientHistogramChooser != null && rangeGradientHistogramChooser.isVisible())
        {
            Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
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
            Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
            if (panel != null) 
            {
                GradientPaintReceiver receiver = panel.getGradientPaintReceiver();
                roiMaskEditor.setReceiver(receiver);
            }
        }
    }


    public void applyROIMask()
    {
        String selectedType = getSelectedType();
        boolean roisAvailable = !rois.isEmpty(selectedType);

        if(roisAvailable)
        {
            Channel2DPanel<Channel2DChart<?>> panel = getSelectedPanel();
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
            };
        }
    }

    @Override
    public void notifyAboutROISampleNeeded()
    {}

    @Override
    public void notifyAboutAspectRatioLock() {

    }

    @Override
    public Window getPublicationSite() 
    {
        return getAssociatedWindow();
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
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Gradient histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            editHistogramGradient();
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

    private class ROIPolygonAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIPolygonAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Polygon ROI");
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

    private class ROIFreeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIFreeAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/freeRoi.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME, "Free hand ROI");
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
    private class ROIWandAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ROIWandAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/roiWand.png"));

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

    //copied from DensityDialog
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

    private class OverlayAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OverlayAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/overlay.png"));
            putValue(LARGE_ICON_KEY, icon);

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_Y);
            putValue(NAME, "Overlay section");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            selectTypesAndOverlay();
        }
    }

    private class ChangeOverlaySettingsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ChangeOverlaySettingsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);

            putValue(NAME, "Change overlay interpolation");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            changeOverlaySettings();
        }
    }


    private class ChangeSectionSettingsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ChangeSectionSettingsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);

            putValue(NAME, "Change section interpolation");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            changeSectionSettings();
        }
    }

    private class AddDomainMarkerAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public AddDomainMarkerAction() 
        {
            // putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Add marker");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)        
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            setAddDomainMarkersMode(selected);
        }
    }

    private class SectionSettingsReceiver implements CrossSectionSettingsReceiver
    {
        @Override
        public String getCurrentSectionType()
        {
            String currentType = getSelectedType();
            return currentType;
        }

        @Override
        public CrossSectionSettings getCrossSectionSettings(String type)
        {
            CrossSectionSettings settings = sectionSettingsManager.getCrossSectionSettings(type);
            return settings;
        }

        @Override
        public Map<String, CrossSectionSettings> getCrossSectionSettings()
        {        
            Map<String, CrossSectionSettings> counts = sectionSettingsManager.getCrossSectionSettings();
            return counts;
        }

        @Override
        public void setCrossSectionSettings(String type, CrossSectionSettings pointCount)
        {       
            sectionSettingsManager.setCrossSectionSettings(type, pointCount);     
            refreshSection(type);
        }   

        @Override
        public void setCrossSectionSettings(Set<String> types, CrossSectionSettings settings)
        {       
            sectionSettingsManager.setCrossSectionSettings(types, settings);
            for(String type : types)
            {
                refreshSection(type);
            }
        }

        @Override
        public void setCrossSectionSettings(Map<String, CrossSectionSettings> settings )
        {       
            sectionSettingsManager.setCrossSectionSettings(settings);
            for(String type : settings.keySet())
            {
                refreshSection(type);
            }
        }
    }

    private class OverlaySectionSettingsReceiver implements CrossSectionSettingsReceiver
    {
        @Override
        public String getCurrentSectionType()
        {
            String currentType = getSelectedType();
            return currentType;
        }

        @Override
        public CrossSectionSettings getCrossSectionSettings(String type)
        {
            CrossSectionSettings settings = overlaySettingsManager.getCrossSectionSettings(type);
            return settings;
        }

        @Override
        public Map<String, CrossSectionSettings> getCrossSectionSettings()
        {        
            Map<String, CrossSectionSettings> counts = overlaySettingsManager.getCrossSectionSettings();
            return counts;
        }

        @Override
        public void setCrossSectionSettings(String type, CrossSectionSettings pointCount)
        {       
            overlaySettingsManager.setCrossSectionSettings(type, pointCount);     
            refreshOverlaysForProfile(type);
        }   

        @Override
        public void setCrossSectionSettings(Set<String> types, CrossSectionSettings settings)
        {       
            overlaySettingsManager.setCrossSectionSettings(types, settings);
            for(String type : types)
            {
                refreshOverlaysForProfile(type);
            }
        }

        @Override
        public void setCrossSectionSettings(Map<String, CrossSectionSettings> settings )
        {       
            overlaySettingsManager.setCrossSectionSettings(settings);
            for(String type : settings.keySet())
            {
                refreshOverlaysForProfile(type);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {      
        String name = evt.getPropertyName();

        if(FRAME_INDEX.equals(name))
        {
            double value = stackView.getStackModel().getCurrentStackingValue();

            for(CustomizableValueMarker marker : rangeMarkers.values())
            {
                marker.setValue(value);
            }
        }                             
    }     

    @Override
    public void addProfileKnob(Object profileKey, double knobPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeProfileKnob(Object profileKey, double knobPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProfileKnobPositions(Object resource, Object profileKey,
            List<Double> knobPositions) {
        // TODO Auto-generated method stub

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

            Channel2DChart<?> selectedChart = getSelectedChart();
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

            Channel2DChart<?> selectedChart = getSelectedChart();
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

    @Override
    public void transform(Channel2DDataTransformation tr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void transform(Channel2DDataInROITransformation tr,
            ROIRelativePosition position) {
        // TODO Auto-generated method stub

    }

    private class CustomMouseInteractiveToolListener implements MouseInteractiveToolListener
    {
        @Override
        public void toolToRedraw() 
        {
            Channel2DPanel<Channel2DChart<?>> selectedPanel = getSelectedPanel();
            if(selectedPanel != null)
            {
                selectedPanel.chartChanged();
            }
        } 
    }
}
