
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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.gui.BasicCustomizableXYBaseChart;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.measurements.DistanceLineMeasurement;
import atomicJ.resources.AbstractResource;
import atomicJ.resources.DataModelResource;
import atomicJ.utilities.CollectionsUtilities;
import atomicJ.utilities.MetaMap;

public class BoxAndWhiskerResource extends AbstractResource implements DataModelResource
{
    private final static File DEFAULT_DIR = FileSystemView.getFileSystemView().getDefaultDirectory();

    private Map<String, BoxAndWhiskersTypeModel> typeModels;

    private final MetaMap<String, Object, QuantitativeSample> samplesToRefresh = new MetaMap<>();
    private final MetaMap<String, Object, DistanceLineMeasurement> distanceMeasurements = new MetaMap<>();

    public BoxAndWhiskerResource(Map<String, BoxAndWhiskersTypeModel> typeModels,
            String shortName, String longName)
    {
        this(typeModels, shortName, longName, DEFAULT_DIR);
    }

    public BoxAndWhiskerResource(Map<String, BoxAndWhiskersTypeModel> typeModels,
            String shortName, String longName, File defaultOutputLocation)
    {
        super(defaultOutputLocation, shortName, longName);
        this.typeModels = typeModels;

    }

    //SAMPLE REFRESHING

    public boolean isRefreshNeeded()
    {
        return !samplesToRefresh.isEmpty();
    }

    public MetaMap<String, Object, QuantitativeSample> getSamplesToRefresh()
    {
        return samplesToRefresh;
    }

    public void registerSamplesToRefresh(String type, Map<Object, QuantitativeSample> samplesChanged)
    {
        samplesToRefresh.putAll(type, samplesChanged);
    }

    public void registerSamplesToRefresh(Map<String, Map<Object, QuantitativeSample>> samplesChanged)
    {
        samplesToRefresh.putAll(samplesChanged);
    }

    public void clearSamplesToRefresh()
    {
        samplesToRefresh.clear();
    }

    public void reset(Map<String, Map<Object, QuantitativeSample>> samples)
    {
        this.typeModels = BoxAndWhiskersTypeModel.getBoxAndWhiskerTypeModels(samples);
        samplesToRefresh.clear();
    }

    public void refresh()
    {
        refresh(samplesToRefresh.getMapCopy());
        samplesToRefresh.clear();
    }

    private void refresh(Map<String, Map<Object, QuantitativeSample>> samplesChanged)
    {
        for(Entry<String, Map<Object, QuantitativeSample>> entry : samplesChanged.entrySet())
        {
            String key = entry.getKey();
            Map<Object, QuantitativeSample> samplesChangedInner = entry.getValue();
            BoxAndWhiskersTypeModel model = typeModels.get(key);

            if(model == null)
            {
                model = new BoxAndWhiskersTypeModel(key, samplesChangedInner);
                typeModels.put(key, model);
            }
            else
            {
                model.refreshSamples(samplesChangedInner);
            }
        }
    }

    public String getBoxPlotName()
    {
        return getShortName();
    }

    public BoxAndWhiskersTypeModel getModel(String key)
    {       
        BoxAndWhiskersTypeModel model = null;
        if(key != null)
        {
            model = typeModels.get(key);
        }

        return model;
    }

    public SampleCollection getSampleCollection(String type)
    {		
        BoxAndWhiskersTypeModel sampleModel = typeModels.get(type);

        Map<Object, QuantitativeSample> samples = sampleModel.getSamples();

        SampleCollection sampleCollection = 
                new StandardSampleCollection(CollectionsUtilities.convertKeysToStrings(samples), sampleModel.getType(), sampleModel.getType(), new File(System.getProperty("user.home")));

        return sampleCollection;
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    {
        List<SampleCollection> collections = new ArrayList<>();

        for(Entry<String, BoxAndWhiskersTypeModel> entry : typeModels.entrySet())
        {
            BoxAndWhiskersTypeModel sampleModel = entry.getValue();
            String type = sampleModel.getType();

            SampleCollection sampleCollection = new StandardSampleCollection(CollectionsUtilities.convertKeysToStrings(sampleModel.getSamples()), type, type, new File(System.getProperty("user.home")));
            collections.add(sampleCollection);
        }

        return collections;
    }

    public Map<String, Map<Object, QuantitativeSample>> getSamples() 
    {
        Map<String, Map<Object, QuantitativeSample>> allSamples = new LinkedHashMap<>();

        for(Entry<String, BoxAndWhiskersTypeModel> entry : typeModels.entrySet())
        {
            BoxAndWhiskersTypeModel sampleModel = entry.getValue();
            String type = sampleModel.getType();

            Map<Object, QuantitativeSample> samples = new LinkedHashMap<>();
            samples.putAll(sampleModel.getSamples());

            allSamples.put(type, samples);
        }

        return allSamples;
    }

    public int getMeasurementCount(String type)
    {
        int count = distanceMeasurements.size(type);
        return count;     
    }

    public Map<Object, DistanceLineMeasurement> getDistanceMeasurements(Object type)
    {
        Map<Object, DistanceLineMeasurement> measurtementsForType = distanceMeasurements.get(type);         
        return new LinkedHashMap<>(measurtementsForType);
    }

    public void addOrReplaceDistanceMeasurement(String type, DistanceLineMeasurement measurement)
    {
        distanceMeasurements.put(type, measurement.getKey(), measurement);    
    }

    public void removeDistanceMeasurement(String type, DistanceLineMeasurement measurement)
    {
        distanceMeasurements.remove(type, measurement.getKey());
    }

    public Map<String, CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> draw()
    {
        Map<String, CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> charts = new LinkedHashMap<>();

        for(Entry<String, BoxAndWhiskersTypeModel> entry : typeModels.entrySet())
        {            
            String quantityName = entry.getKey();                     
            Map<Object, QuantitativeSample> samplesForQuantity = entry.getValue().getSamples();

            CustomizableXYBaseChart<BoxAndWhiskerXYPlot> chart = buildChart(quantityName, samplesForQuantity);

            if(chart != null)
            {
                charts.put(quantityName, chart);
            }
        }

        return charts;
    }

    public static CustomizableXYBaseChart<BoxAndWhiskerXYPlot> buildChart(String type,  Map<Object, QuantitativeSample> samples)
    {        
        XYBoxAndWhiskerIndexDataset dataset = 
                XYBoxAndWhiskerIndexDataset.getDataset(type, samples);

        BoxAndWhiskerXYPlot plot = new BoxAndWhiskerXYPlot(dataset, "");             
        CustomizableXYBaseChart<BoxAndWhiskerXYPlot> chart = new BasicCustomizableXYBaseChart<>(plot, Datasets.BOX_AND_WHISKER_PLOT);

        return chart;
    }
}