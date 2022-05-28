
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

package atomicJ.gui.statistics;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.JTable;


import atomicJ.gui.CellProperties;
import atomicJ.gui.DecimalStandardTableCellRenderer;
import atomicJ.gui.PropertiedTableCellRenderer;
import atomicJ.gui.SimpleCellPropertiesModel;

import com.lowagie.text.Font;

public class InferenceTableCellRenderer extends DecimalStandardTableCellRenderer 
{	
    private static final long serialVersionUID = 1L;

    public InferenceTableCellRenderer() 
    {
    }

    public InferenceTableCellRenderer(Preferences pref) 
    {
        super(pref);
    }

    public InferenceTableCellRenderer(DecimalFormat format) 
    {
        super(format);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        if(table instanceof InferencesTable)
        {
            if(column == 0 && row != 1)
            {
                Object[] data = (Object[]) value;
                SimpleCellPropertiesModel innerModel = new SimpleCellPropertiesModel(0, 1);

                CellProperties properties = new CellProperties(Color.lightGray, Font.BOLD);

                for(int i = 0; i<data.length; i++)
                {
                    Object newRow = data[i];
                    innerModel.addRow(new Object[] {newRow});
                    innerModel.setCellProperties(i, 0, properties);
                }        		
                JTable innerTable = new JTable(innerModel);

                PropertiedTableCellRenderer renderer = new PropertiedTableCellRenderer(getDecimalFormat(), new Insets(1,6,1,6));
                innerTable.setDefaultRenderer(Object.class, renderer);

                int innerTableRowHeight = innerTable.getRowHeight() + 2;
                for(int i = 0; i<innerTable.getRowCount();i++)
                {
                    innerTable.setRowHeight(innerTableRowHeight);
                }

                int desiredHeight = innerTableRowHeight*data.length;
                int currentHeight = table.getRowHeight(row);

                if(currentHeight != desiredHeight)
                {
                    table.setRowHeight(row, desiredHeight);
                }
                return innerTable;
            }
            else if(column == 1 && row == 0)
            {
                Object[][] data = (Object[][])value;
                InferencesTableModel model = (InferencesTableModel)((InferencesTable)table).getModel();
                int sampleCount = model.getSampleCount();

                SimpleCellPropertiesModel innerModel = new SimpleCellPropertiesModel(0, sampleCount);

                CellProperties properties = new CellProperties(Color.lightGray, Font.BOLD);

                for(int i = 0;i<data.length;i++)
                {
                    Object[] newRow = data[i];
                    innerModel.addRow(newRow);
                }
                innerModel.setCellProperties(0, 0, properties);
                innerModel.setCellProperties(0, 1, properties);

                JTable innerTable = new JTable(innerModel);
                innerTable.setIntercellSpacing(new Dimension(0,0));

                PropertiedTableCellRenderer renderer = new PropertiedTableCellRenderer(getDecimalFormat(),new Insets(1,6,1,6));

                innerTable.setDefaultRenderer(Object.class, renderer);

                int innerTableRowHeight = innerTable.getRowHeight() + 2;
                for(int i = 0; i<innerTable.getRowCount();i++)
                {
                    innerTable.setRowHeight(innerTableRowHeight);
                }
                int desiredHeight = innerTableRowHeight*data.length;
                int currentHeight = table.getRowHeight(row);

                if(currentHeight != desiredHeight)
                {
                    table.setRowHeight(row, desiredHeight);
                }
                return innerTable;
            }
            else if(column == 1 && row == 2)
            {
                Object[] data = (Object[]) value;
                SimpleCellPropertiesModel innerModel = new SimpleCellPropertiesModel(0, 1);


                CellProperties properties = new CellProperties(Color.lightGray, Font.BOLD);

                for(int i = 0; i<data.length; i++)
                {
                    Object newRow = data[i];
                    innerModel.addRow(new Object[] {newRow});
                }        
                innerModel.setCellProperties(0, 0, properties);

                JTable innerTable = new JTable(innerModel);
                innerTable.setIntercellSpacing(new Dimension(0,0));

                PropertiedTableCellRenderer renderer = new PropertiedTableCellRenderer(getDecimalFormat(),new Insets(1,6,1,6));
                innerTable.setDefaultRenderer(Object.class, renderer);

                int innerTableRowHeight = innerTable.getRowHeight() + 2;
                for(int i = 0; i<innerTable.getRowCount();i++)
                {
                    innerTable.setRowHeight(innerTableRowHeight);
                }
                int desiredHeight = innerTableRowHeight*data.length;
                int currentHeight = table.getRowHeight(row);

                if(currentHeight != desiredHeight)
                {
                    table.setRowHeight(row, desiredHeight);
                }
                return innerTable;
            }
            else
            {
                JLabel c = new JLabel();
                c.setOpaque(true);
                return c;
            }
        }
        else
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return c;
        }
    }
}
