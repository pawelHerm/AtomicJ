
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

import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.jfree.ui.Layer;

import atomicJ.data.Channel2D;
import atomicJ.data.DataAxis1D;
import atomicJ.data.Datasets;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.Channel2DDataset;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.ColorGradientReceiver;
import atomicJ.gui.CustomizableImageRenderer;
import atomicJ.gui.CustomizableValueMarker;
import atomicJ.gui.DataChangeEvent;
import atomicJ.gui.DataChangeListener;
import atomicJ.gui.DataViewAdapter;
import atomicJ.gui.BasicViewListener;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.Channel2DPlot;
import atomicJ.gui.Channel2DSupervisor;
import atomicJ.gui.DistanceGeometryTableModel;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.FlexibleNumericalTableView;
import atomicJ.gui.GradientPaintReceiver;
import atomicJ.gui.Channel2DReceiver;
import atomicJ.gui.ImagePlot;
import atomicJ.gui.MapChart;
import atomicJ.gui.MapView;
import atomicJ.gui.MapMarker;
import atomicJ.gui.MapMarkerEditor;
import atomicJ.gui.MapMarkerReceiver;
import atomicJ.gui.MapMarkerStyle;
import atomicJ.gui.MarkerPositionListener;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.MovieProcessableDataset;
import atomicJ.gui.ProcessableXYZDataset;
import atomicJ.gui.SpectroscopySupervisor;
import atomicJ.gui.SpectroscopySupervisorAdapter;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.StandardStyleTag;
import atomicJ.gui.annotations.ExportAnnotationModel;
import atomicJ.gui.annotations.ExportAnnotationWizard;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementReceiver;
import atomicJ.gui.measurements.GeneralUnionMeasurement;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.profile.ProfileEditor;
import atomicJ.gui.profile.ProfileReceiver;
import atomicJ.gui.profile.ProfileProxy;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.gui.rois.ModifyObjectsWizard;
import atomicJ.gui.rois.ModifyObjectsModel;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.save.SaveableChartSource;
import atomicJ.resources.CrossSectionSettings;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.MapImageResource;
import atomicJ.resources.SimpleResource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;


public class StackMapView extends SimpleMapStackDialog<StackModel<MapImageResource>> implements Channel2DSupervisor, SpectroscopySupervisor, ProfileReceiver, ColorGradientReceiver, SaveableChartSource<Channel2DChart<?>>, DataChangeListener<String>
{    
    public static final String VERTICAL_MARKER = "VerticalMarker";
    public static final String HORIZONTAL_MARKER = "HorizontalMarker";

    private static final Preferences PREF = Preferences.userNodeForPackage(StackMapView.class).node(StackMapView.class.getName());

    private static final int DEFAULT_HEIGHT = (int)Math.round(0.85*Toolkit.getDefaultToolkit().getScreenSize().height);
    private static final int DEFAULT_WIDTH = (int)Math.min(0.9*DEFAULT_HEIGHT, Toolkit.getDefaultToolkit().getScreenSize().width);  

    private final Action showHorizontalSectionsAction = new ShowHorizontalSections();
    private final Action showVerticalSectionsAction = new ShowVerticalSections();	
    private final Action showFlexibleSectionsAction = new ShowFlexibleSectionsAction();

    private final Action linkMapMarkersWithMapsAction = new LinkMapMarkersWithMapsAction();

    private final Action measurePolyLineAction = new MeasurePolyLineAction();
    private final Action measureFreeAction = new MeasureFreeAction();
    private final Action mergeMeasurementsAction = new MergeMeasurementsAction();
    private final Action linkDistanceMeasurementsWithMapsAction = new LinkDistanceMeasurementsWithMapsAction(); 
    private final Action linkROIsWithMapsAction = new LinkROIsWithMapsAction();

    private final RecalculateROIsAction recalculateROIsAction = new RecalculateROIsAction();

    private final Action linkProfilesWithMapsAction = new LinkProfilesWithMapsAction();
    private final Action modifyMapMarkerStyleAction = new ModifyMapMarkerStyleAction();
    private final Action addMapMarkerAction = new AddMapMarkerAction();

    private final Action showSectionGeometryAction = new ShowSectionGeometryAction();
    private final Action modifyProfileStyleAction = new ModifyProfileStyleAction(); 

    private final Action extractLineSectionAction = new ExtractLineSectionAction();
    private final Action extractPolyLineSectionAction = new ExtractPolyLineSectionAction();
    private final Action extractFreeHandSectionAction = new ExtractFreeHandSectionAction();

    private final Action exportSectionsAction = new ExportSectionsAction();
    private final Action importSectionsAction = new ImportSectionsAction();

    //////////////////////////MAP MARKERS//////////////////////////////////////////////////////

    private final Map<Object, MapMarker> mapMarkers = new LinkedHashMap<>();
    private final MapMarkerEditor mapMarkerEditor;

    //////////////////////////SECTIONS AND PROFILES////////////////////////////////////////////

    private final Map<Object, Profile> profiles = new LinkedHashMap<>();

    private StackModel<MapImageResource> horizontalSectionsStackModel;
    private StackModel<MapImageResource> verticalSectionsStackModel;

    private StackMapCrossSectionDialog horizontalSectionsDialog;
    private StackMapCrossSectionDialog verticalSectionsDialog;

    private final CustomizableValueMarker horizontalMarker = new CustomizableValueMarker("Horizontal section", 0, PREF.node(HORIZONTAL_MARKER), 1.f, HORIZONTAL_MARKER);
    private final CustomizableValueMarker verticalMarker = new CustomizableValueMarker("Vertical section", 0, PREF.node(VERTICAL_MARKER), 1.f, VERTICAL_MARKER);

    private final ProfileEditor profileEditor;
    private final FlexibleNumericalTableView sectionGeometryView;

    ///////////////////////////////////////////////////////////////////////////////

    private final MapView mapView;
    private final FlexibleMapSectionView flexibleSectionView;

    private boolean linkProfilesWithMaps = false;
    private boolean linkROIsWithMaps = false;
    private boolean linkDistanceMeasurementsWithMaps = false;
    private boolean linkMapMarkersWithMaps = false;

    private ModifyObjectsWizard measurementMergeWizard;

