
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.gui.selection.multiple.BasicMultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPage;
import atomicJ.resources.Resource;



public class ResourceSelectionWizardModel extends AbstractModel implements PropertyChangeListener
{
    private static final int SIZE = 2;

    private ResourceReceiver resourceReceiver;
    private WizardPageModel currentPageModel;

    private ResourceSelectionPage<Resource> sourceSelectionPage;
    private MultipleSelectionWizardPage<String> keySelectionPage;

    private ResourceChooserModel<Resource> sourceSelectionModel;
    private BasicMultipleSelectionWizardPageModel<String> keySelectionModel;

    private int currentPageIndex;	

    private boolean skipEnabled;
    private boolean finishEnabled;
    private boolean backEnabled;
    private boolean nextEnabled;

    private boolean approved;

    public ResourceSelectionWizardModel(ResourceReceiver resourceReceiver)
    {
        this.resourceReceiver = resourceReceiver;
        reset();    
    }

    public void setResourceReceiver(ResourceReceiver resourceReceiver)
    {
        this.resourceReceiver = resourceReceiver;
    }

    public void setResources(Map<Resource, List<String>> allResourcesNew)
    {
        this.sourceSelectionModel.setResources(allResourcesNew);
    }

    public Resource getSelectedResource()
    {
        Resource resource = sourceSelectionModel.getChosenResource();       
        return resource;
    }

    public Set<String> getSelectedChannelIdentifiers()
    {
        return keySelectionModel.getSelectedKeys();
    }

    public void reset()
    {
        //RESETS SOURCE SELECTION
        if(this.sourceSelectionModel != null)
        {
            this.sourceSelectionModel.removePropertyChangeListener(this);
        }

        this.sourceSelectionModel = new ResourceStandardChooserModel("Selection", "Choose charts to overlay", true, false);
        this.sourceSelectionModel.addPropertyChangeListener(this);

        if(this.sourceSelectionPage == null)
        {
            this.sourceSelectionPage = new ResourceSelectionPage<>(sourceSelectionModel, true, false);       
        }
        else
        {
            this.sourceSelectionPage.setModel(sourceSelectionModel);        
        }        

        //RESETS KEY SELECTION 
        if(this.keySelectionModel != null)
        {
            this.keySelectionModel.removePropertyChangeListener(this);

        }
        this.keySelectionModel = new BasicMultipleSelectionWizardPageModel<>(new LinkedHashSet<String>(), "Chart types", "Chart types", "Select types of charts to overlay", false, true);
        this.keySelectionModel.addPropertyChangeListener(this);

        if(this.keySelectionPage == null)
        {
            this.keySelectionPage = new MultipleSelectionWizardPage<>("ChannelIdentifierSelection", keySelectionModel, true);
        }
        else
        {
            this.keySelectionPage.setModel(keySelectionModel);
        }

        initDefaults();
    }

    private void initDefaults()
    {
        this.approved = false;
        this.currentPageIndex = 0;		
        setConsistentWithNewPage();
    }

    private WizardPageModel getPageModel(int index)
    {
        WizardPageModel pageModel = null;

        if(index == 0)
        {
            pageModel = sourceSelectionModel;
        }
        else if(index == 1)
        {
            pageModel = keySelectionModel;
        }      

        return pageModel;
    }

