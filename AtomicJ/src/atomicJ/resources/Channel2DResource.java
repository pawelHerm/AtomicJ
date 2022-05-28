
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

package atomicJ.resources;


import java.awt.Window;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.data.DataAxis1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.MapMarker;
import atomicJ.gui.PropertyChangeSource;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionsReceiver;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.rois.ROI;
import atomicJ.utilities.MetaMap;

public interface Channel2DResource extends BasicChannel2DResource, PropertyChangeSource
{
    public Map<Object, QuantitativeSample> getSamples(String type);
    public Map<String, QuantitativeSample> getROIUnionSamples();

    public MetaMap<String, Object, QuantitativeSample> changeROILabel(Object roiKey, String labelOld, String labelNew);

    public List<SampleCollection> getSampleCollection(boolean includeCoordinates);
    public List<SampleCollection> getSampleCollection(Map<Object, ? extends ROI> shapes, boolean includeCoordinates);
    public List<SampleCollection> getSampleCollection2(Map<Object, ? extends ROI> shapes, boolean includeCoordinates);

    public List<SampleCollection> getROISampleCollections(boolean includeCoordinates);
    public List<SampleCollection> getROISampleCollections2(boolean includeCoordinates);

    public Map<String, Map<Object, QuantitativeSample>> getSamples(boolean includeCoordinates);

    public Map<String, Map<Object, QuantitativeSample>> getSamplesForROIs(boolean includeCoordinates);
    public Map<Object, QuantitativeSample> getSamplesForROIs(String type);


    //DISTANCE MEASUREMENTS

    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements(); 
    public Map<Object, DistanceShapeFactors> getDistanceMeasurementGeometries();
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement);
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement);
    public void setDistanceMeasurements(Map<Object, DistanceMeasurementDrawable> measurements);


    //PROFILES - CROSS-SECTIONS

    public Map<Object, Profile> getProfiles();   
    public Profile getProfile(Object key);

    public Map<Object, DistanceShapeFactors> getProfileGemetries();
    public void addOrReplaceProfile(Profile profile, CrossSectionsReceiver receiver, CrossSectionResource resource,  Window taskParent);
    public Map<String, ChannelSectionLine> addOrReplaceProfile(Profile profile);
    public Map<String, ChannelSectionLine> removeProfile(Profile profile);
    public Map<String, Map<Object, ChannelSectionLine>> setProfiles(Map<Object, Profile> profiles);

    public boolean addProfileKnob(Object profileKey, double knobPosition);
    public boolean moveProfileKnob(Object profileKey, int knobIndex, double knobPositionNew);
    public boolean removeProfileKnob(Object profileKey, double knobPosition);
    public boolean setProfileKnobs(Object profileKey, List<Double> knobPositions);

    public Map<String, ChannelSectionLine> getHorizontalCrossSections(double level, Object key, String name, String type);
    public Map<String, ChannelSectionLine> getVerticalCrossSections(double level, Object key, String name, String type);

    public CrossSectionResource getCrossSectionResource();
    public List<ChannelSectionLine> getHorizontalCrossSections(DataAxis1D axis, Object key, String name, String type);
    public List<ChannelSectionLine> getVerticalCrossSections(DataAxis1D axis, Object key, String name, String type);   

    public Map<String, ChannelSectionLine> getCrossSections(Profile profile);
    public Map<String, ChannelSectionLine> getCrossSections(Profile profile, String type);
    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Map<Object, Profile> profiles, String type);
    public Map<String, Map<Object, ChannelSectionLine>> getCrossSections(Map<Object, Profile> profiles, Set<String> types);

    //MAP MARKERS

    public Map<Object, MapMarker> getMapMarkers();   
    public void addOrReplaceMapMarker(MapMarker mapMarker);
    public boolean removeMapMarker(MapMarker mapMarker);
    public void setMapMarkers(Map<Object, MapMarker> mapMarkersNew);
}
