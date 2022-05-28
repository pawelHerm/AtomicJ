
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

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.data.units.Quantity;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.Validation;


public class StandardSample implements QuantitativeSample
{		
    private final double[] magnitudes;	

    //key must be unique, i.e. it should identify unambiguously the sample
    //In any collection of samples, each sample should have a different key
    private final String key;	
    private String nameRoot;

    private final String nameTag;
    private final Quantity quantity;	

    private DescriptiveStatistics descriptiveStatistics;
    private boolean included = false;

    public StandardSample(double[] magnitudes, Quantity quantity)
    {
        this(magnitudes, quantity.getName(), quantity);
    }

    public StandardSample(double[] magnitudes, String key, Quantity quantity)
    {
        this(magnitudes, key, quantity, key);
    }

    public StandardSample(double[] magnitudes, String key,  Quantity quantity, String nameRoot)
    {
        this(magnitudes, key, quantity, nameRoot, "");        
    }

    public StandardSample(QuantitativeSample other)
    {
        this(other.getMagnitudes(), other.getKey(), other.getQuantity(), other.getNameRoot(), other.getNameTag());
    }

    public StandardSample(double[] magnitudes, String key, Quantity quantity, String nameRoot, String nameTag)
    {
        this.magnitudes = Validation.requireNonNullParameterName(magnitudes, "magnitudes");
        this.key = key;
        this.nameTag = nameTag;
        this.quantity = quantity;
        this.nameRoot = nameRoot;
    }

    @Override
    public DescriptiveStatistics getDescriptiveStatistics()
    {
        if(descriptiveStatistics == null)
        {
            descriptiveStatistics = new DescriptiveStatistics(magnitudes, key);
        }

        return descriptiveStatistics;
    }

    @Override
    public int size()
    {
        return magnitudes.length;
    }

    @Override
    public double[] getMagnitudes() 
    {        
        return magnitudes;
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
        boolean empty = (magnitudes.length == 0);
        return empty;
    }	

    @Override
    public boolean containsNumericValues()
    {
        return ArrayUtilities.containsNumericFiniteValues(magnitudes);
    }

    @Override
    public QuantitativeSample trim(double trimLeft, double trimRight)
    {
        double[] trimmedData = DescriptiveStatistics.trimQuickSelect(magnitudes, trimLeft, trimRight);
        StandardSample sample = new StandardSample(trimmedData, key, quantity);
        return sample;
    }

    @Override
    public QuantitativeSample applyFunction(UnivariateFunction f)
    {
        double[] positiveValuedData = DescriptiveStatistics.applyFunction(magnitudes, f);
        StandardSample sample = new StandardSample(positiveValuedData, key, quantity);
        return sample;
    }

    @Override
    public QuantitativeSample applyFunction(UnivariateFunction f, String functionName)
    {
        double[] positiveValuedData = DescriptiveStatistics.applyFunction(magnitudes, f);

        Quantity quantityNew = quantity.applyFunction(functionName);
        StandardSample sample = new StandardSample(positiveValuedData, key, quantityNew);
        return sample;
    }

    @Override
    public QuantitativeSample fixZero()
    {
        double[] positiveValuedData = DescriptiveStatistics.fixZero(magnitudes);
        StandardSample sample = new StandardSample(positiveValuedData, key, quantity);
        return sample;
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
