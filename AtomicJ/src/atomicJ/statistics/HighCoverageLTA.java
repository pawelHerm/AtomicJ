
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

import java.util.Arrays;

import atomicJ.functions.ExactFitFactory;
import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.SingleDoubleExponentFunctionTypes;
import atomicJ.functions.SingleIntegerExponentFunctionTypes;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;


public class HighCoverageLTA implements HighBreakdownEstimator
{
    public static HighCoverageLTA findFit(double[][] data, int deg, boolean constant, double k, int nstarts)
    {
        int n = data.length;
        int p = deg + MathUtilities.boole(constant);	
        double minimalCoverage = MathUtilities.minimalCoverage(n, p);		

        return findFit(data, deg, constant, k, minimalCoverage, nstarts);
    }

    public static HighCoverageLTA findFit(double[][] data, int deg, boolean constant, double k, double h1, int nstarts)
    {
        return findFit(data, 0, data.length, deg, constant, k, h1, nstarts);
    }

    public static HighCoverageLTA findFit(double[][] data, int from, int to, int deg, boolean constant, double k, double h1, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTA.findFit(data, from, to, deg, constant, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int p = deg + MathUtilities.boole(constant);
        int c = (int)Math.min(h1*n, n);    

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.findFit(data, from, to, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTA(finalLTA, median);
    }

    public static HighCoverageLTA findFit(double[] ys, double[] xs, int deg, boolean constant, double k, double h1, int nstarts)
    {
        return findFit(ys, xs, 0, ys.length, deg, constant, k, h1, nstarts);
    }

    public static HighCoverageLTA findFit(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double k, double h1, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTA.findFit(ys, xs, from, to, deg, constant, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int p = deg + MathUtilities.boole(constant);
        int c = (int)Math.min(h1*n, n);    

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.findFit(ys,xs, from, to, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTA(finalLTA, median);
    }

    public static HighCoverageLTA findFit(double[][] data, double[] model, double k, int nstarts)
    {	
        return findFit(data, model, k, MathUtilities.minimalCoverage(data.length, model.length), nstarts);
    }

    public static HighCoverageLTA findFit(double[][] data, double[] model, double k, double h1, int nstarts)
    {		
        return findFit(data, 0, data.length, model, k, h1, nstarts);
    }

    public static HighCoverageLTA findFit(double[][] data, int from, int to, double[] model, double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTA.findFit(data,from,to, model, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int p = model.length;
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.findFit(data, from, to, model, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTA(finalLTA, median);
    }

    public static HighCoverageLTA findFitForSingleExponent(double[][] data, int exponent, double k, int nstarts)
    {   
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), data, 0, data.length, k, MathUtilities.minimalCoverage(data.length, 1), nstarts);
    }

    public static HighCoverageLTA findFitForSingleExponent(double[][] data, double exponent, double k, int nstarts)
    {   
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), data, 0, data.length, k, MathUtilities.minimalCoverage(data.length, 1), nstarts);
    }

    public static HighCoverageLTA findFit(ExactFitFactory exactFitFactory, double[][] data, int from, int to,  double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTA.findFit(exactFitFactory, data,from,to, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int p = exactFitFactory.getEstimatedParameterCount();
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTS = LTA.findFit(exactFitFactory, data, from, to, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTA(finalLTS, median);
    }

    public static HighCoverageLTA findFitForSingleExponent(double[] ys, double[] xs, int exponent, double k, int nstarts)
    {   
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, k, MathUtilities.minimalCoverage(ys.length, 1), nstarts);
    }

    public static HighCoverageLTA findFitForSingleExponent(double[] ys, double[] xs, double exponent, double k, int nstarts)
    {   
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, k, MathUtilities.minimalCoverage(ys.length, 1), nstarts);
    }

    public static HighCoverageLTA findFit(ExactFitFactory exactFitFactory, double[] ys, double[] xs, double k, int nstarts)
    {   
        return findFit(exactFitFactory, ys, xs, 0, ys.length, k, MathUtilities.minimalCoverage(ys.length, exactFitFactory.getEstimatedParameterCount()), nstarts);
    }

    public static HighCoverageLTA findFit(ExactFitFactory exactFitFactory, double[] ys, double[] xs, int from, int to,  double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTA.findFit(exactFitFactory, ys, xs, from,to, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int p = exactFitFactory.getEstimatedParameterCount();
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTS = LTA.findFit(exactFitFactory, ys, xs, from, to, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTA(finalLTS, median);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, double exponent, double k,  int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        HighCoverageLTA lta = findFit(data,from, to, new double[] {exponent}, k, h, nstarts);
        return lta.getBestFit();
    }  

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent( double[][] data, int from, int to, double exponent,  double k,double h, int nstarts)
    {
        HighCoverageLTA lta = findFit(data, from, to, new double[] {exponent}, k, h, nstarts);
        return lta.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, double k, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        HighCoverageLTA lta = findFit(data, from, to, new double[] {exponent}, k, h, nstarts);
        return lta.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, double k, double h,  int nstarts)
    {
        HighCoverageLTA lta = findFit(data, from, to,new double[] {exponent}, k, h, nstarts);
        return lta.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent, double k,  int nstarts)
    {
        HighCoverageLTA lts = findFitForSingleExponent(ys, xs, exponent, k,  nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent, double k,  int nstarts)
    {
        HighCoverageLTA lts = findFitForSingleExponent(ys, xs, exponent, k,  nstarts);
        return lts.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int deg, boolean constant, double k, int nstarts)
    {
        return findFitFunction(ys, xs, 0, ys.length, deg, constant, k, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int from, int to,int deg, boolean constant, double k, int nstarts)
    {
        int n = to - from;
        int p = deg + MathUtilities.boole(constant);    
        double minimalCoverage = MathUtilities.minimalCoverage(n, p);   

        return findFitFunction(ys, xs, from, to, deg, constant, k, minimalCoverage, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int from, int to,int deg, boolean constant, double k, double h, int nstarts)
    {
        HighCoverageLTA lts = findFit(ys, xs, from, to, deg, constant,k,h, nstarts);
        return lts.getBestFit();
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int deg, boolean constant, double k, int nstarts)
    {
        return findFitFunction(points, 0, points.length, deg, constant, k, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int from, int to,int deg, boolean constant, double k, int nstarts)
    {
        int n = to - from;
        int p = deg + MathUtilities.boole(constant);    
        double minimalCoverage = MathUtilities.minimalCoverage(n, p);   

        return findFitFunction(points, from, to, deg, constant, k, minimalCoverage, nstarts);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int from, int to, int deg, boolean constant, double k, double h, int nstarts)
    {
        HighCoverageLTA lts = findFit(points, from, to, deg, constant,k, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int deg, boolean constant, double k, int nstarts)
    {
        double minimalCoverage = MathUtilities.minimalCoverage(data.length, deg + MathUtilities.boole(constant));       
        double[] model = constant ? new double[] {0, deg} : new double[] {deg};
        return findObjectiveFunctionMinimum(data, model, k, minimalCoverage, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, int deg, boolean constant, double k, int nstarts)
    {
        double minimalCoverage = MathUtilities.minimalCoverage((to - from), deg + MathUtilities.boole(constant));       
        double[] model = constant ? new double[] {0, deg} : new double[] {deg};
        return findObjectiveFunctionMinimum(data, from, to, model, k, minimalCoverage, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, double[] model, double k, int nstarts)
    {   
        return findObjectiveFunctionMinimum(data, model, k, MathUtilities.minimalCoverage(data.length, model.length), nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[][] data, double[] model, double k, double h1, int nstarts)
    {       
        return findObjectiveFunctionMinimum(data, 0, data.length, model, k, h1, nstarts);
    }

    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, double[] model, double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTA.findFit(data, from, to, model, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;

        double objectiveFunctionMinimum = LTA.findObjectiveFunctionMinimum(data, from, to, model, h2, nstarts);

        return objectiveFunctionMinimum;
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double k, int nstarts)
    {
        int n = to - from;
        double minimalCoverage = MathUtilities.minimalCoverage(n, deg + MathUtilities.boole(constant));       
        double[] model = constant ? new double[] {0, deg} : new double[] {deg};
        return findObjectiveFunctionMinimum(ys, xs, from, to, model, k, minimalCoverage, nstarts);
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int deg, boolean constant, double k, int nstarts)
    {
        double minimalCoverage = MathUtilities.minimalCoverage(ys.length, deg + MathUtilities.boole(constant));       
        double[] model = constant ? new double[] {0, deg} : new double[] {deg};
        return findObjectiveFunctionMinimum(ys, xs, 0, ys.length,model, k, minimalCoverage, nstarts);
    }

    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, double[] model, double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTA.findFit(ys, xs, from, to, model, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;

        double objectiveFunctionMinimum = LTA.findObjectiveFunctionMinimum(ys, xs, from, to, model, h2, nstarts);

        return objectiveFunctionMinimum;
    }

    public static HighCoverageLTA getHighCoverageLTAforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double k)
    {     
        int p = function.getEstimatedParameterCount();
        double h1 = MathUtilities.minimalCoverage(data.length, p);        

        LinearRegressionEsimator firstReg = LTA.getLTAforFittedFunction(data, function, h1);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.getLTAforFittedFunction(data, function, h2);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTA(finalLTA, median);
    }

    public static double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function, double k)
    {     
        return getObjectiveFunctionValue(data, 0, data.length, function, k);
    }

    public static double getObjectiveFunctionValue(double[][] points, int from, int to, FittedUnivariateFunction function, double k) 
    {
        int n = to - from;

        if(n == 0)
        {
            return 0;
        }

        int p = function.getEstimatedParameterCount();
        double h1 = MathUtilities.minimalCoverage(n, p);        
        int c = (int)Math.min(h1*n, n); 

        double[] absResiduals = function.getAbsoluteResiduals(points, from, to);        
        Arrays.sort(absResiduals);

        double limit = k*absResiduals[c - 1];
        double crit = 0;

        for(int i = 0;i<n;i++)
        {
            double absResidual = absResiduals[i];
            if(absResidual<=limit)
            {
                crit += absResidual;
            }
            else
            {
                break;
            }
        }

        return crit;
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, FittedUnivariateFunction function, double k)
    {     
        return getObjectiveFunctionValue(ys, xs, 0, ys.length, function, k);
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction function, double k) 
    {
        int n = to - from;

        if(n == 0)
        {
            return 0;
        }

        int p = function.getEstimatedParameterCount();
        double h1 = MathUtilities.minimalCoverage(n, p);        
        int c = (int)Math.min(h1*n, n); 

        double[] absResiduals = function.getAbsoluteResiduals(ys, xs, from, to);        
        Arrays.sort(absResiduals);

        double limit = k*absResiduals[c - 1];
        double crit = 0;

        for(int i = 0;i<n;i++)
        {
            double absResidual = absResiduals[i];
            if(absResidual<=limit)
            {
                crit += absResidual;
            }
            else
            {
                break;
            }
        }

        return crit;
    }

    private final LTA finalLTA;
    private final double robustMedian;

    private HighCoverageLTA(LTA reg, double median)
    {
        this.finalLTA = reg;
        this.robustMedian = median;
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit() 
    {
        return finalLTA.getBestFit();
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return finalLTA.getObjectiveFunctionMinimum();
    }

    @Override
    public ResidualVector getResiduals() 
    {
        return finalLTA.getResiduals();
    }

    public double getRobustMedian()
    {
        return robustMedian;
    }

    @Override
    public double getCoverage() 
    {
        return finalLTA.getCoverage();
    }

    @Override
    public double[][] getCoveredCases() 
    {
        return finalLTA.getCoveredCases();
    }

    @Override
    public double[] getLastCoveredPoint()
    {
        return finalLTA.getLastCoveredPoint();
    }

    public double getLargestClusterOfCoveredCases() 
    {
        return finalLTA.getLargestClusterOfCoveredCases();
    }
}
