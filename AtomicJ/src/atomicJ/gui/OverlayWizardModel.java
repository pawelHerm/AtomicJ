
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
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import atomicJ.analysis.PreviewDestination;
import atomicJ.gui.experimental.ExperimentalSourceSelectionModel;
import atomicJ.gui.experimental.ExperimentalSourceSelectionPage;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPage;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;
import atomicJ.readers.ImageReadingModel;
import atomicJ.sources.Channel2DSource;



public class OverlayWizardModel extends AbstractModel implements PropertyChangeListener
{
    private static final int SIZE = 3;

    private static final String NEW_IMAGE_SELECTION_IDENTIFIER = "NewImageSelectionIdentifier";
    private static final String OLD_IMAGE_SELECTION_IDENTIFIER = "OldImageSelectionIdentifier";

    private final PreviewDestination previewDestination;
    private final Overlayable overlayable;
    private WizardPageModel currentPageModel;

    private final ExperimentalSourceSelectionPage sourceSelectionPage;
    private final MultipleSelectionWizardPage<String> newImageSelectionPage;
    private final MultipleSelectionWizardPage<String> oldImageSelectionPage;

    private ExperimentalSourceSelectionModel sourceSelectionModel;
    private MultipleSelectionWizardPageModel<String> newImageSelectionModel;
    private MultipleSelectionWizardPageModel<String> oldImageSelectionModel;

    private int currentPageIndex;	

    private boolean skipEnabled;
    private boolean finishEnabled;
    private boolean backEnabled;
    private boolean nextEnabled;

    private boolean approved;

    public OverlayWizardModel(PreviewDestination previewDestination, Overlayable overlayable)
    {
        this.previewDestination = previewDestination;
        this.overlayable = overlayable;


        this.oldImageSelectionModel = new BasicMultipleSelectionWizardPageModel<>(overlayable.getTypes(), "Old images to be overlaid", "Image selection", "Select old images  you want to overlay", true, false);
        this.oldImageSelectionModel.addPropertyChangeListener(this);
        this.oldImageSelectionPage = new MultipleSelectionWizardPage<>(OLD_IMAGE_SELECTION_IDENTIFIER, oldImageSelectionModel, true);

        this.sourceSelectionModel = new ExperimentalSourceSelectionModel(previewDestination,"Source selection", "Select sources of images you want to overlay", false, false);
        this.sourceSelectionModel.addPropertyChangeListener(this);
        this.sourceSelectionPage = new ExperimentalSourceSelectionPage(sourceSelectionModel, ImageReadingModel.getInstance());		

        this.newImageSelectionModel = new BasicMultipleSelectionWizardPageModel<>(Collections.<String>emptySet(), "New images to overlay", "New image selection", "Select new images you want to overlay", false, true);
        this.newImageSelectionModel.addPropertyChangeListener(this);
        this.newImageSelectionPage = new MultipleSelectionWizardPage<>(NEW_IMAGE_SELECTION_IDENTIFIER, newImageSelectionModel, true);

        initDefaults();
    }

    public void setDefaultSourceLocation(File sourceLocation)
    {
        sourceSelectionPage.setChooserCurrentDirectory(sourceLocation);
    }

    public void reset()
    {
        if(this.oldImageSelectionModel != null)
        {
            this.oldImageSelectionModel.removePropertyChangeListener(this);

            this.oldImageSelectionModel = new BasicMultipleSelectionWizardPageModel<>(overlayable.getTypes(), "Old images to be overlaid", "Image selection", "Select old images  you want to overlay", true, false);
            this.oldImageSelectionModel.addPropertyChangeListener(this);
            this.oldImageSelectionPage.setModel(oldImageSelectionModel);
        }

        if(this.sourceSelectionModel != null)
        {
            this.sourceSelectionModel.removePropertyChangeListener(this);

            this.sourceSelectionModel = new ExperimentalSourceSelectionModel(previewDestination,"Source selection", "Select sources of images you want to overlay", false, false);
            this.sourceSelectionModel.addPropertyChangeListener(this);
            this.sourceSelectionPage.setModel(sourceSelectionModel);        
        }

        if(this.newImageSelectionModel != null)
        {
            this.newImageSelectionModel.removePropertyChangeListener(this);

            this.newImageSelectionModel = new BasicMultipleSelectionWizardPageModel<>(Collections.<String>emptySet(), "New images to overlay", "New image selection", "Select new images you want to overlay", false, true);
            this.newImageSelectionModel.addPropertyChangeListener(this);
            this.newImageSelectionPage.setModel(newImageSelectionModel);
        }

        initDefaults();
    }

