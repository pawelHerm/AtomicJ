
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Jama.LUDecomposition;
import Jama.Matrix;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;


public final class PowerFunctionCombination extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private Map<Double,Double> coefficients;
    private final double[] exponents;
    private final double[] factors;

    public PowerFunctionCombination(double[] exponents, double[] factors)
    {
        int k = exponents.length;
        int v = factors.length;
        if( k != v){throw new IllegalArgumentException();}

        this.exponents = exponents;
        this.factors = factors;              
    }

    @Override
    public PowerFunctionCombination multiply(double s)
    {
        return new PowerFunctionCombination(this.exponents, MathUtilities.multiply(this.factors, s));
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

        int p = this.exponents.length;

        List<Double> expsDer = new ArrayList<>();
        List<Double> factorsDer = new ArrayList<>();

        for(int i = 0; i<p; i++)
        {
            double exp = exponents[i];
            double a = factors[i];

            boolean termDisappears = n > exp && MathUtilities.equalWithinTolerance(exp % 0, 0, 1e-15);
            if(!termDisappears)
            {
                double expDer = exp - n;
                double factorDer = a*MathUtilities.fallingFactorial(exp, n);

                expsDer.add(expDer);
                factorsDer.add(factorDer);
            }          
        }

        if(factorsDer.isEmpty())
        {
            return new Constant(0);
        }

        FittedLinearUnivariateFunction der = new PowerFunctionCombination(ArrayUtilities.getDoubleArray(expsDer), ArrayUtilities.getDoubleArray(factorsDer));
        return der;
    }

    public PowerFunctionCombination getHScaled(double h)
    {
        int n = exponents.length;

        double[] exponentsNew = Arrays.copyOf(exponents, n);
        double[] factorsNew = new double[n];

        for(int i = 0; i < n; i++)
        {
            factorsNew[i] = factors[i]/Math.pow(h, exponents[i]);
        }

        return new PowerFunctionCombination(exponentsNew, factorsNew);
    }

    @Override
    public double value(double x)
    {
        double y = 0;
        double a;
        double exp;
        for(int i = 0; i<exponents.length; i++)
        {
            exp = exponents[i];
            a = factors[i];
            y = y + a*Math.pow(x, exp);
        }
        return y;
    }

    @Override
    public double getCoefficient(Number exp)
    {
        if(coefficients == null)
        {
            int k = exponents.length;

            coefficients = new HashMap<>();
            for(int i = 0;i<k;i++)
            {
                Double ex = Double.valueOf(exponents[i]);
                Double coeff = Double.valueOf(factors[i]);
                coefficients.put(ex, coeff);
            }
        }

        Double value = coefficients.get(exp);
        double coeff = value != null ? value : 0;

        return coeff;
    }


    @Override
    public int getEstimatedParameterCount()
    {
        return exponents.length;
    }

    @Override
    public double[] getParameters()
    {
        return ArrayUtilities.join(factors, exponents);
    }

    public static class PowerFunctionCombinationExactFitFactory implements ExactFitFactory
    {        
        private final double[] model;
        private final int p;

        private PowerFunctionCombinationExactFitFactory(double[] model) 
        {
            this.model = model;
            this.p = model.length;
        };

        public static PowerFunctionCombinationExactFitFactory getInstance(double[] model)
        {
            return new PowerFunctionCombinationExactFitFactory(model);
        }

        @Override
        public PowerFunctionCombination getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, points, 0, points.length);
        }

        @Override
        public PowerFunctionCombination getExactFitForRandomSubsetOfPoints(Random random, double[][] points, int from, int to) 
        {
            Set<double[]> elementalSet = MathUtilities.getRandomSubsampleWithoutOverwritting(random, points, from, to, p);         

            return getExactFit(elementalSet);
        }

        @Override
        public PowerFunctionCombination getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            Set<double[]> elementalSet = MathUtilities.getRandomSubsampleWithoutOverwritting(random, pointsA, pointsB, p);

            return getExactFit(elementalSet);
        }

        @Override
        public PowerFunctionCombination getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            Set<double[]> elementalSet = MathUtilities.getRandomSubsampleWithoutOverwritting(random, pointsAYs, pointsAXs, pointsBYs, pointsBXs, p);

            return getExactFit(elementalSet);
        }


        @Override
        public PowerFunctionCombination getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public PowerFunctionCombination getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            Set<double[]> elementalSet = MathUtilities.getRandomSubsampleWithoutOverwritting(random, ys, xs, from, to, p);         

            return getExactFit(elementalSet);
        }

        public PowerFunctionCombination getExactFit(Set<double[]> elementalSet)
        {
            PowerFunctionCombination elementalFit = null;

            double[][] matrixData1 = new double[p][p];
            double[] matrixData2 = new double[p];
            double[] coeff;

            int m = 0;
            for(double[] point : elementalSet)
            {
                double x = point[0];
                double y = point[1];

                matrixData2[m] = y;

                for(int k = 0;k<p;k++)
                {
                    double e = model[k];
                    matrixData1[m][k] = Math.pow(x, e);
                }

                m++;
            }

            Matrix matrixTest = new Matrix(matrixData1, p, p);
            LUDecomposition decomp = new LUDecomposition(matrixTest);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(matrixData2,p));
                coeff = coeffMatrix.getRowPackedCopy();
                elementalFit = new PowerFunctionCombination(model, coeff);; 
            } 

            return elementalFit;
        }

        @Override
        public int getEstimatedParameterCount()
        {
            return p;
        }
    }
}