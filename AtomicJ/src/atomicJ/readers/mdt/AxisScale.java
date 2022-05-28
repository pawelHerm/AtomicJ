package atomicJ.readers.mdt;

import java.nio.ByteBuffer;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;

public class AxisScale
{
    private final double offset; //r0, Zero offset, defined in physical units.
    private final double scale; //r, Step size in physical units.
    private final PrefixedUnit unit;

    private AxisScale(double offset, double scale, PrefixedUnit unit)
    {
        this.offset = offset;
        this.scale = scale;
        this.unit = unit;
    }


    public static AxisScale readInInstance(ByteBuffer buffer)
    {
        double offset = buffer.getFloat();
        double scale = buffer.getFloat();

        UnitExpression unitExpression = UnitCodes.getUnitExpression(buffer.getShort());

        PrefixedUnit unit = unitExpression.getUnit();

        return new AxisScale(offset, scale*unitExpression.getValue(), unit);
    }

    public UnitExpression getOffset()
    {
        return new UnitExpression(offset, unit);
    }

    public UnitExpression getScale()
    {
        return new UnitExpression(scale, unit);
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }
}
