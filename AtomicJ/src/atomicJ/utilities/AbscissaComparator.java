
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

public class AbscissaComparator implements Comparator<double[]>
{
    /**
     * Returns a negative integer, zero, or a 
     * positive integer as the first point abscissa (i.e. x) is 
     * less than, equal to, or greater than the second's.
     */
    @Override
    public int compare(double[] p1, double[] p2) 
    {
        double x1 = p1[0];
        double x2 = p2[0];
        if(x1 > x2){return 1;}
        else if(x1 < x2){return -1;}		
        else {return 0;}
    }
}
