
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

package atomicJ.data;

import java.util.*;

public abstract class BasicSpectroscopyCurve<E extends TransformableData1D<E>> implements SpectroscopyCurve<E>
{
    private final E approach;
    private final E withdraw;
    private final String key;
    private final String name;

    public BasicSpectroscopyCurve(E approach, E withdraw, String name)
    {
        this(approach, withdraw, name, name);
    }

    public BasicSpectroscopyCurve(E approach, E withdraw, String key, String name)
    {
        this.approach = approach;
        this.withdraw = withdraw;

        this.key = key;
        this.name = name;
    }

    @Override
    public E getApproach()
    {
        return approach;
    }

    @Override
    public E getWithdraw()
    {
        return withdraw;
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> channels = new ArrayList<>();

        channels.addAll(approach.getChannels());
        channels.addAll(withdraw.getChannels());

        return channels;	
    }

    @Override
    public String getName() 
    {
        return name;
    }

    @Override
    public String getIdentifier() 
    {
        return key;
    }
}
