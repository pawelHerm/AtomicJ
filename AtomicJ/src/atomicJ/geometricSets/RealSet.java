package atomicJ.geometricSets;

import java.util.LinkedHashSet;
import java.util.Set;

import atomicJ.utilities.Validation;

public interface RealSet
{
    public boolean contains(double val);
    //should return true if there are any real numbers that belong to the set, even if those numbers cannot be represented as doudle precision floating point numbers
    //e.g. open interval (val, Math.nextUp(val)) is not empty
    //so it returns false even if there are no double precision floating numbers conteined in it
    public boolean isEmpty();
    public Set<Double> getIsolatedPoints();

    //the double set should be equal in the set-theoretic sense to the union of all individual sets (i.e. sets within sets) returned
    //by getIntervalsOfSimpleSetRepresentation() and getDiscreteSetsOfSimpleSetRepresentation()
    //Note that this set representation may not be unique
    public Set<Interval> getIntervalsOfSimpleSetRepresentation();
    public Set<DiscreteRealSet> getDiscreteSetsOfSimpleSetRepresentation();

    //returns Double.NaN if the set is empty
    public double getGreatestContainedDoubleValue();
    //returns Double.NaN if the set is empty
    public double getSmallestContainedDoubleValue();

    //returns Double.NaN if the set is empty
    //returns getGreatestContainedFloatingPointValue() if val is equal to Double.POSITIVE_INFINITY
    //return getSmallestContainedFloatingPointValue() if val is equal to Double.NEGATIVE_INIFINITY
    public double getClosestContainedDoubleValue(double val);

    default public DiscreteRealSet intersect(DiscreteRealSet  otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        Set<Double> elementsInIntersection = new LinkedHashSet<>();
        Set<Double> elementsInOtherSet = otherSet.getIsolatedPoints();

        for(Double el : elementsInOtherSet)
        {
            if(contains(el.doubleValue()))
            {
                elementsInIntersection.add(el);
            }
        }

        DiscreteRealSet difference = StandardDiscreteRealSet.getInstance(elementsInIntersection);

        return difference;
    }

    public RealSet intersect(RealSet otherSet);
    public boolean isNumberOfElementsFinite();
    //returns true if any element contained in the set is Double.POSITIVE_INFINITY or Double.NEGATIVE_INFINITY
    public boolean isAnyElementInfinite();
}
