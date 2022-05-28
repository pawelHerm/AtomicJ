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


package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.statistics.HighCoverageLTA;
import atomicJ.statistics.HighCoverageLTS;
import atomicJ.statistics.L1Regression;
import atomicJ.statistics.L2Regression;
import atomicJ.statistics.LTA;
import atomicJ.statistics.LTS;
import atomicJ.statistics.LinearRegressionEsimator;


public enum BasicRegressionStrategy implements RegressionStrategy
{
    ROBUST_LTS("Robust (LTS)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return LTS.findFit(data, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return LTS.findObjectiveFunctionMinimum(data, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant)
        {
            return LTS.findObjectiveFunctionMinimum(data, from, to, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int degree, boolean constant)
        {
            return LTS.findObjectiveFunctionMinimum(ys, xs, from, to, degree, constant, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return LTS.findFit(data, model, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent)
        {
            return LTS.findFitForSingleExponent(data, exponent, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent)
        {
            return LTS.findFitForSingleExponent(data, exponent, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return LTS.findFitForSingleExponent(ys, xs, exponent, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return LTS.findFitForSingleExponent(ys, xs, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent)
        {
            return LTS.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent)
        {
            return LTS.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return LTS.findFitFunctionForSingleExponent(ys, xs, 0, ys.length, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return LTS.findFitFunctionForSingleExponent(ys, xs, 0, ys.length, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant)
        {
            return LTS.findFitFunction(data, degree, constant, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant)
        {
            return LTS.findFitFunction(ys, xs, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return LTS.findObjectiveFunctionMinimumLinearFitThroughOrigin(data, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs) 
        {
            return LTS.findObjectiveFunctionMinimumLinearFitThroughOrigin(ys, xs, ys.length, 200);
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double[] getLastCoveredPoint(double[] ys, double[] xs, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, ys, xs, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function) 
        {
            return LTS.getObjectiveFunctionValue(data, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
        {
            return LTS.getObjectiveFunctionValue(data, from, to, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function) 
        {
            return LTS.getObjectiveFunctionValue(ys, xs, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
        {
            return LTS.getObjectiveFunctionValue(ys, xs, from, to, function);
        }
    },

    ROBUST_HLTS("Robust (HLTS)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTS.findFit(data, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTS.findObjectiveFunctionMinimum(data, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant)
        {
            return HighCoverageLTS.findObjectiveFunctionMinimum(data, from, to, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int degree, boolean constant)
        {
            return HighCoverageLTS.findObjectiveFunctionMinimum(ys, xs, from, to, degree, constant, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return HighCoverageLTS.findFit(data, model, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent)
        {
            return HighCoverageLTS.findFitForSingleExponent(data, exponent, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent)
        {
            return HighCoverageLTS.findFitForSingleExponent(data, exponent, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return HighCoverageLTS.findFitForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return HighCoverageLTS.findFitForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent)
        {
            return HighCoverageLTS.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent)
        {
            return HighCoverageLTS.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return HighCoverageLTS.findFitFunctionForSingleExponent(ys, xs,  exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return HighCoverageLTS.findFitFunctionForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTS.findFitFunction(data, degree, constant, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant)
        {
            return HighCoverageLTS.findFitFunction(ys, xs, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return HighCoverageLTS.findObjectiveFunctionMinimum(data, 1, false, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs) 
        {
            return HighCoverageLTS.findObjectiveFunctionMinimum(ys, xs, 1, false, 2, 200);
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double[] getLastCoveredPoint(double[] ys, double[] xs, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, ys, xs, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function) 
        {
            return HighCoverageLTS.getObjectiveFunctionValue(data, function, 2);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
        {
            return HighCoverageLTS.getObjectiveFunctionValue(data, from, to, function, 2);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function) 
        {
            return HighCoverageLTS.getObjectiveFunctionValue(ys, xs, function, 2);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
        {
            return HighCoverageLTS.getObjectiveFunctionValue(ys, xs, from, to, function, 2);
        }
    },

    ROBUST_LTA("Robust (LTA)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return LTA.findFit(data, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return LTA.findObjectiveFunctionMinimum(data, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant)
        {
            return LTA.findObjectiveFunctionMinimum(data, from, to, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int degree, boolean constant)
        {
            return LTA.findObjectiveFunctionMinimum(ys, xs, from, to, degree, constant, 200);
        }

        @Override
        public LTA performRegression(double[][] data, double[] model)
        {
            return LTA.findFit(data, model, 200);
        }

        @Override
        public LTA performRegressionForSingleExponent(double[][] data, int exponent)
        {
            return LTA.findFitForSingleExponent(data, exponent, 200);
        }

        @Override
        public LTA performRegressionForSingleExponent(double[][] data, double exponent)
        {
            return LTA.findFitForSingleExponent(data, exponent, 200);
        }

        @Override
        public LTA performRegressionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return LTA.findFitForSingleExponent(ys, xs, exponent, 200);
        }

        @Override
        public LTA performRegressionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return LTA.findFitForSingleExponent(ys, xs, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent)
        {
            return LTA.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent)
        {
            return LTA.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 200);
        }        

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return LTA.findFitFunctionForSingleExponent(ys, xs, 0, ys.length, exponent, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent) 
        {
            return LTA.findFitFunctionForSingleExponent(ys, xs, 0, ys.length, exponent, 200);
        } 

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant)
        {
            return LTA.findFitFunction(data, degree, constant, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant)
        {
            return LTA.findFitFunction(ys, xs, degree, constant, 200);
        }

        //no need for passing a copy, as 
        //LTA.findObjectiveFunctionMinimum does not modify the passes array
        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return LTA.findObjectiveFunctionMinimumLinearFitThroughOrigin(data, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs) 
        {
            return LTA.findObjectiveFunctionMinimumLinearFitThroughOrigin(ys, xs, ys.length, 200);
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double[] getLastCoveredPoint(double[] ys, double[] xs, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, ys, xs, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function) 
        {
            return LTA.getObjectiveFunctionValue(data, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
        {
            return LTA.getObjectiveFunctionValue(data, from, to, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function)
        {
            return LTA.getObjectiveFunctionValue(ys, xs, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
        {
            return LTA.getObjectiveFunctionValue(ys, xs, from, to, function);
        }
    },

    ROBUST_HLTA("Robust (HLTA)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTA.findFit(data, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTA.findObjectiveFunctionMinimum(data, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant)
        {
            return HighCoverageLTA.findObjectiveFunctionMinimum(data, from, to, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int degree, boolean constant)
        {
            return HighCoverageLTA.findObjectiveFunctionMinimum(ys, xs, from, to, degree, constant, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return HighCoverageLTA.findFit(data, model, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent)
        {
            return HighCoverageLTA.findFitForSingleExponent(data, exponent, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent)
        {
            return HighCoverageLTA.findFitForSingleExponent(data, exponent, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return HighCoverageLTA.findFitForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return HighCoverageLTA.findFitForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTA.findFitFunction(data, degree, constant, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant)
        {
            return HighCoverageLTA.findFitFunction(ys, xs, degree, constant, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent)
        {
            return HighCoverageLTA.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent)
        {
            return HighCoverageLTA.findFitFunctionForSingleExponent(data, 0, data.length, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent) 
        {
            return HighCoverageLTA.findFitFunctionForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent) 
        {
            return HighCoverageLTA.findFitFunctionForSingleExponent(ys, xs, exponent, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return HighCoverageLTA.findObjectiveFunctionMinimum(data, 1, false, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs)
        {
            return HighCoverageLTS.findObjectiveFunctionMinimum(ys, xs, 1, false, 2, 200);
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double[] getLastCoveredPoint(double[] ys, double[] xs, UnivariateFunction f) 
        {
            return HighBreakdownTransitionPointEstimator.getTransitionPoint(f, ys, xs, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function) 
        {
            return HighCoverageLTA.getObjectiveFunctionValue(data, function, 2);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
        {
            return HighCoverageLTA.getObjectiveFunctionValue(data, from, to, function, 2);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function)
        {
            return HighCoverageLTA.getObjectiveFunctionValue(ys, xs, function, 2);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
        {
            return HighCoverageLTA.getObjectiveFunctionValue(ys, xs, from, to, function, 2);
        }
    },

    CLASSICAL_L2("Classical (L2)")
    {	
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return L2Regression.findFit(data, degree, constant);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return L2Regression.findFit(data, model);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent)
        {
            return L2Regression.findFitForSingleExponent(data, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent)
        {
            return L2Regression.findFitForSingleExponent(data, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return L2Regression.findFitForSingleExponent(ys, xs, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return L2Regression.findFitForSingleExponent(ys, xs, exponent);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return L2Regression.findObjectiveFunctionMinimum(data, degree, constant);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant)
        {
            return L2Regression.findFitedFunction(data, degree, constant);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant)
        {
            return L2Regression.findFitedFunction(ys, xs, degree, constant);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent)
        {
            return L2Regression.findFitFunctionForSingleExponent(data, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent)
        {
            return L2Regression.findFitFunctionForSingleExponent(data, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return L2Regression.findFitFunctionForSingleExponent(ys, xs, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return L2Regression.findFitFunctionForSingleExponent(ys, xs, exponent);
        }


        @Override
        public double getObjectiveFunctionMinimum(double[][] data,int from, int to, int degree, boolean constant)
        {
            return L2Regression.findObjectiveFunctionMinimum(data, from, to, degree, constant);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys, double[] xs,int from, int to, int degree, boolean constant)
        {
            return L2Regression.findObjectiveFunctionMinimum(ys, xs, from, to, degree, constant);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return L2Regression.findObjectiveFunctionMinimumDeg1NoConstant(data);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs)
        {
            return L2Regression.findObjectiveFunctionMinimumDeg1NoConstant(ys, xs);
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {			
            return data[data.length - 1];
        }	

        @Override
        public double[] getLastCoveredPoint(double[] ys, double[] xs, UnivariateFunction f) 
        {          
            int n = ys.length;
            double[] p = new double[] {xs[n - 1], ys[n - 1]};
            return p;
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function) 
        {
            return L2Regression.getObjectiveFunctionValue(data, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function) 
        {
            return L2Regression.getObjectiveFunctionValue(data, from, to, function);
        }      

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function)
        {
            return L2Regression.getObjectiveFunctionValue(ys, xs, 0, ys.length, function);
        }  

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
        {
            return L2Regression.getObjectiveFunctionValue(ys, xs, from, to, function);
        } 
    },

    CLASSICAL_L1("Classical (L1)")
    {	
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return L1Regression.findFit(data, degree, constant);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return L1Regression.findFit(data, degree, constant).getObjectiveFunctionMinimum();
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant)
        {
            return L1Regression.findFit(data, from, to, degree, constant).getObjectiveFunctionMinimum();
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys, double[] xs,int from, int to, int degree, boolean constant)
        {
            return L1Regression.findFit(ys, xs, from, to, degree, constant).getObjectiveFunctionMinimum();
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant)
        {
            return L1Regression.findFitFunction(data, degree, constant);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent)
        {
            return L1Regression.findFitFunctionForSingleExponent(data, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent)
        {
            return L1Regression.findFitFunctionForSingleExponent(data, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return L1Regression.findFitFunctionForSingleExponent(ys, xs, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent) 
        {
            return L1Regression.findFitFunctionForSingleExponent(ys, xs, exponent);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant)
        {
            return L1Regression.findFitFunction(ys, xs, degree, constant);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return L1Regression.findFit(data, model);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent)
        {
            return L1Regression.findFitForSingleExponent(data, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent)
        {
            return L1Regression.findFitForSingleExponent(data, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, int exponent)
        {
            return L1Regression.findFitForSingleExponent(ys, xs, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, double exponent)
        {
            return L1Regression.findFitForSingleExponent(ys, xs, exponent);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return L1Regression.findFit(data, new double[] {1}).getObjectiveFunctionMinimum();
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs)
        {
            return L1Regression.findFit(ys, xs, new double[] {1}).getObjectiveFunctionMinimum();
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {			
            return data[data.length - 1];
        }	

        @Override
        public double[] getLastCoveredPoint(double[] ys, double[] xs, UnivariateFunction f) 
        {          
            int n = ys.length;
            double[] p = new double[] {xs[n - 1], ys[n - 1]};
            return p;
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function) 
        {
            return L1Regression.getObjectiveFunctionValue(data, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
        {
            return L1Regression.getObjectiveFunctionValue(data, from, to, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function)
        {
            return L1Regression.getObjectiveFunctionValue(ys, xs, function);
        }      

        @Override
        public double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function)
        {
            return L1Regression.getObjectiveFunctionValue(ys, xs, from, to, function);
        }  
    };

    private final String prettyName;

    BasicRegressionStrategy(String prettyName)
    {
        this.prettyName = prettyName;
    }	

    public static BasicRegressionStrategy getValue(String identifier, BasicRegressionStrategy fallBackValue)
    {
        BasicRegressionStrategy strategy = fallBackValue;

        if(identifier != null)
        {
            for(BasicRegressionStrategy str : BasicRegressionStrategy.values())
            {
                String estIdentifier =  str.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    strategy = str;
                    break;
                }
            }
        }

        return strategy;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public String getIdentifier()
    {
        return name();
    }
}
