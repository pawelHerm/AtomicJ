
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

import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import atomicJ.data.Channel2D;
import atomicJ.gui.AbstractMultipleSourceSelectionPage;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.ResourceSelectionModel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.experimental.ExperimentalWizard;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIWizardReceiver;
import atomicJ.readers.SpectroscopyReadingModel;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.SimpleSpectroscopySource;


public class ProcessingSourceSelectionPage extends AbstractMultipleSourceSelectionPage<SimpleSpectroscopySource> implements PropertyChangeListener, WizardPage, ROIWizardReceiver
{
    private static final String TASK_DESCRIPTION = "Select files containing curves to process";
    private static final String TASK_NAME = "Curve selection";

    private final ExperimentalWizard forceVolumeFilteringWizard;

    public ProcessingSourceSelectionPage(ProcessingModel model)
    {
        super(model, SpectroscopyReadingModel.getInstance(), "Batch no ", true, "Preprocess");
        this.forceVolumeFilteringWizard = new ForceVolumeFilteringWizard("Partition force map", model.getPreviewDestination(), this);
    }

    @Override
    public void setModel(ResourceSelectionModel<SimpleSpectroscopySource> modelNew)
    {
        super.setModel(modelNew);
    }   

    @Override
    public void filterSources() 
    {
        forceVolumeFilteringWizard.showDialog(((ProcessingModel)getModel()).getCommonSourceDirectory());             
    }

    @Override
    public String getTaskName() 
    {
        return TASK_NAME;
    }

    @Override
    public String getTaskDescription() 
    {
        return TASK_DESCRIPTION;
    }

    @Override
    public String getIdentifier()
    {
        return ProcessingBatchModelInterface.SOURCES;
    }

    @Override
    public boolean isLast() 
    {
        return false;
    }

    @Override
    public boolean isFirst()
    {
        return true;
    }

    @Override
    protected void handleUnreadImages(List<File> unreadImages) 
    {
        int unreadImagesCount = unreadImages.size();

        if(unreadImagesCount > 0)
        {
            String ending = (unreadImagesCount == 1) ? "": "s";
            JOptionPane.showMessageDialog(getView(), unreadImagesCount + " file" + ending + " contained image data instead of curves", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }		
    }

    @Override
    public void setSelectedROIs(Channel2DSource<? extends Channel2D> image, Channel2D channel, List<ROI> rois)
    {
        ResourceSelectionModel<SimpleSpectroscopySource> model = getModel();
        if(model instanceof ProcessingModel)
        {
            List<Shape> shapes = new ArrayList<>();

            for(ROI roi : rois )
            {
                shapes.add(roi.getROIShape());
            }

            ((ProcessingModel) model).filterSources(shapes);
        }
    }
}
