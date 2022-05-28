
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

import java.awt.Font;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;

public class ItemList <E> extends JList<E>
{
    private static final long serialVersionUID = 1L;

    public ItemList()
    {
        super(new FastDefaultListModel<E>());
        setLayoutOrientation(JList.VERTICAL);
        setFont(new Font("Monospaced",Font.PLAIN,12));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public boolean isNonEmpty()
    {
        return !getModel().isEmpty();
    }

    public boolean containsItem(Object resource)
    {
        FastDefaultListModel<E> model = getModel();
        boolean contains = model.contains(resource);
        return contains;
    }

    public boolean setSelectedItem(Object item, boolean shouldScroll)
    {
        if(containsItem(item))
        {
            super.setSelectedValue(item, shouldScroll);
            return true;
        }
        return false;
    }

    public boolean setSelectedItem(int index, boolean shouldScroll)
    {
        if(index<getItemCount())
        {
            super.setSelectedIndex(index);
            if(shouldScroll)
            {
                ensureIndexIsVisible(index);
            }
            return true;
        }
        return false;
    }

    public void setItems(List<E> items)
    {
        FastDefaultListModel<E> model = getModel();

        setValueIsAdjusting(true);

        model.clear();
        addItems(items);

        setValueIsAdjusting(false);
    }

    public void addItem(E item)
    {
        FastDefaultListModel<E> model = getModel();	
        model.addElement(item);
    }

    public void addItems(List<? extends E> items)
    {
        FastDefaultListModel<E> model = getModel();
        model.addAll(items);
    }

    public void deleteItems(List<? extends E> item)
    {        
        FastDefaultListModel<E> model = getModel();

        setValueIsAdjusting(true);
        model.removeElements(item);           
        setValueIsAdjusting(false);
    }

    public void deleteSelectedItems()
    {
        List<E> selectedElements = getSelectedValuesList();
        deleteItems(selectedElements);
    }

    public void deleteItemtAt(int i)
    {
        FastDefaultListModel<E> model = getModel();
        model.removeElementAt(i);
    }

    public void clear()
    {
        FastDefaultListModel<E> model = getModel();
        model.clear();
    }

    public List<E> getItems()
    {
        FastDefaultListModel<E> model = getModel();
        return model.getElements();
    }

    public E getElementAt(int index)
    {
        FastDefaultListModel<E> model = getModel();

        return model.getElementAt(index);
    }

    public E set(int index, E element)
    {
        FastDefaultListModel<E> model = getModel();

        return model.set(index, element);
    }

    public int getItemCount()
    {
        FastDefaultListModel<E> model = getModel();
        return model.getSize();
    }

    public int getIndexOf(Object element)
    {
        FastDefaultListModel<E> model = getModel();
        return model.indexOf(element);
    }

    public void addListDataListenerToModel(ListDataListener listener)
    {
        FastDefaultListModel<E> model = getModel();
        model.addListDataListener(listener);
    }

    @Override
    public FastDefaultListModel<E> getModel()
    {
        FastDefaultListModel<E> model = (FastDefaultListModel<E>)super.getModel();
        return model;
    }

    @Override
    public void setModel(ListModel<E> model)
    {
        if(!(model instanceof FastDefaultListModel))
        {
            throw new IllegalArgumentException();
        }

        super.setModel(model);
    }

    @Override
    public void setSelectedIndices(int[] indices) 
    {        
        setValueIsAdjusting(true);

        ListSelectionModel sm = getSelectionModel();
        sm.clearSelection();
        int size = getModel().getSize();
        for(int i = 0; i < indices.length; i++)
        {
            if (indices[i] < size) 
            {
                sm.addSelectionInterval(indices[i], indices[i]);
            }
        }

        setValueIsAdjusting(false);
    }
}
