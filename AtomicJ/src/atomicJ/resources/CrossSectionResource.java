
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

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.data.Channel1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionMarkerParameters;


public class CrossSectionResource extends AbstractResource implements DataModelResource
{
    private static final double TOLERANCE = 1e-12;

    //the key in the outer map is the type, the key of the inner map is the identifier of the profile (i.e. 0, 1, 2, 4)
    private Map<String, Map<Object, ChannelSectionLine>> crossSections = new LinkedHashMap<>();
    private final Map<Object, List<Double>> crossSectionMarkers = new LinkedHashMap<>();

    private CrossSectionSettings defaultSettings = new CrossSectionSettings();
    private final Map<String, CrossSectionSettings> crossSectionSettings = new LinkedHashMap<>();

    private final Map<String, Map<Object, DistanceMeasurementDrawable>> distanceMeasurements = new LinkedHashMap<>();

    private final Channel2DResource imageResource;


    public CrossSectionResource(Channel2DResource imageResource, String shortName, String longName, File outputLocation)
    {
        super(outputLocation, shortName, longName);
        this.imageResource = imageResource;
    }

    public CrossSectionResource(CrossSectionResource resourceOld, Channel2DResource densityResourceNew)
    {
        super(resourceOld);
        this.imageResource = densityResourceNew;

        Map<String, Map<Object, ChannelSectionLine>> crossSectionsOld = resourceOld.crossSections;

        for(Entry<String, Map<Object, ChannelSectionLine>> entry : crossSectionsOld.entrySet())
        {
            String type = entry.getKey();
            Map<Object, ChannelSectionLine> crossSectionsForType = new LinkedHashMap<>(crossSectionsOld.get(type));
            this.crossSections.put(type, crossSectionsForType);
        }

        Map<String, CrossSectionSettings> crossSectionSettingsOld = resourceOld.crossSectionSettings;
        for(Entry<String, CrossSectionSettings> entry : crossSectionSettingsOld.entrySet())
        {
            String type = entry.getKey();
            CrossSectionSettings settingsOld = entry.getValue();
            CrossSectionSettings settingsCopy = new CrossSectionSettings(settingsOld);
            this.crossSectionSettings.put(type, settingsCopy);
        }
    }

    public CrossSectionResource(CrossSectionResource resourceOld, Channel2DResource densityResourceNew, Set<String> typesToRetain)
    {
        super(resourceOld);
        this.imageResource = densityResourceNew;


        Map<String, Map<Object, ChannelSectionLine>> crossSectionsOld = resourceOld.crossSections;

        for(Entry<String, Map<Object, ChannelSectionLine>> entry : crossSectionsOld.entrySet())
        {
            String type = entry.getKey();
            if(typesToRetain.contains(type))
            {
                Map<Object, ChannelSectionLine> crossSectionsForType = new LinkedHashMap<>(crossSectionsOld.get(type));
                this.crossSections.put(type, crossSectionsForType);
            }
        }

        Map<String, CrossSectionSettings> crossSectionSettingsOld = resourceOld.crossSectionSettings;
        for(Entry<String, CrossSectionSettings> entry : crossSectionSettingsOld.entrySet())
        {
            String type = entry.getKey();
            if(typesToRetain.contains(type))
            {
                CrossSectionSettings settingsOld = entry.getValue();
                CrossSectionSettings settingsCopy = new CrossSectionSettings(settingsOld);
                this.crossSectionSettings.put(type, settingsCopy);
            }          
        }
    }

    public Channel2DResource getImageResource()
    {
        return imageResource;
    }

    public List<Double> moveMarker(Object profileKey, int markerIndex, double positionNew)
    {
        List<Double> markers = crossSectionMarkers.get(profileKey);        

        if(markers == null)
        {
            return Collections.emptyList();
        }
        else
        {
            markers.set(markerIndex, Double.valueOf(positionNew));
        }

        return new ArrayList<>(markers);
    }

    public List<Double> addMarker(Object profileKey, double position)
    {
        List<Double> markers = crossSectionMarkers.get(profileKey);

        if(markers == null)
        {
            markers = new ArrayList<>();
            crossSectionMarkers.put(profileKey, markers);
        }
        markers.add(Double.valueOf(position));

        return new ArrayList<>(markers);
    }

