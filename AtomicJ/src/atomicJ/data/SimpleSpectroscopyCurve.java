
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

public class SimpleSpectroscopyCurve extends BasicSpectroscopyCurve<Channel1D>
{
    public SimpleSpectroscopyCurve(Channel1D approach, Channel1D withdraw, String name)
    {
        this(approach, withdraw, name, name);
    }

    public SimpleSpectroscopyCurve(Channel1D approach, Channel1D withdraw, String key, String name)
    {
        super(approach, withdraw, key, name);
    }

    @Override
    public SimpleSpectroscopyCurve getCopy(double s)
    {
        Channel1D scaledApproach = getApproach().getCopy(s);
        Channel1D scaledWithdraw = getWithdraw().getCopy(s);
        SimpleSpectroscopyCurve scaledCurve = new SimpleSpectroscopyCurve(scaledApproach, scaledWithdraw, getIdentifier(), getName());

        return scaledCurve;
    }
}
