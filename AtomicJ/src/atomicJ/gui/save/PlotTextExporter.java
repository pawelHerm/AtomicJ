
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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


import java.io.*;
import java.text.NumberFormat;
import java.util.Locale;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import atomicJ.gui.Channel2DPlot;
import atomicJ.gui.Movie2DDataset;


public class PlotTextExporter 
{
    private final String fieldSeparator;
    private final NumberFormat format;

    public PlotTextExporter(String fieldSeparator) 
    {
        this.fieldSeparator = fieldSeparator;
        this.format = NumberFormat.getInstance(Locale.US);
    }

    public PlotTextExporter(String fieldSeparator, NumberFormat format) 
    {
        this.fieldSeparator = fieldSeparator;
        this.format = format;
    }

    public void export(XYPlot plot, File file) throws IOException 
    {
        writeOrExport(plot, new PrintWriter(file), true);
    }

    public void export(XYPlot plot, int frame, File file) throws IOException 
    {
        writeOrExport(plot, frame, new PrintWriter(file), true);
    }

    public void writeToStream(XYPlot plot, OutputStream out,boolean close) throws IOException 
    {
        writeOrExport(plot, new PrintWriter(out), close);
    }

    public void writeToStream(XYPlot plot, int frame, OutputStream out,boolean close) throws IOException 
    {
        writeOrExport(plot, frame, new PrintWriter(out), close);
    }

    public void writeOrExport(XYPlot plot, PrintWriter out, boolean close) throws IOException 
    {		
        String xLabel = plot.getDomainAxis().getLabel();
        String yLabel = plot.getRangeAxis().getLabel();
        String zLabel = null;
        if(plot instanceof Channel2DPlot)
        {
            zLabel = ((Channel2DPlot) plot).getDepthAxis(0).getLabel();
        }
        try
        {					
            out.println("X axis: " + xLabel.replace("μ", "u"));
            out.println("Y axis: " + yLabel.replace("μ", "u"));
            if(zLabel != null)
            {
                out.println("Z axis: " + zLabel.replace("μ", "u"));
            }

            int n  = plot.getDatasetCount();
            for(int i = 0; i<n;i++)
            {
                XYDataset dataset = plot.getDataset(i);
                writeDataset(dataset, out);
            }		
        }
        finally
        {
            if(close)
            {
                out.close();
            }
        }
    }

    public void writeOrExport(XYPlot plot, int frame, PrintWriter out, boolean close) throws IOException 
    {		
        String xLabel = plot.getDomainAxis().getLabel().replace("μ", "u");
        String yLabel = plot.getRangeAxis().getLabel().replace("μ", "u");
        String zLabel = null;
        if(plot instanceof Channel2DPlot)
        {
            zLabel = ((Channel2DPlot) plot).getDepthAxis(0).getLabel().replace("μ", "u");
        }

        try
        {					
            out.println("X axis: " + xLabel);
            out.println("Y axis: " + yLabel);
            if(zLabel != null)
            {
                out.println("Z axis: " + zLabel);
            }

            int n  = plot.getDatasetCount();
            for(int i = 0; i<n;i++)
            {
                XYDataset dataset = plot.getDataset(i);
                if(dataset instanceof Movie2DDataset)
                {
                    Movie2DDataset movieDataset = (Movie2DDataset)dataset;
                    dataset = movieDataset.getFrame(frame);
                }
                writeDataset(dataset, out);
            }		
        }
        finally
        {
            if(close)
            {
                out.close();
            }
        }
    }

    private void writeDataset(XYDataset dataset, PrintWriter out)
    {

        if(dataset instanceof XYZDataset)
        {
            writeXYZDataset((XYZDataset)dataset, out);
        }
        else
        {
            int m = dataset.getSeriesCount();

            for(int j=0; j < m; j++) 
            {
                out.println("\n" + dataset.getSeriesKey(j) + "\n");

                int r = dataset.getItemCount(j);

                for(int k = 0; k < r; k++) 
                {
                    out.print(format.format(dataset.getXValue(j, k)));
                    out.print(fieldSeparator);
                    out.println(format.format(dataset.getYValue(j, k)));
                }
            }
        }

    }

    private void writeXYZDataset(XYZDataset dataset, PrintWriter out)
    {
        int m = dataset.getSeriesCount();

        for(int j=0; j < m; j++) 
        {
            out.println("\n" + dataset.getSeriesKey(j) + "\n");

            int r = dataset.getItemCount(j);

            for(int k = 0; k < r; k++) 
            {
                out.print(format.format(dataset.getXValue(j, k)));
                out.print(fieldSeparator);
                out.print(format.format(dataset.getYValue(j, k)));
                out.print(fieldSeparator);
                out.println(format.format(dataset.getZValue(j, k)));
            }
        }
    }
}


