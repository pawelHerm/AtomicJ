
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

package atomicJ.statistics;

import java.util.*;

import atomicJ.functions.*;
import atomicJ.utilities.*;
import atomicJ.statistics.LTS.LTSOne1DArray;

import Jama.LUDecomposition;
import Jama.Matrix;

public class LTS1DFactory
{
    private static final double TOLERANCE = 1e-14; 
    private static final double[] LINEAR = new double[] {0,1};
    private static final double[] LINEAR_INTERCEPTLESS = new double[] {1};
    private static final double[] QUADRATIC = new double[] {0,1,2};
    private static final double[] QUADRATIC_INTERCEPTLESS = new double[] {1,2};    

    public static LTS findFit(double[] data, int deg, int nstarts)
    {
        int n = data.length;    
        double h = MathUtilities.minimalCoverage(n, deg);

        return findFit(data, deg, true, h, nstarts);        
    }

    public static LTS findFit(double[] data, int deg, boolean constant, int nstarts)
    {
        int n = data.length;
        int p =  deg + MathUtilities.boole(constant);       
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data, deg, constant, h, nstarts);
    }

    public static LTS findFit(double[] data, int deg, boolean constant, double h, int nstarts)
    {
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept; 


        if(deg == 1) {return findLinearFit(data, constant, h, nstarts);}
        if(deg == 2) {return findQuadraticFit(data, constant, h, nstarts);}
        if(deg == 0) {return findConstantFit(data, h, nstarts);}
        else {return findPolynomialFit(data, p, h, nstarts);}
    }

    public static LTS findFit(double[] data, double[] model, int nstarts)
    {
        int n = data.length;
        int p = model.length;
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data,model,h,nstarts);
    }

    public static LTS findFit(double[] data, double[] model, double h, int nstarts)
    {
        if(model.length == 1)
        {
            double exp = model[0];
            if(exp == 2){return findBareQuadraticFit(data, h, nstarts);}
            else if(exp == 1.5){return findSesquiFit(data, h, nstarts);}
            else {return findPowerFit(data, exp, h, nstarts);}
        }
        else if(Arrays.equals(model, LINEAR)) {return findLinearFit(data, true, h, nstarts);}
        else if(Arrays.equals(model, LINEAR_INTERCEPTLESS)) {return findLinearFit(data, false, h, nstarts);}
        else if(Arrays.equals(model, QUADRATIC)){return findQuadraticFit(data, true, h, nstarts);}
        else if(Arrays.equals(model, QUADRATIC_INTERCEPTLESS)) {return findQuadraticFit(data, false, h, nstarts);}
        else {return findPowerFunctionsCombinationFit(data, model,h, nstarts);}
    }

    private LTS1DFactory()
    {
    }

    private static LTS findLinearFit(double[] data, boolean constant, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        FittedLinearUnivariateFunction bestFit = null;
        Random random = new Random();
        double bestIntercept = 0;
        double bestSlope = 0;

        if(constant)
        {
            for(int i = 0;i<nstarts;i++)
            {
                int x0 = random.nextInt(n);
                double y0 = data[x0];                  
                int x1 = random.nextInt(n);
                double y1 = data[x1];            
                double denomin = (x0 - x1);

                if(Math.abs(denomin) > TOLERANCE)
                {
                    double a = (x0*y1 - x1*y0)/denomin;
                    double b = (y0 - y1)/denomin;
                    Line exactFit = new Line(a,b);
                    double crit = exactFit.trimmedSquares(data, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestIntercept = a;
                        bestSlope = b;
                    };
                }       
            }
            bestFit = new Line(bestIntercept,bestSlope);
        }
        else
        {
            for(int i = 0;i<nstarts;i++)
            {
                int x0 = random.nextInt(n);
                double y0 = data[x0]; 

                if(Math.abs(x0)>TOLERANCE)
                {
                    double a = y0/x0;
                    InterceptlessLine exactFit = new InterceptlessLine(a);
                    double crit = exactFit.trimmedSquares(data, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestSlope = a;
                    };
                }

            }
            bestFit = new InterceptlessLine(bestSlope);
        }
        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    public static LTS findConstantFit(double[] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            int x0 = random.nextInt(n);
            double y0 = data[x0];   

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                Constant exactFit = new Constant(a);
                double crit = exactFit.trimmedSquares(data,residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new Constant(bestA);
        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findBareQuadraticFit(double[] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            int x0 = random.nextInt(n);
            double y0 = data[x0]; 

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                BareQuadratic exactFit = new BareQuadratic(a);
                double crit = exactFit.trimmedSquares(data,residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new BareQuadratic(bestA);
        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findSesquiFit(double[] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            int x0 = random.nextInt(n);
            double y0 = data[x0];   

            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/(x0*Math.sqrt(x0));
                SesquiPower exactFit = new SesquiPower(a);
                double crit = exactFit.trimmedSquares(data,residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new SesquiPower(bestA);
        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findPowerFit(double[] data, double exp, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {

            int x0 = random.nextInt(n);
            double y0 = data[x0];    

            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                PowerFunction exactFit = new PowerFunction(a,exp);
                double crit = exactFit.trimmedSquares(data,residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new PowerFunction(bestA,exp);
        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    //Constructs an LTS object, which represent quadratic fit, i.e. quadratic function with intercept is fitted
    private static LTS findQuadraticFit(double[] data, boolean constant, double h, int nstarts)
    {   
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        FittedLinearUnivariateFunction bestFit = null;
        Random random = new Random();

        if(constant)
        {
            double[][] vandermondData = new double[3][3];
            double[] ordinates = new double[3];
            double[] bestFitCoeff = null;

            for(int i = 0;i<nstarts;i++)
            {
                for(int j = 0; j < 3; j++)
                {
                    int x = random.nextInt(n);
                    ordinates[j] = data[x];

                    double[] row = vandermondData[j];
                    row[0] = 1;
                    row[1] = x;
                    row[2] = x*x;
                }

                Matrix vandermondMatrix = new Matrix(vandermondData, 3, 3);
                LUDecomposition decomp = new LUDecomposition(vandermondMatrix);
                if(decomp.isNonsingular())
                {
                    Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,3));
                    double[] coeff = coeffMatrix.getRowPackedCopy();

                    Quadratic exactFit = new Quadratic(coeff[0],coeff[1],coeff[2]);
                    double crit = exactFit.trimmedSquares(data, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFitCoeff = coeff;
                    }
                }

            }
            bestFit = new Polynomial(bestFitCoeff);
        }
        else
        {
            double bestA = 0;
            double bestB = 0;

            for(int i = 0;i<nstarts;i++)
            {
                int x0 = random.nextInt(n);
                double y0 = data[x0];            
                int x1 = random.nextInt(n);
                double y1 = data[x1];     

                double denomin = (x0*(x0 - x1)*x1);

                if(Math.abs(denomin)>TOLERANCE)
                {
                    double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                    double b = ((x1*y0 - x0*y1)/denomin);
                    InterceptlessQuadratic exactFit = new InterceptlessQuadratic(a,b);
                    double crit = exactFit.trimmedSquares(data,residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestA = a;
                        bestB = b;
                    };
                }

            }
            bestFit = new Polynomial(new double[] {0, bestA,bestB});
        }

        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findPolynomialFit(double[] data,int p, double h, int nstarts)
    {   
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        Random random = new Random();

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] ordinates = new double[p];
        double[] bestFitCoeff = null;
        double[] residualsSquared = new double[n];
        double[][] vandermondData = new double[p][p];


        for(int i = 0;i<nstarts;i++)
        {           
            for(int j = 0;j<p;j++)
            {
                int x = random.nextInt(n);
                double y = data[x];

                ordinates[j] = y;

                double[] row = vandermondData[j];
                row[0] = 1;
                row[1] = x;
                for(int k = 2;k<p;k++)
                {
                    row[k] = x*row[k - 1];
                }
            }

            Matrix vandermondMatrix = new Matrix(vandermondData);
            LUDecomposition decomp = new LUDecomposition(vandermondMatrix);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,p));
                double[] coeff = coeffMatrix.getRowPackedCopy();

                double crit = Polynomial.trimmedSquaresForCoeffs(data,coeff, p-1,residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFitCoeff = coeff;
                }
            }

        }
        FittedLinearUnivariateFunction bestFit = new Polynomial(bestFitCoeff);;

        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findPowerFunctionsCombinationFit(double[] data, double[] model, double h, int nstarts)
    {
        int n = data.length;
        int p = model.length;
        int c = (int)Math.min(n, h*n);

        double[] residualsSquared = new double[n];

        double lowestCriterion = Double.POSITIVE_INFINITY;
        PowerFunctionCombination bestFit = null;
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            double[][] matrixData1 = new double[p][p];
            double[] matrixData2 = new double[p];

            for(int j = 0;j<p;j++)
            {
                int x = random.nextInt(n);
                double y = data[x];

                matrixData2[j] = y;

                for(int k = 0;k<p;k++)
                {
                    double e = model[k];
                    matrixData1[j][k] = Math.pow(x, e);
                }
            }

            Matrix matrixTest = new Matrix(matrixData1);
            LUDecomposition decomp = new LUDecomposition(matrixTest);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(matrixData2,p));
                double[] coeff = coeffMatrix.getRowPackedCopy();

                PowerFunctionCombination elementalFit = new PowerFunctionCombination(model, coeff);

                double crit = elementalFit.trimmedSquares(data, residualsSquared, c);             

                if(crit < lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = elementalFit;
                }
            }       
        }   

        LTS fit = new LTSOne1DArray(data, bestFit, lowestCriterion, c);

        return fit; 
    }
}