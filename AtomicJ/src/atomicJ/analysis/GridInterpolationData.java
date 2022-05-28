
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

package atomicJ.analysis;

import java.util.Arrays;

import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.Validation;

public class GridInterpolationData 
{
    private final double[] xNodes;
    private final double[] yNodes;
    private final double[][] data;

    public GridInterpolationData(double[] xNodes, double[] yNodes, double[][] data)
    {
        Validation.requireNonNullAndNonEmptyParameterName(xNodes, "xNodes");
        Validation.requireNonNullAndNonEmptyParameterName(yNodes, "yNodes");

        if(data.length != xNodes.length)
        {
            throw new IllegalArgumentException("Number of rows in data array should be equal to the number of x nodes");
        }

        this.xNodes = xNodes;
        this.yNodes = yNodes;
        this.data = data;
    }

    public GridInterpolationData(GridInterpolationData dataOld)
    {
        Validation.requireNonNullParameterName(dataOld, "dataOld");

        this.xNodes = Arrays.copyOf(dataOld.xNodes, dataOld.xNodes.length);
        this.yNodes = Arrays.copyOf(dataOld.yNodes,dataOld.yNodes.length);
        this.data = ArrayUtilities.deepCopy(dataOld.data);
    }

    public double[] getXNodes()
    {
        return Arrays.copyOf(xNodes, xNodes.length);
    }

    public double getXMinimum()
    {
        return xNodes[0];
    }

    public double getXMaximum()
    {
        return xNodes[xNodes.length - 1];
    }

    public double getYMinimum()
    {
        return yNodes[0];
    }

    public double getYMaximum()
    {
        return yNodes[yNodes.length - 1];
    }

    public double getDomainLength()
    {
        double minimum = xNodes[0];
        double maximum = xNodes[xNodes.length - 1];
        double length = maximum - minimum;
        return length;
    }

    public double getRangeLength()
    {
        double minimum = yNodes[0];
        double maximum = yNodes[yNodes.length - 1];
        double length = maximum - minimum;
        return length;
    }

    public double[] getYNodes()
    {
        return Arrays.copyOf(yNodes, yNodes.length);
    }

    public double[][] getSampledValues()
    {
        return ArrayUtilities.deepCopy(data);
    }
}
