
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

package atomicJ.gui.statistics;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.*;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.DecimalUnitTableCellRenderer;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.rois.ROISampleManager;


public class StatisticsTable extends StandardNumericalTable
{	
    private static final long serialVersionUID = 1L;

    public StatisticsTable(StatisticsTableModel model)
    {
        super(model, new DecimalUnitTableCellRenderer(model.getDataUnits(), model.getDisplayedUnits()), false, true);	
    }

    public void clearData()
    {
        StatisticsTableModel model = getModel();
        model.clearData();
    }

    @Override
    public StatisticsTableModel getModel()
    {
        StatisticsTableModel model = (StatisticsTableModel)super.getModel();
        return model;
    }

    @Override
    public void setModel(TableModel model) 
    {
        if(!(model instanceof StatisticsTableModel))
        {
            throw new IllegalArgumentException("Only StatisticsTableModel can be used with StatisticsTable");
        }

        StatisticsTableModel oldModel = getModel();
        if(oldModel != null)
        {
            oldModel.removePropertyChangeListener(this);
        }

        StatisticsTableModel statModel = (StatisticsTableModel)model;
        super.setModel(statModel);
        statModel.addPropertyChangeListener(this);
    }

    public static Map<String, StatisticsTable>  getStatisticsTables(List<SampleCollection> sampleCollections, String type)
    {
        Map<String, StatisticsTable> tables = new LinkedHashMap<>();

        for(SampleCollection sampleCollection : sampleCollections)
        {
            String sampleCollectionName = sampleCollection.getShortName();

            Map<String, QuantitativeSample> samples = sampleCollection.getAllSamples();
            File defaultOutputFile = sampleCollection.getDefaultOutputDirectory();

            for(QuantitativeSample sample: samples.values())
            {
                Quantity quantity = sample.getQuantity();
                String quantityName = quantity.getName();
                PrefixedUnit quantityUnit = quantity.getUnit();				
                StatisticsTable table = tables.get(quantityName);
                SimpleSampleStatisticsModel model;
                if(table == null)
                {
                    model = new SimpleSampleStatisticsModel(quantityUnit, defaultOutputFile);
                    table = new StatisticsTable(model);
                    tables.put(quantityName, table);
                }
                else
                {
                    model = (SimpleSampleStatisticsModel)table.getModel();
                }

                model.addOrUpdateSample(sampleCollectionName + type, sampleCollectionName + type, sample);
            }
        }

        return tables;
    }

    public static Map<String, StatisticsTable> getStatisticsTablesForRois(List<ROISampleManager> sampleCollections)
    {
        Map<String, StatisticsTable> tables = new LinkedHashMap<>();

        for(ROISampleManager sampleCollection : sampleCollections)
        {
            String sampleCollectionName = sampleCollection.getShortName();

            Map<String, Map<Object, QuantitativeSample>> samples = sampleCollection.getSamples();

            File defaultOutputFile = sampleCollection.getDefaultOutputDirectory();

            for(Entry<String, Map<Object, QuantitativeSample>> entry : samples.entrySet())
            {
                String key = entry.getKey();
                Map<Object, QuantitativeSample> innerMap = entry.getValue();

                StatisticsTable table = tables.get(key);
                SimpleSampleStatisticsModel model;
                if(table == null)
                {
                    model = new SimpleSampleStatisticsModel(SimplePrefixedUnit.getNullInstance(), defaultOutputFile);
                    table = new StatisticsTable(model);
                    tables.put(key, table);
                }
                else
                {
                    model = (SimpleSampleStatisticsModel)table.getModel();
                }

                for(Entry<Object, QuantitativeSample> innerEntry: innerMap.entrySet())
                {
                    Object roiName = innerEntry.getKey().toString();
                    QuantitativeSample sample = innerEntry.getValue();

                    String sampleKey = roiName + "  (" + sampleCollectionName + ")";
                    model.addOrUpdateSample(sampleKey, sampleKey, sample);
                }
            }
        }

        return tables;
    }
}

