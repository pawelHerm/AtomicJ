
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

package atomicJ.gui.statistics;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.*;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.results.ResultDataEvent;
import atomicJ.gui.results.ResultDataListener;
import atomicJ.gui.results.ResultDataModel;
import atomicJ.sources.Channel1DSource;
import atomicJ.statistics.DescriptiveStatistics;


public class ResultStatisticsModel<S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> extends UpdateableStatisticsModel implements ResultDataListener<S,E>
{
    private static final long serialVersionUID = 1L;
    private static final String ALL = "All batches";

    private final ResultDataModel<S,E> resultModel;
    private final ProcessedPackFunction<? super E> function;

    public ResultStatisticsModel(ResultDataModel<S,E> resultModel, ProcessedPackFunction<? super E> function, PrefixedUnit unit)
    {   
        super(resultModel.getDefaultOutputDirectory(), unit);

        this.resultModel = resultModel;
        this.function = function;   
        setModelConsistentWithResultModel();
    }

    private void setModelConsistentWithResultModel()
    {
        List<Batch<E>> batches = resultModel.getBatches();

        Map<String, DescriptiveStatistics> statisticsMap = new LinkedHashMap<>();

        DescriptiveStatistics stats = getStatisticsForAllBatches();
        statisticsMap.put(ALL, stats);

        if(!batches.isEmpty())
        {       
            statisticsMap.putAll(getSampleStatistics(batches));
        }       

        addSamples(statisticsMap);
        resultModel.addResultModelListener(this);
    }

    @Override
    public void batchesAdded(ResultDataEvent<S,E> event) 
    {
        List<Batch<E>> batches = event.getBatches();
        Map<String, DescriptiveStatistics> sampleStatistics = getSampleStatistics(batches);
        addSamples(sampleStatistics);   
        updateStatisticsForAllBatches();
    }

    @Override
    public void batchesRemoved(ResultDataEvent<S,E> event) 
    {
        List<String> removedBatchNames = new ArrayList<>();
        List<Batch<E>> batches = event.getBatches();

        for(Batch<E> batch: batches)
        {
            String name = batch.getName();
            removedBatchNames.add(name);
        }

        removeSamples(removedBatchNames);
        updateStatisticsForAllBatches();
    }


    @Override
    public void batchesCleared(ResultDataEvent<S,E> event) 
    {
        List<String> removedBatchNames = new ArrayList<>();
        List<Batch<E>> batches = event.getBatches();

        for(Batch<E> batch: batches)
        {
            String name = batch.getName();
            removedBatchNames.add(name);
        }

        removeSamples(removedBatchNames);
        updateStatisticsForAllBatches();        
    }

    @Override
    public void packsAdded(ResultDataEvent<S,E> event) 
    {
        packsChanged(event);
    }

    @Override
    public void packsRemoved(ResultDataEvent<S,E> event) 
    {
        packsChanged(event);
    }

    public void packsChanged(ResultDataEvent<S,E> event)
    {
        Batch<E> batch = event.getBatches().get(0);
        String sampleName = batch.getName();
        double[] data = batch.getValues(function);

        DescriptiveStatistics stats = new DescriptiveStatistics(data, sampleName);
        replaceSample(sampleName, stats);

        updateStatisticsForAllBatches();
    }

    private DescriptiveStatistics getStatisticsForAllBatches()
    {
        double[] data = Batch.<E>getValues(resultModel.getBatches(), function);       
        DescriptiveStatistics stats = new DescriptiveStatistics(data, ALL);
        return stats;
    }

    private void updateStatisticsForAllBatches()
    {
        double[] data = Batch.<E>getValues(resultModel.getBatches(), function);       
        DescriptiveStatistics stats = new DescriptiveStatistics(data, ALL);

        replaceSample(ALL, stats);
    }

    private Map<String, DescriptiveStatistics> getSampleStatistics(List<Batch<E>> batches)
    {       
        Map<String, DescriptiveStatistics> statisticsMap = new LinkedHashMap<>();

        for(int i = 0;i<batches.size();i++)
        {
            Batch<E> batch = batches.get(i);
            String sampleName = batch.getName();
            double[] data = batch.getValues(function);
            DescriptiveStatistics stats = new DescriptiveStatistics(data, sampleName);
            statisticsMap.put(sampleName, stats);
        }   

        return statisticsMap;
    }
}