
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

package atomicJ.utilities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Jama.LUDecomposition;
import Jama.Matrix;

public class MathUtilities 
{
    public static double kahanSum(double[] input)
    {
        double sum = 0;
        double c = 0;                  // A running compensation for lost low-order bits.

        for(int i = 0;i<input.length;i++)
        {
            double y = input[i] - c;     
            double t = sum + y;        
            c = (t - sum) - y; 
            sum = t;           
        }

        return sum;
    }

    public static double kahanSumOfArrays(double[] inputA, double[] inputB)
    {
        double sum = 0;
        double c = 0;                  // A running compensation for lost low-order bits.

        for(int i = 0;i<inputA.length;i++)
        {
            double y = inputA[i]*inputB[i] - c;     
            double t = sum + y;        
            c = (t - sum) - y; 
            sum = t;           
        }

        return sum;
    }

    public static int getFractionCount(double v)
    {
        MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        if (v!=v || v == Double.POSITIVE_INFINITY || v == Double.NEGATIVE_INFINITY)
            return 0;//throw exception or return any other stuff

        BigDecimal d = new BigDecimal(v, mc);
        return Math.max(0, d.stripTrailingZeros().scale());	    
    }

    public static boolean equalWithinTolerance(double val1, double val2, double tolerance)
    {
        boolean equal = Math.abs(val1 - val2) <= tolerance;
        return equal;
    }


    public static boolean equalWithinTolerance(double[] nodeYs1,
            double[] nodeYs2, double tolerance) 
    {
        if(nodeYs1 == null && nodeYs2 == null)
        {
            return true;
        }

        if(nodeYs1 == null)
        {
            return false;
        }

        if(nodeYs1.length != nodeYs2.length)
        {
            return false;
        }

        for(int i = 0; i<nodeYs1.length; i++)
        {
            boolean ithElementEqual = equalWithinTolerance(nodeYs1[i], nodeYs2[i], tolerance);
            if(!ithElementEqual)
            {
                return false;
            }
        }

        return true;
    }

    public static int roundDownToMultiple(double value, int base)
    {
        return (int) (Math.floor(value/base) * base);
    }

    public static int roundToMultiple(double value, int base)
    {
        return (int) (Math.rint(value/base) * base);
    }

    public static int roundToMultiple(double value, double base)
    {
        return (int) (Math.rint(value/base) * base);
    }

    public static int roundUpToMultiple(double value, int base)
    {
        return (int) (Math.ceil(value/base) * base);
    }

    public static int boole(boolean b)
    {
        if(b)return 1;
        else return 0;
    }

    public static int[] add(int[] data, int v)
    {
        int n = data.length;

        int[] transformed = new int[n];

        for(int i = 0; i<n; i++)
        {
            transformed[i] = data[i] + v;
        }

        return transformed;
    }

    public static double[] add(double[] data, double v)
    {
        int n = data.length;

        double[] transformed = new double[n];

        for(int i = 0; i<n; i++)
        {
            transformed[i] = data[i] + v;
        }

        return transformed;
    }

    public static int[] abs(int[] values)
    {
        int n = values.length;

        int[] abs = new int[n];

        for(int i = 0; i<n; i++)
        {
            abs[i] = Math.abs(values[i]);
        }

        return abs;
    }

    public static int[][] abs(int[][] values)
    {
        int n = values.length;

        int[][] abs = new int[n][];

        for(int i = 0; i<n;i++)
        {
            abs[i] = abs(values[i]);
        }

        return abs;
    }


    public static int[] multiply(int[] values, int factor)
    {
        int n = values.length;

        int[] multiplied = new int[n];

        for(int i = 0; i<n;i++)
        {
            multiplied[i] = factor*values[i];
        }

        return multiplied;
    }

    public static int[][] multiply(int[][] values, int factor)
    {
        int n = values.length;

        int[][] multiplied = new int[n][];

        for(int i = 0; i<n;i++)
        {
            multiplied[i] = multiply(values[i], factor);
        }

        return multiplied;
    }

    public static double[] multiply(double[] values, double factor)
    {
        int n = values.length;

        double[] multiplied = new double[n];

        for(int i = 0; i<n;i++)
        {
            multiplied[i] = factor*values[i];
        }

        return multiplied;
    }

