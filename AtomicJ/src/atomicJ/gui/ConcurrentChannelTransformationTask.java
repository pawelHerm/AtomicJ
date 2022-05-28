
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

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.data.Channel2DData;
import atomicJ.imageProcessing.Channel2DDataTransformation;



public class ConcurrentChannelTransformationTask extends MonitoredSwingWorker<Void, Void> 
{
    private final List<Channel2DData> channels;
    private final Channel2DData[] transformedChannels;	

    private final Channel2DDataTransformation tr;

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger generatedCount = new AtomicInteger();


    public ConcurrentChannelTransformationTask(List<Channel2DData> channels, Channel2DDataTransformation tr, Window parent)
    {
        super(parent, "Smoothing frames in progress", "Smoothed", channels.size());

        this.problemSize = channels.size();
        this.channels = channels;
        this.transformedChannels = new Channel2DData[problemSize];
        this.tr = tr;
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {		
        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();

        int taskNumber = Math.min(Math.max(problemSize, 1), maxTaskNumber);
        int basicTaskSize = problemSize/taskNumber;
        int remainingFiles = problemSize%taskNumber;

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask> tasks = new ArrayList<>();

        for( int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingFiles)
            {
                currentTaskSize++;
            }

            Subtask task = new Subtask(currentIndex, currentIndex + currentTaskSize);
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
            updateProgressMonitor("I am almost done...");

        }        
        return null;
    }	

    public List<Channel2DData> getChannels()
    {
        return Arrays.asList(transformedChannels);
    }

    private void incrementProgress()
    {
        setStep(generatedCount.incrementAndGet());	
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask implements Callable<Void>
    {
        private final int minIndex;
        private final int maxIndex;

        public Subtask(int minIndex, int maxIndex)
        {
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            for(int i = minIndex; i < maxIndex;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                try
                {		
                    Channel2DData channelToTransform = channels.get(i);	
                    channels.set(i, null);
                    transformedChannels[i] = tr.transform(channelToTransform);										
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
    public void cancelAllTasks() 
    {
        if(executor != null)
        {
            executor.shutdownNow();
        }
        cancel(true);
    }

    @Override
    protected void done()
    {
        super.done();
    }
}
