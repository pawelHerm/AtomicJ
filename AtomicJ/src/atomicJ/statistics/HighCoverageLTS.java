
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


public class HighCoverageLTS implements HighBreakdownEstimator
{
    private final LTS finalLTS;
    private final double robustMedian;

    public static HighCoverageLTS findFit(double[][] data, int deg, boolean constant, double k, int nstarts)
    {
        int n = data.length;
        int p = deg + MathUtilities.boole(constant);	
        double minimalCoverage = MathUtilities.minimalCoverage(n, p);		

        return findFit(data, deg, constant, k, minimalCoverage, nstarts);
    }

    public static HighCoverageLTS findFit(double[] data, int deg, boolean constant, double k, int nstarts)
    {
        double minimalCoverage = MathUtilities.minimalCoverage(data.length, deg + MathUtilities.boole(constant));       

        return findFit(data, deg, constant, k, minimalCoverage, nstarts);
    }

    public static HighCoverageLTS findFit(double[][] data, int deg, boolean constant, double k, double h1, int nstarts)
    {
        return findFit(data, 0, data.length, deg, constant, k, h1, nstarts);
    }

    public static HighCoverageLTS findFit(double[][] data, int from, int to, int deg, boolean constant, double k, double h1, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTS.findFit(data, from, to, deg, constant, h1, nstarts);
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

        LTS finalLTS = LTS.findFit(data, from, to, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
    }


    public static HighCoverageLTS findFit(double[] ys, double[] xs, int deg, boolean constant, double k, double h1, int nstarts)
    {
        return findFit(ys, xs, 0, ys.length, deg, constant, k, h1, nstarts);
    }

    public static HighCoverageLTS findFit(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double k, double h1, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTS.findFit(ys, xs, from, to, deg, constant, h1, nstarts);
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

        LTS finalLTS = LTS.findFit(ys, xs, from, to, deg, constant, h2, nstarts);

        int medianIndex = MathUtilities.robustMedianIndex(n, p);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
    }

    public static HighCoverageLTS findFit(double[] data, int deg, boolean constant, double k, double h1, int nstarts)
    { 
        LinearRegressionEsimator firstReg = LTS1DFactory.findFit(data, deg, constant, h1, nstarts);
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

        LTS finalLTS = LTS1DFactory.findFit(data, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
    }

    public static HighCoverageLTS findFit(double[][] data, double[] model, double k, int nstarts)
    {	
        return findFit(data, model, k, MathUtilities.minimalCoverage(data.length, model.length), nstarts);
    }

    public static HighCoverageLTS findFit(double[][] data, double[] model, double k, double h1, int nstarts)
    {		
        return findFit(data, 0, data.length, model, k, h1, nstarts);
    }

    public static HighCoverageLTS findFit(double[][] data, int from, int to, double[] model, double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTS.findFit(data,from,to, model, h1, nstarts);
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

        LTS finalLTS = LTS.findFit(data, from, to, model, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTS(finalLTS, median);
    }

    public static HighCoverageLTS findFit(ExactFitFactory exactFitFactory, double[][] data, double k, int nstarts)
    {   
        return findFit(exactFitFactory, data, 0, data.length, k, MathUtilities.minimalCoverage(data.length, exactFitFactory.getEstimatedParameterCount()), nstarts);
    }

    public static HighCoverageLTS findFitForSingleExponent(double[][] data, int exponent, double k, int nstarts)
    {   
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), data, 0, data.length, k, MathUtilities.minimalCoverage(data.length, 1), nstarts);
    }

    public static HighCoverageLTS findFitForSingleExponent(double[][] data, double exponent, double k, int nstarts)
    {   
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), data, 0, data.length, k, MathUtilities.minimalCoverage(data.length, 1), nstarts);
    }

    public static HighCoverageLTS findFit(ExactFitFactory exactFitFactory, double[][] data, int from, int to,  double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTS.findFit(exactFitFactory, data,from,to, h1, nstarts);
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

        LTS finalLTS = LTS.findFit(exactFitFactory, data, from, to, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTS(finalLTS, median);
    }

    public static HighCoverageLTS findFitForSingleExponent(double[] ys, double[] xs, int exponent, double k, int nstarts)
    {   
        return findFit(SingleIntegerExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, k, MathUtilities.minimalCoverage(ys.length, 1), nstarts);
    }

    public static HighCoverageLTS findFitForSingleExponent(double[] ys, double[] xs, double exponent, double k, int nstarts)
    {   
        return findFit(SingleDoubleExponentFunctionTypes.findExactFitFactory(exponent), ys, xs, 0, ys.length, k, MathUtilities.minimalCoverage(ys.length, 1), nstarts);
    }

    public static HighCoverageLTS findFit(ExactFitFactory exactFitFactory, double[] ys, double[] xs, double k, int nstarts)
    {   
        return findFit(exactFitFactory, ys, xs, 0, ys.length, k, MathUtilities.minimalCoverage(ys.length, exactFitFactory.getEstimatedParameterCount()), nstarts);
    }

    public static HighCoverageLTS findFit(ExactFitFactory exactFitFactory, double[] ys, double[] xs, int from, int to,  double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTS.findFit(exactFitFactory, ys, xs, from,to, h1, nstarts);
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

        LTS finalLTS = LTS.findFit(exactFitFactory, ys, xs, from, to, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTS(finalLTS, median);
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
        LinearRegressionEsimator firstReg = LTS.findFit(data, from, to, model, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;

        double objectiveFunctionMinimum = LTS.findObjectiveFunctionMinimum(data, from, to, model, h2, nstarts);

        return objectiveFunctionMinimum;
    }

    //does not modify data
    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int deg, boolean constant, double k, int nstarts)
    {
        int n = to - from;
        double minimalCoverage = MathUtilities.minimalCoverage(n, deg + MathUtilities.boole(constant));       
        double[] model = constant ? new double[] {0, deg} : new double[] {deg};
        return findObjectiveFunctionMinimum(ys, xs, from, to,model, k, minimalCoverage, nstarts);
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
        LinearRegressionEsimator firstReg = LTS.findFit(ys, xs, from, to, model, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] absResiduals = residuals.getAbsoluteResiduals();
        int n = absResiduals.length;
        int c = (int)Math.min(h1*n, n); 

        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        int initIndex = k >= 1 ? c: 0;
        double count = ArrayUtilities.binarySearchAscending(absResiduals, initIndex, n, limit);

        double h2 = count/n;

        double objectiveFunctionMinimum = LTS.findObjectiveFunctionMinimum(ys, xs, from, to, model, h2, nstarts);

        return objectiveFunctionMinimum;
    }



    public static HighCoverageLTS findFit(double[] data, double[] model, double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTS1DFactory.findFit(data, model, h1, nstarts);
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

        LTS finalLTS = LTS1DFactory.findFit(data, model, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTS(finalLTS, median);
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, double exponent, double k, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        HighCoverageLTS lts = findFit(data, from, to, new double[] {exponent}, k, h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, double exponent,  double k,double h, int nstarts)
    {
        HighCoverageLTS lts = findFit(data, from, to, new double[] {exponent}, k, h, nstarts);
        return lts.getBestFit();
    }    

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, double k, int nstarts)
    {
        double h = MathUtilities.minimalCoverage(data.length, 1);

        HighCoverageLTS lts = findFit(data, from, to, new double[] {exponent}, k, h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int from, int to, int exponent, double k, double h,  int nstarts)
    {
        HighCoverageLTS lts = findFit(data, from, to, new double[] {exponent}, k, h, nstarts);
        return lts.getBestFit();
    }


    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent, double k,  int nstarts)
    {
        HighCoverageLTS lts = findFitForSingleExponent(ys, xs, exponent, k,  nstarts);
        return lts.getBestFit();
    }


    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent, double k,  int nstarts)
    {
        HighCoverageLTS lts = findFitForSingleExponent(ys, xs, exponent, k,  nstarts);
        return lts.getBestFit();
    }

    //does not modify data
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
        HighCoverageLTS lts = findFit(ys, xs, from, to, deg, constant,k,h, nstarts);
        return lts.getBestFit();
    }

    //does not modify data
    public static FittedLinearUnivariateFunction findFitFunction(double[][] points, int deg, boolean constant, double k, int nstarts)
    {        
        return findFitFunction(points, 0, points.length, deg, constant, k,nstarts);
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
        HighCoverageLTS lts = findFit(points, from, to, deg, constant,k, h, nstarts);
        return lts.getBestFit();
    }

    public static HighCoverageLTS getHighCoverageLTSforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double k)
    {     
        int p = function.getEstimatedParameterCount();
        double h1 = MathUtilities.minimalCoverage(data.length, p);        

        LinearRegressionEsimator firstReg = LTS.getLTSforFittedFunction(data, function, h1);
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

        LTS finalLTS = LTS.getLTSforFittedFunction(data, function, h2);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
    }

    public static double getObjectiveFunctionValue(double[][] points, FittedUnivariateFunction function, double k)
    {     
        return getObjectiveFunctionValue(points, 0, points.length, function, k);
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
                crit += absResidual*absResidual;
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
                crit += absResidual*absResidual;
            }
            else
            {
                break;
            }
        }

        return crit;
    }

    private HighCoverageLTS(LTS reg, double median)
    {
        this.finalLTS = reg;
        this.robustMedian = median;
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit() 
    {
        return finalLTS.getBestFit();
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return finalLTS.getObjectiveFunctionMinimum();
    }

    @Override
    public ResidualVector getResiduals() 
    {
        return finalLTS.getResiduals();
    }

    public double getRobustMedian()
    {
        return robustMedian;
    }

    @Override
    public double getCoverage() 
    {
        return finalLTS.getCoverage();
    }

    public int getCoveredCount()
    {
        return finalLTS.coveredCount;
    }

    public int getDataCount()
    {
        return finalLTS.getDataCount();
    }

    @Override
    public double[][] getCoveredCases() 
    {
        return finalLTS.getCoveredCases();
    }

    @Override
    public double[] getLastCoveredPoint()
    {
        return finalLTS.getLastCoveredPoint();
    }

    public double getLargestClusterOfCoveredCases() 
    {
        return finalLTS.getLargestClusterOfCoveredCases();
    }
}
