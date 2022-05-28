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

import org.apache.commons.math.analysis.BivariateRealFunction;

public interface ArrayChannel2DData extends Channel2DData
{
    public ArraySupport2D getGrid();
    public double[][] getData();
    public double[][] getDataCopy(); 
    public double getZ(int row, int column);
    public double[] getRow(int rowIndex);
    public double[] getColumn(int columnIndex);
    public BivariateRealFunction getBicubicSplineInterpolationFunction();
}

