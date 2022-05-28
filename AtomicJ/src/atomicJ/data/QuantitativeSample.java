
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

public interface QuantitativeSample 
{		
    public int size();
    public boolean isEmpty();
    public boolean containsNumericValues();

    public String getKey();

    public String getSampleName();
    public String getQuantityName();

    public String getNameTag();
    public String getNameRoot();
    public void setNameRoot(String nameRootNew);

    public boolean isIncluded();
    public void setIncluded(boolean included);

    public double[] getMagnitudes();
    public Quantity getQuantity();

    public DescriptiveStatistics getDescriptiveStatistics();

    public QuantitativeSample applyFunction(UnivariateFunction f);
    public QuantitativeSample applyFunction(UnivariateFunction f, String functionName);

    public QuantitativeSample trim(double trimSmallest, double trimLargest);
    public QuantitativeSample fixZero();
}
