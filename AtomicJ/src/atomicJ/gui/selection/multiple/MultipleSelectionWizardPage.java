
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

package atomicJ.gui.selection.multiple;

import atomicJ.gui.WizardPage;

public class MultipleSelectionWizardPage<E> extends MultipleSelectionPanel<E, MultipleSelectionWizardPageModel<E>> implements WizardPage
{
    private static final long serialVersionUID = 1L;

    private final String identifier;

    public MultipleSelectionWizardPage(String identifier)
    {
        super();

        this.identifier = identifier;
    }

    public MultipleSelectionWizardPage(String identifier, MultipleSelectionWizardPageModel<E> model, boolean fullWizardLayout)
    {		
        super(model, fullWizardLayout);

        this.identifier = identifier;
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
        return identifier;
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
