package atomicJ.readers.asylum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;

public class AsylumChannel
{
    private final String label;
    private static final String TRACE = "Trace";
    private static final String RETRACE = "Retrace";
    private static final Pattern pattern = Pattern.compile("(\\w+)(Trace|Retrace)(\\w*)");

    private final String channelType;
    private final String modificationTag;
    private final boolean isTrace;

    public AsylumChannel(String label)
    {
        this.label = label;
        Matcher matcher = pattern.matcher(label);

        if(matcher.find())
        {
            this.channelType = matcher.group(1).trim();                               
            this.isTrace = TRACE.equals(matcher.group(2));
            this.modificationTag = matcher.group(3).trim();
        }
        else
        {
            this.channelType = label;
            this.isTrace = true;
            this.modificationTag = "";
        }
    }

    public String getLabel()
    {
        return label;
    }

    public String getChannelType()
    {
        return channelType;
    }

    public Quantity getQuantity()
    {
        AsylumImageChannelType type = AsylumImageChannelType.getChannelType(channelType);
        Quantity quantity = (type != null) ? type.getQuantity() : new UnitQuantity(channelType, Units.VOLT_UNIT);

        return quantity;
    }

    public String getModificationTag()
    {
        return modificationTag;
    }

    public boolean isTrace()
    {
        return isTrace;
    }
}