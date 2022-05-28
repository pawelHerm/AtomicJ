
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.Validation;

public abstract class MinimalNumericalTable extends JTable implements NumericalFormatStyle, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    public static final String TABLE_EMPTY = "Table empty";

    private final Preferences pref = Preferences.userRoot().node(getModel().getClass().getName());	

    private final boolean addDefaultSorter;

    public MinimalNumericalTable(NumericalTableModel model, boolean addDefaultSorter, boolean addPopup)
    {
        super(Validation.requireNonNullParameterName(model, "model"));

        this.addDefaultSorter = addDefaultSorter;

        if(addDefaultSorter)
        {
            setRowSorter(new TableRowSorter<>(model));
        }
        if(addPopup)
        {
            JPopupMenu menu = buildCopyAndSelectAllPopupMenu();
            initMouseListener(menu);
        }

        model.addPropertyChangeListener(this);
    }

    @Override
    public NumericalTableModel getModel()
    {
        TableModel model = super.getModel();
        return (NumericalTableModel)model;
    }

    @Override
    public void setModel(TableModel modelNew)
    {
        if(!(modelNew instanceof NumericalTableModel))
        {
            throw new IllegalArgumentException("Standard numerical table requires NumericalTableModel");
        }

        NumericalTableModel modelOld = getModel();
        boolean emptyOld = (modelOld != null) ? modelOld.isEmpty() : true;

        if(modelOld!= null)
        {
            modelOld.removePropertyChangeListener(this);
        }

        super.setModel(modelNew);
        ((NumericalTableModel)modelNew).addPropertyChangeListener(this);

        if(addDefaultSorter)
        {
            setRowSorter(new TableRowSorter<>(getModel()));
        }

        boolean emptyNew = isEmpty();
        if(emptyNew != emptyOld)
        {
            firePropertyChange(TABLE_EMPTY, emptyOld, emptyNew);
        }
    }

    public List<IdentityTag> getColumnShortNames()
    {
        return getModel().getColumnShortNames();
    }

    @Override
    public int getMaximumFractionDigits()
    {
        return getDecimalCellRenderer().getMaximumFractionDigits();
    }

    @Override
    public void setMaximumFractionDigits(int n)
    {
        getDecimalCellRenderer().setMaximumFractionDigits(n);
        resizeAndRepaint();
    }

    @Override
    public boolean isShowTrailingZeroes()
    {
        return getDecimalCellRenderer().isShowTrailingZeroes();
    }

    @Override
    public void setShowTrailingZeroes(boolean show)
    {
        getDecimalCellRenderer().setShowTrailingZeroes(show);
        resizeAndRepaint();
    }

    @Override
    public boolean isGroupingUsed()
    {
        return getDecimalCellRenderer().isGroupingUsed();
    }

    @Override
    public void setGroupingUsed(boolean used)
    {
        getDecimalCellRenderer().setGroupingUsed(used);
        resizeAndRepaint();
    }

    @Override
    public char getGroupingSeparator()
    {
        return getDecimalCellRenderer().getGroupingSeparator();
    }

    @Override
    public void setGroupingSeparator(char separator)
    {
        getDecimalCellRenderer().setGroupingSeparator(separator);
        resizeAndRepaint();
    }

    @Override
    public char getDecimalSeparator()
    {
        return getDecimalCellRenderer().getDecimalSeparator();
    }

    @Override
    public void setDecimalSeparator(char separator)
    {
        getDecimalCellRenderer().setDecimalSeparator(separator);
        resizeAndRepaint();
    }

    @Override
    public void saveToPreferences()
    {
        getDecimalCellRenderer().saveToPreferences();
    }

    @Override
    public FormattableNumericalDataState getState()
    {
        return getDecimalCellRenderer().getState();
    }

    @Override
    public void setState(FormattableNumericalDataState memento)
    {
        getDecimalCellRenderer().setState(memento);
        resizeAndRepaint();
    }

    @Override
    public DecimalFormat getDecimalFormat()
    {
        return getDecimalCellRenderer().getDecimalFormat();
    }

    @Override
    public void addListener(FormattableNumericalDataListener listener) {
        getDecimalCellRenderer().addListener(listener);
    }

    @Override
    public void removeListener(FormattableNumericalDataListener listener) {
        getDecimalCellRenderer().removeListener(listener);
    }

    protected abstract DecimalTableCellRenderer getDecimalCellRenderer();

    public void setSaved(boolean saved)
    {	
    }

    public File getDefaultOutputDirectory()
    {
        return getModel().getDefaultOutputDirectory();
    }

    public boolean isEmpty()
    {
        return getModel().isEmpty();
    }

    public Object getValueForSave(int viewRow, int viewColumn)
    {
        int modelRow = convertRowIndexToModel(viewRow);
        int modelColumn = convertColumnIndexToModel(viewColumn);
        return getModel().getValueAt(modelRow, modelColumn);
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    protected void setColumnWidthToDefault()
    {	
        TableColumnModel columnModel = getColumnModel();

        int n = columnModel.getColumnCount();
        for (int i = 0; i<n; i++) 
        {
            TableColumn column = columnModel.getColumn(i);

            TableCellRenderer renderer = column.getHeaderRenderer();
            if (renderer == null) 
            {
                JTableHeader header = getTableHeader();
                if(header != null)
                {
                    renderer = header.getDefaultRenderer();             
                }
                else
                {
                    return;
                }
            }

            Component comp = renderer.getTableCellRendererComponent(this, column.getHeaderValue(), false, false, 0, 0);
            int width = (int)comp.getPreferredSize().getWidth();
            column.setMinWidth(width);	
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    };

    protected void initMouseListener(final JPopupMenu popup)
    {
        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)  {check(e);}

            @Override
            public void mouseReleased(MouseEvent e) {check(e);}

            private void check(MouseEvent e) 
            {
                if (e.isPopupTrigger()) 
                {
                    int clicked = rowAtPoint(e.getPoint());
                    if(!isRowSelected(clicked))
                    {
                        clearSelection();
                        setRowSelectionInterval(clicked, clicked);
                    }	
                    popup.show(MinimalNumericalTable.this, e.getX(), e.getY());	
                }
            }
        };
        addMouseListener(listener);
    }

    protected JPopupMenu buildCopyAndSelectAllPopupMenu() 
    {
        final JPopupMenu popup = new JPopupMenu();

        JMenuItem itemCopy = new JMenuItem("Copy");
        JMenuItem itemSelectAll = new JMenuItem("Select all");

        final Action copy = getActionMap().get("copy");
        final Action selectAll = getActionMap().get("selectAll");

        itemCopy.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0) 
            {
                ActionEvent event = new ActionEvent(MinimalNumericalTable.this, ActionEvent.ACTION_PERFORMED, "");
                copy.actionPerformed(event);
            }      
        });

        itemSelectAll.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0) 
            {
                ActionEvent event = new ActionEvent(MinimalNumericalTable.this, ActionEvent.ACTION_PERFORMED, "");
                selectAll.actionPerformed(event);
            }      
        });

        itemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

        popup.add(itemCopy);   
        popup.add(itemSelectAll);

        return popup;      
    }

    //UGLY SOLUTION!
    //this is necessary because of persistent problem with shrinking columns (width decreased)
    //caused by changes in the TableModel
    @Override
    public void tableChanged(TableModelEvent evt)
    {
        super.tableChanged(evt);
        setColumnWidthToDefault();
    }
}
