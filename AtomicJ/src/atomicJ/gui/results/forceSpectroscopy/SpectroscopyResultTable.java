
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

package atomicJ.gui.results.forceSpectroscopy;


import java.awt.Toolkit;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.gui.results.ResultTable;
import atomicJ.sources.SimpleSpectroscopySource;


public class SpectroscopyResultTable extends ResultTable <SimpleSpectroscopySource, ProcessedSpectroscopyPack, SpectroscopyResultDestination>
{   
    private static final long serialVersionUID = 1L;

    private final Action markOnMapAction = new MarkOnMapAction();

    public SpectroscopyResultTable(SpectroscopyResultTableModel tableModel, SpectroscopyResultDestination resultDestination)
    {
        super(tableModel, resultDestination);

        JPopupMenu menu = buildPopupMenu();
        menu.insert(new JMenuItem(markOnMapAction), 4);

        initMouseListener(menu);
        checkIfMarkSourcesShouldBeEnabled();
    }

    public Action getMarkOnMapAction()
    {
        return markOnMapAction;
    }

    @Override
    protected void runAdditionResponsesToSelectionChange(ListSelectionEvent e)
    {
        if(!e.getValueIsAdjusting())
        {
            checkIfMarkSourcesShouldBeEnabled();
        }
    }

    private void checkIfMarkSourcesShouldBeEnabled()
    {
        SpectroscopyResultTableModel tableModel = (SpectroscopyResultTableModel) getModel();
        boolean enabled = tableModel.containsPacksFromMap(true);
        markOnMapAction.setEnabled(enabled);
    }

    private void markSourcesOnMap(List<ProcessedSpectroscopyPack> packs)
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        for(ProcessedSpectroscopyPack pack : packs)
        {
            SimpleSpectroscopySource source = pack.getSource();
            if(source.isFromMap())
            {
                sources.add(source);
            }
        }

        getResultDestination().markSourcePositions(sources);
    }

    private class MarkOnMapAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public MarkOnMapAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/MarkOnMap.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Mark on map");

            putValue(NAME,"Mark on map");
        }

        @Override
        public void actionPerformed(ActionEvent event)        
        {
            markSourcesOnMap(getSelectedPacks());
        }
    }   
}

