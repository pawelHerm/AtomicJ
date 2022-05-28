
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

import javax.swing.*;

import atomicJ.sources.ChannelSource;

import java.awt.*;


public class SourceListCellRenderer extends JLabel implements ListCellRenderer<ChannelSource> 
{
    private static final long serialVersionUID = 1L;

    private boolean useLongName;

    public SourceListCellRenderer(boolean useLongName) 
    {
        setOpaque(true);
        this.useLongName = useLongName;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChannelSource> list, ChannelSource value, int index, boolean isSelected, boolean cellHasFocus)
    {
        String text = useLongName ? value.getLongName() : value.getShortName();

        setText(text);
        setEnabled(isSelected);

        return this;
    }


    public void setUseLongName(boolean useLongName)
    {
        this.useLongName = useLongName;
    }
}
