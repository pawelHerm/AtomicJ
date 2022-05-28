
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

package atomicJ.gui.boxplots;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.data.QuantitativeSample;
import atomicJ.gui.AbstractModel;
import atomicJ.utilities.CollectionsUtilities;
import atomicJ.utilities.Validation;

public class BoxAndWhiskersTypeModel extends AbstractModel
{  
    private Map<Object, QuantitativeSample> samples;  
    private final String type;

    public BoxAndWhiskersTypeModel(String type, Map<Object, QuantitativeSample> samples)
    {
        Validation.requireNonNullParameterName(samples, "samples");

        this.type = type;
        this.samples = new LinkedHashMap<>(samples); 

        moveCollectiveSamplesToEnd();
        //        this.descriptiveStatistics = buildDescriptiveStatistics();
    }

    public static Map<String, BoxAndWhiskersTypeModel> getBoxAndWhiskerTypeModels(Map<String, Map<Object, QuantitativeSample>> data)
    {
        Map<String, BoxAndWhiskersTypeModel> allQuantitativeSamples = new LinkedHashMap<>();

        for(Entry<String, Map<Object, QuantitativeSample>> entry : data.entrySet())
        {
            String sampleName = entry.getKey();
            Map<Object, QuantitativeSample> samples = entry.getValue();

            BoxAndWhiskersTypeModel boxWhiskerSamples =
                    new BoxAndWhiskersTypeModel(sampleName, samples);      
            allQuantitativeSamples.put(sampleName, boxWhiskerSamples);
        }

        return allQuantitativeSamples;
    }

    public void refreshSamples(Map<Object, QuantitativeSample> samplesChanged)
    {
        for(Entry<Object, QuantitativeSample> entry : samplesChanged.entrySet())
        {
            String key = entry.getKey().toString();
            QuantitativeSample sample = entry.getValue();

            if(sample == null)
            {
                this.samples.remove(key);
            }
            else if(samples.containsKey(key))
            {
                this.samples = CollectionsUtilities.replaceMapValue(this.samples, key, sample);
            }
            else
            {
                this.samples.put(key, sample);
            }
        }

        moveCollectiveSamplesToEnd();
    }

    private void moveCollectiveSamplesToEnd()
    {
        List<Object> collectiveKeys = new ArrayList<>();

        for(Object key : this.samples.keySet())
        {            
            if(key.toString().startsWith("All"))
            {
                collectiveKeys.add(key);
            }
        }
        //changes the order (LinkedHashMap has insertion order) (I know, uglyy, uhrr..........)

        for(Object collectiveKey : collectiveKeys )
        {
            QuantitativeSample sample = this.samples.remove(collectiveKey);         
            this.samples.put(collectiveKey, sample);
        }
    }

    public String getType()
    {
        return type;
    }

    public Map<Object, QuantitativeSample> getSamples()
    {
        return samples;
    }
    //
    //    public Map<String, DescriptiveStatistics> getDescriptiveStatistics()
    //    {
    //        return descriptiveStatistics;
    //    }
    //
    //    private Map<String, DescriptiveStatistics> buildDescriptiveStatistics()
    //    {
    //        Map<String, DescriptiveStatistics> allStatistics = new LinkedHashMap<>();
    //
    //        for(Entry<String, QuantitativeSample> entry : samples.entrySet())
    //        {
    //            String key = entry.getKey();
    //            QuantitativeSample sample = entry.getValue();
    //            DescriptiveStatistics statistics = new DescriptiveStatistics(sample.getMagnitudes(), key);
    //            allStatistics.put(key, statistics);
    //        }
    //
    //        return allStatistics;
    //    }

    public void applyFunction(UnivariateFunction f)
    {
        Map<Object, QuantitativeSample> samplesTransformed = new LinkedHashMap<>();

        for(Entry<Object, QuantitativeSample> entry : samples.entrySet())
        {
            Object key = entry.getKey();
            QuantitativeSample sampleOld = entry.getValue();
            QuantitativeSample sampleTransformed = sampleOld.applyFunction(f);
            samplesTransformed.put(key, sampleTransformed);
        }

        this.samples = samplesTransformed;
        //        this.descriptiveStatistics = buildDescriptiveStatistics();
    }

    public void applyFunction(UnivariateFunction f, String functionName)
    {
        Map<Object, QuantitativeSample> samplesTransformed = new LinkedHashMap<>();

        for(Entry<Object, QuantitativeSample> entry : samples.entrySet())
        {
            Object key = entry.getKey();
            QuantitativeSample sampleOld = entry.getValue();
            QuantitativeSample sampleTransformed = sampleOld.applyFunction(f, functionName);
            samplesTransformed.put(key, sampleTransformed);
        }

        this.samples = samplesTransformed;
        //        this.descriptiveStatistics = buildDescriptiveStatistics();
    }
}
