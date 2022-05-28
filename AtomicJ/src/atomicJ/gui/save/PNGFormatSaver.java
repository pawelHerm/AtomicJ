
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

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;


import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;

import atomicJ.gui.Channel2DChart;

public class PNGFormatSaver extends BasicImageFormatSaver
{
    private static final String EXT = ".png";

    public PNGFormatSaver(Rectangle2D chartInitialArea, int width, int height, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);
    }

    @Override
    public String getExtension() 
    {
        return EXT;
    }	

    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException
    {
        BufferedImage image = getBufferedImage(chart, BufferedImage.TYPE_INT_RGB);

        EncoderUtil.writeBufferedImage(image, ImageFormat.PNG, out);
    }


    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame,
            OutputStream out) throws IOException 
    {
        BufferedImage image = getBufferedImage(chart, frame, BufferedImage.TYPE_INT_RGB);

        EncoderUtil.writeBufferedImage(image, ImageFormat.PNG, out);		
    }
}
