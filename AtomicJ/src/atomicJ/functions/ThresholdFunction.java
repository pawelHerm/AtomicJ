
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


public class ThresholdFunction implements MultivariateFunction
{
    private final double lowerValue;	
    private final double upperValue;

    private final double lowerThreshold;
    private final double upperThreshold;

    public ThresholdFunction(double lowerThreshold, double lowerValue, double upperThreshold, double upperValue)
    {
        this.lowerValue = lowerValue;	
        this.upperValue = upperValue;

        this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
    }

    public ThresholdFunction add(ThresholdFunction f)
    {
        ThresholdFunction sum = 
                new ThresholdFunction(lowerThreshold, lowerValue + f.lowerValue, upperThreshold, upperValue + f.upperValue);
        return sum;
    }

    public ThresholdFunction subtract(ThresholdFunction f)
    {
        return add(f.multiply(-1));
    }

    @Override
    public ThresholdFunction multiply(double k)
    {
        ThresholdFunction f = new ThresholdFunction(lowerThreshold, k*lowerValue, upperThreshold, k*upperValue);
        return f;
    }

    @Override
    public double value(double[] p)
    {
        double z = p[p.length - 1];
        if(z>upperThreshold)
        {
            return upperValue;
        }
        else if(z<lowerThreshold)
        {
            return lowerValue;
        }

        return z;
    }

    @Override
    public boolean isZero()
    {
        if(lowerValue != 0 || upperValue != 0)
        {
            return false;
        }

        if(Double.POSITIVE_INFINITY != upperThreshold || Double.NEGATIVE_INFINITY != lowerThreshold)
        {
            return false;
        }

        return true;
    }
}
