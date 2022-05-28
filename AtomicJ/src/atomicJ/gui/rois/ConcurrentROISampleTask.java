
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

package atomicJ.gui.rois;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
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
import atomicJ.data.Channel2D;
import atomicJ.data.QuantitativeSample;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.UserCommunicableException;



public class ConcurrentROISampleTask extends MonitoredSwingWorker<Map<String, Map<Object, QuantitativeSample>>, Void> 
{
    public static final String RECORDED_CURVE = "Recorded curve";
    public static final String INDENTATION = "Indentatation";
    public static final String POINTWISE_MODULUS = "Pointwise modulus";

    private final Collection<ROI> rois;
    private final List<? extends Channel2D> allChannels;
    private final String sampleKeyTail;
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger readCount = new AtomicInteger();

    public ConcurrentROISampleTask(List<? extends Channel2D> allChannels, Collection<ROI> rois, String sampleKeyTail, Window parent)
    {
        super(parent, "Rendering charts in progress", "Rendered", allChannels.size());
        this.allChannels = allChannels;		
        this.rois = rois;
        this.sampleKeyTail = sampleKeyTail;
        this.problemSize = allChannels.size();

    }

    @Override
    public Map<String, Map<Object, QuantitativeSample>> doInBackground() throws UserCommunicableException
    {	
        Map<String, Map<Object, QuantitativeSample>> allSources = new LinkedHashMap<>();

        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();

        int taskNumber = Math.min(problemSize, maxTaskNumber);
        int basicTaskSize = problemSize/taskNumber;
        int remainingProblems = problemSize%taskNumber;		

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask> tasks = new ArrayList<>();

        for( int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingProblems)
            {
                currentTaskSize++;
            }
            List<Channel2D> channelSublist = new ArrayList<>(allChannels.subList(currentIndex, currentIndex + currentTaskSize));

            Subtask task = new Subtask(channelSublist);
            tasks.add(task);
            currentIndex = currentIndex + currentTaskSize;
        }
        try 
        {
            CompletionService<Map<String, Map<Object, QuantitativeSample>>> completionService = new ExecutorCompletionService<>(executor);

            for(Subtask subtask: tasks)
            {      		
                completionService.submit(subtask);
            }
            for(int i = 0; i<tasks.size(); i++)
            {
                Map<String, Map<Object, QuantitativeSample>> resultCurrent = completionService.take().get();
                allSources.putAll(resultCurrent);
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
        setStep(readCount.incrementAndGet());		
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

    private class Subtask implements Callable<Map<String, Map<Object, QuantitativeSample>>>
    {
        private final List<Channel2D> channels;

        public Subtask(List<Channel2D> channels)
        {
            this.channels = channels;
        }

        @Override
        public Map<String, Map<Object, QuantitativeSample>> call() throws InterruptedException
        {
            Map<String, Map<Object, QuantitativeSample>> allSamples = new LinkedHashMap<>();
            Thread currentThread = Thread.currentThread();

            int n = channels.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                Channel2D channel = channels.get(i);
                String identifier = channel.getIdentifier();
                Map<Object, QuantitativeSample> samples = channel.getROISamples(rois, sampleKeyTail);
                Map<Object, QuantitativeSample> samplesForType = allSamples.get(identifier);
                if(samplesForType == null)
                {
                    samplesForType = new LinkedHashMap<>();
                    allSamples.put(identifier, samplesForType);
                }
                samplesForType.putAll(samples);

                incrementProgress();
            }	
            return allSamples;
        }
    }

}
