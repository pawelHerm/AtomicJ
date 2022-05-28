package atomicJ.gui.stack;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;
import static atomicJ.gui.stack.StackModel.FRAME_INDEX;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel2D;
import atomicJ.data.DataAxis1D;
import atomicJ.data.Datasets;
import atomicJ.gui.Channel1DDataset;
import atomicJ.gui.Channel2DDataset;
import atomicJ.gui.CustomizableValueMarker;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.Channel2DPlot;
import atomicJ.gui.MarkerPositionListener;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.MovieProcessableDataset;
import atomicJ.gui.MovieXYDataset;
import atomicJ.gui.PreferredStandardRoamingLegendTitleStyle;
import atomicJ.gui.RoamingLegend;
import atomicJ.gui.RoamingStandardTitleLegend;
import atomicJ.gui.SpectroscopySupervisor;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionSettingsView;
import atomicJ.gui.profile.CrossSectionSettingsReceiver;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizard;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;
import atomicJ.resources.CrossSectionSettings;
import atomicJ.resources.CrossSectionSettingsManager;
import atomicJ.resources.MapImageResource;

public class StackMapCrossSectionDialog extends SimpleMapStackDialog<StackModel<MapImageResource>>
{
    private static final int DEFAULT_HEIGHT = (int)Math.round(0.5*Toolkit.getDefaultToolkit().getScreenSize().height);
    private static final int DEFAULT_WIDTH = (int)Math.min(0.85*Toolkit.getDefaultToolkit().getScreenSize().height, Toolkit.getDefaultToolkit().getScreenSize().width);

    private static final Preferences PREF = Preferences.userNodeForPackage(SimpleMapStackDialog.class).node(SimpleMapStackDialog.class.getName());
    private static final PreferredStandardRoamingLegendTitleStyle LEGEND_STYLE = PreferredStandardRoamingLegendTitleStyle.getInstance(PREF, Datasets.STACK_SLICE);

    private static final String DATA_TYPE = "STRAIGHT_SECTION";

    public static final String FORCE_LEVEL_MARKER = "ForceLevelMarker";
    private static final String VERTICAL_MARKER = "VerticalMarker";

    private final Action overlayAction = new OverlayAction();
    private final Action changeSectionSectionSettingsAction = new ChangeSectionSettingsAction();
    private final Action changeOverlaySectionSettingsAction = new ChangeOverlaySettingsAction();
    private final Action addDomainMarkerAction = new AddDomainMarkerAction();

    private final CustomizableValueMarker forceLevelMarker;
    private final List<CustomizableValueMarker> domainMarkers = new ArrayList<>();

    private final MarkerPositionListener markerPositionListener;

    private final CrossSectionSettingsManager sectionSettingsManager = new CrossSectionSettingsManager();
    private final CrossSectionSettingsManager overlaySettingsManager = new CrossSectionSettingsManager();

    private final SectionSettingsReceiver sectionSettingsReceiver = new SectionSettingsReceiver();
    private final OverlaySectionSettingsReceiver overlaySettingsReceiver = new OverlaySectionSettingsReceiver();
    private final CrossSectionSettingsView sectionSettingsDialog ;

    private final MultipleSelectionWizard<String> overlayWizard;
    private final MultipleSelectionWizardPageModel<String> overlayModel;
    private Set<String> overlayChannels = new LinkedHashSet<>(); 

    private final StackMapView stackView;
    private final StackModel<?> parentModel;

    private final boolean horizontal;

