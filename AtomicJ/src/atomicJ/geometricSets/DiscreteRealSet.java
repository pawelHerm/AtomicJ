package atomicJ.geometricSets;

import java.util.Collections;
import java.util.Set;

public interface DiscreteRealSet extends RealSet
{
    @Override
    public DiscreteRealSet intersect(RealSet otherSet);
    public DiscreteRealSet createUnion(DiscreteRealSet interval);
    public DiscreteRealSet subtract(RealSet other);

    @Override
    default Set<DiscreteRealSet> getDiscreteSetsOfSimpleSetRepresentation()
    {
        return Collections.singleton(this);
    }

    @Override
    default Set<Interval> getIntervalsOfSimpleSetRepresentation()
    {
        return Collections.emptySet();
    }

    @Override
    default boolean isNumberOfElementsFinite()
    {        
        return true;
    }
}
