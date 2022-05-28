/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanosurf;

import java.util.Map;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;

public class NIDDimension 
{
    private static final String DIM_PREFIX = "Dim";

    private static final String MIN_SUFFIX = "Min";
    private static final String NAME_SUFFIX = "Name";
    private static final String UNIT_SUFFIX = "Unit";
    private static final String RANGE_SUFFIX = "Range";

    private static final String[] INFO_SUFFICES = new String[] {"Min", "Name", "Unit", "Range"};

    private final int dimensionIndex;

    private final String name;
    private final PrefixedUnit unit;
    private final double range;
    private final double min;

    private NIDDimension(int dimensionIndex, String name, PrefixedUnit unit, double range, double min)
    {
        this.dimensionIndex = dimensionIndex;
        this.name = name;
        this.unit = unit;
        this.range = range;
        this.min = min;
    }

    public String getName()
    {
        return name;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public Quantity getQuantity()
    {
        return new UnitQuantity(name, unit);
    }

    public double getRange()
    {
        return range;
    }

    public double getMinimum()
    {
        return min;
    }

    public static NIDDimension build(int dimensionIndex, Map<String, String> keyValuePairs)
    {
        String dimensionIndexString = Integer.toString(dimensionIndex);

        String nameKey = DIM_PREFIX + dimensionIndexString + NAME_SUFFIX;
        String unitKey = DIM_PREFIX + dimensionIndexString + UNIT_SUFFIX;
        String rangeKey = DIM_PREFIX + dimensionIndexString + RANGE_SUFFIX;
        String minKey = DIM_PREFIX + dimensionIndexString + MIN_SUFFIX;

        String nameString = keyValuePairs.get(nameKey);
        String name = (nameString != null) ? nameString : "";
        String unitString = keyValuePairs.get(unitKey);
        PrefixedUnit unit = (unitString != null) ? UnitUtilities.getSIUnit(unitString) : SimplePrefixedUnit.getNullInstance();
        String rangeString = keyValuePairs.get(rangeKey);
        double range = (rangeString != null) ? Double.parseDouble(rangeString) : 0;
        double min = Double.parseDouble(keyValuePairs.get(minKey));

        NIDDimension channelDimension = new NIDDimension(dimensionIndex, name, unit, range, min);

        return channelDimension;
    }

    public static boolean containsDimensionInformation(int dimensionIndex, Map<String, String> keyValuePairs)
    {
        boolean contains = false;

        for(String info : INFO_SUFFICES)
        {
            contains = contains || containsDimensionInformation(dimensionIndex, keyValuePairs, info);
            if(contains)
            {
                break;
            }
        }

        return contains;
    }

    private static boolean containsDimensionInformation(int dimensionIndex, Map<String, String> keyValuePairs, String informationSuffix)
    {
        String dimensionIndexString = Integer.toString(dimensionIndex);

        String nameKey = DIM_PREFIX + dimensionIndexString + informationSuffix;

        String info = keyValuePairs.get(nameKey);

        boolean contains = (info != null);

        return contains;
    }
}