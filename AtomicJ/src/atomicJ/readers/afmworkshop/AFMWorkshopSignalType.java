package atomicJ.readers.afmworkshop;

import java.util.regex.Pattern;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;

public enum AFMWorkshopSignalType
{
    Z_SENSOR("Z_SENSE", Pattern.compile("Z[-_]SENSE",  Pattern.CASE_INSENSITIVE), Units.MILI_VOLT_UNIT), Z_ERROR("Z_ERR", Pattern.compile("Z[-_]ERR",  Pattern.CASE_INSENSITIVE), Units.MILI_VOLT_UNIT), 
    LEFT_MINUS_RIGHT("L-R",Units.MILI_VOLT_UNIT), TOP_MINUS_BOTTOM("T-B", Units.MILI_VOLT_UNIT), TOP_PLUS_BOTTOM("T+B", Units.MILI_VOLT_UNIT),
    Z_PHASE("Z_PHASE", Pattern.compile("Z[-_]PHASE",  Pattern.CASE_INSENSITIVE),Units.MILI_VOLT_UNIT), 
    Z_AMPLITUDE("Z_AMPL",Pattern.compile("Z[-_]AMPL",  Pattern.CASE_INSENSITIVE), Units.MILI_VOLT_UNIT),
    Z_DRIVE("Z_DRIVE", Pattern.compile("Z[-_]DRIVE",  Pattern.CASE_INSENSITIVE), Units.NANO_METER_UNIT), AUX_ADC_1("Aux ADC 1",Units.MILI_VOLT_UNIT),
    AUX_ADC_2("Aux ADC 2",Units.MILI_VOLT_UNIT); 

    private final String prettyName;
    private final Pattern matchedPattern;
    private final PrefixedUnit defaultUnit;

    AFMWorkshopSignalType(String name, PrefixedUnit defaultUnit)
    {
        this(name, Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE), defaultUnit);
    }

    AFMWorkshopSignalType(String prettyName, Pattern matchedPattern, PrefixedUnit defaultUnit)
    {
        this.prettyName = prettyName;
        this.matchedPattern = matchedPattern;
        this.defaultUnit = defaultUnit;
    }

    public PrefixedUnit getDefaultUnit()
    {
        return defaultUnit;
    }

    public String getName()
    {
        return prettyName;
    }

    public static AFMWorkshopSignalType getAFMWorkshopChannelType(String name)
    {
        for(AFMWorkshopSignalType channelType : AFMWorkshopSignalType.values())
        {
            if(channelType.matchedPattern.matcher(name).matches())
            {
                return channelType;
            }
        }

        throw new IllegalArgumentException("No AFMWorkshopSignalType is named " + name);
    }
}