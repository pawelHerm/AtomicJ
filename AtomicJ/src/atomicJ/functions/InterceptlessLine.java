
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

public final class InterceptlessLine extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double b;

    public InterceptlessLine(double b)
    {
        this.b = b;
    }

    @Override
    public InterceptlessLine multiply(double s)
    {
        return new InterceptlessLine(s*b);
    }

    @Override
    public FittedLinearUnivariateFunction getDerivative(int deg)
    {
        if(deg < 0)
        {
            throw new IllegalArgumentException("Negative derivative " + deg);
        }

        if(deg == 0)
        {
            return this;
        }

        FittedLinearUnivariateFunction function = deg > 1 ? new Constant(0) : new Constant(b);
        return function;
    }

    @Override
    public double value(double x)
    {
        double y = b*x;					
        return y;
    }

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 1)return b;
        else return 0;
    }

    @Override
    public int getEstimatedParameterCount()
    {
        return 1;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {b};
    }   

    public static class InterceptlessLineExactFitFactory implements OneParameterExactFitFactory
    {
        private static final double TOLERANCE = 1e-14; 

        private static final InterceptlessLineExactFitFactory INSTANCE = new InterceptlessLineExactFitFactory();

        private InterceptlessLineExactFitFactory() {};

        public static InterceptlessLineExactFitFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, points, 0, points.length);
        }

        @Override
        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to) 
        {
            int n = to - from;
            double[] p = points[random.nextInt(n) + from];

            double x0 = p[0];
            double y0 = p[1]; 

            InterceptlessLine exactFit = null;

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/x0;
                exactFit = new InterceptlessLine(a); 
            }

            return exactFit;
        }

        @Override
        public InterceptlessLine getExactFit(double x0, double y0)
        {
            InterceptlessLine exactFit = null;

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/x0;
                exactFit = new InterceptlessLine(a); 
            }

            return exactFit;
        }

        @Override
        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            InterceptlessLine exactFit = null;
            int n1 = pointsA.length;
            int drawn = random.nextInt(n);
            double[] p = drawn < n1 ? pointsA[drawn] : pointsB[drawn - n1];

            double x0 = p[0];
            double y0 = p[1]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/x0;
                exactFit = new InterceptlessLine(a); 
            }

            return exactFit;
        }

        @Override
        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            InterceptlessLine exactFit = null;
            int n1 = pointsAYs.length;
            int drawn = random.nextInt(n);

            double x0 = drawn < n1 ? pointsAXs[drawn] : pointsBXs[drawn - n1];
            double y0 = drawn < n1 ? pointsAYs[drawn] : pointsBYs[drawn - n1]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/x0;
                exactFit = new InterceptlessLine(a); 
            }

            return exactFit;
        }

        @Override
        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            InterceptlessLine exactFit = null;
            int n = to - from;
            int i = random.nextInt(n) + from;
            double x0 = xs[i];
            double y0 = ys[i]; 

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/x0;
                exactFit = new InterceptlessLine(a); 
            }

            return exactFit;
        }

        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, 0, n);
        }

        public InterceptlessLine getExactFitForRandomSubsetOfPoints(Random random, double[] ys, int from, int to) 
        {
            InterceptlessLine exactFit = null;
            int n = to - from;
            int x0 = random.nextInt(n) + from;
            double y0 = ys[x0]; 

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/x0;
                exactFit = new InterceptlessLine(a);
            }
            return exactFit;
        }


        public static boolean accepts(int deg, boolean constant)
        {
            boolean accepts = (deg == 1) && !constant;
            return accepts;
        }

        public static boolean accepts(double[] model) 
        {
            boolean accepts = (model.length == 1) && (model[0] == 1);           
            return accepts;
        }  

        @Override
        public int getEstimatedParameterCount()
        {
            return 1;
        }
    }
}

