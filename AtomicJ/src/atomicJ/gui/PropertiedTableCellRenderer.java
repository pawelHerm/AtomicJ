
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
import java.awt.Component;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableModel;

public class PropertiedTableCellRenderer extends DecimalStandardTableCellRenderer
{
    private static final long serialVersionUID = 1L;
    private final Insets insets;

    public PropertiedTableCellRenderer(Preferences pref, Insets insets) 
    {
        super(pref);
        this.insets = insets;
    }

    public PropertiedTableCellRenderer(DecimalFormat format, Insets insets)
    {
        super(format);
        this.insets = insets;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        TableModel model = table.getModel();

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if(model instanceof CellPropertiesModel)
        {
            CellPropertiesModel propertiesModel = (CellPropertiesModel)model;
            CellProperties properties = propertiesModel.getCellProperties(row, column);	

            if(properties != null)
            {
                int fontStyle = properties.getFont();
                c.setFont(c.getFont().deriveFont(fontStyle));
                Color color = properties.getColor();
                if(JComponent.class.isInstance(c))
                {
                    Border border = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right);
                    ((JComponent)c).setBorder(border);
                }

                if(color != null && !isSelected)
                {
                    c.setBackground(color);
                }
            }
        }

        return c;
    }
}
