
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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


/**
 * Classes which implement this interface should be immutable
 */
public interface Quantity
{
    public String getName();	
    public String getFullUnitName();
    public PrefixedUnit getUnit();
    public Quantity deriveQuantity(PrefixedUnit unitNew);
    public boolean hasDimension();	
    public Quantity applyFunction(String functionName);
    public Quantity changeName(String nameNew);
    public String getLabel();

    public default boolean isUnitCompatible(Quantity otherQuantity)
    {
        boolean compatible = this.getUnit().isCompatible(otherQuantity.getUnit());            
        return compatible; 
    }
}
