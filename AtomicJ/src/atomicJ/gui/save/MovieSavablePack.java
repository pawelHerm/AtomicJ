
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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



import java.io.File;
import java.io.IOException;


import org.jfree.chart.ChartRenderingInfo;

import atomicJ.gui.Channel2DChart;

public class MovieSavablePack implements Saveable
{
    private final File path;
    private final int frame;
    private final Channel2DChart<?> chart;
    private final ChartSaver saver;
    private final ChartRenderingInfo info;

    public MovieSavablePack(Channel2DChart<?> chart, int frame, File path, ChartSaver saver, ChartRenderingInfo info)
    {
        this.chart = chart;
        this.frame = frame;
        this.path = path;
        this.saver = saver;
        this.info = info;
    }

    @Override
    public void save() throws IOException
    {
        saver.saveMovieFrame(chart, frame, path, info);
    }
}
