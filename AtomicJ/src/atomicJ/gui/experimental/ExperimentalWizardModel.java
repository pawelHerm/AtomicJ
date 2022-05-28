
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

package atomicJ.gui.experimental;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.analysis.PreviewDestination;
import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.Channel2DPanel;
import atomicJ.gui.ImageSourceSelectionModel;
import atomicJ.gui.ROISelectionPage;
import atomicJ.gui.ROISelectionPageModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.WizardPage;
import atomicJ.gui.WizardPageDescriptor;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIWizardReceiver;
import atomicJ.gui.selection.single.BasicSingleSelectionModel;
import atomicJ.gui.selection.single.SingleSelectionWizardPage;
import atomicJ.readers.ImageReadingModel;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.Channel2DSource;


public class ExperimentalWizardModel extends AbstractModel implements PropertyChangeListener
{
    private static final int SIZE = 3;

    private final ChannelFilter dataTypeFilter;

    private PreviewDestination previewDestination;
    private ROIWizardReceiver roiWizardReceiver;
    private WizardPageModel currentPageModel;

    private ExperimentalSourceSelectionPage sourceSelectionPage;
    private SingleSelectionWizardPage<String> keySelectionPage;
    private ROISelectionPage<Channel2DResource, Channel2DChart<?>, Channel2DPanel<Channel2DChart<?>>> roiSelectionPage;

    private ExperimentalSourceSelectionModel sourceSelectionModel;
    private BasicSingleSelectionModel<String> keySelectionModel;
    private ROISelectionPageModel<Channel2DResource> roiSelectionModel;

    private final WizardPageDescriptor sourceSelectionDescriptor;
    private final WizardPageDescriptor channelSelectionDescriptor;
    private final WizardPageDescriptor roiSelectionDescriptor;

    private int currentPageIndex;	

    private boolean skipEnabled;
    private boolean finishEnabled;
    private boolean backEnabled;
    private boolean nextEnabled;

    private final boolean restricted;
    private boolean approved;

    public ExperimentalWizardModel(PreviewDestination previewDestination, ROIWizardReceiver roiImageReceiver, ChannelFilter dataTypeFilter, 
            WizardPageDescriptor sourceSelectionDescriptor, WizardPageDescriptor channelSelectionDescriptor, WizardPageDescriptor roiSelectionDescriptor, boolean restricted)
    {
        this.dataTypeFilter = dataTypeFilter;
        this.previewDestination = previewDestination;
        this.roiWizardReceiver = roiImageReceiver;

        this.restricted = restricted;

        this.sourceSelectionDescriptor = sourceSelectionDescriptor;
        this.channelSelectionDescriptor = channelSelectionDescriptor;
        this.roiSelectionDescriptor = roiSelectionDescriptor;

        reset();    
    }

    public void setROIReceiver(PreviewDestination previewDestination, ROIWizardReceiver roiImageReceiver)
    {
        this.previewDestination = previewDestination;
        this.roiWizardReceiver = roiImageReceiver;

        reset();
    }

    public void setDefaultSourceLocation(File sourceLocation)
    {
        sourceSelectionPage.setChooserCurrentDirectory(sourceLocation);
    }

    public Channel2DSource<?> getSelectedSource()
    {
        List<Channel2DSource<?>> sources = sourceSelectionModel.getSources();
        Channel2DSource<?> source = null;

        if(!sources.isEmpty())
        {
            source = sources.get(0);
        }

        return source;
    }

