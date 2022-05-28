
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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.IntegerPowerFunction;
import atomicJ.functions.InterceptlessCubic;
import atomicJ.functions.InterceptlessLine;
import atomicJ.functions.InterceptlessQuadratic;
import atomicJ.functions.Polynomial;
import atomicJ.functions.PowerFunction;
import atomicJ.functions.PowerFunctionCombination;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.RegressionUtilities;

import Jama.Matrix;
import Jama.QRDecomposition;

public class L2Regression implements LinearRegressionEsimator
{
    public static double findObjectiveFunctionMinimum(double[][] data, int deg, boolean constant)
    {            
        return findObjectiveFunctionMinimum(data, 0, data.length, deg, constant);
    };

    public static double findObjectiveFunctionMinimum(double[][] data, int from, int to, int deg, boolean constant)
    {            
        if(deg == 1 && !constant)
        {
            return findObjectiveFunctionMinimumDeg1NoConstant(data, from, to);
        }

        if(deg == 2 && !constant)
        {
            return findObjectiveFunctionMinimumDeg2NoConstant(data, from, to);
        }

        if(deg == 3 && !constant)
        {
            return findObjectiveFunctionMinimumDeg3NoConstant(data, from, to);
        }

        int n = to - from;
        int p =  deg + MathUtilities.boole(constant); //Number of parameters

        double[] design = new double[n*p]; 
        double[] obs = new double[n];

        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        for(int i = from; i<to; i++)
        {
            double[] pt = data[i];
            double x = pt[0];
            int j = (i - from)*p;
            for(int exponent: model)
            {
                design[j++] = MathUtilities.intPow(x,exponent);
            }
            obs[i - from] = pt[1];
        }

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(MathUtilities.multipyTransposeOfConcatenatedMatrixCByCSymmetryUsedConcatenated(design, n, p), p, p);
        double[] parametersArray = decomposition.solveWithColumn(MathUtilities.multiplyTransposeOfConcatenatedMatrixAAndVector(design,n,p, obs));
        double[] predictedResponses = MathUtilities.multiplyConcatenatedMatrixAndVector(design, n, p, parametersArray);

        double ss = 0;

        for(int i = 0; i<n; i++)
        {
            double dx = obs[i] - predictedResponses[i];
            ss += dx*dx;
        }

        return ss;
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[][] data, int from, int to, int deg, boolean constant)
    {            
        if(deg == 1 && !constant)
        {
            return findFitFunctionDeg1NoConstant(data, from, to);
        }

        if(deg == 2 && !constant)
        {
            return findFitFunctionDeg2NoConstant(data, from, to);
        }

        if(deg == 3 && !constant)
        {
            return findFitFunctionDeg3NoConstant(data, from, to);
        }

        int n = to - from;
        int p =  deg + MathUtilities.boole(constant); //Number of parameters

        double[] design = new double[n*p]; 
        double[] obs = new double[n];

        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        for(int i = from; i<to; i++)
        {
            double[] pt = data[i];
            double x = pt[0];
            int j = (i - from)*p;
            for(int exponent: model)
            {
                design[j++] = MathUtilities.intPow(x,exponent);
            }
            obs[i - from] = pt[1];
        }

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(MathUtilities.multipyTransposeOfConcatenatedMatrixCByCSymmetryUsedConcatenated(design, n, p), p, p);
        double[] parametersArray = decomposition.solveWithColumn(MathUtilities.multiplyTransposeOfConcatenatedMatrixAAndVector(design,n,p, obs));

        Polynomial fit = new Polynomial(parametersArray);
        return fit;
    }

    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int deg, boolean constant)
    {            
        return findObjectiveFunctionMinimum(ys, xs, 0, ys.length, deg, constant);
    };

    public static double findObjectiveFunctionMinimum(double[] ys, double[] xs, int from, int to, int deg, boolean constant)
    {            
        if(deg == 1 && !constant)
        {
            return findObjectiveFunctionMinimumDeg1NoConstant(ys, xs, from, to);
        }

        if(deg == 2 && !constant)
        {
            return findObjectiveFunctionMinimumDeg2NoConstant(ys, xs, from, to);
        }

        if(deg == 3 && !constant)
        {
            return findObjectiveFunctionMinimumDeg3NoConstant(ys, xs, from, to);
        }

        int n = to - from;
        int p =  deg + MathUtilities.boole(constant); //Number of parameters

        double[] design = new double[n*p]; 
        double[] obs = new double[n];

        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        for(int i = from; i<to; i++)
        {
            double x = xs[i];
            int j = (i - from)*p;
            for(int exponent: model)
            {
                design[j++] = MathUtilities.intPow(x,exponent);
            }
            obs[i - from] = ys[i];
        }

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(MathUtilities.multipyTransposeOfConcatenatedMatrixCByCSymmetryUsedConcatenated(design, n, p), p, p);
        double[] parametersArray = decomposition.solveWithColumn(MathUtilities.multiplyTransposeOfConcatenatedMatrixAAndVector(design,n,p, obs));
        double[] predictedResponses = MathUtilities.multiplyConcatenatedMatrixAndVector(design, n, p, parametersArray);

        double ss = 0;

        for(int i = 0; i<n; i++)
        {
            double dx = obs[i] - predictedResponses[i];
            ss += dx*dx;
        }

        return ss;
    };

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int deg, boolean constant)
    {
        return findFitFunction(ys, xs, 0, ys.length, deg, constant);
    }

    public static FittedLinearUnivariateFunction findFitFunction(double[] ys, double[] xs, int from, int to, int deg, boolean constant)
    {            
        if(deg == 1 && !constant)
        {
            return findFitFunctionDeg1NoConstant(ys, xs, from, to);
        }

        if(deg == 2 && !constant)
        {
            return findFitFunctionDeg2NoConstant(ys, xs, from, to);
        }

        if(deg == 3 && !constant)
        {
            return findFitFunctionDeg3NoConstant(ys, xs, from, to);
        }

        int n = to - from;
        int p =  deg + MathUtilities.boole(constant); //Number of parameters

        double[] design = new double[n*p]; 
        double[] obs = new double[n];

        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        for(int i = from; i<to; i++)
        {
            double x = xs[i];
            int j = (i - from)*p;
            for(int exponent: model)
            {
                design[j++] = MathUtilities.intPow(x,exponent);
            }
            obs[i - from] = ys[i];
        }

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(MathUtilities.multipyTransposeOfConcatenatedMatrixCByCSymmetryUsedConcatenated(design, n, p), p, p);
        double[] parametersArray = decomposition.solveWithColumn(MathUtilities.multiplyTransposeOfConcatenatedMatrixAAndVector(design,n,p, obs));

        Polynomial fit = new Polynomial(parametersArray);
        return fit;
    };

    public static double findObjectiveFunctionMinimumDeg1NoConstant(double[][] data)
    {
        return findObjectiveFunctionMinimumDeg1NoConstant(data, 0, data.length);
    };

    public static double findObjectiveFunctionMinimumDeg1NoConstant(double[] ys, double[] xs)
    {
        return findObjectiveFunctionMinimumDeg1NoConstant(ys, xs, 0, ys.length);
    };

    public static double findObjectiveFunctionMinimumDeg1NoConstant(double[][] data, int from, int to)
    {
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = from; i<to; i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            double x = pt[0];            
            designTxDesign += x*x;
            designTxObs += x*ob;
        }

        double parameter = designTxObs/designTxDesign;

        double ss = 0;
        for(int i = from; i < to; i++)
        {
            double[] p = data[i];
            double dx = p[1] - parameter*p[0];
            ss += dx*dx;
        }

        return ss;
    };

    public static FittedLinearUnivariateFunction findFitFunctionDeg1NoConstant(double[][] data, int from, int to)
    {
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = from; i<to; i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            double x = pt[0];            
            designTxDesign += x*x;
            designTxObs += x*ob;
        }

        double parameter = designTxObs/designTxDesign;
        InterceptlessLine line = new InterceptlessLine(parameter);

        return line;
    }

    public static double findObjectiveFunctionMinimumDeg1NoConstant(double[] ys, double[] xs, int from, int to)
    {
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = from; i<to; i++)
        {
            double y = ys[i];
            double x = xs[i];            
            designTxDesign += x*x;
            designTxObs += x*y;
        }

        double parameter = designTxObs/designTxDesign;

        double ss = 0;
        for(int i = from; i < to; i++)
        {
            double y = ys[i];
            double x = xs[i];   

            double dx = y - parameter*x;
            ss += dx*dx;
        }

        return ss;
    }

    public static FittedLinearUnivariateFunction findFitFunctionDeg1NoConstant(double[] ys, double[] xs, int from, int to)
    {
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = from; i<to; i++)
        {
            double y = ys[i];
            double x = xs[i];            
            designTxDesign += x*x;
            designTxObs += x*y;
        }

        double parameter = designTxObs/designTxDesign;

        InterceptlessLine line = new InterceptlessLine(parameter);

        return line;
    }

    public static double findObjectiveFunctionMinimumDeg2NoConstant(double[][] data)
    {            
        return findObjectiveFunctionMinimumDeg2NoConstant(data, 0, data.length);
    }

    public static double findObjectiveFunctionMinimumDeg2NoConstant(double[][] data, int from, int to)
    {            
        double s0 = 0;
        double s1 = 0;
        double s3 = 0;

        double c0 = 0;
        double c1 = 0;
        for (int i = from; i < to; i++) 
        {
            double[] pt = data[i];
            double x = pt[0];
            double x2 = x*x;
            s0 +=  x2;
            s1 +=  x * x2;
            s3 +=  x2 * x2;
            double y = pt[1];

            c0 += x * y;
            c1 += x2 * y;
        }

        double s1Sq = s1*s1;

        double par1 = (-c1*s0 + c0*s1)/(s1Sq - s0*s3);
        double par0 = (-par1*s1*(s0 + s3) + c0*s0 + c1*s1)/(s0*s0+ s1Sq);

        double ss = 0;
        for (int i = from; i < to; i++) 
        {
            double[] p = data[i];
            double x = p[0];
            double d = x * (par0 + x* par1);
            double dx = p[1] - d;
            ss += dx*dx;
        }

        return ss;
    }

    public static FittedLinearUnivariateFunction findFitFunctionDeg2NoConstant(double[][] data, int from, int to)
    {            
        double s0 = 0;
        double s1 = 0;
        double s3 = 0;

        double c0 = 0;
        double c1 = 0;
        for (int i = from; i < to; i++) 
        {
            double[] pt = data[i];
            double x = pt[0];
            double x2 = x*x;
            s0 +=  x2;
            s1 +=  x * x2;
            s3 +=  x2 * x2;
            double y = pt[1];

            c0 += x * y;
            c1 += x2 * y;
        }

        double s1Sq = s1*s1;

        double par1 = (-c1*s0 + c0*s1)/(s1Sq - s0*s3);
        double par0 = (-par1*s1*(s0 + s3) + c0*s0 + c1*s1)/(s0*s0+ s1Sq);

        InterceptlessQuadratic fit = new InterceptlessQuadratic(par0, par1);

        return fit;
    }

    public static double findObjectiveFunctionMinimumDeg2NoConstant(double[] ys, double[] xs)
    {            
        return findObjectiveFunctionMinimumDeg2NoConstant(ys, xs, 0, ys.length);
    }

    public static double findObjectiveFunctionMinimumDeg2NoConstant(double[] ys, double[] xs, int from, int to)
    {            
        double s0 = 0;
        double s1 = 0;
        double s3 = 0;

        double c0 = 0;
        double c1 = 0;
        for (int i = from; i < to; i++) 
        {
            double x = xs[i];
            double x2 = x*x;
            s0 +=  x2;
            s1 +=  x * x2;
            s3 +=  x2 * x2;
            double y = ys[i];

            c0 += x * y;
            c1 += x2 * y;
        }

        double s1Sq = s1*s1;

        double par1 = (-c1*s0 + c0*s1)/(s1Sq - s0*s3);
        double par0 = (-par1*s1*(s0 + s3) + c0*s0 + c1*s1)/(s0*s0+ s1Sq);

        double ss = 0;
        for (int i = from; i < to; i++) 
        {
            double x = xs[i];
            double d = x * (par0 + x* par1);
            double dx = ys[i] - d;
            ss += dx*dx;
        }

        return ss;
    };

    public static FittedLinearUnivariateFunction findFitFunctionDeg2NoConstant(double[] ys, double[] xs, int from, int to)
    {            
        double s0 = 0;
        double s1 = 0;
        double s3 = 0;

        double c0 = 0;
        double c1 = 0;
        for (int i = from; i < to; i++) 
        {
            double x = xs[i];
            double x2 = x*x;
            s0 +=  x2;
            s1 +=  x * x2;
            s3 +=  x2 * x2;
            double y = ys[i];

            c0 += x * y;
            c1 += x2 * y;
        }

        double s1Sq = s1*s1;

        double par1 = (-c1*s0 + c0*s1)/(s1Sq - s0*s3);
        double par0 = (-par1*s1*(s0 + s3) + c0*s0 + c1*s1)/(s0*s0+ s1Sq);

        InterceptlessQuadratic fit = new InterceptlessQuadratic(par0, par1);

        return fit;
    };

    public static double findObjectiveFunctionMinimumDeg3NoConstant(double[][] data)
    {  
        return findObjectiveFunctionMinimumDeg3NoConstant(data, 0, data.length);
    }

    public static double findObjectiveFunctionMinimumDeg3NoConstant(double[][] data, int from, int to)
    {  
        int p =  3; //Number of parameters

        double s2 = 0;
        double s3 = 0;
        double s4 = 0;       
        double s5 = 0;
        double s6 = 0;          
        double syx = 0;
        double syx2 = 0;
        double syx3 = 0;

        for (int k = from; k < to; k++) 
        {
            double[] pt = data[k];
            double x = pt[0];
            double y = pt[1];

            double x2 = x*x;
            double x3 = x2*x;

            s2 +=  x2;
            s3 +=  x3;
            s4 +=  x2 * x2;
            s5 +=  x2 * x3;
            s6 +=  x3 * x3;

            syx += x * y;
            syx2 += x2 * y;
            syx3 += x3 * y;
        }            

        double[] M = new double[] {s2,s3, s4, s3, s4, s5, s4, s5, s6};
        double[] C = new double[] {syx,syx2,syx3};

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(M, p, p);
        double[] parametersArray = decomposition.solveWithColumn(C);

        double ss = 0;

        for (int i = from; i < to; i++) 
        {
            double[] pt = data[i];
            double x = pt[0];
            double y = pt[1];

            double dx = y - x*(parametersArray[0] + x*(parametersArray[1] + x*parametersArray[2]));
            ss += dx*dx;
        }

        return ss;
    }

    public static FittedLinearUnivariateFunction findFitFunctionDeg3NoConstant(double[][] data, int from, int to)
    {  
        int p =  3; //Number of parameters

        double s2 = 0;
        double s3 = 0;
        double s4 = 0;       
        double s5 = 0;
        double s6 = 0;          
        double syx = 0;
        double syx2 = 0;
        double syx3 = 0;

        for (int k = from; k < to; k++) 
        {
            double[] pt = data[k];
            double x = pt[0];
            double y = pt[1];

            double x2 = x*x;
            double x3 = x2*x;

            s2 +=  x2;
            s3 +=  x3;
            s4 +=  x2 * x2;
            s5 +=  x2 * x3;
            s6 +=  x3 * x3;

            syx += x * y;
            syx2 += x2 * y;
            syx3 += x3 * y;
        }            

        double[] M = new double[] {s2,s3, s4, s3, s4, s5, s4, s5, s6};
        double[] C = new double[] {syx,syx2,syx3};

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(M, p, p);
        double[] parametersArray = decomposition.solveWithColumn(C);

        InterceptlessCubic fit = new InterceptlessCubic(parametersArray[0], parametersArray[1], parametersArray[2]);
        return fit;
    }

    public static double findObjectiveFunctionMinimumDeg3NoConstant(double[] ys, double[] xs)
    {  
        return findObjectiveFunctionMinimumDeg3NoConstant(ys, xs, 0, ys.length);
    }

    public static double findObjectiveFunctionMinimumDeg3NoConstant(double[] ys,double[] xs, int from, int to)
    {  
        int p =  3; //Number of parameters

        double s2 = 0;
        double s3 = 0;
        double s4 = 0;       
        double s5 = 0;
        double s6 = 0;          
        double syx = 0;
        double syx2 = 0;
        double syx3 = 0;

        for (int k = from; k < to; k++) 
        {
            double x = xs[k];
            double y = ys[k];

            double x2 = x*x;
            double x3 = x2*x;

            s2 +=  x2;
            s3 +=  x3;
            s4 +=  x2 * x2;
            s5 +=  x2 * x3;
            s6 +=  x3 * x3;

            syx += x * y;
            syx2 += x2 * y;
            syx3 += x3 * y;
        }            

        double[] M = new double[] {s2,s3, s4, s3, s4, s5, s4, s5, s6};
        double[] C = new double[] {syx,syx2,syx3};

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(M, p, p);
        double[] parametersArray = decomposition.solveWithColumn(C);

        double ss = 0;

        for (int i = from; i < to; i++) 
        {
            double x = xs[i];
            double y = ys[i];

            double dx = y - x*(parametersArray[0] + x*(parametersArray[1] + x*parametersArray[2]));
            ss += dx*dx;
        }

        return ss;
    }

    public static FittedLinearUnivariateFunction findFitFunctionDeg3NoConstant(double[] ys,double[] xs, int from, int to)
    {  
        int p = 3; //Number of parameters

        double s2 = 0;
        double s3 = 0;
        double s4 = 0;       
        double s5 = 0;
        double s6 = 0;          
        double syx = 0;
        double syx2 = 0;
        double syx3 = 0;

        for (int k = from; k < to; k++) 
        {
            double x = xs[k];
            double y = ys[k];

            double x2 = x*x;
            double x3 = x2*x;

            s2 +=  x2;
            s3 +=  x3;
            s4 +=  x2 * x2;
            s5 +=  x2 * x3;
            s6 +=  x3 * x3;

            syx += x * y;
            syx2 += x2 * y;
            syx3 += x3 * y;
        }            

        double[] M = new double[] {s2,s3, s4, s3, s4, s5, s4, s5, s6};
        double[] C = new double[] {syx,syx2,syx3};

        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(M, p, p);
        double[] parametersArray = decomposition.solveWithColumn(C);

        InterceptlessCubic fit = new InterceptlessCubic(parametersArray[0], parametersArray[1], parametersArray[2]);
        return fit;
    }

    public static UnivariateFunction[] performRegressionsOnEquispacedLines(double[][] dataSets, int columnCount, int deg) 
    {
        int n = dataSets.length;

        UnivariateFunction[] functions = new UnivariateFunction[n];

        double[][] coeffs = SavitzkyGolay.getCoefficients(0, columnCount - 1, deg);

        int p = deg + 1;

        for(int i = 0; i<n;i++)
        {
            double[] parameters = new double[p];

            double[] data = dataSets[i];

            for(int j = 0; j<p; j++)
            {
                double par = 0;
                double[] coeffsForDeg = coeffs[j];

                for(int k = 0; k<columnCount; k++)
                {
                    par += data[k]*coeffsForDeg[k];
                }

                parameters[j] = par;
            }


            //when we use linear or quadratic functions instead of polynomials, the performance improvement
            //is modest, although measurable (5 % for line fit correction of images)
            functions[i] = Polynomial.getPolynomialFunction(parameters, deg, true);
        }

        return functions;
    }

    public static L2Regression findFitNative(double[][] data,int deg, boolean constant)
    {
        return findFitNative(data, RegressionUtilities.getModel(deg, constant));     
    };

    public static L2Regression findFit(double[][] data, double[] model)
    {
        return findFit(data, 0, data.length, model);
    }

    public static L2Regression findFit(double[][] data, int from, int to, double[] model)
    {        
        int n = to - from;
        int p = model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, from, to, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));
        double[] predictedResponses = MathUtilities.multiply3(design, n, p, parameters);

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model, parameters);	

        //we do not need response matrix any more, so we call subtract() to perform in place operation
        ResidualVector residuals = new ResidualVector(MathUtilities.subtract(obs, predictedResponses));

        return new L2Regression(bestFit, residuals);
    };

    public static L2Regression findFit(double[][] data, int deg, boolean constant)
    {
        return findFit(data, 0, data.length, deg, constant);
    }

    public static L2Regression findFit(double[][] data, int from, int to, int deg, boolean constant)
    {                
        RegressionModel regModel = RegressionModel.getRegressionModel(data, from, to, deg, constant);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        int n = regModel.getObservationCount();
        int p = regModel.getCoefficientsCount();

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));
        double[] predictedResponses = MathUtilities.multiply3(design, n, p, parameters);

        double[] coefficents = constant ? parameters : ArrayUtilities.padLeft(parameters, 0, 1);
        FittedLinearUnivariateFunction bestFit = new Polynomial(coefficents);

        //we do not need response matrix any more, so we call subtract() to perform in place operation
        ResidualVector residuals = new ResidualVector(MathUtilities.subtract(obs, predictedResponses));

        return new L2Regression(bestFit, residuals);
    }

    public static L2Regression findFitForSingleExponent(double[][] data, int exponent)
    {
        int n = data.length;

        double[] designTransposed = new double[n]; 
        double[] obs = new double[n];
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            obs[i] = ob;

            double xexp = MathUtilities.intPow(pt[0],exponent);
            designTransposed[i] = xexp;

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        double[] residuals = new double[n];
        for(int i = 0; i<n;i++)
        {
            residuals[i] = obs[i] - parameter*designTransposed[i];
        }

        FittedLinearUnivariateFunction bestFit = new IntegerPowerFunction(parameter, exponent);  
        ResidualVector residualVector = new ResidualVector(residuals);

        return new L2Regression(bestFit, residualVector);
    }


    public static L2Regression findFitForSingleExponent(double[] ys, double[] xs, int exponent)
    {
        int n = ys.length;

        double[] designTransposed = new double[n]; 
        double[] obs = new double[n];
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double ob = ys[i];
            obs[i] = ob;

            double xexp = MathUtilities.intPow(xs[i],exponent);
            designTransposed[i] = xexp;

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        double[] residuals = new double[n];
        for(int i = 0; i<n;i++)
        {
            residuals[i] = obs[i] - parameter*designTransposed[i];
        }

        FittedLinearUnivariateFunction bestFit = new IntegerPowerFunction(parameter, exponent);  
        ResidualVector residualVector = new ResidualVector(residuals);

        return new L2Regression(bestFit, residualVector);
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, int exponent)
    {
        int n = data.length;

        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];

            double xexp = MathUtilities.intPow(pt[0],exponent);

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        FittedLinearUnivariateFunction bestFit = new IntegerPowerFunction(parameter, exponent);  

        return bestFit;
    };


    public static L2Regression findFitForSingleExponent(double[][] data, double exponent)
    {
        int n = data.length;

        double[] designTransposed = new double[n]; 
        double[] obs = new double[n];
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            obs[i] = ob;

            double xexp = Math.pow(pt[0],exponent);
            designTransposed[i] = xexp;

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        double[] residuals = new double[n];
        for(int i = 0; i<n;i++)
        {
            residuals[i] = obs[i] - parameter*designTransposed[i];
        }

        FittedLinearUnivariateFunction bestFit = new PowerFunction(parameter, exponent);  
        ResidualVector residualVector = new ResidualVector(residuals);

        return new L2Regression(bestFit, residualVector);
    }

    public static L2Regression findFitForSingleExponent(double[] ys, double[] xs, double exponent)
    {
        int n = ys.length;

        double[] designTransposed = new double[n]; 
        double[] obs = new double[n];
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double ob = ys[i];
            obs[i] = ob;

            double xexp = Math.pow(xs[i],exponent);
            designTransposed[i] = xexp;

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        double[] residuals = new double[n];
        for(int i = 0; i<n;i++)
        {
            residuals[i] = obs[i] - parameter*designTransposed[i];
        }

        FittedLinearUnivariateFunction bestFit = new PowerFunction(parameter, exponent);  
        ResidualVector residualVector = new ResidualVector(residuals);

        return new L2Regression(bestFit, residualVector);
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[][] data, double exponent)
    {
        int n = data.length;

        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];

            double xexp = Math.pow(pt[0],exponent);

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;

        FittedLinearUnivariateFunction bestFit = new PowerFunction(parameter, exponent);  

        return bestFit;
    }

    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, int exponent)
    {
        int n = ys.length;

        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double ob = ys[i];

            double xexp = MathUtilities.intPow(xs[i],exponent);

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;

        FittedLinearUnivariateFunction bestFit = new PowerFunction(parameter, exponent);  

        return bestFit;
    }


    public static FittedLinearUnivariateFunction findFitFunctionForSingleExponent(double[] ys, double[] xs, double exponent)
    {
        int n = ys.length;

        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double ob = ys[i];

            double xexp = Math.pow(xs[i],exponent);

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;

        FittedLinearUnivariateFunction bestFit = new PowerFunction(parameter, exponent);  

        return bestFit;
    }

    public static FittedLinearUnivariateFunction findFitedFunction(double[] data,int deg, boolean constant)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, deg, constant);
        return findFitedFunction(regModel, deg, constant);
    }

    public static FittedLinearUnivariateFunction findFitedFunction(double[] dataYs, double[] dataXs, int deg, boolean constant)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(dataYs, dataXs, deg, constant);

        return findFitedFunction(regModel, deg, constant);
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[][] data,int deg, boolean constant)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, deg, constant);
        return findFitedFunction(regModel, deg, constant);
    };

    private static FittedLinearUnivariateFunction findFitedFunction(RegressionModel regModel, int deg, boolean constant)
    {
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        int n = regModel.getObservationCount();
        int p = regModel.getCoefficientsCount();
        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        FittedLinearUnivariateFunction bestFit = Polynomial.getPolynomialFunction(parameters, deg, constant);    

        return bestFit;
    };

    public static UnivariateFunction findFitedFunction(double[] data, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        return findFitedFunction(regModel, model);
    };

    public static FittedLinearUnivariateFunction findFitedFunction(RegressionModel regModel, double[] model)
    {
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        int n = regModel.getObservationCount();
        int p = regModel.getCoefficientsCount();
        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model, parameters);   

        return bestFit;
    }

    public static UnivariateFunction findFitedFunction(double[][] data, double[] weights, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, weights, model);

        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] wobs = regModel.getWeightedObservations();

        int n = data.length;
        int p =  model.length;
        Matrix weightedDesignMatrix = new Matrix(weightedDesign, n, p);
        Matrix weightedDesignTransposed = weightedDesignMatrix.transpose();

        QRDecomposition decomposition = new QRDecomposition(MathUtilities.multipyByTranspose(weightedDesignTransposed));
        Matrix parametersMatrix = decomposition.solve(MathUtilities.multiply(weightedDesignTransposed, wobs));

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model, parametersMatrix.getRowPackedCopy());   

        return bestFit;
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[][] data, int from, int to, double[] weights, int degree)
    {        
        int n = to - from;
        int p = degree + 1;

        RegressionModel regModel = RegressionModel.getWeightedDesignAndWeightedObservations(data, from, to, weights, degree);

        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] wobs = regModel.getWeightedObservations();

        return getFittedFunction(weightedDesign, wobs, degree, n, p);
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[] data, int from, int to, double[] weights, int degree)
    {
        int n = to - from;
        int p = degree + 1;

        RegressionModel regModel = RegressionModel.getWeightedDesignAndWeightedObservations(data, from, to, weights, degree);

        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] wobs = regModel.getWeightedObservations();

        return getFittedFunction(weightedDesign, wobs, degree, n, p);
    };

    private static FittedLinearUnivariateFunction getFittedFunction(double[][] weightedDesign, double[] wobs, int degree, int n, int p)
    {                    
        double[][] weightedDesignTransposed = MathUtilities.transpose(weightedDesign, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(weightedDesignTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(weightedDesignTransposed,p,n, wobs));

        FittedLinearUnivariateFunction bestFit = Polynomial.getPolynomialFunction(parameters, degree, true);    

        return bestFit;
    }

    public static L2Regression findFitNative(double[][] data, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        DoubleMatrix designMatrix = new DoubleMatrix(design);
        DoubleMatrix responseMatrix = new DoubleMatrix(obs);

        DoubleMatrix parametersMatrix = Solve.solveLeastSquares(designMatrix, responseMatrix);
        DoubleMatrix predictedResponses = designMatrix.mmul(parametersMatrix);
        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model,parametersMatrix.toArray());   
        ResidualVector residuals = new ResidualVector(responseMatrix.sub(predictedResponses).toArray());

        return new L2Regression(bestFit, residuals);

    };

    public static L2Regression findFitNative(double[][] data, double[] wieghts, double[] model)
    {      
        RegressionModel regModel = RegressionModel.getRegressionModel(data, wieghts, model);

        double[][] design = regModel.getDesign(); 
        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] obs = regModel.getObservations();
        double[] wobs = regModel.getWeightedObservations();

        DoubleMatrix designMatrix = new DoubleMatrix(design);
        DoubleMatrix weightedDesignMatrix = new DoubleMatrix(weightedDesign);

        DoubleMatrix responseMatrix = new DoubleMatrix(obs);
        DoubleMatrix weightedResponseMatrix = new DoubleMatrix(wobs);

        DoubleMatrix parametersMatrix = Solve.solveLeastSquares(weightedDesignMatrix, weightedResponseMatrix);
        DoubleMatrix predictedResponses = designMatrix.mmul(parametersMatrix);

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model,parametersMatrix.toArray());   
        ResidualVector residuals = new ResidualVector(responseMatrix.sub(predictedResponses).toArray());

        return new L2Regression(bestFit, residuals);
    };

    public static double[] findParameters(double[][] design, double[] obs)
    {
        int n = obs.length;
        int p =  design[0].length;

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        return parameters;   
    };

    public static L2Regression getL2RegressionForFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {      
        ResidualVector residuals = ResidualVector.getInstance(data, data.length, function);
        L2Regression l2Regression = new L2Regression(function, residuals);

        return l2Regression;
    }   

    public static double getObjectiveFunctionValue(double[][] data, FittedUnivariateFunction f)
    {
        return getObjectiveFunctionValue(data, 0, data.length, f);
    }

    public static double getObjectiveFunctionValue(double[][] points, int from, int to, FittedUnivariateFunction f)
    {
        double objective = f.squaresSum(points, from, to);
        return objective;
    }

    public static double getObjectiveFunctionValue(double[] ys, double[] xs, int from, int to, FittedUnivariateFunction f)
    {        
        double objective = f.squaresSum(ys, xs, from, to);
        return objective;
    }

    private final double lowestCriterion;
    private final ResidualVector residuals;
    private final FittedLinearUnivariateFunction bestFit;

    private L2Regression(FittedLinearUnivariateFunction bestFit, ResidualVector residuals)
    {
        this.bestFit = bestFit;	
        this.residuals = residuals;
        this.lowestCriterion = residuals.getSquaresSum();
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit()
    {
        return bestFit;
    }

    @Override
    public ResidualVector getResiduals()
    {
        return residuals;
    }

    @Override
    public double getObjectiveFunctionMinimum()
    {
        return lowestCriterion;
    }

}
