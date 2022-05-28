package atomicJ.gui.results;

import java.util.List;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.IdentityTag;

public class DataType
{
    private final IdentityTag identityTag;
    private final PrefixedUnit dataUnit;
    private final List<PrefixedUnit> possibleDisplayUnits;

    public DataType(String name, PrefixedUnit dataUnit)
    {
        this(name, name, dataUnit, -12, 12);
    }

    public DataType(Object key, String name, PrefixedUnit dataUnit, int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange)
    {
        this.identityTag = new IdentityTag(key, name);
        this.dataUnit = dataUnit;
        this.possibleDisplayUnits = dataUnit.deriveUnits(minTotalPrefixExponentChange, maxTotalPrefixExponentChange);
    }

    public String getLabel(PrefixedUnit unit)
    {
        String name = (unit != null) ? identityTag.getLabel() + " (" + unit.getFullName() + ")" : identityTag.getLabel();
        return name;
    }

    public List<PrefixedUnit> getPossibleDisplayUnit()
    {
        return possibleDisplayUnits;
    }

    public String getName()
    {
        return identityTag.getLabel();
    }

    public String getFullLabel()
    {
        String name = (dataUnit != null) ? identityTag.getLabel() + " (" + dataUnit.getFullName() + ")" : identityTag.getLabel();
        return name;
    }

    public boolean requiresUnit()
    {
        boolean requiresUnit = (dataUnit == null);
        return requiresUnit;
    }

    public IdentityTag getIdentityTag()
    {
        return identityTag;
    }

    public PrefixedUnit getDataUnit()
    {
        return dataUnit;
    }
}
