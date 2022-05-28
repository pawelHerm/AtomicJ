
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

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;


import org.jfree.chart.JFreeChart;

import atomicJ.analysis.*;
import atomicJ.gui.AtomicJ;

public class ZipSavingTask extends MonitoredSwingWorker<Void, Void> 
{
    private final File path;
    private final ChartSaver saver;
    private final List<? extends JFreeChart> charts;
    private final List<String> names;
    private final Component parent;

    private int savedChartCount;

    private Exception exception;

    private ZipSavingTask(Component parent, List<? extends JFreeChart> charts, List<String> names, ChartSaver saver, File path)
    {
        super(parent, "Saving to archive in progress", "Saved", charts.size());
        this.charts = charts;
        this.names = names;
        this.saver = saver;
        this.path = path;
        this.parent = parent;
    }

    public static ZipSavingTask getSavingTask(List<? extends JFreeChart> charts, List<String> names, ChartSaver saver, File path, Component parent)
    {
        if(charts.size() != names.size())
        {
            throw new IllegalArgumentException("The 'charts' and 'names' lists must have the same length");
        }
        if(path.isDirectory())
        {
            throw new IllegalArgumentException("The 'path' cannot be a directory");
        }

        ZipSavingTask task = new ZipSavingTask(parent, charts, names, saver, path);
        return task;
    }

    @Override
    public Void doInBackground() 
    {        
        int n = charts.size();

        try(FileOutputStream fos = new FileOutputStream(path);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ZipOutputStream zos = new ZipOutputStream(bos);)
        {		
            for(int i = 0;i<n;i++)
            {
                if(!isCancelled())
                {
                    String name = names.get(i);
                    ZipEntry entry = new ZipEntry(name);
                    zos.putNextEntry(entry);

                    JFreeChart chart = charts.get(i);
                    saver.writeChartToStream(chart, zos);

                    zos.closeEntry();

                    this.savedChartCount = i + 1;

                    setStep(i + 1);
                }
                else
                {
                    break;
                }
            }
        } 
        catch (IOException e) 
        {
            exception = e;
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
                    savedChartCount + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }	
        else if(exception != null)
        {
            closeProgressMonitor();
            exception.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error occured during saving files in the archive\n" 
                    + exception.getMessage() + "\n Saving terminated. Saved " + savedChartCount + " charts",
                    AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(false);
    }
}
