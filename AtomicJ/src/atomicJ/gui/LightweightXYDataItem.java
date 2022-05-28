
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

import java.io.Serializable;

public class LightweightXYDataItem implements Cloneable, Comparable<LightweightXYDataItem>, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1L;

    private final double x;
    private final double y;

    public LightweightXYDataItem(double x, double y) {

        this.x = x;
        this.y = y;
    }

    public double getX() 
    {
        return this.x;
    }

    public double getY() 
    {
        return y; 
    }

    @Override
    public int compareTo(LightweightXYDataItem that) 
    {         
        if (x > that.getX()) 
        {
            return 1;
        }
        else if (x < that.getX()) 
        {
            return -1;
        }
        else 
        {
            return 0;
        }
    }

    @Override
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        }
        catch (CloneNotSupportedException e) 
        { 
            e.printStackTrace();
        }
        return clone;
    }

}
