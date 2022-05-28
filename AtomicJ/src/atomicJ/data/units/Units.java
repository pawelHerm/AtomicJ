package atomicJ.data.units;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import atomicJ.utilities.MultiMap;

public class Units
{
    public static final PrefixedUnit PICO_METER_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(), SIPrefix.p);
    public static final PrefixedUnit NANO_METER_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(), SIPrefix.n);
    public static final PrefixedUnit MICRO_METER_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(), SIPrefix.u);
    public static final PrefixedUnit MILI_METER_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(), SIPrefix.m);
    public static final PrefixedUnit CENTI_METER_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(), SIPrefix.c);

    public static final PrefixedUnit VOLT_UNIT = new SimplePrefixedUnit(StandardUnitType.VOLT.getSymbol());
    public static final PrefixedUnit MILI_VOLT_UNIT = new SimplePrefixedUnit(StandardUnitType.VOLT.getSymbol(), SIPrefix.m);

    public static final PrefixedUnit AMPERE_UNIT = new SimplePrefixedUnit(StandardUnitType.AMPERE.getSymbol());
    public static final PrefixedUnit NANO_AMPERE_UNIT = new SimplePrefixedUnit(StandardUnitType.AMPERE.getSymbol(), SIPrefix.n);

    public static final PrefixedUnit FARAD_UNIT = new SimplePrefixedUnit("F");

    public static final PrefixedUnit MICRO_METER_PER_VOLT_UNIT = new RationalPrefixedUnit(Units.MICRO_METER_UNIT, Units.VOLT_UNIT);
    public static final PrefixedUnit MICROMETER_PER_NANO_AMPERE_UNIT = new RationalPrefixedUnit(Units.MICRO_METER_UNIT, Units.NANO_AMPERE_UNIT);

    public static final PrefixedUnit METER_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol());
    public static final PrefixedUnit METER_SQUARED_UNIT = new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(), SIPrefix.Empty, 2);
    public static final PrefixedUnit NEWTON_UNIT = new SimplePrefixedUnit(StandardUnitType.NEWTON.getSymbol());
    public static final PrefixedUnit NANO_NEWTON_UNIT = new SimplePrefixedUnit(StandardUnitType.NEWTON.getSymbol(), SIPrefix.n);

    public static final PrefixedUnit PASCAL_UNIT = new SimplePrefixedUnit(StandardUnitType.PASCAL.getSymbol());
    public static final PrefixedUnit KILO_PASCAL_UNIT = new SimplePrefixedUnit(StandardUnitType.PASCAL.getSymbol(), SIPrefix.k);

    public static final SimplePrefixedUnit MILI_JOUL_UNIT = new SimplePrefixedUnit(StandardUnitType.JOULE.getSymbol(), SIPrefix.m);
    public static final PrefixedUnit MILI_JOUL_PER_SQUARE_METER_UNIT = new RationalPrefixedUnit(MILI_JOUL_UNIT, METER_SQUARED_UNIT);

    public static final PrefixedUnit SECOND_UNIT = new SimplePrefixedUnit(StandardUnitType.SECOND.getSymbol());
    public static final PrefixedUnit HERTZ_UNIT = new SimplePrefixedUnit("Hz");

    public static final PrefixedUnit NEWTON_PER_METER = new RationalPrefixedUnit(Units.NEWTON_UNIT, Units.METER_UNIT);


    public static final SimplePrefixedUnit DEGREE_UNIT = new SimplePrefixedUnit("deg");
    public static final PrefixedUnit NANO_NEWTON_PER_MICRO_METER_UNIT = new RationalPrefixedUnit(NANO_NEWTON_UNIT, MICRO_METER_UNIT);

    private static final MultiMap<PrefixedUnit, PrefixedUnit> defaultUnits = new MultiMap<>();

    static
    {      
        defaultUnits.putAll(METER_UNIT, UnitUtilities.buildUnitList(StandardUnitType.METER.getSymbol(), 1, SIPrefix.p, SIPrefix.Empty));
        defaultUnits.putAll(METER_SQUARED_UNIT, UnitUtilities.buildUnitList(StandardUnitType.METER.getSymbol(), 2, SIPrefix.p, SIPrefix.Empty));
        defaultUnits.putAll(NEWTON_UNIT, UnitUtilities.buildUnitList(StandardUnitType.NEWTON.getSymbol(), 1, SIPrefix.p, SIPrefix.Empty));
        defaultUnits.putAll(PASCAL_UNIT, UnitUtilities.buildUnitList(StandardUnitType.PASCAL.getSymbol(), 1, SIPrefix.Empty, SIPrefix.G));
    }

    public static List<PrefixedUnit> getPreferredDerivedUnits(PrefixedUnit unit)
    {        
        if(unit instanceof SimplePrefixedUnit)
        {
            for(Entry<PrefixedUnit, List<PrefixedUnit>> entry : defaultUnits.entrySet())
            {
                if(entry.getKey().isCompatible(unit))
                {                    
                    return entry.getValue();
                }
            }
            return unit.deriveUnits();
        }
        return Collections.singletonList(unit);
    }
}
