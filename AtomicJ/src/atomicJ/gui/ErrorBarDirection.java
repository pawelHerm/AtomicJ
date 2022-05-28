package atomicJ.gui;

import java.util.Objects;

public enum ErrorBarDirection 
{
    BOTH_SIDES("Both sides", true, true), POSITIVE_SIDE_ONLY("Positive side only", false, true),
    NEGATIVE_SIDE_ONLY("Negative side only", true, false);

    private final String prettyName;
    private final boolean negativeSideDrawn;
    private final boolean positiveSideDrawn;

    ErrorBarDirection(String prettyName, boolean negativeSideDrawn, boolean positiveSideDrawn)
    {
        this.prettyName = prettyName;
        this.negativeSideDrawn = negativeSideDrawn;
        this.positiveSideDrawn = positiveSideDrawn;
    }

    public boolean isNegativeSideDrawn()
    {
        return negativeSideDrawn;
    }

    public boolean isPositiveSideDrawn()
    {
        return positiveSideDrawn;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public static ErrorBarDirection getInstance(String name, ErrorBarDirection defaultValue)
    {
        for(ErrorBarDirection approach : ErrorBarDirection.values())
        {
            if(Objects.equals(name, approach.name()))
            {
                return approach;
            }
        }

        return defaultValue;
    }
}
