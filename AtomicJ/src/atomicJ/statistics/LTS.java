
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
import atomicJ.functions.Constant.ConstantExactFitFactory;
import atomicJ.functions.IntegerPowerFunction.IntegerPowerFunctionExactFitFactory;
import atomicJ.functions.InterceptlessLine.InterceptlessLineExactFitFactory;
import atomicJ.functions.Line.LineExactFitFactory;
import atomicJ.functions.PowerFunction.PowerFunctionExactFitFactory;
import atomicJ.functions.Quadratic.QuadraticExactFitFactory;
import atomicJ.utilities.*;

public abstract class LTS implements HighBreakdownEstimator
{
    public static LTS findFit(double[][] data, int deg, int nstarts)
    {
        return findFit(data, deg, true, MathUtilities.minimalCoverage(data.length, deg), nstarts);        
    }

    public static LTS findFit(double[][] data, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, deg + MathUtilities.boole(constant));
        return findFit(data, deg, constant, h, nstarts);
    }

    public static LTS findFit(double[][] data, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), data, h, nstarts);
    }

    public static LTS findFit(double[][] data, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), data, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(double[][] data, double[] model, int nstarts)
    {
        return findFit(data, model, MathUtilities.minimalCoverage(data.length, model.length),nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(double[][] data, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(double[][] data, int from, int to, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), data, from, to, h, nstarts);
    }   

    public static LTS findFit(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), ys, xs, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(double[] ys, double[] xs, int from, int to, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), ys, xs, from, to, h, nstarts);
    }


    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[][] data, int exponent, int nstarts)
    {
        return findFitForSingleExponent(data, exponent, MathUtilities.minimalCoverage(data.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[][] data, int exponent, double h, int nstarts)
    {
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[] ys, double[] xs, int exponent, int nstarts)
    {
        return findFitForSingleExponent(ys, xs, exponent, MathUtilities.minimalCoverage(ys.length, 1), nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[] ys, double[] xs, int exponent, double h, int nstarts)
    {
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[][] data, double exponent, int nstarts)
    {
        return findFitForSingleExponent(data, exponent, MathUtilities.minimalCoverage(data.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[][] data, double exponent, double h, int nstarts)
    {
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[] ys, double[] xs, double exponent, int nstarts)
    {
        return findFitForSingleExponent(ys, xs, exponent, MathUtilities.minimalCoverage(ys.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFitForSingleExponent(double[] ys, double[] xs, double exponent, double h, int nstarts)
    {
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int deg, int nstarts)
    {
        return findObjectiveFunctionMinimum(data, deg, true, MathUtilities.minimalCoverage(data.length, deg), nstarts);        
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, int deg, int nstarts)
    {
        return findObjectiveFunctionMinimum(data, from, to, deg, true, MathUtilities.minimalCoverage(data.length, deg), nstarts);        
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, deg + MathUtilities.boole(constant));
        return findObjectiveFunctionMinimum(data, deg, constant, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, deg + MathUtilities.boole(constant));
        return findObjectiveFunctionMinimum(data, from, to, deg, constant, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int deg, boolean constant, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(PolynomialFunctionTypes.findExactFitFactory(deg, constant), data, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(PolynomialFunctionTypes.findExactFitFactory(deg, constant), data, from, to, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, double[] model, int nstarts)
    {
        return findObjectiveFunctionMinimum(data, model, MathUtilities.minimalCoverage(data.length, model.length), nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, double[] model, int nstarts)
    {
        return findObjectiveFunctionMinimum(data, from, to, model, MathUtilities.minimalCoverage(data.length, model.length), nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), data, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), data, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(ExactFitFactory fitFactory, double[][] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        FittedLinearUnivariateFunction bestFit = null;

        if(n < nstarts && fitFactory instanceof OneParameterExactFitFactory)
        {
            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                OneParameterExactFitFactory oneParameterFactory = (OneParameterExactFitFactory)fitFactory;
                FittedLinearUnivariateFunction exactFit = oneParameterFactory.getExactFit(pt[0],pt[1]);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(data, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFit = exactFit;
                    };
                }       
            }
        }
        else
        {
            Random random = new Random();

            for(int i = 0;i<nstarts;i++)
            {
                FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, data, n);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(data, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFit = exactFit;
                    };
                }       
            }

        }


        LTS fit = LTS.getInstance(data, bestFit, lowestCriterion,c);
        return fit;
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(ExactFitFactory fitFactory, double[][] data, int from, int to, double h, int nstarts)
    {
        if(from == 0 && to == data.length)
        {
            return findFit(fitFactory, data, h, nstarts);
        }

        int n = to  - from;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        FittedLinearUnivariateFunction bestFit = null;

        if(n < nstarts && fitFactory instanceof OneParameterExactFitFactory)
        {
            for(int i = from;i<to;i++)
            {
                double[] pt = data[i];
                OneParameterExactFitFactory oneParameterFactory = (OneParameterExactFitFactory)fitFactory;
                FittedLinearUnivariateFunction exactFit = oneParameterFactory.getExactFit(pt[0],pt[1]);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(data, from, to, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFit = exactFit;
                    };
                }       
            }
        }
        else
        {
            Random random = new Random();

            for(int i = 0;i<nstarts;i++)
            {
                FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, data, from, to);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(data, from, to, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFit = exactFit;
                    };
                }       
            }
        }

        LTS fit = LTS.getInstance(data, from, to, bestFit, lowestCriterion,c);
        return fit;
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findFit(ExactFitFactory fitFactory, double[] ys, double[] xs, int from, int to, double h, int nstarts)
    {
        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        FittedLinearUnivariateFunction bestFit = null;

        if(n < nstarts && fitFactory instanceof OneParameterExactFitFactory)
        {
            for(int i = from;i<to;i++)
            {
                OneParameterExactFitFactory oneParameterFactory = (OneParameterExactFitFactory)fitFactory;

                FittedLinearUnivariateFunction exactFit = oneParameterFactory.getExactFit(xs[i], ys[i]);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(ys, xs, from, to, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFit = exactFit;
                    };
                }       
            }

        }
        else
        {
            Random random = new Random();

            for(int i = 0;i<nstarts;i++)
            {
                FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, ys, xs, from, to);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(ys, xs, from, to, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFit = exactFit;
                    };
                }       
            }

        }
        LTS fit = LTS.getInstance(ys, xs, from, to, bestFit, lowestCriterion,c);
        return fit;
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, double exponent,  int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        LTS lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }  

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent( double[][] data, int from, int to, double exponent, double h, int nstarts)
    {
        LTS lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        LTS lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, double h, int nstarts)
    {
        LTS lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, double exponent,  int nstarts)
    {
        double h = MathUtilities.minimalCoverage(ys.length, 1);

        LTS lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }  

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, double exponent, double h, int nstarts)
    {
        LTS lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, int exponent, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(ys.length, 1);

        LTS lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, int exponent, double h, int nstarts)
    {
        LTS lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int from, int to,int deg, boolean constant, double h, int nstarts)
    {
        LTS lts = findFit(ys, xs, from, to, deg, constant,h, nstarts);
        return lts.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int deg, boolean constant, int nstarts)
    {
        int n = ys.length;
        int p = deg + MathUtilities.boole(constant);
        double h = MathUtilities.minimalCoverage(n, p);

        LTS lta = findFit(ys, xs, 0, ys.length, deg, constant,h, nstarts);
        return lta.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(ExactFitFactory fitFactory, double[] ys, double[] xs, int from, int to, double h, int nstarts)
    {
        LTS lts =findFit(fitFactory, ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction( double[][] points, int from, int to, int deg, boolean constant,double h, int nstarts)
    {
        LTS lts =findFit(points, from, to, deg, constant, h, nstarts);
        return lts.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int deg, boolean constant, int nstarts)
    {
        int n = points.length;
        int p = deg + MathUtilities.boole(constant);
        double h = MathUtilities.minimalCoverage(n, p);

        LTS lta = findFit(points, deg, constant,h, nstarts);
        return lta.getBestFit();
    }


    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(ExactFitFactory fitFactory, double[][] points, int from, int to, double h, int nstarts)
    {
        LTS lts =findFit(fitFactory, points, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    //this is roughly 3% faster then the analogous method with from - to
    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[][] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, data, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(data, residualsSquared, c);

                if(crit < lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }       
        }

        return lowestCriterion;
    }


    //does not modify data
    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[][] data, int from, int to, double h, int nstarts)
    {
        if(from == 0 && to == data.length)
        {
            return findObjectiveFunctionMinimum(fitFactory, data, h, nstarts);
        }

        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, data, from, to);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(data, from, to, residualsSquared, c);

                if(crit < lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }       
        }

        return lowestCriterion;
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(ys.length, deg + MathUtilities.boole(constant));
        return findObjectiveFunctionMinimum(PolynomialFunctionTypes.findExactFitFactory(deg, constant), ys, xs, from,to, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(PolynomialFunctionTypes.findExactFitFactory(deg, constant), ys, xs, from,to, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), ys, xs, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), ys, xs, from, to, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[] ys, double[] xs, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(fitFactory, ys, xs, ys.length, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[] ys, double[] xs, int n, double h, int nstarts)
    {
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, ys, xs, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(ys, xs, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }       
        }

        return lowestCriterion;
    }


    //does not modify data
    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[] ys, double[] xs, int from, int to, double h, int nstarts)
    {
        if(from == 0 && to == ys.length)
        {
            return findObjectiveFunctionMinimum(fitFactory, ys, xs, h, nstarts);
        }

        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, ys, xs, from, to);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(ys, xs, from, to, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }       
        }

        return lowestCriterion;
    }


    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findLinearFitWithConstant(double[][] data, double h, int nstarts)
    {
        return findFit(LineExactFitFactory.getInstance(), data, h, nstarts);
    }

    public static LTS findLinearFitWithConstant(double[][] data, int from, int to, double h, int nstarts)
    {
        return findFit(LineExactFitFactory.getInstance(), data, from, to, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumLinearFitWithConstant(double[][] data, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(LineExactFitFactory.getInstance(), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findLinearFitThroughOrigin(double[][] points, double h, int nstarts)
    {
        return findFit(InterceptlessLineExactFitFactory.getInstance(), points, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumLinearFitThroughOrigin(double[][] points, int nstarts)
    {
        return findObjectiveFunctionMinimum(InterceptlessLineExactFitFactory.getInstance(), points, MathUtilities.minimalCoverage(points.length, 1), nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumLinearFitThroughOrigin(double[] ys, double[] xs, int n, int nstarts)
    {
        return findObjectiveFunctionMinimum(InterceptlessLineExactFitFactory.getInstance(), ys, xs, n, MathUtilities.minimalCoverage(n, 1), nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumLinearFitThroughOrigin(double[][] points, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(InterceptlessLineExactFitFactory.getInstance(), points, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static LTS findConstantFit(double[][] data, double h, int nstarts)
    {
        return findFit(ConstantExactFitFactory.getInstance(), data, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumConstantFit(double[][] data, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ConstantExactFitFactory.getInstance(), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    //Constructs an LTS object, which represent quadratic fit, i.e. quadratic function with an intercept is fitted
    public static LTS findFullQuadraticFit(double[][] data, double h, int nstarts)
    {  
        return findFit(QuadraticExactFitFactory.getInstance(), data, h, nstarts);
    }

    public static LTS findFullQuadraticFit(double[][] data,int from, int to, double h, int nstarts)
    {  
        return findFit(QuadraticExactFitFactory.getInstance(), data, from, to, h, nstarts);
    }

    public static LTS getLTSforFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {
        return getLTSforFittedFunction(data, function, MathUtilities.minimalCoverage(data.length, function.getEstimatedParameterCount()));
    }

    public static LTS getLTSforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double h)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double crit = function.trimmedSquares(data, new double[n], c);

        LTS fit = LTS.getInstance(data, function, crit, c);

        return fit; 
    }

    public static double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function)
    {
        return getObjectiveFunctionValue(data, 0, data.length, function);
    }

    public static double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
    {
        int n = to - from;
        int p = function.getEstimatedParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getObjectiveFunctionValue(data, from, to, function, h);
    }

    public static double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function, double h)
    {
        return getObjectiveFunctionValue(data, 0, data.length, function, h); 
    }

    public static double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function, double h)
    {
        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double[] squaredResiduals = new double[n];        
        double crit = function.trimmedSquares(data, from, to, squaredResiduals, c);        

        return crit; 
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function)
    {
        int n = ys.length;
        int p = function.getEstimatedParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getObjectiveFunctionValue(ys, xs, function, h);
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
    {
        int n = to - from;
        int p = function.getEstimatedParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getObjectiveFunctionValue(ys, xs, from, to, function, h);
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function, double h)
    {
        return getObjectiveFunctionValue(ys, xs, 0, ys.length, function, h);
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function, double h)
    {
        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double[] squaredResiduals = new double[n];        

        for(int i = from;i<to;i++)
        {
            double x = xs[i];
            double y = ys[i];
            double r = y - function.value(x);
            squaredResiduals[i - from] = r*r;
        }

        Selector.sortSmallest(squaredResiduals, c - 1);

        double crit = ArrayUtilities.total(squaredResiduals, c);

        return crit; 
    }


    protected final int coveredCount;
    private final double lowestCriterion;
    private final FittedLinearUnivariateFunction bestFit;

    LTS(FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        this.bestFit = bestFit;
        this.lowestCriterion = lowestCriterion;
        this.coveredCount = c;
    }

    public static LTS getInstance(double[][] data,  FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        return new LTSOne2DArray(data, bestFit, lowestCriterion, c);
    }

    public static LTS getInstance(double[][] data, int from, int to, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        if(from == 0 && data.length == to)
        {
            return new LTSOne2DArray(data, bestFit, lowestCriterion, c);
        }
        return new LTSOne2DArrayRange(data, from, to, bestFit, lowestCriterion, c);
    }

    public static LTS getInstance(double[] ys, double[] xs, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        return new LTSTwo1DArray(ys, xs, bestFit, lowestCriterion, c);
    }

    public static LTS getInstance(double[] ys, double[] xs, int from, int to, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        if(from == 0 && ys.length == to)
        {
            return new LTSTwo1DArray(ys, xs, bestFit, lowestCriterion, c);
        }
        return new LTSTwo1DArrayRange(ys, xs, from, to, bestFit, lowestCriterion, c);
    }

    public abstract int getDataCount();

    @Override
    public FittedLinearUnivariateFunction getBestFit()
    {
        return bestFit;
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return lowestCriterion;
    }

    @Override
    public abstract ResidualVector getResiduals(); 

    public int getCoveredCount()
    {
        return coveredCount;
    }

    @Override
    public double getCoverage()
    {
        double n = getDataCount();
        double h = coveredCount/n;
        return h;
    }

    public double getLargestClusterOfCoveredCases()
    {
        double[] coveredCasesXCoordinates = getCoveredCasesXCoordinates();

        Arrays.sort(coveredCasesXCoordinates);

        return getLargestClusterOfCoveredCases(coveredCasesXCoordinates, coveredCount, getDataCount());
    }

    public static double getLargestClusterOfCoveredCases(double[] coveredCasesXCoordinatesSorted, int coveredCount, int dataCount)
    {     
        double minX = coveredCasesXCoordinatesSorted[0];
        //its sorted in ascending order with respect to the x - coordinates
        double xRangeLength = coveredCasesXCoordinatesSorted[coveredCount - 1] - minX;
        double eps = 15*xRangeLength/dataCount;

        double largestClusterLength = 0;
        double currentClusterLeftX = minX;
        double previousX = minX;

        for(int i = 1; i<coveredCount;i++)
        {
            double x = coveredCasesXCoordinatesSorted[i];

            double dx = x - previousX;

            if(dx <= eps)
            {
                double currentClusterLength = x - currentClusterLeftX;
                if(currentClusterLength > largestClusterLength)
                {
                    largestClusterLength = currentClusterLength;
                }
            }
            else
            {
                currentClusterLeftX = x;
            }

            previousX = x;   
        }

        return currentClusterLeftX;
    }

    public static double[] getCoveredCasesXCoordinatesOverwrittingPassedArrays(double[] ys, double[] xs, int coveredCount, FittedLinearUnivariateFunction fit)
    {
        SelectorFlatArrayAbsoluteResiduals.sortSmallest(ys, xs, coveredCount - 1, fit);         
        return xs;
    }

    @Override
    public abstract double[][] getCoveredCases();

    public abstract double[] getCoveredCasesXCoordinates();

    @Override
    public double[] getLastCoveredPoint()
    {
        double[][] coveredCases = getCoveredCases();
        int index = ArrayUtilities.getMaximumXIndex(coveredCases);

        return coveredCases[index];
    }

    private static class LTSOne2DArray extends LTS
    {
        private final double[][] data;

        LTSOne2DArray(double[][] data, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
        {
            super(bestFit, lowestCriterion, c);

            this.data = data;
        }

        @Override
        public double[][] getCoveredCases()
        {
            double[][] dataCopy = Arrays.copyOf(data, data.length);

            int coveredCount = getCoveredCount();

            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[][] covered = Arrays.copyOf(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public double[] getCoveredCasesXCoordinates()
        {
            double[][] dataCopy = Arrays.copyOf(data, data.length);

            int coveredCount = getCoveredCount();
            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[] covered = ArrayUtilities.getXCoordinateCopy(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public ResidualVector getResiduals()
        {
            return ResidualVector.getInstance(data, data.length, getBestFit());
        }

        @Override
        public int getDataCount() 
        {
            return data.length;
        }     
    }

    private static class LTSOne2DArrayRange extends LTS
    {
        private final double[][] data;
        private final int from;
        private final int to;

        LTSOne2DArrayRange(double[][] data, int from, int to, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
        {
            super(bestFit, lowestCriterion, c);

            this.data = data;
            this.from = from;
            this.to = to;
        }

        @Override
        public double[][] getCoveredCases()
        {
            double[][] dataRangeCopy = Arrays.copyOfRange(data, from, to);

            int coveredCount = getCoveredCount();

            SelectorArray.sortSmallest(dataRangeCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[][] covered = Arrays.copyOf(dataRangeCopy, coveredCount);

            return covered;
        }

        @Override
        public double[] getCoveredCasesXCoordinates()
        {
            double[][] dataRangeCopy = Arrays.copyOfRange(data, from, to);

            int coveredCount = getCoveredCount();
            SelectorArray.sortSmallest(dataRangeCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[] covered = ArrayUtilities.getXCoordinateCopy(dataRangeCopy, coveredCount);

            return covered;
        }

        @Override
        public ResidualVector getResiduals()
        {
            return ResidualVector.getInstance(data, from, to, getBestFit());
        }

        @Override
        public int getDataCount() 
        {
            int n = to - from;
            return n;
        }     
    }

    private static class LTSTwo1DArray extends LTS
    {
        private final double[] ys;
        private final double[] xs;

        LTSTwo1DArray(double[] ys, double[] xs, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
        {
            super(bestFit, lowestCriterion, c);

            this.ys = ys;
            this.xs = xs;
        }

        @Override
        public double[][] getCoveredCases()
        {
            double[][] dataCopy = ArrayUtilities.convertToPoints(ys, xs);

            int coveredCount = getCoveredCount();

            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[][] covered = Arrays.copyOf(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public double[] getCoveredCasesXCoordinates()
        {
            double[][] dataCopy = ArrayUtilities.convertToPoints(ys, xs);

            int coveredCount = getCoveredCount();
            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));

            double[] covered = ArrayUtilities.getXCoordinateCopy(dataCopy, coveredCount);

            return covered;     
        }

        @Override
        public ResidualVector getResiduals()
        {
            return ResidualVector.getInstance(ys, xs, ys.length, getBestFit());
        }

        @Override
        public int getDataCount() 
        {
            return ys.length;
        }     
    }

    private static class LTSTwo1DArrayRange extends LTS
    {
        private final double[] ys;
        private final double[] xs;
        private final int from;
        private final int to;

        LTSTwo1DArrayRange(double[] ys, double[] xs, int from, int to, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
        {
            super(bestFit, lowestCriterion, c);

            this.ys = ys;
            this.xs = xs;
            this.from = from;
            this.to = to;
        }

        @Override
        public double[][] getCoveredCases()
        {
            double[][] dataCopy = ArrayUtilities.convertToPoints(ys, xs, from, to);

            int coveredCount = getCoveredCount();

            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[][] covered = Arrays.copyOf(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public double[] getCoveredCasesXCoordinates()
        {
            double[][] dataCopy = ArrayUtilities.convertToPoints(ys, xs, from, to);

            int coveredCount = getCoveredCount();
            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[] covered = ArrayUtilities.getXCoordinateCopy(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public ResidualVector getResiduals()
        {
            return ResidualVector.getInstance(ys, xs, from, to, getBestFit());
        }

        @Override
        public int getDataCount() 
        {
            int n = to - from;
            return n;
        }     
    }

    public static class LTSOne1DArray extends LTS
    {
        private final double[] ys;

        LTSOne1DArray(double[] ys, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
        {
            super(bestFit, lowestCriterion, c);

            this.ys = ys;
        }

        @Override
        public double[][] getCoveredCases()
        {
            double[][] dataCopy = ArrayUtilities.convertToPoints(ys);

            int coveredCount = getCoveredCount();

            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[][] covered = Arrays.copyOf(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public double[] getCoveredCasesXCoordinates()
        {
            double[][] dataCopy = ArrayUtilities.convertToPoints(ys);

            int coveredCount = getCoveredCount();
            SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(getBestFit()));
            double[] covered = ArrayUtilities.getXCoordinateCopy(dataCopy, coveredCount);

            return covered;
        }

        @Override
        public ResidualVector getResiduals()
        {
            return ResidualVector.getInstance(ys,ys.length, getBestFit());
        }

        @Override
        public int getDataCount() 
        {
            return ys.length;
        }
    }
}