    public List<Double> setProfileMarkerPositions(Object profileKey, List<Double> markerPositions) 
    {
        List<Double> markers = new ArrayList<>(markerPositions);

        crossSectionMarkers.put(profileKey, markers);
        return new ArrayList<>(markers);
    }


    public List<Double> removeMarker(Object profileKey, double markerPosition)
    {
        List<Double> markers = crossSectionMarkers.get(profileKey);

        if(markers == null)
        {
            return Collections.emptyList();
        }

        int n = markers.size();
        int index = -1;

        for(int i = 0; i<n; i++)
        {
            boolean found = Math.abs(markers.get(i) - markerPosition) < TOLERANCE;
            if(found)
            {                    
                index = i;
                break;
            }
        }

        if(index>=0)
        {   
            markers.remove(index);            
        }

        return new ArrayList<>(markers);
    }

    public List<Double> removeAllMarkers(Object profileKey)
    {
        List<Double> markers = crossSectionMarkers.get(profileKey);

        if(markers == null)
        {
            return Collections.emptyList();

        }
        else
        {
            markers.clear();
        }

        return new ArrayList<>(markers);
    }

    public List<Double> getCrossSectionMarkers(Object profileKey)
    {
        return crossSectionMarkers.get(profileKey);
    }

    public Map<Object, List<Double>> getCrossSectionMarkers()
    {
        return crossSectionMarkers;
    }

    public double getClosestLegalMarkerPosition(Object profileKey, double position)
    {
        if(position < 0)
        {
            return 0;
        }

        Map<Object, ChannelSectionLine> crossSectionsForType = crossSections.entrySet().iterator().next().getValue();

        ChannelSectionLine crossSection = crossSectionsForType.get(profileKey);

        double length = crossSection.getLength();
        double legalPosition = (position > length) ? length : position;

        return legalPosition;
    }

    public Map<Object, List<CrossSectionMarkerParameters>> getMarkerParameters(String type)
    {
        CrossSectionSettings settings = getCrossSectionSettings(type);

        Map<Object, List<CrossSectionMarkerParameters>> allMarkerParameters = new LinkedHashMap<>();

        Map<Object, ChannelSectionLine> crossSectionsForType = crossSections.get(type);

        for(Entry<Object, List<Double>> entry : crossSectionMarkers.entrySet())
        {
            Object profileKey = entry.getKey();
            List<Double> positions = entry.getValue();

            ChannelSectionLine channelCrossection = crossSectionsForType.get(profileKey);

            if(channelCrossection != null)
            {
                List<CrossSectionMarkerParameters> parametersForProfile = new ArrayList<>();
                allMarkerParameters.put(profileKey, parametersForProfile);
                for(double d : positions)
                {
                    double z = channelCrossection.getValue(d, settings);

                    CrossSectionMarkerParameters par = new CrossSectionMarkerParameters(new Point2D.Double(d, z), type, profileKey);
                    parametersForProfile.add(par);
                }

            }
        }

        return allMarkerParameters;
    }


    public CrossSectionMarkerParameters getMarkerParameters(String type, Object profileKey, double d)
    {
        CrossSectionSettings settings = getCrossSectionSettings(type);

        Map<Object, ChannelSectionLine> crossSectionsForType = crossSections.get(type);
        if(crossSectionsForType == null)
        {
            return null;
        }

        ChannelSectionLine channelCrossection = crossSectionsForType.get(profileKey);

        if(channelCrossection != null)
        {
            double z = channelCrossection.getValue(d, settings);

            CrossSectionMarkerParameters markerParameters = new CrossSectionMarkerParameters(new Point2D.Double(d, z), type, profileKey);
            return markerParameters;

        }
        return null;
    }

    public boolean canNewMarkerBeAdded(String type, double zSelected, double d, double margin)
    {
        CrossSectionMarkerParameters markerParemeters = getKeyOfSelectedCrossSectionChannel(type, zSelected, d, margin);
        boolean canBeAdded = (markerParemeters != null);

        return canBeAdded;
    }

