
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


import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.plot.XYPlot;

import atomicJ.gui.Channel2DChart;
import atomicJ.gui.Channel2DPlot;
import atomicJ.utilities.Validation;

public abstract class BasicImageFormatSaver extends ChartSaver
{	
    private final Rectangle2D chartInitialArea;
    private final int width;
    private final int height;

    private final boolean saveDataArea;

    public BasicImageFormatSaver(Rectangle2D chartInitialArea, int width, int height, boolean saveDataArea)
    {
        this.chartInitialArea = chartInitialArea;
        this.width = width;
        this.height = height;
        this.saveDataArea = saveDataArea;
    }

    @Override
    public void saveMovieFrame(Channel2DChart<?> chart, int frame, File path,
            ChartRenderingInfo info) throws IOException 
    {
        Validation.requireNonNullParameterName(path, "path");
        Validation.requireNonNullParameterName(chart, "chart");

        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        try 
        {
            writeMovieFrameToStream(chart, frame, out);
        }
        finally 
        {
            out.close();
        }		
    }

    @Override
    public void saveChart(JFreeChart chart, File path, ChartRenderingInfo info) throws IOException 
    {
        Validation.requireNonNullParameterName(path, "path");
        Validation.requireNonNullParameterName(chart, "chart");

        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        try 
        {
            writeChartToStream(chart, out);
        }
        finally 
        {
            out.close();
        }
    }

    protected void paintOnGraphicsDevice(JFreeChart chart, Graphics2D g2)
    {
        if(saveDataArea)
        {
            Rectangle2D plotArea = chart.getPlotArea(g2,chartInitialArea);
            XYPlot xyPlot = chart.getXYPlot();
            Rectangle2D initialDataArea = xyPlot.getDataArea(g2, plotArea);
            Map<Axis, List<ValueTick>> gridLines = xyPlot.getGridlines(g2, initialDataArea);

            double dataInitialWidth = initialDataArea.getWidth();
            double dataInitialHeight = initialDataArea.getHeight();

            double scaleX = getWidth()/dataInitialWidth;
            double scaleY = getHeight()/dataInitialHeight;

            AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
            g2.transform(st);
            xyPlot.drawDataArea(g2, new Rectangle2D.Double(0,0, dataInitialWidth, dataInitialHeight), null, gridLines, null, null);
        }
        else
        {
            double chartInitialWidth = chartInitialArea.getWidth();
            double chartInitialHeight = chartInitialArea.getHeight();
            double scaleX = getWidth()/chartInitialWidth;
            double scaleY = getHeight()/chartInitialHeight;

            AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
            g2.transform(st);
            chart.draw(g2, new Rectangle2D.Double(0, 0, chartInitialWidth, chartInitialHeight), null, null);
        }	
    }

    protected void paintOnGraphicsDevice(Channel2DChart<?> chart, int frame, Graphics2D g2)
    {
        if(saveDataArea)
        {
            Rectangle2D plotArea = chart.getPlotArea(g2,chartInitialArea);
            Channel2DPlot plot = chart.getCustomizablePlot();
            Rectangle2D initialDataArea = plot.getDataArea(g2, plotArea);
            Map<Axis, List<ValueTick>> gridLines = plot.getGridlines(g2, initialDataArea);

            double dataInitialWidth = initialDataArea.getWidth();
            double dataInitialHeight = initialDataArea.getHeight();

            double scaleX = getWidth()/dataInitialWidth;
            double scaleY = getHeight()/dataInitialHeight;


            AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
            g2.transform(st);
            plot.drawDataArea(g2, new Rectangle2D.Double(0,0, dataInitialWidth, dataInitialHeight), null, gridLines, null, null, chart.getStackModel(), frame);
        }
        else
        {
            double chartInitialWidth = chartInitialArea.getWidth();
            double chartInitialHeight = chartInitialArea.getHeight();
            double scaleX = getWidth()/chartInitialWidth;
            double scaleY = getHeight()/chartInitialHeight;

            AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
            g2.transform(st);

            chart.draw(g2, new Rectangle2D.Double(0, 0, chartInitialWidth, chartInitialHeight), null, null, frame);

        }	
    }

    protected BufferedImage getBufferedImage(JFreeChart chart, int type)
    {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), type);
        Graphics2D g2 = image.createGraphics(); 
        paintOnGraphicsDevice(chart,  g2);

        g2.dispose();

        return image;
    }

    protected BufferedImage getBufferedImage(Channel2DChart<?> chart, int frame, int type)
    {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), type);
        Graphics2D g2 = image.createGraphics(); 
        paintOnGraphicsDevice(chart, frame, g2);

        g2.dispose();

        return image;
    }

    public int getWidth() 
    {
        return width;
    }

    public int getHeight() 
    {
        return height;
    }
}
