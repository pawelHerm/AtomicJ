
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



import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;


import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.gui.Channel2DChart;

public class DelimiterSeparatedValuesFormatSaver extends ChartSaver
{
    private final String extension;
    private final PlotTextExporter exporter;

    public DelimiterSeparatedValuesFormatSaver(String fieldSeparator, String extension)
    {
        this.exporter = new PlotTextExporter(fieldSeparator);
        this.extension = extension;
    }

    public DelimiterSeparatedValuesFormatSaver(String fieldSeparator, String extension, NumberFormat format)
    {
        this.exporter = new PlotTextExporter(fieldSeparator, format);
        this.extension = extension;
    }

    @Override
    public String getExtension() 
    {
        return extension;
    }

    @Override
    public void saveChart(JFreeChart chart, File path, ChartRenderingInfo info) throws IOException 
    {
        exporter.export(chart.getXYPlot(),path);
    }

    @Override
    public void saveMovieFrame(Channel2DChart<?> chart, int frame, File path, ChartRenderingInfo info) throws IOException 
    {
        exporter.export(chart.getXYPlot(), frame, path);
    }

    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException 
    {
        exporter.writeToStream(chart.getXYPlot(), out, false);
    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame, OutputStream out) throws IOException 
    {
        exporter.writeToStream(chart.getXYPlot(), frame, out, false);		
    }
}
