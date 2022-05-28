
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

package atomicJ.analysis;

import java.util.List;

import atomicJ.data.units.Quantity;
import atomicJ.utilities.Validation;

public interface ProcessedPackFunction <E extends Processed1DPack<E,?>>
{
    public double evaluate(E pack);
    public Quantity getEvaluatedQuantity();

    public static <E extends Processed1DPack<E,?>> double[] getValuesForPacks(List<E> packs, ProcessedPackFunction<? super E> function)
    {
        Validation.requireNonNullParameterName(packs, "packs");
        Validation.requireNonNullParameterName(function, "function");

        int k = packs.size();
        double[] values = new double[k];
        for(int j = 0; j<k; j++)
        {
            E pack = packs.get(j);
            values[j] = function.evaluate(pack);
        }
        return values;
    }
}
