
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

package atomicJ.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.gui.save.StreamSavable;


public class ConcurrentZipSavingTask extends MonitoredSwingWorker<Void, Void> 
{
    private static final int MAX_TASK_NUMBER = Runtime.getRuntime().availableProcessors();

    private final List<StreamSavable> saveables;
    private final Component parent;

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final File file;

    private final AtomicInteger zippedCount = new AtomicInteger();

    public ConcurrentZipSavingTask(Component parent, List<StreamSavable> savables, File file)
    {
        super(parent, "Saving to archive in progress", "Saved", savables.size());
        this.saveables = savables;
        this.parent = parent;
        this.problemSize = saveables.size();
        this.file = file;
    }

    public static ConcurrentZipSavingTask getSavingTask(List<StreamSavable> savables, File file, Component parent) 
    {
        if(file.isDirectory())
        {
            throw new IllegalArgumentException("The 'file' cannot be a directory");
        }

        ConcurrentZipSavingTask task = new ConcurrentZipSavingTask(parent, savables, file);
        return task;
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {	
        int taskNumber = Math.min(Math.max(problemSize/20, 1), MAX_TASK_NUMBER);
        int basicTaskSize = problemSize/taskNumber;
        int remainingFiles = problemSize%taskNumber;

        Map<String, String> env = new HashMap<>(); 
        env.put("create", "true");

        URI uri = URI.create("jar:" + file.toURI());
        try(FileSystem fs = FileSystems.newFileSystem(uri, env)) 
        {         
            this.executor = Executors.newFixedThreadPool(taskNumber); 

            int currentIndex = 0;

            List<Subtask> tasks = new ArrayList<>();

            for( int i = 0; i <taskNumber; i++ ) 
            {
                int currentTaskSize = basicTaskSize;
                if(i<remainingFiles)
                {
                    currentTaskSize++;
                }
                List<StreamSavable> fileSublist = new ArrayList<>(saveables.subList(currentIndex, currentIndex + currentTaskSize));

                Subtask task = new Subtask(fileSublist, fs);
                tasks.add(task);
                currentIndex = currentIndex + currentTaskSize;
            }
            try 
            {
                CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

                for(Subtask subtask: tasks)
                {
                    completionService.submit(subtask);
                }
                for(int i = 0; i<tasks.size(); i++)
                {
                    completionService.take().get();
                }
            } 
            catch (InterruptedException | ExecutionException e) 
            {
                e.printStackTrace();
            }   
            finally
            {
                executor.shutdown();
                setStep(zippedCount.intValue());
            }        
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return null;
    }	

    private void incrementProgress()
    {
        setStep(zippedCount.incrementAndGet());	
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask implements Callable<Void>
    {
        private final List<StreamSavable> saveableSublist;
        private final FileSystem fs;

        public Subtask(List<StreamSavable> sources, FileSystem fs)
        {
            this.saveableSublist = sources;
            this.fs = fs;
        }

        @Override
        public Void call() throws InterruptedException
        {             
            Thread currentThread = Thread.currentThread();

            int n = saveableSublist.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                StreamSavable saveable = saveableSublist.get(i);				
                try
                {
                    OutputStream outData = Files.newOutputStream(fs.getPath(saveable.getName()));
                    saveable.save(outData);
                    outData.close();
                }
                catch(OutOfMemoryError e)
                {
                    setRunOutOfMemory();
                    e.printStackTrace();
                    throw e;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    failures.incrementAndGet();
                }

                incrementProgress();
            }	
            return null;
        }
    }

    @Override
    public void done()
    {
        super.done();

        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent, "Saving terminated. Saved " + 
                    zippedCount.get() + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            try
            {
                get(); // if we don't call get(), the stack traces of the exceptions thrown in the background thread may not be printed
            }
            catch(Exception e)
            {
                closeProgressMonitor();
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error occured during saving files in the archive\n" 
                        + e.getMessage() + "\n Saving terminated. Saved " + zippedCount.get() + " charts",
                        AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
                setProgress(100);
            }
        }     
    }

    @Override
    public void cancelAllTasks() 
    {
        if(executor != null)
        {
            executor.shutdownNow();
        }
        cancel(true);
    }
}
