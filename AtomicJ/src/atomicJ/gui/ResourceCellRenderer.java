
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

import static atomicJ.gui.PreferenceKeys.SOURCE_NAME_LONG;

import java.awt.Component;
import java.util.prefs.Preferences;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import atomicJ.resources.Resource;



public class ResourceCellRenderer extends DefaultListCellRenderer 
{
    private static final long serialVersionUID = 1L;

    private boolean useShortName = true;

    public ResourceCellRenderer(boolean useShortName) 
    {
        setUseShortName(useShortName);
    }

    public ResourceCellRenderer(Preferences pref) 
    {
        setUseShortName(pref.getBoolean(SOURCE_NAME_LONG, false));
    }	


    public String getDisplayedText(Resource resource)
    {
        String text = useShortName ? resource.getShortName() : resource.getLongName();

        return text;
    }

    private String convertToString(Object item)
    {
        String text;

        if (item instanceof Resource) 
        {
            text = getDisplayedText((Resource)item);		
        }
        else
        {
            text = item.toString();
        }

        return text;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {				
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        String text = convertToString(value);			
        setText(text);	

        return this;
    }

    public boolean getUseShortName()
    {
        return useShortName;
    }

    public void setUseShortName(boolean useShortName)
    {
        this.useShortName = useShortName;     
    }
}
