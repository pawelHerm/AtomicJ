
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

public final class InterceptlessCubic extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double b;
    private final double c;
    private final double d;

    public InterceptlessCubic(double b, double c, double d)
    {
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public InterceptlessCubic multiply(double s)
    {
        return new InterceptlessCubic(s*b, s*c, s*d);
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
        if(n == 3)
        {
            return new Constant(6*d);
        }
        if(n == 2)
        {
            return new Line(2*c, 6*d);
        }
        if(n == 1)
        {
            return new Quadratic(b, 2*c, 3*d);
        }

        throw new IllegalArgumentException("Negative derivative " + n);
    }

    public InterceptlessCubic getHScaled(double h)
    {
        return new InterceptlessCubic(this.b/h, this.c/(h*h), this.d/(h*h*h));
    }

    @Override
    public  double value(double x)
    {
        double y = x*(x*(x*d + c) + b);

        return y;
    }

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 1){return b;}
        else if(i == 2){return c;}
        else if(i == 3){return d;}
        else {return 0;}
    }

    @Override
    public int getEstimatedParameterCount()
    {
        return 3;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {b, c, d};
    }   

    public static class InterceptlessCubicExactFitFactory implements ExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 
        private static final double[] CUBIC_INTERCEPTLESS = new double[] {1,2,3};

        private static final InterceptlessCubicExactFitFactory INSTANCE = new InterceptlessCubicExactFitFactory();

        private InterceptlessCubicExactFitFactory() {};

        public static InterceptlessCubicExactFitFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public InterceptlessCubic getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, data, 0, data.length);
        }

        @Override
        public InterceptlessCubic getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int from, int to) 
        {
            int n = to - from;
            double[] p0 = data[random.nextInt(n) + from];
            double x0 = p0[0];
            double y0 = p0[1];            
            double[] p1 = data[random.nextInt(n) + from];
            double x1 = p1[0];
            double y1 = p1[1];   
            double[] p2 = data[random.nextInt(n) + from];
            double x2 = p2[0];
            double y2 = p2[1];

            return getExactFit(x0, y0, x1, y1, x2, y2);
        }

        @Override
        public InterceptlessCubic getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            int n1 = pointsA.length;

            int drawn0 = random.nextInt(n);
            double[] p0 = drawn0 < n1 ? pointsA[drawn0] : pointsB[drawn0 - n1];
            double x0 = p0[0];
            double y0 = p0[1];

            int drawn1 = random.nextInt(n);
            double[] p1 = drawn1 < n1 ? pointsA[drawn1] : pointsB[drawn1 - n1];
            double x1 = p1[0];
            double y1 = p1[1];            

            int drawn2 = random.nextInt(n);
            double[] p2 = drawn2 < n1 ? pointsA[drawn2] : pointsB[drawn2 - n1];
            double x2 = p2[0];
            double y2 = p2[1]; 

            return getExactFit(x0, y0, x1, y1, x2, y2);
        }

        @Override
        public InterceptlessCubic getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            int n1 = pointsAYs.length;

            int drawn0 = random.nextInt(n);
            double x0 = drawn0 < n1 ? pointsAXs[drawn0] : pointsBXs[drawn0 - n1];
            double y0 = drawn0 < n1 ? pointsAYs[drawn0] : pointsBYs[drawn0 - n1];

            int drawn1 = random.nextInt(n);
            double x1 = drawn1 < n1 ? pointsAXs[drawn1] : pointsBXs[drawn1 - n1];
            double y1 = drawn1 < n1 ? pointsAYs[drawn1] : pointsBYs[drawn1 - n1];            

            int drawn2 = random.nextInt(n);
            double x2 = drawn2 < n1 ? pointsAXs[drawn2] : pointsBXs[drawn2 - n1];
            double y2 = drawn2 < n1 ? pointsAYs[drawn2] : pointsBYs[drawn2 - n1]; 

            return getExactFit(x0, y0, x1, y1, x2, y2);
        }

        @Override
        public InterceptlessCubic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public InterceptlessCubic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            int n = to - from;
            int i0 = random.nextInt(n) + from;
            double x0 = xs[i0];
            double y0 = ys[i0];  

            int i1 = random.nextInt(n) + from;
            double x1 = xs[i1];
            double y1 = ys[i1];

            int i2 = random.nextInt(n) + from;
            double x2 = xs[i2];
            double y2 = ys[i2];

            return getExactFit(x0, y0, x1, y1, x2, y2);
        }

        private InterceptlessCubic getExactFit(double x0, double y0, double x1, double y1, double x2, double y2)
        {
            double denomin = (x0*(x0 - x1)*x1*(x0 - x2)*(x1 - x2)*x2);

            InterceptlessCubic exactFit = null;

            if(Math.abs(denomin)>TOLERANCE)
            {
                double x0Cube = x0*x0*x0;
                double x1Cube = x1*x1*x1;
                double x2Cube = x2*x2*x2;
                double b = (x2*x2*(x1*x1*(x1 - x2)*y0 + x0*x0*(x2-x0)*y1) + x0*x0*(x0 - x1)*x1*x1*y2)/denomin;
                double c = (x0*(x0 - x2)*x2*(x0 + x2)*y1 + x1Cube*(-x2*y0 + x0*y2) + 
                        x1*(x2Cube*y0 - x0Cube*y2))/denomin;
                double d = (x1*(x1 - x2)*x2*y0 + x0*x2*(-x0 + x2)*y1 + x0*(x0 - x1)*x1*y2)/denomin;
                exactFit = new InterceptlessCubic(b,c,d);
            }

            return exactFit;
        }

        public static boolean accepts(int deg, boolean constant)
        {
            boolean accepts = (deg == 3) && !constant;
            return accepts;
        }

        public static boolean accepts(double[] model) 
        {
            boolean accepts = Arrays.equals(model, CUBIC_INTERCEPTLESS);           
            return accepts;
        }    

        @Override
        public int getEstimatedParameterCount()
        {
            return 3;
        }
    }
}