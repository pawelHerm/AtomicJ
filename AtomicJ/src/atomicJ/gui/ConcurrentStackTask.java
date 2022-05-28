
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.StackingPackFunctionFactory;
import atomicJ.data.Channel2D;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.sources.MapSource;


public class ConcurrentStackTask extends MonitoredSwingWorker<Void, Void> 
{
    private static final NumberFormat CURRENT_LABEL_FORMAT = NumberFormat.getInstance(Locale.US);

    private final MapSource<?> mapSource;
    private final Channel2D[] channels;	

    private final double minimum;
    private final double maximum;
    private final double step;
    private final StackingPackFunctionFactory<?> factory;
    private final String unit;
    private final ROI roi;
    private final ROIRelativePosition position;

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger generatedCount = new AtomicInteger();

    public ConcurrentStackTask(StackingPackFunctionFactory<?> factory,  double minimum, double maximum, int frameCount, MapSource<?> mapSource, Window parent)
    {
        this(factory, null, ROIRelativePosition.EVERYTHING, minimum, maximum, frameCount, mapSource, parent);
    }

    public ConcurrentStackTask(StackingPackFunctionFactory<?> factory, ROI roi, ROIRelativePosition position, double minimum, double maximum, int frameCount, MapSource<?> mapSource, Window parent)
    {
        super(parent, "Generating stack frames in progress", "Generated", frameCount);
        this.channels = new Channel2D[frameCount];
        this.roi = roi;
        this.position = position;
        this.problemSize = frameCount;
        this.factory = factory;
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = (maximum - minimum)/(frameCount - 1.);
        this.mapSource = mapSource;
        this.unit = factory.getStackingQuantity().getFullUnitName();
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {	
        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();

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

    public List<Channel2D> getChannels()
    {
        return Arrays.asList(channels);
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

            if(roi == null)
            {
                for(int i = minFrame; i < maxFrame;i++)
                {
                    if(currentThread.isInterrupted())
                    {
                        throw new InterruptedException();
                    }

                    try
                    {		
                        double level = minimum + step*i;

                        ProcessedPackFunction f = factory.getFunction(level);
                        Channel2D channel = mapSource.getChannel(f);
                        channels[i] = channel;

                        String info = CURRENT_LABEL_FORMAT.format(level) + " " + unit;
                        channel.setChannelInfo(info);				
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        failures.incrementAndGet();
                    }

                    incrementProgress();
                }	
            }
            else
            {
                for(int i = minFrame; i < maxFrame;i++)
                {
                    if(currentThread.isInterrupted())
                    {
                        throw new InterruptedException();
                    }

                    try
                    {					
                        double level = minimum + step*i;
                        ProcessedPackFunction f = factory.getFunction(level);
                        Channel2D channel = mapSource.getChannel(roi, position, f);
                        channels[i] = channel;

                        String info = CURRENT_LABEL_FORMAT.format(level) + " " + unit;
                        channel.setChannelInfo(info);				
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        failures.incrementAndGet();
                    }

                    incrementProgress();
                }	
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
