
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


public class DimensionlessQuantity implements Quantity
{
    public static final DimensionlessQuantity NULL_QUANTITY = new DimensionlessQuantity("");
    public static final String RECIPROCAL = "Reciprocal";

    private final String name;
    private final PrefixedUnit dimensionlessUnit;

    public DimensionlessQuantity(String name)
    {
        this.name = name;
        this.dimensionlessUnit = SimplePrefixedUnit.getNullInstance();
    }

    public DimensionlessQuantity(String name, PrefixedUnit dimensionlessUnit)
    {
        this.name = name;
        this.dimensionlessUnit = dimensionlessUnit;
    }

    public static DimensionlessQuantity getNullInstance()
    {
        return NULL_QUANTITY;
    }

    @Override
    public PrefixedUnit getUnit()
    {
        return dimensionlessUnit;
    }

    @Override
    public DimensionlessQuantity deriveQuantity(PrefixedUnit unitNew)
    {
        DimensionlessQuantity quantityNew = new DimensionlessQuantity(name, unitNew);
        return quantityNew;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getLabel()
    {
        return name;
    }

    @Override
    public boolean hasDimension() 
    {
        return false;
    }

    @Override
    public String getFullUnitName()
    {
        return dimensionlessUnit.getFullName();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof DimensionlessQuantity)
        {
            String otherName = ((DimensionlessQuantity)o).getName();
            return this.name.equals(otherName);
        }
        else
        {
            return false;
        }
    }

    @Override
    public Quantity applyFunction(String functionName) 
    {
        String newQuantityNam;
        if(RECIPROCAL.equals(functionName) && name.startsWith(RECIPROCAL))
        {
            newQuantityNam = name.replaceFirst(RECIPROCAL + " (", "");
            newQuantityNam = newQuantityNam.substring(0, newQuantityNam.length() - 1);          
        }
        else
        {
            newQuantityNam = functionName + " (" + name + ")";
        }
        DimensionlessQuantity quantityNew = new DimensionlessQuantity(newQuantityNam);
        return quantityNew;
    }

    @Override
    public Quantity changeName(String nameNew) 
    {
        Quantity newQuantity = new DimensionlessQuantity(nameNew);
        return newQuantity;
    }
}
