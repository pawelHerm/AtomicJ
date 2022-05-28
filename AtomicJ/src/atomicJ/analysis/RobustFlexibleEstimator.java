
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;
import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.InterceptlessLine.InterceptlessLineExactFitFactory;
import atomicJ.statistics.*;
import atomicJ.utilities.ArrayUtilities;


public final class RobustFlexibleEstimator implements ContactEstimator
{
    private final int deg;
    private final ContactEstimationGuide contactModel;
    private final MinimumSearchStrategy searchStrategy;

    public RobustFlexibleEstimator(MinimumSearchStrategy searchStrategy, int deg, ContactEstimationGuide model)
    {
        this.deg = deg;
        this.contactModel = model;
        this.searchStrategy = searchStrategy;
    }

    @Override
    public double[] getContactPoint(Channel1DData curveBranch, Point2D recordingPosition, double springConstant) 
    {		
        CurveBranchFullPartition curveBranchPartition = new CurveBranchFullPartition(curveBranch, recordingPosition, deg, 200, contactModel, springConstant);

        int n = curveBranch.getItemCount();
        int lowForceCount = curveBranchPartition.getLowForcePointCount();

        UnivariateFunction f = getObjectiveFunction(curveBranch, recordingPosition, lowForceCount, springConstant);

        IndexRange validContactIndices = contactModel.getRangeOfValidTrialContactPointIndices(curveBranch, recordingPosition, springConstant);
        int minIndex = Math.max(Math.max((int)(0.075*n), deg + 1), validContactIndices.getMinIndex());
        int maxIndex = Math.min(n - 5, validContactIndices.getMaxIndex());
        int contactPointIndex = (int)Math.rint(searchStrategy.getMinimum(f, minIndex, maxIndex));

        return curveBranch.getPoint(contactPointIndex);    			
    }

    private UnivariateFunction getObjectiveFunction(final Channel1DData curveBranch, final Point2D recordingPosition, final int lowForceCount, final double springConstant)
    {
        final SequentialSearchAssistant searchAssistant = contactModel.getSequentialSearchAssistant(curveBranch, recordingPosition, springConstant);

        UnivariateFunction f = new UnivariateFunction() 
        {
            @Override
            public double value(double x) 
            {
                int j = (int)Math.rint(x);

                int supportLength = lowForceCount - j;                
                double criterion = Double.POSITIVE_INFINITY;

                if(j > deg && supportLength > 2)
                {                      
                    SupportedPostcontactFitStrategy postcontactRegression = new SupportedPostcontactFitStrategy(supportLength);
                    criterion = searchAssistant.getObjectiveFunctionValue(BasicRegressionStrategy.CLASSICAL_L2, postcontactRegression, j);
                }  

                return criterion;
            } 
        };

        return f;
    }

    private static class SupportedPostcontactFitStrategy implements RegressionStrategy
    {
        private static final int NUMBER_OF_STARTS = 150;
        private static final double OPTIONAL_POINTS_COVERAGE = 0.5;

        private final int supportEndIndex;

        private SupportedPostcontactFitStrategy(int supportEndIndex)
        {
            this.supportEndIndex = supportEndIndex;
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant) 
        {      

            return performRegression(data, 0, data.length, degree, constant) ;
        }

        public LinearRegressionEsimator performRegression(double[][] data, int from, int to, int degree, boolean constant) 
        {
            if(to < supportEndIndex)
            {
                return L2Regression.findFit(data, from, to, degree, constant);
            }

            double[][] optional = Arrays.copyOfRange(data, Math.max(supportEndIndex, from), to);
            double[][] support = from < supportEndIndex ? Arrays.copyOfRange(data, from, supportEndIndex)  : new double[][] {};

            LinearRegressionEsimator postcontactFit  = SupportedLTS.findFit(optional, support, degree, constant, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);         

            return postcontactFit;
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model) 
        {
            return performRegression(data, 0, data.length, model);
        }

