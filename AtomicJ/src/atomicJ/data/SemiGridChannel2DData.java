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

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.units.Quantity;

public class SemiGridChannel2DData extends AbstractArrayChannel2DData<ArraySupport2D>
{
    public SemiGridChannel2DData(double[][] gridData, ArraySupport2D grid, Quantity zQuantity)
    {
        super(gridData, grid, zQuantity);
    }

    public SemiGridChannel2DData(SemiGridChannel2DData that)
    {
        super(that);
    }

    @Override
    public SemiGridChannel2DData getCopy()
    {
        return new SemiGridChannel2DData(this);
    }

    @Override
    public Grid2D getDefaultGriddingGrid()
    {
        ArraySupport2D grid = getGrid();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double xIncrement = columnCount > 1 ? grid.getDomainLength()/(grid.getColumnCount() - 1) : 1;
        double yIncrement = rowCount > 1 ? grid.getRangeLength()/(grid.getRowCount() - 1) : 1;

        Grid2D defaultRegularGrid = new Grid2D(xIncrement, yIncrement, grid.getXOrigin(), grid.getYOrigin(), grid.getRowCount(), grid.getColumnCount(),
                grid.getXQuantity(), grid.getYQuantity());

        return defaultRegularGrid;
    }

    @Override
    public GridChannel2DData getDefaultGridding()
    {
        return getGridding(getDefaultGriddingGrid());
    }

    @Override
    public GridChannel2DData getGridding(Grid2D gridNew)
    {
        InterpolationMethod2D interpolationMethod = InterpolationMethod2D.BILINEAR;

        int rowCount = gridNew.getRowCount();
        int columnCount = gridNew.getColumnCount();

        double[][] resizedData = interpolationMethod.getGriddedData(this, rowCount, columnCount);

        GridChannel2DData resizedChannelData = new GridChannel2DData(resizedData, gridNew, getZQuantity());

        return resizedChannelData;
    }
}

