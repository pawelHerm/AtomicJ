
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

public final class BareQuadratic extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double c;

    public BareQuadratic(double c)
    {
        this.c = c;
    }

    @Override
    public BareQuadratic multiply(double s)
    {
        return new BareQuadratic(s*c);
    }

    @Override
    public FittedLinearUnivariateFunction getDerivative(int n)
    {
        if(n == 0)
        {
            return this;
        }
        if(n > 3)
        {
            return new Constant(0);
        }
        if(n == 2)
        {
            return new Constant(2*c);
        }
        if(n == 1)
        {
            return new InterceptlessLine(2*c);
        }

        throw new IllegalArgumentException("Negative derivative " + n);
    }

    public BareQuadratic getHScaled(double h)
    {
        return new BareQuadratic(this.c/(h*h));
    }

    @Override
    public double value(double x) 
    {
        double v = c*x*x;
        return v;
    }

    @Override
    public double getCoefficient(Number n) 
    {
        Double exp = n.doubleValue();
        if(exp == 2){return c;}
        else {return 0;}
    }

    public double getCoefficient() 
    {
        return c;
    }

    @Override
    public int getEstimatedParameterCount()
    {
        return 1;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {c};
    }	

    public static class BareQuadraticExactFitFactory implements OneParameterExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 

        private static final BareQuadraticExactFitFactory INSTANCE = new BareQuadraticExactFitFactory();

        private BareQuadraticExactFitFactory() {};

        public static BareQuadraticExactFitFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, points, 0, n);
        }

        @Override
        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to) 
        {
            int n = to - from;
            BareQuadratic exactFit = null;
            double[] p = points[random.nextInt(n) + from];
            double x0 = p[0];
            double y0 = p[1]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/(x0*x0);
                exactFit = new BareQuadratic(a);
            }

            return exactFit;
        }

        @Override
        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            int n1 = pointsA.length;
            BareQuadratic exactFit = null;
            int drawn = random.nextInt(n);
            double[] p = drawn < n1 ? pointsA[drawn] : pointsB[drawn - n1];
            double x0 = p[0];
            double y0 = p[1]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/(x0*x0);
                exactFit = new BareQuadratic(a);
            }

            return exactFit;
        }

        @Override
        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            int n1 = pointsAYs.length;
            BareQuadratic exactFit = null;
            int drawn = random.nextInt(n);
            double x0 = drawn < n1 ? pointsAXs[drawn] : pointsBXs[drawn - n1];
            double y0 = drawn < n1 ? pointsAYs[drawn] : pointsBYs[drawn - n1]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/(x0*x0);
                exactFit = new BareQuadratic(a);
            }

            return exactFit;
        }

        @Override
        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            int n = to - from;
            BareQuadratic exactFit = null;
            int i = random.nextInt(n) + from;
            double x0 = xs[i];
            double y0 = ys[i]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/(x0*x0);
                exactFit = new BareQuadratic(a);
            }

            return exactFit;
        }

        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, 0, n);
        }

        public BareQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int from, int to) 
        {
            int n = to - from;
            int x0 = random.nextInt(n) + from;
            double y0 = ys[x0]; 

            BareQuadratic exactFit = null;

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                exactFit = new BareQuadratic(a);
            }
            return exactFit;
        }

        @Override
        public FittedLinearUnivariateFunction getExactFit(double x0, double y0)
        {
            BareQuadratic exactFit = null;

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                exactFit = new BareQuadratic(a);
            }
            return exactFit;
        }

        public static boolean accepts(double[] model) 
        {
            boolean accepts = (model.length == 1) && (model[0] == 2);           
            return accepts;
        }       

        @Override
        public int getEstimatedParameterCount()
        {
            return 1;
        }
    }
}
