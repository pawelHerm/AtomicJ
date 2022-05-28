
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

package atomicJ.gui.histogram;

import java.io.File;
import javax.swing.filechooser.FileSystemView;

public class HistogramResourceIdentifier
{
    private final static File defaultDir = FileSystemView.getFileSystemView().getDefaultDirectory();

    private final String shortName;
    private final String longName;

    private final File defaultOutputLocation;

    public HistogramResourceIdentifier( String shortName, String longName)
    {
        this(shortName, longName, defaultDir);
    }
    public HistogramResourceIdentifier(String shortName, String longName, File defaultOutputLocation)
    {
        this.shortName = shortName;
        this.longName = longName;

        this.defaultOutputLocation = defaultOutputLocation;
    }

    public String getHistogramName()
    {
        return shortName;
    }

    @Override
    public String toString()
    {
        return shortName;
    }

    public String getShortName() 
    {
        return shortName;
    }

    public String getLongName()
    {
        return longName;
    }


    public File getDefaultOutputLocation() 
    {
        return defaultOutputLocation;
    }
}
