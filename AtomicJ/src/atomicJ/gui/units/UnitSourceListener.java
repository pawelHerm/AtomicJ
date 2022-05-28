package atomicJ.gui.units;

import java.util.List;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.IdentityTag;

public interface UnitSourceListener
{
    public void unitGroupAdded(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units);
    public void unitGroupRemoved(IdentityTag group);
    public void unitSelected(IdentityTag group, PrefixedUnit unit);
    public void canBoundUnitsChanged(boolean unitsCanBeBoundOld, boolean unitsCanBeBoundNew);
    public void useDefaultUnitsChanged(boolean useDefaultUnitsOld, boolean useDefaultUnitsNew);
}