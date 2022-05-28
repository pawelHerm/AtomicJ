package atomicJ.imageProcessing;

import atomicJ.utilities.ArrayUtilities;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class Kernel2D 
{        
    private final int rank;
    private final int rowCount;
    private final int columnCount;
    private final int centerRow;
    private final int centerColumn;

    private final double[][] matrix;
    private final double[][] matrixReverse;

    public Kernel2D(double[][] kernel)
    {
        boolean rowsEqual = rowsEqual(kernel);

        if(!rowsEqual)
        {
            throw new IllegalArgumentException("'kernel' " +
                    "should have all rows of the same length");
        }

        this.rowCount = kernel.length;
        if(rowCount == 0)
        {
            throw new IllegalArgumentException("'kernel' has not rows");
        }

        boolean rowCountOdd = (rowCount % 2 == 1);
        if(!rowCountOdd)
        {
            throw new IllegalArgumentException("'kernel' should have odd numver of rows");
        }


        this.columnCount = kernel[0].length;
        if(columnCount == 0)
        {
            throw new IllegalArgumentException("'kernel' has no columns");
        }

        boolean columnCountOdd = (columnCount % 2 == 1);
        if(!columnCountOdd)
        {
            throw new IllegalArgumentException("'kernel' should have odd numver of columns");
        }

        this.centerRow = rowCount/2;
        this.centerColumn = columnCount/2;

        this.matrix = ArrayUtilities.deepCopy(kernel);
        this.matrixReverse = ArrayUtilities.deepReverse(matrix);
        this.rank = Matrix.constructWithCopy(matrix).rank();
    }

    public Kernel2D changeYRadius(int yRadiusNew)
    {
        int diff = yRadiusNew - matrix.length/2;

        double[][] matrixNew;
        if(diff > 0)
        {
            matrixNew = ArrayUtilities.addRows(this.matrix, 0, diff, diff);
        }
        else
        {
            matrixNew = ArrayUtilities.removeRows(this.matrix, -diff, -diff);
        }

        Kernel2D kernelNew = new Kernel2D(matrixNew);

        return kernelNew;
    }

    public Kernel2D changeXRadius(int xRadiusNew)
    {        
        int diff = xRadiusNew - matrix[0].length/2;

        double[][] matrixNew;

        if(diff > 0)
        {
            matrixNew = ArrayUtilities.addColumns(matrix, 0, diff, diff);
        }
        else
        {
            matrixNew = ArrayUtilities.removeColumns(matrix, -diff, -diff);
        }       

        Kernel2D kernelNew = new Kernel2D(matrixNew);

        return kernelNew;
    }

    public Kernel2D setElement(int row, int column, double value)
    {
        if(row <0 || row >= matrix.length)
        {
            throw new IllegalArgumentException("The 'row' argument is out of bounds");
        }
        if(column <0 || column >= matrix[0].length)
        {
            throw new IllegalArgumentException("The 'column' argument is out of bounds");
        }

        double[][] matrixNew = ArrayUtilities.deepCopy(matrix);
        matrixNew[row][column] = value;

        Kernel2D kernelNew = new Kernel2D(matrixNew);

        return kernelNew;
    }

    public Kernel2D add(Kernel2D other)
    {        
        if(this.rowCount != other.rowCount)
        {
            throw new IllegalArgumentException("The kernels must have the row counts");
        }

        if(this.columnCount != other.columnCount)
        {
            throw new IllegalArgumentException("The kernels must have the same column counts");
        }

        double[][] matrixNew = new double[this.rowCount][];

        for(int i = 0; i<this.rowCount; i++)
        {
            double[] rowThis = this.matrix[i];        
            double[] rowOther = other.matrix[i];

            int m = rowThis.length;

            double[] rowNew = new double[m];
            for(int j = 0; j<m; j++)
            {
                rowNew[j] = rowThis[j] - rowOther[j];
            }
        }

        Kernel2D kernelNew = new Kernel2D(matrixNew);

        return kernelNew;
    }

    public Kernel2D multiply(double factor)
    {
        int n = matrix.length;

        double[][] matrixNew = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] rowOld = matrix[i];
            int m = rowOld.length;

            double[] rowNew = new double[m];
            for(int j = 0; j<m; j++)
            {
                rowNew[j] = factor*rowOld[j];
            }
        }

        Kernel2D kernelNew = new Kernel2D(matrixNew);

        return kernelNew;
    }

    public Kernel2D divide(double divisor)
    {
        double factor = 1./divisor;
        return multiply(factor);
    }

    public int getColumnCount()
    {
        int columnCount = matrix[0].length;
        return columnCount;                
    }

    public int getXRadius()
    {
        int xRadius = matrix[0].length/2;
        return xRadius;
    }

    public int getRowCount()
    {
        int rowCount = matrix.length;        
        return rowCount;
    }

    public int getYRadius()
    {
        int yRadius = matrix.length/2;
        return yRadius;
    }

    public double[][] getArray()
    {
        return ArrayUtilities.deepCopy(matrix);
    }

    private boolean rowsEqual(double[][] kernel)
    {
        boolean equal = true;
        int n = kernel.length;

        if(n == 0)
        {
            return equal;
        }

        int m = kernel[0].length;

        for(int i = 1; i<n;i++)
        {
            double[] row = kernel[i];

            equal = equal && (row.length == m);

            if(!equal)
            {
                break;
            }
        }

        return equal;
    }

    public boolean isSeparable()
    {
        boolean separable = rowCount > 1 && columnCount > 1 && (rank == 1);

        return separable;
    }

    public double convolve(int i, int j, double[][] image, int imageColumnCount, int imageRowCount)
    {
        double value = 0;

        int imageMinRow = i - centerRow;
        int imageMinColumn = j - centerColumn;

        for(int k = 0; k<rowCount; k++)
        {
            for(int l = 0; l<columnCount; l++)
            {
                double kernelValue = matrixReverse[k][l];

                int row = imageMinRow + k;
                int column = imageMinColumn + l;

                double imageValue = getValue(row, column, image, imageColumnCount, imageRowCount);
                value = value + imageValue*kernelValue;
            }
        }

        return value;
    }    

    public double[][] convolve(double[][] image, int imageColumnCount, int imageRowCount)
    {        
        double[][] transformed = new double[imageRowCount][imageColumnCount];

        int maxRowInside = imageRowCount - rowCount - centerRow;

        for(int j = 0; j<imageColumnCount; j++)
        {
            for(int i = 0 ; i<centerRow; i++)
            {
                transformed[i][j] = convolve(i, j, image, imageColumnCount, imageRowCount);
            }
            for(int i = maxRowInside; i<imageRowCount; i++)
            {
                transformed[i][j] = convolve(i, j, image, imageColumnCount, imageRowCount);
            }
        }

        int maxColumnInside = imageColumnCount - columnCount - centerColumn;

        for(int i = centerRow; i<maxRowInside; i++)
        {
            for(int j = 0 ; j<centerColumn; j++)
            {
                transformed[i][j] = convolve(i, j, image, imageColumnCount, imageRowCount);
            }
            for(int j = maxColumnInside; j<imageColumnCount; j++)
            {
                transformed[i][j] = convolve(i, j, image, imageColumnCount, imageRowCount);
            }
        }

        for(int i = centerRow ; i<maxRowInside; i++)
        {
            for(int j = centerColumn; j<maxColumnInside; j++)
            {
                int imageMinRow = i - centerRow;
                int imageMinColumn = j - centerColumn;

                double value = 0;

                for(int k = 0; k<rowCount; k++)
                {
                    for(int l = 0; l<columnCount; l++)
                    {
                        double kernelValue = matrixReverse[k][l];

                        int row = imageMinRow + k;
                        int column = imageMinColumn + l;

                        double imageValue = image[row][column];
                        value = value + imageValue*kernelValue;
                    }
                }

                transformed[i][j] = value;
            }
        }

        return transformed;
    }    

    protected double getValue(int row, int column, double[][] matrix, int width, int height)
    { 
        if (column < 0 ) column = 0; 
        else if (column>=width) column = width-1; 
        if (row < 0) row = 0; 
        else if (row>=height) row = height-1; 
        return matrix[row][column]; 
    }


    public KernelSeparation getSeparableFilters()
    {                
        if(!isSeparable())
        {
            return null;
        }

        //JAMA handles only matrices with more rows than columns
        //if this is not the case, we have to use transposition
        boolean moreRows = rowCount >= columnCount;
        Matrix m = Matrix.constructWithCopy(this.matrix);

        if(!moreRows)
        {
            m = m.transpose();
        }

        SingularValueDecomposition svd = m.svd();

        Matrix u = moreRows ? svd.getU() : svd.getV();
        Matrix v = moreRows ? svd.getV() : svd.getU();

        double svSqrt = Math.sqrt(svd.getS().get(0, 0));

        int n = u.getRowDimension();

        double[][] verticalKernelArray = new double[n][1];

        for(int i = 0; i<n; i++)
        {
            verticalKernelArray[i][0] = svSqrt*u.get(i, 0);
        }

        int k = v.getRowDimension();

        double[][] horizontalKernelArray = new double[1][k];

        for(int i = 0; i<k; i++)
        {
            horizontalKernelArray[0][i] = svSqrt*v.get(i, 0);
        }

        Kernel2D horizontalKernel = new Kernel2D(horizontalKernelArray);
        Kernel2D verticalKernel = new Kernel2D(verticalKernelArray);

        KernelSeparation separation = new KernelSeparation(verticalKernel, horizontalKernel);

        return separation;
    }
}