    private void setConsistentWithNewPage()
    {
        this.currentPageModel = getPageModel(currentPageIndex);

        boolean backEnabledNew = currentPageModel.isBackEnabled();
        boolean skipEnabledNew = currentPageModel.isSkipEnabled();
        boolean nextEnabledNew = currentPageModel.isNextEnabled();
        boolean finishEnabledNew = currentPageModel.isFinishEnabled();

        setBackEnabled(backEnabledNew);
        setSkipEnabled(skipEnabledNew);
        setNextEnabled(nextEnabledNew);
        setFinishEnabled(finishEnabledNew);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        Object source = evt.getSource();

        if(source == currentPageModel)
        {
            if(WizardModelProperties.BACK_ENABLED.equals(name))
            {			
                boolean backEnabledNew = (boolean)evt.getNewValue();				
                setBackEnabled(backEnabledNew);
            }
            if(WizardModelProperties.NEXT_ENABLED.equals(name))
            {			
                boolean nextEnabledNew = (boolean)evt.getNewValue();				
                setNextEnabled(nextEnabledNew);
            }
            if(WizardModelProperties.SKIP_ENABLED.equals(name))
            {			
                boolean skipEnabledNew = (boolean)evt.getNewValue();				
                setSkipEnabled(skipEnabledNew);
            }
            if(WizardModelProperties.FINISH_ENABLED.equals(name))
            {			
                boolean finishEnabledNew = (boolean)evt.getNewValue();				
                setFinishEnabled(finishEnabledNew);
            }
        }
        if(ResourceStandardChooserModel.RESOURCE_IDENTIFIERS.equals(name))
        {            
            @SuppressWarnings("unchecked")
            Set<String> identifiersNew = new LinkedHashSet<>((List<String>)evt.getNewValue());
            keySelectionModel.setKeys(identifiersNew);	                  
        }
    }

    public boolean isApproved()
    {
        return approved;
    }

    public boolean isBackEnabled()
    {
        return backEnabled;
    }

    public void setBackEnabled(boolean enabledNew)
    {
        boolean enabledOld = backEnabled;
        backEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.BACK_ENABLED, enabledOld, enabledNew);
    }

    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    public void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        nextEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);
    }

    public boolean isSkipEnabled()
    {
        return skipEnabled;
    }

    public void setSkipEnabled(boolean enabledNew)
    {
        boolean enabledOld = this.skipEnabled;
        this.skipEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.SKIP_ENABLED, enabledOld, enabledNew);
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    public void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        finishEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);
    }

    public WizardPage getCurrentWizardPage()
    {
        return getWizardPage(currentPageIndex);
    }

    private WizardPage getWizardPage(int index)
    {
        boolean withinRange = (index>=0)&&(index<SIZE);

        WizardPage page = null; 
        if(withinRange)
        {
            if(index == 0)
            {
                page = sourceSelectionPage;

            }
            else if(index == 1)
            {
                page = keySelectionPage;

            }            
        }

        return page;
    }

    public List<WizardPage> getAvailableWizardPages()
    {
        List<WizardPage> pages = new ArrayList<>();

        pages.add(sourceSelectionPage);
        pages.add(keySelectionPage);

        return pages;
    }

    public void cancel()
    {
    }

    public void finish()
    {
        if(finishEnabled)
        {
            currentPageModel.finish();

            Resource resource = getSelectedResource();
            Set<String> types = keySelectionModel.getSelectedKeys();

            resourceReceiver.setResource(resource, types);
        }
    }

    public WizardPage next()
    {
        if(nextEnabled && currentPageIndex<(SIZE - 1))
        {
            currentPageModel.next();
            int currentPageIndexNew = (currentPageIndex + 1) % SIZE;	
            return setCurrentPage(currentPageIndexNew);
        }
        else
        {
            return getCurrentWizardPage();
        }		
    }

    public WizardPage back()
    {
        if(backEnabled && currentPageIndex>0)
        {
            currentPageModel.back();
            int currentPageIndexNew = (currentPageIndex - 1) % SIZE;	
            return setCurrentPage(currentPageIndexNew);
        }
        else
        {
            return getWizardPage(currentPageIndex);
        }
    }

    private WizardPage setCurrentPage(int newIndex)
    {
        boolean withinRange = (newIndex>=0)&&(newIndex<SIZE);

        if(withinRange)
        {
            if(newIndex != currentPageIndex)
            {
                int oldIndex = this.currentPageIndex;
                this.currentPageIndex = newIndex;
                setConsistentWithNewPage();				
                firePropertyChange(WizardModelProperties.WIZARD_PAGE, getWizardPage(oldIndex), getWizardPage(newIndex));
            }
        }
        return getCurrentWizardPage();
    }
}
