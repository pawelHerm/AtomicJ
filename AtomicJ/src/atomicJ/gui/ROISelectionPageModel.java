
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


import static atomicJ.gui.WizardModelProperties.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.ObjectUtilities;

import atomicJ.data.Channel2D;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.curveProcessing.ProcessingModelInterface;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIUtilities;
import atomicJ.resources.Channel2DResource;


public class ROISelectionPageModel<R extends Channel2DResource> extends AbstractWizardPageModel 
{
    static final String CHANNEL2D_RESOURCE = "Channel2dResource";

    private final List<ROIBatch> roiBatches = new ArrayList<>();
    private ROIBatch currentBatch = new ROIBatch();

    private R resource;
    private String type;

    private final String taskName;
    private final String taskDescription;

    private final boolean isFirst;
    private final boolean isLast;

    private final boolean acceptEmptyResult;

    private boolean necessaryInputProvided;
    private boolean finishEnabled;
    private boolean nextEnabled;

    private final boolean restricted;
    private int currentBatchIndex = 0;

    public ROISelectionPageModel(String taskName, String taskDescription, boolean isFirst, boolean isLast)
    {
        this(taskName, taskDescription, isFirst, isLast, false, true);
    }

    public ROISelectionPageModel(String taskName, String taskDescription, boolean isFirst, boolean isLast, boolean acceptEmptyResult, boolean restricted)
    {
        this.taskName = taskName;
        this.taskDescription = taskDescription;

        this.isFirst = isFirst;
        this.isLast = isLast;
        this.acceptEmptyResult = acceptEmptyResult;
        this.restricted = restricted;

        roiBatches.add(currentBatch);
    }

    public boolean isRestricted()
    {
        return restricted;
    }

    public int getIndex(ROIDrawable roi)
    {
        int n = roiBatches.size();

        for(int i = 0; i<n; i++)
        {
            ROIBatch batch = roiBatches.get(i);

            if(batch.contains(roi))
            {
                return i;
            }
        }

        return -1;
    }

    public void nextBatch()
    {        
        int newIndex = currentBatchIndex + 1;   
        if(newIndex == roiBatches.size())
        {
            addNewBatch();
        }
        setCurrentBatch(newIndex);
    }

    public void previousBatch()
    {
        int newIndex = currentBatchIndex - 1;   
        setCurrentBatch(newIndex);
    }

    private void setCurrentBatch(int newIndex)
    {
        int size = roiBatches.size();

        boolean withinRange = (newIndex>=0)&&(newIndex<size);
        if(withinRange)
        {
            ROIBatch oldModel = currentBatch;
            ROIBatch newModel = roiBatches.get(newIndex);

            int oldIndex = this.currentBatchIndex;

            if(newIndex != oldIndex)
            {
                this.currentBatch = newModel;
                this.currentBatchIndex = newIndex;
                this.necessaryInputProvided = newModel.isNecessaryInputProvided();

                Map<Object, ROIDrawable> newROIs = currentBatch.getROIMap();


                firePropertyChange(ProcessingBatchModel.SELECTED_ROIS, oldModel.getROIMap(), newROIs);
                firePropertyChange(ProcessingModelInterface.CURRENT_BATCH_NUMBER, oldIndex, newIndex);
                firePropertyChange(NECESSARY_INPUT_PROVIDED, oldModel.isNecessaryInputProvided(), this.necessaryInputProvided);              
            }
        }       
    }

    public int getCurrentBatchIndex()
    {
        return currentBatchIndex;
    }

    public void addNewBatch()
    {    
        ROIBatch newBatch = new ROIBatch();

        roiBatches.add(newBatch);
    }

    public void removeROI(ROIDrawable roi)
    {
        currentBatch.removeROI(roi);
        updateROIs();
    }

    public void addOrReplaceROI(ROIDrawable roi)
    {
        currentBatch.addOrReplaceROI(roi);
        updateROIs();
    }

    public void addOrReplaceROI(ROIDrawable roi, int index)
    {
        int n = roiBatches.size();

        if(index>= 0 && index<n)
        {
            ROIBatch batch = roiBatches.get(index);
            batch.addOrReplaceROI(roi);
            updateROIs();
        }
        else if(index<0)
        {
            currentBatch.addOrReplaceROI(roi);
            updateROIs();
        }
    }

    public void setROIs(Map<Object, ROIDrawable> rois)
    {
        currentBatch.setROIs(rois);
        updateROIs();
    }

