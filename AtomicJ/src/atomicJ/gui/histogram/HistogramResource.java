
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

package atomicJ.gui.histogram;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.resources.AbstractResource;
import atomicJ.resources.DataModelResource;
import atomicJ.utilities.MetaMap;


public class HistogramResource extends AbstractResource implements DataModelResource
{
    private final static File DEFAULT_DIRECTORY = FileSystemView.getFileSystemView().getDefaultDirectory();

    private final Map<String, HistogramSampleModel> sampleModels;
    private final MetaMap<String, Object, DistanceMeasurementDrawable> distanceMeasurements = new MetaMap<>();

    public HistogramResource(Map<String, HistogramSampleModel> sampleModels, String shortName, String longName)
    {
        this(sampleModels, shortName, longName, DEFAULT_DIRECTORY);
    }

    public HistogramResource(Map<String, HistogramSampleModel> sampleModels, String shortName, String longName, File defaultOutputLocation)
    {
        super(defaultOutputLocation, shortName, longName);
        this.sampleModels = sampleModels;
    }

    public static HistogramResource buildHistogramResource(String shortName, String longName, Map<String, QuantitativeSample> unionROISamples)
    {
        Map<String, HistogramSampleModel> sampleModels = new LinkedHashMap<>();

        for(Entry<String, QuantitativeSample> entry : unionROISamples.entrySet())
        {
            String type = entry.getKey();

            HistogramSampleModel sampleModel = HistogramSampleModel.getSampleModelFromCopy(entry.getValue());
            sampleModels.put(type, sampleModel);
        }

        HistogramResource histogramResource = new HistogramResource(sampleModels, shortName, longName);

        return histogramResource;
    }

    public String getHistogramName()
    {
        return getShortName();
    }

    public HistogramSampleModel getModel(String key)
    {
        if(key == null)
        {
            return null;
        }
        return sampleModels.get(key);
    }

    public void addOrReplaceModel(String key, HistogramSampleModel model)
    {
        sampleModels.put(key, model);
    }

    public Map<String, HistogramSampleModel> getModels()
    {
        return new LinkedHashMap<>(sampleModels);
    }

    public SampleCollection getSampleCollection(String type)
    {		
        HistogramSampleModel sampleModel = sampleModels.get(type);

        QuantitativeSample sample = sampleModel.getSample();
        String sampleName = sampleModel.getName();

        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();
        samples.put(sampleName, sample);
        SampleCollection sampleCollection = new StandardSampleCollection(samples, sampleName, sampleName, new File(System.getProperty("user.home")));

        return sampleCollection;
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    {
        List<SampleCollection> collections = new ArrayList<>();
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        for(Entry<String, HistogramSampleModel> entry : sampleModels.entrySet())
        {
            HistogramSampleModel sampleModel = entry.getValue();

            QuantitativeSample sample = sampleModel.getSample();
            String sampleName = sampleModel.getName();

            samples.put(sampleName, sample);

        }

        String shortName = getShortName();
        SampleCollection sampleCollection = new StandardSampleCollection(samples, shortName, shortName, new File(System.getProperty("user.home")));
        collections.add(sampleCollection);

        return collections;
    }

    public int getMeasurementCount(String type)
    {
        int count = distanceMeasurements.size(type);
        return count;     
    }

    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements(Object type)
    {
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(type);         
        return new LinkedHashMap<>(measurtementsForType);
    }

    public void addOrReplaceDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.put(type, measurement.getKey(), measurement);    
    }

    public void removeDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.remove(type, measurement.getKey());
    }

    public Map<String,ChannelChart<HistogramPlot>> draw()
    {
        Map<String,ChannelChart<HistogramPlot>> charts = new LinkedHashMap<>();

        for(Entry<String, HistogramSampleModel> entry : sampleModels.entrySet())
        {            
            ChannelChart<HistogramPlot> chart = entry.getValue().getHistogram();
            charts.put(entry.getKey(), chart);            
        }

        return charts;
    }
}