
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Pawe³ Hermanowicz
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

package atomicJ.readers.asylum;

import java.nio.ByteBuffer;
import java.util.Date;

import atomicJ.data.Grid2D;
import atomicJ.data.units.PrefixedUnit;

public interface WaveHeader
{
    public Date getCreationDate();
    public Date getModificationDate();

    public double getDimensionLength(int level);
    public String getDimensionUnit(int level);
    public int getDimensionItemCount(int level);

    public PrefixedUnit getYSIUnit();   
    public PrefixedUnit getXSIUnit();
    public Grid2D getGrid();

    public String getDataUnit();

    public boolean canBeImage();
    public int getLayerCount();

    public int getWaveDataByteCount();
    public int getTotalNumberOfPoints();
    public IgorWaveDataType getDataType();
    public double[][][] readIn(ByteBuffer buffer);
    public double[][][] readInRowByRow(ByteBuffer buffer);
    public int getDataDimensionCount();
}