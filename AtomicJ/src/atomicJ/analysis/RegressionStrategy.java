
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
import atomicJ.statistics.LinearRegressionEsimator;

public interface RegressionStrategy 
{
    public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant);
    public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant);
    public double getObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int degree, boolean constant);

    public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data);
    public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs);

    public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant);
    public LinearRegressionEsimator performRegression(double[][] data, double[] model);
    public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent);
    public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent);
    public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, double exponent);
    public LinearRegressionEsimator performRegressionForSingleExponent(double[] ys, double[] xs, int exponent);

    public FittedLinearUnivariateFunction getFitFunction(double[][] data, int degree, boolean constant);   
    public FittedLinearUnivariateFunction getFitFunction(double[] ys, double[] xs, int degree, boolean constant);
    public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, int exponent);
    public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] data, double exponent);
    public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent);
    public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent);

    public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function);
    //from inclusive, to exclusive
    public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function);
    public double getObjectiveFunctionValue(double[] dataYs, double[] dataXs, FittedUnivariateFunction postcontactFit);
    public double getObjectiveFunctionValue(double[] dataYs, double[] dataXs, int from, int to, FittedUnivariateFunction function);
    public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f);
    public double[] getLastCoveredPoint(double[] dataYs, double[] dataXs, UnivariateFunction fittedFunction);
}
