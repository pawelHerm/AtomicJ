
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

import atomicJ.data.units.Quantity;


public class ArrayChannel2DUtilities 
{
    private ArrayChannel2DUtilities(){};

    public static ArraySupport2D getGrid(DataAxis1D xAxis, DataAxis1D yAxis)
    {
        if(xAxis instanceof Grid1D && yAxis instanceof Grid1D)
        {
            Grid2D grid2D = new Grid2D((Grid1D)xAxis, (Grid1D)yAxis);
            return grid2D;
        }

        return new SemiGrid2D2(xAxis, yAxis);
    }

    public static ArrayChannel2DData buildChannel(ArraySupport2D grid, double[][] arrayData, Quantity zQuantity)
    {
        if(grid instanceof Grid2D)
        {
            return new GridChannel2DData(arrayData, (Grid2D)grid, zQuantity);
        }

        return new SemiGridChannel2DData(arrayData, grid, zQuantity);
    }

    public static ArrayChannel2DData buildChannel(DataAxis1D xAxis, DataAxis1D yAxis, double[][] arrayData, Quantity zQuantity)
    {
        if(xAxis instanceof Grid1D && yAxis instanceof Grid1D)
        {
            Grid2D grid2D = new Grid2D((Grid1D)xAxis, (Grid1D)yAxis);
            return new GridChannel2DData(arrayData, grid2D, zQuantity);
        }

        SemiGrid2D2 grid = new SemiGrid2D2(xAxis, yAxis);
        return new SemiGridChannel2DData(arrayData, grid, zQuantity);
    }
}
