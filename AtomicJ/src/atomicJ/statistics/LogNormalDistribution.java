
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

package atomicJ.statistics;

import org.apache.commons.math.distribution.*;

import java.io.Serializable;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.special.Erf;

public class LogNormalDistribution extends AbstractContinuousDistribution implements Serializable {

    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;

    private static final long serialVersionUID = 1L;

    private static final double SQRT2PI = Math.sqrt(2 * Math.PI);
    private double location = 0;
    private double scale = 1;
    private final double solverAbsoluteAccuracy;

    public LogNormalDistribution(double location, double scale){
        this(location, scale, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    public LogNormalDistribution(double location, double scale, double inverseCumAccuracy) {
        super();
        setLocationParameterInternal(location);
        setScaleParameterInternal(scale);
        solverAbsoluteAccuracy = inverseCumAccuracy;
    }

    public LogNormalDistribution(){
        this(0.0, 1.0);
    }


    public double getLocationParameter() {
        return location;
    }

    private void setLocationParameterInternal(double location) {
        this.location = location;
    }

    public double getScaleParameter() {
        return scale;
    }

    private void setScaleParameterInternal(double scale) 
    {
        if (scale <= 0.0) 
        {
            throw MathRuntimeException.createIllegalArgumentException(
                    "standard deviation must be positive ({0})",
                    scale);
        }
        this.scale = scale;
    }

    @Override
    public double density(double x) {
        double x0 = Math.log(x) - location;
        return Math.exp(-x0 * x0 / (2 * scale * scale)) / (x*scale * SQRT2PI);
    }

    @Override
    public double cumulativeProbability(double x) throws MathException 
    {
        double logX = Math.log(x);
        double x0 = logX - location;

        try 
        {
            return 0.5 * (Erf.erfc(-x0 /(scale * Math.sqrt(2.0))));
        } 
        catch (MaxIterationsExceededException ex) 
        {
            if (logX < (location - 20 * scale)) 
            {
                return 0.0;
            } 
            else if (logX > (location + 20 * scale)) 
            {
                return 1.0;
            } 
            else 
            {
                throw ex;
            }
        }
    }


    @Override
    protected double getSolverAbsoluteAccuracy() 
    {
        return solverAbsoluteAccuracy;
    }

    @Override
    public double inverseCumulativeProbability(final double p)
            throws MathException {
        if (p == 0) 
        {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) 
        {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }

    @Override
    protected double getDomainLowerBound(double p) 
    {
        double ret;

        if (p < .5) 
        {
            ret = 0;
        } 
        else 
        {
            ret = Math.exp(location);
        }

        return ret;
    }

    @Override
    protected double getDomainUpperBound(double p) 
    {
        double ret;

        if (p < .5) 
        {
            ret = Math.exp(location);
        } 
        else 
        {
            ret = Double.MAX_VALUE;
        }

        return ret;
    }


    @Override
    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) 
        {
            ret = Math.exp(location - scale);
        } else if (p > .5) 
        {
            ret = Math.exp(location + scale);
        } else 
        {
            ret = Math.exp(location);
        }

        return ret;
    }
}
