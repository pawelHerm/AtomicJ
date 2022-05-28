
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

import static atomicJ.gui.histogram.HistogramModelProperties.*;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.gui.AbstractWizardPageModel;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.WizardModelProperties;
import atomicJ.statistics.BinningMethod;
import atomicJ.statistics.DistributionType;
import atomicJ.statistics.FitType;
import atomicJ.statistics.HistogramType;



public class HistogramMultiSampleBinningModel extends AbstractWizardPageModel implements HistogramBinningModel, PropertyChangeListener
{
    private final HistogramDestination destination;
    private final Map<HistogramResourceIdentifier, List<HistogramSampleModel>> sampleModelsMap;
    private final List<HistogramSampleModel> allValueModels = new ArrayList<>();

    private int currentValueModelIndex;

    private final boolean isFirst;
    private final boolean isLast;

    private boolean backEnabled;
    private boolean nextEnabled;
    private boolean skipEnabled;
    private boolean finishEnabled;
    private boolean finished;
    private String task;
    private HistogramSampleModel sampleModel;

    public HistogramMultiSampleBinningModel(HistogramDestination destination, 
            Map<HistogramResourceIdentifier, List<HistogramSampleModel>> sampleModelsMap,
            boolean isFirst, boolean isLast)
    {
        this.destination = destination;
        this.currentValueModelIndex = 0;
        this.sampleModelsMap = sampleModelsMap;

        for(List<HistogramSampleModel> models: sampleModelsMap.values())
        {
            allValueModels.addAll(models);
        }

        this.sampleModel = allValueModels.get(currentValueModelIndex);	
        sampleModel.addPropertyChangeListener(this);

        this.isFirst = isFirst;
        this.isLast = isLast;

        checkIfBackEnabled();
        checkIfNextEnabled();
        checkIfSkipEnabled();
        checkIfFinishEnabled();
        checkIfTaskNameChanged();
    }

    @Override
    public String getTaskDescription()
    {
        return task;
    }

    @Override
    public String getUnit()
    {
        return sampleModel.getDomainQuantity().getFullUnitName();
    }

    @Override
    public boolean containsNonpositiveValues()
    {
        return sampleModel.containsNonpositive();
    }

    @Override
    public boolean isBackEnabled()
    {
        return backEnabled;
    }

    @Override
    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    @Override
    public boolean isSkipEnabled()
    {
        return skipEnabled;
    }

    @Override
    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    @Override
    public void back() 
    {
        if(backEnabled)
        {
            previousBatch();
        }
    }

    @Override
    public void next() 
    {		
        if(nextEnabled)
        {
            nextBatch();
        }
    }

    @Override
    public void skip()
    {
        if(skipEnabled)
        {
            allValueModels.remove(sampleModel);                     
            removeSampleModelFormMap(sampleModel);

            if(currentValueModelIndex>= getSampleSize())
            {
                drawAndShowHistograms();
            }
            else
            {
                setCurrentBatch(currentValueModelIndex);
            }
        }
    }

    private int getSampleSize()
    {
        int sampleSize = 0;

        for(Entry<HistogramResourceIdentifier, List<HistogramSampleModel>> entry: sampleModelsMap.entrySet())
        {
            List<HistogramSampleModel> sampleModels = entry.getValue();
            sampleSize = sampleSize + sampleModels.size();


        }

        return sampleSize;
    }

    private void removeSampleModelFormMap(HistogramSampleModel sampleModel)
    {
        List<HistogramResourceIdentifier> emptyGroups = new ArrayList<>();

        for(Entry<HistogramResourceIdentifier, List<HistogramSampleModel>> entry: sampleModelsMap.entrySet())
        {
            List<HistogramSampleModel> sampleModels = entry.getValue();
            sampleModels.remove(sampleModel);

            if(sampleModels.isEmpty())
            {
                emptyGroups.add(entry.getKey() );
            }          
        }

        for(HistogramResourceIdentifier id : emptyGroups)
        {
            sampleModelsMap.remove(id);
        }
    }

    @Override
    public void finish() 
    {
        if(finishEnabled)
        {
            drawAndShowHistograms();
        }
    }

    @Override
    public HistogramDestination getHistogramDestination()
    {
        return destination;
    }

    @Override
    public Window getPublicationSite()
    {
        return destination.getHistogramPublicationSite();
    }

    public void nextBatch()
    {
        int newIndex = currentValueModelIndex + 1;	
        setCurrentBatch(newIndex);
    }

    public void previousBatch()
    {
        int newIndex = currentValueModelIndex - 1;	
        setCurrentBatch(newIndex);
    }

