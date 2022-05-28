package atomicJ.geometricSets;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

import atomicJ.utilities.Validation;

public class RealSetUnion implements RealSet
{
    private final Set<RealSet> individualSets;

    //the set of individualSets should not contain any nulls
    private RealSetUnion(Collection<? extends RealSet> individualSets)
    {
        this.individualSets = new LinkedHashSet<>(individualSets);
    }

    @Override
    public boolean isAnyElementInfinite()
    {
        boolean infinite = false;

        for(RealSet el : individualSets)
        {
            infinite = infinite || el.isAnyElementInfinite();
            if(infinite)
            {
                break;
            }
        }

        return infinite;
    }


    public static RealSet getUnion(Collection<? extends RealSet> individualSets)
    {
        Validation.requireNonNullParameterName(individualSets, "individualSets");

        if(individualSets.isEmpty())
        {
            return EmptyRealSet.getInstance();
        }

        if(individualSets.size() == 1)
        {
            return individualSets.iterator().next();
        }

        RealSet union = new RealSetUnion(individualSets);
        return union;
    }

    @Override
    public Set<Double> getIsolatedPoints()
    {
        if(individualSets.isEmpty())
        {
            return Collections.emptySet();
        }

        Set<Double> isolatedPoints = new LinkedHashSet<>();

        for(RealSet doubleSet : individualSets)
        {
            isolatedPoints.addAll(doubleSet.getIsolatedPoints());
        }

        return isolatedPoints;
    }  

    @Override
    public double getGreatestContainedDoubleValue()
    {
        if(individualSets.isEmpty())
        {
            return Double.NaN;
        }

        Iterator<RealSet> it = individualSets.iterator();

        double greatestContainedFloatingPointValue = Double.NaN;

        //we find the first set with greatestContainedFloatingPointValue that is not NaN
        while(it.hasNext() && Double.isNaN(greatestContainedFloatingPointValue))
        {
            greatestContainedFloatingPointValue = it.next().getGreatestContainedDoubleValue();
        }

        while(it.hasNext())
        {
            RealSet set = it.next();
            double currentGreatestValue = set.getGreatestContainedDoubleValue();

            if(currentGreatestValue > greatestContainedFloatingPointValue)
            {
                greatestContainedFloatingPointValue = currentGreatestValue;
            }
        }

        return greatestContainedFloatingPointValue;
    }

    @Override
    public double getSmallestContainedDoubleValue()
    {
        if(individualSets.isEmpty())
        {
            return Double.NaN;
        }

        Iterator<RealSet> it = individualSets.iterator();

        double smallestContainedFloatingPointValue = Double.NaN;

        //we find the first set with greatestContainedFloatingPointValue that is not NaN
        while(it.hasNext() && Double.isNaN(smallestContainedFloatingPointValue))
        {
            smallestContainedFloatingPointValue = it.next().getSmallestContainedDoubleValue();
        }

        while(it.hasNext())
        {
            RealSet set = it.next();
            double currentSmallestValue = set.getSmallestContainedDoubleValue();

            if(currentSmallestValue < smallestContainedFloatingPointValue)
            {
                smallestContainedFloatingPointValue = currentSmallestValue;
            }
        }

        return smallestContainedFloatingPointValue;
    }

    @Override
    public RealSet intersect(RealSet otherSet)
    {
        Set<RealSet> setsWhoseUnionIsTheSoughtIntersection = new LinkedHashSet<>();

        for(RealSet set : this.individualSets)
        {
            RealSet intersection = set.intersect(otherSet);
            if(!intersection.isEmpty())
            {
                setsWhoseUnionIsTheSoughtIntersection.add(intersection);
            }
        }

        RealSet unionOfIntersections = RealSetUnion.getUnion(setsWhoseUnionIsTheSoughtIntersection);

        return unionOfIntersections;
    }

    @Override
    public double getClosestContainedDoubleValue(double val)
    {
        if(contains(val))
        {
            return val;
        }

        if(Double.compare(val, Double.POSITIVE_INFINITY) == 0)
        {
            getGreatestContainedDoubleValue();
        }

        if(Double.compare(val, Double.NEGATIVE_INFINITY) == 0)
        {
            return getSmallestContainedDoubleValue();
        }

        double closest = Double.NaN;

        Iterator<RealSet> it = individualSets.iterator();

        //we find the first set with greatestContainedFloatingPointValue that is not NaN
        while(it.hasNext() && Double.isNaN(closest))
        {
            closest = it.next().getClosestContainedDoubleValue(val);
        }

        if(Double.isNaN(closest))
        {
            return closest;
        }

        double minDistance = Math.abs(val - closest);

        while(it.hasNext())
        {
            RealSet set = it.next();
            double currentClosest = set.getClosestContainedDoubleValue(val);
            double currentDistance = Math.abs(val - currentClosest);

            if(currentDistance < minDistance)
            {
                closest = currentClosest;
                minDistance = currentDistance;
            }
        }

        return closest;
    }

    @Override
    public boolean contains(double val)
    {
        boolean contained = false;

        for(RealSet set : individualSets)
        {
            if(set.contains(val))
            {
                contained = true;
                break;
            }
        }

        return contained;
    }

    @Override
    public boolean isEmpty() 
    {
        boolean empty = true;

        for(RealSet subset : individualSets)
        {
            boolean subsetEmpty = subset.isEmpty();
            empty = empty && subsetEmpty;
            if(!empty)
            {
                break;
            }
        }

        return empty;
    }

    @Override
    public boolean isNumberOfElementsFinite()
    {
        boolean finite = true;

        for(RealSet subset : individualSets)
        {
            boolean subsetFinite = subset.isNumberOfElementsFinite();
            finite = finite && subsetFinite;
            if(!finite)
            {
                break;
            }
        }
        return finite;
    }


    @Override
    public Set<Interval> getIntervalsOfSimpleSetRepresentation()
    {
        Set<Interval> intervals = new LinkedHashSet<>();
        for(RealSet set : individualSets)
        {
            intervals.addAll(set.getIntervalsOfSimpleSetRepresentation());
        }
        return intervals;
    }

    @Override
    public Set<DiscreteRealSet> getDiscreteSetsOfSimpleSetRepresentation()
    {
        Set<DiscreteRealSet> discreteSets = new LinkedHashSet<>();
        for(RealSet set : individualSets)
        {
            discreteSets.addAll(set.getDiscreteSetsOfSimpleSetRepresentation());
        }
        return discreteSets;
    }

    @Override
    public int hashCode()
    {
        int hashCode = individualSets.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof RealSetUnion))
        {
            return false;
        }

        RealSetUnion that = (RealSetUnion)other;

        if(!Objects.equal(this.individualSets, that.individualSets))
        {
            return false;
        }

        return true;
    }
}
