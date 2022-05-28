
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
import java.util.Random;

import atomicJ.statistics.FittedLinearUnivariateFunction;

public final class InterceptlessQuadratic extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double b;
    private final double c;

    public InterceptlessQuadratic(double b, double c)
    {
        this.b = b;
        this.c = c;
    }

    @Override
    public InterceptlessQuadratic multiply(double s)
    {
        return new InterceptlessQuadratic(s*b, s*c);
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
            return new Line(b, 2*c);
        }

        throw new IllegalArgumentException("Negative derivative " + n);
    }

    public InterceptlessQuadratic getHScaled(double h)
    {
        return new InterceptlessQuadratic(this.b/h, this.c/(h*h));
    }

    @Override
    public  double value(double x)
    {
        double y = x*(x*c + b);

        return y;
    }

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 1){return b;}
        else if(i == 2){return c;}
        else {return 0;}
    }

    @Override
    public int getEstimatedParameterCount()
    {
        return 2;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {b, c};
    }   

    public static class InterceptlessQuadraticExactFitFactory implements ExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 
        private static final double[] QUADRATIC_INTERCEPTLESS = new double[] {1,2};


        private static final InterceptlessQuadraticExactFitFactory INSTANCE = new InterceptlessQuadraticExactFitFactory();

        private InterceptlessQuadraticExactFitFactory() {};

        public static InterceptlessQuadraticExactFitFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public InterceptlessQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, data, 0, data.length);
        }

        @Override
        public InterceptlessQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int from, int to) 
        {
            InterceptlessQuadratic exactFit = null;

            int n = to - from;
            double[] p = data[random.nextInt(n) + from];
            double x0 = p[0];
            double y0 = p[1];            
            p = data[random.nextInt(n) + from];
            double x1 = p[0];
            double y1 = p[1];            
            double denomin = (x0*(x0 - x1)*x1);

            if(Math.abs(denomin)>TOLERANCE)
            {
                double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                double b = ((x1*y0 - x0*y1)/denomin);
                exactFit = new InterceptlessQuadratic(a,b);
            }

            return exactFit;
        }

        @Override
        public InterceptlessQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            InterceptlessQuadratic exactFit = null;

            int n1 = pointsA.length;

            int drawn1 = random.nextInt(n);
            double[] p = drawn1 < n1 ? pointsA[drawn1] : pointsB[drawn1 - n1];
            double x0 = p[0];
            double y0 = p[1];

            int drawn2 = random.nextInt(n);
            double[] p2 = drawn2 < n1 ? pointsA[drawn2] : pointsB[drawn2 - n1];

            double x1 = p2[0];
            double y1 = p2[1];            
            double denomin = (x0*(x0 - x1)*x1);

            if(Math.abs(denomin)>TOLERANCE)
            {
                double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                double b = ((x1*y0 - x0*y1)/denomin);
                exactFit = new InterceptlessQuadratic(a,b);
            }

            return exactFit;
        }

        @Override
        public InterceptlessQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            InterceptlessQuadratic exactFit = null;

            int n1 = pointsAYs.length;

            int drawn1 = random.nextInt(n);
            double x0 = drawn1 < n1 ? pointsAXs[drawn1] : pointsBXs[drawn1 - n1];
            double y0 = drawn1 < n1 ? pointsAYs[drawn1] : pointsBYs[drawn1 - n1];

            int drawn2 = random.nextInt(n);

            double x1 = drawn2 < n1 ? pointsAXs[drawn2] : pointsBXs[drawn2 - n1];
            double y1 = drawn2 < n1 ? pointsAYs[drawn2] : pointsBYs[drawn2 - n1];            
            double denomin = (x0*(x0 - x1)*x1);

            if(Math.abs(denomin)>TOLERANCE)
            {
                double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                double b = ((x1*y0 - x0*y1)/denomin);
                exactFit = new InterceptlessQuadratic(a,b);
            }

            return exactFit;
        }

        @Override
        public InterceptlessQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public InterceptlessQuadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            InterceptlessQuadratic exactFit = null;

            int n = to - from;
            int i0 = random.nextInt(n) + from;
            double x0 = xs[i0];
            double y0 = ys[i0];  

            int i1 = random.nextInt(n) + from;
            double x1 = xs[i1];
            double y1 = ys[i1];            
            double denomin = (x0*(x0 - x1)*x1);

            if(Math.abs(denomin)>TOLERANCE)
            {
                double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                double b = ((x1*y0 - x0*y1)/denomin);
                exactFit = new InterceptlessQuadratic(a,b);
            }

            return exactFit;
        }

        public static boolean accepts(int deg, boolean constant)
        {
            boolean accepts = (deg == 2) && !constant;
            return accepts;
        }

        public static boolean accepts(double[] model) 
        {
            boolean accepts = Arrays.equals(model, QUADRATIC_INTERCEPTLESS);           
            return accepts;
        }    

        @Override
        public int getEstimatedParameterCount()
        {
            return 2;
        }
    }
}