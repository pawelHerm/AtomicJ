package atomicJ.data.units;

import java.util.Comparator;

public class TotalPremultiplierExponentComparator implements Comparator<PrefixedUnit>
{
    @Override
    public int compare(PrefixedUnit firstUnit, PrefixedUnit secondUnit) 
    {
        return Double.compare(firstUnit.getTotalPremultiplierExponent(), secondUnit.getTotalPremultiplierExponent());
    }       
}