package atomicJ.data.units;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum StandardUnitType
{
    METER("m",Arrays.<String>asList("metre","meter")), NEWTON("N",Collections.singletonList("newton")), JOULE("J",Collections.singletonList("joule")), WATT("W",Collections.singletonList("watt")), PASCAL("Pa",Collections.singletonList("pascal")),
    VOLT("V",Collections.singletonList("volt")), COULOMB("C",Collections.singletonList("coulomb")), AMPERE("A",Arrays.<String>asList("ampere","amp")),RADIAN("rad",Collections.singletonList("rad")),HERTZ("Hz",Collections.singletonList("hertz")), SECOND("s",Collections.singletonList("second")),KELVIN("K",Collections.singletonList("kelvin"));

    private final String symbol;
    private final PrefixedUnit unit;
    private final List<String> unitNames;

    private StandardUnitType(String symbol, List<String> unitNames)
    {
        this.symbol = symbol;
        this.unit = new SimplePrefixedUnit(symbol);
        this.unitNames = unitNames;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public static boolean isUnitTypeRecognizable(String name)
    {
        for(StandardUnitType mode : StandardUnitType.values())
        {
            if(mode.unitNames.contains(name.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }

    public static StandardUnitType getUnitType(String name)
    {
        for(StandardUnitType mode : StandardUnitType.values())
        {
            if(mode.unitNames.contains(name.toLowerCase()))
            {
                return mode;
            }
        }

        throw new IllegalArgumentException("No StandardUnitType corresponds to " + name);
    }
}