    public static double[][] multiply(double[][] values, double factor)
    {
        int n = values.length;

        double[][] multiplied = new double[n][];

        for(int i = 0; i<n;i++)
        {
            multiplied[i] = multiply(values[i], factor);
        }

        return multiplied;
    }

    //assumes that the points array is an n by 2 array
    public static double[][] multiplyYValues(double[][] points, double factor)
    {
        int n = points.length;

        double[][] multiplied = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double[] pt = points[i];
            multiplied[i] = new double[] {pt[0],factor*pt[1]};
        }

        return multiplied;
    }

    public static int intPositivePow(int x, int exp)
    {
        int y = 1;

        for(int i = 0;i<exp;i++)
        {
            y = y*x;
        } 

        return y;
    }

    public static double intPow(double x, int exp)
    {
        double y = 1;

        if(exp > 0)
        {
            for(int i = 0;i<exp;i++)
            {
                y = y*x;
            }            
        }
        else
        {
            for(int i = exp;i<0;i++)
            {
                y = y*x;
            }        

            y = 1./y;
        }    
        return y;
    }

    public double[] solveEquationSystem(double[][] coefficients, double[] unknown)
    {
        int p = coefficients.length;

        Matrix matrixTest = new Matrix(coefficients);
        LUDecomposition decomp = new LUDecomposition(matrixTest);
        if(!decomp.isNonsingular())
        {
            return null;
        }
        Matrix coeffMatrix = decomp.solve(new Matrix(unknown,p));
        double[] coeff = coeffMatrix.getRowPackedCopy();

        return coeff;
    }

    //performs operation in place
    public static double[] subtract(double[] row1, double[] row2)
    {
        int n = row2.length;

        for(int i = 0; i<n; i++)
        {
            row1[i] = row1[i] - row2[i];            
        }
        return row1;                
    }

    public static double fallingFactorial(double exp, int p)
    {
        double result = exp;
        for(int i = 1; i < p; i++)
        {
            result = result*(exp - i);
        }

        return result;
    }

    //this could be calculated using Apache Commons factorial as ratio of two factorials, but we don't want intermediate results to overflow
    //so dividing two factorials is not a good solution
    public static int fallingFactorial(int n, int p)
    {
        int result = n;
        for(int i = 1; i < p; i++)
        {
            result = result*(n - i);
        }

        return result;
    }

    public static double minimalCoverage(int n, double p)
    {
        double h = (Math.floor(n/2)+ Math.floor((p + 1)/2))/n; //From Olive, Hawkins 2003. Robust regression with high coverage.
        return h;
    }

    public static int robustMedianIndex(int n, double p)
    {
        int h = (int)Math.round((Math.floor(n/2)+ Math.floor((p + 1)/2)));
        return h;
    }	

    public static Matrix multiply(Matrix matrix, double[] column) {

        int rowCount = matrix.getRowDimension();
        int columnCount = matrix.getColumnDimension();
        double[][] A = matrix.getArray();

        double[][] C = new double[rowCount][1];

        for (int i = 0; i < rowCount; i++) 
        {
            double[] Arowi = A[i];
            double s = 0;
            for (int k = 0; k < columnCount; k++) 
            {
                s += Arowi[k] * column[k];
            }

            C[i][0] = s;
        }

        return new Matrix(C, rowCount, 1);
    }

    public static double[] multiply3(double[][] A, int rowCount, int columnCount, double[] column)
    {
        double[] C = new double[rowCount];

        for (int i = 0; i < rowCount; i++) 
        {
            double[] Arowi = A[i];
            double s = 0;
            for (int k = 0; k < columnCount; k++) 
            {
                s += Arowi[k] * column[k];
            }

            C[i] = s;
        }

        return C;
    }

    //A*vector
    public static double[] multiplyConcatenatedMatrixAndVector(double[] A, int rowCount, int columnCount, double[] vector)
    {
        double[] C = new double[rowCount];

        int index = 0;

        for (int i = 0; i < rowCount; i++) 
        {
            double s = 0;
            for (int k = 0; k < columnCount; k++) 
            {
                s += A[index++] * vector[k];
            }

            C[i] = s;
        }

        return C;
    }

    //A^T*vector, length of the vector must be equal to rowCountOfA
    public static double[] multiplyTransposeOfConcatenatedMatrixAAndVector(double[] A, int rowCountOfA, int columnCountOfA, double[] vector)
    {
        double[] C = new double[columnCountOfA];

        for (int k = 0; k < columnCountOfA; k++) 
        {
            double s = 0;
            for (int i = 0; i < rowCountOfA; i++) 
            {
                s += A[i*columnCountOfA + k] * vector[i];
            }

            C[k] = s;
        }

        return C;
    }

    public static double[][] transpose(double[][] A, int m, int n)
    {
        double[][] C = new double[n][m];

        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++) 
            {
                C[j][i] = A[i][j];
            }
        }

        return C;
    }

    public static double[] concatenate2DArray(double[][] A, int m, int n)
    {
        double[] C = new double[n*m];

        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++) 
            {
                C[i*n + j] = A[i][j];
            }
        }

        return C;
    }

    public static double[][] recover2DArrayFromConcatenatedForm(double[] C, int m, int n)
    {
        double[][] A = new double[m][n];

        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < n; j++) 
            {
                A[i][j] = C[i*n + j];
            }
        }
        return A;
    }

    //calculates  M * M^T
    public static Matrix multipyByTranspose(Matrix matrixTransposed) 
    {
        int rowCount = matrixTransposed.getRowDimension();
        int columnCount = matrixTransposed.getColumnDimension();

        double[][] transposed = matrixTransposed.getArray();
        double[][] C = new double[rowCount][rowCount];
        for (int j = 0; j < rowCount; j++)
        {
            double[] Bcolj = transposed[j];

            for (int i = 0; i < rowCount; i++) 
            {
                double[] Arowi = transposed[i];
                double s = 0;
                for (int k = 0; k < columnCount; k++) 
                {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return new Matrix(C, rowCount, rowCount);
    }

    public static double[][] multipyByTranspose(double[][] transposed, int rowCountT, int columnCountT) 
    {
        double[][] C = new double[rowCountT][rowCountT];
        for (int j = 0; j < rowCountT; j++)
        {
            double[] Bcolj = transposed[j];

            for (int i = 0; i < rowCountT; i++) 
            {
                double[] Arowi = transposed[i];
                double s = 0;
                for (int k = 0; k < columnCountT; k++) 
                {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }

        return C;
    }


    //The result of this method is C^T*C
    public static double[][] multipyTransposeOfConcatenatedMatrixCByC(double[] C, int rowCount, int columnCount) 
    {
        double[][] M = new double[columnCount][columnCount];
        for (int j = 0; j < columnCount; j++)
        {
            double[] Mj = M[j];
            for (int i = 0; i < columnCount; i++) 
            {
                double s = 0;
                for (int k = 0; k < rowCount; k++) 
                {
                    int v = k*columnCount;
                    s +=  C[v + j] * C[v + i];
                }
                Mj[i] = s;
            }
        }

        return M;
    }

    public static double[][] multipyTransposeOfConcatenatedMatrixCByCSymmetryUsed(double[] C, int rowCount, int columnCount) 
    {
        double[][] M = new double[columnCount][columnCount];
        for (int j = 0; j < columnCount; j++)
        {
            double[] Mj = M[j];
            for (int i = 0; i < j; i++) 
            {
                Mj[i] = M[i][j];

            }  
            for (int i = j; i < columnCount; i++) 
            {
                double s = 0;
                for (int k = 0; k < rowCount; k++) 
                {
                    int v = k*columnCount;
                    s +=  C[v + j] * C[v + i];
                }
                Mj[i] = s;  
            }   
        }

        return M;
    }

    public static double[] multipyTransposeOfConcatenatedMatrixCByCSymmetryUsedConcatenated(double[] C, int rowCount, int columnCount) 
    {
        double[] M = new double[columnCount*columnCount];
        for (int j = 0; j < columnCount; j++)
        {
            int w = j*columnCount;
            for (int i = 0; i < j; i++) 
            {
                M[w + i] = M[i*columnCount + j];

            }  
            for (int i = j; i < columnCount; i++) 
            {
                double s = 0;
                for (int k = 0; k < rowCount; k++) 
                {
                    int v = k*columnCount;
                    s +=  C[v + j] * C[v + i];
                }
                M[w + i] = s;  
            }   
        }

        return M;
    }

    //https://eyalsch.wordpress.com/2010/04/01/random-sample/
    //https://stackoverflow.com/questions/2394246/algorithm-to-select-a-single-random-combination-of-values
    public static <T> Set<T> getRandomSubsampleWithoutOverwritting(Random rnd, List<T> data, int sampleSize)
    {
        Set<T> res = new HashSet<>(sampleSize);
        int n = data.size();
        for(int i= n-sampleSize; i<n; i++)
        {
            int pos = rnd.nextInt(i+1);
            T item = data.get(pos);
            if (res.contains(item))
                res.add(data.get(i));
            else
                res.add(item);
        }
        return res;
    }

    public static <T> Set<T> getRandomSubsampleWithoutOverwritting(Random rnd, T[] data,int sampleSize)
    {
        Set<T> res = new HashSet<>(sampleSize);
        int n = data.length;
        for(int i= n-sampleSize; i<n; i++)
        {
            int pos = rnd.nextInt(i+1);
            T item = data[pos];
            if (res.contains(item))
                res.add(data[i]);
            else
                res.add(item);
        }
        return res;
    }

    //does not modify the array 'data'
    public static <T> Set<T> getRandomSubsampleWithoutOverwritting(Random rnd, T[] data,int from, int to, int sampleSize)
    {
        Set<T> res = new HashSet<>(sampleSize);
        int n = to - from;
        for(int i= n-sampleSize; i<n; i++)
        {
            int pos = rnd.nextInt(i+1);
            T item = data[pos + from];
            if (res.contains(item))
                res.add(data[i + from]);
            else
                res.add(item);
        }
        return res;
    }

    //does not modify arrays 'dataPartA' or 'dataPartB'
    public static <T> Set<T> getRandomSubsampleWithoutOverwritting(Random rnd, T[] dataPartA, T[] dataPartB, int sampleSize)
    {
        Set<T> res = new HashSet<>(sampleSize);
        int n1 = dataPartA.length;
        int n2 = dataPartB.length;
        int n = n1 + n2;
        for(int i= n-sampleSize; i<n; i++)
        {
            int drawn = rnd.nextInt(i+1);
            T item = drawn < n1 ? dataPartA[drawn] : dataPartB[drawn - n1];
            if (res.contains(item))
            {
                T secondItem = i < n1 ? dataPartA[i] : dataPartB[i - n1];
                res.add(secondItem);
            }
            else
            {
                res.add(item);
            }
        }
        return res;
    }

    public static Set<double[]> getRandomSubsampleWithoutOverwritting(Random rnd, double[] dataPartAYs, double[] dataPartAXs, double[] dataPartBYs, double[] dataPartBXs, int sampleSize)
    {
        Set<double[]> res = new HashSet<>(sampleSize);
        int n1 = dataPartAYs.length;
        int n2 = dataPartBYs.length;
        int n = n1 + n2;
        for(int i= n-sampleSize; i<n; i++)
        {
            int drawn = rnd.nextInt(i+1);
            double[] item = drawn < n1 ? new double[] {dataPartAXs[drawn], dataPartAYs[drawn]} : new double[] {dataPartBXs[drawn - n1], dataPartBYs[drawn - n1]};
            if (res.contains(item))
            {
                double[] secondItem = i < n1 ? new double[] {dataPartAXs[i], dataPartAYs[i]} : new double[] {dataPartBXs[i - n1], dataPartBYs[i - n1]};
                res.add(secondItem);
            }
            else
            {
                res.add(item);
            }
        }
        return res;
    }


    public static Set<double[]> getRandomSubsampleWithoutOverwritting(Random rnd, double[] ys, double[] xs, int sampleSize)
    {
        Set<double[]> res = new HashSet<>(sampleSize);
        int n = ys.length;
        for(int i= n-sampleSize; i<n; i++)
        {
            int pos = rnd.nextInt(i+1);
            double[] p = new double[] {ys[pos],xs[pos]};
            if (res.contains(p))
                res.add(new double[] {ys[i],xs[i]});
            else
                res.add(p);
        }
        return res;
    }

    public static Set<double[]> getRandomSubsampleWithoutOverwritting(Random rnd, double[] ys, double[] xs, int from, int to, int sampleSize)
    {
        Set<double[]> res = new HashSet<>(sampleSize);
        int n = to - from;
        for(int i= n-sampleSize; i<n; i++)
        {
            int pos = rnd.nextInt(i+1);
            double[] p = new double[] {ys[pos + from],xs[pos + from]};
            if (res.contains(p))
                res.add(new double[] {ys[i + from],xs[i + from]});
            else
                res.add(p);
        }
        return res;
    }
}
