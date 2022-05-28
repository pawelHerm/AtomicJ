
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



import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.gui.Channel2DChart;

public class SVGFormatSaver extends ChartSaver
{
    private static final String EXT = ".svg";

    private final int width;
    private final int height;

    public SVGFormatSaver(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    @Override
    public void saveChart(JFreeChart chart, File path, ChartRenderingInfo info) throws IOException 
    {
        OutputStream out = new java.io.FileOutputStream(path);

        VectorGraphics g2d = new SVGGraphics2D(out, new Dimension(width,height));
        g2d.startExport();
        chart.draw(g2d,new Rectangle(width,height));
        g2d.endExport();
    }


    @Override
    public String getExtension() 
    {
        return EXT;
    }

    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException
    {
        SVGGraphics2D g2d = new SVGGraphics2D(out,new Dimension(width,height));
        g2d.startExport();
        chart.draw(g2d,new Rectangle(width,height));
        g2d.dispose();
        g2d.writeTrailer();
    }

    @Override
    public void saveMovieFrame(Channel2DChart<?> chart, int frame, File path,
            ChartRenderingInfo info) throws IOException 
    {
        OutputStream out = new java.io.FileOutputStream(path);

        VectorGraphics g2d = new SVGGraphics2D(out, new Dimension(width,height));
        g2d.startExport();
        chart.draw(g2d,new Rectangle(width,height), null, null, frame);
        g2d.endExport();		
    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame,
            OutputStream out) throws IOException 
    {
        SVGGraphics2D g2d = new SVGGraphics2D(out,new Dimension(width,height));
        g2d.startExport();
        chart.draw(g2d,new Rectangle(width,height), null, null, frame);
        g2d.dispose();
        g2d.writeTrailer();		
    }
}
