
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

public class EuclideanMetrics implements Metrics
{
    @Override
    public double distance(double[] p1, double[] p2)
    {
        int n = p1.length;
        int m = p2.length;

        if(n != m){throw new IllegalArgumentException();}
        else
        {
            double sumSquares = 0;
            for(int i = 0;i<n;i++)
            {
                double x1 = p1[i];
                double x2 = p2[i];
                double diff = x2 - x1;
                sumSquares = sumSquares + diff*diff;
            }
            double dist = Math.sqrt(sumSquares);
            return dist;
        }
    }
}
