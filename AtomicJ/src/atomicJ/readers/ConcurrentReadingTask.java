
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

package atomicJ.readers;


import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.data.ChannelFilter;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ChannelSource;

public class ConcurrentReadingTask<E extends ChannelSource> extends MonitoredSwingWorker<List<E>, Void> 
{
    private static final int maxTaskNumber = Runtime.getRuntime().availableProcessors();

    private final List<File> illegalImageFiles = Collections.synchronizedList(new ArrayList<File>());
    private final List<File> illegalSpectroscopyFiles = Collections.synchronizedList(new ArrayList<File>());

    private final List<File> files;
    private final SourceReader<E> reader;
    private final SourceReadingDirectives readingDirectives;
    private final AtomicInteger failureCount = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger readCount = new AtomicInteger();

    public ConcurrentReadingTask(List<File> files, Component parent, SourceReader<E> reader)
    {
        this(files, parent, reader, PermissiveChannelFilter.getInstance());
    }

    public ConcurrentReadingTask(List<File> files, Component parent, SourceReader<E> reader, ChannelFilter channelFilter)
    {
        super(parent, "Reading files in progress", "Read", files.size());

        this.files = files;		
        this.reader = reader;
        this.problemSize = files.size();
        this.readingDirectives = new SourceReadingDirectives(channelFilter, this.problemSize);
    }

    @Override
    public List<E> doInBackground() throws UserCommunicableException
    {	                
        if(problemSize == 0)
        {
            return Collections.emptyList();
        }

        boolean canceled = this.reader.prepareSourceReader(files);
        if(canceled)
        {
            cancelAllTasks();
            return Collections.emptyList();
        }

        List<E> allSources = new ArrayList<>();

        int taskNumber = Math.min(Math.max(problemSize/20, 1), maxTaskNumber);
        int basicTaskSize = problemSize/taskNumber;
        int remainingFiles = problemSize%taskNumber;

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
            List<File> fileSublist = new ArrayList<>(files.subList(currentIndex, currentIndex + currentTaskSize));

            Subtask task = new Subtask(fileSublist);
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

            for(Subtask subtask: tasks)
            {
                allSources.addAll(subtask.getConstructedSources());
            }
        } 
        catch (InterruptedException | ExecutionException e) 
        {
            e.printStackTrace();
        }   
        finally
        {
            executor.shutdown();
            setStep(readCount.intValue());
        }        

        return allSources;
    }	

    private void incrementProgress()
    {    
        int currentReadCount = readCount.incrementAndGet();
        setStep(currentReadCount);	
    }

    public int getFailuresCount()
    {
        return failureCount.intValue();
    }

    public List<File> getIllegalImageFiles()
    {
        return illegalImageFiles;
    }

    public List<File> getIllegalSpectroscopyFiles()
    {
        return illegalSpectroscopyFiles;
    }

    private class Subtask implements Callable<Void>
    {
        private final List<File> files;
        private final List<E> constructedSources = new ArrayList<>();

        public Subtask(List<File> files)
        {
            this.files = files;
        }

        private List<E> getConstructedSources()
        {
            return constructedSources;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            int n = files.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {                    
                    throw new InterruptedException();
                }

                File file = files.get(i);
                try
                {
                    List<E> sources = reader.readSources(file, readingDirectives);
                    constructedSources.addAll(sources);
                }
                catch(IllegalImageException e)
                {
                    illegalImageFiles.add(file);
                    e.printStackTrace();
                }
                catch(IllegalSpectroscopySourceException e)
                {
                    illegalSpectroscopyFiles.add(file);
                    e.printStackTrace();
                }
                catch(UserCommunicableException e)
                {
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                }
                catch(OutOfMemoryError e)
                {
                    setRunOutOfMemory();
                    e.printStackTrace();
                    throw e;
                }

                incrementProgress();
            }	
            return null;
        }
    }

    @Override
    public void cancelAllTasks() 
    {        
        readingDirectives.setCanceled(true);

        if(executor != null)
        {
            executor.shutdownNow();
        }
        cancel(true);
    }
}
