
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

import java.awt.Window;

public class MultipleSelectionWizard<E> extends SinglePageWizard<MultipleSelectionWizardPage<E>, MultipleSelectionWizardPageModel<E>>
{
    private static final String PAGE_IDENTIFIER = "keySelection";

    public MultipleSelectionWizard(Window parent, String title)
    {
        super(parent, new MultipleSelectionWizardPage<E>(PAGE_IDENTIFIER), title);
    }

    @Override
    protected void handleChangeOfModel(MultipleSelectionWizardPage<E> wizardPage, MultipleSelectionWizardPageModel<E> modelOld,
            MultipleSelectionWizardPageModel<E> modelNew) {
        wizardPage.setModel(modelNew);
    }
}
