
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
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Selector;



public final class Polynomial extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
{
    private final double[] coeffs;
    private final int deg;

    public Polynomial(double[] coeff)
    {
        this.coeffs = coeff;		
        this.deg = coeff.length - 1;
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

        if(n > this.deg)
        {
            return new Constant(0);
        }

        int p = coeffs.length;

        int pNew = p - n;

        double[] coeffNew = new double[pNew];

        for(int i = n; i < p; i++)
        {
            coeffNew[i - n] = coeffs[i]*MathUtilities.fallingFactorial(i, n);
        }

        return new Polynomial(coeffNew);
    }

    public Polynomial getHScaled(double h)
    {
        int n = coeffs.length;
        double[] coeffNew = new double[n];

        for(int i = 0; i<n; i++)
        {
            coeffNew[i] = coeffs[i]/MathUtilities.intPow(h, i);
        }

        return new Polynomial(coeffNew);
    }

    @Override
    public Polynomial multiply(double s)
    {       
        return new Polynomial(MathUtilities.multiply(coeffs, s));
    }

    public int degree()
    {
        return deg;
    }

    public double getCoefficient(int k)
    {
        double c = k > deg ? 0 : coeffs[k];       
        return c;
    }

    public double[] getCoefficients()
    {
        return Arrays.copyOf(coeffs, deg + 1);
    }

    @Override
    public double getCoefficient(Number n)
    {
        int c = n.intValue();

        double coeff = (c<0||c>deg) ? 0 : coeffs[c];
        return coeff;
    }

    /**Uses Horner's method to compute the value of a polynomial function for a given argument*/
    @Override
    public double value(double x)
    {
        double y = 0;
        for(int i = deg;i>=0;i--)
        {
            y = x*y + coeffs[i];
        }
        return y;
    }

    /**Multiplies the polynomial by a scalar*/
    public Polynomial times(double c)
    {
        int k = this.deg;
        double[] coefResult = new double[k + 1];
        for(int i = 0;i<=k;i++)
        {
            coefResult[i] = c*coeffs[i];
        }
        Polynomial result = new Polynomial(coefResult);
        return result;
    }

    public Polynomial[] divideWithRemainder(Polynomial p2)
    {
        int n = this.deg;
        int m = p2.degree();
        int k = n - m;

        double[] coefDivider = p2.getCoefficients();
        double[] coefQuotient = new double[k + 1];
        double[] coefRemainder = this.getCoefficients();

        for(int i = k;i>=0;i--)
        {
            double q = coefRemainder[i + n]/coefDivider[n];
            coefQuotient[i] = q;
            for(int j = n;j>=0;j--)
            {
                coefRemainder[i+j] = coefRemainder[i+j] - q*coefDivider[j];
            }
        }
        Polynomial quotient = new Polynomial(coefQuotient);
        Polynomial remainder = new Polynomial(coefRemainder);
        Polynomial[] result = {quotient, remainder};
        return result;
    }

    public Polynomial times(Polynomial p2)
    {
        int n = this.deg;
        int m = p2.degree();
        int k = n*m;

        double[] coef1 = this.coeffs;
        double[] coef2 = p2.getCoefficients();
        double[] coefResult = new double[k + 1];

        for(int i = 0;i<=n;i++)
        {
            for(int j = 0;j<=m;j++)
            {
                coefResult[i + j] = coefResult[i + j] + coef1[i]*coef2[j];
            }
        }
        Polynomial result = new Polynomial(coefResult);
        return result;		
    }


    public Polynomial plus(Polynomial p2)
    {
        int m = this.deg;
        int n = p2.degree();
        int k = Math.max(m,n);

        double[] coef1 = Arrays.copyOf(coeffs, k);
        double[] coef2 = Arrays.copyOf(p2.getCoefficients(),k);
        double[] coefResult = new double[k + 1];

        for(int i = 0;i<=k;i++)
        {
            coefResult[i] = coef1[i] + coef2[i];
        }

        Polynomial result = new Polynomial(coefResult);
        return result;
    }

    public Polynomial minus(Polynomial p2)
    {
        Polynomial result = plus(p2.times(-1));
        return result;
    }

    public static double value(double x, double[] coeff, int deg)
    {
        double y = 0;
        for(int i = deg;i>=0;i--)
        {
            y = x*y + coeff[i];
        }
        return y;
    }

    public static double residual(double[] p, double[] coeff, int deg)
    {	
        double x = p[0];
        double y = p[1];
        double r = y - value(x, coeff, deg);
        return r;
    }

    public static double residual(double x, double y, double[] coeff, int deg)
    {   
        double r = y - value(x, coeff, deg);
        return r;
    }


