package atomicJ.readers.nanoscope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum NanoscopeSpectroscopyDataType
{
    DEFLECTION(Pattern.compile("Defl.*"),false, true),       
    TM_DEFLECTION(Pattern.compile("TM [Dd]eflection"), false, true),
    HEIGHT_SENSOR(Pattern.compile("[Hh]eight [Ss]ensor"),true,false),                 
    UNKNOWN(Pattern.compile(".*"), false, false);

    private final Pattern namePattern;
    private final boolean isForceCurveX;
    private final boolean isForceCurveY;

    NanoscopeSpectroscopyDataType(Pattern namePattern, boolean isForceCurveX, boolean isForceCurveY)
    {
        this.namePattern = namePattern;
        this.isForceCurveX = isForceCurveX;
        this.isForceCurveY = isForceCurveY;
    }

    public boolean isForceCurveX()
    {
        return isForceCurveX;
    }

    public boolean isForceCurveY()
    {
        return isForceCurveY;
    }

    public static NanoscopeSpectroscopyDataType getDataType(String name)
    {                        
        for(NanoscopeSpectroscopyDataType type : NanoscopeSpectroscopyDataType.values())
        {
            Matcher matcher = type.namePattern.matcher(name);
            if (matcher.matches())
            {
                return type;
            }
        }

        throw new IllegalArgumentException("Could not found SpectroscopyDataType for " + name);
    }
}