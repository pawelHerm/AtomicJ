
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.statistics.InferenceModelProperties;
import atomicJ.gui.statistics.ProcessedPackSampleModel;
import atomicJ.utilities.IOUtilities;

public class BoxSampleModel<E extends Processed1DPack<E,?>> extends AbstractModel implements WizardPageModel, PropertyChangeListener
{
    private static final String TASK = "Sample selection";
    private static final String TASK_DESCRIPTION = "Select results for each sample in the box plot";

    public static final String CURRENT_SAMPLE_MODEL = "CurrentSampleModel";

    private final List<ProcessedPackSampleModel<E>> batchModels = new ArrayList<>();

    private ProcessedPackSampleModel<E> currentBatch;

    private boolean sealed;    
    private int batchIndex = 0;

    private boolean finishEnabled;
    private boolean nextEnabled;
    private final boolean backEnabled = true;
    private final boolean skipEnabled = false;

    private final boolean isFirst;
    private final boolean isLast;

    private final List<Batch<E>> availableData;
    private final List<ProcessedPackFunction<? super E>> availableFunctions;

    public BoxSampleModel(Collection<ProcessedPackFunction<? super E>> availableFunctions, List<Batch<E>> availableData, boolean isFirst, boolean isLast)
    {
        this.isFirst = isFirst;
        this.isLast = isLast;
        this.availableData = new ArrayList<>(availableData);
        this.availableFunctions = new ArrayList<>(availableFunctions);

        ProcessedPackSampleModel<E> firstBatch = new ProcessedPackSampleModel<>(this.availableFunctions, Integer.toString(1));
        batchModels.add(firstBatch);
        setCurrentBatch(firstBatch);
    }

    public List<Batch<E>> getAvilableData()
    {
        return availableData;
    }

    public List<ProcessedPackSampleModel<E>> getSampleModels()
    {
        return Collections.unmodifiableList(batchModels);
    }

    public List<ProcessedPackSampleModel<E>> getWellFormedSampleModels()
    {
        List<ProcessedPackSampleModel<E>> wellFormedModels = new ArrayList<>();
        for(ProcessedPackSampleModel<E> m : batchModels)
        {
            if(m.isAllInputProvided())
            {
                wellFormedModels.add(m);
            }
        }

        return wellFormedModels;
    }

    public List<String> getWellFormedSampleNames()
    {
        List<String> wellFormedModelNames = new ArrayList<>();
        for(ProcessedPackSampleModel<E> m : batchModels)
        {
            if(m.isAllInputProvided())
            {
                wellFormedModelNames.add(m.getSampleName());
            }
        }

        return wellFormedModelNames;
    }


    public void nextBatch()
    {
        int newIndex = batchIndex + 1;   
        if(newIndex == batchModels.size())
        {
            addNewBatch();
        }

        setCurrentBatch(newIndex);
    }

    public void previousBatch()
    {
        int batchIndexNew = Math.max(0, batchIndex - 1);   
        setCurrentBatch(batchIndexNew);
    }

    private void setCurrentBatch(int batchIndexNew)
    {        
        int size = batchModels.size();
        boolean withinRange = (batchIndexNew>=0)&&(batchIndexNew<size);

        if(!withinRange)
        {
            return;
        }

        if(this.batchIndex != batchIndexNew)
        {
            this.batchIndex = batchIndexNew;
            ProcessedPackSampleModel<E> modelNew = batchModels.get(batchIndexNew);
            setCurrentBatch(modelNew);            
        }   
    }

    private void setCurrentBatch(ProcessedPackSampleModel<E> modelNew)
    {
        ProcessedPackSampleModel<E> modelOld = this.currentBatch;

        this.currentBatch = modelNew;
        this.currentBatch.addPropertyChangeListener(this);

        checkIfNextEnabled();
        checkIfFinishEnabled();

        if(modelOld != null)
        {
            modelOld.removePropertyChangeListener(this); 
            firePropertyChange(CURRENT_SAMPLE_MODEL, modelOld.getSampleName(), modelNew.getSampleName());         
        }        
    }

    public void addNewBatch()
    {
        if(sealed)
        {
            throw new IllegalStateException("Processing model is seled - new batches cannot be added");
        }

        int batchNumber = batchModels.size();
        ProcessedPackSampleModel<E> newBatch = new ProcessedPackSampleModel<>(this.availableFunctions, Integer.toString(batchNumber + 1));
        batchModels.add(newBatch);
    }

