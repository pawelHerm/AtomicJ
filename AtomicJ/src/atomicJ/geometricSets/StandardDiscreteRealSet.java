package atomicJ.geometricSets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.utilities.Validation;

public class StandardDiscreteRealSet implements DiscreteRealSet
{
    private final Set<Double> elements;

    //the set of double values should not contain nulls
    //although we do not check this
    private StandardDiscreteRealSet(Set<Double> values)
    {
        this.elements = values;
    }

    @Override
    public boolean isAnyElementInfinite()
    {
        boolean infinite = false;

        for(Double el : elements)
        {
            infinite = infinite || Double.isInfinite(el.doubleValue());
            if(infinite)
            {
                break;
            }
        }

        return infinite;
    }

    @Override
    public double getGreatestContainedDoubleValue() 
    {
        Iterator<Double> it = elements.iterator();

        if(!it.hasNext())
        {
            return Double.NaN;
        }

        double maxElement = it.next();

        while(it.hasNext())
        {
            double currentElement = it.next().doubleValue();
            if(currentElement > maxElement)
            {
                maxElement = currentElement;
            }
        }

        return maxElement;
    }

    @Override
    public double getSmallestContainedDoubleValue() 
    {
        Iterator<Double> it = elements.iterator();

        if(!it.hasNext())
        {
            return Double.NaN;
        }

        double minElement = it.next();

        while(it.hasNext())
        {
            double currentElement = it.next().doubleValue();
            if(currentElement < minElement)
            {
                minElement = currentElement;
            }
        }

        return minElement;  
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
            return getGreatestContainedDoubleValue();
        }

        if(Double.compare(val, Double.NEGATIVE_INFINITY) == 0)
        {
            return getSmallestContainedDoubleValue();
        }

        double minDistance = Double.POSITIVE_INFINITY;
        double closestVal = Double.NaN;

        for(Double v : elements)
        {
            double currentDistance = Math.abs(val - v.doubleValue());
            if(currentDistance < minDistance)
            {
                minDistance = currentDistance;
                closestVal = v;
            }
        }

        return closestVal;
    }

    public double getClosestContainedDoubleValueNotSmallerThan(double val)
    {       
        if(contains(val))
        {
            return val;
        }         

        //there is no use in considering the case of val equal Double.POSITIVE_INFINITY
        //since we already know that our set does not contain it so the set
        //cannot contain any value that is not smaller than positive infinity
        if(Double.compare(val, Double.NEGATIVE_INFINITY) == 0)
        {
            return getSmallestContainedDoubleValue();
        }

        double minDistance = Double.POSITIVE_INFINITY;
        double closestVal = Double.NaN;

        for(double v : elements)
        {
            if(v < val)
            {
                continue;         
            }

            double currentDistance = val - v;//no need to take absolute value, because we already know that val is equal or greater than v
            if(currentDistance < minDistance)
            {
                minDistance = currentDistance;
                closestVal = v;
            }
        }

        return closestVal;
    }

    @Override
    public Set<Double> getIsolatedPoints()
    {
        return new LinkedHashSet<>(elements);
    }

    public static StandardDiscreteRealSet getInstance(Collection<? extends Double> values)
    {
        Validation.requireNonNullParameterName(values, "values");

        StandardDiscreteRealSet instance = new StandardDiscreteRealSet(new LinkedHashSet<>(values));
        return instance;
    }

    public StandardDiscreteRealSet intersect(StandardDiscreteRealSet otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        Set<Double> retained = new LinkedHashSet<>(this.elements);
        retained.retainAll(otherSet.elements);

        StandardDiscreteRealSet intersection = new StandardDiscreteRealSet(retained);
        return intersection;
    }

    @Override
    public DiscreteRealSet intersect(DiscreteRealSet otherSet) 
    {        
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        Set<Double> elementsInIntersection = new LinkedHashSet<>();

        for(Double el : elements)
        {
            if(otherSet.contains(el.doubleValue()))
            {
                elementsInIntersection.add(el);
            }
        }

        DiscreteRealSet difference = new StandardDiscreteRealSet(elementsInIntersection);

        return difference;
    }

    @Override
    public DiscreteRealSet intersect(RealSet otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        Set<Double> elementsInIntersection = new LinkedHashSet<>();

        for(Double el : elements)
        {
            if(otherSet.contains(el.doubleValue()))
            {
                elementsInIntersection.add(el);
            }
        }

        DiscreteRealSet difference = new StandardDiscreteRealSet(elementsInIntersection);

        return difference;
    }


    public StandardDiscreteRealSet createUnion(StandardDiscreteRealSet otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        Set<Double> union = new LinkedHashSet<>(this.elements);
        union.addAll(otherSet.elements);

        StandardDiscreteRealSet intersection = new StandardDiscreteRealSet(union);
        return intersection;
    }


    @Override
    public DiscreteRealSet createUnion(DiscreteRealSet otherSet)
    {
        Validation.requireNonNullParameterName(otherSet, "otherSet");

        Set<Double> elementsOfUnion = new LinkedHashSet<>(otherSet.getIsolatedPoints());
        elementsOfUnion.addAll(this.elements);

        DiscreteRealSet union = new StandardDiscreteRealSet(elementsOfUnion);
        return union;
    }

    public RealSet createUnion(RealSet otherSet)
    {
        List<RealSet> setsForUnion = Arrays.asList(this, otherSet);

        RealSet union = RealSetUnion.getUnion(setsForUnion);
        return union;
    }

    @Override
    public boolean contains(double val) 
    {
        boolean contained = elements.contains(Double.valueOf(val));
        return contained;
    }

    @Override
    public DiscreteRealSet subtract(RealSet other) 
    {
        Set<Double> elementsAfterSubtraction = new LinkedHashSet<>();

        for(Double el : elements)
        {
            if(!other.contains(el.doubleValue()))
            {
                elementsAfterSubtraction.add(el);
            }
        }

        DiscreteRealSet difference = new StandardDiscreteRealSet(elementsAfterSubtraction);

        return difference;
    }

    @Override
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }
}