    private void initDefaults()
    {
        this.approved = false;
        this.currentPageIndex = 0;		
        setConsistentWithNewPage();
    }

    private void setConsistentWithNewPage()
    {
        this.currentPageModel = getModel(currentPageIndex);

        boolean backEnabledNew = currentPageModel.isBackEnabled();
        boolean skipEnabledNew = currentPageModel.isSkipEnabled();
        boolean nextEnabledNew = currentPageModel.isNextEnabled();
        boolean finishEnabledNew = currentPageModel.isFinishEnabled();

        setBackEnabled(backEnabledNew);
        setSkipEnabled(skipEnabledNew);
        setNextEnabled(nextEnabledNew);
        setFinishEnabled(finishEnabledNew);
    }

    private WizardPageModel getModel(int index)
    {
        if(index == 0)
        {
            return oldImageSelectionModel;
        }
        if(index == 1)
        {
            return sourceSelectionModel;
        }
        if(index == 2)
        {
            return newImageSelectionModel;
        }
        throw new IllegalStateException("A WizardPageModel for the index " + index + " was requested. The index must be between 0 and " + (SIZE - 1) + ", inclusive");
    }


    private WizardPage getWizardPage(int index)
    {
        if(index == 0)
        {
            return oldImageSelectionPage;
        }
        if(index == 1)
        {
            return sourceSelectionPage;
        }
        if(index == 2)
        {
            return newImageSelectionPage;
        }

        return null;
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
        if(ExperimentalSourceSelectionModel.CHANNEL_IDENTIFIERS.equals(name))
        {            
            @SuppressWarnings("unchecked")
            Set<String> channelIdentifiersNew = (Set<String>)evt.getNewValue();
            newImageSelectionModel.setKeys(channelIdentifiersNew);			
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

    public List<WizardPage> getAvailableWizardPages()
    {
        List<WizardPage> pages = Arrays.<WizardPage>asList(oldImageSelectionPage, sourceSelectionPage, newImageSelectionPage);
        return pages;
    }

    public void cancel()
    {}

    public void finish()
    {
        if(finishEnabled)
        {
            currentPageModel.finish();

            List<Channel2DSource<?>> selectedSources = sourceSelectionModel.getSources();
            Set<String> newImages = newImageSelectionModel.getSelectedKeys();
            Set<String> oldImages = oldImageSelectionModel.getSelectedKeys();

            overlayable.overlay(selectedSources, newImages, oldImages);		
        }
    }

    public WizardPage next()
    {
        if(nextEnabled && currentPageIndex < (SIZE - 1))
        {
            currentPageModel.next();
            int currentPageIndexNew = (currentPageIndex + 1) % SIZE;	

            return setCurrentPage(currentPageIndexNew);
        }
        return getCurrentWizardPage();		
    }

    public WizardPage back()
    {
        if(backEnabled && currentPageIndex > 0)
        {
            currentPageModel.back();
            int currentPageIndexNew = (currentPageIndex - 1) % SIZE;	
            return setCurrentPage(currentPageIndexNew);
        }
        return getWizardPage(currentPageIndex);
    }

    private WizardPage setCurrentPage(int newIndex)
    {
        boolean withinRange = (newIndex >= 0) && (newIndex < SIZE);

        if(withinRange && newIndex != currentPageIndex)
        {
            int oldIndex = this.currentPageIndex;
            this.currentPageIndex = newIndex;

            setConsistentWithNewPage();             
            firePropertyChange(WizardModelProperties.WIZARD_PAGE, getWizardPage(oldIndex), getWizardPage(newIndex));
        }

        return getCurrentWizardPage();
    }
}
