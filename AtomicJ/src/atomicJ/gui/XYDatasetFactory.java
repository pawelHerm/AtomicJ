
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

import java.util.List;


import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.*;

import atomicJ.data.QuantitativeSample;

public class XYDatasetFactory
{
    private XYDatasetFactory(){};

    public static IntervalXYDataset getHistogramDataset(QuantitativeSample sample, int bins, HistogramType type)
    {
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(type);

        String name = sample.getQuantityName();
        double[] data = sample.getMagnitudes();
        dataset.addSeries(name, data, bins);

        return dataset;
    }	

    public static IntervalXYDataset getHistogramDataset(List<QuantitativeSample> samples, int bins, HistogramType type)
    {
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(type);

        for(int i = 0;i<samples.size();i++)
        {
            QuantitativeSample sample = samples.get(i);
            String name = sample.getQuantityName();
            double[] data = sample.getMagnitudes();
            dataset.addSeries(name, data, bins);
        }
        return dataset;
    }
}


