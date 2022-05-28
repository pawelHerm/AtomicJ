package atomicJ.data.units;

import java.util.Collections;
import java.util.List;

public enum StandardQuantityTypes 
{    
    LENGTH(StandardUnitType.METER.getUnit()) 
    {
        private final List<PrefixedUnit> units = UnitUtilities.buildUnitList(StandardUnitType.METER.getSymbol(), 1, SIPrefix.p, SIPrefix.k);

        @Override
        public List<PrefixedUnit> getDefaultUnits()
        {
            return Collections.unmodifiableList(units);
        }
    }, 

    ANGLE(StandardUnitType.RADIAN.getUnit())
    {
        @Override
        public List<PrefixedUnit> getDefaultUnits() {
            return Collections.emptyList();
        }
    },

    FREQUENCY(StandardUnitType.HERTZ.getUnit()) 
    {
        @Override
        public List<PrefixedUnit> getDefaultUnits() {
            return Collections.emptyList();
        }
    },

    POWER(StandardUnitType.WATT.getUnit()) {
        @Override
        public List<PrefixedUnit> getDefaultUnits() 
        {
            return Collections.emptyList();
        }
    },

    VOLTAGE(StandardUnitType.VOLT.getUnit())
    {
        @Override
        public List<PrefixedUnit> getDefaultUnits() 
        {
            return Collections.emptyList();
        }
    }, 

    FORCE(StandardUnitType.NEWTON.getUnit())
    {

        @Override
        public List<PrefixedUnit> getDefaultUnits() 
        {
            return Collections.emptyList();
        }
    },

    CURRENT(StandardUnitType.AMPERE.getUnit())
    {
        @Override
        public List<PrefixedUnit> getDefaultUnits() 
        {
            return Collections.emptyList();
        }
    },

    TEMPERATURE(StandardUnitType.KELVIN.getUnit()) {
        @Override
        public List<PrefixedUnit> getDefaultUnits() {
            return Collections.emptyList();
        }
    },

    PRESSURE(StandardUnitType.PASCAL.getUnit()) 
    {
        @Override
        public List<PrefixedUnit> getDefaultUnits() {
            return Collections.emptyList();
        }
    },

    TIME(StandardUnitType.SECOND.getUnit()) 
    {
        @Override
        public List<PrefixedUnit> getDefaultUnits() {
            return Collections.emptyList();
        }
    },

    YOUNGS_MODULUS(StandardUnitType.PASCAL.getUnit())
    {
        private final List<PrefixedUnit> units = UnitUtilities.buildUnitList(StandardUnitType.PASCAL.getSymbol(), 1, SIPrefix.Empty, SIPrefix.G);

        @Override
        public List<PrefixedUnit> getDefaultUnits() 
        {
            return Collections.unmodifiableList(units);
        }
    };


    private final PrefixedUnit basicUnit;

    private StandardQuantityTypes(PrefixedUnit basicUnit)
    {
        this.basicUnit = basicUnit;
    }

    public PrefixedUnit getBasicUnit()
    {
        return basicUnit;
    }

    public abstract List<PrefixedUnit> getDefaultUnits();
    public boolean isCompatible(PrefixedUnit otherUnit)
    {
        return this.basicUnit.isCompatible(otherUnit);
    }
}