    public CrossSectionMarkerParameters getKeyOfSelectedCrossSectionChannel(String type, double zSelected, double d, double margin)
    {
        CrossSectionSettings settings = getCrossSectionSettings(type);

        Map<Object, ChannelSectionLine> crossSectionsForType = crossSections.get(type);

        Object selectedKey = null;
        double minDistance = margin;
        double realZ = 0;

        for(Entry<Object, ChannelSectionLine> entry : crossSectionsForType.entrySet())
        {
            Object key = entry.getKey();
            ChannelSectionLine channelCrossection = entry.getValue();

            double z = channelCrossection.getValue(d, settings);

            double distance = Math.abs(z - zSelected);


            if(distance <= minDistance)
            {
                minDistance = distance;
                selectedKey = key;
                realZ = z;
            }
        }

        if(selectedKey != null)
        {
            CrossSectionMarkerParameters markerParameters = new CrossSectionMarkerParameters(new Point2D.Double(d, realZ), type, selectedKey);
            return markerParameters;
        }
        return null;
    }

    public Map<String, Integer> getPointCounts()
    {
        Map<String, Integer> pointCounts = new LinkedHashMap<>();

        for(Entry<String, CrossSectionSettings> entry: crossSectionSettings.entrySet())
        {
            String type = entry.getKey();
            CrossSectionSettings settings = entry.getValue();

            int pointCount = settings.getPointCount();

            pointCounts.put(type, pointCount);
        }

        return pointCounts;
    }

    public int getPointCount(String type)
    {
        CrossSectionSettings settings = getCrossSectionSettings(type);
        int count = settings.getPointCount();

        return count;
    }

    public void setPointCount(String type, int pointCountNew)
    {
        CrossSectionSettings settings = getCrossSectionSettings(type);
        settings.setPointCount(pointCountNew);
    }

    public void setDefaultCrossSectionSettings(CrossSectionSettings settings)
    {
        this.defaultSettings = new CrossSectionSettings(settings);
    }

    public CrossSectionSettings getCrossSectionSettings(String type)
    {
        CrossSectionSettings settings = crossSectionSettings.get(type);

        if(settings == null)
        {
            settings = new CrossSectionSettings(defaultSettings);
            crossSectionSettings.put(type, settings);
        }

        return settings;
    }

    public void setCrossSectionSettings(String type, CrossSectionSettings settings)
    {
        crossSectionSettings.put(type, new CrossSectionSettings(settings));
    }

    public void setCrossSectionSettings(Set<String> types, CrossSectionSettings settings)
    {
        for(String type: types)
        {
            crossSectionSettings.put(type, new CrossSectionSettings(settings));
        }
    }

    public Map<String, CrossSectionSettings> getCrossSectionSettings()
    {
        Map<String, CrossSectionSettings> settings = new LinkedHashMap<>(crossSectionSettings);

        return settings;
    }


    public void setCrossSectionSettings(Map<String, CrossSectionSettings> allSettings)
    {
        for(Entry<String, CrossSectionSettings> entry: allSettings.entrySet())
        {
            String type = entry.getKey();
            CrossSectionSettings settings = new CrossSectionSettings(entry.getValue());
            crossSectionSettings.put(type, settings);
        }
    }

    public boolean isEmpty()
    {
        int count = crossSections.size();
        boolean empty = (count == 0);

        return empty;
    }

    public int getCrossSectionCount()
    {
        int count = crossSections.size();
        return count;
    }

    public void addOrReplaceCrossSections(Map<String, ChannelSectionLine> crossSectionsNew)
    {        
        for(Entry<String, ChannelSectionLine> p : crossSectionsNew.entrySet())
        {
            String type = p.getKey();
            ChannelSectionLine crossSectionNew = p.getValue();

            Map<Object,ChannelSectionLine> crossSectionsForType = crossSections.get(type);

            if(crossSectionsForType == null)
            {
                crossSectionsForType = new LinkedHashMap<>();
                crossSections.put(type, crossSectionsForType);
            }
            crossSectionsForType.put(crossSectionNew.getKey(), crossSectionNew);

            if(!crossSectionSettings.containsKey(type))
            {
                CrossSectionSettings settings = new CrossSectionSettings(defaultSettings);
                crossSectionSettings.put(type, settings);
            }
        }
    }

