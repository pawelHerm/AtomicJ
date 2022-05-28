
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

import java.util.Arrays;

import atomicJ.utilities.Selector;

public class ResidualVector 
{
    private final double[] residuals;

    public ResidualVector(double[] residuals)
    {
        this.residuals = residuals;
    }

    public static ResidualVector getInstance(double[][] points, int to, FittedLinearUnivariateFunction fit)
    {
        double[] residualsData = new double[to];
        for(int i = 0;i<to;i++)
        {
            residualsData[i] = fit.residual(points[i]);
        }      

        ResidualVector instance = new ResidualVector(residualsData);
        return instance;
    }

    public static ResidualVector getInstance(double[][] points, int from, int to, FittedLinearUnivariateFunction fit)
    {
        int n = to - from;
        double[] residualsData = new double[n];
        for(int i = from;i<to;i++)
        {
            residualsData[i - from] = fit.residual(points[i]);
        }      

        ResidualVector instance = new ResidualVector(residualsData);
        return instance;
    }

    public static ResidualVector getInstance(double[][] pointsA, double[][] pointsB, FittedLinearUnivariateFunction fit)
    {
        int n1 = pointsA.length;
        int n2 = pointsB.length;
        int n = n1 + n2;
        double[] residualsData = new double[n];
        for(int i = 0;i<n1;i++)
        {
            residualsData[i] = fit.residual(pointsA[i]);
        }      

        for(int i = n1;i<n;i++)
        {
            residualsData[i] = fit.residual(pointsB[i - n1]);
        } 

        ResidualVector instance = new ResidualVector(residualsData);
        return instance;
    }

    public static ResidualVector getInstance(double[] ys, double[] xs,  int to, FittedLinearUnivariateFunction fit)
    {
        return getInstance(ys, xs, 0, to, fit);
    }

    public static ResidualVector getInstance(double[] ys, double[] xs, int from, int to, FittedLinearUnivariateFunction fit)
    {
        int n = to - from;
        double[] residualsData = new double[n];
        for(int i = from;i<to;i++)
        {
            residualsData[i - from] = fit.residual(xs[i], ys[i]);
        }      

        ResidualVector instance = new ResidualVector(residualsData);
        return instance;
    }

    public static ResidualVector getInstance(double[] ys, int to, FittedLinearUnivariateFunction fit)
    {
        return getInstance(ys, 0, to, fit);
    }

    public static ResidualVector getInstance(double[] ys, int from, int to, FittedLinearUnivariateFunction fit)
    {
        int n = to - from;
        double[] residualsData = new double[n];
        for(int i = from;i<to;i++)
        {
            residualsData[i - from] = fit.residual(i, ys[i]);
        }      

        ResidualVector instance = new ResidualVector(residualsData);
        return instance;
    }

    public double[] getResiduals()
    {
        return residuals;
    }

    public double[] getResidualsCopy()
    {
        return Arrays.copyOf(residuals, residuals.length);
    }

    public double[] getAbsoluteResiduals()
    {
        int n = residuals.length;

        double[] absResiduals = new double[n];
        for(int i = 0;i<n;i++)
        {
            absResiduals[i] = Math.abs(residuals[i]);
        }
        return absResiduals;
    }

    public double[] getAbsoluteResiduals(int from, int to)
    {
        int n = to - from;

        double[] absResiduals = new double[n];        

        for(int i = from; i<to; i++)
        {
            double r = Math.abs(residuals[i]);
            absResiduals[i - from] = r;
        }

        return absResiduals;
    }

    public double getSquaresSum()
    {
        return getSquaresSum(residuals);
    }

    public static double getSquaresSum(double[] residuals)
    {
        double norm = 0;
        double n = residuals.length;
        for (int i = 0; i < n; i++) 
        {
            double x = residuals[i];
            norm += x*x;
        }
        return norm;
    }

    public double getAbsoluteValuesSum()
    {
        return getAbsoluteValuesSum(residuals);
    }  

    public static double getAbsoluteValuesSum(double[] residuals)
    {
        double norm = 0;
        int n = residuals.length;
        for (int i = 0; i < n; i++) 
        {
            double x = residuals[i];
            norm = Math.abs(x);
        }
        return norm;
    }  

    public double getInfNorm()
    {
        return getInfNorm(residuals);
    }

    public static double getInfNorm(double[] residuals)
    {
        double norm = 0;
        double n = residuals.length;
        for (int i = 0; i < n; i++) 
        {
            double x = Math.abs(residuals[i]);
            norm = Math.max(norm,x);
        }
        return norm;
    }

    public double getAbsoluteRankStatistic(int k)
    {
        return getAbsoluteRankStatistic(residuals, k);
    }

    public static double getAbsoluteRankStatistic(double[] residuals, int k)
    {
        int n = residuals.length;

        double[] absResiduals = new double[n];
        for(int i = 0;i<n;i++)
        {
            absResiduals[i] = Math.abs(residuals[i]);
        }
        Selector.sortSmallest(absResiduals, k);
        return absResiduals[k];
    }
}
