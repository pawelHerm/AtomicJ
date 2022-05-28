
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

package atomicJ.gui.save;


import java.io.IOException;
import java.io.OutputStream;
import org.jfree.chart.JFreeChart;

public class SimpleStreamSavablePack implements StreamSavable
{
    private final String name;
    private final JFreeChart chart;
    private final ChartSaver saver;

    public SimpleStreamSavablePack(JFreeChart chart, String name, ChartSaver saver)
    {
        this.chart = chart;
        this.name = name;
        this.saver = saver;
    }

    @Override
    public String getName() 
    {
        return name;
    }

    @Override
    public void save(OutputStream stream) throws IOException 
    {
        saver.writeChartToStream(chart, stream);
    }

}
