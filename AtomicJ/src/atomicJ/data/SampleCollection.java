
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

package atomicJ.data;

import java.io.File;
import java.util.List;
import java.util.Map;


public interface SampleCollection 
{
    public String getName();
    public String getShortName();
    public Object getKey();

    public QuantitativeSample getSample(String type);
    public List<String> getSampleTypes();

    public List<String> getIncludedSampleTypes();

    public boolean isKeyIncluded(String key);
    public void setKeyIncluded(String key, boolean included);
    public void setKeysIncluded(boolean included);

    public boolean isCollectionIncluded();
    public void setCollectionIncluded(boolean included);

    public Map<String, QuantitativeSample> getAllSamples();
    public Map<String, QuantitativeSample> getIncludedSamples();
    public Map<String, QuantitativeSample> getIncludedSamples(int sizeLimit);

    public File getDefaultOutputDirectory();
}
