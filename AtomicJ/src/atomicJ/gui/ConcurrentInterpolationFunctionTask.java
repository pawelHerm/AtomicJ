
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.data.Channel2D;


public class ConcurrentInterpolationFunctionTask extends MonitoredSwingWorker<Void, Void> 
{
    private final List<Channel2D> packs;
    private final InterpolationMethod2D interpolation;
    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger visualizedCount = new AtomicInteger();

    public ConcurrentInterpolationFunctionTask(InterpolationMethod2D interpolation, List<Channel2D> packs, Window parent)
    {
        super(parent, "Image interpolation in progress", "Interpolated", packs.size());
        this.interpolation = interpolation;
        this.packs = packs;		
        this.problemSize = packs.size();
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
            List<Channel2D> fileSublist = new ArrayList<>(packs.subList(currentIndex, currentIndex + currentTaskSize));

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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } 

        finally
        {
            executor.shutdown();
            setStep(problemSize);		 
        }        
        return null;
    }	

    private void incrementProgress()
    {
        setStep(visualizedCount.incrementAndGet());
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask implements Callable<Void>
    {
        private final List<Channel2D> packs;

        public Subtask(List<Channel2D> packs)
        {
            this.packs = packs;
        }

        @Override
        public Void call() throws InterruptedException
        {

            Thread currentThread = Thread.currentThread();

            int n = packs.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                Channel2D pack = packs.get(i);
                try
                {
                    pack.prepareForInterpolationIfNecessary(interpolation);
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
}
