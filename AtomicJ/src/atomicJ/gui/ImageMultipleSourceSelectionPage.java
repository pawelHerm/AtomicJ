
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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.readers.ImageReadingModel;
import atomicJ.sources.ImageSource;



public class ImageMultipleSourceSelectionPage extends AbstractMultipleSourceSelectionPage<ImageSource> implements PropertyChangeListener, WizardPage
{
    private final boolean isFirst;
    private final boolean isLast;

    public ImageMultipleSourceSelectionPage(ImageSourceSelectionModel model, boolean isFirst, boolean isLast)
    {
        super(model, ImageReadingModel.getInstance(), "");
        this.isFirst = isFirst;
        this.isLast = isLast;
    }

    @Override
    public String getTaskName() 
    {
        return ((WizardPageModel)getModel()).getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return ((WizardPageModel)getModel()).getTaskDescription();
    }

    @Override
    public String getIdentifier()
    {
        return ProcessingBatchModelInterface.SOURCES;
    }

    @Override
    public boolean isLast() 
    {
        return isLast;
    }

    @Override
    public boolean isFirst()
    {
        return isFirst;
    }

    @Override
    protected void handleUnreadSpectroscopyFiles(List<File> unreadSpectroscopyFiles) 
    {		
        int unreadCount = unreadSpectroscopyFiles.size();

        if(unreadCount > 0)
        {
            String ending = (unreadCount == 1) ? "": "s";
            JOptionPane.showMessageDialog(getView(), unreadCount + " file" + ending + " contained spectroscopy recordings instead of images", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }		
    }

}
