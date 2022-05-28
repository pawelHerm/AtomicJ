
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

package atomicJ.gui.histogram;

import java.awt.*;
import java.awt.Dialog.ModalityType;

import javax.swing.*;

public class LiveHistogramView extends TransformableHistogramView implements TransformableHistogramPanelSupervisor, HistogramDestination
{
    public LiveHistogramView(final Window parent, String title)
    {
        this(parent, title, ModalityType.MODELESS, true, true);	
    }

    public LiveHistogramView(Window parent, String title, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        super(parent, title, modalityType, allowsMultipleResources, allowsMultipleTypes);
    }

    @Override
    protected JPanel getButtonsPanel()
    {
        return buildSingleResourcesButtonPanel();
    }

    private JPanel buildSingleResourcesButtonPanel()
    {	
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 5));	
        JPanel buttonPanelOuter = new JPanel();

        if(getAllowsMultipleTypes())
        {
            JButton buttonSaveAll = new JButton(getSaveAllAction());
            buttonPanel.add(buttonSaveAll);
        }

        JButton buttonClose = new JButton(getCloseAction());

        buttonPanel.add(buttonClose);

        buttonPanelOuter.add(buttonPanel);
        buttonPanelOuter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanelOuter;
    }
}
