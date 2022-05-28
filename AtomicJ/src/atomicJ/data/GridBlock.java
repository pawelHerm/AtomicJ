
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

public class GridBlock 
{
    private final int rowMin;
    private final int rowCount;

    private final int columnMin;
    private final int columnCount;

    public GridBlock(int rowMin, int columnMin, int rowCount,  int columnCount)
    {
        this.rowMin = rowMin;
        this.columnMin = columnMin;

        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public int getMinimalRow()
    {
        return rowMin;
    }

    public int getMaximalRow()
    {
        int rowMax = rowMin + rowCount - 1;
        return rowMax;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public int getMinimalColumn()
    {
        return columnMin;
    }

    public int getMaximalColumn()
    {
        int columnMax = columnMin + columnCount - 1;
        return columnMax;
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    public boolean isEmpty()
    {
        boolean empty = (rowCount == 0) || (columnCount == 0);
        return empty;
    }
}
