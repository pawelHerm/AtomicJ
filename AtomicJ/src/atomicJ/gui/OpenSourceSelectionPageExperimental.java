
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

import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.readers.SourceReadingModel;
import atomicJ.sources.ChannelSource;

public class OpenSourceSelectionPageExperimental<E extends ChannelSource> extends AbstractMultipleSourceSelectionPage<E> implements PropertyChangeListener, WizardPage
{
    private static final String TASK_NAME = "Open";
    private static final String TASK_DESCRIPTION = "Select files to open";

    public OpenSourceSelectionPageExperimental(OpeningModelStandard<E> model, SourceReadingModel<E> readingModel)
    {
        super(model, readingModel, "", false,  "Open");
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
        return true;
    }

    @Override
    public boolean isFirst()
    {
        return true;
    }
}
