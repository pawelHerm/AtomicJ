package atomicJ.readers.asylum;

import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;

public enum AsylumImageChannelType
{
    HEIGHT(new UnitQuantity("Height", Units.METER_UNIT)),
    Z_SENSOR(new UnitQuantity("ZSensor", Units.METER_UNIT)),
    DEFLECTION(new UnitQuantity("Deflection", Units.METER_UNIT)),
    AMPLITUDE(new UnitQuantity("Amplitude", Units.METER_UNIT)),
    PHASE(new UnitQuantity("Phase", Units.DEGREE_UNIT)),
    CURRENT(new UnitQuantity("Current", Units.AMPERE_UNIT)),
    FREQUENCY( new UnitQuantity("Frequency", Units.HERTZ_UNIT)),
    CAPACITANCE(new UnitQuantity("Capacitance", Units.FARAD_UNIT)),
    POTENTIAL(new UnitQuantity("Potential", Units.VOLT_UNIT)),
    COUNT(new DimensionlessQuantity("Count")),
    QFACTOR(new DimensionlessQuantity("QFactor"));

    private final Quantity quantity;

    AsylumImageChannelType(Quantity quantity)
    {
        this.quantity = quantity;
    }

    public static AsylumImageChannelType getChannelType(String name)
    {
        AsylumImageChannelType channelType = null;

        for(AsylumImageChannelType cht : AsylumImageChannelType.values())
        {
            boolean match = cht.getTypeString().equals(name);
            if(match)
            {
                channelType = cht;
                break;
            }                 
        }

        return channelType;
    }

    public String getTypeString()
    {
        return quantity.getName();
    }

    public PrefixedUnit getSIUnit()
    {
        return quantity.getUnit();
    }

    public Quantity getQuantity()
    {
        return quantity;
    }
}