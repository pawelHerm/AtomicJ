
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

package atomicJ.gui.statistics;

import static atomicJ.gui.WizardModelProperties.*;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import atomicJ.gui.AbstractModel;


public class SimpleWizardModel extends AbstractModel implements PropertyChangeListener
{
    private boolean backEnabled;
    private boolean nextEnabled;
    private boolean skipEnabled;
    private boolean finishEnabled;

    private String task;
    private RichWizardPage currentPageModel;
    private final List<RichWizardPage> allPageModels = new ArrayList<>();

    public SimpleWizardModel(RichWizardPage currentPageModel, List<RichWizardPage> allPageModels)
    {
        setCurrentPageModel(currentPageModel);
        this.allPageModels.addAll(allPageModels);
    }

    public void setWizard(SimpleWizard wizard)
    {
        for(RichWizardPage page: allPageModels)
        {
            page.setWizard(wizard);
        }
    }

    public String getTask()
    {
        return task;
    }

    public boolean isBackEnabled()
    {
        return backEnabled;
    }

    private void setBackEnabled(boolean enabledNew)
    {
        boolean enabledOld = backEnabled;
        this.backEnabled = enabledNew;

        firePropertyChange(BACK_ENABLED, enabledOld, enabledNew);
    }

    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    private void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        this.nextEnabled = enabledNew;

        firePropertyChange(NEXT_ENABLED, enabledOld, enabledNew);
    }

    public boolean isSkipEnabled()
    {
        return skipEnabled;
    }

    private void setSkipEnabled(boolean enabledNew)
    {
        boolean enabledOld = skipEnabled;
        this.skipEnabled = enabledNew;

        firePropertyChange(SKIP_ENABLED, enabledOld, enabledNew);
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    private void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        this.finishEnabled = enabledNew;

        firePropertyChange(FINISH_ENABLED, enabledOld, enabledNew);
    }

    public void back()
    {
        if(backEnabled)
        {
            currentPageModel.back();
        }
    }

    public void next()
    {
        if(nextEnabled)
        {
            currentPageModel.next();
        }
    }

    public void skip()
    {
        if(skipEnabled)
        {
            currentPageModel.skip();
        }
    }

    public void finish()
    {
        if(finishEnabled)
        {
            currentPageModel.finish();
        }
    }

    public RichWizardPage getCurrentPageModel()
    {
        return currentPageModel;
    }

    public List<RichWizardPage> getAllPageModels()
    {
        return allPageModels;
    }

    private void setCurrentPageModel(RichWizardPage newPageModel)
    {
        if(currentPageModel != null)
        {
            currentPageModel.removePropertyChangeListener(this);
        }
        RichWizardPage oldPageModel = currentPageModel;
        this.currentPageModel = newPageModel;
        currentPageModel.addPropertyChangeListener(this);
        pullModelProperties();	

        firePropertyChange(WIZARD_PAGE, oldPageModel, newPageModel);
    }

    private void pullModelProperties()
    {
        boolean backEnabledNew = currentPageModel.isBackEnabled();
        setBackEnabled(backEnabledNew);
        boolean nextEnabledNew = currentPageModel.isNextEnabled();
        setNextEnabled(nextEnabledNew);
        boolean skipEnabledNew = currentPageModel.isSkipEnabled();
        setSkipEnabled(skipEnabledNew);
        boolean finishEnabledNew = currentPageModel.isFinishEnabled();
        setFinishEnabled(finishEnabledNew);
        String taskNew = currentPageModel.getTaskDescription();
        setTask(taskNew);
    }

    private void setTask(String taskNew)
    {
        String taskOld = task;
        this.task = taskNew;

        firePropertyChange(TASK, taskOld, taskNew);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {

        String name = evt.getPropertyName();		

        if(name.equals(BACK_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();	
            setBackEnabled(newVal);
        }
        else if(name.equals(NEXT_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();	
            setNextEnabled(newVal);			
        }
        else if(name.equals(SKIP_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();	
            setSkipEnabled(newVal);			
        }
        else if(name.equals(FINISH_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();
            setFinishEnabled(newVal);
        }	
        else if(name.equals(TASK))
        {
            String newVal = (String)evt.getNewValue();
            setTask(newVal);
        }
        else if(name.equals(WIZARD_PAGE))
        {
            RichWizardPage pageModel = (RichWizardPage)evt.getNewValue();
            setCurrentPageModel(pageModel);
        }
    }
}
