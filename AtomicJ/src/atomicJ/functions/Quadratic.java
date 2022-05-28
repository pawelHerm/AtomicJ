
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

import Jama.LUDecomposition;
import Jama.Matrix;
import atomicJ.statistics.FittedLinearUnivariateFunction;

public final class Quadratic extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double a;
    private final double b;
    private final double c;

    public Quadratic(double a, double b, double c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public Quadratic multiply(double s)
    {
        return new Quadratic(s*a, s*b, s*c);
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

    public Quadratic getHScaled(double h)
    {
        return new Quadratic(this.a, this.b/h, this.c/(h*h));
    }

    @Override
    public double value(double x)
    {
        double y = x*(x*c + b) + a;

        return y;
    }

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 0)return a;
        else if(i == 1) return b;
        else if(i == 2) return c;
        else return 0;
    } 

    @Override
    public int getEstimatedParameterCount() 
    {
        return 3;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {a, b, c};
    }

    public static class QuadraticExactFitFactory implements ExactFitFactory
    {        
        private static final double[] QUADRATIC = new double[] {0,1,2};

        //those arrays are reused to avoid overhead of heap allocation
        double[][] elementalSet;
        double[][] vandermondData;
        double[] ordinates;        

        private QuadraticExactFitFactory(double[][] elementalSet, double[][] vandermondData, double[] ordinates)
        {
            this.elementalSet = elementalSet;
            this.vandermondData =vandermondData;
            this.ordinates = ordinates;
        }

        public static QuadraticExactFitFactory getInstance()
        {           
            double[][] vandermondData = new double[3][3];
            double[] ordinates = new double[3];
            double[][] elementalSet = new double[3][2]; 

            return new QuadraticExactFitFactory(elementalSet, vandermondData, ordinates);
        }

        @Override
        public Quadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, data, 0, data.length);
        }

        @Override
        public Quadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int from, int to) 
        {
            int n = to - from;
            for(int j = 0; j < 3; j++)
            {
                int k = random.nextInt(n) + from;
                elementalSet[j] = data[k];
            }

            return getExactFit();
        }

        @Override
        public Quadratic getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            int n1 = pointsA.length;
            for(int j = 0; j < 3; j++)
            {
                int k = random.nextInt(n);
                elementalSet[j] = k < n1 ? pointsA[k] : pointsB[k - n1];
            }

            return getExactFit();
        }

        @Override
        public Quadratic getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            int n1 = pointsAYs.length;
            for(int j = 0; j < 3; j++)
            {
                int k = random.nextInt(n);
                double[] elementalSetPoints = elementalSet[j];
                elementalSetPoints[0] = k < n1 ? pointsAXs[k] : pointsBXs[k - n1];
                elementalSetPoints[1] = k < n1 ? pointsAYs[k] : pointsBYs[k - n1];
            }

            return getExactFit();
        }

        @Override
        public Quadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public Quadratic getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            int n = to - from;
            for(int j = 0; j < 3; j++)
            {
                int k = random.nextInt(n) + from;
                double[] elementalSetp = elementalSet[j];
                elementalSetp[0] = xs[k];
                elementalSetp[1] = ys[k];
            }
            return getExactFit();
        }

        private Quadratic getExactFit() 
        {
            Quadratic exactFit = null;

            double[] coeff;

            for(int j = 0; j < 3; j++)
            {
                double[] point = elementalSet[j];
                double x = point[0];
                double y = point[1];

                double[] row = vandermondData[j];
                row[0] = 1;
                row[1] = x;
                row[2] = x*x;

                ordinates[j] = y;
            }

            Matrix vandermondMatrix = new Matrix(vandermondData, 3, 3);

            LUDecomposition decomp = new LUDecomposition(vandermondMatrix);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,3));
                coeff = coeffMatrix.getRowPackedCopy();

                exactFit = new Quadratic(coeff[0],coeff[1],coeff[2]);
            }

            return exactFit;
        }


        public static boolean accepts(int deg, boolean constant)
        {
            boolean accepts = (deg == 2);
            return accepts;
        }

        public static boolean accepts(double[] modelSorted) 
        {
            boolean accepts = Arrays.equals(QUADRATIC, modelSorted);
            return accepts;
        }

        @Override
        public int getEstimatedParameterCount()
        {
            return 3;
        }
    }
}
