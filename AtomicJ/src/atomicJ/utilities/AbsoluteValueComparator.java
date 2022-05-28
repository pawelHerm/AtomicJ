
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

package atomicJ.utilities;

import java.util.Comparator;

public class AbsoluteValueComparator implements Comparator<Double>
{
    public AbsoluteValueComparator(){}

    @Override
    public int compare(Double x1, Double x2) 
    {
        double a1 = Math.abs(x1);
        double a2 = Math.abs(x2);
        if(a1 > a2){return 1;}
        else if(a1<a2){return -1;}		
        else {return 0;}
    }
}