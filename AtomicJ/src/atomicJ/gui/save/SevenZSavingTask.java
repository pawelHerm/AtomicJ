
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;


import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import atomicJ.analysis.*;
import atomicJ.gui.AtomicJ;

public class SevenZSavingTask extends MonitoredSwingWorker<Void, Void> 
{
    private final List<StreamSavable> savables;

    private final File path;
    private final Component parent;

    private int savedCount;

    private Exception exception;

    private SevenZSavingTask(Component parent, List<StreamSavable> savables, File path)
    {
        super(parent, "Saving to archive in progress", "Saved", savables.size());
        this.savables = savables;
        this.path = path;
        this.parent = parent;
    }

    public static SevenZSavingTask getSavingTask(List<StreamSavable> savables, File path, Component parent)
    {       
        if(path.isDirectory())
        {
            throw new IllegalArgumentException("The 'path' cannot be a directory");
        }

        SevenZSavingTask task = new SevenZSavingTask(parent, savables, path);
        return task;
    }

    @Override
    public Void doInBackground() 
    {
        int n = savables.size();

        SevenZOutputFile szof = null;
        try
        {		            
            szof = new SevenZOutputFile(path);

            for(int i = 0;i<n;i++)
            {
                StreamSavable savable = savables.get(i);

                if(!isCancelled())
                {
                    String name = savable.getName();

                    try(ByteArrayOutputStream entryByteStream = new ByteArrayOutputStream())
                    {
                        savable.save(entryByteStream);

                        byte[] chartBytes = entryByteStream.toByteArray();

                        SevenZArchiveEntry entry = szof.createArchiveEntry(path, name);
                        entry.setSize(chartBytes.length);
                        szof.putArchiveEntry(entry);
                        szof.write(chartBytes);
                        szof.closeArchiveEntry();                        
                    }

                    this.savedCount = i + 1;                    
                    setStep(i + 1);
                }
                else
                {
                    break;
                }
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            exception = e;
        } 
        finally
        {
            if(szof != null)
            {
                try {
                    szof.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
                    savedCount + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }	
        else if(exception != null)
        {
            closeProgressMonitor();
            exception.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error occured during saving files in the archive\n" 
                    + exception.getMessage() + "\n Saving terminated. Saved " + savedCount + " charts",
                    AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(false);
    }
}
