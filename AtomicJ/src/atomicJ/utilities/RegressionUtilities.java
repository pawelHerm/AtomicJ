package atomicJ.utilities;

public class RegressionUtilities 
{
    public static double[] getModel(int deg, boolean constant)
    {
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept; //Number of parameters

        double[] model = new double[p];
        for(int j = 0; j<p;)
        {
            model[j++] = j - intercept;
        }

        return model;
    }

    public static int[] getModelInt(int deg, boolean constant)
    {
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept; //Number of parameters

        int[] model = new int[p];
        for(int j = 0; j<p;)
        {
            model[j++] = j - intercept;
        }

        return model;
    }
}
