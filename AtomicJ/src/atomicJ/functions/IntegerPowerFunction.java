
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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

import java.util.Random;

import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.utilities.MathUtilities;

public final class IntegerPowerFunction extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double a;
    private final int exp;

    public IntegerPowerFunction(double a, int exp)
    {
        this.a = a;
        this.exp = exp;
    }

    @Override
    public IntegerPowerFunction multiply(double s)
    {
        return new IntegerPowerFunction(s*a, exp);
    }

    @Override
    public FittedLinearUnivariateFunction getDerivative(int n)
    {
        if(n < 0)
        {
            throw new IllegalArgumentException("Negative derivative " + n);
        }

        if(n == 0)
        {
            return this;
        }

        if(n > this.exp)
        {
            return new Constant(0);
        }

        int expDer = this.exp - n;
        double aDer = this.a*MathUtilities.fallingFactorial(this.exp, n);

        return new IntegerPowerFunction(aDer, expDer);
    }

    @Override
    public double value(double x) 
    {
        double v = a*MathUtilities.intPow(x, exp);
        return v;
    }

    @Override
    public double getCoefficient(Number n) 
    {
        Double exp = n.doubleValue();
        if(this.exp == exp){return a;}
        else {return 0;}
    }

    public double getCoefficient() 
    {
        return a;
    }

    public double getExponent()
    {
        return exp;
    }

    @Override
    public int getEstimatedParameterCount()
    {
        return 1;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {a, exp};
    }  


    public static class IntegerPowerFunctionExactFitFactory implements OneParameterExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 

        private final int exp;

        private IntegerPowerFunctionExactFitFactory(int exp) 
        {
            this.exp = exp;
        };

        public static IntegerPowerFunctionExactFitFactory getInstance(int exp)
        {
            return new IntegerPowerFunctionExactFitFactory(exp);
        }

        @Override
        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, points, 0, points.length);
        }

        @Override
        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to) 
        {
            IntegerPowerFunction exactFit = null;
            int n = to - from;
            double[] p = points[random.nextInt(n) + from];
            double x0 = p[0];
            double y0 = p[1];  

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                exactFit = new IntegerPowerFunction(a,exp);
            }

            return exactFit;
        }

        @Override
        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            IntegerPowerFunction exactFit = null;
            int n1 = pointsA.length;
            int drawn = random.nextInt(n);
            double[] p = drawn < n1 ? pointsA[drawn] : pointsB[drawn - n1];
            double x0 = p[0];
            double y0 = p[1];  

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                exactFit = new IntegerPowerFunction(a,exp);
            }

            return exactFit;
        }

        @Override
        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            IntegerPowerFunction exactFit = null;
            int n1 = pointsAYs.length;
            int drawn = random.nextInt(n);
            double x0 = drawn < n1 ? pointsAXs[drawn] : pointsBXs[drawn - n1];
            double y0 = drawn < n1 ? pointsAYs[drawn] : pointsBYs[drawn - n1];  

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                exactFit = new IntegerPowerFunction(a,exp);
            }

            return exactFit;
        }

        @Override
        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            int n = to - from;
            int i = random.nextInt(n) + from;
            double x0 = xs[i];
            double y0 = ys[i];  

            IntegerPowerFunction exactFit = null;
            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                exactFit = new IntegerPowerFunction(a,exp);
            }

            return exactFit;
        }

        @Override
        public FittedLinearUnivariateFunction getExactFit(double x0, double y0) 
        {
            IntegerPowerFunction exactFit = null;
            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                exactFit = new IntegerPowerFunction(a,exp);
            }

            return exactFit;
        }

        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, 0, n);
        }

        public IntegerPowerFunction getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int from, int to) 
        {
            IntegerPowerFunction exactFit = null;
            int n = to - from;
            int x0 = random.nextInt(n) + from;
            double y0 = ys[x0];    

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                exactFit = new IntegerPowerFunction(a,exp);              
            }

            return exactFit;
        }

        public static boolean accepts(double[] modelSorted)
        {
            boolean accepts = modelSorted.length == 1 && (modelSorted[0] >= 0);
            return accepts;
        }

        @Override
        public int getEstimatedParameterCount()
        {
            return 1;
        }
    }
}
