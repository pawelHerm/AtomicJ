
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
import atomicJ.functions.IntegerPowerFunction.IntegerPowerFunctionExactFitFactory;
import atomicJ.functions.PowerFunction.PowerFunctionExactFitFactory;
import atomicJ.utilities.*;

public class SupportedLTS implements LinearRegressionEsimator
{
    private static int getDefaultNumberOfStarts(double h, int p, double lim)
    {
        int nstarts = (int)Math.ceil(Math.log(lim)/Math.log((1 - Math.pow(h, lim))));
        return nstarts;
    }

    public static SupportedLTS findFit(double[][] optional, double[][] support, int deg, int nstarts)
    {
        return findFit(optional, support, deg, true, MathUtilities.minimalCoverage(optional.length, deg + 1), nstarts);        
    }

    public static SupportedLTS findFit(double[][] optional, double[][] support, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(optional.length, deg + MathUtilities.boole(constant));
        return findFit(optional, support, deg, constant, h, nstarts);
    }

    public static SupportedLTS findFit(double[][] optional, double[][] support, int deg, boolean constant, double h, int nstarts)
    {
        return findFit(PolynomialFunctionTypes.findExactFitFactory(deg, constant), optional, support, h, nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static SupportedLTS findFit(double[][] optional, double[][] support, double[] model, int nstarts)
    {
        return findFit(optional, support, model, MathUtilities.minimalCoverage(optional.length, model.length),nstarts);
    }

    //does not modify data
    //but passes them to LTS object uncopied
    public static SupportedLTS findFit(double[][] optional, double[][] support, double[] model, double h, int nstarts)
    {
        return findFit(ExponentialFunctionCombinationTypes.findExactFitFactory(model), optional, support, h, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] optional, double[][] support, double exponent,  int nstarts)
    {
        double h = MathUtilities.minimalCoverage(optional.length, 1);

        SupportedLTS lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent),optional, support,  h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] optional, double[][] support,  double exponent, double h, int nstarts)
    {
        SupportedLTS lts = findFit(PowerFunctionExactFitFactory.getInstance(exponent),optional, support,  h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] points, int supportLength,  double exponent, double h, int nstarts)
    {
        return findFitFunction(PowerFunctionExactFitFactory.getInstance(exponent),points, supportLength, h, nstarts);
    } 

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] optional, double[][] support, int exponent, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(optional.length, 1);

        SupportedLTS lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent),optional, support,  h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] optional, double[][] support, int exponent, double h,  int nstarts)
    {
        SupportedLTS lts = findFit(IntegerPowerFunctionExactFitFactory.getInstance(exponent),optional, support,  h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] points, int supportLength, int exponent, double h, int nstarts)
    {
        return findFitFunction(IntegerPowerFunctionExactFitFactory.getInstance(exponent),points, supportLength, h, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] optional, double[][] support, int deg, boolean constant, double h, int nstarts)
    {
        SupportedLTS lts = findFit(optional, support, deg, constant, h, nstarts);
        return lts.getBestFit();
    }    

    public static FittedLinearUnivariateFunction findFitFunction(double[] pointsYs, double[] pointsXs, int supportLength, int deg, boolean constant, double h, int nstarts)
    {
        return findFitFunction(PolynomialFunctionTypes.findExactFitFactory(deg, constant), pointsYs, pointsXs, supportLength, h, nstarts);
    }  

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int supportLength, int deg, boolean constant, double h, int nstarts)
    {
        return findFitFunction(PolynomialFunctionTypes.findExactFitFactory(deg, constant),points, supportLength, h, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] optional, double[][] support, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(optional.length, deg + MathUtilities.boole(constant));

        SupportedLTS lts = findFit(optional, support, deg, constant, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] optional, double[][] support,  int deg, boolean constant, double k, double h, int nstarts)
    {
        SupportedLTS lts = findFit(optional, support, deg, constant, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, int deg, boolean constant, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(optional.length, deg + MathUtilities.boole(constant));
        return findObjectiveFunctionMinimum(optional, support, deg, constant, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, int deg, boolean constant, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(PolynomialFunctionTypes.findExactFitFactory(deg, constant), optional, support, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, int from, int to, int deg, boolean constant, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(PolynomialFunctionTypes.findExactFitFactory(deg, constant), optional, support, h, nstarts);
    }


    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, double[] model)
    {
        double h = MathUtilities.minimalCoverage(optional.length, model.length);
        int nstarts = getDefaultNumberOfStarts(h, model.length, 0.0001);

        return findObjectiveFunctionMinimum(optional, support, model, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, double[] model, int nstarts)
    {
        return findObjectiveFunctionMinimum(optional, support, model, MathUtilities.minimalCoverage(optional.length, model.length), nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), optional, support, h, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] optional, double[][] support, int from, int to, double[] model, double h, int nstarts)
    {
        return findObjectiveFunctionMinimum(ExponentialFunctionCombinationTypes.findExactFitFactory(model), optional, support, h, nstarts);
    }

    public static SupportedLTS findFit(ExactFitFactory fitFactory, double[][] optional, double[][] support, double h, int nstarts)
    {        
        int optionalLength = optional.length;
        int c = (int)Math.min(optionalLength, h*optionalLength);
        int n = optional.length + support.length;

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[optionalLength];
        Random random = new Random();
        FittedLinearUnivariateFunction bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, optional, support, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(optional, support, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = exactFit;
                };
            }

        }

        SupportedLTS fit = SupportedLTS.getInstance(optional, support, bestFit,lowestCriterion);
        return fit;
    }

    public static FittedLinearUnivariateFunction findFitFunction(ExactFitFactory fitFactory, double[][] points, int supportLength, double h, int nstarts)
    {        
        int optionalLength = points.length - supportLength;
        int c = (int)Math.min(optionalLength, h*optionalLength);
        int n = points.length;

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[optionalLength];
        Random random = new Random();
        FittedLinearUnivariateFunction bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, points, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(points, supportLength, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = exactFit;
                };
            }

        }

        return bestFit;
    }

    public static FittedLinearUnivariateFunction findFitFunction(ExactFitFactory fitFactory, double[] pointsYs, double[] pointsXs, int supportLength, double h, int nstarts)
    {        
        int n = pointsYs.length;
        int optionalLength = n - supportLength;
        int c = (int)Math.min(optionalLength, h*optionalLength);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[optionalLength];
        Random random = new Random();
        FittedLinearUnivariateFunction bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, pointsYs, pointsXs, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(pointsYs, pointsXs, supportLength, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = exactFit;
                };
            }
        }

        return bestFit;
    }


    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[][] optional, double[][] support)
    {
        int p = fitFactory.getEstimatedParameterCount();
        double h = MathUtilities.minimalCoverage(optional.length, p);
        int nstarts = getDefaultNumberOfStarts(h, p, 0.0001);

        return findObjectiveFunctionMinimum(fitFactory, optional, support, h, nstarts);
    }

    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[][] optional, double[][] support, double h, int nstarts)
    {        
        int optionalLength = optional.length;
        int n = optionalLength + support.length;
        int c = (int)Math.min(optionalLength, h*optionalLength);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[optionalLength];
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, optional, support, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(optional, support, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }

        }

        return lowestCriterion;
    }

    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory,  double[] optionalYs, double[] optionalXs, double[] supportYs, double[] supportXs)
    {
        int p = fitFactory.getEstimatedParameterCount();
        double h = MathUtilities.minimalCoverage(optionalYs.length, p);
        int nstarts = getDefaultNumberOfStarts(h, p, 0.0001);

        return findObjectiveFunctionMinimum(fitFactory, optionalYs, optionalXs, supportYs, supportXs, h, nstarts);
    }

    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[] optionalYs, double[] optionalXs, double[] supportYs, double[] supportXs, double h, int nstarts)
    {        
        int optionalLength = optionalYs.length;
        int n = optionalLength + supportYs.length;
        int c = (int)Math.min(optionalLength, h*optionalLength);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[optionalLength];
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, optionalYs, optionalXs, supportYs, supportXs, n);
            if(exactFit != null)
            {
                double crit = exactFit.trimmedSquares(optionalYs, optionalXs, supportYs,supportXs, residualsSquared, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                };
            }

        }

        return lowestCriterion;
    }

    public static double findObjectiveFunctionMinimum(ExactFitFactory fitFactory, double[] ys, double[] xs, int supportLength, double h, int nstarts)
    {              
        int optionalLength = ys.length - supportLength;
        int c = (int)Math.min(optionalLength, h*optionalLength);

        int n = ys.length;

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSquared = new double[optionalLength];

        if(n <= nstarts && (fitFactory instanceof OneParameterExactFitFactory))
        {
            OneParameterExactFitFactory oneParameterFitFactory = (OneParameterExactFitFactory) fitFactory;
            for(int i = 0;i<n;i++)
            {
                FittedLinearUnivariateFunction exactFit = oneParameterFitFactory.getExactFit(xs[i],ys[i]);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(ys, xs, supportLength, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                    };
                }
            }
        }
        else
        {
            Random random = new Random();

            for(int i = 0;i<nstarts;i++)
            {
                FittedLinearUnivariateFunction exactFit = fitFactory.getExactFitForRandomSubsetOfPoints(random, ys, xs, n);
                if(exactFit != null)
                {
                    double crit = exactFit.trimmedSquares(ys, xs, supportLength, residualsSquared, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                    };
                }
            }
        }


        return lowestCriterion;
    }

    private final double lowestCriterion;
    private final FittedLinearUnivariateFunction bestFit;
    private final ResidualVector residuals; 

    private SupportedLTS(FittedLinearUnivariateFunction bestFit, ResidualVector residuals, double lowestCriterion)
    {
        this.bestFit = bestFit;
        this.residuals = residuals;
        this.lowestCriterion = lowestCriterion;
    }   

    public static SupportedLTS getInstance(double[][] optional, double[][] support, FittedLinearUnivariateFunction bestFit, double lowestCriterion)
    {
        ResidualVector residuals = ResidualVector.getInstance(optional, support, bestFit);
        SupportedLTS supportedLTS = new SupportedLTS(bestFit, residuals, lowestCriterion);

        return supportedLTS;
    }

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
    public ResidualVector getResiduals() 
    {
        return residuals;
    }

}