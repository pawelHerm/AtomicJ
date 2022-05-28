
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

package atomicJ.gui.undo;

import java.util.ArrayList;
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
import atomicJ.gui.AtomicJ;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.UserCommunicableException;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MultiMap;

public class ConcurrentUndoTask extends MonitoredSwingWorker<Void, Void> 
{
    private static final String oomeTip ="<br><b>TIP!:<b><br>";

    private final List<Map.Entry<ChannelResource<?, ?, ?>, List<String>>> resourceTypeMap;
    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final ResourceView<?, ?,?> resourceManager;
    private final AtomicInteger executedCount = new AtomicInteger();

    public ConcurrentUndoTask(ResourceView<?, ?, ?> resourceManager, List<? extends ChannelResource<?, ?, ?>> resources, String type)
    {
        super(resourceManager.getAssociatedWindow(), "Undo in progress", "Undone", resources.size());
        this.resourceTypeMap = buildPairs(resources, type);		
        this.problemSize = resources.size();
        this.resourceManager = resourceManager;
    }

    public ConcurrentUndoTask(ResourceView<?, ?, ?> resourceManager, MultiMap<ChannelResource<?, ?, ?>, String> map)
    {
        super(resourceManager.getAssociatedWindow(), "Undo in progress", "Undone", map.getTotalSize());
        this.resourceTypeMap = new ArrayList<>(map.entrySet());       
        this.problemSize = resourceTypeMap.size();
        this.resourceManager = resourceManager;
    }

    private static List<Map.Entry<ChannelResource<?, ?, ?>, List<String>>> buildPairs(List<? extends ChannelResource<?, ?, ?>> resources, String type)
    {
        MultiMap<ChannelResource<?, ?, ?>, String> map = new MultiMap<>();

        for(ChannelResource<?, ?, ?> resource : resources)
        {
            map.put(resource, type);
        }

        return new ArrayList<>(map.entrySet());
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
            List<Map.Entry<ChannelResource<?, ?, ?>,List<String>>> fileSublist = new ArrayList<>(resourceTypeMap.subList(currentIndex, currentIndex + currentTaskSize));

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

    private void incrementProgress()
    {
        setStep(executedCount.incrementAndGet());	
    }

    private void incrementProgress(int count)
    {
        setStep(executedCount.addAndGet(count));   
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask implements Callable<Void>
    {
        private final List<Map.Entry<ChannelResource<?, ?, ?>, List<String>>> packs;

        public Subtask(List<Map.Entry<ChannelResource<?, ?, ?>, List<String>>> packs)
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

                Map.Entry<ChannelResource<?, ?, ?>,List<String>> entry = packs.get(i);

                ChannelResource<?, ?, ?> resource = entry.getKey();
                List<String> types = entry.getValue();

                try
                {                       
                    for(String type : types)
                    {
                        resource.undo(type);
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

                incrementProgress(types.size());
            }	
            return null;
        }
    }

    @Override
    public void done()
    {
        boolean cancelled = isCancelled();

        if(cancelled)
        {
            JOptionPane.showMessageDialog(resourceManager.getAssociatedWindow(), "Undoing terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            int failures = getFailuresCount();

            if(failures > 0)
            {
                JOptionPane.showMessageDialog(resourceManager.getAssociatedWindow(), "Errors occured during undoing of " + failures + " operations", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } 	

        resourceManager.refreshUndoRedoOperations();
    }

    @Override
    protected String getOOMETip()
    {
        return oomeTip;
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
