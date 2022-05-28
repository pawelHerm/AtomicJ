package atomicJ.gui.units;

import java.util.List;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.IdentityTag;

public class UnitSourceAdapter implements UnitSourceListener
{

    @Override
    public void unitGroupAdded(IdentityTag group, PrefixedUnit selectedUnit,
            List<PrefixedUnit> units) {           
    }

    @Override
    public void unitGroupRemoved(IdentityTag group) {            
    }

    @Override
    public void unitSelected(IdentityTag group, PrefixedUnit unit) {            
    }

    @Override
    public void canBoundUnitsChanged(boolean unitsCanBeBoundOld,
            boolean unitsCanBeBoundNew) {            
    }

    @Override
    public void useDefaultUnitsChanged(boolean useDefaultUnitsOld,
            boolean useDefaultUnitsNew) {        
    }

}