package atomicJ.curveProcessing;

import java.util.Arrays;

import atomicJ.utilities.ArrayUtilities;

public class Kernel1D implements Convolable1D
{        
    private final int columnCount;
    private final int centerColumnIndex;

    private final double[] matrix;
    private final double[] matrixReverse;

    public Kernel1D(double[] kernel)
    {        
        this(kernel, kernel.length/2);
    }

    public Kernel1D(double[] kernel, int centerColumnIndex)
    {                
        this.columnCount = kernel.length;
        if(columnCount == 0)
        {
            throw new IllegalArgumentException("'kernel' has no columns");
        }

        this.centerColumnIndex = centerColumnIndex;

        this.matrix = Arrays.copyOf(kernel, kernel.length);
        this.matrixReverse = ArrayUtilities.reverse(matrix);
    }


    public Kernel1D changeXRadius(int xRadiusNew)
    {        
        int diff = xRadiusNew - matrix.length/2;

        double[] matrixNew = new double[2*xRadiusNew + 1];

        if(diff > 0)
        {
            System.arraycopy(matrix, 0, matrixNew, diff, matrix.length);
        }
        else
        {
            System.arraycopy(matrix, -diff, matrixNew, 0, matrix.length + 2*diff);
        }       

        Kernel1D kernelNew = new Kernel1D(matrixNew);

        return kernelNew;
    }

    public Kernel1D setElement(int column, double value)
    {
        if(column <0 || column >= matrix.length)
        {
            throw new IllegalArgumentException("The 'column' argument is out of bounds");
        }

        double[] matrixNew = Arrays.copyOf(matrix, matrix.length);
        matrixNew[column] = value;

        Kernel1D kernelNew = new Kernel1D(matrixNew, this.centerColumnIndex);

        return kernelNew;
    }


    public Kernel1D multiply(double factor)
    {
        int n = matrix.length;

        double[] matrixNew = new double[n];

        for(int i = 0; i<n; i++)
        {
            matrixNew[i] = factor*matrix[i];
        }

        Kernel1D kernelNew = new Kernel1D(matrixNew, this.centerColumnIndex);

        return kernelNew;
    }

    public Kernel1D divide(double divisor)
    {
        double factor = 1./divisor;
        return multiply(factor);
    }

    public int getColumnCount()
    {
        return columnCount;                
    }

    public int getLeftRadius()
    {
        return centerColumnIndex;
    }

    public int getRightRadius()
    {
        int radius = columnCount - 1 - centerColumnIndex;
        return radius;
    }

    public int getCenterIndex()
    {
        return centerColumnIndex;
    }

    public int getRowCount()
    {
        return 1;
    }

    public double[] getArray()
    {
        return Arrays.copyOf(matrix, matrix.length);
    }

    public boolean isSeparable()
    {
        return false;
    }

    public double convolve(int j, double[] image, int imageColumnCount)
    {
        double value = 0;

        int imageMinColumn = j - centerColumnIndex;

        for(int l = 0; l<columnCount; l++)
        {
            double kernelValue = matrixReverse[l];

            int column = imageMinColumn + l;

            double imageValue = getValue(column, image, imageColumnCount);


            value = value + imageValue*kernelValue;
        }

        return value;
    }    

    @Override
    public double[] convolve(double[] image, int imageColumnCount)
    {        
        double[] transformed = new double[imageColumnCount];

        int maxColumnInside = imageColumnCount - columnCount - centerColumnIndex;

        for(int i = 0 ; i<centerColumnIndex; i++)
        {
            transformed[i] = convolve(i, image, imageColumnCount);
        }
        for(int i = maxColumnInside; i<imageColumnCount; i++)
        {
            transformed[i] = convolve(i, image, imageColumnCount);
        }

        convolveApartFromMargins(image, transformed, imageColumnCount);

        return transformed;
    }    

    //the transformed array must be of the same length as image, but the margins are not filled
    //by this method
    public double[] convolveApartFromMargins(double[] image, double[] transformed, int imageColumnCount)
    {
        int maxColumnInside = imageColumnCount - columnCount - centerColumnIndex;

        for(int j = centerColumnIndex; j<maxColumnInside; j++)
        {
            int imageMinColumn = j - centerColumnIndex;

            double value = 0;

            for(int k = 0; k<columnCount; k++)
            {
                double kernelValue = matrixReverse[k];

                int column = imageMinColumn + k;

                double imageValue = image[column];
                value = value + imageValue*kernelValue;
            }

            transformed[j] = value;
        }

        return transformed;
    }

    protected double getValue(int column, double[] matrix, int width)
    { 
        if (column < 0 ) {column = 0;}
        else if (column>=width) {column = width-1;} 
        return matrix[column]; 
    }
}
