package atomicJ.geometricSets;

import java.util.Collections;
import java.util.Set;

import atomicJ.utilities.Validation;

public class SingleValueDoubleSet implements Interval
{
    private final double value;

    public SingleValueDoubleSet(double value)
    {
        Validation.requireNotNaNParameterName(value, "value");

        this.value = value;
    } 

    @Override
    public double getMinimumEnd()
    {
        return this.value;
    }

    @Override
    public double getMaximumEnd() 
    {
        return this.value;
    }


    public double getValue()
    {
        return this.value;
    }

    @Override
    public double getGreatestContainedDoubleValue()
    {
        return this.value;
    }

    @Override
    public double getSmallestContainedDoubleValue()
    {
        return this.value;
    }

    @Override
    public double getClosestContainedDoubleValue(double val)
    {
        return this.value;
    }

    @Override
    public Set<Double> getIsolatedPoints()
    {
        return Collections.singleton(this.value);
    }   

    @Override
    public boolean contains(double val) 
    {
        boolean contained = (Double.compare(this.value, val) == 0);
        return contained;
    }   

    @Override
    public Interval intersect(Interval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        if(interval.contains(this.value))
        {
            return this;
        }

        return EmptyRealSet.getInstance();
    }

    @Override
    public Interval intersect(OpenInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        if(interval.contains(this.value))
        {
            return interval;
        }

        return EmptyRealSet.getInstance();
    }

    @Override
    public Interval intersect(ClosedInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        if(interval.contains(this.value))
        {
            return interval;
        }

        return EmptyRealSet.getInstance();
    }

    @Override
    public Interval intersect(LeftClosedInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        if(interval.contains(this.value))
        {
            return interval;
        }

        return EmptyRealSet.getInstance();
    }

    @Override
    public Interval intersect(RightClosedInterval interval) 
    {
        Validation.requireNonNullParameterName(interval, "interval");

        if(interval.contains(this.value))
        {
            return interval;
        }

        return EmptyRealSet.getInstance();
    }


    @Override
    public Set<Interval> getIntervalsOfSimpleSetRepresentation()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<DiscreteRealSet> getDiscreteSetsOfSimpleSetRepresentation() 
    {
        return Collections.singleton((DiscreteRealSet)this);
    }

    @Override
    public boolean isEmpty() 
    {
        return false;
    }

    @Override
    public boolean isNumberOfElementsFinite()
    {
        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = Double.hashCode(this.value);

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof SingleValueDoubleSet))
        {
            return false;
        }

        SingleValueDoubleSet that = (SingleValueDoubleSet)other;

        if(Double.compare(this.value, that.value) != 0)
        {
            return false;
        }


        return true;
    }

}
