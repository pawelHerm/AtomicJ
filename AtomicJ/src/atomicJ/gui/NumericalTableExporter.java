
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

import java.io.*;
import java.text.DecimalFormat;

import javax.swing.table.TableModel;

import atomicJ.utilities.IOUtilities;

public class NumericalTableExporter 
{  
    public void export(MinimalNumericalTable table, File file, DecimalFormat format, String[] selectedExtensions) throws IOException 
    {        
        int n = file.getName().lastIndexOf(".");
        boolean extensionProvided = n > 0;

        String selectedExt = selectedExtensions[0];

        if("tsv".equals(selectedExt))
        {
            if(!extensionProvided)
            {
                file = new  File(file.getParent(), file.getName() + ".tsv");
            }
            exportTableAsTSV(table, file, format);
        }
        else
        {
            if(!extensionProvided)
            {
                file = new  File(file.getParent(), file.getName() + ".csv");
            }
            exportTableAsCSV(table, file, format);
        }
    }

    public void exportTableAsTSV(MinimalNumericalTable table, File file, DecimalFormat format) throws IOException 
    {
        exportTable(table, file, "\t", format);
    }

    public void exportTableAsCSV(MinimalNumericalTable table, File file, DecimalFormat format) throws IOException 
    {
        String fieldSeparator = IOUtilities.findRigthCSVFieldSeparator(format);
        exportTable(table, file, fieldSeparator, format);
    }

    private void exportTable(MinimalNumericalTable table, File file, String fieldSeparator, DecimalFormat format) throws IOException 
    {
        TableModel model = table.getModel();
        DecimalTableCellRenderer renderer = table.getDecimalCellRenderer();

        try(FileWriter out = new FileWriter(file))
        {
            for(int viewColumn = 0; viewColumn < table.getColumnCount(); viewColumn++) 
            {
                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                out.write(model.getColumnName(modelColumn) + fieldSeparator);
            }

            out.write("\n");

            for(int viewRow=0; viewRow< table.getRowCount(); viewRow++) 
            {
                for(int viewColumn = 0; viewColumn < table.getColumnCount(); viewColumn++) 
                {				
                    Object currentValue = table.getValueForSave(viewRow, viewColumn);

                    int modelColumn = table.convertColumnIndexToModel(viewColumn);
                    String nextField = renderer.getValue(currentValue, modelColumn) + fieldSeparator;
                    out.write(nextField);
                }
                out.write("\n");
            }
        }	
    }
}



