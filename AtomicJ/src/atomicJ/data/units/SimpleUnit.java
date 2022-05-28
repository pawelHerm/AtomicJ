package atomicJ.data.units;

import java.util.Collection;
import java.util.List;

public interface SimpleUnit extends PrefixedUnit
{
    //should return list with a single element, this object
    @Override
    public List<SimpleUnit> getSimpleUnits();
    //should return this
    @Override
    public SimpleUnit simplify();
    @Override
    public SimpleUnit power(int exp);
    public int getExponent();
    public String getBareName();
    public SIPrefix getPrefix();
    public SimplePrefixedUnit deriveUnit(SIPrefix prefixNew);

    @Override
    public SimpleUnit getNext();
    @Override
    public SimpleUnit getPrevious();

    public static SimpleUnit getSmallestPrefixUnit(List<SimpleUnit> units)
    {
        if(units.isEmpty())
        {
            return null;
        }

        SIPrefix smallestPrefixExp = SIPrefix.getGratestPrefix();
        SimpleUnit smallestPrefixUnit = null;

        for(SimpleUnit unit : units)
        {
            SIPrefix currentPrefix = unit.getPrefix();

            if(currentPrefix.compareTo(smallestPrefixExp) <= 0)
            {
                smallestPrefixExp = currentPrefix;
                smallestPrefixUnit = unit;
            }
        }

        return smallestPrefixUnit;
    }

    public static SimpleUnit getGreatestPrefixUnit(List<SimpleUnit> units)
    {
        if(units.isEmpty())
        {
            return null;
        }

        SIPrefix greatestPrefixExp = SIPrefix.getSmallestPrefix();
        SimpleUnit greatestPrefixUnit = null;

        for(SimpleUnit unit : units)
        {
            SIPrefix currentPrefix = unit.getPrefix();

            if(currentPrefix.compareTo(greatestPrefixExp) >= 0)
            {
                greatestPrefixExp = currentPrefix;
                greatestPrefixUnit = unit;
            }
        }

        return greatestPrefixUnit;
    }

    public static int getTotalPrefixExponent(Collection<? extends SimpleUnit> units)
    {
        int totalPrefixExponent = 0;

        for(SimpleUnit unit : units)
        {
            totalPrefixExponent += unit.getTotalPrefixExponent();
        }

        return totalPrefixExponent;
    }

    public static double getTotalPremultiplierExponent(Collection<? extends SimpleUnit> units)
    {
        int totalPremultiplierExponent = 0;

        for(SimpleUnit unit : units)
        {
            totalPremultiplierExponent += unit.getTotalPremultiplierExponent();
        }

        return totalPremultiplierExponent;
    }

    public static int getTotalUnitExponent(Collection<SimpleUnit> units)
    {
        int totalUnitExponent = 0;

        for(SimpleUnit unit : units)
        {
            totalUnitExponent += unit.getExponent();
        }

        return totalUnitExponent;
    }
}