    private final DistanceMapDialogReceiver distanceMeasurementMapReceiver;
    private final ROIMapDialogReceiver roiMapReceiver;

    private ExportAnnotationWizard exportAnnotationWizard;
    private final ExtensionFileChooser profileImportChooser;

    public StackMapView(Window parent, MapView mapView, StackMapChart<?> chart, StackModel<MapImageResource> stackModel)
    {
        super(parent, mapView, chart, stackModel, PREF, true);		

        this.profileEditor = new ProfileEditor(getAssociatedWindow(), "Section style");

        this.mapView = mapView;
        mapView.addDataChangeListener(this);
        this.mapMarkerEditor = new MapMarkerEditor(getAssociatedWindow());

        this.distanceMeasurementMapReceiver = new DistanceMapDialogReceiver(mapView, stackModel.getCorrespondingResource());
        this.roiMapReceiver = new ROIMapDialogReceiver(mapView, stackModel.getCorrespondingResource());

        horizontalMarker.setVisible(false);
        verticalMarker.setVisible(false);

        chart.addDomainValueMarker(verticalMarker, Layer.FOREGROUND);
        chart.addRangeValueMarker(horizontalMarker, Layer.FOREGROUND);

        buildMenu();

        File defaultOutputDirectory = stackModel.getDefaultOutputDirectory();
        PrefixedUnit xUnit = stackModel.getXQuantity().getUnit();
        PrefixedUnit yUnit = stackModel.getYQuantity().getUnit();
        this.sectionGeometryView = new FlexibleNumericalTableView(getAssociatedWindow(), new StandardNumericalTable(new DistanceGeometryTableModel(defaultOutputDirectory, xUnit, yUnit), true, true), "Stack profile geometry");

        MapMarkerStyle mapMarkerStyle = chart.getMapMarkerStyle();
        mapMarkerEditor.setModel(mapMarkerStyle);

        ProfileStyle profileStyleModel = chart.getProfileStyle();
        profileEditor.setModel(profileStyleModel);          

        this.profileImportChooser = new ExtensionFileChooser(PREF, "Profile file (.profile)", "profile", true);
        profileImportChooser.setApproveButtonMnemonic(KeyEvent.VK_O);

        List<SampleCollection> sampleCollections = new ArrayList<>();
        SimpleResource resource = new SimpleResource(sampleCollections, stackModel.getSourceName(), stackModel.getSourceName(), stackModel.getDefaultOutputDirectory());
        this.flexibleSectionView = new FlexibleMapSectionView(this, ModalityType.MODELESS, resource);

        boolean showFlexibleSed = !flexibleSectionView.isEmpty();
        showFlexibleSectionsAction.setEnabled(showFlexibleSed);

        initViewListener();

        updateROIAvailabilityPrivate();
        updateProfilesAvailability(!profiles.isEmpty());
    }

    private void buildMenu()
    {
        JMenuItem linkMapMarkersWithMapsItem = new JCheckBoxMenuItem(linkMapMarkersWithMapsAction);
        JMenuItem modifyMapMarkerStyleItem = new JMenuItem(modifyMapMarkerStyleAction);
        JMenuItem addMapMarkerItem = new JCheckBoxMenuItem(addMapMarkerAction);

        JMenuItem measurePolyLineItem = new JCheckBoxMenuItem(measurePolyLineAction);
        JMenuItem measureFreeItem = new JCheckBoxMenuItem(measureFreeAction);
        JMenuItem mergeMeasurementsItem = new JMenuItem(mergeMeasurementsAction);

        JMenuItem linkDistanceMeasurementsWithMapsItem = new JCheckBoxMenuItem(linkDistanceMeasurementsWithMapsAction);
        JMenuItem linkROIsWithMapsItem = new JCheckBoxMenuItem(linkROIsWithMapsAction);
        JMenuItem recalculateROIsItem = new JMenuItem(recalculateROIsAction); 

        JMenuItem showHorizontalSectionsItem = new JCheckBoxMenuItem(showHorizontalSectionsAction);
        JMenuItem showVerticalSectionsItem = new JCheckBoxMenuItem(showVerticalSectionsAction);
        JMenuItem showFlexibleSectionsItem = new JCheckBoxMenuItem(showFlexibleSectionsAction);

        JMenuItem linkProfilesWithMapsItem = new JCheckBoxMenuItem(linkProfilesWithMapsAction);

        JMenuItem showSectionGeometryItem = new JCheckBoxMenuItem(showSectionGeometryAction);
        JMenuItem modifyProfileStyleItem = new JMenuItem(modifyProfileStyleAction);

        JMenuItem extractLineSectionItem = new JCheckBoxMenuItem(extractLineSectionAction);
        JMenuItem extractPolyLineSectionItem = new JCheckBoxMenuItem(extractPolyLineSectionAction);
        JMenuItem extractFreeHandSectionItem = new JCheckBoxMenuItem(extractFreeHandSectionAction);

        JMenuItem exportSectionsItem = new JMenuItem(exportSectionsAction);
        JMenuItem importSectionsItem = new JMenuItem(importSectionsAction);

        JMenu measureMenu = getMeasureMenu();
        measureMenu.add(measurePolyLineItem);
        measureMenu.add(measureFreeItem);

        measureMenu.addSeparator();
        measureMenu.add(mergeMeasurementsItem);

        measureMenu.addSeparator();
        measureMenu.add(linkDistanceMeasurementsWithMapsItem);

        JMenu roisMenu = getROIsMenu();
        roisMenu.addSeparator();
        roisMenu.add(linkROIsWithMapsItem);

        JMenu curvesMenu = new JMenu("Curves");
        curvesMenu.setMnemonic(KeyEvent.VK_U);

        curvesMenu.add(modifyMapMarkerStyleItem);
        curvesMenu.add(addMapMarkerItem);
        curvesMenu.add(linkMapMarkersWithMapsItem);
        curvesMenu.addSeparator();
        curvesMenu.add(recalculateROIsItem);

        JMenu sectionsMenu = new JMenu("Sections");
        sectionsMenu.setMnemonic(KeyEvent.VK_T);

        sectionsMenu.add(showHorizontalSectionsItem);
        sectionsMenu.add(showVerticalSectionsItem);
        sectionsMenu.add(showFlexibleSectionsItem);

        sectionsMenu.addSeparator();
        sectionsMenu.add(modifyProfileStyleItem);
        sectionsMenu.add(showSectionGeometryItem);

        sectionsMenu.addSeparator();
        sectionsMenu.add(extractLineSectionItem);
        sectionsMenu.add(extractPolyLineSectionItem);
        sectionsMenu.add(extractFreeHandSectionItem);

        sectionsMenu.addSeparator();
        sectionsMenu.add(exportSectionsItem);
        sectionsMenu.add(importSectionsItem);

        sectionsMenu.addSeparator();
        sectionsMenu.add(linkProfilesWithMapsItem);

        JMenuBar menuBar = getMenuBar();
        menuBar.add(curvesMenu);
        menuBar.add(sectionsMenu);  
    }

