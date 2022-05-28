
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

public final class Line extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double a;
    private final double b;

    public Line(double a, double b)
    {
        this.a = a;
        this.b = b;
    }

    @Override
    public Line multiply(double s)
    {
        return new Line(s*a, s*b);
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

        FittedLinearUnivariateFunction function = n > 1 ? new Constant(0) : new Constant(b);
        return function;
    }

    public Line getHScaled(double h)
    {
        return new Line(this.a, this.b/h);
    }

    @Override
    public double value(double x)
    {
        double y = a + b*x;					
        return y;
    }

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 0) return a;
        else if(i == 1) return b;
        else return 0;
    }

    @Override
    public int getEstimatedParameterCount()
    {
        return 2;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {a, b};
    }   

    public static class LineExactFitFactory implements ExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 

        private static final LineExactFitFactory INSTANCE = new LineExactFitFactory();

        private LineExactFitFactory() {};

        public static LineExactFitFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public Line getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, points, 0, points.length);
        }

        @Override
        public Line getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to) 
        {
            Line exactFit = null;
            int n = to - from;
            double[] p1 = points[random.nextInt(n) + from];
            double x0 = p1[0];
            double y0 = p1[1];            
            double[] p2 = points[random.nextInt(n) + from];
            double x1 = p2[0];
            double y1 = p2[1];            
            double denomin = (x0 - x1);
            if(Math.abs(denomin) > TOLERANCE)
            {
                double a = (x0*y1 - x1*y0)/denomin;
                double b = (y0 - y1)/denomin;
                exactFit = new Line(a,b);
            }

            return exactFit;
        }

        @Override
        public Line getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            Line exactFit = null;
            int n1 = pointsA.length;
            int drawn1 = random.nextInt(n);
            double[] p1 = drawn1 < n1 ? pointsA[drawn1] : pointsB[drawn1 - n1];
            double x0 = p1[0];
            double y0 = p1[1];  

            int drawn2 = random.nextInt(n);

            double[] p2 = drawn2 < n1 ? pointsA[drawn2] : pointsB[drawn2 - n1];
            double x1 = p2[0];
            double y1 = p2[1];            
            double denomin = (x0 - x1);
            if(Math.abs(denomin) > TOLERANCE)
            {
                double a = (x0*y1 - x1*y0)/denomin;
                double b = (y0 - y1)/denomin;
                exactFit = new Line(a,b);
            }

            return exactFit;
        }

        @Override
        public Line getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            Line exactFit = null;
            int n1 = pointsAYs.length;
            int drawn1 = random.nextInt(n);
            double x0 = drawn1 < n1 ? pointsAXs[drawn1] : pointsBXs[drawn1 - n1];
            double y0 = drawn1 < n1 ? pointsAYs[drawn1] : pointsBYs[drawn1 - n1];  

            int drawn2 = random.nextInt(n);

            double x1 = drawn2 < n1 ? pointsAXs[drawn2] : pointsBXs[drawn2 - n1];
            double y1 = drawn2 < n1 ? pointsAYs[drawn2] : pointsBYs[drawn2 - n1];            
            double denomin = (x0 - x1);
            if(Math.abs(denomin) > TOLERANCE)
            {
                double a = (x0*y1 - x1*y0)/denomin;
                double b = (y0 - y1)/denomin;
                exactFit = new Line(a,b);
            }

            return exactFit;
        }

        @Override
        public Line getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public Line getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            Line exactFit = null;
            int n = to - from;
            int i0 = random.nextInt(n) + from;
            double x0 = xs[i0];
            double y0 = ys[i0];            
            int i1 = random.nextInt(n) + from;
            double x1 = xs[i1];
            double y1 = ys[i1];            
            double denomin = (x0 - x1);
            if(Math.abs(denomin) > TOLERANCE)
            {
                double a = (x0*y1 - x1*y0)/denomin;
                double b = (y0 - y1)/denomin;
                exactFit = new Line(a,b);
            }

            return exactFit;
        }

        public Line getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, 0, n);
        }

        public Line getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int from, int to) 
        {
            Line exactFit = null;
            int n = to - from;
            int x0 = random.nextInt(n) + from;
            double y0 = ys[x0];                  
            int x1 = random.nextInt(n) + from;
            double y1 = ys[x1];            
            double denomin = (x0 - x1);

            if(Math.abs(denomin) > TOLERANCE)
            {
                double a = (x0*y1 - x1*y0)/denomin;
                double b = (y0 - y1)/denomin;
                exactFit = new Line(a,b);
            }

            return exactFit;
        }


        public static boolean accepts(int deg, boolean constant)
        {
            boolean accepts = (deg == 1);
            return accepts;
        }

        public static boolean accepts(double[] model) 
        {
            boolean acceptsFull = (model.length == 2) && model[0] == 0 && model[1] == 1;
            return acceptsFull;
        }

        @Override
        public int getEstimatedParameterCount()
        {
            return 2;
        }
    }
}

