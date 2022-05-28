
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 - 2020 by Pawe³ Hermanowicz
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

package atomicJ.gui.curveProcessing;

import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.analysis.Visualizable;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.UserCommunicableException;


public class ConcurrentCurveVisualizationTask<E extends Visualizable> extends MonitoredSwingWorker<Map<E,Map<String, ChannelChart<?>>>, Void> 
{
    private static final String OOME_TIP ="<br><b>TIP!:<b> You may choose not to generate all the charts during processing.<br>This can be changed in the Output tab of the processing assistant<br>";

    private final List<E> packs;
    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger visualizedCount = new AtomicInteger();

    private final ChartDrawingHandler<E> drawingHandle;

    public ConcurrentCurveVisualizationTask(List<E> packs, Window parent, ChartDrawingHandler<E> visualizationHandle)
    {
        super(parent, "Rendering charts in progress", "Rendered", packs.size());
        this.packs = packs;     
        this.problemSize = packs.size();
        this.drawingHandle = visualizationHandle;
    }

    @Override
    public Map<E, Map<String, ChannelChart<?>>> doInBackground() throws UserCommunicableException
    {                   
        Map<E,Map<String, ChannelChart<?>>> allSources = new LinkedHashMap<>();

        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();
        int taskNumber = Math.min(Math.max(problemSize/20, 1), maxTaskNumber);
        int basicTaskSize = problemSize/taskNumber;
        int remainingFiles = problemSize%taskNumber;

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask> tasks = new ArrayList<>();

        for(int i = 0; i <taskNumber; i++) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingFiles)
            {
                currentTaskSize++;
            }
            List<E> fileSublist = new ArrayList<>(packs.subList(currentIndex, currentIndex + currentTaskSize));

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
                allSources.putAll(subtask.getConstructedCharts());
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

        return allSources;
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
        private final List<E> packs;
        private final Map<E, Map<String, ChannelChart<?>>> constructedCharts = new LinkedHashMap<>();

        public Subtask(List<E> packs)
        {
            this.packs = packs;
        }

        private Map<E, Map<String, ChannelChart<?>>> getConstructedCharts()
        {
            return constructedCharts;
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

                E pack = packs.get(i);
                try
                {
                    Map<String, ChannelChart<?>> charts = pack.visualize();
                    constructedCharts.put(pack, charts);
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
        try 
        {
            Map<E, Map<String, ChannelChart<?>>> charts = get();

            if(!charts.isEmpty())
            {               
                if(isCancelled())
                {
                    drawingHandle.reactToCancellation();
                }
                else
                {
                    int failureCount = getFailuresCount();
                    drawingHandle.reactToFailures(failureCount);
                    drawingHandle.sendChartsToDestination(charts);
                    drawingHandle.handleFinishDrawingRequest();
                }
            }
        }
        catch (InterruptedException | ExecutionException e) 
        {
            e.printStackTrace();
        }   
    }

    @Override
    protected String getOOMETip()
    {
        return OOME_TIP;
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