    private void setCurrentBatch(int newIndex)
    {
        int size = allValueModels.size();
        boolean withinRange = (newIndex>=0)&&(newIndex<size);
        if(withinRange)
        {
            HistogramSampleModel oldModel = sampleModel;
            HistogramSampleModel newModel = allValueModels.get(newIndex);

            if(oldModel != newModel)
            {
                oldModel.removePropertyChangeListener(this);
                newModel.addPropertyChangeListener(this);

                sampleModel = newModel;
                currentValueModelIndex = newIndex;

                checkIfBackEnabled();
                checkIfNextEnabled();
                checkIfSkipEnabled();
                checkIfFinishEnabled();
                checkIfTaskNameChanged();

                firePropertyChange(BATCH_CHANGED, false, true);
            }
        }		
    }

    public Set<String> getValuesNames()
    {
        return null;
    }

    public boolean isCurrentBatchFirst()
    {
        boolean isFirst = (currentValueModelIndex == 0);
        return isFirst;
    }

    public boolean isCurrentBatchLast()
    {
        boolean isLast = (currentValueModelIndex == allValueModels.size() - 1);
        return isLast;
    }

    @Override
    public String getName()
    {
        return sampleModel.getName();
    }

    @Override
    public void setName(String newName)
    {
        sampleModel.specifyName(newName);
    }

    @Override
    public boolean isEntitled()
    {
        return sampleModel.isEntitled();
    }

    @Override
    public void setEntitled(boolean entitledNew)
    {
        sampleModel.specifyEntitled(entitledNew);
    }

    @Override
    public int getAllDataCount()
    {
        return sampleModel.getAllDataCount();
    }

    @Override
    public int getTrimmedDataCount()
    {
        return sampleModel.getDataCount();
    }

    @Override
    public int getDiscardedDataCount()
    {
        return sampleModel.getDiscardedDataCount();
    }

    @Override
    public double getBinCount()
    {
        return sampleModel.getBinCount();
    }

    @Override
    public void setBinCount(Number count)
    {
        sampleModel.setBinCount(count);
    }

    @Override
    public void specifyBinCount(Number count)
    {
        sampleModel.specifyBinCount(count);
    }

    @Override
    public void setCountConsistentWithRange()
    {
        sampleModel.setCountConsistentWithRange();
    }

    @Override
    public void setCountConsistentWithWidth()
    {
        sampleModel.setCountConsistentWithWidth();
    }

    @Override
    public void setWidthConsistentWithRangeAndCount()
    {
        sampleModel.setWidthConsistentWithRangeAndCount();
    }

    @Override
    public double getBinWidth()
    {
        return sampleModel.getBinWidth();
    }

    @Override
    public void setBinWidth(Double binWidthNew)
    {
        sampleModel.setBinWidth(binWidthNew);
    }

    @Override
    public void specifyBinWidth(Double width)
    {
        sampleModel.specifyBinWidth(width);
    }

    @Override
    public boolean isFullRange()
    {
        return sampleModel.isFullRange();
    }

    @Override
    public void setFullRange(Boolean full)
    {
        sampleModel.specifyFullRange(full);
    }

    @Override
    public Double getFractionOfSmallestTrimmed()
    {
        return sampleModel.getFractionOfSmallestTrimmed();
    }

    @Override
    public void setFractionOfSmallestTrimmed(Double trimSmallestNew)
    {
        sampleModel.specifyFractionfSmallestTrimmed(trimSmallestNew);	
    }

    @Override
    public Double getFractionOfLargestTrimmed()
    {
        return sampleModel.getFractionOfLargestTrimmed();
    }

    @Override
    public void setFractionOfLargestTrimmed(Double trimLargestNew)
    {
        sampleModel.specifyFractionOfLargestTrimmed(trimLargestNew);	
    }

    @Override
    public boolean isFitted()
    {
        return sampleModel.isFitted();
    }

    @Override
    public void setFitted(Boolean fitted)
    {
        sampleModel.specifyFitted(fitted);
    }

    @Override
    public double getRangeMinimum()
    {
        return sampleModel.getRangeMininmum();
    }

    @Override
    public void setRangeMinimum(Double min)
    {	
        sampleModel.setRangeMinimum(min);
    }

    @Override
    public void specifyRangeMinimum(Double min)
    {
        sampleModel.specifyRangeMinimum(min);
    }

    @Override
    public double getRangeMaximum()
    {
        return sampleModel.getRangeMaximum();
    }

    @Override
    public void setRangeMaximum(Double max)
    {
        sampleModel.setRangeMaximum(max);
    }

    @Override
    public void specifyRangeMaximum(Double max)
    {
        sampleModel.specifyRangeMaximum(max);
    }

    @Override
    public boolean isRangeExtensive()
    {
        return sampleModel.isRangeExtensive();
    }

    @Override
    public HistogramType getHistogramType()
    {
        return sampleModel.getHistogramType();
    }

    @Override
    public void setHistogramType(HistogramType type)
    {
        sampleModel.specifyHistogramType(type);
    }

    @Override
    public DistributionType getDistributionType()
    {
        return sampleModel.getDistributionType();
    }

    @Override
    public void setDistributionType(DistributionType type)
    {
        sampleModel.specifyDistributionType(type);
    }

