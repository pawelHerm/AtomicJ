
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

import java.util.ArrayList;
import java.util.List;

import atomicJ.functions.BiVariatePolynomial;
import atomicJ.functions.MultivariatePolynomial;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;


import Jama.Matrix;
import Jama.QRDecomposition;


public class MultivariateL2Regression 
{
    private final double lowestCriterion;
    private final double[][] data;
    private final ResidualVector residuals;
    private final MultivariateFittedFunction bestFit;

    public static MultivariateL2Regression findFit(double[][] data, int[][] allDegrees)
    {
        int varCount = allDegrees.length;
        int n = data.length;
        int p = 0;

        for(int[] d : allDegrees)
        {
            p = p + d.length;
        }

        double[][] design = new double[n][]; 
        double[] obs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double[] dataPoint = data[i];

            double response = dataPoint[varCount];
            double[] currentRow = new double[p];

            for(int j = 0, index = 0; j<varCount;j++)
            {		
                double x = dataPoint[j];

                int[] varDegrees = allDegrees[j];

                for(int k : varDegrees)
                {
                    currentRow[index++] = MathUtilities.intPow(x, k);
                }
            }

            design[i] = currentRow;
            obs[i] = response;
        }

        Matrix designMatrix = new Matrix(design, n, p); 
        Matrix responseMatrix = new Matrix(obs,n);
        Matrix designTransposed = designMatrix.transpose();
        QRDecomposition decomposition = new QRDecomposition(designTransposed.times(designMatrix));

        Matrix parametersMatrix = decomposition.solve(designTransposed.times(responseMatrix));
        Matrix predictedResponses = designMatrix.times(parametersMatrix);

        List<double[]> coefficients = new ArrayList<>();
        double[] par = parametersMatrix.getRowPackedCopy();

        int start = 0;

        for(int i = 0; i<varCount; i++ )
        {
            int[] powers = allDegrees[i];

            int deg = powers.length > 0 ? ArrayUtilities.getMaximum(powers) : 0;
            double[] coeffs = new double[deg + 1];

            for(int pow : powers)
            {
                coeffs[pow] = par[start++];
            }

            coefficients.add(coeffs);     
        }

        MultivariateFittedFunction bestFit = new MultivariatePolynomial(coefficients);

        ResidualVector residuals = new ResidualVector(responseMatrix.minus(predictedResponses).getRowPackedCopy());

