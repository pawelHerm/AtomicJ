package atomicJ.readers.jpk;

public class JPKLinearConverter
{
    private static final JPKLinearConverter NULL_INSTANCE = new JPKLinearConverter(1,0);

    private final double multiplier;
    private final double offset;

    public JPKLinearConverter(double multiplier, double offset)
    {
        this.multiplier = multiplier;
        this.offset = offset;
    }

    public static JPKLinearConverter getNullInstance()
    {
        return NULL_INSTANCE;
    }

    double getMultiplier()
    {
        return multiplier;
    }

    double getOffset()
    {
        return offset;
    }

    public double[] convert(double[] data)
    {
        int n = data.length;
        double[] dataConverted = new double[n];
        for(int i = 0; i<n; i++)
        {
            double value = data[i];
            dataConverted[i] = multiplier*value + offset;
        }

        return dataConverted;
    }

    private double convert(double value)
    {
        double convertedValue = multiplier*value + offset;
        return convertedValue;
    }

    public JPKLinearConverter multiply(double coeff)
    {
        double multiplierNew = coeff*multiplier;
        double offsetNew = coeff*offset;

        JPKLinearConverter converterNew = new JPKLinearConverter(multiplierNew, offsetNew);

        return converterNew;
    }

    public JPKLinearConverter compose(JPKLinearConverter otherConverter)
    {
        double multiplier2 = otherConverter.getMultiplier();
        double offset2 = otherConverter.getOffset();

        double multiplierComposition = multiplier*multiplier2;
        double offsetComposition = offset*multiplier2 + offset2;

        JPKLinearConverter composition = new JPKLinearConverter(multiplierComposition, offsetComposition);

        return composition;
    }
}