    public void reset()
    {
        //RESETS SOURCE SELECTION
        if(this.sourceSelectionModel != null)
        {
            this.sourceSelectionModel.removePropertyChangeListener(this);
        }

        this.sourceSelectionModel = new ExperimentalSourceSelectionModel(previewDestination, 
                sourceSelectionDescriptor.getTaskName(),
                sourceSelectionDescriptor.getTaskDescription(), sourceSelectionDescriptor.isFirst(), 
                sourceSelectionDescriptor.isLast());
        this.sourceSelectionModel.setTypeOfData(dataTypeFilter, "Chosen image does not contain a channel with sample " + dataTypeFilter);
        this.sourceSelectionModel.addPropertyChangeListener(this);
        /*
         * this.sourceSelectionModel = new ImageSourceSelectionModel(previewDestination, "Image selection", 
         * "Choose an image of the sample topography", true, false);
        this.sourceSelectionModel.setTypeOfData(dataTypeFilter, "Chosen image does not contain a channel with sample topography");
        this.sourceSelectionModel.addPropertyChangeListener(this);

         */

        if(this.sourceSelectionPage == null)
        {
            this.sourceSelectionPage = new ExperimentalSourceSelectionPage(sourceSelectionModel, ImageReadingModel.getInstance());       
            this.sourceSelectionPage.setTypeOfData(dataTypeFilter);
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


        this.keySelectionModel = new BasicSingleSelectionModel<>(new LinkedHashSet<String>(), "Available images",
                channelSelectionDescriptor.getTaskName(), channelSelectionDescriptor.getTaskDescription(), 
                channelSelectionDescriptor.isFirst(), channelSelectionDescriptor.isLast());
        this.keySelectionModel.addPropertyChangeListener(this);

        if(this.keySelectionPage == null)
        {
            this.keySelectionPage = new SingleSelectionWizardPage<>(keySelectionModel, true);
        }
        else
        {
            this.keySelectionPage.setModel(keySelectionModel);
        }

        //RESETS ROI SELECTION
        if(this.roiSelectionModel != null)
        {
            this.roiSelectionModel.removePropertyChangeListener(this);
        }


        this.roiSelectionModel = new ROISelectionPageModel<>(roiSelectionDescriptor.getTaskName(), 
                roiSelectionDescriptor.getTaskDescription(), roiSelectionDescriptor.isFirst(), 
                roiSelectionDescriptor.isLast(), roiSelectionDescriptor.canBeSkipped(), restricted);
        this.roiSelectionModel.addPropertyChangeListener(this);
        if(this.roiSelectionPage == null)
        {
            this.roiSelectionPage = new ROISelectionPage<>(previewDestination.getPublicationSite(), roiSelectionModel);
        }
        else
        {
            this.roiSelectionPage.setModel(roiSelectionModel);
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
        else if(index >= 2)
        {
            pageModel = roiSelectionModel;
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

        if(ImageSourceSelectionModel.CHANNEL_IDENTIFIERS.equals(name))
        {
            @SuppressWarnings("unchecked")
            Set<String> channelIdentifiersNew = (Set<String>)evt.getNewValue();
            keySelectionModel.setKeys(channelIdentifiersNew);	                  
        }

    }

    public PreviewDestination getPreviewDestination()
    {
        return previewDestination;
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
        boolean withinRange = (index>=0);

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
            else if(index == 2)
            {
                page = roiSelectionPage;
            }
            else
            {
                page = roiSelectionPage;
            }
        }

        return page;
    }


    private void initializeROISelectionPage()
    {
        List<Channel2DSource<?>> sources = sourceSelectionModel.getSources();

        if(!sources.isEmpty())
        {
            String type = keySelectionModel.getSelectedKey();  
            Channel2DSource<?> s = sources.get(0);
            StandardChannel2DResource resource = new StandardChannel2DResource(s, s.getIdentifiers());

            roiSelectionModel.setType(type);
            roiSelectionModel.setDensityResource(resource);
        }        
    }

    public List<WizardPage> getAvailableWizardPages()
    {
        List<WizardPage> pages = new ArrayList<>();

        pages.add(sourceSelectionPage);
        pages.add(keySelectionPage);
        pages.add(roiSelectionPage);

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

            Channel2DSource<?> selectedSource = getSelectedSource();
            Channel2D selectedTopographyChannel = roiSelectionModel.getSelectedChannel();
            List<ROI> selectedROIs = roiSelectionModel.getSelectedROIs();

            roiWizardReceiver.setSelectedROIs(selectedSource, selectedTopographyChannel, selectedROIs);
        }
    }

    public WizardPage next()
    {        
        if(nextEnabled)
        {
            currentPageModel.next();
            int currentPageIndexNew = (currentPageIndex + 1);	

            if(currentPageIndexNew == 2)
            {
                initializeROISelectionPage();
            }

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
            int currentPageIndexNew = (currentPageIndex - 1);	
            return setCurrentPage(currentPageIndexNew);
        }
        else
        {
            return getCurrentWizardPage();

        }
    }

    private WizardPage setCurrentPage(int newIndex)
    {
        boolean withinRange = (newIndex>=0);

        if(withinRange)
        {
            if(newIndex != currentPageIndex)
            {
                int oldIndex = this.currentPageIndex;
                this.currentPageIndex = newIndex;

                setConsistentWithNewPage();			

                firePropertyChange(WizardModelProperties.BACK_COMMAND, getBackCommand(oldIndex), getBackCommand(newIndex));
                firePropertyChange(WizardModelProperties.NEXT_COMMAND, getNextCommand(oldIndex), getNextCommand(newIndex));
                firePropertyChange(WizardModelProperties.WIZARD_PAGE, getWizardPage(oldIndex), getWizardPage(newIndex));
            }
        }
        return getCurrentWizardPage();
    }

    private String getNextCommand(int index)
    {        
        String nextCommand = index<(SIZE - 1 ) ? "Next   >>": roiSelectionDescriptor.getSubPageNextCommand();

        return nextCommand;
    }

    private String getBackCommand(int index)
    {      
        String backCommand = index<SIZE ? "<<   Back": roiSelectionDescriptor.getSubPageBackCommand();
        return backCommand;
    }
}
