package atomicJ.resources;


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


import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import atomicJ.sources.ChannelSource;
import atomicJ.utilities.IOUtilities;


public abstract class AbstractResource implements Resource
{
    public static final String RESOURCE_NAME = "Resource name";

    private final File file;
    private final String shortName;
    private final String longName;

    public AbstractResource(File f)
    {
        this(f, f.getAbsolutePath(), IOUtilities.getBareName(f));
    }

    public AbstractResource(String pathname)
    {
        this.file = new File(pathname);
        this.longName = file.getAbsolutePath();
        this.shortName = file.getName().replaceFirst("[.][^.]+$", "");  
    }

    public AbstractResource(String pathname, String shortName, String longName)
    {        
        this(new File(pathname), shortName, longName);
    }

    public AbstractResource(File f, String shortName, String longName)
    {
        this.file = f;
        this.longName = longName;
        this.shortName = shortName;
    }

    public AbstractResource(AbstractResource sourceFile)
    {        
        this.file = sourceFile.file;
        this.longName = sourceFile.getLongName();
        this.shortName = sourceFile.getShortName();
    }

    @Override
    public String getLongName()
    {
        return longName;
    }

    @Override
    public String getShortName() 
    {
        return shortName;
    }

    @Override 
    public String toString()
    {
        return shortName;
    }

    @Override
    public File getDefaultOutputLocation() 
    {
        return file.getParentFile();
    }

    public boolean containsChannelsFromSource(ChannelSource source) 
    {
        return Objects.equals(this, source);
    }

    @Override
    public Map<String, String> getAutomaticChartTitles()
    {
        Map<String, String> titles = Collections.singletonMap(AbstractResource.RESOURCE_NAME, getShortName());    

        return titles;
    }
}
