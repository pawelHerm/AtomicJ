package atomicJ.data.units;

import atomicJ.utilities.ArrayUtilities;

public class QuantityArray2DExpression 
{
    private final double[][] values;
    private final Quantity quantity;

    public QuantityArray2DExpression(double[][] values, Quantity quantity)
    {
        this.values = values;
        this.quantity = quantity;
    }

    public QuantityArray2DExpression(QuantityArray2DExpression other)
    {
        this.values = ArrayUtilities.deepCopy(other.values);
        this.quantity = other.quantity;
    }

    public double[][] getValues()
    {
        return values;
    }

    public Quantity getQuantity()
    {
        return quantity;
    }
}
