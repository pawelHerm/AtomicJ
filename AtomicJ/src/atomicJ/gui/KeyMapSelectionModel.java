
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.gui.selection.multiple.BasicMultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;

public class KeyMapSelectionModel <V, E> extends BasicMultipleSelectionWizardPageModel<E> implements MultipleSelectionWizardPageModel<E>
{
    private final Map<E, V> keyValueMap;

    public KeyMapSelectionModel(Map<E, V> keyValueMap, String selectionName, 
            String taskName, String taskDescription, boolean isFirst, boolean isLast)
    {
        this(keyValueMap, selectionName, taskName, taskDescription, isFirst, isLast, false);
    }

    public KeyMapSelectionModel(Map<E, V> keyValueMap, String selectionName, String taskName, String taskDescription,
            boolean isFirst, boolean isLast, boolean acceptEmptyResult)
    {
        super(keyValueMap.keySet(), selectionName, taskName, taskDescription, isFirst, isLast, acceptEmptyResult);

        this.keyValueMap = keyValueMap;
    }

    public Map<E, V> getIncludedKeyValueMap()
    {
        Map<E, V> includedKeyValueMap = new LinkedHashMap<>();

        for(Entry<E, V> entry : keyValueMap.entrySet())
        {
            E key = entry.getKey();
            boolean isIncluded = isSelected(key);
            if(isIncluded)
            {
                V value = entry.getValue();
                includedKeyValueMap.put(key, value);
            }
        }

        return includedKeyValueMap;
    }
}
