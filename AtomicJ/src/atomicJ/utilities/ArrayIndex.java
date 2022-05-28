
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

package atomicJ.utilities;

public class ArrayIndex 
{
    private final int row;
    private final int column;

    public ArrayIndex(int row, int column)
    {
        if(row <0)
        {
            throw new IllegalArgumentException("Negative 'row' argument");
        }
        if(column <0)
        {
            throw new IllegalArgumentException("Negative 'column' argument");
        }

        this.row = row;
        this.column = column;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof ArrayIndex)
        {
            ArrayIndex thatIndex = (ArrayIndex)that;

            if(this.row != thatIndex.row)
            {
                return false;
            }
            else if(this.column != thatIndex.column)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
}
