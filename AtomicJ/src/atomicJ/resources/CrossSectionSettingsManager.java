
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class CrossSectionSettingsManager 
{	
    private CrossSectionSettings defaultSettings = new CrossSectionSettings();
    private final Map<String, CrossSectionSettings> crossSectionSettings = new LinkedHashMap<>();

    public CrossSectionSettingsManager()
    {}

    public CrossSectionSettingsManager(CrossSectionSettingsManager resourceOld)
    {			
        Map<String, CrossSectionSettings> crossSectionSettingsOld = resourceOld.crossSectionSettings;
        for(Entry<String, CrossSectionSettings> entry : crossSectionSettingsOld.entrySet())
        {
            String type = entry.getKey();
            CrossSectionSettings settingsOld = entry.getValue();
            CrossSectionSettings settingsCopy = new CrossSectionSettings(settingsOld);
            this.crossSectionSettings.put(type, settingsCopy);
        }
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

    public void addTypeIfNecessary(String type)
    {
        if(!crossSectionSettings.containsKey(type))
        {
            CrossSectionSettings settings = new CrossSectionSettings(defaultSettings);
            crossSectionSettings.put(type, settings);
        }
    }

    public void addTypesIfNecessary(Set<String> types)
    {
        for(String type : types)
        {
            if(!crossSectionSettings.containsKey(type))
            {
                CrossSectionSettings settings = new CrossSectionSettings(defaultSettings);
                crossSectionSettings.put(type, settings);
            }
        }
    }
}
