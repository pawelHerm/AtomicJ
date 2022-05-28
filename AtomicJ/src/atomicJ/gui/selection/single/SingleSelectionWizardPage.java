
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

package atomicJ.gui.selection.single;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import atomicJ.gui.WizardPage;

public class SingleSelectionWizardPage<E> extends SingleSelectionPanel<E, SingleSelectionWizardPageModel<E>> implements PropertyChangeListener, WizardPage
{
    private static final long serialVersionUID = 1L;

    private static final String IDENTIFIER = "KeySelection";

    public SingleSelectionWizardPage()
    {
        super();
    }

    public SingleSelectionWizardPage(SingleSelectionWizardPageModel<E> model, boolean fullWizardLayout)
    {		
        super(model, fullWizardLayout);
    }

    @Override
    public String getTaskName() 
    {
        return getModel().getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return getModel().getTaskDescription();
    }

    @Override
    public String getIdentifier() 
    {
        return IDENTIFIER;
    }

    @Override
    public boolean isFirst() 
    {
        return getModel().isFirst();
    }

    @Override
    public boolean isLast() 
    {
        return getModel().isLast();
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return getModel().isNecessaryInputProvided();
    }

    @Override
    public Component getView() 
    {
        return this;
    }

    @Override
    public boolean isBackEnabled() 
    {
        return getModel().isBackEnabled();
    }

    @Override
    public boolean isNextEnabled() 
    {
        return getModel().isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return getModel().isBackEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return getModel().isFinishEnabled();
    }
}
