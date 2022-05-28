
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.StandardSample;
import atomicJ.data.units.Quantity;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.Validation;

import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class ProcessedPackSampleModel <E extends Processed1DPack<E,?>> extends AbstractModel
{
    public static final String SETTINGS_SPECIFIED = "settingsSpecified";
    public static final String PACKS_SELECTED = "packsSelected";

    private List<E> packs = new ArrayList<>();
    private ProcessedPackFunction<? super E> function;
    private final Map<String, ProcessedPackFunction<? super E>> availableFunctionsMap = new LinkedHashMap<>();
    private double[] data = new double[] {};
    private String sampleName;

    private boolean allInputProvided = false;
    private boolean settingsSpecified = false;
    private boolean nonEmpty = false;

    public ProcessedPackSampleModel(Collection<ProcessedPackFunction<? super E>> availableFunctions, String sampleName)
    {
        this.sampleName = sampleName;
        this.function = availableFunctions.iterator().hasNext() ? availableFunctions.iterator().next() : null;

        for(ProcessedPackFunction<? super E> function: availableFunctions)
        {
            Quantity quantity = function.getEvaluatedQuantity();
            String quantityName = quantity.getName();
            this.availableFunctionsMap.put(quantityName, function);
        }

        checkIfNonEmpty();
        checkIfSettingsSpecified();
        checkIfAllInputProvided();
    }

    public ProcessedPackSampleModel(Collection<ProcessedPackFunction<? super E>> availableFunctions, String sampleName, List<E> packs)
    {
        this.sampleName = sampleName;
        this.function = availableFunctions.iterator().hasNext() ? availableFunctions.iterator().next() : null;

        for(ProcessedPackFunction<? super E> function: availableFunctions)
        {
            Quantity quantity = function.getEvaluatedQuantity();
            String quantityName = quantity.getName();
            this.availableFunctionsMap.put(quantityName, function);
        }

        this.packs.addAll(packs);

        checkIfNonEmpty();
        checkIfSettingsSpecified();
        checkIfAllInputProvided();
    }

    public int getCount()
    {
        return packs.size();
    }

    public boolean isNonEmpty()
    {
        return nonEmpty;
    }

    public String getVariable()
    {
        String variable = function.getEvaluatedQuantity().getName();
        return variable;
    }

    public List<String> getAvailableVariables()
    {
        List<String> variables = new ArrayList<>(availableFunctionsMap.keySet());
        return variables;
    }

    public boolean setVariable(String variable)
    {
        ProcessedPackFunction<? super E> function = availableFunctionsMap.get(variable);
        if(variable != null)
        {
            setProcessedPackFunction(function);
            return true;
        }
        return false;
    }

    public String getSampleName()
    {
        return sampleName;
    }

    public void setSampleName(String newSampleName)
    {
        String oldSampleName = sampleName;
        this.sampleName = newSampleName;

        firePropertyChange(SAMPLE_NAME, oldSampleName, newSampleName);

        checkIfSettingsSpecified();
        checkIfAllInputProvided();
    }

    public List<E> getProcessedPacks()
    {
        return packs;
    }

    public void setProcessedPacks(List<E> packsNew)
    {
        Validation.requireNonNullParameterName(packsNew, "packsNew");

        List<E> packsOld = this.packs;
        this.packs = packsNew;

        updateData();

        checkIfAllInputProvided();
        checkIfNonEmpty();

        firePropertyChange(InferenceModelProperties.SAMPLE_PROCESSED_PACKS, packsOld, packsNew);
    }

    public void addProcessedPacks(List<E> newPacks)
    {
        List<E> packsCopy = new ArrayList<>(packs);
        packsCopy.addAll(newPacks);

        setProcessedPacks(packsCopy);
    }

    public void removeProcessedPacks(List<E> removedSources)
    {
        List<E> packsCopy = new ArrayList<>(packs);

        packsCopy.removeAll(removedSources);        
        setProcessedPacks(packsCopy);
    }

    public ProcessedPackFunction<? super E> getFunction()
    {
        return function;
    }

    public void setProcessedPackFunction(ProcessedPackFunction<? super E> functionNew)
    {
        Validation.requireNonNullParameterName(functionNew, "functionNew");

        ProcessedPackFunction<? super E> oldFunction = this.function;
        this.function = functionNew;

        updateData();

        firePropertyChange(SAMPLE_VARIABLE, oldFunction.getEvaluatedQuantity().getName(), functionNew.getEvaluatedQuantity().getName());
        firePropertyChange(InferenceModelProperties.SAMPLE_PROCESSED_PACK_FUNCTION, oldFunction, functionNew);
    }

    public boolean isAllInputProvided()
    {
        return allInputProvided;
    }

    public boolean isSettingsSpecified()
    {
        return settingsSpecified;
    }

    private void checkIfAllInputProvided()
    {
        boolean inputProvidedNew = data.length>0 && isSampleNameValid();
        boolean inputProvidedOld = allInputProvided;

        if(inputProvidedNew != inputProvidedOld)
        {
            this.allInputProvided = inputProvidedNew;
            firePropertyChange(SAMPLE_INPUT_PROVIDED, inputProvidedOld, inputProvidedNew);
        }
    }

    private void checkIfSettingsSpecified()
    {
        boolean settingsSpecifiedNew = isSampleNameValid();
        boolean settingsSpecifiedOld = settingsSpecified;

        if(settingsSpecifiedNew != settingsSpecifiedOld)
        {
            this.settingsSpecified = settingsSpecifiedNew;
            firePropertyChange(SETTINGS_SPECIFIED, settingsSpecifiedOld, settingsSpecifiedNew);
        }
    }

    private void checkIfNonEmpty()
    {
        boolean batchNonEmptyNew = !packs.isEmpty();
        boolean batchNonEmptyOld = this.nonEmpty;

        if(batchNonEmptyNew != this.nonEmpty)
        {
            this.nonEmpty = batchNonEmptyNew;
            firePropertyChange(PACKS_SELECTED, batchNonEmptyOld, batchNonEmptyNew);
        }
    }

    private boolean isSampleNameValid()
    {
        boolean validName = (sampleName != null && !sampleName.isEmpty());

        return validName;
    }

    public double[] getData() throws UserCommunicableException
    {
        return data;
    }

    public void updateData()
    {
        double[] dataOld = data;
        double[] dataNew = ProcessedPackFunction.<E>getValuesForPacks(packs, function);
        this.data = dataNew;

        firePropertyChange(SAMPLE_DATA, dataOld, dataNew);
    }

    public DescriptiveStatistics getDescriptiveStatics() throws UserCommunicableException
    {
        DescriptiveStatistics stats = new DescriptiveStatistics(data, sampleName);
        return stats;
    }

    public DescriptiveStatistics getDescriptiveStatics(ProcessedPackFunction<? super E> function) throws UserCommunicableException
    {
        DescriptiveStatistics stats = new DescriptiveStatistics(ProcessedPackFunction.<E>getValuesForPacks(packs, function), sampleName);
        return stats;
    }

    public QuantitativeSample getSample(ProcessedPackFunction<? super E> f)
    {
        Quantity sampleQuantity = f.getEvaluatedQuantity();
        String sampleKey = sampleQuantity + " " + sampleName;
        QuantitativeSample sample = 
                new StandardSample(ProcessedPackFunction.<E>getValuesForPacks(packs, f), sampleKey, sampleQuantity, sampleName);

        return sample;
    }

    public File getDefaultOutputLocation()
    {
        return BatchUtilities.findLastCommonDirectory(packs);
    }
}