    public StackMapCrossSectionDialog(StackMapView stackView,
            SpectroscopySupervisor spectroscopySupervisor, StackMapChart<?> chart, 
            StackModel<MapImageResource> crossSectionModel, StackModel<?> parentModel, MarkerPositionListener markerPositionListener, boolean horizontal) 
    {
        super(stackView.getAssociatedWindow(), spectroscopySupervisor, chart, crossSectionModel, PREF, false);        

        this.sectionSettingsDialog = new CrossSectionSettingsView(getAssociatedWindow(), "Section interpolation parameters");
        this.overlayWizard = new MultipleSelectionWizard<>(getAssociatedWindow(), "Overlay assistant");

        chart.setUseFixedChartAreaSize(false);

        this.stackView = stackView;
        this.parentModel = parentModel;
        this.markerPositionListener = markerPositionListener;
        this.horizontal = horizontal;  

        int pointCount = crossSectionModel.getDefaultGriddingGrid().getColumnCount();
        CrossSectionSettings defaultSettings = new CrossSectionSettings(pointCount);
        sectionSettingsManager.setDefaultCrossSectionSettings(defaultSettings);
        sectionSettingsManager.addTypeIfNecessary(DATA_TYPE);
        overlaySettingsManager.addTypeIfNecessary(DATA_TYPE);

        this.forceLevelMarker = new CustomizableValueMarker(FORCE_LEVEL_MARKER, parentModel.getCurrentStackingValue(), PREF, 1.0f, FORCE_LEVEL_MARKER);
        chart.addRangeValueMarker(forceLevelMarker, Layer.FOREGROUND);

        Set<String> allTypes = parentModel.getCorrespondingResource().getAllIdentifiers();
        overlayModel = new BasicMultipleSelectionWizardPageModel(allTypes, "Map profiles to overlay",
                "Channel selection", "Select the map profiles you want to overlay", false, true, true);

        Channel2DPlot plot = chart.getCustomizablePlot();
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.getRenderer(0).setBaseSeriesVisibleInLegend(false);    

        RoamingLegend legend = new RoamingStandardTitleLegend("Legend", chart.createDefaultLegend(), LEGEND_STYLE);
        chart.addRoamingSubLegend(legend);

        JMenuItem overlayItem = new JMenuItem(overlayAction);
        JMenuItem changeSectionSectionSettingsItem = new JMenuItem(changeSectionSectionSettingsAction);
        JMenuItem changeOverlaySectionSettingsItem = new JMenuItem(changeOverlaySectionSettingsAction);
        JMenuItem addDomainMarkerItem = new JCheckBoxMenuItem(addDomainMarkerAction);

        JMenu overlayMenu = new JMenu("Overlay");
        overlayMenu.setMnemonic(KeyEvent.VK_O);
        overlayMenu.add(overlayItem);

        JMenu menuInterpolation = new JMenu("Interpolation");
        menuInterpolation.setMnemonic(KeyEvent.VK_I);       
        menuInterpolation.add(changeSectionSectionSettingsItem);
        menuInterpolation.add(changeOverlaySectionSettingsItem);

        JMenu menuMarkers = new JMenu("Markers");
        menuMarkers.add(addDomainMarkerItem);
        menuMarkers.setMnemonic(KeyEvent.VK_K);

        JMenuBar menuBar = getMenuBar();

        menuBar.add(overlayMenu);
        menuBar.add(menuInterpolation);
        menuBar.add(menuMarkers);

        parentModel.addPropertyChangeListener(new PropertyChangeListener() 
        {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();

                if(FRAME_INDEX.equals(name))
                {
                    double value = StackMapCrossSectionDialog.this.parentModel.getCurrentStackingValue();
                    forceLevelMarker.setValue(value);
                }                
            }
        });    
    }

    public void moveMarker(int markerIndex, double positionNew)
    {
        int n = domainMarkers.size();

        if(markerIndex >= 0 && markerIndex <n)
        {
            CustomizableValueMarker marker = domainMarkers.get(markerIndex);
            marker.setValue(positionNew);          
        }
    }

    public void removeMarker(int markerIndex)
    {
        int n = domainMarkers.size();

        if(markerIndex >= 0 && markerIndex <n)
        {
            CustomizableValueMarker removedMarker = domainMarkers.remove(markerIndex);
            getChart().removeDomainValueMarker(removedMarker);
        }
    }

    public void addMarker(double position)
    {
        int index = domainMarkers.size();
        String markerName = "Section " + Integer.toString(index);
        CustomizableValueMarker markerNew = new CustomizableValueMarker(markerName, position, PREF.node(VERTICAL_MARKER), 1.f, Integer.valueOf(index));

        getChart().addDomainValueMarker(markerNew, Layer.FOREGROUND);
        domainMarkers.add(markerNew);
    }


    @Override
    public void requestNewDomainMarker(double knobPosition) 
    {
        Object markerKey = horizontal ? StackMapView.HORIZONTAL_MARKER : StackMapView.VERTICAL_MARKER;
        stackView.addMarkerKnob(markerKey, knobPosition);        
    }

    @Override
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey)
    {  
        if(markerKey instanceof Number)
        {
            markerPositionListener.setMarkerPosition(markerKey, newPosition.getX());

            int index = ((Number)markerKey).intValue();

            CustomizableValueMarker marker = domainMarkers.get(index);
            marker.setValue(newPosition.getX());
        }
        else if(FORCE_LEVEL_MARKER.equals(markerKey))
        {
            parentModel.setFrameClosestTo(newPosition.getY());
        }
    } 

    public void setAddDomainMarkersMode(boolean addMarkerMode)
    {
        MouseInputMode inputMode = addMarkerMode ? MouseInputModeStandard.INSERT_DOMAIN_MARKER : MouseInputModeStandard.NORMAL;        
        setMode(inputMode);
    }

    @Override
    protected int getDefaultWidth()
    {       
        int width =  Math.max(100, PREF.getInt(WINDOW_WIDTH, DEFAULT_WIDTH));

        return width;
    }

    @Override
    protected int getDefaultHeight()
    {  
        int height =  Math.max(100, PREF.getInt(WINDOW_HEIGHT, DEFAULT_HEIGHT));

        return height;
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

    private void selectTypesAndOverlay()
    {
        boolean approved = overlayWizard.showDialog(overlayModel);
        if (approved) 
        {
            overlayChannels = overlayModel.getSelectedKeys();

            refreshOverlayCrossSections();
        }
    }

    public void refreshOverlayCrossSections()
    {
        Map<String, List<ChannelSectionLine>> allCrossSections = new LinkedHashMap<>();

        StackModel<?> model = getStackModel();
        DataAxis1D axis = model.getStackAxis();

        for(String type: overlayChannels)
        {
            List<ChannelSectionLine> crossSections = horizontal ? stackView.getHorizontalCrossSections(axis, type, type, type)
                    : stackView.getVerticalCrossSections(axis, type, type, type);
            allCrossSections.put(type, crossSections);
        }

        clearOverlayCrossSections(overlayModel.getLeftOutKeys());
        setOverlayCrossSections(allCrossSections);
    }

    public void refreshOverlayCrossSections(String type)
    {        
        Map<String, List<ChannelSectionLine>> allCrossSections = new LinkedHashMap<>();

        StackModel<?> model = getStackModel();
        DataAxis1D axis = model.getStackAxis();

        List<ChannelSectionLine> crossSections = horizontal ? stackView.getHorizontalCrossSections(axis, type, type, type)
                : stackView.getVerticalCrossSections(axis, type, type, type);


        allCrossSections.put(type, crossSections);

        clearOverlayCrossSections(overlayModel.getLeftOutKeys());
        setOverlayCrossSections(allCrossSections);
    }

    private void clearOverlayCrossSections(Set<String> overlaysToClear)
    {
        StackPanel panel = getStackPanel();
        Channel2DChart<?> chart = panel.getSelectedChart();
        Channel2DPlot plot = chart.getCustomizablePlot();

        for(String overlayType : overlaysToClear)
        {
            plot.removeMovieDataset(overlayType);
        }

    }

    private void setOverlayCrossSections(Map<String, List<ChannelSectionLine>> crossSections)
    {
        StackPanel panel = getStackPanel();
        Channel2DChart<?> chart = panel.getSelectedChart();
        Channel2DPlot plot = chart.getCustomizablePlot();

        for(Entry<String, List<ChannelSectionLine>> entry: crossSections.entrySet())
        {
            String mapType = entry.getKey();

            List<XYDataset> datasets = new ArrayList<>();
            List<ChannelSectionLine> newCrossSections = entry.getValue();

            CrossSectionSettings settings = overlaySettingsManager.getCrossSectionSettings(DATA_TYPE);

            for(ChannelSectionLine section : newCrossSections)
            {
                Channel1D curve = section.getCrossSection(settings);

                XYDataset dataset = new Channel1DDataset(curve, mapType);                    
                datasets.add(dataset);
            }

            MovieXYDataset movieDataset = new MovieXYDataset(datasets);
            plot.addOrReplaceMovieDataset(mapType, movieDataset, newCrossSections.get(0).getRangeQuantity());           
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


    public void refreshSection(String type)
    {
        StackPanel panel = getStackPanel();
        final  Channel2DChart<?> chart = panel.getSelectedChart();

        final CrossSectionSettings settings = sectionSettingsManager.getCrossSectionSettings(type);

        Runnable runnable = new Runnable() 
        {           
            @Override
            public void run() 
            {                
                StackModel<?> stackModelNew = horizontal ? parentModel.getHorizontalResizedSection(settings.getInterpolationMethod(), settings.getPointCount())
                        :parentModel.getVerticalSections(settings.getInterpolationMethod(), settings.getPointCount());

                List<Channel2D> channels = stackModelNew.getChannels();
                List<Channel2DDataset> frames = Channel2DDataset.getDatasets(channels, stackModelNew.getSourceName());
                MovieProcessableDataset movieDataset = new MovieProcessableDataset(frames, channels.get(0).getIdentifier());

                Channel2DPlot plot = chart.getCustomizablePlot();
                plot.replaceLayerDataset(movieDataset.getKey(), movieDataset);
                plot.setMainMovieDataset(movieDataset);                
            }
        };

        Thread newThread = new Thread(runnable);
        newThread.start();
    }

    private class SectionSettingsReceiver implements CrossSectionSettingsReceiver
    {
        @Override
        public String getCurrentSectionType()
        {
            return DATA_TYPE;
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
            return DATA_TYPE;
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
            refreshOverlayCrossSections();
        }   

        @Override
        public void setCrossSectionSettings(Set<String> types, CrossSectionSettings settings)
        {       
            overlaySettingsManager.setCrossSectionSettings(types, settings);
            refreshOverlayCrossSections();
        }

        @Override
        public void setCrossSectionSettings(Map<String, CrossSectionSettings> settings )
        {       
            overlaySettingsManager.setCrossSectionSettings(settings);
            refreshOverlayCrossSections();
        }
    }
}
