package atomicJ.statistics;

import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.RegressionUtilities;

public class RegressionModel
{
    private final double[][] design;
    private final double[] observations;

    private final double[][] weightedDesign;
    private final double[] weightedObservations;

    public RegressionModel(double[][] design, double[] observations)
    {
        this.design = design;
        this.observations = observations;

        this.weightedDesign = design;
        this.weightedObservations = observations;
    }

    public RegressionModel(double[][] design, double[][] weightedDesign, double[] observations, double[] weightedObservations)
    {
        this.design = design;
        this.observations = observations;

        this.weightedDesign = weightedDesign;
        this.weightedObservations = weightedObservations;
    }

    public int getObservationCount()
    {
        return observations.length;
    }

    public int getCoefficientsCount()
    {
        int p = design.length == 0 ? 0 : design[0].length;

        return p;
    }

    public double[] getObservations()
    {
        return observations;
    }

    public double[] getWeightedObservations()
    {
        return weightedObservations;
    }

    public double[][] getDesign()
    {
        return design;
    }

    public double[][] getWeightedDesign()
    {
        return weightedDesign;
    }

    public static RegressionModel getRegressionModel(double[] xs, double[] ys, double[] zs, int[] xDegrees, int[] yDegrees)
    {
        return getRegressionModel(xs, ys, zs, 0, xs.length, xDegrees, yDegrees);
    }

    //Builds regression model from points, whose coordinates are in the arrays xs, ys and zs, between the indices from (inclusive) and to (exclusive)
    public static RegressionModel getRegressionModel(double[] xs, double[] ys, double[] zs, int from, int to, int[] xDegrees, int[] yDegrees)
    {
        if(from >= to)
        {
            throw new IllegalArgumentException("The argument 'from' must be smaller than 'to'");
        }
        if(xs.length != ys.length || ys.length != zs.length)
        {
            throw new IllegalArgumentException("The arrays xs, ys and zs shoul have the same length");
        }

        int n = to - from;
        int p = xDegrees.length + yDegrees.length;

        double[][] design = new double[n][]; 
        double[] obs = new double[n];

        for(int i = from; i < to; i++)
        {
            double[] currentRow = new double[p];

            double x = xs[i];
            double y = ys[i];

            double response = zs[i];

            int index = 0;

            for(int k : xDegrees)
            {
                currentRow[index++] = MathUtilities.intPow(x, k);
            }
            for(int k : yDegrees)
            {
                currentRow[index++] = MathUtilities.intPow(y, k);
            }

            obs[i] = response;
            design[i] = currentRow;
        }

        RegressionModel model = new RegressionModel(design, obs);
        return model;
    }

    public static RegressionModel getRegressionModel(double[][] matrixData, int rowCount, int columnCount, int[] xDegrees, int[] yDegrees)
    {
        int n = rowCount*columnCount;
        int p = xDegrees.length + yDegrees.length;

        double[][] design = new double[n][]; 
        double[] obs = new double[n];


        for(int i = 0, sampleIndex = 0;i<rowCount;i++)
        {
            double[] row = matrixData[i];

            for(int j = 0; j<columnCount; j++)
            {
                double response = row[j];

                double[] currentRow = new double[p];

                int index = 0;

                for(int k : xDegrees)
                {
                    currentRow[index++] = MathUtilities.intPow(j, k);
                }
                for(int k : yDegrees)
                {
                    currentRow[index++] = MathUtilities.intPow(i, k);
                }

                obs[sampleIndex] = response;
                design[sampleIndex++] = currentRow;
            }
        }

        RegressionModel model = new RegressionModel(design, obs);
        return model;
    }

    public static RegressionModel getRegressionModel(double[] data, double[] model)
    {
        int n = data.length;
        int p =  model.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double y = data[i];
            double[] currentRow = new double[p];
            int j = 0;
            for(double exponent: model)
            {
                currentRow[j++] = Math.pow(i,exponent);
            }

            design[i] = currentRow;
            obs[i] = y;
        }


        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModel(double[] data, int degree, boolean constant)
    {
        return getRegressionModel(data, RegressionUtilities.getModelInt(degree, constant));
    }