        public LinearRegressionEsimator performRegression(double[][] data, int from, int to, double[] model) 
        {
            if(to < supportEndIndex)
            {
                return L2Regression.findFit(data, from, to, model);
            }

            double[][] optional = Arrays.copyOfRange(data, Math.max(supportEndIndex, from), to);
            double[][] support = from < supportEndIndex ? Arrays.copyOfRange(data, from, supportEndIndex)  : new double[][] {};

            LinearRegressionEsimator postcontactFit = SupportedLTS.findFit(optional, support, model, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);

            return postcontactFit;
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, int exponent) 
        {           
            return performRegression(data, new double[] {exponent});
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(double[][] data, double exponent) 
        {           
            return performRegression(data, new double[] {exponent});
        }


        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return getObjectiveFunctionMinimum(data, 0, data.length, degree, constant);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int from, int to, int degree, boolean constant)
        {            
            if(to < supportEndIndex) 
            {
                return L2Regression.findObjectiveFunctionMinimum(data, from, to, degree, constant);
            }

            double[][] optional = Arrays.copyOfRange(data, Math.max(supportEndIndex, from), to);
            double[][] support = from < supportEndIndex ? Arrays.copyOfRange(data, from, supportEndIndex)  : new double[][] {};

            double minimum = SupportedLTS.findObjectiveFunctionMinimum(optional, support, degree, constant, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);      

            return minimum;
        }

        @Override
        public double getObjectiveFunctionMinimum(double[] ys,double[] xs, int from, int to, int degree, boolean constant)
        {
            if(to < supportEndIndex) 
            {
                return L2Regression.findObjectiveFunctionMinimum(ys, xs, from, to, degree, constant);
            }

            double[][] optional = ArrayUtilities.convertToPoints(ys, xs, Math.max(supportEndIndex, from), to);
            double[][] support = from < supportEndIndex ? ArrayUtilities.convertToPoints(ys, xs, from, supportEndIndex)  : new double[][] {};

            double minimum = SupportedLTS.findObjectiveFunctionMinimum(optional, support, degree, constant, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);         

            return minimum;
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data) 
        {           
            return getObjectiveFunctionMinimum(data, 0, data.length, 1, false);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction function)
        {
            return getObjectiveFunctionValue(data, 0, data.length, function);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, int from, int to, FittedUnivariateFunction function)
        {
            return 0;
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f)
        {
            return null;
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[] ys, double[] xs) 
        {
            int n = ys.length;
            if(n - supportEndIndex <= 0)
            {
                return L2Regression.findObjectiveFunctionMinimumDeg1NoConstant(ys, xs);
            }

            double minimum = SupportedLTS.findObjectiveFunctionMinimum(InterceptlessLineExactFitFactory.getInstance(),
                    ys, xs, supportEndIndex, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);

            return minimum;
        }


        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] points, int exponent)
        {
            int n = points.length;

            if(n <= supportEndIndex)
            {
                return L2Regression.findFitFunctionForSingleExponent(points, exponent);
            }

            return SupportedLTS.findFitFunctionForSingleExponent(points, supportEndIndex, exponent, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(double[][] points, double exponent)
        {
            int n = points.length;

            if(n <= supportEndIndex)
            {
                return L2Regression.findFitFunctionForSingleExponent(points, exponent);
            }

            return SupportedLTS.findFitFunctionForSingleExponent(points, supportEndIndex, exponent, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[][] points, int degree, boolean constant) 
        {
            int n = points.length;

            if(n <= supportEndIndex)
            {
                return L2Regression.findFitFunction(points, 0, n, degree, constant);
            }

            return SupportedLTS.findFitFunction(points, supportEndIndex,  degree,  constant, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunction(double[] ys,
                double[] xs, int degree, boolean constant) 
        {
            int n = ys.length;

            if(n <= supportEndIndex)
            {
                return L2Regression.findFitFunction(ys, xs, 0, n, degree, constant);
            }

            return SupportedLTS.findFitFunction(ys, xs, supportEndIndex,  degree,  constant, OPTIONAL_POINTS_COVERAGE, NUMBER_OF_STARTS);
        }

        @Override
        public double getObjectiveFunctionValue(double[] dataYs,
                double[] dataXs,
                FittedUnivariateFunction postcontactFit) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getObjectiveFunctionValue(double[] dataYs,
                double[] dataXs, int from, int to,
                FittedUnivariateFunction function) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(
                double[] ys, double[] xs, double exponent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LinearRegressionEsimator performRegressionForSingleExponent(
                double[] ys, double[] xs, int exponent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(
                double[] ys, double[] xs, int exponent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FittedLinearUnivariateFunction getFitFunctionForSingleExponent(
                double[] ys, double[] xs, double exponent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public double[] getLastCoveredPoint(double[] dataYs, double[] dataXs,
                UnivariateFunction fittedFunction) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @Override
    public boolean isAutomatic()
    {
        return true;
    }
}
