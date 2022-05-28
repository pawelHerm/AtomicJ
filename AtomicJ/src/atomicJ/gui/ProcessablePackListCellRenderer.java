
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

import atomicJ.analysis.ProcessableSpectroscopyPack;
import atomicJ.sources.SimpleSpectroscopySource;
import java.awt.*;


public class ProcessablePackListCellRenderer extends JLabel implements ListCellRenderer<ProcessableSpectroscopyPack> 
{
    private static final long serialVersionUID = 1L;

    private boolean useLongName;

    public ProcessablePackListCellRenderer(boolean useLongName) 
    {
        setOpaque(true);
        this.useLongName = useLongName;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ProcessableSpectroscopyPack> list, ProcessableSpectroscopyPack value, int index, boolean isSelected, boolean cellHasFocus)
    {
        SimpleSpectroscopySource source = value.getSourceToProcess();

        String text = useLongName ? source.getLongName() : source.getShortName();

        setText(text);
        setEnabled(isSelected);

        return this;
    }


    public void setUseLongName(boolean useLongName)
    {
        this.useLongName = useLongName;
    }
}
