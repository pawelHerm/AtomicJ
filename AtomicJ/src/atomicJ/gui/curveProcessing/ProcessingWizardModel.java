
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

package atomicJ.gui.curveProcessing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.WizardPage;


public class ProcessingWizardModel extends AbstractModel implements PropertyChangeListener
{
    private WizardPage currentPage;

    private final List<WizardPage> pages;
    private ProcessingModel processingModel;

    private int currentPageIndex;
    private int currentBatchIndex;
    private int currentStep;

    private boolean finishEnabled;
    private boolean backEnabled;
    private boolean nextEnabled;
    private boolean nextBatchEnabled;
    private boolean previousBatchEnabled;

    public static final String INPUT_PROVIDED = "InputProvided";

    static final String NEXT_BATCH_ENABLED = "NextBatchEnabled";
    static final String PREVIOUS_BATCH_ENABLED = "PreviousBatchEnabled";
    static final String CURRENT_PAGE = "CurrentPage";
    static final String FINISH_ENABLED = "FinishEnabled";
    static final String NEXT_ENABLED = "NextEnabled";
    static final String BACK_ENABLED = "BackEnabled";

    public ProcessingWizardModel(List<WizardPage> pages, ProcessingModel processingModel)
    {
        this.pages = new ArrayList<>(pages);		
        for(WizardPage page: pages)
        {
            page.addPropertyChangeListener(this);
        }

        setProcessingModel(processingModel);
    }

    public void setProcessingModel(ProcessingModel processingModel)
    {
        this.processingModel = processingModel;

        this.nextEnabled = processingModel.areSourcesSelected();				
        this.nextBatchEnabled = processingModel.canProcessingBeFinished();
        this.finishEnabled = processingModel.canProcessingBeFinished();

        initDefaults();
    }

    private void initDefaults()
    {
        this.currentPageIndex = 0;
        this.currentBatchIndex = 0;
        this.currentStep = 0;		
        this.backEnabled = false;
        this.previousBatchEnabled = false;		
        this.currentPage = pages.get(currentPageIndex);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();
        if(INPUT_PROVIDED.equals(property))
        {
            Object source = evt.getSource();
            if(source == currentPage)
            {
                boolean inputProvided = (boolean)evt.getNewValue();

                boolean nextEnabledNew = (inputProvided && !(currentPage.isLast() && processingModel.isCurrentBatchLast()));
                boolean finishEnabledNew = (processingModel.canProcessingBeFinished());
                boolean nextBatchEnabledNew = processingModel.canProcessingOfCurrentBatchBeFinished() && !processingModel.isCurrentBatchLast();

                setNextEnabled(nextEnabledNew&&!currentPage.isLast());
                setNextBatchEnabled(nextBatchEnabledNew);
                setFinishEnabled(finishEnabledNew);		
            }
        }
    }

    public boolean isBackEnabled()
    {
        return backEnabled;
    }

    public void setBackEnabled(boolean enabledNew)
    {
        boolean enabledOld = backEnabled;
        backEnabled = enabledNew;

        firePropertyChange(ProcessingWizardModel.BACK_ENABLED, enabledOld, enabledNew);
    }

    public boolean isPreviousBatchEnabled()
    {
        return previousBatchEnabled;
    }

    public void setPreviousBatchEnabled(boolean enabledNew)
    {
        boolean enabledOld = previousBatchEnabled;
        previousBatchEnabled = enabledNew;

        firePropertyChange(PREVIOUS_BATCH_ENABLED, enabledOld, enabledNew);	}


    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    public void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        nextEnabled = enabledNew;

        firePropertyChange(NEXT_ENABLED, enabledOld, enabledNew);
    }

    public boolean isNextBatchEnabled()
    {
        return nextBatchEnabled;
    }

    public void setNextBatchEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextBatchEnabled;
        nextBatchEnabled = enabledNew;

        firePropertyChange(NEXT_BATCH_ENABLED, enabledOld, enabledNew);
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    public void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        finishEnabled = enabledNew;

        firePropertyChange(FINISH_ENABLED, enabledOld, enabledNew);
    }

    public WizardPage getCurrentPage()
    {
        return currentPage;
    }

    public WizardPage next()
    {
        currentStep++;
        int size = pages.size();
        int currentPageIndexNew = currentStep % size;
        int currentBatchNew = currentStep/size;

        if(currentBatchNew != currentBatchIndex)
        {
            currentBatchIndex = currentBatchNew;
            processingModel.nextBatch();
        }		
        return setCurrentPage(currentPageIndexNew);
    }

    public WizardPage back()
    {
        currentStep = Math.max(currentStep - 1,0);
        int size = pages.size();
        int currentPageIndexNew = currentStep % size;
        int currentBatchNew = currentStep/size;

        if(currentBatchNew != currentBatchIndex)
        {
            currentBatchIndex = currentBatchNew;
            processingModel.previousBatch();
        }		
        return setCurrentPage(currentPageIndexNew);
    }

    private WizardPage setCurrentPage(int newIndex)
    {
        int size = pages.size();
        boolean withinRange = (newIndex>=0)&&(newIndex<size);

        if(withinRange && newIndex != currentPageIndex)
        {
            int oldIndex = currentPageIndex;
            WizardPage newPage = pages.get(newIndex);
            WizardPage oldPage = pages.get(oldIndex);

            currentPage = newPage;
            currentPageIndex = newIndex;

            boolean nextEnabledNew = newPage.isNecessaryInputProvided();
            boolean finishEnabledNew = processingModel.canProcessingBeFinished();
            boolean nextBatchEnabledNew = processingModel.canProcessingOfCurrentBatchBeFinished() && !processingModel.isCurrentBatchLast();
            boolean backEnabledNew = (!newPage.isFirst())||(currentBatchIndex > 0);

            setFinishEnabled(finishEnabledNew);
            setNextEnabled(nextEnabledNew&&!newPage.isLast());
            setBackEnabled(backEnabledNew&&!newPage.isFirst()); 
            setNextBatchEnabled(nextBatchEnabledNew);
            setPreviousBatchEnabled(backEnabledNew);

            firePropertyChange(CURRENT_PAGE, oldPage, newPage);
        }
        return pages.get(currentPageIndex);
    }
}
