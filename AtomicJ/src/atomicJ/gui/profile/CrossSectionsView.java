
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

package atomicJ.gui.profile;


import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.List;
import javax.swing.*;

import atomicJ.data.Channel1D;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.DistanceGeometryTableModel;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.FlexibleNumericalTableView;
import atomicJ.gui.ResourceXYView;
import atomicJ.gui.MinimalNumericalTable;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.profile.CrossSectionPanel.ProfilePanelFactory;
import atomicJ.resources.CrossSectionResource;
import atomicJ.resources.CrossSectionSettings;



public class CrossSectionsView extends ResourceXYView<CrossSectionResource, CrossSectionsChart, CrossSectionPanel<CrossSectionsChart>>
implements CrossSectionsReceiver, CrossSectionSettingsReceiver, CrossSectionsSupervisor
{
    private final ChangeSectionSettingsAction changeSectionSettingsAction = new ChangeSectionSettingsAction();

    private static final Preferences PREF =  Preferences.userNodeForPackage(CrossSectionsView.class).node("MultipleProfilesDialog");
    private static final Preferences PLOT_PREF = Preferences.userNodeForPackage(CrossSectionsView.class).node("Profiles dialog");

    private final CrossSectionSettingsView settingsDialog = new CrossSectionSettingsView(getAssociatedWindow(), "Profile interpolation");

    private final ProfileReceiver profileReceiver;

    private final FlexibleNumericalTableView distanceMeasurementsDialog = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Sections distance measurements");
    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(getAssociatedWindow());

    public CrossSectionsView(Window parent, ProfileReceiver profileReceiver)
    {
        super(parent, ProfilePanelFactory.getInstance(), "Profiles", PREF, ModalityType.MODELESS);				

        this.profileReceiver = profileReceiver;

        JPanel panelResources = getPanelResources();
        JTabbedPane panelCharts = getTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panelResources,panelCharts);
        splitPane.setOneTouchExpandable(true);

        setCenterComponent(splitPane);				
        setCenterComponent(getMultipleResourcesPanelButtons());

        JMenuBar menuBar = getMenuBar();

        JMenu chartMenu = getChartMenu();
        JMenuItem rawDataItem = new JMenuItem(getRawDataAction());
        chartMenu.add(rawDataItem);

        JMenu menuInterpolation = new JMenu("Interpolation");

        JMenuItem changeSectionSettingsItem = new JMenuItem(changeSectionSettingsAction);

        menuInterpolation.add(changeSectionSettingsItem);
        menuInterpolation.setMnemonic(KeyEvent.VK_I);

        menuBar.add(menuInterpolation);

        controlForResourceEmptiness(isEmpty());
    }

    @Override
    public void handleNewChartPanel(CrossSectionPanel<CrossSectionsChart> panel) 
    {
        super.handleNewChartPanel(panel);

        panel.setCrossSectionsSupervisor(this);
    }

    @Override
    public CrossSectionMarkerParameters getMarkerParameters(Object profileKey, double d)
    {
        return getMarkerParameters(getSelectedType(), profileKey, d);
    }

    @Override
    public CrossSectionMarkerParameters getMarkerParameters(String type, Object profileKey, double d)
    {
        CrossSectionResource resource = getSelectedResource();

        return resource.getMarkerParameters(type, profileKey, d);
    }

    @Override
    public void requestMarkerMovement(Object profileKey, int markerIndex, double positionNew) 
    {
        CrossSectionResource crossSectionResource = getSelectedResource();

        positionNew = crossSectionResource.getClosestLegalMarkerPosition(profileKey, positionNew);

        List<Double> newMarkerPositions = crossSectionResource.moveMarker(profileKey, markerIndex, positionNew);
        profileReceiver.setProfileKnobPositions(crossSectionResource.getImageResource(), profileKey, newMarkerPositions);

        for(String type : getTypes())
        {
            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
            if(panel != null)
            {
                CrossSectionMarkerParameters markerParametersForType = getMarkerParameters(type, profileKey, positionNew);
                panel.moveMarker(markerParametersForType, markerIndex);
            }
        }			
    }

    public void externalRequestMarkerMovement(CrossSectionResource resource, Object profileKey, int markerIndex, double positionNew) 
    {
        selectResource(resource);

        CrossSectionResource crossSectionResource = getSelectedResource();
        crossSectionResource.moveMarker(profileKey, markerIndex, positionNew);

        for(String type : getTypes())
        {
            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
            if(panel != null)
            {
                CrossSectionMarkerParameters markerParametersForType = getMarkerParameters(type, profileKey, positionNew);
                panel.moveMarker(markerParametersForType, markerIndex);
            }
        }       
    }


    public void externalAttemptToAddNewMarker(CrossSectionResource crossSectionResource, Object profileKey, double position) 
    {        
        crossSectionResource.addMarker(profileKey,position);

        for(String type : getTypes())
        {
            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
            if(panel != null)
            {
                CrossSectionMarkerParameters markerParametersForType = getMarkerParameters(type, profileKey, position);
                panel.addMarker(markerParametersForType);
            }
        }    
    }

    public void externalAttemptToRemoveMarker(CrossSectionResource crossSectionResource, Object profileKey, double markerPosition) 
    {        
        crossSectionResource.removeMarker(profileKey, markerPosition);

        for(String type : getTypes())
        {
            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
            if(panel != null)
            {
                panel.removeMarker(profileKey, markerPosition);
            }
        }    
    }

    @Override
    public void requestCursorChange(Cursor cursor) 
    {
        CrossSectionPanel<CrossSectionsChart> panel = getSelectedPanel();
        panel.setCursor(cursor);
    }


    @Override
    public boolean canNewMarkerBeAdded(double zSelected, double d, double margin)
    {
        CrossSectionResource selectedResource = getSelectedResource();
        String selectedType = getSelectedType();

        boolean canBeAdded = selectedResource.canNewMarkerBeAdded(selectedType, zSelected, d, margin);

        return canBeAdded;
    }

    @Override
    public void attemptToAddNewMarker(double zSelected, double d, double margin) 
    {
        CrossSectionResource selectedResource = getSelectedResource();
        String selectedType = getSelectedType();

        CrossSectionMarkerParameters markerParameters = selectedResource.getKeyOfSelectedCrossSectionChannel(selectedType, zSelected, d, margin);

        if(markerParameters != null)
        {			
            Object profileKey = markerParameters.getProfileKey();

            CrossSectionResource crossSectionResource = getSelectedResource();

            List<Double> newMarkerPositions = crossSectionResource.addMarker(profileKey, d);

            profileReceiver.setProfileKnobPositions(crossSectionResource.getImageResource(), profileKey, newMarkerPositions);

            for(String type : getTypes())
            {
                CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
                if(panel != null)
                {
                    CrossSectionMarkerParameters markerParametersForType = getMarkerParameters(type, profileKey, d);
                    panel.addMarker(markerParametersForType);
                }
            }
        }	
    }

    @Override
    public void externalSetProfileMarkerPositions(Object profileKey, List<Double> markerPositions) 
    {
        CrossSectionResource crossSectionResource = getSelectedResource();
        crossSectionResource.setProfileMarkerPositions(profileKey, markerPositions);

        for(double d : markerPositions)
        {
            for(String type : getTypes())
            {
                CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
                if(panel != null)
                {
                    CrossSectionMarkerParameters markerParametersForType = getMarkerParameters(type, profileKey, d);
                    panel.addMarker(markerParametersForType);
                }
            }
        }

    }

    @Override
    public void attemptToRemoveMarker(Object profileKey, double markerPosition) 
    {			
        CrossSectionResource crossSectionResource = getSelectedResource();
        List<Double> newMarkerPositions = crossSectionResource.removeMarker(profileKey, markerPosition);

        profileReceiver.setProfileKnobPositions(crossSectionResource.getImageResource(), profileKey, newMarkerPositions);

        for(String type : getTypes())
        {
            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
            if(panel != null)
            {
                panel.removeMarker(profileKey, markerPosition);
            }
        }		
    }


    public void removeAllMarkers(Object profileKey)
    {
        CrossSectionResource crossSectionResource = getSelectedResource();
        List<Double> newMarkerPositions = crossSectionResource.removeAllMarkers(profileKey);
        profileReceiver.setProfileKnobPositions(crossSectionResource.getImageResource(), profileKey, newMarkerPositions);

        for(String type : getTypes())
        {
            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);
            if(panel != null)
            {
                panel.removeAllMarkers(profileKey);
            }
        }	
    }

    @Override
    public Window getPublicationSite()
    {
        return getAssociatedWindow();
    }


    @Override
    public void handleChangeOfSelectedResource(CrossSectionResource resourceOld, CrossSectionResource resourceNew) 
    {
        super.handleChangeOfSelectedResource(resourceOld, resourceNew);

        if(this.settingsDialog.isShowing())
        {
            this.settingsDialog.ensureConsistencyWithReceiver();
        }		

        if(resourceNew != null)
        {
            String selectedType = getSelectedType();


            File defaultOutputFile = resourceNew.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resourceNew.getDistanceMeasurements(selectedType);
            boolean measurementsAvailable = distanceMeasurements.size()>0 && getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            Map<Object, DistanceShapeFactors> distanceShapeFactors = new LinkedHashMap<>();
            for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
            {
                Object key = entry.getKey();
                DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                distanceShapeFactors.put(key, line);
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
                dataUnitY = plot.getDomainDataUnit();
                displayedUnitX = plot.getDomainDisplayedUnit();
                displayedUnitY = plot.getDomainDisplayedUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();

                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
            distanceMeasurementsModel.addDistances(distanceShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel);      
        }
    }

    @Override
    public void handleChangeOfSelectedType(String typeOld, String typeNew)
    {
        super.handleChangeOfSelectedType(typeOld, typeNew);

        if(this.settingsDialog.isShowing())
        {
            this.settingsDialog.ensureConsistencyWithReceiver();
        }

        CrossSectionResource resource = getSelectedResource();

        if (!Objects.equals(typeOld, typeNew) && !(resource == null))
        {
            File defaultOutputFile = resource.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resource.getDistanceMeasurements(typeNew);
            boolean measurementsAvailable = distanceMeasurements.size()>0 && getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            Map<Object, DistanceShapeFactors> distanceMeasurementLines = new LinkedHashMap<>();
            for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
            {
                Object key = entry.getKey();
                DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                distanceMeasurementLines.put(key, line);
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
            distanceMeasurementsModel.addDistances(distanceMeasurementLines);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel); 
        }
    }

    public void changePointCount()
    {
        this.settingsDialog.showDialog(this);
    }

    @Override
    public String getCurrentSectionType()
    {
        String currentType = getSelectedType();
        return currentType;
    }

    @Override
    public CrossSectionSettings getCrossSectionSettings(String type)
    {
        CrossSectionResource selectedResource = getSelectedResource();
        CrossSectionSettings settings = selectedResource.getCrossSectionSettings(type);
        return settings;
    }

    @Override
    public Map<String, CrossSectionSettings> getCrossSectionSettings()
    {
        CrossSectionResource selectedResource = getSelectedResource();

        Map<String, CrossSectionSettings> counts = selectedResource.getCrossSectionSettings();
        return counts;
    }

    @Override
    public void setCrossSectionSettings(String type, CrossSectionSettings pointCount)
    {		
        CrossSectionResource selectedResource = getSelectedResource();		
        selectedResource.setCrossSectionSettings(type, pointCount);		
        refreshCrossSections(selectedResource, type);
    }	

    @Override
    public void setCrossSectionSettings(Set<String> types, CrossSectionSettings settings)
    {       
        CrossSectionResource selectedResource = getSelectedResource(); 
        selectedResource.setCrossSectionSettings(types, settings);
        for(String type : types)
        {
            refreshCrossSections(selectedResource, type);
        }
    }

    @Override
    public void setCrossSectionSettings(Map<String, CrossSectionSettings> settings )
    {       
        CrossSectionResource selectedResource = getSelectedResource(); 
        selectedResource.setCrossSectionSettings(settings);
        for(String type : settings.keySet())
        {
            refreshCrossSections(selectedResource, type);
        }
    }

    //this refresh the plot of a cross-section; cross-sectionResource does not require refreshing, because
    //it contains ChannelCrossections, objects which are 'bound' to the Channel object itself
    public void refreshCrossSections(CrossSectionResource resource, String type)
    {
        Map<Object, ChannelSectionLine> crossSections = resource.getCrossSections(type);

        CrossSectionsChart chart = getChart(resource, type);

        if(chart!= null)
        {
            CrossSectionPlot plot = chart.getCustomizablePlot();

            for(Entry<Object, ChannelSectionLine> entry: crossSections.entrySet())
            {
                ChannelSectionLine newCrossSection = entry.getValue();

                Object key = newCrossSection.getKey();
                Channel1D curve = newCrossSection.getCrossSection(resource.getCrossSectionSettings(type));

                plot.addOrReplaceCrossSectionCurve(key, curve);         
            }

            CrossSectionPanel<CrossSectionsChart> panel = getPanel(type);


            //refreshes knobs

            if(panel != null)
            {
                Map<Object, List<CrossSectionMarkerParameters>> markerParameters = resource.getMarkerParameters(type);

                for(Entry<Object, List<CrossSectionMarkerParameters>> entry : markerParameters.entrySet())
                {
                    Object profileKey = entry.getKey();
                    List<CrossSectionMarkerParameters> markers = entry.getValue();
                    chart.removeAllMarkers(profileKey);

                    for(CrossSectionMarkerParameters marker : markers)
                    {
                        panel.addMarker(marker);
                    }
                }          
            }
        }
    }

    @Override
    public void addOrReplaceCrossSections(CrossSectionResource resource, Map<String, ChannelSectionLine> crossSectionsNew)
    {
        boolean isEmptyOld = isEmpty();
        if(containsResource(resource))
        {
            selectResource(resource);

            for(Entry<String, ChannelSectionLine> entry: crossSectionsNew.entrySet())
            {
                String type = entry.getKey();

                if(containsType(type))
                {
                    ChannelSectionLine newCrossSection = entry.getValue();

                    Object key = newCrossSection.getKey();
                    Channel1D curve = newCrossSection.getCrossSection(resource.getCrossSectionSettings(type));

                    CrossSectionsChart chart = getSelectedChart(type);

                    if(chart != null)
                    {
                        chart.removeAllMarkers(newCrossSection.getKey());
                        CrossSectionPlot plot = chart.getCustomizablePlot();                
                        plot.addOrReplaceCrossSectionCurve(key, curve);
                    }
                }               
            }
        }
        else
        {
            Map<String, CrossSectionsChart> charts = buildNewCharts(resource, crossSectionsNew);
            addResource(resource, charts);

            selectResource(resource);
        }
        boolean isEmptyNew = isEmpty();

        if(isEmptyOld && !isEmptyNew)
        {
            setVisible(true);
        }
    }

    private Map<String, CrossSectionsChart> buildNewCharts(CrossSectionResource resource, Map<String, ChannelSectionLine> crossSectionsNew)
    {
        Map<String, CrossSectionsChart> charts = new LinkedHashMap<>();
        for(Entry<String, ChannelSectionLine> entry: crossSectionsNew.entrySet())
        {
            String type = entry.getKey();
            ChannelSectionLine profileNew = entry.getValue();

            Object key = profileNew.getKey();
            Channel1D curve = profileNew.getCrossSection(resource.getCrossSectionSettings(type));

            CrossSectionPlot plot = new CrossSectionPlot(PLOT_PREF);              
            plot.addOrReplaceCrossSectionCurve(key, curve);

            CrossSectionsChart chart = new CrossSectionsChart(plot);

            charts.put(type, chart);         
        }


        return charts;
    }

    public void removeProfiles(CrossSectionResource resource, Map<String, ChannelSectionLine> removedProfiles)
    {
        boolean isEmptyOld = isEmpty();
        if(!isEmptyOld && containsResource(resource))
        {
            selectResource(resource);

            for(Entry<String, ChannelSectionLine> entry: removedProfiles.entrySet())
            {
                String type = entry.getKey();
                ChannelSectionLine newProfile = entry.getValue();
                Object key = newProfile.getKey();
                Channel1D curve = newProfile.getCrossSection(resource.getCrossSectionSettings(type));

                CrossSectionsChart chart = getSelectedChart(type);
                chart.removeAllMarkers(key);

                CrossSectionPlot plot = getSelectedChart(type).getCustomizablePlot();				
                plot.removeCrossSection(key, curve);
            }
        }

        boolean isEmptyNew = isEmpty();

        if(isEmptyOld && !isEmptyNew)
        {
            setVisible(true);
        }
    }

    @Override
    protected void close()
    {
        setVisible(false);
    }

    private class ChangeSectionSettingsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ChangeSectionSettingsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);

            putValue(NAME, "Change interpolation");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            changePointCount();
        }
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
        CrossSectionResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            boolean measurementsAvailable = getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            CrossSectionPanel<CrossSectionsChart> selectedPanel = getSelectedPanel();
            selectedPanel.addOrReplaceDistanceMeasurement(measurement);        

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());

        }
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {                
        CrossSectionResource selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            int measurementsCount = selectedResource.getMeasurementCount(selectedType);
            boolean measurementsAvailable = (getMode().isMeasurement() && measurementsCount>0);
            updateMeasurementsAvailability(measurementsAvailable);

            CrossSectionPanel<CrossSectionsChart> selectedPanel = getSelectedPanel();
            selectedPanel.removeDistanceMeasurement(measurement);

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }   
    }
}
