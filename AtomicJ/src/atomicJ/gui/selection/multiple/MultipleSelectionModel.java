
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

import java.util.Set;

public interface MultipleSelectionModel<E>
{    
    public String getSelectionName();  
    public void setKeys(Set<E> allKeysNew);
    public Set<E> getKeys();
    public Set<E> getSelectedKeys();
    public Set<E> getLeftOutKeys();
    public boolean areAllKeysSelected();
    public boolean areAllKeysDeselected();
    public boolean isFinishEnabled();
    public boolean isSelected(E key);
    public void setSelected(E key, boolean selectedNew);
    public void setAllSelected(boolean selectedNew);

    //if the set of keys available for selection changes, the model should call the method keySetChanged() on the registered listeners
    //if a key is selected or deselected, the model should call the method keySelectionChanged() on the listeners
    //when the property of all keys being selected changes, the model should call the method allKeysSelectedChanged() on the listeners
    //when the property of all keys being deselected changes, the model should call the method allKeysDeselectedChanged() on the listeners    

    public void addSelectionChangeListener(MultipleSelectionListener<E> listener);
    public void removeSelectionChangeListener(MultipleSelectionListener<E> listener);
}
