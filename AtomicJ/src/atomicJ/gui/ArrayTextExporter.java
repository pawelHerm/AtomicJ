
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
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import atomicJ.utilities.IOUtilities;

public class ArrayTextExporter 
{  

    private DecimalFormat getDefualtFormat()
    {
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        char groupingSeparator = ' ';               
        char decimalSeparator = '.';             
        int maxDigit = 4;
        int minDigit = 0;

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigit);

        format.setDecimalFormatSymbols(symbols);

        return format;
    }

    public void export(double[][] array, File file, String[] selectedExtensions) throws IOException 
    {
        export(array, file, getDefualtFormat(), selectedExtensions);
    }

    public void export(double[][] array, File file, DecimalFormat format, String[] selectedExtensions) throws IOException 
    {        
        int n = file.getName().lastIndexOf(".");
        boolean extensionProvided = n > 0;

        String selectedExt = selectedExtensions[0];

        if(selectedExt.equals("tsv"))
        {
            if(!extensionProvided)
            {
                file = new  File(file.getParent(), file.getName() + ".tsv");
            }
            exportAsTSV(array, file, format);
        }
        else
        {
            if(!extensionProvided)
            {
                file = new  File(file.getParent(), file.getName() + ".csv");
            }
            exportAsCSV(array, file, format);
        }
    }

    public void exportAsTSV(double[][] table, File file, DecimalFormat format) throws IOException 
    {
        export(table, file, "\t", format);
    }

    public void exportAsCSV(double[][] table, File file, DecimalFormat format) throws IOException 
    {
        String fieldSeparator = IOUtilities.findRigthCSVFieldSeparator(format);
        export(table, file, fieldSeparator, format);
    }

    private void export(double[][] array, File file, String fieldSeparator, DecimalFormat format) throws IOException 
    {
        try(FileWriter out = new FileWriter(file))
        {
            out.write("\n");

            for(double[] row : array) 
            {
                for(double el : row) 
                {				
                    String nextField = format.format(el) + fieldSeparator;

                    out.write(nextField);
                }

                out.write("\n");
            }
        }	
    }
}



