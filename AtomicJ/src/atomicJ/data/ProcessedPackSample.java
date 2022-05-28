
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

import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.data.units.Quantity;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;


/*
 * This class is a kind of lazy initialization of a s sample - it gets a list of processed packs as costructor argument,
 * an keeps it until it is asked for data (i.e. getData is called). Then the sample data are calculated from the processed packs and the list of packs itself is set to null,
 * as it is not longer of any use
 */
public class ProcessedPackSample <E extends Processed1DPack<E,?>> implements QuantitativeSample
{
    private final int size;

    private List<E> packs;
    private final ProcessedPackFunction<? super E> function;

    //key must be unique, i.e. it should identify unambiguously the sample
    //In any collection of samples, each sample should have a different key
    private final String key;
    private String nameRoot;

    private final String nameTag;
    private final Quantity quantity;

    private double[] magnitudes;

    private DescriptiveStatistics descriptiveStatistics;
    private boolean included = false;

    public ProcessedPackSample(List<E> packs, ProcessedPackFunction<? super E> function)
    {
        this(packs, function.getEvaluatedQuantity().getName(), function);
    }

    public ProcessedPackSample(List<E> packs, String key, ProcessedPackFunction<? super E> function)
    {
        this(packs, key, function, key);
    }

    public ProcessedPackSample(List<E> packs, String key, ProcessedPackFunction<? super E> function, String nameRoot)
    {
        this(packs, key, function, nameRoot, "");
    }

    public ProcessedPackSample(List<E> packs, String key, ProcessedPackFunction<? super E> function, String nameRoot, String nameTag)
    {
        this.packs = packs;
        this.size = packs.size();
        this.function = function;
        this.key = key;
        this.nameTag = nameTag;
        this.quantity = function.getEvaluatedQuantity();
        this.nameRoot = nameRoot;
    }

    @Override
    public QuantitativeSample trim(double trimLeft, double trimRight)
    {
        double[] trimmedData = DescriptiveStatistics.trimQuickSelect(getMagnitudes(), trimLeft, trimRight);
        StandardSample sample = new StandardSample(trimmedData, key, quantity);
        return sample;
    }

    @Override
    public QuantitativeSample applyFunction(UnivariateFunction f)
    {
        double[] fixedData = DescriptiveStatistics.applyFunction(getMagnitudes(), f);
        StandardSample sample = new StandardSample(fixedData, key, quantity);
        return sample;
    }

    @Override
    public QuantitativeSample applyFunction(UnivariateFunction f, String functionName)
    {
        double[] fixedData = DescriptiveStatistics.applyFunction(getMagnitudes(), f);

        Quantity newQuantity = quantity.applyFunction(functionName);
        StandardSample sample = new StandardSample(fixedData, key, newQuantity);
        return sample;
    }   

    @Override
    public QuantitativeSample fixZero()
    {
        double[] fixedData = DescriptiveStatistics.fixZero(getMagnitudes());
        StandardSample trimmedValues = new StandardSample(fixedData, key, quantity);
        return trimmedValues;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public double[] getMagnitudes() 
    {
        if(magnitudes == null)
        {
            magnitudes = ProcessedPackFunction.<E>getValuesForPacks(packs, function);
            packs = null;
        }
        return magnitudes;
    }

    @Override
    public DescriptiveStatistics getDescriptiveStatistics()
    {
        if(descriptiveStatistics == null)
        {
            descriptiveStatistics = new DescriptiveStatistics(getMagnitudes(), key);
        }

        return descriptiveStatistics;
    }


    @Override
    public String getQuantityName() 
    {
        return quantity.getName();
    }

    @Override
    public String getKey() 
    {
        return key;
    }

    @Override
    public String getNameTag()
    {
        return nameTag;
    }

    @Override
    public String getNameRoot()
    {
        return nameRoot;
    }

    @Override
    public void setNameRoot(String nameRootNew)
    {
        this.nameRoot = nameRootNew;
    }

    @Override
    public String getSampleName() 
    {
        String name =  nameRoot + nameTag;        
        return name;
    }

    @Override
    public boolean isEmpty() 
    {
        boolean empty = (magnitudes == null) || (magnitudes.length == 0);
        return empty;
    }

    @Override
    public boolean containsNumericValues()
    {
        boolean numeric = magnitudes != null && ArrayUtilities.containsNumericFiniteValues(magnitudes);
        return numeric;
    }

    @Override
    public boolean isIncluded() 
    {
        return included;
    }

    @Override
    public void setIncluded(boolean included) 
    {
        this.included = included;
    }

    @Override
    public Quantity getQuantity() 
    {
        return quantity;
    }
}
