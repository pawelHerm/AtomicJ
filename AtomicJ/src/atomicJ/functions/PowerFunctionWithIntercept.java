
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

import java.util.Random;

import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.utilities.MathUtilities;

public final class PowerFunctionWithIntercept extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double a;
    private final double b;
    private final double exp;

    public PowerFunctionWithIntercept(double a, double exp, double b)
    {
        this.a = a;
        this.exp = exp;
        this.b = b;
    }

    @Override
    public PowerFunctionWithIntercept multiply(double s)
    {
        return new PowerFunctionWithIntercept(s*a, exp, s*b);
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

        if(n > this.exp && MathUtilities.equalWithinTolerance(this.exp % 0, 0, 1e-15))
        {
            return new Constant(0);
        }

        double aDer = this.a*MathUtilities.fallingFactorial(this.exp, n);
        double expDer = this.exp - n;

        FittedLinearUnivariateFunction der = MathUtilities.equalWithinTolerance(expDer, 0, 1e-15) ? new Constant(aDer) : new PowerFunction(aDer, expDer);
        return der;
    }

    @Override
    public double value(double x) 
    {
        double v = a*Math.pow(x, exp) + b;
        return v;
    }

    @Override
    public double getCoefficient(Number n) 
    {
        Double exp = n.doubleValue();
        if(this.exp == exp){return a;}
        if(exp == 0){return b;}
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
        return 3;
    }

    @Override
    public double[] getParameters()
    {
        return new double[] {a, exp, b};
    }

    public static class PowerFunctionWithInterceptExactFitFactory implements ExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 

        private final double exp;
        private PowerFunctionWithInterceptExactFitFactory(double exp)
        {
            this.exp = exp;
        };

        public static PowerFunctionWithInterceptExactFitFactory getInstance(double exp)
        {
            PowerFunctionWithInterceptExactFitFactory instance = new PowerFunctionWithInterceptExactFitFactory(exp);
            return instance;
        }

        @Override
        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, points, 0, points.length);
        }

        @Override
        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to) 
        {
            PowerFunctionWithIntercept exactFit = null;
            int n = to - from;
            double[] p0 = points[random.nextInt(n) + from];
            double[] p1 = points[random.nextInt(n) + from];

            double x0 = p0[0];
            double y0 = p0[1];
            double x1 = p1[0];
            double y1 = p1[1]; 

            double x0Exp = Math.pow(x0, exp);
            double denominator = (x0Exp - Math.pow(x1, exp));

            if(Math.abs(denominator)>TOLERANCE)
            {
                double a = (y0 - y1)/denominator;
                double b = y0 - a*x0Exp;

                exactFit = new PowerFunctionWithIntercept(a, exp, b);
            }

            return exactFit;
        }

        @Override
        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            PowerFunctionWithIntercept exactFit = null;
            int n1 = pointsA.length;
            int drawn1 = random.nextInt(n);
            int drawn2 = random.nextInt(n);

            double[] p0 = drawn1 < n1 ? pointsA[drawn1] : pointsB[drawn1 - n1];
            double[] p1 = drawn2 < n1 ? pointsA[drawn2] : pointsB[drawn2 - n1];

            double x0 = p0[0];
            double y0 = p0[1];
            double x1 = p1[0];
            double y1 = p1[1]; 

            double x0Exp = Math.pow(x0, exp);
            double denominator = (x0Exp - Math.pow(x1, exp));

            if(Math.abs(denominator)>TOLERANCE)
            {
                double a = (y0 - y1)/denominator;
                double b = y0 - a*x0Exp;

                exactFit = new PowerFunctionWithIntercept(a, exp, b);
            }

            return exactFit;
        }

        @Override
        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            PowerFunctionWithIntercept exactFit = null;
            int n1 = pointsAYs.length;
            int drawn1 = random.nextInt(n);
            int drawn2 = random.nextInt(n);

            double x0 = drawn1 < n1 ? pointsAXs[drawn1] : pointsBXs[drawn1 - n1];
            double y0 = drawn1 < n1 ? pointsAYs[drawn1] : pointsBYs[drawn1 - n1];
            double x1 = drawn2 < n1 ? pointsAXs[drawn2] : pointsBXs[drawn2 - n1];
            double y1 = drawn2 < n1 ? pointsAYs[drawn2] : pointsBYs[drawn2 - n1]; 

            double x0Exp = Math.pow(x0, exp);
            double denominator = (x0Exp - Math.pow(x1, exp));

            if(Math.abs(denominator)>TOLERANCE)
            {
                double a = (y0 - y1)/denominator;
                double b = y0 - a*x0Exp;

                exactFit = new PowerFunctionWithIntercept(a, exp, b);
            }

            return exactFit;
        }

        @Override
        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            PowerFunctionWithIntercept exactFit = null;
            int n = to - from;

            int i0 = random.nextInt(n) + from;
            int i1 = random.nextInt(n) + from;

            double x0 = xs[i0];
            double y0 = ys[i0];
            double x1 = xs[i1];
            double y1 = ys[i1]; 

            double x0Exp = Math.pow(x0, exp);
            double denominator = (x0Exp - Math.pow(x1, exp));

            if(Math.abs(denominator)>TOLERANCE)
            {
                double a = (y0 - y1)/denominator;
                double b = y0 - a*x0Exp;

                exactFit = new PowerFunctionWithIntercept(a, exp, b);
            }

            return exactFit;
        }

        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys,  0, n);
        }

        public PowerFunctionWithIntercept getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int from, int to) 
        {
            PowerFunctionWithIntercept exactFit = null;
            int n = to - from;

            int x0 = random.nextInt(n) + from;
            int x1 = random.nextInt(n) + from;

            double y0 = ys[x0];
            double y1 = ys[x1]; 

            double x0Exp = Math.pow(x0, exp);
            double denominator = (x0Exp - Math.pow(x1, exp));

            if(Math.abs(denominator)>TOLERANCE)
            {
                double a = (y0 - y1)/denominator;
                double b = y0 - a*x0Exp;

                exactFit = new PowerFunctionWithIntercept(a, exp, b);
            }

            return exactFit;
        }

        public static boolean accepts(double[] modelSorted) 
        {
            boolean accepts = (modelSorted.length == 2) && (modelSorted[0] == 0);
            return accepts;
        }

        @Override
        public int getEstimatedParameterCount()
        {
            return 2;
        }
    }
}