    private void initViewListener()
    {
        sectionGeometryView.addDataViewListener(new BasicViewListener(showSectionGeometryAction));
        flexibleSectionView.addDataViewListener(new BasicViewListener(showFlexibleSectionsAction));
    }

    public void mergeMeasurements()
    {
        Channel2DChart<?> selectedChart = getChart();
        if(selectedChart == null)
        {
            return;
        }    

        Map<Object, DistanceMeasurementDrawable> measurements = getDistanceMeasurements();
        Set<DistanceMeasurementDrawable> measurementSet = new LinkedHashSet<>(measurements.values());

        ModifyObjectsModel<DistanceMeasurementDrawable> model 
        = new ModifyObjectsModel<>(measurementSet,"measurement","Measurement merging", 
                "Select which measurements are to be merged", "Measurements to merge",true, true);

        if(measurementMergeWizard == null)
        {
            measurementMergeWizard = new ModifyObjectsWizard(getAssociatedWindow(), "Measurement merging");
        }

        boolean approved = measurementMergeWizard.showDialog(model);

        if(approved)
        {
            boolean deleteOriginalMeasurements = model.isDeleteOriginalObjects();
            Collection<DistanceMeasurementDrawable> selectedMeasurements = model.getSelectedObjects();

            Integer mergedROIKey =  deleteOriginalMeasurements ? DistanceMeasurementDrawable.getUnionKey(selectedMeasurements) : selectedChart.getCurrentMeasurementIndex();
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

    @Override
    protected void setMultipleMeasurementActionsEnabled(boolean enabled)
    {
        super.setMultipleMeasurementActionsEnabled(enabled);

        mergeMeasurementsAction.setEnabled(enabled);
    }

    public List<ChannelSectionLine> getHorizontalCrossSections(DataAxis1D verticalAxis, Object key, String name, String type)
    {
        StackModel<MapImageResource> stackModel = getStackModel();
        Channel2DResource densityResource = stackModel.getCorrespondingResource();

        return densityResource.getHorizontalCrossSections(verticalAxis, key, name, type);
    }

    public List<ChannelSectionLine> getVerticalCrossSections(DataAxis1D horizontalAxis, Object key, String name, String type)
    {
        StackModel<MapImageResource> stackModel = getStackModel();
        Channel2DResource densityResource = stackModel.getCorrespondingResource();

        return densityResource.getVerticalCrossSections(horizontalAxis, key, name, type);
    }  

    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Object profileKey, Set<String> types)
    {
        StackModel<MapImageResource> stackModel = getStackModel();
        Channel2DResource densityResource = stackModel.getCorrespondingResource();

        Profile profile = this.profiles.get(profileKey);

        Map<Object, Profile> profiles = new LinkedHashMap<>();

        if(profile != null)
        {
            profiles.put(profileKey, profile);
        }
        return densityResource.getCrossSections(profiles, types);
    }


    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Profile profile, Set<String> types)
    {
        StackModel<MapImageResource> stackModel = getStackModel();
        Channel2DResource densityResource = stackModel.getCorrespondingResource();

        Map<Object, Profile> profiles = new LinkedHashMap<>();
        profiles.put(profile.getKey(), profile);
        return densityResource.getCrossSections(profiles, types);
    }

    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Set<String> types)
    {    
        StackModel<MapImageResource> stackModel = getStackModel();
        Channel2DResource densityResource = stackModel.getCorrespondingResource();

        Map<String, Map<Object, ChannelSectionLine>> crossSections = densityResource.getCrossSections(profiles, types);

        return crossSections;
    }

    public void setLinkProfilesWithMaps(boolean linkProfilesWithMaps)
    {
        if(this.linkProfilesWithMaps != linkProfilesWithMaps)
        {
            this.linkProfilesWithMaps = linkProfilesWithMaps;

            if(linkProfilesWithMaps)
            {
                copyProfilesToReceiver(mapView);
                mapView.addProfileReceiver(this);
            }
            else
            {
                mapView.removeDataChangeListener(this);
            }
        }
    }

    private void copyProfilesToReceiver(ProfileReceiver receiver)
    {
        for(Entry<Object, Profile> entry: profiles.entrySet())
        {
            Profile profile = entry.getValue();
            receiver.addOrReplaceProfile(profile);
        }
    }

    public void setLinkROIsWithMaps(boolean linkROIsWithMaps)
    {
        if(this.linkROIsWithMaps != linkROIsWithMaps)
        {
            this.linkROIsWithMaps = linkROIsWithMaps;

            if(linkROIsWithMaps)
            {
                copyROIsToReceiver(roiMapReceiver);
                mapView.addROIReceiver(this);
                addROIReceiver(roiMapReceiver);
            }
            else
            {
                mapView.removeROIReceiver(this);
                removeROIReceiver(roiMapReceiver);
            }
        }
    }

    public void setLinkDistanceMeasurementsWithMaps(boolean linkDistanceMeasurementsWithMaps)
    {
        if(this.linkDistanceMeasurementsWithMaps != linkDistanceMeasurementsWithMaps)
        {
            this.linkDistanceMeasurementsWithMaps = linkDistanceMeasurementsWithMaps;

            if(linkDistanceMeasurementsWithMaps)
            {
                copyDistanceMeasurementsToReceiver(distanceMeasurementMapReceiver);
                mapView.addDistanceMeasurementReceiver(this);
                addDistanceMeasurementReceiver(distanceMeasurementMapReceiver);
            }
            else
            {
                mapView.removeDistanceMeasurementReceiver(this);
                removeDistanceMeasurementReceiver(distanceMeasurementMapReceiver);
            }
        }
    }

    @Override
    public void addProfileKnob(Object profileKey, double knobPosition)
    {
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        { 
            boolean knobAdded = profile.addKnob(knobPosition);

            if(knobAdded)
            {
                StackPanel panel = getStackPanel();
                panel.addProfileKnob(profileKey,  knobPosition);

                if(linkProfilesWithMaps)
                {
                    mapView.selectResource(getStackModel().getCorrespondingResource());
                    mapView.addProfileKnob(profileKey, knobPosition);
                } 

                flexibleSectionView.addDomainMarker(profileKey, knobPosition);
            }
        }      
    }

    @Override
    public void moveProfileKnob(Object profileKey, int knobIndex, double knobPositionNew)
    {
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {    
            boolean positionChanged = profile.moveKnob(knobIndex, knobPositionNew);
            if(positionChanged)
            {
                StackPanel panel = getStackPanel();
                panel.moveProfileKnob(profileKey, knobIndex, knobPositionNew);

                if(linkProfilesWithMaps)
                {
                    mapView.selectResource(getStackModel().getCorrespondingResource());
                    mapView.moveProfileKnob(profileKey, knobIndex, knobPositionNew);
                } 

                flexibleSectionView.moveDomainMarker(profileKey, knobIndex, knobPositionNew);                
            }             
        }	      
    }


    @Override
    public void removeProfileKnob(Object profileKey, double knobPosition)
    {
        Profile oldProfile = profiles.get(profileKey);

        if(oldProfile != null)
        {
            boolean removed = oldProfile.removeKnob(knobPosition);

            if(removed)
            {
                StackPanel panel = getStackPanel();
                panel.removeProfileKnob(profileKey, knobPosition);

                if(linkProfilesWithMaps)
                {
                    mapView.selectResource(getStackModel().getCorrespondingResource());
                    mapView.removeProfileKnob(profileKey, knobPosition);
                } 

                flexibleSectionView.removeDomainMarker(profileKey, knobPosition); 

                setMode(oldProfile.getMouseInputMode(getMode()), false);
            }                  
        }
    }

    @Override
    public void setProfileKnobPositions(Object resource, Object profileKey, List<Double> knobPositions)
    {       
        Profile oldProfile = profiles.get(profileKey);
        boolean newKnobPositionsSet = oldProfile.setKnobPositions(knobPositions);

        if(newKnobPositionsSet)
        {
            StackPanel panel = getStackPanel();
            panel.setProfileKnobPositions(0, profileKey, knobPositions);       

            if(linkProfilesWithMaps)
            {
                StackModel<MapImageResource> model = getStackModel();
                MapImageResource mapResource = model.getCorrespondingResource();
                mapView.selectResource(mapResource);
                mapView.setProfileKnobPositions(resource, profileKey, knobPositions);
            } 

            flexibleSectionView.setDomainMarkers(profileKey, knobPositions); 

            setMode(oldProfile.getMouseInputMode(getMode()), false);
        }
    }

    private void setFlexibleSectionsVisible(boolean visible) 
    {
        flexibleSectionView.setVisible(visible);
    }

    @Override
    public void addOrReplaceProfile(Profile profile) 
    {
        setMode(profile.getMouseInputMode(getMode()), false);

        Object profileKey = profile.getKey();
        Profile oldProfile = profiles.get(profileKey);

        if(profile.equalsUpToStyle(oldProfile))
        {
            return;
        }

        profiles.put(profileKey, profile.copy());

        if(linkProfilesWithMaps)
        {
            StackModel<MapImageResource> model = getStackModel();
            MapImageResource mapResource = model.getCorrespondingResource();

            mapView.selectResource(mapResource);
            mapView.addOrReplaceProfile(profile);

            //            mapDialog.selectResource(resource);
            // mapDialog.addOrReplaceROI(roi);   
        }

        updateProfilesAvailability(true);

        StackPanel panel = getStackPanel();
        panel.addOrReplaceProfile(profile);     

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) sectionGeometryView.getTable().getModel();
        model.addOrUpdateDistance(profileKey, profile.getDistanceShapeFactors());

        flexibleSectionView.addOrReplaceProfileSection(profile);         
    }

    public Channel2D getSection(Object profileKey, CrossSectionSettings crossSectionSettings)
    {
        Profile profile = profiles.get(profileKey);

        Channel2D channel = null;
        if(profile != null)
        {
            StackModel<MapImageResource> stackModel = getStackModel();
            channel = stackModel.getSection(profile, crossSectionSettings);
        }  

        return channel;
    } 

    public void getSectionParallelized(Object profileKey, CrossSectionSettings crossSectionSettings, Channel2DReceiver receiver)
    {
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {
            StackModel<MapImageResource> stackModel = getStackModel();
            stackModel.getSectionParallelized(profile, crossSectionSettings, receiver);
        }  
    } 

    @Override
    public void removeProfile(Profile profile) 
    {        
        Object profileKey = profile.getKey();
        Profile removedProfile = profiles.remove(profileKey);

        if(removedProfile != null)
        {
            int profileCount = profiles.size();
            boolean profilesAvailable = (profileCount > 0);
            updateProfilesAvailability(profilesAvailable);

            StackPanel panel = getStackPanel();
            panel.removeProfile(profile);

            if(linkProfilesWithMaps)
            {
                StackModel<MapImageResource> model = getStackModel();
                MapImageResource mapResource = model.getCorrespondingResource();
                mapView.selectResource(mapResource);
                mapView.removeProfile(profile);
            }

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) sectionGeometryView.getTable().getModel();
            model.removeDistance(profileKey, profile.getDistanceShapeFactors());

            flexibleSectionView.removeProfileSection(profile.getKey());           

            setMode(profile.getMouseInputMode(getMode()), false);
        }
    }

    @Override
    public void setProfiles(Map<Object, Profile> profiles) 
    {
        setMode(MouseInputModeStandard.PROFILE_LINE, false);

        this.profiles.clear();
        this.profiles.putAll(profiles);
        boolean profilesAvailable = !this.profiles.isEmpty();
        updateProfilesAvailability(profilesAvailable);

        StackPanel panel = getStackPanel();
        panel.setProfiles(profiles);

        Map<Object, DistanceShapeFactors> profileGeometries = new LinkedHashMap<>();
        for(Entry<Object, Profile> entry :  profiles.entrySet())
        {
            Object key = entry.getKey();
            DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
            profileGeometries.put(key, line);
        }

        DistanceGeometryTableModel model = (DistanceGeometryTableModel) sectionGeometryView.getTable().getModel();
        model.setDistances(profileGeometries);
    }

    private void updateProfilesAvailability(boolean profilesAvailable) 
    {
        getStackPanel().setProfilesAvailable(profilesAvailable);

        setSingleProfileBasedActionsEnabled(profilesAvailable);
    }


    private void setSingleProfileBasedActionsEnabled(boolean actionsProfileBasedEnabled)
    {
        if(showSectionGeometryAction != null) 
        {
            showSectionGeometryAction.setEnabled(actionsProfileBasedEnabled);
        }

        exportSectionsAction.setEnabled(actionsProfileBasedEnabled);
    }


    /////////////////////////////////////////////////////////////////////////
    ////////////////////////////MAP MARKERS/////////////////////////////////


    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker)
    {
        Object mapMarkerKey = mapMarker.getKey();
        MapMarker oldMapMarker = mapMarkers.get(mapMarkerKey);

        if(mapMarker.equalsUpToStyle(oldMapMarker))
        {
            return;
        }

        Point2D controlPoint = mapMarker.getControlDataPoint();

        String sourceName = getMapMarkerSourceName(mapMarker.getControlDataPoint());
        String sourcePositionDescription = getMapMarkerPositionDescription(controlPoint);

        mapMarker.setCurveName(sourceName);
        mapMarker.setPositionDescription(sourcePositionDescription);

        mapMarkers.put(mapMarkerKey, new MapMarker(mapMarker));

        if(linkMapMarkersWithMaps)
        {
            StackModel<MapImageResource> model = getStackModel();
            MapImageResource mapResource = model.getCorrespondingResource();
            mapView.selectResource(mapResource);
            mapView.addOrReplaceMapMarker(mapMarker);
        }

        updateMapMarkersAvailability(true);

        StackPanel panel = getStackPanel();
        panel.addOrReplaceMapMarker(mapMarker);    

        jumpToResults(controlPoint);
        jumpToFigures(controlPoint);
    }

    private SimpleSpectroscopySource getSpectroscopySource(Point2D p)
    {
        StackModel<MapImageResource> model = getStackModel();
        MapImageResource mapResource = model.getCorrespondingResource();
        MapSource<?> mapSource = mapResource.getMapSource();
        return mapSource.getSimpleSpectroscopySource(p);
    }

    protected String getMapMarkerSourceName(Point2D controlPoint) 
    {      
        SimpleSpectroscopySource simpleSource = getSpectroscopySource(controlPoint);
        return simpleSource.getShortName();
    }

    protected String getMapMarkerPositionDescription(Point2D controlPoint) 
    {      
        SimpleSpectroscopySource simpleSource = getSpectroscopySource(controlPoint);
        return simpleSource.getMapPositionDescription();
    }


    private void updateMapMarkersAvailability(boolean mapMarkersAvailable) 
    {}

    @Override
    public void removeMapMarker(MapMarker mapMarker) 
    {
        Object mapMarkerKey = mapMarker.getKey();
        MapMarker removedMapMarker = mapMarkers.remove(mapMarkerKey);

        if(removedMapMarker != null)
        {
            int mapMarkerCount = mapMarkers.size();
            boolean mapMarkersAvailable = (mapMarkerCount > 0);
            updateMapMarkersAvailability(mapMarkersAvailable);

            StackPanel panel = getStackPanel();
            panel.removeMapMarker(mapMarker);

            if(linkMapMarkersWithMaps)
            {
                StackModel<MapImageResource> model = getStackModel();
                MapImageResource mapResource = model.getCorrespondingResource();
                mapView.selectResource(mapResource);
                mapView.removeMapMarker(mapMarker);
            }
        }
    }


    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkers) 
    {
        this.mapMarkers.clear();
        this.mapMarkers.putAll(mapMarkers);
        boolean mapMarkersAvailable = !this.mapMarkers.isEmpty();
        updateMapMarkersAvailability(mapMarkersAvailable);

        StackPanel panel = getStackPanel();
        panel.setMapMarkers(mapMarkers);
    }

    public void setLinkMapMarkersWithMaps(boolean linkMapMarkersWithMaps)
    {
        if(this.linkMapMarkersWithMaps != linkMapMarkersWithMaps)
        {
            this.linkMapMarkersWithMaps = linkMapMarkersWithMaps;

            if(linkMapMarkersWithMaps)
            {
                copyMapMarkersReceiver(mapView);
                mapView.addMapMarkerReceiver(this);
            }
        }
    }

    private void copyMapMarkersReceiver(MapMarkerReceiver receiver)
    {
        for(Entry<Object, MapMarker> entry: mapMarkers.entrySet())
        {
            MapMarker mapMarker = entry.getValue();
            receiver.addOrReplaceMapMarker(mapMarker);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void dataChanged(DataChangeEvent<String> event) 
    {  
        flexibleSectionView.refreshOverlayCrossSections();
    }

    @Override
    public void setProfileStyleEditorVisible(boolean visible) 
    {
        profileEditor.setVisible(visible);
    }

    @Override
    public void setProfileGeometryVisible(boolean visible) 
    {
        sectionGeometryView.setVisible(visible);
    }

    @Override
    protected int getDefaultWidth()
    {       
        int width =  PREF.getInt(WINDOW_WIDTH, DEFAULT_WIDTH);

        return width;
    }

    @Override
    protected int getDefaultHeight()
    {  
        int height =  PREF.getInt(WINDOW_HEIGHT, DEFAULT_HEIGHT);

        return height;
    }

    @Override
    public void addMarkerKnob(Object markerKey, double knobPosition)
    {
        if(HORIZONTAL_MARKER.equals(markerKey) && horizontalSectionsDialog != null)
        {            
            horizontalSectionsDialog.addMarker(knobPosition);           
        }
        else if(VERTICAL_MARKER.equals(markerKey) && verticalSectionsDialog != null)
        {
            verticalSectionsDialog.addMarker(knobPosition);             
        }

        getStackPanel().addMarkerKnob(markerKey, knobPosition);
    }

    @Override
    public void moveMarkerKnob(Object markerKey, int knobIndex, double knobPositionNew) 
    {        
        if(HORIZONTAL_MARKER.equals(markerKey) && horizontalSectionsDialog != null)
        {            
            horizontalSectionsDialog.moveMarker(knobIndex, knobPositionNew);           
        }
        else if(VERTICAL_MARKER.equals(markerKey) && verticalSectionsDialog != null)
        {
            verticalSectionsDialog.moveMarker(knobIndex, knobPositionNew);             
        }

        getStackPanel().moveMarkerKnob(markerKey, knobIndex, knobPositionNew);
    }

    @Override
    public void removeMarkerKnob(Object markerKey, int knobIndex)
    {
        if(HORIZONTAL_MARKER.equals(markerKey) && horizontalSectionsDialog != null)
        {            
            horizontalSectionsDialog.removeMarker(knobIndex);           
        }
        else if(VERTICAL_MARKER.equals(markerKey) && verticalSectionsDialog != null)
        {
            verticalSectionsDialog.removeMarker(knobIndex);             
        }

        getStackPanel().removeMarkerKnob(markerKey, knobIndex);
    }

    @Override
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey)
    { 
        if(HORIZONTAL_MARKER.equals(markerKey))
        {
            if(horizontalSectionsDialog != null)
            {
                double y = newPosition.getY();
                horizontalSectionsDialog.getStackModel().setFrameClosestTo(y);               
            }
        }
        else if(VERTICAL_MARKER.equals(markerKey))
        {
            if(verticalSectionsDialog != null)
            {
                double x = newPosition.getX();
                verticalSectionsDialog.getStackModel().setFrameClosestTo(x);               
            }
        }
    }

    public void setHorizontalSectionsVisible(boolean visible)
    {
        if(visible)
        {
            if(horizontalSectionsDialog == null)
            {
                initializeHorizontalSectionsDialog();
            }
        }

        if(horizontalSectionsDialog != null)
        {
            horizontalSectionsDialog.setVisible(visible);
            horizontalMarker.setVisible(visible);
        }
    }

    private void initializeHorizontalSectionsDialog()
    {
        horizontalSectionsStackModel = (StackModel<MapImageResource>) getHorizontalSectionsStackModel();

        horizontalSectionsDialog = createSectionsDialog(horizontalSectionsStackModel, new HorizontalCrossSectionSupervisor(), new MarkerPositionListener() {              
            @Override
            public void setMarkerPosition(Object markerKey, double markerPositionNew) 
            {
                if(markerKey instanceof Number)
                {
                    int knobIndex = ((Number) markerKey).intValue();
                    getStackPanel().moveMarkerKnob(HORIZONTAL_MARKER, knobIndex, markerPositionNew);
                }
            }
        }, true);


        double knobPosition = horizontalSectionsStackModel.getSuggestedFirstMarkerPosition();       

        horizontalMarker.setValue(horizontalSectionsStackModel.getCurrentStackingValue());
        horizontalMarker.addKnob(knobPosition);

        for(double position : horizontalMarker.getKnobPositions())
        {
            horizontalSectionsDialog.addMarker(position);
        }

        horizontalSectionsDialog.addDataViewListener(new DataViewAdapter() 
        {
            @Override
            public void dataViewVisibilityChanged(boolean visibleNew)
            {                  
                showHorizontalSectionsAction.putValue(Action.SELECTED_KEY, visibleNew);
                horizontalMarker.setVisible(visibleNew);
            }
        });

        horizontalSectionsStackModel.addPropertyChangeListener(new PropertyChangeListener() 
        {               
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                String property = evt.getPropertyName();

                if(StackModel.FRAME_INDEX.equals(property))
                {
                    double sectionValue = horizontalSectionsStackModel.getCurrentStackingValue();
                    horizontalMarker.setValue(sectionValue);
                }
            }
        });        
    }

    public void setVerticalSectionsVisible(boolean visible)
    {
        if(visible)
        {
            if(verticalSectionsDialog == null)
            {
                initializeVerticalSectionsDialog();
            }
        }

        if(verticalSectionsDialog != null)
        {
            verticalSectionsDialog.setVisible(visible);
            verticalMarker.setVisible(visible);
        }	   
    }

    private void initializeVerticalSectionsDialog()
    {        
        verticalSectionsStackModel = (StackModel<MapImageResource>) getVerticalSectionsStackModel();
        verticalSectionsDialog = createSectionsDialog(verticalSectionsStackModel, new VerticalCrossSectionSupervisor(), new MarkerPositionListener() {

            @Override
            public void setMarkerPosition(Object markerKey, double markerPositionNew) 
            {
                if(markerKey instanceof Number)
                {
                    int knobIndex = ((Number) markerKey).intValue();
                    getStackPanel().moveMarkerKnob(VERTICAL_MARKER, knobIndex, markerPositionNew);
                }
            }
        }, false);

        double knobPosition = verticalSectionsStackModel.getSuggestedFirstMarkerPosition();
        verticalMarker.setValue(verticalSectionsStackModel.getCurrentStackingValue());
        verticalMarker.addKnob(knobPosition);

        for(double position : verticalMarker.getKnobPositions())
        {
            verticalSectionsDialog.addMarker(position);
        }

        verticalSectionsDialog.addDataViewListener(new DataViewAdapter() 
        {
            @Override
            public void dataViewVisibilityChanged(boolean visibleNew)
            {
                showVerticalSectionsAction.putValue(Action.SELECTED_KEY, visibleNew);
                verticalMarker.setVisible(visibleNew);
            }
        });

        verticalSectionsStackModel.addPropertyChangeListener(new PropertyChangeListener() 
        {               
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                String property = evt.getPropertyName();

                if(StackModel.FRAME_INDEX.equals(property))
                {
                    double sectionValue = verticalSectionsStackModel.getCurrentStackingValue();
                    verticalMarker.setValue(sectionValue);
                }
            }
        });       
    }

    private StackMapCrossSectionDialog createSectionsDialog(StackModel<MapImageResource> stackCrossSectionModel, SpectroscopySupervisor supervisor, MarkerPositionListener listener, boolean horizontal)
    {
        String stackType = stackCrossSectionModel.getStackType();
        String sourceName = stackCrossSectionModel.getSourceName();

        Preferences pref = Preferences.userNodeForPackage(ImagePlot.class).node(ImagePlot.class.getName()).node(stackType);

        List<Channel2D> channels = stackCrossSectionModel.getChannels();          

        ProcessableXYZDataset dataset = new MovieProcessableDataset(Channel2DDataset.getDatasets(channels, sourceName), stackCrossSectionModel.getDepthQuantity().getName());
        CustomizableImageRenderer renderer = new CustomizableImageRenderer(new StandardStyleTag(stackType), stackType);

        Channel2DPlot plot = new Channel2DPlot(dataset, renderer, Datasets.DENSITY_PLOT, pref);

        StackMapChart<Channel2DPlot> chart = new StackMapChart<>(plot, Datasets.DENSITY_PLOT);
        chart.setStackModel(stackCrossSectionModel);

        if(renderer instanceof GradientPaintReceiver)
        {
            GradientPaintReceiver receiver = getGradientPaintReceiver();
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

        StackMapCrossSectionDialog dialog = new StackMapCrossSectionDialog(this, supervisor, chart,
                stackCrossSectionModel, this.getStackModel(), listener, 
                horizontal);

        return dialog;    
    }   

    @Override
    public void cleanUp()
    {
        super.cleanUp();

        mapView.removeDataChangeListener(this);
        mapView.removeProfileReceiver(this);
        mapView.removeMapMarkerReceiver(this);
        mapView.removeDistanceMeasurementReceiver(this);
        mapView.removeROIReceiver(this);
    }

    @Override
    protected void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew) 
    {
        super.setConsistentWithMode(modeOld, modeNew);

        boolean isLineProfile = MouseInputModeStandard.PROFILE_LINE.equals(modeNew);
        boolean isPolyLineProfile = MouseInputModeStandard.PROFILE_POLYLINE.equals(modeNew);
        boolean isFreeHandProfile = MouseInputModeStandard.PROFILE_FREEHAND.equals(modeNew);

        extractLineSectionAction.putValue(Action.SELECTED_KEY, isLineProfile);
        extractPolyLineSectionAction.putValue(Action.SELECTED_KEY, isPolyLineProfile);
        extractFreeHandSectionAction.putValue(Action.SELECTED_KEY, isFreeHandProfile);
    }  

    @Override
    protected void updateROIAvailability( ) 
    {       
        super.updateROIAvailability();
        updateROIAvailabilityPrivate();
    }

    protected void updateROIAvailabilityPrivate()
    {
        boolean actionsROIBasedEnabled = !getDrawableROIs().isEmpty();

        recalculateROIsAction.setEnabled(actionsROIBasedEnabled);
    }

    private class ShowHorizontalSections extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowHorizontalSections() 
        {
            putValue(NAME, "Horizontal sections");
            putValue(MNEMONIC_KEY, KeyEvent.VK_H);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean show = (boolean) getValue(SELECTED_KEY);
            setHorizontalSectionsVisible(show);
        }
    }

    private class ShowVerticalSections extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowVerticalSections() 
        {
            putValue(NAME, "Vertical sections");
            putValue(MNEMONIC_KEY, KeyEvent.VK_V);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {

            boolean visible = (boolean) getValue(SELECTED_KEY);
            setVerticalSectionsVisible(visible);
        }
    }

    private class LinkProfilesWithMapsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LinkProfilesWithMapsAction() 
        {
            putValue(NAME, "Link with maps");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {

            boolean link = (boolean) getValue(SELECTED_KEY);
            setLinkProfilesWithMaps(link);
        }
    }

    private class LinkMapMarkersWithMapsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LinkMapMarkersWithMapsAction() 
        {
            putValue(NAME, "Link with maps");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {

            boolean link = (boolean) getValue(SELECTED_KEY);
            setLinkMapMarkersWithMaps(link);
        }
    }

    private class LinkROIsWithMapsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LinkROIsWithMapsAction() 
        {
            putValue(NAME, "Link with maps");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {

            boolean link = (boolean) getValue(SELECTED_KEY);
            setLinkROIsWithMaps(link);
        }
    }

    private class LinkDistanceMeasurementsWithMapsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public LinkDistanceMeasurementsWithMapsAction() 
        {
            putValue(NAME, "Link with maps");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {

            boolean linkWithMaps = (boolean) getValue(SELECTED_KEY);
            setLinkDistanceMeasurementsWithMaps(linkWithMaps);
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

    private class RecalculateROIsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RecalculateROIsAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Recalculate ROIs");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            recalculateFullDialog();
        }
    }

    private class ShowSectionGeometryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowSectionGeometryAction() 
        {           
            putValue(NAME, "Section geometry");
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);

            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);
            setProfileGeometryVisible(visible);
        }
    }

    private class ModifyMapMarkerStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyMapMarkerStyleAction() 
        {
            putValue(NAME, "Map marker style");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            mapMarkerEditor.setVisible(true);
        }
    }

    private class ModifyProfileStyleAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ModifyProfileStyleAction() 
        {
            putValue(NAME, "Section style");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            profileEditor.setVisible(true);
        }
    }

    private class HorizontalCrossSectionSupervisor extends SpectroscopySupervisorAdapter
    {  
        @Override
        public void jumpToResults(Point2D p)
        {
            if(StackMapView.this.horizontalSectionsStackModel != null)
            {
                double x = p.getX();
                double y = horizontalSectionsStackModel.getCurrentStackingValue();

                StackMapView.this.jumpToResults(new Point2D.Double(x, y));
            }
        }

        @Override
        public void jumpToResults(MapImageResource resource, Point2D p)
        {
            if(StackMapView.this.horizontalSectionsStackModel != null)
            {
                double x = p.getX();
                double y = horizontalSectionsStackModel.getCurrentStackingValue();

                StackMapView.this.jumpToResults(new Point2D.Double(x, y));
            }
        }
        @Override
        public void jumpToFigures(Point2D p)
        {
            if(StackMapView.this.horizontalSectionsStackModel != null)
            {
                double x = p.getX();
                double y = horizontalSectionsStackModel.getCurrentStackingValue();

                StackMapView.this.jumpToFigures(new Point2D.Double(x, y));
            }
        }

        @Override
        public void jumpToFigures(MapImageResource resource, Point2D p)
        {
            if(StackMapView.this.horizontalSectionsStackModel != null)
            {
                double x = p.getX();
                double y = horizontalSectionsStackModel.getCurrentStackingValue();

                StackMapView.this.jumpToFigures(resource, new Point2D.Double(x, y));
            }
        }
        @Override
        public Window getPublicationSite() 
        {
            return getAssociatedWindow();
        }  
    }

    private class VerticalCrossSectionSupervisor extends SpectroscopySupervisorAdapter
    {  
        @Override
        public void jumpToResults(Point2D p)
        {
            if(StackMapView.this.verticalSectionsStackModel != null)
            {
                double x = verticalSectionsStackModel.getCurrentStackingValue();
                double y = p.getX();

                StackMapView.this.jumpToResults(new Point2D.Double(x, y));
            }
        }

        @Override
        public void jumpToResults(MapImageResource resource, Point2D p)
        {
            if(StackMapView.this.verticalSectionsStackModel != null)
            {
                double x = verticalSectionsStackModel.getCurrentStackingValue();
                double y = p.getX();

                StackMapView.this.jumpToResults(resource, new Point2D.Double(x, y));
            }
        }
        @Override
        public void jumpToFigures(Point2D p)
        {
            if(StackMapView.this.verticalSectionsStackModel != null)
            {
                double x = verticalSectionsStackModel.getCurrentStackingValue();
                double y = p.getX();

                StackMapView.this.jumpToFigures(new Point2D.Double(x, y));
            }
        }

        @Override
        public void jumpToFigures(MapImageResource resource, Point2D p)
        {
            if(StackMapView.this.verticalSectionsStackModel != null)
            {
                double x = verticalSectionsStackModel.getCurrentStackingValue();
                double y = p.getX();

                StackMapView.this.jumpToFigures(resource, new Point2D.Double(x, y));
            }
        }

        @Override
        public Window getPublicationSite() 
        {
            return getAssociatedWindow();
        }  
    }

    private class MeasurePolyLineAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MeasurePolyLineAction() 
        {
            putValue(SHORT_DESCRIPTION, "<html>Polyline measurement<br>Left click to add vertex<br>Right click to end<html>");
            putValue(NAME, "Polyline measurement");
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
            putValue(SHORT_DESCRIPTION, "<html>Free hand measurement<br>Right click to end<html>");
            putValue(NAME, "Free hand measurement");
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

    private class ExtractLineSectionAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExtractLineSectionAction() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(
                    toolkit.getImage("Resources/CrossSections.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(NAME, "Line section");
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
    private class ExtractPolyLineSectionAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExtractPolyLineSectionAction()
        {
            putValue(SHORT_DESCRIPTION, "<html>Polyline section<br>Left click to add vertex<br>Right click to end</html>");
            putValue(NAME, "Polyline section");
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

    private class ExtractFreeHandSectionAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExtractFreeHandSectionAction() {
            putValue(SHORT_DESCRIPTION, "<html>Free hand section<br>Right click to end</html>");
            putValue(NAME, "Free hand section");
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

    private class ExportSectionsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExportSectionsAction() 
        {
            putValue(NAME, "Export sections");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            exportProfiles();
        }
    }

    public void exportProfiles()
    {
        MapChart<?> selectedChart = getSelectedChart();
        if(selectedChart == null)
        {
            return;
        }   

        Set<Profile> profileSet = new LinkedHashSet<>(profiles.values());

        ExportAnnotationModel<Profile> model = new ExportAnnotationModel<>(profileSet, "Section", "profile", true, true);

        this.exportAnnotationWizard = (exportAnnotationWizard == null) ? new ExportAnnotationWizard(getAssociatedWindow(), "Export sections to file") : exportAnnotationWizard;
        this.exportAnnotationWizard.showDialog(model);
    }

    private final class ImportSectionsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImportSectionsAction()
        {
            putValue(NAME, "Import sections");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
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
                    showErrorMessageDilog("Errors occured during importing sections");
                    return;
                }

                MapChart<?> chart = getSelectedChart();

                if(chart == null)
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


    private class ShowFlexibleSectionsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowFlexibleSectionsAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Flexible sections");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)        
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);
            setFlexibleSectionsVisible(visible);
        }
    }

    private static class ROIMapDialogReceiver implements ROIReceiver
    {
        private final MapView mapDialog;
        private final MapImageResource resource;

        private ROIMapDialogReceiver(MapView mapDialog, MapImageResource resource)
        {
            this.mapDialog = mapDialog;
            this.resource = resource;
        }

        @Override
        public void setROIs(Map<Object, ROIDrawable> rois) 
        {
            mapDialog.selectResource(resource);
            mapDialog.setROIs(rois);       
        }

        @Override
        public void addOrReplaceROI(ROIDrawable roi)
        {
            mapDialog.selectResource(resource);
            mapDialog.addOrReplaceROI(roi);        
        }

        @Override
        public void removeROI(ROIDrawable roi) 
        {
            mapDialog.selectResource(resource);
            mapDialog.removeROI(roi);
        }

        @Override
        public void changeROILabel(Object roiKey, String labelOld,
                String labelNew) {
            mapDialog.selectResource(resource);
            mapDialog.changeROILabel(roiKey, labelOld, labelNew);
        }
    }

    private static class DistanceMapDialogReceiver implements DistanceMeasurementReceiver
    {
        private final MapView mapDialog;
        private final MapImageResource resource;

        private DistanceMapDialogReceiver(MapView mapDialog, MapImageResource resource)
        {
            this.mapDialog = mapDialog;
            this.resource = resource;
        }

        @Override
        public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) {
            mapDialog.selectResource(resource);
            mapDialog.addOrReplaceDistanceMeasurement(measurement);        
        }

        @Override
        public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
        {
            mapDialog.selectResource(resource);
            mapDialog.removeDistanceMeasurement(measurement);
        }
    }
}
