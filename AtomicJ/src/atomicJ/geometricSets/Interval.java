package atomicJ.geometricSets;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import atomicJ.utilities.Validation;

public interface Interval extends RealSet
{
    public double getMinimumEnd();

    public double getMaximumEnd();

    @Override
    default boolean isAnyElementInfinite()
    {
        boolean infinite = Double.isInfinite(getMinimumEnd()) && Double.isInfinite(getMinimumEnd());        
        return infinite;
    }

    @Override
    default Set<Interval> getIntervalsOfSimpleSetRepresentation()
    {
        return Collections.singleton(this);
    }

    @Override
    default Set<DiscreteRealSet> getDiscreteSetsOfSimpleSetRepresentation()
    {
        return Collections.emptySet();
    }

    public Interval intersect(OpenInterval interval);
    public Interval intersect(ClosedInterval interval);
    public Interval intersect(LeftClosedInterval interval);
    public Interval intersect(RightClosedInterval interval);       

    default Interval intersect(SingleValueDoubleSet singleValueSet)
    {
        Validation.requireNonNullParameterName(singleValueSet, "interval");

        if(this.contains(singleValueSet.getValue()))
        {
            return singleValueSet;
        }

        return EmptyRealSet.getInstance();
    }

    @Override
    default DiscreteRealSet intersect(DiscreteRealSet other)
    {
        return other.intersect(this);
    }

    @Override
    default RealSet intersect(RealSet otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        if(otherSet instanceof Interval)
        {
            return intersect((Interval)otherSet);
        }

        if(otherSet instanceof DiscreteRealSet)
        {
            return intersect((DiscreteRealSet)otherSet);
        }

        Set<RealSet> setsWhoseUnionIsTheSoughtIntersection = new LinkedHashSet<>();

        Set<Interval> intervalsOfUnionRepresentation = otherSet.getIntervalsOfSimpleSetRepresentation();
        Set<DiscreteRealSet> discreteDoubleSetsOfUnionRepresentation = otherSet.getDiscreteSetsOfSimpleSetRepresentation();

        for(Interval interval : intervalsOfUnionRepresentation)
        {
            Interval intersection = intersect(interval);
            if(!intersection.isEmpty())
            {
                setsWhoseUnionIsTheSoughtIntersection.add(intersection);
            }
        }

        DiscreteRealSet unionOfIntersectionsOfThisIntervalAndDiscreteSets = EmptyRealSet.getInstance();
        for(DiscreteRealSet set : discreteDoubleSetsOfUnionRepresentation)
        {
            DiscreteRealSet intersection = intersect(set);
            unionOfIntersectionsOfThisIntervalAndDiscreteSets = unionOfIntersectionsOfThisIntervalAndDiscreteSets.createUnion(intersection);
        }
        if(!unionOfIntersectionsOfThisIntervalAndDiscreteSets.isEmpty())
        {
            setsWhoseUnionIsTheSoughtIntersection.add(unionOfIntersectionsOfThisIntervalAndDiscreteSets);
        }

        RealSet unionOfIntersections = RealSetUnion.getUnion(setsWhoseUnionIsTheSoughtIntersection);

        return unionOfIntersections;
    }


    default Interval intersect(Interval interval)
    {
        Validation.requireNonNullParameterName(interval, "interval");

        if(interval instanceof OpenInterval)
        {
            return intersect((SingleValueDoubleSet)interval);
        }
        if(interval instanceof ClosedInterval)
        {
            return intersect((ClosedInterval)interval);
        }
        if(interval instanceof LeftClosedInterval)
        {
            return intersect((LeftClosedInterval)interval);
        }
        if(interval instanceof RightClosedInterval)
        {
            return intersect((RightClosedInterval)interval);
        }
        if(interval instanceof SingleValueDoubleSet)
        {
            return intersect((SingleValueDoubleSet)interval);
        }
        if(interval instanceof EmptyRealSet)
        {
            return EmptyRealSet.getInstance();
        }

        throw new IllegalArgumentException("Unknown type of interval encountered");
    }
}
