
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

package atomicJ.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

public class SimpleCellPropertiesModel extends DefaultTableModel implements CellPropertiesModel
{
    private static final long serialVersionUID = 1L;
    private static final CellProperties DEFAULT_CELL_PROPERTIES = new CellProperties(Color.white, Font.PLAIN);

    private final HashMap<Long, CellProperties> propertiesMap = new HashMap<>();

    public SimpleCellPropertiesModel(int rows, int columns)
    {
        super(rows, columns);
    }

    @Override
    public CellProperties getCellProperties(int row, int column) 
    {
        long n = ((row & 0XFFFFFFFFL) << 32) | (column & 0XFFFFFFFFL);
        CellProperties properties = propertiesMap.get(n);
        if(properties == null)
        {
            return DEFAULT_CELL_PROPERTIES;
        }
        else
        {
            return properties;
        }
    }

    @Override
    public void setCellProperties(int row, int column, CellProperties newProperties) 
    {
        long n = ((row & 0XFFFFFFFFL) << 32) | (column & 0XFFFFFFFFL);
        propertiesMap.put(n, newProperties);
    }
}