    public Map<String, ChannelSectionLine> removeCrossSections(Object key)
    {
        Map<String, ChannelSectionLine> removedCrossSections = new LinkedHashMap<>();

        for(Entry<String, Map<Object, ChannelSectionLine>> outerEntry: crossSections.entrySet())
        {
            String type = outerEntry.getKey();

            Map<Object, ChannelSectionLine> innerMap = outerEntry.getValue();
            ChannelSectionLine crosssection = innerMap.get(key);
            if(crosssection != null)
            {
                removedCrossSections.put(type, crosssection);
                innerMap.remove(key);
            }
        }
        return removedCrossSections;
    }

    public void setCrossSections(Map<String, Map<Object, ChannelSectionLine>> crossSectionsNew)
    {
        crossSections = new LinkedHashMap<>(crossSectionsNew);

        for(String type : crossSections.keySet())
        {
            if(!crossSectionSettings.containsKey(type))
            {
                CrossSectionSettings settings = new CrossSectionSettings(defaultSettings);
                crossSectionSettings.put(type, settings);
            }
        }
    }

    public Map<Object, ChannelSectionLine> getCrossSections(String type)
    {
        Map<Object, ChannelSectionLine> sections = crossSections.get(type);
        Map<Object, ChannelSectionLine> sectionsCopy = (sections != null) ? new LinkedHashMap<>(sections) : Collections.<Object, ChannelSectionLine>emptyMap();

        return sectionsCopy;
    }

    public Channel1D getCrossSectionCurve(String type, Object key, CrossSectionSettings sectionSettings)
    {
        ChannelSectionLine profile = crossSections.get(type).get(key);
        return profile.getCrossSection(sectionSettings);
    }

    public Map<Object, Channel1D> getCrossSectionCurves(String type)
    {
        return getCrossSectionCurves(type, getCrossSectionSettings(type));
    }

    public Map<Object, Channel1D> getCrossSectionCurves(String type, CrossSectionSettings sectionSettings)
    {
        Map<Object, Channel1D> curves = new LinkedHashMap<>();
        Map<Object, ChannelSectionLine> profilesForType = crossSections.get(type);

        if(profilesForType != null)
        {
            for(Entry<Object, ChannelSectionLine> entry: profilesForType.entrySet())
            {
                Object key = entry.getKey();
                ChannelSectionLine profile = entry.getValue();
                Channel1D crossSection = profile.getCrossSection(sectionSettings);

                curves.put(key, crossSection);
            }
        }

        return curves;
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    {
        List<SampleCollection> collections = new ArrayList<>();

        for(Entry<String, Map<Object, ChannelSectionLine>> entry : crossSections.entrySet())
        {
            String type = entry.getKey();
            CrossSectionSettings settings = getCrossSectionSettings(type);

            Map<Object, ChannelSectionLine> crossSectionsForType = entry.getValue();

            int pointCount = getPointCount(type);

            Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

            for(Entry<Object, ChannelSectionLine> innerEntry : crossSectionsForType.entrySet())
            {
                ChannelSectionLine crossection = innerEntry.getValue();
                samples.putAll(crossection.getCrossSectionaSamples(pointCount, settings));
            }

            SampleCollection sampleCollection = new StandardSampleCollection(samples, type, type, getDefaultOutputLocation());

            collections.add(sampleCollection);
        }
        return collections;
    }

    public int getMeasurementCount(String type)
    {
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(type);        

        int count = measurtementsForType == null ? 0 : measurtementsForType.size();

        return count;     
    }

    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements(Object type)
    {
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(type);    
        if(measurtementsForType == null)
        {
            return new LinkedHashMap<>();
        }
        else
        {
            return new LinkedHashMap<>(measurtementsForType);
        }
    }

    public void addOrReplaceDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(type);        

        if(measurtementsForType == null)
        {   
            measurtementsForType = new LinkedHashMap<>();
            distanceMeasurements.put(type, measurtementsForType);
        }       

        measurtementsForType.put(measurement.getKey(), measurement);    

    }
    public void removeDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        Map<Object, DistanceMeasurementDrawable> measurementsForType = distanceMeasurements.get(type);

        if(measurementsForType != null)
        {
            measurementsForType.remove(measurement.getKey());
        }
    }
}
