
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import atomicJ.gui.units.UnitSelectionDialog;


public class FlexibleNumericalTableView extends StandardNumericalTableView
{
    private final Action visibilityAction = new ChangeVisibilityAction();
    private final Action selectUnitsAction = new SelectUnitsAction();
    private final ColumnVisibilityDialog hideColumnsDialog;

    private final UnitSelectionDialog unitSelectionDialog;

    public FlexibleNumericalTableView(Window parent, StandardNumericalTable table, String title)
    {
        super(parent,Dialog.ModalityType.MODELESS, table, title);	

        this.hideColumnsDialog = ColumnVisibilityDialog.getDialog(getMainDialog(), table, table.getColumnShortNames());
        this.unitSelectionDialog = new UnitSelectionDialog(getMainDialog(), table.getUnitSelectionPanel());

        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
        getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JMenuItem itemSelectUnits = new JMenuItem(selectUnitsAction);
        JMenuItem itemColumnVsibility = new JMenuItem(visibilityAction);

        addMenuItem(itemSelectUnits);
        addMenuItem(itemColumnVsibility);	

    }

    private class ChangeVisibilityAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ChangeVisibilityAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Column visibility");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            hideColumnsDialog.setVisible(true);
        }
    }

    private class SelectUnitsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectUnitsAction() {
            putValue(NAME, "Select units");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            unitSelectionDialog.setVisible(true);
        }
    }
}
