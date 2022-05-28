package atomicJ.data.units;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import atomicJ.utilities.MathUtilities;

public class UnitArray1DExpression 
{
    private final double[] values;
    private final PrefixedUnit unit;

    public UnitArray1DExpression(double[] values, PrefixedUnit unit)
    {
        this.values = values;
        this.unit = unit;
    }

    public UnitArray1DExpression(UnitArray1DExpression other)
    {
        this.values = Arrays.copyOf(other.values, other.values.length);
        this.unit = other.unit;
    }

    public double[] getValues()
    {
        return values;
    }

    public int getValueCount()
    {
        return values.length;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public UnitArray1DExpression multiply(double factor)
    {
        double[] valuesNew = getScaledValues(factor);
        return new UnitArray1DExpression(valuesNew, this.unit);
    }

    private double[] getScaledValues(double factor)
    {
        return MathUtilities.multiply(values, factor);
    }

    public UnitArray1DExpression derive(PrefixedUnit unitNew)
    {
        double conversionFactor = this.unit.getConversionFactorTo(unitNew);
        double[] valuesNews = getScaledValues(conversionFactor);

        return new UnitArray1DExpression(valuesNews, unitNew);
    }

    @Override
    public String toString()
    {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setMinimumFractionDigits(5);

        return UnitUtilities.toString(values, format, this.unit);
    }

    public String toString(NumberFormat format)
    {
        return UnitUtilities.toString(values, format, this.unit);
    }

    @Override
    public int hashCode()
    {
        int hashCode = 17;

        hashCode += 31*hashCode + Arrays.hashCode(this.values);
        hashCode += 31*hashCode + this.unit.hashCode();

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(this == other)
        {
            return true;
        }

        if(!(other instanceof UnitArray1DExpression))
        {
            return false;
        }

        UnitArray1DExpression that = (UnitArray1DExpression)other;

        boolean equals = Arrays.equals(this.values, that.values) && Objects.equals(this.unit, that.unit);

        return equals;
    }
}
