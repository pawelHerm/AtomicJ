
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

package atomicJ.gui.results;

import static atomicJ.gui.PreferenceKeys.SOURCE_NAME_LONG;

import java.util.prefs.Preferences;
import javax.swing.table.DefaultTableCellRenderer;

import atomicJ.analysis.Processed1DPack;
import atomicJ.sources.Channel1DSource;


public class ProcessedPackCellRenderer extends DefaultTableCellRenderer 
{
    private static final long serialVersionUID = 1L;

    private boolean useLongName;

    public ProcessedPackCellRenderer(Preferences pref) 
    {
        super();
        setUseLongName(pref.getBoolean(SOURCE_NAME_LONG, true));
    }   

    @Override
    public void setValue(Object entry)
    {
        String text;
        if (entry instanceof Processed1DPack<?,?>) 
        {
            Processed1DPack<?,?> pack = (Processed1DPack<?,?>)entry;
            Channel1DSource<?> source = pack.getSource();
            text = useLongName ? source.getLongName() : source.getShortName();
        }
        else
        {
            text = entry.toString();
        }

        setText(text);
    }

    public void setUseLongName(boolean useLongName)
    {
        this.useLongName = useLongName;
    }
}
