package atomicJ.statistics;

import Jama.Matrix;
import Jama.QRDecomposition;
import atomicJ.utilities.MathUtilities;

public class SavitzkyGolay 
{    
    public static double[][] getCoefficients(int leftHalfLength, int rightHalfLength, int deg)
    {
        return getCoefficients2(-leftHalfLength, leftHalfLength + rightHalfLength + 1, deg);
    }

    public static double[][] getCoefficients2(int min, int n, int deg)
    {
        int p = deg + 1;

        double[][] design = new double[n][]; 
        for(int i = 0;i<n;i++)
        {
            double[] currentRow = new double[p];
            for(int d = 0, j = 0; d <= deg; d++,j++)
            {
                currentRow[j] = MathUtilities.intPow(min + i,d);
            }

            design[i] = currentRow;
        }

        Matrix designMatrix = new Matrix(design, n, p); 
        Matrix designTransposed = designMatrix.transpose();
        QRDecomposition decomposition = new QRDecomposition(MathUtilities.multipyByTranspose(designTransposed));
        Matrix parametersMatrix = decomposition.solve(designTransposed);

        return parametersMatrix.getArray();
    }


    public static double[][] getCoefficients2(int min, int n, int deg, double[] weights)
    {
        if(deg >= n)
        {
            throw new IllegalArgumentException("Argument deg must be smaller than n");
        }

        int p = deg + 1;

        double[][] designWeighted = new double[n][]; 
        double[][] design = new double[n][];
        for(int i = 0;i<n;i++)
        {
            double w = weights[i];

            double[] currentWeightedRow = new double[p];
            double[] currentRow = new double[p];

            for(int d = 0; d <= deg; d++)
            {
                double pow = MathUtilities.intPow(min + i, d);
                currentWeightedRow[d] = w*pow;
                currentRow[d] = pow;
            }

            designWeighted[i] = currentWeightedRow;
            design[i] = currentRow;
        }

        Matrix designMatrix = new Matrix(design, n, p); 
        Matrix designWeightedMatrix = new Matrix(designWeighted, n, p); 
        Matrix designWeightedTransposed = designWeightedMatrix.transpose();
        QRDecomposition decomposition = new QRDecomposition(designWeightedTransposed.times(designMatrix));
        Matrix parametersMatrix = decomposition.solve(designWeightedTransposed);

        return parametersMatrix.getArray();
    }

    public static double[] getCoefficients(int ml, int mp, int deg, int derivative)
    {
        if(derivative > deg)
        {
            throw new IllegalArgumentException("Argument derivative must be smaller or equal deg");
        }

        return getCoefficients(ml, mp, deg)[derivative];
    }

    public static double[][] getCoefficients(int ml, int mp, int deg, double[] weights)
    {

        if(deg > ml + mp)
        {
            throw new IllegalArgumentException("Argument deg must be smaller or equal ml + mp");
        }

        int n = ml + mp + 1;

        return getCoefficients2(-ml, n, deg, weights);
    }


    public static double[] getCoefficients(int ml, int mp, int deg, double[] weights, int derivative)
    {
        if(derivative > deg)
        {
            throw new IllegalArgumentException("Argument derivative must be smaller or equal deg");
        }

        return getCoefficients(ml, mp, deg, weights)[derivative];
    }

    public static double[] getCoefficients2(int min, int n, int deg, double[] weights, int derivative)
    {
        if(derivative > deg)
        {
            throw new IllegalArgumentException("Argument derivative must be smaller or equal deg");
        }

        return getCoefficients2(min, n, deg, weights)[derivative];
    }
}
