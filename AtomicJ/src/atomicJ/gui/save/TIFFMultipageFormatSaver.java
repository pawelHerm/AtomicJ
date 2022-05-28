
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

import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import loci.common.ByteArrayHandle;
import loci.common.RandomAccessOutputStream;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffCompression;
import loci.formats.tiff.TiffRational;
import loci.formats.tiff.TiffSaver;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel2DChart;

public class TIFFMultipageFormatSaver extends BasicImageFormatSaver implements ZippableFrameFormatSaver
{
    private static final String EXT = ".tiff";

    private final TiffCompression compression;


    public TIFFMultipageFormatSaver(Rectangle2D chartInitialArea, int width, int height,
            TIFFMovieCompressionMethod compression, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);
        this.compression = compression.getCompression();  
    }


    @Override
    public String getExtension() 
    {
        return EXT;
    }

    @Override
    public void writeChartToStream(JFreeChart chart, final OutputStream out) throws IOException
    {        
    }

    @Override
    public void saveAsZip(final JFreeChart freeChart, final File path, final String entryName, ChartRenderingInfo info)
    {        
        List<JFreeChart> charts = new ArrayList<>();
        charts.add(freeChart);

        saveChartsAsZip(charts, path, entryName);       
    }

    public void saveChartsAsZip(final List<JFreeChart> charts, final File path, final String entryName)
    {
        final String pathName = path.getAbsolutePath();   

        final ByteArrayHandle byteHandle = new ByteArrayHandle();
        RandomAccessOutputStream raStream = new RandomAccessOutputStream(byteHandle);

        TiffMultipleChartsSavingTask task = new TiffMultipleChartsSavingTask(AtomicJ.getApplicationFrame(), charts, raStream);

        PropertyChangeListener taskStateListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
                {
                    try 
                    {
                        byte[] writtenBytes = byteHandle.getBytes();

                        byteHandle.close();

                        try(FileOutputStream fos = new FileOutputStream(pathName);
                                BufferedOutputStream bos = new BufferedOutputStream(fos);
                                ZipOutputStream zos = new ZipOutputStream(bos);)
                        {     
                            ZipEntry entry = new ZipEntry(entryName);
                            zos.putNextEntry(entry);

                            zos.write(writtenBytes);


                        } catch (IOException e) 
                        {
                            e.printStackTrace();
                        } 
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            }

        };

        task.addPropertyChangeListener("state", taskStateListener);
        task.execute();

    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame, OutputStream out) throws IOException 
    {         
    }

    @Override
    public void saveChart(JFreeChart freeChart, File path, ChartRenderingInfo info)
            throws IOException 
    {     

        List<JFreeChart> charts = new ArrayList<>();
        charts.add(freeChart);


        RandomAccessOutputStream out = new RandomAccessOutputStream(path.getAbsolutePath());

        TiffMultipleChartsSavingTask task = new TiffMultipleChartsSavingTask(AtomicJ.getApplicationFrame(),
                charts, out);
        task.execute();        
    }

    public void saveCharts(List<JFreeChart> charts, File path)
            throws IOException 
    {     
        RandomAccessOutputStream out = new RandomAccessOutputStream(path.getAbsolutePath());

        TiffMultipleChartsSavingTask task = new TiffMultipleChartsSavingTask(AtomicJ.getApplicationFrame(),
                charts, out);
        task.execute();        
    }

    protected byte[] getBytesFromBGRImage(BufferedImage image) throws IOException 
    {
        DataBuffer dataBuffer = image.getData().getDataBuffer();

        if(dataBuffer instanceof DataBufferByte)
        {
            byte[] bytes = ((DataBufferByte) dataBuffer).getData();
            int byteCount = bytes.length;
            int pixelCount = byteCount/3;

            byte[] bandedBytes = new byte[byteCount];

            for(int pixel = 0; pixel<pixelCount; pixel++)
            {
                int pixelStart = 3*pixel;
                byte blue = bytes[pixelStart];
                byte green = bytes[pixelStart + 1];
                byte red = bytes[pixelStart + 2];

                bandedBytes[pixel] = red;
                bandedBytes[pixelCount + pixel] = green;
                bandedBytes[2*pixelCount + pixel] = blue;
            }

            return bandedBytes;
        }

        return new byte[] {};
    }

    private void prepareToWriteTiffImage(TiffSaver saver, int no, byte[] buf, IFD ifd, int x, int y,
            int w, int h, TiffCompression compression) throws IOException, FormatException {

        ifd.put(IFD.COMPRESSION, compression.getCode());
        ifd.put(IFD.IMAGE_WIDTH, Long.valueOf( getWidth()));
        ifd.put(IFD.IMAGE_LENGTH, Long.valueOf( getHeight()));       
        ifd.put(IFD.RESOLUTION_UNIT, 3);
        ifd.put(IFD.X_RESOLUTION, new TiffRational((0), 1000));
        ifd.put(IFD.Y_RESOLUTION, new TiffRational((0), 1000));

        // write the image
        ifd.put(new Integer(IFD.LITTLE_ENDIAN), Boolean.TRUE);
        if (!ifd.containsKey(IFD.REUSE)) {
            ifd.put(IFD.REUSE, saver.getStream().length());
            saver.getStream().seek(saver.getStream().length());
        } else {
            saver.getStream().seek((Long) ifd.get(IFD.REUSE));
        }

        //TUTAJ JAK JEST INTERLEVED TO 1, A JAK BANDS, TO 2
        ifd.putIFDValue(IFD.PLANAR_CONFIGURATION, 2);
        ifd.putIFDValue(IFD.SAMPLE_FORMAT, 1); //1 means that it is unsigned
    }


    private class TiffMultipleChartsSavingTask extends MonitoredSwingWorker<Void, Void> 
    {
        private final List<JFreeChart> charts;
        private final RandomAccessOutputStream out;
        private final Component parent;
        private int savedFrames;

        public TiffMultipleChartsSavingTask(Component parent, List<JFreeChart> charts, RandomAccessOutputStream path)
        {
            super(parent, "Saving in progress", "Saved", charts.size());
            this.parent = parent;
            this.charts = charts;
            this.out = path;
        }

        @Override
        public Void doInBackground() throws IOException
        {
            TiffSaver tiffSaver = new TiffSaver(out, new ByteArrayHandle());

            try 
            {
                int frameCount = charts.size();

                int width = getWidth();
                int height = getHeight();

                tiffSaver.setLittleEndian(true);
                tiffSaver.setBigTiff(false);

                tiffSaver.writeHeader();

                for(int z = 0; z<frameCount; z++)
                {                   
                    boolean cancelled = isCancelled();

                    if(!cancelled)
                    {
                        JFreeChart chart = charts.get(z);
                        BufferedImage image = getBufferedImage(chart, BufferedImage.TYPE_3BYTE_BGR);
                        byte[] frameBytes = getBytesFromBGRImage(image);

                        IFD ifd = new IFD();

                        int pixelType = FormatTools.pixelTypeFromString("uint8");
                        boolean isLast = (z == frameCount -1);
                        prepareToWriteTiffImage(tiffSaver,z, frameBytes, ifd, 0,0, width, height, compression);
                        tiffSaver.writeImage(frameBytes, ifd, z, pixelType,
                                0,0, width, height, isLast);

                        savedFrames = z + 1;
                        setStep(savedFrames);    
                    }
                    else
                    {
                        break;
                    }               
                }
            } catch (Exception e) 
            {
                e.printStackTrace();
            } 
            finally
            {
                tiffSaver.getStream().close();
            }
            return null;
        }   

        @Override
        protected void done()
        {
            super.done();

            if(isCancelled())
            {
                JOptionPane.showMessageDialog(parent, "Saving terminated. Saved " + 
                        savedFrames + " frames", "", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        @Override
        public void cancelAllTasks() 
        {
            cancel(false);
        }
    }
}
