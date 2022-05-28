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

import java.util.ArrayList;
import java.util.List;
import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.units.Quantity;

public class GridChannel2DData extends AbstractArrayChannel2DData<Grid2D> implements ImageMatrix
{
    public GridChannel2DData(double[][] gridData, Grid2D grid, Quantity zQuantity)
    {
        super(gridData, grid, zQuantity);
    }

    public GridChannel2DData(GridChannel2DData that)
    {
        super(that);
    }

    @Override
    public GridChannel2DData getCopy()
    {
        return new GridChannel2DData(this);
    }

    @Override
    public Grid2D getDefaultGriddingGrid()
    {
        return getGrid();
    }

    @Override
    public GridChannel2DData getDefaultGridding()
    {
        return this;
    }

    @Override
    public GridChannel2DData getGridding(Grid2D gridNew)
    {
        Grid2D grid = getGrid();

        if(grid.isEqualUpToPrefixes(gridNew))
        {
            return this;
        }

        InterpolationMethod2D interpolationMethod = InterpolationMethod2D.BILINEAR;

        int rowCount = gridNew.getRowCount();
        int columnCount = gridNew.getColumnCount();

        double[][] resizedData = interpolationMethod.getGriddedData(this, rowCount, columnCount);

        GridChannel2DData resizedChannelData = new GridChannel2DData(resizedData, grid.changeDensity(rowCount, columnCount), getZQuantity());

        return resizedChannelData;
    }


    public static List<GridChannel2DData> getGriddedChannels(List<Channel2DData> channels, Grid2D grid)
    {
        List<GridChannel2DData> allGriddedData = new ArrayList<>();

        for(Channel2DData channelData : channels)
        {
            GridChannel2DData griddedData = channelData.getGridding(grid);
            allGriddedData.add(griddedData);
        }

        return allGriddedData;
    }
}

