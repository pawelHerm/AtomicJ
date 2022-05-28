
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.data.Channel2D;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.profile.CrossSectionsReceiver;
import atomicJ.gui.profile.ProfileLine;
import atomicJ.resources.CrossSectionResource;
import atomicJ.sources.Channel2DSource;


public class ConcurrentCrossSectionTask extends MonitoredSwingWorker<Void, Void> 
{
    private static final int maxTaskNumber = Runtime.getRuntime().availableProcessors();

    private final List<Channel2DSource<?>> sources;
    private final List<Channel2D> allChannels = new ArrayList<>();
    private final InterpolationMethod2D interpolation;
    private final CrossSectionsReceiver receiver;
    private final Window parent;
    private final List<String> types;
    private final ProfileLine profile;
    private final CrossSectionResource resource;

    private ExecutorService executor;
    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;

    private final AtomicInteger interpolatedCount = new AtomicInteger();

    public ConcurrentCrossSectionTask(InterpolationMethod2D interpolation, Collection<Channel2DSource<?>> densitySources, Collection<String> types, ProfileLine profile, CrossSectionResource resource, CrossSectionsReceiver receiver, Window parent)
    {
        super(parent, "Extracting crosssections in progress", "extracted", types.size());
        this.interpolation = interpolation;
        this.sources = new ArrayList<>(densitySources);
        this.types = new ArrayList<>(types);
        this.receiver = receiver;
        this.parent = parent;
        this.profile = profile;
        this.resource = resource;	

        this.problemSize = types.size();

        for(Channel2DSource<?> source : densitySources)
        {
            allChannels.addAll(source.getChannels());
        }
    }

    @Override
    public Void doInBackground() 
    {

        int taskNumber = Math.min(problemSize, maxTaskNumber);
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
            List<Channel2D> fileSublist = new ArrayList<>(allChannels.subList(currentIndex, currentIndex + currentTaskSize));

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

    private class Subtask implements Callable<Void>
    {
        private final List<Channel2D> subTypes;

        public Subtask(List<Channel2D> packs)
        {
            this.subTypes = packs;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            int n = subTypes.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                try
                {
                    Channel2D channel = subTypes.get(i);
                    channel.prepareForInterpolationIfNecessary(interpolation);
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

    private void incrementProgress()
    {		
        setStep(interpolatedCount.incrementAndGet());	
    }

    @Override
    protected void done()
    {
        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent, "Extracting crosssections terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {		
            Map<String, ChannelSectionLine> allCrossSections = new LinkedHashMap<>();

            for(String type : types)
            {
                for(Channel2DSource<?> imageSource: sources)
                {			
                    Map<String, ChannelSectionLine> sections = imageSource.getCrossSections(profile.getLine(), profile.getKey(), profile.getLabel(), type);
                    allCrossSections.putAll(sections);
                }
            }

            resource.addOrReplaceCrossSections(allCrossSections);
            receiver.addOrReplaceCrossSections(resource, allCrossSections);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(true);
    }
}
