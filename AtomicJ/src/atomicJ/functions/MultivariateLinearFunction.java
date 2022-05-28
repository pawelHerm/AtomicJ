
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

import java.util.Arrays;


public class MultivariateLinearFunction implements MultivariateFunction
{
    private final double intercept;
    private final double[] slopes;

    public MultivariateLinearFunction(double[] slopes, double intercept)
    {
        this.slopes = Arrays.copyOf(slopes, slopes.length);
        this.intercept = intercept;
    }

    public int getDomainDimension()
    {
        return slopes.length;
    }

    public MultivariateLinearFunction add(MultivariateLinearFunction f)
    {
        if(this.getDomainDimension() != f.getDomainDimension())
        {
            throw new IllegalArgumentException("The dimensions of functions must be equal");
        }

        int n = slopes.length;
        double[] slopesToAdd = f.slopes;
        double[] slopesNew = new double[n];

        for(int i = 0; i<n; i++)
        {
            double s1 = slopesToAdd[i];
            double s2 = slopes[i];

            slopesNew[i] = s1 + s2;
        }

        double interceptNew = this.intercept + f.intercept;

        MultivariateLinearFunction sum = new MultivariateLinearFunction(slopesNew, interceptNew);
        return sum;
    }

    public MultivariateLinearFunction subtract(MultivariateLinearFunction f)
    {
        return add(f.multiply(-1));
    }

    @Override
    public MultivariateLinearFunction multiply(double k)
    {
        int n = slopes.length;
        double[] slopesNew = new double[n];

        for(int i = 0; i<n; i++)
        {
            double slopeOld = slopes[i];
            slopesNew[i] = k*slopeOld;
        }

        MultivariateLinearFunction f = new MultivariateLinearFunction(slopesNew, k*intercept);
        return f;
    }

    @Override
    public double value(double[] p)
    {
        double result = intercept;
        int n = slopes.length;
        for(int i = 0; i<n; i++)
        {
            double slope = slopes[i];
            double x = p[i];
            result += slope*x;
        }

        return result;
    }

    @Override
    public boolean isZero()
    {
        if(intercept != 0)
        {
            return false;
        }

        for(double slope : slopes)
        {
            if(slope != 0)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof MultivariateLinearFunction)
        {
            MultivariateLinearFunction thatFunction = (MultivariateLinearFunction) that;
            if(this.intercept != thatFunction.intercept)
            {
                return false;
            }
            if(!Arrays.equals(this.slopes, thatFunction.slopes))
            {
                return false;
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}
