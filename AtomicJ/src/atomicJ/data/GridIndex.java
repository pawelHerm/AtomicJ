
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

public class GridIndex 
{
    private final int row;
    private final int column;

    public GridIndex(int row, int column)
    {
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


    public boolean isWithinBounds(int rowCount, int columnCount)
    {
        if(row < 0 ||  column < 0 
                || row >= rowCount || column >= columnCount)
        {
            return false;
        }

        return true;
    }

    public static boolean isWithinBounds(int row, int column, int rowCount, int columnCount)
    {
        if(row < 0 ||  column < 0 
                || row >= rowCount || column >= columnCount)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 20;

        result = 31*result + row;
        result = 31*result + column;

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof GridIndex)
        {
            GridIndex otherIndex = (GridIndex)other;
            if(this.row == otherIndex.row && this.column == otherIndex.column)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        String string = "(" + Integer.toString(row) + ", " + Integer.toString(column) + ")";
        return string;
    }
}
