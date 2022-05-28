
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
import atomicJ.utilities.IOUtilities;

public class NumericalArrayExporter 
{
    public NumericalArrayExporter() { }

    public void exportTableAsTSV(Object[][] data, File file, DecimalFormat format) throws IOException 
    {
        exportTable(data, file, "\t", format);
    }

    public void exportTableAsCSV(Object[][] data, File file, DecimalFormat format) throws IOException 
    {
        String fieldSeparator = IOUtilities.findRigthCSVFieldSeparator(format);
        exportTable(data, file, fieldSeparator, format);
    }

    private void exportTable(Object[][] data, File file, String fieldSeparator, DecimalFormat format) throws IOException 
    {		
        try(FileWriter out = new FileWriter(file))
        {					
            for(int i = 0; i< data.length; i++) 
            {
                Object[] row = data[i];
                for(int j = 0; j < row.length; j++) 
                {
                    Object currentValue = row[j];
                    String nextField;
                    if(currentValue == null)
                    {
                        nextField = ""  + fieldSeparator;
                    }
                    else 
                    {
                        if(currentValue instanceof Number)
                        {
                            nextField = format.format(currentValue) + fieldSeparator;
                        }
                        else
                        {
                            nextField = currentValue.toString() + fieldSeparator;
                        }
                    }
                    out.write(nextField);
                }
                out.write("\n");
            }
        }	
    }
}



