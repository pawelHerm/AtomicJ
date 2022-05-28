package atomicJ.gui;

import java.util.List;

import org.jfree.chart.axis.NumberTickUnit;

public class NamedTickUnit extends NumberTickUnit 
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private final List<String> categories;

    public NamedTickUnit(List<String> categories)
    {
        super(1);        
        this.categories = categories;
    }

    @Override
    public String valueToString(double value)
    {
        String label = "";

        int intValue = (int)Math.rint(value);
        int index = intValue - 1;

        if(index >= 0 && index < categories.size() && Math.abs(value - intValue)<TOLERANCE)
        {
            label = categories.get(index);
        }      

        return label;
    }
}