    private void updateROIs()
    {
        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    public String getROILabel(Object key)
    {

        return key.toString();

        /*
        if(restricted)
        {
            return key.toString();
        }
        else
        {
            String labelNew = Integer.toString(index) + "(" + key.toString() + ")";
            return labelNew;
        }
         */
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Channel2D getSelectedChannel()
    {
        Channel2D selectedGridChannel = null;

        if(resource != null & !resource.getChannels(type).isEmpty())
        {
            selectedGridChannel = resource.getChannels(type).values().iterator().next();
        }

        return selectedGridChannel;
    }

    public List<ROI> getSelectedROIs()
    {
        int n = roiBatches.size();

        List<ROI> selectedROIs = new ArrayList<>();

        for(int i = 0; i<n; i++)
        {
            ROIBatch batch = roiBatches.get(i);
            ROI roiUnion = ROIUtilities.composeROIs(batch.getROIs(), Integer.valueOf(i));
            selectedROIs.add(roiUnion);
        }
        return selectedROIs;
    }

    public R getDensityResource()
    {
        return resource;
    }

    public void setDensityResource(R resourceNew)
    {       
        Channel2DResource resourceOld = this.resource;
        this.resource = resourceNew;

        clearROIBatches();

        if(!ObjectUtilities.equal(resourceOld, resourceNew))
        {
            firePropertyChange(CHANNEL2D_RESOURCE, resourceOld, resourceNew);
        }

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    private void clearROIBatches()
    {
        roiBatches.clear();
        currentBatch = new ROIBatch();
        roiBatches.add(currentBatch);
    }

    @Override
    public boolean isBackEnabled()
    {
        return !isFirst;
    }

    @Override
    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    @Override
    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    @Override
    public boolean isSkipEnabled()
    {
        return false;
    }

    @Override
    public void back() 
    {
        previousBatch();
    }

    @Override
    public void next() 
    {
        nextBatch();
    }

    @Override
    public void skip() 
    {		
    }

    @Override
    public void finish() 
    {		
    }

    private static class ROIBatch
    {
        Map<Object, ROIDrawable> rois = new LinkedHashMap<>();

        private void setROIs(Map<Object, ROIDrawable> rois)
        {
            rois.clear();
            rois.putAll(rois);
        }

        private boolean contains(ROIDrawable roi)
        {
            boolean contains = false;
            for(ROIDrawable r : rois.values())
            {
                if(r.getKey().equals(roi.getKey()))
                {
                    return true;                  
                }
            }

            return contains;
        }

        private Map<Object, ROIDrawable> getROIMap()
        {
            Map<Object, ROIDrawable> roiCopy = new LinkedHashMap<>();
            roiCopy.putAll(this.rois);

            return roiCopy;
        }

        private List<ROI> getROIs()
        {
            List<ROI> rois = new ArrayList<>();

            rois.addAll(this.rois.values());

            return rois;
        }

        private void removeROI(ROIDrawable roi)
        {
            rois.remove(roi.getKey());
        }

        private void addOrReplaceROI(ROIDrawable roi)
        {
            rois.put(roi.getKey(), roi);
        }

        private boolean isNecessaryInputProvided()
        {
            return rois.size()>0;
        }
    }

    private void checkIfNecessaryInputProvided()
    {
        boolean necesaryInputProvidedNew = currentBatch.isNecessaryInputProvided();

        boolean necessaryInputProvidedOld = this.necessaryInputProvided;
        this.necessaryInputProvided = necesaryInputProvidedNew;

        firePropertyChange(NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necesaryInputProvidedNew);
    }

    private void checkIfNextEnabled()
    {
        if(!isLast || !restricted)
        {
            boolean enabledNew = necessaryInputProvided;

            boolean enabledOld = nextEnabled;
            this.nextEnabled = enabledNew;

            firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);
        }		
    }

    private void checkIfFinishEnabled()
    {
        if(isLast)
        {
            boolean enabledNew = false;
            if(acceptEmptyResult)
            {
                enabledNew = true;
            }
            else
            {
                enabledNew = necessaryInputProvided;
            }			
            boolean enabledOld = finishEnabled;
            this.finishEnabled = enabledNew;

            firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);		
        }		
    }

    @Override
    public String getTaskDescription()
    {
        return taskDescription;
    }

    @Override
    public String getTaskName() 
    {
        return taskName;
    }

    @Override
    public boolean isFirst() 
    {
        return isFirst;
    }

    @Override
    public boolean isLast() {
        return isLast;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }

    @Override
    public void cancel() 
    {
    }
}
