package atomicJ.readers.mdt;

import java.util.HashMap;
import java.util.Map;

import atomicJ.data.units.SIPrefix;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;

public class UnitCodes 
{
    private static final Map<Integer, UnitExpression> unitCodes = new HashMap<>();

    static{
        unitCodes.put(-10, new UnitExpression(1, new SimplePrefixedUnit("m", SIPrefix.c, -1)));
        unitCodes.put(-9, new UnitExpression(1, new SimplePrefixedUnit("N - 9", SIPrefix.Empty))); //reserved
        unitCodes.put(-8, new UnitExpression(1, new SimplePrefixedUnit("N - 8", SIPrefix.Empty))); //reserved
        unitCodes.put(-7, new UnitExpression(1, new SimplePrefixedUnit("N - 7", SIPrefix.Empty)));//reserved
        unitCodes.put(-6, new UnitExpression(1, new SimplePrefixedUnit("N - 6", SIPrefix.Empty))); //reserved
        unitCodes.put(-5, new UnitExpression(1, new SimplePrefixedUnit("m", SIPrefix.Empty))); //metric
        unitCodes.put(-4, new UnitExpression(1, new SimplePrefixedUnit("m", SIPrefix.c))); //metric
        unitCodes.put(-3, new UnitExpression(1, new SimplePrefixedUnit("m", SIPrefix.m))); //metric
        unitCodes.put(-2, new UnitExpression(1, Units.MICRO_METER_UNIT)); //metric
        unitCodes.put(-1, new UnitExpression(1, new SimplePrefixedUnit("m", SIPrefix.n))); //metric
        unitCodes.put(0, new UnitExpression(0.1, new SimplePrefixedUnit("m", SIPrefix.n))); //metric, angstrom
        unitCodes.put(1, new UnitExpression(1, new SimplePrefixedUnit("A", SIPrefix.n))); //current
        unitCodes.put(2, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.Empty))); //voltage
        unitCodes.put(3, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //dimensionless
        unitCodes.put(4, new UnitExpression(1, new SimplePrefixedUnit("Hz", SIPrefix.h))); //frequency
        unitCodes.put(5, new UnitExpression(1, new SimplePrefixedUnit("deg", SIPrefix.Empty))); //angle
        unitCodes.put(6, new UnitExpression(1, new SimplePrefixedUnit("%", SIPrefix.Empty))); //dimensionless
        unitCodes.put(7, new UnitExpression(1, new SimplePrefixedUnit("\u2103", SIPrefix.Empty))); //temperature
        unitCodes.put(8, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.Empty))); //voltage?
        unitCodes.put(9, new UnitExpression(1, new SimplePrefixedUnit("s", SIPrefix.Empty))); //time
        unitCodes.put(10, new UnitExpression(1, new SimplePrefixedUnit("s", SIPrefix.m))); //time
        unitCodes.put(11, new UnitExpression(1, new SimplePrefixedUnit("s", SIPrefix.u))); //time
        unitCodes.put(12, new UnitExpression(1, new SimplePrefixedUnit("s", SIPrefix.n))); //time
        unitCodes.put(13, new UnitExpression(1, new SimplePrefixedUnit("Counts", SIPrefix.Empty))); //numbers
        unitCodes.put(14, new UnitExpression(1, new SimplePrefixedUnit("Pixels", SIPrefix.Empty))); //numbers
        unitCodes.put(15, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //reserved for SFOM
        unitCodes.put(16, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //reserved for SFOM
        unitCodes.put(17, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //reserved for SFOM
        unitCodes.put(18, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //reserved for SFOM
        unitCodes.put(19, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //reserved for SFOM
        unitCodes.put(20, new UnitExpression(1, new SimplePrefixedUnit("A", SIPrefix.Empty))); //current
        unitCodes.put(21, new UnitExpression(1, new SimplePrefixedUnit("A", SIPrefix.m))); //current
        unitCodes.put(22, new UnitExpression(1, new SimplePrefixedUnit("A", SIPrefix.u))); //current
        unitCodes.put(23, new UnitExpression(1, new SimplePrefixedUnit("A", SIPrefix.n))); //current
        unitCodes.put(24, new UnitExpression(1, new SimplePrefixedUnit("A", SIPrefix.p))); //current
        unitCodes.put(25, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.Empty))); //voltage
        unitCodes.put(26, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.m))); //voltage
        unitCodes.put(27, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.u))); //voltage
        unitCodes.put(28, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.n))); //voltage
        unitCodes.put(29, new UnitExpression(1, new SimplePrefixedUnit("V", SIPrefix.p))); //voltage

        unitCodes.put(30, new UnitExpression(1, new SimplePrefixedUnit("N", SIPrefix.Empty))); //force
        unitCodes.put(31, new UnitExpression(1, new SimplePrefixedUnit("N", SIPrefix.m))); //force
        unitCodes.put(32, new UnitExpression(1, new SimplePrefixedUnit("N", SIPrefix.u))); //force
        unitCodes.put(33, new UnitExpression(1, new SimplePrefixedUnit("N", SIPrefix.n))); //force
        unitCodes.put(34, new UnitExpression(1, new SimplePrefixedUnit("N", SIPrefix.p))); //force
        unitCodes.put(35, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //Reserved for DOS version (Ext)
        unitCodes.put(36, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //Reserved for DOS version (Ext)
        unitCodes.put(37, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //Reserved for DOS version (Ext)
        unitCodes.put(38, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //Reserved for DOS version (Ext)
        unitCodes.put(39, new UnitExpression(1, new SimplePrefixedUnit("", SIPrefix.Empty))); //Reserved for DOS version (Ext)
    }

    public static UnitExpression getUnitExpression(int unitCode)
    {
        UnitExpression unitExpression = unitCodes.get(new Integer(unitCode));
        return unitExpression;
    }
}
