
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

package atomicJ.gui.save;


import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JOptionPane;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.GeneralPreferences;
import atomicJ.gui.UserCommunicableException;


public class ConcurrentSavingTask extends  MonitoredSwingWorker<Void, Void> 
{
    private final List<Saveable> allPacks;
    private final Component parent;		
    private final int problemSize;
    private final AtomicInteger failures = new AtomicInteger();
    private final AtomicInteger savedCount = new AtomicInteger();

    private ExecutorService executor;

    public ConcurrentSavingTask(List<Saveable> allPacks, Component parent)
    {
        super(parent, "Saving in progress", "Saved", allPacks.size());

        this.allPacks = allPacks;		
        this.parent = parent;	
        this.problemSize = allPacks.size();
    }

    private void incrementProgress()
    {
        setStep(savedCount.incrementAndGet());
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {               
        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();

        int taskNumber = Math.min(Math.max(problemSize/5, 1), maxTaskNumber);
        int basicTaskSize = problemSize/taskNumber;
        int remainingPacks = problemSize%taskNumber;

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask> tasks = new ArrayList<>();

        for( int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingPacks)
            {
                currentTaskSize++;
            }
            List<Saveable> packSublist = new ArrayList<>(allPacks.subList(currentIndex, currentIndex + currentTaskSize));

            Subtask task = new Subtask(packSublist);
            tasks.add(task);
            currentIndex = currentIndex + currentTaskSize;
        }

        try 
        {
            executor.invokeAll(tasks);
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
        finally
        {

            executor.shutdown();
        }

        return null;	
    }	

    @Override
    protected void done()
    {
        super.done();

        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent, "Saving terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            if(failures.intValue() > 0)
            {
                JOptionPane.showMessageDialog(parent, "Errors occured during saving of " + failures + " files", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
            }
        }	
    }


    private class Subtask implements Callable<Void>
    {
        private final List<Saveable> packs;

        public Subtask(List<Saveable> packs)
        {
            this.packs = packs;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            final int n = packs.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                Saveable pack = packs.get(i);
                try 
                {
                    pack.save();
                } 
                catch (OutOfMemoryError e)
                {
                    setRunOutOfMemory();
                    e.printStackTrace();
                    throw e;
                }
                catch (IOException e) 
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
