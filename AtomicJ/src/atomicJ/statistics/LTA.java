
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

public abstract class LTA implements HighBreakdownEstimator
{
    public static LTA findFit(double[][] data, int deg, int nstarts)
    {
        return findFit(data, deg, true, MathUtilities.minimalCoverage(data.length, deg), nstarts);        
    }

    public static LTA findFit(double[][] data, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, deg + MathUtilities.boole(constant));
        return findFit(data, deg, constant, h, nstarts);
    }

    public static LTA findFit(double[][] data, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), data, h, nstarts);
    }

    public static LTA findFit(double[][] data, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), data, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(double[][] data, double[] model, int nstarts)
    {
        return findFit(data, model, MathUtilities.minimalCoverage(data.length, model.length),nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(double[][] data, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(double[][] data, int from, int to, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), data, from, to, h, nstarts);
    }   

    public static LTA findFit(double[] ys, double[] xs, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), ys, xs, 0, ys.length, h, nstarts);
    }

    public static LTA findFit(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), ys, xs, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(double[] ys, double[] xs, int from, int to, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), ys, xs, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[][] data, int exponent, int nstarts)
    {
        return findFitForSingleExponent(data, exponent, MathUtilities.minimalCoverage(data.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[][] data, int exponent, double h, int nstarts)
    {
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[] ys, double[] xs, int exponent, int nstarts)
    {
        return findFitForSingleExponent(ys, xs, exponent, MathUtilities.minimalCoverage(ys.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[] ys, double[] xs, int exponent, double h, int nstarts)
    {
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[][] data, double exponent, int nstarts)
    {
        return findFitForSingleExponent(data, exponent, MathUtilities.minimalCoverage(data.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[][] data, double exponent, double h, int nstarts)
    {
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[] ys, double[] xs, double exponent, int nstarts)
    {
        return findFitForSingleExponent(ys, xs, exponent, MathUtilities.minimalCoverage(ys.length, 1),nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFitForSingleExponent(double[] ys, double[] xs, double exponent, double h, int nstarts)
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
    public static LTA findFit(ExactFitFactory fitFactory, double[][] data, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, fitFactory.getEstimatedParameterCount());
        return findFit(fitFactory, data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(ExactFitFactory fitFactory, double[][] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        FittedLinearUnivariateFunction bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, data, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedAbsoluteDeviations(data, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = exactFit;
                };
            }       
        }

        LTA fit = LTA.getInstance(data, bestFit, lowestCriterion,c);
        return fit;
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(ExactFitFactory fitFactory, double[][] data, int from, int to, double h, int nstarts)
    {
        if(from == 0 && to == data.length)
        {
            return findFit(fitFactory, data, h, nstarts);
        }

        int n = to  - from;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        FittedLinearUnivariateFunction bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, data, from, to);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedAbsoluteDeviations(data, from, to, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = exactFit;
                };
            }       
        }

        LTA fit = LTA.getInstance(data, from, to, bestFit, lowestCriterion,c);
        return fit;
    }

    public static LTA findFit(ExactFitFactory fitFactory, double[] ys, double[] xs, int from, int to, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(ys.length, fitFactory.getEstimatedParameterCount());
        return findFit(fitFactory, ys,xs, from, to, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findFit(ExactFitFactory fitFactory, double[] ys, double[] xs, int from, int to, double h, int nstarts)
    {
        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[n];
        Random random = new Random();
        FittedLinearUnivariateFunction bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, ys, xs, from, to);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedAbsoluteDeviations(ys, xs, from, to, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = exactFit;
                };
            }       
        }

        LTA fit = LTA.getInstance(ys, xs, from, to, bestFit, lowestCriterion,c);
        return fit;
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
                double crit = exactFit.trimmedAbsoluteDeviations(data, residualsSquared, c);

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
                double crit = exactFit.trimmedAbsoluteDeviations(data, from, to, residualsSquared, c);

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
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), ys, xs, ys.length, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), ys, xs, from, to, h, nstarts);
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
                double crit = exactFit.trimmedAbsoluteDeviations(ys, xs, residualsSquared, c);

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
        if(from == 0)
        {
            return findObjectiveFunctionMinimum(fitFactory, ys, xs, to, h, nstarts);
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
                double crit = exactFit.trimmedAbsoluteDeviations(ys, xs, from, to, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }       
        }

        return lowestCriterion;
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, double exponent,  int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        LTA lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }  

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, double exponent, double h, int nstarts)
    {
        LTA lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        LTA lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, double h, int nstarts)
    {
        LTA lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), data, from, to, h, nstarts);
        return lts.getBestFit();
    }


    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, double exponent,  int nstarts)
    {
        double h = MathUtilities.minimalCoverage(ys.length, 1);

        LTA lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }  

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, double exponent, double h, int nstarts)
    {
        LTA lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, int exponent, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(ys.length, 1);

        LTA lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int from, int to, int exponent, double h, int nstarts)
    {
        LTA lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent), ys, xs, from, to, h, nstarts);
        return lts.getBestFit();
    }



    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int deg, boolean constant, double h, int nstarts)
    {
        LTA lta = findFit(ys, xs, deg, constant,h, nstarts);
        return lta.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int deg, boolean constant, int nstarts)
    {
        int n = ys.length;
        int p = deg + MathUtilities.boole(constant);
        double h = MathUtilities.minimalCoverage(n, p);

        LTA lta = findFit(ys, xs, deg, constant,h, nstarts);
        return lta.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction( double[] ys, double[] xs, int from, int to,int deg, boolean constant, double h, int nstarts)
    {
        LTA lts = findFit(ys, xs, from, to, deg, constant,h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(ExactFitFactory fitFactory, double[] ys, double[] xs, int from, int to, double h, int nstarts)
    {
        LTA lta = findFit(fitFactory, ys, xs, from, to, h, nstarts);
        return lta.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] points,  int deg, boolean constant,double h, int nstarts)
    {
        LTA lta = findFit(points, deg, constant, h, nstarts);
        return lta.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int deg, boolean constant, int nstarts)
    {
        int n = points.length;
        int p = deg + MathUtilities.boole(constant);
        double h = MathUtilities.minimalCoverage(n, p);

        LTA lta = findFit(points, deg, constant,h, nstarts);
        return lta.getBestFit();
    }


    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int from, int to, int deg, boolean constant,double h, int nstarts)
    {
        LTA lta = findFit(points, from, to, deg, constant, h, nstarts);
        return lta.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(ExactFitFactory fitFactory, double[][] points, int from, int to, double h, int nstarts)
    {
        LTA lts = findFit(fitFactory, points, from, to, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findLinearFitWithConstant(double[][] data, double h, int nstarts)
    {
        return findFit(LineExactFitFactory.getInstance(), data, h, nstarts);
    }

    public static LTA findLinearFitWithConstant(double[][] data, int from, int to, double h, int nstarts)
    {
        return findFit(LineExactFitFactory.getInstance(), data, from, to, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumLinearFitWithConstant(double[][] data, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(LineExactFitFactory.getInstance(), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    public static LTA findLinearFitThroughOrigin(double[][] points, double h, int nstarts)
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
    //but passes them to LTA object uncopied
    public static LTA findConstantFit(double[][] data, double h, int nstarts)
    {
        return findFit(ConstantExactFitFactory.getInstance(), data, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimumConstantFit(double[][] data, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ConstantExactFitFactory.getInstance(), data, h, nstarts);
    }

    //does not modify data
    //but passes them to LTA object uncopied
    //Constructs an LTA object, which represent quadratic fit, i.e. quadratic function with an intercept is fitted
    public static LTA findFullQuadraticFit(double[][] data, double h, int nstarts)
    {  
        return findFit(QuadraticExactFitFactory.getInstance(), data, h, nstarts);
    }

    public static LTA findFullQuadraticFit(double[][] data,int from, int to, double h, int nstarts)
    {  
        return findFit(QuadraticExactFitFactory.getInstance(), data, from, to, h, nstarts);
    }

    public static LTA getLTAforFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {
        return getLTAforFittedFunction(data, function, MathUtilities.minimalCoverage(data.length, function.getEstimatedParameterCount()));
    }

    public static LTA getLTAforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double h)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double crit = function.trimmedAbsoluteDeviations(data, new double[n], c);

        LTA fit = LTA.getInstance(data, function, crit, c);

        return fit; 
    }

    public static double getObjectiveFunctionValue(double[][] points, FittedUnivariateFunction function)
    {
        return getObjectiveFunctionValue(points, 0, points.length, function);
    }

    public static double getObjectiveFunctionValue(double[][] points, int from, int to, FittedUnivariateFunction function)
    {
        int n = to - from;
        int p = function.getEstimatedParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getObjectiveFunctionValue(points, from, to, function, h);
    }

    public static double getObjectiveFunctionValue(double[][] points, FittedUnivariateFunction function, double h)
    {
        return getObjectiveFunctionValue(points, 0, points.length, function, h); 
    }

    public static double getObjectiveFunctionValue(double[][] points, int from, int to, FittedUnivariateFunction function, double h)
    {
        int n = to - from;
        int c = (int)Math.min(n, h*n);

        double[] absResiduals = new double[n];              
        double crit = function.trimmedAbsoluteDeviations(points, from, to, absResiduals, c);

        return crit; 
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function)
    {
        return getObjectiveFunctionValue(ys, xs, 0, ys.length, function);
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

        double[] absResiduals = new double[n];        

        for(int i = from;i<to;i++)
        {
            double x = xs[i];
            double y = ys[i];
            double r = y - function.value(x);
            absResiduals[i - from] = Math.abs(r);
        }

        Selector.sortSmallest(absResiduals, c - 1);

        double crit = ArrayUtilities.total(absResiduals, c);

        return crit; 
    }

    protected final int coveredCount;
    private final double lowestCriterion;
    private final FittedLinearUnivariateFunction bestFit;

    LTA(FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        this.bestFit = bestFit;
        this.lowestCriterion = lowestCriterion;
        this.coveredCount = c;
    }

    public static LTA getInstance(double[][] data,  FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        return new LTAOne2DArray(data, bestFit, lowestCriterion, c);
    }

    public static LTA getInstance(double[][] data, int from, int to, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        if(from == 0 && data.length == to)
        {
            return new LTAOne2DArray(data, bestFit, lowestCriterion, c);
        }
        return new LTAOne2DArrayRange(data, from, to, bestFit, lowestCriterion, c);
    }

    public static LTA getInstance(double[] ys, double[] xs, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        return new LTATwo1DArray(ys, xs, bestFit, lowestCriterion, c);
    }

    public static LTA getInstance(double[] ys, double[] xs, int from, int to, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        if(from == 0 && ys.length == to)
        {
            return new LTATwo1DArray(ys, xs, bestFit, lowestCriterion, c);
        }
        return new LTATwo1DArrayRange(ys, xs, from, to, bestFit, lowestCriterion, c);
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

        int n = coveredCasesXCoordinates.length;
        double minX = coveredCasesXCoordinates[0];
        //its sorted in ascending order with respect to the x - coordinates
        double xRangeLength = coveredCasesXCoordinates[n - 1] - minX;
        double eps = 15*xRangeLength/getDataCount();

        double largestClusterLength = 0;
        double currentClusterLeftX = minX;
        double previousX = minX;

        for(int i = 1; i<n;i++)
        {
            double x = coveredCasesXCoordinates[i];

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

    private static class LTAOne2DArray extends LTA
    {
        private final double[][] data;

        LTAOne2DArray(double[][] data, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
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

    private static class LTAOne2DArrayRange extends LTA
    {
        private final double[][] data;
        private final int from;
        private final int to;

        LTAOne2DArrayRange(double[][] data, int from, int to, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
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

    private static class LTATwo1DArray extends LTA
    {
        private final double[] ys;
        private final double[] xs;

        LTATwo1DArray(double[] ys, double[] xs, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
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

    private static class LTATwo1DArrayRange extends LTA
    {
        private final double[] ys;
        private final double[] xs;
        private final int from;
        private final int to;

        LTATwo1DArrayRange(double[] ys, double[] xs, int from, int to, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
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

    public static class LTAOne1DArray extends LTA
    {
        private final double[] ys;

        LTAOne1DArray(double[] ys, FittedLinearUnivariateFunction bestFit,double lowestCriterion, int c)
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
            double[][] dataCopy =ArrayUtilities.convertToPoints(ys);

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