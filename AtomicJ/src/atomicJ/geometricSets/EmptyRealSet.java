package atomicJ.geometricSets;

import java.util.Collections;
import java.util.Set;

import atomicJ.utilities.Validation;

public class EmptyRealSet implements Interval, DiscreteRealSet
{
    private static final EmptyRealSet INSTANCE = new EmptyRealSet();

    private EmptyRealSet(){};

    public static EmptyRealSet getInstance()
    {
        return INSTANCE;
    }

    @Override
    public double getMinimumEnd()
    {
        return Double.NaN;
    }

    @Override
    public double getMaximumEnd() 
    {
        return Double.NaN;
    }


    @Override
    public double getGreatestContainedDoubleValue()
    {
        return Double.NaN;
    }

    @Override
    public double getSmallestContainedDoubleValue()
    {
        return Double.NaN;
    }

    @Override
    public double getClosestContainedDoubleValue(double val)
    {
        return Double.NaN;
    }

    public double getClosestContainedDoubleValueNotSmallerThan(double val)
    {
        return Double.NaN;
    }

    @Override
    public Set<Double> getIsolatedPoints()
    {
        return Collections.emptySet();
    }   

    @Override
    public EmptyRealSet intersect(RealSet interval)
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }  

    @Override
    public EmptyRealSet intersect(Interval interval)
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }


    @Override
    public Interval intersect(OpenInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }

    @Override
    public Interval intersect(ClosedInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }

    @Override
    public Interval intersect(LeftClosedInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }

    @Override
    public Interval intersect(RightClosedInterval interval)
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }

    @Override
    public DiscreteRealSet intersect(DiscreteRealSet interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        return INSTANCE;
    }


    public RealSet createUnion(RealSet otherSet)
    {
        return otherSet;
    }


    @Override
    public DiscreteRealSet subtract(RealSet other) 
    {
        return INSTANCE;
    }

    @Override
    public boolean contains(double val)
    {
        return false;
    }

    @Override
    public Set<Interval> getIntervalsOfSimpleSetRepresentation()
    {
        return Collections.singleton(INSTANCE);
    }

    @Override
    public Set<DiscreteRealSet> getDiscreteSetsOfSimpleSetRepresentation() 
    {
        return Collections.singleton(INSTANCE);
    }

    @Override
    public boolean isEmpty() 
    {
        return true;
    }

    @Override
    public boolean isNumberOfElementsFinite()
    {        
        return true;
    }

    @Override
    public DiscreteRealSet createUnion(DiscreteRealSet otherSet) 
    {
        return otherSet;
    }
}
