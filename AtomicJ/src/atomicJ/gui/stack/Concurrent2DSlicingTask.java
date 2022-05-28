
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

package atomicJ.gui.stack;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.data.ArrayChannel2DUtilities;
import atomicJ.data.ArraySupport2D;
import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.Channel2DStandard;
import atomicJ.data.units.Quantity;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.Channel2DReceiver;
import atomicJ.gui.UserCommunicableException;
import atomicJ.resources.CrossSectionSettings;


public class Concurrent2DSlicingTask extends MonitoredSwingWorker<Channel2D, Void> 
{
    private Channel2D slice; //the result of all computation, which is passed to GridChannelReceiver in done() method

    private final List<Channel2D> channels;
    private final Shape profileLine;

    private final double[][] sliceData;	
    private final ArraySupport2D grid;
    private final Quantity depthQuantity;

    private final CrossSectionSettings sectionSettings;

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger generatedCount = new AtomicInteger();

    private final Channel2DReceiver receiver;

    public Concurrent2DSlicingTask(List<Channel2D> channels, Shape profileShape, CrossSectionSettings sectionSettings, ArraySupport2D grid, Quantity depthQuantity, Channel2DReceiver receiver)
    {
        super(receiver.getParent(), "Smoothing frames in progress", "Smoothed", channels.size());

        this.problemSize = channels.size();
        this.channels = channels;
        this.profileLine = profileShape;

        this.sliceData = new double[problemSize][];
        this.sectionSettings = sectionSettings;
        this.depthQuantity = depthQuantity;
        this.grid = grid;

        this.receiver = receiver;
    }

    @Override
    public Channel2D doInBackground() throws UserCommunicableException
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

        Channel2DData channelData = ArrayChannel2DUtilities.buildChannel(grid, sliceData, depthQuantity);
        this.slice = new Channel2DStandard(channelData, depthQuantity.getName()); 

        return slice;
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
        private final int minFrame;
        private final int maxFrame;

        public Subtask(int minFrame, int maxFrame)
        {
            this.minFrame = minFrame;
            this.maxFrame = maxFrame;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            for(int i = minFrame; i < maxFrame;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                try
                {		
                    Channel2D channel = channels.get(i);	
                    sliceData[i] = channel.getProfileValues(profileLine, sectionSettings);                                      
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

        receiver.setGridChannel(slice);
    }
}
