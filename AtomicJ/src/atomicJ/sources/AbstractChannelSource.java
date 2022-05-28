
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

package atomicJ.sources;

import java.io.File;

import atomicJ.data.Channel;
import atomicJ.resources.AbstractResource;

public abstract class AbstractChannelSource<E extends Channel> extends AbstractResource implements ChannelSource
{
    public AbstractChannelSource(File f)
    {
        super(f);
    }

    public AbstractChannelSource(String pathname)
    {
        super(pathname);
    }

    public AbstractChannelSource(File f, String shortName, String longName)
    {
        super(f, shortName, longName);
    }

    public AbstractChannelSource(String pathname, String shortName, String longName)
    {        
        super(pathname, shortName, longName);
    }

    public AbstractChannelSource(AbstractChannelSource<E> sourceFile)
    {        
        super(sourceFile);
    }

    @Override
    public File getCorrespondingFile()
    {
        return getDefaultOutputLocation();
    }

    @Override
    public String getChannelUniversalIdentifier(String identifier)
    {
        String universalIdentifier = identifier + getShortName();
        return universalIdentifier;
    }

    @Override
    public String toString()
    {
        return getLongName();
    }

    @Override
    public int hashCode()
    {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object ob)
    {
        boolean equal = (this == ob);
        return equal;
    }
}
