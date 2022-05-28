package atomicJ.gui.imageProcessing;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;

public class UnitManager 
{
    private final PrefixedUnit dataUnit;
    private final PrefixedUnit displayedUnit;

    public UnitManager(PrefixedUnit dataUnit, PrefixedUnit displayedUnit)
    {
        boolean singleDataUnitKnow = (dataUnit != null);

        this.dataUnit =  singleDataUnitKnow ? dataUnit : SimplePrefixedUnit.getNullInstance();
        this.displayedUnit = singleDataUnitKnow ? displayedUnit : SimplePrefixedUnit.getNullInstance();
    }

    public PrefixedUnit getSingleDataUnit()
    {
        return dataUnit;
    }

    public PrefixedUnit getSingleDisplayedUnit()
    {
        return displayedUnit;
    }
}
