
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
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.gui.Channel2DChart;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class PDFFormatSaver extends BasicImageFormatSaver 
{
    private static final String EXT = ".pdf";

    private final com.lowagie.text.Rectangle pageSize;
    private final Insets margins;

    public PDFFormatSaver(Rectangle2D chartInitialArea, int width, int height, com.lowagie.text.Rectangle pageSize, Insets margins, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);

        this.pageSize = pageSize;
        this.margins = margins;
    }

    @Override
    public void saveChart(JFreeChart chart, File path, ChartRenderingInfo info) throws IOException 
    {	
        try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));)
        {
            com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize, margins.left, margins.right, margins.top, margins.bottom);
            try 
            {
                PdfWriter writer = PdfWriter.getInstance(document, out);
                document.open();
                drawChartToNewPage(chart, document, writer);
            } 
            catch (DocumentException e) 
            {
                e.printStackTrace();
            } 
            finally 
            {
                document.close();
            }
        }
    }

    public void saveCharts(List<JFreeChart> charts, File path) throws IOException 
    {
        try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));)
        {
            com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize, margins.left, margins.right, margins.top, margins.bottom);
            try 
            {
                PdfWriter writer = PdfWriter.getInstance(document, out);
                document.open();
                document.setMarginMirroring(true);

                for(JFreeChart chart: charts)
                {
                    drawChartToNewPage(chart,document, writer);
                    document.newPage();
                }
            } 
            catch (DocumentException e) 
            {
                e.printStackTrace();
            } 
            finally 
            {
                document.close();
            }
        }
    }

    private void drawChartToNewPage(JFreeChart chart, com.lowagie.text.Document document, PdfWriter writer)
    {
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(getWidth(), getHeight());
        Graphics2D g2 = tp.createGraphicsShapes(getWidth(), getHeight());
        paintOnGraphicsDevice(chart, g2);
        g2.dispose();
        cb.addTemplate(tp, margins.left, margins.bottom);
    }

    @Override
    public String getExtension() 
    {
        return EXT;
    }

    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException
    {
        com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize, margins.left, margins.right, margins.top, margins.bottom);
        try 
        {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setCloseStream(false);
            document.open();

            drawChartToNewPage(chart,document, writer);
        } 
        catch (DocumentException e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            document.close();
        }
    }

    public void writeChartsToStream(List<JFreeChart> charts, OutputStream out) throws IOException 
    {
        com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize, margins.left, margins.right, margins.top, margins.bottom);
        try 
        {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setCloseStream(false);
            document.open();
            document.setMarginMirroring(true);

            for(JFreeChart chart: charts)
            {
                drawChartToNewPage(chart,document, writer);
                document.newPage();
            }
        } 
        catch (DocumentException e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            document.close();
        }
    }

    public MultiplePDFSavingJob getSavingJob(List<JFreeChart> charts, OutputStream out)
    {
        return new MultiplePDFSavingJob(charts, out);
    }

    public MultiplePDFSavingJob getSavingJob(List<JFreeChart> charts, File path) throws FileNotFoundException
    {
        return new MultiplePDFSavingJob(charts, path);
    }

    private class MultiplePDFSavingJob
    {
        private final OutputStream stream;
        private final Deque<JFreeChart> charts;
        private boolean opened = false;
        private PdfWriter writer;
        private com.lowagie.text.Document document;

        public MultiplePDFSavingJob(List<JFreeChart> charts, OutputStream out)
        {
            this.charts = new ArrayDeque<>(charts);
            this.stream = out;
        }

        public MultiplePDFSavingJob(List<JFreeChart> charts, File path) throws FileNotFoundException
        {
            this.charts = new ArrayDeque<>(charts);
            this.stream = new BufferedOutputStream(new FileOutputStream(path));;
        }

        public void start(boolean closeStream)
        {
            document = new com.lowagie.text.Document(pageSize, margins.left, margins.right, margins.top, margins.bottom);
            try 
            {
                writer = PdfWriter.getInstance(document, stream);			
                writer.setCloseStream(closeStream);
                document.open();
                document.setMarginMirroring(true);
                opened = true;
            } 
            catch (DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void saveNext()
        {
            if(!opened)
            {
                throw new IllegalStateException("Job is not opened");
            }
            if(hasNext())
            {
                JFreeChart chart = charts.pollFirst();
                drawChartToNewPage(chart, document, writer);
                document.newPage();
            }
            else
            {
                throw new IllegalStateException("Job is already empty");
            }	
        }

        public boolean hasNext()
        {
            return !charts.isEmpty();
        }

        public void finish()
        {
            document.close();
            opened = false;
        }
    }

    @Override
    public void saveMovieFrame(Channel2DChart<?> chart, int frame, File path,
            ChartRenderingInfo info) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame,
            OutputStream out) throws IOException {
        // TODO Auto-generated method stub

    }
}