        return new MultivariateL2Regression(data, bestFit, residuals);
    };

    public static MultivariateL2Regression fitMatrix(double[][] data, int rowCount, int columnCount, int[] xDegrees, int[] yDegrees)
    {       
        int n = rowCount*columnCount;
        int p = xDegrees.length + yDegrees.length;

        RegressionModel regressionModel = RegressionModel.getRegressionModel(data, rowCount, columnCount, xDegrees, yDegrees);
        double[][] design = regressionModel.getDesign(); 
        double[] obs = regressionModel.getObservations();

        Matrix designMatrix = new Matrix(design, n, p); 
        Matrix responseMatrix = new Matrix(obs,n);
        Matrix designTransposed = designMatrix.transpose();
        QRDecomposition decomposition = new QRDecomposition(designTransposed.times(designMatrix));

        Matrix parametersMatrix = decomposition.solve(designTransposed.times(responseMatrix));
        Matrix predictedResponses = designMatrix.times(parametersMatrix);

        List<double[]> coefficients = new ArrayList<>();
        double[] par = parametersMatrix.getRowPackedCopy();

        int index = 0;

        int degX = xDegrees.length > 0 ? ArrayUtilities.getMaximum(xDegrees) : 0;
        double[] coeffsX = new double[degX + 1];

        for(int pow : xDegrees)
        {
            coeffsX[pow] = par[index++];
        }

        coefficients.add(coeffsX); 

        int degY = yDegrees.length > 0 ? ArrayUtilities.getMaximum(yDegrees) : 0;
        double[] coeffsY = new double[degY + 1];

        for(int pow : yDegrees)
        {
            coeffsY[pow] = par[index++];
        }

        coefficients.add(coeffsY);  

        MultivariateFittedFunction bestFit = new MultivariatePolynomial(coefficients);


        ResidualVector residuals = new ResidualVector(responseMatrix.minus(predictedResponses).getRowPackedCopy());

        return new MultivariateL2Regression(data, bestFit, residuals);
    };

    public static BiVariatePolynomial getFittedFunction(double[] xs, double[] ys, double[] zs, int[] xDegrees, int[] yDegrees)
    {
        if(xs.length != ys.length || ys.length != zs.length)
        {
            throw new IllegalArgumentException("The arrays xs, ys and zs shoul have the same length");
        }

        RegressionModel regressionModel = RegressionModel.getRegressionModel(xs, ys, zs, xDegrees, yDegrees);
        return getFittedFunction(regressionModel, xDegrees, yDegrees);

    }

    public static BiVariatePolynomial getFittedFunction(double[] xs, double[] ys, double[] zs, int from, int to, int[] xDegrees, int[] yDegrees)
    {
        if(xs.length != ys.length || ys.length != zs.length)
        {
            throw new IllegalArgumentException("The arrays xs, ys and zs shoul have the same length");
        }

        RegressionModel regressionModel = RegressionModel.getRegressionModel(xs, ys, zs, from, to, xDegrees, yDegrees);
        return getFittedFunction(regressionModel, xDegrees, yDegrees);
    }  

    public static BiVariatePolynomial getFittedFunction(double[][] data, int rowCount, int columnCount, int[] xDegrees, int[] yDegrees)
    {
        RegressionModel regressionModel = RegressionModel.getRegressionModel(data, rowCount, columnCount, xDegrees, yDegrees);

        return getFittedFunction(regressionModel, xDegrees, yDegrees);
    };


    private static BiVariatePolynomial getFittedFunction(RegressionModel regressionModel, int[] xDegrees, int[] yDegrees)
    {
        int n = regressionModel.getObservationCount();
        int p = regressionModel.getCoefficientsCount();

        double[][] design = regressionModel.getDesign(); 
        double[] obs = regressionModel.getObservations();

        Matrix designMatrix = new Matrix(design, n, p); 
        Matrix responseMatrix = new Matrix(obs,n);
        Matrix designTransposed = designMatrix.transpose();
        QRDecomposition decomposition = new QRDecomposition(designTransposed.times(designMatrix));

        Matrix parametersMatrix = decomposition.solve(designTransposed.times(responseMatrix));

        double[] par = parametersMatrix.getRowPackedCopy();

        int index = 0;

        int degX = xDegrees.length > 0 ? ArrayUtilities.getMaximum(xDegrees) : 0;
        double[] coeffsX = new double[degX + 1];

        for(int pow : xDegrees)
        {
            coeffsX[pow] = par[index++];
        }

        int degY = yDegrees.length > 0 ? ArrayUtilities.getMaximum(yDegrees) : 0;
        double[] coeffsY = new double[degY + 1];

        for(int pow : yDegrees)
        {
            coeffsY[pow] = par[index++];
        }

        BiVariatePolynomial bestFit = new BiVariatePolynomial(coeffsX, coeffsY);

        return bestFit;
    }

    private MultivariateL2Regression(double[][] data, MultivariateFittedFunction bestFit, ResidualVector residuals)
    {
        this.data = data;
        this.bestFit = bestFit;	
        this.residuals = residuals;
        this.lowestCriterion = residuals.getSquaresSum();
    }

    public MultivariateFittedFunction getBestFit()
    {
        return bestFit;
    }

    public ResidualVector getResiduals()
    {
        return residuals;
    }

    public double getObjectiveFunctionMinimum()
    {
        return lowestCriterion;
    }

    public double[][] getData()
    {
        return data;
    }
}
