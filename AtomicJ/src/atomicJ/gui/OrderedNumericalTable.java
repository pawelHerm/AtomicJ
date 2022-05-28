
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

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public class OrderedNumericalTable extends StandardNumericalTable
{
    private static final long serialVersionUID = 1L;

    public static final String RESULTS_EMPTY = "Results empty";

    public OrderedNumericalTable(NumericalTableModel model, boolean addPopup)
    {
        super(model, false, addPopup);
        getColumnModel().getColumn(0).setCellRenderer(new RowNumberRenderer());

        TableRowSorter<NumericalTableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortable(0, false);		
        setRowSorter(sorter);
    }

    @Override
    public Object getValueForSave(int viewRow, int viewColumn)
    {
        int modelRow = convertRowIndexToModel(viewRow);
        int modelColumn = convertColumnIndexToModel(viewColumn);
        Object value = (modelColumn == 0) ? viewRow : getModel().getValueAt(modelRow, modelColumn);
        return value;
    }

    private static class RowNumberRenderer extends JLabel implements TableCellRenderer 
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color,
                boolean isSelected, boolean hasFocus, int row, int column) 
        {
            setText(Integer.toString(row + 1));
            return this;
        }
    }
}
