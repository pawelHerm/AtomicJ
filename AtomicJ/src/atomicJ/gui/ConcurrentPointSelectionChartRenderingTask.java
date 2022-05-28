
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

import javax.swing.JOptionPane;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.analysis.ProcessableSpectroscopyPack;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.data.Channel1D;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.sources.SimpleSpectroscopySource;


public class ConcurrentPointSelectionChartRenderingTask extends MonitoredSwingWorker<Void, Void> 
{
    private static final int maxTaskNumber = Runtime.getRuntime().availableProcessors();

    private final List<ProcessableSpectroscopyPack> packs;
    private final Map<ProcessableSpectroscopyPack, PointSelectionChart> charts = new LinkedHashMap<>();

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final Component parent;

    private final AtomicInteger previewedCount = new AtomicInteger();

    public ConcurrentPointSelectionChartRenderingTask(List<ProcessableSpectroscopyPack> packs, Component parent)
    {
        super(parent, "Rendering charts in progress", "Rendered", packs.size());
        this.packs = packs;		
        this.problemSize = packs.size();
        this.parent = parent;
    }

    public Map<ProcessableSpectroscopyPack, PointSelectionChart> getCharts()
    {
        return charts;
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {	
        int taskNumber = Math.min(Math.max(problemSize/20, 1), maxTaskNumber);
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
            List<ProcessableSpectroscopyPack> fileSublist = new ArrayList<>(packs.subList(currentIndex, currentIndex + currentTaskSize));

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
                charts.putAll(subtask.getCharts());
            }
        } 
        catch (InterruptedException | ExecutionException e) 
        {
            e.printStackTrace();
        }	
        finally
        {
            executor.shutdown();
            setStep(previewedCount.intValue());
        }        
        return null;
    }	

    private void incrementProgress()
    {
        setStep(previewedCount.incrementAndGet());	
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask implements Callable<Void>
    {
        private final List<ProcessableSpectroscopyPack> packs;
        private final  Map<ProcessableSpectroscopyPack, PointSelectionChart> constructedCharts = new LinkedHashMap<>();

        public Subtask(List<ProcessableSpectroscopyPack> packs)
        {
            this.packs = packs;
        }

        public Map<ProcessableSpectroscopyPack, PointSelectionChart> getCharts()
        {
            return constructedCharts;
        }

        @Override
        public Void call() throws InterruptedException
        {             
            Thread currentThread = Thread.currentThread();

            for(ProcessableSpectroscopyPack pack : packs)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                try
                {
                    PointSelectionChart chart = buildSelectionChart(pack);
                    constructedCharts.put(pack, chart);
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

    private PointSelectionChart buildSelectionChart(ProcessableSpectroscopyPack pack)
    {
        ProcessingSettings settings = pack.getProcessingSettings();
        SimpleSpectroscopySource spectroscopySource = pack.getSourceToProcess();
        double sensitivity = settings.getSensitivity();
        double springConstant = settings.getSpringConstant();

        SpectroscopyCurve<Channel1D> afmCurve = spectroscopySource.getRecordedForceCurve(sensitivity, 1000*springConstant); 

        Channel1DPlot plot = ForceCurvePlotFactory.getInstance().getPlot(afmCurve);
        PointSelectionChart chart = new PointSelectionChart(plot);
        return chart;
    }

    @Override
    public void done()
    {
        super.done();
        try 
        {
            boolean cancelled = isCancelled();

            if(cancelled)
            {
                JOptionPane.showMessageDialog(parent, "Rendering terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                int failures = getFailuresCount();

                if(failures > 0)
                {
                    JOptionPane.showMessageDialog(parent, "Errors occured during rendering of " + failures + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        finally
        {
            setProgress(100);
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
