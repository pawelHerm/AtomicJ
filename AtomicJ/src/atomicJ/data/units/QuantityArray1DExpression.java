package atomicJ.data.units;

import java.util.Arrays;

public class QuantityArray1DExpression 
{
    private final double[] values;
    private final Quantity quantity;

    public QuantityArray1DExpression(double[] values, Quantity quantity)
    {
        this.values = values;
        this.quantity = quantity;
    }

    public QuantityArray1DExpression(QuantityArray1DExpression other)
    {
        this.values = Arrays.copyOf(other.values, other.values.length);
        this.quantity = other.quantity;
    }

    public double[] getValues()
    {
        return values;
    }

    public int getValueCount()
    {
        return values.length;
    }

    public Quantity getQuantity()
    {
        return quantity;
    }
}