    @Override
    public FitType getFitType()
    {
        return sampleModel.getFitType();
    }

    @Override
    public void setFitType(FitType type)
    {
        sampleModel.specifyFitType(type);
    }

    @Override
    public BinningMethod getBinningMethod()
    {
        return sampleModel.getBinningMethod();
    }

    @Override
    public void setBinningMethod(BinningMethod method)
    {
        sampleModel.specifyBinningMethod(method);
    }

    public boolean isInputProvided()
    {
        return sampleModel.isInputProvided();
    }

    private void setFinished(boolean finishedNew)
    {	
        boolean finishedOld = this.finished;
        this.finished = finishedNew;

        firePropertyChange(BINNING_FINISHED, finishedOld, finishedNew);
    }

    public void drawAndShowHistograms()
    {	
        Map<HistogramResource, Map<String,ChannelChart<HistogramPlot>>> modelsChartsMap = new LinkedHashMap<>();

        for(Entry<HistogramResourceIdentifier, List<HistogramSampleModel>> entry: sampleModelsMap.entrySet())
        {
            HistogramResourceIdentifier identifier = entry.getKey();

            String shortName = identifier.getShortName();
            String longName = identifier.getLongName();


            File defaultOutputDirectory = identifier.getDefaultOutputLocation();

            Map<String, HistogramSampleModel> sampleModels = new LinkedHashMap<>();
            Map<String,ChannelChart<HistogramPlot>> charts = new LinkedHashMap<>();

            for(HistogramSampleModel sampleModel: entry.getValue())
            {
                if(sampleModel.isInputProvided())
                {
                    ChannelChart<HistogramPlot> chart = sampleModel.getHistogram();

                    sampleModels.put(sampleModel.getType(), sampleModel);
                    charts.put(sampleModel.getType(), chart);
                }
            }

            HistogramResource histogramResource = new HistogramResource(sampleModels, shortName, longName, defaultOutputDirectory);
            modelsChartsMap.put(histogramResource, charts);
        }

        destination.publishHistograms(modelsChartsMap);
        setFinished(true);
    }



    public boolean isAllInputProvided()
    {
        return finishEnabled;
    }

    public int getCurrentBatchIndex()
    {
        return currentValueModelIndex;
    }

    public int getBatchNumber()
    {
        return sampleModelsMap.size();
    }

    @Override
    public void setUndoPoint()
    {

    }

    @Override
    public void undo()
    {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == sampleModel)
        {
            firePropertyChange(evt);
        }

        String name = evt.getPropertyName();

        if(name.equals(INPUT_PROVIDED))
        {
            checkIfNextEnabled();
            checkIfFinishEnabled();
        }
    }

    private void checkIfBackEnabled()
    {
        boolean backEnabledNew = !isCurrentBatchFirst();

        boolean backEnabledOld = backEnabled;
        this.backEnabled = backEnabledNew;

        firePropertyChange(WizardModelProperties.BACK_ENABLED, backEnabledOld, backEnabledNew);
    }

    private void checkIfNextEnabled()
    {
        boolean inputProvided = sampleModel.isInputProvided();
        boolean isLast = isCurrentBatchLast();

        boolean nextEnabledNew = (inputProvided && !isLast);
        boolean nextEnabledOld = nextEnabled;

        this.nextEnabled = nextEnabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, nextEnabledOld, nextEnabledNew);
    }

    private void checkIfSkipEnabled()
    {		
        //boolean skipEnabledNew = !(isCurrentBatchLast() && valuesModels.size()<2);
        boolean skipEnabledNew = true;
        boolean skipEnabledOld = skipEnabled;
        this.skipEnabled = skipEnabledNew;

        firePropertyChange(WizardModelProperties.SKIP_ENABLED, skipEnabledOld, skipEnabledNew);
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledNew = isLast;

        if(isLast)
        {
            for(Entry<HistogramResourceIdentifier, List<HistogramSampleModel>> entry: sampleModelsMap.entrySet())
            {
                for(HistogramSampleModel model: entry.getValue())
                {
                    boolean provided = model.isInputProvided();
                    finishEnabledNew = finishEnabledNew && provided;
                }
            }
        }



        if(finishEnabled != finishEnabledNew)
        {
            boolean finishEnabledOld = finishEnabled;
            this.finishEnabled = finishEnabledNew;

            firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, finishEnabledNew);
        }
    }

    private void checkIfTaskNameChanged()
    {
        String taskNew = sampleModel.getTask();
        String taskOld = task;
        this.task = taskNew;

        firePropertyChange(WizardModelProperties.TASK, taskOld, taskNew);
    }

    @Override
    public String getTaskName() 
    {
        return task;
    }

    @Override
    public boolean isFirst() 
    {
        return isFirst;
    }

    @Override
    public boolean isLast() 
    {
        return isLast;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return false;
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }
}
