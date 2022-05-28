
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import atomicJ.sources.IdentityTag;
import atomicJ.utilities.Validation;

public class ColumnVisibilityDialog extends JDialog  implements ItemListener
{
    private static final long serialVersionUID = 1L;

    private final JTable table;
    private final List<IdentityTag> columnIds;
    private final List<Boolean> columnInitialVisibility = new ArrayList<>();
    private final List<JCheckBox> boxes = new ArrayList<>();
    private final JButton buttonReset = new JButton(new ResetAction());
    private final JButton buttonClose = new JButton(new CloseAction());

    private final JPanel  mainPanel;

    private ColumnVisibilityDialog(JDialog parent, JTable table, List<IdentityTag> columnIds)
    {
        super(parent, "Show and hide columns", false);
        this.table = table;
        this.columnIds = new ArrayList<>(columnIds);

        this.mainPanel = buildMainPanel();
        JPanel buttonPanel = buildButtonPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public static ColumnVisibilityDialog getDialog(JDialog parent, JTable table, List<IdentityTag> columnIds)
    {
        Validation.requireNonNullParameterName(columnIds, "columnIds");

        if(table.getModel().getColumnCount() != columnIds.size())
        {
            throw new IllegalArgumentException("The length of the array 'columnNames' should equal the number of columns in the table");
        }
        return new ColumnVisibilityDialog(parent, table, columnIds);
    }

    private void reset()
    {		
        int n = boxes.size();

        for(int modelIndex = 0; modelIndex<n; modelIndex++)
        {
            boolean initVisible = columnInitialVisibility.get(modelIndex);
            boolean selected = boxes.get(modelIndex).isSelected();

            if(initVisible != selected)
            {
                boxes.get(modelIndex).setSelected(initVisible);
            }
        }
    }

    private void close()
    {		
        setVisible(false);
    }

    private void changeColumnVisibility(int modelIndex, boolean visible)
    {
        if(visible)
        {
            int viewIndex = table.convertColumnIndexToView(modelIndex);
            if(viewIndex == -1)
            {
                table.addColumn(new TableColumn(modelIndex));
                table.moveColumn(table.convertColumnIndexToView(modelIndex), modelIndex);
            }
        }
        else
        {
            int viewIndex = table.convertColumnIndexToView(modelIndex);

            if(viewIndex>=0)
            {
                TableColumn column = table.getColumnModel().getColumn(viewIndex);
                table.removeColumn(column);
            }
        }
    }

    private JPanel buildMainPanel()
    {
        JPanel mainPanel = new JPanel(new GridLayout(10,0,5,5));

        TableModel tableModel = table.getModel();
        int n = tableModel.getColumnCount();

        for(int modelIndex = 0; modelIndex<n; modelIndex++)
        {
            IdentityTag columnId = columnIds.get(modelIndex);
            JCheckBox box = new JCheckBox(columnId.getLabel());
            box.setIconTextGap(15);
            box.addItemListener(this);

            int viewIndex = table.convertColumnIndexToView(modelIndex);
            boolean visible = viewIndex>=0;
            box.setSelected(visible);

            columnInitialVisibility.add(visible);
            boxes.add(box);
            mainPanel.add(box);
        }

        return mainPanel;
    }

    public void addNewColumn(IdentityTag columnId)
    {
        int index = columnIds.size();
        columnIds.add(columnId);

        JCheckBox box = new JCheckBox(columnId.getLabel());
        box.setIconTextGap(15);
        box.addItemListener(this);

        int viewIndex = table.convertColumnIndexToView(index);
        boolean visible = viewIndex >= 0;
        box.setSelected(visible);

        columnInitialVisibility.add(visible);
        boxes.add(box);
        mainPanel.add(box);

        revalidate();
    }

    private JPanel buildButtonPanel()
    {
        JPanel outerPanel = new JPanel();
        JPanel innerPanel = new JPanel(new GridLayout(1,0,5,5));
        innerPanel.add(buttonReset);
        innerPanel.add(buttonClose);

        innerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        outerPanel.add(innerPanel);
        outerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return outerPanel;
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);

        int modelIndex = boxes.indexOf(source);

        if(modelIndex > -1 && modelIndex<boxes.size())
        {
            changeColumnVisibility(modelIndex, selected);
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {           
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            reset();
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {           
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            close();
        }
    }
}
