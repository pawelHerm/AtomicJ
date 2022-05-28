
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

package atomicJ.data.units;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitQuantity implements Quantity
{	
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(.+?)(?:\\[|\\()(.*)(?:\\]|\\))");

    public static final UnitQuantity NULL_QUANTITY = new UnitQuantity("", SimplePrefixedUnit.getNullInstance());

    public static final String RECIPROCAL = "Reciprocal";

    private final String name;
    private final PrefixedUnit unit;
    private final String label;

    public UnitQuantity(String name, PrefixedUnit unit)
    {
        this.name = name;
        this.unit = unit;		
        this.label = name + " (" + unit.getFullName() + ")";
    }

    public static UnitQuantity buildQuantity(String label)
    {
        Matcher matcher = QUANTITY_PATTERN.matcher(label.trim());
        matcher.matches();
        String name = matcher.group(1).trim();
        String unitString = matcher.group(2);

        PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);

        UnitQuantity quantity = new UnitQuantity(name, unit);
        return quantity;
    }

    @Override
    public UnitQuantity deriveQuantity(PrefixedUnit unitNew)
    {
        UnitQuantity quantityNew = new UnitQuantity(name, unitNew);
        return quantityNew;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public PrefixedUnit getUnit()
    {
        return unit;
    }

    @Override
    public String getFullUnitName()
    {
        return unit.getFullName();
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public boolean hasDimension() 
    {
        return true;
    }

    @Override
    public Quantity applyFunction(String functionName) 
    {
        String newQuantityNam = "";
        if(RECIPROCAL.equals(functionName))
        {
            if(name.startsWith(RECIPROCAL))
            {
                newQuantityNam = name.replaceFirst(RECIPROCAL + " \\(", "");
                newQuantityNam = newQuantityNam.substring(0, newQuantityNam.length() - 1);

            }
            else
            {
                newQuantityNam = functionName + " (" + name + ")";
            }
        }
        else
        {
            newQuantityNam = functionName + " (" + name + ")";
        }
        UnitQuantity quantityNew = new UnitQuantity(newQuantityNam, unit);
        return quantityNew;
    }

    @Override
    public Quantity changeName(String nameNew) 
    {
        UnitQuantity quantityNew = new UnitQuantity(nameNew, unit);
        return quantityNew;
    }

    @Override
    public int hashCode()
    {
        int result = 17;

        result = 31*result + this.name.hashCode();
        result = 31*result + this.unit.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof UnitQuantity)
        {
            String otherName = ((UnitQuantity)other).name;
            PrefixedUnit otherUnit = ((UnitQuantity)other).unit;
            return (this.name.equals(otherName) && this.unit.equals(otherUnit));
        }

        return false;
    }

    @Override
    public String toString()
    {       
        return label;
    }
}
