
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
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.gui.Channel2DChart;

public class EMFFormatSaver extends BasicImageFormatSaver
{
    private static final String EXT = ".emf";


    public EMFFormatSaver(Rectangle2D chartInitialArea, int width, int height, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);
    }

    @Override
    public String getExtension() 
    {
        return EXT;
    }

    @Override
    public void saveChart(JFreeChart chart, File path, ChartRenderingInfo info) throws IOException 
    {
        OutputStream out = new java.io.FileOutputStream(path);

        VectorGraphics g2d = new EMFGraphics2D(out,new Dimension(getWidth(),getHeight()));
        g2d.startExport();
        paintOnGraphicsDevice(chart, g2d);
        g2d.endExport();
    }


    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException
    {
        EMFGraphics2D g2d = new EMFGraphics2D(out,new Dimension(getWidth(),getHeight()));
        g2d.setDeviceIndependent(true);
        g2d.startExport();
        paintOnGraphicsDevice(chart, g2d);
        g2d.writeTrailer();
    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame, OutputStream out) throws IOException 
    {
        EMFGraphics2D g2d = new EMFGraphics2D(out,new Dimension(getWidth(),getHeight()));
        g2d.setDeviceIndependent(true);
        g2d.startExport();
        paintOnGraphicsDevice(chart, frame, g2d);
        g2d.writeTrailer();
    }
}
