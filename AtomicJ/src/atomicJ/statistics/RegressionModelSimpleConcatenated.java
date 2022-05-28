package atomicJ.statistics;

import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.RegressionUtilities;

public class RegressionModelSimpleConcatenated
{
    private final int p;//coefficient count
    private final double[] design;
    private final double[] observations;

    private RegressionModelSimpleConcatenated(double[] design, double[] observations, int p)
    {
        this.design = design;
        this.observations = observations;
        this.p = p;
    }

    public int getObservationCount()
    {
        return observations.length;
    }

    public int getCoefficientsCount()
    {
        return p;
    }

    public double[] getObservations()
    {
        return observations;
    }

    public double[] getDesign()
    {
        return design;
    }

    public static RegressionModelSimpleConcatenated getRegressionModel(double[][] data, int deg, boolean constant)
    {        
        int n = data.length;
        int p = deg + MathUtilities.boole(constant); 

        double[] design = new double[n*p]; 
        double[] obs = new double[n];
        if(deg == 1 && !constant)
        {
            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                design[i] = pt[0];
                obs[i] = pt[1];
            }
        }
        else if(deg == 2 && !constant)
        {
            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                double x = pt[0];
                design[2*i] = x;
                design[2*i + 1] = x*x;
                obs[i] = pt[1];
            }
        }
        else if (deg == 3 && !constant)
        {
            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                double x = pt[0];
                int index = 3*i;
                double xSq = x*x;
                design[index] = x;
                design[index + 1] = xSq;
                design[index + 2] = xSq*x;
                obs[i] = pt[1];
            }
        }
        else
        {
            int[] model = RegressionUtilities.getModelInt(deg, constant);   

            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                double x = pt[0];
                int j = i*p;
                for(int exponent: model)
                {
                    design[j++] = MathUtilities.intPow(x,exponent);
                }
                obs[i] = pt[1];
            }
        }

        RegressionModelSimpleConcatenated regModel = new RegressionModelSimpleConcatenated(design, obs, p);

        return regModel;
    }
}