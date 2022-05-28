
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

import java.text.NumberFormat;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

public class BasicXYZItemLabelGenerator implements XYItemLabelGenerator
{
    private final NumberFormat format;

    public BasicXYZItemLabelGenerator(NumberFormat format)
    {
        this.format = format;
    }

    @Override
    public String generateLabel(XYDataset dataset, int series, int item) 
    {
        String result  = "";

        if(dataset instanceof XYZDataset)
        {
            XYZDataset xyzDataset = (XYZDataset)dataset;
            double z = xyzDataset.getZValue(series, item);
            if (!(Double.isNaN(z) && dataset.getX(series, item) == null)) 
            {
                result = this.format.format(z);
            } 
        }

        return result;
    }

}
