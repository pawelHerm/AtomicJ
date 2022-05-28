
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

package atomicJ.gui.generalProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.data.Channel;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MetaMap;

public class ConcurrentTransformationTask <R extends ChannelResource<E, ?, ?>, E extends Channel> extends MonitoredSwingWorker<MetaMap<R, String, UndoableCommand>, Void> 
{
    private static final String oomeTip ="<br><b>TIP!:<b> You may choose not to generate all the charts during processing.<br>This can be changed in the Output tab of the processing assistant<br>";

    private final List<UndoableBasicCommand<R,E,?,?>> commands;
    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final ResourceView<R, E, ?> resourceManager;
    private final AtomicInteger executedCount = new AtomicInteger();

    public ConcurrentTransformationTask(ResourceView<R, E, ?> resourceManager, List<UndoableBasicCommand<R,E,?,?>> commands)
    {
        super(resourceManager.getAssociatedWindow(), "Transformation in progress", "Transformed", commands.size());
        this.commands = commands;		
        this.problemSize = commands.size();
        this.resourceManager = resourceManager;
    }

    @Override
    public MetaMap<R, String, UndoableCommand> doInBackground() throws UserCommunicableException
    {	        
        MetaMap<R, String, UndoableCommand> allExecutedCommands = new MetaMap<>();

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
            List<UndoableBasicCommand<R,E,?,?>> fileSublist = new ArrayList<>(commands.subList(currentIndex, currentIndex + currentTaskSize));

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
                allExecutedCommands.putAll(subtask.getExecutedCommands());
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
        return allExecutedCommands;
    }	

    private void incrementProgress()
    {
        setStep(executedCount.incrementAndGet());	
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask implements Callable<Void>
    {
        private final List<UndoableBasicCommand<R,E,?, ?>> packs;
        private final MetaMap<R, String, UndoableCommand> executedCommand = new MetaMap<>();

        public Subtask(List<UndoableBasicCommand<R,E,?,?>> commands)
        {
            this.packs = commands;
        }

        private MetaMap<R, String, UndoableCommand> getExecutedCommands()
        {
            return executedCommand;
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

                UndoableBasicCommand<R,E,?,?> command = packs.get(i);
                try
                {
                    command.execute();
                    executedCommand.put(command.getResource(), command.getType(), command);
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
            MetaMap<R, String, UndoableCommand> commands = get();

            if(!commands.isEmpty())
            {		 		
                boolean cancelled = isCancelled();

                if(cancelled || commands == null)
                {
                    JOptionPane.showMessageDialog(resourceManager.getAssociatedWindow(), "Transforming terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    int failures = getFailuresCount();

                    if(failures > 0)
                    {
                        JOptionPane.showMessageDialog(resourceManager.getAssociatedWindow(), "Errors occured during transforming of " + failures + " curves", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                    }
                }

                resourceManager.pushCommands(commands);
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
