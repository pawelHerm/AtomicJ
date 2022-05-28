package atomicJ.data.units;

import ome.units.UNITS;

public enum LociQuantityType 
{
    TIME(UNITS.SECOND,StandardQuantityTypes.TIME.getBasicUnit()),
    LENGTH(UNITS.METRE, StandardQuantityTypes.LENGTH.getBasicUnit()),
    ANGLE(UNITS.RADIAN,StandardQuantityTypes.ANGLE.getBasicUnit()), 
    ELECTRIC_POTENTIAL(UNITS.VOLT, StandardQuantityTypes.VOLTAGE.getBasicUnit()),
    FREQUENCY(UNITS.HERTZ, StandardQuantityTypes.FREQUENCY.getBasicUnit()),
    POWER(UNITS.WATT, StandardQuantityTypes.POWER.getBasicUnit()),
    PRESSURE(UNITS.PASCAL, StandardQuantityTypes.PRESSURE.getBasicUnit()),
    TEMPERATURE(UNITS.KELVIN, StandardQuantityTypes.TEMPERATURE.getBasicUnit());

    private final ome.units.unit.Unit baseUnit;
    private final PrefixedUnit atomicJUnit;

    LociQuantityType(ome.units.unit.Unit<?> baseUnit, PrefixedUnit atomicJUnit)
    {
        this.baseUnit = baseUnit;
        this.atomicJUnit = atomicJUnit;
    }

    public static UnitExpression convertToUnitExpression(ome.units.quantity.Quantity quantity)
    {
        LociQuantityType lq = getQuantity(quantity);
        if(lq == null)
        {
            ome.units.unit.Unit<?> unit = quantity.unit();
            return new UnitExpression(quantity.value().doubleValue(), new SimplePrefixedUnit(unit.getSymbol()));
        }

        double valueConverted = quantity.unit().convertValue(quantity.value(), lq.baseUnit);
        return new UnitExpression(valueConverted, lq.atomicJUnit);
    }

    public static LociQuantityType getQuantity(ome.units.quantity.Quantity quantity)
    {
        for(LociQuantityType lq : LociQuantityType.values())
        {
            if(quantity.unit().isConvertible(lq.baseUnit))
            {
                return lq;
            }
        }

        return null;
    }
}
