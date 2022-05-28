
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

package atomicJ.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import atomicJ.gui.GeneralPreferences;
import atomicJ.utilities.Validation;

public class ConcurrentProcessingTask<E, Y> extends MonitoredSwingWorker<Void, Void> 
{	
    private final List<E> packs;

    private final Processor<E, Y> processor;

    private final AtomicInteger failures = new AtomicInteger();
    private final AtomicInteger processedCount = new AtomicInteger();

    private final ProcessingResultsHandler<Y> processingResultsHandler;

    private final int problemSize;  

    private ExecutorService executor;

    public ConcurrentProcessingTask(List<E> packs, Processor<E, Y> processor, ProcessingResultsHandler<Y> processingResultsHandler)
    {
        super(processingResultsHandler.getAssociatedComponent(), "Computation in progress", "Computed", packs.size());

        this.processingResultsHandler = Validation.requireNonNullParameterName(processingResultsHandler, "processingResultsHandler");
        this.packs = Validation.requireNonNullParameterName(packs, "packs");
        this.processor = Validation.requireNonNullParameterName(processor, "processor");
        this.problemSize = packs.size();
    }

    @Override
    public Void doInBackground() 
    {	                    
        int maxTaskNumber = GeneralPreferences.GENERAL_PREFERENCES.getTaskNumber();
        int taskNumber = Math.min(Math.max(problemSize/5, 1), maxTaskNumber);

        int basicTaskSize = problemSize/taskNumber;
        int remainingPacks = problemSize%taskNumber;

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask> tasks = new ArrayList<>();

        for(int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingPacks)
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

            List<Y> allProcessingResult = new ArrayList<>();

            for(Subtask subtask: tasks)
            {
                allProcessingResult.addAll(subtask.getProcessingResults());
            }

            processingResultsHandler.acceptAndSegregateResults(allProcessingResult);
        } 
        catch (Exception e) 
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
        try
        {                      
            if(!isCancelled())
            {                     
                processingResultsHandler.reactToFailures(failures.intValue());
                processingResultsHandler.sendResultsToDestination();
            }
            else
            {   
                processingResultsHandler.reactToCancellation();
            }
        }
        catch(Exception e)
        {
            Logger.getLogger("").log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        finally
        {
            processingResultsHandler.endProcessing();
        }
    }

    private void incrementProgress()
    {
        setStep(processedCount.incrementAndGet());
    }

    private class Subtask implements Callable<Void>
    {		
        private final List<E> processablePacks;
        private final List<Y> processedPacks = new ArrayList<>();

        public Subtask(List<E> processablePacks)
        {
            this.processablePacks = processablePacks;
        }

        private  List<Y> getProcessingResults()
        {
            return processedPacks;
        }

        @Override
        public Void call() throws InterruptedException
        { 
            Thread currentThread = Thread.currentThread();

            for(E pack: processablePacks)
            {	
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                try
                {
                    Y result = processor.process(pack);					
                    processedPacks.add(result);	                                                
                }			
                catch(OutOfMemoryError e)
                {
                    setRunOutOfMemory();
                    e.printStackTrace();
                    throw e;
                }
                catch(Exception e)
                {
                    Logger.getLogger("").log(Level.SEVERE, e.getMessage(), e);
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
