package atomicJ.gui;

import java.text.NumberFormat;

import org.jfree.chart.axis.NumberTickUnit;

public class PrefixedTickUnit extends NumberTickUnit 
{
    private static final long serialVersionUID = 1L;

    private final double prefix;

    public PrefixedTickUnit(double size, NumberFormat format, int minorTickCount, double prefix)
    {
        super(size, format, minorTickCount);
        this.prefix = prefix;
    }

    @Override
    public String valueToString(double value)
    {
        return super.valueToString(prefix*value);
    }
}