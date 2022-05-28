
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

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.data.Grid2D;
import atomicJ.functions.MultivariateFunction;


public class ConcurrentAddFunctionTask extends MonitoredSwingWorker<Void, Void> 
{
    private final double[][] gridData;	

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;

    private final Grid2D grid;
    private final boolean useAlsoDependent;
    private final MultivariateFunction f;

    private ExecutorService executor;

    private final AtomicInteger generatedCount = new AtomicInteger();


    public ConcurrentAddFunctionTask(double[][] gridData, Grid2D grid, MultivariateFunction f, boolean useAlsoDependent, Window parent)
    {
        super(parent, "Generating stack frames in progress", "Generated", gridData.length);

        this.useAlsoDependent = useAlsoDependent;
        this.f = f;

        this.gridData = gridData;
        this.grid = grid;
        this.problemSize = gridData.length;
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

    public double[][] getChannels()
    {
        return gridData;
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
        private final int minRow;
        private final int maxRow;

        public Subtask(int minFrame, int maxFrame)
        {
            this.minRow = minFrame;
            this.maxRow = maxFrame;
        }

        @Override
        public Void call() throws InterruptedException
        {
            Thread currentThread = Thread.currentThread();

            for(int i = minRow; i < maxRow;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                try
                {       

                    int m = grid.getColumnCount();

                    if (useAlsoDependent) 
                    {
                        for (int j = 0; j < m; j++) {
                            double x = grid.getX(j);
                            double y = grid.getY(i);

                            double zOld = gridData[i][j];

                            double zNew = zOld + f.value(new double[] { x, y, zOld });
                            gridData[i][j] = zNew;
                        }

                    } 
                    else {

                        for (int j = 0; j < m; j++) {
                            double x = grid.getX(j);
                            double y = grid.getY(i);

                            double zOld = gridData[i][j];

                            double zNew = zOld + f.value(new double[] { x, y });
                            gridData[i][j] = zNew;

                        }

                    }
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