    public static double trimmedSquaresForCoeffs(double[][] points, double[] coeff,int deg, double[] arrayToFillWithSquaredResiduals,int c)
    {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p, coeff, deg);
            arrayToFillWithSquaredResiduals[i] = r*r;
        }
        Selector.sortSmallest(arrayToFillWithSquaredResiduals,c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        return crit;
    }


    public static double trimmedSquaresForCoeffs(double[] yValues, double[] coeff,int deg, double[] arrayToFillWithSquaredResiduals,int c)
    {
        int n = yValues.length - 1;
        for(int i = n;i>=0;i--)
        {
            double r = residual(i, yValues[i], coeff, deg);
            arrayToFillWithSquaredResiduals[i] = r*r;
        }

        Selector.sortSmallest(arrayToFillWithSquaredResiduals, c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        return crit;
    }  

    @Override
    public int getEstimatedParameterCount()
    {
        return coeffs.length;
    }


    @Override
    public double[] getParameters() 
    {
        return Arrays.copyOf(coeffs, coeffs.length);
    } 

    public static double trimmedWSquares(double[][] points, double[] coeff,
            int deg, double[] arrayToFillWithSquaredResiduals, int c) {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p, coeff, deg);
            arrayToFillWithSquaredResiduals[i] = r*r;
        }
        Selector.sortSmallest(arrayToFillWithSquaredResiduals,c);
        double crit = ArrayUtilities.total(arrayToFillWithSquaredResiduals, c);

        int remnants = points.length - c;
        double winsorized = arrayToFillWithSquaredResiduals[c - 1];
        crit = crit + remnants*winsorized;

        return crit;       
    }

    public boolean isZero()
    {
        for(double c : coeffs)
        {
            if(c != 0)
            {
                return false;
            }
        }

        return true;
    }

    public static FittedLinearUnivariateFunction getPolynomialFunction(double[] parameters, int deg, boolean constant)
    {
        double[] coefficents;
        if(constant)
        {
            coefficents = parameters;

            if(deg == 1)
            {
                return new Line(coefficents[0], coefficents[1]);
            }
            if(deg == 2)
            {
                return new Quadratic(coefficents[0], coefficents[1], coefficents[2]); 
            }
        }
        else
        {
            coefficents = ArrayUtilities.padLeft(parameters, 0, 1);
        }

        return new Polynomial(coefficents);
    }

    public static class PolynomialExactFitFactory implements ExactFitFactory
    {        
        private final double[][] elementalSet;
        private final double[] ordinates;
        private final double[][] vandermondData;
        private final int p;

        private PolynomialExactFitFactory(double[][] elementalSet, double[] ordinates, double[][] vandermondData) 
        {
            this.elementalSet = elementalSet;
            this.ordinates = ordinates;
            this.vandermondData = vandermondData;
            this.p = elementalSet.length;
        };

        public static PolynomialExactFitFactory getInstance(int deg, boolean constant)
        {
            int p =  deg + MathUtilities.boole(constant); 
            double[][] vandermondData = new double[p][p];
            double[] ordinates = new double[p];
            double[][] elementalSet = new double[p][2]; 

            return new PolynomialExactFitFactory(elementalSet, ordinates, vandermondData);
        }

        public static ExactFitFactory getInstance(double[] modelSorted)
        {
            int p =  modelSorted.length; 
            double[][] vandermondData = new double[p][p];
            double[] ordinates = new double[p];
            double[][] elementalSet = new double[p][2]; 

            return new PolynomialExactFitFactory(elementalSet, ordinates, vandermondData);
        }

        @Override
        public Polynomial getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, data, 0, data.length);
        }

        @Override
        public Polynomial getExactFitForRandomSubsetOfPoints(Random random, double[][] data, int from, int to) 
        {
            int n = to - from;
            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(n);
                elementalSet[j] = data[k + from];
            }

            return getExactFit();
        }

        @Override
        public Polynomial getExactFitForRandomSubsetOfPoints(Random random, double[][] pointsA, double[][] pointsB, int n) 
        {
            int n1 = pointsA.length;
            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(n);
                elementalSet[j] = k < n1 ? pointsA[k] : pointsB[k - n1];
            }

            return getExactFit();
        }

        @Override
        public Polynomial getExactFitForRandomSubsetOfPoints(Random random, double[] pointsAYs, double[] pointsAXs, double[] pointsBYs, double[] pointsBXs, int n) 
        {
            int n1 = pointsAYs.length;
            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(n);

                double[] elementalPointP = elementalSet[j];

                elementalPointP[0] = k < n1 ? pointsAXs[k] : pointsBXs[k - n1];
                elementalPointP[1] = k < n1 ? pointsAYs[k] : pointsBYs[k - n1];
            }

            return getExactFit();
        }

        @Override
        public Polynomial getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int n) 
        {
            return getExactFitForRandomSubsetOfPoints(random, ys, xs, 0, n);
        }

        @Override
        public Polynomial getExactFitForRandomSubsetOfPoints(Random random, double[] ys, double[] xs, int from, int to) 
        {
            int n = to - from;
            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(n) + from;
                double[] elementalPointP = elementalSet[j];
                elementalPointP[0] = xs[k];
                elementalPointP[1] = ys[k];
            }

            return getExactFit();
        }

        public Polynomial getExactFit()
        {
            Polynomial exactFit = null;
            double[] coeff;

            for(int j = 0;j<p;j++)
            {
                double[] point = elementalSet[j];
                double x = point[0];
                double y = point[1];

                ordinates[j] = y;

                double[] row = vandermondData[j];
                row[0] = 1;
                row[1] = x;
                for(int k = 2;k<p;k++)
                {
                    row[k] = x*row[k - 1];
                }
            }
            Matrix vandermondMatrix = new Matrix(vandermondData, p, p);
            LUDecomposition decomp = new LUDecomposition(vandermondMatrix);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,p));
                coeff = coeffMatrix.getRowPackedCopy();
                exactFit = new Polynomial(coeff);
            }

            return exactFit;
        }

        public static boolean accepts(int deg, boolean constant) 
        {
            boolean accepts = (deg >= 0);
            return accepts;
        }

        public static boolean accepts(double[] modelSorted)
        {
            if(modelSorted.length == 0)
            {
                return false;
            }

            for(double power : modelSorted)
            {
                if((power != (int) power) || power < 0)
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int getEstimatedParameterCount()
        {
            return p;
        }
    }
}
