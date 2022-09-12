
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
import atomicJ.analysis.PreviewDestination;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;


public class ConcurrentPreviewTask extends MonitoredSwingWorker<Void, Void> 
{
    private static final int MAX_TASK_NUMBER = Runtime.getRuntime().availableProcessors();

    private final List<? extends ChannelSource> sources;
    private final Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> curveChartMap = new LinkedHashMap<>();
    private final Map<StandardChannel2DResource, Map<String,Channel2DChart<?>>> imageChartMap = new LinkedHashMap<>();
    private final PreviewDestination parent;

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger previewedCount = new AtomicInteger();

    public ConcurrentPreviewTask(List<? extends ChannelSource> sources, PreviewDestination parent)
    {
        super(parent.getPublicationSite(), "Rendering charts in progress", "Rendered", sources.size());
        this.parent = parent;
        this.sources = sources;		
        this.problemSize = sources.size();
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {	        
        int taskNumber = Math.min(Math.max(problemSize/20, 1), MAX_TASK_NUMBER);
        int basicTaskSize = problemSize/taskNumber;
        int remainingFiles = problemSize%taskNumber;

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask> tasks = new ArrayList<>();

        for( int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i < remainingFiles)
            {
                currentTaskSize++;
            }
            List<ChannelSource> fileSublist = new ArrayList<>(sources.subList(currentIndex, currentIndex + currentTaskSize));

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
                curveChartMap.putAll(subtask.getCurveCharts());
                imageChartMap.putAll(subtask.getImageCharts());
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
        private final List<ChannelSource> sources;
        private final Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> constructedCurveCharts = new LinkedHashMap<>();
        private final Map<StandardChannel2DResource, Map<String,Channel2DChart<?>>> constructedImageCharts = new LinkedHashMap<>();       

        public Subtask(List<ChannelSource> sources)
        {
            this.sources = sources;
        }

        public Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> getCurveCharts()
        {
            return constructedCurveCharts;
        }

        public Map<StandardChannel2DResource, Map<String,Channel2DChart<?>>> getImageCharts()
        {
            return constructedImageCharts;
        }

        @Override
        public Void call() throws InterruptedException
        {             
            Thread currentThread = Thread.currentThread();

            int n = sources.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                ChannelSource source = sources.get(i);				
                try
                {
                    if(source instanceof ImageSource)
                    {
                        ImageSource imageSource = (ImageSource)source;

                        Map<String,Channel2DChart<?>> charts = ChannelSourceVisualization.getCharts(imageSource);        

                        StandardChannel2DResource imageResource = new StandardChannel2DResource(imageSource, imageSource.getIdentifiers());
                        constructedImageCharts.put(imageResource, charts);
                    }
                    else if(source instanceof SimpleSpectroscopySource)
                    {
                        SimpleSpectroscopySource spectroscopySource = (SimpleSpectroscopySource)source;

                        Map<String,ChannelChart<?>> charts = ChannelSourceVisualization.getCharts(spectroscopySource);
                        constructedCurveCharts.put(new SpectroscopyBasicResource(spectroscopySource), charts);
                    }
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
        try 
        {            
            boolean cancelled = isCancelled();

            if(cancelled)
            {
                JOptionPane.showMessageDialog(parent.getPublicationSite(), "Previewing terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                int failures = getFailuresCount();

                if(failures > 0)
                {
                    JOptionPane.showMessageDialog(parent.getPublicationSite(), "Errors occured during rendering of " + failures + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                }

                parent.publishPreviewData(curveChartMap);
                parent.publishPreviewed2DData(imageChartMap);
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
