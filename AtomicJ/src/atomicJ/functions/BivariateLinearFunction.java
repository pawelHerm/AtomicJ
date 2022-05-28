
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

package atomicJ.functions;

import atomicJ.statistics.BivariateFunction;

public class BivariateLinearFunction implements BivariateFunction
{
    private final double intercept;
    private final double slopeX;
    private final double slopeY;

    public BivariateLinearFunction(double slopeX, double slopeY, double intercept)
    {
        this.slopeX = slopeX;
        this.slopeY = slopeY;
        this.intercept = intercept;
    }

    public int getDomainDimension()
    {
        return 2;
    }

    public BivariateLinearFunction add(BivariateLinearFunction f)
    {
        if(this.getDomainDimension() != f.getDomainDimension())
        {
            throw new IllegalArgumentException("The dimensions of functions must be equal");
        }

        double slopeXNew = this.slopeX + f.slopeX;
        double slopeYNew = this.slopeY + f.slopeY;

        double interceptNew = this.intercept + f.intercept;

        BivariateLinearFunction sum = new BivariateLinearFunction(slopeXNew, slopeYNew, interceptNew);
        return sum;
    }

    public BivariateLinearFunction subtract(BivariateLinearFunction f)
    {
        return add(f.multiply(-1));
    }

    @Override
    public BivariateLinearFunction multiply(double k)
    {
        double slopeXnew = k*this.slopeX;
        double slopeYNew = k*this.slopeY;

        BivariateLinearFunction f = new BivariateLinearFunction(slopeXnew, slopeYNew, k*intercept);
        return f;
    }

    @Override
    public double value(double x, double y)
    {
        double result = intercept + slopeX*x + slopeY*y;		
        return result;
    }

    @Override
    public boolean isZero()
    {
        if(intercept != 0 || slopeX != 0 || slopeY != 0)
        {
            return false;
        }

        return true;
    }
}
