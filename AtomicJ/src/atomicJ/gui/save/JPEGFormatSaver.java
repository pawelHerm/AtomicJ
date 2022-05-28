
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
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


import org.jfree.chart.JFreeChart;

import atomicJ.gui.Channel2DChart;

public class JPEGFormatSaver extends BasicImageFormatSaver
{
    private static final String EXT = ".jpg";

    private final float quality;

    public JPEGFormatSaver(float quality, Rectangle2D chartInitialArea, int width, int height, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);
        this.quality = quality;
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

        writeImage(image, out);
    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame,
            OutputStream out) throws IOException 
    {		

        BufferedImage image = getBufferedImage(chart, frame, BufferedImage.TYPE_INT_RGB);     
        writeImage(image, out);
    }

    private void writeImage(BufferedImage image, OutputStream out) throws IOException
    {
        Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = iterator.next();
        ImageWriteParam p = writer.getDefaultWriteParam();
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(this.quality);
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), p);
        ios.flush();
        writer.dispose();

        ios.close();
    }
}