    public void addNewBatch(List<E> packs)
    {
        if(sealed)
        {
            throw new IllegalStateException("Processing model is seled - new batches cannot be added");
        }

        int batchNumber = batchModels.size();
        ProcessedPackSampleModel<E> newBatch = new ProcessedPackSampleModel<>(this.availableFunctions, Integer.toString(batchNumber + 1), packs);
        batchModels.add(newBatch);
    }

    public boolean isCurrentBatchLast()
    {
        boolean isLast = sealed && (batchModels.size() - 1 == batchIndex);
        return isLast;
    }

    public boolean isNonEmpty()
    {
        return currentBatch.isNonEmpty();
    }

    @Override
    public boolean isNecessaryInputProvided()
    {
        return currentBatch.isAllInputProvided();
    }

    public boolean canProcessingOfCurrentBatchBeFinished()
    {
        boolean canBeFinished = currentBatch.isAllInputProvided();

        return canBeFinished;
    }

    private boolean isAtLeastOneBatchNonEmpty()
    {
        boolean isAtLeastOneBatchNonEmpty = false;
        for(ProcessedPackSampleModel<E> batch: batchModels)
        {
            boolean nonEmpty = batch.isNonEmpty();
            isAtLeastOneBatchNonEmpty = isAtLeastOneBatchNonEmpty||nonEmpty;
        }
        boolean canBeFinished = isAtLeastOneBatchNonEmpty;
        return canBeFinished;
    }

    //this method return false only when the current batch is last, but not all input was provided
    //for this batch
    private boolean isInputProvidedToCurrentLastBatch()
    {
        boolean isLast = (this.batchIndex == (batchModels.size() - 1));

        boolean result = isLast ? currentBatch.isAllInputProvided() : true;

        return result;
    }

    private void checkIfNextEnabled()
    {
        boolean enabledNew = isAtLeastOneBatchNonEmpty() && isInputProvidedToCurrentLastBatch();

        boolean enabledOld = this.nextEnabled;
        this.nextEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);     
    }

    private void checkIfFinishEnabled()
    {
        boolean enabledNew = isAtLeastOneBatchNonEmpty();

        boolean enabledOld = finishEnabled;
        this.finishEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);     
    }

    public int getBatchNumber()
    {
        return batchModels.size();
    }

    public String getCurrentBatchName()
    {
        return currentBatch.getSampleName();
    }

    public void setCurrentBatchName(String nameNew)
    {
        currentBatch.setSampleName(nameNew);
    }

    public void setProcessedPacks(List<E> packsNew)
    {
        if(sealed)
        {
            throw new IllegalStateException();
        }

        currentBatch.setProcessedPacks(packsNew);
    }

    public void addProcessedPacks(List<E> packsToAdd)
    {
        currentBatch.addProcessedPacks(packsToAdd);
    }

    public void removeProcessedPacks(List<E> packsToRemove)
    {
        currentBatch.removeProcessedPacks(packsToRemove);
    }

    public List<E> getProcessedPacks()
    {
        return currentBatch.getProcessedPacks();
    }

    public File getDefaultOutputLocation()
    {
        List<ProcessedPackSampleModel<E>> models = getWellFormedSampleModels();
        List<File> files = new ArrayList<>();

        for(ProcessedPackSampleModel<E> model : models)
        {
            File f = model.getDefaultOutputLocation();
            files.add(f);
        }

        File outputLocation = IOUtilities.findLastCommonDirectory(files);

        return outputLocation;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {        
        firePropertyChange(evt);

        String property = evt.getPropertyName();

        if(InferenceModelProperties.SAMPLE_INPUT_PROVIDED.equals(property))
        {
            checkIfNextEnabled();
            checkIfFinishEnabled();

            firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, evt.getNewValue(), evt.getOldValue());
        }
        else if(ProcessedPackSampleModel.PACKS_SELECTED.equals(property))
        {
            checkIfNextEnabled();
            checkIfFinishEnabled();
        }
        else if(ProcessedPackSampleModel.SETTINGS_SPECIFIED.equals(property))
        {
            checkIfNextEnabled();
            checkIfFinishEnabled();
        }
    }

    @Override
    public String getTaskName() 
    {
        return TASK;
    }

    @Override
    public String getTaskDescription() 
    {
        return TASK_DESCRIPTION;
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
        previousBatch();  
    }

    @Override
    public void next() 
    {          
        nextBatch();
    }

    @Override
    public void skip() {        
    }

    @Override
    public void finish() {        
    }

    @Override
    public void cancel() {

    }

}
