package atomicJ.gui.units;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MultiMap;

public class StandardUnitSource 
{
    private Map<IdentityTag, PrefixedUnit> selectedUnits = new LinkedHashMap<>();
    private final Map<IdentityTag, PrefixedUnit> defaultUnits = new LinkedHashMap<>();
    private MultiMap<IdentityTag, PrefixedUnit> groups = new MultiMap<>();

    private final List<UnitSourceListener> listeners = new ArrayList<>();

    private boolean useDefaultUnits;
    private boolean unitsCanBeBound;

    public StandardUnitSource(MultiMap<IdentityTag, PrefixedUnit> unitGroups, Map<IdentityTag, PrefixedUnit> selectedUnits)
    {
        this.groups = new MultiMap<>(unitGroups);
        this.selectedUnits = new LinkedHashMap<>(selectedUnits);
        this.unitsCanBeBound = calculateCanUnitsBeBound();
    }

    public boolean isUseDefaultUnits()
    {
        return useDefaultUnits;
    }

    public void setUseDefaultUnits(boolean useDefaultUnitsNew)
    {
        boolean useDefaultUnitsOld = this.useDefaultUnits;
        this.useDefaultUnits = useDefaultUnitsNew;

        if(useDefaultUnitsOld != useDefaultUnitsNew)
        {
            fireUseDefaultUnitsChanged(useDefaultUnitsOld, useDefaultUnitsNew);
        }

        ensureUnitsConsistantWithDefaults();
    }

    private void ensureUnitsConsistantWithDefaults()
    {
        if(useDefaultUnits)
        {
            for(Entry<IdentityTag, PrefixedUnit> entry : defaultUnits.entrySet())
            {
                forceSelectedUnit(entry.getKey(), entry.getValue());
            }
        }       
    }

    public void setDefaultUnit(IdentityTag group, PrefixedUnit unitNew)
    {
        this.defaultUnits.put(group, unitNew);

        if(useDefaultUnits)
        {
            forceSelectedUnit(group, unitNew);
        }
    }

    public boolean isUnitsCanBeBound()
    {
        return unitsCanBeBound;
    }

    private boolean calculateCanUnitsBeBound()
    {
        if(groups.isEmpty())
        {
            return false;
        }

        boolean canBeBound = true;
        Iterator<List<PrefixedUnit>> it = groups.values().iterator();
        List<PrefixedUnit> firstUnits = it.next();

        while(it.hasNext())
        {
            canBeBound = canBeBound && firstUnits.equals(it.next());
            if(!canBeBound)
            {
                break;
            }
        }

        return canBeBound;
    }

    public Map<IdentityTag, PrefixedUnit> getSelectedUnits()
    {
        return new LinkedHashMap<>(selectedUnits);
    }

    public PrefixedUnit getSelectedUnit(IdentityTag group)
    {
        return selectedUnits.get(group);
    }

    public void setSelectedUnit(IdentityTag group, PrefixedUnit unitNew)
    {
        if(!useDefaultUnits)
        {
            forceSelectedUnit(group, unitNew);
        }
    }

    private void forceSelectedUnit(IdentityTag group, PrefixedUnit unitNew)
    {
        if(!groups.get(group).contains(unitNew))
        {
            return;
        }

        PrefixedUnit unitOld = this.selectedUnits.get(group);

        if(!Objects.equals(unitOld, unitNew))
        {            
            this.selectedUnits.put(group, unitNew);
            fireUnitSelected(group, unitNew);
        }
    }


    public MultiMap<IdentityTag, PrefixedUnit> getAllProposedUnits()
    {
        return new MultiMap<>(groups);
    }

    public List<PrefixedUnit> getProposedUnits(IdentityTag group)
    {
        return groups.get(group);
    }


    public void removeUnitGroup(IdentityTag group)
    {
        if(groups.containsKey(group))
        {
            this.selectedUnits.remove(group);
            this.groups.clear(group);
            fireUnitGroupRemoved(group);
        }
    }

    public void addUnitGroup(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units)
    {
        if(!units.contains(selectedUnit))
        {
            throw new IllegalArgumentException("The list 'units' does not contain the 'selectedUnit'");
        }

        if(!groups.containsKey(group))
        {
            this.groups.putAll(group, units);
            this.selectedUnits.put(group, selectedUnit);

            boolean unitsCanBeBoundOld = this.unitsCanBeBound;
            this.unitsCanBeBound = calculateCanUnitsBeBound();

            fireUnitGroupAdded(group, selectedUnit, units);
            fireUnitSelected(group, selectedUnit);

            if(unitsCanBeBoundOld != this.unitsCanBeBound)
            {
                fireUnitsCanBeBoundChanged(unitsCanBeBoundOld, this.unitsCanBeBound);
            }
        }
    }

    public void addUnitSourceListener(UnitSourceListener listener)
    {
        this.listeners.add(listener);
    }

    public void removeUnitSourceListener(UnitSourceListener listener)
    {
        this.listeners.remove(listener);
    }

    protected void fireUnitGroupAdded(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units)
    {
        for(UnitSourceListener listener : listeners)
        {
            listener.unitGroupAdded(group, selectedUnit, units);
        }
    }

    protected void fireUnitGroupRemoved(IdentityTag group)
    {
        for(UnitSourceListener listener : listeners)
        {
            listener.unitGroupRemoved(group);
        }
    }

    protected void fireUnitSelected(IdentityTag group, PrefixedUnit unit)
    {
        for(UnitSourceListener listener : listeners)
        {
            listener.unitSelected(group, unit);
        }
    }

    protected void fireUnitsCanBeBoundChanged(boolean unitsCanBeBoundOld, boolean unitsCanBeBoundNew)
    {
        for(UnitSourceListener listener : listeners)
        {
            listener.canBoundUnitsChanged(unitsCanBeBoundOld, unitsCanBeBoundNew);
        }
    }

    protected void fireUseDefaultUnitsChanged(boolean useDefaultUnitsOld, boolean useDefaultUnitsNew)
    {
        for(UnitSourceListener listener : listeners)
        {
            listener.useDefaultUnitsChanged(useDefaultUnitsOld, useDefaultUnitsNew);
        }
    }

}