    public static RegressionModel getRegressionModel(double[] data, int[] model)
    {
        int n = data.length;
        int p =  model.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double y = data[i];
            double[] currentRow = new double[p];
            int j = 0;
            for(int exponent: model)
            {
                currentRow[j++] = MathUtilities.intPow(i,exponent);
            }

            design[i] = currentRow;
            obs[i] = y;
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModel(double[][] data, int degree, boolean constant)
    {
        return getRegressionModel(data, 0, data.length, RegressionUtilities.getModelInt(degree, constant));
    }

    public static RegressionModel getRegressionModel(double[][] data, int[] model)
    {
        return getRegressionModel(data, 0, data.length, model);
    }

    public static RegressionModel getRegressionModel(double[][] data, int from, int to, int degree, boolean constant)
    {
        return getRegressionModel(data, from, to, RegressionUtilities.getModelInt(degree, constant));
    }

    public static RegressionModel getRegressionModel(double[][] data, int from, int to, int[] model)
    {        
        int n = to - from;
        int p = model.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i + from];
            double x = pt[0];
            double y = pt[1];
            double[] currentRow = new double[p];
            int j = 0;
            for(int exponent: model)
            {
                currentRow[j++] = MathUtilities.intPow(x,exponent);
            }
            design[i] = currentRow;
            obs[i] = y;
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModel(double[] ys, double[] xs, int[] model)
    {
        return getRegressionModel(ys, xs, 0, ys.length, model);
    }

    public static RegressionModel getRegressionModel(double[] ys, double[] xs, int degree, boolean constant)
    {
        return getRegressionModel(ys, xs, 0, ys.length,  RegressionUtilities.getModelInt(degree, constant));
    }

    public static RegressionModel getRegressionModel(double[] ys, double[] xs, int from, int to, int degree, boolean constant)
    {
        return getRegressionModel(ys, xs, from, to,  RegressionUtilities.getModelInt(degree, constant));
    }

    public static RegressionModel getRegressionModel(double[] ys, double[] xs, int from, int to, int[] model)
    {        
        int n = to - from;
        int p = model.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];
        for(int i = 0;i<n;i++)
        {
            double x = xs[i + from];
            double y = ys[i + from];
            double[] currentRow = new double[p];
            int j = 0;
            for(int exponent: model)
            {
                currentRow[j++] = MathUtilities.intPow(x,exponent);
            }
            design[i] = currentRow;
            obs[i] = y;
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModelForSingleExponent(double[][] data, int exponent)
    {        
        int n = data.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            design[i] = new double[] {MathUtilities.intPow(pt[0],exponent)};
            obs[i] = pt[1];
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModelForSingleExponent(double[] ys, double[] xs, int exponent)
    {        
        int n = ys.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];
        for(int i = 0;i<n;i++)
        {
            design[i] = new double[] {MathUtilities.intPow(xs[i],exponent)};
            obs[i] = ys[i];
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModelForSingleExponent(double[][] data, double exponent)
    {        
        int n = data.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            design[i] = new double[] {Math.pow(pt[0],exponent)};
            obs[i] = pt[1];
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModelForSingleExponent(double[] ys, double[] xs, double exponent)
    {        
        int n = ys.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];
        for(int i = 0;i<n;i++)
        {
            design[i] = new double[] {Math.pow(xs[i],exponent)};
            obs[i] = ys[i];
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModel(double[][] data, double[] model)
    {
        return getRegressionModel(data, 0, data.length, model);
    }

    public static RegressionModel getRegressionModel(double[][] data, int from, int to, double[] model)
    {
        int n = to - from;
        int p =  model.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i + from];
            double x = pt[0];
            double y = pt[1];
            double[] currentRow = new double[p];
            int j = 0;
            for(double exponent: model)
            {
                currentRow[j++] = Math.pow(x,exponent);
            }
            design[i] = currentRow;
            obs[i] = y;
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    public static RegressionModel getRegressionModel(double[] ys, double[] xs, double[] model)
    {
        return getRegressionModel(ys, xs, 0, ys.length, model);
    }

    public static RegressionModel getRegressionModel(double[] ys, double[] xs, int from, int to, double[] model)
    {
        int n = to - from;
        int p =  model.length;
        double[][] design = new double[n][]; 
        double[] obs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double x = xs[i + from];
            double y = ys[i + from];
            double[] currentRow = new double[p];
            int j = 0;
            for(double exponent: model)
            {
                currentRow[j++] = Math.pow(x,exponent);
            }
            design[i] = currentRow;
            obs[i] = y;
        }

        RegressionModel regModel = new RegressionModel(design, obs);

        return regModel;
    }

    //here we assume that errors are uncorrelated, so that we pass only the diagonal
    //of weight matrix (i.e. 1D array weights)
    public static RegressionModel getRegressionModel(double[][] data, double[] weights, double[] model)
    {
        int n = data.length;
        int p =  model.length;
        double[][] design = new double[n][]; 
        double[][] weightedDesign = new double[n][]; 

        double[] obs = new double[n];
        double[] wobs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double x = pt[0];
            double y = pt[1];
            double wSqrt = Math.sqrt(weights[i]);

            double[] designMatrixRow = new double[p];
            double[] weightedDesignMatrixRow = new double[p];

            int j = -1;
            for(double exponent: model)
            {
                double basis = Math.pow(x,exponent);
                designMatrixRow[++j] = basis;
                weightedDesignMatrixRow[j] = wSqrt*basis;
            }
            design[i] = designMatrixRow;
            weightedDesign[i] = weightedDesignMatrixRow;
            obs[i] = y;
            wobs[i] = wSqrt*y;
        }
        RegressionModel regModel = new RegressionModel(design, weightedDesign, obs, wobs);

        return regModel;
    }

    //performs fit only to the part of data between "from" index (inclusive) to "to" index (exclusive)
    //the array of weights should have the length of to - from;
    public static RegressionModel getWeightedDesignAndWeightedObservations(double[][] data, int from, int to, double[] weights, int degree)
    {
        int n = to - from;
        int p =  degree + 1;

        double[][] weightedDesign = new double[n][]; 
        double[] wobs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double[] dataPoint = data[i + from];
            double x = dataPoint[0];
            double y = dataPoint[1];
            double wSqrt = Math.sqrt(weights[i]);

            double[] weightedDesignMatrixRow = new double[p];

            for(int exp = 0; exp <= degree; exp++)
            {
                double basis = MathUtilities.intPow(x,exp);
                weightedDesignMatrixRow[exp] = wSqrt*basis;
            }

            weightedDesign[i] = weightedDesignMatrixRow;
            wobs[i] = wSqrt*y;
        }

        RegressionModel regModel = new RegressionModel(null, weightedDesign, null, wobs);

        return regModel;
    }

    public static RegressionModel getWeightedDesignAndWeightedObservations(double[] data, int from, int to, double[] weights, int degree)
    {
        int n = to - from;
        int p =  degree + 1;

        double[][] weightedDesign = new double[n][]; 
        double[] wobs = new double[n];

        for(int i = 0;i<n;i++)
        {
            double wSqrt = Math.sqrt(weights[i]);

            double[] weightedDesignMatrixRow = new double[p];

            for(int exp = 0; exp <= degree; exp++)
            {
                double basis = MathUtilities.intPow(i + from,exp);
                weightedDesignMatrixRow[exp] = wSqrt*basis;
            }

            weightedDesign[i] = weightedDesignMatrixRow;
            wobs[i] = wSqrt*data[i + from];
        }

        RegressionModel regModel = new RegressionModel(null, weightedDesign, null, wobs);

        return regModel;
    }
}