
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

import java.util.Set;

import atomicJ.gui.PropertyChangeSource;

public interface SingleSelectionModel<E> extends PropertyChangeSource
{   
    //if the set of keys available for selection changes, the model should fire a property change event
    // and the name of the property should be KEY_SET_CHANGED
    public static final String KEY_SET_CHANGED = "KEY_SET_CHANGED";
    public static final String SELECTED_KEY = "SELECTED_KEY";
    public String getSelectionName();

    public Set<E> getKeys();

    public E getSelectedKey();
    public void setSelectedKey(E key);

    public Set<E> getLeftOutKeys();
    public boolean isSelected(E key);	
}
