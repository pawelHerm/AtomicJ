
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
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;

import org.jfree.chart.JFreeChart;

import atomicJ.gui.Channel2DChart;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriter;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi;

public class JPEG2000FormatSaver extends BasicImageFormatSaver
{
    private static final String EXT = ".jp2";

    private final boolean lossless;
    private final double encodingRate;
    private final String filter;

    public JPEG2000FormatSaver(double encodingRate, boolean lossless, Rectangle2D chartInitialArea, int width, int height, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);
        this.encodingRate = lossless ? Double.MAX_VALUE : encodingRate;
        this.lossless = lossless;
        this.filter = lossless ? J2KImageWriteParam.FILTER_53 :J2KImageWriteParam.FILTER_97;
    }

    @Override
    public String getExtension() 
    {
        return EXT;
    }

    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException
    {
        BufferedImage image = getBufferedImage(chart, BufferedImage.TYPE_3BYTE_BGR);

        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);

            IIORegistry registry = IIORegistry.getDefaultInstance();
            Iterator<J2KImageWriterSpi> iter = ServiceRegistry.lookupProviders(J2KImageWriterSpi.class);
            registry.registerServiceProviders(iter);
            J2KImageWriterSpi spi = registry.getServiceProviderByClass(J2KImageWriterSpi.class);
            J2KImageWriter writer = new J2KImageWriter(spi);
            writer.setOutput(ios);

            IIOImage iioImage = new IIOImage(image, null, null);

            J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG2000");
            param.setLossless(lossless);
            param.setFilter(filter);
            param.setCodeBlockSize(new int[]{64,64});

            param.setEncodingRate(encodingRate);

            writer.write(null, iioImage, param);
            ios.close();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame,
            OutputStream out) throws IOException 
    {       
        BufferedImage image = getBufferedImage(chart, frame, BufferedImage.TYPE_3BYTE_BGR);

        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);

            IIORegistry registry = IIORegistry.getDefaultInstance();
            Iterator<J2KImageWriterSpi> iter = ServiceRegistry.lookupProviders(J2KImageWriterSpi.class);
            registry.registerServiceProviders(iter);
            J2KImageWriterSpi spi = registry.getServiceProviderByClass(J2KImageWriterSpi.class);
            J2KImageWriter writer = new J2KImageWriter(spi);
            writer.setOutput(ios);

            IIOImage iioImage = new IIOImage(image, null, null);

            J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG2000");
            param.setLossless(lossless);
            param.setFilter(filter);
            param.setCodeBlockSize(new int[]{64,64});

            param.setEncodingRate(encodingRate);

            writer.write(null, iioImage, param);
            ios.close();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}